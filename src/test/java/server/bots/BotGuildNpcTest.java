package server.bots;
import client.Character;
import scripting.event.EventManager;
import org.junit.jupiter.api.Test;
import javax.script.*;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotGuildNpcTest {
 public static class Conversation {
  public final Character player=mock(Character.class);
  public final EventManager event=mock(EventManager.class);
  public String menu="",answer="",question="";public int warped;
  public Character getPlayer(){return player;}public EventManager getEventManager(String name){return event;}
  public void sendSimple(String s){menu=s;}public void sendOk(String s){answer=s;}public void sendYesNo(String s){question=s;}
  public void dispose(){}public void warp(int map,int portal){warped=map;}
 }
 private ScriptEngine open(String npc,Conversation cm)throws Exception {
  var e=new ScriptEngineManager().getEngineByName("graal.js");var b=e.getBindings(ScriptContext.ENGINE_SCOPE);
  b.put("polyglot.js.allowHostAccess",true);b.put("polyglot.js.allowHostClassLookup",true);b.put("cm",cm);
  e.eval(Files.readString(Path.of("scripts/npc/"+npc+".js")));e.eval("start();");return e;
 }
 @Test void guildlessAssemblyExplainsFeeAndWaitsForConsent()throws Exception {
  var cm=new Conversation();try(var crew=mockStatic(BotGuildCompanions.class)){
   crew.when(BotGuildCompanions::requirements).thenReturn("1500000 mesos; six founders");
   crew.when(()->BotGuildCompanions.prepareGuild(cm.player)).thenReturn("cofounders ready");
   var e=open("9040000",cm);assertTrue(cm.menu.contains("BotPQ Menu"));assertTrue(cm.menu.contains("Register your guild"));
   e.eval("action(1,4,900);");assertTrue(cm.question.contains("1500000"));assertEquals(0,cm.warped);
   crew.verify(()->BotGuildCompanions.prepareGuild(cm.player),never());
   e.eval("action(1,1,0);");assertEquals("cofounders ready",cm.answer);crew.verify(()->BotGuildCompanions.prepareGuild(cm.player));
  }
 }
 @Test void decliningGuildSetupDoesNotPrepareBots()throws Exception {
  var cm=new Conversation();try(var crew=mockStatic(BotGuildCompanions.class)){
   crew.when(BotGuildCompanions::requirements).thenReturn("requirements");var e=open("9040000",cm);
   e.eval("action(1,4,902); action(0,1,0);");crew.verify(()->BotGuildCompanions.prepareGuild(cm.player),never());assertEquals(0,cm.warped);
  }
 }
 @Test void existingGuildUsesCrewAssemblyInsteadOfCreation()throws Exception {
  var cm=new Conversation();when(cm.player.getGuildId()).thenReturn(9);
  try(var crew=mockStatic(BotGuildCompanions.class)){
   crew.when(()->BotGuildCompanions.assemble(cm.player)).thenReturn("ready");var e=open("9040000",cm);e.eval("action(1,4,900);");
   assertEquals("ready",cm.answer);crew.verify(()->BotGuildCompanions.prepareGuild(cm.player),never());
  }
 }
 @Test void heracleKeepsNormalChoicesAndOffersReturnAfterGuildExists()throws Exception {
  var cm=new Conversation();when(cm.player.getGuildId()).thenReturn(9);
  try(var crew=mockStatic(BotGuildCompanions.class)){
   crew.when(()->BotGuildCompanions.assemble(cm.player)).thenReturn("ready");var e=open("2010007",cm);
   assertTrue(cm.menu.contains("Create a Guild"));assertTrue(cm.menu.contains("BotPQ Menu"));e.eval("action(1,4,900);");
   assertEquals(BotGuildCompanions.RECRUIT,cm.warped);assertEquals("ready",cm.answer);
  }
 }
}
