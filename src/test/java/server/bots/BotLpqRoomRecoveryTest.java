package server.bots;

import client.Character;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import java.awt.Point;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotLpqRoomRecoveryTest {
    @Test void everyDarkRoomRejectsRecoveryTeleportToExit() throws Exception {
        var bot=mock(Character.class);var entry=new BotEntry(bot,bot,null);
        var manager=mock(BotManager.class,CALLS_REAL_METHODS);
        when(bot.getEventInstance()).thenReturn(mock(EventInstanceManager.class));
        var recovery=BotManager.class.getDeclaredMethod("executeRecoveryTeleport",BotEntry.class,Character.class,Point.class);
        recovery.setAccessible(true);
        try(var physics=mockStatic(BotPhysicsEngine.class);var movement=mockStatic(BotMovementManager.class)) {
            for(int map=922010401;map<=922010405;map++) {
                when(bot.getMapId()).thenReturn(map);
                assertEquals(false,recovery.invoke(manager,entry,bot,new Point(-1366,-81)));
            }
            physics.verifyNoInteractions();movement.verifyNoInteractions();
        }
    }
    @Test void distantGoalFallsBackToNormalMovementInsteadOfSnapping() {
        var bot=mock(Character.class);var entry=new BotEntry(bot,bot,null);
        var manager=mock(BotManager.class,CALLS_REAL_METHODS);
        when(bot.getEventInstance()).thenReturn(mock(EventInstanceManager.class));
        when(bot.getMapId()).thenReturn(922010401);when(bot.getPosition()).thenReturn(new Point(10000,-81));
        try(var physics=mockStatic(BotPhysicsEngine.class)) {
            assertFalse(manager.recoverTeleportDistance(entry,bot,new Point(-1354,-81)));
            physics.verifyNoInteractions();
        }
    }
}
