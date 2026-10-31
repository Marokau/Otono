package server.bots;
import client.Character;
/** Dismiss autonomous attacking summons during statues; retain passive Beholder support. */
final class BotPinkBeanSummons {
 private BotPinkBeanSummons() {}
 static boolean allowed(Character b,int skill){
  if(BotHorntailRun.inInstance(b)&&BotSummonCombat.supported(skill))
   for(var mob:b.getMap().getAllMonsters())if(mob.isAlive()&&(mob.getId()==8810005||mob.getId()==8810006)
           &&BotHorntailTactics.targets(b,java.util.List.of(mob)).isEmpty()) {
    var bounds=server.bots.combat.BotMobHitboxProvider.getInstance().getMobBounds(mob);
    if(bounds!=null&&BotSummonCombat.reachable(BotSummonCombat.spec(skill),b.getPosition(),bounds))return false;
   }
  return !BotPinkBeanRun.inInstance(b)||!BotSummonCombat.supported(skill)||b.getMap().getMonsterById(8820001)!=null;
 }
}
