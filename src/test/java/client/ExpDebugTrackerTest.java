package client;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpDebugTrackerTest {
    @Test
    void shouldFormatResultsWithGroupedNumbers() {
        ExpDebugTracker.ExpSession session = new ExpDebugTracker.ExpSession(1, "Bowgurl", 70, false);
        session.totalPersonalExp.set(1234);
        session.totalPartyExp.set(5678);
        session.totalEquipExp.set(90);
        session.expGainCount.set(12);
        session.startTime = System.currentTimeMillis() - 60_000;

        List<String> lines = ExpDebugTracker.formatResults(List.of(session));

        assertEquals(2, lines.size());
        assertEquals("--- EXP Debug Report (1 members) ---", lines.get(0));
        assertTrue(lines.get(1).contains("[CHR] Bowgurl (Lvl 70) | Total: 6,912"));
        assertTrue(lines.get(1).contains("Personal: 1,234"));
        assertTrue(lines.get(1).contains("Party: 5,678"));
        assertTrue(lines.get(1).contains("Equip: 90"));
        assertTrue(lines.get(1).contains("Gains: 12"));
        assertTrue(lines.get(1).contains("EXP/min: 6,912"));
    }
}
