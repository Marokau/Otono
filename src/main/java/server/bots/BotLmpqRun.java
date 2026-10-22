package server.bots;

import client.BotClient;
import client.Character;
import client.inventory.InventoryType;
import scripting.event.EventInstanceManager;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Legal maze exploration, real coupon collection, ordinary leader checks and individual rewards. */
public final class BotLmpqRun {
    public static final int RECRUIT = 220000000, FIRST = 809050000, FINISH = 809050015;
    public static final int REWARD = 809050016, EXIT = 809050017, COUPON = 4001106;
    private static final class Crew {
        Character leader, collector;
        List<Character> bots;
        boolean human;
        int room = FIRST + 5;
        final Set<Integer> cleared = new HashSet<>();
        final Set<String> notices = new HashSet<>();
        EventInstanceManager event;
        boolean delivered;
    }
    private static final class State {
        Crew crew;
        long nextAction, quietSince;
        int lastMap;
        boolean rewardSeen, finished;
    }
    private static final Map<Character, State> RUNS = Collections.synchronizedMap(new WeakHashMap<>());
    private BotLmpqRun() {}
    public static boolean inInstance(Character bot) {
        return bot != null && bot.getEventInstance() != null && bot.getMapId() >= FIRST && bot.getMapId() <= REWARD;
    }
    public static void startCrew(List<Character> bots) { start(bots.getFirst(), bots, false); }
    static void startHuman(Character leader, List<Character> bots) { start(leader, bots, true); }
    private static void start(Character leader, List<Character> bots, boolean human) {
        int size = bots.size() + (human ? 1 : 0);
        if (size < 3 || size > 6 || bots.stream().map(Character::getId).distinct().count() != bots.size()
                || (human && leader.getClient() instanceof BotClient)) throw new IllegalArgumentException("Invalid LMPQ crew");
        var ids = new HashSet<>(bots.stream().map(Character::getId).toList()); ids.add(leader.getId());
        if (leader.getLevel() < 51 || leader.getLevel() > 70 || leader.getMapId() != RECRUIT || leader.getEventInstance() != null)
            throw new IllegalArgumentException("Ineligible LMPQ leader");
        var manager = BotManager.getInstance();
        for (var bot : bots) {
            if (!(bot.getClient() instanceof BotClient) || bot.isGM() || bot.getLevel() < 51 || bot.getLevel() > 70
                    || bot.getMapId() != RECRUIT || bot.getEventInstance() != null || bot.getWorld() != leader.getWorld()
                    || bot.getClient().getChannel() != leader.getClient().getChannel()
                    || manager.getEntryByBotCharId(bot.getId()) == null
                    || bot.getParty() != null && bot.getParty().getMembers().stream().anyMatch(p -> !ids.contains(p.getId())))
                throw new IllegalArgumentException("Ineligible LMPQ companion");
        }
        var crew = new Crew(); crew.leader = leader; crew.bots = List.copyOf(bots); crew.human = human;
        crew.collector = human ? bots.stream().min(Comparator.comparingInt(Character::getId)).orElseThrow() : leader;
        for (var bot : bots) {
            var entry = manager.getEntryByBotCharId(bot.getId());
            manager.resumeFromOperatorCommand(entry); manager.issueStop(entry);
            BotBuildManager.setAutoApBuild(entry, bot); BotPqProvisioner.provision(bot);
            var state = new State(); state.crew = crew; RUNS.put(bot, state);
            if (bot != leader) manager.partyUp(leader, bot);
        }
        if (leader.getParty() == null || leader.getParty().getLeaderId() != leader.getId()
                || leader.getParty().getMembers().size() != size)
            throw new IllegalStateException("Could not assemble the LMPQ party");
    }
    static boolean belongsTo(Character bot, int leader) { var s = RUNS.get(bot); return s != null && s.crew.human && s.crew.leader.getId() == leader; }
    static void unregister(Character bot) { RUNS.remove(bot); }
    static boolean canLoot(Character bot) {
        var s = RUNS.get(bot); return s != null && bot == s.crew.collector && !s.crew.delivered;
    }
    static boolean finished(Character bot) { var s = RUNS.get(bot); return s != null && s.finished; }
    public static boolean tick(BotEntry entry, Character bot, boolean ai) {
        var state = RUNS.get(bot); if (state == null) return false;
        var crew = state.crew;
        entry.grinding = false;
        if (crew.human) {
            var human = crew.leader;
            boolean valid = human.getWorldServer().getPlayerStorage().getCharacterById(human.getId()) == human
                    && human.getClient().getChannel() == bot.getClient().getChannel()
                    && bot.getParty() != null && human.getParty() != null
                    && bot.getParty().getId() == human.getParty().getId()
                    && human.getParty().getLeaderId() == human.getId();
            if (!valid || inInstance(bot) && !bot.getEventInstance().isEventCleared()
                    && human.getEventInstance() != bot.getEventInstance()) {
                if (bot.getMapId() == RECRUIT) { BotLmpqCompanions.dismiss(bot); return true; }
                if (inInstance(bot) && !bot.getEventInstance().isEventCleared() && bot.getParty() != null)
                    net.server.world.Party.leaveParty(bot.getParty(), bot.getClient());
                if (bot.getMapId() == EXIT) return talk(entry, bot, 9103003, ai);
                if (bot.getMapId() != REWARD) return BotOpqRun.hold(entry, bot, ai);
            }
        }
        if (crew.human && inInstance(bot) && BotPqStageGuide.tick(entry,bot,crew.leader,bot.getEventInstance(),"lmpq",System.currentTimeMillis()))
            return BotOpqRun.hold(entry,bot,ai);
        if (state.finished && !inInstance(bot)) return BotOpqRun.hold(entry, bot, ai);
        if (bot.getMapId() == REWARD) { state.rewardSeen = true; return talk(entry, bot, 9103002, ai); }
        if (bot.getMapId() == EXIT) return talk(entry, bot, 9103003, ai);
        if (!inInstance(bot)) {
            if (state.rewardSeen && bot.getMapId() == RECRUIT) state.finished = true;
            if (!crew.human && !state.finished && bot == crew.leader
                    && crew.bots.stream().allMatch(b -> b.getMapId() == RECRUIT && b.getHp() > 0))
                return talk(entry, bot, 9103001, ai);
            return BotOpqRun.hold(entry, bot, ai);
        }
        if (BotLpqCures.tryCure(bot)) return BotOpqRun.hold(entry, bot, ai);
        synchronized (crew) {
            if (crew.event != bot.getEventInstance()) {
                crew.event = bot.getEventInstance(); crew.room = FIRST + 5;
                crew.cleared.clear(); crew.notices.clear(); crew.delivered = false;
                for (var member : crew.bots) { var s = RUNS.get(member); if (s != null) { s.finished = false; s.rewardSeen = false; s.quietSince = 0; } }
            }
            if (crew.human) hint(crew);
            if (bot.getMapId() != crew.room) { travel(entry, bot, crew.room, ai); return true; }
            if (crew.human && crew.leader.getMapId()!=crew.room && !crew.cleared.contains(crew.room))
                return BotOpqRun.hold(entry,bot,ai);
            if (crew.room == FINISH) return finish(entry, bot, state, ai);
            if (collect(entry, bot, state, ai)) return true;
            if (!bot.getMap().getAllMonsters().isEmpty()) { state.quietSince = 0; return BotOpqRun.grind(entry, bot, ai); }
            server.maps.Reactor box = null;
            for (var r : bot.getMap().getAllReactors())
                if (r.isActive() && r.getState() < 4 && (box == null || bot.getPosition().distanceSq(r.getPosition()) < bot.getPosition().distanceSq(box.getPosition()))) box = r;
            if (box != null) { state.quietSince = 0; return smash(entry, bot, state, box, ai); }
            if (bot != crew.collector || bot.getMap().getDroppedItems().stream().anyMatch(d -> d.getItemId() == COUPON && !d.isPickedUp()))
                return BotOpqRun.hold(entry, bot, ai);
            if (state.quietSince == 0) state.quietSince = System.currentTimeMillis();
            if (System.currentTimeMillis() - state.quietSince < 1800) return BotOpqRun.hold(entry, bot, ai);
            if (crew.human && crew.leader.getMapId() != crew.room) return BotOpqRun.hold(entry, bot, ai);
            crew.cleared.add(crew.room);
            crew.room = held(crew) >= 30 ? FINISH : nextRoom(crew.cleared);
            state.quietSince = 0;
            return BotOpqRun.hold(entry, bot, ai);
        }
    }
    static int nextRoom(Set<Integer> cleared) {
        for (int room : new int[]{5,6,0,1,2,3,4,7,8,9,10}) if (!cleared.contains(FIRST + room)) return FIRST + room;
        return FINISH;
    }
    private static int held(Crew crew) { return crew.collector.getItemQuantity(COUPON, false) + (crew.human ? crew.leader.getItemQuantity(COUPON, false) : 0); }
    private static boolean collect(BotEntry entry, Character bot, State state, boolean ai) {
        if (!canLoot(bot)) return false;
        server.maps.MapItem best = null;
        for (var d : bot.getMap().getDroppedItems()) {
            if (d.getItemId() != COUPON || d.isPickedUp() || !d.canBePickedBy(bot) || System.currentTimeMillis() - d.getDropTime() < 400) continue;
            if (best == null || bot.getPosition().distanceSq(d.getPosition()) < bot.getPosition().distanceSq(best.getPosition())) best = d;
        }
        if (best == null) return false;
        BotZakumPqRun.stepTowardsAndPickUp(entry, bot, best, ai); return true;
    }
    private static boolean finish(BotEntry entry, Character bot, State state, boolean ai) {
        var crew = state.crew;
        if (crew.human) {
            if (crew.leader.getMapId() != FINISH) return BotOpqRun.hold(entry, bot, ai);
            if (bot == crew.collector) {
                int quantity = bot.getItemQuantity(COUPON, false);
                if (quantity > 0) {
                    if (bot.getPosition().distanceSq(crew.leader.getPosition()) > 30 * 30) { BotZakumPqRun.walkTo(entry, bot, crew.leader.getPosition(), ai); return true; }
                    // The normal drop API handles one inventory stack, not the total carried quantity.
                    if (ready(state) && BotManager.getInstance().issueDropItem(entry, InventoryType.ETC, COUPON, (short) 0)) crew.delivered = true;
                    if (crew.delivered && crew.notices.add("passing")) crew.leader.dropMessage(5, "LMPQ: Passing you the coupon stacks. Pick them up, but wait for our ready message before checking Pietri.");
                    return BotOpqRun.hold(entry, bot, ai);
                }
                crew.delivered = true;
            }
            if (crew.delivered && crew.collector.getItemQuantity(COUPON, false) == 0 && crew.notices.add("turn-in")) crew.leader.dropMessage(5, "LMPQ: All coupon stacks are ready beside you. Pick them all up, then talk to Pietri when everyone has arrived. We will wait for your check.");
            return BotOpqRun.hold(entry, bot, ai);
        }
        if (bot == crew.leader && held(crew) >= 30 && crew.bots.stream().allMatch(b -> b.getMapId() == FINISH)) return talk(entry, bot, 9103000, ai);
        return BotOpqRun.hold(entry, bot, ai);
    }
    private static boolean smash(BotEntry entry, Character bot, State state, server.maps.Reactor box, boolean ai) {
        var p = box.getPosition();
        if (Math.abs(bot.getPosition().x - p.x) <= 70 && Math.abs(bot.getPosition().y - p.y) <= 45) {
            BotOpqRun.hold(entry, bot, ai);
            if (ready(state)) BotZakumPqRun.hitReactor(bot, box);
        } else BotZakumPqRun.walkTo(entry, bot, p, ai);
        return true;
    }
    private static boolean talk(BotEntry entry, Character bot, int npc, boolean ai) {
        var status = BotTravelManager.tickApproachNpc(entry, bot, bot.getMapId(), npc, 2, ai, false, 150);
        if (status == BotTravelManager.ApproachStatus.ARRIVED) {
            BotOpqRun.hold(entry, bot, ai);
            if (ready(RUNS.get(bot))) { if (bot.getClient().getCM() != null) BotNpcInteraction.next(bot, 0); else BotNpcInteraction.talk(bot, npc); }
        }
        return true;
    }
    private static boolean ready(State s) {
        if (System.currentTimeMillis() < s.nextAction) return false;
        s.nextAction = System.currentTimeMillis() + ThreadLocalRandom.current().nextLong(900, 1300); return true;
    }
    static void travel(BotEntry entry, Character bot, int target, boolean ai) {
        BotTravelManager.tickTravel(entry, bot, target, 16, ai, false);
    }
    private static void hint(Crew crew) {
        var human = crew.leader;
        if (human.getEventInstance() != crew.event || human.getMapId() == crew.room) return;
        String key = human.getMapId() + ":" + crew.room;
        if (!crew.notices.add(key)) return;
        var route = BotWorldGraph.route(human.getMapId(), crew.room, 16);
        if (route == null || route.isEmpty()) return;
        var portals = human.getMap().getPortals().stream().filter(p -> p.getTargetMapId() >= FIRST && p.getTargetMapId() <= FINISH)
                .sorted(Comparator.comparingInt(p -> p.getPosition().x)).toList();
        for (int n = 0; n < portals.size(); n++) if (portals.get(n).getTargetMapId() == route.getFirst()) {
            String direction = portals.size() == 1 ? "only" : n == 0 ? "left" : n == 1 ? "middle" : "right";
            human.dropMessage(5, "LMPQ: We are heading to room " + (crew.room - FIRST + 1) + ". From room " + (human.getMapId() - FIRST + 1) + ", take the " + direction + " portal.");
            break;
        }
    }
}
