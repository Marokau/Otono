package server.bots;
import client.Character;import client.BotClient;import net.packet.OutPacket;import net.opcodes.SendOpcode;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
class BotRaidAttackPacketsTest {
 @Test void onlyRaidMonsterMovementEscapesTheVisualOnlyFastPath(){
  var b=mock(Character.class);var move=OutPacket.create(SendOpcode.MOVE_MONSTER);var visual=OutPacket.create(SendOpcode.MOVE_PLAYER);
  assertFalse(BotRaidAttackPackets.required(b,move));when(b.getClient()).thenReturn(mock(BotClient.class));
  try(var horn=mockStatic(BotHorntailRun.class);var pink=mockStatic(BotPinkBeanRun.class)){
   assertFalse(BotRaidAttackPackets.required(b,move));horn.when(()->BotHorntailRun.inInstance(b)).thenReturn(true);assertTrue(BotRaidAttackPackets.required(b,move));assertFalse(BotRaidAttackPackets.required(b,visual));horn.when(()->BotHorntailRun.inInstance(b)).thenReturn(false);pink.when(()->BotPinkBeanRun.inInstance(b)).thenReturn(true);assertTrue(BotRaidAttackPackets.required(b,move));
  }
 }
}
