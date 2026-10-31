package server.bots;
import client.Character;
import client.BotClient;
import net.packet.Packet;
import net.opcodes.SendOpcode;
/** Monster movement carries collision input even when no human renders the raid. */
public final class BotRaidAttackPackets {
 private BotRaidAttackPackets() {}
 public static boolean required(Character source,Packet packet){
  if(source==null||!(source.getClient() instanceof BotClient))return false;
  if(!BotHorntailRun.inInstance(source)&&!BotPinkBeanRun.inInstance(source))return false;
  byte[] bytes=packet.getBytes();
  if(bytes.length<2||((bytes[0]&255)|((bytes[1]&255)<<8))!=SendOpcode.MOVE_MONSTER.getValue())return false;
  return true;
 }
}
