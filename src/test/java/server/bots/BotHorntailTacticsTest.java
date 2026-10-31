package server.bots;
import client.Character;import server.life.Monster;import server.maps.MapleMap;import java.util.List;import org.junit.jupiter.api.Test;import static org.mockito.Mockito.*;import static org.junit.jupiter.api.Assertions.*;
class BotHorntailTacticsTest {
 @Test void allHandsWaitUntilOtherPartsAreDownEvenBelowThirtyPercent(){
  var b=mock(Character.class);var map=mock(MapleMap.class);var head=mock(Monster.class);var hand=mock(Monster.class);when(b.getMap()).thenReturn(map);when(b.getMapId()).thenReturn(240060200);when(head.getId()).thenReturn(8810003);when(head.isAlive()).thenReturn(true);when(hand.getId()).thenReturn(8810005);when(hand.getMaxHp()).thenReturn(230000000);when(hand.getHp()).thenReturn(230000000);when(map.getAllMonsters()).thenReturn(List.of(head,hand));
  try(var horn=mockStatic(BotHorntailRun.class)){horn.when(()->BotHorntailRun.inInstance(b)).thenReturn(true);assertEquals(List.of(head),BotHorntailTactics.targets(b,List.of(head,hand)));when(hand.getHp()).thenReturn(69000000);assertEquals(List.of(head),BotHorntailTactics.targets(b,List.of(head,hand)));when(hand.getHp()).thenReturn(230000000);when(head.isAlive()).thenReturn(false);assertEquals(List.of(hand),BotHorntailTactics.targets(b,List.of(hand)));}
 }
 @Test void otherEncountersKeepTheirTargetList(){var b=mock(Character.class);var hand=mock(Monster.class);var candidates=List.of(hand);assertSame(candidates,BotHorntailTactics.targets(b,candidates));}
 @Test void tailLegsWingsHeadsHandsPriorityAndLastHandsArePreserved(){
  var b=mock(Character.class);assertTrue(BotHorntailCombat.priority(b,8810009)<BotHorntailCombat.priority(b,8810008));assertTrue(BotHorntailCombat.priority(b,8810008)<BotHorntailCombat.priority(b,8810007));assertTrue(BotHorntailCombat.priority(b,8810007)<BotHorntailCombat.priority(b,8810003));assertTrue(BotHorntailCombat.priority(b,8810003)<BotHorntailCombat.priority(b,8810005));
  var map=mock(MapleMap.class);var wing=mock(Monster.class);var hand=mock(Monster.class);when(b.getMap()).thenReturn(map);when(b.getMapId()).thenReturn(240060200);when(wing.getId()).thenReturn(8810007);when(wing.isAlive()).thenReturn(true);when(hand.getId()).thenReturn(8810006);when(hand.getMaxHp()).thenReturn(230000000);when(hand.getHp()).thenReturn(230000000);when(map.getAllMonsters()).thenReturn(List.of(wing,hand));
  try(var horn=mockStatic(BotHorntailRun.class)){horn.when(()->BotHorntailRun.inInstance(b)).thenReturn(true);assertTrue(BotHorntailTactics.targets(b,List.of(hand)).isEmpty());when(wing.isAlive()).thenReturn(false);assertEquals(List.of(hand),BotHorntailTactics.targets(b,List.of(hand)));}
 }
}
