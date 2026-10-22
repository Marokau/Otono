package server.bots;
import client.Character;
import client.Job;
import org.junit.jupiter.api.Test;
import javax.script.*;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WhereLevelPqMenuTest {
    public static class Npc {
        final Character player=mock(Character.class);
        String text,confirmation;
        int warped;
        java.awt.Point landing;
        public Npc() {
            doAnswer(call->{warped=((server.maps.MapleMap)call.getArgument(0)).getId();
                landing=call.getArgument(1);return null;}).when(player).changeMap(any(server.maps.MapleMap.class),any(java.awt.Point.class));
        }
        public server.maps.MapleMap getMap(int id) {
            var map=mock(server.maps.MapleMap.class);when(map.getId()).thenReturn(id);return map;
        }
        public Character getPlayer(){return player;}
        public void sendSimple(String s){text=s;}
        public void sendYesNo(String s){confirmation=s;}
        public void warp(int map,int portal){warped=map;}
        public void dispose(){}
    }
    ScriptEngine open(Npc npc,boolean gm)throws Exception {
        when(npc.player.getLevel()).thenReturn(35);when(npc.player.getJob()).thenReturn(Job.BEGINNER);
        when(npc.player.isGM()).thenReturn(gm);
        var engine=new ScriptEngineManager().getEngineByName("graal.js");
        var bindings=engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowHostAccess",true);bindings.put("polyglot.js.allowHostClassLookup",true);bindings.put("cm",npc);
        engine.eval(Files.readString(Path.of("scripts/npc/wherelevel.js")));engine.eval("start(); action(1,4,6);");return engine;
    }
    @Test void eachPqOpensSummaryAndConfirmedTeleportToItsRecruitMap()throws Exception {
        int[] maps={100000200,103000000,980000000,221024500,300030100,200080101,220000000,980030000,251010404,261000011,980010000,670010100,101030104,610030020,674030100,240050000,970030000,193000000,889100000,889100010,889100020,922240200,925020001,211000001,925010200,220050300};
        for(int i=0;i<maps.length;i++) {
            var npc=new Npc();var e=open(npc,true);
            assertTrue(npc.text.contains("Henesys PQ: Levels 10-255, 3-6 players"));
            assertTrue(npc.text.contains("Carnival PQ: Levels 30-50, 2-6/team"));
            assertTrue(npc.text.contains("Carnival PQ 2: Levels 51-70, 2-6/team"));
            e.eval("action(1,4,"+i+");");
            assertTrue(npc.text.contains("Teleport to "));assertTrue(npc.text.contains("#m"+maps[i]+"#"));
            assertTrue(npc.text.contains("Menu navigation"));
            e.eval("action(1,4,1);");assertNotNull(npc.confirmation);assertEquals(0,npc.warped);
            e.eval("action(1,1,0);");assertEquals(maps[i],npc.warped);
            if(maps[i]==220000000)assertEquals(new java.awt.Point(315,-705),npc.landing);
            else assertNull(npc.landing);
        }
    }
    @Test void backAndCancelDoNotTeleportAndNonGmCannotForgeTravel()throws Exception {
        var npc=new Npc();var e=open(npc,true);
        e.eval("action(1,4,0); action(1,4,0);");
        assertTrue(npc.text.contains("Choose an activity"));
        e.eval("action(1,4,0); action(1,4,1); action(0,1,0);");assertEquals(0,npc.warped);
        var regular=new Npc();e=open(regular,false);e.eval("action(1,4,0);");
        assertFalse(regular.text.contains("Teleport to "));
        e.eval("action(1,4,1); page='tourWarpConfirm'; pendingMap=100000200; action(1,1,0);");
        assertEquals(0,regular.warped);
    }
    @Test void expeditionPageOpensSummariesAndConfirmedEntrances()throws Exception {
        int[] maps={211042400,240050400,270050000,105100100,551030100,801040004,220080000,541010060};
        for(int i=0;i<maps.length;i++) {
            var npc=new Npc();var e=open(npc,true);
            e.eval("action(1,4,900); action(1,4,7);");
            assertTrue(npc.text.contains("Expeditions & Bosses"));
            e.eval("action(1,4,"+i+");");
            assertTrue(npc.text.contains("#m"+maps[i]+"#"));
            assertTrue(npc.text.contains("Back to Expeditions"));
            assertFalse(npc.text.contains("#p0#"));
            assertTrue(npc.text.contains(i==0 ? "Dedicated bot crew available." : "Dedicated bot crew not implemented yet."));
            e.eval("action(1,4,0);");assertTrue(npc.text.contains("Expeditions & Bosses"));
            e.eval("action(1,4,"+i+"); action(1,4,1); action(1,1,0);");
            assertEquals(maps[i],npc.warped);
        }
        var regular=new Npc();var e=open(regular,false);
        e.eval("action(1,4,900); action(1,4,7); action(1,4,0);");
        assertFalse(regular.text.contains("Teleport to "));
        e.eval("action(1,4,1);");assertEquals(0,regular.warped);
    }
}
