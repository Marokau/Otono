package server.bots;

import client.Character;
import client.inventory.WeaponType;
import server.life.Monster;

import java.awt.Point;
import java.awt.Rectangle;

/** Legal firing ledges for the isolated Ratz triggers in stages seven and nine. */
final class BotLpqRanged {
    record Goal(Point point, boolean jump) {}
    private BotLpqRanged() {}

    static boolean approach(BotEntry entry, Character bot, boolean ai) {
        var weapon = BotAttackExecutionProvider.getEquippedWeaponType(bot);
        if (!BotCombatManager.isRangedAmmoWeapon(weapon)) return false;
        var graph = BotNavigationGraphProvider.peekGraph(bot.getMap(), entry.movementProfile);
        if (graph == null) return false;
        Goal best = null;
        double score = Double.MAX_VALUE;
        for (var mob : bot.getMap().getAllMonsters()) {
            boolean trigger = bot.getMapId() == 922010700 ? mob.getId() >= 9300169 && mob.getId() <= 9300171
                    : bot.getMapId() == 922010900 && mob.getId() == 9300006;
            if (!mob.isAlive() || !trigger) continue;
            var plan = BotCombatManager.planAttack(entry, bot, mob);
            if (!entry.climbing && BotCombatManager.isTargetInAttackRange(plan, bot, mob)
                    && BotCombatManager.canUseAttackPlanNow(entry, weapon, plan)) {
                // Keep the firing ledge: generic grind engagement hops can leave it.
                BotTravelManager.clearMoveTargetPin(entry);
                if (!entry.inAir) BotPhysicsEngine.idleOnGround(entry, bot);
                BotCombatManager.attackMonster(entry, bot, plan);
                if (entry.inAir) BotManager.getInstance().stepMovementCore(entry, bot.getPosition(), ai);
                return true;
            }
            Goal goal = firingGoal(graph, bot, mob, weapon == WeaponType.CLAW);
            if (goal == null) continue;
            double cost = bot.getPosition().distance(goal.point()) + (goal.jump() ? 1000 : 0);
            if (cost < score) { best = goal; score = cost; }
        }
        if (best == null) return false;
        if (best.jump() && !entry.inAir && !entry.climbing && bot.getPosition().distanceSq(best.point()) <= 8 * 8) {
            BotTravelManager.clearMoveTargetPin(entry);
            BotMovementManager.initiateJump(entry, bot, 0);
            entry.fixedAirArc = true;
        } else BotZakumPqRun.walkTo(entry, bot, best.point(), ai);
        return true;
    }

    static Goal firingGoal(BotNavigationGraph graph, Character bot, Monster target, boolean jumpAllowed) {
        Rectangle bounds = server.bots.combat.BotMobHitboxProvider.getInstance().getMobBounds(target);
        if (bounds == null) bounds = new Rectangle(target.getPosition().x, target.getPosition().y, 1, 1);
        int from = graph.findRegionId(bot.getMap(), bot.getPosition());
        Goal best = null;
        double score = Double.MAX_VALUE;
        for (var region : graph.regions) {
            if (region.isRopeRegion || !graph.canReach(from, region.id, 0)) continue;
            for (var segment : region.segments) {
                for (int x : new int[]{segment.minX + 5, segment.maxX - 5, (segment.minX + segment.maxX) / 2}) {
                    Point p = segment.pointAt(x);
                    Rectangle hit = BotCombatManager.clientProjectileHitBox(bot, target.getPosition().x < p.x, 1.0f);
                    if (hit == null) continue;
                    hit.translate(p.x - bot.getPosition().x, p.y - bot.getPosition().y);
                    boolean jump = false;
                    if (!hit.intersects(bounds)) {
                        if (!jumpAllowed) continue;
                        hit.translate(0, -(int) BotPhysicsEngine.calculateMaxJumpHeight(BotMovementProfile.fromCharacter(bot)));
                        if (!hit.intersects(bounds)) continue;
                        jump = true;
                    }
                    double cost = bot.getPosition().distance(p) + (jump ? 1000 : 0);
                    if (cost < score) { best = new Goal(p, jump); score = cost; }
                }
            }
        }
        return best;
    }
}
