package server.bots;

import client.Character;
import net.opcodes.SendOpcode;
import net.packet.Packet;
import scripting.event.EventInstanceManager;

import java.awt.Point;
import java.awt.Rectangle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Five workers enumerate visible box placements; only normal NPC feedback judges them. */
public final class BotLpqPuzzle {
    private static final class State {
        int attempt;
        boolean introduced;
        boolean checking;
        long readySince;
        long nextTalk;
    }
    private static final Map<EventInstanceManager, State> RUNS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final List<int[]> CHOICES = choices();
    private BotLpqPuzzle() {}

    static boolean tick(BotEntry entry, Character bot, boolean ai) {
        var event = bot.getEventInstance();
        if (event.getProperty("8stageclear") != null) {
            BotTravelManager.tickTravel(entry, bot, 922010900, 2, ai, false);
            return true;
        }
        var leader = event.getPlayers().stream().filter(event::isEventLeader).findFirst().orElse(null);
        if (leader == null) return true;
        var state = RUNS.computeIfAbsent(event, ignored -> new State());
        synchronized (state) {
            var workers = event.getPlayers().stream().filter(b -> b != leader)
                    .sorted(Comparator.comparingInt(Character::getId)).toList();
            var areas = bot.getMap().getAreas();
            if (workers.size() != 5 || areas.size() != 9) return true;
            int[] candidate = CHOICES.get(state.attempt % CHOICES.size());
            if (bot != leader) {
                int slot = workers.indexOf(bot);
                Rectangle area = areas.get(candidate[slot]);
                Point target = new Point((int) area.getCenterX(), (int) area.getCenterY());
                Point ground = BotPhysicsEngine.findGroundPoint(bot.getMap(), new Point(target.x, area.y - 1));
                if (ground != null && area.contains(ground)) target = ground;
                BotZakumPqRun.walkTo(entry, bot, target, ai);
                return true;
            }
            var approach = BotTravelManager.tickApproachNpc(entry, bot, bot.getMapId(), 2040043, 2, ai, false, 40);
            if (approach != BotTravelManager.ApproachStatus.ARRIVED) return true;
            BotTravelManager.clearMoveTargetPin(entry);
            BotManager.getInstance().stepMovementCore(entry, bot.getPosition(), ai);
            long now = System.currentTimeMillis();
            if (now < state.nextTalk) return true;
            if (!state.introduced) {
                if (BotNpcInteraction.talk(bot, 2040043)) state.introduced = true;
                state.nextTalk = now + 1500;
                return true;
            }
            boolean ready = true;
            for (int n = 0; n < workers.size(); n++)
                if (workers.get(n).getMapId() != bot.getMapId() || !areas.get(candidate[n]).contains(workers.get(n).getPosition())) ready = false;
            if (!ready) { state.readySince = 0; return true; }
            if (state.readySince == 0) state.readySince = now;
            if (now - state.readySince < 1200) return true;
            state.checking = true;
            state.nextTalk = now + 1500;
            BotNpcInteraction.talk(bot, 2040043);
            state.checking = false;
            return true;
        }
    }

    public static void observeEffect(Character bot, Packet packet) {
        if (bot != null && bot.getMapId() == 922010800 && isWrongEffect(packet)) BotLpqHumanPuzzle.observeWrong(bot);
        if (bot == null || bot.getMapId() != 922010800 || !BotLpqRun.isLeader(bot) || !isWrongEffect(packet)) return;
        var state = RUNS.get(bot.getEventInstance());
        if (state == null) return;
        synchronized (state) {
            if (state.checking) { state.attempt++; state.readySince = 0; state.checking = false; }
        }
    }

    static boolean isWrongEffect(Packet packet) {
        byte[] bytes = packet.getBytes();
        if (bytes.length < 5) return false;
        var b = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        if (Short.toUnsignedInt(b.getShort()) != SendOpcode.FIELD_EFFECT.getValue() || b.get() != 3) return false;
        int length = Short.toUnsignedInt(b.getShort());
        return length <= b.remaining() && "quest/party/wrong_kor".equals(new String(bytes, 5, length, StandardCharsets.US_ASCII));
    }

    static List<int[]> choices() {
        List<int[]> result = new ArrayList<>();
        for (int a = 0; a < 5; a++) for (int b = a + 1; b < 6; b++) for (int c = b + 1; c < 7; c++)
            for (int d = c + 1; d < 8; d++) for (int e = d + 1; e < 9; e++) result.add(new int[]{a,b,c,d,e});
        return result;
    }
}
