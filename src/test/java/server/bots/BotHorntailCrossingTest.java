package server.bots;
import client.Character;import server.maps.MapleMap;import java.awt.Point;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
class BotHorntailCrossingTest {
 @Test void descendOutsideThenCrossBelowTheHeadsBeforeClimbing(){
  var b=mock(Character.class);var map=mock(MapleMap.class);when(b.getMap()).thenReturn(map);when(b.getMapId()).thenReturn(240060200);when(map.getPointBelow(any(Point.class))).thenAnswer(c->{Point p=c.getArgument(0);return new Point(p.x,260);});
  try(var horn=mockStatic(BotHorntailRun.class)){
   horn.when(()->BotHorntailRun.inInstance(b)).thenReturn(true);var goal=new Point(-374,-160);when(b.getPosition()).thenReturn(new Point(572,20));assertEquals(new Point(550,260),BotHorntailMovement.approach(b,goal));
   when(b.getPosition()).thenReturn(new Point(550,260));assertEquals(new Point(-374,260),BotHorntailMovement.approach(b,goal));
   when(b.getPosition()).thenReturn(new Point(-374,260));assertEquals(goal,BotHorntailMovement.approach(b,goal));
   when(b.getMapId()).thenReturn(270050100);assertEquals(goal,BotHorntailMovement.approach(b,goal));
  }
 }
 @Test void livingTailBlocksFloorCrossingButAllowsSameSideApproachAndReleasesAfterDeath(){
  var b=mock(Character.class);var map=mock(MapleMap.class);var tail=mock(server.life.Monster.class);var boxes=mock(server.bots.combat.BotMobHitboxProvider.class);
  when(b.getMap()).thenReturn(map);when(b.getMapId()).thenReturn(240060200);when(b.getPosition()).thenReturn(new Point(80,260));when(map.getMonsterById(8810009)).thenReturn(tail);when(tail.isAlive()).thenReturn(true);when(boxes.getMobBounds(tail)).thenReturn(new java.awt.Rectangle(191,120,307,142));when(map.getPointBelow(any(Point.class))).thenAnswer(c->{Point p=c.getArgument(0);return new Point(p.x,260);});
  try(var horn=mockStatic(BotHorntailRun.class);var hitboxes=mockStatic(server.bots.combat.BotMobHitboxProvider.class)){
   horn.when(()->BotHorntailRun.inInstance(b)).thenReturn(true);hitboxes.when(server.bots.combat.BotMobHitboxProvider::getInstance).thenReturn(boxes);
   assertEquals(new Point(80,260),BotHorntailMovement.approach(b,new Point(572,20)));
   assertEquals(new Point(120,260),BotHorntailMovement.approach(b,new Point(120,260)));
   when(b.getPosition()).thenReturn(new Point(550,260));assertEquals(new Point(550,260),BotHorntailMovement.approach(b,new Point(-374,-160)));
   when(b.getPosition()).thenReturn(new Point(400,20));assertEquals(new Point(400,20),BotHorntailMovement.approach(b,new Point(-374,-160)));
   when(b.getPosition()).thenReturn(new Point(80,260));when(tail.isAlive()).thenReturn(false);assertEquals(new Point(572,20),BotHorntailMovement.approach(b,new Point(572,20)));
  }
 }
 @Test void sameSideUpperRouteDoesNotBounceBetweenFloorStagingAndRopeLaunch(){
  var b=mock(Character.class);var map=mock(MapleMap.class);when(b.getMap()).thenReturn(map);when(b.getMapId()).thenReturn(240060200);when(map.getPointBelow(any(Point.class))).thenAnswer(c->{Point p=c.getArgument(0);return new Point(p.x,260);});
  try(var horn=mockStatic(BotHorntailRun.class)){
   horn.when(()->BotHorntailRun.inInstance(b)).thenReturn(true);var goal=new Point(392,-160);
   for(int x:new int[]{421,430,451,510}){when(b.getPosition()).thenReturn(new Point(x,260));assertEquals(goal,BotHorntailMovement.approach(b,goal));}
  }
 }
}
