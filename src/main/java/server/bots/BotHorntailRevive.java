package server.bots;
import client.Character;
import client.Disease;
import client.SkillFactory;
import constants.skills.Bishop;
import server.StatEffect;
import java.awt.Point;
import java.util.List;

/** Approach a reachable casting stance; the existing support handler owns Resurrection. */
final class BotHorntailRevive {
 private BotHorntailRevive() {}
 static boolean tick(BotEntry entry, Character bot, List<Character> crew, boolean ai) {
  int level=bot.getSkillLevel(Bishop.RESURRECTION);
  if(level<=0||bot.skillIsCooling(Bishop.RESURRECTION)||entry.attackCooldownMs>0
      ||bot.hasDisease(Disease.STUN)||bot.hasDisease(Disease.SEAL))return false;
  if(BotHorntailRun.inInstance(bot)&&!BotHorntailCombat.safe(bot,bot.getPosition(),bot.getMap().getAllMonsters()))return false;
  var effect=SkillFactory.getSkill(Bishop.RESURRECTION).getEffect(level);
  if(!effect.canPaySkillCost(bot))return false;
  for(var member:crew)if(member!=bot&&member.getHp()<=0&&member.getMap()==bot.getMap()
      &&bot.getParty()!=null&&member.getParty()!=null&&bot.getParty().getId()==member.getParty().getId()) {
   if(server.expeditions.PinkBeanLives.exhausted(member))continue;
   if(BotPinkBeanRun.inInstance(bot)&&!BotHorntailCombat.safe(member,member.getPosition(),bot.getMap().getAllMonsters()))continue;
   if(BotHorntailRun.inInstance(bot)&&!BotHorntailCombat.safe(bot,member.getPosition(),bot.getMap().getAllMonsters()))continue;
   if(!entry.inAir&&!entry.climbing&&effect.calculateBoundingBox(bot.getPosition(),bot.isFacingLeft()).contains(member.getPosition()))
    return BotZakumSupport.resurrect(entry,bot,List.of(bot,member),ai);
   if(BotPinkBeanRun.inInstance(bot)){
    var munin=bot.getMap().getMonsterById(8820006);
    var danger=munin==null?null:BotPinkBeanOuterFire.area(munin);
    if(danger!=null&&(danger.contains(bot.getPosition())||danger.contains(member.getPosition())
        ||(bot.getPosition().x<danger.getCenterX())!=(member.getPosition().x<danger.getCenterX())))continue;
   }
   var destination=stance(entry,bot,member.getPosition(),effect);
   if(destination!=null){BotPinkBeanMovement.walkTo(entry,bot,destination,ai);return true;}
  }
  return false;
 }
 static Point stance(BotEntry entry,Character bot,Point corpse,StatEffect effect) {
  var graph=BotNavigationGraphProvider.peekGraph(bot.getMap(),entry.movementProfile);if(graph==null)return null;
  int from=graph.findRegionId(bot.getMap(),bot.getPosition());int mask=BotNavigationManager.botCanUseMovementSkill(bot)?BotNavigationManager.botSkillMask(bot):0;
  Point best=null;double distance=Double.MAX_VALUE;
  for(var region:graph.regions)if(!region.isRopeRegion&&graph.canReach(from,region.id,mask))
   for(var segment:region.segments)for(int x=segment.minX+8;x<segment.maxX-8;x+=35){
    var p=segment.pointAt(x);
    if(!effect.calculateBoundingBox(p,bot.isFacingLeft()).contains(corpse)
       ||!BotHorntailCombat.safe(bot,p,bot.getMap().getAllMonsters()))continue;
    double score=p.distanceSq(bot.getPosition());if(score<distance){distance=score;best=p;}
   }
  return best;
 }
}
