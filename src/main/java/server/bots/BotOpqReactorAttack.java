package server.bots;

import client.Character;
import io.netty.buffer.Unpooled;
import net.packet.ByteBufInPacket;
import net.server.channel.handlers.AbstractDealDamageHandler;
import net.server.channel.handlers.CloseRangeDamageHandler;
import net.server.channel.handlers.ReactorHitHandler;
import server.maps.Reactor;
import java.awt.Point;
import java.util.HashMap;

/** OPQ-only reactor inputs: a normal weapon swing followed by the ordinary reactor packet. */
final class BotOpqReactorAttack {
    private BotOpqReactorAttack() {}
    static boolean hit(BotEntry entry,Character bot,Reactor reactor) {
        if(!reactor.isActive() || entry.attackCooldownMs>0)return false;
        var position=bot.getPosition();var target=reactor.getPosition();
        if(Math.abs(position.x-target.x)>70 || Math.abs(position.y-target.y)>45)return false;
        boolean left=target.x<position.x;
        // Point-blank normal attack selection gives bows/claws their real melee swing,
        // rather than putting a projectile action into a close-range broadcast.
        var data=BotAttackExecutionProvider.buildBasicAttackData(bot,new Point(position.x+(left?-1:1),position.y));
        var attack=new AbstractDealDamageHandler.AttackInfo();
        attack.skill=0;attack.skilllevel=0;attack.numDamage=1;attack.numAttacked=0;attack.numAttackedAndDamage=1;
        attack.speed=data.speed();attack.stance=data.stance();attack.direction=data.direction();attack.display=data.display();
        attack.targets=new HashMap<>();
        CloseRangeDamageHandler.applyCloseRangeEffects(attack,bot,bot.getClient());
        entry.attackCooldownMs=Math.max(entry.attackCooldownMs,data.cooldownMs());
        entry.facingDir=left?-1:1;BotCombatManager.markAlerted(entry);
        short reactorStance=(short)((entry.inAir?0:2)+(left?0:1));
        var packet=Unpooled.buffer().writeIntLE(reactor.getObjectId()).writeIntLE(left?0:1)
                .writeShortLE(reactorStance).writeIntLE(0).writeIntLE(0);
        try { new ReactorHitHandler().handlePacket(new ByteBufInPacket(packet),bot.getClient()); }
        finally { packet.release(); }
        return true;
    }
}
