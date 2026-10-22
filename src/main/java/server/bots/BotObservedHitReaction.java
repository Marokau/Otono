package server.bots;

import client.BuffStat;
import client.Character;
import server.life.Monster;
import java.util.concurrent.ThreadLocalRandom;

/** Client-side recoil after an observed attack has already used the normal damage handler. */
final class BotObservedHitReaction {
    private BotObservedHitReaction() {}
    static void apply(BotEntry entry,Character bot,Monster mob) {
        if(entry.climbing||bot.getHp()<=0)return;
        Integer stance=bot.getBuffedValue(BuffStat.STANCE);
        if(stance!=null&&stance>0&&ThreadLocalRandom.current().nextFloat()<=Math.clamp(stance/100f,0f,1f))return;
        var position=bot.getPosition();
        float scale=BotMovementManager.cfg.TICK_MS/8.0f;
        int impulse=Math.round((mob.getPosition().x>position.x?-1f:1f)*BotCombatManager.cfg.KNOCKBACK_HSPEED*scale);
        BotHurricane.cancel(bot);
        entry.grindTarget=null;entry.attackCooldownMs=0;entry.moveWindowMs=0;
        BotMovementManager.clearNavigationState(entry);entry.movementBroadcastValid=false;
        if(entry.inAir)BotPhysicsEngine.applyAirKnockback(entry,bot,impulse);
        else BotPhysicsEngine.beginKnockback(entry,bot,position,-BotCombatManager.cfg.KNOCKBACK_VFORCE*scale,impulse);
        BotMovementManager.broadcastMovement(entry);
    }
}
