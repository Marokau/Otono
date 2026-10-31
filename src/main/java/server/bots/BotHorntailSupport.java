package server.bots;
import client.*;
import client.Character;
import constants.skills.Magician;
import constants.skills.Spearman;
import java.awt.Point;

/** Horntail survival priorities use normal learned support skills and bound potions. */
final class BotHorntailSupport {
    private BotHorntailSupport() {}
    static boolean missingBody(Character b){return b.getBuffedValue(BuffStat.HYPERBODYHP)==null||b.getBuffedValue(BuffStat.HYPERBODYMP)==null;}
    static Skill criticalSkill(Character b){
        if((!BotHorntailRun.inInstance(b)&&!BotPinkBeanRun.inInstance(b))||!b.isAlive())return null;
        if(b.getJob().isA(Job.MAGICIAN)&&b.getBuffedValue(BuffStat.MAGIC_GUARD)==null&&b.getSkillLevel(Magician.MAGIC_GUARD)>0)return SkillFactory.getSkill(Magician.MAGIC_GUARD);
        int level=b.getSkillLevel(Spearman.HYPER_BODY);if(level<=0)return null;
        var skill=SkillFactory.getSkill(Spearman.HYPER_BODY);
        if(missingBody(b))return skill;
        var box=skill.getEffect(level).calculateBoundingBox(b.getPosition(),b.isFacingLeft());
        for(var mate:b.getPartyMembersOnSameMap())if(mate.isAlive()&&missingBody(mate)&&box.contains(mate.getPosition()))return skill;
        return null;
    }
    static boolean protectedMember(Character b){return b.isAlive()&&!missingBody(b)&&(!b.getJob().isA(Job.MAGICIAN)||b.getBuffedValue(BuffStat.MAGIC_GUARD)!=null);}
    static boolean tick(BotEntry e,Character b,BotHorntailRun.Crew c,boolean ai){
        return tick(e,b,BotHorntailRun.all(c),c.notices,c.leader,"Horntail",ai);
    }
    static boolean tick(BotEntry e,Character b,java.util.List<Character> members,java.util.Set<String> notices,Character leader,String label,boolean ai){
        b.triggerAutopotAfterStatLoss(-1,-1);
        String key="prepared-"+b.getMapId();
        if(!notices.contains(key)){
            var portal=b.getMap().getPortal("sp");var p=portal.getPosition();var floor=b.getMap().getPointBelow(p);if(floor!=null)p=floor;
            if(b.getPosition().distanceSq(p)>60*60){BotPinkBeanMovement.walkTo(e,b,p,ai);return true;}
            for(var mate:members)if(mate.getMapId()!=b.getMapId()||!protectedMember(mate)||mate.getPosition().distanceSq(p)>150*150)return BotOpqRun.hold(e,b,ai);
            if(notices.add(key))leader.dropMessage(5,"Pink Bean".equals(label)
                    ?"Pink Bean: The bot crew is gathered with Hyper Body and Magic Guard ready. Join us when you can; we will keep moving."
                    :label+": Everyone is here with Hyper Body and Magic Guard ready. Stay clear of dangerous body contact.");
        }
        if(b.getJob().isA(Job.MAGICIAN)&&b.getBuffedValue(BuffStat.MAGIC_GUARD)==null)return BotOpqRun.hold(e,b,ai);
        // A moving caster can enter/leave Heal range during the same rope arc.
        // Keep the active approach until landing instead of selecting a different
        // support stance every time that transient range check changes.
        if(BotHorntailRun.inInstance(b)&&(e.inAir||e.climbing)&&e.moveTarget!=null
                &&!b.getMap().getAllMonsters().isEmpty()){
            BotPinkBeanMovement.walkTo(e,b,new Point(e.moveTarget),ai);return true;
        }
        if(BotHorntailRun.inInstance(b)&&b.getJob()==Job.BISHOP&&!b.hasDisease(Disease.SEAL)&&!b.hasDisease(Disease.STUN)) {
            int level=b.getSkillLevel(constants.skills.Cleric.HEAL);
            if(level>0)for(var mate:b.getPartyMembersOnSameMap())
                if(mate.isAlive()&&mate.hasDisease(Disease.SEDUCE)&&mate.getHp()<mate.getCurrentMaxHp()*0.9) {
                    var heal=SkillFactory.getSkill(constants.skills.Cleric.HEAL).getEffect(level);
                    if(heal.calculateBoundingBox(b.getPosition(),b.isFacingLeft()).contains(mate.getPosition())) {
                        BotCombatManager.tickSupportHealing(e,b);return BotOpqRun.hold(e,b,ai);
                    }
                    var stance=BotHorntailRevive.stance(e,b,mate.getPosition(),heal);
                    if(stance!=null){BotPinkBeanMovement.walkTo(e,b,stance,ai);return true;}
                }
        }
        if(BotHorntailRun.inInstance(b)&&b.getJob()==Job.BISHOP) {
            int level=b.getSkillLevel(constants.skills.Cleric.HEAL);
            Character anchor=BotHorntailHealingAnchor.find(b);
            if(anchor!=null) {
                var heal=SkillFactory.getSkill(constants.skills.Cleric.HEAL).getEffect(level);
                if(!heal.calculateBoundingBox(b.getPosition(),b.isFacingLeft()).contains(anchor.getPosition())) {
                    var stance=BotHorntailRevive.stance(e,b,anchor.getPosition(),heal);
                    if(stance!=null){BotPinkBeanMovement.walkTo(e,b,stance,ai);return true;}
                }
            }
        }
        Character provider=null;
        for(var mate:b.getPartyMembersOnSameMap())if(mate.isAlive()&&!BotPinkBeanParticipation.human(mate)&&mate.getSkillLevel(Spearman.HYPER_BODY)>0){provider=mate;break;}
        if(provider==null)return false;
        if(provider==b){
            for(var mate:b.getPartyMembersOnSameMap())if(mate.isAlive()&&missingBody(mate)){
                int lv=b.getSkillLevel(Spearman.HYPER_BODY);var bounds=SkillFactory.getSkill(Spearman.HYPER_BODY).getEffect(lv).calculateBoundingBox(b.getPosition(),b.isFacingLeft());
                if(bounds.contains(mate.getPosition()))return BotOpqRun.hold(e,b,ai);
                if(BotPinkBeanParticipation.human(mate))continue;
                Point meeting=mate.getPosition();
                if(BotHorntailRun.inInstance(b)||BotPinkBeanRun.inInstance(b)) {
                    var effect=SkillFactory.getSkill(Spearman.HYPER_BODY).getEffect(lv);
                    var stance=BotHorntailRevive.stance(e,b,mate.getPosition(),effect);
                    if(stance!=null){BotPinkBeanMovement.walkTo(e,b,stance,ai);return true;}
                }
                if(BotHorntailRun.inInstance(b)&&!BotHorntailCombat.safe(b,meeting,b.getMap().getAllMonsters()))
                    meeting=b.getMap().getPortal("sp").getPosition();
                BotPinkBeanMovement.walkTo(e,b,meeting,ai);return true;
            }
        }else if(missingBody(b)){
            // In Pink's split-height statue fight, let the caster approach the
            // unprotected member's safe position instead of both chasing each
            // other across lethal bodies after Dispel. Once the final formation
            // is active, regroup with its anchored caster instead of waiting below.
            if(BotPinkBeanRun.inInstance(b)&&!BotPinkBeanFormation.required(b)
                    &&BotHorntailCombat.safe(b,b.getPosition(),b.getMap().getAllMonsters()))
                return BotOpqRun.hold(e,b,ai);
            Point meeting=provider.getPosition();
            if(BotHorntailRun.inInstance(b)&&!BotHorntailCombat.safe(b,meeting,b.getMap().getAllMonsters()))
                meeting=b.getMap().getPortal("sp").getPosition();
            if(b.getPosition().distanceSq(meeting)>60*60)BotPinkBeanMovement.walkTo(e,b,meeting,ai);else BotOpqRun.hold(e,b,ai);
            return true;
        }
        return false;
    }
}
