package server.bots;
import client.Character;
import client.BotClient;
import client.Client;
import client.Job;
import net.server.world.Party;
import net.server.world.PartyCharacter;
import org.junit.jupiter.api.Test;
import scripting.npc.NPCConversationManager;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotCarnivalTeamsTest {
    @Test void existingCrewPreventsAllocatingAnotherBatchWhileItReturns() {
        var human=mock(Character.class);when(human.getClient()).thenReturn(mock(Client.class));
        when(human.getMapId()).thenReturn(980000000);when(human.getLevel()).thenReturn(40);
        var bot=mock(Character.class);var crew=new BotCarnivalRun.Crew();crew.leader=human;
        BotCarnivalRun.CREWS.put(bot,crew);
        try(var pool=mockStatic(BotPqCompanionPool.class)) {
            assertTrue(BotCarnivalCompanions.assemble(human).contains("previous Carnival crew"));
            pool.verifyNoInteractions();
        }finally{BotCarnivalRun.CREWS.remove(bot);}
    }
    @Test void bothBracketsAssembleThreeAlliesAndFourOpponentsWithoutPopulationRecruitment() throws Exception {
        for(boolean second:new boolean[]{false,true}) {
            var human=mock(Character.class);when(human.getClient()).thenReturn(mock(Client.class));
            when(human.getMapId()).thenReturn(second?980030000:980000000);when(human.getLevel()).thenReturn(second?55:40);
            var manager=mock(BotManager.class);var bots=new ArrayList<Character>();
            for(int i=0;i<7;i++)bots.add(mock(Character.class));
            var index=new java.util.concurrent.atomic.AtomicInteger();
            try(var pool=mockStatic(BotPqCompanionPool.class);var managers=mockStatic(BotManager.class);var run=mockStatic(BotCarnivalRun.class)) {
                managers.when(BotManager::getInstance).thenReturn(manager);
                pool.when(()->BotPqCompanionPool.acquire(eq(human),eq(second?"cpq2":"cpq"),any(Job.class),anyString(),eq(second?980030000:980000000)))
                        .thenAnswer(call->index.getAndIncrement());
                for(int i=0;i<7;i++)when(manager.loadOfflineBot(i,0,0)).thenReturn(bots.get(i));
                assertTrue(BotCarnivalCompanions.assemble(human).contains("Four-versus-four"));
                assertEquals(7,index.get());
                for(int i=0;i<3;i++)verify(manager).partyUp(human,bots.get(i));
                for(int i=4;i<7;i++)verify(manager).partyUp(bots.get(3),bots.get(i));
                run.verify(()->BotCarnivalRun.register(human,bots.get(3),bots));
            }
        }
    }
    @Test void eachBracketUsesItsNormalLargeFieldChallengeAndNpc() {
        for(boolean second:new boolean[]{false,true}) {
            int recruit=second?980030000:980000000;
            var human=mock(Character.class);when(human.getId()).thenReturn(10);when(human.getMapId()).thenReturn(recruit);
            when(human.getLevel()).thenReturn(second?55:40);
            var ours=mock(Party.class);var theirs=mock(Party.class);
            when(ours.getId()).thenReturn(1);when(theirs.getId()).thenReturn(2);
            when(ours.getLeaderId()).thenReturn(10);when(theirs.getLeaderId()).thenReturn(23);
            var members=List.of(mock(PartyCharacter.class),mock(PartyCharacter.class),mock(PartyCharacter.class),mock(PartyCharacter.class));
            when(ours.getMembers()).thenReturn(members);when(theirs.getMembers()).thenReturn(members);when(human.getParty()).thenReturn(ours);
            var bots=new ArrayList<Character>();
            for(int i=0;i<7;i++) {
                var b=mock(Character.class,RETURNS_DEEP_STUBS);when(b.getId()).thenReturn(20+i);
                when(b.getName()).thenReturn((second?"CPq2":"CPq")+"Test"+i);when(b.getMapId()).thenReturn(recruit);
                when(b.getLevel()).thenReturn(second?55:40);when(b.getParty()).thenReturn(i<3?ours:theirs);
                when(b.getClient().getCM()).thenReturn(null);
                when(b.getWorldServer().getPlayerStorage().getCharacterById(20+i)).thenReturn(b);bots.add(b);
            }
            var crew=new BotCarnivalRun.Crew();crew.leader=human;crew.opponent=bots.get(3);crew.bots=bots;
            for(var b:bots)BotCarnivalRun.CREWS.put(b,crew);
            var cm=mock(NPCConversationManager.class);
            when(cm.fieldLobbied(second?2:4)).thenReturn(true);when(cm.fieldLobbied2(second?2:4)).thenReturn(true);
            try(var supplies=mockStatic(BotPqProvisioner.class);var pots=mockStatic(BotPotionManager.class);
                var npcs=mockStatic(BotNpcInteraction.class);var run=mockStatic(BotCarnivalRun.class,CALLS_REAL_METHODS)) {
                npcs.when(()->BotNpcInteraction.talk(crew.opponent,second?2042005:2042000)).thenReturn(true);
                assertNull(BotCarnivalCompanions.begin(human,cm));
                if(second) {verify(cm).challengeParty2(2);verify(cm,never()).challengeParty(anyInt());}
                else {verify(cm).challengeParty(4);verify(cm,never()).challengeParty2(anyInt());}
                npcs.verify(()->BotNpcInteraction.next(crew.opponent,second?2:4));
                assertTrue(crew.started);
            }finally{for(var b:bots)BotCarnivalRun.CREWS.remove(b);}
        }
    }
}
