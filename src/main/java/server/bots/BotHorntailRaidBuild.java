package server.bots;

import client.Job;
import client.SkillFactory;
import constants.skills.*;
import server.bots.build.BuildStep;
import java.util.List;

/** Raid priorities for dedicated Horntail fixtures; SP and mastery books remain native. */
final class BotHorntailRaidBuild {
    static final String VARIANT = "horntail-raid";
    private BotHorntailRaidBuild() {}
    private static BuildStep max(int id) { return new BuildStep(id, SkillFactory.getSkill(id).getMaxLevel()); }
    static List<BuildStep> order(Job job) {

        if (job == Job.NIGHTLORD) return List.of(max(NightLord.TRIPLE_THROW), max(NightLord.MAPLE_WARRIOR),
                    max(NightLord.SHADOW_SHIFTER), max(NightLord.SHADOW_STARS), max(NightLord.VENOMOUS_STAR),
                    max(NightLord.HEROS_WILL), max(NightLord.NINJA_STORM), max(NightLord.TAUNT), max(NightLord.NINJA_AMBUSH));
        if (job == Job.BOWMASTER) return List.of(max(Bowmaster.HURRICANE), max(Bowmaster.SHARP_EYES), max(Bowmaster.BOW_EXPERT),
                    max(Bowmaster.PHOENIX), max(Bowmaster.MAPLE_WARRIOR), max(Bowmaster.HEROS_WILL),
                    max(Bowmaster.CONCENTRATE), max(Bowmaster.DRAGONS_BREATH), max(Bowmaster.HAMSTRING));
        if (job == Job.BISHOP) return List.of(max(Bishop.ANGEL_RAY), max(Bishop.HOLY_SHIELD), max(Bishop.RESURRECTION),
                    max(Bishop.MAPLE_WARRIOR), max(Bishop.GENESIS), max(Bishop.BAHAMUT), max(Bishop.HEROS_WILL),
                    max(Bishop.INFINITY), max(Bishop.BIG_BANG), max(Bishop.MANA_REFLECTION));
        if (job == Job.DARKKNIGHT) return List.of(max(DarkKnight.BEHOLDER), max(DarkKnight.BERSERK), max(DarkKnight.STANCE),
                    max(DarkKnight.ACHILLES), max(DarkKnight.MAPLE_WARRIOR), max(DarkKnight.HEROS_WILL),
                    max(DarkKnight.HEX_OF_BEHOLDER), max(DarkKnight.AURA_OF_BEHOLDER), max(DarkKnight.RUSH), max(DarkKnight.MONSTER_MAGNET));
        if (job == Job.HERO) return List.of(max(Hero.BRANDISH), max(Hero.ADVANCED_COMBO), max(Hero.STANCE), max(Hero.ACHILLES),
                    max(Hero.MAPLE_WARRIOR), max(Hero.HEROS_WILL), max(Hero.ENRAGE), max(Hero.RUSH));
        return null;
    }
}
