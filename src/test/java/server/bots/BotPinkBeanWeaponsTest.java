package server.bots;
import org.junit.jupiter.api.Test;import java.util.Map;import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
class BotPinkBeanWeaponsTest {
 @Test void nativeSixtyPercentAttackScrollsAreAllowedButCursedAndGmStrengthAreNot(){assertTrue(BotPinkBeanWeapons.ordinaryAttackScroll(Map.of("PAD",2,"success",60),false));assertTrue(BotPinkBeanWeapons.ordinaryAttackScroll(Map.of("MAD",2,"success",60),true));assertFalse(BotPinkBeanWeapons.ordinaryAttackScroll(Map.of("PAD",5,"success",30,"cursed",50),false));assertFalse(BotPinkBeanWeapons.ordinaryAttackScroll(Map.of("PAD",5,"success",100),false));assertFalse(BotPinkBeanWeapons.ordinaryAttackScroll(Map.of("PAD",2,"success",60),true));}
 @Test void humansAndActiveRaidBotsCannotBePrepared(){var b=mock(client.Character.class);assertThrows(IllegalArgumentException.class,()->BotPinkBeanWeapons.prepare(b));when(b.getClient()).thenReturn(mock(client.BotClient.class));when(b.getEventInstance()).thenReturn(mock(scripting.event.EventInstanceManager.class));assertThrows(IllegalArgumentException.class,()->BotPinkBeanWeapons.prepare(b));}
}
