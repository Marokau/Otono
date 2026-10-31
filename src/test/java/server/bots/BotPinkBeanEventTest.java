package server.bots;
import client.Character;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import server.life.*;
import server.maps.*;
import javax.script.*;
import java.awt.Point;
import java.nio.file.*;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class BotPinkBeanEventTest {
 private ScriptEngine open(EventInstanceManager event)throws Exception{var engine=new ScriptEngineManager().getEngineByName("graal.js");var vars=engine.getBindings(ScriptContext.ENGINE_SCOPE);vars.put("polyglot.js.allowHostAccess",true);vars.put("polyglot.js.allowHostClassLookup",true);vars.put("event",event);engine.eval(Files.readString(Path.of("scripts/event/PinkBeanBattle.js")));return engine;}
 @Test void fifthWaveAwakensAllFiveRealStatuesAndLeavesDormantBeanAlone()throws Exception{
  var event=mock(EventInstanceManager.class);var map=mock(MapleMap.class);when(event.getMapInstance(270050100)).thenReturn(map);when(event.getProperty("stage")).thenReturn("5");
  try(var factory=mockStatic(LifeFactory.class)){
   for(int id=8820002;id<=8820006;id++){var old=mock(Monster.class);var awake=mock(Monster.class);when(old.getId()).thenReturn(id+17);when(old.getMap()).thenReturn(map);when(old.getPosition()).thenReturn(new Point(5,-43));when(map.getMonsterById(id+17)).thenReturn(old);int key=id;factory.when(()->LifeFactory.getMonster(key)).thenReturn(awake);}
   open(event).eval("startWave(event);");for(int id=8820002;id<=8820006;id++){verify(map).killMonster(id+17);int key=id;factory.verify(()->LifeFactory.getMonster(key));}verify(map,never()).killMonster(8820000);
  }
 }
 @Test void completedFifthWaveUsesNativeGroundDropAtTheReactor()throws Exception{
  var event=mock(EventInstanceManager.class,RETURNS_DEEP_STUBS);var map=mock(MapleMap.class);var statue=mock(Monster.class);var reactor=mock(Reactor.class);var leader=mock(Character.class);when(statue.getId()).thenReturn(8820002);when(statue.getMap()).thenReturn(map);when(event.getIntProperty("stage")).thenReturn(5);when(event.getMapFactory().getMap(270050100)).thenReturn(map);when(map.getReactorById(2708000)).thenReturn(reactor);when(reactor.getPosition()).thenReturn(new Point(2,-75));when(event.getPlayers()).thenReturn(List.of(leader));var engine=open(event);engine.put("statue",statue);engine.eval("monsterKilled(statue,event);");verify(map).spawnItemDrop(eq(leader),eq(leader),argThat(item->item.getItemId()==4001193&&item.getQuantity()==1),eq(new Point(2,-75)),eq(true),eq(true));verify(map,never()).killMonster(8820000);verify(event,never()).setEventCleared();
 }
 @Test void onlyTheRealBodyDeathSignalsNativeVictory()throws Exception{
  var event=mock(EventInstanceManager.class);var map=mock(MapleMap.class);var body=mock(Monster.class);when(body.getId()).thenReturn(8820001);when(body.getMap()).thenReturn(map);when(map.getId()).thenReturn(270050100);var engine=open(event);engine.put("body",body);engine.eval("monsterKilled(body,event);");verify(event).setIntProperty("defeatedBoss",1);verify(event).clearPQ();verify(map).killAllMonsters();
 }
 @Test void historicalPinkBeanTimerIs60Minutes()throws Exception{var event=mock(EventInstanceManager.class);var engine=open(event);assertEquals(60,((Number)engine.eval("eventTime")).intValue());}

 @Test void fiveDeathsArePerCharacterAndOnlyAnExhaustedWholeCrewEndsTheEvent()throws Exception{
  var event=mock(EventInstanceManager.class);var first=mock(Character.class);var second=mock(Character.class);when(first.getId()).thenReturn(1);when(second.getId()).thenReturn(2);when(event.getPlayers()).thenReturn(List.of(first,second));var values=new java.util.HashMap<String,Integer>();when(event.getProperty(anyString())).thenAnswer(call->values.containsKey(call.getArgument(0))?String.valueOf(values.get(call.getArgument(0))):null);when(event.getIntProperty(anyString())).thenAnswer(call->values.getOrDefault(call.getArgument(0),0));doAnswer(call->{values.put(call.getArgument(0),call.getArgument(1));return null;}).when(event).setIntProperty(anyString(),anyInt());var engine=open(event);engine.put("first",first);engine.put("second",second);
  engine.eval("for(var n=0;n<5;n++) playerDead(event,first);");assertEquals(5,values.get("pinkDeaths:1"));assertEquals(false,engine.eval("playerRevive(event,first)"));assertEquals(true,engine.eval("playerRevive(event,second)"));verify(event,never()).dispose();
  engine.eval("for(var n=0;n<5;n++) playerDead(event,second);");assertEquals(10,values.get("fallenPlayers"));verify(event).dispose();
 }
 @Test void auditHooksRecordResultsAndScheduleCompactSnapshots()throws Exception{
  var event=mock(EventInstanceManager.class);var engine=open(event);
  try(var audit=mockStatic(BotPinkBeanAudit.class)){
   engine.eval("afterSetup(event); auditSnapshot(event); clearPQ(event); scheduledTimeout(event);");
   audit.verify(()->BotPinkBeanAudit.record(event,"START"));
   audit.verify(()->BotPinkBeanAudit.record(event,"PROGRESS"));
   audit.verify(()->BotPinkBeanAudit.record(event,"CLEAR"));
   audit.verify(()->BotPinkBeanAudit.record(event,"TIMEOUT"));
   verify(event,times(2)).schedule("auditSnapshot",30000);
  }
 }
}
