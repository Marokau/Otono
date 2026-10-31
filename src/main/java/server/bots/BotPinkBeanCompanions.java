package server.bots;

import client.Character;
import client.Job;
import net.server.world.Party;
import tools.DatabaseConnection;
import java.util.*;

/** Dedicated thirty-person Pink Bean crews, separate from the autonomous population. */
public final class BotPinkBeanCompanions {
    static final List<Job> JOBS=BotPinkBeanRoster.JOBS;
    private BotPinkBeanCompanions() {}
    public static synchronized String assemble(Character leader){return assemble(leader,false);}
    static synchronized String assembleTest(Character leader){return assemble(leader,true);}
    private static String assemble(Character leader,boolean test){
        if(leader.getMapId()!=BotPinkBeanRun.RECRUIT||leader.getEventInstance()!=null)return "Assemble at the Forgotten Temple Manager outside the expedition.";
        if(leader.getLevel()<120)return "Pink Bean expedition entry requires level 120 or higher.";
        if(!test&&leader.getItemQuantity(4032002,false)<=0
                &&leader.getQuest(server.quest.Quest.getInstance(3522)).getStatus()!=client.QuestStatus.Status.COMPLETED)
            return "Complete the Temple Keeper's prerequisites and bring your Marble of Chaos. Human prerequisites still apply.";
        var old=BotPinkBeanRun.crew(leader);
        if(old!=null){
            if(old.bots.size()!=BotPinkBeanRoster.JOBS.size())return "Dismiss your previous crew before assembling the thirty-person raid.";
            if(old.bots.stream().anyMatch(b->b.getEventInstance()!=null||b.getMap()!=leader.getMap()))return "Wait for your whole crew to return before preparing another attempt.";
            for(var b:old.bots)prepare(b);
            old.notices.clear();old.clearedAt=0;old.rewardPickups=0;
            return ready();
        }
        if(leader.getParty()!=null&&(leader.getParty().getLeaderId()!=leader.getId()||leader.getParty().getMembers().size()>1))return "Leave your current party before assembling your dedicated Pink Bean crew.";
        var bots=new ArrayList<Character>();
        try{
            for(var job:BotPinkBeanRoster.JOBS){
                String prefix=switch(job){case BISHOP->"PinkBishop";case DARKKNIGHT->"PinkSpear";case BOWMASTER->"PinkArcher";case NIGHTLORD->"PinkThief";default->"PinkSword";};
                int id=BotPqCompanionPool.acquire(leader,"pinkbean",job,prefix,BotPinkBeanRun.RECRUIT);
                var result=BotManager.getInstance().spawnPqCompanion(leader,id);
                if(!result.success())throw new IllegalStateException(result.errorMessage());
                var b=result.bot();bots.add(b);
                BotManager.getInstance().applyOperatorCommand(BotManager.getInstance().getEntryByBotCharId(id),BotEntry.OperatorCmd.IDLE,-1,0);
                prepare(b);
                if(b.getMapId()!=leader.getMapId())b.changeMap(leader.getMapId(),0);
            }
            BotPinkBeanRun.start(leader,bots,!test);return ready();
        }catch(RuntimeException error){
            org.slf4j.LoggerFactory.getLogger(BotPinkBeanCompanions.class).warn("Pink Bean crew preparation failed",error);
            for(var b:bots)dismiss(b);
            return "I could not prepare the whole crew. Please try again.";
        }
    }
    private static String ready(){return "Your twenty-nine Pink Bean companions are ready in five parties of six, with a Bishop and Dark Knight in each. Create the expedition normally; we register ourselves. Start when all thirty are registered. We will explain the statue waves and explain the final reactor activation. Boss loot is yours. Human Temple prerequisites remain unchanged.";}
    static int createFixture(String name,Job job,int world,int channel){
        int id=BotZakumCrewFactory.create(name,job,world,channel);
        try(var db=DatabaseConnection.getConnection();var q=db.prepareStatement("UPDATE characters SET map=?,spawnpoint=0 WHERE id=?")){
            q.setInt(1,BotPinkBeanRun.RECRUIT);q.setInt(2,id);q.executeUpdate();return id;
        }catch(java.sql.SQLException error){throw new IllegalStateException(error);}
    }
    static void prepare(Character b){
        BotPinkBeanReadiness.mark(b);
        BotHorntailCompanions.prepareRaid(b,8820001);
        for(int attempt=0;attempt<5;attempt++){
            BotPinkBeanEquipment.prepare(b);int required=BotPinkBeanEquipment.hpGoal(b);
            if(required==0)break;
            int worn=0;for(var item:b.getInventory(client.inventory.InventoryType.EQUIPPED).list())if(item instanceof client.inventory.Equip eq)worn+=eq.getHp();
            if(worn>=required)break;
            if(attempt==4)throw new IllegalStateException("HP equipment goal not reached for "+b.getName());
        }
        BotRaidDamageEquipment.prepare(b);
        BotZakumSupplies.stock(b,2002023,2000); // Reserve for the full native expedition.
        BotZakumSupplies.stock(b,2050004,500); // Full-run native ailment recovery reserve.
        BotRaidDamageEquipment.supplies(b);
        BotZakumSupplies.stock(b,4032002,1); // Dedicated companion-only Temple prerequisite.
    }
    static void prepareStars(Character b){
        for(var item:List.copyOf(b.getInventory(client.inventory.InventoryType.USE).list()))
            if(item.getItemId()/10000==207&&item.getItemId()!=2070018)
                client.inventory.manipulator.InventoryManipulator.removeFromSlot(b.getClient(),client.inventory.InventoryType.USE,item.getPosition(),item.getQuantity(),false);
        BotZakumSupplies.stock(b,2070018,12000); // WZ: Balanced Fury, level 70, 30 attack.
    }
    public static synchronized String release(Character leader){
        var c=BotPinkBeanRun.crew(leader);if(c==null)return "You have no active Pink Bean crew.";
        if(leader.getEventInstance()!=null||c.bots.stream().anyMatch(b->b.getEventInstance()!=null))return "Leave the expedition and wait for your companions before dismissing.";
        for(var b:List.copyOf(c.bots))dismiss(b);
        return "Your Pink Bean companions have been saved and logged off.";
    }
    static void dismiss(Character b){
        var m=BotManager.getInstance();var e=m.getEntryByBotCharId(b.getId());if(e==null)return;
        BotPinkBeanRun.unregister(b);m.applyOperatorCommand(e,BotEntry.OperatorCmd.IDLE,-1,0);
        if(b.getParty()!=null)Party.leaveParty(b.getParty(),b.getClient());
        b.saveCharToDB(true);b.getClient().disconnect(false,false);
    }
}
