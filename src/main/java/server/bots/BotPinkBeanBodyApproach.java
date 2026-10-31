package server.bots;

import java.awt.Point;
import java.awt.Rectangle;

/** Never cross the living body's floor just to reach a party/combat destination. */
final class BotPinkBeanBodyApproach {
    private BotPinkBeanBodyApproach() {}
    static Point waypoint(Point feet,Point goal,Rectangle body) {
        boolean left=feet.x<body.getCenterX();
        boolean crosses=left?goal.x>=body.x-12:goal.x<=body.getMaxX()+12;
        if(crosses)goal=new Point(left?body.x-112:body.x+body.width+112,-42);
        // A safe floor destination can otherwise select an inward tower drop.
        // Use the existing outer platform edge/rope route before approaching.
        if(feet.y< -200&&goal.y> -200) {
            if(Math.abs(feet.x)<540)return new Point(feet.x<0?-550:550,feet.y);
            return new Point(feet.x<0?-610:610,-42);
        }
        // Leave the small central platforms outward before descending to the floor.
        if(feet.y<=-130&&feet.y>=-200&&goal.y> -130&&Math.abs(feet.x)<145) {
            if(Math.abs(feet.x)<70)return new Point(left?-85:85,-133);
            return new Point(left?Math.min(body.x-112,-180):(int)Math.max(body.getMaxX()+112,180),-42);
        }
        return goal;
    }
}
