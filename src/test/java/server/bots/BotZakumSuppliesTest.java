package server.bots;

import client.Character;
import client.BotClient;
import client.inventory.Inventory;
import client.inventory.InventoryType;
import client.inventory.WeaponType;
import client.inventory.manipulator.InventoryManipulator;
import org.junit.jupiter.api.Test;
import server.ItemInformationProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotZakumSuppliesTest {
    @org.junit.jupiter.api.BeforeAll
    static void initializeItemProviderWithoutDatabase() throws Exception {
        var connection=mock(java.sql.Connection.class);
        var statement=mock(java.sql.PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(mock(java.sql.ResultSet.class));
        try(var database=mockStatic(tools.DatabaseConnection.class)) {
            database.when(tools.DatabaseConnection::getConnection).thenReturn(connection);
            Class.forName("server.ItemInformationProvider");
        }
    }
    @Test void repeatedRetryRestockTopsUpArrowsAndPotionsWithoutDuplicatingStock() {
        var bot=mock(Character.class);var botClient=mock(BotClient.class);
        when(bot.getClient()).thenReturn(botClient);when(bot.getJob()).thenReturn(client.Job.BOWMASTER);
        var use=mock(Inventory.class);when(bot.getInventory(InventoryType.USE)).thenReturn(use);
        when(use.list()).thenReturn(List.of());
        Map<Integer,Integer> stock=new HashMap<>(Map.of(2060000,7000,2002023,17,2050004,5,4001017,1));
        when(bot.getItemQuantity(anyInt(),eq(false))).thenAnswer(call->stock.getOrDefault(call.getArgument(0),0));
        var ii=mock(ItemInformationProvider.class);when(ii.getSlotMax(eq(botClient),anyInt())).thenReturn((short)2000);
        try(var items=mockStatic(ItemInformationProvider.class);var inventory=mockStatic(InventoryManipulator.class);
                var companions=mockStatic(BotZakumCompanions.class);var attacks=mockStatic(BotAttackExecutionProvider.class)) {
            items.when(ItemInformationProvider::getInstance).thenReturn(ii);
            attacks.when(()->BotAttackExecutionProvider.getEquippedWeaponType(bot)).thenReturn(WeaponType.BOW);
            inventory.when(()->InventoryManipulator.addById(eq(botClient),anyInt(),anyShort())).thenAnswer(call->{
                int item=call.getArgument(1);short amount=call.getArgument(2);assertTrue(amount>0&&amount<=2000);
                stock.merge(item,(int)amount,Integer::sum);return true;
            });
            var entry=new BotEntry(bot,null,null);
            BotZakumSupplies.restock(entry,bot);BotZakumSupplies.restock(entry,bot);
            assertEquals(12000,stock.get(2060000));assertEquals(600,stock.get(2002023));
            assertEquals(100,stock.get(2050004));assertEquals(5,stock.get(4001017));
            inventory.verify(()->InventoryManipulator.addById(eq(botClient),anyInt(),anyShort()),times(6));
        }
    }

    @Test void rechargeableStarsAreGrantedInLegalStacks() {
        var bot=mock(Character.class);var botClient=mock(BotClient.class);when(bot.getClient()).thenReturn(botClient);
        when(bot.getItemQuantity(2070006,false)).thenReturn(2300);
        var ii=mock(ItemInformationProvider.class);when(ii.getSlotMax(botClient,2070006)).thenReturn((short)2000);
        try(var items=mockStatic(ItemInformationProvider.class);var inventory=mockStatic(InventoryManipulator.class)) {
            items.when(ItemInformationProvider::getInstance).thenReturn(ii);
            inventory.when(()->InventoryManipulator.addById(eq(botClient),eq(2070006),anyShort())).thenReturn(true);
            BotZakumSupplies.stock(bot,2070006,12000);
            inventory.verify(()->InventoryManipulator.addById(botClient,2070006,(short)2000),times(4));
            inventory.verify(()->InventoryManipulator.addById(botClient,2070006,(short)1700));
        }
    }

    @Test void activeFightCannotBeRestocked() {
        var bot=mock(Character.class);when(bot.getClient()).thenReturn(mock(BotClient.class));
        when(bot.getEventInstance()).thenReturn(mock(scripting.event.EventInstanceManager.class));
        assertThrows(IllegalArgumentException.class,()->BotZakumSupplies.restock(new BotEntry(bot,null,null),bot));
    }
}
