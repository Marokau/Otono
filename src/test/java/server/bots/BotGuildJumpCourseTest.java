package server.bots;
import org.junit.jupiter.api.Test;
import java.awt.Point;
import static org.junit.jupiter.api.Assertions.*;
class BotGuildJumpCourseTest {
 @Test void mageWaitsOnTheTinyTowerLedgeWhileTeleportIsCoolingDown(){
  var map=BotNavigationMapLoader.loadMapGeometry(990000440);
  var lab=BotMovementSimulationLab.fromMap(map);
  var entry=lab.spawnBot("GPqMage",662,map,new Point(1578,-9));
  org.mockito.Mockito.when(entry.bot.getJob()).thenReturn(client.Job.PRIEST);
  org.mockito.Mockito.when(entry.bot.getSkillLevel(2301001)).thenReturn(20);
  org.mockito.Mockito.when(entry.bot.getMeso()).thenReturn(1000000);
  org.mockito.Mockito.when(entry.bot.getMp()).thenReturn(10000);
  org.mockito.Mockito.when(entry.bot.getMaxMp()).thenReturn(10000);
  org.mockito.Mockito.when(entry.bot.getTotalMoveSpeedStat()).thenReturn(140);
  org.mockito.Mockito.when(entry.bot.getTotalJumpStat()).thenReturn(123);
  entry.movementProfile=new BotMovementProfile(140,123);
  BotNavigationGraphProvider.rebuildGraph(map,entry.movementProfile);
  entry.skillHopReadyAtMs=System.currentTimeMillis()+60000;
  lab.setMoveTarget("GPqMage",new Point(1571,-1054),true);
  lab.step(40);
  assertFalse(entry.inAir,lab.describeCurrentState("GPqMage"));
  assertEquals(-9,lab.position("GPqMage").y,"Wait for the next normal cast, do not fall off: " + String.join("\n",lab.formatRecentTrace("GPqMage",40)));
  assertTrue(lab.position("GPqMage").x>=1520&&lab.position("GPqMage").x<=1578);
 }

 @Test void trousersWorkerActuallyTraversesTheCourseAndRecoversFromTheBottomFloor(){
  for(Point start: new Point[]{new Point(-413,7),new Point(2625,185)}){
   var map=BotNavigationMapLoader.loadMapGeometry(990000611);
   var lab=BotMovementSimulationLab.fromMap(map);
   var entry=lab.spawnBot("GPqSpear",683,map,start);
   lab.setMoveTarget("GPqSpear",new Point(2623,-74),true);
   lab.step(3000);
   assertTrue(lab.position("GPqSpear").distance(new Point(2623,-74))<=12,
     "Course traversal from "+start+": "+lab.describeCurrentState("GPqSpear"));
  }
 }

 @Test void trousersCourseHasAnOrdinaryBaseProfileRouteFromItsEntranceToTheClothingBox(){
  var map=BotNavigationMapLoader.loadMapGeometry(990000611);
  var graph=BotNavigationGraphProvider.rebuildGraph(map);
  int start=graph.findRegionId(map,new Point(-413,7));
  int stair=graph.findRegionId(map,new Point(-330,-70));
  int goal=graph.findRegionId(map,new Point(2623,-74));
  assertTrue(start>=0&&stair>=0&&goal>=0);
  assertTrue(graph.canReach(start,stair,0),"The ordinary 77-pixel jump must be baked into the course");
  assertTrue(graph.canReach(start,goal,0),"No Teleport or Flash Jump in this restricted map");
 }
}
