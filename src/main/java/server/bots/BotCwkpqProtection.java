package server.bots;

import client.BuffStat;
import client.Character;
import client.SkillFactory;
import constants.skills.Magician;
import constants.skills.Spearman;

/** Refresh the party's native protection after Dispel or before Jack starts a wave. */
final class BotCwkpqProtection {
    private BotCwkpqProtection() {}

    static boolean cast(BotEntry entry, Character bot, boolean ai) {
        if (!ai || !bot.isAlive() || entry.attackCooldownMs > 0) return false;
        int skillId = 0;
        if (bot.getSkillLevel(Magician.MAGIC_GUARD) > 0 && bot.getBuffedValue(BuffStat.MAGIC_GUARD) == null) {
            skillId = Magician.MAGIC_GUARD;
        } else {
            int level = bot.getSkillLevel(Spearman.HYPER_BODY);
            if (level > 0) {
                var effect = SkillFactory.getSkill(Spearman.HYPER_BODY).getEffect(level);
                var area = effect.calculateBoundingBox(bot.getPosition(), bot.isFacingLeft());
                if (BotGuildBossSupport.needsHyperBody(bot) || bot.getPartyMembersOnSameMap().stream()
                        .anyMatch(b -> b.isAlive() && BotGuildBossSupport.needsHyperBody(b) && area.contains(b.getPosition())))
                    skillId = Spearman.HYPER_BODY;
            }
        }
        if (skillId == 0) return false;
        var skill = SkillFactory.getSkill(skillId);
        int level = bot.getSkillLevel(skill);
        if (!skill.getEffect(level).canPaySkillCost(bot) || !BotSupportCast.dispatch(bot, skill, level))
            return false;
        entry.attackCooldownMs = 800;
        return true;
    }
}
