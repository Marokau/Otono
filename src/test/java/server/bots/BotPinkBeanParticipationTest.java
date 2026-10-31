package server.bots;
import client.*;
import client.Character;
import constants.skills.Spearman;
import java.awt.*;
import java.util.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotPinkBeanParticipationTest {
 @Test void humanIsExcludedOnlyFromReadinessAndHeadlessLeaderStillCounts(){
  var c=new BotPinkBeanRun.Crew();c.leader=mock(Character.class);var bot=mock(Character.class);c.bots.add(bot);c.human=true;
  assertEquals(List.of(bot),BotPinkBeanParticipation.readyMembers(c));
  assertTrue(BotPinkBeanRun.all(c).contains(c.leader));
  c.human=false;assertTrue(BotPinkBeanParticipation.readyMembers(c).contains(c.leader));
 }
 @Test void aHumanBowmasterDoesNotBecomeTheBotsFormationAnchor(){
  var human=mock(Character.class);var bot=mock(Character.class);var c=new BotPinkBeanRun.Crew();c.leader=human;c.human=true;c.bots.add(bot);
  when(human.isAlive()).thenReturn(true);when(human.getJob()).thenReturn(Job.BOWMASTER);
  when(bot.isAlive()).thenReturn(true);when(bot.getJob()).thenReturn(Job.NIGHTLORD);when(bot.getPartyMembersOnSameMap()).thenReturn(List.of(human,bot));
  try(var pink=mockStatic(BotPinkBeanRun.class)){pink.when(()->BotPinkBeanRun.crew(human)).thenReturn(c);pink.when(()->BotPinkBeanRun.crew(bot)).thenReturn(c);assertSame(bot,BotPinkBeanFormation.anchor(bot));}
 }
 @Test void hyperBodyStillIncludesNearbyHumanWithoutChasingDistantHuman(){
  var dk=mock(Character.class);var human=mock(Character.class);var c=new BotPinkBeanRun.Crew();c.leader=human;c.human=true;c.bots.add(dk);
  var map=mock(server.maps.MapleMap.class);var skill=mock(Skill.class);var fx=mock(server.StatEffect.class);var e=new BotEntry(dk,dk,null);
  when(dk.getMap()).thenReturn(map);when(dk.getMapId()).thenReturn(270050100);when(dk.getJob()).thenReturn(Job.DARKKNIGHT);when(dk.isAlive()).thenReturn(true);when(dk.getPosition()).thenReturn(new Point(200,-42));
  when(dk.getSkillLevel(Spearman.HYPER_BODY)).thenReturn(30);when(dk.getBuffedValue(BuffStat.HYPERBODYHP)).thenReturn(60);when(dk.getBuffedValue(BuffStat.HYPERBODYMP)).thenReturn(60);
  when(human.isAlive()).thenReturn(true);when(human.getBuffedValue(BuffStat.HYPERBODYHP)).thenReturn(null);when(human.getBuffedValue(BuffStat.HYPERBODYMP)).thenReturn(null);when(human.getPosition()).thenReturn(new Point(-800,-42));
  when(dk.getPartyMembersOnSameMap()).thenReturn(List.of(dk,human));when(skill.getEffect(30)).thenReturn(fx);when(fx.calculateBoundingBox(any(),anyBoolean())).thenReturn(new Rectangle(-100,-242,600,400));
  try(var pink=mockStatic(BotPinkBeanRun.class);var skills=mockStatic(SkillFactory.class);var walk=mockStatic(BotPinkBeanMovement.class)){
   pink.when(()->BotPinkBeanRun.crew(human)).thenReturn(c);pink.when(()->BotPinkBeanRun.crew(dk)).thenReturn(c);pink.when(()->BotPinkBeanRun.inInstance(dk)).thenReturn(true);skills.when(()->SkillFactory.getSkill(Spearman.HYPER_BODY)).thenReturn(skill);
   assertFalse(BotHorntailSupport.tick(e,dk,List.of(dk),Set.of("prepared-270050100"),human,"Pink Bean",true));walk.verifyNoInteractions();assertNull(BotHorntailSupport.criticalSkill(dk));
   when(human.getPosition()).thenReturn(new Point(250,-42));assertSame(skill,BotHorntailSupport.criticalSkill(dk));
  }
 }
}
