package server.bots;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BotOpqCloudNavigationTest {
 @Test void cloudsHaveRopeRoutesWithoutThePhantomPortal() {
  var map=BotNavigationMapLoader.loadMapGeometry(920010000);
  var graph=BotNavigationGraphProvider.rebuildGraph(map);
  assertNotNull(graph);
  assertTrue(graph.regions.stream().flatMap(r->graph.getOutgoing(r.id).stream()).anyMatch(e->e.type==BotNavigationGraph.EdgeType.CLIMB));
  assertFalse(graph.regions.stream().flatMap(r->graph.getOutgoing(r.id).stream()).anyMatch(e->e.type==BotNavigationGraph.EdgeType.PORTAL));
 }
}
