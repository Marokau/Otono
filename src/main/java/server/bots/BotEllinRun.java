package server.bots;

import client.Character;
import client.BotClient;
import client.inventory.InventoryType;
import scripting.event.EventInstanceManager;
import server.life.Monster;
import server.maps.Reactor;
import java.awt.Point;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import io.netty.buffer.Unpooled;
import net.packet.ByteBufInPacket;
import net.server.channel.handlers.UseCatchItemHandler;

/** Ellin companions use ordinary attacks, capture-item packets, reactors, NPCs and portals. */
public final class BotEllinRun {
    public static final int RECRUIT=300030100, FIRST=930000000;
    static final int CONCENTRATE=4001161, DILUTED=4001162, STONE=4001163, MARBLE=4001169, PURIFIER=2270004;
    private static final Map<Character,State> RUNS=Collections.synchronizedMap(new WeakHashMap<>());
    static final class Crew {
        Character leader,collector; List<Character> bots; boolean human;
        EventInstanceManager event; final Set<String> said=new HashSet<>();
        final Map<Integer,Long> introduced=new HashMap<>();
    }
    static final class State { Crew crew; long next,pending; int pendingItem; boolean finished; }
    private BotEllinRun() {}
    public static boolean inInstance(Character b) { return b!=null && b.getEventInstance()!=null && b.getMapId()>=FIRST && b.getMapId()<=FIRST+800; }
    static boolean belongsTo(Character b,int id) { var s=RUNS.get(b);return s!=null && s.crew.human && s.crew.leader.getId()==id; }
    static void unregister(Character b) { RUNS.remove(b); }
    static void startHuman(Character leader,List<Character> bots) { start(leader,bots,true); }
    public static void startCrew(List<Character> bots) { start(bots.getFirst(),bots,false); }
    private static void start(Character leader,List<Character> bots,boolean human) {
        if(bots.size()+(human?1:0)!=4 || bots.stream().map(Character::getId).distinct().count()!=bots.size()) throw new IllegalArgumentException("Four distinct participants required");
        var c=new Crew();c.leader=leader;c.bots=List.copyOf(bots);c.human=human;c.collector=human?bots.getFirst():leader;
        var manager=BotManager.getInstance();
        for(var b:bots) {
            if(!(b.getClient() instanceof BotClient) || b.getMapId()!=RECRUIT && b.getMapId()!=FIRST+800 || b.getEventInstance()!=null || b.getLevel()<44 || b.getLevel()>55
                    || b.getJob().getId()/1000!=0 || b.getWorld()!=leader.getWorld() || b.getClient().getChannel()!=leader.getClient().getChannel()) throw new IllegalArgumentException("Ineligible Ellin companion");
            var e=manager.getEntryByBotCharId(b.getId()); if(e==null) throw new IllegalArgumentException("Companion not active");
            manager.applyOperatorCommand(e,BotEntry.OperatorCmd.IDLE,-1,0);
        }
        for(var b:bots) {
            var e=manager.getEntryByBotCharId(b.getId());
            BotBuildManager.setAutoApBuild(e,b);BotMagatiaRun.provision(e,b);
            if(b!=leader)manager.partyUp(leader,b);
        }
        if(leader.getParty()==null || leader.getParty().getLeaderId()!=leader.getId() || leader.getParty().getMembers().size()!=4) throw new IllegalStateException("Could not assemble Ellin party");
        // Publish the whole crew before releasing its preparation hold. No bot can
        // take an autonomous tick or mistake the partly assembled party for a quit.
        for(var b:bots) { var s=new State();s.crew=c;RUNS.put(b,s); }
        for(var b:bots) {
            var e=manager.getEntryByBotCharId(b.getId());
            manager.resumeFromOperatorCommand(e);manager.issueStop(e);
        }
    }
    public static void refresh(Character leader) {
        if(leader.getEventInstance()!=null)throw new IllegalArgumentException("Prepare before entry");
        if(leader.getParty()!=null)for(var member:leader.getParty().getMembers()) {
            var e=BotManager.getInstance().getEntryByBotCharId(member.getId());
            if(e!=null && belongsTo(e.bot,leader.getId())) {
                if(e.bot.getMapId()!=RECRUIT || e.bot.getEventInstance()!=null)throw new IllegalStateException("Wait for the crew to return");
                BotMagatiaRun.provision(e,e.bot);
            }
        }
    }
    static boolean canLoot(Character b,int item) {
        var s=RUNS.get(b);if(s==null)return false;
        if(item==4001164)return !s.crew.human && b==s.crew.collector && b.getMapId()==FIRST+600;
        return b==s.crew.collector && (b.getMapId()==FIRST+200 || b.getMapId()==FIRST+500)
                && !(s.pendingItem==item && System.currentTimeMillis()<s.pending);
    }
    public static boolean canAttack(Character b,Monster mob) {
        if(!RUNS.containsKey(b) || b.getMapId()!=FIRST+400 || mob.getId()!=9300175)return true;
        return mob.getHp()>=(mob.getMaxHp()/10)*4;
    }
    public static boolean tick(BotEntry e,Character b,boolean ai) {
        var s=RUNS.get(b);if(s==null)return false;var c=s.crew;e.grinding=false;
        if(b.isAlive())b.triggerAutopotAfterStatLoss(-1,0);
        if(c.human && (c.leader.getParty()==null || b.getParty()==null || c.leader.getParty().getId()!=b.getParty().getId()
                || c.leader.getParty().getLeaderId()!=c.leader.getId()
                || c.leader.getClient().getChannel()!=b.getClient().getChannel()
                || inInstance(b) && !b.getEventInstance().isEventCleared() && c.leader.getEventInstance()!=b.getEventInstance()
                || c.leader.getWorldServer().getPlayerStorage().getCharacterById(c.leader.getId())!=c.leader)) {
            if(b.getMapId()==RECRUIT) { BotEllinCompanions.dismiss(b);return true; }
            if(inInstance(b) && b.getEventInstance().isEventCleared())return BotMagatiaRun.portal(e,b,b.getMapId()==FIRST+600?"out00":"east00",ai);
            if(inInstance(b) && b.getParty()!=null)net.server.world.Party.leaveParty(b.getParty(),b.getClient());
            if(b.getMapId()==FIRST+800)return BotMagatiaRun.portal(e,b,"east00",ai);
            return BotOpqRun.hold(e,b,ai);
        }
        if(b.getMapId()==FIRST+800) {
            s.finished=true;
            if(!c.human) {
                Reactor fountain=null;
                for(var r:b.getMap().getAllReactors())if(r.getId()==3008000)fountain=r;
                var leaderState=RUNS.get(c.leader);
                boolean offered=c.leader.getItemQuantity(4001164,false)>0
                        || leaderState.pendingItem==4001164 && System.currentTimeMillis()<leaderState.pending;
                for(var d:b.getMap().getDroppedItems())if(!d.isPickedUp() && d.getItemId()==4001164)offered=true;
                if(fountain!=null && fountain.getState()==0 && offered) {
                    if(b==c.leader && b.getItemQuantity(4001164,false)>0 && System.currentTimeMillis()>=s.pending)
                        return dropAt(e,b,s,fountain,4001164,ai);
                    return BotOpqRun.hold(e,b,ai);
                }
            }
            if(!c.human || c.leader.getMapId()!=b.getMapId())return BotMagatiaRun.portal(e,b,"east00",ai);
            speak(c,"reward","If the golem dropped a marble, drop it at the fountain for bonus EXP. Then use the exit portal; I will follow you out.");
            return BotOpqRun.hold(e,b,ai);
        }
        if(!inInstance(b)) {
            if(!c.human && !s.finished && b==c.leader)return talk(e,b,2133000,ai);
            return BotOpqRun.hold(e,b,ai);
        }
        synchronized(c) {
            if(c.event!=b.getEventInstance()) { c.event=b.getEventInstance();c.said.clear();c.introduced.clear();for(var member:c.bots){var state=RUNS.get(member);state.finished=false;state.pending=0;} }
        }
        int stage=(b.getMapId()-FIRST)/100;
        if(intro(e,b,c,stage))return BotOpqRun.hold(e,b,ai);
        if(BotLpqCures.tryCure(b) || System.currentTimeMillis()<s.pending)return BotOpqRun.hold(e,b,ai);
        return switch(stage) {
            case 0 -> advance(e,b,"east00",ai);
            case 1 -> {
                if(b.getMap().getAllMonsters().isEmpty()) {
                    notice(c,"trees","EPQ: The forest is clear. Use the right portal when ready.");
                    yield advance(e,b,"east00",ai);
                }
                yield BotOpqRun.grind(e,b,ai);
            }
            case 2 -> poison(e,b,s,ai);
            case 3 -> maze(e,b,s,ai);
            case 4 -> capture(e,b,s,ai);
            case 5 -> boxes(e,b,s,ai);
            case 6 -> battle(e,b,s,ai);
            default -> BotOpqRun.hold(e,b,ai);
        };
    }
    private static boolean intro(BotEntry e,Character b,Crew c,int stage) {
        if(!c.human)return false;
        synchronized(c) {
            long now=System.currentTimeMillis();
            if(!c.introduced.containsKey(stage)) {
                c.introduced.put(stage,now);
                String text=switch(stage) {
                    case 0 -> "Welcome! Follow the portal to start. We will explain each stage and wait for your actions.";
                    case 1 -> "Stage One: Defeat the poisoned trees. We will clear the forest; use the right portal once all monsters are gone.";
                    case 2 -> "Stage Two: We will collect poison, dilute it at the fountain and feed four diluted poisons to the thorn barrier. Use the right portal once it opens.";
                    case 3 -> "Stage Three: These portals have random destinations. We will search for Ellin; finding her takes the whole team onward. You can explore too.";
                    case 4 -> "Stage Four: We will get Purification marbles, weaken monsters to low health and capture them with the Purifier marbles. Pick up our 20 Monster marbles and speak to Ellin as leader.";
                    case 5 -> "Stage Five: We will climb and break boxes to find the purple stone. Take it to the Sprite to move everyone to the boss.";
                    case 6 -> "Stage Six: Drop the purple stone at the altar when ready. We will fight the golem's three forms. Keep the boss loot, then use the right portal.";
                    default -> "We are ready to follow you.";
                };
                BotPqChat.queue(e,text,false);
            }
            return now-c.introduced.get(stage)<7000;
        }
    }
    private static boolean poison(BotEntry e,Character b,State s,boolean ai) {
        var spine=b.getMap().getReactorByName("spine");
        if(spine==null || spine.getState()>=4) { notice(s.crew,"spine","EPQ: The thorn barrier is open. Use the right portal when ready.");return advance(e,b,"east00",ai); }
        if(b==s.crew.collector) {
            if(b.getItemQuantity(DILUTED,false)>0)return dropAt(e,b,s,spine,DILUTED,ai);
            if(collect(e,b,Set.of(DILUTED),ai))return true;
            if(b.getItemQuantity(CONCENTRATE,false)>0) {
                var pond=b.getMap().getAllReactors().stream().filter(r->r.getId()==3002000).findFirst().orElse(null);
                if(pond!=null)return dropAt(e,b,s,pond,CONCENTRATE,ai);
            }
            if(collect(e,b,Set.of(CONCENTRATE),ai))return true;
        }
        return BotOpqRun.grind(e,b,ai);
    }
    private static boolean maze(BotEntry e,Character b,State s,boolean ai) {
        var ellin=b.getMap().getMapObjects().stream().filter(o->o instanceof server.life.NPC n && n.getId()==2133001).findFirst().orElse(null);
        // The successful arrival is below Ellin, with three ordinary jumps still to climb.
        // Recognize the compartment we actually reached, not a hidden portal outcome.
        server.maps.Portal arrival=null;
        double distance=Double.MAX_VALUE;
        for(var candidate:b.getMap().getPortals()) {
            double d=b.getPosition().distanceSq(candidate.getPosition());
            if(candidate.getName().matches("[0-9]{2}st") && d<distance) { arrival=candidate;distance=d; }
        }
        if(ellin!=null && arrival!=null && arrival.getName().equals("16st"))return talk(e,b,2133001,ai);
        var p=b.getMap().getPortals().stream().filter(portal->portal.getScriptName()!=null && portal.getScriptName().startsWith("party6_stage5"))
                .min(Comparator.comparingDouble(portal->b.getPosition().distanceSq(portal.getPosition()))).orElse(null);
        if(p!=null)return BotMagatiaRun.portal(e,b,p.getName(),ai);
        return BotOpqRun.hold(e,b,ai);
    }
    private static boolean capture(BotEntry e,Character b,State s,boolean ai) {
        var c=s.crew;
        int gathered=c.leader.getItemQuantity(MARBLE,false);
        for(var member:c.bots)if(member!=c.leader)gathered+=member.getItemQuantity(MARBLE,false);
        for(var drop:b.getMap().getDroppedItems())if(!drop.isPickedUp() && drop.getItemId()==MARBLE)gathered+=drop.getItem().getQuantity();
        int milestone=gathered/5*5;
        if(milestone>0 && milestone<20)speak(c,"capture-"+milestone,"We have captured "+milestone+" of 20 monsters. Keep collecting our marbles; we will handle the rest.");
        if(gathered>=20)notice(c,"capture-ready","EPQ: All 20 monster marbles have been captured. Pick up our deliveries and speak to Ellin when ready.");
        if(c.leader.getItemQuantity(MARBLE,false)>=20) {
            notice(c,"capture-ready","EPQ: All 20 marbles are ready. Talk to Ellin to continue.");
            return !c.human && b==c.leader?talk(e,b,2133001,ai):BotOpqRun.hold(e,b,ai);
        }
        if(!c.human && b==c.leader && collect(e,b,Set.of(MARBLE),ai))return true;
        if(b.getItemQuantity(MARBLE,false)>0 && b!=c.leader)return deliver(e,b,s,MARBLE,b.getItemQuantity(MARBLE,false),ai);
        if(b.getItemQuantity(PURIFIER,false)==0 && b.getMap().countMonsters()>0)return talk(e,b,2133001,ai);
        var target=b.getMap().getAllMonsters().stream().filter(m->m.isAlive() && m.getId()==9300175)
                .min(Comparator.comparingDouble(m->b.getPosition().distanceSq(m.getPosition()))).orElse(null);
        if(target==null)return BotOpqRun.hold(e,b,ai);
        if(!canAttack(b,target)) {
            if(b.getPosition().distanceSq(target.getPosition())>120*120) { BotZakumPqRun.walkTo(e,b,target.getPosition(),ai);return true; }
            BotOpqRun.hold(e,b,ai);
            if(ready(s)) {
                var packet=Unpooled.buffer().writeIntLE(0).writeShortLE(0).writeIntLE(PURIFIER).writeIntLE(target.getObjectId());
                try { new UseCatchItemHandler().handlePacket(new ByteBufInPacket(packet),b.getClient()); } finally { packet.release(); }
            }
            return true;
        }
        return BotOpqRun.grind(e,b,ai);
    }
    private static boolean boxes(BotEntry e,Character b,State s,boolean ai) {
        var c=s.crew;
        if(c.leader.getItemQuantity(STONE,false)>0) {
            notice(c,"stone","EPQ: The purple stone is ready. Talk to the Sprite to continue.");
            return !c.human && b==c.leader?talk(e,b,2133004,ai):BotOpqRun.hold(e,b,ai);
        }
        if(b.getItemQuantity(STONE,false)>0)return deliver(e,b,s,STONE,1,ai);
        if(b==c.collector && collect(e,b,Set.of(STONE),ai))return true;
        if(c.human) for(var drop:b.getMap().getDroppedItems())
            if(!drop.isPickedUp() && drop.getItemId()==STONE && drop.isPlayerDrop()) {
                notice(c,"stone-on-ground","EPQ: Pick up the purple stone beside you and speak to the Sprite to continue.");
                return BotOpqRun.hold(e,b,ai);
            }
        // Both visually identical box kinds are searched; the shuffled reward is not inspected.
        var box=b.getMap().getAllReactors().stream().filter(r->(r.getId()==3002001 || r.getId()==3002002) && r.getState()<4)
                .min(Comparator.comparingDouble(r->b.getPosition().distanceSq(r.getPosition()))).orElse(null);
        if(box!=null) {
            if(b.getPosition().distanceSq(box.getPosition())>85*85)BotZakumPqRun.walkTo(e,b,box.getPosition(),ai);
            else { BotOpqRun.hold(e,b,ai);if(ready(s))BotZakumPqRun.hitReactor(b,box); }
            return true;
        }
        return BotOpqRun.hold(e,b,ai);
    }
    private static boolean battle(BotEntry e,Character b,State s,boolean ai) {
        var c=s.crew;
        if(c.event.isEventCleared()) {
            if(!c.human) {
                synchronized(c) { c.introduced.putIfAbsent(600,System.currentTimeMillis()); }
                if(b==c.leader && collect(e,b,Set.of(4001164),ai))return true;
                if(System.currentTimeMillis()-c.introduced.get(600)<2500)return BotOpqRun.hold(e,b,ai);
            }
            notice(c,"clear","EPQ: The Poison Golem is defeated. Collect your loot and use the right portal for the fountain reward.");
            return advance(e,b,"out00",ai);
        }
        if(!c.human && b==c.leader && b.getItemQuantity(STONE,false)>0) {
            var altar=b.getMap().getAllReactors().stream().filter(r->r.getId()==3001000).findFirst().orElse(null);
            if(altar!=null)return dropAt(e,b,s,altar,STONE,ai);
        }
        return b.getMap().countMonsters()>0?BotOpqRun.grind(e,b,ai):BotOpqRun.hold(e,b,ai);
    }
    private static boolean advance(BotEntry e,Character b,String portal,boolean ai) {
        var c=RUNS.get(b).crew;
        if(c.human && c.leader.getMapId()==b.getMapId())return BotOpqRun.hold(e,b,ai);
        return BotMagatiaRun.portal(e,b,portal,ai);
    }
    private static boolean collect(BotEntry e,Character b,Set<Integer> wanted,boolean ai) {
        var s=RUNS.get(b);
        var d=b.getMap().getDroppedItems().stream().filter(i->!i.isPickedUp() && wanted.contains(i.getItemId()) && i.canBePickedBy(b)
                && !(s.pendingItem==i.getItemId() && System.currentTimeMillis()<s.pending)
                && !(s.crew.human && i.getItemId()==STONE && i.isPlayerDrop()))
                .min(Comparator.comparingDouble(i->b.getPosition().distanceSq(i.getPosition()))).orElse(null);
        if(d==null)return false;BotZakumPqRun.stepTowardsAndPickUp(e,b,d,ai);return true;
    }
    private static boolean deliver(BotEntry e,Character b,State s,int item,int quantity,boolean ai) {
        var h=s.crew.leader;
        if(h.getMapId()!=b.getMapId())return BotOpqRun.hold(e,b,ai);
        if(b.getPosition().distanceSq(h.getPosition())>30*30) { BotZakumPqRun.walkTo(e,b,h.getPosition(),ai);return true; }
        BotOpqRun.hold(e,b,ai);
        if(ready(s) && BotManager.getInstance().issueDropItem(e,InventoryType.ETC,item,(short)quantity)) {
            speak(s.crew,"handoff-"+item+"-"+b.getId(),item==STONE?"Here is the purple stone. Pick it up and speak to the Sprite.":"Here are my monster marbles. Collect 20 and speak to Ellin.");
        }
        return true;
    }
    private static boolean dropAt(BotEntry e,Character b,State s,Reactor r,int item,boolean ai) {
        var area=r.getArea();
        // Reactor areas describe dropped-item pixels, not necessarily a standable floor.
        var point=BotPhysicsEngine.findGroundPoint(b.getMap(),new Point((int)area.getCenterX(),area.y));
        if(point==null || !area.contains(point))return BotOpqRun.hold(e,b,ai);
        if(!area.contains(b.getPosition()) || e.inAir || e.climbing) { BotZakumPqRun.walkTo(e,b,point,ai);return true; }
        BotOpqRun.hold(e,b,ai);
        if(ready(s) && BotManager.getInstance().issueDropItem(e,InventoryType.ETC,item,(short)1)) { s.pendingItem=item;s.pending=System.currentTimeMillis()+5600; }
        return true;
    }
    private static boolean talk(BotEntry e,Character b,int npc,boolean ai) {
        var status=BotTravelManager.tickApproachNpc(e,b,b.getMapId(),npc,2,ai,false,b.getMapId()==RECRUIT?250:140);
        if(status==BotTravelManager.ApproachStatus.ARRIVED) {
            BotOpqRun.hold(e,b,ai);
            if(ready(RUNS.get(b))) { if(b.getClient().getCM()==null)BotNpcInteraction.talk(b,npc);else BotNpcInteraction.next(b,0); }
        }
        return true;
    }
    private static boolean ready(State s) { long now=System.currentTimeMillis();if(now<s.next)return false;s.next=now+ThreadLocalRandom.current().nextLong(900,1300);return true; }
    private static void speak(Crew c,String key,String text) { synchronized(c) { if(c.human && c.said.add(key)) { var e=BotManager.getInstance().getEntryByBotCharId(c.collector.getId());if(e!=null)BotPqChat.queue(e,text,false); } } }
    private static void notice(Crew c,String key,String text) { synchronized(c) { if(c.human && c.said.add(key))c.leader.dropMessage(5,text); } }
}
