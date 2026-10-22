package server.bots;

import client.Character;
import scripting.event.EventInstanceManager;
import java.awt.Point;
import java.util.*;

/** Learn numbered portals from actual arrivals, then coordinate two visible levers. */
final class BotOpqAscent {
    private static final class State {
        final Map<Integer, Integer> known = new HashMap<>();
        final Map<Integer, Set<Integer>> rejected = new HashMap<>();
        final Map<Integer, Integer> row = new HashMap<>();
        int attempt;
        long readySince;
    }
    private static final Map<EventInstanceManager, State> RUNS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final List<int[]> LEVERS = leverChoices();
    private BotOpqAscent() {}

    static boolean tick(BotEntry entry, Character bot, boolean ai) {
        var event = bot.getEventInstance();
        if (BotOpqRun.isLeader(bot) && bot.getItemQuantity(4001049, false) > 0) return BotOpqRun.travel(entry, bot, BotOpqRun.HUB, ai);
        if (BotOpqRun.collect(entry, bot, item -> item == 4001049, ai)) return true;
        if (event.getIntProperty("statusStg6") == 1)
            return BotOpqRun.smash(entry, bot, r -> r.getId() == 2002013 && r.getState() > 0, ai);
        var state = RUNS.computeIfAbsent(event, ignored -> new State());
        synchronized (state) {
            state.attempt += BotOpqHuman.consumeWrong(bot, bot.getMapId());
            var leader = event.getPlayers().stream().filter(event::isEventLeader).findFirst().orElse(null);
            if (leader == null) return BotOpqRun.hold(entry, bot, ai);
            var workers = event.getPlayers().stream().filter(b -> b != leader).sorted(Comparator.comparingInt(Character::getId)).toList();
            if (workers.size() != 5) return BotOpqRun.hold(entry, bot, ai);
            int slot = workers.indexOf(bot);
            if (slot >= 0) {
                int row = state.row.getOrDefault(bot.getId(), 1);
                if (!entry.inAir && !entry.climbing) {
                    while (row > 1 && row <= 16) {
                        var rowPortal = row % 4 == 1 ? bot.getMap().getPortal(row / 4)
                                : bot.getMap().getPortal(String.format(Locale.ROOT, "rp%02d1", row));
                        if (rowPortal == null || rowPortal.getPosition() == null
                                || bot.getPosition().y <= rowPortal.getPosition().y + 90) break;
                        row--;
                        state.row.put(bot.getId(), row); // Replay known portals after a physical fall.
                    }
                }
                if (row <= 16) {
                    Integer column = state.known.get(row);
                    if (column == null) {
                        // Rotate real trials among workers on this row. A failed worker can
                        // replay the learned route while a teammate tries the next column.
                        var available = new ArrayList<Character>();
                        for (var worker : workers)
                            if (worker.getMapId() == bot.getMapId() && state.row.getOrDefault(worker.getId(), 1) == row)
                                available.add(worker);
                        var rejected = state.rejected.getOrDefault(row, Set.of());
                        if (available.isEmpty() || available.get(rejected.size() % available.size()) != bot)
                            return BotOpqRun.hold(entry, bot, ai);
                        column = nextColumn(rejected);
                    }
                    var portal = bot.getMap().getPortal(String.format(Locale.ROOT, "rp%02d%d", row, column));
                    if (portal == null) return BotOpqRun.hold(entry, bot, ai);
                    if (state.known.containsKey(row) && BotOpqPortalMarkers.beforeFollowing(entry, bot, portal, row, ai)) return true;
                    var destination = bot.getMap().getPortal(successPortalId(row, portal.getId()));
                    var result = BotOpqPortalTrial.tick(entry, bot, portal, destination, ai);
                    if (result != BotOpqPortalTrial.Outcome.APPROACHING) {
                        if (result == BotOpqPortalTrial.Outcome.SUCCESS) {
                            state.known.put(row, column);
                            BotOpqHuman.learned(bot, "On the Way Up", row, column);
                            state.row.put(bot.getId(), row + 1);
                        } else {
                            state.rejected.computeIfAbsent(row, ignored -> new HashSet<>()).add(column);
                            state.row.put(bot.getId(), checkpointRow(bot));
                        }
                    }
                    return true;
                }
                int lever = slot + 1;
                var reactor = bot.getMap().getReactorByName(Integer.toString(lever));
                if (reactor == null) return BotOpqRun.hold(entry, bot, ai);
                var choice = LEVERS.get(state.attempt % LEVERS.size());
                if (BotOpqHuman.leader(bot) != null) {
                    boolean placed = true;
                    for (int n = 1; n <= 5; n++) {
                        var r = bot.getMap().getReactorByName(Integer.toString(n));
                        if (r == null || workers.get(n - 1).getMapId() != bot.getMapId()
                                || state.row.getOrDefault(workers.get(n - 1).getId(), 1) <= 16
                                || (r.getState() > 0) != (n == choice[0] || n == choice[1])) placed = false;
                    }
                    BotOpqHuman.puzzleReady(bot, state.attempt, placed);
                }
                boolean desired = lever == choice[0] || lever == choice[1];
                if ((reactor.getState() > 0) != desired)
                    return BotOpqRun.smash(entry, bot, r -> r == reactor, ai);
                BotZakumPqRun.walkTo(entry, bot, reactor.getPosition(), ai);
                return true;
            }
            var approach = BotTravelManager.tickApproachNpc(entry, bot, bot.getMapId(), 2013001, 2, ai, false, 100);
            if (approach != BotTravelManager.ApproachStatus.ARRIVED) return true;
            BotOpqRun.hold(entry, bot, ai);
            boolean ready = true;
            var choice = LEVERS.get(state.attempt % LEVERS.size());
            for (int n = 1; n <= 5; n++) {
                var r = bot.getMap().getReactorByName(Integer.toString(n));
                if (r == null || state.row.getOrDefault(workers.get(n - 1).getId(), 1) <= 16
                        || (r.getState() > 0) != (n == choice[0] || n == choice[1])) ready = false;
            }
            if (!ready) { state.readySince = 0; return true; }
            if (state.readySince == 0) state.readySince = System.currentTimeMillis();
            if (System.currentTimeMillis() - state.readySince < 1200 || !BotOpqRun.ready(entry)) return true;
            if (bot.getClient().getCM() != null) BotNpcInteraction.next(bot, 0);
            else BotNpcInteraction.talk(bot, 2013001);
            var message = ((client.BotClient) bot.getClient()).npcMessage;
            if (message != null && (message.text().contains("One of the pushed levers") || message.text().contains("Both of the pushed levers"))) {
                state.attempt++;
                state.readySince = 0;
            }
            return true;
        }
    }

    static int successPortalId(int row, int portal) { return row % 4 == 0 ? row / 4 : portal + 4; }
    static int nextColumn(Set<Integer> rejected) {
        for (int n = 1; n <= 4; n++) if (!rejected.contains(n)) return n;
        throw new IllegalStateException("All four visible columns failed");
    }
    private static int checkpointRow(Character bot) {
        for (int n = 1; n <= 5; n++) {
            var p = bot.getMap().getPortal(n);
            if (p != null && bot.getPosition().distanceSq(p.getPosition()) <= 25) return n == 5 ? 1 : n * 4 + 1;
        }
        return 1;
    }
    static List<int[]> leverChoices() {
        var result = new ArrayList<int[]>();
        for (int a = 1; a <= 4; a++) for (int b = a + 1; b <= 5; b++) result.add(new int[]{a, b});
        return result;
    }
}
