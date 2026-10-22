package server.bots;

import client.Character;
import org.junit.jupiter.api.Test;
import server.maps.MapItem;
import server.maps.MapleMap;
import server.maps.Reactor;
import java.awt.Point;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotLpqBonusLootTest {
    @Test void bonusBlocksEquipmentConsumablesMesosAndRedrops() {
        var bot=mock(Character.class);var human=mock(Character.class);var entry=new BotEntry(bot,human,null);
        var drop=mock(MapItem.class);when(drop.canBePickedBy(bot)).thenReturn(true);
        when(bot.getMapId()).thenReturn(922011000);BotLpqHuman.register(human,List.of(bot));
        try {
            for(int id:new int[]{1302000,2000000,4000000,100}) {
                when(drop.getItemId()).thenReturn(id);
                assertFalse(BotLootEligibility.canBotLoot(entry,bot,drop));
            }
            when(drop.isPlayerDrop()).thenReturn(true);
            assertFalse(BotLootEligibility.canBotLoot(entry,bot,drop));
        } finally { BotLpqHuman.unregister(bot); }
    }
    @Test void crewStillOpensNearbyBonusBox() {
        var bot=mock(Character.class);var map=mock(MapleMap.class);var box=mock(Reactor.class);
        var entry=new BotEntry(bot,bot,null);var manager=mock(BotManager.class);
        when(bot.getId()).thenReturn(981234);when(bot.getMapId()).thenReturn(922011000);
        when(bot.getMap()).thenReturn(map);when(bot.getPosition()).thenReturn(new Point());
        when(map.getAllReactors()).thenReturn(List.of(box));when(box.isActive()).thenReturn(true);
        when(box.getId()).thenReturn(2202004);when(box.getPosition()).thenReturn(new Point());
        try(var managers=mockStatic(BotManager.class);var actions=mockStatic(BotZakumPqRun.class)) {
            managers.when(BotManager::getInstance).thenReturn(manager);
            assertTrue(BotLpqLater.tick(entry,bot,true));
            actions.verify(()->BotZakumPqRun.hitReactor(bot,box));
        }
    }
}
