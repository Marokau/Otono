package server.bots;
import client.Character;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotPqChatTest {
    @Test void zakumPhaseSpeechIsCheckedAgainWhenDelivered() {
        var bot=mock(Character.class);var event=mock(EventInstanceManager.class);var map=mock(server.maps.MapleMap.class);
        when(bot.getMapId()).thenReturn(BotZakumBattle.ALTAR);when(bot.getEventInstance()).thenReturn(event);when(bot.getMap()).thenReturn(map);
        var entry=new BotEntry(bot,bot,null);entry.msgSending=true;
        var body=mock(server.life.Monster.class);when(body.getId()).thenReturn(8800000);when(body.isAlive()).thenReturn(true);when(body.isFake()).thenReturn(true);
        var arm=mock(server.life.Monster.class);when(arm.getId()).thenReturn(8800004);when(arm.isAlive()).thenReturn(true);
        when(map.getAllMonsters()).thenReturn(java.util.List.of(body,arm));
        BotPqChat.queue(entry,"Body Phase One: The arms are down.",false);assertFalse(BotPqChat.isCurrent(entry,entry.msgQueue.poll()));
        BotPqChat.queue(entry,"Arm Phase: Fight the arms.",false);assertTrue(BotPqChat.isCurrent(entry,entry.msgQueue.poll()));
        when(arm.isAlive()).thenReturn(false);
        BotPqChat.queue(entry,"Body Phase One: The arms are down.",false);assertFalse(BotPqChat.isCurrent(entry,entry.msgQueue.poll()));
        when(body.isFake()).thenReturn(false);
        BotPqChat.queue(entry,"Body Phase One: The arms are down.",false);assertTrue(BotPqChat.isCurrent(entry,entry.msgQueue.poll()));
        BotPqChat.queue(entry,"Arm Phase: Fight the arms.",false);assertFalse(BotPqChat.isCurrent(entry,entry.msgQueue.poll()));
    }

    @Test void puzzleReadinessDoesNotSurviveTravelToTheBossRoom() {
        var bot=mock(Character.class);var event=mock(EventInstanceManager.class);
        var entry=new BotEntry(bot,bot,null);entry.msgSending=true;
        when(bot.getEventInstance()).thenReturn(event);when(bot.getMapId()).thenReturn(103000803);
        BotPqChat.queue(entry,"We are in position. Please ask Cloto to check.",true);
        when(bot.getMapId()).thenReturn(103000804);
        assertFalse(BotPqChat.isCurrent(entry,entry.msgQueue.poll()));
    }
    @Test void clearedPuzzleExpiresReadinessEvenBeforeLeavingTheMap() {
        var bot=mock(Character.class);var event=mock(EventInstanceManager.class);
        var entry=new BotEntry(bot,bot,null);entry.msgSending=true;
        when(bot.getEventInstance()).thenReturn(event);when(bot.getMapId()).thenReturn(103000803);
        BotPqChat.queue(entry,"Ready for Cloto",true);
        when(event.getProperty("4stageclear")).thenReturn("true");
        assertFalse(BotPqChat.isCurrent(entry,entry.msgQueue.poll()));
        BotPqChat.queue(entry,"Stage clear; use the portal",false);
        assertTrue(BotPqChat.isCurrent(entry,entry.msgQueue.poll()));
    }
    @Test void currentHintsStayValidButDoNotCarryIntoAnotherRunOrAlterSupplyMessages() {
        var bot=mock(Character.class);var event=mock(EventInstanceManager.class);
        var entry=new BotEntry(bot,bot,null);entry.msgSending=true;
        when(bot.getEventInstance()).thenReturn(event);when(bot.getMapId()).thenReturn(103000803);
        BotPqChat.queue(entry,"Ready",true);
        assertTrue(BotPqChat.isCurrent(entry,entry.msgQueue.poll()));
        BotPqChat.queue(entry,"Ready",true);
        when(bot.getEventInstance()).thenReturn(mock(EventInstanceManager.class));
        assertFalse(BotPqChat.isCurrent(entry,entry.msgQueue.poll()));
        assertTrue(BotPqChat.isCurrent(entry,new BotChatManager.QueuedMessage("Need summoning rocks",false)));
    }
}
