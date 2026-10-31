package server.bots;
import client.*;import client.Character;import scripting.event.EventInstanceManager;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
class BotRaidCooldownsTest {
 @Test void preparedBotUsesNativeCooldownResetAndPreservesHullState(){var b=mock(Character.class);when(b.getClient()).thenReturn(mock(BotClient.class));BotRaidCooldowns.prepare(b);verify(b).removeAllCooldownsExcept(5221999,true);}
 @Test void humansAndActiveInstancesCannotResetCooldowns(){var human=mock(Character.class);assertThrows(IllegalArgumentException.class,()->BotRaidCooldowns.prepare(human));verify(human,never()).removeAllCooldownsExcept(anyInt(),anyBoolean());var active=mock(Character.class);when(active.getClient()).thenReturn(mock(BotClient.class));when(active.getEventInstance()).thenReturn(mock(EventInstanceManager.class));assertThrows(IllegalArgumentException.class,()->BotRaidCooldowns.prepare(active));verify(active,never()).removeAllCooldownsExcept(anyInt(),anyBoolean());}
}
