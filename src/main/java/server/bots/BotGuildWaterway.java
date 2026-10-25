package server.bots;
import client.Character;
import client.Job;
import java.util.*;

/** Four legitimate clothing routes, including the <=30 room. */
final class BotGuildWaterway {
 private BotGuildWaterway(){}
 static boolean tick(BotEntry e,Character b,BotGuildCompanions.Crew c,BotGuildRun.State s,boolean ai){
  int m=b.getMapId();var event=b.getEventInstance();var grave=event.getMapInstance(990000700);
  if("true".equals(event.getProperty("stage4clear"))){
   if(m==990000700)return BotGuildRun.advance(e,b,c,"next00",ai);
   return toGrave(e,b,ai);
  }
  if(BotGuildRun.intro(e,b,s,"clothes-intro","Stage Four: We will search four waterways for Sharen's shoes, trousers, shirt and crown. Our scout handles the level-30-and-under room. Reach the door at the far end of a side room to join us at Sharen's grave; we return his clothes from bottom to top.",ai))return true;
  if(m==990000600){
   if(b.getLevel()<=30)return BotMagatiaRun.portal(e,b,"in03",ai);
   if(b==c.leader)return BotMagatiaRun.portal(e,b,"in00",ai);
   if((b.getJob().isA(Job.WARRIOR)||b.getJob().isA(Job.BOWMAN))&&b!=c.leader)return BotMagatiaRun.portal(e,b,"in00",ai);
   if(b.getJob().isA(Job.THIEF))return BotMagatiaRun.portal(e,b,"in02",ai);
   if(b.getJob().isA(Job.MAGICIAN)||b==c.leader)return BotMagatiaRun.portal(e,b,"in01",ai);
   if(grave.getNPCById(9040003)!=null)return BotMagatiaRun.portal(e,b,"in01",ai);
   return BotOpqRun.hold(e,b,ai);
  }
  if(m==990000610||m==990000630||m==990000640){
   String name=m==990000610?"secretgate1":m==990000630?"secretgate2":"secretgate3";
   int need=m==990000610?9:m==990000630?8:20;
   var gate=b.getMap().getReactorByName(name);
   if(gate.getState()>0)return BotMagatiaRun.portal(e,b,"in00",ai);
   if(m==990000610&&(b==c.leader||b.getJob().isA(Job.BOWMAN))){if(b.getMap().getSpawnedMonstersOnMap()>0)return BotOpqRun.grind(e,b,ai);return BotOpqRun.hold(e,b,ai);}
   if(b.getItemQuantity(4001035,false)>=need)return BotGuildRun.drop(e,b,s,gate,4001035,need,ai);
   if(BotGuildRun.collect(e,b,x->x==4001035||x==4001036,ai))return true;
   if(m==990000630 && b.getItemQuantity(4001036,false)>0){var gargoyle=b.getMap().getAllReactors().stream().filter(r->r.getId()==9201000).findFirst().orElse(null);return BotGuildRun.drop(e,b,s,gargoyle,4001036,1,ai);}
   return BotOpqRun.grind(e,b,ai);
  }
  if(m==990000611||m==990000620||m==990000631||m==990000641){
   int item=m==990000611?4001031:m==990000620?4001032:m==990000631?4001033:4001034;
   int reactor=m==990000611?9202005:m==990000620?9202006:m==990000631?9202007:9202008;
   // A returning/extra teammate need not take another copy of the clothing.
   boolean delivered=grave.getAllReactors().stream().anyMatch(r->r.getId()==9201001&&r.getState()>clothingOrder(item));
   if(b==c.leader||m==990000611&&b.getJob().isA(Job.BOWMAN)||b.getItemQuantity(item,false)>0||delivered)return BotMagatiaRun.portal(e,b,"guild1F00",ai);
   if(BotGuildRun.collect(e,b,x->x==item,ai))return true;
   return BotGuildActions.smash(e,b,r->r.getId()==reactor&&r.isActive()&&r.getState()<5,ai);
  }
  if(m==990000700){
   var body=grave.getAllReactors().stream().filter(r->r.getId()==9201001).findFirst().orElse(null);
   if(body!=null&&body.getState()<4){int item=new int[]{4001032,4001031,4001033,4001034}[body.getState()];if(b.getItemQuantity(item,false)>0)return BotGuildRun.drop(e,b,s,body,item,1,ai);}
   if(grave.getNPCById(9040003)!=null){if(s.notices.add("clothes-ready"))c.leader.dropMessage(5,"GPQ: Sharen's clothing has been returned at Sharen III's Grave. Use the far-end door of a clothing room to reach the grave, then speak to Sharen III's Soul to open the next gate.");if(c.test&&b==c.leader)return BotGuildRun.talk(e,b,s,9040003,ai);}
   return BotOpqRun.hold(e,b,ai);
  }
  return BotOpqRun.hold(e,b,ai);
 }
 static int clothingOrder(int item){return switch(item){case 4001032->0;case 4001031->1;case 4001033->2;case 4001034->3;default->4;};}
 static boolean toGrave(BotEntry e,Character b,boolean ai){int m=b.getMapId();if(m==990000600)return BotMagatiaRun.portal(e,b,"in00",ai);if(m==990000610||m==990000630||m==990000640)return BotMagatiaRun.portal(e,b,"in00",ai);if(m==990000611||m==990000620||m==990000631||m==990000641)return BotMagatiaRun.portal(e,b,"guild1F00",ai);return BotOpqRun.hold(e,b,ai);}
}
