package server.bots;
import client.Character;
import org.junit.jupiter.api.Test;
import server.maps.Portal;
import java.awt.Point;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotOpqPortalTrialTest {
    @Test void learnsOnlyFromAnActualPortalUseAndVisibleArrival() {
        var bot = mock(Character.class);
        var portal = mock(Portal.class);
        var success = mock(Portal.class);
        when(bot.getPosition()).thenReturn(new Point(10, 10));
        when(success.getPosition()).thenReturn(new Point(10, 10));
        var entry = new BotEntry(bot, bot, null);
        try (var travel = mockStatic(BotTravelManager.class)) {
            assertEquals(BotOpqPortalTrial.Outcome.APPROACHING, BotOpqPortalTrial.tick(entry, bot, portal, success, true));
            travel.when(() -> BotTravelManager.walkToPortalAndEnter(eq(entry), eq(bot), eq(portal), anyLong(), eq(true)))
                    .thenAnswer(call -> { entry.portalUseCooldownUntilMs++; return true; });
            assertEquals(BotOpqPortalTrial.Outcome.SUCCESS, BotOpqPortalTrial.tick(entry, bot, portal, success, true));
            when(bot.getPosition()).thenReturn(new Point(500, 500));
            assertEquals(BotOpqPortalTrial.Outcome.WRONG, BotOpqPortalTrial.tick(entry, bot, portal, success, true));
        }
    }
}
