package server.bots;
import client.Character;import client.BotClient;import org.junit.jupiter.api.Test;import server.maps.*;import java.util.List;
import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
class BotMvpqTest {
 @Test void normalHumanRequirementsAreNotBypassed(){var b=mock(Character.class);when(b.getMapId()).thenReturn(BotMvpqRun.RECRUIT);when(b.getLevel()).thenReturn(139);assertNotNull(BotMvpqCompanions.eligibility(b,false));when(b.getLevel()).thenReturn(140);assertNull(BotMvpqCompanions.eligibility(b,false));when(b.getClient()).thenReturn(new BotClient(0,1));assertNotNull(BotMvpqCompanions.eligibility(b,false));assertNull(BotMvpqCompanions.eligibility(b,true));when(b.getEventInstance()).thenReturn(mock(scripting.event.EventInstanceManager.class));assertNotNull(BotMvpqCompanions.eligibility(b,true));}
 @Test void letterExpirationProtectionRequiresAssignedCrewAndEndsWithItsEvent(){var map=mock(MapleMap.class);var d=mock(MapItem.class);var e=mock(scripting.event.EventInstanceManager.class);var b=mock(Character.class);when(map.getId()).thenReturn(BotMvpqRun.COURSE);when(map.getEventInstance()).thenReturn(e);when(d.getItemId()).thenReturn(BotMvpqRun.LETTER);when(e.getPlayers()).thenReturn(List.of(b));try(var crews=mockStatic(BotMvpqRun.class)){assertFalse(BotPqDropPolicy.keepsForHandoff(map,d));crews.when(()->BotMvpqRun.assigned(b)).thenReturn(true);assertTrue(BotPqDropPolicy.keepsForHandoff(map,d));when(e.isEventCleared()).thenReturn(true);assertFalse(BotPqDropPolicy.keepsForHandoff(map,d));}}
 @Test void companionsCannotPassivelyTakeLettersBossGearOrTreasure(){var b=mock(Character.class);var lead=mock(Character.class);var d=mock(MapItem.class);var e=new BotEntry(b,lead,null);when(lead.getId()).thenReturn(9);when(d.canBePickedBy(b)).thenReturn(true);try(var crews=mockStatic(BotMvpqRun.class)){crews.when(()->BotMvpqRun.inInstance(b)).thenReturn(true);crews.when(()->BotMvpqRun.belongsTo(b,9)).thenReturn(true);when(d.getItemId()).thenReturn(BotMvpqRun.LETTER);assertFalse(BotLootEligibility.canBotLoot(e,b,d));when(d.getItemId()).thenReturn(1000001);when(b.getMapId()).thenReturn(BotMvpqRun.BOSS);assertFalse(BotLootEligibility.canBotLoot(e,b,d));when(b.getMapId()).thenReturn(BotMvpqRun.BONUS);assertFalse(BotLootEligibility.canBotLoot(e,b,d));}}
 @Test void mvAndSpikeAttacksKeepTheirNativeDamageTiming(){var quake=BotMobAttackObserver.attack(9400589,0);var swing=BotMobAttackObserver.attack(9400589,1);var spike=BotMobAttackObserver.attack(9400704,0);assertNotNull(quake);assertEquals(1380,quake.delay());assertEquals(700,quake.pad());assertEquals(2250,swing.delay());assertEquals(620,swing.pad());assertEquals(800,spike.delay());assertEquals(25,spike.pad());var bonus=BotMobAttackObserver.attack(9400748,0);assertEquals(720,bonus.pad());assertEquals(1380,bonus.delay());}
 @Test void unreachableBoxClaimIsReleasedForAReachableOne(){
  var b=mock(Character.class);var map=mock(MapleMap.class);var graph=mock(BotNavigationGraph.class);
  var blocked=mock(Reactor.class);var near=mock(Reactor.class);var here=new java.awt.Point(0,0);var far=new java.awt.Point(100,0);var reachable=new java.awt.Point(20,0);
  when(b.getMap()).thenReturn(map);when(b.getId()).thenReturn(7);when(b.getPosition()).thenReturn(here);
  when(map.getReactorByOid(1)).thenReturn(blocked);when(map.getAllReactors()).thenReturn(List.of(blocked,near));
  when(blocked.getObjectId()).thenReturn(1);when(near.getObjectId()).thenReturn(2);
  when(blocked.getId()).thenReturn(6741015);when(near.getId()).thenReturn(6741015);
  when(blocked.isActive()).thenReturn(true);when(near.isActive()).thenReturn(true);
  when(blocked.getPosition()).thenReturn(far);when(near.getPosition()).thenReturn(reachable);
  when(graph.findRegionId(map,here)).thenReturn(0);when(graph.findRegionId(map,far)).thenReturn(1);when(graph.findRegionId(map,reachable)).thenReturn(2);
  when(graph.canReach(0,2,0)).thenReturn(true);
  var c=new BotMvpqRun.Crew();c.claims.put(1,7);
  try(var graphs=mockStatic(BotNavigationGraphProvider.class);var nav=mockStatic(BotNavigationManager.class)){
   graphs.when(()->BotNavigationGraphProvider.peekGraph(eq(map),any(BotMovementProfile.class))).thenReturn(graph);
   assertSame(near,BotMvpqRun.claim(c,b,6741015,6741016));assertFalse(c.claims.containsKey(1));assertEquals(7,c.claims.get(2));
  }
 }
 @Test void airborneCharacterAvoidsMvsGroundQuake()throws Exception{
  var b=mock(Character.class);var map=mock(MapleMap.class);var mob=mock(server.life.Monster.class);
  when(b.getMap()).thenReturn(map);when(b.getHp()).thenReturn(1000);when(mob.isAlive()).thenReturn(true);when(mob.getId()).thenReturn(9400589);
  var entry=new BotEntry(b,b,null);entry.inAir=true;
  var cast=new BotMobAttackObserver.Cast(map,mob,BotMobAttackObserver.attack(9400589,0),0,0,new java.awt.Point(0,0),0);
  var field=BotMobAttackObserver.class.getDeclaredField("PENDING");field.setAccessible(true);
  @SuppressWarnings("unchecked") var pending=(java.util.Map<Character,java.util.Queue<BotMobAttackObserver.Cast>>)field.get(null);
  var queue=new java.util.concurrent.ConcurrentLinkedQueue<BotMobAttackObserver.Cast>();queue.add(cast);pending.put(b,queue);
  try(var crews=mockStatic(BotMvpqRun.class)){
   crews.when(()->BotMvpqRun.inInstance(b)).thenReturn(true);BotMobAttackObserver.tick(entry,b);
   assertTrue(queue.isEmpty());entry.inAir=false;entry.climbing=true;queue.add(cast);BotMobAttackObserver.tick(entry,b);assertTrue(queue.isEmpty());verify(b,never()).addMPHP(anyInt(),anyInt());verify(b,never()).getPosition();
  }finally{pending.remove(b);}
 }
 @Test void ordinaryReentryResuppliesTheSameFiveAndStartsFreshGuidance(){
  var leader=mock(Character.class);when(leader.getMapId()).thenReturn(BotMvpqRun.RECRUIT);when(leader.getId()).thenReturn(99);
  var party=mock(net.server.world.Party.class);when(leader.getParty()).thenReturn(party);
  var manager=mock(BotManager.class);var bots=new java.util.ArrayList<Character>();var members=new java.util.ArrayList<net.server.world.PartyCharacter>();
  for(int i=1;i<=5;i++){var b=mock(Character.class);when(b.getId()).thenReturn(i);when(b.getMapId()).thenReturn(BotMvpqRun.RECRUIT);bots.add(b);var pc=mock(net.server.world.PartyCharacter.class);when(pc.getId()).thenReturn(i);members.add(pc);var entry=new BotEntry(b,leader,null);when(manager.getEntryByBotCharId(i)).thenReturn(entry);}
  when(party.getMembers()).thenReturn(members);
  try(var managers=mockStatic(BotManager.class);var crews=mockStatic(BotMvpqRun.class);var provision=mockStatic(BotMagatiaRun.class)){
   managers.when(BotManager::getInstance).thenReturn(manager);for(var b:bots)crews.when(()->BotMvpqRun.belongsTo(b,99)).thenReturn(true);
   assertNull(BotMvpqCompanions.refresh(leader));
   crews.verify(()->BotMvpqRun.start(leader,bots,true));for(var b:bots){var entry=manager.getEntryByBotCharId(b.getId());provision.verify(()->BotMagatiaRun.provision(entry,b));}
  }
 }
 @Test void humanBeginnerLeaderIsNeverProvisionedOrJobChanged(){
  var leader=mock(Character.class);when(leader.getId()).thenReturn(99);when(leader.getJob()).thenReturn(client.Job.BEGINNER);
  var party=mock(net.server.world.Party.class);when(party.getLeaderId()).thenReturn(99);when(party.getMembers()).thenReturn(java.util.Collections.nCopies(6,mock(net.server.world.PartyCharacter.class)));when(leader.getParty()).thenReturn(party);
  var manager=mock(BotManager.class);var bots=new java.util.ArrayList<Character>();
  for(int i=1;i<=5;i++){var b=mock(Character.class);when(b.getId()).thenReturn(i);when(b.getMapId()).thenReturn(BotMvpqRun.RECRUIT);when(b.getLevel()).thenReturn(150);when(b.getClient()).thenReturn(new BotClient(0,1));var entry=new BotEntry(b,leader,null);when(manager.getEntryByBotCharId(i)).thenReturn(entry);bots.add(b);}
  try(var managers=mockStatic(BotManager.class);var preparation=mockStatic(BotMvpqCompanions.class)){
   managers.when(BotManager::getInstance).thenReturn(manager);BotMvpqRun.start(leader,bots,true);
   verify(manager,never()).getEntryByBotCharId(99);verify(leader,never()).changeJob(any(client.Job.class));
   assertEquals(client.Job.BEGINNER,leader.getJob());
  }finally{for(var b:bots)BotMvpqRun.unregister(b);}
 }
}
