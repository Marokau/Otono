package server.bots;

import client.Character;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import server.maps.MapItem;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotLpqSevenTriggerTest {
    @Test void ratzTriggerMustRemainForReactorInsteadOfEnteringCollectorInventory() {
        var bot=mock(Character.class);var drop=mock(MapItem.class);
        var entry=new BotEntry(bot,bot,null);
        when(bot.getMapId()).thenReturn(922010700);
        when(bot.getEventInstance()).thenReturn(mock(EventInstanceManager.class));
        when(drop.canBePickedBy(bot)).thenReturn(true);when(drop.getItemId()).thenReturn(4001156);
        assertFalse(BotLootEligibility.canBotLoot(entry,bot,drop));
        var map=mock(server.maps.MapleMap.class);
        when(map.getMapObject(drop.getObjectId())).thenReturn(drop);
        assertFalse(BotLootEligibility.canBotTargetLoot(entry,bot,map,drop,System.currentTimeMillis()));
    }
    @Test void stageSevenDoesNotAnnounceBossLootForRombardWave() {
        var bot=mock(Character.class);var entry=new BotEntry(bot,bot,null);
        when(bot.getMapId()).thenReturn(922010700);
        when(bot.getEventInstance()).thenReturn(mock(EventInstanceManager.class));
        try(var chat=mockStatic(BotPqChat.class)) {
            BotPqBossLoot.tick(entry,bot);
            chat.verifyNoInteractions();
        }
    }
}
