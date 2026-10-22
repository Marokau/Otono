package server.bots;

import org.junit.jupiter.api.Test;
import java.awt.Point;
import static org.junit.jupiter.api.Assertions.*;

class BotOpqReactorApproachTest {
    @Test void cloudSeventeenCanBeAttackedWhileDroppingFromItsUpperLedge() {
        var map = BotNavigationMapLoader.loadMapGeometry(920010000);
        var graph = BotNavigationGraphProvider.getGraph(map, new BotMovementProfile(140, 120));
        var lab = BotMovementSimulationLab.fromMap(map);
        var entry = lab.spawnBot("OPQ", 586, map, new Point(199, -243));
        var target = new Point(201, -443);
        var stance = BotOpqReactorApproach.stance(graph, entry.bot, target);
        assertNotNull(stance);
        assertTrue(Math.abs(stance.x - target.x) <= 65);
        assertTrue(stance.y < target.y - 45);
        assertTrue(BotPhysicsEngine.canStartDownJump(map, stance));
        var landing = BotPhysicsEngine.simulateDownJumpLanding(map, stance);
        assertNotNull(landing);
        assertTrue(landing.point().y >= target.y - 45);
    }
    @Test void cloudNineteenIsHitFromTheAdjacentPlatformInsteadOfTheFloorFarBelow() {
        var map = BotNavigationMapLoader.loadMapGeometry(920010000);
        var graph = BotNavigationGraphProvider.getGraph(map);
        var lab = BotMovementSimulationLab.fromMap(map);
        var entry = lab.spawnBot("OPQ", 586, map, new Point(-503, -205));
        var cloud = new Point(-504, -540);
        var stance = BotOpqReactorApproach.stance(graph, entry.bot, cloud);
        assertNotNull(stance);
        assertTrue(Math.abs(stance.x - cloud.x) <= 65);
        assertTrue(Math.abs(stance.y - cloud.y) <= 45);
        assertTrue(graph.canReach(graph.findRegionId(map, entry.bot.getPosition()), graph.findRegionId(map, stance), 0));
    }
}
