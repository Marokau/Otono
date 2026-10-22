package server.bots;

import client.BotClient;
import client.Character;
import constants.string.CharsetConstants;
import io.netty.buffer.Unpooled;
import net.opcodes.SendOpcode;
import net.packet.ByteBufInPacket;
import net.packet.Packet;
import net.server.channel.handlers.NPCMoreTalkHandler;
import net.server.channel.handlers.NPCTalkHandler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** The headless client's view of ordinary NPC dialogue, and its player-handler inputs. */
public final class BotNpcInteraction {
    public record Message(int npc, int type, String text) {}

    private BotNpcInteraction() {}

    public static Message read(Packet packet) {
        byte[] bytes = packet.getBytes();
        if (bytes.length < 11) return null;
        ByteBuffer b = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        if (Short.toUnsignedInt(b.getShort()) != SendOpcode.NPC_TALK.getValue()) return null;
        b.get();
        int npc = b.getInt();
        int type = Byte.toUnsignedInt(b.get());
        b.get(); // speaker
        if (type != 0 && type != 1 && type != 4 && type != 12) return null;
        int length = Short.toUnsignedInt(b.getShort());
        if (length > b.remaining()) return null;
        byte[] text = new byte[length];
        b.get(text);
        return new Message(npc, type, new String(text, CharsetConstants.CHARSET));
    }

    public static boolean talk(Character bot, int npcId) {
        var npc = bot.getMap().getNPCById(npcId);
        if (npc == null) return false;
        return talkObject(bot, npc.getObjectId());
    }

    /** Books and other repeated NPC templates must be addressed by their visible object. */
    static boolean talkObject(Character bot, int objectId) {
        var buffer = Unpooled.buffer().writeIntLE(objectId);
        try {
            ((BotClient) bot.getClient()).npcMessage = null;
            new NPCTalkHandler().handlePacket(new ByteBufInPacket(buffer), bot.getClient());
            return ((BotClient) bot.getClient()).npcMessage != null;
        } finally { buffer.release(); }
    }

    public static void next(Character bot, int selection) {
        var message = ((BotClient) bot.getClient()).npcMessage;
        if (message == null || bot.getClient().getCM() == null) return;
        var buffer = Unpooled.buffer().writeByte(message.type()).writeByte(1).writeIntLE(selection);
        try {
            new NPCMoreTalkHandler().handlePacket(new ByteBufInPacket(buffer), bot.getClient());
        } finally { buffer.release(); }
    }
}
