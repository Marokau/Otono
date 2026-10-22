package server.bots;

import client.Character;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BotLpqStageGuideTest {
    @Test void everyLaterStageGetsOneIntroductionAndBriefCrewPause() {
        var bot=mock(Character.class);var human=mock(Character.class);var event=mock(EventInstanceManager.class);
        var entry=new BotEntry(bot,human,null);when(bot.getEventInstance()).thenReturn(event);
        try(var crew=mockStatic(BotLpqHuman.class);var chat=mockStatic(BotPqChat.class)) {
            crew.when(()->BotLpqHuman.isCollector(bot)).thenReturn(true);
            for(int map:new int[]{922010200,922010300,922010400,922010500,922010600,922010700,922010800,922010900,922011000,922011100}) {
                when(bot.getMapId()).thenReturn(map);when(human.getMapId()).thenReturn(map);
                assertTrue(BotLpqStageGuide.tick(entry,bot,human,1000));
                assertTrue(BotLpqStageGuide.tick(entry,bot,human,2000));
                assertFalse(BotLpqStageGuide.tick(entry,bot,human,8000));
            }
            chat.verify(()->BotPqChat.queue(eq(entry),anyString(),eq(true)),times(10));
            when(bot.getMapId()).thenReturn(922010200);when(human.getMapId()).thenReturn(922010200);
            when(bot.getEventInstance()).thenReturn(mock(EventInstanceManager.class));
            assertTrue(BotLpqStageGuide.tick(entry,bot,human,10000));
            chat.verify(()->BotPqChat.queue(eq(entry),anyString(),eq(true)),times(11));
        }
    }
    @Test void skipsClearedStagesAndRoomSubmaps() {
        var bot=mock(Character.class);var human=mock(Character.class);var event=mock(EventInstanceManager.class);
        var entry=new BotEntry(bot,human,null);when(bot.getEventInstance()).thenReturn(event);
        when(bot.getMapId()).thenReturn(922010200);when(human.getMapId()).thenReturn(922010200);
        when(event.getProperty("2stageclear")).thenReturn("true");
        try(var chat=mockStatic(BotPqChat.class)) {
            assertFalse(BotLpqStageGuide.tick(entry,bot,human,1000));
            when(bot.getMapId()).thenReturn(922010201);when(human.getMapId()).thenReturn(922010201);
            assertFalse(BotLpqStageGuide.tick(entry,bot,human,2000));
            chat.verifyNoInteractions();
        }
    }
}
