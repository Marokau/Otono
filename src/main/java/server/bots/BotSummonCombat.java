package server.bots;

import client.Character;
import client.Disease;
import client.SkillFactory;
import client.status.MonsterStatus;
import constants.skills.Bishop;
import constants.skills.Bowmaster;
import io.netty.buffer.Unpooled;
import net.packet.ByteBufInPacket;
import net.packet.ByteBufOutPacket;
import net.server.channel.handlers.MoveSummonHandler;
import net.server.channel.handlers.SummonDamageHandler;
import provider.DataProviderFactory;
import provider.DataTool;
import provider.wz.WZFiles;
import server.StatEffect;
import server.bots.combat.BotMobHitboxProvider;
import server.combat.CombatFormulaProvider;
import server.life.Monster;
import server.maps.Summon;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Headless client adapter for the Zakum crew's Phoenix and Bahamut. */
final class BotSummonCombat {
    record Spec(Rectangle box, int radius, Point origin, int targets, int hitDelay) {}
    private static final Map<Integer, Spec> SPECS = new ConcurrentHashMap<>();
    private static final Map<Summon, Long> NEXT = Collections.synchronizedMap(new WeakHashMap<>());
    // Original pre-BB packet measurements: southperry.net/showthread.php?tid=3088.
    static final int ATTACK_MS = 3030;
    private BotSummonCombat() {}

    static boolean supported(int id) { return id == Bowmaster.PHOENIX || id == Bishop.BAHAMUT; }

    static Spec spec(int id) {
        return SPECS.computeIfAbsent(id, key -> {
            var data = DataProviderFactory.getDataProvider(WZFiles.SKILL).getData(key / 10000 + ".img");
            var info = data.getChildByPath("skill/" + key + "/summon/attack1/info");
            Point lt = DataTool.getPoint("range/lt", info), rb = DataTool.getPoint("range/rb", info);
            return new Spec(new Rectangle(lt.x, lt.y, rb.x - lt.x + 1, rb.y - lt.y + 1),
                    DataTool.getInt("range/r", info, 0), DataTool.getPoint("range/sp", info, new Point()),
                    DataTool.getInt("mobCount", info, 1), DataTool.getInt("attackAfter", info, 0));
        });
    }

    static boolean canCast(BotEntry entry, Character bot, long now) {
        return bot.isAlive() && !entry.inAir && !entry.climbing && entry.attackCooldownMs <= 0
                && entry.skillBuffsEnabled && !bot.hasDisease(Disease.STUN) && !bot.hasDisease(Disease.SEAL);
    }

    static void tick(BotEntry entry, Character bot) {
        if ((!BotZakumBattle.inInstance(bot) && !(BotMvpqRun.inInstance(bot) || (BotHorntailRun.inInstance(bot) || BotPinkBeanRun.inInstance(bot)))) || !bot.isAlive()) return;
        long now = System.currentTimeMillis();
        // Summons continue independently while their owner is stunned or sealed.
        for (var summon : List.copyOf(bot.getSummonsValues())) {
            int id = summon.getSkill();
            if (!supported(id)) continue;
            if(!BotPinkBeanSummons.allowed(bot,id)){
                bot.cancelEffect(SkillFactory.getSkill(id).getEffect(summon.getSkillLevel()),false,-1);
                continue;
            }
            follow(bot, summon);
            if (now < NEXT.getOrDefault(summon, 0L)) continue;
            var fx = SkillFactory.getSkill(id).getEffect(summon.getSkillLevel());
            Spec spec = spec(id);
            List<Monster> targets = targets(bot, summon, spec, fx);
            if(!BotHorntailTactics.targets(bot,targets).containsAll(targets)){
                bot.cancelEffect(SkillFactory.getSkill(id).getEffect(summon.getSkillLevel()),false,-1);
                continue;
            }
            if (targets.isEmpty()) continue;
            NEXT.put(summon, now + ATTACK_MS);
            var packet = new ByteBufOutPacket();
            packet.writeInt(summon.getObjectId());
            packet.writeInt(net.server.Server.getInstance().getCurrentTimestamp());
            // This byte packs attack action AND facing. PacketCreator's real summon
            // capture uses 04 for attack1; 00/01 are idle actions, not attacks.
            packet.writeByte(4 | (BotMobHitboxProvider.getInstance().getMobBounds(targets.getFirst()).getCenterX()
                    < summon.getPosition().x ? 1 : 0));
            packet.writeByte(targets.size());
            packet.writeBytes(new byte[8]);
            for (var mob : targets) {
                packet.writeInt(mob.getObjectId());
                packet.writeBytes(new byte[8]);
                packet.writePos(mob.getPosition()); packet.writePos(mob.getPosition());
                packet.writeShort(spec.hitDelay);
                packet.writeInt(damage(bot, mob, id, fx));
            }
            var buffer = Unpooled.wrappedBuffer(packet.getBytes());
            try { new SummonDamageHandler().handlePacket(new ByteBufInPacket(buffer), bot.getClient()); }
            finally { buffer.release(); }
        }
    }

    static void follow(Character bot, Summon summon) {
        Point position = bot.getPosition();
        if (position.equals(summon.getPosition())) return;
        var packet = new ByteBufOutPacket();
        packet.writeInt(summon.getObjectId()); packet.writePos(summon.getPosition());
        packet.writeByte(1); packet.writeByte(0); // one absolute movement fragment
        packet.writePos(position); packet.writeShort(0); packet.writeShort(0); packet.writeShort(0);
        packet.writeByte(bot.isFacingLeft() ? 1 : 0); packet.writeShort(250);
        var buffer = Unpooled.wrappedBuffer(packet.getBytes());
        try { new MoveSummonHandler().handlePacket(new ByteBufInPacket(buffer), bot.getClient()); }
        finally { buffer.release(); }
    }

    static List<Monster> targets(Character bot, Summon summon, Spec spec, StatEffect fx) {
        boolean magic = summon.getSkill() == Bishop.BAHAMUT;
        var candidates = new ArrayList<Monster>();
        for (var mob : bot.getMap().getAllMonsters()) {
            if (!BotHorntailTargets.allowed(bot,mob) || !BotZakumArmLanes.canTarget(bot,mob) || !mob.isAlive() || mob.isFake() || mob.isBuffed(magic ? MonsterStatus.MAGIC_IMMUNITY : MonsterStatus.WEAPON_IMMUNITY)) continue;
            Rectangle bounds = BotMobHitboxProvider.getInstance().getMobBounds(mob);
            if (bounds != null && reachable(spec, summon.getPosition(), bounds)) candidates.add(mob);
        }
        candidates.sort(Comparator.comparingDouble((Monster mob) -> mob.getPosition().distanceSq(summon.getPosition()))
                .thenComparingInt(Monster::getObjectId));
        if (candidates.isEmpty()) return List.of();
        // Phoenix selects a target in its circle, then splashes only the local WZ hit box.
        if (spec.radius > 0) {
            // Zakum's separated arms share a monster origin; their hit rectangles
            // are offset from it. Aim at the actual body rectangle, not that origin.
            Rectangle primary = BotMobHitboxProvider.getInstance().getMobBounds(candidates.getFirst());
            Point center = new Point((int)primary.getCenterX(), (int)primary.getCenterY());
            Rectangle splash = new Rectangle(spec.box); splash.translate(center.x, center.y);
            candidates.removeIf(mob -> !splash.intersects(BotMobHitboxProvider.getInstance().getMobBounds(mob)));
        } else {
            boolean facingLeft = BotMobHitboxProvider.getInstance().getMobBounds(candidates.getFirst()).getCenterX() < summon.getPosition().x;
            Rectangle area = new Rectangle(spec.box);
            if (!facingLeft) area.x = -area.x - area.width;
            area.translate(summon.getPosition().x, summon.getPosition().y);
            candidates.removeIf(mob -> !area.intersects(BotMobHitboxProvider.getInstance().getMobBounds(mob)));
        }
        int limit = spec.radius > 0 ? spec.targets : Math.min(spec.targets, Math.max(1, fx.getMobCount()));
        return candidates.subList(0, Math.min(candidates.size(), limit));
    }

    static boolean reachable(Spec spec, Point position, Rectangle bounds) {
        if (spec.radius > 0) {
            double x = position.x + (bounds.getCenterX() < position.x ? spec.origin.x : -spec.origin.x);
            double y = position.y + spec.origin.y;
            double dx = Math.max(bounds.x - x, Math.max(0, x - bounds.getMaxX()));
            double dy = Math.max(bounds.y - y, Math.max(0, y - bounds.getMaxY()));
            return dx * dx + dy * dy <= (double) spec.radius * spec.radius;
        }
        Rectangle left = new Rectangle(spec.box); left.translate(position.x, position.y);
        Rectangle right = new Rectangle(position.x - spec.box.x - spec.box.width, left.y, left.width, left.height);
        return left.intersects(bounds) || right.intersects(bounds);
    }

    static int damage(Character bot, Monster mob, int id, StatEffect fx) {
        var formula = CombatFormulaProvider.getInstance();
        int low, high;
        boolean magic = id == Bishop.BAHAMUT;
        if (magic) {
            // Reuse the ordinary spell base; summons have their own WZ mastery and no Amplification.
            double mastery = Math.min(1, 0.1 + fx.getMastery() / 20.0);
            low = (int) (formula.magicDamageBaseMin(bot.getTotalMagic(), bot.getTotalInt(), mastery) * fx.getMatk());
            high = (int) (formula.magicDamageBase(bot.getTotalMagic(), bot.getTotalInt()) * fx.getMatk());
            return formula.makeTarget(bot, mob, 1, id,
                    new CombatFormulaProvider.DamageProfile(low, high, true, false, true), 0)
                    .damageLines().getFirst();
        } else {
            // Pre-BB Phoenix ignores weapon attack/defense: southperry.net/showthread.php?tid=1033.
            low = (int) ((bot.getTotalDex() * 2.5 * 0.7 + bot.getTotalStr()) * fx.getWatk() / 100.0);
            high = (int) ((bot.getTotalDex() * 2.5 + bot.getTotalStr()) * fx.getWatk() / 100.0);
        }
        low = (int) formula.applySkillElementalMultiplier(low, id, mob, false);
        high = (int) formula.applySkillElementalMultiplier(high, id, mob, false);
        double levelFactor = Math.max(0, 1 - 0.01 * Math.max(0, mob.getLevel() - bot.getLevel()));
        low = (int) (low * levelFactor); high = (int) (high * levelFactor);
        return formula.rollDamageLines(bot, mob, 1, Math.max(1, low), Math.max(1, high), magic).getFirst();
    }
}
