package server.bots;
import client.Character;import server.maps.MapleMap;import java.awt.Point;import java.util.List;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
class BotHorntailRopeIntentTest {
 @Test void activeSupportApproachSurvivesCombatHandoffOnRopeAndInAir(){
  var b=mock(Character.class);var map=mock(MapleMap.class);var e=new BotEntry(b,b,null);when(b.getMap()).thenReturn(map);when(map.getAllMonsters()).thenReturn(List.of());e.moveTarget=new Point(598,20);
  try(var horn=mockStatic(BotHorntailRun.class);var movement=mockStatic(BotPinkBeanMovement.class)){
   horn.when(()->BotHorntailRun.inInstance(b)).thenReturn(true);e.climbing=true;
   assertTrue(BotHorntailCombat.tick(e,b,true));movement.verify(()->BotPinkBeanMovement.walkTo(e,b,new Point(598,20),true));
   movement.clearInvocations();e.climbing=false;e.inAir=true;
   assertTrue(BotHorntailCombat.tick(e,b,false));movement.verify(()->BotPinkBeanMovement.walkTo(e,b,new Point(598,20),false));assertEquals(new Point(598,20),e.moveTarget);
  }
 }
}
