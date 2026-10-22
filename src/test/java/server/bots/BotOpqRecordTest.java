package server.bots;
import client.Character;
import constants.skills.ILWizard;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotOpqRecordTest {
    @Test void recordCourierUsesTheTeleportMemberRatherThanAMeleeLeader() {
        var event = mock(EventInstanceManager.class);
        var warrior = mock(Character.class);
        var mage = mock(Character.class);
        when(warrior.getId()).thenReturn(1);
        when(mage.getId()).thenReturn(2);
        for (var bot : List.of(warrior, mage)) {
            when(bot.getEventInstance()).thenReturn(event);
            when(bot.getClient()).thenReturn(mock(client.BotClient.class));
            when(bot.getMapId()).thenReturn(920010400);
        }
        when(event.getPlayers()).thenReturn(List.of(warrior, mage));
        when(event.isEventLeader(warrior)).thenReturn(true);
        when(mage.getSkillLevel(ILWizard.TELEPORT)).thenReturn(1);
        assertFalse(BotOpqRecord.isCourier(warrior));
        assertTrue(BotOpqRecord.isCourier(mage));
        var humanMage = mock(Character.class);
        when(humanMage.getClient()).thenReturn(mock(client.Client.class));
        when(humanMage.getSkillLevel(ILWizard.TELEPORT)).thenReturn(20);
        when(event.getPlayers()).thenReturn(List.of(humanMage, warrior, mage));
        assertTrue(BotOpqRecord.isCourier(mage));
    }
    @Test void aClericLeftAtTheEntranceDoesNotBlockTheMageInTheRecordRoom() {
        var event=mock(EventInstanceManager.class);var cleric=mock(Character.class);var mage=mock(Character.class);
        when(cleric.getId()).thenReturn(1);when(mage.getId()).thenReturn(2);
        for(var bot:List.of(cleric,mage)) {
            when(bot.getClient()).thenReturn(mock(client.BotClient.class));when(bot.getEventInstance()).thenReturn(event);
        }
        when(cleric.getSkillLevel(constants.skills.Cleric.TELEPORT)).thenReturn(20);
        when(mage.getSkillLevel(ILWizard.TELEPORT)).thenReturn(20);
        when(cleric.getMapId()).thenReturn(920010000);when(mage.getMapId()).thenReturn(920010400);
        when(event.getPlayers()).thenReturn(List.of(cleric,mage));
        assertTrue(BotOpqRecord.isCourier(mage));assertFalse(BotOpqRecord.isCourier(cleric));
        int record=4001056+java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)-1;
        when(mage.getItemQuantity(record,false)).thenReturn(1);when(cleric.getMapId()).thenReturn(920010400);
        assertTrue(BotOpqRecord.isCourier(mage));assertFalse(BotOpqRecord.isCourier(cleric));
    }}
