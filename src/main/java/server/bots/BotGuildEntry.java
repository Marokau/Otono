package server.bots;
import client.Character;
import java.util.ArrayList;
import java.util.List;
/** Readiness check only for an explicitly assigned GPQ crew; normal guilds are unchanged. */
public final class BotGuildEntry {
 private BotGuildEntry() {}
 public static String checkReady(Character leader) {
  if(leader.getParty()==null)return null;
  var bots=new ArrayList<Character>();
  for(var m:leader.getParty().getMembers()) {
   if(m.getId()==leader.getId())continue;
   var e=BotManager.getInstance().getEntryByBotCharId(m.getId());
   if(e!=null && BotGuildCompanions.belongsTo(e.bot,leader.getId()))bots.add(e.bot);
  }
  if(bots.isEmpty())return null;
  String error=checkMembers(leader,bots);
  if(error!=null)return error;
  // Every normal registration is a fresh attempt, even when reusing the party.
  for(var b:bots){var e=BotManager.getInstance().getEntryByBotCharId(b.getId());BotMagatiaRun.provision(e,b);}
  return null;
 }
 static String checkMembers(Character leader,List<Character> bots) {
  if(bots.size()!=5)return "Your GPQ crew needs all five assigned bots. Reassemble it before registering.";
  var npc=leader.getMap().getNPCById(9040000);
  if(npc==null)return "Register with Shuang at the GPQ camp.";
  for(var b:bots)if(b.getEventInstance()!=null || b.getMapId()!=BotGuildCompanions.RECRUIT
    || b.getGuildId()!=leader.getGuildId() || b.getClient().getChannel()!=leader.getClient().getChannel()
    || b.getPosition().distanceSq(npc.getPosition())>140*140)
   return "Please wait for all five GPQ companions to reach Shuang. We walk here before you start the three-minute gathering timer.";
  return null;
 }
}
