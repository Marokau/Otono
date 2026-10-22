package server.bots;

import client.Character;
import client.Client;
import org.junit.jupiter.api.Test;
import scripting.event.EventManager;
import javax.script.*;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotPqRecruitmentScriptTest {
    @Test void ellinKeepsNormalOptionsAboveCrewMenuWithLegacyStyleEnabled()throws Exception {
        var npc=new Npc(BotEllinRun.RECRUIT);
        try(var crews=mockStatic(BotEllinCompanions.class)) {
            crews.when(()->BotEllinCompanions.assemble(npc.player)).thenReturn("ellin ready");
            crews.when(()->BotEllinCompanions.release(npc.player)).thenReturn("ellin saved");
            var engine=open("2133000",npc);
            assertTrue(npc.menu.indexOf("#L3#")<npc.menu.indexOf("#eBotPQ Menu#n"));
            engine.eval("action(1,4,900);");assertEquals("ellin ready",npc.answer);
            engine.eval("start(); action(1,4,901);");assertEquals("ellin saved",npc.answer);
        }
    }
    @Test void carnivalLobbyOffersDedicatedTeamsAndRetainsNormalFieldSelection()throws Exception {
        var npc=new Npc(BotCarnivalRun.RECRUIT);
        try(var crews=mockStatic(BotCarnivalCompanions.class)) {
            crews.when(()->BotCarnivalCompanions.assemble(npc.player)).thenReturn("teams ready");
            crews.when(()->BotCarnivalCompanions.release(npc.player)).thenReturn("teams saved");
            for (int id : new int[]{2042000,2042001,2042002}) {
                var engine=open(Integer.toString(id),npc);
                assertTrue(npc.menu.contains("#L900#Assemble"));
                assertTrue(npc.menu.contains("#L901#Start"));
                assertTrue(npc.menu.contains("#L903#Normal"));
                engine.eval("action(1,4,900);");assertEquals("teams ready",npc.answer);
                engine.eval("start(); action(1,4,902);");assertEquals("teams saved",npc.answer);
            }
        }
    }
    @Test void carnivalTwoLobbyOffersFourPerSideAndNormalFields()throws Exception {
        var npc=new Npc(980030000);
        try(var crews=mockStatic(BotCarnivalCompanions.class)) {
            crews.when(()->BotCarnivalCompanions.assemble(npc.player)).thenReturn("CPQ2 ready");
            crews.when(()->BotCarnivalCompanions.release(npc.player)).thenReturn("CPQ2 saved");
            var engine=open("2042005",npc);
            assertTrue(npc.menu.contains("(4 vs 4)"));
            assertTrue(npc.menu.indexOf("#L903#Normal")<npc.menu.indexOf("BotPQ Menu"));
            engine.eval("action(1,4,900);");assertEquals("CPQ2 ready",npc.answer);
            engine.eval("start(); action(1,4,902);");assertEquals("CPQ2 saved",npc.answer);
        }
    }
    public static final class Npc {
        final Character player=mock(Character.class);
        final EventManager event=mock(EventManager.class);
        final int map;
        final net.server.world.Party party=mock(net.server.world.Party.class);
        final server.maps.MapleMap room=mock(server.maps.MapleMap.class);
        server.expeditions.Expedition expedition;
        String menu,answer;
        Npc(int map){this.map=map;when(player.getClient()).thenReturn(mock(Client.class));when(player.getLevel()).thenReturn(150);when(event.getProperty("party")).thenReturn("51-70");}
        public int getMapId(){return map;}
        public net.server.world.Party getParty(){return party;}
        public server.maps.MapleMap getMap(){return room;}
        public boolean isLeader(){return true;}
        public Character getPlayer(){return player;}
        public Client getClient(){return player.getClient();}
        public EventManager getEventManager(String name){return event;}
        public boolean isUsingOldPqNpcStyle(){return true;}
        public server.expeditions.Expedition getExpedition(server.expeditions.ExpeditionType type){return expedition;}
        public void sendSimple(String text){menu=text;}
        public void sendOk(String text){answer=text;}
        public void dispose(){}
    }
    @Test void adobisKeepsOrdinaryRegistrationAndOffersDedicatedCrewAssembly()throws Exception {
        var npc=new Npc(BotZakumBattle.RECRUIT);
        try(var crews=mockStatic(BotZakumCompanions.class)){
            crews.when(()->BotZakumCompanions.assemble(npc.player)).thenReturn("crew ready");
            crews.when(()->BotZakumCompanions.release(npc.player)).thenReturn("crew saved");
            var engine=open("2030013",npc);
            assertTrue(npc.menu.contains("#L1#Lets get this going"));assertTrue(npc.menu.contains("#L10#Assemble"));
            engine.eval("action(1,4,10);");assertEquals("crew ready",npc.answer);
            engine.eval("status=0; start(); action(1,4,11);");assertEquals("crew saved",npc.answer);
        }
    }
    @Test void adobisRestocksCompanionsBeforeStartingEveryExpedition()throws Exception {
        var npc=new Npc(BotZakumBattle.RECRUIT);
        var engine=open("2030013",npc);
        npc.expedition=mock(server.expeditions.Expedition.class);
        when(npc.event.startInstance(npc.expedition)).thenReturn(true);
        try(var supplies=mockStatic(BotZakumSupplies.class)) {
            engine.eval("status=4; action(1,0,0);");
            supplies.verify(()->BotZakumSupplies.refresh(npc.player));
            verify(npc.event).startInstance(npc.expedition);
        }
    }
    @Test void adobisDoesNotStartAnUnderstockedCrewWhenPreparationFails()throws Exception {
        var npc=new Npc(BotZakumBattle.RECRUIT);var engine=open("2030013",npc);
        npc.expedition=mock(server.expeditions.Expedition.class);
        try(var supplies=mockStatic(BotZakumSupplies.class)) {
            supplies.when(()->BotZakumSupplies.refresh(npc.player)).thenThrow(new IllegalStateException("No room"));
            engine.eval("status=4; action(1,0,0);");
            assertTrue(npc.answer.contains("could not restock"));
            verify(npc.event,never()).startInstance(npc.expedition);
        }
    }
    ScriptEngine open(String script,Npc npc)throws Exception {
        var engine=new ScriptEngineManager().getEngineByName("graal.js");
        var bindings=engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowHostAccess",true);bindings.put("polyglot.js.allowHostClassLookup",true);bindings.put("cm",npc);
        engine.eval(Files.readString(Path.of("scripts/npc/"+script+".js")));engine.eval("start();");return engine;
    }
    @Test void rollyShowsHumanCrewChoicesEvenWithLegacyStyleEnabled()throws Exception {
        var npc=new Npc(BotLmpqRun.RECRUIT);
        try(var crews=mockStatic(BotLmpqCompanions.class)){
            crews.when(()->BotLmpqCompanions.assemble(npc.player)).thenReturn("crew ready");
            crews.when(()->BotLmpqCompanions.release(npc.player)).thenReturn("crew saved");
            var engine=open("9103001",npc);assertTrue(npc.menu.contains("#L3#Assemble"));
            engine.eval("action(1,4,3);");assertEquals("crew ready",npc.answer);
            engine.eval("start(); action(1,4,4);");assertEquals("crew saved",npc.answer);
            crews.verify(()->BotLmpqCompanions.assemble(npc.player));crews.verify(()->BotLmpqCompanions.release(npc.player));
        }
    }
    @Test void wonkyRetainsPrizeChoiceAndAddsHumanCrewAssembly()throws Exception {
        var npc=new Npc(BotOpqRun.RECRUIT_MAP);
        try(var crews=mockStatic(BotOpqCompanions.class)){
            crews.when(()->BotOpqCompanions.assemble(npc.player)).thenReturn("crew ready");
            crews.when(()->BotOpqCompanions.release(npc.player)).thenReturn("crew saved");
            var engine=open("2013000",npc);assertTrue(npc.menu.contains("#L3#I would like to reclaim"));assertTrue(npc.menu.contains("#L4#Assemble"));
            engine.eval("action(1,4,4);");assertEquals("crew ready",npc.answer);
            engine.eval("start(); action(1,4,5);");assertEquals("crew saved",npc.answer);
            crews.verify(()->BotOpqCompanions.assemble(npc.player));crews.verify(()->BotOpqCompanions.release(npc.player));
        }
    }
    @Test void bothMagatiaEntrancesExposeAssemblyAndDismissal()throws Exception {
        for (int id : new int[]{2112003,2112004}) {
            var npc=new Npc(id==2112003?BotMagatiaRun.ALCADNO:BotMagatiaRun.ZENUMIST);
            try(var crews=mockStatic(BotMagatiaCompanions.class)) {
                crews.when(()->BotMagatiaCompanions.assemble(npc.player)).thenReturn("crew ready");
                crews.when(()->BotMagatiaCompanions.release(npc.player)).thenReturn("crew saved");
                var engine=open(Integer.toString(id),npc);
                assertTrue(npc.menu.contains("#L0#I want to participate"));assertTrue(npc.menu.contains("#L3#Assemble"));
                engine.eval("action(1,4,3);");assertEquals("crew ready",npc.answer);
                engine.eval("start(); action(1,4,4);");assertEquals("crew saved",npc.answer);
            }
        }
    }
    @Test void magatiaEntryRestocksThenUsesTheOriginalEventEntry()throws Exception {
        for(int id:new int[]{2112003,2112004}) {
            var npc=new Npc(id==2112003?BotMagatiaRun.ALCADNO:BotMagatiaRun.ZENUMIST);
            when(npc.player.getMap()).thenReturn(npc.room);
            when(npc.event.getEligibleParty(npc.party)).thenReturn(java.util.List.of(mock(net.server.world.PartyCharacter.class)));
            when(npc.event.startInstance(npc.party,npc.room,1)).thenReturn(true);
            var engine=open(Integer.toString(id),npc);
            try(var runs=mockStatic(BotMagatiaRun.class)) {
                engine.eval("action(1,4,0);");runs.verify(()->BotMagatiaRun.refresh(npc.player));
                verify(npc.event).startInstance(npc.party,npc.room,1);
            }
        }
    }
    @Test void magatiaPreparationFailureDoesNotStartAnInstance()throws Exception {
        var npc=new Npc(BotMagatiaRun.ZENUMIST);
        when(npc.player.getMap()).thenReturn(npc.room);
            when(npc.event.getEligibleParty(npc.party)).thenReturn(java.util.List.of(mock(net.server.world.PartyCharacter.class)));
        var engine=open("2112004",npc);
        try(var runs=mockStatic(BotMagatiaRun.class)) {
            runs.when(()->BotMagatiaRun.refresh(npc.player)).thenThrow(new IllegalStateException("Crew still inside"));
            engine.eval("action(1,4,0);");assertTrue(npc.answer.contains("companions to return"));
            verify(npc.event,never()).startInstance(npc.party,npc.room,1);
        }
    }
}
