package server.bots;

import client.Character;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import server.maps.MapleMap;
import tools.PacketCreator;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotLpqHumanPuzzleTest {
    @Test void onlyOneObserverAdvancesAfterARealWrongCheckAndMovingWorkersIgnoreEarlyFeedback() {
        var human = mock(Character.class);
        var event = mock(EventInstanceManager.class);
        var map = mock(MapleMap.class);
        var areas = new ArrayList<Rectangle>();
        for (int n = 0; n < 9; n++) areas.add(new Rectangle(n * 100, 0, 50, 50));
        when(map.getAreas()).thenReturn(areas);
        var workers = new ArrayList<Character>();
        for (int n = 0; n < 5; n++) {
            var bot = mock(Character.class);
            when(bot.getId()).thenReturn(n + 1);
            when(bot.getEventInstance()).thenReturn(event);
            when(bot.getMap()).thenReturn(map);
            when(bot.getMapId()).thenReturn(922010800);
            when(bot.getPosition()).thenReturn(new Point(n * 100 + 25, 25));
            workers.add(bot);
        }
        var players = new ArrayList<>(workers);
        players.add(human);
        when(event.getPlayers()).thenReturn(players);
        BotLpqHuman.register(human, workers);
        var last = workers.get(4);
        var entry = new BotEntry(last, last, null);
        try (var movement = mockStatic(BotZakumPqRun.class); var physics = mockStatic(BotPhysicsEngine.class)) {
            BotLpqHumanPuzzle.tick(entry, last, true);
            movement.verify(() -> BotZakumPqRun.walkTo(entry, last, new Point(425, 25), true));
            for (var worker : workers) BotLpqPuzzle.observeEffect(worker, PacketCreator.showEffect("quest/party/clear"));
            BotLpqHumanPuzzle.tick(entry, last, true);
            movement.verify(() -> BotZakumPqRun.walkTo(entry, last, new Point(425, 25), true), times(2));
            for (var worker : workers) BotLpqPuzzle.observeEffect(worker, PacketCreator.showEffect("quest/party/wrong_kor"));
            BotLpqHumanPuzzle.tick(entry, last, true);
            movement.verify(() -> BotZakumPqRun.walkTo(entry, last, new Point(525, 25), true));
            // The new candidate is not occupied yet. Another early/duplicate effect cannot skip it.
            BotLpqPuzzle.observeEffect(workers.getFirst(), PacketCreator.showEffect("quest/party/wrong_kor"));
            BotLpqHumanPuzzle.tick(entry, last, true);
            movement.verify(() -> BotZakumPqRun.walkTo(entry, last, new Point(525, 25), true), times(2));
            verify(event, never()).getProperty("stage8combo");
        } finally { workers.forEach(BotLpqHuman::unregister); }
    }
}
