package server.bots;

import org.junit.jupiter.api.Test;
import javax.script.ScriptEngineManager;
import javax.script.ScriptContext;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class BotOpqScriptTest {
    public static final class TrialEvent {
        public String getProperty(String key) { return "01111111111111111"; }
        public void setProperty(String key, String value) {}
    }
    public static final class TrialPortal {
        public String getName() { return "rp062"; }
        public int getId() { return 45; }
    }
    public static final class TrialInteraction {
        int checkpoint = -1;
        public TrialEvent getEventInstance() { return new TrialEvent(); }
        public TrialPortal getPortal() { return new TrialPortal(); }
        public int getMapId() { return 920010700; }
        public void playPortalSound() {}
        public void warp(int map, int portal) { checkpoint = portal; }
        public void warp(int map, String portal) { throw new IllegalArgumentException("Expected an integer checkpoint: " + portal); }
    }
    @Test void failedAscentRowUsesAnIntegerCheckpointWithTheRealScriptEngine() throws Exception {
        var engine = new ScriptEngineManager().getEngineByName("graal.js");
        var bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowHostAccess", true);
        bindings.put("polyglot.js.allowHostClassLookup", true);
        var interaction = new TrialInteraction();
        bindings.put("pi", interaction);
        engine.eval(Files.readString(Path.of("scripts/portal/party3_r6pt.js")));
        engine.eval("enter(pi);");
        assertEquals(1, interaction.checkpoint);
    }
    @Test void minervaRestorationCompletesItsNormalExpAndTimerActions() throws Exception {
        var engine = new ScriptEngineManager().getEngineByName("graal.js");
        engine.eval("""
                var awarded = 0, timer = 0, cleared = false;
                var event = { clearPQ: function() { cleared = true; }, setProperty: function(k,v) {},
                  giveEventPlayersExp: function(n) { awarded = n; }, showClearEffect: function(v) {},
                  startEventTimer: function(n) { timer = n; } };
                var rm = { getEventInstance: function() { return event; }, spawnNpc: function(id) {} };
                """);
        engine.eval(Files.readString(Path.of("scripts/reactor/2006001.js")));
        engine.eval("act();");
        assertEquals(3500, ((Number)engine.get("awarded")).intValue());
        assertEquals(300000, ((Number)engine.get("timer")).intValue());
        assertEquals(true, engine.get("cleared"));
    }
}
