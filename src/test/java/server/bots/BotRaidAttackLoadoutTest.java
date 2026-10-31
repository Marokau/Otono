package server.bots;
import client.Character;import client.BotClient;import client.Job;import client.inventory.*;import org.junit.jupiter.api.Test;import static org.mockito.Mockito.*;import static org.junit.jupiter.api.Assertions.*;
class BotRaidAttackLoadoutTest {
 @Test void finishedAttackSetIsKeptOnlyWhileTheHpFloorIsMet(){
  var b=mock(Character.class,RETURNS_DEEP_STUBS);when(b.getClient()).thenReturn(mock(BotClient.class));when(b.getJob()).thenReturn(Job.NIGHTLORD);
  var glove=mock(Equip.class);when(glove.getItemId()).thenReturn(1082223);when(glove.getWatk()).thenReturn((short)16);var cape=mock(Equip.class);when(cape.getItemId()).thenReturn(1102206);when(cape.getWatk()).thenReturn((short)10);var shoe=mock(Equip.class);when(shoe.getItemId()).thenReturn(1072344);when(shoe.getWatk()).thenReturn((short)7);when(b.getEventInstance()).thenReturn(null);
  when(b.getInventory(InventoryType.EQUIPPED).getItem((short)-8)).thenReturn(glove);when(b.getInventory(InventoryType.EQUIPPED).getItem((short)-9)).thenReturn(cape);when(b.getInventory(InventoryType.EQUIPPED).getItem((short)-7)).thenReturn(shoe);
  try(var hp=mockStatic(BotRaidHpScrolls.class);var floor=mockStatic(BotRaidHp.class)){
   floor.when(()->BotRaidHp.minimumUnbuffed(b)).thenReturn(8400);hp.when(()->BotRaidHpScrolls.unbuffedHp(b)).thenReturn(8400);assertTrue(BotRaidAttackLoadout.ready(b));
   assertDoesNotThrow(()->BotRaidDamageEquipment.prepare(b)); // early return avoids catalog/optimizer initialization
   hp.when(()->BotRaidHpScrolls.unbuffedHp(b)).thenReturn(8399);assertFalse(BotRaidAttackLoadout.ready(b));hp.when(()->BotRaidHpScrolls.unbuffedHp(b)).thenReturn(8400);when(glove.getWatk()).thenReturn((short)15);assertFalse(BotRaidAttackLoadout.ready(b));
  }
 }
 @Test void humansAreNeverTreatedAsPreparedDedicatedBotGear(){assertFalse(BotRaidAttackLoadout.ready(mock(Character.class)));}
}
