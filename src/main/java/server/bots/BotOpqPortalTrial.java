package server.bots;
import client.Character;
import server.maps.Portal;

/** A client-visible verdict from actually using a portal, never from event answer properties. */
final class BotOpqPortalTrial {
    enum Outcome { APPROACHING, SUCCESS, WRONG }
    private BotOpqPortalTrial() {}
    static Outcome tick(BotEntry entry, Character bot, Portal portal, Portal success, boolean ai) {
        long before = entry.portalUseCooldownUntilMs;
        BotTravelManager.walkToPortalAndEnter(entry, bot, portal, System.currentTimeMillis(), ai);
        if (before == entry.portalUseCooldownUntilMs || success == null) return Outcome.APPROACHING;
        return bot.getPosition().distanceSq(success.getPosition()) <= 25 ? Outcome.SUCCESS : Outcome.WRONG;
    }
}
