package server.bots;
import client.Character;
import client.BotClient;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import server.maps.MapleMap;
import server.maps.Portal;
import java.awt.Point;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotOpqPortalMarkersTest {
 @Test void dropsTenThroughThePlayerHandlerOnlyOnceForTheSharedRow() {
  var bot=mock(Character.class);var event=mock(EventInstanceManager.class);var map=mock(MapleMap.class);var portal=mock(Portal.class);
  when(bot.getMapId()).thenReturn(920010600);var pos=new Point(20,-100);when(bot.getEventInstance()).thenReturn(event);when(bot.getMap()).thenReturn(map);
  when(bot.getPosition()).thenReturn(pos);when(portal.getPosition()).thenReturn(pos);when(bot.isAlive()).thenReturn(true);
  var client=new BotClient(0,1);client.setPlayer(bot);when(bot.getClient()).thenReturn(client);
  var mesos=new AtomicInteger(160);when(bot.getMeso()).thenAnswer(a->mesos.get());
  doAnswer(a->{mesos.addAndGet(a.getArgument(0));return null;}).when(bot).gainMeso(anyInt(),anyBoolean(),anyBoolean(),anyBoolean());
  var entry=new BotEntry(bot,bot,null);
  try(var chat=mockStatic(BotPqChat.class)) {
   assertFalse(BotOpqPortalMarkers.beforeFollowing(entry,bot,portal,1,true));assertEquals(150,mesos.get());
   verify(map).spawnMesoDrop(10,pos,bot,bot,true,(byte)2,(short)0);
   assertFalse(BotOpqPortalMarkers.beforeFollowing(entry,bot,portal,1,true));assertEquals(150,mesos.get());
   var other=mock(Character.class);when(other.getEventInstance()).thenReturn(event);when(other.getMapId()).thenReturn(920010600);
   assertFalse(BotOpqPortalMarkers.beforeFollowing(new BotEntry(other,other,null),other,portal,1,true));
   verify(map,times(1)).spawnMesoDrop(10,pos,bot,bot,true,(byte)2,(short)0);
   when(bot.getMapId()).thenReturn(920010700);
   assertFalse(BotOpqPortalMarkers.beforeFollowing(entry,bot,portal,1,true));assertEquals(140,mesos.get());
   verify(map,times(2)).spawnMesoDrop(10,pos,bot,bot,true,(byte)2,(short)0);
  }
 }
 @Test void walksToThePortalBeforeDroppingInsteadOfPlacingMoneyRemotely() {
  var bot=mock(Character.class);var portal=mock(Portal.class);when(bot.getEventInstance()).thenReturn(mock(EventInstanceManager.class));
  when(bot.getMeso()).thenReturn(160);when(bot.getPosition()).thenReturn(new Point(0,0));var target=new Point(300,-200);when(portal.getPosition()).thenReturn(target);
  var entry=new BotEntry(bot,bot,null);
  try(var movement=mockStatic(BotZakumPqRun.class)) {
   assertTrue(BotOpqPortalMarkers.beforeFollowing(entry,bot,portal,1,true));
   movement.verify(()->BotZakumPqRun.walkTo(entry,bot,target,true));verify(bot,never()).gainMeso(anyInt(),anyBoolean(),anyBoolean(),anyBoolean());
  }
 }
 @Test void routeMarkersAreKeptAndCompanionsDoNotLootThem() {
  var bot=mock(Character.class);var map=mock(MapleMap.class);when(map.getId()).thenReturn(920010700);
  when(bot.getMapId()).thenReturn(920010700);when(bot.getMap()).thenReturn(map);
  when(bot.getClient()).thenReturn(new BotClient(0,1));when(bot.getEventInstance()).thenReturn(mock(EventInstanceManager.class));
  var drop=mock(server.maps.MapItem.class);when(drop.getMeso()).thenReturn(10);when(drop.isPlayerDrop()).thenReturn(true);
  when(drop.getDropper()).thenReturn(bot);when(drop.canBePickedBy(bot)).thenReturn(true);
  assertTrue(BotPqDropPolicy.keepsForHandoff(map,drop));
  assertFalse(BotLootEligibility.canBotLoot(new BotEntry(bot,bot,null),bot,drop));
  when(drop.getMeso()).thenReturn(11);assertFalse(BotPqDropPolicy.keepsForHandoff(map,drop));
 }}
