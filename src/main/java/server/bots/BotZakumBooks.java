package server.bots;

import client.Character;
import client.Job;
import client.inventory.InventoryType;
import client.inventory.manipulator.InventoryManipulator;
import client.processor.stat.SkillBookProcessor;
import constants.inventory.ItemConstants;

/** Owner-authorized level/build readiness, using real books, their requirements and success rolls. */
final class BotZakumBooks {
    private BotZakumBooks() {}
    static void prepare(Character bot) {
        var entry=BotManager.getInstance().getEntryByBotCharId(bot.getId());
        if(entry==null||bot.getEventInstance()!=null)throw new IllegalArgumentException("Prepare books before entering");
        if(!BotHorntailRaidBuild.VARIANT.equals(entry.spVariant)) {
            if(bot.getJob()==Job.HERO)entry.spVariant="2h";
            if(bot.getJob()==Job.BISHOP)entry.spVariant="boss-support";
        }
        // These dedicated fixtures also receive their skill-quest unlocks. NPCs such as
        // 2081200/2081300 use teachSkill(..., 0, 10, -1); actual SP still comes from leveling.
        // Only the bot's own planned fourth-job skills receive the initial cap, never free SP.
        for(int id=bot.getJob().getId()*10000;id<=bot.getJob().getId()*10000+1100;id++){
            var skill=client.SkillFactory.getSkill(id);
            if(skill!=null&&skill.isFourthJob()&&BotBuildManager.plannedSkillTarget(entry,bot,id)>0&&bot.getMasterLevel(skill)==0)
                bot.changeSkillLevel(skill,bot.getSkillLevel(skill),Math.min(10,skill.getMaxLevel()),-1);
        }
        BotBuildManager.respecSp(entry,bot);
        for(int pass=0;pass<100;pass++) {
            boolean used=false;
            for(int id=ItemConstants.SKILL_BOOK_FIRST;id<=ItemConstants.MASTERY_BOOK_LAST;id++){
                if(!ItemConstants.isSkillBook(id)||!BotSkillBookManager.canUseWantedBookNow(entry,bot,id))continue;
                var inv=bot.getInventory(InventoryType.USE);var book=inv.findById(id);
                if(book==null){if(!InventoryManipulator.addById(bot.getClient(),id,(short)1))throw new IllegalStateException("No room for skill book");book=inv.findById(id);}
                var result=SkillBookProcessor.useSkillBook(bot,book.getPosition(),id);
                if(result!=null&&result.success())BotBuildManager.autoAssignSp(entry,bot);
                used=true;
            }
            BotBuildManager.respecSp(entry,bot);
            if(!used)break;
        }
    }
}
