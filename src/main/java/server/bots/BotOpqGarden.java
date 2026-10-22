package server.bots;

import client.Character;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Real seed planting, Dark Nependeath/Papa Pixie combat, Grass of Life and reward dialogue. */
final class BotOpqGarden {
    private static final Set<Integer> REWARD_VISITS = ConcurrentHashMap.newKeySet();
    private BotOpqGarden() {}
    static void reset(Character bot) { REWARD_VISITS.remove(bot.getId()); }
    static boolean finished(Character bot) { return bot.getMapId() == BotOpqRun.RECRUIT_MAP && bot.getEventInstance() == null && REWARD_VISITS.contains(bot.getId()); }

    static boolean tick(BotEntry entry, Character bot, boolean ai) {
        if (bot.getMap().getMonsterById(9300039) != null) return BotOpqRun.grind(entry, bot, ai);
        if (BotOpqRun.isLeader(bot) && bot.getItemQuantity(4001055, false) > 0) return BotOpqRun.travel(entry, bot, BotOpqRun.HUB, ai);
        if (BotOpqRun.collect(entry, bot, item -> item >= 4001053 && item <= 4001055, ai)) return true;
        if (BotOpqRun.isLeader(bot)) {
            if (bot.getItemQuantity(4001054, false) > 0)
                return BotOpqRun.dropAt(entry, bot, bot.getMap().getReactorById(2002003), 4001054, 1, ai);
            if (bot.getItemQuantity(4001053, false) > 0 && bot.getMap().getMonsterById(9300039) == null) {
                server.maps.Reactor pot = null;
                double distance = Double.MAX_VALUE;
                for (var r : bot.getMap().getAllReactors()) {
                    if (r.getId() != 2001000 && r.getId() != 2001001 || r.getState() != 0) continue;
                    double d = bot.getPosition().distanceSq(r.getPosition());
                    if (d < distance) { pot = r; distance = d; }
                }
                if (pot != null) return BotOpqRun.dropAt(entry, bot, pot, 4001053, 1, ai);
            }
        }
        var grass = bot.getMap().getReactorById(2002003);
        if (grass != null && grass.getState() > 0)
            return BotOpqRun.smash(entry, bot, r -> r == grass, ai);
        return BotOpqRun.grind(entry, bot, ai);
    }

    static boolean restoredHub(BotEntry entry, Character bot, boolean ai) {
        if (bot.getMap().getNPCById(2013002) != null) return BotOpqRun.talk(entry, bot, 2013002, ai);
        if (BotOpqRun.isLeader(bot) && bot.getItemQuantity(4001055, false) > 0)
            return BotOpqRun.dropAt(entry, bot, bot.getMap().getReactorByName("minerva"), 4001055, 1, ai);
        return false;
    }

    static boolean reward(BotEntry entry, Character bot, boolean ai) {
        REWARD_VISITS.add(bot.getId());
        return BotOpqRun.talk(entry, bot, 2013002, ai);
    }
}
