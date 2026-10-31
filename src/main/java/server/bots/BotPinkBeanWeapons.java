package server.bots;
import client.Character;
import client.inventory.*;
import server.ItemInformationProvider;
import tools.DatabaseConnection;
import java.util.*;
import io.netty.buffer.Unpooled;
import net.packet.*;
/** Dedicated raid weapons/gloves: ordinary non-cursed scrolls, real failures and spent slots. */
final class BotPinkBeanWeapons {
 private BotPinkBeanWeapons() {}
 static boolean ordinaryAttackScroll(Map<String,Integer> stats,boolean magic){
  if(stats==null||stats.getOrDefault("cursed",0)!=0)return false;
  int chance=stats.getOrDefault("success",0),attack=stats.getOrDefault(magic?"MAD":"PAD",0);
  return attack>0&&(chance==10&&attack<=5||chance==60&&attack<=2||chance==100&&attack<=1);
 }
 static void prepare(Character b){
  if(!(b.getClient() instanceof client.BotClient)||b.isGM()||b.getEventInstance()!=null)throw new IllegalArgumentException("Scroll dedicated raid equipment outside the event");
  for(short slot:new short[]{-11,-8}) {
   if(slot==-8&&BotEquipManager.isMageJob(b.getJob()))continue;
   if(b.getInventory(InventoryType.EQUIPPED).getItem(slot) instanceof Equip equip&&equip.getUpgradeSlots()>0)prepareEquip(b,equip);
  }
 }
 private static void prepareEquip(Character b,Equip weapon){
  var ii=ItemInformationProvider.getInstance();boolean magic=BotEquipManager.isMageJob(b.getJob());int selected=0;double best=0;
  try(var db=DatabaseConnection.getConnection();var q=db.prepareStatement("SELECT itemid FROM drop_data WHERE itemid>=2040000 AND itemid<2050000 UNION SELECT itemid FROM shopitems WHERE itemid>=2040000 AND itemid<2050000");var rows=q.executeQuery()){
   while(rows.next()){int id=rows.getInt(1);var stats=ii.getEquipStats(id);if(!ordinaryAttackScroll(stats,magic)||!BotScrollManager.applicable(ii,id,weapon.getItemId()))continue;double score=stats.get(magic?"MAD":"PAD")*stats.get("success")/100.0;if(score>best||score==best&&id<selected){selected=id;best=score;}}
  }catch(java.sql.SQLException ex){throw new IllegalStateException("Cannot read obtainable weapon scrolls",ex);}
  if(selected==0){if(weapon.getPosition()==-11)throw new IllegalStateException("No ordinary weapon attack scroll for "+weapon.getItemId());return;}
  int oldWatk=weapon.getWatk(),oldMatk=weapon.getMatk(),attempts=0,successes=0;var handler=new net.server.channel.handlers.ScrollHandler();
  while(weapon.getUpgradeSlots()>0&&attempts<15){
   BotZakumSupplies.stock(b,selected,1);var scroll=b.getInventory(InventoryType.USE).findById(selected);if(scroll==null)throw new IllegalStateException("Weapon scroll missing");
   int slots=weapon.getUpgradeSlots(),quantity=scroll.getQuantity(),attack=magic?weapon.getMatk():weapon.getWatk();
   var p=new ByteBufOutPacket();p.writeInt(net.server.Server.getInstance().getCurrentTimestamp());p.writeShort(scroll.getPosition());p.writeShort(weapon.getPosition());p.writeShort(0);
   var buf=Unpooled.wrappedBuffer(p.getBytes());try{handler.handlePacket(new ByteBufInPacket(buf),b.getClient());}finally{buf.release();}
   if(weapon.getUpgradeSlots()==slots&&scroll.getQuantity()==quantity)throw new IllegalStateException("Native weapon scroll refused");
   if((magic?weapon.getMatk():weapon.getWatk())>attack)successes++;attempts++;
  }
  org.slf4j.LoggerFactory.getLogger(BotPinkBeanWeapons.class).info("Raid attack scrolling {} item={} scroll={} attempts={} successes={} watk={}->{} matk={}->{} slots={}",b.getName(),weapon.getItemId(),selected,attempts,successes,oldWatk,weapon.getWatk(),oldMatk,weapon.getMatk(),weapon.getUpgradeSlots());
 }
}
