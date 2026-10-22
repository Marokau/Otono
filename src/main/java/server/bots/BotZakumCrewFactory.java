package server.bots;

import client.BotClient;
import client.Character;
import client.Job;
import tools.DatabaseConnection;

/** Offline boss fixture preparation, sharing ordinary level/job/AP/SP paths. */
final class BotZakumCrewFactory {
    private BotZakumCrewFactory() {}
    static int create(String name,Job target,int world,int channel) {
        var second=Job.getById(target.getId()/10*10);
        int id=BotPqCrewFactory.createLpqPlaceholder(name,second,world,channel);
        var client=new BotClient(world,channel);
        try {
            var bot=Character.loadCharFromDB(id,client,true);client.setPlayer(bot);client.setAccID(bot.getAccountID());
            var entry=new BotEntry(bot,bot,null);entry.personality=BotPersonality.parse(BotConfigService.getInstance().load(id));
            entry.apAuto=true;entry.kpq.recruiting=true;
            var reference=server.life.LifeFactory.getMonster(8800002);
            if(target==Job.NIGHTLORD)entry.spVariant="claw";
            if(target==Job.HERO)entry.spVariant="2h";
            while(bot.getLevel()<150) {
                if(bot.getLevel()==70||bot.getLevel()==120){
                    var old=bot.getJob();var next=Job.getById(old.getId()+1);
                    bot.changeJob(next);BotBuildManager.handleJobAdvance(entry,bot,old,next);
                }
                bot.levelUp(false);BotBuildManager.autoAssignSp(entry,bot);
                BotBuildManager.maybeRecomputeAutonomousApBuild(entry,bot);BotBuildManager.autoAssignAp(entry,bot);
            }
            entry.aspirationalMobLevel=reference.getLevel();entry.aspirationalMobAvoid=reference.getAvoidability();
            BotBuildManager.maybeRecomputeAutonomousApBuild(entry,bot);BotBuildManager.autoAssignAp(entry,bot);
            if(bot.getJob()!=target)throw new IllegalStateException("Wrong fixture job");
            bot.saveCharToDB(true);
            try(var c=DatabaseConnection.getConnection();var s=c.prepareStatement("UPDATE characters SET map=?,spawnpoint=0 WHERE id=?")){
                s.setInt(1,BotZakumBattle.RECRUIT);s.setInt(2,id);s.executeUpdate();
            }
            return id;
        } catch(java.sql.SQLException failure){throw new IllegalStateException("Could not prepare Zakum fixture",failure);}
    }
}
