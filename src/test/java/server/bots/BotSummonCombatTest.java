package server.bots;

import client.Character;
import client.Disease;
import constants.skills.Bishop;
import constants.skills.Bowmaster;
import org.junit.jupiter.api.Test;
import server.StatEffect;
import server.combat.CombatFormulaProvider;
import server.life.Monster;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotSummonCombatTest {
    @Test void summonFollowingUsesTheOrdinaryMovementHandler() {
        var bot=mock(Character.class);var botClient=mock(client.BotClient.class);
        when(botClient.getPlayer()).thenReturn(bot);when(bot.getClient()).thenReturn(botClient);
        when(bot.getPosition()).thenReturn(new Point(100,-300));
        var map=mock(server.maps.MapleMap.class);when(bot.getMap()).thenReturn(map);
        var summon=mock(server.maps.Summon.class);when(summon.getObjectId()).thenReturn(9);
        var position=new java.util.concurrent.atomic.AtomicReference<>(new Point());
        when(summon.getPosition()).thenAnswer(call->position.get());
        doAnswer(call->{position.set(call.getArgument(0));return null;}).when(summon).setPosition(any(Point.class));
        when(bot.getSummonsValues()).thenReturn(List.of(summon));
        BotSummonCombat.follow(bot,summon);
        assertEquals(new Point(100,-300),position.get());
        var broadcast=org.mockito.ArgumentCaptor.forClass(net.packet.Packet.class);
        verify(map).broadcastMessage(eq(bot),broadcast.capture(),eq(new Point(100,-300)));
        var bytes=broadcast.getValue().getBytes();
        assertEquals(net.opcodes.SendOpcode.MOVE_SUMMON.getValue(),(bytes[0]&255)|((bytes[1]&255)<<8));
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(booleans={false,true})
    void summonUsesSharedDamageHandlerAndDoesNotRepeatDuringItsCooldownEvenWhenOwnerStunned(boolean left) {
        var bot=mock(Character.class);var botClient=mock(client.BotClient.class);
        when(botClient.getPlayer()).thenReturn(bot);when(bot.getClient()).thenReturn(botClient);
        when(bot.isAlive()).thenReturn(true);when(bot.getPosition()).thenReturn(new Point());
        when(bot.hasDisease(Disease.STUN)).thenReturn(true);
        when(bot.getInventory(client.inventory.InventoryType.EQUIPPED)).thenReturn(mock(client.inventory.Inventory.class));
        when(bot.getTotalWatk()).thenReturn(14);
        when(bot.calculateMaxBaseDamage(14,client.inventory.WeaponType.SWORD1H)).thenReturn(10000);
        var map=mock(server.maps.MapleMap.class);when(bot.getMap()).thenReturn(map);
        var summon=mock(server.maps.Summon.class);when(summon.getSkill()).thenReturn(Bowmaster.PHOENIX);
        when(summon.getSkillLevel()).thenReturn((byte)1);when(summon.getObjectId()).thenReturn(9);
        when(summon.getPosition()).thenReturn(new Point());when(bot.getSummonsValues()).thenReturn(List.of(summon));
        var mob=mock(Monster.class);when(mob.isAlive()).thenReturn(true);when(mob.getObjectId()).thenReturn(11);
        when(mob.getPosition()).thenReturn(new Point(left?-100:100,0));when(map.getAllMonsters()).thenReturn(List.of(mob));
        when(map.getMonsterByOid(11)).thenReturn(mob);
        var boxes=mock(server.bots.combat.BotMobHitboxProvider.class);
        when(boxes.getMobBounds(mob)).thenReturn(new Rectangle(left?-110:90,-30,30,31));
        var skill=mock(client.Skill.class);var fx=mock(StatEffect.class);
        when(skill.getEffect(1)).thenReturn(fx);when(fx.getWatk()).thenReturn((short)305);
        var formula=mock(CombatFormulaProvider.class);
        when(formula.rollDamageLines(eq(bot),eq(mob),eq(1),anyInt(),anyInt(),eq(false))).thenReturn(List.of(6000));
        try(var zakum=mockStatic(BotZakumBattle.class);var skills=mockStatic(client.SkillFactory.class);
                var provider=mockStatic(server.bots.combat.BotMobHitboxProvider.class);
                var shared=mockStatic(CombatFormulaProvider.class)) {
            zakum.when(()->BotZakumBattle.inInstance(bot)).thenReturn(true);
            skills.when(()->client.SkillFactory.getSkill(Bowmaster.PHOENIX)).thenReturn(skill);
            provider.when(server.bots.combat.BotMobHitboxProvider::getInstance).thenReturn(boxes);
            shared.when(CombatFormulaProvider::getInstance).thenReturn(formula);
            var entry=new BotEntry(bot,null,null);
            BotSummonCombat.tick(entry,bot);BotSummonCombat.tick(entry,bot);
            verify(map,times(1)).damageMonster(bot,mob,6000,(short)360);
            var broadcast=org.mockito.ArgumentCaptor.forClass(net.packet.Packet.class);
            verify(map).broadcastMessage(eq(bot),broadcast.capture(),eq(new Point()));
            assertEquals(left?5:4,Byte.toUnsignedInt(broadcast.getValue().getBytes()[11]),
                    "Summon attack1 packs action 2 and facing, rather than broadcasting idle 0/1");
        }
    }
    @Test void phoenixAimsAtOffsetArmBoundsRatherThanSharedMonsterOrigin() {
        var bot=mock(Character.class);var map=mock(server.maps.MapleMap.class);
        when(bot.getMap()).thenReturn(map);
        var summon=mock(server.maps.Summon.class);when(summon.getSkill()).thenReturn(Bowmaster.PHOENIX);
        when(summon.getPosition()).thenReturn(new Point(-447,-422));
        var arm=mock(Monster.class);when(arm.isAlive()).thenReturn(true);
        when(arm.getPosition()).thenReturn(new Point(-9,-216));
        when(map.getAllMonsters()).thenReturn(List.of(arm));
        var boxes=mock(server.bots.combat.BotMobHitboxProvider.class);
        when(boxes.getMobBounds(arm)).thenReturn(new Rectangle(-229,-526,132,104));
        try(var provider=mockStatic(server.bots.combat.BotMobHitboxProvider.class)) {
            provider.when(server.bots.combat.BotMobHitboxProvider::getInstance).thenReturn(boxes);
            assertEquals(List.of(arm),BotSummonCombat.targets(bot,summon,
                    BotSummonCombat.spec(Bowmaster.PHOENIX),mock(StatEffect.class)));
        }
    }
    @Test void attackSpecsComeFromLocalWz() {
        var phoenix=BotSummonCombat.spec(Bowmaster.PHOENIX);
        assertEquals(270,phoenix.radius()); assertEquals(4,phoenix.targets()); assertEquals(360,phoenix.hitDelay());
        var bahamut=BotSummonCombat.spec(Bishop.BAHAMUT);
        assertEquals(0,bahamut.radius()); assertEquals(3,bahamut.targets()); assertEquals(990,bahamut.hitDelay());
    }

    @Test void phoenixReachUsesNearestEdgeAndRejectsVerticalOrHorizontalOutOfRange() {
        var spec=BotSummonCombat.spec(Bowmaster.PHOENIX);
        assertTrue(BotSummonCombat.reachable(spec,new Point(),new Rectangle(200,-50,200,50)));
        assertFalse(BotSummonCombat.reachable(spec,new Point(),new Rectangle(400,-50,200,50)));
        assertFalse(BotSummonCombat.reachable(spec,new Point(),new Rectangle(0,-500,50,50)));
    }

    @Test void bahamutCanFaceEitherWayButCannotHitRemoteArms() {
        var spec=BotSummonCombat.spec(Bishop.BAHAMUT);
        assertTrue(BotSummonCombat.reachable(spec,new Point(),new Rectangle(-180,-100,30,30)));
        assertTrue(BotSummonCombat.reachable(spec,new Point(),new Rectangle(180,-100,30,30)));
        assertFalse(BotSummonCombat.reachable(spec,new Point(),new Rectangle(400,-100,30,30)));
    }

    @Test void summonCastRespectsDeathStunSealAndActionLocks() {
        var bot=mock(Character.class); var entry=new BotEntry(bot,null,null);entry.skillBuffsEnabled=true;
        assertFalse(BotSummonCombat.canCast(entry,bot,0));
        when(bot.isAlive()).thenReturn(true);assertTrue(BotSummonCombat.canCast(entry,bot,0));
        when(bot.hasDisease(Disease.STUN)).thenReturn(true);assertFalse(BotSummonCombat.canCast(entry,bot,0));
        when(bot.hasDisease(Disease.STUN)).thenReturn(false);when(bot.hasDisease(Disease.SEAL)).thenReturn(true);
        assertFalse(BotSummonCombat.canCast(entry,bot,0));when(bot.hasDisease(Disease.SEAL)).thenReturn(false);
        entry.attackCooldownMs=1;assertFalse(BotSummonCombat.canCast(entry,bot,0));
        entry.attackCooldownMs=0;entry.inAir=true;assertFalse(BotSummonCombat.canCast(entry,bot,0));
    }

    @Test void phoenixUsesDexAndSkillAttackRatherThanEquippedWeaponAttack() {
        var bot=mock(Character.class);var mob=mock(Monster.class);var fx=mock(StatEffect.class);
        when(bot.getTotalDex()).thenReturn(1000);when(bot.getTotalStr()).thenReturn(50);
        when(fx.getWatk()).thenReturn((short)305);
        var formula=mock(CombatFormulaProvider.class);
        when(formula.applySkillElementalMultiplier(anyLong(),anyInt(),eq(mob),eq(false)))
                .thenAnswer(call->call.getArgument(0));
        when(formula.rollDamageLines(eq(bot),eq(mob),eq(1),anyInt(),anyInt(),eq(false))).thenReturn(List.of(6000));
        try(var shared=mockStatic(CombatFormulaProvider.class)) {
            shared.when(CombatFormulaProvider::getInstance).thenReturn(formula);
            assertEquals(6000,BotSummonCombat.damage(bot,mob,Bowmaster.PHOENIX,fx));
            verify(formula).rollDamageLines(bot,mob,1,5490,7777,false);
            verify(bot,never()).getTotalWatk();verify(mob,never()).getWdef();
        }
    }
}
