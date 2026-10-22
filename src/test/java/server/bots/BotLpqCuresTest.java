package server.bots;

import client.Character;
import client.Disease;
import client.inventory.Inventory;
import client.inventory.InventoryType;
import client.inventory.Item;
import constants.id.ItemId;
import net.server.channel.handlers.UseItemHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotLpqCuresTest {
    @Test
    void sealUsesTheCarriedPotionThroughThePlayerItemHandler() {
        var bot = mock(Character.class);
        var inventory = mock(Inventory.class);
        var potion = mock(Item.class);
        when(bot.getDiseasesSize()).thenReturn(1);
        when(bot.hasDisease(Disease.SEAL)).thenReturn(true);
        when(bot.getInventory(InventoryType.USE)).thenReturn(inventory);
        when(inventory.findById(ItemId.ALL_CURE_POTION)).thenReturn(potion);
        when(potion.getQuantity()).thenReturn((short) 100);
        when(potion.getPosition()).thenReturn((short) 2);
        try (var items = mockStatic(UseItemHandler.class); var delay = mockStatic(BotPqCureDelay.class)) {
            delay.when(() -> BotPqCureDelay.ready(eq(bot), anyLong())).thenReturn(true);
            items.when(() -> UseItemHandler.consumeUseItem(bot, (short) 2, ItemId.ALL_CURE_POTION)).thenReturn(true);
            assertTrue(BotLpqCures.tryCure(bot));
            items.verify(() -> UseItemHandler.consumeUseItem(bot, (short) 2, ItemId.ALL_CURE_POTION));
        }
    }
    @Test
    void anUnrelatedStateDoesNotWasteAllCures() {
        var bot = mock(Character.class);
        when(bot.getDiseasesSize()).thenReturn(1);
        assertFalse(BotLpqCures.tryCure(bot));
        verify(bot, never()).getInventory(InventoryType.USE);
    }
}
