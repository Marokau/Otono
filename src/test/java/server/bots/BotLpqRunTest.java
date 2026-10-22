package server.bots;

import client.Character;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import server.maps.MapItem;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotLpqRunTest {
    @Test void boxReadinessUsesNormalBotSpeechWhileClearsRemainPink() {
        var bot=mock(Character.class);var human=mock(Character.class);
        var entry=new BotEntry(bot,human,null);var manager=mock(BotManager.class);
        when(manager.getEntryByBotCharId(bot.getId())).thenReturn(entry);
        BotLpqHuman.register(human,List.of(bot));
        try(var managers=mockStatic(BotManager.class);var chat=mockStatic(BotPqChat.class)) {
            managers.when(BotManager::getInstance).thenReturn(manager);
            BotLpqHuman.notice(bot,"boxes-0","Box arrangement 1 is ready.");
            BotLpqHuman.notice(bot,"boxes-0","Box arrangement 1 is ready.");
            chat.verify(()->BotPqChat.queue(entry,"Box arrangement 1 is ready.",true),times(1));
            verify(human,never()).dropMessage(anyInt(),eq("Box arrangement 1 is ready."));
            BotLpqHuman.notice(bot,"clear-922010800","LPQ: Stage clear!");
            verify(human).dropMessage(5,"LPQ: Stage clear!");
        } finally { BotLpqHuman.unregister(bot); }
    }
    @Test
    void mapIdAloneDoesNotEstablishInstanceMembership() {
        var bot = mock(Character.class);
        when(bot.getMapId()).thenReturn(BotLpqRun.FIRST_MAP);
        assertFalse(BotLpqRun.inInstance(bot));
        when(bot.getEventInstance()).thenReturn(mock(EventInstanceManager.class));
        assertTrue(BotLpqRun.inInstance(bot));
    }

    @Test
    void onlyRegisteredLeaderCanLootLpqPasses() {
        var bot = mock(Character.class);
        var event = mock(EventInstanceManager.class);
        when(bot.getMapId()).thenReturn(BotLpqRun.FIRST_MAP);
        when(bot.getEventInstance()).thenReturn(event);
        var drop = mock(MapItem.class);
        when(drop.getItemId()).thenReturn(BotLpqRun.PASS);
        when(drop.canBePickedBy(bot)).thenReturn(true);
        var entry = new BotEntry(bot, bot, null);
        assertFalse(BotLootEligibility.canBotLoot(entry, bot, drop));
        when(event.isEventLeader(bot)).thenReturn(true);
        assertTrue(BotLootEligibility.canBotLoot(entry, bot, drop));
    }

    @Test
    void doesNotTakeControlOfAnUnrelatedBot() {
        var bot = mock(Character.class);
        var entry = new BotEntry(bot, bot, null);
        assertFalse(BotLpqRun.tick(entry, bot, true));
        assertThrows(IllegalArgumentException.class, () -> BotLpqRun.startCrew(List.of(bot)));
    }
}
