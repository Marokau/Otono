package server.bots;
import client.Character;
import client.BuffStat;
import client.inventory.InventoryType;
import scripting.event.EventInstanceManager;
import server.maps.Reactor;
import java.awt.Point;
import java.util.*;
import java.util.function.IntPredicate;

/** GPQ stage intentions; all movement, skills, drops and checks use ordinary gameplay. */
final class BotGuildRun {
 static final class State {
  final Set<String> notices=java.util.concurrent.ConcurrentHashMap.newKeySet();
  final Map<Integer,Long> next=new HashMap<>();
  final Set<Integer> spearDone=java.util.concurrent.ConcurrentHashMap.newKeySet();
  long entered;
 }
 private static final Map<EventInstanceManager,State> STATES=Collections.synchronizedMap(new WeakHashMap<>());
 private BotGuildRun(){}
 static State state(Character b){synchronized(STATES){return STATES.computeIfAbsent(b.getEventInstance(),k->new State());}}
 static boolean needsDarkSight(Character b){return b!=null && BotGuildCompanions.inInstance(b) && b.getMapId()==990000430 && b.getJob().isA(client.Job.THIEF);}
 static boolean tick(BotEntry e,Character b,BotGuildCompanions.Crew c,boolean ai){
  var s=state(b);int m=b.getMapId();
  synchronized(s){if(System.currentTimeMillis()<s.next.getOrDefault(b.getId(),0L))return BotOpqRun.hold(e,b,ai);}
  if(m==990000301 || m==990000401)return advance(e,b,c,"next01",ai);
  if(m>=990000400 && m<=990000440)return spears(e,b,c,s,ai);
  if(m>=990000500&&m<=990000502)return BotGuildOfferings.tick(e,b,c,s,ai);
  if(m>=990000600&&m<=990000700)return BotGuildWaterway.tick(e,b,c,s,ai);
  if(m>=990000800)return BotGuildBoss.tick(e,b,c,s,ai);
  if(s.notices.add("pending-"+m))c.leader.dropMessage(5,"GPQ: The spear stage is complete. The next stage is being developed; we will wait here.");
  return BotOpqRun.hold(e,b,ai);
 }
 static boolean intro(BotEntry e,Character b,State s,String key,String text,boolean ai){
  synchronized(s){if(s.notices.add(key)){s.entered=System.currentTimeMillis();BotPqChat.queue(e,text,false);}if(System.currentTimeMillis()-s.entered<6500){BotOpqRun.hold(e,b,ai);return true;}}return false;
 }
 static boolean advance(BotEntry e,Character b,BotGuildCompanions.Crew c,String p,boolean ai){
  if(!c.test && c.leader.getMapId()<=b.getMapId())return BotOpqRun.hold(e,b,ai);
  return BotMagatiaRun.portal(e,b,p,ai);
 }
 static boolean collect(BotEntry e,Character b,IntPredicate wanted,boolean ai){
  var drop=b.getMap().getDroppedItems().stream().filter(d->!d.isPickedUp()&&wanted.test(d.getItemId())&&d.canBePickedBy(b)&&System.currentTimeMillis()-d.getDropTime()>500)
   .min(Comparator.comparingDouble(d->b.getPosition().distanceSq(d.getPosition()))).orElse(null);
  if(drop==null)return false;
  var position=b.getPosition();var target=drop.getPosition();
  if(position.distanceSq(target)<=30*30){b.pickupItem(drop);BotOpqRun.hold(e,b,ai);return true;}
  // Pickup need not finish standing on the item's ledge: a player can jump and
  // press pickup while airborne. Keep the same ordinary 30px pickup radius.
  int above=position.y-target.y;
  if(!e.inAir&&!e.climbing&&Math.abs(position.x-target.x)<=20&&above>0
      && above<=BotPhysicsEngine.calculateMaxJumpHeight(e.movementProfile)+30
      && !server.maps.FieldLimit.JUMP.check(b.getMap().getFieldLimit())){
   BotTravelManager.clearMoveTargetPin(e);BotMovementManager.initiateJump(e,b,0);e.fixedAirArc=true;
   BotManager.getInstance().stepMovementCore(e,position,ai);return true;
  }
  BotZakumPqRun.stepTowardsAndPickUp(e,b,drop,ai);return true;
 }
 static boolean ready(State s,Character b){synchronized(s){long now=System.currentTimeMillis();if(now<s.next.getOrDefault(b.getId(),0L))return false;s.next.put(b.getId(),now+1400);return true;}}
 static boolean drop(BotEntry e,Character b,State s,Reactor r,int item,int qty,boolean ai){
  if(r==null||b.getItemQuantity(item,false)<qty)return BotOpqRun.hold(e,b,ai);
  if(!r.getArea().contains(b.getPosition())||e.inAir||e.climbing){BotZakumPqRun.walkTo(e,b,r.getPosition(),ai);return true;}
  BotOpqRun.hold(e,b,ai);if(ready(s,b)&&BotManager.getInstance().issueDropItem(e,InventoryType.ETC,item,(short)qty)){synchronized(s){s.next.put(b.getId(),System.currentTimeMillis()+5500);}}return true;
 }
 static boolean talk(BotEntry e,Character b,State s,int npc,boolean ai){
  // The elevated Guardian Statue is clicked from the adjacent ledges/floor.
  // NPCTalkHandler uses the visible NPC object, not a requirement to stand on its origin.
  int radius=npc==9040006?400:100;
  var a=BotTravelManager.tickApproachNpc(e,b,b.getMapId(),npc,2,ai,false,radius);
  if(a==BotTravelManager.ApproachStatus.ARRIVED&&ready(s,b)){if(b.getClient().getCM()==null)BotNpcInteraction.talk(b,npc);else BotNpcInteraction.next(b,1);}return true;
 }
 static boolean spears(BotEntry e,Character b,BotGuildCompanions.Crew c,State s,boolean ai){
  int m=b.getMapId();var main=b.getEventInstance().getMapInstance(990000400);var gate=main.getReactorByName("speargate");
  if(gate.getState()==4){
   synchronized(s){if(s.notices.add("spears-clear"))c.leader.dropMessage(5,"GPQ: All four Longinus Spears are placed. Enter the opened door when ready.");}
   if(m!=990000400)return BotMagatiaRun.portal(e,b,"out00",ai);
   return advance(e,b,c,"next00",ai);
  }
  if(intro(e,b,s,"spears-intro","Stage Two: We will retrieve four Longinus Spears and place them on the towers. Our thief retrieves the guarded spear, our mage handles the Teleport tower, and the others climb the jump courses. You can explore; follow the opened door when all four are placed.",ai))return true;
  boolean mage=b.getJob().isA(client.Job.MAGICIAN),thief=b.getJob().isA(client.Job.THIEF);
  if(m==990000400){
   if(thief && b.getItemQuantity(4001025,false)>0){
    Character partner=null;for(var member:c.bots)if(member.getJob().isA(client.Job.MAGICIAN)){partner=member;break;}
    if(partner==null)return BotOpqRun.hold(e,b,ai);
    if(partner.getMapId()!=m||b.getPosition().distanceSq(partner.getPosition())>1600){if(partner.getMapId()==m)BotZakumPqRun.walkTo(e,b,partner.getPosition(),ai);return true;}
    BotOpqRun.hold(e,b,ai);if(ready(s,b)&&BotManager.getInstance().issueDropItem(e,InventoryType.ETC,4001025,(short)1))s.spearDone.add(b.getId());return true;
   }
   if(b.getItemQuantity(4001025,false)>0)return BotMagatiaRun.portal(e,b,"in03",ai);
   if(thief&&s.spearDone.contains(b.getId()))return BotMagatiaRun.portal(e,b,"in03",ai);
   if(s.spearDone.contains(b.getId())||b==c.leader)return BotOpqRun.hold(e,b,ai);
   if(mage){if(collect(e,b,BotGuildPredicates.SPEAR,ai))return true;return BotOpqRun.hold(e,b,ai);}
   if(thief){if(main.getReactorByName("stonegate").getState()==0){if(b.getItemQuantity(4001026,false)>0)return drop(e,b,s,main.getReactorByName("stonegate"),4001026,1,ai);return BotMagatiaRun.portal(e,b,"in01",ai);}return BotMagatiaRun.portal(e,b,"in02",ai);}
   return BotMagatiaRun.portal(e,b,"in00",ai);
  }
  if(m==990000410){
   if(b.getItemQuantity(4001025,false)>0)return BotMagatiaRun.portal(e,b,"out00",ai);
   if(collect(e,b,x->x==4001025,ai))return true;
   // Separate workers claim the three distinct spear rocks, never teleport to them.
   var workers=c.bots.stream().filter(x->!x.getJob().isA(client.Job.MAGICIAN)&&!x.getJob().isA(client.Job.THIEF)).sorted(Comparator.comparingInt(Character::getId)).toList();
   var rocks=b.getMap().getAllReactors().stream().filter(r->r.getId()==9202001).sorted(Comparator.comparingInt(r->r.getPosition().x)).toList();
   int index=workers.indexOf(b);if(index<0||index>=rocks.size())return BotOpqRun.hold(e,b,ai);
   var target=rocks.get(index);return BotGuildActions.smash(e,b,r->r==target&&r.getState()<5,ai);
  }
  if(m==990000420){if(main.getReactorByName("stonegate").getState()>0)return BotMagatiaRun.portal(e,b,"out00",ai);if(b.getItemQuantity(4001026,false)>0)return BotMagatiaRun.portal(e,b,"out00",ai);if(collect(e,b,x->x==4001026,ai))return true;return BotOpqRun.grind(e,b,ai);}
  if(m==990000430){
   if(thief){if(b.getBuffedValue(BuffStat.DARKSIGHT)==null)return BotOpqRun.hold(e,b,ai);if(b.getItemQuantity(4001025,false)>0)return BotMagatiaRun.portal(e,b,"out00",ai);if(b.getMap().getReactorByName("metalgate").getState()>0)return BotMagatiaRun.portal(e,b,"in00",ai);if(b.getItemQuantity(4001037,false)>0)return drop(e,b,s,b.getMap().getReactorByName("metalgate"),4001037,1,ai);if(collect(e,b,x->x==4001037,ai))return true;return BotGuildActions.smash(e,b,r->r.getId()==9202002&&r.getState()<30,ai);}
   if(mage){if(b.getItemQuantity(4001025,false)>0)return BotMagatiaRun.portal(e,b,"out00",ai);if(b.getMap().getReactorByName("metalgate").getState()>0)return BotMagatiaRun.portal(e,b,"in00",ai);return BotOpqRun.hold(e,b,ai);}
  }
  if(m==990000431){if(b.getBuffedValue(BuffStat.DARKSIGHT)!=null)b.cancelEffectFromBuffStat(BuffStat.DARKSIGHT);if(b.getItemQuantity(4001025,false)>0)return BotMagatiaRun.portal(e,b,"out00",ai);if(collect(e,b,x->x==4001025,ai))return true;return BotGuildActions.smash(e,b,r->r.getId()==9202001&&r.getState()<5,ai);}
  if(m==990000440){
   if(thief){
    Character jumper=null;for(var member:c.bots)if(member!=b&&!member.getJob().isA(client.Job.MAGICIAN)&&member.getMapId()==m&&member.getItemQuantity(4001025,false)>0){jumper=member;break;}
    if(jumper!=null&&b.getPosition().distanceSq(jumper.getPosition())>22500){BotZakumPqRun.walkTo(e,b,jumper.getPosition(),ai);return true;}
    return BotOpqRun.hold(e,b,ai);
   }
   var targets=b.getMap().getAllReactors().stream().filter(r->r.getId()==9208007).sorted(Comparator.comparingInt(r->r.getPosition().x)).toList();
   if(b.getItemQuantity(4001025,false)==0){s.spearDone.add(b.getId());return BotMagatiaRun.portal(e,b,"out00",ai);}
   var placers=c.bots.stream().filter(x->!x.getJob().isA(client.Job.THIEF)).sorted(Comparator.comparingInt(Character::getId)).toList();
   int index=b.getJob().isA(client.Job.MAGICIAN)?3:b.getJob()==client.Job.BOWMAN?0:b.getJob().isA(client.Job.WARRIOR)?2:1;if(index>=0&&index<targets.size())return drop(e,b,s,targets.get(index),4001025,1,ai);
  }
  return BotOpqRun.hold(e,b,ai);
 }
}
