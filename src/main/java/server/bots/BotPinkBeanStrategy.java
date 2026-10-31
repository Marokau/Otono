package server.bots;
import client.Character;
import client.Job;
import server.life.Monster;
import java.util.*;

/** Guide-informed target choices; native damage, reach, status and movement remain authoritative. */
final class BotPinkBeanStrategy {
 private BotPinkBeanStrategy() {}
 static List<Monster> targets(Character bot,List<Monster> attackable){
  if(!BotPinkBeanRun.inInstance(bot))return attackable;
  int wave=bot.getEventInstance().getIntProperty("stage");var crew=BotPinkBeanRun.crew(bot);
  boolean right=BotPinkBeanRun.rightSide(crew,bot);
  int[] order=wave<=2?new int[]{8820003,8820004}:wave==3?new int[]{8820003,8820005,8820004}:right?new int[]{8820004,8820006,8820003,8820005,8820002}:new int[]{8820003,8820005,8820004,8820006,8820002};
  boolean melee=bot.getJob()==Job.HERO||bot.getJob()==Job.DARKKNIGHT;
  if(wave>=4&&melee&&(right||bot.getMap().getMonsterById(8820003)==null&&bot.getMap().getMonsterById(8820005)==null)&&bot.getMap().getMonsterById(8820004)==null&&bot.getMap().getMonsterById(8820006)!=null)order=new int[0];
  Monster chosen=bot.getMap().getMonsterById(8820001);
  if(chosen==null)for(int id:order){
   // Close-range fighters can help on the other side while ranged attackers handle Munin.
   if(id==8820006&&melee)continue;
   if(id==8820002){boolean sidesRemain=false;for(int side=8820003;side<=8820006;side++){var other=bot.getMap().getMonsterById(side);if(other!=null&&other.isAlive())sidesRemain=true;}if(sidesRemain)continue;}
   var mob=bot.getMap().getMonsterById(id);if(mob!=null&&mob.isAlive()){chosen=mob;break;}
  }
  var result=new ArrayList<Monster>();
  for(var mob:attackable)if(!BotPinkBeanRun.boss(mob.getId())||mob==chosen)result.add(mob);
  // If the chosen statue reflects, hold rather than chase another statue across the raid.
  return result;
 }
}
