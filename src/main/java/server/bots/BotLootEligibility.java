package server.bots;

import client.Character;
import client.inventory.Inventory;
import client.inventory.InventoryType;
import constants.inventory.ItemConstants;
import server.bots.pq.BotPqHooks;
import server.maps.MapItem;
import server.maps.MapleMap;

public final class BotLootEligibility {
    public static final int KPQ_COUPON = 4001007;
    public static final int KPQ_PASS = 4001008;
    public static final int HPQ_RICE_CAKE = 4001101;
    public static final long MIN_TARGET_LOOT_AGE_MS = 3_000L;
    public static final long BOT_INVENTORY_DROP_TARGET_LOOT_AGE_MS = 15_000L;

    private BotLootEligibility() {
    }

    public static boolean isPresent(MapleMap map, MapItem drop) {
        return map != null
                && drop != null
                && !drop.isPickedUp()
                && map.getMapObject(drop.getObjectId()) == drop;
    }

    public static boolean canBotLoot(BotEntry entry, Character bot, MapItem drop) {
        if (entry == null || bot == null || drop == null || !drop.canBePickedBy(bot)) {
            return false;
        }

        if (BotPinkBeanRun.inInstance(bot) && BotPinkBeanRun.crew(bot)!=null) {
            // The invisible native stage item belongs to the reactor, never a loot scan.
            if (drop.getItemId()==4001193 || !drop.isPlayerDrop()) return false;
        }
        if (BotHorntailRun.inInstance(bot) && BotHorntailRun.belongsTo(bot, entry.owner == null ? -1 : entry.owner.getId())) {
            if (bot.getMapId() >= 240060000 && !drop.isPlayerDrop()) return false;
            if (drop.getItemId() >= 4001087 && drop.getItemId() <= 4001093) return false;
        }
        if (BotMvpqRun.inInstance(bot) && BotMvpqRun.belongsTo(bot, entry.owner == null ? -1 : entry.owner.getId())) {
            if (drop.getItemId() == BotMvpqRun.LETTER || bot.getMapId() == BotMvpqRun.BONUS) return false;
            if (bot.getMapId() == BotMvpqRun.BOSS && !drop.isPlayerDrop()) return false;
        }
        // Dedicated GPQ work items are picked up only by the assigned stage driver.
        // This also keeps placed offerings and the leader's Rubian off passive-loot lists.
        if (BotGuildCompanions.inInstance(bot) && BotGuildCompanions.belongsTo(bot, entry.owner == null ? -1 : entry.owner.getId())) {
            int id = drop.getItemId();
            if (id == 1032033 || id >= 4001024 && id <= 4001037) return false;
            if (bot.getMapId() == 990001000 || bot.getMapId() == 990001101) return false;
            if (bot.getMapId() == 990000900 && !drop.isPlayerDrop()) return false;
        }
        // Leave Lounge lift and On the Way Up route markers for the player.
        if ((bot.getMapId() == 920010700 || bot.getMapId() == 920010600) && BotOpqRun.inInstance(bot) && drop.getMeso() == 10 && drop.isPlayerDrop()) return false;
        int itemId = drop.getItemId();
        if (itemId == 4001163 && drop.isPlayerDrop() && BotEllinRun.inInstance(bot)
                && entry.owner != null && BotEllinRun.belongsTo(bot, entry.owner.getId())) return false;
        if (BotEllinRun.inInstance(bot) && (itemId == 4001161 || itemId == 4001162 || itemId == 4001163 || itemId == 4001164 || itemId == 4001169)) return BotEllinRun.canLoot(bot, itemId);
        // Human-led LPQ bonus loot belongs entirely to the player; companions only open boxes.
        if (bot.getMapId() == 922011000 && BotLpqHuman.leader(bot) != null) return false;
        // Ratz drops feed stage-seven reactors after five seconds; taking one prevents a Rombard summon.
        if (itemId == 4001156 && bot.getMapId() == 922010700 && BotLpqRun.inInstance(bot)) return false;
        if (BotMagatiaRun.inInstance(bot) && itemId >= 4001130 && itemId <= 4001135)
            return BotMagatiaRun.canLoot(bot, itemId);
        if (BotZakumBattle.reservesLoot(bot) && !(drop.isPlayerDrop()
                && drop.getDropper() instanceof Character player && !(player.getClient() instanceof client.BotClient))) return false;
        if (BotZakumBattle.inInstance(bot) && itemId == 4001017) return false;
        if (itemId == BotLmpqRun.COUPON && BotLmpqRun.inInstance(bot)) return BotLmpqRun.canLoot(bot);
        if (BotOpqRun.inInstance(bot) && (itemId >= 4001044 && itemId <= 4001063 || itemId == 4001074))
            return BotOpqRun.canLoot(entry, bot, itemId);
        if ((itemId == BotLpqRun.PASS || itemId == 4001023) && BotLpqRun.inInstance(bot)) return BotLpqHuman.canCollect(bot);
        if (itemId == KPQ_PASS) {
            return BotKpqRun.isLeader(bot);
        }
        if (itemId == HPQ_RICE_CAKE) {
            return false;
        }
        if (itemId == KPQ_COUPON && BotKpqRun.inInstance(bot)) {
            return BotKpqRun.wantsCoupons(entry, bot);
        }
        if (itemId == KPQ_COUPON && (BotPqHooks.shouldSkipCouponLoot(entry)
                || (entry.kpq.couponTarget > 0 && bot.getItemQuantity(KPQ_COUPON, false) >= entry.kpq.couponTarget))) {
            return false;
        }
        if (itemId == BotZakumPrequestManager.ITEM_KEY || itemId == BotZakumPrequestManager.ITEM_FIRE_ORE
                || itemId == BotZakumPrequestManager.ITEM_PQ_DOCUMENT) {
            // Zakum PQ items are picked up explicitly by role in BotZakumPqRun (SSOT for both the
            // autonomous errand and player-led runs); passive loot taking them would vacuum the
            // leader's delivered key pile or the 7-stack pending under the Giant Chest.
            return false;
        }
        if (BotPqBossLoot.reserves(entry, bot, drop)) return false;
        if (itemId > 0 && !bot.needQuestItem(drop.getQuest(), itemId)) {
            return false;
        }
        if (drop.getMeso() <= 0 && itemId > 0) {
            InventoryType type = ItemConstants.getInventoryType(itemId);
            Inventory inv = bot.getInventory(type);
            return inv == null || !inv.isFull();
        }
        return true;
    }

    public static boolean canBotTargetLoot(BotEntry entry, Character bot, MapleMap map, MapItem drop, long now) {
        return isPresent(map, drop)
                && canBotLoot(entry, bot, drop)
                && now - drop.getDropTime() >= requiredTargetLootAgeMs(bot, drop);
    }

    static long requiredTargetLootAgeMs(Character bot, MapItem drop) {
        if (bot == null || drop == null) {
            return MIN_TARGET_LOOT_AGE_MS;
        }
        if (isBotInventoryDrop(drop)) {
            return BOT_INVENTORY_DROP_TARGET_LOOT_AGE_MS;
        }
        return MIN_TARGET_LOOT_AGE_MS;
    }

    private static boolean isBotInventoryDrop(MapItem drop) {
        int ownerId = drop.getOwnerId();
        return drop.isPlayerDrop()
                && ownerId > 0
                && BotManager.getInstance().getActiveOwnerByBotCharId(ownerId) != null;
    }
}
