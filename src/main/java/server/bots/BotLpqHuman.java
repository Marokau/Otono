package server.bots;

import client.BotClient;
import client.Character;
import client.inventory.InventoryType;
import client.inventory.manipulator.InventoryManipulator;

import java.util.*;

/** Human-led LPQ lifecycle and handoffs. Stage controllers still perform ordinary gameplay. */
final class BotLpqHuman {
    private static final class State {
        Character leader;
        int collector;
        scripting.event.EventInstanceManager event;
        final Set<Integer> delivered = new HashSet<>();
        final Set<String> notices = new HashSet<>();
        long nextAction;
    }
    private static final Map<Character, State> RUNS = Collections.synchronizedMap(new WeakHashMap<>());
    private BotLpqHuman() {}

    static void start(Character leader, List<Character> bots) {
        if (leader.getClient() instanceof BotClient || bots.size() != 5
                || bots.stream().map(Character::getId).distinct().count() != 5
                || leader.getLevel() < 35 || leader.getLevel() > 50
                || leader.getMapId() != BotLpqRun.RECRUIT_MAP || leader.getEventInstance() != null)
            throw new IllegalArgumentException("LPQ requires an eligible human leader and five companions");
        var ids = new HashSet<>(bots.stream().map(Character::getId).toList());
        ids.add(leader.getId());
        var manager = BotManager.getInstance();
        for (var bot : bots) {
            if (!(bot.getClient() instanceof BotClient) || bot.isGM() || bot.getLevel() < 35 || bot.getLevel() > 50
                    || bot.getMapId() != BotLpqRun.RECRUIT_MAP || bot.getEventInstance() != null
                    || bot.getWorld() != leader.getWorld() || bot.getClient().getChannel() != leader.getClient().getChannel()
                    || manager.getEntryByBotCharId(bot.getId()) == null
                    || bot.getParty() != null && bot.getParty().getMembers().stream().anyMatch(p -> !ids.contains(p.getId())))
                throw new IllegalArgumentException("Ineligible LPQ companion: " + bot.getName());
        }
        for (var bot : bots) {
            var entry = manager.getEntryByBotCharId(bot.getId());
            manager.resumeFromOperatorCommand(entry);
            manager.issueStop(entry);
            BotLpqLater.reset(bot);
            BotBuildManager.setAutoApBuild(entry, bot);
            BotPqProvisioner.provision(bot);
            int cures = Math.max(0, 100 - bot.getItemQuantity(2050004, false));
            if (cures > 0 && !InventoryManipulator.addById(bot.getClient(), 2050004, (short) cures))
                throw new IllegalStateException("No room for LPQ cures");
            manager.partyUp(leader, bot);
        }
        if (leader.getParty() == null || leader.getParty().getLeaderId() != leader.getId()
                || leader.getParty().getMembers().size() != 6)
            throw new IllegalStateException("Could not assemble six party members");
        register(leader, bots);
    }

    static void register(Character leader, List<Character> bots) {
        int collector = bots.stream().mapToInt(Character::getId).min().orElseThrow();
        for (var bot : bots) {
            var state = new State();
            state.leader = leader;
            state.collector = collector;
            RUNS.put(bot, state);
        }
    }

    static void unregister(Character bot) { RUNS.remove(bot); }
    static boolean hasCrew(Character human) {
        synchronized (RUNS) {
            for (var state : RUNS.values()) if (state.leader == human) return true;
        }
        return false;
    }
    static Character leader(Character bot) { var state = RUNS.get(bot); return state == null ? null : state.leader; }
    static boolean belongsTo(Character bot, int id) { var human = leader(bot); return human != null && human.getId() == id; }
    static boolean isCollector(Character bot) {
        var state = RUNS.get(bot);
        return state == null ? BotLpqRun.isLeader(bot) : bot.getId() == state.collector;
    }
    static int stageMap(int map) { return map / 100 * 100; }

    static boolean canCollect(Character bot) {
        var state = RUNS.get(bot);
        // Room workers explicitly pick their own passes. Leave delivered piles for the human.
        if (state != null && stageMap(bot.getMapId()) == 922010500) return false;
        return isCollector(bot) && (state == null || !state.delivered.contains(stageMap(bot.getMapId())));
    }

    static int carriedWithLeader(Character bot, int item) {
        var human = leader(bot);
        return bot.getItemQuantity(item, false) + (human == null ? 0 : human.getItemQuantity(item, false));
    }

    /** Once a complete stage's items are delivered, never vacuum that pile back up. */
    static boolean deliver(BotEntry entry, Character bot, int item, int required, boolean ai) {
        var state = RUNS.get(bot);
        if (state == null) return false;
        int stage = stageMap(bot.getMapId());
        if (!state.delivered.contains(stage) && carriedWithLeader(bot, item) < required) return false;
        if (state.delivered.contains(stage)) return hold(entry, bot, ai);
        var human = state.leader;
        if (human.getMapId() != bot.getMapId()) return hold(entry, bot, ai);
        if (bot.getPosition().distanceSq(human.getPosition()) > 30 * 30) {
            BotZakumPqRun.walkTo(entry, bot, human.getPosition(), ai);
            return true;
        }
        hold(entry, bot, ai);
        int quantity = bot.getItemQuantity(item, false);
        if (quantity == 0 || BotManager.getInstance().issueDropItem(entry, InventoryType.ETC, item, (short) quantity)) {
            state.delivered.add(stage);
            if (stage == BotLpqRun.FIRST_MAP) BotLpqDialogue.delivered(entry);
            notice(bot, "passes-" + stage, "LPQ: Stage items are ready beside you. Pick them up and check the stage NPC when ready.");
        }
        return true;
    }

    static void notice(Character bot, String key, String text) {
        var state = RUNS.get(bot);
        if (state != null && state.notices.add(key)) {
            if (key.equals("rooms") || key.equals("vaults") || key.equals("numbers") || key.startsWith("boxes-")) {
                var entry = BotManager.getInstance().getEntryByBotCharId(bot.getId());
                if (entry != null) BotPqChat.queue(entry, text, true);
            } else state.leader.dropMessage(5, text);
        }
    }

    static boolean tick(BotEntry entry, Character bot, boolean ai) {
        var state = RUNS.get(bot);
        if (state == null) return false;
        var human = state.leader;
        boolean validLeader = human.getWorldServer().getPlayerStorage().getCharacterById(human.getId()) == human
                && human.getClient().getChannel() == bot.getClient().getChannel()
                && bot.getParty() != null && human.getParty() != null
                && human.getParty().getId() == bot.getParty().getId() && human.getParty().getLeaderId() == human.getId();
        if (!validLeader) {
            if (bot.getMapId() == BotLpqRun.RECRUIT_MAP) BotLpqCompanions.dismiss(bot);
            else exit(entry, bot, ai, state);
            return true;
        }
        if (bot.getMapId() == 922010000) return exit(entry, bot, ai, state);
        if (!BotLpqRun.inInstance(bot)) return hold(entry, bot, ai);
        if (state.event != bot.getEventInstance()) {
            state.event = bot.getEventInstance();
            state.delivered.clear();
            state.notices.clear();
            BotLpqLater.reset(bot);
        }
        if (human.getEventInstance() != bot.getEventInstance() && !bot.getEventInstance().isEventCleared())
            return exit(entry, bot, ai, state);
        BotLpqBoxReminder.tick(bot, human, System.currentTimeMillis());
        if (BotLpqCures.tryCure(bot)) return hold(entry, bot, ai);
        int map = bot.getMapId(), stage = stageMap(map), humanStage = stageMap(human.getMapId());
        if (BotLpqDialogue.tick(entry, bot, human, System.currentTimeMillis())) return hold(entry, bot, ai);
        if (BotLpqStageGuide.tick(entry, bot, human, System.currentTimeMillis())) return hold(entry, bot, ai);
        if (map <= 922010900) {
            if (humanStage < stage) return hold(entry, bot, ai);
            if (map == stage && humanStage == stage
                    && bot.getEventInstance().getProperty(((stage - BotLpqRun.FIRST_MAP) / 100 + 1) + "stageclear") != null) {
                if (isCollector(bot)) notice(bot, "clear-" + stage,
                        stage == 922010900 ? "LPQ: Alishar's stage is clear! Speak to the stage NPC to continue to the bonus and rewards."
                                : "LPQ: Stage clear! Use the opened portal when ready. Your crew will follow.");
                return hold(entry, bot, ai);
            }
        }
        if (map == BotLpqRun.FIRST_MAP || map == 922010300 || map == 922010700 || map == 922010900)
            return BotLpqStageThree.tick(entry, bot, ai);
        if (map == 922010200 || map == 922010201) return BotLpqBoxes.tick(entry, bot, ai);
        if (map >= 922010400 && map <= 922010405) return BotLpqRooms.tick(entry, bot, ai);
        if (map >= 922010500 && map <= 922010506) return BotLpqVaults.tick(entry, bot, ai);
        if (map == 922010800) return BotLpqHumanPuzzle.tick(entry, bot, ai);
        if (map == 922010600 || map == 922011000 || map == 922011100) return BotLpqLater.tick(entry, bot, ai);
        return hold(entry, bot, ai);
    }

    private static boolean exit(BotEntry entry, Character bot, boolean ai, State state) {
        var approach = BotTravelManager.tickApproachNpc(entry, bot, bot.getMapId(), 2040047, 2, ai, false, 150);
        if (approach == BotTravelManager.ApproachStatus.ARRIVED) {
            hold(entry, bot, ai);
            if (System.currentTimeMillis() >= state.nextAction) {
                state.nextAction = System.currentTimeMillis() + 1200;
                if (bot.getClient().getCM() != null) BotNpcInteraction.next(bot, 0);
                else BotNpcInteraction.talk(bot, 2040047);
            }
        }
        return true;
    }

    static boolean hold(BotEntry entry, Character bot, boolean ai) {
        BotTravelManager.clearMoveTargetPin(entry);
        BotManager.getInstance().stepMovementCore(entry, bot.getPosition(), ai);
        return true;
    }
}
