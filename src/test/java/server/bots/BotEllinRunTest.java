package server.bots;
import client.Character;
import client.Job;
import org.junit.jupiter.api.Test;
import server.life.Monster;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.awt.Point;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotEllinRunTest {
 @Test void preparationPublishesTheWholePartyBeforeResumingAnyBot()throws Exception {
  var leader=mock(Character.class);var client=mock(client.Client.class);when(leader.getClient()).thenReturn(client);when(client.getChannel()).thenReturn(2);
  var party=mock(net.server.world.Party.class);when(leader.getParty()).thenReturn(party);when(leader.getId()).thenReturn(900);when(party.getLeaderId()).thenReturn(900);
  when(party.getMembers()).thenReturn(java.util.List.of(mock(net.server.world.PartyCharacter.class),mock(net.server.world.PartyCharacter.class),mock(net.server.world.PartyCharacter.class),mock(net.server.world.PartyCharacter.class)));
  var bots=new java.util.ArrayList<Character>();var manager=mock(BotManager.class);
  for(int i=0;i<3;i++) {
   var b=mock(Character.class);when(b.getId()).thenReturn(100+i);when(b.getClient()).thenReturn(new client.BotClient(0,2));
   when(b.getMapId()).thenReturn(BotEllinRun.RECRUIT);when(b.getLevel()).thenReturn(45);when(b.getJob()).thenReturn(Job.SPEARMAN);
   bots.add(b);var entry=new BotEntry(b,leader,null);when(manager.getEntryByBotCharId(b.getId())).thenReturn(entry);
  }
  doAnswer(invocation->{for(var b:bots)assertTrue(runs().containsKey(b));return null;}).when(manager).resumeFromOperatorCommand(any());
  try(var managers=mockStatic(BotManager.class);var builds=mockStatic(BotBuildManager.class);var supplies=mockStatic(BotMagatiaRun.class)) {
   managers.when(BotManager::getInstance).thenReturn(manager);BotEllinRun.startHuman(leader,bots);
   verify(manager,times(3)).applyOperatorCommand(any(),eq(BotEntry.OperatorCmd.IDLE),eq(-1),eq(0));
   verify(manager,times(3)).resumeFromOperatorCommand(any());
  } finally {for(var b:bots)BotEllinRun.unregister(b);}
 }
 @Test void humanKeepsTheStageTransitionAndMarbleTurnIn()throws Exception {
  var b=mock(Character.class);var h=mock(Character.class);var map=mock(server.maps.MapleMap.class);
  when(b.getMap()).thenReturn(map);when(b.getMapId()).thenReturn(930000400);when(h.getMapId()).thenReturn(930000400);
  when(h.getItemQuantity(4001169,false)).thenReturn(20);when(map.getDroppedItems()).thenReturn(java.util.List.of());
  var e=new BotEntry(b,h,null);var s=new BotEllinRun.State();s.crew=new BotEllinRun.Crew();s.crew.human=true;s.crew.leader=h;s.crew.bots=java.util.List.of(b);
  runs().put(b,s);
  var capture=BotEllinRun.class.getDeclaredMethod("capture",BotEntry.class,Character.class,BotEllinRun.State.class,boolean.class);capture.setAccessible(true);
  var advance=BotEllinRun.class.getDeclaredMethod("advance",BotEntry.class,Character.class,String.class,boolean.class);advance.setAccessible(true);
  try(var holding=mockStatic(BotOpqRun.class);var portals=mockStatic(BotMagatiaRun.class);var npcs=mockStatic(BotNpcInteraction.class)) {
   capture.invoke(null,e,b,s,false);advance.invoke(null,e,b,"east00",false);
   portals.verifyNoInteractions();npcs.verifyNoInteractions();
   when(h.getMapId()).thenReturn(930000500);advance.invoke(null,e,b,"east00",false);
   portals.verify(()->BotMagatiaRun.portal(e,b,"east00",false));
  } finally { BotEllinRun.unregister(b); }
 }
 @Test void rewardExitStillWorksAfterNormalEventDisposal()throws Exception {
  var b=mock(Character.class);var map=mock(server.maps.MapleMap.class);when(b.getMap()).thenReturn(map);when(b.getMapId()).thenReturn(930000800);
  when(map.getAllReactors()).thenReturn(java.util.List.of());when(map.getDroppedItems()).thenReturn(java.util.List.of());
  var e=new BotEntry(b,b,null);var s=new BotEllinRun.State();s.crew=new BotEllinRun.Crew();s.crew.leader=b;runs().put(b,s);
  try(var portals=mockStatic(BotMagatiaRun.class)) {
   BotEllinRun.tick(e,b,false);assertTrue(s.finished);portals.verify(()->BotMagatiaRun.portal(e,b,"east00",false));
  } finally { BotEllinRun.unregister(b); }
 }
 @Test void mazeRecognizesTheSuccessfulCompartmentBeforeReachingEllinsSprite()throws Exception {
  var b=mock(Character.class);var map=mock(server.maps.MapleMap.class);when(b.getMap()).thenReturn(map);
  when(b.getPosition()).thenReturn(new java.awt.Point(1389,1770));when(b.getMapId()).thenReturn(930000300);
  var npc=mock(server.life.NPC.class);when(npc.getId()).thenReturn(2133001);when(npc.getPosition()).thenReturn(new java.awt.Point(1772,1612));
  when(map.getMapObjects()).thenReturn(java.util.List.of(npc));
  var goal=mock(server.maps.Portal.class);when(goal.getName()).thenReturn("16st");when(goal.getPosition()).thenReturn(new java.awt.Point(1471,1754));
  var other=mock(server.maps.Portal.class);when(other.getName()).thenReturn("15st");when(other.getPosition()).thenReturn(new java.awt.Point(1084,1609));
  when(map.getPortals()).thenReturn(java.util.List.of(goal,other));
  var e=new BotEntry(b,b,null);var method=BotEllinRun.class.getDeclaredMethod("maze",BotEntry.class,Character.class,BotEllinRun.State.class,boolean.class);method.setAccessible(true);
  try(var travel=mockStatic(BotTravelManager.class)) {
   travel.when(()->BotTravelManager.tickApproachNpc(e,b,930000300,2133001,2,false,false,140)).thenReturn(BotTravelManager.ApproachStatus.WALKING);
   method.invoke(null,e,b,new BotEllinRun.State(),false);
   travel.verify(()->BotTravelManager.tickApproachNpc(e,b,930000300,2133001,2,false,false,140));
  }
 }
 @Test void reactorApproachTargetsARealFloorInsideTheItemArea()throws Exception {
  var method=BotEllinRun.class.getDeclaredMethod("dropAt",BotEntry.class,Character.class,BotEllinRun.State.class,server.maps.Reactor.class,int.class,boolean.class);
  method.setAccessible(true);
  for(int mapId:new int[]{930000200,930000600}) {
   var map=BotNavigationMapLoader.loadMapGeometry(mapId);var b=mock(Character.class);when(b.getMap()).thenReturn(map);
   when(b.getPosition()).thenReturn(new java.awt.Point(-180,150));var e=new BotEntry(b,b,null);
   var r=mock(server.maps.Reactor.class);
   var area=mapId==930000200?new java.awt.Rectangle(955,137,202,81):new java.awt.Rectangle(301,86,174,70);
   when(r.getArea()).thenReturn(area);
   var expected=BotPhysicsEngine.findGroundPoint(map,new java.awt.Point((int)area.getCenterX(),area.y));
   assertNotNull(expected);assertTrue(area.contains(expected));
   try(var walking=mockStatic(BotZakumPqRun.class)) {
    method.invoke(null,e,b,new BotEllinRun.State(),r,4001161,false);
    walking.verify(()->BotZakumPqRun.walkTo(e,b,expected,false));
   }
  }
 }
 @SuppressWarnings("unchecked") Map<Character,BotEllinRun.State> runs()throws Exception { Field f=BotEllinRun.class.getDeclaredField("RUNS");f.setAccessible(true);return (Map<Character,BotEllinRun.State>)f.get(null); }
 @Test void populationBotsAreNeverClaimedOrHaveCombatChanged() {
  var b=mock(Character.class);var m=mock(Monster.class);when(b.getMapId()).thenReturn(930000400);
  assertFalse(BotEllinRun.tick(null,b,false));assertTrue(BotEllinRun.canAttack(b,m));assertFalse(BotEllinRun.canLoot(b,4001169));
 }
 @Test void captureUsesThePlayerHandlersStrictFortyPercentBoundary()throws Exception {
  var b=mock(Character.class);when(b.getMapId()).thenReturn(930000400);var m=mock(Monster.class);
  when(m.getId()).thenReturn(9300175);when(m.getMaxHp()).thenReturn(7500);
  runs().put(b,new BotEllinRun.State());
  try { when(m.getHp()).thenReturn(3000);assertTrue(BotEllinRun.canAttack(b,m));when(m.getHp()).thenReturn(2999);assertFalse(BotEllinRun.canAttack(b,m)); }
  finally { BotEllinRun.unregister(b); }
 }
 @Test void reactorDropsAndHumanHandoffsCannotBeReclaimed()throws Exception {
  var b=mock(Character.class);var s=new BotEllinRun.State();s.crew=new BotEllinRun.Crew();s.crew.collector=b;runs().put(b,s);
  try {
   when(b.getMapId()).thenReturn(930000200);assertTrue(BotEllinRun.canLoot(b,4001161));
   s.pendingItem=4001161;s.pending=System.currentTimeMillis()+10000;assertFalse(BotEllinRun.canLoot(b,4001161));
   when(b.getMapId()).thenReturn(930000400);assertFalse(BotEllinRun.canLoot(b,4001169));
   when(b.getMapId()).thenReturn(930000600);assertFalse(BotEllinRun.canLoot(b,4001163));
   s.crew.human=true;assertFalse(BotEllinRun.canLoot(b,4001164));
   s.crew.human=false;assertTrue(BotEllinRun.canLoot(b,4001164));
  } finally { BotEllinRun.unregister(b); }
 }
 @Test void aDroppedPurpleStoneIsReservedForTheHumanInBothLootPaths()throws Exception {
  var b=mock(Character.class);var human=mock(Character.class);when(human.getId()).thenReturn(900);
  var map=mock(server.maps.MapleMap.class);when(b.getMap()).thenReturn(map);when(b.getMapId()).thenReturn(930000500);
  when(b.getEventInstance()).thenReturn(mock(scripting.event.EventInstanceManager.class));when(b.getPosition()).thenReturn(new Point());
  var s=new BotEllinRun.State();s.crew=new BotEllinRun.Crew();s.crew.human=true;s.crew.leader=human;s.crew.collector=b;runs().put(b,s);
  var drop=mock(server.maps.MapItem.class);when(drop.getItemId()).thenReturn(4001163);when(drop.isPlayerDrop()).thenReturn(true);
  when(drop.canBePickedBy(b)).thenReturn(true);when(drop.getPosition()).thenReturn(new Point());when(map.getDroppedItems()).thenReturn(List.of(drop));
  var e=new BotEntry(b,human,null);var collect=BotEllinRun.class.getDeclaredMethod("collect",BotEntry.class,Character.class,Set.class,boolean.class);collect.setAccessible(true);
  try(var movement=mockStatic(BotZakumPqRun.class)) {
   assertFalse(BotLootEligibility.canBotLoot(e,b,drop));assertEquals(false,collect.invoke(null,e,b,Set.of(4001163),true));movement.verifyNoInteractions();
   when(drop.isPlayerDrop()).thenReturn(false);assertTrue(BotLootEligibility.canBotLoot(e,b,drop));assertEquals(true,collect.invoke(null,e,b,Set.of(4001163),true));
   movement.verify(()->BotZakumPqRun.stepTowardsAndPickUp(e,b,drop,true));
  }finally{BotEllinRun.unregister(b);}
 } @Test void instanceDetectionRequiresARealEvent() {
  assertFalse(BotEllinRun.inInstance(null));var b=mock(Character.class);when(b.getMapId()).thenReturn(930000100);assertFalse(BotEllinRun.inInstance(b));
  when(b.getEventInstance()).thenReturn(mock(scripting.event.EventInstanceManager.class));assertTrue(BotEllinRun.inInstance(b));
  when(b.getMapId()).thenReturn(300030100);assertFalse(BotEllinRun.inInstance(b));
 }
}
