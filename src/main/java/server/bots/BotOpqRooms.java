package server.bots;

import client.Character;
import scripting.event.EventInstanceManager;
import java.awt.Point;
import java.util.*;

/** The record room, visible platform puzzle and four-room lounge. */
final class BotOpqRooms {
    private static final class State {
        int attempt;
        long readySince;
        int loungeRoom = 920010601;
        int expected = 10;
    }
    private static final Map<EventInstanceManager, State> RUNS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final List<int[]> SEALED = placements();
    private BotOpqRooms() {}
    static boolean collectStatuePart(BotEntry entry, Character bot, boolean ai) {
        return BotOpqRun.collect(entry, bot, item -> item >= 4001044 && item <= 4001049, ai);
    }

    static boolean lobby(BotEntry entry, Character bot, boolean ai) {
        if (BotOpqRun.isLeader(bot) && bot.getItemQuantity(4001046, false) > 0) return BotOpqRun.travel(entry, bot, BotOpqRun.HUB, ai);
        if (BotOpqRun.collect(entry, bot, item -> item == 4001046, ai)) return true;
        int status = bot.getEventInstance().getIntProperty("statusStg3");
        if (status >= 0) {
            if (status == 0 && BotOpqRun.isLeader(bot)) return BotOpqRun.talk(entry, bot, 2013001, ai);
            return BotOpqRun.smash(entry, bot, r -> r.getId() == 2002011 && r.getState() > 0, ai);
        }
        if (BotOpqRecord.work(entry, bot, ai)) return true;
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        if (BotOpqRun.isLeader(bot) && bot.getItemQuantity(4001056 + day, false) > 0)
            return BotOpqRun.dropAt(entry, bot, bot.getMap().getReactorByName("music"), 4001056 + day, 1, ai);
        return BotOpqRun.smash(entry, bot, r -> r.getId() == 2002004 + day, ai);
    }

    static boolean sealed(BotEntry entry, Character bot, boolean ai) {
        if (BotOpqRun.isLeader(bot) && bot.getItemQuantity(4001047, false) > 0) return BotOpqRun.travel(entry, bot, BotOpqRun.HUB, ai);
        if (BotOpqRun.collect(entry, bot, item -> item == 4001047, ai)) return true;
        if (bot.getEventInstance().getIntProperty("statusStg4") == 1)
            return BotOpqRun.smash(entry, bot, r -> r.getId() == 2002012 && r.getState() > 0, ai);
        var state = RUNS.computeIfAbsent(bot.getEventInstance(), ignored -> new State());
        synchronized (state) {
            state.attempt += BotOpqHuman.consumeWrong(bot, bot.getMapId());
            var leader = bot.getEventInstance().getPlayers().stream().filter(bot.getEventInstance()::isEventLeader).findFirst().orElse(null);
            if (leader == null) return BotOpqRun.hold(entry, bot, ai);
            var workers = bot.getEventInstance().getPlayers().stream().filter(b -> b != leader)
                    .sorted(Comparator.comparingInt(Character::getId)).toList();
            if (workers.size() != 5 || bot.getMap().getAreas().size() != 3) return BotOpqRun.hold(entry, bot, ai);
            var candidate = SEALED.get(state.attempt % SEALED.size());
            if (BotOpqHuman.leader(bot) != null) {
                boolean placed = true;
                for (int n = 0; n < 3; n++)
                    if (workers.get(n).getMapId() != bot.getMapId()
                            || !bot.getMap().getArea(areaForWorker(candidate, n)).contains(workers.get(n).getPosition())) placed = false;
                for (int n = 3; n < 5; n++)
                    for (var area : bot.getMap().getAreas()) if (area.contains(workers.get(n).getPosition())) placed = false;
                for (var area : bot.getMap().getAreas()) if (area.contains(leader.getPosition())) placed = false;
                BotOpqHuman.puzzleReady(bot, state.attempt, placed);
            }
            int slot = workers.indexOf(bot);
            if (slot >= 0 && slot < 3) {
                int areaIndex = areaForWorker(candidate, slot);
                var area = bot.getMap().getArea(areaIndex);
                Point target = new Point((int) area.getCenterX(), (int) area.getCenterY());
                var ground = BotPhysicsEngine.findGroundPoint(bot.getMap(), new Point(target.x, area.y - 1));
                if (ground != null && area.contains(ground)) target = ground;
                BotZakumPqRun.walkTo(entry, bot, target, ai);
                return true;
            }
            var approach = BotTravelManager.tickApproachNpc(entry, bot, bot.getMapId(), 2013001, 2, ai, false, 35);
            if (approach != BotTravelManager.ApproachStatus.ARRIVED) return true;
            BotOpqRun.hold(entry, bot, ai);
            if (BotOpqHuman.leader(bot) != null) return true;
            if (!BotOpqRun.isLeader(bot)) return true;
            boolean ready = true;
            for (int n = 0; n < 3; n++)
                if (workers.get(n).getMapId() != bot.getMapId()
                        || !bot.getMap().getArea(areaForWorker(candidate, n)).contains(workers.get(n).getPosition())) ready = false;
            for (int n = 3; n < 5; n++)
                for (var area : bot.getMap().getAreas()) if (area.contains(workers.get(n).getPosition())) ready = false;
            if (!ready) { state.readySince = 0; return true; }
            if (state.readySince == 0) state.readySince = System.currentTimeMillis();
            if (System.currentTimeMillis() - state.readySince < 1200 || !BotOpqRun.ready(entry)) return true;
            if (bot.getClient().getCM() != null) BotNpcInteraction.next(bot, 0);
            else BotNpcInteraction.talk(bot, 2013001);
            var message = ((client.BotClient) bot.getClient()).npcMessage;
            if (message != null && (message.text().contains("One of the platforms has") || message.text().contains("All of the platforms have"))) {
                state.attempt++;
                state.readySince = 0;
            }
            return true;
        }
    }

    static int areaForWorker(int[] candidate, int worker) {
        int area = 0;
        while (worker >= candidate[area]) { worker -= candidate[area]; area++; }
        return area;
    }
    static List<int[]> placements() {
        var result = new ArrayList<int[]>();
        for (int a = 3; a >= 0; a--) for (int b = 3 - a; b >= 0; b--) result.add(new int[]{a, b, 3 - a - b});
        return result;
    }

    static boolean lounge(BotEntry entry, Character bot, boolean ai) {
        var state = RUNS.computeIfAbsent(bot.getEventInstance(), ignored -> new State());
        if (bot.getMapId() == 920010600) {
            if (BotOpqRun.isLeader(bot) && bot.getItemQuantity(4001048, false) > 0) return BotOpqRun.travel(entry, bot, BotOpqRun.HUB, ai);
            if (BotOpqRun.isLeader(bot) && bot.getItemQuantity(4001052, false) >= 40) return BotOpqRun.talk(entry, bot, 2013001, ai);
            return state.loungeRoom > 920010604 ? BotOpqRun.hold(entry, bot, ai) : BotOpqRun.travel(entry, bot, state.loungeRoom, ai);
        }
        if (bot.getMapId() != state.loungeRoom) return BotOpqRun.travel(entry, bot, 920010600, ai);
        if (BotOpqRun.collect(entry, bot, item -> item == 4001052, ai)) return true;
        if (BotOpqRun.isLeader(bot) && bot.getItemQuantity(4001052, false) >= state.expected
                && bot.getMap().getAllMonsters().isEmpty()) {
            state.loungeRoom++;
            state.expected += 10;
            return BotOpqRun.travel(entry, bot, 920010600, ai);
        }
        if (bot.getMapId() == 920010604) return BotOpqRun.smash(entry, bot, r -> r.getId() == 2002002, ai);
        return BotOpqRun.grind(entry, bot, ai);
    }
}
