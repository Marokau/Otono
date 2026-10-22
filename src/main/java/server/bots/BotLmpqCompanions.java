package server.bots;
import client.BotClient;
import client.Character;
import client.Job;
import net.server.world.Party;
import java.util.*;

/** Rolly's explicit dedicated crew request. Population characters are never recruited. */
public final class BotLmpqCompanions {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BotLmpqCompanions.class);
    private BotLmpqCompanions() {}
    public static synchronized String assemble(Character leader) {
        if (leader.getClient() instanceof BotClient || leader.getLevel() < 51 || leader.getLevel() > 70
                || leader.getMapId() != BotLmpqRun.RECRUIT || leader.getEventInstance() != null)
            return "You must be level 51-70 and in Ludibrium to request an LMPQ crew.";
        if (leader.getParty() != null && (leader.getParty().getLeaderId() != leader.getId() || leader.getParty().getMembers().size() > 1))
            return "Please dismiss your current crew or leave your party first. I will make you leader of the new party.";
        var manager = BotManager.getInstance(); var bots = new ArrayList<Character>();
        try {
            for (var job : List.of(Job.SPEARMAN, Job.CLERIC, Job.ASSASSIN, Job.HUNTER)) {
                String prefix = job == Job.SPEARMAN ? "LMPqSpear" : job == Job.CLERIC ? "LMPqCleric" : job == Job.ASSASSIN ? "LMPqThief" : "LMPqArcher";
                int id = BotPqCompanionPool.acquire(leader, "lmpq", job, prefix, BotLmpqRun.RECRUIT);
                var result = manager.spawnPqCompanion(leader, id);
                if (!result.success()) throw new IllegalStateException(result.errorMessage());
                bots.add(result.bot()); manager.issueStop(manager.getEntryByBotCharId(id));
            }
            BotLmpqRun.startHuman(leader, bots);
            return "Your four companions are ready, and you are the party leader. Talk to Rolly again to enter.\r\n\r\nWe start in random rooms. Follow the portal hints to room 6. The crew collects coupons and waits for you before moving on. If the drops fall short, we try room 7 and other rooms.\r\n\r\nMeet us in room 16, pick up the coupons dropped beside you, and talk to Pietri yourself. Claim your own reward and exit afterward. You can dismiss the crew here.";
        } catch (RuntimeException failure) {
            log.warn("Could not assemble LMPQ crew for {}", leader.getId(), failure);
            for (var bot : bots) dismiss(bot);
            return "I could not prepare the crew. Please try again in a moment.";
        }
    }
    public static synchronized String release(Character leader) {
        if (leader.getEventInstance() != null) return "Please finish or leave the maze before dismissing your crew.";
        if (leader.getParty() == null) return "You have no LMPQ crew to dismiss.";
        var bots = new ArrayList<Character>();
        for (var member : new ArrayList<>(leader.getParty().getMembers())) {
            var entry = BotManager.getInstance().getEntryByBotCharId(member.getId());
            if (entry != null && BotLmpqRun.belongsTo(entry.getBot(), leader.getId())) {
                if (entry.getBot().getEventInstance() != null || entry.getBot().getMapId() != BotLmpqRun.RECRUIT)
                    return "Please wait until all companions finish claiming their rewards and return to Ludibrium.";
                bots.add(entry.getBot());
            }
        }
        for (var bot : bots) dismiss(bot);
        return "Your LMPQ companions have been saved and dismissed.";
    }
    static void dismiss(Character bot) {
        var manager = BotManager.getInstance(); var entry = manager.getEntryByBotCharId(bot.getId()); if (entry == null) return;
        BotLmpqRun.unregister(bot); manager.applyOperatorCommand(entry, BotEntry.OperatorCmd.IDLE, -1, 0);
        if (bot.getParty() != null) Party.leaveParty(bot.getParty(), bot.getClient());
        bot.saveCharToDB(true); bot.getClient().disconnect(false, false);
    }
}
