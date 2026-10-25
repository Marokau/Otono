package server.bots;

import client.Character;
import client.Disease;
import java.awt.Point;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/** Choose the native pit reset after landing on the course's isolated central islands. */
final class BotMvpqCourseReset {
    private static final Set<BotEntry> FALLING = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private BotMvpqCourseReset() {}
    static boolean tick(BotEntry e, Character b, BotMvpqRun.Crew crew, Point goal, boolean ai) {
        if (b.getMapId()!=BotMvpqRun.COURSE) { FALLING.remove(e); return false; }
        if (e.climbing) { FALLING.remove(e); return false; }
        if (!e.inAir) {
            var graph=BotNavigationGraphProvider.peekGraph(b.getMap(),BotMovementProfile.fromCharacter(b));
            if (graph==null || goal==null) return false;
            int from=graph.findRegionId(b.getMap(),b.getPosition());
            int to=graph.findRegionId(b.getMap(),goal);
            int mask=BotNavigationManager.botCanUseMovementSkill(b)?BotNavigationManager.botSkillMask(b):0;
            if (from<0 || graph.canReach(from,to,mask)) { FALLING.remove(e); return false; }
            FALLING.add(e);
        }
        if (!FALLING.contains(e)) return false;
        if (b.hasDisease(Disease.STUN)) return BotOpqRun.hold(e,b,ai);
        BotTravelManager.clearMoveTargetPin(e);
        BotMovementManager.clearNavigationState(e);
        e.moveDir=1; // Ordinary right-arrow input until the actual pit portal catches the fall.
        if (e.inAir) BotMovementManager.tickAirborne(e,new Point(b.getPosition().x+1000,b.getPosition().y));
        else {
            BotPhysicsEngine.tickMotionTimers(e);
            var ground=BotPhysicsEngine.syncAndDetectGround(e,b);
            if (ground!=null) BotPhysicsEngine.applyGroundMotion(e,b,ground);
            BotMovementManager.broadcastMovement(e);
        }
        return true;
    }
}
