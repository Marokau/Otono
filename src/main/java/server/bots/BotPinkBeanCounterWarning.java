package server.bots;
import client.Character;
import server.life.*;
import java.util.*;
/** Stop for a received reflection-cast warning before the native buff takes effect. */
final class BotPinkBeanCounterWarning {
 record Warning(boolean physical,boolean magic,long began,long until) {}
 private static final Map<Monster,Warning> WARNINGS=Collections.synchronizedMap(new WeakHashMap<>());
 private BotPinkBeanCounterWarning() {}
 static void observe(Character b,Monster mob,int id,int level){
  var type=MobSkillType.from(id).orElse(null);
  if(type!=MobSkillType.PHYSICAL_COUNTER&&type!=MobSkillType.MAGIC_COUNTER&&type!=MobSkillType.PHYSICAL_AND_MAGIC_COUNTER)return;
  if(!mob.getSkills().contains(new MobSkillId(type,level)))return;
  var skill=MobSkillFactory.getMobSkillOrThrow(type,level);int animation=MonsterInformationProvider.getInstance().getMobSkillAnimationTime(skill);long now=System.currentTimeMillis();
  WARNINGS.put(mob,new Warning(type!=MobSkillType.MAGIC_COUNTER,type!=MobSkillType.PHYSICAL_COUNTER,now,now+Math.max(250,animation)+500));
  if(type!=MobSkillType.MAGIC_COUNTER)BotHurricane.cancel(b);
 }
 static boolean blocked(Character b,Monster mob,boolean magic,long now){
  var warning=WARNINGS.get(mob);if(warning==null)return false;
  if(now>=warning.until()){WARNINGS.remove(mob);return false;}
  return now>=warning.began()+150+Math.floorMod(b.getId(),100)&&(magic?warning.magic():warning.physical());
 }
}
