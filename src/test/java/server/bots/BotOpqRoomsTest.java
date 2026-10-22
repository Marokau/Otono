package server.bots;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class BotOpqRoomsTest {
    @Test void sealedRoomCoversAllThreePlayerDistributionsAcrossThreePlatforms() {
        var choices = BotOpqRooms.placements();
        assertEquals(10, choices.size());
        var unique = new HashSet<String>();
        for (var choice : choices) {
            assertEquals(3, Arrays.stream(choice).sum());
            assertTrue(unique.add(Arrays.toString(choice)));
            int[] counts = new int[3];
            for (int worker = 0; worker < 3; worker++) counts[BotOpqRooms.areaForWorker(choice, worker)]++;
            assertArrayEquals(choice, counts);
        }
    }
}
