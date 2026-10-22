package server.bots;
import client.Character;
import org.junit.jupiter.api.Test;
import server.maps.MapleMap;
import server.maps.Reactor;
import server.life.Monster;
import java.awt.Point;
import java.util.List;
import static org.mockito.Mockito.*;
class BotOpqGardenTest {
    @Test void leaderFightsPapaPixieInsteadOfContinuingToPlantUnusedSeeds() {
        var bot = mock(Character.class);
        var map = mock(MapleMap.class);
        var pot = mock(Reactor.class);
        when(bot.getMap()).thenReturn(map);
        when(bot.getPosition()).thenReturn(new Point());
        when(bot.getItemQuantity(4001053, false)).thenReturn(1);
        when(pot.getId()).thenReturn(2001000);
        when(pot.getPosition()).thenReturn(new Point());
        when(map.getAllReactors()).thenReturn(List.of(pot));
        when(map.getMonsterById(9300039)).thenReturn(mock(Monster.class));
        var entry = new BotEntry(bot, bot, null);
        try (var actions = mockStatic(BotOpqRun.class)) {
            actions.when(() -> BotOpqRun.isLeader(bot)).thenReturn(true);
            BotOpqGarden.tick(entry, bot, true);
            actions.verify(() -> BotOpqRun.grind(entry, bot, true));
            actions.verify(() -> BotOpqRun.dropAt(entry, bot, pot, 4001053, 1, true), never());
        }
    }
}
