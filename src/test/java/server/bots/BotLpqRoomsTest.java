package server.bots;

import client.Character;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import server.maps.MapleMap;

import java.awt.Point;
import java.util.List;

import static org.mockito.Mockito.*;

class BotLpqRoomsTest {
    @Test
    void leaderKeepsReturningAfterAdvancingTheRoomAssignment() {
        var bot = mock(Character.class);
        var event = mock(EventInstanceManager.class);
        var map = mock(MapleMap.class);
        when(bot.getEventInstance()).thenReturn(event);
        when(event.getPlayers()).thenReturn(List.of(bot));
        when(event.isEventLeader(bot)).thenReturn(true);
        when(bot.getMapId()).thenReturn(922010401);
        when(bot.getMap()).thenReturn(map);
        when(bot.getPosition()).thenReturn(new Point());
        when(bot.getItemQuantity(BotLpqRun.PASS, false)).thenReturn(1);
        var entry = new BotEntry(bot, bot, null);
        try (var travel = mockStatic(BotTravelManager.class); var combat = mockStatic(BotCombatManager.class)) {
            BotLpqRooms.tick(entry, bot, true);
            BotLpqRooms.tick(entry, bot, true);
            travel.verify(() -> BotTravelManager.tickTravel(entry, bot, 922010400, 2, true, false), times(2));
            combat.verifyNoInteractions();
        }
    }
}
