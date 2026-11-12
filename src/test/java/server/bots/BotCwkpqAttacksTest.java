package server.bots;

import client.Character;
import client.BotClient;
import net.opcodes.SendOpcode;
import net.packet.OutPacket;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import server.life.Monster;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotCwkpqAttacksTest {
    @Test void nativeCouncilClientForwardsObservedMonsterAttacks() {
        var bot = mock(Character.class);
        when(bot.getMapId()).thenReturn(610030600);
        when(bot.getEventInstance()).thenReturn(mock(EventInstanceManager.class));
        var client = spy(new BotClient(0, 1));
        doReturn(bot).when(client).getPlayer();
        doReturn(null).when(client).getCM();
        var packet = OutPacket.create(SendOpcode.MOVE_MONSTER);
        try (var observer = mockStatic(BotMobAttackObserver.class)) {
            client.sendPacket(packet);
            observer.verify(() -> BotMobAttackObserver.observe(bot, packet));
        }
        when(bot.getClient()).thenReturn(client);
        assertTrue(BotRaidAttackPackets.required(bot, packet));
        assertFalse(BotRaidAttackPackets.required(bot, OutPacket.create(SendOpcode.MOVE_PLAYER)));
        when(bot.getEventInstance()).thenReturn(null);
        assertFalse(BotCwkpqAttacks.inInstance(bot));
        assertFalse(BotRaidAttackPackets.required(bot, packet));
    }

    @Test void redNirgStunUsesItsNativeDelayAndActuallyQueues() throws Exception {
        var bot = mock(Character.class);
        var map = mock(server.maps.MapleMap.class);
        var mob = mock(Monster.class);
        when(bot.getMapId()).thenReturn(610030600);
        when(bot.getEventInstance()).thenReturn(mock(EventInstanceManager.class));
        when(bot.getMap()).thenReturn(map);
        when(map.getMapObject(42)).thenReturn(mob);
        when(mob.getId()).thenReturn(9400591);
        var packet = mock(net.packet.Packet.class);
        when(packet.getBytes()).thenReturn(ByteBuffer.allocate(17).order(ByteOrder.LITTLE_ENDIAN)
                .putShort((short)SendOpcode.MOVE_MONSTER.getValue()).putInt(42)
                .put((byte)0).put((byte)0).put((byte)26).putInt(0)
                .putShort((short)100).putShort((short)200).array());
        var field = BotMobAttackObserver.class.getDeclaredField("PENDING");
        field.setAccessible(true);
        var pending = (Map<Character, Queue<BotMobAttackObserver.Cast>>)field.get(null);
        try {
            BotMobAttackObserver.observe(bot, packet);
            assertNotNull(pending.get(bot));
            var cast = pending.get(bot).peek();
            assertEquals(1, cast.index());
            assertEquals(550, cast.attack().delay());
            assertEquals(630, cast.attack().pad());
            assertTrue(BotMobAttackObserver.areas(cast).getFirst().contains(100, 150));
            var nativeAttack = server.life.MobAttackInfoFactory.getMobAttackInfo(mob, 1);
            assertEquals(server.life.MobSkillType.STUN.getId(), nativeAttack.getDiseaseSkill());
            assertEquals(3, nativeAttack.getDiseaseLevel());
        } finally {
            pending.remove(bot);
        }
    }

    @Test void rellikKeepsItsNativeDeadlyPoisonAndLightningDefinitions() {
        var poison = BotMobAttackObserver.attack(9400592, 0);
        assertNotNull(poison);
        assertTrue(poison.deadly());
        assertTrue(poison.magic());
        assertEquals(2750, poison.delay());
        var lightning = BotMobAttackObserver.attack(9400592, 1);
        assertNotNull(lightning);
        assertTrue(lightning.magic());
        assertEquals(3000, lightning.delay());
        var guardian = BotMobAttackObserver.attack(9400594, 0);
        assertNotNull(guardian);
        assertTrue(guardian.magic());
        assertEquals(1040, guardian.delay());
    }

    @Test void redNirgHitAppliesNativeStunAndDamage() {
        var bot = mock(Character.class);
        var botClient = mock(BotClient.class);
        var map = mock(server.maps.MapleMap.class);
        var mob = mock(Monster.class);
        var stats = mock(server.life.MonsterStats.class);
        var stun = mock(server.life.MobSkill.class);
        when(bot.getClient()).thenReturn(botClient);
        when(botClient.getPlayer()).thenReturn(bot);
        when(bot.getMap()).thenReturn(map);
        when(bot.getPosition()).thenReturn(new java.awt.Point(0, 0));
        when(bot.getJob()).thenReturn(client.Job.BOWMASTER);
        when(bot.getHp()).thenReturn(7000);
        when(bot.getMp()).thenReturn(3000);
        when(bot.getAutobanManager()).thenReturn(mock(client.autoban.AutobanManager.class));
        when(mob.getId()).thenReturn(9400591);
        when(mob.getObjectId()).thenReturn(42);
        when(mob.getPosition()).thenReturn(new java.awt.Point(100, 0));
        when(mob.getStats()).thenReturn(stats);
        when(map.getMapObject(42)).thenReturn(mob);
        try (var serverMock = mockStatic(net.server.Server.class);
             var skills = mockStatic(server.life.MobSkillFactory.class)) {
            serverMock.when(net.server.Server::getInstance).thenReturn(mock(net.server.Server.class));
            skills.when(() -> server.life.MobSkillFactory.getMobSkillOrThrow(server.life.MobSkillType.STUN, 3))
                    .thenReturn(stun);
            BotMobAttackObserver.report(new BotEntry(bot, bot, null), bot, mob, 1, 1200);
            verify(stun).applyEffect(eq(bot), eq(mob), eq(false), anyList());
            verify(bot).addMPHP(-1200, 0);
        }
    }
}
