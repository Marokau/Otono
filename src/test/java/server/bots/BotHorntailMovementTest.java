package server.bots;
import java.awt.Point;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BotHorntailMovementTest {
 @Test void flashJumpMustRemainOnTheLandingPlatformThroughOrdinaryGroundTicks(){
  var map=BotNavigationMapLoader.loadMapGeometry(240050101);var profile=new BotMovementProfile(140,123);var takeoff=new Point(864,-160);
  var landing=BotPhysicsEngine.simulateFlashJumpLanding(map,takeoff,9,profile);assertNotNull(landing);
  var settled=BotPhysicsEngine.simulatePostLandingGroundTicks(map,landing,1,profile,3);
  assertTrue(settled.lostGround(),"The observed edge lands but immediately walks off its ledge");
  var graph=BotNavigationGraphProvider.rebuildGraph(map,profile);int from=graph.findRegionId(map,takeoff);int target=graph.findRegionId(map,landing.point());
  for(var edge:graph.getOutgoing(from,BotNavigationGraph.SKILL_FLASH_JUMP))
   if(edge.type==BotNavigationGraph.EdgeType.FLASH_JUMP&&edge.toRegionId==target&&edge.launchStepX==9)
    assertFalse(edge.launchMinX<=864&&edge.launchMaxX>=864,"The launch window must exclude this unstable landing");
 }
}
