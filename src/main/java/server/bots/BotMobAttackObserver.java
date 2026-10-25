package server.bots;

import client.Character;
import io.netty.buffer.Unpooled;
import net.opcodes.SendOpcode;
import net.packet.ByteBufInPacket;
import net.packet.Packet;
import net.server.channel.handlers.TakeDamageHandler;
import provider.*;
import provider.wz.WZFiles;
import server.bots.combat.BotDefenseDataProvider;
import server.life.Monster;
import server.maps.MapleMap;
import tools.StringUtil;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Headless client collision reports for observed Zakum, Poison Golem, Ergoth, MV and spike-trap attacks. */
public final class BotMobAttackObserver {
    record Attack(int type, Rectangle range, int start, int areaCount, int delay, int pad, boolean deadly, boolean magic) {}
    record Cast(MapleMap map, Monster mob, Attack attack, int index, int mask, Point origin, long due) {}
    private static final Map<Character,Queue<Cast>> PENDING=Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<String,Optional<Attack>> DATA=new ConcurrentHashMap<>();
    private static final TakeDamageHandler DAMAGE=new TakeDamageHandler();
    private BotMobAttackObserver() {}

    public static void observe(Character bot,Packet packet) {
        byte[] bytes=packet.getBytes();
        if(bytes.length<17)return;
        var p=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        if(Short.toUnsignedInt(p.getShort())!=SendOpcode.MOVE_MONSTER.getValue())return;
        int oid=p.getInt();p.get();p.get();int action=Byte.toUnsignedInt(p.get());
        if(action<24||action>41)return;
        int mask=p.getInt();
        var origin=new Point(p.getShort(),p.getShort());
        var object=bot.getMap().getMapObject(oid);
        if(!(object instanceof Monster mob)||mob.isFake())return;
        if(!(mob.getId()>=8800000&&mob.getId()<=8800010)
                && !(mob.getId()>=9300180&&mob.getId()<=9300182&&BotEllinRun.inInstance(bot))
                && !(mob.getId()==9300028&&BotGuildCompanions.inInstance(bot))
                && !((mob.getId()==9400589||mob.getId()==9400704||mob.getId()>=9400741&&mob.getId()<=9400748)&&BotMvpqRun.inInstance(bot)))return;
        int index=(action-24)/2;
        var attack=attack(mob.getId(),index);
        if(attack==null||attack.type!=0&&attack.type!=3)return;
        synchronized(PENDING) {
            PENDING.computeIfAbsent(bot,ignored->new ConcurrentLinkedQueue<>())
                    .add(new Cast(bot.getMap(),mob,attack,index,mask,origin,System.currentTimeMillis()+attack.delay));
        }
    }

    static Attack attack(int mobId,int index) {
        return DATA.computeIfAbsent(mobId+":"+index,ignored->{
            var root=DataProviderFactory.getDataProvider(WZFiles.MOB).getData(StringUtil.getLeftPaddedStr(mobId+".img",'0',11));
            var info=root==null?null:root.getChildByPath("attack"+(index+1)+"/info");
            if(info==null)return Optional.empty();
            // Ground-only tremors need their own airborne-avoidance rule, not a blanket area hit.
            if(DataTool.getInt("jumpAttack",info,0)!=0 && mobId!=9400589 && mobId!=9400748)return Optional.empty();
            var range=info.getChildByPath("range");
            if(range==null||range.getChildByPath("lt")==null||range.getChildByPath("rb")==null)return Optional.empty();
            Point lt=DataTool.getPoint("lt",range),rb=DataTool.getPoint("rb",range);
            return Optional.of(new Attack(DataTool.getInt("type",info,0),new Rectangle(lt.x,lt.y,rb.x-lt.x,rb.y-lt.y),
                    DataTool.getInt("start",range,0),DataTool.getInt("areaCount",range,0),DataTool.getInt("attackAfter",info,0),
                    DataTool.getInt("PADamage",info,0),DataTool.getInt("deadlyAttack",info,0)!=0,DataTool.getInt("magic",info,0)!=0));
        }).orElse(null);
    }

    static java.util.List<Rectangle> areas(Cast cast) {
        var a=cast.attack;var result=new ArrayList<Rectangle>();
        if(a.type==0) {
            var r=new Rectangle(a.range);r.translate(cast.origin.x,cast.origin.y);result.add(r);
        } else if(a.type==3) {
            // WZ columns have width rb.x-lt.x. Captured attack-option bits select columns;
            // the 0x0658 cast visibly used columns 3,4,6,9,10 in the body-one recording.
            for(int i=0;i<Math.min(cast.mob.getId()==9300182||cast.mob.getId()==9300028?32:16,a.areaCount);i++)if((cast.mask&(1<<i))!=0) {
                int x=cast.origin.x+(a.start+i)*a.range.width+a.range.width/2;
                Point ground=cast.map.getPointBelow(new Point(x,cast.origin.y));
                if(ground!=null) {var r=new Rectangle(a.range);r.translate(x,ground.y);result.add(r);}
            }
        }
        return result;
    }

    static void tick(BotEntry entry,Character bot) {
        BotPoisonMistDamage.tick(entry,bot,System.currentTimeMillis());
        Queue<Cast> queue; synchronized(PENDING){queue=PENDING.get(bot);}
        if(queue==null)return;
        if(!BotZakumBattle.inInstance(bot)&&!BotEllinRun.inInstance(bot)&&!BotGuildCompanions.inInstance(bot)&&!BotMvpqRun.inInstance(bot)){synchronized(PENDING){PENDING.remove(bot);}return;}
        long now=System.currentTimeMillis();
        // Different attack delays need not be FIFO ordered.
        for(var cast:queue)if(now>=cast.due&&queue.remove(cast)) {
            if(cast.map!=bot.getMap()||!cast.mob.isAlive()||bot.getHp()<=0||entry.mobHitCooldownMs>0)continue;
            // MV attack one is a ground-only quake (WZ jumpAttack=1).
            if((cast.mob.getId()==9400589||cast.mob.getId()==9400748)&&cast.index==0&&(entry.inAir||entry.climbing))continue;
            Rectangle bounds=BotCombatManager.getBotTouchBounds(entry,bot);
            if(areas(cast).stream().noneMatch(r->r.intersects(bounds)))continue;
            int damage;
            if(cast.attack.deadly)damage=server.combat.CombatFormulaProvider.getInstance().doesMobHit(bot,cast.mob)?Math.max(0,bot.getHp()-1):0;
            else if(cast.attack.magic)damage=BotIncomingMagicDamage.roll(bot,cast.mob,cast.index);
            else damage=BotDefenseDataProvider.getInstance().rollPhysicalAttackDamage(bot,cast.mob,cast.attack.pad);
            report(entry,bot,cast.mob,cast.index,damage);
        }
    }

    static byte[] hitPacket(int timestamp,Monster mob,int index,int damage,int direction) {
        return ByteBuffer.allocate(23).order(ByteOrder.LITTLE_ENDIAN).putInt(timestamp).put((byte)index).put((byte)0)
                .putInt(damage).putInt(mob.getId()).putInt(mob.getObjectId()).put((byte)direction).putInt(0).array();
    }

    static void report(BotEntry entry,Character bot,Monster mob,int index,int damage) {
        int oldHp=bot.getHp(),oldMp=bot.getMp();
        int direction=mob.getPosition().x>bot.getPosition().x?0:1;
        var buffer=Unpooled.wrappedBuffer(hitPacket(net.server.Server.getInstance().getCurrentTimestamp(),mob,index,damage,direction));
        try {DAMAGE.handlePacket(new ByteBufInPacket(buffer),bot.getClient());}finally{buffer.release();}
        entry.mobHitCooldownMs=BotMovementManager.delayAfterCurrentTick(BotCombatManager.cfg.MOB_HIT_COOLDOWN_MS);
        // The shared handler applies damage/status; a real client also performs its recoil movement.
        // React only to actual stat loss, so misses/blocked hits do not shove headless characters.
        if(damage>0&&bot.getHp()>0&&(bot.getHp()<oldHp||bot.getMp()<oldMp))
            BotObservedHitReaction.apply(entry,bot,mob);
        if(bot.getHp()>0)bot.triggerAutopotAfterStatLoss(bot.getHp()-oldHp,bot.getMp()-oldMp);
    }
}
