package server.bots;

import client.BotClient;
import client.Character;
import client.inventory.InventoryType;
import server.maps.MapItem;

import java.awt.Point;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** KPQ roles use ordinary NPC dialogue, inventory drops, pickups, combat, and portals. */
public final class BotKpqRun {
    public static final int FIRST_MAP = 103000800;
    public static final int LAST_MAP = 103000805;
    private static final int CLOTO = 9020001;
    private static final int COUPON = 4001007;
    private static final int PASS = 4001008;

    private BotKpqRun() {}

    /** Explicit opt-in only: never recruits from the autonomous population. */
    public static void startCrew(List<Character> crew) {
        if (crew.size() != 4) throw new IllegalArgumentException("KPQ test crew requires four members");
        var manager = BotManager.getInstance();
        var ids = crew.stream().map(Character::getId).toList();
        if (ids.stream().distinct().count() != 4) throw new IllegalArgumentException("Duplicate crew member");
        for (Character member : crew) {
            if (!(member.getClient() instanceof BotClient) || member.isGM()
                    || member.getLevel() < 21 || member.getLevel() > 30
                    || member.getEventInstance() != null || manager.getEntryByBotCharId(member.getId()) == null
                    || member.getClient().getChannel() != crew.getFirst().getClient().getChannel()
                    || member.getWorld() != crew.getFirst().getWorld())
                throw new IllegalArgumentException("Ineligible crew member: " + member.getName());
            if (member.getParty() != null && member.getParty().getMembers().stream().anyMatch(p -> !ids.contains(p.getId())))
                throw new IllegalArgumentException("Crew member already has other party members");
        }
        for (Character member : crew) {
            var entry = manager.getEntryByBotCharId(member.getId());
            manager.resumeFromOperatorCommand(entry);
            manager.issueStop(entry);
            BotPqProvisioner.provision(member);
            entry.kpq.recruiting = true;
            entry.kpq.leaderId = crew.getFirst().getId();
            entry.kpq.crewIds = ids;
            entry.kpq.waitUntilMs = 0;
            entry.kpq.state = 0;
            if (member != crew.getFirst()) manager.partyUp(crew.getFirst(), member);
        }
    }

    public static boolean inInstance(Character bot) {
        return bot != null && bot.getMapId() >= FIRST_MAP && bot.getMapId() <= LAST_MAP
                && bot.getEventInstance() != null;
    }

    public static boolean isLeader(Character bot) {
        return inInstance(bot) && bot.getEventInstance().isEventLeader(bot);
    }

    public static boolean wantsCoupons(BotEntry entry, Character bot) {
        return bot.getMapId() == FIRST_MAP && !isLeader(bot)
                && entry.kpq.couponTarget > 0
                && bot.getItemQuantity(COUPON, false) < entry.kpq.couponTarget
                && bot.getItemQuantity(PASS, false) == 0 && entry.kpq.state != 7;
    }

    public static boolean tick(BotEntry entry, Character bot, boolean runAiTick) {
        if (entry.kpq.humanLed) {
            var human = bot.getWorldServer().getPlayerStorage().getCharacterById(entry.kpq.leaderId);
            if (human == null || bot.getParty() == null || human.getParty() == null
                    || human.getParty().getId() != bot.getParty().getId()
                    || human.getParty().getLeaderId() != human.getId()) {
                BotKpqCompanions.dismiss(bot);
                return true;
            }
            if (!inInstance(bot)) {
                entry.kpq.mapId = -1;
                if (bot.getMapId() == 103000890) {
                    if (approachNpc(entry, bot, 9020002, runAiTick)) talk(entry, bot, 9020002);
                    return true;
                }
                return hold(entry, bot, runAiTick);
            }
            if (bot.getMapId() == LAST_MAP && human.getMapId() != LAST_MAP
                    && human.getMapId() != LAST_MAP - 1) {
                if (approachNpc(entry, bot, 9020002, runAiTick)) talk(entry, bot, 9020002);
                return true;
            }
        }
        if (!inInstance(bot)) {
            entry.kpq.mapId = -1;
            if (entry.kpq.recruiting) return tickRecruiting(entry, bot, runAiTick);
            return false;
        }
        entry.kpq.recruiting = false;
        var manager = BotManager.getInstance();
        var state = entry.kpq;
        if (state.mapId != bot.getMapId()) {
            manager.issueStop(entry);
            state.mapId = bot.getMapId();
            state.state = 0;
            state.couponTarget = -1;
            state.waitUntilMs = 0;
        }
        var event = bot.getEventInstance();
        Character leader = event.getPlayers().stream().filter(event::isEventLeader).findFirst().orElse(null);
        if (leader == null) return hold(entry, bot, runAiTick);
        int stage = bot.getMapId() - FIRST_MAP + 1;
        BotPqDialogue.tick(entry, bot, leader, stage);
        if (stage <= 4 && event.getProperty(stage + "stageclear") != null) {
            if (entry.kpq.humanLed && leader.getMapId() == bot.getMapId())
                return hold(entry, bot, runAiTick);
            BotTravelManager.tickTravel(entry, bot, bot.getMapId() + 1, 2, runAiTick, false);
            return true;
        }
        if (stage >= 2 && stage <= 4) return BotKpqPuzzles.tick(entry, bot, leader, stage, runAiTick);
        if (stage == 5) {
            // Unlike a respawning grind spot, this stage requires sweeping the whole map.
            // Monsters are ordinary map objects sent to clients; keep local combat's range
            // unchanged and navigate toward a remaining enemy when none is locally targetable.
            if (event.getProperty("5stageclear") == null && nearestDrop(bot, PASS) == null
                    && bot.getItemQuantity(PASS, false) < 10
                    && BotCombatManager.findGrindTarget(entry, bot) == null) {
                server.life.Monster remaining = null;
                double closest = Double.MAX_VALUE;
                for (var monster : bot.getMap().getAllMonsters()) {
                    double distance = bot.getPosition().distanceSq(monster.getPosition());
                    if (monster.isAlive() && distance < closest) {
                        remaining = monster;
                        closest = distance;
                    }
                }
                if (remaining != null) {
                    walk(entry, remaining.getPosition(), runAiTick);
                    return true;
                }
            }
            if (event.getProperty("5stageclear") != null) {
                if (approachNpc(entry, bot, runAiTick)) talk(entry, bot);
            } else if (isLeader(bot)) {
                if (bot.getItemQuantity(PASS, false) >= 10) {
                    if (approachNpc(entry, bot, runAiTick)) talk(entry, bot);
                } else {
                    MapItem pass = nearestDrop(bot, PASS);
                    if (pass != null) collect(entry, bot, pass, runAiTick);
                    else {
                        BotTravelManager.clearMoveTargetPin(entry);
                        manager.tickPqGrinding(entry, bot, runAiTick);
                    }
                }
            } else {
                BotTravelManager.clearMoveTargetPin(entry);
                manager.tickPqGrinding(entry, bot, runAiTick);
            }
            return true;
        }
        if (stage == 6) {
            state.stage5Claimed = true; // The NPC reward path moved this character to bonus.
            return hold(entry, bot, runAiTick);
        }
        if (stage != 1) return hold(entry, bot, runAiTick);

        if (isLeader(bot)) {
            state.state = 7;
            if (bot.getItemQuantity(PASS, false) >= event.getPlayerCount() - 1) {
                if (approachNpc(entry, bot, runAiTick)) talk(entry, bot);
                return true;
            }
            MapItem pass = nearestDrop(bot, PASS);
            if (pass != null) return collect(entry, bot, pass, runAiTick);
            approachNpc(entry, bot, runAiTick);
            return true;
        }

        if (bot.getItemQuantity(PASS, false) > 0) {
            state.state = 6;
            if (!near(bot, leader.getPosition(), 30)) walk(entry, leader.getPosition(), runAiTick);
            else if (System.currentTimeMillis() >= state.waitUntilMs) {
                if (manager.issueDropItem(entry, InventoryType.ETC, PASS, (short) 1))
                    BotPqDialogue.handedPass(entry, bot, leader);
                state.waitUntilMs = System.currentTimeMillis() + 1500;
            }
            return true;
        }
        if (state.state == 6 || state.state == 7) {
            state.state = 7;
            // A finished worker still helps the remaining workers earn their coupons.
            BotTravelManager.clearMoveTargetPin(entry);
            manager.tickPqGrinding(entry, bot, runAiTick);
            return true;
        }
        if (state.couponTarget < 0) {
            state.state = 1;
            if (approachNpc(entry, bot, runAiTick) && talk(entry, bot)) {
                var message = ((BotClient) bot.getClient()).npcMessage;
                state.couponTarget = couponAnswer(message.text());
                if (state.couponTarget > 0)
                    BotChatManager.queueBotSay(entry, "I need " + state.couponTarget + " coupons.");
            }
            return true;
        }
        int have = bot.getItemQuantity(COUPON, false);
        BotPqDialogue.progress(entry, bot, have, state.couponTarget);
        if (have >= state.couponTarget) {
            state.state = 4;
            if (have > state.couponTarget) {
                manager.issueDropItem(entry, InventoryType.ETC, COUPON, (short) (have - state.couponTarget));
            } else if (approachNpc(entry, bot, runAiTick)) talk(entry, bot);
            return true;
        }
        state.state = 3;
        MapItem coupon = nearestDrop(bot, COUPON);
        if (coupon != null) return collect(entry, bot, coupon, runAiTick);
        BotTravelManager.clearMoveTargetPin(entry);
        manager.tickPqGrinding(entry, bot, runAiTick);
        return true;
    }

    // Knowledge of the quiz wording, not its random grid index or an inventory exchange.
    static int couponAnswer(String question) {
        if (question.contains("minimum level") && question.contains("warrior")) return 10;
        if (question.contains("STR") && question.contains("warrior")) return 35;
        if (question.contains("INT") && question.contains("magician")) return 20;
        if (question.contains("DEX") && (question.contains("bowman") || question.contains("thief"))) return 25;
        if (question.contains("2nd job")) return 30;
        if (question.contains("minimum level") && question.contains("magician")) return 8;
        return -1;
    }

    private static boolean talk(BotEntry entry, Character bot) {
        return talk(entry, bot, CLOTO);
    }

    private static boolean talk(BotEntry entry, Character bot, int npc) {
        long now = System.currentTimeMillis();
        if (now < entry.kpq.waitUntilMs) return false;
        entry.kpq.waitUntilMs = now + ThreadLocalRandom.current().nextLong(1000, 1600);
        if (bot.getClient().getCM() != null) BotNpcInteraction.next(bot, 0);
        else return BotNpcInteraction.talk(bot, npc);
        return false;
    }

    private static boolean approachNpc(BotEntry entry, Character bot, boolean runAiTick) {
        return approachNpc(entry, bot, CLOTO, runAiTick);
    }

    private static boolean approachNpc(BotEntry entry, Character bot, int npcId, boolean runAiTick) {
        var result = BotTravelManager.tickApproachNpc(entry, bot, bot.getMapId(), npcId,
                2, runAiTick, false, 300);
        if (result != BotTravelManager.ApproachStatus.ARRIVED) return false;
        hold(entry, bot, runAiTick);
        return true;
    }

    private static boolean tickRecruiting(BotEntry entry, Character bot, boolean runAiTick) {
        if (bot.getLevel() < 21 || bot.getLevel() > 30) {
            entry.kpq.recruiting = false;
            BotChatManager.queueBotSay(entry, "I am outside the KPQ level range now.");
            return hold(entry, bot, runAiTick);
        }
        if (bot.getMapId() == 103000890) {
            if (approachNpc(entry, bot, 9020002, runAiTick)) talk(entry, bot, 9020002);
            return true;
        }
        // Use the existing shop's valuation, sales, purchase budget, and actual inventory.
        if (entry.shopVisitPending) {
            boolean consumed = BotShopManager.tickShopVisit(entry, bot);
            if (!consumed && entry.shopApproachDelayMs <= 0 && entry.shopTargetPos != null)
                BotTravelManager.movementStep.step(entry, entry.shopTargetPos, runAiTick);
            return true;
        }
        int[] pots = BotPotionManager.countPotions(bot);
        if (pots[0] < BotManager.cfg.POT_LOW_WARN || pots[1] < BotManager.cfg.POT_LOW_WARN
                || BotShopManager.isOutOfUsableAmmo(bot) || BotShopManager.needsPreferredWeaponForCurrentJob(bot)) {
            entry.kpq.state = -1; // preparing, never submit an under-supplied crew
            Integer shopMap = BotShopManager.findNearestShopMap(bot, false);
            if (shopMap == null) return hold(entry, bot, runAiTick);
            if (bot.getMapId() != shopMap) {
                BotTravelManager.tickTravel(entry, bot, shopMap, BotAutopilotManager.MAX_TRAVEL_HOPS, runAiTick, false);
            } else {
                BotShopManager.requestSellTrashVisit(entry, bot);
                if (!entry.shopVisitPending) BotShopManager.onMapChange(entry, bot);
            }
            return true;
        }
        entry.kpq.state = 0;
        if (bot.getMapId() != 103000000) {
            BotTravelManager.tickTravel(entry, bot, 103000000, BotAutopilotManager.MAX_TRAVEL_HOPS, runAiTick, false);
            return true;
        }
        if (!approachNpc(entry, bot, 9020000, runAiTick)) return true;
        if (bot.getId() != entry.kpq.leaderId) return true;
        boolean ready = entry.kpq.crewIds.stream().allMatch(id -> {
            var member = bot.getWorldServer().getPlayerStorage().getCharacterById(id);
            var memberEntry = BotManager.getInstance().getEntryByBotCharId(id);
            return member != null && member.getMapId() == 103000000 && member.getHp() > 0
                    && memberEntry != null && memberEntry.kpq.recruiting && memberEntry.kpq.state == 0;
        });
        if (ready) talk(entry, bot, 9020000);
        return true;
    }

    private static MapItem nearestDrop(Character bot, int item) {
        return bot.getMap().getDroppedItems().stream()
                .filter(d -> !d.isPickedUp() && d.getItem() != null && d.getItem().getItemId() == item
                        && d.canBePickedBy(bot) && System.currentTimeMillis() - d.getDropTime() >= 400)
                .min(Comparator.comparingDouble(d -> bot.getPosition().distanceSq(d.getPosition())))
                .orElse(null);
    }

    private static boolean collect(BotEntry entry, Character bot, MapItem drop, boolean runAiTick) {
        if (near(bot, drop.getPosition(), 30)) {
            bot.pickupItem(drop);
            hold(entry, bot, runAiTick);
        } else walk(entry, drop.getPosition(), runAiTick);
        return true;
    }

    private static void walk(BotEntry entry, Point target, boolean runAiTick) {
        BotTravelManager.pinMoveTarget(entry, target);
        BotTravelManager.movementStep.step(entry, target, runAiTick);
    }

    private static boolean hold(BotEntry entry, Character bot, boolean runAiTick) {
        BotTravelManager.clearMoveTargetPin(entry);
        BotManager.getInstance().stepMovementCore(entry, bot.getPosition(), runAiTick);
        return true;
    }

    private static boolean near(Character bot, Point target, int distance) {
        return Math.abs(bot.getPosition().x - target.x) <= distance
                && Math.abs(bot.getPosition().y - target.y) <= distance;
    }
}
