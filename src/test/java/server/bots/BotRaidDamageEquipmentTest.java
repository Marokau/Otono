package server.bots;
import client.Job;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BotRaidDamageEquipmentTest {
 @Test void geltsReplacePhysicalApplesButNeverTheBishopsMagicAttackBuff() {
  for(var job:java.util.List.of(Job.NIGHTLORD,Job.BOWMASTER,Job.DARKKNIGHT,Job.HERO))
   assertEquals(2022121,BotRaidDamageEquipment.attackConsumable(job));
  assertEquals(2022179,BotRaidDamageEquipment.attackConsumable(Job.BISHOP));
  var data=provider.DataProviderFactory.getDataProvider(provider.wz.WZFiles.ITEM).getData("Consume/0202.img");
  assertEquals(120,provider.DataTool.getInt("02022121/spec/pad",data,0));
  assertEquals(0,provider.DataTool.getInt("02022121/spec/mad",data,0));
  assertEquals(100,provider.DataTool.getInt("02022179/spec/mad",data,0));
 }
 @Test void finishedPreparationClearsOnlySpareEquipmentAndTheReplacedAttackPotion() {
  var b=org.mockito.Mockito.mock(client.Character.class,org.mockito.Mockito.RETURNS_DEEP_STUBS);
  org.mockito.Mockito.when(b.getClient()).thenReturn(org.mockito.Mockito.mock(client.BotClient.class));
  org.mockito.Mockito.when(b.getJob()).thenReturn(Job.NIGHTLORD);
  var spare=org.mockito.Mockito.mock(client.inventory.Equip.class);
  org.mockito.Mockito.when(spare.getPosition()).thenReturn((short)3);org.mockito.Mockito.when(spare.getQuantity()).thenReturn((short)1);
  var apple=new client.inventory.Item(2022179,(short)7,(short)20);
  org.mockito.Mockito.when(b.getInventory(client.inventory.InventoryType.EQUIP).list()).thenReturn(java.util.List.of(spare));
  org.mockito.Mockito.when(b.getInventory(client.inventory.InventoryType.USE).list()).thenReturn(java.util.List.of(apple));
  try(var inventory=org.mockito.Mockito.mockStatic(client.inventory.manipulator.InventoryManipulator.class);var stock=org.mockito.Mockito.mockStatic(BotZakumSupplies.class)) {
   BotRaidDamageEquipment.supplies(b);
   inventory.verify(()->client.inventory.manipulator.InventoryManipulator.removeFromSlot(b.getClient(),client.inventory.InventoryType.EQUIP,(short)3,(short)1,false));
   inventory.verify(()->client.inventory.manipulator.InventoryManipulator.removeFromSlot(b.getClient(),client.inventory.InventoryType.USE,(short)7,(short)20,false));
   inventory.verifyNoMoreInteractions();
   stock.verify(()->BotZakumSupplies.stock(b,2022121,20));
   org.mockito.Mockito.verify(b,org.mockito.Mockito.never()).getInventory(client.inventory.InventoryType.EQUIPPED);
   org.mockito.Mockito.verify(b,org.mockito.Mockito.never()).getInventory(client.inventory.InventoryType.ETC);
  }
 }
}
