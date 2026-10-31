package server.bots;

import client.Character;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/** Rejoin from the left using the existing ropes and the center's high step. */
final class BotPinkBeanBodyReturn {
    private static final Map<Character,Route> ROUTES=Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Character,BotMobAttackObserver.Cast> WARNINGS=Collections.synchronizedMap(new WeakHashMap<>());
    private static final Point[] STOPS={new Point(-465,-456),new Point(-170,-456),
            new Point(0,-160),new Point(85,-133),new Point(200,-42)};
    static final class Route { int step; }
    private BotPinkBeanBodyReturn() {}

    static void observe(Character b,BotMobAttackObserver.Cast cast) {
        if(cast.mob().getId()==8820001&&cast.index()==1&&BotPinkBeanRun.inInstance(b))WARNINGS.put(b,cast);
    }
    static Point destination(Route route,Point feet,boolean moving) {
        // Someone already on the high center steps need not descend to start over.
        if(!moving&&route.step==0&&feet.y<=-130&&feet.y>=-200&&Math.abs(feet.x)<145)route.step=2;
        // A forced fall/knockback can invalidate the already completed climb.
        if(!moving&&feet.x< -125&&feet.y> -130&&route.step>0)route.step=0;
        if(!moving&&feet.distanceSq(STOPS[route.step])<=12*12)route.step++;
        return route.step==STOPS.length?null:new Point(STOPS[route.step]);
    }
    static boolean waitForImpact(Point feet,boolean moving,java.util.List<Rectangle> areas) {
        if(moving)return false;
        var player=new Rectangle(feet.x-12,feet.y-45,24,45);
        for(var area:areas)if(area.intersects(player))return false;
        return true;
    }
    static boolean tick(BotEntry e,Character b,boolean ai) {
        var body=b.getMap().getMonsterById(8820001);
        if(body==null||!body.isAlive()) { ROUTES.remove(b);WARNINGS.remove(b);return false; }
        if(!e.inAir&&!e.climbing&&b.getPosition().x>=150&&b.getPosition().y>=-130) {
            ROUTES.remove(b);return false;
        }
        var route=ROUTES.get(b);
        if(route==null) {
            // A member already fighting on the right has no reason to leave it.
            if(b.getPosition().x>=0)return false;
            route=new Route();ROUTES.put(b,route);
        }
        boolean moving=e.inAir||e.climbing;
        var goal=destination(route,b.getPosition(),moving);
        if(goal==null) { ROUTES.remove(b);return false; }
        var warning=WARNINGS.get(b);
        if(warning!=null) {
            if(warning.map()!=b.getMap()||System.currentTimeMillis()>=warning.due())WARNINGS.remove(b);
            else if(waitForImpact(b.getPosition(),moving,BotMobAttackObserver.areas(warning)))
                return BotOpqRun.hold(e,b,ai);
        }
        // Finish the selected authored arc; do not retarget midway through a jump or rope.
        if(moving&&e.moveTarget!=null)goal=new Point(e.moveTarget);
        // This proved upper route intentionally crosses ABOVE the body. The ordinary
        // floor-approach guard must not redirect it back down the left tower.
        BotZakumPqRun.walkTo(e,b,goal,ai);
        return true;
    }
}
