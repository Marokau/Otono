package server.bots.pq;

import client.Character;
import server.bots.BotKpqRun;
import server.bots.BotEntry;


/**
 * Single call-site for all per-map party-quest bot automation.
 * BotManager calls {@link #tick} once per bot tick; each PQ class handles its own map range.
 */
public final class BotPqHooks {

    private BotPqHooks() {}

    /**
     * Returns true when a PQ machine consumed the whole bot tick (Zakum dead-mine run: the SSOT
     * {@link server.bots.BotZakumPqRun} drives the bot's movement/actions and the normal companion
     * AI must stand down. KPQ likewise owns its per-map intent and shares normal combat/movement.
     */
    public static boolean tick(BotEntry entry, Character bot, Character owner, boolean runAiTick) {
        server.bots.BotPqBossLoot.tick(entry, bot);
        if (server.bots.BotMvpqRun.tick(entry, bot, runAiTick)) return true;
        if (server.bots.BotGuildCompanions.tick(entry, bot, runAiTick)) return true;
        if (server.bots.BotCarnivalRun.tick(entry, bot, runAiTick)) return true;
        if (server.bots.BotEllinRun.tick(entry, bot, runAiTick)) return true;
        if (BotKpqRun.tick(entry, bot, runAiTick)) return true;
        if (server.bots.BotLpqRun.tick(entry, bot, runAiTick)) return true;
        if (server.bots.BotOpqRun.tick(entry, bot, runAiTick)) return true;
        if (server.bots.BotLmpqRun.tick(entry, bot, runAiTick)) return true;
        if (server.bots.BotMagatiaRun.tick(entry, bot, runAiTick)) return true;
        if (server.bots.BotZakumBattle.tick(entry, bot, runAiTick)) return true;
        return server.bots.BotZakumPqRun.tickSupervised(entry, bot, owner);
    }

    /**
     * Returns true when the bot should stand idle at an NPC and skip normal AI.
     */
    public static boolean isNpcLocked(BotEntry entry) {
        return false;
    }

    /** Returns true if the bot is in a PQ map that requires grind mode (KPQ stage 1). */
    public static boolean requiresGrind(BotEntry entry, Character bot) {
        return bot.getMapId() == BotKpqRun.FIRST_MAP && entry.kpq.state == 3;
    }

    /** True once the bot no longer needs coupons â€” suppress coupon loot. */
    public static boolean shouldSkipCouponLoot(BotEntry entry) {
        return entry.kpq.state >= 4;
    }

    /** Returns true if the bot is in a PQ map that should default to follow mode (KPQ stages 2-5). */
    public static boolean requiresFollow(BotEntry entry, Character bot) {
        int mapId = bot.getMapId();
        return mapId >= 103000801 && mapId <= 103000805;
    }
}
