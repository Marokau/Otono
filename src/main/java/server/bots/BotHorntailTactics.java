package server.bots;
import client.Character;
import server.life.Monster;
import java.util.*;
/** Defer all hands while other main parts are still attacking. */
final class BotHorntailTactics {
 private BotHorntailTactics() {}
 static List<Monster> targets(Character bot,List<Monster> candidates) {
  if(!BotHorntailRun.inInstance(bot)||bot.getMapId()!=240060200)return candidates;
  boolean others=false;for(var mob:bot.getMap().getAllMonsters())
   if(mob.isAlive()&&(mob.getId()>=8810002&&mob.getId()<=8810004||mob.getId()>=8810007&&mob.getId()<=8810009)){others=true;break;}
  if(!others)return candidates;
  var allowed=new ArrayList<Monster>();
  for(var mob:candidates)if(mob.getId()!=8810005&&mob.getId()!=8810006)allowed.add(mob);
  return allowed;
 }
}
