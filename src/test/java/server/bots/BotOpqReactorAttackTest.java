package server.bots;
import client.Character;
import client.BotClient;
import net.server.channel.handlers.AbstractDealDamageHandler;
import net.server.channel.handlers.CloseRangeDamageHandler;
import org.junit.jupiter.api.Test;
import server.maps.Reactor;
import server.maps.MapleMap;
import java.awt.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotOpqReactorAttackTest {
 @Test void reactorHitSendsANormalSwingAndUsesThePlayerReactorHandler() {
  var b=mock(Character.class);var client=new BotClient(0,1);client.setPlayer(b);when(b.getClient()).thenReturn(client);
  when(b.getPosition()).thenReturn(new Point(100,150));var map=mock(MapleMap.class);when(b.getMap()).thenReturn(map);
  var r=mock(Reactor.class);when(r.isActive()).thenReturn(true);when(r.getObjectId()).thenReturn(7);when(r.getPosition()).thenReturn(new Point(80,125));
  when(map.getReactorByOid(7)).thenReturn(r);var entry=new BotEntry(b,b,null);
  var data=new BotAttackExecutionProvider.BasicAttackData(new Rectangle(),0,14,14,"swingP1",128,6,200,750,BotCombatManager.AttackRoute.CLOSE);
  try(var attacks=mockStatic(BotAttackExecutionProvider.class);var effects=mockStatic(CloseRangeDamageHandler.class);var combat=mockStatic(BotCombatManager.class)) {
   attacks.when(()->BotAttackExecutionProvider.buildBasicAttackData(b,new Point(99,150))).thenReturn(data);
   assertTrue(BotOpqReactorAttack.hit(entry,b,r));
   var info=org.mockito.ArgumentCaptor.forClass(AbstractDealDamageHandler.AttackInfo.class);
   effects.verify(()->CloseRangeDamageHandler.applyCloseRangeEffects(info.capture(),eq(b),eq(client)));
   assertEquals(0,info.getValue().skill);assertEquals(0,info.getValue().numAttacked);assertTrue(info.getValue().targets.isEmpty());
   assertEquals(14,info.getValue().direction);assertEquals(128,info.getValue().stance);assertEquals(6,info.getValue().speed);
   assertEquals(750,entry.attackCooldownMs);assertEquals(-1,entry.facingDir);
   verify(r).hitReactor(true,0,(short)2,0,client);
   assertFalse(BotOpqReactorAttack.hit(entry,b,r));effects.verifyNoMoreInteractions();
  }
 }
 @Test void outOfReachCloudsCannotBeHitOrAnimatedRemotely() {
  var b=mock(Character.class);when(b.getPosition()).thenReturn(new Point());var r=mock(Reactor.class);
  when(r.isActive()).thenReturn(true);when(r.getPosition()).thenReturn(new Point(100,0));var entry=new BotEntry(b,b,null);
  try(var attacks=mockStatic(BotAttackExecutionProvider.class)) {
   assertFalse(BotOpqReactorAttack.hit(entry,b,r));attacks.verifyNoInteractions();
   when(r.getPosition()).thenReturn(new Point(0,-46));assertFalse(BotOpqReactorAttack.hit(entry,b,r));attacks.verifyNoInteractions();
  }
 }
 @Test void everyOpqStageRejectsGenericRecoveryTeleportWithoutChangingLpq()throws Exception {
  var b=mock(Character.class);var entry=new BotEntry(b,b,null);var manager=mock(BotManager.class,CALLS_REAL_METHODS);
  when(b.getEventInstance()).thenReturn(mock(scripting.event.EventInstanceManager.class));
  var method=BotManager.class.getDeclaredMethod("executeRecoveryTeleport",BotEntry.class,Character.class,Point.class);method.setAccessible(true);
  try(var physics=mockStatic(BotPhysicsEngine.class);var movement=mockStatic(BotMovementManager.class)) {
   for(int map:new int[]{920010000,920010100,920010200,920010700,920010800,920011300}) {
    when(b.getMapId()).thenReturn(map);assertEquals(false,method.invoke(manager,entry,b,new Point(300,-500)));
   }
   physics.verifyNoInteractions();movement.verifyNoInteractions();
  }
 }
}
