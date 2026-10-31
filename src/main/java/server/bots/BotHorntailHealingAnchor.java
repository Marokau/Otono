package server.bots;
import client.Character;
import client.Job;
import client.SkillFactory;
import constants.skills.Cleric;
import java.awt.Point;

/** One healing anchor shared by Horntail support and combat positioning. */
final class BotHorntailHealingAnchor {
    private BotHorntailHealingAnchor() {}
    static Character find(Character bishop) {
        if (!BotHorntailRun.inInstance(bishop) || bishop.getJob()!=Job.BISHOP
                || bishop.getSkillLevel(Cleric.HEAL)<=0) return null;
        Character anchor=null;
        for (var mate:bishop.getPartyMembersOnSameMap())
            if (mate.isAlive()&&(mate.getJob()==Job.BOWMASTER||mate.getJob()==Job.NIGHTLORD)) {
                var fighter=BotManager.getInstance().getEntryByBotCharId(mate.getId());
                if(fighter!=null&&fighter.grindTarget!=null&&fighter.grindTarget.isAlive()
                        &&(anchor==null||mate.getCurrentMaxHp()<anchor.getCurrentMaxHp()))anchor=mate;
            }
        return anchor;
    }
    static boolean covers(Character bishop,Point stance) {
        var anchor=find(bishop);
        return anchor==null||SkillFactory.getSkill(Cleric.HEAL).getEffect(bishop.getSkillLevel(Cleric.HEAL))
                .calculateBoundingBox(stance,bishop.isFacingLeft()).contains(anchor.getPosition());
    }
}
