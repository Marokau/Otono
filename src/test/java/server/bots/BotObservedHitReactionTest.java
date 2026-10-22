package server.bots;
import client.Character;
import client.BuffStat;
import org.junit.jupiter.api.Test;
import server.life.Monster;
import java.awt.Point;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotObservedHitReactionTest {
    @Test void nonResistantGroundedBotRecoilsAwayAndInterruptsHurricane() {
        var bot=mock(Character.class);when(bot.getHp()).thenReturn(1000);when(bot.getPosition()).thenReturn(new Point(-400,-214));
        var mob=mock(Monster.class);when(mob.getPosition()).thenReturn(new Point(-9,-216));
        var entry=new BotEntry(bot,null,null);entry.attackCooldownMs=120;entry.moveWindowMs=10;entry.grindTarget=mob;
        try(var physics=mockStatic(BotPhysicsEngine.class);var movement=mockStatic(BotMovementManager.class);var hurricane=mockStatic(BotHurricane.class)) {
            BotObservedHitReaction.apply(entry,bot,mob);
            physics.verify(()->BotPhysicsEngine.beginKnockback(eq(entry),eq(bot),eq(new Point(-400,-214)),anyFloat(),intThat(x->x<0)));
            hurricane.verify(()->BotHurricane.cancel(bot));movement.verify(()->BotMovementManager.broadcastMovement(entry));
            assertNull(entry.grindTarget);assertEquals(0,entry.attackCooldownMs);assertEquals(0,entry.moveWindowMs);
        }
    }
    @Test void fullStanceResistancePreventsRecoilAndAirborneHitsUseAirPhysics() {
        var bot=mock(Character.class);when(bot.getHp()).thenReturn(1000);when(bot.getPosition()).thenReturn(new Point(400,-214));
        var mob=mock(Monster.class);when(mob.getPosition()).thenReturn(new Point(-9,-216));
        var entry=new BotEntry(bot,null,null);entry.inAir=true;
        try(var physics=mockStatic(BotPhysicsEngine.class);var movement=mockStatic(BotMovementManager.class);var hurricane=mockStatic(BotHurricane.class)) {
            when(bot.getBuffedValue(BuffStat.STANCE)).thenReturn(100);BotObservedHitReaction.apply(entry,bot,mob);physics.verifyNoInteractions();hurricane.verifyNoInteractions();
            when(bot.getBuffedValue(BuffStat.STANCE)).thenReturn(null);BotObservedHitReaction.apply(entry,bot,mob);
            physics.verify(()->BotPhysicsEngine.applyAirKnockback(eq(entry),eq(bot),intThat(x->x>0)));
        }
    }
}
