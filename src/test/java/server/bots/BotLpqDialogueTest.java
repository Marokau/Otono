package server.bots;

import client.Character;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotLpqDialogueTest {
    @Test void crewWaitsForIntroductionAndUsesActualPassCountsWithoutRepeating() {
        var bot=mock(Character.class);var human=mock(Character.class);
        var event=mock(EventInstanceManager.class);var entry=new BotEntry(bot,human,null);
        when(bot.getMapId()).thenReturn(BotLpqRun.FIRST_MAP);
        when(human.getMapId()).thenReturn(BotLpqRun.FIRST_MAP);
        when(bot.getEventInstance()).thenReturn(event);
        try(var crew=mockStatic(BotLpqHuman.class);var chat=mockStatic(BotPqChat.class)) {
            crew.when(()->BotLpqHuman.isCollector(bot)).thenReturn(true);
            assertTrue(BotLpqDialogue.tick(entry,bot,human,1000));
            assertFalse(BotLpqDialogue.tick(entry,bot,human,8000));
            crew.when(()->BotLpqHuman.carriedWithLeader(bot,BotLpqRun.PASS)).thenReturn(13);
            BotLpqDialogue.tick(entry,bot,human,9000);BotLpqDialogue.tick(entry,bot,human,10000);
            chat.verify(()->BotPqChat.queue(entry,"Over halfway! We have 13 of the 25 passes.",true),times(1));
            when(human.getItemQuantity(BotLpqRun.PASS,false)).thenReturn(25);
            BotLpqDialogue.tick(entry,bot,human,11000);
            verify(human).dropMessage(5,"LPQ: You have all 25 passes! Talk to the Red Balloon, then use the opened portal. We will follow you.");
            when(event.getProperty("1stageclear")).thenReturn("true");
            assertFalse(BotLpqDialogue.tick(entry,bot,human,12000));
        }
    }
}
