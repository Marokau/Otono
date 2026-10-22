package server.bots;

import client.Character;
import client.inventory.WeaponType;
import server.bots.combat.BotMobHitboxProvider;
import server.life.Monster;
import client.status.MonsterStatus;
import java.awt.*;
import java.util.*;
import java.util.List;

/** Platform stances for separated boss hitboxes; attack planning/execution stays shared. */
final class BotZakumCombat {
    private static final Map<Character,Point> GOALS=Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Character,Long> RETRY=Collections.synchronizedMap(new WeakHashMap<>());
    private BotZakumCombat() {}
    static boolean tick(BotEntry entry,Character bot,boolean ai) {
        entry.grinding=false;
        var equipped=BotAttackExecutionProvider.getEquippedWeaponType(bot);
        boolean ranged=equipped==WeaponType.BOW||equipped==WeaponType.CROSSBOW||equipped==WeaponType.CLAW
                ||equipped==WeaponType.GUN||equipped==WeaponType.WAND||equipped==WeaponType.STAFF;
        if(entry.inAir&&!entry.climbing){BotManager.getInstance().stepMovementCore(entry,GOALS.getOrDefault(bot,bot.getPosition()),ai);return true;}
        for(var mob:bot.getMap().getAllMonsters())if(mob.getId()>=8800000&&mob.getId()<=8800002){
            var bounds=BotMobHitboxProvider.getInstance().getMobBounds(mob);
            // Keep the fragile support caster away from body contact before it happens.
            int clearance=ranged?100:20;
            if(bounds!=null&&bounds.intersects(new Rectangle(bot.getPosition().x-clearance,bot.getPosition().y-45,clearance*2,45))){
                int retreat=ranged?175:60;
                int x=bot.getPosition().x<bounds.getCenterX()?bounds.x-retreat:bounds.x+bounds.width+retreat;
                var safe=new Point(x,bot.getPosition().y);GOALS.put(bot,safe);BotZakumPqRun.walkTo(entry,bot,safe,ai);return true;
            }
        }
        var mobs=new ArrayList<Monster>();
        for(var mob:bot.getMap().getAllMonsters())if(mob.isAlive()&&!mob.isFake()&&canDamage(bot,mob))mobs.add(mob);
        // Commit to the first living arm, rather than firing at a later arm just because it is closer.
        int firstArm=0,firstRank=Integer.MAX_VALUE;
        for(var mob:bot.getMap().getAllMonsters())if(mob.isAlive()&&!mob.isFake()&&mob.getId()>=8800003&&mob.getId()<=8800010) {
            int rank=priority(bot,mob.getId());if(rank<firstRank){firstRank=rank;firstArm=mob.getId();}
        }
        if(ranged&&firstArm!=0)for(var iterator=mobs.iterator();iterator.hasNext();) {
            int id=iterator.next().getId();if(id>=8800003&&id<=8800010&&id!=firstArm)iterator.remove();
        }
        mobs.sort(Comparator.comparingInt((Monster m)->priority(bot,m.getId())).thenComparingInt(Monster::getObjectId));
        // The Bishop clears summons even when reaching them requires leaving an in-range body.
        if(bot.getJob()==client.Job.BISHOP&&mobs.stream().anyMatch(m->!isZakum(m.getId())))
            mobs.removeIf(m->isZakum(m.getId()));
        if(entry.climbing) {
            // A rope is transit, not a stance: never default the navigation goal to this rope position.
            long now=System.currentTimeMillis();Point climbGoal=GOALS.get(bot);
            if(climbGoal==null||climbGoal.equals(bot.getPosition())||now>=RETRY.getOrDefault(bot,0L)) {
                climbGoal=stance(entry,bot,mobs);
                if(climbGoal==null)climbGoal=entry.owner!=null&&entry.owner.getMap()==bot.getMap()
                        &&!entry.owner.getPosition().equals(bot.getPosition())?entry.owner.getPosition():BotZakumSupport.STAGING;
                GOALS.put(bot,new Point(climbGoal));RETRY.put(bot,now+2000);
            }
            BotZakumPqRun.walkTo(entry,bot,climbGoal,ai);return true;
        }
        if(entry.attackCooldownMs<=0)for(var mob:mobs){
            if(!BotZakumArmLanes.canTarget(bot,mob))continue;
            // Fight arms from their exterior side, even if a long projectile box can cross Zakum.
            if(ranged&&mob.getId()>=8800003&&mob.getId()<=8800010) {
                var arm=BotMobHitboxProvider.getInstance().getMobBounds(mob);
                if(arm==null||((mob.getId()&1)==0
                        ?bot.getPosition().x<arm.x+arm.width+60:bot.getPosition().x>arm.x-60))continue;
            }
            var plan=BotCombatManager.planAttack(entry,bot,mob);
            var weapon=BotAttackExecutionProvider.getEquippedWeaponType(bot);
            if(plan!=null&&BotCombatManager.isTargetInAttackRange(plan,bot,mob)&&BotCombatManager.canUseAttackPlanNow(entry,weapon,plan)){
                // Reuse the normal combat policy: a short step for a much stronger
                // skill beats indefinitely chipping with a weak long-range attack.
                Point better=BotCombatManager.betterReachRepositionTarget(entry,bot,mob,plan);
                if(better!=null){
                    // A damage upgrade is not a valid step if it points past the platform's edge.
                    var surface=bot.getMap().getFootholds().findBelow(new Point(better.x,better.y-3));
                    boolean safe=surface!=null&&!surface.isWall()
                            &&better.x>=Math.min(surface.getX1(),surface.getX2())&&better.x<=Math.max(surface.getX1(),surface.getX2())
                            &&Math.abs(surface.getY1()+surface.slope()*(better.x-surface.getX1())-better.y)<=6;
                    int clearance=bot.getJob()==client.Job.BISHOP?100:25;
                    var lane=new Rectangle(Math.min(better.x,bot.getPosition().x)-clearance,
                            Math.min(better.y,bot.getPosition().y)-50,
                            Math.abs(better.x-bot.getPosition().x)+clearance*2,
                            Math.abs(better.y-bot.getPosition().y)+50);
                    for(var body:bot.getMap().getAllMonsters())if(body.getId()>=8800000&&body.getId()<=8800002){
                        var bounds=BotMobHitboxProvider.getInstance().getMobBounds(body);
                        if(bounds!=null&&bounds.intersects(lane)){safe=false;break;}
                    }
                    if(ranged&&mob.getId()>=8800003&&mob.getId()<=8800010) {
                        var arm=BotMobHitboxProvider.getInstance().getMobBounds(mob);
                        if(arm==null||((mob.getId()&1)==0?better.x<arm.x+arm.width+60:better.x>arm.x-60))safe=false;
                    }
                    if(ranged&&mob.getId()>=8800003&&mob.getId()<=8800010&&BotZakumArmLanes.armForSurface(better)!=mob.getId())safe=false;
                    if(safe){BotZakumPqRun.walkTo(entry,bot,better,ai);return true;}
                }
                entry.grinding=true;entry.grindTarget=mob;BotTravelManager.clearMoveTargetPin(entry);
                BotCombatManager.attackMonster(entry,bot,plan);
                return BotOpqRun.hold(entry,bot,ai);
            }
        }
        if(entry.attackCooldownMs>0)return BotOpqRun.hold(entry,bot,ai);
        long now=System.currentTimeMillis();
        Point goal=GOALS.get(bot);
        if(goal==null||now>=RETRY.getOrDefault(bot,0L)){
            goal=stance(entry,bot,mobs);RETRY.put(bot,now+2000);
            if(goal==null)GOALS.remove(bot);else GOALS.put(bot,goal);
        }
        if(goal!=null){
            // Use the real upper-platform teleporter rather than walking across the body.
            boolean arms=false;
            for(var mob:mobs)if(mob.getId()>=8800003&&mob.getId()<=8800010){arms=true;break;}
            if(ranged&&arms&&Integer.signum(goal.x)!=Integer.signum(bot.getPosition().x)) {
                var portal=bot.getMap().getPortal(bot.getPosition().x<0?"h000":"h001");
                if(portal!=null){BotTravelManager.walkToPortalAndEnter(entry,bot,portal,now,ai);return true;}
            }
            BotZakumPqRun.walkTo(entry,bot,goal,ai);return true;
        }
        // No usable combat stance: settle with normal ground physics, not a stale jump route.
        BotTravelManager.clearMoveTargetPin(entry);BotMovementManager.clearNavigationState(entry);
        BotNavigationManager.clearCommittedRoute(entry);BotFidgetManager.clear(entry);
        entry.moveDir=0;entry.ropeEntryPending=false;entry.downJumpPending=false;
        BotMovementManager.tickGrounded(entry,null);return true;
    }
    static boolean canDamage(Character bot,Monster mob) {
        return !mob.isBuffed(BotEquipManager.isMageJob(bot.getJob())?MonsterStatus.MAGIC_IMMUNITY:MonsterStatus.WEAPON_IMMUNITY);
    }
    static boolean isZakum(int id) { return id>=8800000&&id<=8800010; }
    static int priority(Character bot,int id) {
        // Owner numbering: 1/2 top left/right, 3/4 next row, 5/6 next, 7/8 bottom.
        int[] order={8800007,8800009,8800006,8800008,8800010,8800005,8800003,8800004};
        for(int i=0;i<order.length;i++)if(id==order[i])return i;
        if(!isZakum(id))return bot.getJob()==client.Job.BISHOP?10:30;
        return 20;
    }
    private static Point stance(BotEntry entry,Character bot,List<Monster> mobs) {
        var graph=BotNavigationGraphProvider.peekGraph(bot.getMap(),entry.movementProfile);
        if(graph==null)return null;
        int from=graph.findRegionId(bot.getMap(),bot.getPosition());
        WeaponType type=BotAttackExecutionProvider.getEquippedWeaponType(bot);
        boolean ranged=type==WeaponType.BOW||type==WeaponType.CROSSBOW||type==WeaponType.CLAW||type==WeaponType.GUN||type==WeaponType.WAND||type==WeaponType.STAFF;
        int reach=ranged?350:140;
        Rectangle body=null;
        for(var mob:bot.getMap().getAllMonsters())if(mob.getId()>=8800000&&mob.getId()<=8800002)body=BotMobHitboxProvider.getInstance().getMobBounds(mob);
        for(var mob:mobs){
            Rectangle bounds=BotMobHitboxProvider.getInstance().getMobBounds(mob);if(bounds==null)continue;
            Point best=null;double cost=Double.MAX_VALUE;
            for(var region:graph.regions){
                if(region.isRopeRegion||!graph.canReach(from,region.id,0))continue;
                for(var segment:region.segments){
                    if(segment.maxX-segment.minX<12)continue;
                    for(int desired:new int[]{bounds.x-reach/2,bounds.x+bounds.width+reach/2,bot.getPosition().x}){
                        var p=segment.pointAt(Math.clamp(desired,segment.minX+6,segment.maxX-6));
                        int clearance=ranged?100:25;
                        if(ranged&&mob.getId()>=8800003&&mob.getId()<=8800010
                                &&((mob.getId()&1)==0?p.x<bounds.x+bounds.width+60:p.x>bounds.x-60))continue;
                        if(body!=null&&body.intersects(new Rectangle(p.x-clearance,p.y-50,clearance*2,50)))continue;
                        // A safe endpoint on the opposite side is not a safe walking route.
                        // Stay on this side while clearing summons instead of crossing the body.
                        if(body!=null&&bot.getJob()==client.Job.BISHOP
                                &&!(mob.getId()>=8800003&&mob.getId()<=8800010)
                                &&body.intersects(new Rectangle(Math.min(p.x,bot.getPosition().x)-25,
                                Math.min(p.y,bot.getPosition().y)-50,
                                Math.abs(p.x-bot.getPosition().x)+50,
                                Math.abs(p.y-bot.getPosition().y)+50)))continue;
                        var attackArea=new Rectangle(p.x-reach,p.y-50,reach*2,80);
                        if(ranged&&mob.getId()>=8800003&&mob.getId()<=8800010
                                &&BotZakumArmLanes.armForSurface(p)!=mob.getId())continue;
                        if(!attackArea.intersects(bounds))continue;
                        double score=p.distance(bot.getPosition());
                        if(score<cost){best=p;cost=score;}
                    }
                }
            }
            if(best!=null)return best;
        }
        return null;
    }
}
