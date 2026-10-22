package server.bots;

import scripting.event.EventInstanceManager;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/** Stage guidance uses the ordinary chat queue, but expires before delivery if obsolete. */
final class BotPqChat {
    private record Context(EventInstanceManager event, int map, boolean uncleared) {}
    private static final ThreadLocal<Context> ENQUEUING = new ThreadLocal<>();
    private static final Map<BotChatManager.QueuedMessage, Context> CONTEXTS = Collections.synchronizedMap(new WeakHashMap<>());
    private BotPqChat() {}
    static void queue(BotEntry entry, String text, boolean uncleared) {
        var previous=ENQUEUING.get();
        ENQUEUING.set(new Context(entry.bot.getEventInstance(),entry.bot.getMapId(),uncleared));
        try { BotChatManager.queueBotSay(entry,text); }
        finally { if(previous==null) ENQUEUING.remove(); else ENQUEUING.set(previous); }
    }
    static void capture(BotChatManager.QueuedMessage message) {
        var context=ENQUEUING.get();
        if(context!=null) CONTEXTS.put(message,context);
    }
    static boolean isCurrent(BotEntry entry, BotChatManager.QueuedMessage message) {
        var context=CONTEXTS.remove(message);
        if(context==null) return true; // ordinary social and supply messages are unchanged
        var bot=entry.bot;
        if(bot.getEventInstance()!=context.event || bot.getMapId()!=context.map) return false;
        if(context.map==BotZakumBattle.ALTAR) {
            boolean armNotice=message.text.startsWith("Arm Phase:");
            int body=message.text.startsWith("Body Phase One:")?8800000
                    :message.text.startsWith("Body Phase Two:")?8800001:message.text.startsWith("Body Phase Three:")?8800002:-1;
            if(armNotice||body>=0) {
                boolean armsAlive=false,bodyActive=false;
                for(var mob:bot.getMap().getAllMonsters())if(mob.isAlive()) {
                    if(mob.getId()>=8800003&&mob.getId()<=8800010)armsAlive=true;
                    if(mob.getId()==body&&!mob.isFake())bodyActive=true;
                }
                return armNotice?armsAlive:!armsAlive&&bodyActive;
            }
        }
        int stage=context.map-BotKpqRun.FIRST_MAP+1;
        if(context.map>=BotLpqRun.FIRST_MAP && context.map<=922010900) {
            int lpqStage=(context.map-BotLpqRun.FIRST_MAP)/100+1;
            return !context.uncleared || context.event==null || context.event.getProperty(lpqStage+"stageclear")==null;
        }
        return !context.uncleared || context.event==null || stage<1 || stage>5
                || context.event.getProperty(stage+"stageclear")==null;
    }
}
