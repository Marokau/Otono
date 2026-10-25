package server.bots;
import org.junit.jupiter.api.Test;import server.maps.MapleMap;import java.awt.Point;import static org.mockito.Mockito.*;
class BotRopeExitCacheTest {
 @Test void repeatedRejectedLaunchIsMemoizedOnlyWithinTheBuildScope(){
  var map=mock(MapleMap.class);var p=new Point(2,3);var profile=new BotMovementProfile(140,123);BotRopeExitCache.clear();
  try(var physics=mockStatic(BotPhysicsEngine.class)){
   BotRopeExitCache.landing(map,p,9,profile);BotRopeExitCache.landing(map,p,9,profile);
   physics.verify(()->BotPhysicsEngine.simulateRopeJumpLanding(map,p,9,profile),times(1));
   BotRopeExitCache.clear();BotRopeExitCache.landing(map,p,9,profile);
   physics.verify(()->BotPhysicsEngine.simulateRopeJumpLanding(map,p,9,profile),times(2));
  }finally{BotRopeExitCache.clear();}
 }
}
