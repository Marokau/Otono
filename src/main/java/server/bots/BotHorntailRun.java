package server.bots;

import client.Character;
import client.Job;
import net.server.world.Party;
import scripting.event.EventInstanceManager;
import server.expeditions.ExpeditionType;
import java.util.*;

/** Normal Horntail NPC registration, trial actions, raid stages and player-led waiting. */
public final class BotHorntailRun {
    static final int TRIAL_RECRUIT=240050000,RAID_RECRUIT=240050400,EXIT=240050600;
    static final class Crew {
        Character leader; final List<Character> bots=new ArrayList<>(); boolean human;
        EventInstanceManager trial;
        final Set<String> notices=java.util.concurrent.ConcurrentHashMap.newKeySet();
        final Map<Integer,Long> next=new java.util.concurrent.ConcurrentHashMap<>();
        final Map<Integer,Long> arrival=new java.util.concurrent.ConcurrentHashMap<>();
        long clearedAt; int rewardPickups;
    }
    private static final Map<Character,Crew> RUNS=Collections.synchronizedMap(new WeakHashMap<>());
    private BotHorntailRun() {}
    static Crew crew(Character b){synchronized(RUNS){var c=RUNS.get(b);if(c!=null)return c;for(var x:RUNS.values())if(x.leader==b)return x;return null;}}
    static boolean belongsTo(Character b,int owner){var c=RUNS.get(b);return c!=null&&c.leader.getId()==owner;}
    static void unregister(Character b){RUNS.remove(b);}
    public static boolean inInstance(Character b){return b!=null&&b.getEventInstance()!=null&&(b.getMapId()>=240050100&&b.getMapId()<=240050310||b.getMapId()>=240060000&&b.getMapId()<=240060200);}
    static void start(Character leader,List<Character> bots,boolean human) {
        if(bots.size()!=5&&bots.size()!=11)throw new IllegalArgumentException("Horntail crew must contain five or eleven companions");
        var c=new Crew();c.leader=leader;c.bots.addAll(bots);c.human=human;
        var m=BotManager.getInstance();
        for(int i=0;i<bots.size();i++) {
            var b=bots.get(i);var partyLeader=i>=5?bots.get(5):leader;
            if(i==5){if(b.getParty()!=null)Party.leaveParty(b.getParty(),b.getClient());if(!Party.createParty(b,true))throw new IllegalStateException("Second party failed");}
            else m.partyUp(partyLeader,b);
            RUNS.put(b,c);var e=m.getEntryByBotCharId(b.getId());m.resumeFromOperatorCommand(e);m.issueStop(e);
        }
        if(!human){RUNS.put(leader,c);var e=m.getEntryByBotCharId(leader.getId());m.resumeFromOperatorCommand(e);m.issueStop(e);}
        if(leader.getParty()==null||leader.getParty().getLeaderId()!=leader.getId()||leader.getParty().getMembers().size()!=6)throw new IllegalStateException("First party not ready");
        if(bots.size()==11&&bots.get(5).getParty().getMembers().size()!=6)throw new IllegalStateException("Second party not ready");
    }
    static boolean ready(Crew c,Character b){long now=System.currentTimeMillis();if(now<c.next.getOrDefault(b.getId(),0L))return false;c.next.put(b.getId(),now+1200);return true;}
    static boolean intro(BotEntry e,Character b,Crew c,String text,boolean ai){int map=b.getMapId();long now=System.currentTimeMillis();if(c.notices.add("intro-"+map)){c.arrival.put(map,now);BotPqChat.queue(e,text,false);}return now-c.arrival.getOrDefault(map,now)<6500&&BotOpqRun.hold(e,b,ai);}
    static boolean talk(BotEntry e,Character b,Crew c,int npc,int selection,boolean ai){var a=BotTravelManager.tickApproachNpc(e,b,b.getMapId(),npc,3,ai,false,150);if(a==BotTravelManager.ApproachStatus.ARRIVED){BotOpqRun.hold(e,b,ai);if(ready(c,b)){if(b.getClient().getCM()==null)BotNpcInteraction.talk(b,npc);else BotNpcInteraction.next(b,selection);}}return true;}
    static List<Character> all(Crew c){var out=new ArrayList<>(c.bots);if(!out.contains(c.leader))out.add(c.leader);return out;}
    public static boolean tick(BotEntry e,Character b,boolean ai) {
        var c=RUNS.get(b);if(c==null)return false;e.grinding=false;
        e.buffConsumablesEnabled=true;e.buffCheapMode=false;e.autoBuffEngaged=false;
        var leader=c.leader;int map=b.getMapId();
        boolean online=leader.getWorldServer().getPlayerStorage().getCharacterById(leader.getId())==leader;
        var expected=c.bots.indexOf(b)>=5?c.bots.get(5):leader;
        boolean valid=online&&leader.getParty()!=null&&b.getParty()!=null&&expected.getParty()!=null
            &&b.getParty().getId()==expected.getParty().getId()&&leader.getParty().getLeaderId()==leader.getId();
        if(c.human&&(!valid||inInstance(b)&&!inInstance(leader))){
            if(inInstance(b))return talk(e,b,c,2083002,1,ai);
            BotHorntailCompanions.dismiss(b);return true;
        }
        if(b.isAlive())b.triggerAutopotAfterStatLoss(-1,-1);
        if(inInstance(b)&&BotLpqCures.tryCure(b))return BotOpqRun.hold(e,b,ai);
        if(map==TRIAL_RECRUIT){
            if(!c.human&&b==leader&&leader.getEventInstance()==null)return talk(e,b,c,2083001,0,ai);
            return BotOpqRun.hold(e,b,ai);
        }
        if(map>=240050100&&map<=240050310){c.trial=b.getEventInstance();return BotHorntailTrial.tick(e,b,c,ai);}
        if(map==RAID_RECRUIT){
            if(c.bots.size()<11)return BotOpqRun.hold(e,b,ai);
            var expedition=b.getClient().getChannelServer().getExpedition(ExpeditionType.HORNTAIL);
            if(expedition!=null&&expedition.isLeader(leader)) {
                if(!expedition.contains(b)||expedition.isInProgress())return talk(e,b,c,2083004,0,ai);
                if(!c.human&&b==leader&&expedition.getMemberList().size()==12)return talk(e,b,c,2083004,2,ai);
            }else if(expedition==null&&!c.human&&b==leader)return talk(e,b,c,2083004,1,ai);
            return BotOpqRun.hold(e,b,ai);
        }
        if(map==EXIT)return BotOpqRun.hold(e,b,ai);
        if(!inInstance(b))return BotOpqRun.hold(e,b,ai);
        var event=b.getEventInstance();
        if(BotHorntailRevive.tick(e,b,event.getPlayers(),ai))return true;
        if(event.isEventCleared()) {
            if(c.clearedAt==0)c.clearedAt=System.currentTimeMillis();
            if(c.notices.add("victory"))leader.dropMessage(5,"Horntail: Victory! All loot is yours. Leave through the Crystal of Roots when you are finished; we will follow.");
            if(!c.human&&b==leader&&BotHorntailTrial.collect(e,b,c,id->true,ai))return true;
            if(c.human&&leader.getMapId()==map||!c.human&&System.currentTimeMillis()-c.clearedAt<15000)return BotOpqRun.hold(e,b,ai);
            return talk(e,b,c,2083002,1,ai);
        }
        if(map==240060000||map==240060100) {
            if(intro(e,b,c,(map==240060000?"First Head: ":"Second Head: ")+"Gather near the entrance and buff. We will fight from within our weapons' reach. Stay clear of the head's body. After it falls, enter the opened portal when ready.",ai))return true;
            int need=map==240060000?1:2;
            if(event.getIntProperty("defeatedHead")>=need){if(c.human&&leader.getMapId()==map)return BotOpqRun.hold(e,b,ai);return BotMagatiaRun.portal(e,b,"next00",ai);}
        } else if(intro(e,b,c,"Main Chamber: Gather and buff first. Break the crystal when our protection is ready. We will prioritize the tail, legs, wings, then heads, and save healthy hands for last to avoid mass Seduce. Stay close to your party for Hyper Body and healing. Boss loot is yours.",ai))return true;
        if(BotHorntailSupport.tick(e,b,c,ai))return true;
        if(b.getMap().getAllMonsters().stream().anyMatch(m->m.isAlive()&&!m.isFake()))return BotHorntailCombat.tick(e,b,ai);
        if(map==240060200){if(c.human)return BotOpqRun.hold(e,b,ai);if(b==leader)return BotGuildActions.smash(e,b,r->r.getId()==2401000&&r.isActive()&&r.getReactorType()==0,ai);}
        return BotOpqRun.hold(e,b,ai);
    }
}
