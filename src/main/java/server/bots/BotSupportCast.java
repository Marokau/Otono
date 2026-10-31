package server.bots;
import client.Character;
import client.Skill;
import io.netty.buffer.Unpooled;
import net.packet.ByteBufInPacket;
import net.PacketProcessor;
/** One adapter for ordinary bot support-skill packets; player handlers own effects and costs. */
final class BotSupportCast {
 private BotSupportCast() {}
 static boolean dispatch(Character bot,Skill skill,int level) {
  var client=bot.getClient();if(client==null)return false;
  var bytes=BotCombatManager.buildSupportSpecialMovePacket(bot,skill.getId(),level,net.server.Server.getInstance().getCurrentTimestamp());
  var buffer=Unpooled.wrappedBuffer(bytes);
  try {var packet=new ByteBufInPacket(buffer);short opcode=packet.readShort();
   var handler=PacketProcessor.getProcessor(bot.getWorld(),client.getChannel()).getHandler(opcode);
   if(handler==null||!handler.validateState(client))return false;
   handler.handlePacket(packet,client);return true;
  }finally{buffer.release();}
 }
}
