package server.bots;
import client.Character;import client.*;import java.awt.*;import java.util.List;import org.junit.jupiter.api.Test;import server.StatEffect;import server.life.Monster;import static org.mockito.Mockito.*;import static org.junit.jupiter.api.Assertions.*;
class BotHorntailHealingAnchorTest {
 @Test void wingPlatformCannotPullBishopOutOfNativeHealRangeOfHisAnchor(){
  var b=mock(Character.class);var archer=mock(Character.class);var manager=mock(BotManager.class);var mob=mock(Monster.class);var fighter=new BotEntry(archer,b,null);fighter.grindTarget=mob;var skill=mock(Skill.class);var heal=mock(StatEffect.class);when(b.getJob()).thenReturn(Job.BISHOP);when(b.getSkillLevel(constants.skills.Cleric.HEAL)).thenReturn(30);when(b.getPartyMembersOnSameMap()).thenReturn(List.of(archer));when(archer.isAlive()).thenReturn(true);when(archer.getJob()).thenReturn(Job.BOWMASTER);when(archer.getId()).thenReturn(7);when(archer.getPosition()).thenReturn(new Point(-475,-160));when(mob.isAlive()).thenReturn(true);when(manager.getEntryByBotCharId(7)).thenReturn(fighter);when(skill.getEffect(30)).thenReturn(heal);when(heal.calculateBoundingBox(any(Point.class),anyBoolean())).thenAnswer(c->{Point p=c.getArgument(0);return new Rectangle(p.x-300,p.y-200,600,400);});
  try(var horn=mockStatic(BotHorntailRun.class);var managers=mockStatic(BotManager.class);var skills=mockStatic(SkillFactory.class)){
   horn.when(()->BotHorntailRun.inInstance(b)).thenReturn(true);managers.when(BotManager::getInstance).thenReturn(manager);skills.when(()->SkillFactory.getSkill(constants.skills.Cleric.HEAL)).thenReturn(skill);
   assertSame(archer,BotHorntailHealingAnchor.find(b));assertFalse(BotHorntailHealingAnchor.covers(b,new Point(-302,80)));assertTrue(BotHorntailHealingAnchor.covers(b,new Point(-357,20)));when(mob.isAlive()).thenReturn(false);assertTrue(BotHorntailHealingAnchor.covers(b,new Point(-302,80)));
  }
 }
}
