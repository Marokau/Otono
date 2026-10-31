package server.bots;
import client.Character;import java.awt.Point;import server.life.Monster;
/** Keep statue approaches on a safe side, rather than crossing a sage to reach an outer firing spot. */
final class BotPinkBeanStances {
 private BotPinkBeanStances() {}
 static boolean allows(Character b,Point p,Monster mob){
  if(!BotPinkBeanRun.inInstance(b))return true;
  int wave=b.getEventInstance().getIntProperty("stage"),id=mob.getId();
  boolean melee=b.getJob()==client.Job.HERO||b.getJob()==client.Job.DARKKNIGHT;
  if(id==8820003)return melee?Math.abs(p.y+42)<=8&&(wave==4?p.x>=-530&&p.x<=-450:p.x<=-800):Math.abs(p.y+456)<=8&&p.x>=-360&&p.x<=-320;
  if(id==8820004)return melee?Math.abs(p.y+42)<=8&&(wave<=3?p.x>=400&&p.x<=530:p.x>=800):Math.abs(p.y+456)<=8&&p.x>=320&&p.x<=360;
  if(id==8820005)return Math.abs(p.y+99)<=8&&p.x<=-530;
  if(id==8820006)return Math.abs(p.y+102)<=8&&p.x>=540;
  return true;
 }
}
