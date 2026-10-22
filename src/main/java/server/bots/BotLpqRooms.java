package server.bots;

import client.Character;
import scripting.event.EventInstanceManager;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;

/** Stage-four room sweep. The leader collects real drops while both damage roles accompany it. */
final class BotLpqRooms {
    private static final class State {
        int room = 922010401;
        int expectedPasses = 1;
        long nextTalk;
    }
    private static final Map<EventInstanceManager, State> RUNS = Collections.synchronizedMap(new WeakHashMap<>());
    private BotLpqRooms() {}

    static boolean tick(BotEntry entry, Character bot, boolean ai) {
        var event = bot.getEventInstance();
        var state = RUNS.computeIfAbsent(event, ignored -> new State());
        if (event.getProperty("4stageclear") != null) {
            BotTravelManager.tickTravel(entry, bot, bot.getMapId() == 922010400 ? 922010500 : 922010400, 2, ai, false);
            return true;
        }
        var leader = event.getPlayers().stream().filter(event::isEventLeader).findFirst().orElse(null);
        if (leader == null) return hold(entry, bot, ai);
        if (BotLpqHuman.isCollector(bot) && bot.getMapId() == 922010400
                && BotLpqHuman.deliver(entry, bot, BotLpqRun.PASS, 6, ai)) return true;
        if (BotLpqRun.isLeader(bot) && bot.getMapId() == 922010400 && bot.getItemQuantity(BotLpqRun.PASS, false) >= 6) {
            var approach = BotTravelManager.tickApproachNpc(entry, bot, 922010400, 2040039, 2, ai, false, 300);
            if (approach == BotTravelManager.ApproachStatus.ARRIVED) {
                hold(entry, bot, ai);
                if (System.currentTimeMillis() >= state.nextTalk) {
                    state.nextTalk = System.currentTimeMillis() + ThreadLocalRandom.current().nextLong(1000, 1400);
                    if (bot.getClient().getCM() != null) BotNpcInteraction.next(bot, 0);
                    else BotNpcInteraction.talk(bot, 2040039);
                }
            }
            return true;
        }
        if (bot.getMapId() == 922010400) {
            if (state.room > 922010405) return hold(entry, bot, ai);
            BotTravelManager.tickTravel(entry, bot, state.room, 2, ai, false);
            return true;
        }
        if (bot.getMapId() != state.room) {
            BotTravelManager.tickTravel(entry, bot, 922010400, 2, ai, false);
            return true;
        }
        if (BotLpqHuman.isCollector(bot)) {
            server.maps.MapItem pass = null;
            double distance = Double.MAX_VALUE;
            for (var drop : bot.getMap().getDroppedItems()) {
                double d = bot.getPosition().distanceSq(drop.getPosition());
                if (!drop.isPickedUp() && drop.getItemId() == BotLpqRun.PASS && drop.canBePickedBy(bot)
                        && System.currentTimeMillis() - drop.getDropTime() >= 400 && d < distance) {
                    pass = drop;
                    distance = d;
                }
            }
            if (pass != null) {
                BotZakumPqRun.stepTowardsAndPickUp(entry, bot, pass, ai);
                return true;
            }
            if (bot.getMap().getAllMonsters().isEmpty() && BotLpqHuman.carriedWithLeader(bot, BotLpqRun.PASS) >= state.expectedPasses) {
                if (bot.getMapId() == state.room) {
                    state.room++;
                    state.expectedPasses += state.room == 922010404 ? 2 : 1;
                }
                BotTravelManager.tickTravel(entry, bot, 922010400, 2, ai, false);
                return true;
            }
        }
        BotTravelManager.clearMoveTargetPin(entry);
        if (BotCombatManager.findGrindTarget(entry, bot) == null) {
            server.life.Monster nearest = null;
            double distance = Double.MAX_VALUE;
            for (var mob : bot.getMap().getAllMonsters()) {
                double d = bot.getPosition().distanceSq(mob.getPosition());
                if (mob.isAlive() && d < distance) { nearest = mob; distance = d; }
            }
            if (nearest != null) { BotZakumPqRun.walkTo(entry, bot, nearest.getPosition(), ai); return true; }
        }
        BotManager.getInstance().tickPqGrinding(entry, bot, ai);
        return true;
    }

    private static boolean hold(BotEntry entry, Character bot, boolean ai) {
        BotTravelManager.clearMoveTargetPin(entry);
        BotManager.getInstance().stepMovementCore(entry, bot.getPosition(), ai);
        return true;
    }
}
