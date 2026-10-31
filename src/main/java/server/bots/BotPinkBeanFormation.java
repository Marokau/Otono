package server.bots;
import client.Character;
import client.Job;
import client.SkillFactory;
import constants.skills.Spearman;
import java.awt.Point;

/** Maintain a reachable party formation before Dispel, rather than chase after it. */
final class BotPinkBeanFormation {
 private BotPinkBeanFormation() {}
 static boolean required(Character b){
  if(!BotPinkBeanRun.inInstance(b)||b.getMap()==null)return false;
  if(b.getMap().getMonsterById(8820001)!=null)return true;
  if(b.getMap().getMonsterById(8820002)==null)return false;
  for(int id=8820003;id<=8820006;id++)if(b.getMap().getMonsterById(id)!=null)return false;
  return true;
 }
 static boolean ranged(Character b){return b.getJob()==Job.NIGHTLORD||b.getJob()==Job.BOWMASTER||b.getJob()==Job.BISHOP;}
 static Character anchor(Character b){
  Character best=null;
  for(var mate:b.getPartyMembersOnSameMap())if(mate.isAlive()&&!BotPinkBeanParticipation.human(mate)&&(mate.getJob()==Job.BOWMASTER||mate.getJob()==Job.NIGHTLORD))
   if(best==null||mate.getJob()==Job.BOWMASTER&&best.getJob()!=Job.BOWMASTER||mate.getJob()==best.getJob()&&mate.getId()<best.getId())best=mate;
  return best;
 }
 static boolean side(Character b,Point p){
  var body=b.getMap().getMonsterById(8820001);
  return body!=null?(p.x>body.getPosition().x)==(b.getPosition().x>body.getPosition().x):BotPinkBeanRun.rightSide(BotPinkBeanRun.crew(b),b)?p.x>0:p.x<0;
 }
 static boolean allows(Character b,Point p){
  if(!required(b)||b.getJob()!=Job.DARKKNIGHT&&!ranged(b))return true;
  if(!side(b,p))return false;
  var body=b.getMap().getMonsterById(8820001);
  if(body!=null&&ranged(b)&&Math.abs(p.x-body.getPosition().x)>300)return false;
  var anchor=anchor(b);if(anchor==null||anchor==b||!side(b,anchor.getPosition()))return true;
  Character knight=null;for(var mate:b.getPartyMembersOnSameMap())if(mate.isAlive()&&!BotPinkBeanParticipation.human(mate)&&mate.getSkillLevel(Spearman.HYPER_BODY)>0){knight=mate;break;}
  if(knight==null)return true;
  Point center=body==null?anchor.getPosition():new Point(body.getPosition().x+(b.getPosition().x>body.getPosition().x?250:-250),-42);
  var box=SkillFactory.getSkill(Spearman.HYPER_BODY).getEffect(knight.getSkillLevel(Spearman.HYPER_BODY)).calculateBoundingBox(center,false);
  // Each member stays in half the native box around the anchor. Two members
  // at opposite edges are then still within the caster's full skill range.
  if(body==null||b.getJob()!=Job.DARKKNIGHT)box.grow(-box.width/4,-box.height/4);
  return box.contains(p);
 }
 static boolean tick(BotEntry e,Character b,boolean ai){
  if(!required(b)||b.getJob()!=Job.DARKKNIGHT)return false;
  if(allows(b,b.getPosition())){
   // Entering buff range halfway up a rope does not finish the approach.
   // Preserve the normal committed movement until the caster lands.
   if((e.climbing||e.inAir)&&e.moveTarget!=null){BotPinkBeanMovement.walkTo(e,b,new Point(e.moveTarget),ai);return true;}
   return false;
  }
  var anchor=anchor(b);if(anchor==null||!side(b,anchor.getPosition()))return false;
  Point destination=anchor.getPosition();var leader=BotManager.getInstance().getEntryByBotCharId(anchor.getId());
  if(leader!=null&&(leader.inAir||leader.climbing)&&leader.moveTarget!=null)destination=new Point(leader.moveTarget);
  if(!BotHorntailCombat.safe(b,destination,b.getMap().getAllMonsters()))return false;
  BotPinkBeanMovement.walkTo(e,b,destination,ai);return true;
 }
}
