package server.bots;

import client.Character;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** React to Pink Bean's visible Stun/Seduce column warnings with ordinary walking. */
final class BotPinkBeanColumnWarning {
    private record Warning(BotMobAttackObserver.Cast cast, long reactAt) {}
    private static final Map<Character, Warning> WARNINGS = Collections.synchronizedMap(new WeakHashMap<>());
    private BotPinkBeanColumnWarning() {}

    static void observe(Character bot, BotMobAttackObserver.Cast cast) {
        if (cast.mob().getId() != 8820001 || cast.index() != 0 && cast.index() != 2 || cast.attack().type() != 3
                || !BotPinkBeanRun.inInstance(bot)) return;
        // The packet is the visible warning, not a prediction of a future cast.
        long began = cast.due() - cast.attack().delay();
        WARNINGS.put(bot, new Warning(cast, began + 250 + Math.floorMod(bot.getId(), 100)));
    }

    static Point gap(Point feet, List<Rectangle> columns, BotNavigationGraph.Region region) {
        return BotPinkBeanDodgeGap.find(null,feet,columns,region);
    }

    static boolean tick(BotEntry entry, Character bot, boolean ai) {
        var warning = WARNINGS.get(bot);
        if (warning == null) return false;
        var cast = warning.cast();
        long now = System.currentTimeMillis();
        if (cast.map() != bot.getMap() || !cast.mob().isAlive() || now >= cast.due()) {
            WARNINGS.remove(bot);
            return false;
        }
        if (now < warning.reactAt() || entry.inAir || entry.climbing) return false;
        var graph = BotNavigationGraphProvider.peekGraph(bot.getMap(), entry.movementProfile);
        if (graph == null) return false;
        var region = graph.regionsById.get(graph.findRegionId(bot.getMap(), bot.getPosition()));
        if (region == null || region.isRopeRegion) return false;
        var columns = BotMobAttackObserver.areas(cast);
        var current = new Rectangle(bot.getPosition().x - 12, bot.getPosition().y - 50, 24, 50);
        // Once outside the warning, keep the safe gap until impact. Returning to
        // a combat/party anchor early can walk straight back into the same cast.
        boolean threatened = false;
        for (var column : columns) if (column.intersects(current)) threatened = true;
        if (!threatened) {
            // A safe player can keep firing without walking back to formation.
            // Use normal attack plans, costs, cooldowns and actual reach here.
            BotCombatManager.AttackPlan shot = null;
            server.life.Monster aimed = null;
            int priority = Integer.MAX_VALUE;
            if (entry.attackCooldownMs <= 0 && BotHorntailCombat.safe(bot, bot.getPosition(), bot.getMap().getAllMonsters())) {
                for (var target : BotPinkBeanStrategy.targets(bot, bot.getMap().getAllMonsters())) {
                    if (!target.isAlive() || target.isFake() || !BotHorntailTargets.allowed(bot, target)) continue;
                    var plan = BotCombatManager.planAttack(entry, bot, target);
                    int rank = BotHorntailCombat.priority(bot, target.getId());
                    if (rank < priority && plan != null && BotCombatManager.isTargetInAttackRange(plan, bot, target)
                            && BotCombatManager.canUseAttackPlanNow(entry, BotAttackExecutionProvider.getEquippedWeaponType(bot), plan)) {
                        shot = plan; aimed = target; priority = rank;
                    }
                }
            }
            if (shot != null) {
                entry.grinding = true; entry.grindTarget = aimed;
                BotCombatManager.attackMonster(entry, bot, shot);
            }
            else BotHurricane.cancel(bot);
            return BotOpqRun.hold(entry, bot, ai);
        }
        Point destination = BotPinkBeanDodgeGap.find(bot,bot.getPosition(),columns,region);
        if (destination == null || !BotHorntailCombat.safe(bot, destination, bot.getMap().getAllMonsters())) return false;
        BotHurricane.cancel(bot);
        BotPinkBeanMovement.walkTo(entry, bot, destination, ai);
        return true;
    }
}
