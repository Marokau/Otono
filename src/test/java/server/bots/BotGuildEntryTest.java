package server.bots;
import client.Character;import client.Client;import server.maps.MapleMap;import server.life.NPC;import org.junit.jupiter.api.Test;import java.awt.Point;import java.util.*;
import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
class BotGuildEntryTest {
 @Test void crewMustActuallyReachShuangBeforeGatheringTimerStarts(){
  var leader=mock(Character.class);var map=mock(MapleMap.class);var npc=mock(NPC.class);var client=mock(Client.class);
  when(leader.getMap()).thenReturn(map);when(map.getNPCById(9040000)).thenReturn(npc);when(npc.getPosition()).thenReturn(new Point(100,100));
  when(leader.getGuildId()).thenReturn(5);when(leader.getClient()).thenReturn(client);when(client.getChannel()).thenReturn(1);
  var bots=new ArrayList<Character>();for(int i=0;i<5;i++){var b=mock(Character.class);when(b.getMapId()).thenReturn(BotGuildCompanions.RECRUIT);when(b.getGuildId()).thenReturn(5);when(b.getClient()).thenReturn(client);when(b.getPosition()).thenReturn(new Point(110,100));bots.add(b);}
  assertNull(BotGuildEntry.checkMembers(leader,bots));
  when(bots.getFirst().getPosition()).thenReturn(new Point(1000,100));assertNotNull(BotGuildEntry.checkMembers(leader,bots));
  when(bots.getFirst().getPosition()).thenReturn(new Point(110,100));when(bots.getFirst().getGuildId()).thenReturn(6);assertNotNull(BotGuildEntry.checkMembers(leader,bots));
  assertNotNull(BotGuildEntry.checkMembers(leader,bots.subList(0,4)));
 }
}
