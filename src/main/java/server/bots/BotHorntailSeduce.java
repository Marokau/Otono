package server.bots;
import client.Character;
import client.Disease;
import java.awt.Point;

/** Headless forced inputs for Horntail's native Seduce values (left, right, jump). */
final class BotHorntailSeduce {
    private BotHorntailSeduce() {}
    static boolean canUsePotions(Character b){return (!BotHorntailRun.inInstance(b)&&!BotPinkBeanRun.inInstance(b))||!b.hasDisease(Disease.SEDUCE);}
    static int direction(int mode){return mode==1?-1:mode==2?1:0;}
    static void tick(BotEntry e,Character b){
        if(b.hasDisease(Disease.STUN)){BotManager.getInstance().tickStunned(e);return;}
        var active=b.getAllDiseases().get(Disease.SEDUCE);
        if((BotHorntailRun.inInstance(b)||BotPinkBeanRun.inInstance(b))&&active!=null&&active.getRight().getDuration()-active.getLeft()>=500
                &&e.attackCooldownMs<=0&&!b.hasDisease(Disease.SEAL)) {
            var job=b.getJob();
            int will=job==client.Job.HERO?constants.skills.Hero.HEROS_WILL
                    :job==client.Job.DARKKNIGHT?constants.skills.DarkKnight.HEROS_WILL
                    :job==client.Job.BISHOP?constants.skills.Bishop.HEROS_WILL
                    :job==client.Job.BOWMASTER?constants.skills.Bowmaster.HEROS_WILL
                    :job==client.Job.NIGHTLORD?constants.skills.NightLord.HEROS_WILL:0;
            if(will!=0&&b.getSkillLevel(will)>0&&!b.skillIsCooling(will)) {
                var skill=client.SkillFactory.getSkill(will);int level=b.getSkillLevel(will);
                if(skill.getEffect(level).canPaySkillCost(b)&&BotSupportCast.dispatch(b,skill,level)) {
                    e.attackCooldownMs=Math.max(600,skill.getAnimationTime());
                    if(!b.hasDisease(Disease.SEDUCE)){b.triggerAutopotAfterStatLoss(-1,-1);return;}
                }
            }
        }
        BotHurricane.cancel(b);e.grindTarget=null;
        BotTravelManager.clearMoveTargetPin(e);BotMovementManager.clearNavigationState(e);
        if(e.climbing){BotPhysicsEngine.holdClimb(e,b);BotMovementManager.broadcastMovement(e);return;}
        var disease=b.getAllDiseases().get(Disease.SEDUCE);
        if(disease==null)return;
        int mode=disease.getRight().getX();e.moveDir=direction(mode);
        if(e.inAir)BotMovementManager.tickAirborne(e,new Point(b.getPosition().x+e.moveDir*1000,b.getPosition().y));
        else {
            if(mode==3)BotMovementManager.initiateJump(e,b,0);
            if(e.inAir)BotMovementManager.tickAirborne(e,b.getPosition());
            else {BotPhysicsEngine.tickMotionTimers(e);var floor=BotPhysicsEngine.syncAndDetectGround(e,b);if(floor!=null)BotPhysicsEngine.applyGroundMotion(e,b,floor);BotMovementManager.broadcastMovement(e);}
        }
    }
}
