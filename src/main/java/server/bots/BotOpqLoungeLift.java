package server.bots;
import client.Character;
import scripting.event.EventInstanceManager;
import java.util.*;

/** The lounge's two three-choice portal rows, learned from real success/failure arrivals. */
final class BotOpqLoungeLift {
    private static final class State {
        final Map<Integer, Integer> known = new HashMap<>();
        final Map<Integer, Set<Integer>> rejected = new HashMap<>();
        final Map<Integer, Integer> row = new HashMap<>();
    }
    private static final Map<EventInstanceManager, State> RUNS = Collections.synchronizedMap(new WeakHashMap<>());
    private BotOpqLoungeLift() {}
    static boolean tick(BotEntry entry, Character bot, int destination, boolean ai) {
        var door = bot.getMap().getPortal(destination == 920010602 ? "in01" : "in02");
        if (door == null) return BotOpqRun.hold(entry, bot, ai);
        var graph = BotNavigationGraphProvider.peekGraph(bot.getMap(), entry.movementProfile);
        if (graph == null) return BotOpqRun.hold(entry, bot, ai);
        int skills = BotNavigationManager.botCanUseMovementSkill(bot) ? BotNavigationManager.botSkillMask(bot) : 0;
        if (graph.canReach(graph.findRegionId(bot.getMap(), bot.getPosition()), graph.findRegionId(bot.getMap(), door.getPosition()), skills)) {
            BotTravelManager.walkToPortalAndEnter(entry, bot, door, System.currentTimeMillis(), ai);
            return true;
        }
        var state = RUNS.computeIfAbsent(bot.getEventInstance(), ignored -> new State());
        synchronized (state) {
            int row = state.row.getOrDefault(bot.getId(), 1);
            if (row > 2) row = 1; // A later fall returned this member below the lift.
            var rowPortal = bot.getMap().getPortal("r4rp" + row + "1");
            if (row > 1 && !entry.inAir && !entry.climbing && rowPortal != null
                    && rowPortal.getPosition() != null && bot.getPosition().y > rowPortal.getPosition().y + 90) {
                row = 1; // A missed physical jump must replay the learned lower portal.
                state.row.put(bot.getId(), row);
            }
            Integer column = state.known.get(row);
            int scout = Integer.MAX_VALUE;
            for (var player : bot.getEventInstance().getPlayers())
                if (player.getClient() instanceof client.BotClient) scout = Math.min(scout, player.getId());
            if (column == null && bot.getId() != scout) return BotOpqRun.hold(entry, bot, ai);
            if (column == null) {
                var rejected = state.rejected.getOrDefault(row, Set.of());
                for (int n = 1; n <= 3; n++) if (!rejected.contains(n)) { column = n; break; }
            }
            if (column == null) return BotOpqRun.hold(entry, bot, ai);
            var portal = bot.getMap().getPortal("r4rp" + row + column);
            if (portal == null) return BotOpqRun.hold(entry, bot, ai);
            if (state.known.containsKey(row) && BotOpqPortalMarkers.beforeFollowing(entry, bot, portal, row, ai)) return true;
            var success = bot.getMap().getPortal(row == 1 ? portal.getId() + 3 : 1);
            var result = BotOpqPortalTrial.tick(entry, bot, portal, success, ai);
            if (result == BotOpqPortalTrial.Outcome.SUCCESS) {
                state.known.put(row, column);
                BotOpqHuman.learned(bot, "Lounge lift", row, column);
                state.row.put(bot.getId(), row + 1);
            } else if (result == BotOpqPortalTrial.Outcome.WRONG) {
                state.rejected.computeIfAbsent(row, ignored -> new HashSet<>()).add(column);
                state.row.put(bot.getId(), 1);
            }
            return true;
        }
    }
}
