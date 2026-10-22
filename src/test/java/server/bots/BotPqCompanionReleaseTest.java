package server.bots;

import client.Character;
import net.server.world.Party;
import net.server.world.PartyCharacter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import scripting.event.EventInstanceManager;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotPqCompanionReleaseTest {
    @ParameterizedTest @ValueSource(booleans={true,false})
    void earlyHumanExitCannotPartiallyDismissTheCrewOrSaveACompanionInsideItsRewardMap(boolean maze) {
        var human=mock(Character.class);when(human.getId()).thenReturn(100);
        var party=mock(Party.class);when(human.getParty()).thenReturn(party);
        var first=mock(Character.class);var last=mock(Character.class);
        var p1=mock(PartyCharacter.class);when(p1.getId()).thenReturn(1);
        var p2=mock(PartyCharacter.class);when(p2.getId()).thenReturn(2);
        when(party.getMembers()).thenReturn(List.of(p1,p2));
        when(first.getMapId()).thenReturn(maze?BotLmpqRun.RECRUIT:BotOpqRun.RECRUIT_MAP);
        when(last.getMapId()).thenReturn(maze?BotLmpqRun.REWARD:920011300);
        when(last.getEventInstance()).thenReturn(mock(EventInstanceManager.class));
        var manager=mock(BotManager.class);
        var firstEntry=new BotEntry(first,first,null);var lastEntry=new BotEntry(last,last,null);
        when(manager.getEntryByBotCharId(1)).thenReturn(firstEntry);
        when(manager.getEntryByBotCharId(2)).thenReturn(lastEntry);
        try(var managers=mockStatic(BotManager.class);var mazeRuns=mockStatic(BotLmpqRun.class);var orbisRuns=mockStatic(BotOpqHuman.class)) {
            managers.when(BotManager::getInstance).thenReturn(manager);
            mazeRuns.when(()->BotLmpqRun.belongsTo(first,100)).thenReturn(true);mazeRuns.when(()->BotLmpqRun.belongsTo(last,100)).thenReturn(true);
            orbisRuns.when(()->BotOpqHuman.belongsTo(first,100)).thenReturn(true);orbisRuns.when(()->BotOpqHuman.belongsTo(last,100)).thenReturn(true);
            String message=maze?BotLmpqCompanions.release(human):BotOpqCompanions.release(human);
            assertTrue(message.contains("wait"));verify(first,never()).saveCharToDB(anyBoolean());verify(last,never()).saveCharToDB(anyBoolean());
            verify(first,never()).getClient();verify(last,never()).getClient();
        }
    }
}
