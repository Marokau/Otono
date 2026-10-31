package server.bots;
import client.Character;
import server.life.Monster;
/** Statues use offset WZ hitboxes; their shared map origin is not an aiming point. */
final class BotPinkBeanTargets {
    private BotPinkBeanTargets() {}
    static java.awt.Point aim(Character b,Monster m){
        if(BotPinkBeanRun.inInstance(b)&&m.getId()>=8820002&&m.getId()<=8820006){
            var bounds=server.bots.combat.BotMobHitboxProvider.getInstance().getMobBounds(m);
            if(bounds!=null)return new java.awt.Point((int)bounds.getCenterX(),(int)bounds.getMaxY());
        }
        return m.getPosition();
    }
}
