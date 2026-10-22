package server.bots;

import client.Character;
import client.Job;
import tools.DatabaseConnection;

/** Persistent dedicated crew registry, separate from the autonomous population. */
final class BotKpqPool {
    private BotKpqPool() {}

    static int acquire(Character leader, Job job, String prefix) {
        try (var connection = DatabaseConnection.getConnection()) {
            try (var query = connection.prepareStatement("SELECT c.id FROM bot_kpq_companion p "
                    + "JOIN characters c ON c.id=p.bot_char_id JOIN accounts a ON a.id=c.accountid "
                    + "WHERE c.world=? AND c.level BETWEEN 21 AND 30 AND c.job DIV 100=? "
                    + "AND c.map IN (103000000,103000890) AND c.gm=0 AND a.loggedin=0 AND c.name LIKE ? "
                    + "AND NOT EXISTS (SELECT 1 FROM managed_bot m WHERE m.bot_char_id=c.id) "
                    + "AND NOT EXISTS (SELECT 1 FROM bot_owners o WHERE o.bot_char_id=c.id) "
                    + "ORDER BY CHAR_LENGTH(c.name),c.name")) {
                query.setInt(1, leader.getWorld());
                query.setInt(2, job.getId() / 100);
                query.setString(3, prefix + "%");
                try (var rows = query.executeQuery()) {
                    while (rows.next()) {
                        int id = rows.getInt(1);
                        if (BotManager.getInstance().getEntryByBotCharId(id) == null
                                && leader.getWorldServer().getPlayerStorage().getCharacterById(id) == null)
                            return id;
                    }
                }
            }
            int suffix = 1;
            while (Character.getIdByName(prefix + suffix) >= 0) suffix++;
            int id = BotPqCrewFactory.createKpqPlaceholder(prefix + suffix, job,
                    leader.getWorld(), leader.getClient().getChannel());
            try (var insert = connection.prepareStatement("INSERT INTO bot_kpq_companion (bot_char_id) VALUES (?)")) {
                insert.setInt(1, id);
                insert.executeUpdate();
            }
            return id;
        } catch (java.sql.SQLException e) {
            throw new IllegalStateException("Could not acquire a dedicated KPQ companion", e);
        }
    }
}
