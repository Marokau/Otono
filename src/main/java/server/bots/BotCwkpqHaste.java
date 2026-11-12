package server.bots;

import client.BuffStat;
import client.Character;
import client.SkillFactory;
import constants.skills.Assassin;

import java.util.List;

/** Gather each party for its learned Haste before attempting the vertical courses. */
final class BotCwkpqHaste {
    private BotCwkpqHaste() {}

    static boolean gather(BotEntry entry, Character bot, List<Character> crew, boolean ai) {
        var party = bot.getParty();
        if (party == null) return false;
        var members = crew.stream().filter(b -> b.isAlive() && b.getParty() == party).toList();
        var provider = members.stream().filter(b -> b.getSkillLevel(Assassin.HASTE) > 0).findFirst().orElse(null);
        if (provider == null || members.stream().allMatch(b -> b.getBuffSource(BuffStat.JUMP) == Assassin.HASTE))
            return false;
        var portal = bot.getMap().getPortal("sp");
        var staging = bot.getMap().getPointBelow(portal.getPosition());
        if (staging == null) staging = portal.getPosition();
        if (bot.getPosition().distanceSq(staging) > 65 * 65 || entry.inAir || entry.climbing) {
            BotZakumPqRun.walkTo(entry, bot, staging, ai);
            return true;
        }
        if (ai && provider == bot && entry.attackCooldownMs <= 0
                && members.stream().allMatch(b -> b.getMap() == bot.getMap()
                && b.getPosition().distanceSq(bot.getPosition()) <= 130 * 130)) {
            var skill = SkillFactory.getSkill(Assassin.HASTE);
            int level = bot.getSkillLevel(skill);
            if (skill.getEffect(level).canPaySkillCost(bot) && BotSupportCast.dispatch(bot, skill, level))
                entry.attackCooldownMs = 800;
        }
        return BotOpqRun.hold(entry, bot, ai);
    }
}
