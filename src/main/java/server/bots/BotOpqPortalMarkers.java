package server.bots;

import client.Character;
import io.netty.buffer.Unpooled;
import net.packet.ByteBufInPacket;
import net.server.channel.handlers.MesoDropHandler;
import scripting.event.EventInstanceManager;
import server.maps.Portal;
import java.util.*;

/** A teammate marks an observed correct portal before physically following it. */
final class BotOpqPortalMarkers {
    private static final Map<EventInstanceManager, Set<String>> MARKED = Collections.synchronizedMap(new WeakHashMap<>());
    private BotOpqPortalMarkers() {}
    static boolean beforeFollowing(BotEntry entry, Character bot, Portal portal, int row, boolean ai) {
        var marked = MARKED.computeIfAbsent(bot.getEventInstance(), ignored -> new HashSet<>());
        String key = bot.getMapId() + ":" + row;
        synchronized (marked) {
            if (marked.contains(key) || bot.getMeso() < 10) return false;
            var position = bot.getPosition(); var target = portal.getPosition();
            if (entry.inAir || entry.climbing || Math.abs(position.x-target.x)>18 || Math.abs(position.y-target.y)>45) {
                BotZakumPqRun.walkTo(entry, bot, target, ai);
                return true;
            }
            int before = bot.getMeso();
            var packet = Unpooled.buffer().writeIntLE(0).writeIntLE(10);
            try { new MesoDropHandler().handlePacket(new ByteBufInPacket(packet), bot.getClient()); }
            finally { packet.release(); }
            if (bot.getMeso() == before-10) {
                marked.add(key);
                if (bot.getMapId() != 920010700)
                    BotPqChat.queue(entry, "I marked the working portal on row " + row + " with 10 mesos.", true);
            }
            return false;
        }
    }
}
