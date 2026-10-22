package server.bots;

import client.BotClient;
import client.Character;
import client.Client;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import server.maps.MapItem;
import server.maps.MapleMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotPqDropPolicyTest {
    @Test void ellinMarbleAndStoneHandoffsStayOnlyWithTheirActiveHumanCrew() {
        var map=mock(MapleMap.class);var drop=mock(MapItem.class);var bot=mock(Character.class);var human=mock(Character.class);
        var manager=mock(BotManager.class);var entry=new BotEntry(bot,human,null);
        when(drop.isPlayerDrop()).thenReturn(true);when(drop.getDropper()).thenReturn(bot);when(bot.getMap()).thenReturn(map);
        when(manager.getEntryByBotCharId(bot.getId())).thenReturn(entry);
        try(var managers=mockStatic(BotManager.class);var ellin=mockStatic(BotEllinRun.class)) {
            managers.when(BotManager::getInstance).thenReturn(manager);
            ellin.when(()->BotEllinRun.inInstance(bot)).thenReturn(true);
            ellin.when(()->BotEllinRun.belongsTo(bot,human.getId())).thenReturn(true);
            when(drop.getItemId()).thenReturn(4001169);assertTrue(BotPqDropPolicy.keepsForHandoff(map,drop));
            when(drop.getItemId()).thenReturn(4001163);assertTrue(BotPqDropPolicy.keepsForHandoff(map,drop));
            ellin.when(()->BotEllinRun.inInstance(bot)).thenReturn(false);assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
            ellin.when(()->BotEllinRun.inInstance(bot)).thenReturn(true);
            when(bot.getMap()).thenReturn(mock(MapleMap.class));assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
            when(bot.getMap()).thenReturn(map);when(drop.getItemId()).thenReturn(4001162);assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
        }
    }
    @Test void finalStageProtectsMonsterPassesOnlyWithActiveHumanLedCrew() {
        var map=mock(MapleMap.class);var drop=mock(MapItem.class);var bot=mock(Character.class);
        var manager=mock(BotManager.class);var entry=new BotEntry(bot,bot,null);
        entry.kpq.humanLed=true;
        when(map.getId()).thenReturn(BotKpqRun.FIRST_MAP+4);
        when(map.getCharacters()).thenReturn(java.util.List.of(bot));
        when(drop.getItemId()).thenReturn(BotLootEligibility.KPQ_PASS);
        when(bot.getClient()).thenReturn(new BotClient(0,1));when(bot.getMap()).thenReturn(map);
        when(bot.getEventInstance()).thenReturn(mock(EventInstanceManager.class));
        when(manager.getEntryByBotCharId(bot.getId())).thenReturn(entry);
        try(var managers=mockStatic(BotManager.class)) {
            managers.when(BotManager::getInstance).thenReturn(manager);
            assertTrue(BotPqDropPolicy.keepsForHandoff(map,drop));
            when(drop.isPlayerDrop()).thenReturn(true);
            assertTrue(BotPqDropPolicy.keepsForHandoff(map,drop));
            when(drop.getItemId()).thenReturn(BotLootEligibility.KPQ_COUPON);
            assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
            when(drop.getItemId()).thenReturn(BotLootEligibility.KPQ_PASS);
            entry.kpq.humanLed=false;assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
            entry.kpq.humanLed=true;
            when(bot.getEventInstance()).thenReturn(null);assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
            when(map.getCharacters()).thenReturn(java.util.List.of());assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
        }
    }
    @Test void protectsOnlyActiveHumanLedKpqBotPassHandoffs() {
        var map=mock(MapleMap.class);var drop=mock(MapItem.class);var bot=mock(Character.class);
        var manager=mock(BotManager.class);var entry=new BotEntry(bot,bot,null);
        entry.kpq.humanLed=true;
        when(map.getId()).thenReturn(BotKpqRun.FIRST_MAP);
        when(drop.getItemId()).thenReturn(BotLootEligibility.KPQ_PASS);
        when(drop.isPlayerDrop()).thenReturn(true);when(drop.getDropper()).thenReturn(bot);
        when(bot.getClient()).thenReturn(new BotClient(0,1));when(bot.getMap()).thenReturn(map);
        when(bot.getEventInstance()).thenReturn(mock(EventInstanceManager.class));
        when(manager.getEntryByBotCharId(bot.getId())).thenReturn(entry);
        try(var managers=mockStatic(BotManager.class)) {
            managers.when(BotManager::getInstance).thenReturn(manager);
            assertTrue(BotPqDropPolicy.keepsForHandoff(map,drop));
            entry.kpq.humanLed=false;assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
            entry.kpq.humanLed=true;
            when(bot.getEventInstance()).thenReturn(null);assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
            when(bot.getEventInstance()).thenReturn(mock(EventInstanceManager.class));
            when(bot.getMap()).thenReturn(mock(MapleMap.class));assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
            when(bot.getMap()).thenReturn(map);
            when(drop.getItemId()).thenReturn(BotLootEligibility.KPQ_COUPON);assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
            when(drop.getItemId()).thenReturn(BotLootEligibility.KPQ_PASS);
            when(drop.isPlayerDrop()).thenReturn(false);assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
            when(drop.isPlayerDrop()).thenReturn(true);
            when(bot.getClient()).thenReturn(mock(Client.class));assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
            when(bot.getClient()).thenReturn(new BotClient(0,1));
            when(map.getId()).thenReturn(BotLpqRun.FIRST_MAP);assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
        }
    }

    @Test void mpqPlayerMesoMarkersPersistOnlyDuringTheirActivePuzzleInstance() {
        var map=mock(MapleMap.class);var drop=mock(MapItem.class);var event=mock(EventInstanceManager.class);
        when(map.getEventInstance()).thenReturn(event);when(drop.isPlayerDrop()).thenReturn(true);when(drop.getMeso()).thenReturn(10);
        for(int base:new int[]{926100300,926110300}) {
            for(int room=0;room<=4;room++) { when(map.getId()).thenReturn(base+room);assertTrue(BotPqDropPolicy.keepsForHandoff(map,drop)); }
        }
        when(event.isEventCleared()).thenReturn(true);assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
        when(event.isEventCleared()).thenReturn(false);when(map.getEventInstance()).thenReturn(null);assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
        when(map.getEventInstance()).thenReturn(event);when(drop.isPlayerDrop()).thenReturn(false);assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
        when(drop.isPlayerDrop()).thenReturn(true);when(drop.getMeso()).thenReturn(0);assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
        when(drop.getMeso()).thenReturn(100);assertTrue(BotPqDropPolicy.keepsForHandoff(map,drop));
        when(map.getId()).thenReturn(926100401);assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
    }

    @Test void mpqLabKeysAndFilesSurviveUntilTheAttemptEnds() {
        var map=mock(MapleMap.class);var drop=mock(MapItem.class);var event=mock(EventInstanceManager.class);
        when(map.getEventInstance()).thenReturn(event);
        for(int base:new int[]{926100200,926110200})for(int room=0;room<3;room++) {
            when(map.getId()).thenReturn(base+room);
            for(int item:new int[]{4001133,4001134,4001135}) {when(drop.getItemId()).thenReturn(item);assertTrue(BotPqDropPolicy.keepsForHandoff(map,drop));}
        }
        when(event.isEventCleared()).thenReturn(true);assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
        when(event.isEventCleared()).thenReturn(false);when(map.getEventInstance()).thenReturn(null);assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
        when(map.getEventInstance()).thenReturn(event);when(drop.getItemId()).thenReturn(2002023);assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
        when(drop.getItemId()).thenReturn(4001134);when(map.getId()).thenReturn(926100401);assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
    }
}
