package server.bots;

import client.Character;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import server.maps.MapleMap;
import server.maps.Portal;
import java.awt.Point;
import java.util.List;
import static org.mockito.Mockito.*;

class BotOpqLoungeLiftTest {
    @Test void wrongLandingTriesAnotherColumnThenARealSuccessAdvancesTheRow() {
        var bot = mock(Character.class);
        var map = mock(MapleMap.class);
        var event = mock(EventInstanceManager.class);
        when(bot.getId()).thenReturn(1);
        when(bot.getClient()).thenReturn(mock(client.BotClient.class));
        when(bot.getMap()).thenReturn(map);
        when(bot.getPosition()).thenReturn(new Point());
        when(bot.getEventInstance()).thenReturn(event);
        var human = mock(Character.class);
        when(human.getClient()).thenReturn(mock(client.Client.class));
        when(event.getPlayers()).thenReturn(List.of(human, bot));
        var door = mock(Portal.class);
        when(door.getPosition()).thenReturn(new Point(0, -1830));
        when(map.getPortal("in01")).thenReturn(door);
        var p11 = mock(Portal.class); when(p11.getId()).thenReturn(11);
        var p12 = mock(Portal.class); when(p12.getId()).thenReturn(12);
        var p14 = mock(Portal.class); when(p14.getId()).thenReturn(14);
        var p15 = mock(Portal.class); when(p15.getId()).thenReturn(15);
        var success = mock(Portal.class);
        when(map.getPortal("r4rp11")).thenReturn(p11);
        when(map.getPortal("r4rp12")).thenReturn(p12);
        when(map.getPortal("r4rp21")).thenReturn(p14);
        when(map.getPortal(14)).thenReturn(p14);
        when(map.getPortal(15)).thenReturn(p15);
        when(map.getPortal(1)).thenReturn(success);
        var entry = new BotEntry(bot, bot, null);
        var graph = mock(BotNavigationGraph.class);
        try (var graphs = mockStatic(BotNavigationGraphProvider.class); var nav = mockStatic(BotNavigationManager.class);
             var trials = mockStatic(BotOpqPortalTrial.class)) {
            graphs.when(() -> BotNavigationGraphProvider.peekGraph(map, entry.movementProfile)).thenReturn(graph);
            trials.when(() -> BotOpqPortalTrial.tick(entry, bot, p11, p14, true)).thenReturn(BotOpqPortalTrial.Outcome.WRONG);
            trials.when(() -> BotOpqPortalTrial.tick(entry, bot, p12, p15, true)).thenReturn(BotOpqPortalTrial.Outcome.SUCCESS);
            BotOpqLoungeLift.tick(entry, bot, 920010602, true);
            BotOpqLoungeLift.tick(entry, bot, 920010602, true);
            BotOpqLoungeLift.tick(entry, bot, 920010602, true);
            trials.verify(() -> BotOpqPortalTrial.tick(entry, bot, p11, p14, true));
            trials.verify(() -> BotOpqPortalTrial.tick(entry, bot, p12, p15, true));
            trials.verify(() -> BotOpqPortalTrial.tick(entry, bot, p14, success, true));
            when(p14.getPosition()).thenReturn(new Point(-181, -1635));
            when(bot.getPosition()).thenReturn(new Point(-183, -1458));
            BotOpqLoungeLift.tick(entry, bot, 920010602, true);
            trials.verify(() -> BotOpqPortalTrial.tick(entry, bot, p12, p15, true), times(2));
            verify(event, never()).getProperty("stage4_comb");
        }
    }
}
