package server.bots;

import client.Character;
import client.Job;
import client.inventory.WeaponType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotPqEquipmentPolicyTest {
    @Test void pqMagesUseWandsOrStavesWithoutChangingPopulationWeapons() {
        for (var job : new Job[]{Job.CLERIC, Job.IL_WIZARD, Job.MAGICIAN}) {
            var bot = mock(Character.class);
            when(bot.getJob()).thenReturn(job);
            assertTrue(BotEquipManager.isWeaponCompatible(bot, WeaponType.POLE_ARM_SWING));
            BotPqEquipmentPolicy.register(bot);
            assertFalse(BotEquipManager.isWeaponCompatible(bot, WeaponType.POLE_ARM_SWING));
            assertFalse(BotEquipManager.isWeaponCompatible(bot, WeaponType.SWORD1H));
            assertTrue(BotEquipManager.isWeaponCompatible(bot, WeaponType.WAND));
            assertTrue(BotEquipManager.isWeaponCompatible(bot, WeaponType.STAFF));
        }
    }
    @Test void pqHunterAndAssassinKeepTheirSkillWeapons() {
        var hunter = mock(Character.class); when(hunter.getJob()).thenReturn(Job.HUNTER);
        BotPqEquipmentPolicy.register(hunter);
        assertTrue(BotEquipManager.isWeaponCompatible(hunter, WeaponType.BOW));
        assertFalse(BotEquipManager.isWeaponCompatible(hunter, WeaponType.CLAW));
        var thief = mock(Character.class); when(thief.getJob()).thenReturn(Job.ASSASSIN);
        BotPqEquipmentPolicy.register(thief);
        assertTrue(BotEquipManager.isWeaponCompatible(thief, WeaponType.CLAW));
        assertFalse(BotEquipManager.isWeaponCompatible(thief, WeaponType.BOW));
    }
}
