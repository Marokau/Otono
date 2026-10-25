package server.bots;

import client.BuffStat;
import client.Character;
import client.Job;
import client.Skill;
import client.SkillFactory;
import constants.skills.Magician;
import constants.skills.Spearman;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/** Ergoth preparation and protection intents; casts stay in the ordinary support pipeline. */
final class BotGuildBossSupport {
    private BotGuildBossSupport() {}

    static boolean needsHyperBody(Character b) {
        return b.getBuffedValue(BuffStat.HYPERBODYHP) == null
                || b.getBuffedValue(BuffStat.HYPERBODYMP) == null;
    }

    static Skill criticalSkill(Character bot) {
        if (bot.getMapId() != 990000900 || !BotGuildCompanions.inInstance(bot)
                || !bot.isAlive()) return null;
        if (bot.getJob().isA(Job.MAGICIAN) && bot.getBuffedValue(BuffStat.MAGIC_GUARD) == null
                && bot.getSkillLevel(Magician.MAGIC_GUARD) > 0)
            return SkillFactory.getSkill(Magician.MAGIC_GUARD);
        int level = bot.getSkillLevel(Spearman.HYPER_BODY);
        if (level <= 0) return null;
        Skill skill = SkillFactory.getSkill(Spearman.HYPER_BODY);
        var bounds = skill.getEffect(level).calculateBoundingBox(bot.getPosition(), bot.isFacingLeft());
        if (needsHyperBody(bot)) return skill;
        for (var member : bot.getPartyMembersOnSameMap())
            if (member.isAlive() && needsHyperBody(member) && bounds.contains(member.getPosition()))
                return skill;
        return null;
    }

    static List<Character> participants(BotGuildCompanions.Crew crew) {
        var all = new ArrayList<>(crew.bots);
        if (!all.contains(crew.leader)) all.add(crew.leader);
        return all;
    }

    static Point staging(Character bot) {
        var entrance = bot.getMap().getPortal("st00").getPosition();
        Point floor = bot.getMap().getPointBelow(entrance);
        return floor == null ? entrance : floor;
    }

    static boolean prepared(BotGuildCompanions.Crew crew, Point staging) {
        for (var member : participants(crew)) {
            if (!member.isAlive() || member.getMapId() != 990000900
                    || member.getPosition().distanceSq(staging) > 120 * 120
                    || needsHyperBody(member)
                    || member.getJob().isA(Job.MAGICIAN)
                    && member.getBuffedValue(BuffStat.MAGIC_GUARD) == null) return false;
        }
        return true;
    }

    static boolean tick(BotEntry entry, Character bot, BotGuildCompanions.Crew crew,
                        boolean beforeSpawn, boolean ai) {
        // Re-evaluate both bound potion keys, including MP left at one after a deadly attack.
        bot.triggerAutopotAfterStatLoss(-1, -1);
        if (beforeSpawn) {
            var state = BotGuildRun.state(bot);
            if (state.notices.contains("ergoth-prepared")) return false;
            Point target = staging(bot);
            if (bot.getPosition().distanceSq(target) > 60 * 60) {
                BotZakumPqRun.walkTo(entry, bot, target, ai);
                return true;
            }
            if (!prepared(crew, target)) {
                if (crew.said.add("ergoth-preparation"))
                    crew.leader.dropMessage(5, "GPQ: Gather by the chamber entrance. We will wait for everyone, Hyper Body and Magic Guard before awakening Ergoth.");
                return BotOpqRun.hold(entry, bot, ai);
            }
            state.notices.add("ergoth-prepared");
            return false;
        }
        if (bot.getJob().isA(Job.MAGICIAN) && bot.getBuffedValue(BuffStat.MAGIC_GUARD) == null)
            return BotOpqRun.hold(entry, bot, ai);
        Character protector = null;
        for (var member : crew.bots)
            if (member.isAlive() && member.getMap() == bot.getMap()
                    && member.getSkillLevel(Spearman.HYPER_BODY) > 0) { protector = member; break; }
        if (protector == null) return false;
        if (bot == protector) {
            Character closest = null;
            double distance = Double.MAX_VALUE;
            for (var member : participants(crew)) {
                if (!member.isAlive() || member.getMap() != bot.getMap()
                        || member.getLevel() <= 30 || !needsHyperBody(member)) continue;
                double d = member.getPosition().distanceSq(bot.getPosition());
                if (d < distance) { closest = member; distance = d; }
            }
            if (closest == null) return false;
            int level = bot.getSkillLevel(Spearman.HYPER_BODY);
            var bounds = SkillFactory.getSkill(Spearman.HYPER_BODY).getEffect(level)
                    .calculateBoundingBox(bot.getPosition(), bot.isFacingLeft());
            if (!bounds.contains(closest.getPosition()))
                BotZakumPqRun.walkTo(entry, bot, closest.getPosition(), ai);
            else BotOpqRun.hold(entry, bot, ai);
            return true;
        }
        if (needsHyperBody(bot) && bot.getLevel() > 30) {
            if (bot.getPosition().distanceSq(protector.getPosition()) > 60 * 60)
                BotZakumPqRun.walkTo(entry, bot, protector.getPosition(), ai);
            else BotOpqRun.hold(entry, bot, ai);
            return true;
        }
        return false;
    }
}
