package server.partyquest;
import client.Character;import scripting.event.EventInstanceManager;import server.maps.MapleMap;
import org.junit.jupiter.api.Test;import javax.script.*;import java.nio.file.*;import java.awt.Point;
import static org.mockito.Mockito.*;import static org.junit.jupiter.api.Assertions.*;
class MvpqEntryScriptTest {
 public static class Pit {public final Character player=mock(Character.class);public final MapleMap course=mock(MapleMap.class);public Character getPlayer(){return player;}public MapleMap getMap(){return course;}}
 private ScriptEngine script(String path)throws Exception{var e=new ScriptEngineManager().getEngineByName("graal.js");var bindings=e.getBindings(ScriptContext.ENGINE_SCOPE);bindings.put("polyglot.js.allowHostAccess",true);bindings.put("polyglot.js.allowHostClassLookup",true);e.eval(Files.readString(Path.of(path)));return e;}
 @Test void nativeEventEntryUsesTheSameConnectedSpawnForEveryClass()throws Exception{
  var player=mock(Character.class);var event=mock(EventInstanceManager.class);var course=mock(MapleMap.class);when(course.getId()).thenReturn(674030000);when(event.getMapInstance(674030000)).thenReturn(course);
  var engine=script("scripts/event/TreasurePQ.js");((Invocable)engine).invokeFunction("playerEntry",event,player);
  verify(player).changeMap(eq(course),eq(new Point(900,245)));
 }
 @Test void ordinaryPitResetReturnsToTheSameConnectedStart()throws Exception{
  var pit=new Pit();when(pit.course.getId()).thenReturn(674030000);var engine=script("scripts/portal/guyfawkes0_floor.js");assertEquals(Boolean.TRUE,((Invocable)engine).invokeFunction("enter",pit));verify(pit.player).changeMap(eq(pit.course),eq(new Point(900,245)));
 }
 @Test void entryHelperCannotBeUsedToSkipToTheBoss(){var player=mock(Character.class);var wrong=mock(MapleMap.class);when(wrong.getId()).thenReturn(674030200);assertThrows(IllegalArgumentException.class,()->MvpqEntry.enterCourse(player,wrong));verifyNoInteractions(player);}
}
