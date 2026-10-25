package server.bots;

import client.BotClient;
import client.Character;
import server.maps.MapItem;
import server.maps.MapleMap;

/** Keep earned handoff items available during active human-led companion stages. */
public final class BotPqDropPolicy {
    private BotPqDropPolicy() {}

    public static boolean keepsForHandoff(MapleMap map, MapItem drop) {
        if (map.getId() == BotMvpqRun.COURSE && drop.getItemId() == BotMvpqRun.LETTER) {
            var event = map.getEventInstance();
            if (event != null && !event.isEventCleared())
                for (var member : event.getPlayers()) if (BotMvpqRun.assigned(member)) return true;
        }
        // GPQ offerings and earned stage items must survive a human pause.
        if (map.getId() >= 990000000 && map.getId() <= 990001000
                && (drop.getItemId() == 1032033 || drop.getItemId() >= 4001024 && drop.getItemId() <= 4001037)) {
            var event = map.getEventInstance();
            if (event != null && !event.isEventCleared())
                for (var member : event.getPlayers())
                    if (member.getClient() instanceof BotClient && BotGuildCompanions.inInstance(member)) return true;
        }
        // Human and companion route markers stay until the MPQ attempt ends.
        int mapId = map.getId();
        if ((mapId >= 926100300 && mapId <= 926100304 || mapId >= 926110300 && mapId <= 926110304)
                && drop.isPlayerDrop() && drop.getMeso() > 0) {
            var event = map.getEventInstance();
            if (event != null && !event.isEventCleared()) return true;
        }
        // Special-lab keys/files must survive an AFK human handoff or a closed door.
        if ((mapId >= 926100200 && mapId <= 926100202 || mapId >= 926110200 && mapId <= 926110202)
                && (drop.getItemId() == BotMagatiaRun.KEY || drop.getItemId() == BotMagatiaRun.LEFT_FILE
                    || drop.getItemId() == BotMagatiaRun.RIGHT_FILE)) {
            var event = map.getEventInstance();
            if (event != null && !event.isEventCleared()) return true;
        }
        // Earned Lounge pieces must not vanish while a crew waits or troubleshoots.
        if (map.getId() >= 920010600 && map.getId() <= 920010604 && drop.getItemId() == 4001052) {
            var event = map.getEventInstance();
            if (event != null && !event.isEventCleared())
                for (var member : event.getPlayers())
                    if (member.getClient() instanceof BotClient && BotOpqRun.inInstance(member)) return true;
        }
        // Route markers remain visible for a human who climbs after the crew.
        if ((map.getId() == 920010700 || map.getId() == 920010600) && drop.getMeso() == 10 && drop.isPlayerDrop()
                && drop.getDropper() instanceof Character marker && marker.getClient() instanceof BotClient
                && BotOpqRun.inInstance(marker) && (marker.getMap() == map
                    || map.getEventInstance() != null && marker.getEventInstance() == map.getEventInstance()
                        && !map.getEventInstance().isEventCleared())) return true;
        if ((drop.getItemId() == 4001169 || drop.getItemId() == 4001163)
                && drop.isPlayerDrop() && drop.getDropper() instanceof Character companion
                && companion.getMap() == map && BotEllinRun.inInstance(companion)) {
            var entry = BotManager.getInstance().getEntryByBotCharId(companion.getId());
            if (entry != null && entry.owner != null && BotEllinRun.belongsTo(companion, entry.owner.getId())) return true;
        }
        if (drop.getItemId() != BotLootEligibility.KPQ_PASS) return false;
        if (map.getId() == BotKpqRun.FIRST_MAP + 4) {
            // Final-stage monster passes belong to the whole party, regardless of killer.
            for (var member : map.getCharacters()) {
                if (!(member.getClient() instanceof BotClient) || member.getEventInstance() == null
                        || member.getMap() != map) continue;
                var entry = BotManager.getInstance().getEntryByBotCharId(member.getId());
                if (entry != null && entry.kpq.humanLed) return true;
            }
            return false;
        }
        if (map.getId() != BotKpqRun.FIRST_MAP
                || !drop.isPlayerDrop() || !(drop.getDropper() instanceof Character bot)
                || !(bot.getClient() instanceof BotClient) || bot.getMap() != map
                || bot.getEventInstance() == null) return false;
        var entry = BotManager.getInstance().getEntryByBotCharId(bot.getId());
        return entry != null && entry.kpq.humanLed;
    }
}
