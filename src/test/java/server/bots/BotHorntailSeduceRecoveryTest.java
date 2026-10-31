package server.bots;
import client.*;import client.Character;import constants.skills.Bowmaster;import java.util.Map;import org.junit.jupiter.api.Test;import server.StatEffect;import server.life.MobSkill;import tools.Pair;import static org.mockito.Mockito.*;import static org.junit.jupiter.api.Assertions.*;
class BotHorntailSeduceRecoveryTest {
 @org.junit.jupiter.params.ParameterizedTest @org.junit.jupiter.params.provider.ValueSource(booleans={false,true}) void nativeWillReactsWithinOneSecondThenAllowsNormalPotionRecovery(boolean pinkRaid){
  var b=mock(Character.class);var disease=mock(MobSkill.class);var skill=mock(Skill.class);var effect=mock(StatEffect.class);var e=new BotEntry(b,b,null);e.climbing=true;
  when(b.getJob()).thenReturn(Job.BOWMASTER);when(disease.getDuration()).thenReturn(10000L);when(b.getAllDiseases()).thenReturn(Map.of(Disease.SEDUCE,new Pair<>(9600L,disease)));when(b.getSkillLevel(Bowmaster.HEROS_WILL)).thenReturn(5);when(skill.getEffect(5)).thenReturn(effect);when(effect.canPaySkillCost(b)).thenReturn(true);
  try(var pink=mockStatic(BotPinkBeanRun.class);var horn=mockStatic(BotHorntailRun.class);var skills=mockStatic(SkillFactory.class);var cast=mockStatic(BotSupportCast.class);var physics=mockStatic(BotPhysicsEngine.class);var movement=mockStatic(BotMovementManager.class);var hurricane=mockStatic(BotHurricane.class)){
   horn.when(()->BotHorntailRun.inInstance(b)).thenReturn(!pinkRaid);pink.when(()->BotPinkBeanRun.inInstance(b)).thenReturn(pinkRaid);skills.when(()->SkillFactory.getSkill(Bowmaster.HEROS_WILL)).thenReturn(skill);
   BotHorntailSeduce.tick(e,b);cast.verifyNoInteractions();
   when(b.getAllDiseases()).thenReturn(Map.of(Disease.SEDUCE,new Pair<>(9500L,disease)));cast.when(()->BotSupportCast.dispatch(b,skill,5)).thenReturn(true);
   BotHorntailSeduce.tick(e,b);cast.verify(()->BotSupportCast.dispatch(b,skill,5));verify(b).triggerAutopotAfterStatLoss(-1,-1);assertTrue(e.attackCooldownMs>=600);
  }
 }
 @Test void nativeCostsAndCooldownStillPreventWill(){
  var b=mock(Character.class);var disease=mock(MobSkill.class);var skill=mock(Skill.class);var effect=mock(StatEffect.class);var e=new BotEntry(b,b,null);e.climbing=true;
  when(b.getJob()).thenReturn(Job.BOWMASTER);when(disease.getDuration()).thenReturn(10000L);when(b.getAllDiseases()).thenReturn(Map.of(Disease.SEDUCE,new Pair<>(6000L,disease)));when(b.getSkillLevel(Bowmaster.HEROS_WILL)).thenReturn(5);when(skill.getEffect(5)).thenReturn(effect);
  try(var horn=mockStatic(BotHorntailRun.class);var skills=mockStatic(SkillFactory.class);var cast=mockStatic(BotSupportCast.class);var physics=mockStatic(BotPhysicsEngine.class);var movement=mockStatic(BotMovementManager.class);var hurricane=mockStatic(BotHurricane.class)){
   horn.when(()->BotHorntailRun.inInstance(b)).thenReturn(true);skills.when(()->SkillFactory.getSkill(Bowmaster.HEROS_WILL)).thenReturn(skill);
   BotHorntailSeduce.tick(e,b);cast.verifyNoInteractions();when(effect.canPaySkillCost(b)).thenReturn(true);when(b.skillIsCooling(Bowmaster.HEROS_WILL)).thenReturn(true);BotHorntailSeduce.tick(e,b);cast.verifyNoInteractions();
  }
 }
}
