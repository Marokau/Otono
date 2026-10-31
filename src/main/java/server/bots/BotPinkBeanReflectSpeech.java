package server.bots;

import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import server.life.Monster;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/** One immediate map-chat callout per actual reflect application, not cast warning. */
final class BotPinkBeanReflectSpeech {
    private static final Map<Monster,MonsterStatusEffect> ANNOUNCED=Collections.synchronizedMap(new WeakHashMap<>());
    private BotPinkBeanReflectSpeech() {}
    static String activated(Monster mob) {
        var effect=mob.getStati(MonsterStatus.WEAPON_REFLECT);
        if(effect==null)effect=mob.getStati(MonsterStatus.MAGIC_REFLECT);
        if(effect==null)return null;
        boolean physical=effect.getStati().containsKey(MonsterStatus.WEAPON_REFLECT);
        boolean magic=effect.getStati().containsKey(MonsterStatus.MAGIC_REFLECT);
        if(!physical&&!magic)return null;
        synchronized(ANNOUNCED) {
            if(ANNOUNCED.get(mob)==effect)return null;
            ANNOUNCED.put(mob,effect);
        }
        return physical&&magic?"Both reflects are active. Everyone, hold fire."
                :physical?"Physical reflect is active. Physical attackers, hold fire."
                :"Magic reflect is active. Magic attackers, hold fire.";
    }
    static void tick(BotEntry e) {
        for(var mob:e.bot.getMap().getAllMonsters()) {
            if(!mob.isAlive()||!BotPinkBeanRun.boss(mob.getId()))continue;
            var text=activated(mob);
            if(text!=null)BotManager.getInstance().botSay(e.bot,mob.getName()+": "+text);
        }
    }
}
