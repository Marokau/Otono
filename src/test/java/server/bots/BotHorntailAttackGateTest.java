package server.bots;
import client.Character;import client.Job;import server.life.Monster;import java.util.List;import org.junit.jupiter.api.Test;import static org.mockito.Mockito.*;import static org.junit.jupiter.api.Assertions.*;
class BotHorntailAttackGateTest {
 @Test void deferredHandIsRejectedBeforeNightLordFastPath(){
  var b=mock(Character.class);var hand=mock(Monster.class);var e=new BotEntry(b,b,null);when(b.getJob()).thenReturn(Job.NIGHTLORD);
  try(var tactics=mockStatic(BotHorntailTactics.class)){
   tactics.when(()->BotHorntailTactics.targets(b,List.of(hand))).thenReturn(List.of());assertNull(BotCombatManager.planAttack(e,b,hand));verify(b,never()).getJob();
  }
 }
 @Test void cachedOrCollateralPlanIsRejectedAtFinalDispatch(){
  var b=mock(Character.class);var hand=mock(Monster.class);var e=new BotEntry(b,b,null);var plan=new BotCombatManager.AttackPlan(4121007,30,6,null,List.of(hand),BotCombatManager.AttackRoute.RANGED,0,0,0,0,0,0,100,null);
  try(var arms=mockStatic(BotZakumArmLanes.class);var tactics=mockStatic(BotHorntailTactics.class)){
   arms.when(()->BotZakumArmLanes.filter(b,plan)).thenReturn(plan);tactics.when(()->BotHorntailTactics.targets(b,plan.targets)).thenReturn(List.of());BotCombatManager.attackMonster(e,b,plan);verify(hand,never()).isAlive();assertEquals(0,e.attackCooldownMs);
  }
 }
}
