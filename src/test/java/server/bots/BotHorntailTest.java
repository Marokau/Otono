package server.bots;
import client.Character;
import client.*;
import org.junit.jupiter.api.Test;
import server.maps.*;
import server.life.*;
import scripting.event.EventInstanceManager;
import java.awt.*;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class BotHorntailTest {
 @Test void dismissalRecoversStaleDisbandedPartyBeforeSavingAndDisconnecting(){
  var b=mock(Character.class,RETURNS_DEEP_STUBS);var party=mock(net.server.world.Party.class);var manager=mock(BotManager.class);var entry=new BotEntry(b,b,null);
  when(b.getId()).thenReturn(77);when(b.getParty()).thenReturn(party);when(party.getId()).thenReturn(123);when(b.getWorldServer().getParty(123)).thenReturn(null);when(manager.getEntryByBotCharId(77)).thenReturn(entry);
  try(var managers=mockStatic(BotManager.class);var runs=mockStatic(BotHorntailRun.class);var parties=mockStatic(net.server.world.Party.class)){
   managers.when(BotManager::getInstance).thenReturn(manager);BotHorntailCompanions.dismiss(b);
   verify(b).setParty(null);verify(b).setMPC(null);parties.verifyNoInteractions();
   var order=inOrder(b);order.verify(b).setParty(null);order.verify(b).saveCharToDB(true);
   runs.verify(()->BotHorntailRun.unregister(b));verify(b.getClient()).disconnect(false,false);
  }
 }

 @Test void readinessTargetsTheMostEvasiveMainHeadRatherThanTheFirstHead(){
  var b=mock(Character.class,RETURNS_DEEP_STUBS);when(b.getClient()).thenReturn(mock(BotClient.class));when(b.getEventInstance()).thenReturn(null);when(b.getLevel()).thenReturn(180);when(b.getJob()).thenReturn(Job.HERO);when(b.getId()).thenReturn(1);when(b.getInventory(client.inventory.InventoryType.USE).list()).thenReturn(List.of());var e=new BotEntry(b,b,null);var manager=mock(BotManager.class);when(manager.getEntryByBotCharId(1)).thenReturn(e);
  try(var managers=mockStatic(BotManager.class);var builds=mockStatic(BotBuildManager.class);var books=mockStatic(BotZakumBooks.class);var goals=mockStatic(BotHorntailEquipment.class);var gear=mockStatic(BotMagatiaRun.class);var supplies=mockStatic(BotZakumSupplies.class);var attacks=mockStatic(BotAttackExecutionProvider.class);var life=mockStatic(LifeFactory.class);var hpPreparation=mockStatic(BotPinkBeanEquipment.class);var damage=mockStatic(BotRaidDamageEquipment.class)){
   var data=provider.DataProviderFactory.getDataProvider(provider.wz.WZFiles.MOB);int hardest=0;
   for(int id=8810000;id<=8810009;id++){var raw=data.getData(id+".img");var prototype=mock(Monster.class);int avoid=provider.DataTool.getInt("info/eva",raw,0);when(prototype.getLevel()).thenReturn(provider.DataTool.getInt("info/level",raw,0));when(prototype.getAvoidability()).thenReturn(avoid);hardest=Math.max(hardest,avoid);int key=id;life.when(()->LifeFactory.getMonster(key)).thenReturn(prototype);}
   managers.when(BotManager::getInstance).thenReturn(manager);BotHorntailCompanions.prepare(b);
   damage.verify(()->BotRaidDamageEquipment.prepare(b));damage.verify(()->BotRaidDamageEquipment.supplies(b));assertEquals(hardest,e.aspirationalMobAvoid);assertEquals(160,e.aspirationalMobLevel);verify(b).cancelEffectFromBuffStat(BuffStat.MAPLE_WARRIOR);
  }
 }

 @Test void ropesAreTransitAndNeverAnAttackStance(){
  var b=mock(Character.class);var map=mock(MapleMap.class);var head=mock(Monster.class);var e=new BotEntry(b,b,null);e.climbing=true;var landing=new Point(200,0);
  when(b.getMap()).thenReturn(map);when(b.getPosition()).thenReturn(new Point(200,100));when(b.getJob()).thenReturn(Job.NIGHTLORD);when(map.getAllMonsters()).thenReturn(List.of(head));when(head.getId()).thenReturn(8810003);when(head.isAlive()).thenReturn(true);
  try(var horn=mockStatic(BotHorntailCombat.class,CALLS_REAL_METHODS);var combat=mockStatic(BotCombatManager.class);var walking=mockStatic(BotZakumPqRun.class)){
   horn.when(()->BotHorntailCombat.stance(e,b,List.of(head))).thenReturn(landing);assertTrue(BotHorntailCombat.tick(e,b,true));walking.verify(()->BotZakumPqRun.walkTo(e,b,landing,true));combat.verify(()->BotCombatManager.planAttack(any(),any(),any()),never());
  }
 }

 @Test void companionsFollowNormalLeaderExitEvenWhenTheExpeditionCanContinue()throws Exception {
  var leader=mock(Character.class,RETURNS_DEEP_STUBS);var b=mock(Character.class);var party=mock(net.server.world.Party.class);var ev=mock(EventInstanceManager.class);var c=new BotHorntailRun.Crew();c.leader=leader;c.bots.add(b);c.human=true;var e=new BotEntry(b,leader,null);
  when(leader.getId()).thenReturn(1);when(party.getId()).thenReturn(10);when(party.getLeaderId()).thenReturn(1);when(leader.getParty()).thenReturn(party);when(b.getParty()).thenReturn(party);when(leader.getWorldServer().getPlayerStorage().getCharacterById(1)).thenReturn(leader);when(b.getMapId()).thenReturn(240060200);when(b.getEventInstance()).thenReturn(ev);when(leader.getMapId()).thenReturn(240050600);when(leader.getEventInstance()).thenReturn(null);
  var f=BotHorntailRun.class.getDeclaredField("RUNS");f.setAccessible(true);var runs=(java.util.Map<Character,BotHorntailRun.Crew>)f.get(null);runs.put(b,c);
  try(var run=mockStatic(BotHorntailRun.class,CALLS_REAL_METHODS)){run.when(()->BotHorntailRun.talk(e,b,c,2083002,1,true)).thenReturn(true);assertTrue(BotHorntailRun.tick(e,b,true));run.verify(()->BotHorntailRun.talk(e,b,c,2083002,1,true));}finally{runs.remove(b);}
 }

 @Test void stunStopsSeduceInputsWithoutStoppingNormalRecoilPhysics(){var b=mock(Character.class);var e=new BotEntry(b,b,null);when(b.hasDisease(Disease.STUN)).thenReturn(true);var m=mock(BotManager.class);try(var managers=mockStatic(BotManager.class)){managers.when(BotManager::getInstance).thenReturn(m);BotHorntailSeduce.tick(e,b);verify(m).tickStunned(e);verify(b,never()).getAllDiseases();}}

 @Test void darkRouteDoesNotKeepHittingAConsumedKeyBox(){
  var b=mock(Character.class);var leader=mock(Character.class);var map=mock(MapleMap.class);var ev=mock(EventInstanceManager.class);var r=mock(Reactor.class);var inv=mock(client.inventory.Inventory.class);var c=new BotHorntailRun.Crew();c.leader=leader;c.human=true;for(int i=0;i<5;i++)c.bots.add(i==3?b:mock(Character.class));c.notices.add("intro-240050310");c.arrival.put(240050310,0L);
  when(b.getMapId()).thenReturn(240050310);when(b.getMap()).thenReturn(map);when(b.getEventInstance()).thenReturn(ev);when(b.getInventory(client.inventory.InventoryType.ETC)).thenReturn(inv);when(inv.list()).thenReturn(List.of());when(map.getDroppedItems()).thenReturn(List.of());when(map.getAllReactors()).thenReturn(List.of(r));when(r.getId()).thenReturn(2402006);when(r.isActive()).thenReturn(true);when(r.getState()).thenReturn((byte)4);when(r.getReactorType()).thenReturn(999);
  try(var actions=mockStatic(BotGuildActions.class);var hold=mockStatic(BotOpqRun.class)){BotHorntailTrial.tick(new BotEntry(b,leader,null),b,c,true);actions.verifyNoInteractions();}
 }

 @Test void bishopChoosesSummonsBeforeBossParts(){
  var b=mock(Character.class,RETURNS_DEEP_STUBS);var map=mock(MapleMap.class);var add=mock(Monster.class);var head=mock(Monster.class);var hitboxes=mock(server.bots.combat.BotMobHitboxProvider.class);
  when(b.getMap()).thenReturn(map);when(b.getPosition()).thenReturn(new Point(0,0));when(b.getJob()).thenReturn(Job.BISHOP);when(map.getAllMonsters()).thenReturn(List.of(head,add));for(var m:List.of(head,add))when(m.isAlive()).thenReturn(true);when(head.getId()).thenReturn(8810003);when(add.getId()).thenReturn(8810019);when(hitboxes.getMobBounds(head)).thenReturn(new Rectangle(400,-300,50,80));
  try(var boxes=mockStatic(server.bots.combat.BotMobHitboxProvider.class);var combat=mockStatic(BotCombatManager.class);var attacks=mockStatic(BotAttackExecutionProvider.class);var graphs=mockStatic(BotNavigationGraphProvider.class);var hold=mockStatic(BotOpqRun.class)){
   boxes.when(server.bots.combat.BotMobHitboxProvider::getInstance).thenReturn(hitboxes);BotHorntailCombat.tick(new BotEntry(b,b,null),b,true);combat.verify(()->BotCombatManager.planAttack(any(),eq(b),eq(add)));combat.verify(()->BotCombatManager.planAttack(any(),eq(b),eq(head)),never());
  }
 }

 @Test void immunePartsStillCountAsDangerousBodyContact(){
  var b=mock(Character.class);var map=mock(MapleMap.class);var leg=mock(Monster.class);var head=mock(Monster.class);var hitboxes=mock(server.bots.combat.BotMobHitboxProvider.class);var defense=mock(server.bots.combat.BotDefenseDataProvider.class);
  when(b.getMap()).thenReturn(map);when(b.getPosition()).thenReturn(new Point(0,0));when(b.getJob()).thenReturn(Job.NIGHTLORD);when(b.getCurrentMaxHp()).thenReturn(1000);when(map.getAllMonsters()).thenReturn(List.of(leg,head));for(var m:List.of(leg,head))when(m.isAlive()).thenReturn(true);
  when(leg.getId()).thenReturn(8810008);when(head.getId()).thenReturn(8810003);when(leg.isBuffed(client.status.MonsterStatus.WEAPON_IMMUNITY)).thenReturn(true);when(hitboxes.getMobBounds(leg)).thenReturn(new Rectangle(-20,-50,40,70));when(hitboxes.getMobBounds(head)).thenReturn(new Rectangle(400,-300,50,80));when(defense.expectedPhysicalTouchDamageOnHit(eq(b),anyInt(),anyInt())).thenReturn(2000);
  try(var boxes=mockStatic(server.bots.combat.BotMobHitboxProvider.class);var incoming=mockStatic(server.bots.combat.BotDefenseDataProvider.class);var combat=mockStatic(BotCombatManager.class);var graphs=mockStatic(BotNavigationGraphProvider.class);var hold=mockStatic(BotOpqRun.class)){
   boxes.when(server.bots.combat.BotMobHitboxProvider::getInstance).thenReturn(hitboxes);incoming.when(server.bots.combat.BotDefenseDataProvider::getInstance).thenReturn(defense);BotHorntailCombat.tick(new BotEntry(b,b,null),b,true);combat.verify(()->BotCombatManager.planAttack(any(),any(),any()),never());
  }
 }

 @Test void mainCrystalContinuesThroughItsSixthNativeHit()throws Exception {
  var b=mock(Character.class,RETURNS_DEEP_STUBS);var map=mock(MapleMap.class);var ev=mock(EventInstanceManager.class);var r=mock(Reactor.class);var c=new BotHorntailRun.Crew();c.leader=b;
  when(b.getMapId()).thenReturn(240060200);when(b.getMap()).thenReturn(map);when(b.getEventInstance()).thenReturn(ev);when(map.getAllMonsters()).thenReturn(List.of());when(r.getId()).thenReturn(2401000);when(r.isActive()).thenReturn(true);when(r.getState()).thenReturn((byte)5);when(r.getReactorType()).thenReturn(0);
  c.notices.add("intro-240060200");c.arrival.put(240060200,0L);var f=BotHorntailRun.class.getDeclaredField("RUNS");f.setAccessible(true);var runs=(java.util.Map<Character,BotHorntailRun.Crew>)f.get(null);runs.put(b,c);
  try(var support=mockStatic(BotHorntailSupport.class);var rez=mockStatic(BotZakumSupport.class);var actions=mockStatic(BotGuildActions.class)){
   actions.when(()->BotGuildActions.smash(any(),eq(b),any(),eq(true))).thenAnswer(call->{java.util.function.Predicate<Reactor> wanted=call.getArgument(2);assertTrue(wanted.test(r));when(r.getState()).thenReturn((byte)6);when(r.getReactorType()).thenReturn(999);assertFalse(wanted.test(r));return true;});
   assertTrue(BotHorntailRun.tick(new BotEntry(b,b,null),b,true));actions.verify(()->BotGuildActions.smash(any(),eq(b),any(),eq(true)));
  }finally{runs.remove(b);}
 }

 @Test void companionsDoNotReclaimLeaderHandoffsButCanReceiveNativeReactorRelays(){var b=mock(Character.class);var leader=mock(Character.class);var drop=mock(MapItem.class);var c=new BotHorntailRun.Crew();c.leader=leader;when(drop.isPlayerDrop()).thenReturn(true);when(drop.getDropper()).thenReturn(b);try(var horn=mockStatic(BotHorntailRun.class)){horn.when(()->BotHorntailRun.crew(b)).thenReturn(c);assertFalse(BotHorntailTargets.canCollect(b,drop));when(drop.getDropper()).thenReturn(mock(Reactor.class));assertTrue(BotHorntailTargets.canCollect(b,drop));when(drop.getDropper()).thenReturn(b);c.leader=b;assertTrue(BotHorntailTargets.canCollect(b,drop));}}
 @Test void aggregateHpAndIntroAreNotCombatTargets(){var b=mock(Character.class);var m=mock(Monster.class);try(var horn=mockStatic(BotHorntailRun.class)){horn.when(()->BotHorntailRun.inInstance(b)).thenReturn(true);when(m.getId()).thenReturn(8810018);assertFalse(BotHorntailTargets.allowed(b,m));for(int id=8810010;id<=8810018;id++){when(m.getId()).thenReturn(id);assertFalse(BotHorntailTargets.allowed(b,m));}for(int id=8810024;id<=8810026;id++){when(m.getId()).thenReturn(id);assertFalse(BotHorntailTargets.allowed(b,m));}when(m.getId()).thenReturn(8810019);assertTrue(BotHorntailTargets.allowed(b,m));when(m.getId()).thenReturn(8810008);assertTrue(BotHorntailTargets.allowed(b,m));horn.when(()->BotHorntailRun.inInstance(b)).thenReturn(false);when(m.getId()).thenReturn(8810018);assertTrue(BotHorntailTargets.allowed(b,m));}}
 @Test void humanCaveAccessStillNeedsTransformationOrCertificate(){var b=mock(Character.class);assertFalse(BotHorntailCompanions.caveAccess(b));when(b.getBuffSource(BuffStat.MORPH)).thenReturn(2210003);assertTrue(BotHorntailCompanions.caveAccess(b));when(b.getBuffSource(BuffStat.MORPH)).thenReturn(0);when(b.getItemQuantity(4001086,false)).thenReturn(1);assertTrue(BotHorntailCompanions.caveAccess(b));}
 @Test void humanAssemblyWithoutPrerequisitesNeverCreatesCompanions(){var b=mock(Character.class);when(b.getLevel()).thenReturn(150);when(b.getMapId()).thenReturn(BotHorntailRun.TRIAL_RECRUIT);assertTrue(BotHorntailCompanions.assemble(b).contains("transformation medicine"));verify(b,never()).changeMap(anyInt(),anyInt());}
 @Test void humanCannotReceiveCompanionProvisioning(){var b=mock(Character.class);assertThrows(IllegalArgumentException.class,()->BotHorntailCompanions.prepare(b));verify(b,never()).getInventory(any());}
 @Test void raidCompositionHasTwoBishopsAndTwoDarkKnights(){assertEquals(11,BotHorntailCompanions.JOBS.size());for(int from:new int[]{0,5}){var group=BotHorntailCompanions.JOBS.subList(from,from==0?5:11);assertTrue(group.contains(Job.BISHOP));assertTrue(group.contains(Job.DARKKNIGHT));}}
 @Test void protectionReadinessIncludesBothHyperBodyStatsAndMageGuard(){var b=mock(Character.class);when(b.isAlive()).thenReturn(true);when(b.getJob()).thenReturn(Job.BISHOP);when(b.getBuffedValue(any(BuffStat.class))).thenReturn(null);assertFalse(BotHorntailSupport.protectedMember(b));when(b.getBuffedValue(BuffStat.HYPERBODYHP)).thenReturn(60);when(b.getBuffedValue(BuffStat.HYPERBODYMP)).thenReturn(60);assertFalse(BotHorntailSupport.protectedMember(b));when(b.getBuffedValue(BuffStat.MAGIC_GUARD)).thenReturn(80);assertTrue(BotHorntailSupport.protectedMember(b));when(b.isAlive()).thenReturn(false);assertFalse(BotHorntailSupport.protectedMember(b));}
 @Test void seduceBlocksPotionsOnlyInHorntailAndUsesNativeInputModes(){var b=mock(Character.class);when(b.hasDisease(Disease.SEDUCE)).thenReturn(true);try(var horn=mockStatic(BotHorntailRun.class)){horn.when(()->BotHorntailRun.inInstance(b)).thenReturn(true);assertFalse(BotHorntailSeduce.canUsePotions(b));when(b.hasDisease(Disease.SEDUCE)).thenReturn(false);assertTrue(BotHorntailSeduce.canUsePotions(b));horn.when(()->BotHorntailRun.inInstance(b)).thenReturn(false);when(b.hasDisease(Disease.SEDUCE)).thenReturn(true);assertTrue(BotHorntailSeduce.canUsePotions(b));}assertEquals(-1,BotHorntailSeduce.direction(1));assertEquals(1,BotHorntailSeduce.direction(2));assertEquals(0,BotHorntailSeduce.direction(3));}
 @Test void headColumnsDoNotTruncateHigherAttackMaskBits(){var a=BotMobAttackObserver.attack(8810000,1);assertNotNull(a);assertEquals(31,a.areaCount());assertTrue(a.deadly());var map=mock(MapleMap.class);var mob=mock(Monster.class);when(mob.getId()).thenReturn(8810000);when(map.getPointBelow(any(Point.class))).thenAnswer(c->new Point(((Point)c.getArgument(0)).x,260));var cast=new BotMobAttackObserver.Cast(map,mob,a,1,1<<30,new Point(960,120),0);assertEquals(1,BotMobAttackObserver.areas(cast).size());assertNotNull(BotMobAttackObserver.attack(8810008,0));assertNotNull(BotMobAttackObserver.attack(8810009,0));}
 @Test void activeEarnedTrialKeysAreProtectedFromExpiry(){var map=mock(MapleMap.class);var d=mock(MapItem.class);var b=mock(Character.class);var ev=mock(EventInstanceManager.class);when(map.getId()).thenReturn(240050101);when(map.getEventInstance()).thenReturn(ev);when(ev.getPlayers()).thenReturn(List.of(b));when(d.getItemId()).thenReturn(4001088);try(var horn=mockStatic(BotHorntailRun.class)){horn.when(()->BotHorntailRun.crew(b)).thenReturn(new BotHorntailRun.Crew());assertTrue(BotPqDropPolicy.keepsForHandoff(map,d));when(ev.isEventCleared()).thenReturn(true);assertFalse(BotPqDropPolicy.keepsForHandoff(map,d));}}
}
