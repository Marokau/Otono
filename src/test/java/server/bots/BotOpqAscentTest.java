package server.bots;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import client.Character;
import constants.skills.Assassin;
import scripting.event.EventInstanceManager;
import server.maps.MapleMap;
import server.maps.Portal;
class BotOpqAscentTest {
    @Test void aWorkerCanLearnRowsAndReplayTheCheckpointRoute() {
        var event = mock(EventInstanceManager.class);
        var map = mock(MapleMap.class);
        var crew = new ArrayList<Character>();
        for (int n = 1; n <= 6; n++) {
            var bot = mock(Character.class);
            when(bot.getId()).thenReturn(n);
            when(bot.getEventInstance()).thenReturn(event);
            when(bot.getMap()).thenReturn(map);
            crew.add(bot);
        }
        when(event.getPlayers()).thenReturn(crew);
        when(event.isEventLeader(crew.getFirst())).thenReturn(true);
        var thief = crew.get(1);
        when(thief.getSkillLevel(Assassin.HASTE)).thenReturn(20);
        var portal = mock(Portal.class);
        when(portal.getId()).thenReturn(24);
        when(map.getPortal("rp011")).thenReturn(portal);
        var destination = mock(Portal.class);
        when(map.getPortal(28)).thenReturn(destination);
        var followerEntry = new BotEntry(crew.get(2), crew.get(2), null);
        var scoutEntry = new BotEntry(thief, thief, null);
        try (var actions = mockStatic(BotOpqRun.class); var trials = mockStatic(BotOpqPortalTrial.class)) {
            trials.when(() -> BotOpqPortalTrial.tick(scoutEntry, thief, portal, destination, true)).thenReturn(BotOpqPortalTrial.Outcome.APPROACHING);
            BotOpqAscent.tick(followerEntry, crew.get(2), true);
            trials.verifyNoInteractions();
            BotOpqAscent.tick(scoutEntry, thief, true);
            trials.verify(() -> BotOpqPortalTrial.tick(scoutEntry, thief, portal, destination, true));
            for (int row = 1; row <= 4; row++) {
                var p = row == 1 ? portal : mock(Portal.class);
                when(p.getId()).thenReturn(20 + row * 4);
                when(map.getPortal(String.format(Locale.ROOT, "rp%02d1", row))).thenReturn(p);
                var dest = mock(Portal.class);
                when(map.getPortal(BotOpqAscent.successPortalId(row, p.getId()))).thenReturn(dest);
                trials.when(() -> BotOpqPortalTrial.tick(scoutEntry, thief, p, dest, true)).thenReturn(BotOpqPortalTrial.Outcome.SUCCESS);
                BotOpqAscent.tick(scoutEntry, thief, true);
            }
            var checkpoint = map.getPortal(1);
            when(checkpoint.getPosition()).thenReturn(new java.awt.Point(-187,-2581));
            when(thief.getPosition()).thenReturn(new java.awt.Point(-187,-2583));
            var fifth = mock(Portal.class);
            when(fifth.getId()).thenReturn(40);
            when(fifth.getPosition()).thenReturn(new java.awt.Point(-179,-2810));
            when(map.getPortal("rp051")).thenReturn(fifth);
            var sixth = mock(Portal.class);
            when(map.getPortal(44)).thenReturn(sixth);
            trials.when(() -> BotOpqPortalTrial.tick(scoutEntry, thief, fifth, sixth, true)).thenReturn(BotOpqPortalTrial.Outcome.APPROACHING);
            BotOpqAscent.tick(scoutEntry, thief, true);
            trials.verify(() -> BotOpqPortalTrial.tick(scoutEntry, thief, fifth, sixth, true));
        }
    }
    @Test void failuresPassTheNextColumnToAnotherWorkerAndSuccessIsShared() {
        var event=mock(EventInstanceManager.class);var map=mock(MapleMap.class);
        var crew=new ArrayList<Character>();var entries=new ArrayList<BotEntry>();
        for(int n=1;n<=6;n++) {
            var bot=mock(Character.class);when(bot.getId()).thenReturn(n);
            when(bot.getEventInstance()).thenReturn(event);when(bot.getMap()).thenReturn(map);
            when(bot.getMapId()).thenReturn(920010700);crew.add(bot);entries.add(new BotEntry(bot,bot,null));
        }
        when(event.getPlayers()).thenReturn(crew);when(event.isEventLeader(crew.getFirst())).thenReturn(true);
        var success=mock(Portal.class);when(map.getPortal(28)).thenReturn(success);
        var portals=new ArrayList<Portal>();
        for(int c=1;c<=3;c++) {
            var p=mock(Portal.class);when(p.getId()).thenReturn(24);
            when(map.getPortal("rp01"+c)).thenReturn(p);portals.add(p);
        }
        try(var actions=mockStatic(BotOpqRun.class);var trials=mockStatic(BotOpqPortalTrial.class);
            var chat=mockStatic(BotPqChat.class);var human=mockStatic(BotOpqHuman.class)) {
            trials.when(()->BotOpqPortalTrial.tick(entries.get(1),crew.get(1),portals.get(0),success,true)).thenReturn(BotOpqPortalTrial.Outcome.WRONG);
            trials.when(()->BotOpqPortalTrial.tick(entries.get(2),crew.get(2),portals.get(1),success,true)).thenReturn(BotOpqPortalTrial.Outcome.WRONG);
            trials.when(()->BotOpqPortalTrial.tick(entries.get(3),crew.get(3),portals.get(2),success,true)).thenReturn(BotOpqPortalTrial.Outcome.SUCCESS);
            BotOpqAscent.tick(entries.get(1),crew.get(1),true);
            BotOpqAscent.tick(entries.get(1),crew.get(1),true); // The failed worker waits its turn.
            BotOpqAscent.tick(entries.get(2),crew.get(2),true);
            BotOpqAscent.tick(entries.get(3),crew.get(3),true);
            human.verify(()->BotOpqHuman.learned(crew.get(3),"On the Way Up",1,3));
            // A teammate follows the observed correct column, never retrying columns one/two.
            trials.when(()->BotOpqPortalTrial.tick(entries.get(4),crew.get(4),portals.get(2),success,true)).thenReturn(BotOpqPortalTrial.Outcome.SUCCESS);
            BotOpqAscent.tick(entries.get(4),crew.get(4),true);
            trials.verify(()->BotOpqPortalTrial.tick(entries.get(1),crew.get(1),portals.get(0),success,true),times(1));
            trials.verify(()->BotOpqPortalTrial.tick(entries.get(2),crew.get(2),portals.get(1),success,true),times(1));
            trials.verify(()->BotOpqPortalTrial.tick(entries.get(3),crew.get(3),portals.get(2),success,true),times(1));
            trials.verify(()->BotOpqPortalTrial.tick(entries.get(4),crew.get(4),portals.get(2),success,true),times(1));
            verify(event,never()).getProperty("stage6_comb");
        }
    }    @Test void portalArrivalsMatchTheOrdinarySixteenRowScript() {
        assertEquals(28, BotOpqAscent.successPortalId(1, 24));
        assertEquals(1, BotOpqAscent.successPortalId(4, 36));
        assertEquals(4, BotOpqAscent.successPortalId(16, 84));
        assertEquals(3, BotOpqAscent.nextColumn(Set.of(1, 2)));
    }
    @Test void enumeratesEveryVisibleTwoLeverArrangement() {
        var choices = BotOpqAscent.leverChoices();
        assertEquals(10, choices.size());
        var unique = new HashSet<String>();
        for (var choice : choices) {
            assertTrue(choice[0] < choice[1]);
            assertTrue(choice[0] >= 1 && choice[1] <= 5);
            assertTrue(unique.add(Arrays.toString(choice)));
        }
    }
}
