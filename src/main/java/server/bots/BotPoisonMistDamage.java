package server.bots;

import client.Character;
import io.netty.buffer.Unpooled;
import net.packet.ByteBufInPacket;
import net.server.channel.handlers.TakeDamageHandler;
import server.life.MobSkillFactory;
import server.life.MobSkillType;
import server.maps.Mist;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/** Client-side contact ticks for monster poison clouds in event instances. */
final class BotPoisonMistDamage {
    private record Contact(server.maps.MapleMap map,long nextHit) {}
    private static final Map<Character,Contact> CONTACTS=Collections.synchronizedMap(new WeakHashMap<>());
    private static final TakeDamageHandler DAMAGE=new TakeDamageHandler();
    private BotPoisonMistDamage() {}

    static void tick(BotEntry entry,Character bot,long now) {
        if(bot.getEventInstance()==null||bot.getHp()<=0) {CONTACTS.remove(bot);return;}
        var contact=CONTACTS.get(bot);
        if(contact!=null&&contact.map==bot.getMap()&&now<contact.nextHit)return;
        var bounds=BotCombatManager.getBotTouchBounds(entry,bot);
        int damage=0;
        for(var object:bot.getMap().getMapObjects()) {
            if(!(object instanceof Mist mist)||!mist.isMobMist()||!mist.isPoisonMist()
                    ||!mist.getBox().intersects(bounds))continue;
            var owner=mist.getMobOwner();
            if(owner==null)continue;
            // Overlapping clouds share the contact clock; do not multiply damage by cloud count.
            damage=Math.max(damage,damage(mist));
        }
        if(damage<=0)return;
        CONTACTS.put(bot,new Contact(bot.getMap(),now+1000));
        report(bot,damage);
    }

    static int damage(Mist mist) {
        // The normal spawn packet exposes the exact skill/level of this cloud,
        // including clouds left behind when their owner changes boss phase.
        var p=ByteBuffer.wrap(mist.makeSpawnData().getBytes()).order(ByteOrder.LITTLE_ENDIAN);
        p.position(14);
        if(p.getInt()!=MobSkillType.AREA_POISON.getId())return 0;
        return MobSkillFactory.getMobSkill(MobSkillType.AREA_POISON,Byte.toUnsignedInt(p.get()))
                .map(skill->Math.max(0,skill.getX())).orElse(0);
    }

    static void report(Character bot,int damage) {
        int oldHp=bot.getHp(),oldMp=bot.getMp();
        var bytes=ByteBuffer.allocate(10).order(ByteOrder.LITTLE_ENDIAN)
                .putInt(net.server.Server.getInstance().getCurrentTimestamp())
                .put((byte)-4).put((byte)0).putInt(damage).array();
        var buffer=Unpooled.wrappedBuffer(bytes);
        try {DAMAGE.handlePacket(new ByteBufInPacket(buffer),bot.getClient());}finally{buffer.release();}
        if(bot.getHp()>0)bot.triggerAutopotAfterStatLoss(bot.getHp()-oldHp,bot.getMp()-oldMp);
    }
}
