package server.bots;
import client.Character;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotMagatiaRunTest {
 @Test void waitingForTheHumanDoesNotStopParallelPuzzleRooms()throws Exception {
  var human=mock(Character.class,RETURNS_DEEP_STUBS);when(human.getId()).thenReturn(99);
  when(human.getMapId()).thenReturn(926100301);when(human.getClient().getChannel()).thenReturn(1);
  when(human.getWorldServer().getPlayerStorage().getCharacterById(99)).thenReturn(human);
  var bot=mock(Character.class);when(bot.getId()).thenReturn(100);when(bot.getMapId()).thenReturn(926100302);
  when(bot.getClient()).thenReturn(new client.BotClient(0,1));when(bot.getPosition()).thenReturn(new java.awt.Point(0,120));
  var party=mock(net.server.world.Party.class);when(party.getLeaderId()).thenReturn(99);when(human.getParty()).thenReturn(party);when(bot.getParty()).thenReturn(party);
  var event=mock(scripting.event.EventInstanceManager.class);when(bot.getEventInstance()).thenReturn(event);when(human.getEventInstance()).thenReturn(event);
  var s=state();s.room=1;s.crew.human=true;s.crew.leader=human;s.crew.base=926100000;s.crew.event=event;s.crew.bots=java.util.List.of(bot);
  var field=BotMagatiaRun.class.getDeclaredField("RUNS");field.setAccessible(true);
  @SuppressWarnings("unchecked") var runs=(java.util.Map<Character,BotMagatiaRun.State>)field.get(null);runs.put(bot,s);
  var e=new BotEntry(bot,human,null);
  try(var run=mockStatic(BotMagatiaRun.class,CALLS_REAL_METHODS);var guide=mockStatic(BotPqStageGuide.class);var cures=mockStatic(BotLpqCures.class)) {
   run.when(()->BotMagatiaRun.portal(e,bot,"pt00",false)).thenReturn(true);
   assertTrue(BotMagatiaRun.tick(e,bot,false));run.verify(()->BotMagatiaRun.portal(e,bot,"pt00",false));
  } finally {runs.remove(bot);}
 }
 @Test void onlyAnExplicitlyRegisteredCrewConsumesTicks() {
  var bot=mock(Character.class);when(bot.getMapId()).thenReturn(926100000);
  assertFalse(BotMagatiaRun.tick(null,bot,false));
  assertFalse(BotMagatiaRun.canLoot(bot,4001132));
 }
 @Test void puzzleRowsFollowRealLandingHeights() {
  assertEquals(0,BotMagatiaRun.rowAt(120));assertEquals(0,BotMagatiaRun.rowAt(-52));
  assertEquals(1,BotMagatiaRun.rowAt(-352));assertEquals(9,BotMagatiaRun.rowAt(-2722));
  assertEquals(10,BotMagatiaRun.rowAt(-3042));
 }
 private BotMagatiaRun.State state() {
  var s=new BotMagatiaRun.State();s.crew=new BotMagatiaRun.Crew();s.room=0;
  for(var row:s.crew.learned)java.util.Arrays.fill(row,-1);return s;
 }
 @Test void puzzleStartsWithTrialsAndRemembersFailures() {
  var s=state();assertEquals(0,BotMagatiaRun.chooseColumn(s,0));
  s.attempts[0]=1;assertEquals(1,BotMagatiaRun.chooseColumn(s,0));
  s.attempts[0]=7;assertEquals(3,BotMagatiaRun.chooseColumn(s,0));
 }
 @Test void confirmedOwnSolutionSurvivesAResetToTheBottom() {
  var s=state();s.known[3]=2;s.attempts[3]=15;assertEquals(2,BotMagatiaRun.chooseColumn(s,3));
 }
 @Test void puzzleEliminatesOnlyTeammatesObservedColumns() {
  var s=state();s.crew.learned[1][4]=0;s.crew.learned[2][4]=2;s.crew.learned[3][4]=3;
  assertEquals(1,BotMagatiaRun.chooseColumn(s,4));assertEquals(0,BotMagatiaRun.chooseColumn(s,5));
 }
 @Test void nullAndOutsideCharactersAreNotAnInstance() {
  assertFalse(BotMagatiaRun.inInstance(null));var bot=mock(Character.class);
  when(bot.getMapId()).thenReturn(261000011);assertFalse(BotMagatiaRun.inInstance(bot));
 }
 @Test void aHumanArrivingLateReceivesConfirmedHintsOnce()throws Exception {
  var s=state();var c=s.crew;c.human=true;c.leader=mock(Character.class);c.base=926100000;when(c.leader.getMapId()).thenReturn(926100304);
  c.event=mock(scripting.event.EventInstanceManager.class);when(c.event.getIntProperty("statusStg5")).thenReturn(15);
  c.rooms[0]=10;c.rooms[1]=11;c.rooms[2]=12;c.learned[0][2]=0;c.learned[1][2]=3;c.learned[2][2]=1;
  var share=BotMagatiaRun.class.getDeclaredMethod("shareClue",BotMagatiaRun.Crew.class,int.class);share.setAccessible(true);
  share.invoke(null,c,2);share.invoke(null,c,2);
  verify(c.leader,times(1)).dropMessage(5,"MPQ: Your puzzle row 3 is platform 3 from the left, based on our confirmed portals. Each row uses all four platform positions exactly once across the four rooms. We share our discoveries to help you find yours.");
 }
 @Test void incompleteObservationsDoNotInventHumanPuzzleAnswers()throws Exception {
  var s=state();var c=s.crew;c.human=true;c.leader=mock(Character.class);c.base=926100000;when(c.leader.getMapId()).thenReturn(926100304);
  c.event=mock(scripting.event.EventInstanceManager.class);when(c.event.getIntProperty("statusStg5")).thenReturn(15);
  c.rooms[0]=10;c.rooms[1]=11;c.rooms[2]=12;c.learned[0][0]=1;
  var share=BotMagatiaRun.class.getDeclaredMethod("shareClue",BotMagatiaRun.Crew.class,int.class);share.setAccessible(true);share.invoke(null,c,0);
  verify(c.leader,never()).dropMessage(anyInt(),anyString());
 }
 @Test void aHumanCanTakeAReservedRoomBeforeTheBotArrives()throws Exception {
  var s=state();var c=s.crew;var bot=mock(Character.class);when(bot.getId()).thenReturn(42);
  when(bot.getMap()).thenReturn(mock(server.maps.MapleMap.class));
  c.rooms[0]=42;c.rooms[1]=43;c.rooms[2]=44;c.event=mock(scripting.event.EventInstanceManager.class);
  when(c.event.getIntProperty("statusStg5")).thenReturn(1);
  var method=BotMagatiaRun.class.getDeclaredMethod("chooseRoom",BotEntry.class,Character.class,BotMagatiaRun.State.class,boolean.class);method.setAccessible(true);
  var entry=new BotEntry(bot,bot,null);
  try(var movement=mockStatic(BotOpqRun.class)) {
   method.invoke(null,entry,bot,s,false);assertEquals(3,s.room);assertEquals(0,c.rooms[0]);assertEquals(42,c.rooms[3]);
  }
 }

 @Test void wrongFirstRowPortalIsRememberedEvenWhenItLandsOnRowZero() throws Exception {
  var s=state();var bot=mock(Character.class);var map=mock(server.maps.MapleMap.class);
  var door=mock(server.maps.Portal.class);when(bot.getMap()).thenReturn(map);when(map.getPortal("pt00")).thenReturn(door);
  when(bot.getMapId()).thenReturn(926100301);
  var pos=new java.util.concurrent.atomic.AtomicReference<>(new java.awt.Point(-298,-52));
  when(bot.getPosition()).thenAnswer(a->pos.get());var entry=new BotEntry(bot,null,null);
  var method=BotMagatiaRun.class.getDeclaredMethod("puzzle",BotEntry.class,Character.class,BotMagatiaRun.State.class,boolean.class);method.setAccessible(true);
  try(var travel=mockStatic(BotTravelManager.class)) {
   travel.when(()->BotTravelManager.walkToPortalAndEnter(eq(entry),eq(bot),eq(door),anyLong(),eq(true))).thenAnswer(a->{entry.followTravelEnteredAtMs++;pos.set(new java.awt.Point(-300,125));return true;});
   method.invoke(null,entry,bot,s,true);
  }
  assertEquals(1,s.attempts[0]);assertEquals(-1,s.puzzleRow);assertEquals(1,BotMagatiaRun.chooseColumn(s,0));
 }
 @Test void walkingTowardFirstRowPortalDoesNotCountAsAFailedAttempt() throws Exception {
  var s=state();var bot=mock(Character.class);var map=mock(server.maps.MapleMap.class);
  var door=mock(server.maps.Portal.class);when(bot.getMap()).thenReturn(map);when(map.getPortal("pt00")).thenReturn(door);
  when(bot.getMapId()).thenReturn(926100301);
  var pos=new java.util.concurrent.atomic.AtomicReference<>(new java.awt.Point(-200,125));
  when(bot.getPosition()).thenAnswer(a->pos.get());var entry=new BotEntry(bot,null,null);
  var method=BotMagatiaRun.class.getDeclaredMethod("puzzle",BotEntry.class,Character.class,BotMagatiaRun.State.class,boolean.class);method.setAccessible(true);
  try(var travel=mockStatic(BotTravelManager.class)) {
   travel.when(()->BotTravelManager.walkToPortalAndEnter(eq(entry),eq(bot),eq(door),anyLong(),eq(true))).thenAnswer(a->{pos.set(new java.awt.Point(-209,125));return true;});
   method.invoke(null,entry,bot,s,true);
  }
  assertEquals(0,s.attempts[0]);assertEquals(0,BotMagatiaRun.chooseColumn(s,0));
 }

 @Test void puzzleHintsStopAfterHumanLeavesTheirRoom()throws Exception {
  var s=state();var c=s.crew;c.human=true;c.base=926100000;c.leader=mock(Character.class);
  when(c.leader.getMapId()).thenReturn(926100401);
  c.learned[0][0]=0;c.learned[1][0]=1;c.learned[2][0]=2;
  var share=BotMagatiaRun.class.getDeclaredMethod("shareClue",BotMagatiaRun.Crew.class,int.class);share.setAccessible(true);
  share.invoke(null,c,0);verify(c.leader,never()).dropMessage(anyInt(),anyString());
 }

 @Test void completedRewardExitRunsBeforeDetachedPartyAndHumanWaitGates()throws Exception {
  for(int base:new int[]{926100000,926110000}) {
   var human=mock(Character.class);when(human.getMapId()).thenReturn(261000011);
   var bot=mock(Character.class);when(bot.getMapId()).thenReturn(base+600);
   var event=mock(scripting.event.EventInstanceManager.class);when(event.isEventCleared()).thenReturn(true);when(bot.getEventInstance()).thenReturn(event);
   var state=state();state.crew.human=true;state.crew.leader=human;state.crew.base=base;state.crew.event=event;
   var entry=new BotEntry(bot,human,null);
   var field=BotMagatiaRun.class.getDeclaredField("RUNS");field.setAccessible(true);
   @SuppressWarnings("unchecked") var runs=(java.util.Map<Character,BotMagatiaRun.State>)field.get(null);runs.put(bot,state);
   try(var travel=mockStatic(BotTravelManager.class)) {
    travel.when(()->BotTravelManager.tickApproachNpc(eq(entry),eq(bot),eq(base+600),eq(2112018),eq(2),eq(true),eq(false),eq(140))).thenReturn(BotTravelManager.ApproachStatus.WALKING);
    assertTrue(BotMagatiaRun.tick(entry,bot,true));assertTrue(state.rewardSeen);
    travel.verify(()->BotTravelManager.tickApproachNpc(eq(entry),eq(bot),eq(base+600),eq(2112018),eq(2),eq(true),eq(false),eq(140)));
    verify(human,never()).getParty();
   } finally {runs.remove(bot);}
  }
 }
}


