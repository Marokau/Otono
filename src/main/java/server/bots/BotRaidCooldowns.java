package server.bots;

import client.BotClient;
import client.Character;

/** Dedicated raid companions begin a prepared attempt with fresh skill cooldowns. */
final class BotRaidCooldowns {
    private BotRaidCooldowns() {}
    static void prepare(Character bot) {
        if (!(bot.getClient() instanceof BotClient) || bot.isGM() || bot.getEventInstance()!=null)
            throw new IllegalArgumentException("Reset dedicated raid companions outside their event");
        // Character stores Battleship hull HP under this pseudo-cooldown key.
        // Preserve that state; clear real skill cooldowns via the native API.
        bot.removeAllCooldownsExcept(5221999, true);
    }
}
