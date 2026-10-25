package server.bots;

import client.BuffStat;
import client.Character;
import client.Job;
import client.Skill;
import client.SkillFactory;
import constants.skills.Magician;
import constants.skills.Spearman;
import org.junit.jupiter.api.Test;
import server.StatEffect;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotGuildBossSupportTest {
    private Character protectedMember(Job job) {
        var b = mock(Character.class);
        when(b.isAlive()).thenReturn(true);
        when(b.getMapId()).thenReturn(990000900);
        when(b.getPosition()).thenReturn(new Point(0, 0));
        when(b.getJob()).thenReturn(job);
        when(b.getBuffedValue(BuffStat.HYPERBODYHP)).thenReturn(60);
        when(b.getBuffedValue(BuffStat.HYPERBODYMP)).thenReturn(60);
        if (job.isA(Job.MAGICIAN)) when(b.getBuffedValue(BuffStat.MAGIC_GUARD)).thenReturn(80);
        return b;
    }

    @Test void preparationRequiresHumanAndWholeCrewAliveNearbyAndProtected() {
        var leader = protectedMember(Job.NIGHTLORD);
        var priest = protectedMember(Job.PRIEST);
        var crew = new BotGuildCompanions.Crew(); crew.leader = leader; crew.bots = List.of(priest);
        assertTrue(BotGuildBossSupport.prepared(crew, new Point()));
        when(leader.getMapId()).thenReturn(990000800);
        assertFalse(BotGuildBossSupport.prepared(crew, new Point()));
        when(leader.getMapId()).thenReturn(990000900);
        when(priest.isAlive()).thenReturn(false);
        assertFalse(BotGuildBossSupport.prepared(crew, new Point()));
        when(priest.isAlive()).thenReturn(true);
        when(priest.getPosition()).thenReturn(new Point(400, 0));
        assertFalse(BotGuildBossSupport.prepared(crew, new Point()));
        when(priest.getPosition()).thenReturn(new Point());
        when(priest.getBuffedValue(BuffStat.MAGIC_GUARD)).thenReturn(null);
        assertFalse(BotGuildBossSupport.prepared(crew, new Point()));
        when(priest.getBuffedValue(BuffStat.MAGIC_GUARD)).thenReturn(80);
        when(leader.getBuffedValue(BuffStat.HYPERBODYMP)).thenReturn(null);
        assertFalse(BotGuildBossSupport.prepared(crew, new Point()));
    }

    @Test void dispelledMagicGuardBypassesRebuffDeadlineButKeepsSkillCostCheck() {
        var b = protectedMember(Job.PRIEST);
        when(b.getBuffedValue(BuffStat.MAGIC_GUARD)).thenReturn(null);
        var skill = mock(Skill.class); var effect = mock(StatEffect.class);
        when(skill.getId()).thenReturn(Magician.MAGIC_GUARD);
        when(b.getSkillLevel(Magician.MAGIC_GUARD)).thenReturn(20);
        when(b.getSkillLevel(skill)).thenReturn((byte)20);
        when(skill.getEffect(20)).thenReturn(effect);
        var e = new BotEntry(b, b, null); e.skillBuffsEnabled = true;
        e.nextBuffCheckAtMs = Long.MAX_VALUE; e.nextBuffAt.put(Magician.MAGIC_GUARD, Long.MAX_VALUE);
        try (var guild = mockStatic(BotGuildCompanions.class); var skills = mockStatic(SkillFactory.class)) {
            guild.when(() -> BotGuildCompanions.inInstance(b)).thenReturn(true);
            skills.when(() -> SkillFactory.getSkill(Magician.MAGIC_GUARD)).thenReturn(skill);
            BotCombatManager.tickBuffs(e, b);
            verify(effect).canPaySkillCost(b);
            verify(effect, never()).applyTo(b);
            clearInvocations(effect); e.attackCooldownMs = 100;
            BotCombatManager.tickBuffs(e, b); verifyNoInteractions(effect);
        }
    }

    @Test void hyperBodyRecastsForDispelledAllyOnlyWithinNativeSkillBounds() {
        var dk = protectedMember(Job.DRAGONKNIGHT); var ally = protectedMember(Job.HERMIT);
        when(ally.getBuffedValue(BuffStat.HYPERBODYHP)).thenReturn(null);
        when(dk.getSkillLevel(Spearman.HYPER_BODY)).thenReturn(30);
        when(dk.getPartyMembersOnSameMap()).thenReturn(List.of(dk, ally));
        var skill = mock(Skill.class); var effect = mock(StatEffect.class);
        when(skill.getEffect(30)).thenReturn(effect);
        when(effect.calculateBoundingBox(any(Point.class), anyBoolean())).thenReturn(new Rectangle(-200,-100,400,200));
        try (var guild = mockStatic(BotGuildCompanions.class); var skills = mockStatic(SkillFactory.class)) {
            guild.when(() -> BotGuildCompanions.inInstance(dk)).thenReturn(true);
            skills.when(() -> SkillFactory.getSkill(Spearman.HYPER_BODY)).thenReturn(skill);
            assertSame(skill, BotGuildBossSupport.criticalSkill(dk));
            when(ally.getPosition()).thenReturn(new Point(500,0));
            assertNull(BotGuildBossSupport.criticalSkill(dk));
            when(dk.getBuffedValue(BuffStat.HYPERBODYHP)).thenReturn(null);
            assertSame(skill, BotGuildBossSupport.criticalSkill(dk));
            when(dk.getMapId()).thenReturn(922010800);
            assertNull(BotGuildBossSupport.criticalSkill(dk));
        }
    }

    @Test void protectedCombatantChecksBothPotionKeysAndContinuesFighting() {
        var b = protectedMember(Job.HERMIT); var dk = protectedMember(Job.DRAGONKNIGHT);
        when(dk.getSkillLevel(Spearman.HYPER_BODY)).thenReturn(30);
        var crew = new BotGuildCompanions.Crew(); crew.leader = b; crew.bots = List.of(b, dk);
        assertFalse(BotGuildBossSupport.tick(new BotEntry(b,b,null),b,crew,false,true));
        verify(b).triggerAutopotAfterStatLoss(-1,-1);
    }
}
