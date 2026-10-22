package server.bots;

import client.BotClient;
import client.Character;
import client.Job;
import client.inventory.InventoryType;
import client.inventory.WeaponType;
import client.inventory.manipulator.InventoryManipulator;
import server.ItemInformationProvider;
import java.util.List;

/** Shared assembly/retry resupply; only the human's registered Zakum companions qualify. */
public final class BotZakumSupplies {
    private BotZakumSupplies() {}

    public static void refresh(Character leader) {
        if (leader.getClient() instanceof BotClient || leader.getEventInstance() != null
                || leader.getMapId() != BotZakumBattle.RECRUIT)
            throw new IllegalArgumentException("Restock at Adobis before entering Zakum");
        if (leader.getParty() == null) return;
        var crew = new java.util.ArrayList<BotEntry>();
        for (var member : leader.getWorldServer().getPlayerStorage().getAllCharacters()) {
            var entry = BotManager.getInstance().getEntryByBotCharId(member.getId());
            if (entry != null && BotZakumBattle.belongsTo(entry.bot, leader.getId())) crew.add(entry);
        }
        if (crew.isEmpty()) return;
        if (leader.getParty().getLeaderId() != leader.getId())
            throw new IllegalArgumentException("Only the party leader can prepare a retry");
        for (var entry : crew) {
            if (entry.bot.getMapId() != leader.getMapId() || entry.bot.getEventInstance() != null)
                throw new IllegalStateException("Bring the companions back to Adobis before retrying");
            restock(entry, entry.bot);
        }
    }

    static void restock(BotEntry entry, Character bot) {
        if (!(bot.getClient() instanceof BotClient) || bot.getEventInstance() != null)
            throw new IllegalArgumentException("Restock dedicated companions outside the fight");
        for (var item : List.copyOf(bot.getInventory(InventoryType.USE).list())) {
            if (item.getItemId() / 10000 == 207 && (item.getQuantity() == 0 || item.getItemId() == 2070015))
                InventoryManipulator.removeFromSlot(bot.getClient(), InventoryType.USE, item.getPosition(), item.getQuantity(), false);
        }
        BotZakumCompanions.provisionSkillConsumables(entry, bot);
        if (bot.getJob() == Job.NIGHTLORD) stock(bot, 2070006, 12000); // handbook: Ilbi
        WeaponType weapon = BotAttackExecutionProvider.getEquippedWeaponType(bot);
        if (weapon == WeaponType.BOW) stock(bot, 2060000, 12000);
        if (weapon == WeaponType.CROSSBOW) stock(bot, 2061000, 12000);
        stock(bot, 4001017, 5);
        stock(bot, 2050004, 100);
        stock(bot, 2002023, 600);
    }

    static void stock(Character bot, int item, int target) {
        int needed = Math.max(0, target - bot.getItemQuantity(item, false));
        if (needed == 0) return;
        int stack = ItemInformationProvider.getInstance().getSlotMax(bot.getClient(), item);
        if (stack <= 0) throw new IllegalStateException("Invalid stack size for Zakum supply " + item);
        while (needed > 0) {
            int amount = Math.min(needed, stack);
            if (!InventoryManipulator.addById(bot.getClient(), item, (short)amount))
                throw new IllegalStateException("No room for Zakum supply " + item);
            needed -= amount;
        }
    }
}
