package server.bots;
import client.Character;
import client.Job;
/** Gear-only readiness floors, independent of temporary Hyper Body or Maple Warrior. */
final class BotRaidHp {
    private BotRaidHp() {}
    static int minimumUnbuffed(Character bot) {
        var job=bot.getJob();
        if(job==Job.HERO||job==Job.DARKKNIGHT||job==Job.PALADIN)return 15625;
        if(job==Job.BISHOP)return 6000;
        if(job==Job.NIGHTLORD||job==Job.BOWMASTER)return Math.max(7500,BotPinkBeanReadiness.rangedGoal(bot));
        return 0;
    }
}
