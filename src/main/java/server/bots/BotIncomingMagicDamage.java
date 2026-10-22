package server.bots;

import client.Character;
import client.SkillFactory;
import constants.skills.DragonKnight;
import constants.skills.Priest;
import provider.DataProviderFactory;
import provider.DataTool;
import provider.wz.WZFiles;
import server.combat.CombatFormulaProvider;
import server.life.Monster;
import tools.StringUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/** Client-side incoming magic roll; shared TakeDamageHandler still owns damage and ailments. */
final class BotIncomingMagicDamage {
    private record Attack(int madOverride, boolean elemental) {}
    private static final Map<String,Attack> ATTACKS=new ConcurrentHashMap<>();
    private BotIncomingMagicDamage() {}

    static int roll(Character bot,Monster mob,int index) {
        if(!CombatFormulaProvider.getInstance().doesMobHit(bot,mob))return 0;
        var attack=ATTACKS.computeIfAbsent(mob.getId()+":"+index,ignored->{
            var root=DataProviderFactory.getDataProvider(WZFiles.MOB)
                    .getData(StringUtil.getLeftPaddedStr(mob.getId()+".img",'0',11));
            var info=root.getChildByPath("attack"+(index+1)+"/info");
            String element=DataTool.getString("elemAttr",info,"");
            return new Attack(DataTool.getInt("MADamage",info,-1),!element.isEmpty()&&!"P".equalsIgnoreCase(element));
        });
        int mad=attack.madOverride>=0?attack.madOverride:mob.getStats().getMADamage();
        double factor=ThreadLocalRandom.current().nextDouble(0.0075d,Math.nextUp(0.008d));
        double damage=onHit(bot,mad,factor);
        if(attack.elemental) {
            int resistance=0;
            for(int id:new int[]{Priest.ELEMENTAL_RESISTANCE,DragonKnight.ELEMENTAL_RESISTANCE}) {
                int level=bot.getSkillLevel(id);
                if(level>0)resistance=Math.max(resistance,SkillFactory.getSkill(id).getEffect(level).getX());
            }
            damage*=1-Math.clamp(resistance,0,100)/100.0d;
        }
        return Math.max(1,(int)Math.floor(damage));
    }

    /**
     * Pre-BB client research: https://www.southperry.net/showthread.php?tid=1033 (Mob Damage).
     * Cross-checked against eight captured v83 lightning hits: MAD 550, MDEF 258,
     * STR 27 / DEX 111 / LUK 755 predicts 2160..2312; observed 2166..2290.
     */
    static double onHit(Character bot,int attackMad,double factor) {
        int mad=Math.clamp(attackMad,0,1999);
        double defense=Math.max(0,bot.getTotalMdef())/4.0d+bot.getTotalStr()/28.0d
                +bot.getTotalDex()/24.0d+bot.getTotalLuk()/20.0d;
        double jobFactor=BotEquipManager.isMageJob(bot.getJob())?1.2d:1.0d;
        return Math.max(1,mad*(double)mad*factor-defense*jobFactor);
    }
}
