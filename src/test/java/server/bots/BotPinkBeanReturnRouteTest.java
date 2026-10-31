package server.bots;
import java.awt.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BotPinkBeanReturnRouteTest {
 @Test void upperRouteUsesNativeMovementWithoutBodyContact(){
  var map=BotNavigationMapLoader.loadMapGeometry(270050100);
  var lab=BotMovementSimulationLab.fromMap(map);
  for(var profile:java.util.List.of(new BotMovementProfile(100,100),new BotMovementProfile(140,120))){
  for(var start:java.util.List.of(new Point(-590,-42),new Point(-200,-42),new Point(-85,-133))){
  var e=lab.spawnBot("return",99001,map,start);
  org.mockito.Mockito.when(e.bot.getTotalMoveSpeedStat()).thenReturn(profile.totalSpeedStat());
  org.mockito.Mockito.when(e.bot.getTotalJumpStat()).thenReturn(profile.totalJumpStat());
  e.movementProfile=profile;
  BotNavigationGraphProvider.rebuildGraph(map,profile);
  var body=new Rectangle(-30,-127,67,85);
  int ticks=0,exposed=0;
  var route=new BotPinkBeanBodyReturn.Route();boolean arrived=false;
  for(int n=0;n<1800;n++){
   boolean moving=e.inAir||e.climbing;
   var goal=BotPinkBeanBodyReturn.destination(route,lab.position("return"),moving);
   if(goal==null){arrived=true;break;}
   if(moving&&e.moveTarget!=null)goal=new Point(e.moveTarget);
   lab.setMoveTarget("return",goal,true);lab.stepRaw("return",goal,true);
   var feet=lab.position("return");ticks++;
   if(new Rectangle(-500,-315,490,372).intersects(new Rectangle(feet.x-12,feet.y-45,24,45)))exposed++;
   assertFalse(body.intersects(new Rectangle(feet.x-12,feet.y-45,24,45)),"Contact approaching "+goal+" at "+feet);
  }
  assertTrue(arrived,"Return did not finish at "+lab.position("return")+"\n"+String.join("\n",lab.formatRecentTrace("return",8)));
  System.out.println("Upper return "+start+" "+profile+": "+ticks*50+"ms, left heavy cone exposure "+exposed*50+"ms");
  }
  }
 }
 @Test void aForcedFallRestartsTheClimbInsteadOfCrossingTheFloor(){
  var route=new BotPinkBeanBodyReturn.Route();route.step=3;
  assertEquals(new Point(-465,-456),BotPinkBeanBodyReturn.destination(route,new Point(-200,-42),false));
  assertEquals(0,route.step);
 }
 @Test void heavyAttackWarningWaitsOnSafeGroundWithoutInterruptingAnArc(){
  var cone=java.util.List.of(new Rectangle(-500,-315,490,372));
  assertTrue(BotPinkBeanBodyReturn.waitForImpact(new Point(-590,-42),false,cone));
  assertFalse(BotPinkBeanBodyReturn.waitForImpact(new Point(-465,-277),false,cone));
  assertFalse(BotPinkBeanBodyReturn.waitForImpact(new Point(-590,-42),true,cone));
 }
 @Test void rejoinedMembersWaitForProtectionBeforeReturningToCombat(){
  for(var job:java.util.List.of(client.Job.NIGHTLORD,client.Job.BISHOP)){
   var b=org.mockito.Mockito.mock(client.Character.class);var dk=org.mockito.Mockito.mock(client.Character.class);
   var map=org.mockito.Mockito.mock(server.maps.MapleMap.class);
   org.mockito.Mockito.when(b.getMap()).thenReturn(map);org.mockito.Mockito.when(b.getMapId()).thenReturn(270050100);
   org.mockito.Mockito.when(b.getPosition()).thenReturn(new Point(200,-42));org.mockito.Mockito.when(b.getJob()).thenReturn(job);
   org.mockito.Mockito.when(dk.getPosition()).thenReturn(new Point(200,-42));org.mockito.Mockito.when(dk.isAlive()).thenReturn(true);
   org.mockito.Mockito.when(dk.getSkillLevel(constants.skills.Spearman.HYPER_BODY)).thenReturn(30);
   org.mockito.Mockito.when(b.getPartyMembersOnSameMap()).thenReturn(java.util.List.of(b,dk));
   org.mockito.Mockito.when(b.getBuffedValue(client.BuffStat.HYPERBODYHP)).thenReturn(null);
   org.mockito.Mockito.when(b.getBuffedValue(client.BuffStat.HYPERBODYMP)).thenReturn(null);
   org.mockito.Mockito.when(b.getBuffedValue(client.BuffStat.MAGIC_GUARD)).thenReturn(null);
   var e=new BotEntry(b,b,null);var notices=new java.util.HashSet<String>();notices.add("prepared-270050100");
   try(var hold=org.mockito.Mockito.mockStatic(BotOpqRun.class)){
    hold.when(()->BotOpqRun.hold(e,b,true)).thenReturn(true);
    assertTrue(BotHorntailSupport.tick(e,b,java.util.List.of(b,dk),notices,b,"Pink Bean",true));
    org.mockito.Mockito.when(b.getBuffedValue(client.BuffStat.HYPERBODYHP)).thenReturn(60);
    org.mockito.Mockito.when(b.getBuffedValue(client.BuffStat.HYPERBODYMP)).thenReturn(60);
    org.mockito.Mockito.when(b.getBuffedValue(client.BuffStat.MAGIC_GUARD)).thenReturn(80);
    assertFalse(BotHorntailSupport.tick(e,b,java.util.List.of(b,dk),notices,b,"Pink Bean",true));
   }
  }
 }

}
