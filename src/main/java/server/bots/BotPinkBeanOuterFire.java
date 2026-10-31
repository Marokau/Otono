package server.bots;
import client.Character;
import client.Job;
import server.life.*;
import server.bots.combat.BotMobHitboxProvider;
import java.awt.*;
import java.lang.invoke.*;
/** Stand beyond Munin's native effect box; attack only with ordinary legal skill hitboxes. */
final class BotPinkBeanOuterFire {
 private BotPinkBeanOuterFire() {}
 private static final MethodHandle NATIVE_BOUNDS;
 static {try {NATIVE_BOUNDS=MethodHandles.privateLookupIn(MobSkill.class,MethodHandles.lookup()).findVirtual(MobSkill.class,"calculateBoundingBox",MethodType.methodType(Rectangle.class,Point.class));}catch(ReflectiveOperationException ex){throw new ExceptionInInitializerError(ex);}}
 static Rectangle area(Monster bird){
  Rectangle all=null;
  for(var id:bird.getSkills())if(id.type()==MobSkillType.SEDUCE){
   var skill=MobSkillFactory.getMobSkillOrThrow(id.type(),id.level());
   try {var r=(Rectangle)NATIVE_BOUNDS.invokeExact(skill,bird.getPosition());all=all==null?r:all.union(r);}catch(Throwable ex){throw new IllegalStateException("Cannot read native Seduce bounds",ex);}
  }
  return all;
 }
 static boolean required(Character b){
  if(!BotPinkBeanRun.inInstance(b)||b.getMap()==null)return false;
  if(b.getMap().getMonsterById(8820006)!=null)return true;
  int wave=b.getEventInstance().getIntProperty("stage");
  if(wave!=3&&wave!=4)return false;
  Monster last=null;
  for(var m:b.getMap().getAllMonsters())if(m.isAlive()&&m.getId()>=8820002&&m.getId()<=8820006){if(last!=null)return false;last=m;}
  return last!=null&&last.getMaxHp()>0&&last.getHp()<=last.getMaxHp()/4;
 }
 static Point position(Character b){
  var bird=b.getMap().getMonsterById(8820006);var danger=bird==null?null:area(bird);
  if(bird!=null&&danger==null)throw new IllegalStateException("Munin has no native Seduce range");
  int wave=b.getEventInstance().getIntProperty("stage");
  // A returning member must not cross Munin's whole active effect box to
  // regain its party's side. Shelter on the current side until he is defeated.
  boolean right=bird==null?BotPinkBeanAssignments.rightParty(wave+1,BotPinkBeanRun.partyIndex(BotPinkBeanRun.crew(b),b)):b.getPosition().x>=danger.getCenterX();
  // Before the next wave, use the same connected outer-floor rally points.
  int x=danger==null?(right?840:-830):(right?danger.x+danger.width+35:danger.x-35);
  return BotPhysicsEngine.findGroundPoint(b.getMap(),new Point(x,-43));
 }
 static boolean tick(BotEntry e,Character b,boolean ai){
  if(!required(b))return false;
  Point spot=position(b);if(spot==null)throw new IllegalStateException("No reachable outer firing floor");
  if(!e.inAir&&!e.climbing&&Math.abs(b.getPosition().x-spot.x)>15){BotPinkBeanMovement.walkTo(e,b,spot,ai);return true;}
  if(e.climbing){BotPinkBeanMovement.walkTo(e,b,spot,ai);return true;}
  var crew=BotPinkBeanRun.crew(b);
  if(b.getMap().getMonsterById(8820006)==null&&crew!=null){
   int wave=b.getEventInstance().getIntProperty("stage");String key="outer-rally-ready-"+wave;
   if(!crew.notices.contains(key)){
    if(crew.notices.add("outer-rally-guide-"+wave))BotPqChat.queue(e,"Follow your party onto its assigned outer floor to stay clear of Munin's Seduce reach. The bot crew will regroup and continue.",false);
    boolean together=true;
    for(var mate:BotPinkBeanParticipation.readyMembers(crew))if(mate.isAlive()){
     if(mate.getMap()!=b.getMap()){together=false;break;}
     Point meeting=position(mate);
     if(mate.getPosition().distanceSq(meeting)>60*60){together=false;break;}
    }
    if(!together){
     long now=System.currentTimeMillis();int reminder=-8820000-wave;
     Long due=crew.next.putIfAbsent(reminder,now+15000);
     if(due!=null&&now>=due&&crew.next.replace(reminder,due,now+15000)){
      BotPqChat.queue(e,"We are regrouping the last bot crew members on their outer floor before finishing this statue. Join your party when you can.",false);
     }
     return BotOpqRun.hold(e,b,ai);
    }
    crew.next.remove(-8820000-wave);
    crew.notices.add(key);
   }
  }
  var choices=BotPinkBeanStrategy.targets(b,b.getMap().getAllMonsters());
  Monster target=null;for(var m:choices)if(m.isAlive()&&!m.isFake()&&BotPinkBeanRun.boss(m.getId())&&BotPinkBeanRun.canTarget(b,m)){target=m;break;}
  if(target!=null){
   var plan=BotCombatManager.planAttack(e,b,target);
   boolean inRange=plan!=null&&BotCombatManager.isTargetInAttackRange(plan,b,target);
   if(inRange&&e.attackCooldownMs<=0&&BotCombatManager.canUseAttackPlanNow(e,BotAttackExecutionProvider.getEquippedWeaponType(b),plan)){
    e.grinding=true;e.grindTarget=target;BotCombatManager.attackMonster(e,b,plan);
   }else if(!inRange&&!e.inAir&&!e.climbing&&e.attackCooldownMs<=0
       &&(b.getJob()==Job.NIGHTLORD||b.getJob()==Job.BISHOP)){
    var box=BotCombatManager.clientProjectileHitBox(b,BotPinkBeanTargets.aim(b,target).x<b.getPosition().x,1f);
    var body=BotMobHitboxProvider.getInstance().getMobBounds(target);
    if(body!=null&&box.x<body.x+body.width&&box.x+box.width>body.x){
     BotHurricane.cancel(b);BotTravelManager.clearMoveTargetPin(e);BotMovementManager.initiateJump(e,b,0);e.fixedAirArc=true;
    }
   }
  }
  return BotOpqRun.hold(e,b,ai);
 }
}
