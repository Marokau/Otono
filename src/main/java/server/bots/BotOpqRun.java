package server.bots;

import java.awt.Point;

import client.BotClient;
import client.Character;
import client.inventory.InventoryType;
import client.inventory.manipulator.InventoryManipulator;
import scripting.event.EventInstanceManager;
import server.maps.MapItem;
import server.maps.Reactor;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

/** Explicit Orbis test crews. All progress comes from real drops, reactors, NPCs and portals. */
public final class BotOpqRun {
    public static final int RECRUIT_MAP = 200080101;
    public static final int FIRST_MAP = 920010000;
    public static final int HUB = 920010100;
    private static final class State {
        List<Integer> crew;
        int leader;
        long nextAction;
        long pendingUntil;
        int pendingItem;
    }
    private static final class Progress { int room = 920010200; }
    private static final Map<BotEntry, State> RUNS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<EventInstanceManager, Progress> PROGRESS = Collections.synchronizedMap(new WeakHashMap<>());
    private BotOpqRun() {}

    public static void startCrew(List<Character> crew) {
        var ids = crew.stream().map(Character::getId).toList();
        if (crew.size() != 6 || ids.stream().distinct().count() != 6) throw new IllegalArgumentException("OPQ requires six distinct party members");
        var manager = BotManager.getInstance();
        boolean human = BotOpqHuman.leader(crew.get(1)) == crew.getFirst();
        for (var bot : crew) {
            boolean humanLeader = human && bot == crew.getFirst();
            if ((!humanLeader && !(bot.getClient() instanceof BotClient)) || (!humanLeader && bot.isGM()) || bot.getLevel() < 51 || bot.getLevel() > 70
                    || bot.getEventInstance() != null || bot.getMapId() != RECRUIT_MAP
                    || bot.getWorld() != crew.getFirst().getWorld()
                    || bot.getClient().getChannel() != crew.getFirst().getClient().getChannel()
                    || (!humanLeader && manager.getEntryByBotCharId(bot.getId()) == null)
                    || bot.getParty() != null && bot.getParty().getMembers().stream().anyMatch(p -> !ids.contains(p.getId())))
                throw new IllegalArgumentException("Ineligible OPQ member: " + bot.getName());
        }
        for (var bot : crew) {
            if (human && bot == crew.getFirst()) continue;
            var entry = manager.getEntryByBotCharId(bot.getId());
            manager.resumeFromOperatorCommand(entry);
            manager.issueStop(entry);
            BotOpqGarden.reset(bot);
            BotBuildManager.setAutoApBuild(entry, bot);
            BotPqProvisioner.provision(bot);
            int cures = Math.max(0, 100 - bot.getItemQuantity(2050004, false));
            if (cures > 0 && !InventoryManipulator.addById(bot.getClient(), 2050004, (short) cures))
                throw new IllegalStateException("No room for OPQ cures");
            var state = new State();
            state.crew = ids;
            state.leader = ids.getFirst();
            RUNS.put(entry, state);
            if (bot != crew.getFirst()) manager.partyUp(crew.getFirst(), bot);
        }
        if (crew.getFirst().getParty() == null || crew.getFirst().getParty().getLeaderId() != ids.getFirst()
                || crew.getFirst().getParty().getMembers().size() != 6)
            throw new IllegalStateException("Could not assemble the OPQ party");
    }

    public static boolean inInstance(Character bot) {
        return bot != null && bot.getMapId() >= FIRST_MAP && bot.getMapId() <= 920011300 && bot.getEventInstance() != null;
    }
    static boolean isLeader(Character bot) { return inInstance(bot) && (BotOpqHuman.leader(bot) == null
            ? bot.getEventInstance().isEventLeader(bot) : BotOpqHuman.isCollector(bot)); }

    static boolean canLoot(BotEntry entry, Character bot, int item) {
        var state = RUNS.get(entry);
        // Celion's invisible item reactor must receive its death drop untouched.
        if (item == 4001074) return false;
        boolean collector = item >= 4001056 && item <= 4001062 ? BotOpqRecord.isCourier(bot) : isLeader(bot);
        return state != null && collector && BotOpqHuman.canCollect(bot, item)
                && !(state.pendingItem == item && System.currentTimeMillis() < state.pendingUntil);
    }

    public static boolean tick(BotEntry entry, Character bot, boolean ai) {
        var state = RUNS.get(entry);
        if (state == null) return false;
        entry.grinding = false; // Puzzle goals may approach ledges; combat alone uses grind margins.
        if (bot.getMapId() == FIRST_MAP && bot.getEventInstance() != null
                && bot.getEventInstance().getIntProperty("statusStg0") == 1
                && Math.abs(bot.getPosition().y - entry.physY) > 100) {
            BotPhysicsEngine.teleportTo(entry, bot, bot.getPosition());
            BotMovementManager.resetEntryStateAfterTeleport(entry);
        }
        if (BotOpqHuman.beforeTick(entry, bot, ai)) return true;
        if (BotOpqGarden.finished(bot)) return hold(entry, bot, ai);
        if (!inInstance(bot)) {
            if (bot.getMapId() != RECRUIT_MAP) return hold(entry, bot, ai);
            if (bot.getId() != state.leader) return hold(entry, bot, ai);
            for (int id : state.crew) {
                var member = bot.getWorldServer().getPlayerStorage().getCharacterById(id);
                if (member == null || member.getMapId() != RECRUIT_MAP || member.getHp() <= 0) return hold(entry, bot, ai);
            }
            return talk(entry, bot, 2013000, ai);
        }
        if (BotLpqCures.tryCure(bot)) return hold(entry, bot, ai);
        if (System.currentTimeMillis() < state.pendingUntil) return hold(entry, bot, ai);
        var progress = PROGRESS.computeIfAbsent(bot.getEventInstance(), ignored -> new Progress());
        int map = bot.getMapId();
        if (BotOpqHuman.leader(bot) != null && (map == 920010200 || map == 920010600)
                && BotOpqHuman.roomCleared(bot, map)) progress.room = HUB;
        if (map == FIRST_MAP) return clouds(entry, bot, ai);
        if (map == HUB) return hub(entry, bot, progress, ai);
        if (map >= 920010601 && map <= 920010604)
            return progress.room == 920010600 ? BotOpqRooms.lounge(entry, bot, ai) : travel(entry, bot, 920010600, ai);
        if (map >= 920010200 && map <= 920010700 && map != progress.room)
            return travel(entry, bot, HUB, ai);
        if (map == 920010200) {
            if (isLeader(bot) && bot.getItemQuantity(4001044, false) > 0) { progress.room = HUB; return travel(entry, bot, HUB, ai); }
            if (isLeader(bot) && bot.getItemQuantity(4001050, false) >= 30) return talk(entry, bot, 2013001, ai);
            if (collect(entry, bot, item -> item == 4001050 || item == 4001044, ai)) return true;
            return grind(entry, bot, ai);
        }
        if (map == 920010300) {
            if (isLeader(bot) && bot.getItemQuantity(4001045, false) > 0) { progress.room = HUB; return travel(entry, bot, HUB, ai); }
            if (collect(entry, bot, item -> item == 4001045, ai)) return true;
            if (!bot.getMap().getAllMonsters().isEmpty()) return grind(entry, bot, ai);
            return hold(entry, bot, ai); // Invisible reactors process the Celion death drops normally.
        }
        if (map == 920010400) return BotOpqRooms.lobby(entry, bot, ai);
        if (map == 920010500) return BotOpqRooms.sealed(entry, bot, ai);
        if (map == 920010600) return BotOpqRooms.lounge(entry, bot, ai);
        if (map == 920010700) return BotOpqAscent.tick(entry, bot, ai);
        if (map == 920010800) return BotOpqGarden.tick(entry, bot, ai);
        if (map == 920011300) return BotOpqGarden.reward(entry, bot, ai);
        return hold(entry, bot, ai);
    }

    private static boolean clouds(BotEntry entry, Character bot, boolean ai) {
        if (bot.getEventInstance().getIntProperty("statusStg0") == 1) {
            var exit = bot.getMap().getPortal("in00");
            if (exit != null && bot.getPosition().y > exit.getPosition().y + 100)
                return talk(entry, bot, 2013001, ai); // Eak normally transfers stragglers to the upper exit.
            return travel(entry, bot, HUB, ai);
        }
        if (bot.getMap().getNPCById(2013001) != null)
            return isLeader(bot) ? talk(entry, bot, 2013001, ai) : hold(entry, bot, ai);
        if (isLeader(bot) && bot.getItemQuantity(4001063, false) >= 20)
            return dropAt(entry, bot, bot.getMap().getReactorByName("eak"), 4001063, 20, ai);
        if (collect(entry, bot, item -> item == 4001063, ai)) return true;
        return smash(entry, bot, reactor -> reactor.getId() == 2002001, ai);
    }

    private static boolean hub(BotEntry entry, Character bot, Progress progress, boolean ai) {
        if (BotOpqGarden.restoredHub(entry, bot, ai)) return true;
        if (isLeader(bot) && BotOpqRooms.collectStatuePart(entry, bot, ai)) return true;
        if (!isLeader(bot)) return progress.room == HUB ? hold(entry, bot, ai) : travel(entry, bot, progress.room, ai);
        Reactor missing = null;
        for (int n = 1; n <= 6; n++) {
            var scar = bot.getMap().getReactorByName("scar" + n);
            if (scar == null || scar.getState() > 0) continue;
            var item = scar.getReactItem((byte) 0);
            if (item == null) continue;
            if (bot.getItemQuantity(item.getLeft(), false) >= item.getRight()) {
                progress.room = HUB;
                return dropAt(entry, bot, scar, item.getLeft(), item.getRight(), ai);
            }
            if (missing == null || item.getLeft() < missing.getReactItem((byte) 0).getLeft()) missing = scar;
        }
        if (missing == null) return talk(entry, bot, 2013001, ai);
        int item = missing.getReactItem((byte) 0).getLeft();
        progress.room = 920010200 + (item - 4001044) * 100;
        return travel(entry, bot, progress.room, ai);
    }

    static boolean collect(BotEntry entry, Character bot, java.util.function.IntPredicate wanted, boolean ai) {
        if (!isLeader(bot)) return false;
        var state = RUNS.get(entry);
        if (state == null || System.currentTimeMillis() < state.pendingUntil) return false;
        MapItem nearest = null;
        double distance = Double.MAX_VALUE;
        for (var item : bot.getMap().getDroppedItems()) {
            double d = bot.getPosition().distanceSq(item.getPosition());
            if (!item.isPickedUp() && wanted.test(item.getItemId()) && BotOpqHuman.canCollect(bot, item.getItemId()) && item.canBePickedBy(bot)
                    && System.currentTimeMillis() - item.getDropTime() >= 400 && d < distance) { nearest = item; distance = d; }
        }
        if (nearest == null) return false;
        BotZakumPqRun.stepTowardsAndPickUp(entry, bot, nearest, ai);
        return true;
    }

    static boolean dropAt(BotEntry entry, Character bot, Reactor reactor, int item, int quantity, boolean ai) {
        if (reactor == null) return hold(entry, bot, ai);
        var state = RUNS.get(entry);
        if (state == null || System.currentTimeMillis() < state.pendingUntil) return hold(entry, bot, ai);
        var p = reactor.getPosition();
        if (!reactor.getArea().contains(bot.getPosition()) || entry.inAir || entry.climbing) {
            BotZakumPqRun.walkTo(entry, bot, p, ai);
            return true;
        }
        hold(entry, bot, ai);
        if (ready(entry) && BotManager.getInstance().issueDropItem(entry, InventoryType.ETC, item, (short) quantity)) {
            state.pendingItem = item;
            state.pendingUntil = System.currentTimeMillis() + 8000;
        }
        return true;
    }

    static boolean smash(BotEntry entry, Character bot, Predicate<Reactor> wanted, boolean ai) {
        Reactor nearest = null;
        double distance = Double.MAX_VALUE;
        for (var r : bot.getMap().getAllReactors()) {
            double d = bot.getPosition().distanceSq(r.getPosition());
            if (r.isActive() && wanted.test(r) && d < distance) { nearest = r; distance = d; }
        }
        if (nearest == null) return hold(entry, bot, ai);
        var p = nearest.getPosition();
        if (Math.abs(bot.getPosition().x - p.x) <= 70 && Math.abs(bot.getPosition().y - p.y) <= 45) {
            hold(entry, bot, ai);
            if (ready(entry)) BotOpqReactorAttack.hit(entry, bot, nearest);
        } else if (Math.abs(bot.getPosition().x - p.x) <= 70 && bot.getPosition().y > p.y
                && bot.getPosition().y - p.y <= BotPhysicsEngine.calculateMaxJumpHeight(entry.movementProfile) + 45
                && !entry.inAir && !entry.climbing) {
            BotTravelManager.clearMoveTargetPin(entry);
            BotMovementManager.initiateJump(entry, bot, 0);
            entry.fixedAirArc = true;
            BotManager.getInstance().stepMovementCore(entry, bot.getPosition(), ai);
        } else if (Math.abs(bot.getPosition().x - p.x) <= 70 && bot.getPosition().y < p.y - 45
                && !entry.inAir && !entry.climbing && BotPhysicsEngine.canStartDownJump(bot.getMap(), bot.getPosition())) {
            var state = RUNS.get(entry);
            if (state != null && System.currentTimeMillis() < state.nextAction) return hold(entry, bot, ai);
            BotTravelManager.clearMoveTargetPin(entry);
            entry.downJumpPending = true;
            BotManager.getInstance().stepMovementCore(entry, bot.getPosition(), ai);
        } else {
            var stance = BotOpqReactorApproach.stance(BotNavigationGraphProvider.peekGraph(bot.getMap(), entry.movementProfile), bot, p);
            BotZakumPqRun.walkTo(entry, bot, stance == null ? p : stance, ai);
        }
        return true;
    }

    static boolean grind(BotEntry entry, Character bot, boolean ai) {
        entry.grinding = true;
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

    static boolean talk(BotEntry entry, Character bot, int npc, boolean ai) {
        if (BotOpqHuman.npc(entry, bot, npc, ai)) return true;
        var approach = BotTravelManager.tickApproachNpc(entry, bot, bot.getMapId(), npc, 2, ai, false, 150);
        if (approach == BotTravelManager.ApproachStatus.ARRIVED) {
            hold(entry, bot, ai);
            if (ready(entry)) {
                int oldMap = bot.getMapId();
                Point oldPosition = new Point(bot.getPosition());
                if (bot.getClient().getCM() != null) BotNpcInteraction.next(bot, 0);
                else BotNpcInteraction.talk(bot, npc);
                if (bot.getMapId() == oldMap && !bot.getPosition().equals(oldPosition)) {
                    BotPhysicsEngine.teleportTo(entry, bot, bot.getPosition()); // Reconcile Eak's actual scripted transfer.
                    BotMovementManager.resetEntryStateAfterTeleport(entry);
                }
            }
        }
        return true;
    }
    static boolean ready(BotEntry entry) {
        var state = RUNS.get(entry);
        if (state == null || System.currentTimeMillis() < state.nextAction) return false;
        state.nextAction = System.currentTimeMillis() + ThreadLocalRandom.current().nextLong(1000, 1400);
        return true;
    }
    static boolean travel(BotEntry entry, Character bot, int map, boolean ai) {
        var portal = BotOpqPortals.roomPortal(bot.getMap(), map);
        if (portal != null) {
            if (bot.getMapId() == HUB && BotOpqHuman.leader(bot) != null)
                BotOpqHuman.notice(bot, "room-door-" + map, "OPQ: Next is " + BotOpqHuman.roomName(map)
                        + ". Use the Center Tower portal (" + switch (map) {
                            case 920010200 -> "top left";
                            case 920010300 -> "upper right";
                            case 920010400 -> "bottom right";
                            case 920010500 -> "upper left, below the Walkway portal";
                            case 920010600 -> "upper middle, below the very top";
                            case 920010700 -> "very top, middle";
                            default -> "toward " + BotOpqHuman.roomName(map);
                        } + "). We will wait inside.");
            if (!portal.getPortalStatus()) return hold(entry, bot, ai);
            BotTravelManager.walkToPortalAndEnter(entry, bot, portal, System.currentTimeMillis(), ai);
            return true;
        }
        if (bot.getMapId() == 920010600 && (map == 920010602 || map == 920010603))
            return BotOpqLoungeLift.tick(entry, bot, map, ai);
        BotTravelManager.tickTravel(entry, bot, map, 2, ai, false);
        return true;
    }
    static boolean hold(BotEntry entry, Character bot, boolean ai) {
        BotTravelManager.clearMoveTargetPin(entry);
        BotManager.getInstance().stepMovementCore(entry, bot.getPosition(), ai);
        return true;
    }
}
