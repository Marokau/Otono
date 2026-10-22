package server.bots;
import org.junit.jupiter.api.Test;
import javax.script.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotMagatiaMechanicsScriptTest {
 public static final class Properties {
  final Map<String,String> values=new HashMap<>();
  public void setProperty(String k,String v){values.put(k,v);}
 }
 ScriptEngine engine(String file)throws Exception {
  var e=new ScriptEngineManager().getEngineByName("graal.js");var b=e.getBindings(ScriptContext.ENGINE_SCOPE);
  b.put("polyglot.js.allowHostAccess",true);b.put("polyglot.js.allowHostClassLookup",true);
  e.eval(Files.readString(Path.of(file)));return e;
 }
 @Test void bothEventsGenerateDistinctColumnsForEveryRow()throws Exception {
  for(String side:List.of("A","Z")) {
   var e=engine("scripts/event/MagatiaPQ_"+side+".js");var p=new Properties();e.put("testEvent",p);
   e.eval("generateStg6Combo(testEvent);");
   for(int row=0;row<10;row++) {var seen=new HashSet<java.lang.Character>();
    for(int room=1;room<=4;room++)seen.add(p.values.get("stage6_comb"+room).charAt(row));
    assertEquals(Set.of('0','1','2','3'),seen);
   }
  }
 }
 public static final class Beaker {
  final server.maps.Reactor reactor=mock(server.maps.Reactor.class);
  final scripting.event.EventInstanceManager event=mock(scripting.event.EventInstanceManager.class);
  final server.maps.MapleMap map=mock(server.maps.MapleMap.class);
  final server.maps.Reactor door=mock(server.maps.Reactor.class);
  final client.Client client=mock(client.Client.class);
  public server.maps.Reactor getReactor(){return reactor;}
  public scripting.event.EventInstanceManager getEventInstance(){return event;}
  public server.maps.MapleMap getMap(){return map;}
  public client.Client getClient(){return client;}
 }
 @Test void beakerCountsOnSeventhDropAndOpensDoorOnlyAfterThirdBeaker()throws Exception {
  var e=engine("scripts/reactor/2618000.js");var rm=new Beaker();e.put("rm",rm);
  when(rm.reactor.getState()).thenReturn((byte)5);e.eval("hit();");
  verify(rm.event,never()).setIntProperty(eq("statusStg3"),anyInt());
  when(rm.reactor.getState()).thenReturn((byte)6);when(rm.event.getIntProperty("statusStg3")).thenReturn(2);
  when(rm.map.getReactorByName("rnj2_door")).thenReturn(rm.door);e.eval("hit();");
  verify(rm.event).setIntProperty("statusStg3",3);verify(rm.event).giveEventPlayersStageReward(3);
  verify(rm.door).hitReactor(rm.client);
 }
}
