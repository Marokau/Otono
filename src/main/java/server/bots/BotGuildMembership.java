package server.bots;
import client.Character;
import tools.DatabaseConnection;
import java.util.HashSet;
/** Account for dormant dedicated fixtures already in this guild after an interrupted session. */
final class BotGuildMembership {
 private BotGuildMembership(){}
 static int reusableSeats(Character leader){
  var jobs=new HashSet<Integer>();
  try(var c=DatabaseConnection.getConnection();var q=c.prepareStatement(
   "SELECT c.id,c.job FROM bot_pq_companion p JOIN characters c ON c.id=p.bot_char_id JOIN accounts a ON a.id=c.accountid "
   +"WHERE p.pq='gpq' AND c.guildid=? AND c.world=? AND c.gm=0 AND a.loggedin=0 AND a.name=c.name "
   +"AND (c.map=101030104 OR c.map=200000301 OR c.map BETWEEN 990000000 AND 990001101) "
   +"AND ((c.job=300 AND c.level BETWEEN 20 AND 30) OR (c.job IN (131,231,311,411) AND c.level BETWEEN 100 AND 120)) "
   +"AND NOT EXISTS (SELECT 1 FROM managed_bot m WHERE m.bot_char_id=c.id) "
   +"AND NOT EXISTS (SELECT 1 FROM bot_owners o WHERE o.bot_char_id=c.id)")){
   q.setInt(1,leader.getGuildId());q.setInt(2,leader.getWorld());
   try(var rows=q.executeQuery()){while(rows.next())if(BotManager.getInstance().getEntryByBotCharId(rows.getInt("id"))==null)jobs.add(rows.getInt("job"));}
  }catch(java.sql.SQLException ex){org.slf4j.LoggerFactory.getLogger(BotGuildMembership.class).warn("Could not inspect dormant GPQ guild seats",ex);}
  return jobs.size();
 }
}
