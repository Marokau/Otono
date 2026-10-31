package server.bots;
import client.Character;
import client.Job;
import client.inventory.InventoryType;
import client.inventory.Item;
import client.inventory.manipulator.InventoryManipulator;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotPinkBeanRosterTest {
 @Test void thirtyParticipantsHaveFiveBalancedPartiesWithCorrectLeaders(){
  var c=new BotPinkBeanRun.Crew();c.leader=mock(Character.class);when(c.leader.getJob()).thenReturn(Job.NIGHTLORD);
  for(var job:BotPinkBeanCompanions.JOBS){var b=mock(Character.class);when(b.getJob()).thenReturn(job);c.bots.add(b);}
  assertEquals(29,c.bots.size());assertEquals(14,c.bots.stream().filter(b->b.getJob()==Job.NIGHTLORD).count());assertEquals(0,c.bots.stream().filter(b->b.getJob()==Job.HERO).count());var groups=new HashMap<Integer,List<Character>>();int right=0;
  for(var b:BotPinkBeanRun.all(c)){int group=BotPinkBeanRun.partyIndex(c,b);groups.computeIfAbsent(group,k->new ArrayList<>()).add(b);if(BotPinkBeanRun.rightSide(c,b))right++;assertSame(group==0?c.leader:c.bots.get(5+(group-1)*6),BotPinkBeanRun.partyLeader(c,b));}
  assertEquals(5,groups.size());assertEquals(12,right);
  for(var group:groups.values()){assertEquals(6,group.size());assertEquals(1,group.stream().filter(b->b.getJob()==Job.BISHOP).count());assertEquals(1,group.stream().filter(b->b.getJob()==Job.DARKKNIGHT).count());}
  assertFalse(BotPinkBeanRun.rightSide(c,c.bots.get(11)));assertTrue(BotPinkBeanRun.rightSide(c,c.bots.get(17)));assertFalse(BotPinkBeanRun.rightSide(c,c.bots.get(23)));
 }
 @Test void balancedFuryReplacesOldStarsThroughInventoryHandlers(){
  var b=mock(Character.class,RETURNS_DEEP_STUBS);var ilbi=new Item(2070006,(short)1,(short)800);var crystal=new Item(2070016,(short)2,(short)800);var fury=new Item(2070018,(short)3,(short)800);when(b.getInventory(InventoryType.USE).list()).thenReturn(List.of(ilbi,crystal,fury));
  try(var inventory=mockStatic(InventoryManipulator.class);var stock=mockStatic(BotZakumSupplies.class)){
   BotPinkBeanCompanions.prepareStars(b);inventory.verify(()->InventoryManipulator.removeFromSlot(b.getClient(),InventoryType.USE,(short)1,(short)800,false));inventory.verify(()->InventoryManipulator.removeFromSlot(b.getClient(),InventoryType.USE,(short)2,(short)800,false));inventory.verify(()->InventoryManipulator.removeFromSlot(b.getClient(),InventoryType.USE,(short)3,(short)800,false),never());stock.verify(()->BotZakumSupplies.stock(b,2070018,12000));
  }
 }
 @Test void preparationChecksTheActualHpGoalAndRetriesOnlyABoundedNumberOfTimes(){
  var b=mock(Character.class,RETURNS_DEEP_STUBS);var equip=mock(client.inventory.Equip.class);when(b.getInventory(InventoryType.EQUIPPED).list()).thenReturn(List.of(equip));var rolls=new java.util.concurrent.atomic.AtomicInteger();when(equip.getHp()).thenAnswer(call->(short)(rolls.get()>=2?200:100));
  try(var raid=mockStatic(BotHorntailCompanions.class);var gear=mockStatic(BotPinkBeanEquipment.class);var supplies=mockStatic(BotZakumSupplies.class);var weapons=mockStatic(BotPinkBeanWeapons.class);var damage=mockStatic(BotRaidDamageEquipment.class)){
   gear.when(()->BotPinkBeanEquipment.hpGoal(b)).thenReturn(200);gear.when(()->BotPinkBeanEquipment.prepare(b)).thenAnswer(call->{rolls.incrementAndGet();return null;});BotPinkBeanCompanions.prepare(b);assertEquals(2,rolls.get());
   rolls.set(0);when(equip.getHp()).thenReturn((short)100);assertThrows(IllegalStateException.class,()->BotPinkBeanCompanions.prepare(b));assertEquals(5,rolls.get());
  }
 }

}
