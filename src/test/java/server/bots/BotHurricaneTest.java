package server.bots;

import client.Character;
import constants.skills.Bowmaster;
import org.junit.jupiter.api.Test;
import java.awt.Point;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotHurricaneTest {
    @Test void heldShotsPayStartDelayAndMovementOrCancellationRequiresRestart() {
        var bot=mock(Character.class);when(bot.getPosition()).thenReturn(new Point(100,200));
        var entry=new BotEntry(bot,null,null);
        var plan=new BotCombatManager.AttackPlan(Bowmaster.HURRICANE,30,1,null,java.util.List.of(),
                BotCombatManager.AttackRoute.RANGED,0,0,0,0,4,0,120,null);
        try(var zakum=mockStatic(BotZakumBattle.class)) {
            zakum.when(()->BotZakumBattle.inInstance(bot)).thenReturn(true);
            assertFalse(BotHurricane.ready(entry,bot,plan,1000));assertEquals(300,entry.attackCooldownMs);
            assertFalse(BotHurricane.ready(entry,bot,plan,1299));
            assertTrue(BotHurricane.ready(entry,bot,plan,1300));
            assertTrue(BotHurricane.ready(entry,bot,plan,1420));
            when(bot.getPosition()).thenReturn(new Point(101,200));
            assertFalse(BotHurricane.ready(entry,bot,plan,1540));
            BotHurricane.cancel(bot);assertFalse(BotHurricane.ready(entry,bot,plan,1840));
        }
    }

    @Test void zakumHurricaneUsesFixedCadenceAndNoOrdinaryBowAnimationFloor() {
        var bot=mock(Character.class);var skill=mock(client.Skill.class);
        when(skill.getId()).thenReturn(Bowmaster.HURRICANE);
        try(var zakum=mockStatic(BotZakumBattle.class)) {
            zakum.when(()->BotZakumBattle.inInstance(bot)).thenReturn(true);
            var timing=BotAttackExecutionProvider.resolveSkillAttackTiming(skill,"shoot1",bot,null);
            assertEquals(120,timing.cooldownMs());assertEquals(0,timing.hitDelayMs());
        }
    }
    @Test void fiftyMillisecondTicksPreserveTheAverageCadenceWithoutBankingExtraShots(){
        var bot=mock(Character.class);when(bot.getPosition()).thenReturn(new Point(100,200));
        var entry=new BotEntry(bot,null,null);
        var plan=new BotCombatManager.AttackPlan(Bowmaster.HURRICANE,30,1,null,java.util.List.of(),
                BotCombatManager.AttackRoute.RANGED,0,0,0,0,4,0,120,null);
        try(var zakum=mockStatic(BotZakumBattle.class)) {
            zakum.when(()->BotZakumBattle.inInstance(bot)).thenReturn(true);
            int shots=0;long first=-1,last=-1;
            for(long now=0;now<30300;now+=50){
                entry.attackCooldownMs=Math.max(0,entry.attackCooldownMs-50);
                if(entry.attackCooldownMs<=0&&BotHurricane.ready(entry,bot,plan,now)){shots++;if(first<0)first=now;last=now;}
            }
            assertEquals(250,shots);assertEquals(300,first);assertTrue(Math.abs((last-first)/(double)(shots-1)-120)<1);
            assertFalse(BotHurricane.ready(entry,bot,plan,last));
            assertFalse(BotHurricane.ready(entry,bot,plan,last+1000)); // a long pause pays a fresh start delay
        }
    }
}
