package server.bots;

import client.BotClient;
import client.Character;
import client.Job;
import client.Client;
import client.QuestStatus;
import client.inventory.manipulator.InventoryManipulator;
import org.junit.jupiter.api.Test;
import server.quest.Quest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotZakumBattleTest {
    @Test void rangedSideSwitchUsesTheNormalUpperPlatformPortal() throws Exception {
        var bot=mock(Character.class);when(bot.getJob()).thenReturn(Job.NIGHTLORD);
        when(bot.getPosition()).thenReturn(new java.awt.Point(-400,-327));
        var map=mock(server.maps.MapleMap.class);when(bot.getMap()).thenReturn(map);
        var arm=mock(server.life.Monster.class);when(arm.getId()).thenReturn(8800010);when(arm.isAlive()).thenReturn(true);
        when(map.getAllMonsters()).thenReturn(java.util.List.of(arm));
        var portal=mock(server.maps.Portal.class);when(map.getPortal("h000")).thenReturn(portal);
        var bounds=mock(server.bots.combat.BotMobHitboxProvider.class);
        when(bounds.getMobBounds(arm)).thenReturn(new java.awt.Rectangle(58,-358,256,116));
        var goals=BotZakumCombat.class.getDeclaredField("GOALS");goals.setAccessible(true);
        var retries=BotZakumCombat.class.getDeclaredField("RETRY");retries.setAccessible(true);
        ((java.util.Map<Character,java.awt.Point>)goals.get(null)).put(bot,new java.awt.Point(438,-327));
        ((java.util.Map<Character,Long>)retries.get(null)).put(bot,Long.MAX_VALUE);
        var entry=new BotEntry(bot,null,null);
        try(var provider=mockStatic(server.bots.combat.BotMobHitboxProvider.class);
                var weapons=mockStatic(BotAttackExecutionProvider.class);var travel=mockStatic(BotTravelManager.class);
                var walking=mockStatic(BotZakumPqRun.class)) {
            provider.when(server.bots.combat.BotMobHitboxProvider::getInstance).thenReturn(bounds);
            weapons.when(()->BotAttackExecutionProvider.getEquippedWeaponType(bot)).thenReturn(client.inventory.WeaponType.CLAW);
            BotZakumCombat.tick(entry,bot,true);
            travel.verify(()->BotTravelManager.walkToPortalAndEnter(eq(entry),eq(bot),eq(portal),anyLong(),eq(true)));
            walking.verifyNoInteractions();
        }
    }

    @Test void rangedAttackerPlansArmsOnlyFromTheirExteriorSide() {
        var bot=mock(Character.class);when(bot.getJob()).thenReturn(Job.NIGHTLORD);
        var map=mock(server.maps.MapleMap.class);when(bot.getMap()).thenReturn(map);
        var arm=mock(server.life.Monster.class);when(arm.getId()).thenReturn(8800010);when(arm.isAlive()).thenReturn(true);
        when(map.getAllMonsters()).thenReturn(java.util.List.of(arm));
        var bounds=mock(server.bots.combat.BotMobHitboxProvider.class);
        when(bounds.getMobBounds(arm)).thenReturn(new java.awt.Rectangle(58,-358,256,116));
        var entry=new BotEntry(bot,null,null);
        try(var provider=mockStatic(server.bots.combat.BotMobHitboxProvider.class);
                var weapons=mockStatic(BotAttackExecutionProvider.class);var combat=mockStatic(BotCombatManager.class);
                var navigation=mockStatic(BotNavigationGraphProvider.class);var hold=mockStatic(BotOpqRun.class);
                var movement=mockStatic(BotMovementManager.class)) {
            provider.when(server.bots.combat.BotMobHitboxProvider::getInstance).thenReturn(bounds);
            weapons.when(()->BotAttackExecutionProvider.getEquippedWeaponType(bot)).thenReturn(client.inventory.WeaponType.CLAW);
            when(bot.getPosition()).thenReturn(new java.awt.Point(-335,-300));
            BotZakumCombat.tick(entry,bot,true);
            combat.verify(()->BotCombatManager.planAttack(entry,bot,arm),never());
            when(bot.getPosition()).thenReturn(new java.awt.Point(330,-300));
            BotZakumCombat.tick(entry,bot,true);
            combat.verify(()->BotCombatManager.planAttack(entry,bot,arm),never());
            when(bot.getPosition()).thenReturn(new java.awt.Point(438,-300));
            BotZakumCombat.tick(entry,bot,true);
            combat.verify(()->BotCombatManager.planAttack(entry,bot,arm));
        }
    }
    @Test void zakumNightLordDoesNotPlanAvengerAgainstBoss() throws Exception {
        var bot=mock(Character.class);when(bot.getJob()).thenReturn(Job.NIGHTLORD);
        when(bot.getMapId()).thenReturn(BotZakumBattle.ALTAR);
        when(bot.getEventInstance()).thenReturn(mock(scripting.event.EventInstanceManager.class));
        var arm=mock(server.life.Monster.class);when(arm.getId()).thenReturn(8800010);
        var method=BotCombatManager.class.getDeclaredMethod("planSkillAttack",BotEntry.class,Character.class,server.life.Monster.class,int.class);
        method.setAccessible(true);
        assertNull(method.invoke(null,new BotEntry(bot,bot,null),bot,arm,constants.skills.Hermit.AVENGER));
        verify(bot,never()).getSkillLevel(any(client.Skill.class));
    }

    @Test void bishopRetreatsBeforeEnteringBodyContactRange() {
        var bot=mock(Character.class);when(bot.getJob()).thenReturn(Job.BISHOP);
        when(bot.getInventory(client.inventory.InventoryType.EQUIPPED)).thenReturn(mock(client.inventory.Inventory.class));
        when(bot.getPosition()).thenReturn(new java.awt.Point(-70,0));
        var map=mock(server.maps.MapleMap.class);when(bot.getMap()).thenReturn(map);
        var body=mock(server.life.Monster.class);when(body.getId()).thenReturn(8800002);
        when(map.getAllMonsters()).thenReturn(java.util.List.of(body));
        var bounds=mock(server.bots.combat.BotMobHitboxProvider.class);
        when(bounds.getMobBounds(body)).thenReturn(new java.awt.Rectangle(0,-200,200,201));
        var entry=new BotEntry(bot,null,null);
        try(var provider=mockStatic(server.bots.combat.BotMobHitboxProvider.class);
                var movement=mockStatic(BotZakumPqRun.class);var weapons=mockStatic(BotAttackExecutionProvider.class)) {
            weapons.when(()->BotAttackExecutionProvider.getEquippedWeaponType(bot)).thenReturn(client.inventory.WeaponType.STAFF);
            provider.when(server.bots.combat.BotMobHitboxProvider::getInstance).thenReturn(bounds);
            assertTrue(BotZakumCombat.tick(entry,bot,true));
            movement.verify(()->BotZakumPqRun.walkTo(entry,bot,new java.awt.Point(-175,0),true));
        }
    }
    @Test void dedicatedBishopBuildLearnsPartyShieldBeforeLateDamageSkills() {
        client.SkillFactory.loadAllSkills();
        var order=server.bots.build.MageBuilds.getBuildOrder(client.Job.BISHOP,"boss-support");
        assertEquals(constants.skills.Bishop.HOLY_SHIELD,order.get(1).skillId());
        assertEquals(client.SkillFactory.getSkill(constants.skills.Bishop.HOLY_SHIELD).getMaxLevel(),order.get(1).targetLevel());
        assertTrue(BotCombatManager.isPartySupportSkill(constants.skills.Bishop.HOLY_SHIELD));
        assertEquals(constants.skills.Bishop.MAPLE_WARRIOR,
                server.bots.build.MageBuilds.getBuildOrder(client.Job.BISHOP).get(1).skillId(),"Population build remains unchanged");
    }
    @Test void bishopPrioritizesSummonsAndDamageDealersPrioritizeTheBody() {
        var bishop=mock(Character.class);when(bishop.getJob()).thenReturn(Job.BISHOP);
        var thief=mock(Character.class);when(thief.getJob()).thenReturn(Job.NIGHTLORD);
        assertTrue(BotZakumCombat.priority(bishop,6300003)<BotZakumCombat.priority(bishop,8800002));
        assertTrue(BotZakumCombat.priority(thief,8800002)<BotZakumCombat.priority(thief,6300003));
    }
    @Test void matchingDamageImmunityPreventsWastedBossAttacks() {
        var bishop=mock(Character.class);when(bishop.getJob()).thenReturn(Job.BISHOP);
        var thief=mock(Character.class);when(thief.getJob()).thenReturn(Job.NIGHTLORD);
        var mob=mock(server.life.Monster.class);
        when(mob.isBuffed(client.status.MonsterStatus.WEAPON_IMMUNITY)).thenReturn(true);
        assertFalse(BotZakumCombat.canDamage(thief,mob));assertTrue(BotZakumCombat.canDamage(bishop,mob));
        when(mob.isBuffed(client.status.MonsterStatus.WEAPON_IMMUNITY)).thenReturn(false);
        when(mob.isBuffed(client.status.MonsterStatus.MAGIC_IMMUNITY)).thenReturn(true);
        assertTrue(BotZakumCombat.canDamage(thief,mob));assertFalse(BotZakumCombat.canDamage(bishop,mob));
    }
    @Test void crewRestocksOnlyItsLearnedSkillConsumables() {
        var bot=mock(Character.class);var client=mock(client.BotClient.class);when(bot.getClient()).thenReturn(client);
        when(bot.getName()).thenReturn("ZakumThief1");when(bot.getJob()).thenReturn(Job.NIGHTLORD);
        var entry=new BotEntry(bot,bot,null);
        try(var combat=mockStatic(BotCombatManager.class);var rocks=mockStatic(BotRockManager.class);
            var inventory=mockStatic(InventoryManipulator.class)) {
            rocks.when(()->BotRockManager.neededRockIds(entry,bot)).thenReturn(java.util.Set.of(4006001));
            rocks.when(()->BotRockManager.countRocks(bot,4006001)).thenReturn(7);
            inventory.when(()->InventoryManipulator.addById(client,4006001,(short)193)).thenReturn(true);
            BotZakumCompanions.provisionSkillConsumables(entry,bot);
            inventory.verify(()->InventoryManipulator.addById(client,4006001,(short)193));
        }
    }
    @Test void crewRestocksSummonRequirementsOutsideTheBuffCache() {
        var bot=mock(Character.class);when(bot.getName()).thenReturn("ZakumBishop1");when(bot.getJob()).thenReturn(Job.BISHOP);var botClient=mock(client.BotClient.class);
        when(bot.getClient()).thenReturn(botClient);
        var skill=mock(client.Skill.class);var effect=mock(server.StatEffect.class);
        when(bot.getSkills()).thenReturn(java.util.Map.of(skill,new Character.SkillEntry((byte)1,1,-1)));
        when(bot.getSkillLevel(skill)).thenReturn((byte)1);when(skill.getEffect(1)).thenReturn(effect);
        when(effect.getItemCon()).thenReturn(4006001);when(effect.getItemConNo()).thenReturn(1);
        var entry=new BotEntry(bot,bot,null);
        try(var combat=mockStatic(BotCombatManager.class);var rocks=mockStatic(BotRockManager.class);
                var inventory=mockStatic(InventoryManipulator.class)) {
            rocks.when(()->BotRockManager.neededRockIds(entry,bot)).thenReturn(java.util.Set.of());
            rocks.when(()->BotRockManager.countRocks(bot,4006001)).thenReturn(7);
            inventory.when(()->InventoryManipulator.addById(botClient,4006001,(short)193)).thenReturn(true);
            inventory.when(()->InventoryManipulator.addById(botClient,4006000,(short)200)).thenReturn(true);
            BotZakumCompanions.provisionSkillConsumables(entry,bot);
            inventory.verify(()->InventoryManipulator.addById(botClient,4006001,(short)193));
            inventory.verify(()->InventoryManipulator.addById(botClient,4006000,(short)200));
        }
    }
    @Test void shadowStarsIsATimedSupportBuffWithoutAnActionNode() {
        client.SkillFactory.loadAllSkills();
        var skill=client.SkillFactory.getSkill(constants.skills.NightLord.SHADOW_STARS);
        assertFalse(skill.getAction(),"The local WZ entry omits an action node");
        assertTrue(BotCombatManager.isActiveSupportSkill(skill,skill.getEffect(1)));
        assertFalse(BotCombatManager.isActiveAttackSkill(skill,skill.getEffect(1)));
    }
    @Test void summonWaitsForHyperBodyAndMageMagicGuardAtTheStagingPoint() {
        var bot=mock(Character.class);var mage=mock(Character.class);
        var event=mock(scripting.event.EventInstanceManager.class);
        when(bot.getEventInstance()).thenReturn(event);
        for(var member:java.util.List.of(bot,mage)){
            when(member.getMapId()).thenReturn(BotZakumBattle.ALTAR);
            when(member.getPosition()).thenReturn(new java.awt.Point(BotZakumSupport.STAGING));
            when(member.getHp()).thenReturn(1000);
            when(member.getBuffedValue(client.BuffStat.HYPERBODYHP)).thenReturn(null);
            when(member.getBuffedValue(client.BuffStat.MAGIC_GUARD)).thenReturn(null);
        }
        when(bot.getJob()).thenReturn(Job.DARKKNIGHT);when(mage.getJob()).thenReturn(Job.BISHOP);
        var crew=java.util.List.of(bot,mage);
        assertFalse(BotZakumSupport.prepared(bot,crew));
        for(var member:crew)when(member.getBuffedValue(client.BuffStat.HYPERBODYHP)).thenReturn(60);
        assertFalse(BotZakumSupport.prepared(bot,crew));
        when(mage.getBuffedValue(client.BuffStat.MAGIC_GUARD)).thenReturn(80);
        assertTrue(BotZakumSupport.prepared(bot,crew));
        when(bot.getPosition()).thenReturn(new java.awt.Point(0,-216));
        assertTrue(BotZakumSupport.prepared(bot,crew),"The summoner can leave staging once the party is prepared");
    }
    @Test void inactiveZakumBodyCannotBeAttackedOrDealContactDamage() throws Exception {
        var monster=mock(server.life.Monster.class);when(monster.isAlive()).thenReturn(true);
        var check=BotCombatManager.class.getDeclaredMethod("isHostileLivingMonster",server.life.Monster.class);check.setAccessible(true);
        when(monster.isFake()).thenReturn(true);assertEquals(false,check.invoke(null,monster));
        when(monster.isFake()).thenReturn(false);assertEquals(true,check.invoke(null,monster));
    }
    @Test void altarDoesNotSpendItsPotionSupplyOnPercentageHpAttacks() throws Exception {
        var bot=mock(Character.class);when(bot.getMapId()).thenReturn(BotZakumBattle.ALTAR);
        when(bot.getEventInstance()).thenReturn(mock(scripting.event.EventInstanceManager.class));
        int id=constants.skills.DragonKnight.DRAGON_ROAR;
        var skill=mock(client.Skill.class);var effect=mock(server.StatEffect.class);
        when(bot.getSkillLevel(skill)).thenReturn((byte)30);when(skill.getEffect(30)).thenReturn(effect);
        when(effect.canPaySkillCost(bot)).thenReturn(true);when(effect.getHpRate()).thenReturn(-0.3);
        try(var skills=mockStatic(client.SkillFactory.class)){
            skills.when(()->client.SkillFactory.getSkill(id)).thenReturn(skill);
            var plan=BotCombatManager.class.getDeclaredMethod("planSkillAttack",BotEntry.class,Character.class,server.life.Monster.class,int.class);
            plan.setAccessible(true);
            assertNull(plan.invoke(null,new BotEntry(bot,bot,null),bot,mock(server.life.Monster.class),id));
            verify(effect).getHpRate();
        }
    }
    @Test void humanMustActuallyCompleteTrialsEvenWithGmPermissions() {
        var human=mock(Character.class);
        when(human.getClient()).thenReturn(mock(Client.class));when(human.getLevel()).thenReturn(150);
        when(human.isGM()).thenReturn(true);
        var approval=mock(QuestStatus.class);var trials=mock(QuestStatus.class);
        try(var quests=mockStatic(Quest.class)) {
            var q1=mock(Quest.class);var q2=mock(Quest.class);
            quests.when(()->Quest.getInstance(100200)).thenReturn(q1);quests.when(()->Quest.getInstance(100201)).thenReturn(q2);
            when(human.getQuest(q1)).thenReturn(approval);when(human.getQuest(q2)).thenReturn(trials);
            when(approval.getStatus()).thenReturn(QuestStatus.Status.STARTED);
            when(trials.getStatus()).thenReturn(QuestStatus.Status.NOT_STARTED);
            assertFalse(BotZakumCompanions.humanEligible(human));
            when(trials.getStatus()).thenReturn(QuestStatus.Status.COMPLETED);
            assertTrue(BotZakumCompanions.humanEligible(human));
            verify(q1,never()).forceStartWithActions(any(),anyInt());
            verify(q2,never()).forceCompleteWithActions(any(),anyInt(),any());
        }
    }
    @Test void preparationCannotGrantHumanPrerequisitesOrItems() {
        var human=mock(Character.class);when(human.getClient()).thenReturn(mock(Client.class));
        assertThrows(IllegalArgumentException.class,()->BotZakumCompanions.prepare(human));
        verify(human,never()).getQuest(any(Quest.class));
    }
    @Test void unregisteredBotsAreNotControlledAndAltarRequiresAnEvent() {
        var bot=mock(Character.class);when(bot.getClient()).thenReturn(mock(BotClient.class));
        when(bot.getMapId()).thenReturn(BotZakumBattle.ALTAR);
        assertFalse(BotZakumBattle.inInstance(bot));
        assertFalse(BotZakumBattle.tick(new BotEntry(bot,bot,null),bot,true));
        assertFalse(BotZakumBattle.reservesLoot(bot));
    }
}
