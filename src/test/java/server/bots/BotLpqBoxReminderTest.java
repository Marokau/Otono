package server.bots;

import client.Character;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import static org.mockito.Mockito.*;

class BotLpqBoxReminderTest {
    @Test void remindsOnceAtOneMinuteEvenWhenCollectorIsInStageSeven() {
        var bot=mock(Character.class);var human=mock(Character.class);var event=mock(EventInstanceManager.class);
        when(bot.getEventInstance()).thenReturn(event);when(human.getEventInstance()).thenReturn(event);
        when(human.getMapId()).thenReturn(922010600);when(bot.getMapId()).thenReturn(922010700);
        when(bot.getName()).thenReturn("LPqSpear1");
        try(var crew=mockStatic(BotLpqHuman.class)) {
            crew.when(()->BotLpqHuman.isCollector(bot)).thenReturn(true);
            BotLpqBoxReminder.tick(bot,human,1000);BotLpqBoxReminder.tick(bot,human,60999);
            verify(human,never()).dropMessage(anyInt(),anyString());
            BotLpqBoxReminder.tick(bot,human,61000);BotLpqBoxReminder.tick(bot,human,121000);
            verify(human,times(1)).dropMessage(eq(6),contains("Say 'box order'"));
        }
    }
    @Test void requestOrLeavingTheMazeSuppressesReminder() {
        var bot=mock(Character.class);var human=mock(Character.class);var event=mock(EventInstanceManager.class);
        when(bot.getEventInstance()).thenReturn(event);when(human.getEventInstance()).thenReturn(event);
        when(human.getMapId()).thenReturn(922010600);
        try(var crew=mockStatic(BotLpqHuman.class)) {
            crew.when(()->BotLpqHuman.isCollector(bot)).thenReturn(true);
            BotLpqBoxReminder.tick(bot,human,1000);BotLpqBoxReminder.requested(event);
            BotLpqBoxReminder.tick(bot,human,61000);
            var next=mock(EventInstanceManager.class);
            when(bot.getEventInstance()).thenReturn(next);when(human.getEventInstance()).thenReturn(next);
            BotLpqBoxReminder.tick(bot,human,100000);
            when(human.getMapId()).thenReturn(922010700);
            BotLpqBoxReminder.tick(bot,human,160000);
            verify(human,never()).dropMessage(anyInt(),anyString());
        }
    }
}
