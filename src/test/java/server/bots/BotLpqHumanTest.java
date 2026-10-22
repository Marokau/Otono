package server.bots;

import client.Character;
import client.Client;
import client.inventory.InventoryType;
import net.server.PlayerStorage;
import net.server.world.Party;
import net.server.world.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;

import java.awt.Point;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotLpqHumanTest {
    final Character human = mock(Character.class);
    final Character bot = mock(Character.class);
    final BotEntry entry = new BotEntry(bot, bot, null);

    @AfterEach void cleanup() { BotLpqHuman.unregister(bot); }

    void register() {
        when(human.getId()).thenReturn(100);
        when(bot.getId()).thenReturn(1);
        when(human.getPosition()).thenReturn(new Point());
        when(bot.getPosition()).thenReturn(new Point());
        BotLpqHuman.register(human, List.of(bot));
    }

    @Test void deliveredPassPileIsNotPickedBackUpWhileTheHumanReadsTheNpc() {
        register();
        when(bot.getMapId()).thenReturn(922010100);
        when(human.getMapId()).thenReturn(922010100);
        when(bot.getItemQuantity(BotLpqRun.PASS, false)).thenReturn(20);
        when(human.getItemQuantity(BotLpqRun.PASS, false)).thenReturn(5);
        var manager = mock(BotManager.class);
        when(manager.issueDropItem(entry, InventoryType.ETC, BotLpqRun.PASS, (short) 20)).thenReturn(true);
        try (var managers = mockStatic(BotManager.class); var travel = mockStatic(BotTravelManager.class)) {
            managers.when(BotManager::getInstance).thenReturn(manager);
            assertTrue(BotLpqHuman.deliver(entry, bot, BotLpqRun.PASS, 25, true));
            when(bot.getItemQuantity(BotLpqRun.PASS, false)).thenReturn(0);
            assertFalse(BotLpqHuman.canCollect(bot));
            assertTrue(BotLpqHuman.deliver(entry, bot, BotLpqRun.PASS, 25, true));
            verify(manager, times(1)).issueDropItem(entry, InventoryType.ETC, BotLpqRun.PASS, (short) 20);
        }
    }

    @Test void vaultDeliveryPilesAreReservedForTheHumanEvenForTheCollector() {
        register();
        when(bot.getMapId()).thenReturn(922010500);
        assertTrue(BotLpqHuman.isCollector(bot));
        assertFalse(BotLpqHuman.canCollect(bot));
    }

    EventInstanceManager liveParty() {
        var world = mock(World.class);
        var storage = mock(PlayerStorage.class);
        var party = mock(Party.class);
        var event = mock(EventInstanceManager.class);
        var humanClient = mock(Client.class);
        var botClient = mock(Client.class);
        when(human.getWorldServer()).thenReturn(world);
        when(world.getPlayerStorage()).thenReturn(storage);
        when(storage.getCharacterById(100)).thenReturn(human);
        when(human.getClient()).thenReturn(humanClient);
        when(bot.getClient()).thenReturn(botClient);
        when(human.getParty()).thenReturn(party);
        when(bot.getParty()).thenReturn(party);
        when(party.getLeaderId()).thenReturn(100);
        when(human.getEventInstance()).thenReturn(event);
        when(bot.getEventInstance()).thenReturn(event);
        when(bot.getMapId()).thenReturn(922010100);
        when(human.getMapId()).thenReturn(922010100);
        return event;
    }

    @Test void clearedStageWaitsForHumanPortalUseThenTakesTheNormalRoute() {
        register();
        var event = liveParty();
        when(event.getProperty("1stageclear")).thenReturn("true");
        var manager = mock(BotManager.class);
        try (var managers = mockStatic(BotManager.class); var travel = mockStatic(BotTravelManager.class)) {
            managers.when(BotManager::getInstance).thenReturn(manager);
            assertTrue(BotLpqHuman.tick(entry, bot, true));
            travel.verify(() -> BotTravelManager.tickTravel(entry, bot, 922010200, 2, true, false), never());
            when(human.getMapId()).thenReturn(922010200);
            assertTrue(BotLpqHuman.tick(entry, bot, true));
            travel.verify(() -> BotTravelManager.tickTravel(entry, bot, 922010200, 2, true, false));
        }
    }

    @Test void reenteringWithTheSameCrewClearsThePreviousRunsDeliveryState() {
        register();
        var event = liveParty();
        when(event.getProperty("1stageclear")).thenReturn("true");
        when(bot.getItemQuantity(BotLpqRun.PASS, false)).thenReturn(25);
        var manager = mock(BotManager.class);
        when(manager.issueDropItem(entry, InventoryType.ETC, BotLpqRun.PASS, (short) 25)).thenReturn(true);
        try (var managers = mockStatic(BotManager.class); var travel = mockStatic(BotTravelManager.class)) {
            managers.when(BotManager::getInstance).thenReturn(manager);
            BotLpqHuman.tick(entry, bot, true);
            assertTrue(BotLpqHuman.deliver(entry, bot, BotLpqRun.PASS, 25, true));
            assertFalse(BotLpqHuman.canCollect(bot));
            var nextEvent = mock(EventInstanceManager.class);
            when(nextEvent.getProperty("1stageclear")).thenReturn("true");
            when(bot.getEventInstance()).thenReturn(nextEvent);
            when(human.getEventInstance()).thenReturn(nextEvent);
            BotLpqHuman.tick(entry, bot, true);
            assertTrue(BotLpqHuman.canCollect(bot));
        }
    }

    @Test void unrelatedCharactersCannotDismissAnotherHumansCrew() {
        register();
        assertTrue(BotLpqHuman.belongsTo(bot, 100));
        assertFalse(BotLpqHuman.belongsTo(bot, 101));
    }
}
