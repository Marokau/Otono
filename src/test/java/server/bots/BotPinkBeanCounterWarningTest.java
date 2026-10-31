package server.bots;
import client.Character;import server.life.*;import java.util.Set;import org.junit.jupiter.api.Test;import static org.mockito.Mockito.*;import static org.junit.jupiter.api.Assertions.*;
class BotPinkBeanCounterWarningTest {
 @Test void aRealCounterCastPausesOnlyMatchingDamageAndDoesNotCreateABuff()throws Exception{
  var b=mock(Character.class);var mob=mock(Monster.class);when(mob.getSkills()).thenReturn(Set.of(new MobSkillId(MobSkillType.MAGIC_COUNTER,1)));
  try(var db=mockStatic(tools.DatabaseConnection.class)){
   db.when(tools.DatabaseConnection::getConnection).thenReturn(mock(java.sql.Connection.class,RETURNS_DEEP_STUBS));
   BotPinkBeanCounterWarning.observe(b,mob,MobSkillType.MAGIC_COUNTER.getId(),1);long began=System.currentTimeMillis();
   assertFalse(BotPinkBeanCounterWarning.blocked(b,mob,false,began+300));assertTrue(BotPinkBeanCounterWarning.blocked(b,mob,true,began+300));assertFalse(BotPinkBeanCounterWarning.blocked(b,mob,true,began+100000));
  }
 }

 @Test void anUndeclaredCounterCannotInventAReflectionWarning(){var b=mock(Character.class);var mob=mock(Monster.class);when(mob.getSkills()).thenReturn(Set.of());BotPinkBeanCounterWarning.observe(b,mob,MobSkillType.PHYSICAL_AND_MAGIC_COUNTER.getId(),2);assertFalse(BotPinkBeanCounterWarning.blocked(b,mob,true,System.currentTimeMillis()+300));}

 @Test void theReceivedMonsterSkillPacketForwardsTheDeclaredCounterType(){var b=mock(Character.class);var map=mock(server.maps.MapleMap.class);var mob=mock(Monster.class);when(b.getMap()).thenReturn(map);when(map.getMapObject(31)).thenReturn(mob);when(mob.getId()).thenReturn(8820005);var packet=mock(net.packet.Packet.class);when(packet.getBytes()).thenReturn(java.nio.ByteBuffer.allocate(17).order(java.nio.ByteOrder.LITTLE_ENDIAN).putShort((short)net.opcodes.SendOpcode.MOVE_MONSTER.getValue()).putInt(31).put((byte)0).put((byte)0).put((byte)42).put((byte)MobSkillType.MAGIC_COUNTER.getId()).put((byte)1).putShort((short)0).putShort((short)5).putShort((short)-43).array());try(var pink=mockStatic(BotPinkBeanRun.class,CALLS_REAL_METHODS);var warning=mockStatic(BotPinkBeanCounterWarning.class)){pink.when(()->BotPinkBeanRun.inInstance(b)).thenReturn(true);BotMobAttackObserver.observe(b,packet);warning.verify(()->BotPinkBeanCounterWarning.observe(b,mob,MobSkillType.MAGIC_COUNTER.getId(),1));}}
}
