package server.bots;

import client.BotClient;
import client.Character;
import client.Client;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import server.life.Monster;
import server.maps.MapItem;
import server.maps.MapleMap;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotPqBossLootTest {
    @Test void originalBossLootIsReservedButHumanRedropsAndPopulationAreUnaffected() {
        var bot=mock(Character.class);var human=mock(Character.class);var boss=mock(Monster.class);
        var event=mock(EventInstanceManager.class);var drop=mock(MapItem.class);
        var entry=new BotEntry(bot,human,null);
        when(human.getClient()).thenReturn(mock(Client.class));when(bot.getClient()).thenReturn(new BotClient(0,1));
        when(bot.getEventInstance()).thenReturn(event);when(human.getEventInstance()).thenReturn(event);
        when(drop.getDropper()).thenReturn(boss);when(boss.isBoss()).thenReturn(true);
        when(drop.getItemId()).thenReturn(1302000);when(drop.canBePickedBy(bot)).thenReturn(true);
        assertFalse(BotPqBossLoot.reserves(entry,bot,drop));
        BotPqDialogue.register(bot);
        assertTrue(BotPqBossLoot.reserves(entry,bot,drop));
        assertFalse(BotLootEligibility.canBotLoot(entry,bot,drop));
        when(drop.isPlayerDrop()).thenReturn(true);when(drop.getDropper()).thenReturn(human);
        assertFalse(BotPqBossLoot.reserves(entry,bot,drop));
        when(bot.needQuestItem(drop.getQuest(),1302000)).thenReturn(true);
        assertTrue(BotLootEligibility.canBotLoot(entry,bot,drop));
        when(drop.isPlayerDrop()).thenReturn(false);when(drop.getDropper()).thenReturn(boss);
        when(boss.isBoss()).thenReturn(false);assertFalse(BotPqBossLoot.reserves(entry,bot,drop));
        when(boss.isBoss()).thenReturn(true);when(human.getEventInstance()).thenReturn(null);
        assertFalse(BotPqBossLoot.reserves(entry,bot,drop));
    }
    @Test void bossLootAnnouncementIsOncePerBossAcrossRepeatedTicks() {
        var bot=mock(Character.class);var human=mock(Character.class);var boss=mock(Monster.class);
        var event=mock(EventInstanceManager.class);var drop=mock(MapItem.class);var map=mock(MapleMap.class);
        var entry=new BotEntry(bot,human,null);
        when(human.getClient()).thenReturn(mock(Client.class));when(bot.getClient()).thenReturn(new BotClient(0,1));
        when(bot.getId()).thenReturn(1);when(bot.getEventInstance()).thenReturn(event);
        when(human.getEventInstance()).thenReturn(event);when(event.getPlayers()).thenReturn(List.of(bot,human));
        when(bot.getMap()).thenReturn(map);when(map.getDroppedItems()).thenReturn(List.of(drop));
        when(drop.getDropper()).thenReturn(boss);when(boss.isBoss()).thenReturn(true);
        BotPqDialogue.register(bot);
        try(var chat=mockStatic(BotPqChat.class)) {
            BotPqBossLoot.tick(entry,bot);BotPqBossLoot.tick(entry,bot);
            chat.verify(()->BotPqChat.queue(entry,"The boss loot is yours! Take your time picking it up. Drop any gear you don't want if you'd like us to have it.",false),times(1));
        }
    }
}
