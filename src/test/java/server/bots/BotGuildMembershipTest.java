package server.bots;
import client.Character;import tools.DatabaseConnection;import org.junit.jupiter.api.Test;import java.sql.*;import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
class BotGuildMembershipTest {
 @Test void dormantSeatsCountDistinctRequiredClassesAndExcludeActiveCompanions()throws Exception{
  var leader=mock(Character.class);when(leader.getGuildId()).thenReturn(9);var db=mock(Connection.class);var q=mock(PreparedStatement.class);var r=mock(ResultSet.class);
  when(db.prepareStatement(anyString())).thenReturn(q);when(q.executeQuery()).thenReturn(r);
  int[] jobs={231,231,411,311,131,300};var row=new AtomicInteger(-1);when(r.next()).thenAnswer(i->row.incrementAndGet()<jobs.length);
  when(r.getInt("job")).thenAnswer(i->jobs[row.get()]);when(r.getInt("id")).thenAnswer(i->100+row.get());
  var manager=mock(BotManager.class);when(manager.getEntryByBotCharId(105)).thenReturn(mock(BotEntry.class));
  try(var database=mockStatic(DatabaseConnection.class);var bm=mockStatic(BotManager.class)){
   database.when(DatabaseConnection::getConnection).thenReturn(db);bm.when(BotManager::getInstance).thenReturn(manager);
   assertEquals(4,BotGuildMembership.reusableSeats(leader));verify(q).setInt(1,9);
  }
 }
}
