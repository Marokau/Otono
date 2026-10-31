package server.bots;
import client.Character;
import client.inventory.WeaponType;
import java.awt.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import server.life.Monster;
import server.maps.*;
import server.bots.combat.BotMobHitboxProvider;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotHorntailPositioningTest {
 @Test void preliminaryMeleeUsesApproachSideInsteadOfThePlatformBehindTheHead(){
  var b=mock(Character.class);var map=mock(MapleMap.class);var portal=mock(Portal.class);var mob=mock(Monster.class);var boxes=mock(BotMobHitboxProvider.class);
  when(b.getMap()).thenReturn(map);when(b.getMapId()).thenReturn(240060000);when(map.getPortal("sp")).thenReturn(portal);when(portal.getPosition()).thenReturn(new Point(480,-40));when(mob.getId()).thenReturn(8810000);when(boxes.getMobBounds(mob)).thenReturn(new Rectangle(696,-222,224,271));
  try(var horn=mockStatic(BotHorntailRun.class);var hitboxes=mockStatic(BotMobHitboxProvider.class);var weapons=mockStatic(BotAttackExecutionProvider.class)){
   horn.when(()->BotHorntailRun.inInstance(b)).thenReturn(true);hitboxes.when(BotMobHitboxProvider::getInstance).thenReturn(boxes);weapons.when(()->BotAttackExecutionProvider.getEquippedWeaponType(b)).thenReturn(WeaponType.SPEAR_STAB);
   assertTrue(BotHorntailCombat.safe(b,new Point(556,-40),List.of(mob)));
   assertFalse(BotHorntailCombat.safe(b,new Point(943,-160),List.of(mob)));
   when(portal.getPosition()).thenReturn(new Point(1000,-40));when(b.getMapId()).thenReturn(240060100);
   assertTrue(BotHorntailCombat.safe(b,new Point(976,-160),List.of(mob)));
   assertFalse(BotHorntailCombat.safe(b,new Point(556,-40),List.of(mob)));
  }
 }
 @Test void meleeStanceIncludesTheSafeEndOfAShortPlatform(){
  var b=mock(Character.class);var map=mock(MapleMap.class);var mob=mock(Monster.class);var boxes=mock(BotMobHitboxProvider.class);var e=new BotEntry(b,b,null);
  when(b.getMap()).thenReturn(map);when(b.getPosition()).thenReturn(new Point(249,-40));when(map.getAllMonsters()).thenReturn(List.of(mob));when(mob.getId()).thenReturn(8810000);when(boxes.getMobBounds(mob)).thenReturn(new Rectangle(696,-222,224,271));
  var region=new BotNavigationGraph.Region(0,List.of(new BotNavigationGraph.Segment(new Foothold(new Point(540,-40),new Point(580,-40),1))));
  var real=spy(new BotNavigationGraph(240060000,1,List.of(region),java.util.Map.of(0,region),java.util.Map.of(1,0),java.util.Map.of(),java.util.Set.of()));
  var origin=b.getPosition();doReturn(0).when(real).findRegionId(map,origin);doReturn(true).when(real).canReach(0,0,0);
  try(var graphs=mockStatic(BotNavigationGraphProvider.class);var navigation=mockStatic(BotNavigationManager.class);var hitboxes=mockStatic(BotMobHitboxProvider.class);var weapons=mockStatic(BotAttackExecutionProvider.class);var damage=mockStatic(BotZakumCombat.class);var safety=mockStatic(BotHorntailCombat.class,CALLS_REAL_METHODS)){
   graphs.when(()->BotNavigationGraphProvider.peekGraph(map,e.movementProfile)).thenReturn(real);hitboxes.when(BotMobHitboxProvider::getInstance).thenReturn(boxes);weapons.when(()->BotAttackExecutionProvider.getEquippedWeaponType(b)).thenReturn(WeaponType.SWORD2H);damage.when(()->BotZakumCombat.canDamage(b,mob)).thenReturn(true);safety.when(()->BotHorntailCombat.safe(eq(b),any(Point.class),anyList())).thenReturn(true);
   assertEquals(new Point(572,-40),BotHorntailCombat.stance(e,b,List.of(mob)));
  }
 }
 @Test void rangedSupportKeepsClearanceEvenWhenTouchDamageWouldNotBeFatal(){
  var b=mock(Character.class);var mob=mock(Monster.class);var boxes=mock(BotMobHitboxProvider.class);when(b.getMapId()).thenReturn(240060200);when(mob.getId()).thenReturn(8810003);when(boxes.getMobBounds(mob)).thenReturn(new Rectangle(696,-222,224,271));
  try(var horn=mockStatic(BotHorntailRun.class);var hitboxes=mockStatic(BotMobHitboxProvider.class);var weapons=mockStatic(BotAttackExecutionProvider.class)){
   horn.when(()->BotHorntailRun.inInstance(b)).thenReturn(true);hitboxes.when(BotMobHitboxProvider::getInstance).thenReturn(boxes);weapons.when(()->BotAttackExecutionProvider.getEquippedWeaponType(b)).thenReturn(WeaponType.STAFF);
   assertTrue(BotHorntailCombat.safe(b,new Point(556,-40),List.of(mob)));
   assertFalse(BotHorntailCombat.safe(b,new Point(650,-40),List.of(mob)));
  }
 }
}
