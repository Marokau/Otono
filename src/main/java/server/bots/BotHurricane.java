package server.bots;

import client.Character;
import constants.skills.Bowmaster;
import java.awt.Point;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/** Held Hurricane has a start delay, then fixed-rate shots independent of bow speed. */
final class BotHurricane {
    // LazyBui's original pre-BB packet measurements: southperry.net/showthread.php?tid=3088.
    static final int SHOT_MS = 120;
    static final int START_MS = 300;
    private record Hold(int map, Point position, int facing, long ready, long last) {}
    private static final Map<Character, Hold> HOLDS = Collections.synchronizedMap(new WeakHashMap<>());
    private BotHurricane() {}

    static boolean ready(BotEntry entry, Character bot, BotCombatManager.AttackPlan plan, long now) {
        if (plan.skillId != Bowmaster.HURRICANE || !BotZakumBattle.inInstance(bot)) {
            cancel(bot);
            return true;
        }
        Hold old = HOLDS.get(bot);
        if (old == null || old.map != bot.getMapId() || !old.position.equals(bot.getPosition())
                || old.facing != plan.stance || now - old.last > START_MS + SHOT_MS + 250) {
            BotHurricaneVisual.refresh(bot,plan,now);
            HOLDS.put(bot, new Hold(bot.getMapId(), new Point(bot.getPosition()), plan.stance,
                    now + START_MS, now));
            entry.attackCooldownMs = Math.max(entry.attackCooldownMs, START_MS);
            return false;
        }
        if (now < old.ready) return false;
        BotHurricaneVisual.refresh(bot,plan,now);
        HOLDS.put(bot, new Hold(old.map, old.position, old.facing, old.ready, now));
        return true;
    }

    static void cancel(Character bot) { HOLDS.remove(bot); BotHurricaneVisual.stop(bot); }
}
