package server.bots;

import client.Character;
import client.QuestStatus;
import client.inventory.manipulator.InventoryManipulator;
import constants.id.NpcId;
import net.server.world.Party;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import server.quest.Quest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static server.bots.BotZakumPrequestManager.*;

class BotZakumFallbackTest {
    private final BotQuestManager.QuestGate originalGate = BotQuestManager.gate;

    @AfterEach
    void restore() {
        BotQuestManager.gate = originalGate;
    }

    private QuestStatus progress(Map<Integer, String> saved) {
        QuestStatus status = mock(QuestStatus.class);
        when(status.getProgress(anyInt())).thenAnswer(call -> saved.getOrDefault(call.getArgument(0), ""));
        doAnswer(call -> { saved.put(call.getArgument(0), call.getArgument(1)); return null; })
                .when(status).setProgress(anyInt(), anyString());
        return status;
    }

    @Test
    void retryDisarmAndReloadKeepTheAccumulatedBudget() {
        Map<Integer, String> saved = new HashMap<>();
        QuestStatus status = progress(saved);
        BotEntry entry = new BotEntry(null, null, null);
        advanceBudget(entry, status, 1_000L);
        advanceBudget(entry, status, 2_000L);
        clearZakumErrand(entry);
        advanceBudget(entry, status, 3_000L);
        assertEquals(2_000L, budgetSpent(status));
        BotEntry reloaded = new BotEntry(null, null, null);
        QuestStatus restored = progress(new HashMap<>(saved));
        advanceBudget(reloaded, restored, 9_000_000L);
        assertEquals(2_000L, budgetSpent(restored), "offline time is not pursuit time");
        advanceBudget(reloaded, restored, 9_001_000L);
        assertEquals(3_000L, budgetSpent(restored));
    }

    @Test
    void movementAndStepProgressCannotExtendTheBudget() {
        QuestStatus status = progress(new HashMap<>());
        BotEntry entry = new BotEntry(null, null, null);
        advanceBudget(entry, status, 1_000L);
        for (long time = 2_000; time <= FALLBACK_BUDGET_MS + 2_000; time += 1_000) {
            entry.zakumErrandProgress.touch(time);
            advanceBudget(entry, status, time);
        }
        assertEquals(FALLBACK_BUDGET_MS, budgetSpent(status));
    }

    @Test
    void pausedTicksAndClockRollbackCannotConsumeOrReverseHours() {
        QuestStatus status = progress(new HashMap<>());
        BotEntry entry = new BotEntry(null, null, null);
        advanceBudget(entry, status, 1_000L);
        advanceBudget(entry, status, 8_000_000L);
        advanceBudget(entry, status, 7_000_000L);
        assertEquals(1_000L, budgetSpent(status));
    }

    @Test
    void fullInventoryDoesNotCompleteQuest() {
        BotQuestManager.gate = mock(BotQuestManager.QuestGate.class);
        Character bot = mock(Character.class);
        try (var quests = mockStatic(Quest.class)) {
            assertFalse(completeFallback(bot));
            verify(bot).canHold(ITEM_EYE_OF_FIRE, 5);
            quests.verifyNoInteractions();
        }
    }

    @Test
    void grantsOnlyMissingEyesAndCompletesWithoutPartySync() {
        BotQuestManager.gate = mock(BotQuestManager.QuestGate.class);
        Character bot = mock(Character.class);
        when(bot.getItemQuantity(ITEM_EYE_OF_FIRE, false)).thenReturn(2);
        when(bot.canHold(ITEM_EYE_OF_FIRE, 3)).thenReturn(true);
        try (var items = mockStatic(InventoryManipulator.class); var quests = mockStatic(Quest.class)) {
            Quest approval = mock(Quest.class);
            Quest trials = mock(Quest.class);
            quests.when(() -> Quest.getInstance(Q_APPROVAL)).thenReturn(approval);
            quests.when(() -> Quest.getInstance(Q_TRIALS)).thenReturn(trials);
            items.when(() -> InventoryManipulator.addById(null, ITEM_EYE_OF_FIRE, (short) 3)).thenReturn(true);
            assertTrue(completeFallback(bot));
            items.verify(() -> InventoryManipulator.addById(null, ITEM_EYE_OF_FIRE, (short) 3));
            verify(approval).forceStartWithActions(bot, NpcId.MAPLE_ADMINISTRATOR);
            verify(trials).forceCompleteWithActions(bot, NpcId.MAPLE_ADMINISTRATOR, null);
            verify(trials, never()).forceComplete(any(), anyInt());
            when(BotQuestManager.gate.isCompleted(bot, Q_TRIALS)).thenReturn(true);
            assertTrue(completeFallback(bot));
            items.verifyNoMoreInteractions();
            verifyNoMoreInteractions(trials, approval);
        }
    }

    @Test
    void failedItemGrantLeavesTrialsIncomplete() {
        BotQuestManager.gate = mock(BotQuestManager.QuestGate.class);
        Character bot = mock(Character.class);
        when(bot.canHold(ITEM_EYE_OF_FIRE, 5)).thenReturn(true);
        try (var items = mockStatic(InventoryManipulator.class); var quests = mockStatic(Quest.class)) {
            assertFalse(completeFallback(bot));
            quests.verifyNoInteractions();
        }
    }

    @Test
    void exhaustedBudgetExitsTrialEvenDuringLongRetryCooldown() {
        BotQuestManager.gate = mock(BotQuestManager.QuestGate.class);
        Character bot = mock(Character.class);
        QuestStatus status = progress(new HashMap<>(Map.of(BUDGET_PROGRESS_KEY, Long.toString(FALLBACK_BUDGET_MS))));
        when(bot.getQuest(Q_TRIALS)).thenReturn(status);
        when(bot.getMapId()).thenReturn(280020000);
        BotEntry entry = new BotEntry(bot, null, null);
        entry.autopilotMapId = DOOR_MAP;
        entry.zakumErrandMapId = DOOR_MAP;
        entry.nextZakumScanAtMs = Long.MAX_VALUE;
        try (var run = mockStatic(BotZakumPqRun.class); var items = mockStatic(InventoryManipulator.class)) {
            assertTrue(tickErrand(entry, bot, true));
            run.verify(() -> BotZakumPqRun.warpToDoor(bot));
            items.verifyNoInteractions();
        }
    }

    @Test
    void joiningHumanPartyDoesNotWarpOutOrAutoComplete() {
        Character bot = mock(Character.class);
        when(bot.getParty()).thenReturn(mock(Party.class));
        BotEntry entry = new BotEntry(bot, null, null);
        entry.autopilotMapId = DOOR_MAP;
        entry.zakumErrandMapId = DOOR_MAP;
        try (var manager = mockStatic(BotManager.class); var run = mockStatic(BotZakumPqRun.class);
             var items = mockStatic(InventoryManipulator.class)) {
            manager.when(() -> BotManager.onlinePartyMembersAllBots(bot)).thenReturn(false);
            assertFalse(tickErrand(entry, bot, true));
            assertFalse(active(entry));
            run.verifyNoInteractions();
            items.verifyNoInteractions();
        }
    }
}
