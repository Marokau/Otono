package server.bots;

import client.BotClient;
import client.Character;
import net.packet.Packet;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import tools.PacketCreator;
import server.maps.MapleMap;
import java.awt.Point;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotKpqRunTest {
    @Test
    void bossStageWorkersWaitForUpperPassThenSeekSlimeAfterPickup() {
        var bot=mock(Character.class);var leader=mock(Character.class);
        var event=mock(EventInstanceManager.class);var map=mock(MapleMap.class);
        var slime=mock(server.life.Monster.class);var drop=mock(server.maps.MapItem.class);
        var pass=mock(client.inventory.Item.class);
        when(bot.getMapId()).thenReturn(BotKpqRun.FIRST_MAP+4);
        when(bot.getEventInstance()).thenReturn(event);when(bot.getMap()).thenReturn(map);
        when(bot.getPosition()).thenReturn(new Point(-246,-1455));
        when(event.getPlayers()).thenReturn(List.of(leader,bot));when(event.isEventLeader(leader)).thenReturn(true);
        when(map.getDroppedItems()).thenReturn(List.of(drop));when(drop.getItem()).thenReturn(pass);
        when(pass.getItemId()).thenReturn(4001008);when(drop.canBePickedBy(bot)).thenReturn(true);
        when(drop.getPosition()).thenReturn(new Point(-230,-2175));
        when(map.getAllMonsters()).thenReturn(List.of(slime));when(slime.isAlive()).thenReturn(true);
        when(slime.getPosition()).thenReturn(new Point(481,-438));
        var entry=new BotEntry(bot,leader,null);entry.kpq.mapId=BotKpqRun.FIRST_MAP+4;
        var movement=BotTravelManager.movementStep;
        var targets=new java.util.ArrayList<Point>();
        BotTravelManager.movementStep=(e,p,ai)->targets.add(p);
        try(var managers=mockStatic(BotManager.class);var combat=mockStatic(BotCombatManager.class)) {
            managers.when(BotManager::getInstance).thenReturn(mock(BotManager.class));
            assertTrue(BotKpqRun.tick(entry,bot,true));
            assertTrue(targets.isEmpty());
            when(map.getDroppedItems()).thenReturn(List.of());
            assertTrue(BotKpqRun.tick(entry,bot,true));
            assertEquals(List.of(slime.getPosition()),targets);
        } finally { BotTravelManager.movementStep=movement; }
    }
    @Test
    void waitsForHumanLeaderBeforeTakingAnOpenedPortal() {
        Character bot = mock(Character.class);
        Character human = mock(Character.class);
        var event = mock(EventInstanceManager.class);
        var world = mock(net.server.world.World.class, RETURNS_DEEP_STUBS);
        var party = mock(net.server.world.Party.class);
        when(bot.getMapId()).thenReturn(BotKpqRun.FIRST_MAP);
        when(human.getMapId()).thenReturn(BotKpqRun.FIRST_MAP);
        when(bot.getEventInstance()).thenReturn(event);
        when(bot.getWorldServer()).thenReturn(world);
        when(world.getPlayerStorage().getCharacterById(99)).thenReturn(human);
        when(bot.getParty()).thenReturn(party);
        when(human.getParty()).thenReturn(party);
        when(human.getId()).thenReturn(99);
        when(party.getLeaderId()).thenReturn(99);
        when(event.getPlayers()).thenReturn(List.of(human, bot));
        when(event.isEventLeader(human)).thenReturn(true);
        when(event.getProperty("1stageclear")).thenReturn("true");
        when(bot.getPosition()).thenReturn(new Point(0, 0));
        BotEntry entry = new BotEntry(bot, bot, null);
        entry.kpq.humanLed = true;
        entry.kpq.leaderId = 99;
        entry.kpq.mapId = BotKpqRun.FIRST_MAP;
        var manager = mock(BotManager.class);
        try (var managers = mockStatic(BotManager.class);
             var travel = mockStatic(BotTravelManager.class, CALLS_REAL_METHODS)) {
            managers.when(BotManager::getInstance).thenReturn(manager);
            assertTrue(BotKpqRun.tick(entry, bot, true));
            verify(manager).stepMovementCore(entry, bot.getPosition(), true);
            travel.verify(() -> BotTravelManager.tickTravel(entry, bot, BotKpqRun.FIRST_MAP + 1, 2, true, false), never());
            when(human.getMapId()).thenReturn(BotKpqRun.FIRST_MAP + 1);
            travel.when(() -> BotTravelManager.tickTravel(entry, bot, BotKpqRun.FIRST_MAP + 1, 2, true, false)).thenReturn(true);
            travel.clearInvocations();
            assertTrue(BotKpqRun.tick(entry, bot, true));
            travel.verify(() -> BotTravelManager.tickTravel(entry, bot, BotKpqRun.FIRST_MAP + 1, 2, true, false));
        }
    }

    @Test
    void refusesToReplaceAnExistingHumanParty() {
        Character human = mock(Character.class);
        when(human.getMapId()).thenReturn(103000000);
        when(human.getLevel()).thenReturn(25);
        var party = mock(net.server.world.Party.class);
        when(human.getParty()).thenReturn(party);
        when(human.getId()).thenReturn(99);
        when(party.getLeaderId()).thenReturn(12);
        try (var factory = mockStatic(BotPqCrewFactory.class)) {
            assertTrue(BotKpqCompanions.assemble(human).contains("leave your current party"));
            factory.verifyNoInteractions();
        }
    }

    @Test
    void releasesConsumedCouponDestinationBeforeReturningToCombat() {
        Character bot = mock(Character.class);
        Character leader = mock(Character.class);
        var event = mock(EventInstanceManager.class);
        var map = mock(MapleMap.class);
        when(bot.getMapId()).thenReturn(BotKpqRun.FIRST_MAP);
        when(bot.getEventInstance()).thenReturn(event);
        when(bot.getMap()).thenReturn(map);
        when(event.getPlayers()).thenReturn(List.of(leader, bot));
        when(event.isEventLeader(leader)).thenReturn(true);
        when(map.getDroppedItems()).thenReturn(List.of());
        BotEntry entry = new BotEntry(bot, bot, null);
        entry.kpq.mapId = BotKpqRun.FIRST_MAP;
        entry.kpq.couponTarget = 25;
        entry.moveTarget = entry.followTravelMoveTarget = new Point(100, 100);
        var manager = mock(BotManager.class);
        try (var managers = mockStatic(BotManager.class)) {
            managers.when(BotManager::getInstance).thenReturn(manager);
            assertTrue(BotKpqRun.tick(entry, bot, true));
            assertNull(entry.moveTarget);
            verify(manager).tickPqGrinding(entry, bot, true);
        }
    }
    @Test
    void readsTheNpcDialogueActuallySentToTheHeadlessClient() {
        Packet packet = PacketCreator.getNPCTalk(9020001, (byte) 0,
                "Collect the same number of coupons as the minimum level required to advance to 2nd job.",
                "00 01", (byte) 0);
        var message = BotNpcInteraction.read(packet);
        assertEquals(9020001, message.npc());
        assertEquals(0, message.type());
        assertEquals(30, BotKpqRun.couponAnswer(message.text()));
        assertNull(BotNpcInteraction.read(() -> new byte[0]));
    }

    @Test
    void answersTheActualClotoQuestionsAndRejectsUnknownText() {
        assertEquals(10, BotKpqRun.couponAnswer("minimum level required to make the first job advancement as warrior."));
        assertEquals(35, BotKpqRun.couponAnswer("minimum amount of STR needed to make the first job advancement as a warrior."));
        assertEquals(20, BotKpqRun.couponAnswer("minimum amount of INT needed to make the first job advancement as a magician."));
        assertEquals(25, BotKpqRun.couponAnswer("minimum amount of DEX needed to make the first job advancement as a bowman."));
        assertEquals(25, BotKpqRun.couponAnswer("minimum amount of DEX needed to make the first job advancement as a thief."));
        assertEquals(8, BotKpqRun.couponAnswer("minimum level required to make the first job advancement as a magician."));
        assertEquals(-1, BotKpqRun.couponAnswer("Please give your pass to the leader."));
    }

    @Test
    void requiresRegisteredInstanceAndKeepsLeaderOutOfWorkerCouponCollection() {
        Character bot = mock(Character.class);
        when(bot.getMapId()).thenReturn(BotKpqRun.FIRST_MAP);
        assertFalse(BotKpqRun.inInstance(bot));
        var event = mock(EventInstanceManager.class);
        when(bot.getEventInstance()).thenReturn(event);
        BotEntry entry = new BotEntry(bot, bot, null);
        entry.kpq.couponTarget = 10;
        assertTrue(BotKpqRun.wantsCoupons(entry, bot));
        when(event.isEventLeader(bot)).thenReturn(true);
        assertFalse(BotKpqRun.wantsCoupons(entry, bot));
        when(event.isEventLeader(bot)).thenReturn(false);
        when(bot.getItemQuantity(4001007, false)).thenReturn(10);
        assertFalse(BotKpqRun.wantsCoupons(entry, bot));
    }
}
