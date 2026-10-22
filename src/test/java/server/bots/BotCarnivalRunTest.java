package server.bots;

import client.Character;
import client.keybind.KeyBinding;
import org.junit.jupiter.api.Test;
import server.life.Monster;
import server.partyquest.MonsterCarnival;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotCarnivalRunTest {
    @Test void departedHumanDoesNotKeepBotsWaitingForAStageBriefingInExitRoom() {
        var human=mock(Character.class,RETURNS_DEEP_STUBS);var b=mock(Character.class,RETURNS_DEEP_STUBS);
        when(human.getId()).thenReturn(42);when(human.getParty()).thenReturn(null);
        when(human.getClient()).thenReturn(mock(client.Client.class));
        when(human.getWorldServer().getPlayerStorage().getCharacterById(42)).thenReturn(human);
        when(b.getName()).thenReturn("CPq2Mage1");when(b.getMapId()).thenReturn(980030010);
        when(b.getClient()).thenReturn(mock(client.BotClient.class));when(b.getMonsterCarnival()).thenReturn(null);
        var c=new BotCarnivalRun.Crew();c.leader=human;c.opponent=b;c.bots=java.util.List.of(b);BotCarnivalRun.CREWS.put(b,c);
        try(var guide=mockStatic(BotPqStageGuide.class);var npc=mockStatic(BotNpcInteraction.class);var hold=mockStatic(BotOpqRun.class)) {
            BotCarnivalRun.tick(new BotEntry(b,b,null),b,true);
            guide.verifyNoInteractions();npc.verify(()->BotNpcInteraction.talk(b,2042007));
        }finally{BotCarnivalRun.CREWS.remove(b);}
    }
    @Test void onlineHumanLeavingOrReplacingPartyDismissesBothSidesAfterLobbyReturn() {
        for(boolean second:new boolean[]{false,true})for(boolean newParty:new boolean[]{false,true}) {
            var human=mock(Character.class,RETURNS_DEEP_STUBS);when(human.getId()).thenReturn(42);
            when(human.getClient()).thenReturn(mock(client.Client.class));
            when(human.getWorldServer().getPlayerStorage().getCharacterById(42)).thenReturn(human);
            var original=mock(net.server.world.Party.class);when(original.getId()).thenReturn(1);
            var replacement=mock(net.server.world.Party.class);when(replacement.getId()).thenReturn(2);when(replacement.getLeaderId()).thenReturn(42);
            when(human.getParty()).thenReturn(newParty?replacement:null);
            var ally=mock(Character.class,RETURNS_DEEP_STUBS);var enemy=mock(Character.class,RETURNS_DEEP_STUBS);
            when(ally.getParty()).thenReturn(original);
            for(var b:java.util.List.of(ally,enemy)) {
                when(b.getName()).thenReturn(second?"CPq2Mage1":"CPqMage1");
                when(b.getMapId()).thenReturn(second?980030000:980000000);
                when(b.getMonsterCarnival()).thenReturn(null);
            }
            var c=new BotCarnivalRun.Crew();c.leader=human;c.opponent=enemy;c.bots=java.util.List.of(ally,enemy);
            BotCarnivalRun.CREWS.put(ally,c);BotCarnivalRun.CREWS.put(enemy,c);
            try(var dismissal=mockStatic(BotCarnivalCompanions.class);var hold=mockStatic(BotOpqRun.class)) {
                BotCarnivalRun.tick(new BotEntry(ally,ally,null),ally,true);
                BotCarnivalRun.tick(new BotEntry(enemy,enemy,null),enemy,true);
                dismissal.verify(()->BotCarnivalCompanions.dismiss(ally));
                dismissal.verify(()->BotCarnivalCompanions.dismiss(enemy));
            }finally{BotCarnivalRun.CREWS.remove(ally);BotCarnivalRun.CREWS.remove(enemy);}
        }
    }
    @Test void activeHumanPartyKeepsItsWaitingCrewForAnotherMatch() {
        var human=mock(Character.class,RETURNS_DEEP_STUBS);var b=mock(Character.class,RETURNS_DEEP_STUBS);
        when(human.getId()).thenReturn(42);when(human.getClient()).thenReturn(mock(client.Client.class));
        when(human.getWorldServer().getPlayerStorage().getCharacterById(42)).thenReturn(human);
        var party=mock(net.server.world.Party.class);when(party.getId()).thenReturn(1);when(party.getLeaderId()).thenReturn(42);
        when(human.getParty()).thenReturn(party);when(b.getParty()).thenReturn(party);
        when(b.getName()).thenReturn("CPqMage1");when(b.getMapId()).thenReturn(980000000);when(b.getMonsterCarnival()).thenReturn(null);
        var c=new BotCarnivalRun.Crew();c.leader=human;c.opponent=b;c.bots=java.util.List.of(b);BotCarnivalRun.CREWS.put(b,c);
        try(var dismissal=mockStatic(BotCarnivalCompanions.class);var hold=mockStatic(BotOpqRun.class)) {
            BotCarnivalRun.tick(new BotEntry(b,b,null),b,true);
            dismissal.verifyNoInteractions();
        }finally{BotCarnivalRun.CREWS.remove(b);}
    }
    @Test void bothBracketsUseTheirOwnRewardNpcAndResetAfterReturning() {
        for(boolean second:new boolean[]{false,true}) {
            var b=mock(Character.class,RETURNS_DEEP_STUBS);var leader=mock(Character.class,RETURNS_DEEP_STUBS);
            when(b.getMonsterCarnival()).thenReturn(null);when(leader.getMonsterCarnival()).thenReturn(null);
            when(b.getName()).thenReturn(second?"CPq2Archer1":"CPqArcher1");
            when(b.getMapId()).thenReturn(second?980033300:980000503);
            when(b.getMap().isCPQWinnerMap()).thenReturn(true);
            when(b.getClient()).thenReturn(mock(client.BotClient.class));
            when(leader.getClient()).thenReturn(mock(client.BotClient.class));
            when(leader.getWorldServer().getPlayerStorage().getCharacterById(0)).thenReturn(leader);
            var c=new BotCarnivalRun.Crew();c.leader=leader;c.opponent=b;c.bots=java.util.List.of(b);c.started=true;
            BotCarnivalRun.CREWS.put(b,c);var entry=new BotEntry(b,b,null);
            try(var npc=mockStatic(BotNpcInteraction.class);var hold=mockStatic(BotOpqRun.class)) {
                BotCarnivalRun.tick(entry,b,true);
                npc.verify(()->BotNpcInteraction.talk(b,second?2042007:2042002));
                when(b.getMap().isCPQWinnerMap()).thenReturn(false);
                when(b.getMapId()).thenReturn(second?980030000:980000000);
                BotCarnivalRun.tick(entry,b,true);
                assertFalse(c.started);assertEquals(-1,c.field);
            }finally{BotCarnivalRun.CREWS.remove(b);}
        }
    }
    @Test void ordinaryChallengeAcceptDeclineDialogueIsVisibleToTheHeadlessClient() {
        var packet=tools.PacketCreator.getNPCTalk(2042000,(byte)12,"Accept this Carnival challenge?","",(byte)0);
        var message=BotNpcInteraction.read(packet);
        assertNotNull(message);assertEquals(12,message.type());
        assertEquals("Accept this Carnival challenge?",message.text());
    }
    @Test void competitorsCannotTargetTheirOwnSummons() {
        var b=mock(Character.class); var m=mock(Monster.class);
        when(b.getMonsterCarnival()).thenReturn(mock(MonsterCarnival.class));
        for (byte team=0;team<2;team++) {
            when(b.getTeam()).thenReturn(team);when(m.getTeam()).thenReturn((int)team);
            assertFalse(BotCarnivalRun.canAttack(b,m));
            when(m.getTeam()).thenReturn(1-team);assertTrue(BotCarnivalRun.canAttack(b,m));
            when(m.getTeam()).thenReturn(-1);assertTrue(BotCarnivalRun.canAttack(b,m));
        }
        when(b.getMonsterCarnival()).thenReturn(null);
        assertTrue(BotCarnivalRun.canAttack(b,m));
    }
    @Test void carnivalDisablesBothCarriedPotionKeys() {
        var b=mock(Character.class);var keys=new HashMap<Integer,KeyBinding>();
        keys.put(91,new KeyBinding(7,2002023));keys.put(92,new KeyBinding(7,2002023));
        when(b.getKeymap()).thenReturn(keys);
        BotCarnivalRun.disableCarriedPotions(b);
        assertFalse(keys.containsKey(91));assertFalse(keys.containsKey(92));
        verify(b).setAutopotHpAlert(0);verify(b).setAutopotMpAlert(0);
    }
    @Test void registrationRemainsExplicitAndOwnerScoped() {
        var b=mock(Character.class);var h=mock(Character.class); when(h.getId()).thenReturn(42);
        assertFalse(BotCarnivalRun.registered(b));
        var c=new BotCarnivalRun.Crew();c.leader=h;
        BotCarnivalRun.CREWS.put(b,c);
        try { assertTrue(BotCarnivalRun.belongsTo(b,42));assertFalse(BotCarnivalRun.belongsTo(b,43)); }
        finally { BotCarnivalRun.CREWS.remove(b); }
    }
}
