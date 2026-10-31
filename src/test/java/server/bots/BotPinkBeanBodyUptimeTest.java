package server.bots;
import client.Character;import client.Job;import java.awt.Point;import java.util.List;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
class BotPinkBeanBodyUptimeTest {
 @Test void aProtectedReturningThiefCanDispatchBodyAttacksFromEitherSide(){
  for(int x:new int[]{-200,200}){
   var b=mock(Character.class);var map=mock(server.maps.MapleMap.class);var mob=mock(server.life.Monster.class);var event=mock(scripting.event.EventInstanceManager.class);var plan=mock(BotCombatManager.AttackPlan.class);var e=new BotEntry(b,b,null);
   when(b.getMap()).thenReturn(map);when(b.getMapId()).thenReturn(270050100);when(b.getPosition()).thenReturn(new Point(x,-42));when(b.getJob()).thenReturn(Job.NIGHTLORD);when(b.isAlive()).thenReturn(true);when(b.getPartyMembersOnSameMap()).thenReturn(List.of(b));when(b.getEventInstance()).thenReturn(event);when(event.getName()).thenReturn("PinkBean2");when(event.getIntProperty("stage")).thenReturn(5);when(map.getAllMonsters()).thenReturn(List.of(mob));when(map.getMonsterById(8820001)).thenReturn(mob);when(mob.getId()).thenReturn(8820001);when(mob.isAlive()).thenReturn(true);when(mob.getPosition()).thenReturn(new Point(0,-42));
   try(var pink=mockStatic(BotPinkBeanRun.class,CALLS_REAL_METHODS);var physical=mockStatic(BotHorntailCombat.class,CALLS_REAL_METHODS);var combat=mockStatic(BotCombatManager.class);var damage=mockStatic(BotZakumCombat.class);var weapon=mockStatic(BotAttackExecutionProvider.class);var hold=mockStatic(BotOpqRun.class)){
    pink.when(()->BotPinkBeanRun.canTarget(b,mob)).thenReturn(true);physical.when(()->BotHorntailCombat.safe(eq(b),any(),anyList())).thenReturn(true);damage.when(()->BotZakumCombat.canDamage(b,mob)).thenReturn(true);weapon.when(()->BotAttackExecutionProvider.getEquippedWeaponType(b)).thenReturn(client.inventory.WeaponType.CLAW);combat.when(()->BotCombatManager.planAttack(e,b,mob)).thenReturn(plan);combat.when(()->BotCombatManager.isTargetInAttackRange(plan,b,mob)).thenReturn(true);combat.when(()->BotCombatManager.canUseAttackPlanNow(e,client.inventory.WeaponType.CLAW,plan)).thenReturn(true);hold.when(()->BotOpqRun.hold(e,b,true)).thenReturn(true);
    assertTrue(BotHorntailCombat.tick(e,b,true));combat.verify(()->BotCombatManager.attackMonster(e,b,plan));assertSame(mob,e.grindTarget);
   }
  }
 }
}
