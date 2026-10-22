package server.bots;

import client.BotClient;
import client.Character;
import client.Job;
import net.server.world.Party;

import java.util.ArrayList;
import java.util.List;

/** Explicit LPQ recruitment; never borrows characters from the population. */
public final class BotLpqCompanions {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BotLpqCompanions.class);
    private BotLpqCompanions() {}

    /** Companion runs retain ordinary puzzle checks even when the human has GM permissions. */
    public static boolean usesCompanions(Character leader) { return BotLpqHuman.hasCrew(leader); }

    public static synchronized String assemble(Character leader) {
        if (leader.getClient() instanceof BotClient || leader.getMapId() != BotLpqRun.RECRUIT_MAP
                || leader.getEventInstance() != null || leader.getLevel() < 35 || leader.getLevel() > 50)
            return "You must be level 35-50 and at the 101st Floor to request an LPQ crew.";
        if (leader.getParty() != null && (leader.getParty().getLeaderId() != leader.getId()
                || leader.getParty().getMembers().size() > 1))
            return "Please dismiss your existing crew or leave your current party first. I will make you leader of a new bot party.";
        var manager = BotManager.getInstance();
        List<Character> bots = new ArrayList<>();
        try {
            for (Job job : List.of(Job.SPEARMAN, Job.CLERIC, Job.IL_WIZARD, Job.ASSASSIN, Job.HUNTER)) {
                String prefix = switch (job) {
                    case SPEARMAN -> "LPqSpear";
                    case CLERIC -> "LPqCleric";
                    case IL_WIZARD -> "LPqMage";
                    case ASSASSIN -> "LPqThief";
                    default -> "LPqArcher";
                };
                int id = BotLpqPool.acquire(leader, job, prefix);
                var result = manager.spawnPqCompanion(leader, id);
                if (!result.success()) throw new IllegalStateException(result.errorMessage());
                var bot = result.bot();
                bots.add(bot);
                manager.issueStop(manager.getEntryByBotCharId(id));
            }
            BotLpqHuman.start(leader, bots);
            return "Your five companions are ready! You are the party leader. Talk to me again and choose to participate.\r\n\r\nThe crew will collect passes and drop them beside you. Pick them up and talk to each stage NPC yourself. Use each opened portal when you are ready; they will wait for you.\r\n\r\nThey cover the magic, physical, Teleport and Dark Sight rooms. In stage five they handle all six rooms, so you can wait safely in the main room. At the box puzzle, stay OFF the boxes and check the NPC whenever the crew says it is ready.\r\n\r\nClaim your own reward at the end. You can dismiss the crew here after leaving the PQ.";
        } catch (RuntimeException failure) {
            log.warn("Could not assemble LPQ companions for character {}", leader.getId(), failure);
            for (var bot : bots) dismiss(bot);
            return "I could not prepare the crew. Please try again in a moment.";
        }
    }

    public static synchronized String release(Character leader) {
        if (leader.getEventInstance() != null) return "Please leave the party quest through Sgt. Anderson first.";
        if (leader.getParty() == null) return "You have no bot crew to dismiss.";
        for (var member : new ArrayList<>(leader.getParty().getMembers())) {
            var entry = BotManager.getInstance().getEntryByBotCharId(member.getId());
            if (entry != null && BotLpqHuman.belongsTo(entry.getBot(), leader.getId())) dismiss(entry.getBot());
        }
        return "Your LPQ companions have been saved and dismissed. Talk to me whenever you need a crew.";
    }

    static void dismiss(Character bot) {
        var manager = BotManager.getInstance();
        var entry = manager.getEntryByBotCharId(bot.getId());
        if (entry == null) return;
        BotLpqHuman.unregister(bot);
        manager.applyOperatorCommand(entry, BotEntry.OperatorCmd.IDLE, -1, 0);
        if (bot.getParty() != null) Party.leaveParty(bot.getParty(), bot.getClient());
        bot.saveCharToDB(true);
        bot.getClient().disconnect(false, false);
    }
}
