package server.bots;

import client.Character;
import client.Client;
import client.BotClient;
import client.inventory.InventoryType;
import net.server.PlayerStorage;
import net.server.world.Party;
import net.server.world.World;
import org.junit.jupiter.api.*;
import scripting.event.EventInstanceManager;
import java.awt.Point;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotOpqHumanTest {
    final Character human=mock(Character.class), bot=mock(Character.class), other=mock(Character.class);
    final BotEntry entry=new BotEntry(bot,bot,null);
    final EventInstanceManager event=mock(EventInstanceManager.class);
    @BeforeEach void setup() {
        when(human.getId()).thenReturn(100);when(bot.getId()).thenReturn(1);when(other.getId()).thenReturn(2);
        when(human.getPosition()).thenReturn(new Point());when(bot.getPosition()).thenReturn(new Point());
        when(bot.getMapId()).thenReturn(920010200);when(human.getMapId()).thenReturn(920010200);
        BotOpqHuman.register(human,List.of(bot,other));
    }
    @AfterEach void cleanup(){BotOpqHuman.unregister(bot);BotOpqHuman.unregister(other);}
    void liveParty() {
        var world=mock(World.class);var storage=mock(PlayerStorage.class);var party=mock(Party.class);
        when(human.getWorldServer()).thenReturn(world);when(world.getPlayerStorage()).thenReturn(storage);
        when(storage.getCharacterById(100)).thenReturn(human);
        when(human.getClient()).thenReturn(mock(Client.class));when(bot.getClient()).thenReturn(mock(BotClient.class));
        when(bot.getParty()).thenReturn(party);when(human.getParty()).thenReturn(party);when(party.getLeaderId()).thenReturn(100);
        when(bot.getEventInstance()).thenReturn(event);when(human.getEventInstance()).thenReturn(event);
    }
    org.mockito.MockedStatic<BotOpqRun> actions() {
        var actions=mockStatic(BotOpqRun.class);actions.when(()->BotOpqRun.hold(entry,bot,true)).thenReturn(true);return actions;
    }
    @Test void realStagePiecesAreDeliveredOnceAndReservedForTheHuman() {
        when(bot.getItemQuantity(4001050,false)).thenReturn(30);
        var manager=mock(BotManager.class);when(manager.issueDropItem(entry,InventoryType.ETC,4001050,(short)30)).thenReturn(true);
        try(var managers=mockStatic(BotManager.class);var run=actions()) {
            managers.when(BotManager::getInstance).thenReturn(manager);
            assertTrue(BotOpqHuman.deliver(entry,bot,4001050,30,true));
            assertTrue(BotOpqHuman.deliver(entry,bot,4001050,30,true));
            assertFalse(BotOpqHuman.canCollect(bot,4001050));
            assertTrue(BotOpqHuman.canCollect(bot,4001044));
            assertFalse(BotOpqHuman.isCollector(other));
            verify(manager,times(1)).issueDropItem(entry,InventoryType.ETC,4001050,(short)30);
            verify(human,never()).changeMap(anyInt());
        }
    }
    @Test void npcChecksAreLeftToTheHumanAndMinervaWaitsForTheirExitChoice() {
        try(var run=actions()) {
            assertTrue(BotOpqHuman.npc(entry,bot,2013001,true));
            when(bot.getMapId()).thenReturn(BotOpqRun.HUB);when(human.getMapId()).thenReturn(BotOpqRun.HUB);
            assertTrue(BotOpqHuman.npc(entry,bot,2013002,true));
            when(human.getMapId()).thenReturn(920011300);
            assertFalse(BotOpqHuman.npc(entry,bot,2013002,true));
        }
    }
    @Test void loungeCollectorCanLeaveItsFourthSubroomWithAllFortyPieces() {
        liveParty();when(bot.getMapId()).thenReturn(920010604);when(human.getMapId()).thenReturn(920010600);
        when(bot.getItemQuantity(4001052,false)).thenReturn(40);
        try(var run=actions()) {
            run.when(()->BotOpqRun.inInstance(bot)).thenReturn(true);
            assertFalse(BotOpqHuman.beforeTick(entry,bot,true));
            assertTrue(BotOpqHuman.canCollect(bot,4001052));
        }
    }
    @Test void statuePieceHandoffReleasesTheHubCollectorAfterTheHumanDropsIt() {
        liveParty();when(bot.getMapId()).thenReturn(BotOpqRun.HUB);when(human.getMapId()).thenReturn(BotOpqRun.HUB);
        when(human.getItemQuantity(4001044,false)).thenReturn(1);
        try(var run=actions()) {
            run.when(()->BotOpqRun.inInstance(bot)).thenReturn(true);
            assertTrue(BotOpqHuman.beforeTick(entry,bot,true));
            when(human.getItemQuantity(4001044,false)).thenReturn(0);
            assertFalse(BotOpqHuman.beforeTick(entry,bot,true));
            assertTrue(BotOpqHuman.canCollect(bot,4001044));
        }
    }
    @Test void crewWaitsForHumanRoomEntryAndNewInstanceClearsDeliveryState() {
        liveParty();when(human.getMapId()).thenReturn(BotOpqRun.HUB);
        try(var run=actions()) {
            run.when(()->BotOpqRun.inInstance(bot)).thenReturn(true);
            assertTrue(BotOpqHuman.beforeTick(entry,bot,true));
            when(human.getMapId()).thenReturn(920010200);
            assertFalse(BotOpqHuman.beforeTick(entry,bot,true));
            when(bot.getItemQuantity(4001050,false)).thenReturn(30);var manager=mock(BotManager.class);
            when(manager.issueDropItem(entry,InventoryType.ETC,4001050,(short)30)).thenReturn(true);
            try(var managers=mockStatic(BotManager.class)){
                managers.when(BotManager::getInstance).thenReturn(manager);
                BotOpqHuman.deliver(entry,bot,4001050,30,true);assertFalse(BotOpqHuman.canCollect(bot,4001050));
                var next=mock(EventInstanceManager.class);when(bot.getEventInstance()).thenReturn(next);when(human.getEventInstance()).thenReturn(next);
                when(bot.getItemQuantity(4001050,false)).thenReturn(0);
                BotOpqHuman.beforeTick(entry,bot,true);assertTrue(BotOpqHuman.canCollect(bot,4001050));
            }
        }
    }
    @Test void onlyOneDesignatedObserverAdvancesOnceForARealWrongEffect() throws Exception {
        when(bot.getMapId()).thenReturn(920010500);when(other.getMapId()).thenReturn(920010500);when(human.getMapId()).thenReturn(920010500);
        BotOpqHuman.puzzleReady(bot,0,true);
        var field=BotOpqHuman.class.getDeclaredField("RUNS");field.setAccessible(true);var crew=((Map<?,?>)field.get(null)).get(bot);
        var since=crew.getClass().getDeclaredField("readySince");since.setAccessible(true);since.setLong(crew,System.currentTimeMillis()-2000);
        BotOpqHuman.puzzleReady(bot,0,true);
        var wrong=tools.PacketCreator.showEffect("quest/party/wrong_kor");
        BotOpqHuman.observeEffect(other,wrong);assertEquals(0,BotOpqHuman.consumeWrong(bot,920010500));
        BotOpqHuman.observeEffect(bot,wrong);BotOpqHuman.observeEffect(bot,wrong);
        assertEquals(1,BotOpqHuman.consumeWrong(bot,920010500));assertEquals(0,BotOpqHuman.consumeWrong(bot,920010500));
        verify(event,never()).getProperty("stage4_0");verify(event,never()).getProperty("stage6_c");
    }
    @Test void crewOwnershipIsIsolated(){assertTrue(BotOpqHuman.belongsTo(bot,100));assertFalse(BotOpqHuman.belongsTo(bot,101));}
}
