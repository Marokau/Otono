package server.bots;
import client.Character;
import net.packet.Packet;
import net.opcodes.SendOpcode;
import scripting.event.EventInstanceManager;
import java.nio.*;
import java.util.*;
/** Learns only the visible statue-flash packets; never reads the event's hidden combination. */
public final class BotGuildStatues {
 static final class State {
  final List<Integer> seen=new ArrayList<>();
  int replay; boolean displaying; long nextTalk, arrived; final Set<String> notices=new HashSet<>();
 }
 private static final Map<EventInstanceManager,State> STATES=Collections.synchronizedMap(new WeakHashMap<>());
 private BotGuildStatues(){}
 static State state(EventInstanceManager event){synchronized(STATES){return STATES.computeIfAbsent(event,k->new State());}}
 public static void observe(Character b,Packet packet){
  if(b==null || b.getMapId()!=990000300 || b.getEventInstance()==null || !b.getJob().isA(client.Job.MAGICIAN))return;
  var e=BotManager.getInstance().getEntryByBotCharId(b.getId());
  if(e==null || e.owner==null || !BotGuildCompanions.belongsTo(b,e.owner.getId()))return;
  var event=b.getEventInstance();String status=event.getProperty("stage1status");
  if(!"display".equals(status) && !"active".equals(status))return;
  int oid=flashOid(packet);if(oid<0)return;
  var r=b.getMap().getReactorByOid(oid);
  if(r==null || r.getId()<9208000 || r.getId()>9208002)return;
  var s=state(event);synchronized(s){
   if("display".equals(status) && !r.isRecentHitFromAttack()){
    if(!s.displaying){s.seen.clear();s.replay=0;s.displaying=true;}s.seen.add(oid);
   }else if("active".equals(status) && r.isRecentHitFromAttack() && s.replay<s.seen.size()
       && s.seen.get(s.replay)==oid)s.replay++;
  }
 }
 static int flashOid(Packet packet){
  var bytes=packet.getBytes();if(bytes.length<7)return -1;
  var b=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
  return Short.toUnsignedInt(b.getShort())==SendOpcode.REACTOR_HIT.getValue()?b.getInt():-1;
 }
 static boolean tick(BotEntry e,Character b,Character leader,List<Character> bots,boolean test,boolean ai){
  var event=b.getEventInstance();var s=state(event);
  synchronized(s){
   long now=System.currentTimeMillis();
   if("true".equals(event.getProperty("stage1clear"))){
    if(s.notices.add("clear"))leader.dropMessage(5,"GPQ: The Gatekeeper Test is clear. Enter the opened gate when ready.");
    if(!test && leader.getMapId()==990000300)return BotOpqRun.hold(e,b,ai);
    return BotMagatiaRun.portal(e,b,"next00",ai);
   }
   if(s.arrived==0){s.arrived=now;BotPqChat.queue(e,"Stage One: Watch the statues flash in order. We will remember the pattern and repeat it with normal attacks. Speak to the Gatekeeper to begin each round and check our answer.",false);}
   if(now-s.arrived<6000)return BotOpqRun.hold(e,b,ai);
   String status=event.getProperty("stage1status");int phase=event.getProperty("stage1phase")==null?1:Integer.parseInt(event.getProperty("stage1phase"));
   if(status==null || status.equals("waiting")){
    s.displaying=false;
    if(test && b==leader && bots.stream().allMatch(x->x.getMapId()==990000300))return talk(e,b,s,ai);
    return BotOpqRun.hold(e,b,ai);
   }
   if(!status.equals("active"))return BotOpqRun.hold(e,b,ai);
   if(s.seen.size()!=phase+3){
    if(s.notices.add("missed-"+phase))leader.dropMessage(5,"GPQ: We missed part of the displayed pattern. Please wait; we cannot guess a hidden answer.");
    return BotOpqRun.hold(e,b,ai);
   }
   if(s.replay>=s.seen.size()){
    if(s.notices.add("ready-"+phase))leader.dropMessage(5,"GPQ: Round "+phase+" has been repeated. Ask the Gatekeeper to check it.");
    if(test && b==leader)return talk(e,b,s,ai);
    return BotOpqRun.hold(e,b,ai);
   }
   var r=b.getMap().getReactorByOid(s.seen.get(s.replay));if(r==null)return BotOpqRun.hold(e,b,ai);
   var worker=bots.stream().filter(x->x.isAlive()&&x.getMapId()==990000300).min(Comparator.comparingDouble(x->x.getPosition().distanceSq(r.getPosition()))).orElse(null);
   if(worker!=b)return BotOpqRun.hold(e,b,ai);
   var p=r.getPosition();
   if(Math.abs(b.getPosition().x-p.x)<=70 && Math.abs(b.getPosition().y-p.y)<=45 && !e.inAir && !e.climbing && !b.hasDisease(client.Disease.STUN)){
    BotOpqRun.hold(e,b,ai);BotOpqReactorAttack.hit(e,b,r);return true;
   }
   // The regular reactor approach helper handles ropes/jumps and legal attack stances.
   return BotGuildActions.smash(e,b,x->x.getObjectId()==r.getObjectId(),ai);
  }
 }
 private static boolean talk(BotEntry e,Character b,State s,boolean ai){
  var ready=BotTravelManager.tickApproachNpc(e,b,990000300,9040009,2,ai,false,100);
  if(ready==BotTravelManager.ApproachStatus.ARRIVED && System.currentTimeMillis()>=s.nextTalk){
   s.nextTalk=System.currentTimeMillis()+1200;
   if(b.getClient().getCM()==null)BotNpcInteraction.talk(b,9040009);else BotNpcInteraction.next(b,1);
  }
  return true;
 }
}
