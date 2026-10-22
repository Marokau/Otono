package server.bots;

import client.Character;
import scripting.event.EventInstanceManager;
import java.util.*;

/** One optional-hint reminder while the human remains in the fixed box maze. */
final class BotLpqBoxReminder {
    private static final Map<EventInstanceManager, State> RUNS = new WeakHashMap<>();
    private static final class State {
        long entered;
        boolean asked, reminded;
    }
    private BotLpqBoxReminder() {}
    static synchronized void requested(EventInstanceManager event) {
        RUNS.computeIfAbsent(event,ignored->new State()).asked=true;
    }
    static synchronized void tick(Character bot, Character human, long now) {
        if(human.getMapId()!=922010600 || bot.getEventInstance()==null
                || bot.getEventInstance()!=human.getEventInstance() || !BotLpqHuman.isCollector(bot)) return;
        var state=RUNS.get(bot.getEventInstance());
        if(state==null) {
            state=new State();state.entered=now;RUNS.put(bot.getEventInstance(),state);
        }
        if(!state.asked && !state.reminded && now-state.entered>=60000) {
            state.reminded=true;
            human.dropMessage(6,bot.getName()+": Want a hand with the boxes? Say 'box order' and I will share the route. No rush if you want to keep solving it yourself.");
        }
    }
}
