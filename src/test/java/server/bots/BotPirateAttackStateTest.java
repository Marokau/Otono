package server.bots;

import client.Character;
import client.Skill;
import client.SkillFactory;
import constants.skills.Buccaneer;
import constants.skills.Marauder;
import org.junit.jupiter.api.Test;
import server.StatEffect;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotPirateAttackStateTest {
    private boolean ready(Character bot, int id) throws Exception {
        Method method = BotCombatManager.class.getDeclaredMethod("canUseSkill", Character.class, int.class, int.class);
        method.setAccessible(true);
        return (boolean)method.invoke(null, bot, id, 30);
    }

    @Test void transformationFinishersRequireTheirActiveForm() throws Exception {
        var bot = mock(Character.class);
        var skill = mock(Skill.class);
        var effect = mock(StatEffect.class);
        when(skill.getEffect(30)).thenReturn(effect);
        when(effect.canPaySkillCost(bot)).thenReturn(true);
        try (var factory = mockStatic(SkillFactory.class)) {
            factory.when(() -> SkillFactory.getSkill(anyInt())).thenReturn(skill);
            assertFalse(ready(bot, Buccaneer.DEMOLITION));
            assertFalse(ready(bot, Buccaneer.SNATCH));
            assertFalse(ready(bot, Marauder.SHOCKWAVE));
            when(bot.hasActiveBuff(Marauder.TRANSFORMATION)).thenReturn(true);
            assertTrue(ready(bot, Marauder.SHOCKWAVE));
            assertFalse(ready(bot, Buccaneer.DEMOLITION));
            when(bot.hasActiveBuff(Buccaneer.SUPER_TRANSFORMATION)).thenReturn(true);
            assertTrue(ready(bot, Buccaneer.DEMOLITION));
            assertTrue(ready(bot, Buccaneer.SNATCH));
        }
    }

    @Test void energyAttacksRequireTheNativeChargedMeter() throws Exception {
        var bot = mock(Character.class);
        var skill = mock(Skill.class);
        var effect = mock(StatEffect.class);
        when(skill.getEffect(30)).thenReturn(effect);
        when(effect.canPaySkillCost(bot)).thenReturn(true);
        try (var factory = mockStatic(SkillFactory.class)) {
            factory.when(() -> SkillFactory.getSkill(anyInt())).thenReturn(skill);
            when(bot.getEnergyBar()).thenReturn(9999);
            for (int id : new int[]{Marauder.ENERGY_BLAST, Marauder.ENERGY_DRAIN, Buccaneer.ENERGY_ORB})
                assertFalse(ready(bot, id));
            when(bot.getEnergyBar()).thenReturn(15000);
            assertTrue(ready(bot, Buccaneer.ENERGY_ORB));
            assertTrue(ready(bot, Marauder.ENERGY_BLAST));
            assertTrue(ready(bot, Marauder.ENERGY_DRAIN));
        }
    }

    @Test void ordinaryAttacksRetainTheirCostsWithoutRequiringCharge() throws Exception {
        var bot = mock(Character.class);
        var skill = mock(Skill.class);
        var effect = mock(StatEffect.class);
        when(skill.getEffect(30)).thenReturn(effect);
        when(effect.canPaySkillCost(bot)).thenReturn(true);
        try (var factory = mockStatic(SkillFactory.class)) {
            factory.when(() -> SkillFactory.getSkill(anyInt())).thenReturn(skill);
            assertTrue(ready(bot, Buccaneer.BARRAGE));
            assertTrue(ready(bot, Buccaneer.DRAGON_STRIKE));
            when(effect.canPaySkillCost(bot)).thenReturn(false);
            assertFalse(ready(bot, Buccaneer.BARRAGE));
        }
    }
}
