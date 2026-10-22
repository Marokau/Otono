package server.bots;

import client.Character;
import scripting.event.EventInstanceManager;
import java.util.*;

/** Human-led stage-one guidance, shared by the crew and reset for each instance. */
final class BotLpqDialogue {
    private static final Map<EventInstanceManager, State> RUNS = new WeakHashMap<>();
    private static final class State {
        long start;
        final Set<String> said = new HashSet<>();
    }
    private BotLpqDialogue() {}
    static synchronized boolean tick(BotEntry entry, Character bot, Character human, long now) {
        if (bot.getMapId() != BotLpqRun.FIRST_MAP || human.getMapId() != bot.getMapId()) return false;
        var event = bot.getEventInstance();
        if (event.getProperty("1stageclear") != null) return false;
        var s = RUNS.computeIfAbsent(event, ignored -> new State());
        if (BotLpqHuman.isCollector(bot)) {
            if (s.said.add("intro")) {
                s.start = now;
                BotPqChat.queue(entry, "Stage One: Defeat the Ratz and collect 25 passes. We will fight and bring the passes to you. Pick them up, then ask the Red Balloon to check.", true);
            }
            int held = BotLpqHuman.carriedWithLeader(bot, BotLpqRun.PASS);
            if (human.getItemQuantity(BotLpqRun.PASS, false) >= 25) {
                if (s.said.add("ready")) human.dropMessage(5, "LPQ: You have all 25 passes! Talk to the Red Balloon, then use the opened portal. We will follow you.");
            } else if (held >= 21 && held < 25) {
                if (s.said.add("nearly")) BotPqChat.queue(entry, "Almost there! We need " + (25-held) + " more passes.", true);
            } else if (held >= 13 && held < 21) {
                if (s.said.add("half")) BotPqChat.queue(entry, "Over halfway! We have " + held + " of the 25 passes.", true);
            }
        }
        return !s.said.contains("intro") || now - s.start < 7000;
    }
    static void delivered(BotEntry entry) {
        BotPqChat.queue(entry, "Here are our passes at your feet! Pick them up and bring all 25 to the Red Balloon.", true);
    }
}
