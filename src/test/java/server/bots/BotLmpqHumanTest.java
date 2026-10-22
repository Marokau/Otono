package server.bots;

import client.BotClient;
import client.Character;
import client.Client;
import client.inventory.InventoryType;
import net.server.PlayerStorage;
import net.server.world.Party;
import net.server.world.PartyCharacter;
import net.server.world.World;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import java.awt.Point;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotLmpqHumanTest {
    @Test void mazeTravelAllowsRoutesLongerThanTwoPortals() {
        var bot = mock(Character.class); var entry = new BotEntry(bot, bot, null);
        try (var travel = mockStatic(BotTravelManager.class)) {
            BotLmpqRun.travel(entry, bot, BotLmpqRun.FINISH, true);
            travel.verify(() -> BotTravelManager.tickTravel(entry, bot, BotLmpqRun.FINISH, 16, true, false));
        }
    }
    @Test void couponHandoffWaitsForHumanAndDoesNotReclaimTheirPileAcrossTicks() {
        var human = mock(Character.class); var hc = mock(Client.class); var world = mock(World.class);
        var storage = mock(PlayerStorage.class); var party = mock(Party.class);
        var manager = mock(BotManager.class); var bots = new ArrayList<Character>(); var entries = new HashMap<Integer,BotEntry>();
        when(human.getId()).thenReturn(100); when(human.getLevel()).thenReturn(51);
        when(human.getMapId()).thenReturn(BotLmpqRun.RECRUIT); when(human.getClient()).thenReturn(hc);
        when(human.getWorldServer()).thenReturn(world); when(world.getPlayerStorage()).thenReturn(storage);
        when(storage.getCharacterById(100)).thenReturn(human); when(human.getParty()).thenReturn(party);
        when(party.getLeaderId()).thenReturn(100); when(party.getMembers()).thenReturn(List.of(mock(PartyCharacter.class),mock(PartyCharacter.class),mock(PartyCharacter.class),mock(PartyCharacter.class),mock(PartyCharacter.class)));
        when(human.getPosition()).thenReturn(new Point());
        for(int id=1;id<=4;id++) {
            var bot=mock(Character.class); when(bot.getId()).thenReturn(id); when(bot.getLevel()).thenReturn(51);
            when(bot.getMapId()).thenReturn(BotLmpqRun.RECRUIT); when(bot.getClient()).thenReturn(mock(BotClient.class));
            when(bot.getPosition()).thenReturn(new Point()); bots.add(bot); var entry=new BotEntry(bot,bot,null); entries.put(id,entry);
            when(manager.getEntryByBotCharId(id)).thenReturn(entry);
        }
        try(var managers=mockStatic(BotManager.class);var builds=mockStatic(BotBuildManager.class);var supplies=mockStatic(BotPqProvisioner.class);
            var run=mockStatic(BotOpqRun.class);var cures=mockStatic(BotLpqCures.class);var travel=mockStatic(BotTravelManager.class);var graph=mockStatic(BotWorldGraph.class);
            var guidance=mockStatic(BotPqStageGuide.class)) {
            managers.when(BotManager::getInstance).thenReturn(manager);
            BotLmpqRun.startHuman(human,bots);
            var event=mock(EventInstanceManager.class); var collector=bots.getFirst(); var entry=entries.get(1);
            for(var bot:bots) {when(bot.getEventInstance()).thenReturn(event);when(bot.getParty()).thenReturn(party);when(bot.getMapId()).thenReturn(BotLmpqRun.FINISH);}
            when(human.getEventInstance()).thenReturn(event); when(human.getMapId()).thenReturn(BotLmpqRun.FIRST+5);
            // Shared target is initially room 6: the crew cannot finish ahead of its human.
            BotLmpqRun.tick(entry,collector,true);
            verify(manager,never()).issueDropItem(any(),any(),anyInt(),anyShort());
            // Set the controller's observed next destination, without modifying any game event or puzzle state.
            var runs=BotLmpqRun.class.getDeclaredField("RUNS");runs.setAccessible(true);
            var state=((Map<?,?>)runs.get(null)).get(collector);var cf=state.getClass().getDeclaredField("crew");cf.setAccessible(true);
            var crew=cf.get(state);var room=crew.getClass().getDeclaredField("room");room.setAccessible(true);room.setInt(crew,BotLmpqRun.FINISH);
            when(human.getMapId()).thenReturn(BotLmpqRun.FINISH);
            var remaining=new java.util.concurrent.atomic.AtomicInteger(260);
            when(collector.getItemQuantity(4001106,false)).thenAnswer(invocation->remaining.get());
            when(manager.issueDropItem(entry,InventoryType.ETC,4001106,(short)0)).thenAnswer(invocation->{remaining.updateAndGet(n->Math.max(0,n-100));return true;});
            var nextAction=state.getClass().getDeclaredField("nextAction");nextAction.setAccessible(true);
            for(int n=0;n<4;n++){nextAction.setLong(state,0);BotLmpqRun.tick(entry,collector,true);assertFalse(BotLmpqRun.canLoot(collector));}
            assertFalse(BotLmpqRun.canLoot(collector));verify(manager,times(3)).issueDropItem(entry,InventoryType.ETC,4001106,(short)0);
            assertTrue(BotLmpqRun.belongsTo(collector,100)); assertFalse(BotLmpqRun.belongsTo(collector,101));
            // A new real instance clears delivery/finish flags so this same crew can run again.
            var next=mock(EventInstanceManager.class);for(var bot:bots)when(bot.getEventInstance()).thenReturn(next);when(human.getEventInstance()).thenReturn(next);
            BotLmpqRun.tick(entry,collector,true);assertTrue(BotLmpqRun.canLoot(collector));
        } catch(ReflectiveOperationException e) {throw new AssertionError(e);}
        finally {for(var bot:bots)BotLmpqRun.unregister(bot);}
    }
}
