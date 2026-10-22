package server.bots;

import client.Character;
import server.maps.Reactor;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;

/** Combat/pass checks for stage three, stage seven and Alishar. */
final class BotLpqStageThree {
    private static final Map<BotEntry, Long> NEXT_ACTION = Collections.synchronizedMap(new WeakHashMap<>());
    private BotLpqStageThree() {}

    static boolean tick(BotEntry entry, Character bot, boolean ai) {
        int stage = (bot.getMapId() - BotLpqRun.FIRST_MAP) / 100 + 1;
        int item = stage == 9 ? 4001023 : BotLpqRun.PASS;
        int required = stage == 1 ? 25 : stage == 3 ? 32 : stage == 7 ? 3 : 1;
        int npc = stage == 1 ? 2040036 : stage == 3 ? 2040038 : stage == 7 ? 2040042 : 2040044;
        if (bot.getEventInstance().getProperty(stage + "stageclear") != null) {
            if (stage != 9) BotTravelManager.tickTravel(entry, bot, bot.getMapId() + 100, 2, ai, false);
            return true;
        }
        if (BotLpqHuman.isCollector(bot)) {
            if (BotLpqHuman.deliver(entry, bot, item, required, ai)) return true;
            if (bot.getItemQuantity(item, false) >= required) {
                var approach = BotTravelManager.tickApproachNpc(entry, bot, bot.getMapId(), npc, 2, ai, false, 300);
                if (approach == BotTravelManager.ApproachStatus.ARRIVED && ready(entry)) {
                    if (bot.getClient().getCM() != null) BotNpcInteraction.next(bot, 0);
                    else BotNpcInteraction.talk(bot, npc);
                }
                return true;
            }
            server.maps.MapItem nearest = null;
            double distance = Double.MAX_VALUE;
            for (var drop : bot.getMap().getDroppedItems()) {
                double d = bot.getPosition().distanceSq(drop.getPosition());
                if (!drop.isPickedUp() && drop.getItemId() == item && drop.canBePickedBy(bot)
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
        BotTravelManager.clearMoveTargetPin(entry);
        if ((stage == 7 || stage == 9) && BotLpqRanged.approach(entry, bot, ai)) return true;
        if (BotCombatManager.findGrindTarget(entry, bot) != null) {
            BotManager.getInstance().tickPqGrinding(entry, bot, ai);
            return true;
        }
        server.life.Monster monster = null;
        double distance = Double.MAX_VALUE;
        for (var mob : bot.getMap().getAllMonsters()) {
            double d = bot.getPosition().distanceSq(mob.getPosition());
            if (mob.isAlive() && d < distance) { monster = mob; distance = d; }
        }
        if (monster != null) {
            BotZakumPqRun.walkTo(entry, bot, monster.getPosition(), ai);
            return true;
        }
        Reactor nearest = null;
        distance = Double.MAX_VALUE;
        for (var reactor : bot.getMap().getAllReactors()) {
            double d = bot.getPosition().distanceSq(reactor.getPosition());
            if (stage == 3 && reactor.isActive() && reactor.getId() == 2201001 && d < distance) {
                nearest = reactor;
                distance = d;
            }
        }
        if (nearest != null) {
            var p = nearest.getPosition();
            if (Math.abs(bot.getPosition().x - p.x) <= 70 && Math.abs(bot.getPosition().y - p.y) <= 45) {
                BotManager.getInstance().stepMovementCore(entry, bot.getPosition(), ai);
                if (ready(entry)) BotZakumPqRun.hitReactor(bot, nearest);
            } else BotZakumPqRun.walkTo(entry, bot, p, ai);
        } else BotManager.getInstance().stepMovementCore(entry, bot.getPosition(), ai);
        return true;
    }

    private static boolean ready(BotEntry entry) {
        long now = System.currentTimeMillis();
        if (now < NEXT_ACTION.getOrDefault(entry, 0L)) return false;
        NEXT_ACTION.put(entry, now + ThreadLocalRandom.current().nextLong(1000, 1400));
        return true;
    }
}
