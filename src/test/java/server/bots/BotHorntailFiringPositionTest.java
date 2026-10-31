package server.bots;
import client.Character;import client.inventory.WeaponType;import server.life.Monster;import server.bots.combat.BotMobHitboxProvider;import java.awt.*;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
class BotHorntailFiringPositionTest {
 @Test void rangedHandsRequireSameSideAndHorizontalFiringHeight(){
  var b=mock(Character.class);var m=mock(Monster.class);var boxes=mock(BotMobHitboxProvider.class);when(b.getMapId()).thenReturn(240060200);when(m.getId()).thenReturn(8810006);when(m.getPosition()).thenReturn(new Point(71,258));when(boxes.getMobBounds(m)).thenReturn(new Rectangle(184,-63,97,138));
  try(var run=mockStatic(BotHorntailRun.class);var weapons=mockStatic(BotAttackExecutionProvider.class);var hits=mockStatic(BotMobHitboxProvider.class)){
   run.when(()->BotHorntailRun.inInstance(b)).thenReturn(true);weapons.when(()->BotAttackExecutionProvider.getEquippedWeaponType(b)).thenReturn(WeaponType.CLAW);hits.when(BotMobHitboxProvider::getInstance).thenReturn(boxes);
   assertFalse(BotHorntailFiringPosition.allows(b,new Point(-100,20),m));assertTrue(BotHorntailFiringPosition.allows(b,new Point(430,20),m));assertFalse(BotHorntailFiringPosition.allows(b,new Point(430,150),m));
   when(m.getId()).thenReturn(8810005);when(boxes.getMobBounds(m)).thenReturn(new Rectangle(-139,-63,95,141));assertFalse(BotHorntailFiringPosition.allows(b,new Point(200,20),m));assertTrue(BotHorntailFiringPosition.allows(b,new Point(-350,20),m));
  }
 }
 @Test void preliminaryHeadDoesNotUseExtremeGenericProjectileRange(){
  var b=mock(Character.class);var m=mock(Monster.class);var boxes=mock(BotMobHitboxProvider.class);when(b.getMapId()).thenReturn(240060100);when(m.getId()).thenReturn(8810001);when(boxes.getMobBounds(m)).thenReturn(new Rectangle(-378,-218,218,266));
  try(var run=mockStatic(BotHorntailRun.class);var weapons=mockStatic(BotAttackExecutionProvider.class);var hits=mockStatic(BotMobHitboxProvider.class)){
   run.when(()->BotHorntailRun.inInstance(b)).thenReturn(true);weapons.when(()->BotAttackExecutionProvider.getEquippedWeaponType(b)).thenReturn(WeaponType.CLAW);hits.when(BotMobHitboxProvider::getInstance).thenReturn(boxes);
   assertFalse(BotHorntailFiringPosition.allows(b,new Point(431,-40),m));assertTrue(BotHorntailFiringPosition.allows(b,new Point(-17,-40),m));
   weapons.when(()->BotAttackExecutionProvider.getEquippedWeaponType(b)).thenReturn(WeaponType.SPEAR_STAB);assertTrue(BotHorntailFiringPosition.allows(b,new Point(431,-40),m));
   run.when(()->BotHorntailRun.inInstance(b)).thenReturn(false);assertTrue(BotHorntailFiringPosition.allows(b,new Point(431,-40),m));
  }
 }
 @Test void upperRightHeadUsesNativeProjectileHeightWhileKeepingDistanceAndSideLimits(){
  var b=mock(Character.class);var m=mock(Monster.class);var boxes=mock(BotMobHitboxProvider.class);
  when(b.getMapId()).thenReturn(240060200);when(b.getPosition()).thenReturn(new Point(430,-220));
  when(m.getId()).thenReturn(8810004);when(m.getPosition()).thenReturn(new Point(71,258));
  when(boxes.getMobBounds(m)).thenReturn(new Rectangle(203,-216,88,117));
  try(var run=mockStatic(BotHorntailRun.class);var weapons=mockStatic(BotAttackExecutionProvider.class);var hits=mockStatic(BotMobHitboxProvider.class)){
   run.when(()->BotHorntailRun.inInstance(b)).thenReturn(true);
   weapons.when(()->BotAttackExecutionProvider.getEquippedWeaponType(b)).thenReturn(WeaponType.CLAW);
   hits.when(BotMobHitboxProvider::getInstance).thenReturn(boxes);
   assertTrue(BotHorntailFiringPosition.allows(b,new Point(430,-220),m));
   assertFalse(BotHorntailFiringPosition.allows(b,new Point(430,20),m));
   assertFalse(BotHorntailFiringPosition.allows(b,new Point(-335,-160),m));
   assertFalse(BotHorntailFiringPosition.allows(b,new Point(670,-220),m));
  }
 }
}
