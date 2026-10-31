package server.bots;
import client.BotClient;
import client.Character;
import client.inventory.*;
import server.ItemInformationProvider;
import tools.DatabaseConnection;
import java.util.*;
import io.netty.buffer.Unpooled;
import net.packet.*;

/** Authorized gear-only HP preparation through the ordinary player ScrollHandler. */
final class BotRaidHpScrolls {
    private BotRaidHpScrolls() {}
    static boolean ordinaryHpScroll(Map<String,Integer> stats) {
        if(stats==null)return false;
        int chance=stats.getOrDefault("success",0),hp=stats.getOrDefault("MHP",0);
        return hp>0&&(chance==10||chance==30||chance==60||chance==100)
                &&stats.getOrDefault("cursed",0)==0&&stats.getOrDefault("PAD",0)==0&&stats.getOrDefault("MAD",0)==0
                &&(chance<100||hp<=10);
    }
    static int unbuffedHp(Character b) {
        int hp=b.getMaxHp();for(var item:b.getInventory(InventoryType.EQUIPPED).list())if(item instanceof Equip e)hp+=e.getHp();return hp;
    }
    static void prepare(Character b) {
        if(!(b.getClient() instanceof BotClient)||b.isGM()||b.getEventInstance()!=null)
            throw new IllegalArgumentException("HP scrolling is only for prepared bots outside their event");
        int minimum=BotRaidHp.minimumUnbuffed(b);if(unbuffedHp(b)>=minimum)return;
        var ii=ItemInformationProvider.getInstance();var ids=new ArrayList<Integer>();
        try(var db=DatabaseConnection.getConnection();var q=db.prepareStatement("SELECT itemid FROM drop_data WHERE itemid>=2040000 AND itemid<2050000 UNION SELECT itemid FROM shopitems WHERE itemid>=2040000 AND itemid<2050000");var rows=q.executeQuery()) {
            while(rows.next()){int id=rows.getInt(1);if(ordinaryHpScroll(ii.getEquipStats(id)))ids.add(id);}
        }catch(java.sql.SQLException ex){throw new IllegalStateException("Cannot read obtainable HP scrolls",ex);}
        var handler=new net.server.channel.handlers.ScrollHandler();int attempted=0,succeeded=0,replacements=0;
        for(int trial=0;trial<96&&unbuffedHp(b)<minimum;trial++) {
            Equip chosen=null;int scrollId=0;double best=-1;
            for(var item:b.getInventory(InventoryType.EQUIPPED).list())if(item instanceof Equip equip&&equip.getUpgradeSlots()>0&&!ii.isCash(equip.getItemId()))
                for(int id:ids)if(BotScrollManager.applicable(ii,id,equip.getItemId())) {
                    var stats=ii.getEquipStats(id);double gain=stats.get("MHP")*stats.get("success")/100.0;
                    if(gain>best){best=gain;chosen=equip;scrollId=id;}
                }
            if(chosen==null) {
                if(replacements>=8)break;
                Equip replacement=null;short destination=0;double improvement=0;
                for(var item:b.getInventory(InventoryType.EQUIPPED).list())if(item instanceof Equip worn&&!ii.isCash(worn.getItemId())) {
                    int maxGain=0;double expected=0;
                    for(int id:ids)if(BotScrollManager.applicable(ii,id,worn.getItemId())) {
                        var stats=ii.getEquipStats(id);maxGain=Math.max(maxGain,stats.get("MHP"));expected=Math.max(expected,stats.get("MHP")*stats.get("success")/100.0);
                    }
                    if(maxGain==0)continue;
                    Equip fresh=null;
                    for(var spare:b.getInventory(InventoryType.EQUIP).list())if(spare instanceof Equip candidate&&candidate.getItemId()==worn.getItemId()&&candidate.getUpgradeSlots()>0&&(fresh==null||candidate.getHp()+candidate.getUpgradeSlots()*expected>fresh.getHp()+fresh.getUpgradeSlots()*expected))fresh=candidate;
                    boolean existingSpare=fresh!=null;
                    for(int roll=0;!existingSpare&&roll<80;roll++){var e=ii.randomizeStats((Equip)ii.getEquipById(worn.getItemId()));if(fresh==null||e.getHp()>fresh.getHp())fresh=e;}
                    if(fresh==null||fresh.getUpgradeSlots()<=0||fresh.getHp()+fresh.getUpgradeSlots()*maxGain<=worn.getHp())continue;
                    double score=fresh.getHp()+fresh.getUpgradeSlots()*expected-worn.getHp();
                    if(replacement==null||score>improvement){replacement=fresh;destination=worn.getPosition();improvement=score;}
                }
                if(replacement==null)break;
                // A failed scroll keeps its failed slot. Try a distinct, normally
                // rolled spare instead of restoring slots or stats on the old item.
                if(replacement.getPosition()<=0) {
                    replacement.setPosition((short)0);
                    if(!client.inventory.manipulator.InventoryManipulator.addFromDrop(b.getClient(),replacement,false))break;
                }
                client.inventory.manipulator.InventoryManipulator.equip(b.getClient(),replacement.getPosition(),destination);
                if(b.getInventory(InventoryType.EQUIPPED).getItem(destination)!=replacement)throw new IllegalStateException("Native equip rejected HP-scroll spare");
                replacements++;continue;
            }
            BotZakumSupplies.stock(b,scrollId,1);var scroll=b.getInventory(InventoryType.USE).findById(scrollId);
            if(scroll==null)throw new IllegalStateException("HP scroll supply missing");
            int oldHp=chosen.getHp(),oldSlots=chosen.getUpgradeSlots(),oldQuantity=scroll.getQuantity();
            var packet=new ByteBufOutPacket();packet.writeInt(net.server.Server.getInstance().getCurrentTimestamp());packet.writeShort(scroll.getPosition());packet.writeShort(chosen.getPosition());packet.writeShort(0);
            var buffer=Unpooled.wrappedBuffer(packet.getBytes());try{handler.handlePacket(new ByteBufInPacket(buffer),b.getClient());}finally{buffer.release();}
            if(chosen.getHp()>oldHp)succeeded++;attempted++;
            if(chosen.getUpgradeSlots()==oldSlots&&scroll.getQuantity()==oldQuantity)throw new IllegalStateException("Native HP scroll handler rejected preparation");
        }
        for(int pass=0;pass<4;pass++)if(!BotEquipManager.autoEquip(b,b,null,true))break;
        org.slf4j.LoggerFactory.getLogger(BotRaidHpScrolls.class).info("Raid HP scrolling {} attempts={} successes={} spares={} unbuffedHP={} required={}",b.getName(),attempted,succeeded,replacements,unbuffedHp(b),minimum);
    }
}
