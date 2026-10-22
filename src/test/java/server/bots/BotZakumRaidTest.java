package server.bots;
import client.Character;
import client.BotClient;
import net.server.world.Party;
import net.server.world.PartyCharacter;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class BotZakumRaidTest {
 @Test void dormantBodyNeverAnnouncesBodyPhaseWhileAnyArmLives() {
  try(var c=new Crew(5);var guidance=mockStatic(BotPqStageGuide.class);var cures=mockStatic(BotLpqCures.class);var hold=mockStatic(BotOpqRun.class)) {
   var event=mock(scripting.event.EventInstanceManager.class);var map=mock(server.maps.MapleMap.class);
   var bot=c.bots.get(0);var entry=c.manager.getEntryByBotCharId(bot.getId());
   when(bot.getMapId()).thenReturn(BotZakumBattle.ALTAR);when(bot.getMap()).thenReturn(map);when(bot.getEventInstance()).thenReturn(event);
   when(c.human.getMapId()).thenReturn(BotZakumBattle.ALTAR);when(c.human.getEventInstance()).thenReturn(event);
   var body=mock(server.life.Monster.class);when(body.getId()).thenReturn(8800000);when(body.isAlive()).thenReturn(true);when(body.isFake()).thenReturn(true);
   var arm=mock(server.life.Monster.class);when(arm.getId()).thenReturn(8800004);when(arm.isAlive()).thenReturn(true);
   cures.when(()->BotLpqCures.tryCure(bot)).thenReturn(true);
   when(map.getAllMonsters()).thenReturn(List.of(body,arm));BotZakumBattle.tick(entry,bot,true);
   when(map.getAllMonsters()).thenReturn(List.of(arm,body));BotZakumBattle.tick(entry,bot,true);
   guidance.verify(()->BotPqStageGuide.tick(eq(entry),eq(bot),eq(c.human),eq(event),eq("zakum-arms"),anyLong()),times(2));
   guidance.verify(()->BotPqStageGuide.tick(eq(entry),eq(bot),eq(c.human),eq(event),eq("zakum-body-0"),anyLong()),never());
   when(arm.isAlive()).thenReturn(false);BotZakumBattle.tick(entry,bot,true);
   guidance.verify(()->BotPqStageGuide.tick(eq(entry),eq(bot),eq(c.human),eq(event),eq("zakum-body-0"),anyLong()),never());
   when(body.isFake()).thenReturn(false);BotZakumBattle.tick(entry,bot,true);
   guidance.verify(()->BotPqStageGuide.tick(eq(entry),eq(bot),eq(c.human),eq(event),eq("zakum-body-0"),anyLong()));
  }
 }

 static class Crew implements AutoCloseable {
  final Character human=mock(Character.class,RETURNS_DEEP_STUBS);
  final List<Character> bots=new ArrayList<>();
  final Party first=mock(Party.class),second=mock(Party.class);
  final List<PartyCharacter> firstMembers=new ArrayList<>(),secondMembers=new ArrayList<>();
  final BotManager manager=mock(BotManager.class);
  final org.mockito.MockedStatic<BotManager> managers=mockStatic(BotManager.class);
  final org.mockito.MockedStatic<Party> parties=mockStatic(Party.class);
  Crew(int count) {
   when(human.getEventInstance()).thenReturn(null);when(human.getId()).thenReturn(9000);when(human.getParty()).thenReturn(first);when(human.getMapId()).thenReturn(BotZakumBattle.RECRUIT);
   when(human.getWorldServer().getPlayerStorage().getCharacterById(9000)).thenReturn(human);
   when(first.getId()).thenReturn(100);when(first.getLeaderId()).thenReturn(9000);when(first.getMembers()).thenReturn(firstMembers);
   when(second.getId()).thenReturn(200);when(second.getLeaderId()).thenReturn(9006);when(second.getMembers()).thenReturn(secondMembers);
   firstMembers.add(mock(PartyCharacter.class));
   for(int i=0;i<count;i++) {
    var b=mock(Character.class);when(b.getId()).thenReturn(9001+i);when(b.getName()).thenReturn("ZakumFixture"+i);
    when(b.getClient()).thenReturn(mock(BotClient.class,RETURNS_DEEP_STUBS));when(b.getMapId()).thenReturn(BotZakumBattle.RECRUIT);when(b.getLevel()).thenReturn(150);
    bots.add(b);var entry=new BotEntry(b,human,null);when(manager.getEntryByBotCharId(9001+i)).thenReturn(entry);
   }
   managers.when(BotManager::getInstance).thenReturn(manager);
   doAnswer(a->{Character leader=a.getArgument(0),bot=a.getArgument(1);Party group=leader==human?first:second;
    var members=leader==human?firstMembers:secondMembers;assertTrue(members.size()<6);members.add(mock(PartyCharacter.class));when(bot.getParty()).thenReturn(group);return null;}).when(manager).partyUp(any(),any());
   if(count==11)parties.when(()->Party.createParty(bots.get(5),true)).thenAnswer(a->{when(bots.get(5).getParty()).thenReturn(second);secondMembers.add(mock(PartyCharacter.class));return true;});
   var all=new ArrayList<Character>();all.add(human);all.addAll(bots);when(human.getWorldServer().getPlayerStorage().getAllCharacters()).thenReturn(all);
   BotZakumBattle.start(human,bots,true);
  }
  public void close(){for(var b:bots)BotZakumBattle.unregister(b);parties.close();managers.close();}
 }
 @Test void elevenBotsFormTwoLegalPartiesAndSecondPartyStaysAttached() {
  try(var c=new Crew(11);var hold=mockStatic(BotOpqRun.class)) {
   assertEquals(6,c.firstMembers.size());assertEquals(6,c.secondMembers.size());
   for(int i=0;i<11;i++){assertEquals(i<5?100:200,c.bots.get(i).getParty().getId());assertTrue(BotZakumBattle.belongsTo(c.bots.get(i),9000));}
   hold.when(()->BotOpqRun.hold(any(),any(),anyBoolean())).thenReturn(true);
   var bot=c.bots.get(6);assertTrue(BotZakumBattle.tick(c.manager.getEntryByBotCharId(bot.getId()),bot,true));
   verify(c.manager,never()).applyOperatorCommand(any(),any(),anyInt(),anyInt());
  }
 }
 @Test void legacySixPersonCrewStillFormsOneParty() {
  try(var c=new Crew(5)){assertEquals(6,c.firstMembers.size());assertTrue(c.secondMembers.isEmpty());}
 }
 @Test void restockAndDismissFindBothParties() {
  try(var c=new Crew(11);var supplies=mockStatic(BotZakumSupplies.class,CALLS_REAL_METHODS);var companions=mockStatic(BotZakumCompanions.class,CALLS_REAL_METHODS)) {
   supplies.when(()->BotZakumSupplies.restock(any(),any())).thenAnswer(a->null);
   companions.when(()->BotZakumCompanions.dismiss(any())).thenAnswer(a->null);
   BotZakumSupplies.refresh(c.human);BotZakumCompanions.release(c.human);
   for(var b:c.bots){supplies.verify(()->BotZakumSupplies.restock(c.manager.getEntryByBotCharId(b.getId()),b));companions.verify(()->BotZakumCompanions.dismiss(b));}
  }
 }

 @Test void bishopResurrectionOnlyTargetsItsOwnParty() {
  var bishop=mock(Character.class);var dead=mock(Character.class);var first=mock(Party.class);var second=mock(Party.class);
  when(first.getId()).thenReturn(1);when(second.getId()).thenReturn(2);when(bishop.getParty()).thenReturn(first);when(dead.getParty()).thenReturn(second);
  var map=mock(server.maps.MapleMap.class);when(bishop.getMap()).thenReturn(map);when(dead.getMap()).thenReturn(map);when(dead.getHp()).thenReturn(0);
  when(bishop.getSkillLevel(constants.skills.Bishop.RESURRECTION)).thenReturn(1);when(bishop.getPosition()).thenReturn(new java.awt.Point());when(dead.getPosition()).thenReturn(new java.awt.Point(200,0));
  var skill=mock(client.Skill.class);var effect=mock(server.StatEffect.class);when(skill.getEffect(1)).thenReturn(effect);when(effect.canPaySkillCost(bishop)).thenReturn(true);
  when(effect.calculateBoundingBox(any(),anyBoolean())).thenReturn(new java.awt.Rectangle(-10,-10,20,20));
  try(var skills=mockStatic(client.SkillFactory.class);var movement=mockStatic(BotZakumPqRun.class)) {
   skills.when(()->client.SkillFactory.getSkill(constants.skills.Bishop.RESURRECTION)).thenReturn(skill);
   var entry=new BotEntry(bishop,null,null);
   assertFalse(BotZakumSupport.resurrect(entry,bishop,List.of(dead),true));movement.verifyNoInteractions();
   when(dead.getParty()).thenReturn(first);assertTrue(BotZakumSupport.resurrect(entry,bishop,List.of(dead),true));
   movement.verify(()->BotZakumPqRun.walkTo(entry,bishop,new java.awt.Point(200,0),true));
  }
 }
}

