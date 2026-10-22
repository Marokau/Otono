package server.bots;

import client.BotClient;
import client.Character;
import net.server.PlayerStorage;
import net.server.Server;
import net.server.world.World;
import net.packet.Packet;
import org.mockito.ArgumentCaptor;
import server.TimerManager;
import tools.PacketCreator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BotAmbientMegaphoneTest {
    private final BotManager.Config original = BotManager.cfg;

    @AfterEach
    void restoreConfig() {
        BotManager.cfg = original;
    }

    @Test
    void originalDialogueLoadsAllCategoriesAndNormalizesForV83() throws Exception {
        var dialogue = BotAmbientMegaphone.loadDialogue();
        assertEquals(8, dialogue.size());
        assertTrue(dialogue.stream().mapToInt(List::size).sum() > 800);
        for (var category : dialogue) {
            assertFalse(category.isEmpty());
            assertTrue(category.stream().anyMatch(line -> line.length() <= 52), "Category needs avatar dialogue");
            for (String line : category) {
                assertFalse(line.isBlank());
                assertTrue(line.chars().allMatch(c -> c >= 32 && c <= 126), line);
            }
        }
        assertTrue(dialogue.get(0).contains("Happy birthday Sarah!!! <3 <3 <3"));
        assertEquals("can't - wait...", BotAmbientMegaphone.ascii("can\u2019t \u2014 wait\u2026"));
    }

    @Test
    void avatarRowsPreserveWholeShortMessagesAndAlwaysHaveFourRows() {
        for (String line : List.of("hi", "1234567890123", "x".repeat(52))) {
            List<String> rows = BotAmbientMegaphone.avatarLines(line);
            assertEquals(4, rows.size());
            assertTrue(rows.stream().allMatch(row -> row.length() <= 13));
            assertEquals(line, String.join("", rows));
        }
        assertThrows(IllegalArgumentException.class, () -> BotAmbientMegaphone.avatarLines("x".repeat(53)));
    }

    @Test
    void avatarUsesSendersChannelAndSchedulesRemovalAfterTenSeconds() {
        Character bot = sender().bot;
        when(bot.getWorld()).thenReturn(3);
        try (var servers = mockStatic(Server.class); var timers = mockStatic(TimerManager.class);
             var packets = mockStatic(PacketCreator.class)) {
            Server server = mock(Server.class);
            TimerManager timer = mock(TimerManager.class);
            Packet show = mock(Packet.class);
            Packet hide = mock(Packet.class);
            servers.when(Server::getInstance).thenReturn(server);
            timers.when(TimerManager::getInstance).thenReturn(timer);
            packets.when(() -> PacketCreator.getAvatarMega(bot, "", 2, 5390006,
                    List.of("hello", "", "", ""), true)).thenReturn(show);
            packets.when(PacketCreator::byeAvatarMega).thenReturn(hide);
            BotAmbientMegaphone.broadcast(bot, "hello", true, 5390006);
            verify(server).broadcastMessage(3, show);
            var removal = ArgumentCaptor.forClass(Runnable.class);
            verify(timer).schedule(removal.capture(), eq(10_000L));
            removal.getValue().run();
            verify(server).broadcastMessage(3, hide);
            verifyNoMoreInteractions(server);
        }
    }

    @Test
    void categoryDistributionMatchesSoloMapling() {
        int[] counts = new int[8];
        for (int roll = 0; roll < 100; roll++) {
            counts[BotAmbientMegaphone.categoryForRoll(roll)]++;
        }
        assertArrayEquals(new int[]{5, 7, 7, 7, 19, 19, 18, 18}, counts);
        assertThrows(IllegalArgumentException.class, () -> BotAmbientMegaphone.categoryForRoll(-1));
        assertThrows(IllegalArgumentException.class, () -> BotAmbientMegaphone.categoryForRoll(100));
    }

    private BotEntry sender() {
        Character bot = mock(Character.class);
        BotClient client = mock(BotClient.class);
        when(bot.getClient()).thenReturn(client);
        when(bot.isLoggedinWorld()).thenReturn(true);
        when(bot.isAlive()).thenReturn(true);
        when(bot.getId()).thenReturn(7);
        when(bot.getName()).thenReturn("MapleBot");
        when(client.getChannel()).thenReturn(2);
        return new BotEntry(bot, null, null);
    }

    @Test
    void ownedOfflineAndLoggingOutBotsCannotSpeak() {
        BotEntry entry = sender();
        assertTrue(BotAmbientMegaphone.eligible(entry));
        entry.loggingOut = true;
        assertFalse(BotAmbientMegaphone.eligible(entry));
        entry.loggingOut = false;
        when(entry.bot.isLoggedinWorld()).thenReturn(false);
        assertFalse(BotAmbientMegaphone.eligible(entry));
        when(entry.bot.isLoggedinWorld()).thenReturn(true);
        assertFalse(BotAmbientMegaphone.eligible(new BotEntry(entry.bot, mock(Character.class), null)));
    }

    @Test
    void populationDoesNotMultiplyBroadcastsAndEmptyWorldsStayQuiet() {
        BotManager.cfg = new BotManager.Config();
        BotManager.cfg.AMBIENT_AVATAR_MEGAPHONE_PERCENT = 0;
        BotManager.cfg.AMBIENT_MEGAPHONE_MIN_MS = 10_000;
        BotManager.cfg.AMBIENT_MEGAPHONE_MAX_MS = 10_000;
        var ticker = new BotAmbientMegaphone();
        BotEntry entry = sender();
        Character human = mock(Character.class);
        when(human.isLoggedinWorld()).thenReturn(true);
        World world = mock(World.class);
        PlayerStorage storage = mock(PlayerStorage.class);
        when(world.getPlayerStorage()).thenReturn(storage);
        when(storage.getAllCharacters()).thenReturn(List.of(human, entry.bot));
        try (var servers = mockStatic(Server.class); var managers = mockStatic(BotManager.class)) {
            Server server = mock(Server.class);
            BotManager manager = mock(BotManager.class);
            servers.when(Server::getInstance).thenReturn(server);
            managers.when(BotManager::getInstance).thenReturn(manager);
            when(server.getWorlds()).thenReturn(List.of(world));
            // Even an oversized roster still shares a single world cooldown.
            when(manager.allEntries()).thenReturn(java.util.Collections.nCopies(100, entry));
            ticker.tick(1_000);
            ticker.tick(10_999);
            verify(server, never()).broadcastMessage(anyInt(), any());
            ticker.tick(11_000);
            ticker.tick(11_001);
            verify(server).broadcastMessage(eq(0), any());
            when(storage.getAllCharacters()).thenReturn(List.of(entry.bot));
            ticker.tick(21_000);
            verify(server).broadcastMessage(eq(0), any());
            when(storage.getAllCharacters()).thenReturn(List.of(human, entry.bot));
            ticker.tick(31_000);
            verify(server, times(2)).broadcastMessage(eq(0), any());
            BotManager.cfg.AMBIENT_MEGAPHONES_ENABLED = false;
            ticker.tick(41_000);
            verify(server, times(2)).broadcastMessage(eq(0), any());
        }
    }
}
