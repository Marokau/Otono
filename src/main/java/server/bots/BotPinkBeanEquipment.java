package server.bots;
import client.Character;
import client.Job;
import client.inventory.*;
import client.inventory.manipulator.InventoryManipulator;
import server.ItemInformationProvider;
import java.util.*;

/** Owner-authorized ordinary drop rolls; no HP washing, direct stat writes or forced godly gates. */
final class BotPinkBeanEquipment {
    static final int BUFFED_HP=12000;
    private static final Map<Character,Integer> GOALS=Collections.synchronizedMap(new WeakHashMap<>());
    private BotPinkBeanEquipment() {}
    static int hpGoal(Character b){
        // Register the Pink ranged floor before ordinary provisioning scores gear.
        // Otherwise a returning fixture's full bag can reject a needless fresh
        // DPS sidegrade before its already-owned HP set is restored.
        int pink=BotPinkBeanReadiness.rangedGoal(b);
        return GOALS.getOrDefault(b,pink>0?Math.max(0,pink-b.getMaxHp()):0);
    }
    static void prepare(Character b){
        if(!(b.getClient() instanceof client.BotClient)||b.getEventInstance()!=null||b.isGM())throw new IllegalArgumentException("Prepare dedicated companions outside their event");
        int unbuffedGoal=BotRaidHp.minimumUnbuffed(b);
        if(unbuffedGoal==0)return;
        int needed=Math.max(0,unbuffedGoal-b.getMaxHp());GOALS.put(b,needed);
        var ii=ItemInformationProvider.getInstance();int wornHp=0;
        for(var it:b.getInventory(InventoryType.EQUIPPED).list())if(it instanceof Equip equip)wornHp+=equip.getHp();
        if(wornHp>=needed)return;
        var ids=new LinkedHashSet<Integer>();var hpBySlot=new HashMap<String,Equip>();var levelBySlot=new HashMap<String,Equip>();
        for(var it:b.getInventory(InventoryType.EQUIPPED).list())if(it instanceof Equip equip&&!ii.isCash(equip.getItemId()))ids.add(equip.getItemId());
        try(var db=tools.DatabaseConnection.getConnection();var q=db.prepareStatement("SELECT itemid FROM drop_data WHERE itemid >= 1000000 AND itemid < 1500000 UNION SELECT itemid FROM shopitems WHERE itemid >= 1000000 AND itemid < 1500000");var rows=q.executeQuery()){
            while(rows.next()){
                int id=rows.getInt(1);if(ii.isCash(id)||ii.getEquipLevelReq(id)<10||ii.getEquipLevelReq(id)>b.getLevel()||Set.of(1002357,1002390,1002430,1002858,1003016).contains(id))continue;
                if(!(ii.getEquipById(id) instanceof Equip equip))continue;
                boolean wearable=ii.meetsEquipRequirements(equip,b.getJob(),b.getLevel(),b.getTotalStr(),b.getTotalDex(),b.getTotalInt(),b.getTotalLuk(),b.getFame());
                // Normal secondary-stat AP can unlock better mage gear; excluding it
                // before the existing AP planner runs traps a pure-INT mage in low-level gear.
                if(!wearable&&b.getJob()==Job.BISHOP)
                    wearable=ii.meetsEquipRequirements(equip,b.getJob(),b.getLevel(),b.getTotalStr(),b.getTotalDex(),b.getTotalInt(),
                            b.getTotalLuk()+Math.max(0,b.getInt()-4)+b.getRemainingAp(),b.getFame());
                if(!wearable)continue;
                String slot=ii.getEquipmentSlot(id);if(slot==null)continue;
                if(id>=1300000&&!BotEquipManager.isPreferredWeapon(b,ii.getWeaponType(id),equip))continue;
                var hp=hpBySlot.get(slot);
                int bonus=Math.max((int)(ii.getEquipLevelReq(id)*config.YamlConfig.config.server.GODLY_STATS_HPMP_SCALING),config.YamlConfig.config.server.GODLY_STATS_MIN_HPMP_BONUS);
                int oldBonus=hp==null?0:Math.max((int)(ii.getEquipLevelReq(hp.getItemId())*config.YamlConfig.config.server.GODLY_STATS_HPMP_SCALING),config.YamlConfig.config.server.GODLY_STATS_MIN_HPMP_BONUS);
                if(hp==null||equip.getHp()+2*bonus>hp.getHp()+2*oldBonus)hpBySlot.put(slot,equip);
                var level=levelBySlot.get(slot);if(level==null||ii.getEquipLevelReq(id)>ii.getEquipLevelReq(level.getItemId()))levelBySlot.put(slot,equip);
            }
        }catch(java.sql.SQLException error){throw new IllegalStateException("Could not read raid HP gear goals",error);}
        for(var equip:hpBySlot.values())ids.add(equip.getItemId());for(var equip:levelBySlot.values())ids.add(equip.getItemId());
        // Native fourth-job reward (quest 29903): these level-180 Adventurers
        // already meet its level/job requirements. The drop/shop catalog omits it.
        if(b.getLevel()>=120)ids.add(1142110);
        var extras=new ArrayList<Equip>();
        for(int id:ids){
            int copies=id/10000==111&&!ii.isPickupRestricted(id)?4:1;
            for(int copyIndex=0;copyIndex<copies;copyIndex++){
            Equip best=null;
            // Bounded preparation samples the same configured gate and stat rolls as drops.
            // Keep a real result; never assign a bonus or reroll a granted item in place.
            for(int attempt=0;attempt<400;attempt++){
                var rolled=ii.randomizeStats((Equip)ii.getEquipById(id));
                if(best==null||rolled.getHp()>best.getHp())best=rolled;
            }
            if(best!=null)extras.add(best);
            }
        }
        var plan=BotEquipManager.runOptimizerWithExtras(b,extras);
        // Pay secondary requirements before wearing mutually dependent rolled pieces.
        // A feasible final DEX set need not have a legal first item to equip.
        var entry=BotManager.getInstance().getEntryByBotCharId(b.getId());
        var build=BotBuildManager.resolveApBuild(entry,b);
        String[] keys={"reqSTR","reqDEX","reqINT","reqLUK"};
        int secondary=build.secondaryTarget;
        for(var equip:extras)secondary=Math.max(secondary,ii.getEquipStats(equip.getItemId()).getOrDefault(keys[build.secondaryStat.ordinal()],0));
        entry.apBuild=new BotBuildManager.ApBuild(build.primaryStat,build.secondaryStat,secondary);
        BotBuildManager.respecAp(entry,b);
        // Respec/provisioning can temporarily unseat an owned HP set. Try it
        // after paying its secondary requirements before inserting fresh gear.
        for(int pass=0;pass<4;pass++)if(!BotEquipManager.autoEquip(b,b,null,true))break;
        if(BotRaidHpScrolls.unbuffedHp(b)>=unbuffedGoal)return;
        plan=BotEquipManager.runOptimizerWithExtras(b,extras);
        var chosen=new LinkedHashSet<>(plan.picks().values());if(plan.weapon()!=null)chosen.add(plan.weapon());
        for(var equip:chosen){
            if(equip.getPosition()<0)continue;
            // Existing inventory pieces already have positive positions; new rolls use position zero.
            if(equip.getPosition()>0&&b.getInventory(InventoryType.EQUIP).getItem(equip.getPosition())==equip)continue;
            var copy=equip.copy();copy.setPosition((short)0);
            if(!InventoryManipulator.addFromDrop(b.getClient(),copy,false))throw new IllegalStateException("No room for rolled raid gear");
        }
        for(int pass=0;pass<4;pass++)if(!BotEquipManager.autoEquip(b,b,null,true))break;
        BotRaidHpScrolls.prepare(b);
    }
}
