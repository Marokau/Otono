package server.bots;

import client.BotClient;
import client.Character;
import client.Job;
import tools.DatabaseConnection;

/** Explicit PQ fixtures with per-PQ level ranges; never borrows population or owned bots. */
final class BotPqCompanionPool {
    private BotPqCompanionPool() {}
    static int acquire(Character leader, String pq, Job job, String prefix, int recruitMap) {
        if (!pq.equals("opq") && !pq.equals("lmpq") && !pq.equals("zakum") && !pq.equals("mpq") && !pq.equals("cpq") && !pq.equals("cpq2") && !pq.equals("epq")) throw new IllegalArgumentException(pq);
        String levels = pq.equals("epq") ? "44 AND 55" : pq.equals("cpq") ? "30 AND 50" : pq.equals("zakum") ? "150 AND 200" : pq.equals("mpq") ? "71 AND 85" : "51 AND 70";
        String savedMaps = pq.equals("zakum") ? "(c.map=? OR c.map BETWEEN 211041900 AND 211042400)"
                : pq.equals("cpq") ? "(c.map=? OR c.map=980000010)"
                : pq.equals("cpq2") ? "(c.map=? OR c.map=980030010)"
                : pq.equals("epq") ? "(c.map=? OR c.map=930000800)" : "c.map=?";
        try (var connection = DatabaseConnection.getConnection()) {
            try (var query = connection.prepareStatement("SELECT c.id FROM bot_pq_companion p "
                    + "JOIN characters c ON c.id=p.bot_char_id JOIN accounts a ON a.id=c.accountid "
                    + "WHERE p.pq=? AND c.world=? AND c.level BETWEEN " + levels + " AND c.job=? "
                    + "AND " + savedMaps + " AND c.gm=0 AND a.loggedin=0 AND c.name LIKE ? "
                    + "AND NOT EXISTS (SELECT 1 FROM managed_bot m WHERE m.bot_char_id=c.id) "
                    + "AND NOT EXISTS (SELECT 1 FROM bot_owners o WHERE o.bot_char_id=c.id) "
                    + "ORDER BY CHAR_LENGTH(c.name),c.name")) {
                query.setString(1, pq); query.setInt(2, leader.getWorld()); query.setInt(3, job.getId());
                query.setInt(4, recruitMap); query.setString(5, prefix + "%");
                try (var rows = query.executeQuery()) {
                    while (rows.next()) {
                        int id = rows.getInt(1);
                        if (BotManager.getInstance().getEntryByBotCharId(id) == null
                                && leader.getWorldServer().getPlayerStorage().getCharacterById(id) == null) return id;
                    }
                }
            }
            int suffix = 1;
            while (Character.getIdByName(prefix + suffix) >= 0) suffix++;
            if (pq.equals("zakum")) {
                int id = BotZakumCrewFactory.create(prefix + suffix, job, leader.getWorld(), leader.getClient().getChannel());
                try (var insert = connection.prepareStatement("INSERT INTO bot_pq_companion (bot_char_id,pq) VALUES (?,?)")) {
                    insert.setInt(1, id); insert.setString(2, pq); insert.executeUpdate();
                }
                return id;
            }
            Job secondJob = pq.equals("mpq") ? Job.getById(job.getId() - 1) : job;
            int id = BotPqCrewFactory.createLpqPlaceholder(prefix + suffix, secondJob,
                    leader.getWorld(), leader.getClient().getChannel());
            var client = new BotClient(leader.getWorld(), leader.getClient().getChannel());
            var bot = Character.loadCharFromDB(id, client, true);
            client.setPlayer(bot); client.setAccID(bot.getAccountID());
            var entry = new BotEntry(bot, bot, null);
            entry.personality = BotPersonality.parse(BotConfigService.getInstance().load(id));
            entry.apAuto = true; entry.kpq.recruiting = true;
            var reference = server.life.LifeFactory.getMonster(pq.equals("epq") ? 9300182 : pq.equals("cpq2") ? 9300316 : pq.equals("cpq") ? 9300127 : pq.equals("mpq") ? 9300140 : pq.equals("lmpq") ? 9400218 : 9300039);
            entry.aspirationalMobLevel = reference.getLevel(); entry.aspirationalMobAvoid = reference.getAvoidability();
            if (job == Job.ASSASSIN || job == Job.HERMIT) entry.spVariant = "claw";
            while (bot.getLevel() < (pq.equals("epq") ? 45 : pq.equals("cpq2") ? 55 : pq.equals("cpq") ? 40 : pq.equals("mpq") ? 71 : 51)) {
                if (pq.equals("mpq") && bot.getLevel() == 70) {
                    bot.changeJob(job); BotBuildManager.handleJobAdvance(entry, bot, secondJob, job);
                }
                bot.levelUp(false); BotBuildManager.autoAssignSp(entry, bot);
                BotBuildManager.maybeRecomputeAutonomousApBuild(entry, bot); BotBuildManager.autoAssignAp(entry, bot);
            }
            bot.saveCharToDB(true);
            // Initial saved spawn of a NEW offline fixture, never an instance or existing-player warp.
            try (var save = connection.prepareStatement("UPDATE characters SET map=?,spawnpoint=0 WHERE id=?")) {
                save.setInt(1, recruitMap); save.setInt(2, id); save.executeUpdate();
            }
            try (var insert = connection.prepareStatement("INSERT INTO bot_pq_companion (bot_char_id,pq) VALUES (?,?)")) {
                insert.setInt(1, id); insert.setString(2, pq); insert.executeUpdate();
            }
            return id;
        } catch (java.sql.SQLException e) { throw new IllegalStateException("Could not acquire a dedicated PQ companion", e); }
    }
}
