package server.bots;

import client.BotClient;
import client.Character;
import io.netty.buffer.Unpooled;
import net.packet.ByteBufInPacket;
import net.server.channel.handlers.MonsterCarnivalHandler;
import server.ItemInformationProvider;
import server.life.Monster;
import server.maps.MapItem;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Dedicated competitors; scoring, summons, pickups and rewards stay in player handlers. */
public final class BotCarnivalRun {
    public static final int RECRUIT = 980000000;
    static final Map<Character, Crew> CREWS = Collections.synchronizedMap(new WeakHashMap<>());
    static final class Crew {
        Character leader, opponent;
        List<Character> bots;
        long lastSpend;
        int field = -1;
        boolean started;
    }
    private BotCarnivalRun() {}
    public static boolean inInstance(Character bot) { return bot != null && bot.getMonsterCarnival() != null; }
    static boolean registered(Character bot) { return CREWS.containsKey(bot); }
    static boolean belongsTo(Character bot, int owner) {
        var c = CREWS.get(bot); return c != null && c.leader.getId() == owner;
    }
    static void register(Character leader, Character opponent, List<Character> bots) {
        var c = new Crew(); c.leader = leader; c.opponent = opponent; c.bots = List.copyOf(bots);
        for (var b : bots) {
            CREWS.put(b, c);
            var e = BotManager.getInstance().getEntryByBotCharId(b.getId());
            BotManager.getInstance().resumeFromOperatorCommand(e); BotManager.getInstance().issueStop(e);
            BotPqProvisioner.provision(b); disableCarriedPotions(b);
        }
    }
    /** A summoned monster belongs to its summoner's team and is fought by the other team. */
    static boolean canAttack(Character bot, Monster mob) {
        return !inInstance(bot) || mob.getTeam() < 0 || mob.getTeam() != bot.getTeam();
    }
    static void disableCarriedPotions(Character bot) {
        bot.getKeymap().remove(91); bot.getKeymap().remove(92);
        bot.setAutopotHpAlert(0); bot.setAutopotMpAlert(0);
    }
    public static boolean tick(BotEntry e, Character b, boolean ai) {
        var c = CREWS.get(b); if (c == null) return false;
        e.grinding = false;
        boolean attached = c.leader.getWorldServer().getPlayerStorage().getCharacterById(c.leader.getId()) == c.leader
                && c.leader.getClient().getChannel() == b.getClient().getChannel()
                && c.leader.getParty() != null && c.leader.getParty().getLeaderId() == c.leader.getId()
                && c.bots.getFirst().getParty() != null
                && c.leader.getParty().getId() == c.bots.getFirst().getParty().getId();
        if (!(c.leader.getClient() instanceof client.BotClient) && attached) {
            String guide = inInstance(b) ? b.getMap().isCPQMap() ? "cpq-fight" : "cpq-recovery"
                    : b.getMap().isCPQWinnerMap() || b.getMap().isCPQLoserMap() || (b.getMapId()==980000010||b.getMapId()==980030010) ? "cpq-rewards" : null;
            if (guide!=null && BotPqStageGuide.tick(e,b,c.leader,c,guide,System.currentTimeMillis()))return BotOpqRun.hold(e,b,ai);
        }
        if (inInstance(b)) {
            disableCarriedPotions(b);
            if (b.getMap().isCPQMap()) {
                if (ai) spend(c, b);
                var drop = recovery(b);
                if (drop != null) { BotZakumPqRun.stepTowardsAndPickUp(e, b, drop, ai); return true; }
                var guardian = b.getMap().getAllReactors().stream()
                        .filter(r -> r.isActive() && r.getName() != null && r.getName().startsWith((1-b.getTeam()) + ""))
                        .min(Comparator.comparingDouble(r -> r.getPosition().distanceSq(b.getPosition()))).orElse(null);
                if (guardian != null) {
                    if (guardian.getPosition().distanceSq(b.getPosition()) < 70*70 && !e.inAir && !e.climbing) {
                        BotOpqRun.hold(e,b,ai);
                        if (e.attackCooldownMs <= 0) { BotZakumPqRun.hitReactor(b,guardian); e.attackCooldownMs = 700; }
                    } else BotZakumPqRun.walkTo(e,b,guardian.getPosition(),ai);
                    return true;
                }
                e.grinding = true;
                BotGrindDoctrine.clear(e,b);
                BotManager.getInstance().tickPqGrinding(e,b,ai); return true;
            }
            // Standard death popup respawns in this field's recovery room; use its real portal.
            var portal = b.getMap().getPortals().stream().filter(p -> p.getScriptName() != null
                    && p.getScriptName().startsWith("MCrevive")).findFirst().orElse(null);
            if (portal != null) BotTravelManager.walkToPortalAndEnter(e,b,portal,System.currentTimeMillis(),ai);
            else BotOpqRun.hold(e,b,ai);
            return true;
        }
        if (b.getMap().isCPQWinnerMap() || b.getMap().isCPQLoserMap() || (b.getMapId() == 980000010 || b.getMapId() == 980030010)) {
            if (ai) {
                if (b.getClient().getCM() != null) BotNpcInteraction.next(b,0);
                else BotNpcInteraction.talk(b,BotCarnivalFormat.resultNpc(c.opponent));
            }
            return BotOpqRun.hold(e,b,ai);
        }
        synchronized (c) {
            if (b == c.opponent && b.getClient().getCM() != null && b.isChallenged() && ai)
                BotNpcInteraction.next(b,0);
        }
        if (b.getMapId() == BotCarnivalFormat.recruit(c.opponent) && c.started && !inInstance(c.leader)
                && c.bots.stream().allMatch(p -> p.getMapId() == BotCarnivalFormat.recruit(p) && !inInstance(p))) {
            c.started = false; c.field = -1;
            BotPqStageGuide.reset(c);
        }
        if (b.getMapId() == BotCarnivalFormat.recruit(c.opponent) && !attached) {
            BotCarnivalCompanions.dismiss(b); return true;
        }
        return BotOpqRun.hold(e,b,ai);
    }
    static MapItem recovery(Character b) {
        var ii = ItemInformationProvider.getInstance();
        return b.getMap().getMapObjects().stream().filter(o -> o instanceof MapItem).map(o -> (MapItem)o).filter(d -> !d.isPickedUp() && d.canBePickedBy(b)
                && d.getMeso() == 0 && d.getItem() != null && d.getItemId() > 0 && ii.isConsumeOnPickup(d.getItemId()))
                .filter(d -> {
                    var fx = ii.getItemEffect(d.getItemId());
                    return b.getHp() < b.getCurrentMaxHp() * .8 && (fx.getHp() > 0 || fx.getHpRate() > 0)
                            || b.getMp() < b.getCurrentMaxMp() * .6 && (fx.getMp() > 0 || fx.getMpRate() > 0)
                            || b.getDiseasesSize() > 0;
                }).min(Comparator.comparingDouble(d -> d.getPosition().distanceSq(b.getPosition()))).orElse(null);
    }
    static void spend(Crew c, Character b) {
        if (System.currentTimeMillis() < c.lastSpend) return;
        c.lastSpend = System.currentTimeMillis() + ThreadLocalRandom.current().nextLong(4000,7000);
        var game = b.getMonsterCarnival();
        var mobs = b.getMap().getMobsToSpawn();
        int tab = 0, choice = -1;
        if (b.getTeam() == 0 ? game.canSummonR() : game.canSummonB()) {
            for (int i=0;i<mobs.size();i++) if (mobs.get(i).right <= b.getCP()) { choice=i; break; }
        } else if ((b.getTeam() == 0 ? game.canGuardianR() : game.canGuardianB())
                && ThreadLocalRandom.current().nextInt(3) == 0) {
            for (int i=0;i<9;i++) {
                var guardian = server.partyquest.CarnivalFactory.getInstance().getGuardian(i);
                boolean exists = false;
                for (var r : b.getMap().getAllReactors())
                    if (r.isActive() && (b.getTeam() + "" + i).equals(r.getName())) { exists=true; break; }
                if (!exists && guardian != null && guardian.cpLoss() <= b.getCP()) { tab=2; choice=i; break; }
            }
        } else {
            var skills = b.getMap().getSkillIds();
            for (int i=0;i<skills.size();i++) {
                var skill = server.partyquest.CarnivalFactory.getInstance().getSkill(skills.get(i));
                if (skill != null && skill.cpLoss() <= b.getCP()) { tab=1; choice=i; break; }
            }
        }
        if (choice >= 0) {
            var p=Unpooled.buffer().writeByte(tab).writeByte(choice);
            try { new MonsterCarnivalHandler().handlePacket(new ByteBufInPacket(p),b.getClient()); }
            finally { p.release(); }
        }
    }
}
