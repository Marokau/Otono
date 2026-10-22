package server.bots;
import client.Character;
import client.BotClient;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import server.maps.MapleMap;
import server.maps.MapItem;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotOpqLoungeDropTest {
 @Test void originalRoom303RocksRemainForAnActiveCrewEvenWhileTheRoomIsEmpty() {
  var map=mock(MapleMap.class);when(map.getId()).thenReturn(920010603);var event=mock(EventInstanceManager.class);when(map.getEventInstance()).thenReturn(event);
  var bot=mock(Character.class);when(bot.getClient()).thenReturn(new BotClient(0,1));when(bot.getMapId()).thenReturn(920010600);when(bot.getEventInstance()).thenReturn(event);
  when(event.getPlayers()).thenReturn(List.of(bot));var drop=mock(MapItem.class);when(drop.getItemId()).thenReturn(4001052);
  assertTrue(BotPqDropPolicy.keepsForHandoff(map,drop));
  when(event.isEventCleared()).thenReturn(true);assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
 }
 @Test void loungeMesoMarkersStayVisibleAndBotsLeaveThemAlone() {
  var bot=mock(Character.class);var map=mock(MapleMap.class);when(map.getId()).thenReturn(920010600);when(bot.getMap()).thenReturn(map);when(bot.getMapId()).thenReturn(920010600);
  when(bot.getClient()).thenReturn(new BotClient(0,1));when(bot.getEventInstance()).thenReturn(mock(EventInstanceManager.class));
  var drop=mock(MapItem.class);when(drop.getMeso()).thenReturn(10);when(drop.isPlayerDrop()).thenReturn(true);when(drop.getDropper()).thenReturn(bot);when(drop.canBePickedBy(bot)).thenReturn(true);
  assertTrue(BotPqDropPolicy.keepsForHandoff(map,drop));assertFalse(BotLootEligibility.canBotLoot(new BotEntry(bot,bot,null),bot,drop));
  var activeEvent=bot.getEventInstance();when(map.getEventInstance()).thenReturn(activeEvent);when(bot.getMap()).thenReturn(mock(MapleMap.class));when(bot.getMapId()).thenReturn(920010603);
  assertTrue(BotPqDropPolicy.keepsForHandoff(map,drop));
 }
}
