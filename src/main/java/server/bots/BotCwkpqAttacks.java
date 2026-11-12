package server.bots;

import client.Character;

/** Client-observed incoming attacks throughout the native CWKPQ instance. */
public final class BotCwkpqAttacks {
    private BotCwkpqAttacks() {}

    public static boolean inInstance(Character bot) {
        if (bot == null || bot.getEventInstance() == null) return false;
        int map = bot.getMapId();
        return map >= 610030100 && map <= 610030800;
    }
}
