package server.bots;
import client.BotClient;
import client.Character;
import client.Job;
import net.server.world.Party;
import java.util.*;

/** Explicit Wonky recruitment from the dedicated OPQ pool. */
public final class BotOpqCompanions {
    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(BotOpqCompanions.class);
    private BotOpqCompanions() {}
    public static synchronized String assemble(Character leader) {
        if(leader.getClient() instanceof BotClient||leader.getLevel()<51||leader.getLevel()>70
                ||leader.getMapId()!=BotOpqRun.RECRUIT_MAP||leader.getEventInstance()!=null)
            return "You must be level 51-70 and at the Unknown Tower to request an Orbis crew.";
        if(leader.getParty()!=null&&(leader.getParty().getLeaderId()!=leader.getId()||leader.getParty().getMembers().size()>1))
            return "Please dismiss your existing crew or leave your current party first. I will make you leader of a new crew.";
        var manager=BotManager.getInstance();var bots=new ArrayList<Character>();
        try {
            for(var job:List.of(Job.SPEARMAN,Job.CLERIC,Job.IL_WIZARD,Job.ASSASSIN,Job.HUNTER)) {
                String prefix=switch(job){case SPEARMAN->"OPqSpear";case CLERIC->"OPqCleric";case IL_WIZARD->"OPqMage";case ASSASSIN->"OPqThief";default->"OPqArcher";};
                int id=BotPqCompanionPool.acquire(leader,"opq",job,prefix,BotOpqRun.RECRUIT_MAP);
                var result=manager.spawnPqCompanion(leader,id);if(!result.success())throw new IllegalStateException(result.errorMessage());
                bots.add(result.bot());manager.issueStop(manager.getEntryByBotCharId(id));
            }
            BotOpqHuman.start(leader,bots);
            return "Your five companions are ready. You are the party leader. Talk to Wonky again to enter.\r\n\r\nYou make Eak's NPC checks. The crew collects stage items and drops required pieces beside you. Pick them up before checking Eak. When he gives YOU a statue piece, return to the Center Tower and drop it beside the crew; we will place it on the statue.\r\n\r\nJoin the crew in each room when ready. They cover the Lounge subrooms and upper Ascent levers. Stay OFF the Sealed Room platforms, and check Eak when the crew says its arrangement is ready. Learned portal routes are shared in chat.\r\n\r\nIn the Garden, pick up the Grass of Life and use the return portal yourself. Drop it at Minerva's statue, then choose her exit and claim your reward. You can dismiss your companions here afterward.";
        }catch(RuntimeException failure){log.warn("Could not assemble OPQ companions for {}",leader.getId(),failure);for(var bot:bots)dismiss(bot);return "I could not prepare the crew. Please try again in a moment.";}
    }
    public static synchronized String release(Character leader) {
        if(leader.getEventInstance()!=null)return "Please finish or leave the Tower before dismissing the crew.";
        if(leader.getParty()==null)return "You have no Orbis crew to dismiss.";
        var bots=new ArrayList<Character>();
        for(var member:new ArrayList<>(leader.getParty().getMembers())){
            var e=BotManager.getInstance().getEntryByBotCharId(member.getId());
            if(e!=null&&BotOpqHuman.belongsTo(e.bot,leader.getId())){
                if(e.bot.getEventInstance()!=null||e.bot.getMapId()!=BotOpqRun.RECRUIT_MAP)
                    return "Please wait until all companions finish claiming their rewards and return to the Unknown Tower.";
                bots.add(e.bot);
            }
        }
        for(var bot:bots)dismiss(bot);
        return "Your Orbis companions have been saved and dismissed.";
    }
    static void dismiss(Character bot) {
        var manager=BotManager.getInstance();var e=manager.getEntryByBotCharId(bot.getId());if(e==null)return;
        BotOpqHuman.unregister(bot);manager.applyOperatorCommand(e,BotEntry.OperatorCmd.IDLE,-1,0);
        if(bot.getParty()!=null)Party.leaveParty(bot.getParty(),bot.getClient());bot.saveCharToDB(true);bot.getClient().disconnect(false,false);
    }
}
