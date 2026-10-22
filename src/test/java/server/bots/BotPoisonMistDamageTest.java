package server.bots;

import client.Character;
import client.Job;
import client.BuffStat;
import client.autoban.AutobanManager;
import net.server.Server;
import org.junit.jupiter.api.Test;
import server.life.MobSkillFactory;
import server.life.MobSkillType;
import server.life.Monster;
import server.maps.MapleMap;
import server.maps.Mist;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotPoisonMistDamageTest {
    private Mist cloud(int level,Monster owner) {
        return new Mist(new Rectangle(-135,-80,270,120),owner,
                MobSkillFactory.getMobSkillOrThrow(MobSkillType.AREA_POISON,level));
    }

    @Test void realCloudSpawnPacketPreservesItsOwnWzDamageAcrossBossPhases() {
        var owner=mock(Monster.class);when(owner.getId()).thenReturn(9300182);
        assertEquals(50,BotPoisonMistDamage.damage(cloud(8,owner)));
        assertEquals(200,BotPoisonMistDamage.damage(cloud(5,owner)));
    }

    @Test void contactTicksOncePerSecondWithoutStackingAndStopsOutsideOrAfterRemoval() {
        var bot=mock(Character.class);var map=mock(MapleMap.class);var owner=mock(Monster.class);
        when(bot.getMap()).thenReturn(map);when(bot.getHp()).thenReturn(1000);
        when(bot.getEventInstance()).thenReturn(mock(scripting.event.EventInstanceManager.class));
        when(owner.getId()).thenReturn(9300182);
        var first=cloud(5,owner);var second=cloud(5,owner);
        when(map.getMapObjects()).thenReturn(List.of(first,second));
        var entry=new BotEntry(bot,bot,null);
        try(var ellin=mockStatic(BotEllinRun.class);var combat=mockStatic(BotCombatManager.class);
            var damage=mockStatic(BotPoisonMistDamage.class,CALLS_REAL_METHODS)) {
            ellin.when(()->BotEllinRun.inInstance(bot)).thenReturn(true);
            combat.when(()->BotCombatManager.getBotTouchBounds(entry,bot)).thenReturn(new Rectangle(-10,-30,20,30));
            damage.when(()->BotPoisonMistDamage.report(bot,200)).thenAnswer(call->null);
            BotPoisonMistDamage.tick(entry,bot,1000);
            BotPoisonMistDamage.tick(entry,bot,1999);
            damage.verify(()->BotPoisonMistDamage.report(bot,200),times(1));
            BotPoisonMistDamage.tick(entry,bot,2000);
            damage.verify(()->BotPoisonMistDamage.report(bot,200),times(2));
            combat.when(()->BotCombatManager.getBotTouchBounds(entry,bot)).thenReturn(new Rectangle(500,0,20,30));
            BotPoisonMistDamage.tick(entry,bot,3000);
            combat.when(()->BotCombatManager.getBotTouchBounds(entry,bot)).thenReturn(new Rectangle(-10,-30,20,30));
            when(map.getMapObjects()).thenReturn(List.of());
            BotPoisonMistDamage.tick(entry,bot,4000);
            damage.verify(()->BotPoisonMistDamage.report(bot,200),times(2));
        }
    }

    @Test void inactiveInstancesAndDeadBotsDoNotTakeCloudDamage() {
        var bot=mock(Character.class);var entry=new BotEntry(bot,bot,null);
        try(var ellin=mockStatic(BotEllinRun.class)) {
            BotPoisonMistDamage.tick(entry,bot,1000);
            ellin.when(()->BotEllinRun.inInstance(bot)).thenReturn(true);
            BotPoisonMistDamage.tick(entry,bot,2000);
            verify(bot,never()).getMap();
        }
    }

    @Test void mistDamageUsesPlayerHandlerAndExistingMagicGuard() {
        var bot=mock(Character.class);var client=mock(client.BotClient.class);var map=mock(MapleMap.class);
        when(bot.getClient()).thenReturn(client);when(client.getPlayer()).thenReturn(bot);
        when(bot.getMap()).thenReturn(map);when(bot.getJob()).thenReturn(Job.CLERIC);
        when(bot.getHp()).thenReturn(1000);when(bot.getMp()).thenReturn(1000);
        when(bot.getAutobanManager()).thenReturn(mock(AutobanManager.class));
        when(bot.getBuffedValue(BuffStat.MAGIC_GUARD)).thenReturn(80);
        try(var server=mockStatic(Server.class)) {
            server.when(Server::getInstance).thenReturn(mock(Server.class));
            BotPoisonMistDamage.report(bot,200);
            verify(bot).addMPHP(-40,-160);
        }
    }

    @Test void frankenroidCloudDamagesEventBotWithoutEllinRegistration() {
        var bot=mock(Character.class);var map=mock(MapleMap.class);var owner=mock(Monster.class);
        when(bot.getMap()).thenReturn(map);when(bot.getHp()).thenReturn(1000);
        when(bot.getEventInstance()).thenReturn(mock(scripting.event.EventInstanceManager.class));
        when(owner.getId()).thenReturn(9300139);
        var mist=cloud(6,owner);int amount=BotPoisonMistDamage.damage(mist);assertTrue(amount>0);
        when(map.getMapObjects()).thenReturn(List.of(mist));var entry=new BotEntry(bot,bot,null);
        try(var combat=mockStatic(BotCombatManager.class);var damage=mockStatic(BotPoisonMistDamage.class,CALLS_REAL_METHODS)) {
            combat.when(()->BotCombatManager.getBotTouchBounds(entry,bot)).thenReturn(new Rectangle(-10,-30,20,30));
            damage.when(()->BotPoisonMistDamage.report(bot,amount)).thenAnswer(call->null);
            BotPoisonMistDamage.tick(entry,bot,1000);BotPoisonMistDamage.tick(entry,bot,1999);
            damage.verify(()->BotPoisonMistDamage.report(bot,amount),times(1));
            when(bot.getEventInstance()).thenReturn(null);BotPoisonMistDamage.tick(entry,bot,2000);
            damage.verify(()->BotPoisonMistDamage.report(bot,amount),times(1));
        }
    }
}
