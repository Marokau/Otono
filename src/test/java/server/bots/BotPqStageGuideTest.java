package server.bots;
import client.Character;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotPqStageGuideTest {
 @Test void correctedArmDetectionRearmsAnEarlierPrematureBodyAnnouncement() {
  var human=mock(Character.class);var bot=mock(Character.class);Object event=new Object();
  when(human.getMapId()).thenReturn(BotZakumBattle.ALTAR);when(bot.getMapId()).thenReturn(BotZakumBattle.ALTAR);
  var entry=new BotEntry(bot,human,null);
  try(var chat=mockStatic(BotPqChat.class)) {
   BotPqStageGuide.tick(entry,bot,human,event,"zakum-body-0",1000);
   BotPqStageGuide.tick(entry,bot,human,event,"zakum-arms",2000);
   BotPqStageGuide.tick(entry,bot,human,event,"zakum-body-0",3000);
   chat.verify(()->BotPqChat.queue(entry,BotPqStageGuide.text("zakum-body-0",BotZakumBattle.ALTAR),false),times(2));
  }
 }

 @Test void everyImplementedRoomHasAnAsciiObjectiveAndPlayerAction() {
  int[] opq={920010000,920010100,920010200,920010300,920010400,920010500,920010600,920010601,920010602,920010603,920010604,920010700,920010800,920011200,920011300};
  for(int map:opq)check("opq",map);
  for(int base:new int[]{926100000,926110000})for(int room:new int[]{0,1,100,200,201,202,203,300,301,302,303,304,400,401,500,600})check("mpq",base+room);
  for(int map=BotLmpqRun.FIRST;map<=BotLmpqRun.REWARD;map++)check("lmpq",map);
  for(String activity:List.of("cpq-fight","cpq-recovery","cpq-rewards","zakum","zakum-arms","zakum-body-0","zakum-body-1","zakum-body-2"))check(activity,280030000);
  assertNull(BotPqStageGuide.text("kpq",103000800));assertNull(BotPqStageGuide.text("lpq",922010100));
 }
 void check(String activity,int map) {
  String text=BotPqStageGuide.text(activity,map);assertNotNull(text,activity+":"+map);
  assertTrue(text.chars().allMatch(c->c<128));assertTrue(text.contains(":"));
  assertTrue(java.lang.Character.isUpperCase(text.charAt(text.indexOf(':')+2)),text);
 }
 @Test void allCompanionsPauseOnceAndANewInstanceGetsItsOwnBriefing() {
  var human=mock(Character.class);var a=mock(Character.class);var b=mock(Character.class);
  when(human.getMapId()).thenReturn(920010200);when(a.getMapId()).thenReturn(920010200);when(b.getMapId()).thenReturn(920010200);
  var ea=new BotEntry(a,human,null);var eb=new BotEntry(b,human,null);Object event=new Object();
  try(var chat=mockStatic(BotPqChat.class)) {
   assertTrue(BotPqStageGuide.tick(ea,a,human,event,"opq",1000));
   assertTrue(BotPqStageGuide.tick(eb,b,human,event,"opq",7999));
   assertFalse(BotPqStageGuide.tick(ea,a,human,event,"opq",8000));
   chat.verify(()->BotPqChat.queue(ea,BotPqStageGuide.text("opq",920010200),false),times(1));
   assertTrue(BotPqStageGuide.tick(ea,a,human,new Object(),"opq",9000));
  }
 }
 @Test void queuedSpeechExtendsTheReadingPauseAndAbsentHumansDoNotConsumeIt() {
  var human=mock(Character.class);var b=mock(Character.class);when(b.getMapId()).thenReturn(920010500);
  var e=new BotEntry(b,human,null);e.msgSending=true;Object event=new Object();
  try(var chat=mockStatic(BotPqChat.class)) {
   assertFalse(BotPqStageGuide.tick(e,b,human,event,"opq",1000));chat.verifyNoInteractions();
   when(human.getMapId()).thenReturn(920010500);
   assertTrue(BotPqStageGuide.tick(e,b,human,event,"opq",1000));
   assertTrue(BotPqStageGuide.tick(e,b,human,event,"opq",13199));
   assertFalse(BotPqStageGuide.tick(e,b,human,event,"opq",13200));
  }
 }
 @Test void carnivalOpponentsWaitButDoNotExplainThePlayersObjectives() {
  var human=mock(Character.class);var opponent=mock(Character.class);var ally=mock(Character.class);
  var party=mock(net.server.world.Party.class);when(party.getId()).thenReturn(1);when(human.getParty()).thenReturn(party);when(ally.getParty()).thenReturn(party);
  var enemyParty=mock(net.server.world.Party.class);when(enemyParty.getId()).thenReturn(2);when(opponent.getParty()).thenReturn(enemyParty);
  var eo=new BotEntry(opponent,human,null);var ea=new BotEntry(ally,human,null);var crew=new Object();
  try(var chat=mockStatic(BotPqChat.class)) {
   assertTrue(BotPqStageGuide.tick(eo,opponent,human,crew,"cpq-fight",1000));chat.verifyNoInteractions();
   assertTrue(BotPqStageGuide.tick(ea,ally,human,crew,"cpq-fight",1000));
   assertFalse(BotPqStageGuide.tick(eo,opponent,human,crew,"cpq-fight",8000));
   BotPqStageGuide.reset(crew);assertTrue(BotPqStageGuide.tick(ea,ally,human,crew,"cpq-fight",9000));
  }
 }
}
