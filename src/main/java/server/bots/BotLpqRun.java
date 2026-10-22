package server.bots;

import client.BotClient;
import client.Character;
import client.inventory.manipulator.InventoryManipulator;
import server.maps.MapItem;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Explicit LPQ test crews: normal NPC entry, real combat/pass pickup, and stage portals. */
public final class BotLpqRun {
    public static final int RECRUIT_MAP = 221024500;
    public static final int FIRST_MAP = 922010100;
    public static final int PASS = 4001022;
    private static final Map<BotEntry, State> RUNS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final class State {
        List<Integer> crew;
        int leader;
        long nextTalk;
    }

    private BotLpqRun() {}

    public static void startCrew(List<Character> crew) {
        var ids = crew.stream().map(Character::getId).toList();
        if (crew.size() != 6 || ids.stream().distinct().count() != 6)
            throw new IllegalArgumentException("LPQ test requires six distinct members");
        var manager = BotManager.getInstance();
        for (var bot : crew) {
            if (!(bot.getClient() instanceof BotClient) || bot.isGM() || bot.getLevel() < 35 || bot.getLevel() > 50
                    || bot.getMapId() != RECRUIT_MAP || bot.getEventInstance() != null
                    || bot.getWorld() != crew.getFirst().getWorld()
                    || bot.getClient().getChannel() != crew.getFirst().getClient().getChannel()
                    || manager.getEntryByBotCharId(bot.getId()) == null
                    || bot.getParty() != null && bot.getParty().getMembers().stream().anyMatch(p -> !ids.contains(p.getId())))
                throw new IllegalArgumentException("Ineligible LPQ member: " + bot.getName());
        }
        for (var bot : crew) {
            BotLpqLater.reset(bot);
            var entry = manager.getEntryByBotCharId(bot.getId());
            manager.resumeFromOperatorCommand(entry);
            manager.issueStop(entry);
            BotBuildManager.setAutoApBuild(entry, bot);
            BotPqProvisioner.provision(bot);
            int cures = Math.max(0, 100 - bot.getItemQuantity(2050004, false)); // handbook: All Cure Potion
            if (cures > 0 && !InventoryManipulator.addById(bot.getClient(), 2050004, (short) cures))
                throw new IllegalStateException("No room for LPQ cures");
            var state = new State();
            state.crew = ids;
            state.leader = crew.getFirst().getId();
            RUNS.put(entry, state);
            if (bot != crew.getFirst()) manager.partyUp(crew.getFirst(), bot);
        }
    }

    public static boolean inInstance(Character bot) {
        return bot != null && bot.getMapId() >= FIRST_MAP && bot.getMapId() <= 922011100
                && bot.getEventInstance() != null;
    }

    public static boolean isLeader(Character bot) {
        return inInstance(bot) && bot.getEventInstance().isEventLeader(bot);
    }

    public static boolean tick(BotEntry entry, Character bot, boolean ai) {
        if (BotLpqHuman.leader(bot) != null) return BotLpqHuman.tick(entry, bot, ai);
        var state = RUNS.get(entry);
        if (state == null) return false;
        if (BotLpqLater.finished(bot)) return hold(entry, bot, ai);
        if (inInstance(bot) && BotLpqCures.tryCure(bot)) return hold(entry, bot, ai);
        if (!inInstance(bot)) {
            if (bot.getMapId() != RECRUIT_MAP) return hold(entry, bot, ai);
            var approach = BotTravelManager.tickApproachNpc(entry, bot, RECRUIT_MAP, 2040034, 2, ai, false, 300);
            if (approach != BotTravelManager.ApproachStatus.ARRIVED) return true;
            hold(entry, bot, ai);
            if (bot.getId() != state.leader) return true;
            if (bot.getParty() == null || bot.getParty().getMembers().size() != state.crew.size()) return true;
            for (int id : state.crew) {
                var member = bot.getWorldServer().getPlayerStorage().getCharacterById(id);
                if (member == null || member.getMapId() != RECRUIT_MAP || member.getHp() <= 0) return true;
            }
            talk(bot, state, 2040034);
            return true;
        }
        if (bot.getMapId() == 922010200 || bot.getMapId() == 922010201)
            return BotLpqBoxes.tick(entry, bot, ai);
        if (bot.getMapId() == 922010300) return BotLpqStageThree.tick(entry, bot, ai);
        if (bot.getMapId() >= 922010400 && bot.getMapId() <= 922010405) return BotLpqRooms.tick(entry, bot, ai);
        if (bot.getMapId() >= 922010500 && bot.getMapId() <= 922010506) return BotLpqVaults.tick(entry, bot, ai);
        if (bot.getMapId() == 922010700 || bot.getMapId() == 922010900) return BotLpqStageThree.tick(entry, bot, ai);
        if (bot.getMapId() == 922010800) return BotLpqPuzzle.tick(entry, bot, ai);
        if (bot.getMapId() == 922010600 || bot.getMapId() == 922011000 || bot.getMapId() == 922011100)
            return BotLpqLater.tick(entry, bot, ai);
        if (bot.getMapId() != FIRST_MAP) return hold(entry, bot, ai); // Next stages are verified incrementally.
        if (bot.getEventInstance().getProperty("1stageclear") != null) {
            BotTravelManager.tickTravel(entry, bot, FIRST_MAP + 100, 2, ai, false);
            return true;
        }
        if (isLeader(bot)) {
            if (bot.getItemQuantity(PASS, false) >= 25) {
                var approach = BotTravelManager.tickApproachNpc(entry, bot, FIRST_MAP, 2040036, 2, ai, false, 300);
                if (approach == BotTravelManager.ApproachStatus.ARRIVED) {
                    hold(entry, bot, ai);
                    talk(bot, state, 2040036);
                }
                return true;
            }
            MapItem drop = bot.getMap().getDroppedItems().stream()
                    .filter(d -> !d.isPickedUp() && d.getItemId() == PASS && d.canBePickedBy(bot)
                            && System.currentTimeMillis() - d.getDropTime() >= 400)
                    .min(Comparator.comparingDouble(d -> bot.getPosition().distanceSq(d.getPosition()))).orElse(null);
            if (drop != null) {
                BotZakumPqRun.stepTowardsAndPickUp(entry, bot, drop, ai);
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
            if (nearest != null) {
                BotZakumPqRun.walkTo(entry, bot, nearest.getPosition(), ai);
                return true;
            }
        }
        BotManager.getInstance().tickPqGrinding(entry, bot, ai);
        return true;
    }

    private static void talk(Character bot, State state, int npc) {
        long now = System.currentTimeMillis();
        if (now < state.nextTalk) return;
        state.nextTalk = now + ThreadLocalRandom.current().nextLong(1000, 1600);
        if (bot.getClient().getCM() != null) BotNpcInteraction.next(bot, 0);
        else BotNpcInteraction.talk(bot, npc);
    }

    private static boolean hold(BotEntry entry, Character bot, boolean ai) {
        BotTravelManager.clearMoveTargetPin(entry);
        BotManager.getInstance().stepMovementCore(entry, bot.getPosition(), ai);
        return true;
    }
}
