package server.bots;
import client.Character;
import client.inventory.InventoryType;
import client.inventory.manipulator.InventoryManipulator;

/** Normal earring sacrifice, Ergoth combat, Rubian handoff and timed bonus exit. */
final class BotGuildBoss {
 private BotGuildBoss(){}
 static boolean sacrificing(Character b){return b!=null&&BotGuildCompanions.inInstance(b)&&b.getMapId()==990000800&&b.getJob()==client.Job.BOWMAN&&b.getMap().getReactorByName("kinggate").getState()==0;}
 static boolean tick(BotEntry e,Character b,BotGuildCompanions.Crew c,BotGuildRun.State s,boolean ai){
  int m=b.getMapId();
  if(m==990000800){
   if(BotGuildRun.intro(e,b,s,"sacrifice-intro","Stage Five: One volunteer must remove and offer their protective earrings at the sealed door. Our scout will make the sacrifice. Keep your own earrings equipped, wait for the gate to open, then enter Ergoth's chamber.",ai))return true;
   var gate=b.getMap().getReactorByName("kinggate");
   if(gate.getState()>0){var earrings=b.getInventory(InventoryType.EQUIPPED).getItem((short)-4);if(b.getJob()==client.Job.BOWMAN&&(earrings==null||earrings.getItemId()!=1032033))return BotOpqRun.hold(e,b,ai);if(s.notices.add("sacrifice-clear"))c.leader.dropMessage(5,"GPQ: The sacrifice opened Ergoth's chamber. Enter when ready; keep your protective earrings equipped.");return BotGuildRun.advance(e,b,c,"boss00",ai);}
   if(!sacrificing(b)||!c.bots.stream().allMatch(x->x.getMapId()==990000800)||c.leader.getMapId()!=990000800)return BotOpqRun.hold(e,b,ai);
   if(!gate.getArea().contains(b.getPosition())||e.inAir||e.climbing){BotZakumPqRun.walkTo(e,b,gate.getPosition(),ai);return true;}
   BotOpqRun.hold(e,b,ai);
   var worn=b.getInventory(InventoryType.EQUIPPED).getItem((short)-4);
   if(worn!=null&&worn.getItemId()==1032033){short slot=b.getInventory(InventoryType.EQUIP).getNextFreeSlot();if(slot<0)return true;InventoryManipulator.unequip(b.getClient(),(short)-4,slot);}
   if(b.getItemQuantity(1032033,false)>0&&BotGuildRun.ready(s,b))BotManager.getInstance().issueDropItem(e,InventoryType.EQUIP,1032033,(short)1);
   return true;
  }
  if(m==990000900){
   if(BotGuildRun.intro(e,b,s,"ergoth-intro","Final Stage: Gather by the chamber entrance first. We will wait for everyone, cast Hyper Body and Magic Guard, then break the Rubian crystal to awaken Ergoth and fight his guards. The boss loot is yours. Pick up the Rubian and return it to the knight to finish the quest.",ai))return true;
   boolean living=b.getMap().getAllMonsters().stream().anyMatch(x->x.isAlive());
   if(living){if(b.getJob()==client.Job.BOWMAN)return BotOpqRun.hold(e,b,ai);if(BotGuildBossSupport.tick(e,b,c,false,ai))return true;return BotOpqRun.grind(e,b,ai);}
   if(b.getMap().getAllReactors().stream().anyMatch(r->r.getId()==9201002&&r.isActive()&&r.getState()<5)){if(BotGuildBossSupport.tick(e,b,c,true,ai))return true;return BotGuildActions.smash(e,b,r->r.getId()==9201002&&r.getState()<5,ai);}
   if(c.test&&b==c.leader){if(b.getItemQuantity(4001024,false)>0)return BotGuildRun.talk(e,b,s,9040010,ai);if(BotGuildRun.collect(e,b,x->x==4001024,ai))return true;}
   if(s.notices.add("ergoth-clear"))c.leader.dropMessage(5,"GPQ: Ergoth is defeated. Collect the Rubian and speak to the knight. All boss gear is yours.");
   return BotOpqRun.hold(e,b,ai);
  }
  if(m==990001000||m==990001101){
   if(BotGuildRun.intro(e,b,s,"bonus-intro","Bonus Room: We will help break the treasure boxes. All rewards are yours to collect. This room has a short timer, so collect what you want before it ends.",ai))return true;
   if(c.test&&b==c.leader&&BotGuildRun.collect(e,b,BotGuildPredicates.ANY_ITEM,ai))return true;
   return BotGuildActions.smash(e,b,r->r.getId()==9202012&&r.isActive()&&r.getState()<5,ai);
  }
  return BotOpqRun.hold(e,b,ai);
 }
}
