package server.bots;

import client.Character;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import server.maps.MapleMap;
import server.maps.Reactor;
import java.awt.Point;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotOpqRunTest {
    @Test void gmHumanLeaderPassesEligibilityButGmCompanionsRemainRejected() {
        var human = mock(Character.class);
        when(human.getId()).thenReturn(100);
        when(human.getName()).thenReturn("Human");
        when(human.isGM()).thenReturn(true);
        when(human.getLevel()).thenReturn(51);
        when(human.getMapId()).thenReturn(BotOpqRun.RECRUIT_MAP);
        when(human.getClient()).thenReturn(mock(client.Client.class));
        var bots = new java.util.ArrayList<Character>();
        for (int id = 1; id <= 5; id++) {
            var bot = mock(Character.class);
            when(bot.getId()).thenReturn(id);
            when(bot.getName()).thenReturn("Companion" + id);
            when(bot.getClient()).thenReturn(mock(client.BotClient.class));
            when(bot.isGM()).thenReturn(true);
            bots.add(bot);
        }
        BotOpqHuman.register(human, bots);
        var crew = new java.util.ArrayList<Character>(); crew.add(human); crew.addAll(bots);
        try (var managers = mockStatic(BotManager.class)) {
            managers.when(BotManager::getInstance).thenReturn(mock(BotManager.class));
            var failure = assertThrows(IllegalArgumentException.class, () -> BotOpqRun.startCrew(crew));
            assertEquals("Ineligible OPQ member: Companion1", failure.getMessage());
        } finally { for (var bot : bots) BotOpqHuman.unregister(bot); }
    }
    @Test void anUnrelatedBotIsNotControlled() {
        var bot = mock(Character.class);
        assertFalse(BotOpqRun.tick(new BotEntry(bot, bot, null), bot, true));
        assertThrows(IllegalArgumentException.class, () -> BotOpqRun.startCrew(List.of(bot)));
    }
    @Test void instanceMembershipNeedsAnActualEvent() {
        var bot = mock(Character.class);
        when(bot.getMapId()).thenReturn(920010000);
        assertFalse(BotOpqRun.inInstance(bot));
        when(bot.getEventInstance()).thenReturn(mock(EventInstanceManager.class));
        assertTrue(BotOpqRun.inInstance(bot));
    }
    @Test void celionReactorDropsAreNeverVacuumedUp() {
        var bot = mock(Character.class);
        assertFalse(BotOpqRun.canLoot(new BotEntry(bot, bot, null), bot, 4001074));
    }
    @Test void entranceCloudAboveTheFloorIsReachedWithARealVerticalJump() {
        var bot = mock(Character.class);
        var map = mock(MapleMap.class);
        var cloud = mock(Reactor.class);
        when(bot.getMap()).thenReturn(map);
        when(bot.getPosition()).thenReturn(new Point(23, 143));
        when(cloud.getPosition()).thenReturn(new Point(24, 53));
        when(cloud.isActive()).thenReturn(true);
        when(cloud.getId()).thenReturn(2002001);
        when(map.getAllReactors()).thenReturn(List.of(cloud));
        var entry = new BotEntry(bot, bot, null);
        entry.movementProfile = BotMovementProfile.BASE;
        try (var physics = mockStatic(BotPhysicsEngine.class); var movement = mockStatic(BotMovementManager.class);
             var manager = mockStatic(BotManager.class); var travel = mockStatic(BotTravelManager.class)) {
            physics.when(() -> BotPhysicsEngine.calculateMaxJumpHeight(BotMovementProfile.BASE)).thenReturn(110.0f);
            manager.when(BotManager::getInstance).thenReturn(mock(BotManager.class));
            BotOpqRun.smash(entry, bot, r -> r.getId() == 2002001, true);
            movement.verify(() -> BotMovementManager.initiateJump(entry, bot, 0));
            assertTrue(entry.fixedAirArc);
        }
    }
}
