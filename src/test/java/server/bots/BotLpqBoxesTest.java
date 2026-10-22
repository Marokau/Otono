package server.bots;

import client.Character;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import server.maps.MapItem;
import server.maps.MapleMap;
import server.maps.Reactor;

import java.awt.Point;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class BotLpqBoxesTest {
    @Test
    void approachesDistantBoxInsteadOfHittingIt() {
        var bot = bot(922010200);
        var entry = new BotEntry(bot, bot, null);
        var box = mock(Reactor.class);
        when(box.getId()).thenReturn(2202003);
        when(box.isActive()).thenReturn(true);
        when(box.getPosition()).thenReturn(new Point(200, 0));
        when(bot.getMap().getAllReactors()).thenReturn(List.of(box));
        try (var interactions = mockStatic(BotZakumPqRun.class)) {
            assertTrue(BotLpqBoxes.tick(entry, bot, true));
            interactions.verify(() -> BotZakumPqRun.walkTo(entry, bot, new Point(200, 0), true));
            interactions.verify(() -> BotZakumPqRun.hitReactor(bot, box), never());
        }
    }

    @Test
    void trapRoomWaitsForUncollectedPassBeforeReturning() {
        var bot = bot(922010201);
        var entry = new BotEntry(bot, bot, null);
        var pass = mock(MapItem.class);
        when(pass.getItemId()).thenReturn(BotLpqRun.PASS);
        when(bot.getMap().getDroppedItems()).thenReturn(List.of(pass));
        try (var travel = mockStatic(BotTravelManager.class); var managers = mockStatic(BotManager.class)) {
            managers.when(BotManager::getInstance).thenReturn(mock(BotManager.class));
            assertTrue(BotLpqBoxes.tick(entry, bot, true));
            travel.verify(() -> BotTravelManager.tickTravel(entry, bot, 922010200, 2, true, false), never());
            when(pass.isPickedUp()).thenReturn(true);
            assertTrue(BotLpqBoxes.tick(entry, bot, true));
            travel.verify(() -> BotTravelManager.tickTravel(entry, bot, 922010200, 2, true, false));
        }
    }

    @Test
    void waitsForLastStageOneMemberBeforeTriggeringTrap() {
        var bot = bot(922010200);
        var entry = new BotEntry(bot, bot, null);
        var late = mock(Character.class);
        when(late.getMapId()).thenReturn(BotLpqRun.FIRST_MAP);
        when(bot.getEventInstance().getPlayers()).thenReturn(List.of(bot, late));
        try (var interactions = mockStatic(BotZakumPqRun.class); var managers = mockStatic(BotManager.class)) {
            managers.when(BotManager::getInstance).thenReturn(mock(BotManager.class));
            assertTrue(BotLpqBoxes.tick(entry, bot, true));
            interactions.verifyNoInteractions();
        }
    }

    private static Character bot(int mapId) {
        var bot = mock(Character.class);
        when(bot.getMapId()).thenReturn(mapId);
        when(bot.getPosition()).thenReturn(new Point());
        when(bot.getMap()).thenReturn(mock(MapleMap.class));
        when(bot.getEventInstance()).thenReturn(mock(EventInstanceManager.class));
        when(bot.getEventInstance().getPlayers()).thenReturn(List.of(bot));
        return bot;
    }
}
