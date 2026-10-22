package server.bots;
import org.junit.jupiter.api.Test;
import java.awt.Point;
import server.maps.Rope;
import static org.junit.jupiter.api.Assertions.*;
class BotOpqAscentNavigationTest {
    @Test void ascentStartingLadderIsReachableAtForcedBaseStats() {
        var map = BotNavigationMapLoader.loadMapGeometry(920010700);
        var graph = BotNavigationGraphProvider.getGraph(map, BotMovementProfile.base());
        assertTrue(graph.canReach(graph.findRegionId(map, new Point(-178, -846)),
                graph.findRegionId(map, new Point(-179, -2092)), 0));
    }
    @Test void liveJumpAndBuilderBothCatchTheLadderBetweenFiftyMillisecondSamples() {
        var map = BotNavigationMapLoader.loadMapGeometry(920010700);
        var rope = map.getRopes().stream().filter(r -> r.x() == -43).findFirst().orElseThrow();
        var from = new Point(-107, -846);
        int step = BotMovementManager.walkStep(map, BotMovementProfile.base());
        var expected = BotPhysicsEngine.simulateGroundJumpRopeGrab(map, from, step, rope);
        assertNotNull(expected);
        var lab = BotMovementSimulationLab.fromMap(map);
        var entry = lab.spawnBot("Scout", 589, map, from);
        BotPhysicsEngine.beginClimbUpJump(entry, entry.bot, step);
        entry.climbIntentRope = rope;
        entry.fixedAirArc = true;
        for (int n = 0; n < 12; n++) {
            BotPhysicsEngine.stepAirborne(entry, entry.bot);
            if (BotPhysicsEngine.climbableAtPoint(map, entry.bot.getPosition()) != null) break;
        }
        var actual = entry.bot.getPosition();
        assertSame(rope, BotPhysicsEngine.climbableAtPoint(map, actual));
        assertEquals(expected.y, actual.y);
    }
    @Test void sweptContactKeepsTheExistingHitboxAndRespectsEarlierCollision() {
        var rope = new Rope(-43, -1623, -878, true);
        assertNotNull(BotRopeGrabSweep.contact(new Point(-53,-893), new Point(-47,-874), rope, 1));
        assertNull(BotRopeGrabSweep.contact(new Point(-53,-893), new Point(-47,-874), rope, .2));
        assertNull(BotRopeGrabSweep.contact(new Point(-53,-875), new Point(-47,-856), rope, 1));
    }
}
