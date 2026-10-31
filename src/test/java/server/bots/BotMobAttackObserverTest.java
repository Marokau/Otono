package server.bots;

import client.Character;
import client.BuffStat;
import client.Disease;
import client.Job;
import client.autoban.AutobanManager;
import net.server.Server;
import server.TimerManager;
import org.junit.jupiter.api.Test;
import server.life.Monster;
import server.life.MonsterStats;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.life.MobSkillType;
import server.maps.MapleMap;

import java.awt.Point;
import java.awt.Rectangle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotMobAttackObserverTest {
    @Test void ergothDeadlyAttackUsesNativeHpMpLossAndNotifiesBothPotionKeys() {
        var bot=mock(Character.class); var client=mock(client.BotClient.class); var map=mock(MapleMap.class);
        var mob=mock(Monster.class); var stats=mock(MonsterStats.class);
        when(bot.getClient()).thenReturn(client); when(client.getPlayer()).thenReturn(bot);
        when(bot.getMap()).thenReturn(map); when(bot.getPosition()).thenReturn(new Point());
        when(bot.getJob()).thenReturn(Job.PRIEST);
        var hp=new java.util.concurrent.atomic.AtomicInteger(1290);
        var mp=new java.util.concurrent.atomic.AtomicInteger(6000);
        when(bot.getHp()).thenAnswer(call->hp.get()); when(bot.getMp()).thenAnswer(call->mp.get());
        when(bot.getBuffedValue(any(BuffStat.class))).thenReturn(null);
        when(bot.getBuffedValue(BuffStat.MAGIC_GUARD)).thenReturn(80);
        doAnswer(call->{hp.addAndGet(call.getArgument(0));mp.addAndGet(call.getArgument(1));return null;})
                .when(bot).addMPHP(anyInt(),anyInt());
        when(bot.getAutobanManager()).thenReturn(mock(AutobanManager.class));
        when(mob.getId()).thenReturn(9300028); when(mob.getObjectId()).thenReturn(123);
        when(mob.getStats()).thenReturn(stats); when(mob.getPosition()).thenReturn(new Point());
        when(map.getMapObject(123)).thenReturn(mob);
        try(var server=mockStatic(Server.class); var reaction=mockStatic(BotObservedHitReaction.class)) {
            server.when(Server::getInstance).thenReturn(mock(Server.class));
            BotMobAttackObserver.report(new BotEntry(bot,bot,null),bot,mob,1,1289);
            assertEquals(1,hp.get()); assertEquals(1,mp.get());
            verify(bot).triggerAutopotAfterStatLoss(-1289,-5999);
        }
    }

    @Test void pinkBeanHeadlessClientForwardsStatueAttackPackets() {
        var bot=mock(Character.class);var client=spy(new client.BotClient(0,1));
        doReturn(bot).when(client).getPlayer();doReturn(null).when(client).getCM();
        var packet=mock(net.packet.Packet.class);
        try(var pink=mockStatic(BotPinkBeanRun.class);var observer=mockStatic(BotMobAttackObserver.class)) {
            pink.when(()->BotPinkBeanRun.inInstance(bot)).thenReturn(true);
            client.sendPacket(packet);
            observer.verify(()->BotMobAttackObserver.observe(bot,packet));
        }
    }

    @Test void gpqHeadlessClientForwardsErgothAttackPackets() {
        var bot=mock(Character.class);var client=spy(new client.BotClient(0,1));
        doReturn(bot).when(client).getPlayer();doReturn(null).when(client).getCM();
        var packet=mock(net.packet.Packet.class);
        try(var gpq=mockStatic(BotGuildCompanions.class);var observer=mockStatic(BotMobAttackObserver.class)) {
            gpq.when(()->BotGuildCompanions.inInstance(bot)).thenReturn(true);
            client.sendPacket(packet);
            observer.verify(()->BotMobAttackObserver.observe(bot,packet));
        }
    }

    @Test void ergothAttacksKeepTheirRealDelayDeadlyFlagAndAllNineteenColumns() {
        var fire=BotMobAttackObserver.attack(9300028,0);
        var deadly=BotMobAttackObserver.attack(9300028,1);
        var columns=BotMobAttackObserver.attack(9300028,2);
        assertNotNull(fire);assertTrue(fire.magic());assertFalse(fire.deadly());assertEquals(900,fire.delay());
        assertTrue(deadly.deadly());assertEquals(900,deadly.delay());
        assertEquals(3,columns.type());assertEquals(19,columns.areaCount());assertEquals(2040,columns.delay());
        var map=mock(MapleMap.class);var mob=mock(Monster.class);when(mob.getId()).thenReturn(9300028);
        when(map.getPointBelow(any())).thenAnswer(inv->{Point p=inv.getArgument(0);return new Point(p.x,150);});
        var cast=new BotMobAttackObserver.Cast(map,mob,columns,2,(1<<0)|(1<<18),new Point(0,0),0);
        assertEquals(2,BotMobAttackObserver.areas(cast).size());
    }

    @Test void poisonGolemRocksUseTheirOwnDamageDelayAndAllTwentyThreeColumns() {
        var attack=BotMobAttackObserver.attack(9300182,2);
        assertNotNull(attack);assertEquals(3,attack.type());assertEquals(290,attack.pad());
        assertEquals(2640,attack.delay());assertEquals(23,attack.areaCount());
        assertFalse(attack.magic());assertFalse(attack.deadly());
        assertNull(BotMobAttackObserver.attack(9300182,0),"Ground tremors remain excluded");
        var map=mock(MapleMap.class);var mob=mock(Monster.class);
        when(mob.getId()).thenReturn(9300182);
        when(map.getPointBelow(any())).thenAnswer(inv->{Point p=inv.getArgument(0);return new Point(p.x,100);});
        var cast=new BotMobAttackObserver.Cast(map,mob,attack,2,(1<<0)|(1<<22),new Point(0,0),0);
        var areas=BotMobAttackObserver.areas(cast);
        assertEquals(2,areas.size());
        assertTrue(areas.get(0).contains(-1050,50));
        assertTrue(areas.get(1).contains(1150,50));
        assertFalse(areas.stream().anyMatch(r->r.contains(0,50)));
        assertFalse(areas.stream().anyMatch(r->r.contains(1150,-200)));
    }

    @Test void observedGolemAttackOptionsKeepUpperColumnBits() throws Exception {
        var bot=mock(Character.class);var map=mock(MapleMap.class);var mob=mock(Monster.class);
        when(bot.getMap()).thenReturn(map);when(map.getMapObject(42)).thenReturn(mob);
        when(mob.getId()).thenReturn(9300182);
        var packet=mock(net.packet.Packet.class);
        when(packet.getBytes()).thenReturn(ByteBuffer.allocate(17).order(ByteOrder.LITTLE_ENDIAN)
                .putShort((short)net.opcodes.SendOpcode.MOVE_MONSTER.getValue()).putInt(42)
                .put((byte)0).put((byte)0).put((byte)28).putInt(1<<22)
                .putShort((short)0).putShort((short)0).array());
        try(var ellin=mockStatic(BotEllinRun.class)) {
            ellin.when(()->BotEllinRun.inInstance(bot)).thenReturn(true);
            BotMobAttackObserver.observe(bot,packet);
            var field=BotMobAttackObserver.class.getDeclaredField("PENDING");field.setAccessible(true);
            var pending=(java.util.Map<?,?>)field.get(null);
            try {
                var queue=(java.util.Queue<?>)pending.get(bot);
                assertNotNull(queue);assertEquals(1,queue.size());
                var cast=(BotMobAttackObserver.Cast)queue.peek();
                assertEquals(1<<22,cast.mask());assertEquals(2,cast.index());
                assertSame(map,cast.map());
            } finally {pending.remove(bot);}
        }
    }

    @Test void ellinHeadlessClientForwardsAttackPacketsToObserver() {
        var bot=mock(Character.class);var client=spy(new client.BotClient(0,1));
        doReturn(bot).when(client).getPlayer();doReturn(null).when(client).getCM();
        var packet=mock(net.packet.Packet.class);
        try(var ellin=mockStatic(BotEllinRun.class);var zakum=mockStatic(BotZakumBattle.class);
            var observer=mockStatic(BotMobAttackObserver.class)) {
            ellin.when(()->BotEllinRun.inInstance(bot)).thenReturn(true);
            client.sendPacket(packet);
            observer.verify(()->BotMobAttackObserver.observe(bot,packet));
        }
    }

    @Test void golemRockUsesRealPlayerDamageHandlerWithoutInventedStun() {
        var bot=mock(Character.class);var client=mock(client.BotClient.class);var map=mock(MapleMap.class);
        var mob=mock(Monster.class);var stats=mock(MonsterStats.class);
        when(bot.getClient()).thenReturn(client);when(client.getPlayer()).thenReturn(bot);
        when(bot.getMap()).thenReturn(map);when(bot.getPosition()).thenReturn(new Point(0,100));
        when(bot.getJob()).thenReturn(Job.SPEARMAN);
        var hp=new java.util.concurrent.atomic.AtomicInteger(3500);when(bot.getHp()).thenAnswer(call->hp.get());
        doAnswer(call->{hp.addAndGet(call.getArgument(0));return null;}).when(bot).addMPHP(anyInt(),anyInt());
        when(bot.getAutobanManager()).thenReturn(mock(AutobanManager.class));
        when(mob.getId()).thenReturn(9300182);when(mob.getObjectId()).thenReturn(123);when(mob.getStats()).thenReturn(stats);
        when(mob.getPosition()).thenReturn(new Point(0,100));when(map.getMapObject(123)).thenReturn(mob);
        try(var server=mockStatic(Server.class);var mobSkills=mockStatic(MobSkillFactory.class);var reaction=mockStatic(BotObservedHitReaction.class)) {
            server.when(Server::getInstance).thenReturn(mock(Server.class));
            var entry=new BotEntry(bot,bot,null);
            BotMobAttackObserver.report(entry,bot,mob,2,57);
            reaction.verify(()->BotObservedHitReaction.apply(entry,bot,mob));
            verify(bot).addMPHP(-57,0);
            mobSkills.verifyNoInteractions();
        }
    }

    @Test void normalPlayerTimersExpireHeadlessShieldAndAllowCooldownReuse() throws Exception {
        client.SkillFactory.loadAllSkills();
        var botClient=mock(client.BotClient.class,RETURNS_DEEP_STUBS);var bot=spy(Character.getDefault(botClient));
        doReturn(java.util.List.of()).when(bot).recalcLocalStats();
        when(botClient.getPlayer()).thenReturn(bot);
        when(botClient.getChannelServer().getPlayerStorage().getCharacterById(anyInt())).thenReturn(bot);
        bot.setMap(mock(MapleMap.class));
        var now=new java.util.concurrent.atomic.AtomicLong(100000);
        var callbacks=new java.util.ArrayList<Runnable>();
        try(var server=mockStatic(Server.class);var timers=mockStatic(TimerManager.class)) {
            var instance=mock(Server.class);server.when(Server::getInstance).thenReturn(instance);
            when(instance.getCurrentTime()).thenAnswer(ignored->now.get());
            var timer=mock(TimerManager.class);timers.when(TimerManager::getInstance).thenReturn(timer);
            when(timer.register(any(Runnable.class),eq(1500L))).thenAnswer(call->{callbacks.add(call.getArgument(0));return mock(java.util.concurrent.ScheduledFuture.class);});
            var shield=client.SkillFactory.getSkill(constants.skills.Bishop.HOLY_SHIELD).getEffect(30);
            bot.registerEffect(shield,now.get(),now.get()+40000,false);
            bot.addCooldown(constants.skills.Bishop.HOLY_SHIELD,now.get(),120000);
            bot.buffExpireTask();bot.skillCooldownTask();
            assertNotNull(bot.getBuffedValue(BuffStat.HOLY_SHIELD));
            now.set(140001);callbacks.forEach(Runnable::run);
            assertNull(bot.getBuffedValue(BuffStat.HOLY_SHIELD));
            assertTrue(bot.skillIsCooling(constants.skills.Bishop.HOLY_SHIELD));
            now.set(220001);callbacks.forEach(Runnable::run);
            assertFalse(bot.skillIsCooling(constants.skills.Bishop.HOLY_SHIELD));
        } finally {bot.cancelBuffExpireTask();bot.cancelSkillCooldownTask();}
    }
    @Test void incomingMagicRangeContainsTheRecordedRealClientLightningHits() {
        var bot=mock(Character.class);when(bot.getJob()).thenReturn(Job.NIGHTLORD);
        when(bot.getTotalMdef()).thenReturn(258);when(bot.getTotalStr()).thenReturn(27);
        when(bot.getTotalDex()).thenReturn(111);when(bot.getTotalLuk()).thenReturn(755);
        int min=(int)Math.floor(BotIncomingMagicDamage.onHit(bot,550,0.0075d));
        int max=(int)Math.floor(BotIncomingMagicDamage.onHit(bot,550,0.008d));
        assertEquals(2160,min);assertEquals(2312,max);
        for(int actual:new int[]{2285,2248,2234,2290,2255,2216,2166,2225})assertTrue(actual>=min&&actual<=max);
        double normal=BotIncomingMagicDamage.onHit(bot,550,0.00775d);
        when(bot.getTotalMdef()).thenReturn(1258);
        assertEquals(250,normal-BotIncomingMagicDamage.onHit(bot,550,0.00775d),0.00001);
    }
    @Test void armIceColumnsUseTheirOwnWzDamageAndDelay() {
        var attack=BotMobAttackObserver.attack(8800006,0);
        assertNotNull(attack);assertEquals(3,attack.type());assertEquals(3280,attack.delay());assertEquals(515,attack.pad());
        var lightning=BotMobAttackObserver.attack(8800001,1);
        assertTrue(lightning.magic());assertEquals(1100,lightning.delay());
        assertNull(BotMobAttackObserver.attack(8800009,0),"Do not turn a grounded-only tremor into an unconditional area attack");
    }
    @Test void stunnedInputStopsQueuedMovementButContinuesGroundPhysics() {
        var bot=mock(Character.class);when(bot.hasDisease(Disease.STUN)).thenReturn(true);
        var entry=new BotEntry(bot,bot,null);entry.moveDir=1;entry.ropeEntryPending=true;
        entry.downJumpPending=true;entry.fixedAirArc=true;
        try(var movement=mockStatic(BotMovementManager.class)) {
            BotManager.getInstance().tickStunned(entry);
            assertEquals(0,entry.moveDir);assertFalse(entry.ropeEntryPending);
            assertFalse(entry.downJumpPending);assertFalse(entry.fixedAirArc);
            movement.verify(()->BotMovementManager.clearNavigationState(entry));
            movement.verify(()->BotMovementManager.tickGrounded(entry,null));
        }
    }
    @Test void stunBlocksEveryAttackAndSealBlocksSkillAttacks() {
        var bot=mock(Character.class);var entry=new BotEntry(bot,bot,null);
        var skill=new BotCombatManager.AttackPlan(4121007,30,3,new Rectangle(),java.util.List.of(),
                BotCombatManager.AttackRoute.RANGED,0,0,0,0,0,0,600,client.inventory.WeaponType.CLAW);
        var basic=new BotCombatManager.AttackPlan(0,0,1,new Rectangle(),java.util.List.of(),
                BotCombatManager.AttackRoute.CLOSE,0,0,0,0,0,0,600,client.inventory.WeaponType.CLAW);
        when(bot.hasDisease(Disease.STUN)).thenReturn(true);
        assertFalse(BotCombatManager.canUseAttackPlanNow(entry,client.inventory.WeaponType.CLAW,skill));
        assertFalse(BotCombatManager.canUseAttackPlanNow(entry,client.inventory.WeaponType.CLAW,basic));
        assertFalse(BotCombatManager.tickSupportHealing(entry,bot));
        when(bot.hasDisease(Disease.STUN)).thenReturn(false);when(bot.hasDisease(Disease.SEAL)).thenReturn(true);
        assertFalse(BotCombatManager.canUseAttackPlanNow(entry,client.inventory.WeaponType.CLAW,skill));
        assertTrue(BotCombatManager.canUseAttackPlanNow(entry,client.inventory.WeaponType.CLAW,basic));
    }
    @Test void capturedPillarMaskSelectsOnlyTheVisibleWzColumns() {
        var attack=BotMobAttackObserver.attack(8800000,2);
        assertEquals(3,attack.type());assertEquals(2560,attack.delay());assertEquals(560,attack.pad());
        var map=mock(MapleMap.class);
        when(map.getPointBelow(any())).thenAnswer(inv->{Point p=inv.getArgument(0);return new Point(p.x,-215);});
        var cast=new BotMobAttackObserver.Cast(map,mock(Monster.class),attack,2,0x0658,new Point(-9,-216),0);
        var areas=BotMobAttackObserver.areas(cast);
        assertEquals(java.util.List.of(-369,-249,-9,351,471),areas.stream().map(r->r.x).toList());
        assertTrue(areas.stream().anyMatch(r->r.contains(-275,-230)));
        assertFalse(areas.stream().anyMatch(r->r.contains(-426,-230)),"An unselected column must not hit the human's recorded location");
        assertFalse(areas.stream().anyMatch(r->r.contains(-275,-500)),"Pillars must not become full-height arena hits");
    }

    @Test void capturedHumanPillarHitsFallInsideTheirSelectedColumn() {
        var map=mock(MapleMap.class);
        when(map.getPointBelow(any())).thenAnswer(inv->{Point p=inv.getArgument(0);return new Point(p.x,-215);});
        for(int mask:new int[]{0x0cc4,0x0e94}) {
            var cast=new BotMobAttackObserver.Cast(map,mock(Monster.class),BotMobAttackObserver.attack(8800000,2),2,mask,new Point(-9,-216),0);
            assertTrue(BotMobAttackObserver.areas(cast).stream().anyMatch(r->r.contains(-426,-230)));
        }
    }

    @Test void damagePacketMatchesCapturedTakeDamageFieldOrder() {
        var mob=mock(Monster.class);when(mob.getId()).thenReturn(8800000);when(mob.getObjectId()).thenReturn(1000000005);
        var p=ByteBuffer.wrap(BotMobAttackObserver.hitPacket(17,mob,2,2300,1)).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(17,p.getInt());assertEquals(2,p.get());assertEquals(0,p.get());
        assertEquals(2300,p.getInt());assertEquals(8800000,p.getInt());assertEquals(1000000005,p.getInt());assertEquals(1,p.get());
    }

    @Test void physicalPillarUsesThePlayerHandlerForStunAndDamage() {
        var bot=mock(Character.class);var client=mock(client.BotClient.class);var map=mock(MapleMap.class);
        var mob=mock(Monster.class);var stats=mock(MonsterStats.class);var skill=mock(MobSkill.class);
        when(bot.getClient()).thenReturn(client);when(client.getPlayer()).thenReturn(bot);
        when(bot.getMap()).thenReturn(map);when(bot.getPosition()).thenReturn(new Point(-275,-215));
        when(bot.getJob()).thenReturn(Job.BOWMASTER);when(bot.getHp()).thenReturn(7000);when(bot.getMp()).thenReturn(3000);
        when(bot.getAutobanManager()).thenReturn(mock(AutobanManager.class));
        when(mob.getId()).thenReturn(8800000);when(mob.getObjectId()).thenReturn(123);when(mob.getStats()).thenReturn(stats);
        when(mob.getPosition()).thenReturn(new Point(-9,-216));when(map.getMapObject(123)).thenReturn(mob);
        try(var server=mockStatic(Server.class);var mobSkills=mockStatic(MobSkillFactory.class)) {
            var instance=mock(Server.class);server.when(Server::getInstance).thenReturn(instance);
            mobSkills.when(()->MobSkillFactory.getMobSkillOrThrow(MobSkillType.STUN,3)).thenReturn(skill);
            var entry=new BotEntry(bot,bot,null);
            BotMobAttackObserver.report(entry,bot,mob,2,2300);
            verify(skill).applyEffect(eq(bot),eq(mob),eq(false),anyList());
            verify(bot).addMPHP(-2300,0);
            assertTrue(entry.mobHitCooldownMs>0);
        }
    }

    @Test void lightningUsesThePlayerHandlerForSealAndMagicGuard() {
        var bot=mock(Character.class);var client=mock(client.BotClient.class);var map=mock(MapleMap.class);
        var mob=mock(Monster.class);var stats=mock(MonsterStats.class);var skill=mock(MobSkill.class);
        when(bot.getClient()).thenReturn(client);when(client.getPlayer()).thenReturn(bot);
        when(bot.getMap()).thenReturn(map);when(bot.getPosition()).thenReturn(new Point(-275,-215));
        when(bot.getJob()).thenReturn(Job.BISHOP);when(bot.getHp()).thenReturn(3500);when(bot.getMp()).thenReturn(10000);
        when(bot.getAutobanManager()).thenReturn(mock(AutobanManager.class));
        when(bot.getBuffedValue(BuffStat.MAGIC_GUARD)).thenReturn(80);
        when(mob.getId()).thenReturn(8800001);when(mob.getObjectId()).thenReturn(123);when(mob.getStats()).thenReturn(stats);
        when(mob.getPosition()).thenReturn(new Point(-9,-216));when(map.getMapObject(123)).thenReturn(mob);
        try(var server=mockStatic(Server.class);var mobSkills=mockStatic(MobSkillFactory.class)) {
            var instance=mock(Server.class);server.when(Server::getInstance).thenReturn(instance);
            mobSkills.when(()->MobSkillFactory.getMobSkillOrThrow(MobSkillType.SEAL,4)).thenReturn(skill);
            BotMobAttackObserver.report(new BotEntry(bot,bot,null),bot,mob,1,2200);
            verify(skill).applyEffect(eq(bot),eq(mob),eq(false),anyList());
            verify(bot).addMPHP(-440,-1760);
        }
    }
}
