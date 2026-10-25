package server.bots;
import client.Character;
import client.Client;
import client.BotClient;
import config.YamlConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotGuildCompanionsTest {
 private Character leader() {
  var b=mock(Character.class);when(b.getClient()).thenReturn(mock(Client.class));when(b.getMapId()).thenReturn(BotGuildCompanions.RECRUIT);return b;
 }
 @Test void ordinaryHumanCannotAssembleWithoutGuildLeadership(){
  assertEquals("You must be your guild's master or junior master to register GPQ.",BotGuildCompanions.eligibility(leader(),false,false));
 }
 @Test void missingFeeIsExplainedBeforeAnyCrewOrWarp(){
  var b=leader();when(b.getMeso()).thenReturn(YamlConfig.config.server.CREATE_GUILD_COST-1);
  assertTrue(BotGuildCompanions.eligibility(b,true,false).contains("mesos"));
 }
 @Test void fundedGuildlessHumanCanUseNormalFounderFlow(){
  var b=leader();when(b.getMeso()).thenReturn(YamlConfig.config.server.CREATE_GUILD_COST);
  assertNull(BotGuildCompanions.eligibility(b,true,false));
 }
 @Test void regularPopulationBotCannotRecruitGPqFixtures(){
  var b=leader();when(b.getClient()).thenReturn(new BotClient(0,1));
  assertNotNull(BotGuildCompanions.eligibility(b,true,false));
 }
 @Test void existingGuildCannotBeReplacedByFounderFlow(){
  var b=leader();when(b.getGuildId()).thenReturn(9);
  assertTrue(BotGuildCompanions.eligibility(b,true,false).contains("already have a guild"));
 }
 @Test void wrongMapAndActiveInstanceAreRejected(){
  var b=leader();when(b.getMapId()).thenReturn(100000000);assertNotNull(BotGuildCompanions.eligibility(b,true,false));
  when(b.getMapId()).thenReturn(BotGuildCompanions.RECRUIT);
  when(b.getEventInstance()).thenReturn(mock(scripting.event.EventInstanceManager.class));assertNotNull(BotGuildCompanions.eligibility(b,true,false));
 }
 @Test void requirementsUseActualConfiguredFeeAndFounderCount(){
  assertTrue(BotGuildCompanions.requirements().contains(Integer.toString(YamlConfig.config.server.CREATE_GUILD_COST)));
  assertTrue(BotGuildCompanions.requirements().contains("level-30-and-under"));
 }
 @Test void unrelatedBotTickIsUntouched(){
  var b=leader();assertFalse(BotGuildCompanions.tick(new BotEntry(b,b,null),b,true));
 }
}
