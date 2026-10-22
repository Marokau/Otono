package server.bots;

import client.Character;
import client.Disease;
import client.inventory.InventoryType;
import constants.id.ItemId;
import net.server.channel.handlers.UseItemHandler;

/** Ordinary carried All Cures for LPQ's seal/darkness and other curable ailments. */
final class BotLpqCures {
    private BotLpqCures() {}

    static boolean tryCure(Character bot) {
        if (!BotPqCureDelay.ready(bot, System.currentTimeMillis())) return false;
        var potion = bot.getInventory(InventoryType.USE).findById(ItemId.ALL_CURE_POTION);
        return potion != null && potion.getQuantity() > 0
                && UseItemHandler.consumeUseItem(bot, potion.getPosition(), ItemId.ALL_CURE_POTION);
    }
}
