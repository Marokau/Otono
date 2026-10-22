package server.bots;

import org.junit.jupiter.api.Test;

import javax.script.ScriptContext;
import javax.script.ScriptEngineManager;
import java.nio.file.Files;
import java.nio.file.Path;
import client.Character;
import scripting.event.EventInstanceManager;
import server.maps.MapleMap;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class BotLpqNpcTest {
    public static final class Conversation {
        private final Character player;
        Conversation(Character player) { this.player = player; }
        public Character getPlayer() { return player; }
        public int getMapId() { return player.getMapId(); }
        public EventInstanceManager getEventInstance() { return player.getEventInstance(); }
        public void sendNext(String text) {}
        public void sendOk(String text) {}
        public void dispose() {}
    }

    @Test void gmHumanCompanionLeaderStillNeedsTheActualPuzzleAnswer() throws Exception {
        var human = mock(Character.class);
        var event = mock(EventInstanceManager.class);
        var map = mock(MapleMap.class);
        var areas = new ArrayList<Rectangle>();
        for (int n = 0; n < 9; n++) areas.add(new Rectangle(n * 100, 0, 50, 50));
        when(map.getAreas()).thenReturn(areas);
        when(map.getArea(anyInt())).thenAnswer(call -> areas.get(call.getArgument(0, Integer.class)));
        when(human.getMapId()).thenReturn(922010800);
        when(human.getPosition()).thenReturn(new Point(-100, 0));
        when(human.getMap()).thenReturn(map);
        when(human.getEventInstance()).thenReturn(event);
        when(human.gmLevel()).thenReturn(6);
        when(event.isEventLeader(human)).thenReturn(true);
        when(event.getIntProperty("statusStg8")).thenReturn(0);
        when(event.getProperty("stage8combo")).thenReturn("1,1,1,1,0,1,0,0,0");
        var bots = new ArrayList<Character>();
        for (int n = 0; n < 5; n++) {
            var bot = mock(Character.class);
            when(bot.getId()).thenReturn(n + 1);
            when(bot.getPosition()).thenReturn(new Point(n * 100 + 25, 25));
            bots.add(bot);
        }
        var players = new ArrayList<>(bots);
        players.add(human);
        when(event.getPlayers()).thenReturn(players);
        BotLpqHuman.register(human, bots);
        try {
            var engine = new ScriptEngineManager().getEngineByName("graal.js");
            var bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("polyglot.js.allowHostAccess", true);
            bindings.put("polyglot.js.allowHostClassLookup", true);
            bindings.put("cm", new Conversation(human));
            engine.eval(Files.readString(Path.of("scripts/npc/2040043.js")));
            engine.eval("start();");
            verify(event).showWrongEffect();
            verify(event, never()).setProperty("8stageclear", "true");
        } finally { bots.forEach(BotLpqHuman::unregister); }
    }

    @Test void humanMenuOffersAssemblyEvenWhenTheOldNpcStyleIsEnabled() throws Exception {
        var engine = new ScriptEngineManager().getEngineByName("graal.js");
        var bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowHostAccess", true);
        bindings.put("polyglot.js.allowHostClassLookup", true);
        engine.eval("""
                var shown = '';
                var cm = {
                  getEventManager: function() { return { getProperty: function() { return '35-50'; } }; },
                  isUsingOldPqNpcStyle: function() { return true; },
                  getPlayer: function() { return { getClient: function() { return {}; }, isRecvPartySearchInviteEnabled: function() { return false; } }; },
                  sendSimple: function(text) { shown = text; }
                };
                """);
        engine.eval(Files.readString(Path.of("scripts/npc/2040034.js")));
        engine.eval("start();");
        String text = engine.get("shown").toString();
        assertTrue(text.contains("#L0#I want to participate"));
        assertTrue(text.contains("#L3#Assemble a bot party"));
        assertTrue(text.contains("#L4#Dismiss my bot party"));
    }
}
