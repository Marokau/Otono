package server.bots;

import client.Character;
import client.Job;
import tools.DatabaseConnection;

/** Persistent dedicated LPQ fixtures, kept outside population scheduling and ownership. */
final class BotLpqPool {
    private BotLpqPool() {}

    static int acquire(Character leader, Job job, String prefix) {
        try (var connection = DatabaseConnection.getConnection()) {
            try (var query = connection.prepareStatement("SELECT c.id FROM bot_lpq_companion p "
                    + "JOIN characters c ON c.id=p.bot_char_id JOIN accounts a ON a.id=c.accountid "
                    + "WHERE c.world=? AND c.level BETWEEN 35 AND 50 AND c.job=? "
                    + "AND c.map=221024500 AND c.gm=0 AND a.loggedin=0 AND c.name LIKE ? "
                    + "AND NOT EXISTS (SELECT 1 FROM managed_bot m WHERE m.bot_char_id=c.id) "
                    + "AND NOT EXISTS (SELECT 1 FROM bot_owners o WHERE o.bot_char_id=c.id) "
                    + "ORDER BY CHAR_LENGTH(c.name),c.name")) {
                query.setInt(1, leader.getWorld());
                query.setInt(2, job.getId());
                query.setString(3, prefix + "%");
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
            int id = BotPqCrewFactory.createLpqPlaceholder(prefix + suffix, job,
                    leader.getWorld(), leader.getClient().getChannel());
            try (var insert = connection.prepareStatement("INSERT INTO bot_lpq_companion (bot_char_id) VALUES (?)")) {
                insert.setInt(1, id);
                insert.executeUpdate();
            }
            return id;
        } catch (java.sql.SQLException e) {
            throw new IllegalStateException("Could not acquire a dedicated LPQ companion", e);
        }
    }
}
