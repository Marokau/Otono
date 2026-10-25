package server.bots;

import client.BotClient;
import client.Character;
import scripting.event.EventInstanceManager;
import server.life.Monster;
import server.maps.MapItem;
import java.util.*;

/** Human-led dedicated crews leave original boss loot for their player. */
public final class BotPqBossLoot {
    private static final Map<EventInstanceManager, Set<Integer>> ANNOUNCED = new WeakHashMap<>();
    private BotPqBossLoot() {}

    static boolean dedicatedHumanRun(BotEntry entry, Character bot) {
        var human=entry.owner;
        return human!=null && !(human.getClient() instanceof BotClient)
                && bot.getEventInstance()!=null && human.getEventInstance()==bot.getEventInstance()
                && !BotPqDialogue.allowsGearOffer(bot,human);
    }
    static boolean reserves(BotEntry entry, Character bot, MapItem drop) {
        return dedicatedHumanRun(entry,bot) && !drop.isPlayerDrop()
                && drop.getDropper() instanceof Monster monster && monster.isBoss();
    }
    public static void tick(BotEntry entry, Character bot) {
        // Rombards carry the data's boss flag, but stage seven is a summon-and-pass wave.
        if(bot.getMapId()==922010700 && BotLpqRun.inInstance(bot)) return;
        if(!dedicatedHumanRun(entry,bot)) return;
        var event=bot.getEventInstance();
        for(var member:event.getPlayers())
            if(member.getClient() instanceof BotClient && member.getMapId()==bot.getMapId()
                    && member.getId()<bot.getId() && !BotPqDialogue.allowsGearOffer(member,entry.owner)) return;
        for(var drop:bot.getMap().getDroppedItems()) {
            if(drop.isPickedUp() || !reserves(entry,bot,drop)) continue;
            synchronized(ANNOUNCED) {
                var said=ANNOUNCED.computeIfAbsent(event,ignored->new HashSet<>());
                if(said.add(drop.getDropper().getObjectId()))
                    BotPqChat.queue(entry,BotMvpqRun.inInstance(bot) && bot.getMapId()==BotMvpqRun.BOSS
                            ? "MV is down! The boss loot is yours. Pick it up before the automatic treasure-room warp in ten seconds."
                            : "The boss loot is yours! Take your time picking it up. Drop any gear you don't want if you'd like us to have it.",false);
            }
        }
    }
}
