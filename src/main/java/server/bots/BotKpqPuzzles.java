package server.bots;

import client.BotClient;
import client.Character;
import scripting.event.EventInstanceManager;
import server.partyquest.KerningPqGeometry;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;

/** Enumerates visible placements; only Cloto's ordinary response judges a candidate. */
public final class BotKpqPuzzles {
    private static final Map<EventInstanceManager, Progress> RUNS = new WeakHashMap<>();
    private static final class Progress {
        int stage;
        int attempt;
        boolean introduced;
        long readySince;
        long nextTalk;
        int notifiedAttempt = -1;
    }

    private BotKpqPuzzles() {}

    static boolean tick(BotEntry entry, Character bot, Character leader, int stage, boolean runAiTick) {
        var event = bot.getEventInstance();
        synchronized (RUNS) {
            var progress = RUNS.computeIfAbsent(event, ignored -> new Progress());
            if (progress.stage != stage) {
                progress.stage = stage;
                progress.attempt = 0;
                progress.introduced = false;
                progress.readySince = 0;
                progress.notifiedAttempt = -1;
            }
            var areas = KerningPqGeometry.areas(stage);
            var workers = event.getPlayers().stream().filter(p -> p.getId() != leader.getId())
                    .sorted(Comparator.comparingInt(Character::getId)).toList();
            if (workers.size() != 3) return true;
            var choices = combinations(areas.length);
            int[] candidate = choices.get(progress.attempt % choices.size());
            if (bot.getId() != leader.getId()) {
                int slot = workers.indexOf(bot);
                Rectangle area = areas[candidate[slot]];
                Point target = new Point((int) area.getCenterX(), (int) area.getCenterY());
                if (stage != 2) {
                    Point ground = BotPhysicsEngine.findGroundPoint(bot.getMap(), new Point(target.x, area.y - 1));
                    if (ground != null && area.contains(ground)) target = ground;
                }
                BotZakumPqRun.walkTo(entry, bot, target, runAiTick);
                if (!(leader.getClient() instanceof BotClient) && ready(workers, bot.getMapId(), areas, candidate)
                        && progress.notifiedAttempt != progress.attempt) {
                    progress.notifiedAttempt = progress.attempt;
                    BotPqChat.queue(entry, "We are in position. Please ask Cloto to check.", true);
                }
                return true;
            }
            var approach = BotTravelManager.tickApproachNpc(entry, bot, bot.getMapId(), 9020001,
                    2, runAiTick, false, 300);
            if (approach != BotTravelManager.ApproachStatus.ARRIVED) return true;
            BotManager.getInstance().stepMovementCore(entry, bot.getPosition(), runAiTick);
            long now = System.currentTimeMillis();
            if (!progress.introduced) {
                if (now >= progress.nextTalk && BotNpcInteraction.talk(bot, 9020001)) {
                    progress.introduced = true;
                    progress.nextTalk = now + 1500;
                }
                return true;
            }
            boolean ready = ready(workers, bot.getMapId(), areas, candidate);
            if (!ready) { progress.readySince = 0; return true; }
            if (progress.readySince == 0) progress.readySince = now;
            if (now < progress.nextTalk || now - progress.readySince < 1200) return true;
            if (BotNpcInteraction.talk(bot, 9020001)) {
                var text = ((BotClient) bot.getClient()).npcMessage.text();
                if (text.contains("haven't found")) {
                    progress.attempt++;
                    progress.readySince = 0;
                    BotPqChat.queue(entry, "Trying another arrangement.", true);
                }
                progress.nextTalk = now + 1500;
            }
            return true;
        }
    }

    /** Ordinary failed NPC check feedback; never reads the hidden winning combination. */
    public static void humanCheck(Character leader, int stage) {
        if (leader.getClient() instanceof BotClient || stage < 2 || stage > 4) return;
        var event = leader.getEventInstance();
        if (event == null || !event.isEventLeader(leader)) return;
        synchronized (RUNS) {
            var progress = RUNS.get(event);
            if (progress == null || progress.stage != stage) return;
            var workers = event.getPlayers().stream().filter(p -> p.getId() != leader.getId())
                    .sorted(Comparator.comparingInt(Character::getId)).toList();
            var areas = KerningPqGeometry.areas(stage);
            var choices = combinations(areas.length);
            int[] candidate = choices.get(progress.attempt % choices.size());
            if (ready(workers, leader.getMapId(), areas, candidate)) {
                progress.attempt++;
                progress.readySince = 0;
            }
        }
    }

    private static boolean ready(List<Character> workers, int map, Rectangle[] areas, int[] candidate) {
        if (workers.size() != 3) return false;
        for (int i = 0; i < workers.size(); i++)
            if (workers.get(i).getMapId() != map || !areas[candidate[i]].contains(workers.get(i).getPosition())) return false;
        return true;
    }

    static List<int[]> combinations(int count) {
        List<int[]> result = new ArrayList<>();
        for (int a = 0; a < count - 2; a++)
            for (int b = a + 1; b < count - 1; b++)
                for (int c = b + 1; c < count; c++) result.add(new int[]{a, b, c});
        return result;
    }
}
