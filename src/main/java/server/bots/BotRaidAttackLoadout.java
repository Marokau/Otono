package server.bots;

import client.Character;
import client.inventory.Equip;
import client.inventory.InventoryType;

/** Retain a finalized physical raid set once it satisfies both damage and HP goals. */
final class BotRaidAttackLoadout {
    private BotRaidAttackLoadout() {}
    static boolean ready(Character b) {
        if (!(b.getClient() instanceof client.BotClient) || b.isGM()
                || BotEquipManager.isMageJob(b.getJob())) return false;
        var worn=b.getInventory(InventoryType.EQUIPPED);
        return attack(worn.getItem((short)-8),1082223,16)
                && attack(worn.getItem((short)-9),1102206,10)
                && attack(worn.getItem((short)-7),1072344,7)
                && BotRaidHpScrolls.unbuffedHp(b)>=BotRaidHp.minimumUnbuffed(b);
    }
    private static boolean attack(client.inventory.Item item,int id,int minimum) {
        return item instanceof Equip equip && equip.getItemId()==id && equip.getWatk()>=minimum;
    }
}
