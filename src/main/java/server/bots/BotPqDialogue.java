package server.bots;

import client.BotClient;
import client.Character;
import java.util.*;

/** Companion-only guidance and social policy; observations never change PQ requirements. */
final class BotPqDialogue {
    private static final Set<Character> DEDICATED = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private static final Map<Character, State> KPQ = Collections.synchronizedMap(new WeakHashMap<>());
    private static final class State {
        Object event;
        int map;
        boolean delivered;
        final Set<String> said = new HashSet<>();
    }
    private BotPqDialogue() {}
    static void register(Character bot) { DEDICATED.add(bot); }
    static boolean allowsGearOffer(Character bot, Character recipient) {
        return !DEDICATED.contains(bot) && !DEDICATED.contains(recipient);
    }
    private static State state(Character bot) {
        synchronized (KPQ) {
            var s=KPQ.get(bot);
            if(s==null || s.event!=bot.getEventInstance() || s.map!=bot.getMapId()) {
                s=new State();s.event=bot.getEventInstance();s.map=bot.getMapId();KPQ.put(bot,s);
            }
            return s;
        }
    }
    private static void say(BotEntry e, State s, String key, String text) {
        synchronized (s) {
            if(!s.said.add(key)) return;
            if(key.equals("clear") || key.equals("ready") || key.equals("all-delivered"))
                e.owner.dropMessage(5,"KPQ: " + text);
            else BotPqChat.queue(e,text,true);
        }
    }
    static void progress(BotEntry e, Character bot, int have, int target) {
        if(!e.kpq.humanLed || target<=0) return;
        var s=state(bot);int remaining=Math.max(0,target-have);
        if(remaining==0) say(e,s,"coupons-done","Got all " + target + " coupons. Taking them to Cloto for my pass.");
        else if(remaining<=4) {
            synchronized(s) { s.said.add("halfway"); }
            say(e,s,"nearly-done","Just " + remaining + " more coupons, then I can get my pass.");
        } else if(have*2>=target)
            say(e,s,"halfway","Halfway done! I still need " + remaining + " coupons for my pass.");
    }
    static void handedPass(BotEntry e, Character bot, Character leader) {
        if(!e.kpq.humanLed) return;
        var s=state(bot);
        synchronized(KPQ) { s.delivered=true; }
        say(e,s,"handoff","Here's my pass, " + leader.getName() + ". Pick it up from your feet.");
    }
    static void tick(BotEntry e, Character bot, Character leader, int stage) {
        if(!e.kpq.humanLed) return;
        var event=bot.getEventInstance();
        var workers=event.getPlayers().stream().filter(p -> p.getClient() instanceof BotClient && p!=leader).toList();
        if(workers.stream().mapToInt(Character::getId).min().orElse(-1)!=bot.getId()) return;
        var s=state(bot);
        String intro=switch(stage) {
            case 1 -> "KPQ: Talk to Cloto. We each collect coupons for our question and bring you one pass. Pick up all "
                    + workers.size() + " passes, then talk to Cloto to clear this stage.";
            case 2,3,4 -> "Wait for us to say we are in position, then ask Cloto to check. If it is wrong, we will try new spots.";
            case 5 -> "Final stage: defeat every monster, including King Slime. Collect 10 passes and talk to Cloto. We will help fight.";
            case 6 -> "Bonus room! Hunt the monsters for extra EXP if you like, then talk to Nella to leave. We will follow you out.";
            default -> null;
        };
        if(intro!=null && (stage>5 || event.getProperty(stage+"stageclear")==null)) say(e,s,"intro",intro);
        if(stage<=5 && event.getProperty(stage+"stageclear")!=null) {
            say(e,s,"clear",stage==5 ? "King Slime's stage is clear! Talk to Cloto to claim your reward and enter the bonus room."
                    : "Stage clear! Go through the portal first and we will follow.");
            return;
        }
        if(stage==5 && leader.getMapId()==bot.getMapId() && leader.getPosition().y>=-743
                && bot.getPosition().y < -743) {
            // WZ map 103000804: in01_1 enters the boss section at y=-743.
            int remaining=0;
            for(var drop:bot.getMap().getDroppedItems())
                if(!drop.isPickedUp() && drop.getItemId()==4001008 && drop.getPosition().y < -743)
                    remaining+=drop.getItem().getQuantity();
            if(remaining>0) say(e,s,"waiting-upper-passes",
                    "We are waiting for you to collect " + remaining + " pass(es) on the platforms above before we head down to King Slime. Check near the portals; a pass can be hard to see there.");
        }
        if(stage!=1) return;
        int required=workers.size();
        if(required>0 && leader.getItemQuantity(4001008,false)>=required)
            say(e,s,"ready","You have all " + required + " passes! Talk to Cloto, then enter the opened portal. We will follow.");
        else {
            long delivered=workers.stream().filter(p -> state(p).delivered).count();
            if(delivered==required && required>0)
                say(e,s,"all-delivered","All our passes are delivered! Pick them up and talk to Cloto to clear stage one.");
            else if(delivered>0)
                say(e,s,"passes-"+delivered,delivered+" pass(es) delivered; we still need " + (required-delivered) + " more.");
        }
    }
}
