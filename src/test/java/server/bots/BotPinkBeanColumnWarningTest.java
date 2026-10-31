package server.bots;
import org.junit.jupiter.api.Test;import server.maps.Foothold;import java.awt.*;import java.util.List;import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
class BotPinkBeanColumnWarningTest {
 private BotNavigationGraph.Region floor(int left,int right){return new BotNavigationGraph.Region(1,List.of(new BotNavigationGraph.Segment(new Foothold(new Point(left,-42),new Point(right,-42),1))));}
 @Test void adjacentWarningsRequireWalkingPastBothColumnsWithBodyClearance(){var columns=List.of(new Rectangle(-120,-442,120,400),new Rectangle(0,-442,120,400));var p=BotPinkBeanColumnWarning.gap(new Point(0,-42),columns,floor(-899,901));assertEquals(new Point(-150,-42),p);for(var column:columns)assertFalse(column.intersects(new Rectangle(p.x-12,p.y-50,24,50)));}
 @Test void aFullyCoveredNearbyFloorDoesNotInventAnEscapeOrRemoveTheAttack(){assertNull(BotPinkBeanColumnWarning.gap(new Point(0,-42),List.of(new Rectangle(-300,-442,600,400)),floor(-899,901)));}
 @Test void aPlatformEdgeCannotBeUsedAsAnImaginaryGap(){assertNull(BotPinkBeanColumnWarning.gap(new Point(0,-42),List.of(new Rectangle(-60,-442,120,400)),floor(-70,70)));}
 @Test void anAlreadySafePositionNeedsNoMovement(){assertNull(BotPinkBeanColumnWarning.gap(new Point(200,-42),List.of(new Rectangle(-60,-442,120,400)),floor(-899,901)));}
 @org.junit.jupiter.params.ParameterizedTest
 @org.junit.jupiter.params.provider.ValueSource(ints={0,2})
 void anAlreadyDodgedStunOrSeduceColumnKeepsTheBotInTheGapUntilImpact(int attackIndex){
  var b=mock(client.Character.class);var map=mock(server.maps.MapleMap.class);var mob=mock(server.life.Monster.class);when(b.getMap()).thenReturn(map);when(b.getPosition()).thenReturn(new Point(200,-42));when(mob.getId()).thenReturn(8820001);when(mob.isAlive()).thenReturn(true);var region=floor(-899,901);var graph=spy(new BotNavigationGraph(270050100,1,List.of(region),java.util.Map.of(1,region),java.util.Map.of(),java.util.Map.of(),java.util.Set.of()));doReturn(1).when(graph).findRegionId(any(),any());var e=new BotEntry(b,b,null);var attack=new BotMobAttackObserver.Attack(3,new Rectangle(-60,-442,120,400),0,1,20000,0,true,false);var cast=new BotMobAttackObserver.Cast(map,mob,attack,attackIndex,1,new Point(0,-42),System.currentTimeMillis()+10000);
  try(var pink=mockStatic(BotPinkBeanRun.class);var graphs=mockStatic(BotNavigationGraphProvider.class);var areas=mockStatic(BotMobAttackObserver.class);var hold=mockStatic(BotOpqRun.class);var hurricane=mockStatic(BotHurricane.class);var walk=mockStatic(BotPinkBeanMovement.class);var safe=mockStatic(BotHorntailCombat.class);var combat=mockStatic(BotCombatManager.class);var weapon=mockStatic(BotAttackExecutionProvider.class)){
   pink.when(()->BotPinkBeanRun.inInstance(b)).thenReturn(true);graphs.when(()->BotNavigationGraphProvider.peekGraph(map,e.movementProfile)).thenReturn(graph);areas.when(()->BotMobAttackObserver.areas(cast)).thenReturn(List.of(new Rectangle(-60,-442,120,400)));hold.when(()->BotOpqRun.hold(e,b,true)).thenReturn(true);BotPinkBeanColumnWarning.observe(b,cast);assertTrue(BotPinkBeanColumnWarning.tick(e,b,true));hold.verify(()->BotOpqRun.hold(e,b,true));walk.verifyNoInteractions();
   var event=mock(scripting.event.EventInstanceManager.class);when(b.getEventInstance()).thenReturn(event);when(event.getIntProperty("stage")).thenReturn(5);when(map.getMonsterById(8820001)).thenReturn(mob);when(map.getAllMonsters()).thenReturn(List.of(mob));pink.when(()->BotPinkBeanRun.canTarget(b,mob)).thenReturn(true);safe.when(()->BotHorntailCombat.safe(b,b.getPosition(),map.getAllMonsters())).thenReturn(true);var plan=mock(BotCombatManager.AttackPlan.class);combat.when(()->BotCombatManager.planAttack(e,b,mob)).thenReturn(plan);combat.when(()->BotCombatManager.isTargetInAttackRange(plan,b,mob)).thenReturn(true);weapon.when(()->BotAttackExecutionProvider.getEquippedWeaponType(b)).thenReturn(client.inventory.WeaponType.CLAW);combat.when(()->BotCombatManager.canUseAttackPlanNow(e,client.inventory.WeaponType.CLAW,plan)).thenReturn(true);
   assertTrue(BotPinkBeanColumnWarning.tick(e,b,true));combat.verify(()->BotCombatManager.attackMonster(e,b,plan));assertTrue(e.grinding);assertSame(mob,e.grindTarget);walk.verifyNoInteractions();
   combat.when(()->BotCombatManager.isTargetInAttackRange(plan,b,mob)).thenReturn(false);assertTrue(BotPinkBeanColumnWarning.tick(e,b,true));combat.verify(()->BotCombatManager.attackMonster(e,b,plan),times(1));walk.verifyNoInteractions();
  }
 }
 @Test void aBodyBlockedNearestGapDoesNotHideTheOtherSafeGap(){
  var b=mock(client.Character.class,RETURNS_DEEP_STUBS);when(b.getMap().getAllMonsters()).thenReturn(List.of());when(b.getMap().getMonsterById(8820001)).thenReturn(null);
  try(var safe=mockStatic(BotHorntailCombat.class)){
   safe.when(()->BotHorntailCombat.safe(eq(b),any(Point.class),anyList())).thenAnswer(c->((Point)c.getArgument(1)).x>=150);
   assertEquals(new Point(330,-42),BotPinkBeanDodgeGap.find(b,new Point(180,-42),List.of(new Rectangle(100,-442,200,400)),floor(-899,901)));
  }
 }
}
