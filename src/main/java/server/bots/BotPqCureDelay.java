package server.bots;

import client.Character;
import client.Disease;
import java.util.*;

/** Keep curable ailments visible before All Cure: one second in Pink Bean, three elsewhere. */
final class BotPqCureDelay {
    private static final Map<Character, EnumMap<Disease,Long>> SEEN = new WeakHashMap<>();
    private BotPqCureDelay() {}
    static synchronized boolean ready(Character bot, long now) {
        if (bot.getDiseasesSize() == 0) { SEEN.remove(bot); return false; }
        var seen = SEEN.computeIfAbsent(bot, ignored -> new EnumMap<>(Disease.class));
        long latest = Long.MIN_VALUE;
        for (var disease : new Disease[]{Disease.SEAL,Disease.DARKNESS,Disease.POISON,Disease.WEAKEN,Disease.SLOW,Disease.CURSE}) {
            if (bot.hasDisease(disease)) {
                Long first = seen.get(disease);
                if (first == null) { first = now; seen.put(disease, first); }
                latest = Math.max(latest, first);
            } else seen.remove(disease);
        }
        if (seen.isEmpty()) { SEEN.remove(bot); return false; }
        return now - latest >= (BotPinkBeanRun.inInstance(bot) ? 1000 : 3000);
    }
}
