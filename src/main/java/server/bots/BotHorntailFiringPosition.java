package server.bots;

import client.Character;
import client.inventory.WeaponType;
import server.life.Monster;
import server.bots.combat.BotMobHitboxProvider;
import java.awt.Point;

/** Conservative raid positioning, in addition to normal weapon/skill reach. */
final class BotHorntailFiringPosition {
    private BotHorntailFiringPosition() {}
    static boolean allows(Character bot, Point position, Monster target) {
        if (!BotHorntailRun.inInstance(bot) || target.getId()<8810000 || target.getId()>8810006) return true;
        var weapon=BotAttackExecutionProvider.getEquippedWeaponType(bot);
        if (weapon!=WeaponType.CLAW && weapon!=WeaponType.BOW && weapon!=WeaponType.CROSSBOW && weapon!=WeaponType.GUN) return true;
        var bounds=BotMobHitboxProvider.getInstance().getMobBounds(target);
        if (bounds==null || position==null) return false;
        int id=target.getId();
        if (bot.getMapId()==240060200) {
            int center=target.getPosition().x;
            if ((id==8810002 || id==8810005) && position.x>=center) return false;
            if ((id==8810004 || id==8810006) && position.x<=center) return false;
        }
        // Use the existing 350px stance-search envelope rather than firing from
        // the extreme edge of the generic 600px Keen Eyes rectangle. This is
        // a positioning policy, not a claim about the client's maximum range.
        int gap=Math.max(bounds.x-position.x,position.x-(bounds.x+bounds.width));
        if(bot.getMapId()==240060200 && id>=8810002 && id<=8810004) {
            // Main-head platforms can sit just above the head's hitbox. Retain
            // native projectile height instead of shrinking it to the hand policy.
            var origin=bot.getPosition();
            var projectile=BotCombatManager.clientProjectileHitBox(bot,target.getPosition().x<position.x,1f);
            if(origin==null||projectile==null)return false;
            projectile.translate(position.x-origin.x,position.y-origin.y);
            return gap<=350 && projectile.intersects(bounds);
        }
        int shotY=position.y-30;
        return gap<=350 && shotY+15>=bounds.y && shotY-15<=bounds.y+bounds.height;
    }
}
