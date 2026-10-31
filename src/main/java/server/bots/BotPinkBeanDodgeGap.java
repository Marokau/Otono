package server.bots;
import client.Character;import java.awt.Point;import java.awt.Rectangle;import java.util.List;
/** Search alternative visible gaps when the nearest gap is blocked by the boss. */
final class BotPinkBeanDodgeGap {
 private BotPinkBeanDodgeGap() {}
 static Point find(Character bot,Point feet,List<Rectangle> columns,BotNavigationGraph.Region region) {
        var body = new Rectangle(feet.x - 12, feet.y - 50, 24, 50);
        boolean threatened = false;
        for (var column : columns) if (column.intersects(body)) threatened = true;
        if (!threatened) return null;
        // Stay on the same continuous, level foothold segment. No launch prediction,
        // dropping through platforms, teleport, or movement outside the map.
        for (int distance = 15; distance <= 180; distance += 15) {
            for (int direction : new int[]{-1, 1}) {
                Point candidate = new Point(feet.x + distance * direction, feet.y);
                boolean grounded = false;
                for (var segment : region.segments) {
                    if (Math.min(feet.x, candidate.x) >= segment.minX + 12
                            && Math.max(feet.x, candidate.x) <= segment.maxX - 12
                            && Math.abs(segment.pointAt(candidate.x).y - feet.y) <= 3) grounded = true;
                }
                if (!grounded) continue;
                var moved = new Rectangle(candidate.x - 20, candidate.y - 50, 40, 50);
                boolean clear = true;
                for (var column : columns) if (column.intersects(moved)) clear = false;
                if (!clear) continue;
                if(bot!=null) {
                    if(!BotHorntailCombat.safe(bot,candidate,bot.getMap().getAllMonsters()))continue;
                    var mob=bot.getMap().getMonsterById(8820001);
                    if(mob!=null) {
                        var contact=server.bots.combat.BotMobHitboxProvider.getInstance().getMobBounds(mob);
                        var corridor=new Rectangle(Math.min(feet.x,candidate.x)-12,feet.y-50,Math.abs(candidate.x-feet.x)+24,50);
                        if(contact!=null&&contact.intersects(corridor))continue;
                    }
                }
                return candidate;
            }
        }
        return null;
    }
}
