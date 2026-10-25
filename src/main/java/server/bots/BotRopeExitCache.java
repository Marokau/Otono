package server.bots;
import server.maps.MapleMap;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
/** Memoize repeated rope launch-window samples within a single graph-build thread. */
final class BotRopeExitCache {
 private record Key(int x,int y,int step){}
 private static final class Scope {MapleMap map;BotMovementProfile profile;final Map<Key,BotPhysicsEngine.JumpLanding> values=new HashMap<>();}
 private static final ThreadLocal<Scope> LOCAL=new ThreadLocal<>();
 private BotRopeExitCache(){}
 static void clear(){LOCAL.remove();}
 static BotPhysicsEngine.JumpLanding landing(MapleMap map,Point from,int step,BotMovementProfile profile){
  Scope s=LOCAL.get();if(s==null||s.map!=map||!profile.equals(s.profile)){s=new Scope();s.map=map;s.profile=profile;LOCAL.set(s);}
  Key key=new Key(from.x,from.y,step);if(s.values.containsKey(key))return s.values.get(key);
  var result=BotPhysicsEngine.simulateRopeJumpLanding(map,from,step,profile);s.values.put(key,result);return result;
 }
}
