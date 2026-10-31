package server.bots;
import java.awt.*;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class BotPinkBeanBodyApproachTest {
 private final Rectangle body=new Rectangle(-30,-126,67,85);
 @Test void floorReturnsStayOnTheirSideInsteadOfWalkingThroughTheBody(){
  assertEquals(new Point(-142,-42),BotPinkBeanBodyApproach.waypoint(new Point(-500,-42),new Point(550,-42),body));
  assertEquals(new Point(149,-42),BotPinkBeanBodyApproach.waypoint(new Point(500,-42),new Point(-550,-42),body));
 }
 @Test void towerDescentUsesTheOutsideAndSameSideTravelIsUnchanged(){
  assertEquals(new Point(550,-456),BotPinkBeanBodyApproach.waypoint(new Point(427,-456),new Point(200,-42),body));
  assertEquals(new Point(610,-42),BotPinkBeanBodyApproach.waypoint(new Point(550,-456),new Point(200,-42),body));
  var goal=new Point(300,-42);assertEquals(goal,BotPinkBeanBodyApproach.waypoint(new Point(600,-42),goal,body));
 }
 @Test void realMapMovementStopsBeforeTheBodyAndDescendsOutside(){
  var map=BotNavigationMapLoader.loadMapGeometry(270050100);var lab=BotMovementSimulationLab.fromMap(map);
  for(var start:java.util.List.of(new Point(-500,-42),new Point(500,-42),new Point(427,-456),new Point(0,-160),new Point(-85,-133))){
   String name="route"+start.x+"_"+start.y;var e=lab.spawnBot(name,9000+start.x,map,start);
   var goal=start.x<0?new Point(550,-42):start.y< -200?new Point(200,-42):new Point(-550,-42);
   var expected=start.x<0?new Point(-142,-42):start.y< -200?goal:start.x==0?new Point(-180,-42):new Point(149,-42);boolean arrived=false;
   for(int n=0;n<1200;n++){
    var feet=lab.position(name);Point target=(e.inAir||e.climbing)&&e.moveTarget!=null?new Point(e.moveTarget):BotPinkBeanBodyApproach.waypoint(feet,goal,body);
    lab.setMoveTarget(name,target,true);lab.stepRaw(name,target,true);feet=lab.position(name);
    assertFalse(body.intersects(new Rectangle(feet.x-12,feet.y-45,24,45)),"Body contact from "+start+" at "+feet);
    if(feet.distanceSq(expected)<=12*12){arrived=true;break;}
   }
   assertTrue(arrived,"Did not finish from "+start+"\n"+String.join("\n",lab.formatRecentTrace(name,8)));
  }
 }
 @Test void aReturningMemberCanUseItsCurrentSideRatherThanBeingForcedBackAcross(){
  var b=org.mockito.Mockito.mock(client.Character.class,org.mockito.Mockito.RETURNS_DEEP_STUBS);
  var mob=org.mockito.Mockito.mock(server.life.Monster.class);org.mockito.Mockito.when(mob.getPosition()).thenReturn(new Point(0,-42));
  org.mockito.Mockito.when(b.getMap().getMonsterById(8820001)).thenReturn(mob);org.mockito.Mockito.when(b.getPosition()).thenReturn(new Point(-500,-42));
  assertTrue(BotPinkBeanFormation.side(b,new Point(-200,-42)));assertFalse(BotPinkBeanFormation.side(b,new Point(200,-42)));
  org.mockito.Mockito.when(b.getPosition()).thenReturn(new Point(500,-42));assertTrue(BotPinkBeanFormation.side(b,new Point(200,-42)));
 }
}
