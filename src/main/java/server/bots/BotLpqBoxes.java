package server.bots;

import client.Character;
import server.maps.MapItem;
import server.maps.Reactor;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;

/** Stage-two ordinary hit reactors, including the map-wide trap and its return portal. */
final class BotLpqBoxes {
    private static final Map<BotEntry, Long> NEXT_ACTION = Collections.synchronizedMap(new WeakHashMap<>());
    private BotLpqBoxes() {}

    static boolean tick(BotEntry entry, Character bot, boolean ai) {
        boolean trap = bot.getMapId() == 922010201;
        if (!trap && bot.getEventInstance().getProperty("2stageclear") != null) {
            BotTravelManager.tickTravel(entry, bot, 922010300, 2, ai, false);
            return true;
        }
        if (!trap) {
            for (var player : bot.getEventInstance().getPlayers())
                if (player.getMapId() == BotLpqRun.FIRST_MAP) return hold(entry, bot, ai);
        }
        if (BotLpqHuman.isCollector(bot)) {
            if (!trap && BotLpqHuman.deliver(entry, bot, BotLpqRun.PASS, 15, ai)) return true;
            if (!trap && bot.getItemQuantity(BotLpqRun.PASS, false) >= 15) {
                var approach = BotTravelManager.tickApproachNpc(entry, bot, bot.getMapId(), 2040037, 2, ai, false, 300);
                if (approach == BotTravelManager.ApproachStatus.ARRIVED) {
                    hold(entry, bot, ai);
                    if (ready(entry)) {
                        if (bot.getClient().getCM() != null) BotNpcInteraction.next(bot, 0);
                        else BotNpcInteraction.talk(bot, 2040037);
                    }
                }
                return true;
            }
            MapItem nearest = null;
            double distance = Double.MAX_VALUE;
            for (var drop : bot.getMap().getDroppedItems()) {
                double d = bot.getPosition().distanceSq(drop.getPosition());
                if (!drop.isPickedUp() && drop.getItemId() == BotLpqRun.PASS && drop.canBePickedBy(bot)
                        && System.currentTimeMillis() - drop.getDropTime() >= 400 && d < distance) {
                    nearest = drop;
                    distance = d;
                }
            }
            if (nearest != null) {
                BotZakumPqRun.stepTowardsAndPickUp(entry, bot, nearest, ai);
                return true;
            }
        }
        Reactor nearest = null;
        double distance = Double.MAX_VALUE;
        for (var reactor : bot.getMap().getAllReactors()) {
            double d = bot.getPosition().distanceSq(reactor.getPosition());
            if (reactor.isActive() && (reactor.getId() == 2202003 || reactor.getId() == 2200002) && d < distance) {
                nearest = reactor;
                distance = d;
            }
        }
        if (nearest != null) {
            var position = nearest.getPosition();
            if (Math.abs(bot.getPosition().x - position.x) <= 70 && Math.abs(bot.getPosition().y - position.y) <= 45) {
                hold(entry, bot, ai);
                if (ready(entry)) BotZakumPqRun.hitReactor(bot, nearest);
            } else BotZakumPqRun.walkTo(entry, bot, position, ai);
            return true;
        }
        if (trap) {
            for (var drop : bot.getMap().getDroppedItems())
                if (!drop.isPickedUp() && drop.getItemId() == BotLpqRun.PASS) return hold(entry, bot, ai);
            BotTravelManager.tickTravel(entry, bot, 922010200, 2, ai, false);
            return true;
        }
        return hold(entry, bot, ai);
    }

    private static boolean ready(BotEntry entry) {
        long now = System.currentTimeMillis();
        if (now < NEXT_ACTION.getOrDefault(entry, 0L)) return false;
        NEXT_ACTION.put(entry, now + ThreadLocalRandom.current().nextLong(1000, 1400));
        return true;
    }

    private static boolean hold(BotEntry entry, Character bot, boolean ai) {
        BotTravelManager.clearMoveTargetPin(entry);
        BotManager.getInstance().stepMovementCore(entry, bot.getPosition(), ai);
        return true;
    }
}
