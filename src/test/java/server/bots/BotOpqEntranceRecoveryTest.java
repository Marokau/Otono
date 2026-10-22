package server.bots;
import client.Character;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import server.maps.MapleMap;
import server.maps.Portal;
import java.awt.Point;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotOpqEntranceRecoveryTest {
 @Test void aClearedEntranceStragglerReturnsToEakRatherThanClimbingToTheIsolatedExit()throws Exception {
  var b=mock(Character.class);var event=mock(EventInstanceManager.class);var map=mock(MapleMap.class);var exit=mock(Portal.class);
  when(b.getEventInstance()).thenReturn(event);when(event.getIntProperty("statusStg0")).thenReturn(1);
  when(b.getMap()).thenReturn(map);when(map.getPortal("in00")).thenReturn(exit);when(exit.getPosition()).thenReturn(new Point(497,-1280));
  when(b.getPosition()).thenReturn(new Point(-519,-1004));var e=new BotEntry(b,b,null);
  var clouds=BotOpqRun.class.getDeclaredMethod("clouds",BotEntry.class,Character.class,boolean.class);clouds.setAccessible(true);
  try(var run=mockStatic(BotOpqRun.class,CALLS_REAL_METHODS)) {
   run.when(()->BotOpqRun.talk(e,b,2013001,true)).thenReturn(true);
   assertEquals(true,clouds.invoke(null,e,b,true));run.verify(()->BotOpqRun.talk(e,b,2013001,true));
   run.verify(()->BotOpqRun.travel(e,b,BotOpqRun.HUB,true),never());
  }
 }
 @Test void aCompanionMayUseEaksClearedEntranceTransferButCannotCheckOtherStages() {
  var human=mock(Character.class);var b=mock(Character.class);when(b.getId()).thenReturn(1);var event=mock(EventInstanceManager.class);
  when(b.getMapId()).thenReturn(BotOpqRun.FIRST_MAP);when(b.getEventInstance()).thenReturn(event);when(event.getIntProperty("statusStg0")).thenReturn(1);
  BotOpqHuman.register(human,List.of(b));var e=new BotEntry(b,human,null);
  try(var run=mockStatic(BotOpqRun.class)) {
   assertFalse(BotOpqHuman.npc(e,b,2013001,true));
   when(b.getMapId()).thenReturn(920010500);run.when(()->BotOpqRun.hold(e,b,true)).thenReturn(true);
   assertTrue(BotOpqHuman.npc(e,b,2013001,true));
  }finally{BotOpqHuman.unregister(b);}
 }
 @Test void sameMapNpcTransferResetsTheOldRopePhysicsAtTheActualArrival() {
  var b=mock(Character.class);when(b.getMapId()).thenReturn(BotOpqRun.FIRST_MAP);var pos=new AtomicReference<>(new Point(-400,-200));
  when(b.getPosition()).thenAnswer(a->pos.get());var e=new BotEntry(b,b,null);var arrival=new Point(165,-1276);
  try(var run=mockStatic(BotOpqRun.class,CALLS_REAL_METHODS);var human=mockStatic(BotOpqHuman.class);
      var travel=mockStatic(BotTravelManager.class);var npc=mockStatic(BotNpcInteraction.class);
      var physics=mockStatic(BotPhysicsEngine.class);var movement=mockStatic(BotMovementManager.class)) {
   travel.when(()->BotTravelManager.tickApproachNpc(e,b,BotOpqRun.FIRST_MAP,2013001,2,true,false,150)).thenReturn(BotTravelManager.ApproachStatus.ARRIVED);
   run.when(()->BotOpqRun.hold(e,b,true)).thenReturn(true);run.when(()->BotOpqRun.ready(e)).thenReturn(true);
   when(b.getClient()).thenReturn(mock(client.BotClient.class));npc.when(()->BotNpcInteraction.talk(b,2013001)).thenAnswer(a->{pos.set(arrival);return true;});
   assertTrue(BotOpqRun.talk(e,b,2013001,true));
   physics.verify(()->BotPhysicsEngine.teleportTo(e,b,arrival));movement.verify(()->BotMovementManager.resetEntryStateAfterTeleport(e));
  }
 }
}
