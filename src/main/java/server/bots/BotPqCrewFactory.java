package server.bots;

import client.BotClient;
import client.Character;
import client.Job;
import tools.DatabaseConnection;

/** Owner-authorized placeholder setup, separate from live PQ gameplay and population scheduling. */
public final class BotPqCrewFactory {
    private BotPqCrewFactory() {}

    public static int createKpqPlaceholder(String name, Job job) {
        return createKpqPlaceholder(name, job, 0, 1);
    }

    public static int createKpqPlaceholder(String name, Job job, int world, int channel) {
        return createPlaceholder(name, job, null, 21, 103000000, 9300003, world, channel);
    }

    public static int createLpqPlaceholder(String name, Job secondJob, int world, int channel) {
        Job firstJob = switch (secondJob) {
            case FIGHTER, PAGE, SPEARMAN -> Job.WARRIOR;
            case CLERIC, IL_WIZARD, FP_WIZARD -> Job.MAGICIAN;
            case HUNTER, CROSSBOWMAN -> Job.BOWMAN;
            case ASSASSIN, BANDIT -> Job.THIEF;
            default -> throw new IllegalArgumentException("Choose an explorer second job");
        };
        // Stage one's Black Ratz; boss accuracy is separately verified before boss attempts.
        return createPlaceholder(name, firstJob, secondJob, 35, 221024500, 9300006, world, channel);
    }

    static int createGuildPlaceholder(String name, Job target, int world, int channel) {
        if (target == Job.BOWMAN)
            return createPlaceholder(name, Job.BOWMAN, null, 25, BotGuildCompanions.RECRUIT, 9300028, world, channel);
        Job second = Job.getById(target.getId() - 1);
        int id = createLpqPlaceholder(name, second, world, channel);
        var c = new BotClient(world, channel);
        try {
            var bot = Character.loadCharFromDB(id, c, true); c.setPlayer(bot); c.setAccID(bot.getAccountID());
            var e = new BotEntry(bot, bot, null);
            e.personality = BotPersonality.parse(BotConfigService.getInstance().load(id));
            e.apAuto = true; e.kpq.recruiting = true;
            var reference = server.life.LifeFactory.getMonster(9300028); // Ergoth, handbook/Mob.txt.
            e.aspirationalMobLevel = reference.getLevel(); e.aspirationalMobAvoid = reference.getAvoidability();
            if (target == Job.HERMIT) e.spVariant = "claw";
            while (bot.getLevel() < 100) {
                if (bot.getLevel() == 70) { bot.changeJob(target); BotBuildManager.handleJobAdvance(e, bot, second, target); }
                bot.levelUp(false); BotBuildManager.autoAssignSp(e, bot);
                BotBuildManager.maybeRecomputeAutonomousApBuild(e, bot); BotBuildManager.autoAssignAp(e, bot);
            }
            bot.saveCharToDB(true);
            try (var db = DatabaseConnection.getConnection(); var q = db.prepareStatement("UPDATE characters SET map=?,spawnpoint=0 WHERE id=?")) {
                q.setInt(1, BotGuildCompanions.RECRUIT); q.setInt(2, id); q.executeUpdate();
            }
            return id;
        } catch (java.sql.SQLException failure) { throw new IllegalStateException("Could not prepare Guild PQ fixture", failure); }
    }

    private static int createPlaceholder(String name, Job job, Job secondJob, int level,
                                         int spawnMap, int referenceMob, int world, int channel) {
        if (job != Job.WARRIOR && job != Job.MAGICIAN && job != Job.BOWMAN && job != Job.THIEF)
            throw new IllegalArgumentException("Choose an explorer first job");
        var result = BotGenerator.createBotCharacter(world, channel, name);
        if (!result.ok()) throw new IllegalStateException(result.error());
        var client = new BotClient(world, channel);
        try {
            Character bot = Character.loadCharFromDB(result.charId(), client, true);
            client.setPlayer(bot);
            client.setAccID(bot.getAccountID());
            var entry = new BotEntry(bot, bot, null);
            entry.apAuto = job != Job.WARRIOR;
            // Delay AP spending until the target level and PQ accuracy requirements are known.
            entry.kpq.recruiting = true;
            var accuracyReference = server.life.LifeFactory.getMonster(referenceMob);
            entry.aspirationalMobLevel = accuracyReference.getLevel();
            entry.aspirationalMobAvoid = accuracyReference.getAvoidability();
            Job next = switch (job) {
                case WARRIOR -> Job.SPEARMAN;
                case MAGICIAN -> Job.CLERIC;
                case BOWMAN -> Job.HUNTER;
                case THIEF -> Job.ASSASSIN;
                default -> throw new IllegalArgumentException();
            };
            if (secondJob != null) next = secondJob;
            entry.personality = BotPersonality.defaults().withPlannedJobs(job, next);
            if (job == Job.THIEF) entry.spVariant = "claw";
            int advanceLevel = job == Job.MAGICIAN ? 8 : 10;
            while (bot.getLevel() < advanceLevel) bot.levelUp(false);
            bot.changeJob(job);
            BotBuildManager.handleJobAdvance(entry, bot, Job.BEGINNER, job);
            while (bot.getLevel() < level) {
                if (secondJob != null && bot.getLevel() == 30 && bot.getJob() == job) {
                    bot.changeJob(secondJob);
                    BotBuildManager.handleJobAdvance(entry, bot, job, secondJob);
                }
                bot.levelUp(false);
                BotBuildManager.autoAssignSp(entry, bot);
                if (job != Job.WARRIOR) {
                    BotBuildManager.maybeRecomputeAutonomousApBuild(entry, bot);
                    BotBuildManager.autoAssignAp(entry, bot);
                }
            }
            entry.apAuto = true;
            BotBuildManager.maybeRecomputeAutonomousApBuild(entry, bot);
            BotBuildManager.autoAssignAp(entry, bot);
            BotConfigService.getInstance().save(bot.getId(), entry.personality.serialize());
            bot.saveCharToDB(true);
            // Initial saved spawn for these new, still-offline fixtures. Never an instance warp.
            try (var connection = DatabaseConnection.getConnection();
                 var statement = connection.prepareStatement("UPDATE characters SET map = ?, spawnpoint = 0 WHERE id = ?")) {
                statement.setInt(1, spawnMap);
                statement.setInt(2, bot.getId());
                statement.executeUpdate();
            }
            return bot.getId();
        } catch (java.sql.SQLException e) {
            throw new IllegalStateException("Could not prepare placeholder " + name, e);
        }
    }
}
