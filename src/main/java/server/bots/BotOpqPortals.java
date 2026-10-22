package server.bots;

import server.maps.MapleMap;
import server.maps.Portal;

/** Deterministic room links verified against party3_room1..6 and party3_roomout. */
final class BotOpqPortals {
    private BotOpqPortals() {}
    static Portal roomPortal(MapleMap map, int target) {
        if (map.getId() == BotOpqRun.FIRST_MAP && target == BotOpqRun.HUB) return map.getPortal("in00");
        if (map.getId() == 920010800 && target == BotOpqRun.HUB) return map.getPortal("in00");
        if (map.getId() == BotOpqRun.HUB && target >= 920010200 && target <= 920010700 && target % 100 == 0) {
            return map.getPortal("in0" + ((target - 920010200) / 100));
        }
        if (target == BotOpqRun.HUB && map.getId() >= 920010200 && map.getId() <= 920010700 && map.getId() % 100 == 0)
            return map.getPortal("st00");
        return null;
    }
}
