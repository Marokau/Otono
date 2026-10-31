package server.bots;
import client.Character;import client.*;import java.awt.*;import java.util.List;import java.util.Set;import org.junit.jupiter.api.Test;import server.StatEffect;import server.life.Monster;import server.maps.MapleMap;import static org.mockito.Mockito.*;import static org.junit.jupiter.api.Assertions.*;
class BotHorntailCohesionTest {
 @Test void bishopFollowsAFightingPartyMemberToASafeHealStanceBeforeAnEmergency(){
  var b=mock(Character.class);var archer=mock(Character.class);var map=mock(MapleMap.class);var target=mock(Monster.class);var manager=mock(BotManager.class);var skill=mock(Skill.class);var heal=mock(StatEffect.class);var e=new BotEntry(b,b,null);var fighter=new BotEntry(archer,b,null);fighter.grindTarget=target;
  when(b.getMap()).thenReturn(map);when(b.getMapId()).thenReturn(240060200);when(b.getJob()).thenReturn(Job.BISHOP);when(b.getBuffedValue(BuffStat.MAGIC_GUARD)).thenReturn(80);when(b.getSkillLevel(constants.skills.Cleric.HEAL)).thenReturn(30);when(b.getPosition()).thenReturn(new Point(-500,260));when(b.getPartyMembersOnSameMap()).thenReturn(List.of(archer));when(archer.isAlive()).thenReturn(true);when(archer.getJob()).thenReturn(Job.BOWMASTER);when(archer.getId()).thenReturn(7);when(archer.getPosition()).thenReturn(new Point(440,-220));when(target.isAlive()).thenReturn(true);when(manager.getEntryByBotCharId(7)).thenReturn(fighter);when(skill.getEffect(30)).thenReturn(heal);when(heal.calculateBoundingBox(any(Point.class),anyBoolean())).thenReturn(new Rectangle(-900,-40,800,600));var stance=new Point(440,-160);
  try(var horn=mockStatic(BotHorntailRun.class);var skills=mockStatic(SkillFactory.class);var managers=mockStatic(BotManager.class);var reviving=mockStatic(BotHorntailRevive.class);var walking=mockStatic(BotPinkBeanMovement.class)){
   horn.when(()->BotHorntailRun.inInstance(b)).thenReturn(true);skills.when(()->SkillFactory.getSkill(constants.skills.Cleric.HEAL)).thenReturn(skill);managers.when(BotManager::getInstance).thenReturn(manager);reviving.when(()->BotHorntailRevive.stance(e,b,archer.getPosition(),heal)).thenReturn(stance);
   assertTrue(BotHorntailSupport.tick(e,b,List.of(b,archer),Set.of("prepared-240060200"),b,"Horntail",true));walking.verify(()->BotPinkBeanMovement.walkTo(e,b,stance,true));
  }
 }
 @Test void supportDoesNotReplaceAnActiveRopeApproachWithANewStance(){
  var b=mock(Character.class);var e=new BotEntry(b,b,null);var map=mock(MapleMap.class);var mob=mock(Monster.class);when(b.getMap()).thenReturn(map);when(map.getAllMonsters()).thenReturn(List.of(mob));when(mob.isAlive()).thenReturn(true);when(b.getMapId()).thenReturn(240060200);when(b.getJob()).thenReturn(Job.BISHOP);when(b.getBuffedValue(BuffStat.MAGIC_GUARD)).thenReturn(80);e.climbing=true;e.moveTarget=new Point(508,20);
  try(var horn=mockStatic(BotHorntailRun.class);var walking=mockStatic(BotPinkBeanMovement.class);var stances=mockStatic(BotHorntailRevive.class)){
   horn.when(()->BotHorntailRun.inInstance(b)).thenReturn(true);assertTrue(BotHorntailSupport.tick(e,b,List.of(b),Set.of("prepared-240060200"),b,"Horntail",true));walking.verify(()->BotPinkBeanMovement.walkTo(e,b,new Point(508,20),true));stances.verifyNoInteractions();
  }
 }
 @Test void crystalJumpCanReachItsNativeAttackInsteadOfBeingCapturedByCombatHandoff(){
  var b=mock(Character.class);var map=mock(MapleMap.class);var e=new BotEntry(b,b,null);e.inAir=true;e.moveTarget=new Point(577,20);when(b.getMap()).thenReturn(map);when(b.getMapId()).thenReturn(240060200);when(b.getJob()).thenReturn(Job.NIGHTLORD);when(map.getAllMonsters()).thenReturn(List.of());when(b.getPartyMembersOnSameMap()).thenReturn(List.of());
  try(var horn=mockStatic(BotHorntailRun.class);var walking=mockStatic(BotPinkBeanMovement.class)){
   horn.when(()->BotHorntailRun.inInstance(b)).thenReturn(true);assertFalse(BotHorntailSupport.tick(e,b,List.of(b),Set.of("prepared-240060200"),b,"Horntail",true));walking.verifyNoInteractions();
  }
 }
}
