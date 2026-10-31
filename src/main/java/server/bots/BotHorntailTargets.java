package server.bots;
import client.Character;
import server.life.Monster;
/** Defeated-part decorations, aggregate HP and summon animations are not attackable parts. */
final class BotHorntailTargets {
    private BotHorntailTargets() {}
    static boolean canCollect(Character b,server.maps.MapItem drop){
        var crew=BotHorntailRun.crew(b);
        return crew==null||crew.leader==b||!drop.isPlayerDrop()||!(drop.getDropper() instanceof Character);
    }
    static boolean allowed(Character b,Monster m){return BotPinkBeanRun.canTarget(b,m) && (!BotHorntailRun.inInstance(b)||!constants.id.MobId.isDeadHorntailPart(m.getId())&&m.getId()!=constants.id.MobId.HORNTAIL&&(m.getId()<8810024||m.getId()>8810026));}
}
