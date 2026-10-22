package server.bots;

import client.Character;
import server.life.Monster;
import java.awt.Point;

/** Owner-audited arm lanes; range checks still apply after this permission check. */
final class BotZakumArmLanes {
    private BotZakumArmLanes() {}
    static int armForSurface(Point p) {
        if(p==null||Math.abs(p.x)<200)return 0;
        boolean right=p.x>0;
        if(p.y>=-240&&p.y<=-180)return right?8800010:8800009;
        if(right) {
            if(p.y>=-535&&p.y<=-510)return 8800004;
            if(p.y>=-395&&p.y<=-385)return 8800006;
            if(p.y>=-333&&p.y<=-309)return 8800008;
        } else {
            if(p.y>=-560&&p.y<=-550)return 8800003;
            if(p.y>=-492&&p.y<=-482)return 8800005;
            if(p.y>=-428&&p.y<=-404)return 8800007;
        }
        return 0;
    }
    static boolean canTarget(Character bot,Monster mob) {
        int id=mob.getId();
        if(id<8800003||id>8800010||!BotZakumBattle.inInstance(bot)
                ||BotPqDialogue.allowsGearOffer(bot,null))return true;
        // Melee crews use normal skill/weapon reach and may help on another reachable arm.
        var job=bot.getJob();
        if(job!=null&&(job.getId()/100==1||job==client.Job.BANDIT||job==client.Job.CHIEFBANDIT
                ||job==client.Job.SHADOWER||job==client.Job.BRAWLER||job==client.Job.MARAUDER||job==client.Job.BUCCANEER))return true;
        // Apply the agreed kill order to substitutions, secondary targets, cached plans and summons too.
        for(var other:bot.getMap().getAllMonsters())if(other.isAlive()&&!other.isFake()
                &&other.getId()>=8800003&&other.getId()<=8800010
                &&BotZakumCombat.priority(bot,other.getId())<BotZakumCombat.priority(bot,id))return false;
        var position=bot.getPosition();var map=bot.getMap();
        if(position==null||map==null||map.getFootholds()==null)return false;
        var foothold=map.getFootholds().findBelow(new Point(position.x,position.y-3));
        if(foothold==null||foothold.isWall())return false;
        double y=foothold.getY1()+foothold.slope()*(position.x-foothold.getX1());
        if(Math.abs(y-position.y)>6)return false; // No arm attacks between platforms/on ropes.
        return armForSurface(new Point(position.x,(int)Math.round(y)))==id;
    }
    static BotCombatManager.AttackPlan filter(Character bot,BotCombatManager.AttackPlan plan) {
        if(plan==null)return null;
        var targets=new java.util.ArrayList<Monster>();
        for(var mob:plan.targets)if(canTarget(bot,mob))targets.add(mob);
        if(targets.isEmpty())return null;
        if(targets.size()==plan.targets.size())return plan;
        return new BotCombatManager.AttackPlan(plan.skillId,plan.skillLevel,plan.numDamage,plan.hitBox,
                targets,plan.route,plan.display,plan.direction,plan.rangedDirection,plan.stance,plan.speed,
                plan.hitDelayMs,plan.cooldownMs,plan.damageWeaponType);
    }
}
