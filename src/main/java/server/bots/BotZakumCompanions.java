package server.bots;

import client.BotClient;
import client.Character;
import client.Job;
import client.inventory.manipulator.InventoryManipulator;
import net.server.world.Party;
import server.quest.Quest;
import java.util.*;

/** Adobis' opt-in boss crew. Prerequisite grants apply only to dedicated headless companions. */
public final class BotZakumCompanions {
    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(BotZakumCompanions.class);
    private BotZakumCompanions() {}
    static boolean humanEligible(Character human) {
        return !(human.getClient() instanceof BotClient)&&human.getLevel()>=50&&human.getLevel()<=255
                &&human.getQuest(Quest.getInstance(100200)).getStatus()!=client.QuestStatus.Status.NOT_STARTED
                &&human.getQuest(Quest.getInstance(100200)).getStatus()!=client.QuestStatus.Status.UNDEFINED
                &&human.getQuest(Quest.getInstance(100201)).getStatus()==client.QuestStatus.Status.COMPLETED;
    }
    public static synchronized String assemble(Character leader) {
        if(!humanEligible(leader)||leader.getMapId()!=BotZakumBattle.RECRUIT||leader.getEventInstance()!=null)
            return "Complete your Zakum approval and trials, then enter the altar entrance with an Eye of Fire. Level 50 or higher is required.";
        if(leader.getParty()!=null&&(leader.getParty().getLeaderId()!=leader.getId()||leader.getParty().getMembers().size()>1))
            return "Please dismiss your existing crew or leave your current party first.";
        for (var member : leader.getWorldServer().getPlayerStorage().getAllCharacters())
            if (BotZakumBattle.belongsTo(member, leader.getId())) return "Your previous Zakum crew is still active. Dismiss it at Adobis before assembling another.";
        var bots=new ArrayList<Character>();
        try {
            for(var job:List.of(Job.DARKKNIGHT,Job.BISHOP,Job.BOWMASTER,Job.NIGHTLORD,Job.HERO,
                    Job.BISHOP,Job.DARKKNIGHT,Job.BOWMASTER,Job.NIGHTLORD,Job.HERO,Job.NIGHTLORD))bots.add(acquire(leader,job));
            BotZakumBattle.start(leader,bots,true);
            return "Your eleven Zakum companions are ready in two parties of six, with a Bishop and Dark Knight in each. You lead the expedition; the second party is led by its Bishop. Create the expedition; all eleven bots will register themselves. Once all twelve participants are registered, start the fight. Inside, wait for our buff-ready message, then drop your own Eye of Fire at the altar when ready. We will fight, leave the loot for you and wait until you exit through Amon. Return here to dismiss the crew.";
        } catch(RuntimeException failure){log.warn("Could not assemble Zakum crew for {}",leader.getId(),failure);for(var bot:bots)dismiss(bot);return "I could not prepare the crew. Please try again shortly.";}
    }
    static Character acquire(Character leader,Job job) {
        String prefix=switch(job){case DARKKNIGHT->"ZakumSpear";case BISHOP->"ZakumBishop";case BOWMASTER->"ZakumArcher";case NIGHTLORD->"ZakumThief";case HERO->"ZakumSword";default->"ZakumMage";};
        int id=BotPqCompanionPool.acquire(leader,"zakum",job,prefix,BotZakumBattle.RECRUIT);
        var result=BotManager.getInstance().spawnPqCompanion(leader,id);
        if(!result.success())throw new IllegalStateException(result.errorMessage());
        var bot=result.bot();
        try {
            prepare(bot);
            // Adobis summons only the prepared dedicated companion, through the same
            // changeMap path used by NPC warps. Never teleport into the boss instance.
            if(bot.getMapId()!=BotZakumBattle.RECRUIT){
                var recruit=bot.getClient().getChannelServer().getMapFactory().getMap(BotZakumBattle.RECRUIT);
                bot.changeMap(recruit,recruit.getPortal("west00"));
            }
            return bot;
        }catch(RuntimeException failure){dismiss(bot);throw failure;}
    }
    static void prepare(Character bot) {
        if(!(bot.getClient() instanceof BotClient)||bot.isGM()||bot.getEventInstance()!=null)
            throw new IllegalArgumentException("Only dedicated headless companions can receive Zakum preparation");
        Quest.getInstance(100200).forceStartWithActions(bot,2030008);
        Quest.getInstance(100201).forceCompleteWithActions(bot,2030008,null);
        var entry=BotManager.getInstance().getEntryByBotCharId(bot.getId());
        var reference=server.life.LifeFactory.getMonster(8800002);
        entry.aspirationalMobLevel=reference.getLevel();entry.aspirationalMobAvoid=reference.getAvoidability();
        entry.apBuild=BotBuildManager.resolveApBuild(entry,bot);
        BotBuildManager.respecAp(entry,bot);
        BotZakumBooks.prepare(bot);
        // Used rechargeable sets retain zero-quantity slots. Retire empty supplied sets
        // before awarding the next run's ammunition so repeat preparation has room.
        for(var item:List.copyOf(bot.getInventory(client.inventory.InventoryType.USE).list()))
            if(item.getItemId()/10000==207&&item.getQuantity()==0)
                InventoryManipulator.removeFromSlot(bot.getClient(),client.inventory.InventoryType.USE,item.getPosition(),(short)0,false);
        BotPqProvisioner.provision(bot);
        BotZakumSupplies.restock(entry,bot);
    }
    static void provisionSkillConsumables(BotEntry entry,Character bot) {
        BotCombatManager.rebuildSkillCacheIfNeeded(entry,bot);
        var required=new LinkedHashSet<>(BotRockManager.neededRockIds(entry,bot));
        // Summon/attack skills are separate caches from buffs. Read every learned
        // skill's itemCon so Summon Dragon and other requirements are not omitted.
        for(var skill:bot.getSkills().keySet()) {
            int level=bot.getSkillLevel(skill);if(level<=0)continue;
            var effect=skill.getEffect(level);
            if(effect.getItemCon()>0&&effect.getItemConNo()>0)required.add(effect.getItemCon());
        }
        if (bot.getJob().getId() / 100 == 2) {
            required.add(4006000); // Magic Rock
            required.add(4006001); // Summoning Rock
        }
        for(int item:required) {
            int target=200;
            int needed=Math.max(0,target-BotRockManager.countRocks(bot,item));
            if(needed>0&&!InventoryManipulator.addById(bot.getClient(),item,(short)needed))
                throw new IllegalStateException("No room for Zakum skill consumables");
        }
    }
    public static synchronized String release(Character leader) {
        var bots=new ArrayList<Character>();
        for(var member:leader.getWorldServer().getPlayerStorage().getAllCharacters()){
            var e=BotManager.getInstance().getEntryByBotCharId(member.getId());
            if(e!=null&&BotZakumBattle.belongsTo(e.bot,leader.getId())){
                if(e.bot.getEventInstance()!=null||e.bot.getMapId()!=BotZakumBattle.RECRUIT&&e.bot.getMapId()!=211042300)
                    return "Wait until all companions have left the altar through Amon.";
                bots.add(e.bot);
            }
        }
        for(var bot:bots)dismiss(bot);return "Your Zakum companions have been saved and dismissed.";
    }
    static void dismiss(Character bot) {
        var manager=BotManager.getInstance();var entry=manager.getEntryByBotCharId(bot.getId());if(entry==null)return;
        BotZakumBattle.unregister(bot);manager.applyOperatorCommand(entry,BotEntry.OperatorCmd.IDLE,-1,0);
        if(bot.getParty()!=null)Party.leaveParty(bot.getParty(),bot.getClient());
        bot.saveCharToDB(true);bot.getClient().disconnect(false,false);
    }
}
