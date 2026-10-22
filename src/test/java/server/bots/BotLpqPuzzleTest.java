package server.bots;

import org.junit.jupiter.api.Test;
import tools.PacketCreator;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class BotLpqPuzzleTest {
    @Test
    void enumeratesEveryDistinctFiveOfNinePlacement() {
        var choices = BotLpqPuzzle.choices();
        assertEquals(126, choices.size());
        assertEquals(126, choices.stream().map(Arrays::toString).collect(Collectors.toSet()).size());
        for (var choice : choices) {
            assertEquals(5, Arrays.stream(choice).distinct().count());
            assertTrue(Arrays.stream(choice).allMatch(n -> n >= 0 && n < 9));
        }
    }
    @Test
    void recognizesOnlyTheActualWrongPlacementEffect() {
        assertTrue(BotLpqPuzzle.isWrongEffect(PacketCreator.showEffect("quest/party/wrong_kor")));
        assertFalse(BotLpqPuzzle.isWrongEffect(PacketCreator.showEffect("quest/party/clear")));
        assertFalse(BotLpqPuzzle.isWrongEffect(PacketCreator.playSound("Party1/Failed")));
    }
}
