package server.bots;
import org.junit.jupiter.api.Test;
import java.awt.Point;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
class BotOpqLoungeNavigationTest {
    @org.junit.jupiter.params.ParameterizedTest @org.junit.jupiter.params.provider.ValueSource(ints = {-1640,-1635})
    void hasteWorkerCanCrossTheSecondPortalRowWithoutFalling(int arrivalY) {
        var map = BotNavigationMapLoader.loadMapGeometry(920010600);
        var lab = BotMovementSimulationLab.fromMap(map);
        var entry = lab.spawnBot("Scout", 586, map, new Point(-94,arrivalY));
        when(entry.bot.getTotalMoveSpeedStat()).thenReturn(140);
        when(entry.bot.getTotalJumpStat()).thenReturn(120);
        entry.movementProfile = new BotMovementProfile(140,120);
        BotNavigationGraphProvider.rebuildGraph(map, entry.movementProfile);
        lab.setMoveTarget("Scout", new Point(-181,-1635), true);
        for (int n = 0; n < 120; n++) {
            lab.step(1);
            if (!entry.inAir && entry.bot.getPosition().distance(new Point(-181,-1635)) < 10) break;
        }
        assertTrue(entry.bot.getPosition().y < -1600, entry.bot.getPosition().toString());
        assertTrue(Math.abs(entry.bot.getPosition().x + 181) < 10, entry.bot.getPosition().toString());
    }
}
