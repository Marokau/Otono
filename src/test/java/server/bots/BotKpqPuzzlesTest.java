package server.bots;

import org.junit.jupiter.api.Test;
import server.partyquest.KerningPqGeometry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.awt.Point;
import client.Character;
import scripting.event.EventInstanceManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotKpqPuzzlesTest {
    @Test
    void humanFailedCheckChangesArrangementOnlyAfterWorkersAreReady() {
        Character human = mock(Character.class);
        when(human.getId()).thenReturn(99);
        var event = mock(EventInstanceManager.class);
        when(human.getEventInstance()).thenReturn(event);
        when(event.isEventLeader(human)).thenReturn(true);
        when(human.getMapId()).thenReturn(103000801);
        var workers = List.of(mock(Character.class), mock(Character.class), mock(Character.class));
        for (int i = 0; i < workers.size(); i++) {
            when(workers.get(i).getId()).thenReturn(i + 1);
            when(workers.get(i).getMapId()).thenReturn(103000801);
            when(workers.get(i).getEventInstance()).thenReturn(event);
            when(workers.get(i).getPosition()).thenReturn(new Point());
        }
        when(event.getPlayers()).thenReturn(List.of(human, workers.get(0), workers.get(1), workers.get(2)));
        var entry = new BotEntry(workers.get(2), workers.get(2), null);
        var previous = BotTravelManager.movementStep;
        BotTravelManager.movementStep = (e, target, ai) -> {};
        try {
            BotKpqPuzzles.tick(entry, workers.get(2), human, 2, true);
            Point first = new Point(entry.moveTarget);
            BotKpqPuzzles.humanCheck(human, 2);
            BotKpqPuzzles.tick(entry, workers.get(2), human, 2, true);
            assertEquals(first, entry.moveTarget, "An early human click must not scramble moving workers");
            var areas = KerningPqGeometry.areas(2);
            for (int i = 0; i < 3; i++)
                when(workers.get(i).getPosition()).thenReturn(new Point((int) areas[i].getCenterX(), (int) areas[i].getCenterY()));
            BotKpqPuzzles.humanCheck(human, 2);
            BotKpqPuzzles.tick(entry, workers.get(2), human, 2, true);
            assertEquals(new Point((int) areas[3].getCenterX(), (int) areas[3].getCenterY()), entry.moveTarget);
        } finally {
            BotTravelManager.movementStep = previous;
        }
    }

    @Test
    void enumeratesEveryLegalThreeWorkerPlacementWithoutKnowingTheAnswer() {
        for (int stage = 2; stage <= 4; stage++) {
            int size = KerningPqGeometry.areas(stage).length;
            var combinations = BotKpqPuzzles.combinations(size);
            assertEquals(size * (size - 1) * (size - 2) / 6, combinations.size());
            var unique = new HashSet<String>();
            for (var placement : combinations) {
                assertEquals(3, placement.length);
                assertTrue(placement[0] < placement[1] && placement[1] < placement[2]);
                assertTrue(placement[0] >= 0 && placement[2] < size);
                assertTrue(unique.add(Arrays.toString(placement)));
            }
        }
    }

    @Test
    void callersCannotMutateClotosGeometryForLaterAttempts() {
        var areas = KerningPqGeometry.areas(2);
        areas[0].x = 0;
        assertEquals(-755, KerningPqGeometry.areas(2)[0].x);
        assertThrows(IllegalArgumentException.class, () -> KerningPqGeometry.areas(1));
    }
}
