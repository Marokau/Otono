package server.bots;

import client.BuffStat;
import client.Character;
import client.inventory.InventoryType;
import constants.skills.ILWizard;
import constants.skills.FPWizard;
import constants.skills.Cleric;
import constants.skills.Rogue;
import scripting.event.EventInstanceManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/** Stage-five assigned rooms, skill traversal, real box drops and ordinary pass handoff. */
final class BotLpqVaults {
    private static final class State {
        final Map<Integer, Integer> rooms = new HashMap<>();
        final Set<Integer> done = ConcurrentHashMap.newKeySet();
        final Set<Integer> roomDone = ConcurrentHashMap.newKeySet();
        final Map<Integer, Long> nextAction = new ConcurrentHashMap<>();
        State(EventInstanceManager event) {
            var crew = new ArrayList<Character>();
            for (var member : event.getPlayers())
                if (member.getClient() instanceof client.BotClient) crew.add(member);
            crew.sort(Comparator.comparingInt(Character::getId));
            var mage = crew.stream().filter(b -> b.getSkillLevel(ILWizard.TELEPORT) > 0
                    || b.getSkillLevel(FPWizard.TELEPORT) > 0 || b.getSkillLevel(Cleric.TELEPORT) > 0).findFirst().orElse(null);
            var thief = crew.stream().filter(b -> b.getSkillLevel(Rogue.DARK_SIGHT) > 0).findFirst().orElse(null);
            if (mage == null || thief == null || mage == thief) return;
            rooms.put(mage.getId(), 922010501);
            rooms.put(thief.getId(), 922010506);
            int room = 922010502;
            for (var b : crew) if (!rooms.containsKey(b.getId()) && room <= 922010505) rooms.put(b.getId(), room++);
        }
    }
    private static final Map<EventInstanceManager, State> RUNS = Collections.synchronizedMap(new WeakHashMap<>());
    private BotLpqVaults() {}

    static boolean needsDarkSight(Character bot) {
        if (bot == null || (bot.getMapId() != 922010500 && bot.getMapId() != 922010506)) return false;
        var state = RUNS.get(bot.getEventInstance());
        return state != null && state.rooms.getOrDefault(bot.getId(), 0) == 922010506 && !state.done.contains(bot.getId());
    }

    static boolean tick(BotEntry entry, Character bot, boolean ai) {
        var event = bot.getEventInstance();
        var state = RUNS.computeIfAbsent(event, State::new);
        boolean main = bot.getMapId() == 922010500;
        if (event.getProperty("5stageclear") != null) {
            BotTravelManager.tickTravel(entry, bot, main ? 922010600 : 922010500, 2, ai, false);
            return true;
        }
        var leader = event.getPlayers().stream().filter(event::isEventLeader).findFirst().orElse(null);
        boolean humanLed = BotLpqHuman.leader(bot) != null;
        if (leader == null || state.rooms.size() != (humanLed ? 5 : 6)) return hold(entry, bot, ai);
        // Five companions cover six rooms: the first ordinary-room worker takes
        // the unassigned final room after delivering its first four passes.
        if (humanLed && main && state.done.contains(bot.getId())
                && state.rooms.getOrDefault(bot.getId(), 0) == 922010502 && !state.rooms.containsValue(922010505)) {
            state.rooms.put(bot.getId(), 922010505);
            state.done.remove(bot.getId());
            state.roomDone.remove(bot.getId());
        }
        int room = state.rooms.getOrDefault(bot.getId(), 0);
        if (room == 0) return hold(entry, bot, ai);
        if (needsDarkSight(bot) && bot.getBuffedValue(BuffStat.DARKSIGHT) == null) return hold(entry, bot, ai);
        if (!main) {
            var pass = nearestPass(bot);
            if (pass != null) { BotZakumPqRun.stepTowardsAndPickUp(entry, bot, pass, ai); return true; }
            boolean emptyRoom = true;
            for (var reactor : bot.getMap().getAllReactors())
                if (reactor.isActive() && reactor.getId() == 2202003) emptyRoom = false;
            for (var drop : bot.getMap().getDroppedItems())
                if (!drop.isPickedUp() && drop.getItemId() == BotLpqRun.PASS) emptyRoom = false;
            if (bot.getItemQuantity(BotLpqRun.PASS, false) >= 4 || emptyRoom) {
                state.roomDone.add(bot.getId());
                BotTravelManager.tickTravel(entry, bot, 922010500, 2, ai, false);
                return true;
            }
            server.maps.Reactor box = null;
            double distance = Double.MAX_VALUE;
            for (var r : bot.getMap().getAllReactors()) {
                double d = bot.getPosition().distanceSq(r.getPosition());
                if (r.isActive() && r.getId() == 2202003 && d < distance) { box = r; distance = d; }
            }
            if (box == null) return hold(entry, bot, ai);
            var p = box.getPosition();
            if (Math.abs(bot.getPosition().x - p.x) <= 70 && Math.abs(bot.getPosition().y - p.y) <= 45
                    && !entry.inAir && !entry.climbing) {
                hold(entry, bot, ai);
                if (ready(state, bot)) {
                    // A real attack ends Dark Sight. Only attack from a spot clear of guards;
                    // the ordinary buff pipeline restores it before traversal resumes.
                    if (bot.getBuffedValue(BuffStat.DARKSIGHT) != null) {
                        for (var mob : bot.getMap().getAllMonsters())
                            if (mob.isAlive() && BotCombatManager.isMobTouchingBot(entry, bot, mob)) return true;
                        bot.cancelEffectFromBuffStat(BuffStat.DARKSIGHT);
                    }
                    BotZakumPqRun.hitReactor(bot, box);
                }
            } else BotZakumPqRun.walkTo(entry, bot, p, ai);
            return true;
        }
        if (BotLpqRun.isLeader(bot)) {
            var pass = nearestPass(bot);
            if (pass != null) { BotZakumPqRun.stepTowardsAndPickUp(entry, bot, pass, ai); return true; }
            if (state.roomDone.contains(bot.getId())) state.done.add(bot.getId());
            if (bot.getItemQuantity(BotLpqRun.PASS, false) >= 24) {
                var approach = BotTravelManager.tickApproachNpc(entry, bot, 922010500, 2040040, 2, ai, false, 300);
                if (approach == BotTravelManager.ApproachStatus.ARRIVED && ready(state, bot)) {
                    if (bot.getClient().getCM() != null) BotNpcInteraction.next(bot, 0);
                    else BotNpcInteraction.talk(bot, 2040040);
                }
                return true;
            }
        } else if (!state.done.contains(bot.getId()) && state.roomDone.contains(bot.getId())
                && (humanLed || bot.getItemQuantity(BotLpqRun.PASS, false) >= 4)) {
            if (leader.getMapId() != bot.getMapId() || !humanLed && !state.roomDone.contains(leader.getId())) return hold(entry, bot, ai);
            if (bot.getPosition().distanceSq(leader.getPosition()) > 30 * 30)
                BotZakumPqRun.walkTo(entry, bot, leader.getPosition(), ai);
            else if (ready(state, bot) && (bot.getItemQuantity(BotLpqRun.PASS, false) == 0
                    || BotManager.getInstance().issueDropItem(entry, InventoryType.ETC, BotLpqRun.PASS,
                    (short) bot.getItemQuantity(BotLpqRun.PASS, false)))) {
                state.done.add(bot.getId());
                if (humanLed) BotLpqHuman.notice(bot, "vault-delivery-" + room,
                        "LPQ: " + bot.getName() + " delivered room passes beside you. Pick them up; submit 24 passes to clear this stage.");
                if (bot.getBuffedValue(BuffStat.DARKSIGHT) != null) bot.cancelEffectFromBuffStat(BuffStat.DARKSIGHT);
            }
            return true;
        }
        if (!state.done.contains(bot.getId())) {
            BotTravelManager.tickTravel(entry, bot, room, 2, ai, false);
            return true;
        }
        var approach = BotTravelManager.tickApproachNpc(entry, bot, 922010500, 2040040, 2, ai, false, 200);
        if (approach == BotTravelManager.ApproachStatus.ARRIVED) hold(entry, bot, ai);
        return true;
    }

    private static server.maps.MapItem nearestPass(Character bot) {
        return bot.getMap().getDroppedItems().stream().filter(d -> !d.isPickedUp() && d.getItemId() == BotLpqRun.PASS
                && d.canBePickedBy(bot) && System.currentTimeMillis() - d.getDropTime() >= 400)
                .min(Comparator.comparingDouble(d -> bot.getPosition().distanceSq(d.getPosition()))).orElse(null);
    }
    private static boolean ready(State state, Character bot) {
        long now = System.currentTimeMillis();
        if (now < state.nextAction.getOrDefault(bot.getId(), 0L)) return false;
        state.nextAction.put(bot.getId(), now + ThreadLocalRandom.current().nextLong(1000, 1400));
        return true;
    }
    private static boolean hold(BotEntry entry, Character bot, boolean ai) {
        BotTravelManager.clearMoveTargetPin(entry);
        BotManager.getInstance().stepMovementCore(entry, bot.getPosition(), ai);
        return true;
    }
}
