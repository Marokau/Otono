package server.bots;

import client.Character;
import client.inventory.WeaponType;
import server.life.Monster;

import java.awt.Point;
import java.util.Comparator;

/** Ordinary combat in the class chambers, including learned Teleport routes. */
final class BotCwkpqChamberCombat {
    private BotCwkpqChamberCombat() {}

    static boolean tick(BotEntry entry, Character bot, boolean ai) {
        if ((entry.inAir || entry.climbing) && entry.moveTarget != null) {
            BotZakumPqRun.walkTo(entry, bot, entry.moveTarget, ai);
            return true;
        }
        Monster target = entry.grindTarget;
        if (target == null || target.getMap() != bot.getMap() || !target.isAlive()
                || !BotZakumCombat.canDamage(bot, target)) {
            target = BotCombatManager.findGrindTarget(entry, bot);
            if (target == null) {
                var map = bot.getMap();
                var graph = BotNavigationGraphProvider.peekGraph(map, entry.movementProfile);
                if (graph != null) {
                    int from = graph.findRegionId(map, bot.getPosition());
                    int mask = BotNavigationManager.botSkillMask(bot);
                    target = map.getAllMonsters().stream()
                            .filter(m -> m.isAlive() && !m.isFake() && BotZakumCombat.canDamage(bot, m))
                            .filter(m -> graph.canReach(from, graph.findRegionId(map, m.getPosition()), mask))
                            .min(Comparator.comparingDouble(m -> m.getPosition().distanceSq(bot.getPosition())))
                            .orElse(null);
                }
            }
            entry.grindTarget = target;
        }
        if (target == null) return BotOpqRun.hold(entry, bot, ai);
        var weapon = BotAttackExecutionProvider.getEquippedWeaponType(bot);
        var plan = BotCombatManager.planAttackCadenced(entry, bot, target, ai);
        if (plan != null && BotCombatManager.isTargetInAttackRange(plan, bot, target)
                && BotCombatManager.canUseAttackPlanNow(entry, weapon, plan)) {
            if (entry.attackCooldownMs <= 0) BotCombatManager.attackMonster(entry, bot, plan);
            return BotOpqRun.hold(entry, bot, ai);
        }
        Point goal = new Point(target.getPosition());
        boolean ranged = weapon == WeaponType.BOW || weapon == WeaponType.CROSSBOW
                || weapon == WeaponType.CLAW || weapon == WeaponType.WAND
                || weapon == WeaponType.STAFF || weapon == WeaponType.GUN;
        int spacing = ranged ? 130 : 45;
        var surface = bot.getMap().getFootholds().findBelow(new Point(goal.x, goal.y - 2));
        if (surface != null && !surface.isWall()) {
            int min = Math.min(surface.getX1(), surface.getX2());
            int max = Math.max(surface.getX1(), surface.getX2());
            int dx = bot.getPosition().x <= goal.x ? -spacing : spacing;
            goal.x = Math.clamp(goal.x + dx, min, max);
            goal.y = (int) Math.round(surface.getY1() + surface.slope() * (goal.x - surface.getX1()));
        }
        BotZakumPqRun.walkTo(entry, bot, goal, ai);
        return true;
    }
}
