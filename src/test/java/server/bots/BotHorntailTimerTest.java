package server.bots;
import org.junit.jupiter.api.Test;
import javax.script.*;
import java.nio.file.*;
import scripting.event.*;
import scripting.reactor.ReactorActionManager;
import server.maps.MapleMap;
import server.life.LifeFactory;
import static org.mockito.Mockito.*;
class BotHorntailTimerTest {
 @Test void normalSetupAndCrystalShareTheRequestedTwelveHourDuration()throws Exception{
  var engine=new ScriptEngineManager().getEngineByName("graal.js");var bindings=engine.getBindings(ScriptContext.ENGINE_SCOPE);bindings.put("polyglot.js.allowHostAccess",true);bindings.put("polyglot.js.allowHostClassLookup",true);
  var em=mock(EventManager.class);var instance=mock(EventInstanceManager.class);var map=mock(MapleMap.class);when(em.newInstance("Horntail1")).thenReturn(instance);when(instance.getInstanceMap(anyInt())).thenReturn(map);when(instance.getIntProperty("mainBodyMinutes")).thenReturn(720);engine.put("em",em);
  try(var life=mockStatic(LifeFactory.class)){
   engine.eval(Files.readString(Path.of("scripts/event/HorntailBattle.js")));((Invocable)engine).invokeFunction("init");((Invocable)engine).invokeFunction("setup",1);
   verify(instance).setIntProperty("mainBodyMinutes",720);verify(instance).startEventTimer(43_200_000L);
   var rm=mock(ReactorActionManager.class,RETURNS_DEEP_STUBS);when(rm.getEventInstance()).thenReturn(instance);when(rm.getReactor().getMap().getMonsterById(8810026)).thenReturn(null);engine.put("rm",rm);engine.eval(Files.readString(Path.of("scripts/reactor/2401000.js")));((Invocable)engine).invokeFunction("act");verify(instance).restartEventTimer(43_200_000L);
  }
 }
}
