package server.bots;

import client.BotClient;
import client.Character;
import client.inventory.InventoryType;
import server.maps.MapItem;
import server.maps.Reactor;
import java.awt.Point;
import java.util.*;

/** Normal letter collection, leader hand-in, MV fight and optional treasure-room help. */
public final class BotMvpqRun {
    public static final int RECRUIT = 674030100;
    static final int COURSE = 674030000, BOSS = 674030200, BONUS = 674030300, LETTER = 4032118;
    static final class Crew {
        Character leader; List<Character> bots; boolean human;
        final Set<String> notices = new HashSet<>();
        final Map<Integer,Integer> claims = new HashMap<>();
        final Map<Integer,Long> next = new HashMap<>();
        final Map<Integer,Long> arrived = new HashMap<>();
        int rewardPickups;
    }
    private static final Map<Character,Crew> RUNS = Collections.synchronizedMap(new WeakHashMap<>());
    private BotMvpqRun() {}
    public static boolean inInstance(Character b) { return b != null && b.getEventInstance() != null && (b.getMapId()==COURSE || b.getMapId()==BOSS || b.getMapId()==BONUS); }
    static boolean belongsTo(Character b,int owner) { var c=RUNS.get(b); return c!=null && c.leader.getId()==owner; }
    static boolean assigned(Character b) { return RUNS.containsKey(b); }
    static void unregister(Character b) { RUNS.remove(b); }
    static void start(Character leader,List<Character> bots,boolean human) {
        if (bots.size()!=5 || bots.stream().map(Character::getId).distinct().count()!=5 || bots.contains(leader)) throw new IllegalArgumentException("MVPQ requires five distinct companions");
        var c=new Crew();c.leader=leader;c.bots=List.copyOf(bots);c.human=human;
        var manager=BotManager.getInstance();
        for(var b:bots) {
            if(!(b.getClient() instanceof BotClient)||b.isGM()||b.getLevel()<140||b.getMapId()!=RECRUIT||b.getEventInstance()!=null)throw new IllegalArgumentException("Ineligible MVPQ companion");
            var e=manager.getEntryByBotCharId(b.getId());prepareEntry(e,b);manager.partyUp(leader,b);RUNS.put(b,c);
        }
        if(!human){prepareEntry(manager.getEntryByBotCharId(leader.getId()),leader);RUNS.put(leader,c);}
        if(leader.getParty()==null||leader.getParty().getLeaderId()!=leader.getId()||leader.getParty().getMembers().size()!=6)throw new IllegalStateException("Six-person MVPQ party not ready");
    }
    private static void prepareEntry(BotEntry e,Character b){var m=BotManager.getInstance();m.resumeFromOperatorCommand(e);m.issueStop(e);BotMvpqCompanions.prepare(e,b);}
    static boolean ready(Crew c,Character b,long delay){synchronized(c){long now=System.currentTimeMillis();if(now<c.next.getOrDefault(b.getId(),0L))return false;c.next.put(b.getId(),now+delay);return true;}}
    static boolean intro(BotEntry e,Character b,Crew c,String text,boolean ai){synchronized(c){long now=System.currentTimeMillis();String key="intro-"+b.getMapId();if(c.notices.add(key)){c.arrived.put(b.getMapId(),now);BotPqChat.queue(e,text,false);}if(now-c.arrived.getOrDefault(b.getMapId(),now)<(b.getMapId()==COURSE?20000:6500))return BotOpqRun.hold(e,b,ai);}return false;}
    static int letters(Crew c){int n=c.leader.getItemQuantity(LETTER,false);for(var b:c.bots)n+=b.getItemQuantity(LETTER,false);for(var d:c.leader.getMap().getDroppedItems())if(!d.isPickedUp()&&d.getItemId()==LETTER)n+=d.getItem().getQuantity();return n;}
    static boolean collect(BotEntry e,Character b,Crew c,boolean lettersOnly,boolean ai){
        MapItem best=null;double distance=Double.MAX_VALUE;
        for(var d:b.getMap().getDroppedItems())if(!d.isPickedUp()&&d.canBePickedBy(b)&&System.currentTimeMillis()-d.getDropTime()>500
                &&(!lettersOnly||d.getItemId()==LETTER&&(!d.isPlayerDrop()||b==c.leader))){double x=b.getPosition().distanceSq(d.getPosition());if(x<distance){best=d;distance=x;}}
        if(best==null)return false;
        if(distance<=30*30){int before=best.getItemId()>0?b.getItemQuantity(best.getItemId(),false):0;int mesos=b.getMeso();b.pickupItem(best);if(!lettersOnly&&best.getDropper() instanceof Reactor source&&source.getId()==6742014&&(b.getMeso()>mesos||best.getItemId()>0&&b.getItemQuantity(best.getItemId(),false)>before))synchronized(c){c.rewardPickups++;}return BotOpqRun.hold(e,b,ai);}
        if(BotMvpqCourseReset.tick(e,b,c,best.getPosition(),ai))return true;
        BotZakumPqRun.stepTowardsAndPickUp(e,b,best,ai);return true;
    }
    static Reactor claim(Crew c,Character b,int id1,int id2){synchronized(c){
        c.claims.entrySet().removeIf(x->{var r=b.getMap().getReactorByOid(x.getKey());return r==null||!r.isActive()||r.getState()>=5;});
        var graph=BotNavigationGraphProvider.peekGraph(b.getMap(),BotMovementProfile.fromCharacter(b));
        int from=graph==null?-1:graph.findRegionId(b.getMap(),b.getPosition());
        int mask=BotNavigationManager.botCanUseMovementSkill(b)?BotNavigationManager.botSkillMask(b):0;
        for(var x:new ArrayList<>(c.claims.entrySet()))if(x.getValue()==b.getId()){
            var held=b.getMap().getReactorByOid(x.getKey());
            if(graph==null||graph.canReach(from,graph.findRegionId(b.getMap(),held.getPosition()),mask))return held;
            c.claims.remove(x.getKey());
        }
        if(graph!=null){
            Reactor best=null;double distance=Double.MAX_VALUE;
            for(var candidate:b.getMap().getAllReactors()){
                if(!candidate.isActive()||candidate.getState()>=5||(candidate.getId()!=id1&&candidate.getId()!=id2)||c.claims.containsKey(candidate.getObjectId()))continue;
                if(!graph.canReach(from,graph.findRegionId(b.getMap(),candidate.getPosition()),mask))continue;
                double d=b.getPosition().distanceSq(candidate.getPosition());if(d<distance){best=candidate;distance=d;}
            }
            if(best!=null)c.claims.put(best.getObjectId(),b.getId());return best;
        }
        var r=b.getMap().getAllReactors().stream().filter(x->x.isActive()&&x.getState()<5&&(x.getId()==id1||x.getId()==id2)&&!c.claims.containsKey(x.getObjectId())).min(Comparator.comparingDouble(x->b.getPosition().distanceSq(x.getPosition()))).orElse(null);
        if(r!=null)c.claims.put(r.getObjectId(),b.getId());return r;
    }}
    public static boolean tick(BotEntry e,Character b,boolean ai){
        var c=RUNS.get(b);if(c==null)return false;e.grinding=false;
        var leader=c.leader;boolean online=leader.getWorldServer().getPlayerStorage().getCharacterById(leader.getId())==leader;
        if(!online){if(!inInstance(b))BotMvpqCompanions.dismiss(b);else if(b.getMapId()==BONUS)BotMagatiaRun.portal(e,b,"home00",ai);else talk(e,b,c,9220019,ai);return true;}
        if(b!=leader&&(leader.getParty()==null||b.getParty()==null||b.getParty().getId()!=leader.getParty().getId())){
            // Leaving the human's party uses the native PQ cancellation path.
            if(b.getParty()!=null)net.server.world.Party.leaveParty(b.getParty(),b.getClient());
            if(!inInstance(b))BotMvpqCompanions.dismiss(b);
            else if(b.getMapId()==BONUS)BotMagatiaRun.portal(e,b,"home00",ai);
            else talk(e,b,c,9220019,ai);
            return true;
        }
        if(b.isAlive())b.triggerAutopotAfterStatLoss(-1,0);
        if(!inInstance(b)){
            if(b!=leader&&(leader.getParty()==null||b.getParty()==null||b.getParty().getId()!=leader.getParty().getId())){BotMvpqCompanions.dismiss(b);return true;}
            return BotOpqRun.hold(e,b,ai);
        }
        if(BotLpqCures.tryCure(b))return BotOpqRun.hold(e,b,ai);
        synchronized(c){if(System.currentTimeMillis()<c.next.getOrDefault(b.getId(),0L))return BotOpqRun.hold(e,b,ai);}
        int map=b.getMapId();
        if(map==COURSE){
            if(intro(e,b,c,"Stage One: We will search the boxes for 15 Secret Letters and bring them to you. Use the ladders and platforms, and watch for spike traps. If you land on the central islands, drop below to reset beside the ladders. Pick up our letters, then speak to the guide beside the upper-left tunnel.",ai))return true;
            if(b.getEventInstance().getIntProperty("statusStg1")==1){
                var routeExit=b.getMap().getPortal("out00");
                if(routeExit!=null&&BotMvpqCourseReset.tick(e,b,c,routeExit.getPosition(),ai))return true;
                synchronized(c){if(c.notices.add("letters-clear"))leader.dropMessage(5,c.human?"MVPQ: The letters are accepted. We are gathering by the tunnel; wait for our ready message before entering.":"MVPQ: The letters are accepted. Enter the upper-left tunnel when ready; we follow.");}
                if(c.human&&leader.getMapId()==COURSE){
                    var tunnel=b.getMap().getPortal("out00");
                    if(tunnel==null)return BotOpqRun.hold(e,b,ai);
                    boolean together=true;
                    for(var member:c.bots)if(member.getEventInstance()!=null&&(member.getMapId()!=COURSE||member.getPosition().distanceSq(tunnel.getPosition())>90*90))together=false;
                    synchronized(c){if(together&&c.notices.add("tunnel-ready"))leader.dropMessage(5,"MVPQ: The crew is ready beside the tunnel. Enter when you are ready; we will follow.");}
                    if(b.getPosition().distanceSq(tunnel.getPosition())>60*60){BotZakumPqRun.walkTo(e,b,tunnel.getPosition(),ai);return true;}
                    return BotOpqRun.hold(e,b,ai);
                }
                return BotMagatiaRun.portal(e,b,"out00",ai);
            }
            int total = letters(c);
            synchronized(c){for(int threshold:new int[]{5,10})if(total>=threshold&&c.notices.add("letter-progress-"+threshold))BotPqChat.queue(e,"We have found "+threshold+" Secret Letters. We need "+(15-threshold)+" more.",false);}
            if(total>=15){
                // Count floor letters to avoid extra box cycles, but still retrieve
                // real reactor drops before waiting or handing off carried letters.
                if(b!=leader&&collect(e,b,c,true,ai))return true;
                synchronized(c){if(c.notices.add("letters-found"))BotPqChat.queue(e,"We found all 15 Secret Letters. We are bringing them to you now.",false);}
                if(b!=leader&&b.getItemQuantity(LETTER,false)>0){
                    if(leader.getMapId()!=map)return BotOpqRun.hold(e,b,ai);
                    if(b.getPosition().distanceSq(leader.getPosition())>40*40){if(BotMvpqCourseReset.tick(e,b,c,leader.getPosition(),ai))return true;BotZakumPqRun.walkTo(e,b,leader.getPosition(),ai);return true;}
                    BotOpqRun.hold(e,b,ai);if(!e.inAir&&!e.climbing&&ready(c,b,5500)){if(BotManager.getInstance().issueDropItem(e,InventoryType.ETC,LETTER,(short)b.getItemQuantity(LETTER,false)))synchronized(c){if(c.notices.add("handoff-"+b.getId()))BotPqChat.queue(e,"Here are my Secret Letters. Please pick them up.",false);}}return true;
                }
                int beside=leader.getItemQuantity(LETTER,false);
                if(leader.getMapId()==map)for(var drop:b.getMap().getDroppedItems())if(!drop.isPickedUp()&&drop.getItemId()==LETTER&&drop.getPosition().distanceSq(leader.getPosition())<=100*100)beside+=drop.getItem().getQuantity();
                synchronized(c){if(beside>=15&&leader.getItemQuantity(LETTER,false)<15&&c.notices.add("letters-beside"))leader.dropMessage(5,"MVPQ: All 15 Secret Letters are beside you. Pick them up, then speak to the guide by the upper-left tunnel.");}
                if(b==leader&&collect(e,b,c,true,ai))return true;
                if(leader.getItemQuantity(LETTER,false)>=15){synchronized(c){if(c.notices.add("letters-ready"))leader.dropMessage(5,"MVPQ: You have all 15 Secret Letters. Speak to the guide by the upper-left tunnel.");}if(!c.human&&b==leader)return talk(e,b,c,9220020,ai);}
                if(c.human&&b!=leader&&leader.getItemQuantity(LETTER,false)>=15&&leader.getMapId()==map&&b.getPosition().distanceSq(leader.getPosition())>70*70){if(BotMvpqCourseReset.tick(e,b,c,leader.getPosition(),ai))return true;BotZakumPqRun.walkTo(e,b,leader.getPosition(),ai);return true;}
                return BotOpqRun.hold(e,b,ai);
            }
            if(collect(e,b,c,true,ai))return true;
            var r=claim(c,b,6741015,6741016);
            if(r==null){var guide=b.getMap().getNPCById(9220020);if(guide!=null&&BotMvpqCourseReset.tick(e,b,c,guide.getPosition(),ai))return true;return BotOpqRun.hold(e,b,ai);}
            if(BotMvpqCourseReset.tick(e,b,c,r.getPosition(),ai))return true;
            return BotGuildActions.smash(e,b,x->x.getObjectId()==r.getObjectId(),ai);
        }
        if(map==BOSS){
            if(intro(e,b,c,"Stage Two: Gather near the rock and buff first. We will break it to awaken MV, then help defeat him. All boss loot is yours. Pick it up quickly when MV falls: the treasure-room warp follows in ten seconds.",ai))return true;
            if(b.getEventInstance().isEventCleared()){synchronized(c){if(c.notices.add("mv-clear"))leader.dropMessage(5,"MVPQ: MV is defeated! Pick up boss drops now; the treasure-room warp follows in ten seconds.");}return BotOpqRun.hold(e,b,ai);}
            if(b.getMap().getSpawnedMonstersOnMap()>0)return BotOpqRun.grind(e,b,ai);
            if(leader.getMapId()!=BOSS||c.bots.stream().anyMatch(x->x.getMapId()!=BOSS&&x.getEventInstance()!=null))return BotOpqRun.hold(e,b,ai);
            return BotGuildActions.smash(e,b,x->x.getId()==6741001&&x.getState()<5,ai);
        }
        if(intro(e,b,c,"Treasure Room: We will help defeat the monsters and open treasure boxes. All drops are yours. There are ten minutes here; leave through the bottom portal whenever you are finished and we will return too.",ai))return true;
        if(!c.human&&b==leader){
            if(c.rewardPickups>0&&System.currentTimeMillis()-c.arrived.get(BONUS)>30000)return BotMagatiaRun.portal(e,b,"home00",ai);
            if(collect(e,b,c,false,ai))return true;
        }
        if(b!=leader&&leader.getMapId()==RECRUIT)return BotMagatiaRun.portal(e,b,"home00",ai);
        // The treasure room has optional bosses; opening its boxes does not require killing every one.
        var box=claim(c,b,6742014,6742014);
        if(box!=null)return BotGuildActions.smash(e,b,x->x.getObjectId()==box.getObjectId(),ai);
        return b.getMap().getSpawnedMonstersOnMap()>0?BotOpqRun.grind(e,b,ai):BotOpqRun.hold(e,b,ai);
    }
    private static boolean talk(BotEntry e,Character b,Crew c,int npc,boolean ai){var target=b.getMap().getNPCById(npc);if(target!=null&&BotMvpqCourseReset.tick(e,b,c,target.getPosition(),ai))return true;var a=BotTravelManager.tickApproachNpc(e,b,b.getMapId(),npc,2,ai,false,100);if(a==BotTravelManager.ApproachStatus.ARRIVED&&ready(c,b,1000)){if(b.getClient().getCM()==null)BotNpcInteraction.talk(b,npc);else BotNpcInteraction.next(b,1);}return true;}
}
