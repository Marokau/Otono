package server.bots;
import client.Character;import client.BotClient;import client.inventory.*;import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
class BotGuildEquipmentTest {
 @Test void earnedProtectorIsNotReplacedByStatGearDuringGpq(){
  var b=mock(Character.class);when(b.getClient()).thenReturn(new BotClient(0,1));when(b.getMapId()).thenReturn(990000300);
  when(b.getEventInstance()).thenReturn(mock(scripting.event.EventInstanceManager.class));
  var inv=mock(Inventory.class);when(b.getInventory(InventoryType.EQUIPPED)).thenReturn(inv);
  var item=mock(Item.class);when(item.getItemId()).thenReturn(1032033);when(inv.getItem((short)-4)).thenReturn(item);
  assertTrue(BotGuildEquipment.keepsProtection(b,(short)-4));assertFalse(BotGuildEquipment.keepsProtection(b,(short)-11));
  when(item.getItemId()).thenReturn(1032030);assertFalse(BotGuildEquipment.keepsProtection(b,(short)-4));
  when(item.getItemId()).thenReturn(1032033);when(b.getEventInstance()).thenReturn(null);assertFalse(BotGuildEquipment.keepsProtection(b,(short)-4));
 }
 @Test void unrelatedBotGearRemainsUnrestricted(){var b=mock(Character.class);when(b.getClient()).thenReturn(new BotClient(0,1));when(b.getMapId()).thenReturn(100000000);assertFalse(BotGuildEquipment.keepsProtection(b,(short)-4));}
}
