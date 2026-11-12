package server.bots;

import client.Character;
import client.SkillFactory;
import constants.skills.DragonKnight;
import constants.skills.Hermit;
import constants.skills.Magician;
import constants.skills.Pirate;
import constants.skills.Ranger;
import io.netty.buffer.Unpooled;
import net.packet.ByteBufInPacket;
import net.server.channel.handlers.AbstractDealDamageHandler;
import net.server.channel.handlers.ReactorHitHandler;
import scripting.event.EventInstanceManager;
import server.maps.Reactor;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class BotCwkpqStages {
    private static final int SIGIL_HALL = 610030200;
    private static final int MENHIRS = 610030300;
    private static final int RANDOM_SIGILS = 610030400;
    private static final int STATUE = 610030500;
    private static final int[] SIGIL_SKILLS = {
            DragonKnight.SPEAR_DRAGON_FURY, Ranger.STRAFE, Magician.MAGIC_CLAW,
            Hermit.AVENGER, Pirate.FLASH_FIST
    };

    static final class State {
        final Set<Integer> announced = ConcurrentHashMap.newKeySet();
        final Set<Integer> crossedMenhirs = ConcurrentHashMap.newKeySet();

        void reset() {
            announced.clear();
            crossedMenhirs.clear();
        }
    }

    private BotCwkpqStages() {}

    static boolean tick(BotEntry entry, Character bot, State state, List<Character> crew,
                        Character leader, EventInstanceManager event, boolean ai) {
        int map = bot.getMapId();
        if ((map == MENHIRS && event.getIntProperty("glpq3_p") < 5
                || map == RANDOM_SIGILS && event.getIntProperty("glpq4") < 5)
                && BotCwkpqHaste.gather(entry, bot, crew, ai)) return true;
        return switch (map) {
            case SIGIL_HALL -> sigilHall(entry, bot, state, event, ai);
            case MENHIRS -> menhirs(entry, bot, state, event, ai);
            case RANDOM_SIGILS -> randomSigils(entry, bot, state, event, ai);
            default -> BotCwkpqRelics.tick(entry, bot, state, crew, leader, event, ai);
        };
    }

    private static boolean sigilHall(BotEntry entry, Character bot, State state,
                                     EventInstanceManager event, boolean ai) {
        announce(entry, state, SIGIL_HALL,
                "Stage Two: We will use a skill from each class to open the sigils. The right portal opens after all five.");
        if (event.getIntProperty("glpq2") >= 5) return BotMagatiaRun.portal(entry, bot, "next00", ai);
        return sigil(entry, bot, 6109000, 0, ai);
    }

    private static boolean menhirs(BotEntry entry, Character bot, State state,
                                    EventInstanceManager event, boolean ai) {
        announce(entry, state, MENHIRS,
                "Stage Three: We will activate the sigils and send five companions through the top portal. Then use the bottom exit.");
        if (event.getIntProperty("glpq3") >= 5 && event.getIntProperty("glpq3_p") >= 5)
            return BotMagatiaRun.portal(entry, bot, "next01", ai);
        var reactor = findSigil(bot, 6109000, 0);
        if (reactor != null) return approachSigil(entry, bot, reactor, ai);
        if (event.getIntProperty("glpq3_p") < 5 && !state.crossedMenhirs.contains(bot.getId())) {
            synchronized (state) {
                int crossed = event.getIntProperty("glpq3_p");
                if (crossed < 5) {
                    BotMagatiaRun.portal(entry, bot, "next00", ai);
                    if (event.getIntProperty("glpq3_p") > crossed) state.crossedMenhirs.add(bot.getId());
                }
            }
            return true;
        }
        return BotOpqRun.hold(entry, bot, ai);
    }

    private static boolean randomSigils(BotEntry entry, Character bot, State state,
                                        EventInstanceManager event, boolean ai) {
        announce(entry, state, RANDOM_SIGILS,
                "Stage Four: We will climb to the active class sigils. When all five light up, take the bottom portal.");
        if (event.getIntProperty("glpq4") >= 5) return BotMagatiaRun.portal(entry, bot, "next00", ai);
        return sigil(entry, bot, 6109016, 1, ai);
    }

    private static void announce(BotEntry entry, State state, int map, String message) {
        if (state.announced.add(map)) BotPqChat.queue(entry, message, false);
    }

    private static boolean sigil(BotEntry entry, Character bot, int first, int state, boolean ai) {
        var reactor = findSigil(bot, first, state);
        return reactor == null ? BotOpqRun.hold(entry, bot, ai) : approachSigil(entry, bot, reactor, ai);
    }

    private static int familyIndex(Character bot) {
        return switch (bot.getJob().getJobNiche()) {
            case 1 -> 0;
            case 2 -> 2;
            case 3 -> 1;
            case 4 -> 3;
            case 5 -> 4;
            default -> -1;
        };
    }

    private static Reactor findSigil(Character bot, int first, int state) {
        int family = familyIndex(bot);
        if (family < 0) return null;
        int id = first + family;
        return bot.getMap().getReactorsByIdRange(id, id).stream()
                .filter(r -> r.isActive() && r.getState() == state).findFirst().orElse(null);
    }

    private static boolean approachSigil(BotEntry entry, Character bot, Reactor reactor, boolean ai) {
        Point target = bot.getMap().getPointBelow(reactor.getPosition());
        if (target == null) return BotOpqRun.hold(entry, bot, ai);
        Point position = bot.getPosition();
        if (Math.abs(position.x - target.x) > 65 || Math.abs(position.y - target.y) > 45
                || entry.climbing || entry.inAir) {
            BotZakumPqRun.walkTo(entry, bot, target, ai);
            return true;
        }
        if (ai && entry.attackCooldownMs <= 0) {
            synchronized (bot.getEventInstance()) {
                int expectedState = bot.getMapId() == RANDOM_SIGILS ? 1 : 0;
                if (reactor.isActive() && reactor.getState() == expectedState)
                    strike(entry, bot, reactor, SIGIL_SKILLS[familyIndex(bot)]);
            }
        }
        return BotOpqRun.hold(entry, bot, ai);
    }

    private static void strike(BotEntry entry, Character bot, Reactor reactor, int skillId) {
        var skill = SkillFactory.getSkill(skillId);
        int level = bot.getSkillLevel(skill);
        if (level <= 0 || !skill.getEffect(level).canPaySkillCost(bot)) return;
        var weapon = BotAttackExecutionProvider.getEquippedWeaponType(bot);
        if (!BotCombatManager.canUseAttackSkillWithWeapon(skillId, weapon)) return;
        var route = BotAttackExecutionProvider.determineSkillRoute(bot, skillId);
        var effect = skill.getEffect(level);
        int ammo = Math.max(effect.getBulletCount(), effect.getBulletConsume());
        if (bot.getBuffedValue(client.BuffStat.SHADOWPARTNER) != null) ammo *= 2;
        if (route == BotCombatManager.AttackRoute.RANGED && BotCombatManager.countAmmo(bot, weapon) < ammo)
            return;
        boolean left = reactor.getPosition().x < bot.getPosition().x;
        var base = BotAttackExecutionProvider.buildBasicAttackData(bot,
                new Point(bot.getPosition().x + (left ? -1 : 1), bot.getPosition().y));
        String action = BotAttackExecutionProvider.resolveSkillAttackAction(bot, skill, level, weapon);
        var timing = BotAttackExecutionProvider.resolveSkillAttackTiming(skill, action, bot, base);
        var attack = new AbstractDealDamageHandler.AttackInfo();
        attack.skill = skillId;
        attack.skilllevel = level;
        attack.speed = base.speed();
        attack.stance = BotAttackExecutionProvider.attackPacketStance(left);
        attack.direction = BotAttackExecutionProvider.bodyActionId(action, null, weapon);
        attack.rangedirection = attack.direction;
        if (route == BotCombatManager.AttackRoute.CLOSE) {
            var fields = BotAttackExecutionProvider.mimicCloseRangePacketFields(action, null, left);
            attack.direction = fields.bodyActionId();
            attack.display = fields.display();
        }
        attack.numDamage = skill.getEffect(level).getAttackCount();
        attack.numAttackedAndDamage = attack.numDamage;
        attack.targets = new HashMap<>();
        attack.magic = route == BotCombatManager.AttackRoute.MAGIC;
        attack.ranged = route == BotCombatManager.AttackRoute.RANGED;
        BotAttackExecutionProvider.applyAttackRoute(route, attack, bot);
        entry.attackCooldownMs = Math.max(entry.attackCooldownMs, timing.cooldownMs());
        entry.facingDir = left ? -1 : 1;
        short stance = (short) (left ? 2 : 3);
        var packet = Unpooled.buffer().writeIntLE(reactor.getObjectId()).writeIntLE(left ? 0 : 1)
                .writeShortLE(stance).writeIntLE(0).writeIntLE(skillId);
        try {
            new ReactorHitHandler().handlePacket(new ByteBufInPacket(packet), bot.getClient());
        } finally {
            packet.release();
        }
    }
}
