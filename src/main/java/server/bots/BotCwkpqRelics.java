package server.bots;

import client.Character;
import client.inventory.InventoryType;
import scripting.event.EventInstanceManager;
import server.maps.Reactor;

import java.awt.Point;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Native class-room and boss objectives after the sigil stages. */
final class BotCwkpqRelics {
    private static final int STATUE = 610030500;
    private static final int BOSSES = 610030600;
    private static final int REWARD_HALL = 610030700;
    private static final int BONUS = 610030800;
    private record Room(int map, String entrance, int npc, int weapon, int offering) {}

    private static final Map<Integer, Room> ROOMS = Map.of(
            1, new Room(610030510, "in01", 9201107, 4001259, 6109005),
            2, new Room(610030521, "in03", 9201109, 4001257, 6109007),
            3, new Room(610030540, "in02", 9201108, 4001258, 6109006),
            4, new Room(610030530, "in04", 9201110, 4001256, 6109008),
            5, new Room(610030550, "in05", 0, 4001260, 6109009)
    );
    private static final Map<Character, Long> NEXT = new ConcurrentHashMap<>();

    private BotCwkpqRelics() {}

    static void unregister(Character bot) {
        NEXT.remove(bot);
    }

    static boolean tick(BotEntry entry, Character bot, BotCwkpqStages.State state,
                        List<Character> crew, Character leader, EventInstanceManager event, boolean ai) {
        int map = bot.getMapId();
        var room = ROOMS.get(bot.getJob().getJobNiche());
        if (room == null) return BotOpqRun.hold(entry, bot, ai);
        if (map == STATUE) {
            announce(entry, state, map,
                    "Stage Five: Each class will retrieve a weapon from its chamber and offer it to the statue. Take the middle portal after all five are returned.");
            if (event.getIntProperty("glpq5") >= 5) return BotMagatiaRun.portal(entry, bot, "next00", ai);
            return statue(entry, bot, room, crew, leader, event, ai);
        }
        if (map == room.map) {
            bot.triggerAutopotAfterStatLoss(-1, -1);
            if (BotCwkpqProtection.cast(entry, bot, ai)) return BotOpqRun.hold(entry, bot, ai);
            return chamber(entry, bot, room, event, ai);
        }
        if (map == BOSSES) return bosses(entry, bot, state, crew, leader, event, ai);
        if (map == REWARD_HALL) {
            announce(entry, state, map, "The Twisted Masters are defeated. The bonus portal is on the right. The treasure is yours.");
            if (leader.getMapId() == BONUS) return BotMagatiaRun.portal(entry, bot, "next00", ai);
        }
        if (map == BONUS) {
            if (leader.getEventInstance() != event) return BotMagatiaRun.portal(entry, bot, "out00", ai);
            var box = bot.getMap().getReactorsByIdRange(6102002, 6102005).stream()
                    .filter(r -> r.isActive() && r.getState() < 5)
                    .min(Comparator.comparingDouble(r -> r.getPosition().distanceSq(bot.getPosition())))
                    .orElse(null);
            if (box != null) return smash(entry, bot, box, ai);
        }
        return BotOpqRun.hold(entry, bot, ai);
    }

    private static boolean statue(BotEntry entry, Character bot, Room room, List<Character> crew,
                                   Character leader, EventInstanceManager event, boolean ai) {
        var altar = bot.getMap().getReactorsByIdRange(room.offering, room.offering).stream().findFirst().orElse(null);
        if (altar == null || altar.getState() != 0) return BotOpqRun.hold(entry, bot, ai);
        if (bot.getItemQuantity(room.weapon, false) > 0) {
            if (!altar.getArea().contains(bot.getPosition()) || entry.climbing || entry.inAir) {
                var area = altar.getArea();
                int center = area.x + area.width / 2;
                Point landing = null;
                for (int offset = 0; offset <= area.width / 2 && landing == null; offset += 5) {
                    for (int sign : new int[]{1, -1}) {
                        var floor = bot.getMap().getPointBelow(new Point(center + sign * offset, area.y));
                        if (floor != null && area.contains(floor)) {
                            landing = floor;
                            break;
                        }
                    }
                }
                if (landing != null) {
                    BotZakumPqRun.walkTo(entry, bot, landing, ai);
                    return true;
                }
                return BotOpqRun.hold(entry, bot, ai);
            }
            if (ai && ready(bot))
                BotManager.getInstance().issueDropItem(entry, InventoryType.ETC, room.weapon, (short) 1);
            return BotOpqRun.hold(entry, bot, ai);
        }
        if (leader.getItemQuantity(room.weapon, false) > 0
                || crew.stream().anyMatch(b -> b.getItemQuantity(room.weapon, false) > 0))
            return BotOpqRun.hold(entry, bot, ai);
        int family = bot.getJob().getJobNiche();
        if ((event.getIntProperty("glpq5_room") & (1 << family)) != 0)
            return BotOpqRun.hold(entry, bot, ai);
        var assigned = crew.stream().filter(b -> b.isAlive() && b.getJob().getJobNiche() == family)
                .findFirst().orElse(null);
        // A nearly vertical foothold on the mage portal approach must be jumped,
        // even though the navigation graph groups it with the adjacent slopes.
        Point position = bot.getPosition();
        if (family == 2 && ai && !entry.inAir && !entry.climbing
                && position.x == 1003 && Math.abs(position.y + 268) <= 3) {
            BotMovementManager.clearNavigationState(entry);
            BotMovementManager.initiateJump(entry, bot, -1);
            return true;
        }
        if (family <= 3 || bot == assigned) return BotMagatiaRun.portal(entry, bot, room.entrance, ai);
        return BotOpqRun.hold(entry, bot, ai);
    }

    private static boolean chamber(BotEntry entry, Character bot, Room room,
                                    EventInstanceManager event, boolean ai) {
        int family = bot.getJob().getJobNiche();
        if (bot.getItemQuantity(room.weapon, false) > 0
                || (event.getIntProperty("glpq5_room") & (1 << family)) != 0)
            return BotMagatiaRun.portal(entry, bot, "out00", ai);
        if (bot.getMap().getDroppedItems().stream().anyMatch(d -> !d.isPickedUp() && d.getItemId() == room.weapon)) {
            var drop = bot.getMap().getDroppedItems().stream()
                    .filter(d -> !d.isPickedUp() && d.getItemId() == room.weapon).findFirst().orElse(null);
            if (drop != null) BotZakumPqRun.stepTowardsAndPickUp(entry, bot, drop, ai);
            return true;
        }
        if (room.map == 610030530) {
            var eye = nearestReactor(bot, 6108004, 1);
            if (eye != null) return smash(entry, bot, eye, ai);
        } else if (room.map == 610030550) {
            var chest = nearestReactor(bot, 6102001, 4);
            if (chest != null) return smash(entry, bot, chest, ai);
        }
        if (room.map != 610030530
                && bot.getMap().getAllMonsters().stream().anyMatch(m -> m.isAlive() && !m.isFake()))
            return combat(entry, bot, event, ai);
        if (room.npc != 0) {
            // Lockewood's displayed origin is 121 px above his own foothold.
            int talkRange = room.map == 610030540 ? 130 : 120;
            var approach = BotTravelManager.tickApproachNpc(entry, bot, room.map, room.npc, 3, ai, false, talkRange);
            if (approach == BotTravelManager.ApproachStatus.ARRIVED && ai && ready(bot)
                    && bot.getClient().getCM() == null) {
                synchronized (event) {
                    BotNpcInteraction.talk(bot, room.npc);
                }
            }
            return true;
        }
        return BotOpqRun.hold(entry, bot, ai);
    }

    private static boolean bosses(BotEntry entry, Character bot, BotCwkpqStages.State state,
                                   List<Character> crew, Character leader, EventInstanceManager event, boolean ai) {
        announce(entry, state, BOSSES,
                "Stage Six: Leader, speak to the Battle Statue on the far right to begin. Speak to it again after the guardians and after the Twisted Masters. Boss loot is yours.");
        int phase = event.getIntProperty("glpq6");
        if (phase >= 2 && bot.getMap().countMonsters() == 0 && leader.getMapId() == BOSSES) {
            // Distinguish the completion hint from this map's entry briefing.
            announce(entry, state, -BOSSES,
                    "The Twisted Masters are defeated! Speak to the Battle Statue on the far right to confirm the clear. "
                            + "Then take the far-right door and continue to the bonus room. Collect your boss loot before leaving.");
        }
        if (phase < 3 && BotCwkpqProtection.cast(entry, bot, ai))
            return BotOpqRun.hold(entry, bot, ai);
        if (phase == 3) {
            if (leader.getMapId() == REWARD_HALL || leader.getMapId() == BONUS)
                return BotMagatiaRun.portal(entry, bot, "next00", ai);
            return BotOpqRun.hold(entry, bot, ai);
        }
        if (phase > 0 && bot.getMap().getAllMonsters().stream().anyMatch(m -> m.isAlive() && !m.isFake()))
            return combat(entry, bot, event, ai);
        return BotOpqRun.hold(entry, bot, ai);
    }

    private static boolean combat(BotEntry entry, Character bot, EventInstanceManager event, boolean ai) {
        bot.triggerAutopotAfterStatLoss(-1, -1);
        if (BotLpqCures.tryCure(bot) || BotHorntailRevive.tick(entry, bot, event.getPlayers(), ai)
                || BotCombatManager.tickSupportHealing(entry, bot))
            return BotOpqRun.hold(entry, bot, ai);
        return bot.getMapId() == BOSSES
                ? BotZakumCombat.tick(entry, bot, ai)
                : BotCwkpqChamberCombat.tick(entry, bot, ai);
    }

    private static boolean ready(Character bot) {
        long now = System.currentTimeMillis();
        if (now < NEXT.getOrDefault(bot, 0L)) return false;
        NEXT.put(bot, now + 1400);
        return true;
    }

    private static Reactor nearestReactor(Character bot, int id, int terminalState) {
        return bot.getMap().getReactorsByIdRange(id, id).stream()
                .filter(r -> r.isActive() && r.getState() < terminalState)
                .min(Comparator.comparingDouble(r -> r.getPosition().distanceSq(bot.getPosition())))
                .orElse(null);
    }

    private static boolean smash(BotEntry entry, Character bot, Reactor reactor, boolean ai) {
        Point target = bot.getMap().getPointBelow(reactor.getPosition());
        if (target == null) return BotOpqRun.hold(entry, bot, ai);
        Point position = bot.getPosition();
        if (reactor.getId() == 6108004 && Math.abs(position.x - target.x) <= 65 && !entry.climbing) {
            if (ai && BotOpqReactorAttack.hit(entry, bot, reactor)) return BotOpqRun.hold(entry, bot, ai);
            if (entry.inAir) return BotOpqRun.hold(entry, bot, ai);
            if (ai && Math.abs(position.y - target.y) <= 10 && entry.attackCooldownMs <= 0) {
                BotPhysicsEngine.beginGroundJump(entry, bot, 0);
                return BotOpqRun.hold(entry, bot, ai);
            }
        }
        if (entry.inAir || entry.climbing || Math.abs(position.x - target.x) > 65
                || Math.abs(position.y - target.y) > 45) {
            BotZakumPqRun.walkTo(entry, bot, target, ai);
            return true;
        }
        if (ai) BotOpqReactorAttack.hit(entry, bot, reactor);
        return BotOpqRun.hold(entry, bot, ai);
    }

    private static void announce(BotEntry entry, BotCwkpqStages.State state, int map, String text) {
        if (state.announced.add(map)) BotPqChat.queue(entry, text, false);
    }
}
