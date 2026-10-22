package server.bots;

import client.Character;
import server.maps.MapleMap;
import java.util.*;

/** Opt-in knowledge of LPQ's fixed box route; never reads a randomized answer. */
final class BotLpqBoxHint {
    private BotLpqBoxHint() {}
    static boolean handle(BotEntry entry, String message) {
        String request=message.strip().toLowerCase(Locale.ROOT).replaceAll("[.!?]+$", "");
        if(!Set.of("box order","share box order","what is the box order").contains(request)) return false;
        Character human=entry.owner, bot=entry.bot;
        if(human==null || human.getMapId()!=922010600 || BotLpqHuman.leader(bot)!=human
                || bot.getEventInstance()==null || bot.getEventInstance()!=human.getEventInstance()) return false;
        // Owner chat reaches all companions, including those already waiting in stage seven.
        if(BotLpqHuman.isCollector(bot)) {
            BotLpqBoxReminder.requested(bot.getEventInstance());
            String order=order(human.getMap());
            human.dropMessage(6,bot.getName()+": From the bottom, take boxes " + order
                    + ". Each digit is one row, from the bottom up. We will wait for you at the top.");
        }
        return true;
    }
    static String order(MapleMap map) {
        // Fixed player-facing labels, confirmed by the owner and the map's box art.
        // Portal indices are positions within a row, not the printed box numbers.
        return "133 221 333 123 111";
    }
}
