package server.bots;
import client.Character;import java.awt.Point;import server.life.Monster;
/** One party stages for the next wave while the party already on the last statue finishes it. */
final class BotPinkBeanTransition {
 private BotPinkBeanTransition() {}
 static Point destination(Character b){
  if(!BotPinkBeanRun.inInstance(b))return null;int wave=b.getEventInstance().getIntProperty("stage");if(wave<2||wave>5)return null;
  var crew=BotPinkBeanRun.crew(b);if(crew==null)return null;Monster last=null;
  for(var m:b.getMap().getAllMonsters())if(m.isAlive()&&m.getId()>=8820002&&m.getId()<=8820006){if(last!=null)return null;last=m;}
  boolean right=BotPinkBeanRun.rightSide(crew,b);
  if(last==null){
   if(b.getMap().getMonsterById(8820001)!=null)return null;
   // The warning describes the wave about to spawn, not the one just cleared.
   // Keep each complete party on its assigned safe side before the spawn.
   boolean nextRight=crew.notices.contains("body-rally")||BotPinkBeanAssignments.rightParty(wave,BotPinkBeanRun.partyIndex(crew,b));
   return new Point(nextRight?840:-830,-42);
  }
  if(wave==5||last.getHp()>last.getMaxHp()/10)return null;
 int id=last.getId();boolean lastLeft=id==8820003||id==8820005;boolean lastRight=id==8820004||id==8820006;
  if(right&&lastLeft&&wave>=3)return new Point(900,-42);
  if(!right&&lastRight)return BotPinkBeanAssignments.rightParty(wave+1,BotPinkBeanRun.partyIndex(crew,b))?new Point(900,-42):new Point(-900,-42);
  return null;
 }
}
