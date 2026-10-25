package server.bots;
import client.Character;
import client.BotClient;
import client.inventory.InventoryType;
/** Protector Rock is survival equipment throughout GPQ, not a stat-scored accessory. */
final class BotGuildEquipment {
 private BotGuildEquipment(){}
 static boolean keepsProtection(Character bot,short slot){
  if(bot==null || slot!=-4 || !(bot.getClient() instanceof BotClient) || !BotGuildCompanions.inInstance(bot)
    || bot.getMapId()<BotGuildCompanions.OUTSKIRTS)return false;
  var item=bot.getInventory(InventoryType.EQUIPPED).getItem(slot);
  return item!=null && item.getItemId()==BotGuildCompanions.EARRING;
 }
}
