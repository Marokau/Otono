package server.bots;

import client.BotClient;
import client.BuffStat;
import client.Character;
import client.Job;
import net.server.world.Party;
import tools.DatabaseConnection;
import java.util.*;

/** Opt-in level-180 Horntail fixtures; prerequisite awards never target humans. */
public final class BotHorntailCompanions {
    static final List<Job> JOBS=List.of(Job.DARKKNIGHT,Job.BISHOP,Job.BOWMASTER,Job.NIGHTLORD,Job.HERO,
            Job.BISHOP,Job.DARKKNIGHT,Job.BOWMASTER,Job.NIGHTLORD,Job.HERO,Job.NIGHTLORD);
    private BotHorntailCompanions() {}
    static boolean caveAccess(Character b) {
        return b.getBuffSource(BuffStat.MORPH)==2210003 || b.getItemQuantity(4001086,false)>0;
    }
    public static synchronized String assemble(Character leader) { return assemble(leader,false); }
    static synchronized String assembleTest(Character leader) { return assemble(leader,true); }
    private static String assemble(Character leader,boolean test) {
        int map=leader.getMapId();
        if(leader.getEventInstance()!=null || map!=BotHorntailRun.TRIAL_RECRUIT&&map!=BotHorntailRun.RAID_RECRUIT)
            return "Assemble outside the trial or expedition, at the normal cave NPC.";
        if(leader.getLevel()<(map==BotHorntailRun.TRIAL_RECRUIT?120:100))return "The trial requires level 120; the expedition requires level 100.";
        var existing=BotHorntailRun.crew(leader);
        if(!test&&!caveAccess(leader)&&(existing==null||existing.trial==null||!existing.trial.isEventCleared()))
            return "Complete Moira's transformation medicine and use it for cave access, or bring your Certificate of the Dragon Squad. Human prerequisites still apply.";
        if(existing!=null){
            if(existing.bots.stream().anyMatch(b->b.getEventInstance()!=null||b.getMapId()!=map))return "Wait for the whole crew to return before assembling again.";
            if(map==BotHorntailRun.RAID_RECRUIT&&existing.bots.size()==5) {
                var added=new ArrayList<Character>();
                try {
                    for(int i=5;i<JOBS.size();i++) added.add(acquire(leader,JOBS.get(i)));
                    var combined=new ArrayList<>(existing.bots);combined.addAll(added);
                    BotHorntailRun.start(leader,combined,!test);
                    BotHorntailRun.crew(combined.get(0)).trial=existing.trial;
                }catch(RuntimeException failure){for(var b:added)dismiss(b);return "I could not expand the complete raid crew. Your trial companions remain; please try again.";}
            }
            for(var b:existing.bots)prepare(b);
            return ready(map);
        }
        if(leader.getParty()!=null&&(leader.getParty().getLeaderId()!=leader.getId()||leader.getParty().getMembers().size()>1))return "Leave your current party before assembling the dedicated Horntail crew.";
        var bots=new ArrayList<Character>();
        try {
            int count=map==BotHorntailRun.TRIAL_RECRUIT?5:11;
            for(int i=0;i<count;i++)bots.add(acquire(leader,JOBS.get(i)));
            BotHorntailRun.start(leader,bots,!test);
            return ready(map);
        } catch(RuntimeException x) {
            org.slf4j.LoggerFactory.getLogger(BotHorntailCompanions.class).warn("Horntail crew assembly failed",x);
            for(var b:bots)dismiss(b);
            return "I could not prepare the complete Horntail crew. Please dismiss and try again.";
        }
    }
    private static String ready(int map) {
        return map==BotHorntailRun.TRIAL_RECRUIT
            ? "Your five Horntail companions are ready. You lead the six-person trial through the normal cave NPC. We will explain the keys and wait for your NPC checks. After clearing, assemble again at the expedition NPC to expand to twelve participants."
            : "Your eleven Horntail companions are ready in two parties of six, each with a Bishop and Dark Knight. You lead the expedition. Create it normally; we register ourselves. Start when all twelve are registered. Wait for protection buffs before approaching each head or breaking the main chamber crystal. Boss loot is yours.";
    }
    static int createFixture(String name,Job job,int world,int channel) {
        int id=BotZakumCrewFactory.create(name,job,world,channel);
        try(var db=DatabaseConnection.getConnection();var q=db.prepareStatement("UPDATE characters SET map=?,spawnpoint=0 WHERE id=?")) {
            q.setInt(1,BotHorntailRun.RAID_RECRUIT);q.setInt(2,id);q.executeUpdate();return id;
        }catch(java.sql.SQLException x){throw new IllegalStateException(x);}
    }
    static Character acquire(Character leader,Job job) {
        String prefix=switch(job){case BISHOP->"HornBishop";case DARKKNIGHT->"HornSpear";case BOWMASTER->"HornArcher";case NIGHTLORD->"HornThief";default->"HornSword";};
        int id=BotPqCompanionPool.acquire(leader,"horntail",job,prefix,leader.getMapId());
        var result=BotManager.getInstance().spawnPqCompanion(leader,id);
        if(!result.success())throw new IllegalStateException(result.errorMessage());
        var b=result.bot();
        try {
            BotManager.getInstance().applyOperatorCommand(BotManager.getInstance().getEntryByBotCharId(id),BotEntry.OperatorCmd.IDLE,-1,0);
            prepare(b);
            if(b.getMapId()!=leader.getMapId())b.changeMap(leader.getMapId(),0);
            return b;
        }catch(RuntimeException failure){dismiss(b);throw failure;}
    }
    static void prepare(Character b) {
        prepareRaid(b,8810003);
        for(int attempt=0;attempt<5;attempt++){
            BotPinkBeanEquipment.prepare(b);int required=BotPinkBeanEquipment.hpGoal(b);
            if(required==0)break;
            int worn=0;for(var item:b.getInventory(client.inventory.InventoryType.EQUIPPED).list())if(item instanceof client.inventory.Equip eq)worn+=eq.getHp();
            if(worn>=required)break;
            if(attempt==4&&b.getMaxHp()+worn<BotRaidHp.minimumUnbuffed(b))
                throw new IllegalStateException("Unbuffed raid HP minimum not reached for "+b.getName());
        }
        BotRaidDamageEquipment.prepare(b);
        BotRaidDamageEquipment.supplies(b);
        BotZakumSupplies.stock(b,4001086,1); // Authorized companion-only cave prerequisite.
    }
    static void prepareRaid(Character b,int referenceId) {
        if(!(b.getClient() instanceof BotClient)||b.isGM()||b.getEventInstance()!=null)
            throw new IllegalArgumentException("Prepare dedicated headless companions outside their event");
        BotRaidCooldowns.prepare(b);
        var e=BotManager.getInstance().getEntryByBotCharId(b.getId());
        e.spVariant=BotHorntailRaidBuild.VARIANT;
        while(b.getLevel()<180)b.levelUp(false);
        b.cancelEffectFromBuffStat(BuffStat.MAPLE_WARRIOR); // Budget permanent stats before normal rebuffing.
        var reference=server.life.LifeFactory.getMonster(referenceId);
        e.aspirationalMobLevel=reference.getLevel();e.aspirationalMobAvoid=reference.getAvoidability();
        e.apBuild=BotBuildManager.resolveApBuild(e,b);
        if(!BotRaidAttackLoadout.ready(b))BotBuildManager.respecAp(e,b);
        // Bahamut's cap comes from its native five-quest chain, not mastery books.
        if(b.getJob()==Job.BISHOP&&b.getMasterLevel(client.SkillFactory.getSkill(constants.skills.Bishop.BAHAMUT))<30){
            int[] items={4031449,4031482,4031483,4031484,4031485};
            for(int i=0;i<items.length;i++){
                var quest=server.quest.Quest.getInstance(6120+i);
                if(b.getQuest(quest).getStatus()==client.QuestStatus.Status.COMPLETED)continue;
                BotZakumSupplies.stock(b,items[i],20+5*i);
                quest.forceStartWithActions(b,2081007);
                quest.forceCompleteWithActions(b,2081007,null);
            }
        }
        BotZakumBooks.prepare(b);
        for(var item:List.copyOf(b.getInventory(client.inventory.InventoryType.USE).list()))
            if(item.getItemId()/10000==207&&item.getQuantity()==0)
                client.inventory.manipulator.InventoryManipulator.removeFromSlot(b.getClient(),client.inventory.InventoryType.USE,item.getPosition(),(short)0,false);
        if(!BotRaidAttackLoadout.ready(b)){
            BotHorntailEquipment.prepareAp(e,b);
            BotMagatiaRun.provision(e,b);
            e.apBuild=BotBuildManager.resolveApBuild(e,b);BotBuildManager.respecAp(e,b);
        }
        if(b.getJob()==Job.NIGHTLORD){
            BotPinkBeanCompanions.prepareStars(b); // Balanced Fury for both endgame raid crews.
        }
        if(BotAttackExecutionProvider.getEquippedWeaponType(b)==client.inventory.WeaponType.BOW)BotZakumSupplies.stock(b,2060000,12000);
        BotZakumSupplies.stock(b,2002023,referenceId==8810003?2000:500);
    }
    public static synchronized String release(Character leader) {
        var c=BotHorntailRun.crew(leader);if(c==null)return "You have no active Horntail crew.";
        if(leader.getEventInstance()!=null||c.bots.stream().anyMatch(b->b.getEventInstance()!=null))return "Leave the trial or expedition and wait for the companions before dismissing.";
        for(var b:List.copyOf(c.bots))dismiss(b);
        return "Your Horntail companions have been saved and logged off.";
    }
    static void dismiss(Character b) {
        synchronized(BotHorntailCompanions.class) {
        var m=BotManager.getInstance();var e=m.getEntryByBotCharId(b.getId());if(e==null)return;
        if(b.getParty()!=null) {
            if(b.getWorldServer().getParty(b.getParty().getId())!=null)Party.leaveParty(b.getParty(),b.getClient());
            else {b.setParty(null);b.setMPC(null);}
        }
        b.saveCharToDB(true);
        BotHorntailRun.unregister(b);m.applyOperatorCommand(e,BotEntry.OperatorCmd.IDLE,-1,0);
        b.getClient().disconnect(false,false);
        }
    }
}
