package server.bots;

import client.Character;
import client.BuffStat;
import client.SkillFactory;
import constants.skills.Bishop;
import io.netty.buffer.Unpooled;
import net.PacketProcessor;
import net.packet.ByteBufInPacket;
import scripting.event.EventInstanceManager;
import java.awt.Point;
import java.util.*;

/** Visible party preparation and ordinary Bishop resurrection; no HP or buff grants. */
final class BotZakumSupport {
    private static final Set<EventInstanceManager> PREPARED=Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    static final Point STAGING=new Point(-250,-216);
    private BotZakumSupport() {}
    static boolean prepared(Character bot,List<Character> crew) {
        var event=bot.getEventInstance();if(PREPARED.contains(event))return true;
        for(var member:crew)if(member.getMapId()!=BotZakumBattle.ALTAR||member.getPosition().distanceSq(STAGING)>50*50
                ||member.getHp()<=0||member.getBuffedValue(BuffStat.HYPERBODYHP)==null
                ||BotEquipManager.isMageJob(member.getJob())&&member.getBuffedValue(BuffStat.MAGIC_GUARD)==null)return false;
        if(PREPARED.add(event)) {
            for (var member : crew) {
                if (member.getParty() == null) continue;
                var leader=member.getWorldServer().getPlayerStorage().getCharacterById(member.getParty().getLeaderId());
                if(leader!=null&&!(leader.getClient() instanceof client.BotClient)) {
                    leader.dropMessage(5,"Zakum: Hyper Body and Magic Guard are ready. Drop your Eye of Fire at the altar when you are ready to summon.");
                    break;
                }
            }
        }
        return true;
    }
    static boolean resurrect(BotEntry entry,Character bot,List<Character> crew,boolean ai) {
        if(bot.hasDisease(client.Disease.STUN)||bot.hasDisease(client.Disease.SEAL))return false;
        int id=Bishop.RESURRECTION,level=bot.getSkillLevel(id);
        if(level<=0||bot.skillIsCooling(id)||entry.attackCooldownMs>0||entry.inAir||entry.climbing)return false;
        var skill=SkillFactory.getSkill(id);var effect=skill.getEffect(level);
        if(!effect.canPaySkillCost(bot))return false;
        for(var member:crew)if(member!=bot&&member.getHp()<=0&&member.getMap()==bot.getMap()
                && bot.getParty()!=null && member.getParty()!=null && bot.getParty().getId()==member.getParty().getId()){
            if(!effect.calculateBoundingBox(bot.getPosition(),bot.isFacingLeft()).contains(member.getPosition())){BotZakumPqRun.walkTo(entry,bot,member.getPosition(),ai);return true;}
            var bytes=BotCombatManager.buildSupportSpecialMovePacket(bot,id,level,net.server.Server.getInstance().getCurrentTimestamp());
            var buffer=Unpooled.wrappedBuffer(bytes);
            try {
                var packet=new ByteBufInPacket(buffer);var opcode=packet.readShort();
                var handler=PacketProcessor.getProcessor(bot.getWorld(),bot.getClient().getChannel()).getHandler(opcode);
                if(handler!=null&&handler.validateState(bot.getClient())){
                    handler.handlePacket(packet,bot.getClient());entry.attackCooldownMs=Math.max(1000,skill.getAnimationTime());
                }
            }finally{buffer.release();}
            return BotOpqRun.hold(entry,bot,ai);
        }
        return false;
    }
}
