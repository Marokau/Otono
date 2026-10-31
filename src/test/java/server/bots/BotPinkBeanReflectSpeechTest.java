package server.bots;
import client.status.*;
import server.life.Monster;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotPinkBeanReflectSpeechTest {
 private MonsterStatusEffect effect(boolean physical,boolean magic){
  var values=new java.util.HashMap<MonsterStatus,Integer>();
  if(physical)values.put(MonsterStatus.WEAPON_REFLECT,10);
  if(magic)values.put(MonsterStatus.MAGIC_REFLECT,10);
  return new MonsterStatusEffect(values,null,null,true);
 }
 @Test void announcesOnlyActualReflectAndNamesItsType(){
  for(int mask=1;mask<=3;mask++){
   var mob=mock(Monster.class);assertNull(BotPinkBeanReflectSpeech.activated(mob));
   var buff=effect((mask&1)!=0,(mask&2)!=0);
   when(mob.getStati(MonsterStatus.WEAPON_REFLECT)).thenReturn((mask&1)!=0?buff:null);
   when(mob.getStati(MonsterStatus.MAGIC_REFLECT)).thenReturn((mask&2)!=0?buff:null);
   String text=BotPinkBeanReflectSpeech.activated(mob);
   assertTrue(text.startsWith(mask==3?"Both reflects":mask==1?"Physical reflect":"Magic reflect"));
   for(int bot=0;bot<30;bot++)assertNull(BotPinkBeanReflectSpeech.activated(mob));
   when(mob.getStati(MonsterStatus.WEAPON_REFLECT)).thenReturn(null);
   when(mob.getStati(MonsterStatus.MAGIC_REFLECT)).thenReturn(null);
   assertNull(BotPinkBeanReflectSpeech.activated(mob));
  }
 }
 @Test void aNewApplicationOfTheSameReflectIsAnnouncedAgain(){
  var mob=mock(Monster.class);
  when(mob.getStati(MonsterStatus.WEAPON_REFLECT)).thenReturn(effect(true,false));
  assertNotNull(BotPinkBeanReflectSpeech.activated(mob));
  when(mob.getStati(MonsterStatus.WEAPON_REFLECT)).thenReturn(effect(true,false));
  assertNotNull(BotPinkBeanReflectSpeech.activated(mob));
 }
 @Test void combinedApplicationIsOneMessageEvenWhileTheStatusMapIsBeingFilled(){
  var mob=mock(Monster.class);var buff=effect(true,true);
  when(mob.getStati(MonsterStatus.WEAPON_REFLECT)).thenReturn(buff);
  assertTrue(BotPinkBeanReflectSpeech.activated(mob).startsWith("Both reflects"));
  when(mob.getStati(MonsterStatus.MAGIC_REFLECT)).thenReturn(buff);
  assertNull(BotPinkBeanReflectSpeech.activated(mob));
 }
}
