package server.bots;

import client.Character;
import scripting.event.EventInstanceManager;
import java.util.*;

/** One stage introduction from the collector, before the human-led crew sets off. */
final class BotLpqStageGuide {
    private static final Map<EventInstanceManager, Map<Integer,Long>> STARTED = new WeakHashMap<>();
    private BotLpqStageGuide() {}
    static synchronized boolean tick(BotEntry entry, Character bot, Character human, long now) {
        int map=bot.getMapId();
        if(map!=human.getMapId() || map==BotLpqRun.FIRST_MAP) return false;
        int stage=(map-BotLpqRun.FIRST_MAP)/100+1;
        if(stage>=2 && stage<=9 && (map%100!=0 || bot.getEventInstance().getProperty(stage+"stageclear")!=null)) return false;
        String text=switch(map) {
            case 922010200 -> "Stage Two: We will break boxes and collect 15 passes for you. One box sends us to a trap room; we will clear it and return. Pick up our delivery, then ask the stage NPC to check.";
            case 922010300 -> "Stage Three: We will break boxes and defeat the monsters they release. We need 32 passes. You can help fight; we will collect and bring the passes to you for the stage NPC.";
            case 922010400 -> "Stage Four: These dark rooms need both magic and physical attacks. We will sweep the rooms and bring you six passes. You can wait here or help, then check with the stage NPC.";
            case 922010500 -> "Stage Five: We need 24 passes from six rooms. Our mage handles the Teleport room and our thief uses Dark Sight past the guards. Stay clear of the guards and wait in the main room for deliveries.";
            case 922010600 -> "Stage Six: The box order is always the same. Want to solve it yourself? Take your time. Say 'box order' whenever you would like the answer. We will wait for you at the top.";
            case 922010700 -> "Stage Seven: We will shoot the Ratz. Leave their drops on the platforms: they summon Rombots automatically. Help us defeat those for three passes; we will bring them to you for the stage NPC.";
            case 922010800 -> "Stage Eight: We will stand on five boxes and try combinations. Please stay off the boxes. When we say the arrangement is ready, ask the stage NPC to check; we will adjust after a wrong answer.";
            case 922010900 -> "Stage Nine: We will hit the Ratz on its platform to summon Alishar, then help fight. We will bring you the boss's stage item for the Violet Balloon. The boss gear is yours.";
            case 922011000 -> "Bonus room! We will help break boxes, but all the items and mesos are yours. Pick up what you want while the timer lasts, then meet us at the reward NPC.";
            case 922011100 -> "LPQ rewards! Talk to the reward NPC and make sure you have inventory space. We will collect our rewards and return to recruitment.";
            default -> null;
        };
        if(text==null) return false;
        var started=STARTED.computeIfAbsent(bot.getEventInstance(),ignored->new HashMap<>());
        if(!started.containsKey(map) && BotLpqHuman.isCollector(bot)) {
            started.put(map,now);
            BotPqChat.queue(entry,text,true);
        }
        return !started.containsKey(map) || now-started.get(map)<7000;
    }
}
