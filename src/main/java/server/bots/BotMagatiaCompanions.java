package server.bots;

import client.BotClient;
import client.Character;
import client.Job;
import net.server.world.Party;
import java.util.*;

/** Explicit dedicated fixtures, isolated from the autonomous population. */
public final class BotMagatiaCompanions {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BotMagatiaCompanions.class);
    private BotMagatiaCompanions() {}
    public static synchronized String assemble(Character leader) {
        if (leader.getClient() instanceof BotClient || leader.getLevel() < 71 || leader.getLevel() > 85
                || leader.getMapId() != BotMagatiaRun.ZENUMIST && leader.getMapId() != BotMagatiaRun.ALCADNO
                || leader.getEventInstance() != null) return "You must be level 71-85 and in Romeo or Juliet's hidden room.";
        if (leader.getParty() != null && (leader.getParty().getLeaderId() != leader.getId() || leader.getParty().getMembers().size() > 1))
            return "Please dismiss your current crew or leave your party first. I will make you party leader.";
        var bots = new ArrayList<Character>(); var manager = BotManager.getInstance();
        try {
            for (var job : List.of(Job.PRIEST, Job.HERMIT, Job.CRUSADER)) {
                String prefix = job == Job.PRIEST ? "MPqMage" : job == Job.HERMIT ? "MPqThief" : "MPqSword";
                int id = BotPqCompanionPool.acquire(leader, "mpq", job, prefix, leader.getMapId());
                var result = manager.spawnPqCompanion(leader, id);
                if (!result.success()) throw new IllegalStateException(result.errorMessage());
                bots.add(result.bot()); manager.issueStop(manager.getEntryByBotCharId(id));
            }
            BotMagatiaRun.startHuman(leader, bots);
            return "Your Priest, Hermit and Crusader are ready, and you are party leader. Talk to me again to enter.\r\n\r\nThe crew searches books, fights, fills beakers and handles both research rooms. Pick up the two files they drop beside you and turn them in yourself.\r\n\r\nChoose the remaining puzzle room; we share confirmed row clues. At the end, talk to Yulete to start the boss. Our Priest guards Romeo/Juliet. Claim your own rewards. We wait for your actions and can be dismissed here afterward.";
        } catch (RuntimeException failure) {
            log.warn("Could not assemble Magatia crew for {}", leader.getId(), failure);
            for (var bot : bots) dismiss(bot);
            return "I could not prepare the crew. Please try again in a moment.";
        }
    }
    public static synchronized String release(Character leader) {
        if (leader.getEventInstance() != null) return "Finish or leave the party quest before dismissing your crew.";
        if (leader.getParty() == null) return "You have no Magatia crew to dismiss.";
        var bots = new ArrayList<Character>();
        for (var member : new ArrayList<>(leader.getParty().getMembers())) {
            var entry = BotManager.getInstance().getEntryByBotCharId(member.getId());
            if (entry != null && BotMagatiaRun.belongsTo(entry.bot, leader.getId())) {
                if (entry.bot.getEventInstance() != null || entry.bot.getMapId() != leader.getMapId())
                    return "Wait for all companions to finish their rewards and return first.";
                bots.add(entry.bot);
            }
        }
        for (var bot : bots) dismiss(bot);
        return "Your Magatia companions have been saved and dismissed.";
    }
    static void dismiss(Character bot) {
        var manager = BotManager.getInstance(); var e = manager.getEntryByBotCharId(bot.getId()); if (e == null) return;
        BotMagatiaRun.unregister(bot); manager.applyOperatorCommand(e, BotEntry.OperatorCmd.IDLE, -1, 0);
        if (bot.getParty() != null) Party.leaveParty(bot.getParty(), bot.getClient());
        bot.saveCharToDB(true); bot.getClient().disconnect(false, false);
    }
}
