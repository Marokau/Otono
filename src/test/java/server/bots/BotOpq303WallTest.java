package server.bots;
import org.junit.jupiter.api.Test;
import java.awt.Point;
import static org.junit.jupiter.api.Assertions.*;
class BotOpq303WallTest {
 @Test void enclosedRoomFloorsDoNotBecomeWalkOffLedgesAtTheirAttachedWalls() {
  var map=BotNavigationMapLoader.loadMapGeometry(920010603);
  BotNavigationGraphProvider.rebuildGraph(map);
  for(int y:new int[]{-328,-643,-1046,-1444,-1845}) {
   assertTrue(BotPhysicsEngine.isGroundStepBlockedByWall(map,new Point(219,y),5),"right wall at "+y);
   assertTrue(BotPhysicsEngine.isGroundStepBlockedByWall(map,new Point(-309,y),-5),"left wall at "+y);
  }
 }
}
