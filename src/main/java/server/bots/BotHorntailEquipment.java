package server.bots;

import client.Character;
import client.Stat;
import client.inventory.Equip;
import client.inventory.InventoryType;
import client.inventory.WeaponType;
import client.processor.stat.AssignAPProcessor;
import constants.skills.Bowmaster;
import server.ItemInformationProvider;
import tools.DatabaseConnection;
import java.util.ArrayList;
import java.util.List;

/** Companion-only AP planning over the same ordinary drop/shop catalog as PQ provisioning. */
final class BotHorntailEquipment {
    private BotHorntailEquipment() {}
    static int attackCycle(Character bot, int weaponId) {
        if (bot.getName() == null || !(bot.getName().startsWith("Horn") || bot.getName().startsWith("Pink"))) return BotEquipManager.weaponCycleMs(weaponId);
        var entry = BotManager.getInstance().getEntryByBotCharId(bot.getId());
        if (entry != null && BotHorntailRaidBuild.VARIANT.equals(entry.spVariant)
                && bot.getSkillLevel(Bowmaster.HURRICANE) > 0
                && ItemInformationProvider.getInstance().getWeaponType(weaponId) == WeaponType.BOW)
            return BotHurricane.SHOT_MS;
        return BotEquipManager.weaponCycleMs(weaponId);
    }
    static void prepareAp(BotEntry entry, Character bot) {
        if (BotEquipManager.isMageJob(bot.getJob())) return;
        var ii = ItemInformationProvider.getInstance();
        var weapons = new ArrayList<Equip>();
        try (var db = DatabaseConnection.getConnection(); var query = db.prepareStatement(
                "SELECT itemid FROM drop_data WHERE itemid >= 1300000 AND itemid < 1500000 UNION SELECT itemid FROM shopitems WHERE itemid >= 1300000 AND itemid < 1500000"); var rows = query.executeQuery()) {
            while (rows.next()) {
                int id = rows.getInt(1);
                if (ii.isCash(id) || ii.getEquipLevelReq(id) < 10 || ii.getEquipLevelReq(id) > bot.getLevel()) continue;
                if (ii.getEquipById(id) instanceof Equip equip
                        && BotEquipManager.isPreferredWeapon(bot, ii.getWeaponType(id), equip)) weapons.add(equip);
            }
        } catch (java.sql.SQLException failure) { throw new IllegalStateException("Could not load Horntail weapon goals", failure); }
        // Include the actual scrolled weapon, rather than scoring only unscrolled catalog bases.
        if(bot.getInventory(InventoryType.EQUIPPED).getItem((short)-11) instanceof Equip worn)weapons.add(worn);
        entry.apBuild = buildForWeapons(entry, bot, weapons);
        BotBuildManager.respecAp(entry, bot);
    }
    static BotBuildManager.ApBuild buildForWeapons(BotEntry entry, Character bot, List<Equip> weapons) {
        var current = BotBuildManager.resolveApBuild(entry, bot);
        if (current == null || BotEquipManager.isMageJob(bot.getJob())) return current;
        var ii = ItemInformationProvider.getInstance();
        int[] base = {bot.getStr(), bot.getDex(), bot.getInt(), bot.getLuk()};
        int[] gear = {bot.getTotalStr()-base[0], bot.getTotalDex()-base[1], bot.getTotalInt()-base[2], bot.getTotalLuk()-base[3]};
        Stat[] stats = {Stat.STR, Stat.DEX, Stat.INT, Stat.LUK};
        int[] floors = new int[4];
        int budget = bot.getRemainingAp();
        for (int n=0;n<4;n++) { floors[n]=AssignAPProcessor.getMinStatFloor(bot.getJob(),stats[n]); budget+=Math.max(0,base[n]-floors[n]); }
        int primary=current.primaryStat.ordinal(), secondary=current.secondaryStat.ordinal();
        String[] keys={"reqSTR","reqDEX","reqINT","reqLUK"};
        var worn = (Equip)bot.getInventory(InventoryType.EQUIPPED).getItem((short)-11);
        // The weapon must be wearable without borrowing its own secondary stats.
        if(worn!=null)gear[secondary]-=switch(secondary){case 0->worn.getStr();case 1->worn.getDex();case 2->worn.getInt();default->worn.getLuk();};
        // HP armor must remain independently wearable after the AP refund too.
        int armorFloor=current.secondaryTarget;
        for(var item:bot.getInventory(InventoryType.EQUIPPED).list())if(item instanceof Equip equip&&equip!=worn) {
            int own=switch(secondary){case 0->equip.getStr();case 1->equip.getDex();case 2->equip.getInt();default->equip.getLuk();};
            armorFloor=Math.max(armorFloor,ii.getEquipStats(equip.getItemId()).getOrDefault(keys[secondary],0)-gear[secondary]+own);
        }
        current=new BotBuildManager.ApBuild(current.primaryStat,current.secondaryStat,armorFloor);
        int withoutWeapon=bot.getTotalWatk()-(worn==null?0:worn.getWatk());
        var choices=new ArrayList<BotEquipManager.WeaponCand>();
        for (var weapon:weapons) {
            int id=weapon.getItemId();
            if (!BotEquipManager.isPreferredWeapon(bot,ii.getWeaponType(id),weapon)) continue;
            var requirements=ii.getEquipStats(id);
            int need=Math.max(current.secondaryTarget,requirements.getOrDefault(keys[secondary],0)-gear[secondary]);
            int cost=need-floors[secondary];
            if(cost>budget)continue;
            int[] projected={floors[0]+gear[0],floors[1]+gear[1],floors[2]+gear[2],floors[3]+gear[3]};
            projected[primary]=floors[primary]+budget-cost+gear[primary];projected[secondary]=need+gear[secondary];
            if(!ii.meetsEquipRequirements(weapon,bot.getJob(),bot.getLevel(),projected[0],projected[1],projected[2],projected[3],bot.getFame()))continue;
            choices.add(new BotEquipManager.WeaponCand(requirements.getOrDefault(keys[secondary],0),withoutWeapon+weapon.getWatk(),ii.getWeaponType(id),attackCycle(bot,id)));
        }
        char[] codes={'s','d','i','l'};
        int equippedRequirement=worn==null?0:ii.getEquipStats(worn.getItemId()).getOrDefault(keys[secondary],0);
        int target=BotEquipManager.chooseSecondaryTarget(bot.getJob(),codes[primary],codes[secondary],current.secondaryTarget,floors,base,gear,bot.getRemainingAp(),choices,equippedRequirement);
        return new BotBuildManager.ApBuild(current.primaryStat,current.secondaryStat,target);
    }
}
