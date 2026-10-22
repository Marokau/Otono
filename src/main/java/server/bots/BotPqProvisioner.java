package server.bots;

import client.Character;
import client.inventory.Equip;
import client.inventory.InventoryType;
import client.inventory.manipulator.InventoryManipulator;
import server.ItemInformationProvider;
import tools.DatabaseConnection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** Explicit PQ-crew provisioning authorized by the owner; never runs for population bots by default. */
public final class BotPqProvisioner {
    private static final int GINGER_ALE = 2002023; // handbook/Use.txt, 75% HP + MP

    private BotPqProvisioner() {}



    public static List<String> provision(Character bot) {
        if (!(bot.getClient() instanceof client.BotClient) || bot.getEventInstance() != null)
            throw new IllegalArgumentException("Provision only a headless crew member outside its PQ");
        BotPqEquipmentPolicy.register(bot);
        // Ellin concentrate is not event-exclusive: remove leftovers before every attempt.
        if (bot.getName().startsWith("EPq")) {
            for (int item : new int[]{4001161, 4001162}) {
                int count = bot.getItemQuantity(item, false);
                if (count > 0) InventoryManipulator.removeById(bot.getClient(), InventoryType.ETC, item, count, false, false);
            }
        }
        var ii = ItemInformationProvider.getInstance();
        // These PQ fixtures use ordinary headgear rather than special boss/event rewards.
        if (bot.getName().startsWith("OPq") || bot.getName().startsWith("LMPq") || bot.getName().startsWith("CPq2") || bot.getName().startsWith("MPq")) {
            if (bot.getName().startsWith("OPq") && bot.getMeso() < 180) bot.gainMeso(180 - bot.getMeso(), false, true, false); // Sixteen ascent rows plus two Lounge lift rows.
            for (var type : List.of(InventoryType.EQUIPPED, InventoryType.EQUIP)) {
                for (var item : new ArrayList<>(bot.getInventory(type).list())) {
                    if (java.util.Set.of(1002357, 1002390, 1002430, 1002858, 1003016).contains(item.getItemId()))
                        InventoryManipulator.removeFromSlot(bot.getClient(), type, item.getPosition(), (short) 1, false);
                }
            }
        }

        var firstJob = client.Job.getById(bot.getJob().getId() / 100 * 100);
        List<Equip> candidates = new ArrayList<>();
        // The ordinary gear-farming catalog, not cash/GM or unobtainable catalog equipment.
        try (var connection = DatabaseConnection.getConnection();
             var statement = connection.prepareStatement("SELECT itemid FROM drop_data WHERE itemid >= 1000000 AND itemid < 1500000 UNION SELECT itemid FROM shopitems WHERE itemid >= 1000000 AND itemid < 1500000");
             var rows = statement.executeQuery()) {
            while (rows.next()) {
                int id = rows.getInt(1);
                if ((bot.getName().startsWith("OPq") || bot.getName().startsWith("LMPq") || bot.getName().startsWith("CPq2") || bot.getName().startsWith("MPq")) && java.util.Set.of(1002357, 1002390, 1002430, 1002858, 1003016).contains(id)) continue;
                if (ii.isCash(id) || ii.getEquipLevelReq(id) < 10 || ii.getEquipLevelReq(id) > bot.getLevel()) continue;
                if (ii.getEquipById(id) instanceof Equip equip
                        && BotScrollManager.levelsUntilWearable(bot, ii, equip, 0) == 0) candidates.add(equip);
            }
        } catch (SQLException e) { throw new IllegalStateException("Could not load normal gear goals", e); }
        // Some first-job weapons are scripted starter rewards, absent from mob/shop tables.
        for (var grant : BotStarterKitManager.starterKitFor(firstJob)) {
            if (grant.itemId() < 1300000 || grant.itemId() >= 1500000) continue;
            if (ii.getEquipById(grant.itemId()) instanceof Equip equip
                    && BotScrollManager.levelsUntilWearable(bot, ii, equip, 0) == 0) candidates.add(equip);
        }
        var optimized = BotEquipManager.runOptimizerWithExtras(bot, candidates);
        var selected = new LinkedHashSet<>(optimized.picks().values());
        if (optimized.weapon() != null) selected.add(optimized.weapon());
        List<String> granted = new ArrayList<>();
        for (Equip equip : selected) {
            if (bot.getItemQuantity(equip.getItemId(), true) > 0) continue;
            if (!InventoryManipulator.addById(bot.getClient(), equip.getItemId(), (short) 1))
                throw new IllegalStateException("No room for PQ gear: " + equip.getItemId());
            granted.add(ii.getName(equip.getItemId()));
        }
        BotEquipManager.autoEquip(bot, bot, null, true);
        // Ensure MPQ's replacement hat is worn now through the ordinary equipment handler.
        if (bot.getName().startsWith("MPq")) {
            Equip hat = optimized.picks().get((short) -1);
            if (hat == null) {
                for (Equip candidate : candidates) {
                    if (candidate.getItemId() / 10000 != 100) continue;
                    if (hat == null || ii.getEquipLevelReq(candidate.getItemId()) > ii.getEquipLevelReq(hat.getItemId())) hat = candidate;
                }
            }
            if (hat == null) throw new IllegalStateException("No normal MPQ headgear available");
            if (bot.getItemQuantity(hat.getItemId(), true) == 0
                    && !InventoryManipulator.addById(bot.getClient(), hat.getItemId(), (short) 1))
                throw new IllegalStateException("No room for MPQ headgear");
            for (var item : new ArrayList<>(bot.getInventory(InventoryType.EQUIP).list())) {
                if (item.getItemId() == hat.getItemId()) {
                    InventoryManipulator.equip(bot.getClient(), item.getPosition(), (short) -1);
                    break;
                }
            }
        }
        int needed = Math.max(0, 200 - bot.getItemQuantity(GINGER_ALE, false));
        if (needed > 0 && !InventoryManipulator.addById(bot.getClient(), GINGER_ALE, (short) needed))
            throw new IllegalStateException("No room for PQ potions");
        // Reuse the existing first-job ammunition kit. Weapons still come from the optimizer above.
        for (var grant : BotStarterKitManager.starterKitFor(firstJob)) {
            int category = grant.itemId() / 10000;
            if (category != 206 && category != 207 && category != 233) continue;
            int quantity = Math.max(0, 5000 - bot.getItemQuantity(grant.itemId(), false));
            // Rechargeable ammunition needs separate legal stacks; addById does not split it.
            int stackMax = ii.getSlotMax(bot.getClient(), grant.itemId());
            while (quantity > 0) {
                int stack = Math.min(quantity, stackMax);
                if (!InventoryManipulator.addById(bot.getClient(), grant.itemId(), (short) stack))
                    throw new IllegalStateException("No room for PQ ammunition");
                quantity -= stack;
            }
        }
        if (BotAttackExecutionProvider.getEquippedWeaponType(bot) == client.inventory.WeaponType.CROSSBOW) {
            int bolts = Math.max(0, 5000 - bot.getItemQuantity(2061000, false)); // handbook/Use.txt
            if (bolts > 0 && !InventoryManipulator.addById(bot.getClient(), 2061000, (short) bolts))
                throw new IllegalStateException("No room for crossbow bolts");
        }
        if (bot.getName().startsWith("MPq")) {
            if (bot.getMeso() < 10) bot.gainMeso(10 - bot.getMeso(), false, true, false); // One stage-six entrance marker.
            int rocks = Math.max(0, 200 - bot.getItemQuantity(4006001, false)); // The Summoning Rock, handbook/Etc.txt.
            if (rocks > 0 && !InventoryManipulator.addById(bot.getClient(), 4006001, (short) rocks))
                throw new IllegalStateException("No room for MPQ Summoning Rocks");
        }
        granted.add("200 Ginger Ales ready");
        return granted;
    }
}
