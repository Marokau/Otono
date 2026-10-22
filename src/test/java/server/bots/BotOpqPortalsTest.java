package server.bots;
import org.junit.jupiter.api.Test;
import server.maps.MapleMap;
import server.maps.Portal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotOpqPortalsTest {
    @Test void selectsTheRealScriptedDoorsWithoutInventingAWarp() {
        var map = mock(MapleMap.class);
        var door = mock(Portal.class);
        when(map.getId()).thenReturn(BotOpqRun.FIRST_MAP);
        when(map.getPortal("in00")).thenReturn(door);
        assertSame(door, BotOpqPortals.roomPortal(map, BotOpqRun.HUB));
        when(map.getId()).thenReturn(BotOpqRun.HUB);
        when(map.getPortal("in00")).thenReturn(door);
        assertSame(door, BotOpqPortals.roomPortal(map, 920010200));
        when(map.getId()).thenReturn(920010200);
        when(map.getPortal("st00")).thenReturn(door);
        assertSame(door, BotOpqPortals.roomPortal(map, BotOpqRun.HUB));
        assertNull(BotOpqPortals.roomPortal(map, 100000000));
    }
}
