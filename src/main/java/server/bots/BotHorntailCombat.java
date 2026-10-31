package server.bots;
import client.Character;
import client.Job;
import client.inventory.WeaponType;
import server.life.Monster;
import server.bots.combat.BotMobHitboxProvider;
import server.bots.combat.BotDefenseDataProvider;
import java.awt.*;
import java.util.*;
import java.util.List;

/** Reachable, real weapon attacks against Horntail parts; no artificial boss damage. */
final class BotHorntailCombat {
    private static final Map<Character,Point> GOALS=Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Character,Long> RETRY=Collections.synchronizedMap(new WeakHashMap<>());
    private BotHorntailCombat() {}
    static boolean part(int id){return id>=8810000&&id<=8810009 || BotPinkBeanRun.boss(id);}
    static int priority(Character b,int id){
        if(!part(id))return b.getJob()==Job.BISHOP?0:5;
        return switch(id){case 8810009->1;case 8810008->2;case 8810007->3;case 8810002,8810003,8810004->4;case 8810005,8810006->6;default->6;};
    }
    static boolean safe(Character b,Point p,List<Monster> mobs){
        var body=new Rectangle(p.x-12,p.y-45,24,45);
        for(var m:mobs)if(part(m.getId())){
            var bounds=BotMobHitboxProvider.getInstance().getMobBounds(m);
            if(bounds!=null&&(BotHorntailRun.inInstance(b)||BotPinkBeanRun.inInstance(b))) {
                // Preliminary heads have a normal approach side. Do not climb behind
                // their artwork just because the rear platform is technically reachable.
                if(b.getMapId()==240060000||b.getMapId()==240060100) {
                    var entrance=b.getMap().getPortal("sp");
                    if(entrance!=null&&(entrance.getPosition().x<bounds.getCenterX()
                            ? p.x>bounds.x-24 : p.x<bounds.x+bounds.width+24))return false;
                }
                var weapon=BotAttackExecutionProvider.getEquippedWeaponType(b);
                boolean ranged=weapon==WeaponType.CLAW||weapon==WeaponType.BOW||weapon==WeaponType.CROSSBOW
                        ||weapon==WeaponType.GUN||weapon==WeaponType.WAND||weapon==WeaponType.STAFF;
                var clearance=new Rectangle(bounds);clearance.grow(ranged?100:20,ranged?35:0);
                if(clearance.intersects(body))return false;
            }
            if(bounds!=null&&bounds.intersects(body)&&BotDefenseDataProvider.getInstance().expectedPhysicalTouchDamageOnHit(b,m.getPADamage(),m.getLevel())>=b.getCurrentMaxHp()*0.65)return false;
        }
        return true;
    }
    static boolean tick(BotEntry e,Character b,boolean ai){
        e.grinding=false;
        var mobs=new ArrayList<Monster>();for(var m:b.getMap().getAllMonsters())if(m.isAlive()&&!m.isFake()&&BotHorntailTargets.allowed(b,m))mobs.add(m);
        var hazards=new ArrayList<>(mobs);
        if(BotPinkBeanRun.inInstance(b))for(var m:b.getMap().getAllMonsters())if(m.isAlive()&&BotPinkBeanRun.boss(m.getId())&&!hazards.contains(m))hazards.add(m);
        if(b.getJob()==Job.BISHOP){
            boolean adds=false;for(var mob:mobs)if(!part(mob.getId())){adds=true;break;}
            if(adds)for(var it=mobs.iterator();it.hasNext();)if(part(it.next().getId()))it.remove();
        }
        mobs.sort(Comparator.comparingInt(m->priority(b,m.getId())));
        mobs=new ArrayList<>(BotPinkBeanStrategy.targets(b,BotHorntailTactics.targets(b,mobs)));
        // Support runs before combat. Once its approach has launched or grabbed
        // a rope, finish that active movement instead of reviving an older combat
        // stance as soon as the ally happens to enter the Heal box mid-route.
        if(BotHorntailRun.inInstance(b)&&(e.inAir||e.climbing)&&e.moveTarget!=null){
            BotPinkBeanMovement.walkTo(e,b,new Point(e.moveTarget),ai);return true;
        }
        if(e.inAir&&!e.climbing){BotPinkBeanMovement.walkTo(e,b,GOALS.getOrDefault(b,b.getPosition()),ai);return true;}
        if(e.climbing){
            long now=System.currentTimeMillis();Point goal=GOALS.get(b);
            if(goal==null||goal.equals(b.getPosition())||now>=RETRY.getOrDefault(b,0L)){
                goal=stance(e,b,mobs);RETRY.put(b,now+1500);
                if(goal==null)GOALS.remove(b);else GOALS.put(b,goal);
            }
            if(goal!=null)BotPinkBeanMovement.walkTo(e,b,goal,ai);
            else {BotPhysicsEngine.holdClimb(e,b);BotMovementManager.broadcastMovement(e);}
            return true;
        }
        boolean hereSafe=safe(b,b.getPosition(),hazards);
        if(hereSafe)for(var mob:mobs){
            if(!BotPinkBeanStances.allows(b,b.getPosition(),mob)||!BotPinkBeanFormation.allows(b,b.getPosition())||!BotZakumCombat.canDamage(b,mob))continue;
            var plan=BotCombatManager.planAttack(e,b,mob);var weapon=BotAttackExecutionProvider.getEquippedWeaponType(b);
            if(plan!=null&&BotCombatManager.isTargetInAttackRange(plan,b,mob)&&BotCombatManager.canUseAttackPlanNow(e,weapon,plan)){
                Point better=BotCombatManager.betterReachRepositionTarget(e,b,mob,plan);
                if(better!=null&&safe(b,better,hazards)&&BotPinkBeanStances.allows(b,better,mob)&&BotPinkBeanFormation.allows(b,better)&&BotHorntailHealingAnchor.covers(b,better)){
                    var floor=b.getMap().getFootholds().findBelow(new Point(better.x,better.y-3));
                    if(floor!=null&&!floor.isWall()&&Math.abs(floor.getY1()+floor.slope()*(better.x-floor.getX1())-better.y)<=6){
                        BotPinkBeanMovement.walkTo(e,b,better,ai);return true;
                    }
                }
                e.grinding=true;e.grindTarget=mob;BotTravelManager.clearMoveTargetPin(e);BotCombatManager.attackMonster(e,b,plan);return BotOpqRun.hold(e,b,ai);
            }
        }
        if(e.attackCooldownMs>0&&hereSafe)return BotOpqRun.hold(e,b,ai);
        long now=System.currentTimeMillis();Point goal=GOALS.get(b);
        if(goal==null||!BotPinkBeanFormation.allows(b,goal)||!BotHorntailHealingAnchor.covers(b,goal)||now>=RETRY.getOrDefault(b,0L)){
            goal=stance(e,b,mobs);RETRY.put(b,now+1500);if(goal==null)GOALS.remove(b);else GOALS.put(b,goal);
        }
        if(goal!=null){BotPinkBeanMovement.walkTo(e,b,goal,ai);return true;}
        return BotOpqRun.hold(e,b,ai);
    }
    static Point stance(BotEntry e,Character b,List<Monster> mobs){
        var graph=BotNavigationGraphProvider.peekGraph(b.getMap(),e.movementProfile);if(graph==null)return null;
        int from=graph.findRegionId(b.getMap(),b.getPosition());int mask=BotNavigationManager.botCanUseMovementSkill(b)?BotNavigationManager.botSkillMask(b):0;
        WeaponType type=BotAttackExecutionProvider.getEquippedWeaponType(b);
        boolean ranged=type==WeaponType.CLAW||type==WeaponType.BOW||type==WeaponType.CROSSBOW||type==WeaponType.GUN||type==WeaponType.WAND||type==WeaponType.STAFF;
        int reach=ranged?350:140;
        // Pink's party anchor can choose a valid outer firing platform beyond the
        // old coarse estimate. Search with learned projectile reach; execution
        // still validates the actual skill hitbox. Horntail's policy stays intact.
        if(ranged&&BotPinkBeanRun.inInstance(b))reach=Math.max(reach,BotCombatManager.clientProjectileHitBox(b,false,1f).width);
        for(var mob:mobs){
            if(!BotZakumCombat.canDamage(b,mob))continue;
            var bounds=BotMobHitboxProvider.getInstance().getMobBounds(mob);if(bounds==null)continue;
            Point best=null;double cost=Double.MAX_VALUE;
            for(var region:graph.regions){if(region.isRopeRegion||!graph.canReach(from,region.id,mask))continue;
                for(var segment:region.segments)for(int x=segment.minX+8;x<segment.maxX-8+35;x+=35){
                    Point p=segment.pointAt(Math.min(x,segment.maxX-8));if(!safe(b,p,b.getMap().getAllMonsters())||!BotPinkBeanStances.allows(b,p,mob)||!BotPinkBeanFormation.allows(b,p)||!BotHorntailHealingAnchor.covers(b,p))continue;
                    // Raised sages sit just above the old coarse floor envelope.
                    // Search valid melee ground candidates; native attack hitboxes
                    // still decide whether any eventual attack can land.
                    int up=BotPinkBeanRun.inInstance(b)&&!ranged?90:55;
                    if(!new Rectangle(p.x-reach,p.y-up,reach*2,up+35).intersects(bounds)
                            ||!BotHorntailFiringPosition.allows(b,p,mob))continue;
                    double score=p.distance(b.getPosition())+(ranged?Math.abs(Math.abs(p.x-bounds.getCenterX())-reach*.6)*.3:0);
                    if(score<cost){cost=score;best=p;}
                }
            }
            if(best!=null)return best;
        }
        return null;
    }
}
