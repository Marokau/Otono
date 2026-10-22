package server.bots;

import client.Character;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Read-only view of actual grinding activity; does not rerun or alter bot decisions. */
public final class BotTrainingGuide {
    private BotTrainingGuide() {}

    public record Spot(int mapId, int channel, int count, int lowestLevel, int highestLevel) {}

    public static List<Spot> spots(Character player, boolean sameClass) {
        return spots(player, sameClass, BotManager.getInstance().allBotEntries());
    }

    static List<Spot> spots(Character player, boolean sameClass, List<BotEntry> entries) {
        Map<String, Spot> grouped = new HashMap<>();
        for (BotEntry entry : entries) {
            Character bot = entry.bot;
            if (bot == null || !bot.isLoggedinWorld() || !entry.grinding || entry.following
                    || bot.getWorld() != player.getWorld() || bot.getMap() == null
                    || bot.getMap().isTown() || bot.getEventInstance() != null
                    || Math.abs(bot.getLevel() - player.getLevel()) > 10
                    || (sameClass && bot.getJob().getId() / 10 != player.getJob().getId() / 10)) {
                continue;
            }
            int mapId = bot.getMapId();
            if (mapId >= 900000000 || (entry.autopilotMapId >= 0 && entry.autopilotMapId != mapId)) {
                continue;
            }
            int channel = bot.getClient().getChannel();
            String key = mapId + ":" + channel;
            Spot previous = grouped.get(key);
            int level = bot.getLevel();
            grouped.put(key, previous == null ? new Spot(mapId, channel, 1, level, level)
                    : new Spot(mapId, channel, previous.count() + 1,
                    Math.min(level, previous.lowestLevel()), Math.max(level, previous.highestLevel())));
        }
        return grouped.values().stream().sorted(Comparator.comparingInt(Spot::count).reversed()
                .thenComparingInt(Spot::mapId).thenComparingInt(Spot::channel)).limit(10).toList();
    }
}
