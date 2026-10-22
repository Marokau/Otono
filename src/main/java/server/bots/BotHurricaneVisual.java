package server.bots;

import client.Character;
import client.Disease;
import constants.skills.Bowmaster;
import server.maps.MapleMap;
import tools.PacketCreator;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/** Remote-client held-skill presentation; damage and cadence remain in BotHurricane. */
final class BotHurricaneVisual {
    private record Visual(WeakReference<MapleMap> map, Point position, int stance, long refreshed) {}
    private static final Map<Character,Visual> ACTIVE=Collections.synchronizedMap(new WeakHashMap<>());
    private BotHurricaneVisual() {}

    static synchronized void refresh(Character bot,BotCombatManager.AttackPlan plan,long now) {
        var map=bot.getMap();if(map==null)return;
        var old=ACTIVE.get(bot);
        if(old==null||old.map.get()!=map||!old.position.equals(bot.getPosition())||old.stance!=plan.stance||now-old.refreshed>670) {
            stop(bot);
            // SKILL_EFFECT's final three bytes are the packed action/direction short and attack speed.
            // PacketCreator's historical names (flags, speed, direction) obscure that wire layout.
            int action=(plan.direction&0x7fff)|((plan.stance&0x80)!=0?0x8000:0);
            map.broadcastMessage(bot,PacketCreator.skillEffect(bot,Bowmaster.HURRICANE,plan.skillLevel,
                    (byte)action,(action>>>8)&0xff,(byte)plan.speed),false);
        }
        ACTIVE.put(bot,new Visual(new WeakReference<>(map),new Point(bot.getPosition()),plan.stance,now));
    }

    static synchronized void tick(BotEntry entry,long now) {
        var bot=entry.bot;var old=ACTIVE.get(bot);if(old==null)return;
        if(!bot.isAlive()||bot.getMap()!=old.map.get()||!old.position.equals(bot.getPosition())
                ||entry.inAir||entry.climbing||entry.moveDir!=0
                ||bot.hasDisease(Disease.STUN)||bot.hasDisease(Disease.SEAL)||now-old.refreshed>750)
            BotHurricane.cancel(bot);
    }

    static synchronized void stop(Character bot) {
        var old=ACTIVE.remove(bot);if(old==null)return;
        var map=old.map.get();if(map!=null)
            map.broadcastMessage(bot,PacketCreator.skillCancel(bot,Bowmaster.HURRICANE),false);
    }
}
