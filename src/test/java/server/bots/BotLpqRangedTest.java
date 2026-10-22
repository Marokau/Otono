package server.bots;

import constants.skills.Rogue;
import org.junit.jupiter.api.Test;
import server.life.Monster;

import java.awt.Point;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotLpqRangedTest {
    @Test
    void alisharTriggerAlsoHasALegalJumpThrowPosition() {
        var map = BotNavigationMapLoader.loadMapGeometry(922010900);
        var graph = BotNavigationGraphProvider.getGraph(map);
        var lab = BotMovementSimulationLab.fromMap(map);
        var entry = lab.spawnBot("LPQ", 550, map, new Point(-171, 184));
        when(entry.bot.getSkillLevel(Rogue.KEEN_EYES)).thenReturn(8);
        var target = mock(Monster.class);
        when(target.getId()).thenReturn(9300006);
        when(target.getPosition()).thenReturn(new Point(588, 0));
        var goal = BotLpqRanged.firingGoal(graph, entry.bot, target, true);
        assertNotNull(goal);
        assertTrue(goal.point().x < 554);
        assertTrue(goal.jump());
    }
    @Test
    void selectsReachableLeftLedgeRatherThanTheIsolatedRatzPlatform() {
        var map = BotNavigationMapLoader.loadMapGeometry(922010700);
        var graph = BotNavigationGraphProvider.getGraph(map);
        var lab = BotMovementSimulationLab.fromMap(map);
        var entry = lab.spawnBot("LPQ", 550, map, new Point(-210, -709));
        when(entry.bot.getSkillLevel(Rogue.KEEN_EYES)).thenReturn(8);
        var target = mock(Monster.class);
        when(target.getId()).thenReturn(9300170);
        when(target.getPosition()).thenReturn(new Point(224, -1037));
        var goal = BotLpqRanged.firingGoal(graph, entry.bot, target, false);
        assertNotNull(goal);
        assertTrue(goal.point().x < 0);
        assertFalse(goal.jump());
        assertTrue(graph.canReach(graph.findRegionId(map, entry.bot.getPosition()), graph.findRegionId(map, goal.point()), 0));
    }

    @Test
    void topRatzRequiresAJumpCapableProjectileRole() {
        var map = BotNavigationMapLoader.loadMapGeometry(922010700);
        var graph = BotNavigationGraphProvider.getGraph(map);
        var lab = BotMovementSimulationLab.fromMap(map);
        var entry = lab.spawnBot("LPQ", 550, map, new Point(-210, -1469));
        when(entry.bot.getSkillLevel(Rogue.KEEN_EYES)).thenReturn(8);
        var target = mock(Monster.class);
        when(target.getId()).thenReturn(9300171);
        when(target.getPosition()).thenReturn(new Point(228, -1535));
        assertNull(BotLpqRanged.firingGoal(graph, entry.bot, target, false));
        var goal = BotLpqRanged.firingGoal(graph, entry.bot, target, true);
        assertNotNull(goal);
        assertTrue(goal.jump());
    }
}
