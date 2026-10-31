package server.bots;
import client.Character;import client.BotClient;import java.util.Map;import org.junit.jupiter.api.Test;import scripting.event.EventInstanceManager;import static org.mockito.Mockito.*;import static org.junit.jupiter.api.Assertions.*;
class BotRaidHpScrollsTest {
 @Test void ordinaryHpChoicesExcludeCurseAndGmStrengthScrolls(){assertTrue(BotRaidHpScrolls.ordinaryHpScroll(Map.of("MHP",10,"success",60)));assertTrue(BotRaidHpScrolls.ordinaryHpScroll(Map.of("MHP",5,"success",100)));assertFalse(BotRaidHpScrolls.ordinaryHpScroll(Map.of("MHP",30,"success",100)));assertFalse(BotRaidHpScrolls.ordinaryHpScroll(Map.of("MHP",30,"success",30,"cursed",50)));assertFalse(BotRaidHpScrolls.ordinaryHpScroll(Map.of("MHP",30,"success",10,"PAD",5)));}
 @Test void cannotScrollHumansOrBotsAlreadyInEvents(){var b=mock(Character.class);assertThrows(IllegalArgumentException.class,()->BotRaidHpScrolls.prepare(b));when(b.getClient()).thenReturn(mock(BotClient.class));when(b.getEventInstance()).thenReturn(mock(EventInstanceManager.class));assertThrows(IllegalArgumentException.class,()->BotRaidHpScrolls.prepare(b));}
}
