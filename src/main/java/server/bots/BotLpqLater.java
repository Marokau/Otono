package server.bots;

import client.Character;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/** Normal number-route travel, bonus boxes and reward NPC. */
final class BotLpqLater {
    private static final Set<Integer> REWARD_VISITS = ConcurrentHashMap.newKeySet();
    private static final Map<Integer, Long> NEXT_ACTION = new ConcurrentHashMap<>();
    private BotLpqLater() {}

    static boolean finished(Character bot) {
        return bot.getMapId() == BotLpqRun.RECRUIT_MAP && bot.getEventInstance() == null && REWARD_VISITS.contains(bot.getId());
    }
    static void reset(Character bot) { REWARD_VISITS.remove(bot.getId()); NEXT_ACTION.remove(bot.getId()); }

    static boolean tick(BotEntry entry, Character bot, boolean ai) {
        if (bot.getMapId() == 922010600) {
            BotTravelManager.tickTravel(entry, bot, 922010700, 2, ai, false);
            return true;
        }
        if (bot.getMapId() == 922011100) {
            var approach = BotTravelManager.tickApproachNpc(entry, bot, bot.getMapId(), 2040035, 2, ai, false, 300);
            if (approach == BotTravelManager.ApproachStatus.ARRIVED) {
                hold(entry, bot, ai);
                if (ready(bot)) {
                    REWARD_VISITS.add(bot.getId());
                    if (bot.getClient().getCM() != null) BotNpcInteraction.next(bot, 0);
                    else BotNpcInteraction.talk(bot, 2040035);
                }
            }
            return true;
        }
        server.maps.Reactor box = null;
        double distance = Double.MAX_VALUE;
        for (var r : bot.getMap().getAllReactors()) {
            double d = bot.getPosition().distanceSq(r.getPosition());
            if (r.isActive() && r.getId() == 2202004 && d < distance) { box = r; distance = d; }
        }
        if (box != null) {
            var p = box.getPosition();
            if (Math.abs(bot.getPosition().x - p.x) <= 70 && Math.abs(bot.getPosition().y - p.y) <= 45) {
                hold(entry, bot, ai);
                if (ready(bot)) BotZakumPqRun.hitReactor(bot, box);
            } else BotZakumPqRun.walkTo(entry, bot, p, ai);
        } else hold(entry, bot, ai);
        return true;
    }
    private static boolean ready(Character bot) {
        long now = System.currentTimeMillis();
        if (now < NEXT_ACTION.getOrDefault(bot.getId(), 0L)) return false;
        NEXT_ACTION.put(bot.getId(), now + ThreadLocalRandom.current().nextLong(1000, 1400));
        return true;
    }
    private static void hold(BotEntry entry, Character bot, boolean ai) {
        BotTravelManager.clearMoveTargetPin(entry);
        BotManager.getInstance().stepMovementCore(entry, bot.getPosition(), ai);
    }
}
