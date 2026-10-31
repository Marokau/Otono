package server.bots;
import client.Character;import server.maps.MapleMap;import server.life.Monster;import server.bots.combat.BotMobHitboxProvider;import java.awt.*;import java.util.List;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
class BotHorntailProjectilePrimaryTest {
 @Test void sharedAnchorDoesNotRetargetWingsToADeferredHand(){
  var b=mock(Character.class);var map=mock(MapleMap.class);var hand=mock(Monster.class);var wings=mock(Monster.class);var boxes=mock(BotMobHitboxProvider.class);when(b.getMap()).thenReturn(map);when(b.getPosition()).thenReturn(new Point(-399,20));when(b.getMapId()).thenReturn(240060200);when(hand.getId()).thenReturn(8810005);when(wings.getId()).thenReturn(8810007);when(map.getAllMonsters()).thenReturn(List.of(hand,wings));
  for(var m:List.of(hand,wings)){when(m.isAlive()).thenReturn(true);when(m.getPosition()).thenReturn(new Point(71,258));}when(boxes.getMobBounds(hand)).thenReturn(new Rectangle(-139,-63,95,141));when(boxes.getMobBounds(wings)).thenReturn(new Rectangle(-216,-82,574,179));
  try(var run=mockStatic(BotHorntailRun.class);var hits=mockStatic(BotMobHitboxProvider.class);var lanes=mockStatic(BotZakumArmLanes.class);var firing=mockStatic(BotHorntailFiringPosition.class)){
   run.when(()->BotHorntailRun.inInstance(b)).thenReturn(true);hits.when(BotMobHitboxProvider::getInstance).thenReturn(boxes);for(var m:List.of(hand,wings)){lanes.when(()->BotZakumArmLanes.canTarget(b,m)).thenReturn(true);firing.when(()->BotHorntailFiringPosition.allows(b,b.getPosition(),m)).thenReturn(true);}
   assertSame(wings,BotCombatManager.resolveEffectivePrimary(b,wings,new Rectangle(-394,-30,595,100)));
  }
 }
 @Test void pinkSharedAnchorCannotReplaceRexWithDeferredSolomon(){
  var b=mock(Character.class);var map=mock(MapleMap.class);var ev=mock(scripting.event.EventInstanceManager.class);var left=mock(Monster.class);var right=mock(Monster.class);var boxes=mock(BotMobHitboxProvider.class);when(b.getMap()).thenReturn(map);when(b.getPosition()).thenReturn(new Point(200,-42));when(b.getMapId()).thenReturn(270050100);when(b.getEventInstance()).thenReturn(ev);when(ev.getName()).thenReturn("PinkBeanTest");when(ev.getIntProperty("stage")).thenReturn(4);when(b.getJob()).thenReturn(client.Job.NIGHTLORD);when(left.getId()).thenReturn(8820003);when(right.getId()).thenReturn(8820004);when(map.getAllMonsters()).thenReturn(List.of(left,right));when(map.getMonsterById(8820003)).thenReturn(left);when(map.getMonsterById(8820004)).thenReturn(right);
  for(var m:List.of(left,right)){when(m.isAlive()).thenReturn(true);when(m.getPosition()).thenReturn(new Point(5,-43));when(boxes.getMobBounds(m)).thenReturn(new Rectangle(300,-100,100,100));}
  try(var run=mockStatic(BotPinkBeanRun.class,CALLS_REAL_METHODS);var hits=mockStatic(BotMobHitboxProvider.class);var lanes=mockStatic(BotZakumArmLanes.class)){run.when(()->BotPinkBeanRun.rightSide(null,b)).thenReturn(true);hits.when(BotMobHitboxProvider::getInstance).thenReturn(boxes);for(var m:List.of(left,right))lanes.when(()->BotZakumArmLanes.canTarget(b,m)).thenReturn(true);assertSame(right,BotCombatManager.resolveEffectivePrimary(b,left,new Rectangle(205,-100,595,100)));}
 }
}
