package server.bots;

import java.awt.Point;
import server.maps.Rope;

/** Swept contact with the existing rope hitbox, so a 50ms step cannot skip it. */
final class BotRopeGrabSweep {
    private BotRopeGrabSweep() {}
    static Point contact(Point from, Point to, Rope rope, double before) {
        double low = 0, high = Math.min(1, before);
        int[] starts = {from.x, from.y};
        int[] deltas = {to.x - from.x, to.y - from.y};
        int[] mins = {rope.x() - BotPhysicsEngine.cfg.ROPE_GRAB_X, BotPhysicsEngine.firstClimbableY(rope)};
        int[] maxs = {rope.x() + BotPhysicsEngine.cfg.ROPE_GRAB_X, rope.bottomY()};
        for (int axis = 0; axis < 2; axis++) {
            if (deltas[axis] == 0) {
                if (starts[axis] < mins[axis] || starts[axis] > maxs[axis]) return null;
            } else {
                double a = (mins[axis] - starts[axis]) / (double) deltas[axis];
                double b = (maxs[axis] - starts[axis]) / (double) deltas[axis];
                low = Math.max(low, Math.min(a, b));
                high = Math.min(high, Math.max(a, b));
            }
        }
        if (low > high || low < 0 || low >= before) return null;
        return new Point(Math.clamp((int)Math.round(from.x + deltas[0] * low), mins[0], maxs[0]),
                Math.clamp((int)Math.round(from.y + deltas[1] * low), mins[1], maxs[1]));
    }
}
