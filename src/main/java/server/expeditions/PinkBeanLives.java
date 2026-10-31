package server.expeditions;
import client.Character;
/** Otono Pink Bean lives: the fifth death leaves a participant down until the run ends. */
public final class PinkBeanLives {
 private PinkBeanLives() {}
 public static boolean exhausted(Character player){
  if(player==null)return false;var event=player.getEventInstance();
  return event!=null&&event.getName()!=null&&event.getName().startsWith("PinkBean")&&!event.isEventCleared()
    &&"5".equals(event.getProperty("pinkDeathLimit"))
    &&event.getProperty("pinkDeaths:"+player.getId())!=null
    &&Integer.parseInt(event.getProperty("pinkDeaths:"+player.getId()))>=5;
 }
}
