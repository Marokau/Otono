package server.bots;

import client.BotClient;
import client.Character;
import client.inventory.InventoryType;
import net.packet.Packet;
import scripting.event.EventInstanceManager;
import java.util.*;

/** Human leadership, real inventory handoffs and visible puzzle feedback for Orbis companions. */
public final class BotOpqHuman {
    private static final class Crew {
        Character leader, collector;
        List<Character> bots;
        EventInstanceManager event;
        final Set<Integer> delivered = new HashSet<>();
        final Set<String> notices = new HashSet<>();
        final Map<Integer,Integer> wrong = new HashMap<>();
        long readySince;
        int readyMap, readyAttempt;
        boolean armed;
    }
    private static final Map<Character,Crew> RUNS = Collections.synchronizedMap(new WeakHashMap<>());
    private BotOpqHuman() {}
    static void start(Character human,List<Character> bots) {
        if(human.getClient() instanceof BotClient || bots.size()!=5) throw new IllegalArgumentException("A human and five companions are required");
        register(human,bots);
        var party=new ArrayList<Character>();party.add(human);party.addAll(bots);
        try { BotOpqRun.startCrew(party); }
        catch(RuntimeException failure) {for(var bot:bots)unregister(bot);throw failure;}
    }
    static void register(Character human,List<Character> bots) {
        var crew=new Crew();crew.leader=human;crew.bots=List.copyOf(bots);
        crew.collector=bots.stream().min(Comparator.comparingInt(Character::getId)).orElseThrow();
        for(var bot:bots)RUNS.put(bot,crew);
    }
    static void unregister(Character bot) {RUNS.remove(bot);}
    static Character leader(Character bot) {var c=RUNS.get(bot);return c==null?null:c.leader;}
    static boolean belongsTo(Character bot,int id) {var h=leader(bot);return h!=null&&h.getId()==id;}
    static boolean isCollector(Character bot) {var c=RUNS.get(bot);return c!=null&&c.collector==bot;}
    static boolean canCollect(Character bot,int item) {var c=RUNS.get(bot);if(c==null)return true;synchronized(c){return !c.delivered.contains(item);}}
    static int stage(int map) {return map>=920010601&&map<=920010604?920010600:map;}
    static void notice(Character bot,String key,String text) {var c=RUNS.get(bot);if(c!=null)synchronized(c){if(c.notices.add(key)){if(key.startsWith("puzzle-")||(key.startsWith("route-")&&!key.startsWith("route-Lounge lift-"))){var entry=BotManager.getInstance().getEntryByBotCharId(bot.getId());if(entry!=null)BotPqChat.queue(entry,text,true);}else c.leader.dropMessage(5,text);}}}

    static boolean beforeTick(BotEntry entry,Character bot,boolean ai) {
        var c=RUNS.get(bot);if(c==null)return false;
        var human=c.leader;
        boolean valid=human.getWorldServer().getPlayerStorage().getCharacterById(human.getId())==human
                &&human.getClient().getChannel()==bot.getClient().getChannel()&&bot.getParty()!=null&&human.getParty()!=null
                &&bot.getParty().getId()==human.getParty().getId()&&human.getParty().getLeaderId()==human.getId();
        if(!valid || BotOpqRun.inInstance(bot)&&!bot.getEventInstance().isEventCleared()&&human.getEventInstance()!=bot.getEventInstance()) {
            if(bot.getMapId()==BotOpqRun.RECRUIT_MAP){BotOpqCompanions.dismiss(bot);return true;}
            if(BotOpqRun.inInstance(bot)&&!bot.getEventInstance().isEventCleared()&&bot.getParty()!=null)
                net.server.world.Party.leaveParty(bot.getParty(),bot.getClient());
            if(bot.getMapId()==920011200)return BotOpqRun.talk(entry,bot,2013001,ai);
            if(bot.getMapId()!=920011300)return BotOpqRun.hold(entry,bot,ai);
        }
        if(valid && BotOpqRun.inInstance(bot) && BotPqStageGuide.tick(entry,bot,human,bot.getEventInstance(),"opq",System.currentTimeMillis()))
            return BotOpqRun.hold(entry,bot,ai);
        if(bot.getMapId()==920011200)return BotOpqRun.talk(entry,bot,2013001,ai);
        if(!BotOpqRun.inInstance(bot))return BotOpqRun.hold(entry,bot,ai);
        synchronized(c) {
            if(c.event!=bot.getEventInstance()) {
                c.event=bot.getEventInstance();c.delivered.clear();c.notices.clear();c.wrong.clear();c.readySince=0;c.armed=false;
                for(var member:c.bots)BotOpqGarden.reset(member);
            }
            int map=stage(bot.getMapId()),humanMap=stage(human.getMapId());
            if(map==920011300)return false;
            if(map==BotOpqRun.HUB) {
                for(int item=4001044;item<=4001049;item++)if(human.getItemQuantity(item,false)>0){
                    notice(bot,"offer-"+item,"OPQ: You have a statue piece from Eak. Bring it to the Center Tower and drop it beside us; we will place it on the statue. We will wait for your handoff.");
                    return BotOpqRun.hold(entry,bot,ai);
                }
                if(human.getItemQuantity(4001055,false)>0){notice(bot,"grass-offer","OPQ: Drop your Grass of Life at Minerva's statue's body. Then talk to Minerva when ready to leave.");return BotOpqRun.hold(entry,bot,ai);}
                if(humanMap==BotOpqRun.FIRST_MAP)return BotOpqRun.hold(entry,bot,ai);
                if(humanMap>=920010200&&humanMap<=920010700&&roomCleared(c,humanMap)){
                    notice(bot,"return-"+humanMap,"OPQ: "+roomName(humanMap)+" is clear. Return to the Center Tower when ready; we are waiting there.");
                    return BotOpqRun.hold(entry,bot,ai);
                }
                return false;
            }
            if(map>=920010200&&map<=920010800&&humanMap!=map) {
                if(roomCleared(c,map)&&map<=920010700)return false;
                notice(bot,"join-"+map,"OPQ: We are waiting in "+roomName(map)+". Join us when you are ready; the crew will handle its rooms and puzzles.");
                return BotOpqRun.hold(entry,bot,ai);
            }
            if(map>=920010200&&map<=920010700&&roomCleared(c,map))
                notice(bot,"return-"+map,"OPQ: "+roomName(map)+" is clear. We will collect its statue piece; return to the Center Tower when you are ready.");
            if(bot==c.collector) {
                if(map==920010200&&!roomCleared(c,map)&&deliver(entry,bot,4001050,30,ai))return true;
                if(bot.getMapId()==920010600&&!roomCleared(c,map)&&deliver(entry,bot,4001052,40,ai))return true;
                if(map==920010800&&deliver(entry,bot,4001055,1,ai))return true;
            }
            if(map==920010800&&c.delivered.contains(4001055))return BotOpqRun.hold(entry,bot,ai);
            return false;
        }
    }
    static boolean roomCleared(Character bot,int room){var c=RUNS.get(bot);return c!=null&&roomCleared(c,room);}
    private static boolean roomCleared(Crew c,int room) {
        int n=(room-920010100)/100;
        if(n<1||n>6||c.event==null)return false;
        return c.event.getIntProperty("statusStg"+n)==1 || n==3&&c.event.getIntProperty("statusStg3")==2;
    }
    static String roomName(int map) {
        return switch(stage(map)){case 920010200->"Walkway";case 920010300->"Storage";case 920010400->"Lobby";
            case 920010500->"Sealed Room";case 920010600->"Lounge";case 920010700->"On the Way Up";case 920010800->"Garden";default->"the Center Tower";};
    }
    static boolean deliver(BotEntry entry,Character bot,int item,int required,boolean ai) {
        var c=RUNS.get(bot);if(c==null||bot!=c.collector)return false;
        if(c.delivered.contains(item))return BotOpqRun.hold(entry,bot,ai);
        if(bot.getItemQuantity(item,false)+c.leader.getItemQuantity(item,false)<required)return false;
        if(bot.getMapId()!=c.leader.getMapId())return BotOpqRun.hold(entry,bot,ai);
        if(bot.getPosition().distanceSq(c.leader.getPosition())>30*30){BotZakumPqRun.walkTo(entry,bot,c.leader.getPosition(),ai);return true;}
        BotOpqRun.hold(entry,bot,ai);
        int quantity=bot.getItemQuantity(item,false);
        if(quantity==0||BotManager.getInstance().issueDropItem(entry,InventoryType.ETC,item,(short)quantity)){
            c.delivered.add(item);
            notice(bot,"deliver-"+item,item==4001055?"OPQ: Pick up the Grass of Life beside you, then use the Garden's return portal. It needs the party leader carrying the Grass."
                    :"OPQ: The stage pieces are beside you. Pick them up and talk to Eak yourself. Then return to the Center Tower with your statue piece.");
        }
        return true;
    }
    static boolean npc(BotEntry entry,Character bot,int npc,boolean ai) {
        var c=RUNS.get(bot);if(c==null)return false;
        if(npc==2013001&&bot.getMapId()!=920011200
                && !(bot.getMapId()==BotOpqRun.FIRST_MAP && bot.getEventInstance()!=null
                    && bot.getEventInstance().getIntProperty("statusStg0")==1)){
            notice(bot,"npc-"+bot.getMapId(),"OPQ: Talk to Eak here when ready. You make the stage checks; we will wait for your action.");return BotOpqRun.hold(entry,bot,ai);
        }
        if(npc==2013002&&bot.getMapId()==BotOpqRun.HUB&&c.leader.getMapId()!=920011300&&c.leader.getMapId()!=BotOpqRun.RECRUIT_MAP){
            notice(bot,"minerva-exit","OPQ: Minerva is restored! Check out the bonus room below before leaving. When you are ready to leave and claim your reward, talk to Minerva. We will wait for you.");return BotOpqRun.hold(entry,bot,ai);
        }
        return false;
    }
    static int consumeWrong(Character bot,int map) {
        var c=RUNS.get(bot);if(c==null)return 0;
        synchronized(c){return c.wrong.remove(map)==null?0:1;}
    }
    static void puzzleReady(Character bot,int attempt,boolean ready) {
        var c=RUNS.get(bot);if(c==null||bot!=c.collector)return;
        synchronized(c){
            if(!ready||c.leader.getMapId()!=bot.getMapId()){c.readySince=0;c.armed=false;return;}
            if(c.readyMap!=bot.getMapId()||c.readyAttempt!=attempt){c.readySince=0;c.armed=false;}
            c.readyMap=bot.getMapId();c.readyAttempt=attempt;
            if(c.readySince==0)c.readySince=System.currentTimeMillis();
            if(System.currentTimeMillis()-c.readySince>=1200){c.armed=true;notice(bot,"puzzle-"+c.readyMap+"-"+attempt,
                    "OPQ: Arrangement "+(attempt+1)+" is ready. "+(c.readyMap==920010500?"Stay off the three platforms and check Eak.":"Check Eak below; we handle the upper levers. You may stay with him."));}
        }
    }
    public static void observeEffect(Character bot,Packet packet) {
        var c=RUNS.get(bot);if(c==null||bot!=c.collector||!BotLpqPuzzle.isWrongEffect(packet))return;
        synchronized(c){if(c.armed&&c.readyMap==bot.getMapId()){c.wrong.put(c.readyMap,1);c.armed=false;c.readySince=0;}}
    }
    static void learned(Character bot,String puzzle,int row,int column) {
        notice(bot,"route-"+puzzle+"-"+row,"OPQ: "+puzzle+" row "+row+" uses column "+column+" (left to right). Learned by trying its portals.");
    }
}
