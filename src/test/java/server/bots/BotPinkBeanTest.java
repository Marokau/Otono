package server.bots;
import client.Character;
import client.*;
import client.status.MonsterStatus;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import server.life.Monster;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class BotPinkBeanTest {
 private Character raider(Job job){var b=mock(Character.class);var event=mock(EventInstanceManager.class);when(b.getEventInstance()).thenReturn(event);when(event.getName()).thenReturn("PinkBean1");when(b.getMapId()).thenReturn(270050100);when(b.getJob()).thenReturn(job);return b;}
 @Test void dormantBeanAndStatueDecorationsAreNeverAttackTargets(){var b=raider(Job.NIGHTLORD);var mob=mock(Monster.class);for(int id:new int[]{8820000,8820008,8820014,8820019,8820020,8820023,8820026}){when(mob.getId()).thenReturn(id);assertFalse(BotPinkBeanRun.canTarget(b,mob),"id="+id);}for(int id:new int[]{8820001,8820002,8820006,8820007}){when(mob.getId()).thenReturn(id);assertTrue(BotPinkBeanRun.canTarget(b,mob),"id="+id);}}
 @Test void reflectStopsMatchingDamageTypeWithoutChangingOtherMaps(){var b=raider(Job.NIGHTLORD);var mob=mock(Monster.class);when(mob.getId()).thenReturn(8820001);when(mob.isBuffed(MonsterStatus.WEAPON_REFLECT)).thenReturn(true);assertFalse(BotPinkBeanRun.canTarget(b,mob));when(b.getJob()).thenReturn(Job.BISHOP);assertTrue(BotPinkBeanRun.canTarget(b,mob));when(mob.isBuffed(MonsterStatus.MAGIC_REFLECT)).thenReturn(true);assertFalse(BotPinkBeanRun.canTarget(b,mob));when(b.getMapId()).thenReturn(100000000);assertTrue(BotPinkBeanRun.canTarget(b,mob));}
 @Test void zombiePartyMemberSuppressesHealInsteadOfRiskingHarm(){var b=raider(Job.BISHOP);var mate=mock(Character.class);when(b.getPartyMembersOnSameMap()).thenReturn(List.of(b,mate));assertTrue(BotPinkBeanRun.safeToHeal(b));when(mate.hasDisease(Disease.ZOMBIFY)).thenReturn(true);assertFalse(BotPinkBeanRun.safeToHeal(b));assertFalse(BotCombatManager.tickSupportHealing(new BotEntry(b,b,null),b));when(b.getMapId()).thenReturn(100000000);assertTrue(BotPinkBeanRun.safeToHeal(b));}
 @Test void readinessUsesPinkBeansActualAccuracyRequirement(){var b=mock(Character.class);try(var prep=mockStatic(BotHorntailCompanions.class);var supplies=mockStatic(BotZakumSupplies.class);var rolled=mockStatic(BotPinkBeanEquipment.class);var weapons=mockStatic(BotPinkBeanWeapons.class);var damage=mockStatic(BotRaidDamageEquipment.class)){BotPinkBeanCompanions.prepare(b);prep.verify(()->BotHorntailCompanions.prepareRaid(b,8820001));supplies.verify(()->BotZakumSupplies.stock(b,4032002,1));damage.verify(()->BotRaidDamageEquipment.prepare(b));damage.verify(()->BotRaidDamageEquipment.supplies(b));supplies.verify(()->BotZakumSupplies.stock(b,2002023,2000));}}
 @Test void supplyAwardsNeverTargetHumanClients(){var b=mock(Character.class,RETURNS_DEEP_STUBS);assertThrows(IllegalArgumentException.class,()->BotPinkBeanCompanions.prepare(b));}
 @Test void seduceDisallowsPotionsInThisExpeditionToo(){var b=raider(Job.HERO);when(b.hasDisease(Disease.SEDUCE)).thenReturn(true);assertFalse(BotHorntailSeduce.canUsePotions(b));when(b.hasDisease(Disease.SEDUCE)).thenReturn(false);assertTrue(BotHorntailSeduce.canUsePotions(b));}
 @Test void aimingUsesActualStatueBoundsRatherThanItsSharedOrigin(){var b=raider(Job.NIGHTLORD);var mob=mock(Monster.class);when(mob.getId()).thenReturn(8820003);when(mob.getPosition()).thenReturn(new java.awt.Point(5,-43));var boxes=mock(server.bots.combat.BotMobHitboxProvider.class);when(boxes.getMobBounds(mob)).thenReturn(new java.awt.Rectangle(-737,-537,120,438));try(var provider=mockStatic(server.bots.combat.BotMobHitboxProvider.class)){provider.when(server.bots.combat.BotMobHitboxProvider::getInstance).thenReturn(boxes);assertEquals(new java.awt.Point(-677,-99),BotPinkBeanTargets.aim(b,mob));when(b.getMapId()).thenReturn(100000000);assertEquals(new java.awt.Point(5,-43),BotPinkBeanTargets.aim(b,mob));}}
 @Test void raisedPlatformApproachWalksUnderTheStatueBeforeJumping(){var b=raider(Job.BOWMASTER);when(b.getMap()).thenReturn(mock(server.maps.MapleMap.class));var e=new BotEntry(b,b,null);when(b.getPosition()).thenReturn(new java.awt.Point(-801,-42));var goal=new java.awt.Point(-530,-99);try(var walk=mockStatic(BotZakumPqRun.class)){BotPinkBeanMovement.walkTo(e,b,goal,true);walk.verify(()->BotZakumPqRun.walkTo(e,b,new java.awt.Point(-530,-42),true));when(b.getPosition()).thenReturn(new java.awt.Point(-530,-42));BotPinkBeanMovement.walkTo(e,b,goal,true);walk.verify(()->BotZakumPqRun.walkTo(e,b,goal,true));}}
 @Test void humanAssemblyPreservesTemplePrerequisites(){var b=mock(Character.class,RETURNS_DEEP_STUBS);when(b.getMapId()).thenReturn(270050000);when(b.getLevel()).thenReturn(180);when(b.getEventInstance()).thenReturn(null);try(var supplies=mockStatic(BotZakumSupplies.class)){assertTrue(BotPinkBeanCompanions.assemble(b).contains("Human prerequisites still apply"));supplies.verifyNoInteractions();verify(b,never()).levelUp(anyBoolean());}}
 @Test void reflectionNeverMakesTheStatuesBodyHarmless(){var b=raider(Job.NIGHTLORD);var mob=mock(Monster.class);var map=mock(server.maps.MapleMap.class);when(b.getMap()).thenReturn(map);when(b.getHp()).thenReturn(8000);when(b.getPosition()).thenReturn(new java.awt.Point(-650,-100));when(mob.getId()).thenReturn(8820003);when(mob.isAlive()).thenReturn(true);when(mob.isBuffed(MonsterStatus.WEAPON_REFLECT)).thenReturn(true);when(map.getAllMonsters()).thenReturn(List.of(mob));var e=new BotEntry(b,b,null);try(var combat=mockStatic(BotCombatManager.class,CALLS_REAL_METHODS)){combat.when(()->BotCombatManager.isMobTouchingBot(any(),eq(mob))).thenReturn(true);combat.when(()->BotCombatManager.applyMobHit(e,b,mob)).thenAnswer(call->null);assertFalse(BotPinkBeanRun.canTarget(b,mob));BotCombatManager.tickMobDamage(e,b);combat.verify(()->BotCombatManager.applyMobHit(e,b,mob));}}
 @Test void passiveLootLeavesTheNativeReactorItemAndOriginalRewardsAlone(){var b=raider(Job.NIGHTLORD);var e=new BotEntry(b,b,null);var d=mock(server.maps.MapItem.class);when(d.canBePickedBy(b)).thenReturn(true);var c=new BotPinkBeanRun.Crew();c.leader=b;try(var runs=mockStatic(BotPinkBeanRun.class,CALLS_REAL_METHODS)){runs.when(()->BotPinkBeanRun.crew(b)).thenReturn(c);when(d.getItemId()).thenReturn(4001193);when(d.isPlayerDrop()).thenReturn(true);assertFalse(BotLootEligibility.canBotLoot(e,b,d));when(d.getItemId()).thenReturn(0);when(d.getMeso()).thenReturn(100);when(d.isPlayerDrop()).thenReturn(false);assertFalse(BotLootEligibility.canBotLoot(e,b,d));when(d.isPlayerDrop()).thenReturn(true);assertTrue(BotLootEligibility.canBotLoot(e,b,d));}}
 @Test void headlessCompanionsWaitForLeaderLootBeforeDroppingBelowNativeMinimum()throws Exception{
  var b=mock(Character.class,RETURNS_DEEP_STUBS);var leader=mock(Character.class,RETURNS_DEEP_STUBS);var event=mock(EventInstanceManager.class);var map=mock(server.maps.MapleMap.class);var c=new BotPinkBeanRun.Crew();c.leader=leader;c.bots.add(b);c.clearedAt=System.currentTimeMillis()-60000;
  when(b.getEventInstance()).thenReturn(event);when(b.getMapId()).thenReturn(270050100);when(b.getMap()).thenReturn(map);when(leader.getMap()).thenReturn(map);when(event.getName()).thenReturn("PinkBean1");when(event.isEventCleared()).thenReturn(true);when(event.getPlayers()).thenReturn(List.of(b,leader));
  var field=BotPinkBeanRun.class.getDeclaredField("RUNS");field.setAccessible(true);var runs=(java.util.Map<Character,BotPinkBeanRun.Crew>)field.get(null);runs.put(b,c);var e=new BotEntry(b,leader,null);
  try(var run=mockStatic(BotPinkBeanRun.class,CALLS_REAL_METHODS);var hold=mockStatic(BotOpqRun.class)){
   hold.when(()->BotOpqRun.hold(e,b,true)).thenReturn(true);assertTrue(BotPinkBeanRun.tick(e,b,true));run.verify(()->BotPinkBeanRun.talk(any(),any(),any(),anyInt(),anyInt(),anyBoolean()),never());
  }finally{runs.remove(b);}
 }
 @Test void returningForSupportDescendsOutsideTheBirdBeforeCrossing(){
  var b=raider(Job.BISHOP);var map=mock(server.maps.MapleMap.class);when(b.getMap()).thenReturn(map);when(b.getPosition()).thenReturn(new java.awt.Point(-513,-99));when(map.getMonsterById(8820005)).thenReturn(mock(Monster.class));var e=new BotEntry(b,b,null);var goal=new java.awt.Point(-129,-42);
  try(var walking=mockStatic(BotZakumPqRun.class)){BotPinkBeanMovement.walkTo(e,b,goal,true);walking.verify(()->BotZakumPqRun.walkTo(e,b,new java.awt.Point(-600,-42),true));when(b.getPosition()).thenReturn(new java.awt.Point(-600,-42));BotPinkBeanMovement.walkTo(e,b,goal,true);walking.verify(()->BotZakumPqRun.walkTo(e,b,goal,true));}
 }

 @Test void companionsOutsideWaitInsteadOfRejoiningAfterTheirLeaderLeaves()throws Exception{
  var b=mock(Character.class,RETURNS_DEEP_STUBS);var leader=mock(Character.class,RETURNS_DEEP_STUBS);var expedition=mock(server.expeditions.Expedition.class);var c=new BotPinkBeanRun.Crew();c.leader=leader;c.bots.add(b);when(b.getMapId()).thenReturn(270050000);when(leader.getMapId()).thenReturn(270050000);when(b.getClient().getChannelServer().getExpedition(server.expeditions.ExpeditionType.PINKBEAN)).thenReturn(expedition);when(expedition.isInProgress()).thenReturn(true);
  var field=BotPinkBeanRun.class.getDeclaredField("RUNS");field.setAccessible(true);var runs=(java.util.Map<Character,BotPinkBeanRun.Crew>)field.get(null);runs.put(b,c);var e=new BotEntry(b,leader,null);
  try(var run=mockStatic(BotPinkBeanRun.class,CALLS_REAL_METHODS);var hold=mockStatic(BotOpqRun.class)){hold.when(()->BotOpqRun.hold(e,b,true)).thenReturn(true);assertTrue(BotPinkBeanRun.tick(e,b,true));run.verify(()->BotPinkBeanRun.talk(any(),any(),any(),anyInt(),anyInt(),anyBoolean()),never());}finally{runs.remove(b);}
 }

 @Test void savedTempleReturnFixturesAreReusedInsteadOfCreatingAnotherBatch()throws Exception{
  var leader=mock(Character.class,RETURNS_DEEP_STUBS);var manager=mock(BotManager.class);var connection=mock(java.sql.Connection.class);var query=mock(java.sql.PreparedStatement.class);var rows=mock(java.sql.ResultSet.class);when(connection.prepareStatement(anyString())).thenReturn(query);when(query.executeQuery()).thenReturn(rows);when(rows.next()).thenReturn(true,false);when(rows.getInt(1)).thenReturn(727);when(leader.getWorldServer().getPlayerStorage().getCharacterById(727)).thenReturn(null);
  try(var database=mockStatic(tools.DatabaseConnection.class);var managers=mockStatic(BotManager.class)){database.when(tools.DatabaseConnection::getConnection).thenReturn(connection);managers.when(BotManager::getInstance).thenReturn(manager);assertEquals(727,BotPqCompanionPool.acquire(leader,"pinkbean",Job.BISHOP,"PinkBishop",270050000));verify(connection).prepareStatement(argThat(sql->sql.contains("c.map=270000000")&&sql.contains("FROM managed_bot")&&sql.contains("FROM bot_owners")));}
 }

 @Test void theBirdsInnerPlatformEntryIsReplacedByAnOuterFiringPosition(){
  var b=raider(Job.NIGHTLORD);var map=mock(server.maps.MapleMap.class);when(b.getMap()).thenReturn(map);when(b.getPosition()).thenReturn(new java.awt.Point(-445,-42));when(map.getMonsterById(8820005)).thenReturn(mock(Monster.class));var e=new BotEntry(b,b,null);
  try(var walk=mockStatic(BotZakumPqRun.class)){BotPinkBeanMovement.walkTo(e,b,new java.awt.Point(-445,-99),true);walk.verify(()->BotZakumPqRun.walkTo(e,b,new java.awt.Point(-530,-42),true));when(b.getPosition()).thenReturn(new java.awt.Point(-530,-42));BotPinkBeanMovement.walkTo(e,b,new java.awt.Point(-445,-99),true);walk.verify(()->BotZakumPqRun.walkTo(e,b,new java.awt.Point(-530,-99),true));}
 }

 @Test void pinkBeanAndHorntailPotionsTopUpWithoutChangingOrdinaryMaps()throws Exception{
  var b=raider(Job.NIGHTLORD);var choice=new BotPotionManager.AutopotChoice(2002023,null,2002023,null);var setup=BotPotionManager.class.getDeclaredMethod("setupAutopotForBot",Character.class,BotPotionManager.AutopotChoice.class);setup.setAccessible(true);setup.invoke(null,b,choice);verify(b).setAutopotHpAlert(0.99f);clearInvocations(b);when(b.getMapId()).thenReturn(240060200);setup.invoke(null,b,choice);verify(b).setAutopotHpAlert(0.95f);clearInvocations(b);when(b.getMapId()).thenReturn(100000000);setup.invoke(null,b,choice);verify(b).setAutopotHpAlert(BotManager.cfg.AUTOPOT_HP_THRESH);
 }

 @Test void resurrectionWaitsWhenTheCorpseOverlapsALethalBossBody(){
  var b=raider(Job.BISHOP);var map=mock(server.maps.MapleMap.class);when(b.getMap()).thenReturn(map);var dead=mock(Character.class,RETURNS_DEEP_STUBS);when(dead.getMap()).thenReturn(map);when(dead.getPosition()).thenReturn(new java.awt.Point(403,-182));var party=mock(net.server.world.Party.class);when(party.getId()).thenReturn(1);when(b.getParty()).thenReturn(party);when(dead.getParty()).thenReturn(party);when(b.getSkillLevel(constants.skills.Bishop.RESURRECTION)).thenReturn(10);var skill=mock(Skill.class);var fx=mock(server.StatEffect.class);when(skill.getEffect(10)).thenReturn(fx);when(fx.canPaySkillCost(b)).thenReturn(true);
  try(var skills=mockStatic(SkillFactory.class);var safety=mockStatic(BotHorntailCombat.class);var normal=mockStatic(BotZakumSupport.class)){skills.when(()->SkillFactory.getSkill(constants.skills.Bishop.RESURRECTION)).thenReturn(skill);assertFalse(BotHorntailRevive.tick(new BotEntry(b,b,null),b,List.of(b,dead),true));normal.verifyNoInteractions();}
 }

 @Test void sacrificeIsNotAFreeDamageUpgradeInPinkBean()throws Exception{var b=raider(Job.DARKKNIGHT);var plan=BotCombatManager.class.getDeclaredMethod("planSkillAttack",BotEntry.class,Character.class,Monster.class,int.class);plan.setAccessible(true);try(var skills=mockStatic(SkillFactory.class)){assertNull(plan.invoke(null,new BotEntry(b,b,null),b,mock(Monster.class),constants.skills.DragonKnight.SACRIFICE));skills.verifyNoInteractions();}}

 @Test void defeatedBirdDoesNotKeepPullingCrossingBotsBackOntoItsPlatform()throws Exception{
  var b=mock(Character.class,RETURNS_DEEP_STUBS);var event=mock(EventInstanceManager.class);var map=mock(server.maps.MapleMap.class);var rex=mock(Monster.class);when(rex.getId()).thenReturn(8820004);when(rex.isAlive()).thenReturn(true);when(map.getAllMonsters()).thenReturn(List.of(rex));when(b.getMap()).thenReturn(map);when(b.getMapId()).thenReturn(270050100);when(b.getPosition()).thenReturn(new java.awt.Point(-510,-99));when(b.getEventInstance()).thenReturn(event);when(event.getName()).thenReturn("PinkBean1");when(event.getIntProperty("stage")).thenReturn(3);when(b.getJob()).thenReturn(Job.NIGHTLORD);var c=new BotPinkBeanRun.Crew();c.leader=b;c.notices.add("wave-3");var e=new BotEntry(b,b,null);
  var field=BotPinkBeanRun.class.getDeclaredField("RUNS");field.setAccessible(true);var runs=(java.util.Map<Character,BotPinkBeanRun.Crew>)field.get(null);runs.put(b,c);
  try(var cures=mockStatic(BotLpqCures.class);var revive=mockStatic(BotHorntailRevive.class);var support=mockStatic(BotHorntailSupport.class);var staging=mockStatic(BotPinkBeanTransition.class);var platforms=mockStatic(BotPinkBeanPlatforms.class);var combat=mockStatic(BotHorntailCombat.class)){
   combat.when(()->BotHorntailCombat.tick(e,b,true)).thenReturn(true);assertTrue(BotPinkBeanRun.tick(e,b,true));platforms.verifyNoInteractions();combat.verify(()->BotHorntailCombat.tick(e,b,true));
   when(map.getMonsterById(8820005)).thenReturn(mock(Monster.class));assertTrue(BotPinkBeanRun.tick(e,b,true));platforms.verify(()->BotPinkBeanPlatforms.adjustment(b.getPosition(),3));
  }finally{runs.remove(b);}
 }

 @Test void arielsUpperPlatformsKeepTheirFullRouteInsteadOfResettingToTheFloor(){
  var b=raider(Job.NIGHTLORD);when(b.getMap()).thenReturn(mock(server.maps.MapleMap.class));var e=new BotEntry(b,b,null);
  try(var walk=mockStatic(BotZakumPqRun.class)){
   for(int x:new int[]{-250,-219,-300,200,300}){when(b.getPosition()).thenReturn(new java.awt.Point(x,-42));var goal=new java.awt.Point(x<0?-219:219,-456);BotPinkBeanMovement.walkTo(e,b,goal,true);walk.verify(()->BotZakumPqRun.walkTo(e,b,goal,true),atLeastOnce());}
   walk.verify(()->BotZakumPqRun.walkTo(eq(e),eq(b),argThat(p->p.y==-42),eq(true)),never());
  }
 }

 @Test void arielCrossingsDescendOutsideThenUseTheOppositeSidesRopes(){
  var b=raider(Job.BISHOP);var map=mock(server.maps.MapleMap.class);when(b.getMap()).thenReturn(map);when(map.getMonsterById(8820002)).thenReturn(mock(Monster.class));var e=new BotEntry(b,b,null);var goal=new java.awt.Point(219,-456);
  try(var walk=mockStatic(BotZakumPqRun.class)){
   when(b.getPosition()).thenReturn(new java.awt.Point(-409,-456));BotPinkBeanMovement.walkTo(e,b,goal,true);walk.verify(()->BotZakumPqRun.walkTo(e,b,new java.awt.Point(-600,-42),true));
   BotPinkBeanMovement.walkTo(e,b,new java.awt.Point(-33,-160),true);walk.verify(()->BotZakumPqRun.walkTo(e,b,new java.awt.Point(-600,-42),true),times(2));
   when(b.getPosition()).thenReturn(new java.awt.Point(-600,-42));BotPinkBeanMovement.walkTo(e,b,goal,true);walk.verify(()->BotZakumPqRun.walkTo(e,b,new java.awt.Point(610,-42),true));
   when(b.getPosition()).thenReturn(new java.awt.Point(418,-42));BotPinkBeanMovement.walkTo(e,b,goal,true);walk.verify(()->BotZakumPqRun.walkTo(e,b,goal,true));
  }
 }
 @Test void arielsFloorApproachDoesNotUndoTheActualRopeLaunch(){
  var b=raider(Job.NIGHTLORD);var map=mock(server.maps.MapleMap.class);when(b.getMap()).thenReturn(map);when(map.getMonsterById(8820002)).thenReturn(mock(Monster.class));var e=new BotEntry(b,b,null);var goal=new java.awt.Point(-219,-456);
  try(var walk=mockStatic(BotZakumPqRun.class)){
   when(b.getPosition()).thenReturn(new java.awt.Point(-250,-42));BotPinkBeanMovement.walkTo(e,b,goal,true);walk.verify(()->BotZakumPqRun.walkTo(e,b,new java.awt.Point(-600,-42),true));
   when(b.getPosition()).thenReturn(new java.awt.Point(-418,-42));BotPinkBeanMovement.walkTo(e,b,goal,true);walk.verify(()->BotZakumPqRun.walkTo(e,b,goal,true));
  }
 }
 @Test void pinkDeathsUseTheNativeReturnRoomWhenNoPartyResurrectionIsAvailable()throws Exception{
  var b=mock(Character.class,RETURNS_DEEP_STUBS);var mate=mock(Character.class,RETURNS_DEEP_STUBS);when(b.getId()).thenReturn(723);when(mate.getId()).thenReturn(727);var event=mock(EventInstanceManager.class);when(b.getEventInstance()).thenReturn(event);when(b.getMapId()).thenReturn(270050100);when(event.getName()).thenReturn("PinkBean1");when(b.getMap().getReturnMapId()).thenReturn(270050200);when(b.getPosition()).thenReturn(new java.awt.Point(-33,-372));when(b.getMap().getAllMonsters()).thenReturn(List.of());when(event.getPlayers()).thenReturn(List.of(b,mate));when(mate.isAlive()).thenReturn(true);when(mate.getHp()).thenReturn(100);when(mate.getMapId()).thenReturn(270050100);
  var wait=BotManager.class.getDeclaredMethod("hasLiveEventMateInMap",Character.class);wait.setAccessible(true);assertEquals(false,wait.invoke(null,b));
  when(event.getName()).thenReturn("LudiPQ1");assertEquals(true,wait.invoke(null,b));
 }
 @Test void anAvailableSamePartyResurrectionIsStillPreferred(){
  var dead=mock(Character.class);var bishop=mock(Character.class);var map=mock(server.maps.MapleMap.class);when(dead.getMap()).thenReturn(map);when(bishop.getMap()).thenReturn(map);when(dead.getPosition()).thenReturn(new java.awt.Point(-33,-372));when(bishop.isAlive()).thenReturn(true);var party=mock(net.server.world.Party.class);when(party.getId()).thenReturn(1);when(dead.getParty()).thenReturn(party);when(bishop.getParty()).thenReturn(party);when(bishop.getSkillLevel(constants.skills.Bishop.RESURRECTION)).thenReturn(10);
  try(var safe=mockStatic(BotHorntailCombat.class)){safe.when(()->BotHorntailCombat.safe(dead,dead.getPosition(),map.getAllMonsters())).thenReturn(true);assertTrue(BotPinkBeanRecovery.waitForResurrection(dead,List.of(bishop)));when(bishop.skillIsCooling(constants.skills.Bishop.RESURRECTION)).thenReturn(true);assertFalse(BotPinkBeanRecovery.waitForResurrection(dead,List.of(bishop)));when(bishop.skillIsCooling(constants.skills.Bishop.RESURRECTION)).thenReturn(false);var other=mock(net.server.world.Party.class);when(other.getId()).thenReturn(2);when(bishop.getParty()).thenReturn(other);assertFalse(BotPinkBeanRecovery.waitForResurrection(dead,List.of(bishop)));when(bishop.getParty()).thenReturn(party);safe.when(()->BotHorntailCombat.safe(dead,dead.getPosition(),map.getAllMonsters())).thenReturn(false);assertFalse(BotPinkBeanRecovery.waitForResurrection(dead,List.of(bishop)));}
 }


 @Test void leavingATopPlatformForTheOuterFloorDescendsUnderTheLivingSageFirst(){
  var b=raider(Job.NIGHTLORD);var map=mock(server.maps.MapleMap.class);when(b.getMap()).thenReturn(map);var e=new BotEntry(b,b,null);
  try(var walk=mockStatic(BotZakumPqRun.class)){
   for(int side:new int[]{-1,1}){when(map.getMonsterById(side<0?8820003:8820004)).thenReturn(mock(Monster.class));when(b.getPosition()).thenReturn(new java.awt.Point(side*350,-456));BotPinkBeanMovement.walkTo(e,b,new java.awt.Point(side*900,-42),true);walk.verify(()->BotZakumPqRun.walkTo(e,b,new java.awt.Point(side*508,-42),true));}
   when(b.getPosition()).thenReturn(new java.awt.Point(508,-42));BotPinkBeanMovement.walkTo(e,b,new java.awt.Point(840,-42),true);walk.verify(()->BotZakumPqRun.walkTo(e,b,new java.awt.Point(840,-42),true));
  }
 }
 @Test void aSupportTripToTheMiddlePlatformUsesTheRopeApproachToo(){var b=raider(Job.DARKKNIGHT);var map=mock(server.maps.MapleMap.class);when(b.getMap()).thenReturn(map);when(map.getMonsterById(8820004)).thenReturn(mock(Monster.class));when(b.getPosition()).thenReturn(new java.awt.Point(840,-42));var e=new BotEntry(b,b,null);try(var walk=mockStatic(BotZakumPqRun.class)){BotPinkBeanMovement.walkTo(e,b,new java.awt.Point(508,-258),true);walk.verify(()->BotZakumPqRun.walkTo(e,b,new java.awt.Point(508,-42),true));}}
 @Test void aFullHeadlessLootBagDoesNotPreventTheNativeExit()throws Exception{
  var b=mock(Character.class,RETURNS_DEEP_STUBS);var drop=mock(server.maps.MapItem.class,RETURNS_DEEP_STUBS);var e=new BotEntry(b,b,null);var crew=new BotPinkBeanRun.Crew();crew.leader=b;
  when(b.getMap().getDroppedItems()).thenReturn(List.of(drop));when(drop.canBePickedBy(b)).thenReturn(true);when(drop.getItemId()).thenReturn(1002000);when(drop.getItem().getQuantity()).thenReturn((short)1);when(drop.getItem().getOwner()).thenReturn("");
  var collect=BotPinkBeanRun.class.getDeclaredMethod("collect",BotEntry.class,Character.class,BotPinkBeanRun.Crew.class,boolean.class);collect.setAccessible(true);
  try(var inventory=mockStatic(client.inventory.manipulator.InventoryManipulator.class)){
   inventory.when(()->client.inventory.manipulator.InventoryManipulator.checkSpace(b.getClient(),1002000,1,"")).thenReturn(false);
   assertFalse((boolean)collect.invoke(null,e,b,crew,true));verify(b,never()).pickupItem(drop);
  }
 }
}
