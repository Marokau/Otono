package server.bots;

import org.junit.jupiter.api.Test;
import server.bots.combat.BotMobHitboxProvider;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class BotMobHitboxProviderTest {
    @Test void fixedRightZakumArmUsesItsAlreadyOrientedBounds() {
        var mob=mock(server.life.Monster.class);var stats=mock(server.life.MonsterStats.class);
        when(mob.getId()).thenReturn(8800004);when(mob.getPosition()).thenReturn(new Point(-10,-216));
        when(mob.getStats()).thenReturn(stats);when(stats.getFixedStance()).thenReturn(5);
        when(mob.isFacingLeft()).thenReturn(true);
        assertEquals(new Rectangle(85,-625,91,153),BotMobHitboxProvider.getInstance().getMobBounds(mob));
    }
    @Test void floorAttackCannotHitAnUpperArmThroughItsSharedSpawnOrigin() throws Exception {
        var mob=mock(server.life.Monster.class);var stats=mock(server.life.MonsterStats.class);
        when(mob.getId()).thenReturn(8800003);when(mob.getPosition()).thenReturn(new Point(-10,-216));
        when(mob.getStats()).thenReturn(stats);when(stats.getFixedStance()).thenReturn(4);
        var check=BotCombatManager.class.getDeclaredMethod("doesHitBoxIntersectMonster",Rectangle.class,server.life.Monster.class);
        check.setAccessible(true);
        assertEquals(false,check.invoke(null,new Rectangle(-60,-260,100,80),mob));
        assertEquals(true,check.invoke(null,new Rectangle(-160,-600,100,80),mob));
    }
    @Test
    void shouldUseStandZeroBoundsForRightFacingMob() {
        Rectangle bounds = BotMobHitboxProvider.getInstance().getMobBounds(100100, new Point(100, 200), false);

        assertNotNull(bounds);
        assertEquals(new Rectangle(82, 174, 37, 26), bounds);
    }

    @Test
    void shouldMirrorBoundsForLeftFacingMob() {
        Rectangle bounds = BotMobHitboxProvider.getInstance().getMobBounds(100100, new Point(100, 200), true);

        assertNotNull(bounds);
        assertEquals(new Rectangle(81, 174, 37, 26), bounds);
    }
}
