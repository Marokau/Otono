package server.bots;

import client.Character;
import scripting.event.EventInstanceManager;

import java.awt.Point;
import java.util.*;

/** Five companions take the boxes; the human supplies each ordinary NPC check. */
final class BotLpqHumanPuzzle {
    private static final class State {
        int attempt;
        long readySince;
    }
    private static final Map<EventInstanceManager, State> RUNS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final List<int[]> CHOICES = BotLpqPuzzle.choices();
    private BotLpqHumanPuzzle() {}

    static boolean tick(BotEntry entry, Character bot, boolean ai) {
        var event = bot.getEventInstance();
        if (event.getProperty("8stageclear") != null) {
            BotTravelManager.tickTravel(entry, bot, 922010900, 2, ai, false);
            return true;
        }
        var state = RUNS.computeIfAbsent(event, ignored -> new State());
        synchronized (state) {
            var workers = workers(event);
            var areas = bot.getMap().getAreas();
            int slot = workers.indexOf(bot);
            if (workers.size() != 5 || areas.size() != 9 || slot < 0) return BotLpqHuman.hold(entry, bot, ai);
            var area = areas.get(CHOICES.get(state.attempt % CHOICES.size())[slot]);
            var target = new Point((int) area.getCenterX(), (int) area.getCenterY());
            var ground = BotPhysicsEngine.findGroundPoint(bot.getMap(), new Point(target.x, area.y - 1));
            if (ground != null && area.contains(ground)) target = ground;
            BotZakumPqRun.walkTo(entry, bot, target, ai);
            if (!BotLpqHuman.isCollector(bot)) return true;
            if (!ready(event, state)) state.readySince = 0;
            else {
                long now = System.currentTimeMillis();
                if (state.readySince == 0) state.readySince = now;
                if (now - state.readySince >= 1200)
                    BotLpqHuman.notice(bot, "boxes-" + state.attempt,
                            "LPQ: Box arrangement " + (state.attempt + 1) + " is ready. Stay off the boxes and check the NPC. If this is your first talk, finish the introduction and talk again.");
            }
            return true;
        }
    }

    /** One designated bot observes the broadcast; five recipients must not advance five times. */
    static void observeWrong(Character bot) {
        if (BotLpqHuman.leader(bot) == null || !BotLpqHuman.isCollector(bot)) return;
        var event = bot.getEventInstance();
        var state = RUNS.get(event);
        if (state == null) return;
        synchronized (state) {
            if (ready(event, state)) { state.attempt++; state.readySince = 0; }
        }
    }

    private static List<Character> workers(EventInstanceManager event) {
        return event.getPlayers().stream().filter(b -> BotLpqHuman.leader(b) != null)
                .sorted(Comparator.comparingInt(Character::getId)).toList();
    }

    private static boolean ready(EventInstanceManager event, State state) {
        var workers = workers(event);
        if (workers.size() != 5) return false;
        var map = workers.getFirst().getMap();
        var areas = map.getAreas();
        if (areas.size() != 9) return false;
        var candidate = CHOICES.get(state.attempt % CHOICES.size());
        for (int n = 0; n < 5; n++)
            if (workers.get(n).getMapId() != 922010800 || !areas.get(candidate[n]).contains(workers.get(n).getPosition())) return false;
        return true;
    }
}
