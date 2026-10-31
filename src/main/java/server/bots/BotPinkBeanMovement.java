package server.bots;
import client.Character;
/** Normal floor approach before climbing beside a raised statue. */
final class BotPinkBeanMovement {
    private BotPinkBeanMovement() {}
    static void walkTo(BotEntry e,Character b,java.awt.Point goal,boolean ai){
        goal=BotHorntailMovement.approach(b,goal);
        // Choose the outer half of the firing platform before asking native navigation
        // for an approach; an inner vertical/diagonal entry can cross the bird.
        if(BotPinkBeanRun.inInstance(b)&&goal.y>=-110&&goal.y<=-90){
            if(goal.x< -384&&(b.getEventInstance().getIntProperty("stage")>=3||b.getMap().getMonsterById(8820005)!=null))
                goal=new java.awt.Point(Math.min(goal.x,-530),goal.y);
            else if(goal.x>389&&(b.getEventInstance().getIntProperty("stage")>=4||b.getMap().getMonsterById(8820006)!=null))
                goal=new java.awt.Point(Math.max(goal.x,540),goal.y);
        }
        // The inner edge of each lower platform meets a bird's body. Support trips
        // to the central floor must descend outward before crossing underneath.
        if(BotPinkBeanRun.inInstance(b)&&!e.inAir&&!e.climbing
                &&b.getPosition().y>=-110&&b.getPosition().y<=-90&&goal.y>=-133){
            var p=b.getPosition();
            if(p.x< -397&&goal.x> -397&&b.getMap().getMonsterById(8820005)!=null)
                goal=new java.awt.Point(-600,-42);
            else if(p.x>405&&goal.x<405&&b.getMap().getMonsterById(8820006)!=null)
                goal=new java.awt.Point(610,-42);
        }
        // Ariel occupies the gap between the upper platforms. Cross on the
        // bottom floor, then take the destination side's existing rope route.
        if(BotPinkBeanRun.inInstance(b)&&b.getMap().getMonsterById(8820002)!=null
                &&!e.inAir&&!e.climbing){
            var p=b.getPosition();int side=goal.x<0?-1:1;
            if(p.y< -300&&(p.x*side<0||Math.abs(goal.x)<169))
                goal=new java.awt.Point(p.x<0?-600:610,-42);
            else if(goal.y< -300&&p.y>=-300&&p.x*side<389)
                goal=new java.awt.Point(side<0?-600:610,-42);
        }
        // An outer-floor goal must not turn a top-platform retreat into a fall
        // through a living sage. Descend in the gap at the existing rope first;
        // cross underneath only after reaching the bottom floor.
        if(BotPinkBeanRun.inInstance(b)&&!e.inAir&&!e.climbing
                &&b.getPosition().y< -133&&goal.y>=-133){
            var p=b.getPosition();
            if(b.getMap().getMonsterById(p.x<0?8820003:8820004)!=null)
                goal=new java.awt.Point(p.x<0?-508:508,-42);
        }
        // Descend outside the bird before seeking a lower firing platform.
        // A direct diagonal drop from the upper inner platform crosses its body.
        if(BotPinkBeanRun.inInstance(b)&&!e.inAir&&!e.climbing
                &&b.getPosition().y< -300&&goal.y> -300){
            var p=b.getPosition();
            if(p.x<0&&p.x> -530&&b.getMap().getMonsterById(8820005)!=null)
                goal=new java.awt.Point(-550,p.y);
            else if(p.x>0&&p.x<540&&b.getMap().getMonsterById(8820006)!=null)
                goal=new java.awt.Point(550,p.y);
        }
        // Walk underneath a raised statue before jumping onto its adjacent platform.
        // A shortest jump from the side can cross the statue's lethal body.
        if(BotPinkBeanRun.inInstance(b)&&!e.inAir&&!e.climbing&&b.getPosition().y== -42
                &&goal.y>=-110&&goal.y<=-90&&Math.abs(b.getPosition().x-goal.x)>25)
            goal=new java.awt.Point(goal.x,-42);
        // Reach the rope base on foot before climbing to a sage firing platform.
        // Jumping diagonally from the outer floor cuts through the raised sage.
        if(BotPinkBeanRun.inInstance(b)&&!e.inAir&&!e.climbing
                &&b.getPosition().y== -42&&goal.y< -133
                &&(b.getMap().getMonsterById(8820003)!=null||b.getMap().getMonsterById(8820004)!=null)){
            int ropeX=goal.x<0?-508:508;
            if(Math.abs(b.getPosition().x-ropeX)>25)goal=new java.awt.Point(ropeX,-42);
        }
        if(BotPinkBeanRun.inInstance(b)) {
            var body=b.getMap().getMonsterById(8820001);
            if(body!=null&&body.isAlive()) {
                var bounds=server.bots.combat.BotMobHitboxProvider.getInstance().getMobBounds(body);
                if(bounds!=null)goal=(e.inAir||e.climbing)&&e.moveTarget!=null
                        ?new java.awt.Point(e.moveTarget)
                        :BotPinkBeanBodyApproach.waypoint(b.getPosition(),goal,bounds);
            }
        }
        BotZakumPqRun.walkTo(e,b,goal,ai);
    }
}
