package server.bots;

import client.Character;
import client.Job;
import client.BotClient;
import net.server.world.Party;
import java.util.*;

/** Reusable Ellin fixtures, never drawn from the autonomous population. */
public final class BotEllinCompanions {
    private BotEllinCompanions() {}
    public static synchronized String assemble(Character leader) {
        if (leader.getClient() instanceof BotClient || leader.getMapId()!=BotEllinRun.RECRUIT
                || leader.getLevel()<44 || leader.getLevel()>55 || leader.getJob().getId()/1000!=0
                || leader.getEventInstance()!=null) return "You must be a level 44-55 Explorer in Deep Fairy Forest.";
        if (leader.getParty()!=null && (leader.getParty().getLeaderId()!=leader.getId() || leader.getParty().getMembers().size()>1))
            return "Please dismiss your current crew or leave your party first. I will make you party leader.";
        var bots=new ArrayList<Character>(); var manager=BotManager.getInstance();
        try {
            for (var job:List.of(Job.SPEARMAN,Job.CLERIC,Job.ASSASSIN)) {
                String prefix=job==Job.SPEARMAN?"EPqSpear":job==Job.CLERIC?"EPqMage":"EPqThief";
                int id=BotPqCompanionPool.acquire(leader,"epq",job,prefix,BotEllinRun.RECRUIT);
                var result=manager.spawnPqCompanion(leader,id);
                if(!result.success()) throw new IllegalStateException(result.errorMessage());
                bots.add(result.bot());
                manager.applyOperatorCommand(manager.getEntryByBotCharId(id),BotEntry.OperatorCmd.IDLE,-1,0);
            }
            BotEllinRun.startHuman(leader,bots);
            return "Your Spearman, Cleric and Assassin are assigned. You are party leader. Wait for everyone to arrive, then talk to me again to enter.\r\n\r\nWe explain each stage, dilute poison, capture monsters and search for the stone. You handle stage turn-ins, summon the golem and claim the rewards. We wait for you.";
        } catch(RuntimeException failure) {
            org.slf4j.LoggerFactory.getLogger(BotEllinCompanions.class).warn("Ellin assembly failed",failure);
            for(var b:bots) dismiss(b);
            return "I could not prepare your crew. Please try again in a moment.";
        }
    }
    public static synchronized String release(Character leader) {
        if(leader.getEventInstance()!=null) return "Finish or leave the party quest before dismissing your crew.";
        if(leader.getParty()==null) return "You have no Ellin crew to dismiss.";
        var bots=new ArrayList<Character>();
        for(var member:leader.getParty().getMembers()) {
            var e=BotManager.getInstance().getEntryByBotCharId(member.getId());
            if(e!=null && BotEllinRun.belongsTo(e.bot,leader.getId())) {
                if(e.bot.getEventInstance()!=null || e.bot.getMapId()!=leader.getMapId()) return "Wait for your companions to return first.";
                bots.add(e.bot);
            }
        }
        for(var b:bots) dismiss(b);
        return "Your Ellin companions have been saved and dismissed.";
    }
    static void dismiss(Character b) {
        var m=BotManager.getInstance(); var e=m.getEntryByBotCharId(b.getId()); if(e==null)return;
        BotEllinRun.unregister(b); m.applyOperatorCommand(e,BotEntry.OperatorCmd.IDLE,-1,0);
        if(b.getParty()!=null) Party.leaveParty(b.getParty(),b.getClient());
        b.saveCharToDB(true); b.getClient().disconnect(false,false);
    }
}
