package server.bots;

import client.BotClient;
import client.Character;
import client.Job;
import net.server.world.Party;

import java.util.ArrayList;
import java.util.List;

/** Lakelis' explicit companion request. Never recruits population characters. */
public final class BotKpqCompanions {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BotKpqCompanions.class);
    private BotKpqCompanions() {}

    public static synchronized String assemble(Character leader) {
        if (leader.getClient() instanceof BotClient || leader.getMapId() != 103000000
                || leader.getEventInstance() != null || leader.getLevel() < 21 || leader.getLevel() > 30)
            return "You must be level 21-30 and in Kerning City to request a KPQ crew.";
        if (leader.getParty() != null && (leader.getParty().getLeaderId() != leader.getId()
                || leader.getParty().getMembers().size() > 1))
            return "Please leave your current party first. I will make you leader of a new bot party.";
        var manager = BotManager.getInstance();
        List<Character> bots = new ArrayList<>();
        try {
            int family = leader.getJob().getId() / 100;
            for (Job job : List.of(Job.WARRIOR, Job.MAGICIAN, Job.BOWMAN, Job.THIEF)) {
                if (job.getId() / 100 == family || bots.size() == 3) continue;
                String prefix = job == Job.WARRIOR ? "KPqSword" : job == Job.MAGICIAN ? "KPqMage"
                        : job == Job.BOWMAN ? "KPqArcher" : "KPqThief";
                int id = BotKpqPool.acquire(leader, job, prefix);
                var result = manager.spawnPqCompanion(leader, id);
                if (!result.success()) throw new IllegalStateException(result.errorMessage());
                var bot = result.bot();
                bots.add(bot);
                var entry = manager.getEntryByBotCharId(bot.getId());
                manager.issueStop(entry);
                BotBuildManager.setAutoApBuild(entry, bot);
                BotPqProvisioner.provision(bot);
                manager.partyUp(leader, bot);
                entry.kpq.leaderId = leader.getId();
                entry.kpq.humanLed = true;
            }
            if (leader.getParty() == null || leader.getParty().getMembers().size() != 4)
                throw new IllegalStateException("Could not assemble four party members");
            return "Your three companions are ready! You are the party leader. Talk to me again and choose to participate.\r\n\r\nThe bots will solve their coupon tasks and give you passes. You collect passes and talk to Cloto to check each stage. On puzzle stages, wait for the bots to get into position before checking. They will follow you through opened portals. Talk to Cloto yourself for your reward.\r\n\r\nAfter the run, leave through Nella. You can dismiss the crew here when you are done.";
        } catch (RuntimeException failure) {
            log.warn("Could not assemble KPQ companions for character {}", leader.getId(), failure);
            for (var bot : bots) dismiss(bot);
            return "I could not prepare the crew. Please try again in a moment.";
        }
    }

    public static synchronized String release(Character leader) {
        if (leader.getEventInstance() != null) return "Please leave the party quest through Nella first.";
        var party = leader.getParty();
        if (party == null) return "You have no bot crew to dismiss.";
        for (var member : new ArrayList<>(party.getMembers())) {
            var entry = BotManager.getInstance().getEntryByBotCharId(member.getId());
            if (entry != null && entry.kpq.humanLed && entry.kpq.leaderId == leader.getId()) dismiss(entry.getBot());
        }
        return "Your KPQ companions have been dismissed. Talk to me whenever you need a new crew.";
    }

    static void dismiss(Character bot) {
        var entry = BotManager.getInstance().getEntryByBotCharId(bot.getId());
        if (entry == null) return;
        BotManager.getInstance().applyOperatorCommand(entry, BotEntry.OperatorCmd.IDLE, -1, 0);
        if (bot.getParty() != null) Party.leaveParty(bot.getParty(), bot.getClient());
        bot.getClient().disconnect(false, false);
    }
}
