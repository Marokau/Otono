package server.bots;

import client.Character;
import java.awt.Point;

/** Select a reachable attack ledge for floating reactors, rather than aiming at empty air. */
final class BotOpqReactorApproach {
    private BotOpqReactorApproach() {}
    static Point stance(BotNavigationGraph graph, Character bot, Point target) {
        if (graph == null) return null;
        int from = graph.findRegionId(bot.getMap(), bot.getPosition());
        float jump = BotPhysicsEngine.calculateMaxJumpHeight(BotMovementProfile.fromCharacter(bot));
        Point best = null;
        double cost = Double.MAX_VALUE;
        for (var region : graph.regions) {
            if (region.isRopeRegion || !graph.canReach(from, region.id, 0)) continue;
            for (var segment : region.segments) {
                if (segment.maxX - segment.minX < 6) continue;
                int x = Math.clamp(target.x, segment.minX + 3, segment.maxX - 3);
                Point p = segment.pointAt(x);
                int dy = p.y - target.y;
                if (Math.abs(p.x - target.x) > 65 || dy > jump + 45) continue;
                if (dy < -45 && !BotPhysicsEngine.canStartDownJump(bot.getMap(), p)) continue;
                double d = bot.getPosition().distance(p) + (Math.abs(dy) > 45 ? 1000 + Math.abs(dy) : 0);
                if (d < cost) { best = p; cost = d; }
            }
        }
        return best;
    }
}
