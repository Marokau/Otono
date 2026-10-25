package server.bots;
import client.Character;
import client.Disease;
import server.maps.Reactor;
import java.util.Comparator;
import java.util.function.Predicate;
/** GPQ intent only; movement and reactor attacks share the existing player-compatible paths. */
final class BotGuildActions {
 private BotGuildActions(){}
 static boolean smash(BotEntry e,Character b,Predicate<Reactor> wanted,boolean ai){
  var target=b.getMap().getAllReactors().stream().filter(r->r.isActive()&&wanted.test(r))
    .min(Comparator.comparingDouble(r->b.getPosition().distanceSq(r.getPosition()))).orElse(null);
  if(target==null)return BotOpqRun.hold(e,b,ai);
  var p=target.getPosition();
  if(Math.abs(b.getPosition().x-p.x)<=70&&Math.abs(b.getPosition().y-p.y)<=45){
   BotOpqRun.hold(e,b,ai);
   if(!b.hasDisease(Disease.STUN)&&e.attackCooldownMs<=0)BotOpqReactorAttack.hit(e,b,target);
   return true;
  }
  if(Math.abs(b.getPosition().x-p.x)<=65 && b.getPosition().y>p.y+45
      && b.getPosition().y-p.y<=BotPhysicsEngine.calculateMaxJumpHeight(e.movementProfile)+45
      && !e.inAir && !e.climbing){
   BotTravelManager.clearMoveTargetPin(e);BotMovementManager.initiateJump(e,b,0);e.fixedAirArc=true;
   BotManager.getInstance().stepMovementCore(e,b.getPosition(),ai);return true;
  }
  if(Math.abs(b.getPosition().x-p.x)<=65 && b.getPosition().y<p.y-45
      && !e.inAir && !e.climbing && BotPhysicsEngine.canStartDownJump(b.getMap(),b.getPosition())){
   BotTravelManager.clearMoveTargetPin(e);e.downJumpPending=true;
   BotManager.getInstance().stepMovementCore(e,b.getPosition(),ai);return true;
  }
  // Grounded GPQ rocks should target their actual supporting floor. A nearby
  // decorative ledge can otherwise win the floating-reactor heuristic and strand
  // the worker at a rope top instead of taking the ordinary two-jump route.
  var ground=BotPhysicsEngine.findGroundPoint(b.getMap(),new java.awt.Point(p.x,p.y-1));
  var stance=ground!=null&&Math.abs(ground.y-p.y)<=45?ground:
   BotOpqReactorApproach.stance(BotNavigationGraphProvider.peekGraph(b.getMap(),e.movementProfile),b,p);
  BotZakumPqRun.walkTo(e,b,stance==null?p:stance,ai);return true;
 }
}
