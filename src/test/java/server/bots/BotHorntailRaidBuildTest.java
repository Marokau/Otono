package server.bots;
import client.Character;
import client.Job;
import client.SkillFactory;
import constants.skills.DarkKnight;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotHorntailRaidBuildTest {
 @org.junit.jupiter.api.BeforeAll static void loadSkills(){SkillFactory.loadAllSkills();}
 @Test void raidAccuracyFloorBuysDexForReliableHitsWithoutChangingTheHitFormula(){
  var b=mock(Character.class);when(b.getLevel()).thenReturn(180);when(b.getDex()).thenReturn(4);when(b.getTotalDex()).thenReturn(39);when(b.getTotalLuk()).thenReturn(24);var e=new BotEntry(b,b,null);e.aspirationalMobAvoid=45;e.aspirationalMobLevel=160;e.kpq.recruiting=true;e.spVariant=BotHorntailRaidBuild.VARIANT;
  var f=spy(server.combat.CombatFormulaProvider.getInstance());doReturn(99).when(f).getTotalAccuracy(b);
  try(var formulas=mockStatic(server.combat.CombatFormulaProvider.class)){
   formulas.when(server.combat.CombatFormulaProvider::getInstance).thenReturn(f);int raid=BotBuildManager.accuracyDexFloor(e,b);int accuracy=(int)Math.floor((raid+35)*0.8+24*0.5)+56;assertEquals(1.0,f.calculatePhysicalMobHitChance(accuracy,180,160,45));when(b.getBuffedValue(client.BuffStat.ACC)).thenReturn(20);doReturn(119).when(f).getTotalAccuracy(b);assertEquals(raid,BotBuildManager.accuracyDexFloor(e,b));e.spVariant=null;assertTrue(BotBuildManager.accuracyDexFloor(e,b)<raid);
  }
 }

 @Test void coreRaidSkillsFitTheEarnedLevel180FourthJobBudget(){
  for(var job:java.util.List.of(Job.NIGHTLORD,Job.BOWMASTER,Job.BISHOP,Job.DARKKNIGHT,Job.HERO)){
   var order=BotHorntailRaidBuild.order(job);int core=job==Job.BISHOP?7:6;
   assertTrue(order.subList(0,core).stream().mapToInt(s->s.targetLevel()).sum()<=183,job.toString());
   for(var step:order){assertEquals(SkillFactory.getSkill(step.skillId()).getMaxLevel(),step.targetLevel());assertTrue(constants.game.GameConstants.isInJobTree(step.skillId(),job.getId()));}
  }
  assertNull(BotHorntailRaidBuild.order(Job.SPEARMAN));
 }
 @Test void raidPrioritiesAreOptInAndDoNotReplacePopulationBuilds(){
  var b=mock(Character.class);when(b.getJob()).thenReturn(Job.DARKKNIGHT);var e=new BotEntry(b,b,null);
  assertEquals(10,BotBuildManager.plannedSkillTarget(e,b,DarkKnight.MAPLE_WARRIOR));e.spVariant=BotHorntailRaidBuild.VARIANT;
  assertEquals(30,BotBuildManager.plannedSkillTarget(e,b,DarkKnight.MAPLE_WARRIOR));
 }
}
