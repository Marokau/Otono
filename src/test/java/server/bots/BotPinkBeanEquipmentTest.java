package server.bots;
import client.Character;
import client.Job;
import client.inventory.Equip;
import client.inventory.WeaponType;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class BotPinkBeanEquipmentTest {
 @org.junit.jupiter.api.BeforeAll static void itemDataWithoutDatabase() throws Exception {
  var connection=mock(java.sql.Connection.class);var query=mock(java.sql.PreparedStatement.class);when(connection.prepareStatement(anyString())).thenReturn(query);when(query.executeQuery()).thenReturn(mock(java.sql.ResultSet.class));
  try(var database=mockStatic(tools.DatabaseConnection.class)){database.when(tools.DatabaseConnection::getConnection).thenReturn(connection);Class.forName("server.ItemInformationProvider");}
 }


 private BotEquipManager.OptimizerHooks hooks(){return new BotEquipManager.OptimizerHooks(){
  public boolean isTwoHanded(int id){return true;}
  public WeaponType getWeaponType(int id){return id==1472068?WeaponType.CLAW:WeaponType.NOT_A_WEAPON;}
  public boolean isOverall(int id){return false;}
  public boolean meetsReqs(Equip e,Job j,int lv,int s,int d,int i,int l,int fame){return true;}
  public Map<String,Integer> getEquipStats(int id){return Map.of();}
 };}
 private Equip equip(int id,int atk,int hp){var e=new Equip(id,(short)0);e.setWatk((short)atk);e.setHp((short)hp);return e;}
 @Test void dedicatedHpGoalSurvivesPruningAndTakesPriorityOverDamage(){
  var b=mock(Character.class);when(b.getJob()).thenReturn(Job.NIGHTLORD);var weapon=equip(1472068,56,0);var damage=equip(1082149,20,0);var health=equip(1082149,10,200);var naked=new BotEquipManager.StatSnapshot(4,150,4,850,0,0,0,180,0,Job.NIGHTLORD);
  try(var goals=mockStatic(BotPinkBeanEquipment.class);var cycle=mockStatic(BotHorntailEquipment.class)){
   goals.when(()->BotPinkBeanEquipment.hpGoal(b)).thenReturn(200);cycle.when(()->BotHorntailEquipment.attackCycle(b,1472068)).thenReturn(600);
   var result=BotEquipManager.solveForWeapon(b,hooks(),naked,weapon,List.of((short)-8),Map.of(),Map.of((short)-8,List.of(damage,health)),null);
   assertSame(health,result.picks().get((short)-8));assertTrue(result.score().damage()>0);
   goals.when(()->BotPinkBeanEquipment.hpGoal(b)).thenReturn(0);
   var ordinary=BotEquipManager.solveForWeapon(b,hooks(),naked,weapon,List.of((short)-8),Map.of(),Map.of((short)-8,List.of(damage,health)),null);
   assertSame(damage,ordinary.picks().get((short)-8));
  }
 }
 @Test void equalOffensePiecesWithDifferentHpAreNotDeduplicatedAway(){
  var b=mock(Character.class);when(b.getJob()).thenReturn(Job.NIGHTLORD);var weapon=equip(1472068,56,0);var low=equip(1082149,10,40);var high=equip(1082149,10,200);var naked=new BotEquipManager.StatSnapshot(4,150,4,850,0,0,0,180,0,Job.NIGHTLORD);
  try(var goals=mockStatic(BotPinkBeanEquipment.class);var cycle=mockStatic(BotHorntailEquipment.class)){
   goals.when(()->BotPinkBeanEquipment.hpGoal(b)).thenReturn(200);cycle.when(()->BotHorntailEquipment.attackCycle(b,1472068)).thenReturn(600);
   var result=BotEquipManager.solveForWeapon(b,hooks(),naked,weapon,List.of((short)-8),Map.of(),Map.of((short)-8,List.of(low,high)),null);assertSame(high,result.picks().get((short)-8));
  }
 }
 @Test void humanAndActiveEncounterPreparationAreRefused(){var b=mock(Character.class,RETURNS_DEEP_STUBS);assertThrows(IllegalArgumentException.class,()->BotPinkBeanEquipment.prepare(b));when(b.getClient()).thenReturn(mock(client.BotClient.class));assertThrows(IllegalArgumentException.class,()->BotPinkBeanEquipment.prepare(b));}
 @Test void fullOptimizerKeepsHpCandidatesDuringItemPruning(){
  var b=mock(Character.class,RETURNS_DEEP_STUBS);when(b.getJob()).thenReturn(Job.NIGHTLORD);when(b.getLevel()).thenReturn(180);when(b.getMap()).thenReturn(null);when(b.getTotalStr()).thenReturn(4);when(b.getTotalDex()).thenReturn(150);when(b.getTotalInt()).thenReturn(4);when(b.getTotalLuk()).thenReturn(850);when(b.getTotalWatk()).thenReturn(56);when(b.getTotalMagic()).thenReturn(4);
  var weapon=equip(1472068,56,0);weapon.setPosition((short)-11);when(b.getInventory(client.inventory.InventoryType.EQUIPPED).list()).thenReturn(List.of(weapon));when(b.getInventory(client.inventory.InventoryType.EQUIPPED).getItem((short)-11)).thenReturn(weapon);when(b.getInventory(client.inventory.InventoryType.EQUIP).list()).thenReturn(List.of());var damage=equip(1082149,20,0);var health=equip(1082149,10,200);
  try(var goals=mockStatic(BotPinkBeanEquipment.class);var cycle=mockStatic(BotHorntailEquipment.class)){
   goals.when(()->BotPinkBeanEquipment.hpGoal(b)).thenReturn(200);cycle.when(()->BotHorntailEquipment.attackCycle(b,1472068)).thenReturn(600);
   var result=BotEquipManager.runOptimizerWithExtras(b,List.of(damage,health));assertSame(health,result.picks().get((short)-8));
  }
 }
 @Test void bishopsKeepEnoughGearHpForContactThatLeaksThroughMagicGuard(){
  var b=mock(Character.class,RETURNS_DEEP_STUBS);when(b.getClient()).thenReturn(mock(client.BotClient.class));when(b.getEventInstance()).thenReturn(null);when(b.getJob()).thenReturn(Job.BISHOP);when(b.getMaxHp()).thenReturn(2200);
  when(b.getInventory(client.inventory.InventoryType.EQUIPPED).list()).thenReturn(List.of(equip(1050001,0,3800)));
  BotPinkBeanEquipment.prepare(b);assertEquals(3800,BotPinkBeanEquipment.hpGoal(b));assertEquals(2200,b.getMaxHp());
 }

 @Test void horntailTargetsSixThousandHpWithoutDependingOnHyperBody(){
  var b=mock(Character.class,RETURNS_DEEP_STUBS);when(b.getClient()).thenReturn(mock(client.BotClient.class));when(b.getEventInstance()).thenReturn(null);when(b.getJob()).thenReturn(Job.BISHOP);when(b.getName()).thenReturn("HornBishop1");when(b.getMaxHp()).thenReturn(2200);when(b.getInventory(client.inventory.InventoryType.EQUIPPED).list()).thenReturn(List.of(equip(1050001,0,3800)));
  BotPinkBeanEquipment.prepare(b);assertEquals(3800,BotPinkBeanEquipment.hpGoal(b));assertEquals(2200,b.getMaxHp());
 }
 @Test void meleeAndRangedFloorsDoNotDependOnHyperBodyRemainingActive(){
  var b=mock(Character.class);when(b.getJob()).thenReturn(Job.HERO);assertEquals(15625,BotRaidHp.minimumUnbuffed(b));when(b.getJob()).thenReturn(Job.DARKKNIGHT);assertEquals(15625,BotRaidHp.minimumUnbuffed(b));when(b.getJob()).thenReturn(Job.NIGHTLORD);assertEquals(7500,BotRaidHp.minimumUnbuffed(b));when(b.getJob()).thenReturn(Job.BOWMASTER);assertEquals(7500,BotRaidHp.minimumUnbuffed(b));when(b.getJob()).thenReturn(Job.BISHOP);assertEquals(6000,BotRaidHp.minimumUnbuffed(b));verify(b,never()).getBuffedValue(any());
 }
 @Test void hpPreparationRetainsFourDistinctRingsRatherThanOnlyTheBestCopy()throws Exception{
  var ii=mock(server.ItemInformationProvider.class);when(ii.getWeaponType(anyInt())).thenReturn(WeaponType.NOT_A_WEAPON);when(ii.getEquipStats(anyInt())).thenReturn(Map.of());var rings=new ArrayList<Equip>();for(int n=0;n<5;n++){var r=equip(1112408,0,100-n*10);r.setLuk((short)(20-n));rings.add(r);}var prune=BotEquipManager.class.getDeclaredMethod("pruneDominatedWithReqs",server.ItemInformationProvider.class,List.class,Job.class,boolean[].class);prune.setAccessible(true);
  var kept=(List<?>)prune.invoke(null,ii,rings,Job.NIGHTLORD,new boolean[]{false,false,false,false,true});assertEquals(4,kept.size());assertFalse(kept.contains(rings.get(4)));var ordinary=(List<?>)prune.invoke(null,ii,rings,Job.NIGHTLORD,new boolean[]{false,false,false,false});assertEquals(1,ordinary.size());
 }
}
