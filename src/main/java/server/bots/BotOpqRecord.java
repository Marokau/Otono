package server.bots;

import client.Character;
import constants.skills.Cleric;
import constants.skills.FPWizard;
import constants.skills.ILWizard;
import java.util.Calendar;
import java.util.Comparator;

/** A Teleport-capable courier fetches and offers the weekday record for any leader class. */
final class BotOpqRecord {
    private BotOpqRecord() {}
    static boolean isCourier(Character bot) {
        if (!BotOpqRun.inInstance(bot)) return false;
        var event = bot.getEventInstance();
        int record = 4001056 + Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        // Keep the courier who already picked it up; a late arrival must not steal the role.
        for (var member : event.getPlayers())
            if (member.getClient() instanceof client.BotClient && member.getMapId() == 920010400
                    && member.getItemQuantity(record, false) > 0) return member == bot;
        var courier = event.getPlayers().stream().filter(b -> b.getMapId() == 920010400 && b.getClient() instanceof client.BotClient && (b.getSkillLevel(Cleric.TELEPORT) > 0
                        || b.getSkillLevel(ILWizard.TELEPORT) > 0 || b.getSkillLevel(FPWizard.TELEPORT) > 0))
                .min(Comparator.comparingInt(Character::getId)).orElse(null);
        return courier == null ? event.isEventLeader(bot) : courier == bot;
    }
    static boolean work(BotEntry entry, Character bot, boolean ai) {
        if (!isCourier(bot)) return false;
        int record = 4001056 + Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        if (bot.getItemQuantity(record, false) > 0)
            return BotOpqRun.dropAt(entry, bot, bot.getMap().getReactorByName("music"), record, 1, ai);
        server.maps.MapItem nearest = null;
        double distance = Double.MAX_VALUE;
        for (var item : bot.getMap().getDroppedItems()) {
            double d = bot.getPosition().distanceSq(item.getPosition());
            if (!item.isPickedUp() && item.getItemId() == record && item.canBePickedBy(bot)
                    && System.currentTimeMillis() - item.getDropTime() >= 400 && d < distance) { nearest = item; distance = d; }
        }
        if (nearest == null) return false;
        BotZakumPqRun.stepTowardsAndPickUp(entry, bot, nearest, ai);
        return true;
    }
}
