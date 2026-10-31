package server.bots;
import client.Character;
import java.awt.Point;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotPinkBeanBodyRallyTest {
 @Test void rightUpperAttackersContinueWhileLeftBotsCrossAndOnlyFinishReserveWaits(){
  var b=mock(Character.class);var left=mock(Character.class);var human=mock(Character.class);var map=mock(server.maps.MapleMap.class);var ariel=mock(server.life.Monster.class);
  var c=new BotPinkBeanRun.Crew();c.leader=human;c.human=true;c.bots.add(b);c.bots.add(left);c.notices.add("body-rally");
  when(b.getMap()).thenReturn(map);when(b.getPosition()).thenReturn(new Point(350,-456));when(b.isAlive()).thenReturn(true);
  when(left.getMap()).thenReturn(map);when(left.getPosition()).thenReturn(new Point(-350,-456));when(left.isAlive()).thenReturn(true);
  when(human.isAlive()).thenReturn(true);when(human.getPosition()).thenReturn(new Point(-800,-42));
  when(map.getMonsterById(8820002)).thenReturn(ariel);when(ariel.isAlive()).thenReturn(true);when(ariel.getMaxHp()).thenReturn(600000000);when(ariel.getHp()).thenReturn(120000000);
  var e=new BotEntry(b,b,null);
  try(var pink=mockStatic(BotPinkBeanRun.class);var move=mockStatic(BotPinkBeanMovement.class);var hold=mockStatic(BotOpqRun.class)){
   pink.when(()->BotPinkBeanRun.crew(b)).thenReturn(c);hold.when(()->BotOpqRun.hold(e,b,true)).thenReturn(true);
   assertFalse(BotPinkBeanBodyRally.tick(e,b,true));move.verifyNoInteractions();hold.verifyNoInteractions();
   when(ariel.getHp()).thenReturn(20000000);assertTrue(BotPinkBeanBodyRally.tick(e,b,true));
   when(left.getPosition()).thenReturn(new Point(400,-456));assertFalse(BotPinkBeanBodyRally.tick(e,b,true));assertTrue(c.notices.contains("body-rally-ready"));
  }
 }
 @Test void onlyLeftMembersAreSentAcrossUsingOrdinaryMovement(){
  var b=mock(Character.class);var map=mock(server.maps.MapleMap.class);var ariel=mock(server.life.Monster.class);var c=new BotPinkBeanRun.Crew();c.leader=b;c.bots.add(b);c.notices.add("body-rally");
  when(b.getMap()).thenReturn(map);when(b.getPosition()).thenReturn(new Point(-350,-456));when(map.getMonsterById(8820002)).thenReturn(ariel);when(ariel.isAlive()).thenReturn(true);
  var e=new BotEntry(b,b,null);
  try(var pink=mockStatic(BotPinkBeanRun.class);var move=mockStatic(BotPinkBeanMovement.class)){
   pink.when(()->BotPinkBeanRun.crew(b)).thenReturn(c);assertTrue(BotPinkBeanBodyRally.tick(e,b,true));move.verify(()->BotPinkBeanMovement.walkTo(e,b,new Point(610,-42),true));
  }
 }
 @Test void actualTargetGateAllowsArielUntilFinishReserveRatherThanBlockingTheWholeRally(){
  var b=mock(Character.class);var mob=mock(server.life.Monster.class);var c=new BotPinkBeanRun.Crew();c.notices.add("body-rally");
  when(b.getJob()).thenReturn(client.Job.NIGHTLORD);when(mob.getId()).thenReturn(8820002);when(mob.getMaxHp()).thenReturn(600000000);when(mob.getHp()).thenReturn(120000000);
  try(var pink=mockStatic(BotPinkBeanRun.class,CALLS_REAL_METHODS)){
   pink.when(()->BotPinkBeanRun.inInstance(b)).thenReturn(true);pink.when(()->BotPinkBeanRun.crew(b)).thenReturn(c);
   assertTrue(BotPinkBeanRun.canTarget(b,mob));when(mob.getHp()).thenReturn(20000000);assertFalse(BotPinkBeanRun.canTarget(b,mob));
   c.notices.add("body-rally-ready");assertTrue(BotPinkBeanRun.canTarget(b,mob));
  }
 }

}
