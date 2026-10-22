package server.bots;
import client.Character;
import org.junit.jupiter.api.Test;
import server.maps.MapleMap;
import static org.mockito.Mockito.*;
class BotEnvironmentalPotionTest {
 @Test void environmentalDrainUsesOrdinaryBoundAutopotEvenBetweenInventoryScans() {
  var bot=mock(Character.class);var map=mock(MapleMap.class);when(bot.getMap()).thenReturn(map);
  when(map.getHPDec()).thenReturn(200);when(bot.isAlive()).thenReturn(true);
  var entry=new BotEntry(bot,bot,null);entry.potCheckTimerMs=5000;
  try(var pots=mockStatic(BotPotionManager.class,CALLS_REAL_METHODS)) {
   pots.when(()->BotPotionManager.potionEffectsReady(bot)).thenReturn(false);
   BotPotionManager.tickPotionCheck(entry,bot);
   verify(bot).triggerAutopotAfterStatLoss(-1,0);
  }
 }
 @Test void ordinaryMapsAndDeadCharactersDoNotTriggerEnvironmentalAutopot() {
  var bot=mock(Character.class);var map=mock(MapleMap.class);when(bot.getMap()).thenReturn(map);
  var entry=new BotEntry(bot,bot,null);
  try(var pots=mockStatic(BotPotionManager.class,CALLS_REAL_METHODS)) {
   pots.when(()->BotPotionManager.potionEffectsReady(bot)).thenReturn(false);
   BotPotionManager.tickPotionCheck(entry,bot);when(map.getHPDec()).thenReturn(200);
   BotPotionManager.tickPotionCheck(entry,bot);verify(bot,never()).triggerAutopotAfterStatLoss(anyInt(),anyInt());
  }
 }
}
