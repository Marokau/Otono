package server.bots;
import client.Character;
import client.Job;
import net.packet.Packet;
import net.opcodes.SendOpcode;
import scripting.event.EventInstanceManager;
import server.maps.MapleMap;
import server.maps.Reactor;
import org.junit.jupiter.api.Test;
import java.nio.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotGuildStatuesTest {
 private Packet flash(int oid){var p=mock(Packet.class);when(p.getBytes()).thenReturn(ByteBuffer.allocate(14).order(ByteOrder.LITTLE_ENDIAN).putShort((short)SendOpcode.REACTOR_HIT.getValue()).putInt(oid).array());return p;}
 @Test void parsesOnlyReactorPackets(){assertEquals(42,BotGuildStatues.flashOid(flash(42)));var p=mock(Packet.class);when(p.getBytes()).thenReturn(new byte[2]);assertEquals(-1,BotGuildStatues.flashOid(p));}
 @Test void learnsVisibleOrderAndTracksRealHitsWithoutReadingSecretCombination(){
  var b=mock(Character.class);var owner=mock(Character.class);when(owner.getId()).thenReturn(7);when(b.getId()).thenReturn(8);
  when(b.getJob()).thenReturn(Job.PRIEST);when(b.getMapId()).thenReturn(990000300);
  var map=mock(MapleMap.class);when(b.getMap()).thenReturn(map);var event=mock(EventInstanceManager.class);when(b.getEventInstance()).thenReturn(event);
  when(event.getProperty("stage1status")).thenReturn("display");
  var r=mock(Reactor.class);when(r.getId()).thenReturn(9208000);when(map.getReactorByOid(anyInt())).thenReturn(r);
  var manager=mock(BotManager.class);var entry=new BotEntry(b,owner,null);when(manager.getEntryByBotCharId(8)).thenReturn(entry);
  try(var m=mockStatic(BotManager.class);var crew=mockStatic(BotGuildCompanions.class)){
   m.when(BotManager::getInstance).thenReturn(manager);crew.when(()->BotGuildCompanions.belongsTo(b,7)).thenReturn(true);
   for(int id:new int[]{15,11,19,13})BotGuildStatues.observe(b,flash(id));
   assertEquals(java.util.List.of(15,11,19,13),BotGuildStatues.state(event).seen);
   when(event.getProperty("stage1status")).thenReturn("active");when(r.isRecentHitFromAttack()).thenReturn(true);
   BotGuildStatues.observe(b,flash(19));assertEquals(0,BotGuildStatues.state(event).replay);
   BotGuildStatues.observe(b,flash(15));assertEquals(1,BotGuildStatues.state(event).replay);
   verify(event,never()).getProperty("stage1combo");verify(event,never()).getProperty("stage1guess");
  }
 }
 @Test void unrelatedCharacterCannotRecordFlashes(){var b=mock(Character.class);when(b.getMapId()).thenReturn(100000000);BotGuildStatues.observe(b,flash(1));verify(b,never()).getEventInstance();}
}
