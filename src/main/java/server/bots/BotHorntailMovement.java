package server.bots;
import client.Character;
import java.awt.Point;
import java.awt.Rectangle;
import server.bots.combat.BotMobHitboxProvider;
/** Use the existing bottom-floor route before changing sides under the main heads. */
final class BotHorntailMovement {
 private BotHorntailMovement() {}
 static Point approach(Character bot,Point goal) {
  if(!BotHorntailRun.inInstance(bot)||bot.getMapId()!=240060200)return goal;
  Point position=bot.getPosition();
  // The measured head/wing contact areas end above y=100; the native floor is
  // below them. Descend on the outer side before asking navigation to cross.
  if(position.y<200&&(position.x<71)!=(goal.x<71)) {
   Point floor=bot.getMap().getPointBelow(new Point(position.x<71?-500:550,200));
   if(floor!=null)goal=floor;
  }
  if(position.y>=200&&goal.y<200&&(position.x<71)!=(goal.x<71)) {
   Point floor=bot.getMap().getPointBelow(new Point(goal.x,200));
   if(floor!=null)goal=floor;
  }
  var tail=bot.getMap().getMonsterById(8810009);
  if(tail!=null&&tail.isAlive()) {
   var contact=BotMobHitboxProvider.getInstance().getMobBounds(tail);
   if(contact!=null) {
    var hazard=new Rectangle(contact);hazard.grow(32,0);
    var body=new Rectangle(position.x-12,position.y-45,24,45);
    if(!hazard.intersects(body)) {
     // A clear destination does not make a floor crossing through the tail safe.
     // Wait on the current side; normal combat can still attack reachable parts.
     var corridor=new Rectangle(Math.min(position.x,goal.x)-12,Math.min(position.y,goal.y)-45,
             Math.abs(position.x-goal.x)+24,Math.abs(position.y-goal.y)+45);
     if(hazard.intersects(corridor))return position;
    }
   }
  }
  return goal;
 }
}
