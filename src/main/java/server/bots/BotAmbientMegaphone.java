package server.bots;

import client.BotClient;
import client.Character;
import com.esotericsoftware.yamlbeans.YamlReader;
import net.server.Server;
import net.server.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.TimerManager;
import tools.PacketCreator;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Occasional world-wide smega flavor from existing autonomous bots, without changing their AI. */
final class BotAmbientMegaphone {
    private static final Logger log = LoggerFactory.getLogger(BotAmbientMegaphone.class);
    private static final BotAmbientMegaphone instance = new BotAmbientMegaphone();
    static final String[] CATEGORIES = {"BirthdayMessages", "ItemSales", "GuildRecruitment", "PQRecruitment",
            "RWTSpam", "Flex", "RandomAnnouncements", "SocialMessages"};
    // SoloMapling SocialHotPotatoManager's category distribution.
    static final int[] WEIGHTS = {5, 7, 7, 7, 19, 19, 18, 18};
    // Verified in handbook/Cash.txt; the same five styles used by SoloMapling.
    static final int[] AVATAR_ITEMS = {5390000, 5390001, 5390002, 5390005, 5390006};
    private final Map<Integer, Long> nextAt = new HashMap<>();
    private final Map<Integer, Integer> lastSender = new HashMap<>();
    private final Map<Integer, String> lastLine = new HashMap<>();
    private List<List<String>> dialogue;

    static BotAmbientMegaphone getInstance() {
        return instance;
    }

    void tick() {
        tick(System.currentTimeMillis());
    }

    synchronized void tick(long now) {
        if (!BotManager.cfg.AMBIENT_MEGAPHONES_ENABLED) {
            nextAt.clear();
            return;
        }
        try {
            if (dialogue == null) {
                dialogue = loadDialogue();
            }
            List<BotEntry> entries = BotManager.getInstance().allEntries();
            for (World world : Server.getInstance().getWorlds()) {
                if (now < nextAt.computeIfAbsent(world.getId(), id -> now + delayMs())) {
                    continue;
                }
                nextAt.put(world.getId(), now + delayMs());
                boolean observed = world.getPlayerStorage().getAllCharacters().stream()
                        .anyMatch(chr -> chr.isLoggedinWorld() && !(chr.getClient() instanceof BotClient));
                if (!observed) {
                    continue;
                }
                List<Character> senders = entries.stream().filter(BotAmbientMegaphone::eligible)
                        .map(entry -> entry.bot).filter(bot -> bot.getWorld() == world.getId()).toList();
                if (senders.isEmpty()) {
                    continue;
                }
                int previous = lastSender.getOrDefault(world.getId(), -1);
                List<Character> fresh = senders.stream().filter(bot -> bot.getId() != previous).toList();
                if (!fresh.isEmpty()) {
                    senders = fresh;
                }
                Character sender = senders.get(ThreadLocalRandom.current().nextInt(senders.size()));
                List<String> lines = dialogue.get(categoryForRoll(ThreadLocalRandom.current().nextInt(100)));
                boolean avatar = ThreadLocalRandom.current().nextInt(100)
                        < Math.clamp(BotManager.cfg.AMBIENT_AVATAR_MEGAPHONE_PERCENT, 0, 100);
                if (avatar) {
                    // Four 13-character rows: choose a complete short line rather than cutting it off.
                    List<String> shortLines = lines.stream().filter(text -> text.length() <= 52).toList();
                    if (shortLines.isEmpty()) {
                        avatar = false;
                    } else {
                        lines = shortLines;
                    }
                }
                String line = lines.get(ThreadLocalRandom.current().nextInt(lines.size()));
                if (line.equals(lastLine.get(world.getId())) && lines.size() > 1) {
                    line = lines.get((lines.indexOf(line) + 1) % lines.size());
                }
                broadcast(sender, line, avatar,
                        AVATAR_ITEMS[ThreadLocalRandom.current().nextInt(AVATAR_ITEMS.length)]);
                lastSender.put(world.getId(), sender.getId());
                lastLine.put(world.getId(), line);
            }
        } catch (Exception e) {
            log.warn("Ambient bot megaphone tick failed", e);
        }
    }

    static void broadcast(Character sender, String line, boolean avatar, int style) {
        int world = sender.getWorld();
        int channel = sender.getClient().getChannel();
        // Reuse both player broadcast paths, including UseCashItemHandler's timed avatar removal.
        if (avatar) {
            Server.getInstance().broadcastMessage(world,
                    PacketCreator.getAvatarMega(sender, "", channel, style, avatarLines(line), true));
            TimerManager.getInstance().schedule(() -> Server.getInstance().broadcastMessage(world,
                    PacketCreator.byeAvatarMega()), 10_000L);
        } else {
            Server.getInstance().broadcastMessage(world,
                    PacketCreator.serverNotice(3, channel, sender.getName() + " : " + line, true));
        }
    }

    static List<String> avatarLines(String line) {
        if (line.length() > 52) {
            throw new IllegalArgumentException("Avatar dialogue exceeds four rows");
        }
        List<String> rows = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int start = Math.min(i * 13, line.length());
            rows.add(line.substring(start, Math.min(start + 13, line.length())));
        }
        return rows;
    }

    static boolean eligible(BotEntry entry) {
        Character bot = entry.bot;
        return entry.selfDrivingOwnership && !entry.loggingOut && bot != null
                && bot.getClient() instanceof BotClient && bot.isLoggedinWorld() && bot.isAlive()
                && bot.getEventInstance() == null;
    }

    private static long delayMs() {
        long min = Math.max(10_000L, BotManager.cfg.AMBIENT_MEGAPHONE_MIN_MS);
        long max = Math.max(min, BotManager.cfg.AMBIENT_MEGAPHONE_MAX_MS);
        return min == max ? min : ThreadLocalRandom.current().nextLong(min, max + 1);
    }

    static int categoryForRoll(int roll) {
        if (roll < 0 || roll >= 100) {
            throw new IllegalArgumentException("Megaphone roll must be in [0,99]");
        }
        int cumulative = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            cumulative += WEIGHTS[i];
            if (roll < cumulative) {
                return i;
            }
        }
        throw new IllegalArgumentException("Megaphone roll must be in [0,99]");
    }

    static List<List<String>> loadDialogue() throws Exception {
        var stream = BotAmbientMegaphone.class.getResourceAsStream("/bots/MegaphoneDialogue.yaml");
        if (stream == null) {
            throw new IllegalStateException("Missing megaphone dialogue resource");
        }
        try (var input = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            Map<?, ?> root = (Map<?, ?>) new YamlReader(input).read();
            Map<?, ?> broadcast = (Map<?, ?>) root.get("MegaphoneBroadcast");
            List<List<String>> result = new ArrayList<>();
            for (String category : CATEGORIES) {
                Map<?, ?> data = (Map<?, ?>) broadcast.get(category);
                List<String> lines = new ArrayList<>();
                for (Object text : (List<?>) data.get("text")) {
                    String line = ascii(text instanceof Map<?, ?> row ? (String) row.get("line") : (String) text);
                    if (!line.isBlank()) {
                        lines.add(line);
                    }
                }
                if (lines.isEmpty()) {
                    throw new IllegalStateException("Empty megaphone category: " + category);
                }
                result.add(List.copyOf(lines));
            }
            return List.copyOf(result);
        }
    }

    static String ascii(String text) {
        String plain = text.replace('\u2018', '\'').replace('\u2019', '\'')
                .replace('\u201c', '"').replace('\u201d', '"')
                .replace('\u2013', '-').replace('\u2014', '-').replace("\u2026", "...")
                .replace('\n', ' ').replace('\r', ' ');
        return plain.replaceAll("[^\\x20-\\x7E]", "");
    }
}
