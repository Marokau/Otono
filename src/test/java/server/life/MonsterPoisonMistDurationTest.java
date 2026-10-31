package server.life;
import java.awt.Point;
import org.junit.jupiter.api.Test;
import server.maps.MapleMap;
import server.maps.Mist;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class MonsterPoisonMistDurationTest {
 @Test void nativePoisonCloudLifetimeUsesWzTimeRatherThanTickDamage()throws Exception{
  int[] seconds={10,15,5,20,15,15,10,15,20};
  var spawn=MobSkill.class.getDeclaredMethod("spawnMonsterMist",Monster.class);spawn.setAccessible(true);
  for(int level=1;level<=seconds.length;level++){
   var skill=MobSkillFactory.getMobSkillOrThrow(MobSkillType.AREA_POISON,level);
   var mob=mock(Monster.class);var map=mock(MapleMap.class);
   when(mob.getMap()).thenReturn(map);when(mob.getPosition()).thenReturn(new Point(5,-44));
   assertEquals(seconds[level-1]*1000L,skill.getDuration());
   spawn.invoke(skill,mob);
   verify(map).spawnMist(any(Mist.class),eq(seconds[level-1]*1000),eq(false),eq(false),eq(false));
   if(level==4)assertEquals(2000,skill.getX(),"Ariel poison damage must remain unchanged");
  }
 }
}
