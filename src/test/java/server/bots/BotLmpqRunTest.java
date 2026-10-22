package server.bots;
import client.Character;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotLmpqRunTest {
    @Test void unrelatedBotsAreNotControlledOrGivenCouponOwnership() {
        var bot = mock(Character.class);
        assertFalse(BotLmpqRun.tick(new BotEntry(bot, bot, null), bot, true));
        assertFalse(BotLmpqRun.canLoot(bot));
        assertThrows(IllegalArgumentException.class, () -> BotLmpqRun.startCrew(List.of(bot)));
    }
    @Test void instanceMembershipRequiresTheRealMazeEventAndMap() {
        var bot = mock(Character.class);
        when(bot.getMapId()).thenReturn(BotLmpqRun.FIRST);
        assertFalse(BotLmpqRun.inInstance(bot));
        when(bot.getEventInstance()).thenReturn(mock(EventInstanceManager.class));
        assertTrue(BotLmpqRun.inInstance(bot));
        when(bot.getMapId()).thenReturn(BotLmpqRun.EXIT);
        assertFalse(BotLmpqRun.inInstance(bot));
    }
    @Test void poorDropsLeadToFreshRoomsWithoutRevisitingSpentBoxes() {
        var rooms = new HashSet<Integer>();
        assertEquals(809050005, BotLmpqRun.nextRoom(rooms)); rooms.add(809050005);
        assertEquals(809050006, BotLmpqRun.nextRoom(rooms)); rooms.add(809050006);
        for (int n=0;n<9;n++) assertTrue(rooms.add(BotLmpqRun.nextRoom(rooms)));
        assertEquals(BotLmpqRun.FINISH, BotLmpqRun.nextRoom(rooms));
    }
}
