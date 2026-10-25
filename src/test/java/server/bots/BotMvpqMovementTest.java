package server.bots;
import org.junit.jupiter.api.Test;import java.awt.Point;import static org.junit.jupiter.api.Assertions.*;
class BotMvpqMovementTest {
 @Test void trappedCourseDoesNotBakeTheWallBlockedRopeExit(){
  var map=BotNavigationMapLoader.loadMapGeometry(674030000);
  assertNull(BotPhysicsEngine.simulateRopeJumpLanding(map,new Point(-852,-827),-9,new BotMovementProfile(140,123)),"The old base-group simulation crossed a wall belonging to the arriving mover's retained group");
 }
 @Test void lowerCourseJumpMustAgreeBetweenBakedAndLivePhysics(){
  var map=BotNavigationMapLoader.loadMapGeometry(674030000);var profile=new BotMovementProfile(140,123);
  var graph=BotNavigationGraphProvider.rebuildGraph(map,profile);
  var lab=BotMovementSimulationLab.fromMap(map);var e=lab.spawnBot("MVPqReplay",1,map,new Point(-1109,65));
  org.mockito.Mockito.when(e.bot.getTotalMoveSpeedStat()).thenReturn(140);org.mockito.Mockito.when(e.bot.getTotalJumpStat()).thenReturn(123);e.movementProfile=profile;
  e.lastRegionId=graph.findRegionId(map,e.bot.getPosition());
  var baked=BotPhysicsEngine.simulateJumpLanding(map,e.bot.getPosition(),-9,profile);assertNotNull(baked);assertEquals(5,baked.point().y,"Authored jump must actually reach the next ledge");
  BotPhysicsEngine.beginGroundJump(e,e.bot,-9);e.fixedAirArc=true;
  for(int tick=0;tick<80&&e.inAir;tick++)BotPhysicsEngine.stepAirborne(e,e.bot);
  assertFalse(e.inAir);assertEquals(baked.point(),e.bot.getPosition(),"The wall collision group must match for a ground launch too");
 }
 @Test void nearApexDoesNotAttachToTheOverheadPlatformsCollisionGroup(){
  var map=BotNavigationMapLoader.loadMapGeometry(674030000);var profile=new BotMovementProfile(140,123);var graph=BotNavigationGraphProvider.rebuildGraph(map,profile);
  var lab=BotMovementSimulationLab.fromMap(map);var e=lab.spawnBot("MVPqApex",2,map,new Point(-1109,65));
  e.lastRegionId=graph.findRegionId(map,e.bot.getPosition());int launchRegion=e.lastRegionId;e.inAir=true;e.fixedAirArc=true;
  assertEquals(-1,BotNavigationManager.resolveCurrentRegionId(graph,e,map,new Point(-1155,-47)),"Passing just underneath a ledge is not landing on it");
  assertEquals(launchRegion,e.lastRegionId,"Retain the real takeoff collision group until landing");
 }
 @Test void wholeNavigationStackActuallyReachesTheNextLowerCourseLedge(){
  var map=BotNavigationMapLoader.loadMapGeometry(674030000);var profile=new BotMovementProfile(140,123);BotNavigationGraphProvider.rebuildGraph(map,profile);
  var lab=BotMovementSimulationLab.fromMap(map);var e=lab.spawnBot("MVPqLower",3,map,new Point(-1109,65));
  org.mockito.Mockito.when(e.bot.getTotalMoveSpeedStat()).thenReturn(140);org.mockito.Mockito.when(e.bot.getTotalJumpStat()).thenReturn(123);e.movementProfile=profile;
  lab.setMoveTarget("MVPqLower",new Point(-1212,5),true);lab.step(120);
  assertTrue(lab.position("MVPqLower").distance(new Point(-1212,5))<16,lab.describeCurrentState("MVPqLower"));
 }
 @Test void connectedLowerPlatformSupportsTheWholeCourseWithoutMovementSkills(){
  var map=BotNavigationMapLoader.loadMapGeometry(674030000);
  for(var profile:new BotMovementProfile[]{BotMovementProfile.base(),new BotMovementProfile(140,123)}){
   var graph=BotNavigationGraphProvider.rebuildGraph(map,profile);
   int start=graph.findRegionId(map,new Point(900,245));int guide=graph.findRegionId(map,new Point(-1483,-1675));
   assertTrue(graph.canReach(start,guide,0),"Ordinary route from connected entry to guide for "+profile);
  }
 }
 @Test void unbuffedBeginnerCanWalkAndClimbFromTheApprovedEntryToTheGuide(){
  var map=BotNavigationMapLoader.loadMapGeometry(674030000);BotNavigationGraphProvider.rebuildGraph(map);
  var lab=BotMovementSimulationLab.fromMap(map);var beginner=lab.spawnBot("MVPqBeginner",4,map,server.partyquest.MvpqEntry.startingPosition());
  lab.setMoveTarget("MVPqBeginner",new Point(-1483,-1675),true);lab.step(5000);
  assertTrue(lab.position("MVPqBeginner").distance(new Point(-1483,-1675))<18,lab.describeCurrentState("MVPqBeginner"));
  assertEquals(client.Job.BEGINNER,beginner.bot.getJob());
 }
 @Test void strandedBeginnerWalksAndFallsToTheRealResetHeightWithoutWarping(){
  var map=BotNavigationMapLoader.loadMapGeometry(674030000);var guide=server.life.LifeFactory.getNPC(9220020);guide.setPosition(new Point(-1483,-1675));map.addMapObject(guide);BotNavigationGraphProvider.rebuildGraph(map);
  var lab=BotMovementSimulationLab.fromMap(map);var e=lab.spawnBot("MVPqFall",5,map,new Point(-100,-175));
  var crew=new BotMvpqRun.Crew();Point previous=new Point(e.bot.getPosition());
  try(var chat=org.mockito.Mockito.mockStatic(BotPqChat.class)){
  for(int tick=0;tick<250&&e.bot.getPosition().y<300;tick++){
   assertTrue(BotMvpqCourseReset.tick(e,e.bot,crew,new Point(-1483,-1675),true));Point current=e.bot.getPosition();
   assertTrue(Math.abs(current.x-previous.x)<=BotPhysicsEngine.walkStep(map)+1&&Math.abs(current.y-previous.y)<=Math.ceil(BotPhysicsEngine.maxFallPerTick()+BotPhysicsEngine.gravityPerTick()/2)+1,"Only ordinary per-tick motion: "+previous+" -> "+current);previous=new Point(current);
  }
  assertTrue(e.bot.getPosition().y>=300,"Walk off and fall to the native pit portals, do not stay parked");
  }
 }
 @Test void resetDoesNotPreemptAnIslandBoxThatIsAlreadyReachable(){
  var map=BotNavigationMapLoader.loadMapGeometry(674030000);BotNavigationGraphProvider.rebuildGraph(map);
  var lab=BotMovementSimulationLab.fromMap(map);var e=lab.spawnBot("MVPqIsland",6,map,new Point(-54,-175));var c=new BotMvpqRun.Crew();
  assertFalse(BotMvpqCourseReset.tick(e,e.bot,c,new Point(-54,-176),true));assertEquals(new Point(-54,-175),e.bot.getPosition());
 }
}
