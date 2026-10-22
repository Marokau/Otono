package server.bots;

import client.BotClient;
import client.Character;
import client.inventory.InventoryType;
import constants.skills.ILWizard;
import constants.skills.Rogue;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import server.maps.MapleMap;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

class BotLpqVaultsTest {
    @Test void fiveBotsCoverTheSixthRoomWithoutWaitingForAHumanRoomDoneFlag() {
        var human = mock(Character.class);
        var event = mock(EventInstanceManager.class);
        var map = mock(MapleMap.class);
        var crew = new ArrayList<Character>();
        when(human.getId()).thenReturn(100);
        when(human.getMapId()).thenReturn(922010500);
        when(human.getPosition()).thenReturn(new Point());
        when(event.isEventLeader(human)).thenReturn(true);
        for (int n = 0; n < 5; n++) {
            var bot = mock(Character.class);
            when(bot.getId()).thenReturn(n + 1);
            when(bot.getClient()).thenReturn(mock(BotClient.class));
            when(bot.getEventInstance()).thenReturn(event);
            when(bot.getMap()).thenReturn(map);
            when(bot.getMapId()).thenReturn(922010500);
            when(bot.getPosition()).thenReturn(new Point());
            crew.add(bot);
        }
        when(crew.get(2).getSkillLevel(ILWizard.TELEPORT)).thenReturn(1);
        when(crew.get(3).getSkillLevel(Rogue.DARK_SIGHT)).thenReturn(20);
        var players = new ArrayList<>(crew);
        players.add(human);
        when(event.getPlayers()).thenReturn(players);
        BotLpqHuman.register(human, crew);
        var bot = crew.getFirst();
        var entry = new BotEntry(bot, bot, null);
        var manager = mock(BotManager.class);
        when(manager.issueDropItem(entry, InventoryType.ETC, BotLpqRun.PASS, (short) 4)).thenReturn(true);
        try (var travel = mockStatic(BotTravelManager.class); var managers = mockStatic(BotManager.class)) {
            managers.when(BotManager::getInstance).thenReturn(manager);
            BotLpqVaults.tick(entry, bot, true);
            travel.verify(() -> BotTravelManager.tickTravel(entry, bot, 922010502, 2, true, false));
            when(bot.getMapId()).thenReturn(922010502);
            when(bot.getItemQuantity(BotLpqRun.PASS, false)).thenReturn(4);
            BotLpqVaults.tick(entry, bot, true);
            travel.verify(() -> BotTravelManager.tickTravel(entry, bot, 922010500, 2, true, false));
            when(bot.getMapId()).thenReturn(922010500);
            BotLpqVaults.tick(entry, bot, true);
            verify(manager).issueDropItem(entry, InventoryType.ETC, BotLpqRun.PASS, (short) 4);
            when(bot.getItemQuantity(BotLpqRun.PASS, false)).thenReturn(0);
            BotLpqVaults.tick(entry, bot, true);
            travel.verify(() -> BotTravelManager.tickTravel(entry, bot, 922010505, 2, true, false));
        } finally { crew.forEach(BotLpqHuman::unregister); }
    }
}
