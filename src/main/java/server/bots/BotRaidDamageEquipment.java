package server.bots;

import client.Character;
import client.inventory.*;
import client.inventory.manipulator.InventoryManipulator;
import server.ItemInformationProvider;
import java.util.*;

/** Dedicated raid goals from native v83 items; the shared optimizer retains the HP constraint. */
final class BotRaidDamageEquipment {
    private BotRaidDamageEquipment() {}
    static void prepare(Character b) {
        if (!(b.getClient() instanceof client.BotClient) || b.isGM() || b.getEventInstance()!=null)
            throw new IllegalArgumentException("Prepare raid damage gear outside the event");
        if (BotRaidAttackLoadout.ready(b)) return;
        var ii=ItemInformationProvider.getInstance();
        int weapon=switch(b.getJob()) {
            case NIGHTLORD -> 1472068;
            case BOWMASTER -> 1452057;
            case DARKKNIGHT -> 1432047;
            case HERO -> 1402046;
            default -> 0;
        };
        var goals=new ArrayList<Integer>();
        if(weapon!=0)goals.add(weapon);
        goals.add(1002357); // Native Zakum helmet, appropriate for these level-180 raiders.
        if(!BotEquipManager.isMageJob(b.getJob()))goals.addAll(List.of(1082223,1102041,1072344));
        var owned=new HashSet<Integer>();
        for(var type:List.of(InventoryType.EQUIP,InventoryType.EQUIPPED))
            for(var item:b.getInventory(type).list())owned.add(item.getItemId());
        var extras=new ArrayList<Equip>();
        for(int id:goals) {
            // A saved goal is reused, including its real scroll failures. Re-entry is not a reroll.
            if(owned.contains(id))continue;
            Equip hp=null,attack=null;
            for(int roll=0;roll<400;roll++) {
                var item=ii.randomizeStats((Equip)ii.getEquipById(id));
                if(hp==null||item.getHp()>hp.getHp())hp=item;
                if(attack==null||item.getWatk()>attack.getWatk()
                        ||item.getWatk()==attack.getWatk()&&item.getHp()>attack.getHp())attack=item;
            }
            if(hp!=null)extras.add(hp);
            if(attack!=null&&attack!=hp)extras.add(attack);
        }
        var entry=BotManager.getInstance().getEntryByBotCharId(b.getId());
        BotHorntailEquipment.prepareAp(entry,b);
        if(!BotEquipManager.isMageJob(b.getJob())) {
            var weapons=new ArrayList<Equip>();
            for(var type:List.of(InventoryType.EQUIP,InventoryType.EQUIPPED))
                for(var item:b.getInventory(type).list())if(item instanceof Equip equip&&equip.getItemId()>=1300000)weapons.add(equip);
            for(var equip:extras)if(equip.getItemId()>=1300000)weapons.add(equip);
            entry.apBuild=BotHorntailEquipment.buildForWeapons(entry,b,weapons);
            BotBuildManager.respecAp(entry,b);
        }
        var plan=BotEquipManager.runOptimizerWithExtras(b,extras);
        var selected=new LinkedHashSet<>(plan.picks().values());
        if(plan.weapon()!=null)selected.add(plan.weapon());
        for(var item:selected)if(extras.contains(item)) {
            if(!InventoryManipulator.addFromDrop(b.getClient(),item,false))
                throw new IllegalStateException("No room for raid damage goal");
        }
        for(int pass=0;pass<4;pass++)if(!BotEquipManager.autoEquip(b,b,null,true))break;
        // Reclaim secondary AP now that the selected set supplies its own DEX/STR.
        if(!BotEquipManager.isMageJob(b.getJob())) {
            BotHorntailEquipment.prepareAp(entry,b);
            for(int pass=0;pass<4;pass++)if(!BotEquipManager.autoEquip(b,b,null,true))break;
        }
        if(BotEquipManager.isMageJob(b.getJob())) {
            // A staff's LUK requirement is a TOTAL requirement, not a base-AP target.
            var build=BotBuildManager.resolveApBuild(entry,b);
            int gearLuk=b.getTotalLuk()-b.getLuk(),target=build.secondaryTarget;
            for(var item:b.getInventory(InventoryType.EQUIPPED).list())if(item instanceof Equip equip)
                target=Math.max(target,ii.getEquipStats(equip.getItemId()).getOrDefault("reqLUK",0)-gearLuk+equip.getLuk());
            entry.apBuild=new BotBuildManager.ApBuild(build.primaryStat,build.secondaryStat,target);
            BotBuildManager.respecAp(entry,b);
        }
        if(BotRaidHpScrolls.unbuffedHp(b)<BotRaidHp.minimumUnbuffed(b)) {
            BotPinkBeanEquipment.prepare(b);
            BotRaidHpScrolls.prepare(b);
        }
        BotPinkBeanWeapons.prepare(b); // Scroll the final worn set, after any HP restoration.
        if(BotRaidHpScrolls.unbuffedHp(b)<BotRaidHp.minimumUnbuffed(b))
            throw new IllegalStateException("Damage equipment lost the raid HP floor for "+b.getName()+": "+BotRaidHpScrolls.unbuffedHp(b));
    }
    static int attackConsumable(client.Job job) {
        return BotEquipManager.isMageJob(job)?2022179:2022121;
    }
    static void supplies(Character b) {
        if(!(b.getClient() instanceof client.BotClient)||b.isGM())throw new IllegalArgumentException("Dedicated bot supplies only");
        int chosen=attackConsumable(b.getJob()),other=chosen==2022179?2022121:2022179;
        for(var item:List.copyOf(b.getInventory(InventoryType.USE).list()))
            if(item.getItemId()==other)InventoryManipulator.removeFromSlot(b.getClient(),InventoryType.USE,item.getPosition(),item.getQuantity(),false);
        BotZakumSupplies.stock(b,chosen,20);
        // Scrolling/optimization is finished: retain the worn set and all USE/ETC supplies.
        // Dedicated fixtures have no reason to carry the rejected gear candidates into a raid.
        for(var item:List.copyOf(b.getInventory(InventoryType.EQUIP).list()))
            InventoryManipulator.removeFromSlot(b.getClient(),InventoryType.EQUIP,item.getPosition(),item.getQuantity(),false);
    }
}
