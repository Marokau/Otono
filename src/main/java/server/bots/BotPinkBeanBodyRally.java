package server.bots;
import client.Character;
import java.awt.Point;
/** Cross before Ariel falls, while the real body's contact area is still empty. */
final class BotPinkBeanBodyRally {
 private BotPinkBeanBodyRally() {}
 static boolean tick(BotEntry e,Character b,boolean ai){
  var crew=BotPinkBeanRun.crew(b);if(crew==null||b.getMap()==null)return false;
  var ariel=b.getMap().getMonsterById(8820002);if(ariel==null||!ariel.isAlive()||b.getMap().getMonsterById(8820001)!=null)return false;
  if(!crew.notices.contains("body-rally")){
   if(ariel.getMaxHp()<=0||ariel.getHp()>ariel.getMaxHp()/4)return false;
   for(int id=8820003;id<=8820006;id++)if(b.getMap().getMonsterById(id)!=null)return false;
   if(crew.notices.add("body-rally"))BotPqChat.queue(e,"Those of us already on the right will keep attacking Ariel while the others cross, before Pink Bean's body blocks the way.",false);
  }
  if(crew.notices.contains("body-rally-ready"))return false;
  // Any safe right-side platform is sufficient. Do not send attackers down
  // to a distant floor marker only to have them climb back to the same target.
  if(b.getPosition().x<150){BotPinkBeanMovement.walkTo(e,b,new Point(610,-42),ai);return true;}
  boolean crossed=true;
  for(var mate:BotPinkBeanParticipation.readyMembers(crew))
   if(mate.isAlive()&&(mate.getMap()!=b.getMap()||mate.getPosition().x<150)){crossed=false;break;}
  if(crossed){crew.notices.add("body-rally-ready");return false;}
  // Continue damaging Ariel while the crossing completes; retain only a small
  // finish reserve if a bot is delayed, rather than holding the entire last quarter.
  return ariel.getHp()<=Math.max(1,ariel.getMaxHp()/20)?BotOpqRun.hold(e,b,ai):false;
 }
}
