package server.bots;
import client.Character;
import client.Job;
import client.inventory.Equip;
import client.inventory.InventoryType;
import constants.skills.Bowmaster;
import org.junit.jupiter.api.Test;
import server.ItemInformationProvider;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotHorntailEquipmentTest {
 @org.junit.jupiter.api.BeforeAll static void itemDataWithoutDatabase() throws Exception {
  var connection=mock(java.sql.Connection.class);var query=mock(java.sql.PreparedStatement.class);when(connection.prepareStatement(anyString())).thenReturn(query);when(query.executeQuery()).thenReturn(mock(java.sql.ResultSet.class));
  try(var database=mockStatic(tools.DatabaseConnection.class)){database.when(tools.DatabaseConnection::getConnection).thenReturn(connection);Class.forName("server.ItemInformationProvider");}
 }

 @Test void weaponRequirementsAreFundedBeforeProvisioningRatherThanFilteringOutTheUpgrade(){
  var b=mock(Character.class,RETURNS_DEEP_STUBS);when(b.getJob()).thenReturn(Job.NIGHTLORD);when(b.getLevel()).thenReturn(180);
  when(b.getStr()).thenReturn(4);when(b.getDex()).thenReturn(25);when(b.getInt()).thenReturn(4);when(b.getLuk()).thenReturn(900);
  when(b.getTotalStr()).thenReturn(25);when(b.getTotalDex()).thenReturn(58);when(b.getTotalInt()).thenReturn(8);when(b.getTotalLuk()).thenReturn(930);when(b.getTotalWatk()).thenReturn(100);
  var ii=ItemInformationProvider.getInstance();var old=(Equip)ii.getEquipById(1472086);var goal=(Equip)ii.getEquipById(1472068);old.setDex((short)10); // Its own DEX cannot fund the next weapon.
  when(b.getInventory(InventoryType.EQUIPPED).getItem((short)-11)).thenReturn(old);var e=new BotEntry(b,b,null);var current=new BotBuildManager.ApBuild(BotBuildManager.StatType.LUK,BotBuildManager.StatType.DEX,25);
  assertFalse(ii.meetsEquipRequirements(goal,b.getJob(),180,25,48,8,930,0));
  try(var builds=mockStatic(BotBuildManager.class)){builds.when(()->BotBuildManager.resolveApBuild(e,b)).thenReturn(current);var result=BotHorntailEquipment.buildForWeapons(e,b,List.of(old,goal));assertEquals(137,result.secondaryTarget);
   var armor=(Equip)ii.getEquipById(1052158);armor.setDex((short)20);
   when(b.getInventory(InventoryType.EQUIPPED).list()).thenReturn(List.of(old,armor));
   assertEquals(157,BotHorntailEquipment.buildForWeapons(e,b,List.of(old,goal)).secondaryTarget,"Keep HP armor wearable without borrowing its own DEX");
   when(b.getInventory(InventoryType.EQUIPPED).list()).thenReturn(List.of(old));when(b.getLevel()).thenReturn(100);assertEquals(25,BotHorntailEquipment.buildForWeapons(e,b,List.of(goal)).secondaryTarget);}
 }
 @Test void bowPlanningUsesTheRaidHurricaneCadenceRatherThanNormalBowSpeed(){
  var b=mock(Character.class);when(b.getName()).thenReturn("HornArcher1");when(b.getId()).thenReturn(700);when(b.getSkillLevel(Bowmaster.HURRICANE)).thenReturn(30);var e=new BotEntry(b,b,null);e.spVariant=BotHorntailRaidBuild.VARIANT;var m=mock(BotManager.class);when(m.getEntryByBotCharId(700)).thenReturn(e);
  try(var managers=mockStatic(BotManager.class)){managers.when(BotManager::getInstance).thenReturn(m);assertEquals(BotHurricane.SHOT_MS,BotHorntailEquipment.attackCycle(b,1452045));assertEquals(BotHurricane.SHOT_MS,BotHorntailEquipment.attackCycle(b,1452057));e.spVariant=null;assertEquals(BotEquipManager.weaponCycleMs(1452057),BotHorntailEquipment.attackCycle(b,1452057));}
 }
}
