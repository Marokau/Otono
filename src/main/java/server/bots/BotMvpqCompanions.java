package server.bots;

import client.BotClient;
import client.Character;
import client.Job;
import net.server.world.Party;
import tools.DatabaseConnection;
import java.util.*;

/** Charles' opt-in, reusable MV's Lair crew; population characters are never recruited. */
public final class BotMvpqCompanions {
    private BotMvpqCompanions() {}
    static String eligibility(Character leader, boolean test) {
        if (leader == null || !test && leader.getClient() instanceof BotClient || leader.getLevel() < 140
                || leader.getLevel() > 255 || leader.getMapId() != BotMvpqRun.RECRUIT || leader.getEventInstance() != null)
            return "You must be level 140 or higher and outside MV's Lair at Charles.";
        if (leader.getParty() != null && (leader.getParty().getLeaderId() != leader.getId()
                || leader.getParty().getMembers().size() > 1)) return "Dismiss your current crew or leave your party first. I will make you party leader.";
        return null;
    }
    public static synchronized String assemble(Character leader) { return assemble(leader, false); }
    static synchronized String assembleTest(Character leader) { return assemble(leader, true); }
    private static String assemble(Character leader, boolean test) {
        String problem = eligibility(leader, test); if (problem != null) return problem;
        var bots = new ArrayList<Character>(); var manager = BotManager.getInstance();
        try {
            for (var job : List.of(Job.BISHOP, Job.DARKKNIGHT, Job.BOWMASTER, Job.NIGHTLORD, Job.HERO)) {
                String prefix = switch (job) { case BISHOP -> "MVPqBishop"; case DARKKNIGHT -> "MVPqSpear";
                    case BOWMASTER -> "MVPqArcher"; case NIGHTLORD -> "MVPqThief"; default -> "MVPqSword"; };
                int id = BotPqCompanionPool.acquire(leader, "mvpq", job, prefix, BotMvpqRun.RECRUIT);
                var result = manager.spawnPqCompanion(leader, id);
                if (!result.success()) throw new IllegalStateException(result.errorMessage());
                bots.add(result.bot());
            }
            BotMvpqRun.start(leader, bots, !test);
            return "Your five MV's Lair companions are ready, and you are party leader. Talk to Charles again to enter normally. We search the trapped course for 15 Secret Letters and bring them to you. Pick them up and speak to the guide by the upper-left tunnel. Once the letters are accepted, wait for our ready message by the tunnel. Enter first; we follow and help fight MV. Boss and treasure-room loot is yours. Leave through the treasure room's bottom portal whenever you are finished, then dismiss us here.";
        } catch (RuntimeException failure) {
            org.slf4j.LoggerFactory.getLogger(BotMvpqCompanions.class).warn("MVPQ crew preparation failed for {}", leader.getId(), failure);
            for (var bot : bots) dismiss(bot);
            return "I could not prepare your MV's Lair crew: " + failure.getMessage();
        }
    }
    static int createFixture(String name, Job job, int world, int channel) {
        int id = BotZakumCrewFactory.create(name, job, world, channel);
        try (var db = DatabaseConnection.getConnection(); var q = db.prepareStatement("UPDATE characters SET map=?,spawnpoint=0 WHERE id=?")) {
            q.setInt(1, BotMvpqRun.RECRUIT); q.setInt(2, id); q.executeUpdate(); return id;
        } catch (java.sql.SQLException x) { throw new IllegalStateException("Could not save MVPQ fixture spawn", x); }
    }
    static void prepare(BotEntry entry, Character bot) {
        var target = server.life.LifeFactory.getMonster(9400589);
        entry.aspirationalMobLevel = target.getLevel(); entry.aspirationalMobAvoid = target.getAvoidability();
        entry.apBuild = BotBuildManager.resolveApBuild(entry, bot); BotBuildManager.respecAp(entry, bot);
        BotZakumBooks.prepare(bot); BotMagatiaRun.provision(entry, bot);
    }
    public static String refresh(Character leader) {
        if (leader.getEventInstance() != null || leader.getMapId() != BotMvpqRun.RECRUIT) return "Prepare your crew at Charles before entering.";
        if (leader.getParty() == null) return null;
        int count = 0; var ready = new ArrayList<Character>();
        for (var member : leader.getParty().getMembers()) {
            if (member.getId() == leader.getId()) continue;
            var e = BotManager.getInstance().getEntryByBotCharId(member.getId());
            if (e != null && BotMvpqRun.belongsTo(e.bot, leader.getId())) {
                if (e.bot.getEventInstance() != null || e.bot.getMapId() != leader.getMapId()) return "Wait for all five MV's Lair companions to return first.";
                count++; ready.add(e.bot); BotMagatiaRun.provision(e, e.bot);
            }
        }
        if (count == 5) BotMvpqRun.start(leader, ready, !(leader.getClient() instanceof BotClient));
        return count == 0 || count == 5 ? null : "Reassemble your complete five-bot MV's Lair crew before entering.";
    }
    public static synchronized String release(Character leader) {
        if (leader.getEventInstance() != null) return "Finish or leave MV's Lair before dismissing the crew.";
        var bots = new ArrayList<Character>();
        for (var b : leader.getWorldServer().getPlayerStorage().getAllCharacters())
            if (BotMvpqRun.belongsTo(b, leader.getId()) && b != leader) {
                if (b.getEventInstance() != null || b.getMapId() != BotMvpqRun.RECRUIT) return "Wait until all companions return to Charles.";
                bots.add(b);
            }
        for (var b : bots) dismiss(b);
        return "Your MV's Lair crew has been saved and logged off.";
    }
    static void dismiss(Character bot) {
        var m = BotManager.getInstance(); var e = m.getEntryByBotCharId(bot.getId()); if (e == null) return;
        BotMvpqRun.unregister(bot); m.applyOperatorCommand(e, BotEntry.OperatorCmd.IDLE, -1, 0);
        if (bot.getParty() != null) Party.leaveParty(bot.getParty(), bot.getClient());
        bot.saveCharToDB(true); bot.getClient().disconnect(false, false);
    }
}
