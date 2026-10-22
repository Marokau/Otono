package server.bots;

import client.BotClient;
import client.Character;
import client.inventory.InventoryType;
import server.expeditions.ExpeditionType;
import java.util.*;

/** Dedicated expedition companions: ordinary registration, summon offering, combat and exit. */
public final class BotZakumBattle {
    public static final int RECRUIT = 211042400, ALTAR = 280030000;
    private static final class Crew {
        Character leader;
        List<Character> bots;
        boolean human, announced;
        long clearedAt;
    }
    private static final Map<Character,Crew> RUNS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Character,Long> ACTIONS = Collections.synchronizedMap(new WeakHashMap<>());
    private BotZakumBattle() {}
    static void start(Character leader,List<Character> bots,boolean human) {
        if (human ? bots.size() != 5 && bots.size() != 11 : bots.size() != 6) throw new IllegalArgumentException("Zakum crews require six or twelve participants");
        if (bots.stream().map(Character::getId).distinct().count() != bots.size()) throw new IllegalArgumentException("Duplicate Zakum companion");
        var crew = new Crew(); crew.leader=leader; crew.bots=List.copyOf(bots); crew.human=human;
        for (var bot : bots) {
            if (!(bot.getClient() instanceof BotClient) || bot.isGM() || bot.getEventInstance()!=null
                    || bot.getMapId()<211041900 || bot.getMapId()>RECRUIT || bot.getLevel()<150)
                throw new IllegalArgumentException("Ineligible Zakum companion");
        }
        var manager=BotManager.getInstance();
        for (int index = 0; index < bots.size(); index++) {
            var bot = bots.get(index);
            var partyLeader = human && bots.size() == 11 && index >= 5 ? bots.get(5) : leader;
            if (bot == partyLeader && human) {
                if (bot.getParty() != null) net.server.world.Party.leaveParty(bot.getParty(), bot.getClient());
                if (!net.server.world.Party.createParty(bot, true)) throw new IllegalStateException("Could not create second Zakum party");
            } else manager.partyUp(partyLeader,bot);
            RUNS.put(bot,crew);
            var entry=manager.getEntryByBotCharId(bot.getId());
            manager.resumeFromOperatorCommand(entry); manager.issueStop(entry);
        }
        if(leader.getParty()==null || leader.getParty().getMembers().size()!=6 || leader.getParty().getLeaderId()!=leader.getId())
            throw new IllegalStateException("Could not assemble Zakum party");
        if (human && bots.size() == 11) {
            var second = bots.get(5).getParty();
            if (second == null || second.getLeaderId() != bots.get(5).getId() || second.getMembers().size() != 6
                    || second.getId() == leader.getParty().getId()) throw new IllegalStateException("Could not assemble second Zakum party");
            for (int index = 0; index < bots.size(); index++) {
                var expected = index < 5 ? leader.getParty() : second;
                if (bots.get(index).getParty() == null || bots.get(index).getParty().getId() != expected.getId())
                    throw new IllegalStateException("Zakum companion joined the wrong party");
            }
        }
    }
    public static boolean inInstance(Character bot) { return bot!=null&&bot.getMapId()==ALTAR&&bot.getEventInstance()!=null; }
    static boolean belongsTo(Character bot,int leader) {var c=RUNS.get(bot);return c!=null&&c.leader.getId()==leader;}
    static boolean reservesLoot(Character bot) {var c=RUNS.get(bot);return c!=null&&c.human&&bot.getMapId()==ALTAR;}
    static void unregister(Character bot) {RUNS.remove(bot);ACTIONS.remove(bot);}
    public static boolean tick(BotEntry entry,Character bot,boolean ai) {
        var c=RUNS.get(bot);if(c==null)return false;
        entry.grinding=false;
        var human=c.leader;
        int index = c.bots.indexOf(bot);
        var partyLeader = c.human && c.bots.size() == 11 && index >= 5 ? c.bots.get(5) : human;
        boolean valid=human.getWorldServer().getPlayerStorage().getCharacterById(human.getId())==human
                &&bot.getParty()!=null&&human.getParty()!=null&&partyLeader.getParty()!=null
                &&bot.getParty().getId()==partyLeader.getParty().getId()
                &&partyLeader.getParty().getLeaderId()==partyLeader.getId()
                &&human.getParty().getLeaderId()==human.getId();
        if(c.human&&!valid) {
            if(bot.getEventInstance()!=null)return talk(entry,bot,2030010,0,ai);
            if(bot.getMapId()==RECRUIT){BotZakumCompanions.dismiss(bot);return true;}
        }
        if(bot.getMapId()==211042300){
            var portal=bot.getMap().getPortal("ps00");
            if(portal!=null)BotTravelManager.walkToPortalAndEnter(entry,bot,portal,System.currentTimeMillis(),ai);
            return true;
        }
        if(c.human&&!valid){BotTravelManager.tickTravel(entry,bot,211042300,24,ai,false);return true;}
        if(bot.getMapId()==RECRUIT) {
            if(c.clearedAt>0)return BotOpqRun.hold(entry,bot,ai);
            var expedition=bot.getClient().getChannelServer().getExpedition(ExpeditionType.ZAKUM);
            if(expedition!=null&&expedition.isLeader(human)) {
                if(!expedition.contains(bot))return talk(entry,bot,2030013,0,ai);
                if(expedition.isInProgress())return talk(entry,bot,2030013,0,ai);
                if(!c.human&&bot==human&&expedition.getMemberList().size()==c.bots.size())
                    return talk(entry,bot,2030013,2,ai);
            } else if(expedition==null&&!c.human&&bot==human)return talk(entry,bot,2030013,1,ai);
            return BotOpqRun.hold(entry,bot,ai);
        }
        if(!inInstance(bot)){
            if(bot.getEventInstance()==null)BotTravelManager.tickTravel(entry,bot,211042300,24,ai,false);
            else BotOpqRun.hold(entry,bot,ai);
            return true;
        }
        var event=bot.getEventInstance();
        if(c.human&&human.getEventInstance()!=event&&!event.isEventCleared())return talk(entry,bot,2030010,0,ai);
        if(c.human && !event.isEventCleared() && bot.getMap().getAllMonsters().isEmpty()
                && !"true".equals(event.getProperty("summoned"))
                && BotPqStageGuide.tick(entry,bot,human,event,"zakum",System.currentTimeMillis()))return BotOpqRun.hold(entry,bot,ai);
        if(c.human && !event.isEventCleared()) {
            String phase=null;boolean armsAlive=false;int activeBody=-1;
            for(var monster:bot.getMap().getAllMonsters()) {
                if(!monster.isAlive())continue;
                if(monster.getId()>=8800003 && monster.getId()<=8800010)armsAlive=true;
                if(!monster.isFake() && monster.getId()>=8800000 && monster.getId()<=8800002)
                    activeBody=Math.max(activeBody,monster.getId()-8800000);
            }
            if(armsAlive)phase="zakum-arms";
            else if(activeBody>=0)phase="zakum-body-"+activeBody;
            // Explain active phases without stopping attacks, healing or resurrection.
            if(phase!=null)BotPqStageGuide.tick(entry,bot,human,event,phase,System.currentTimeMillis());
        }
        if(BotLpqCures.tryCure(bot))return BotOpqRun.hold(entry,bot,ai);
        if(BotZakumSupport.resurrect(entry,bot,event.getPlayers(),ai))return true;
        if(event.isEventCleared()) {
            if(c.clearedAt==0)c.clearedAt=System.currentTimeMillis();
            if(c.human) {
                if(!c.announced){c.announced=true;human.dropMessage(5,"Zakum: Victory! Pick up your loot. We will wait until you leave through Amon.");}
                if(human.getMapId()==ALTAR)return BotOpqRun.hold(entry,bot,ai);
            } else {
                for(var object:bot.getMap().getDroppedItems())if(BotLootEligibility.canBotLoot(entry,bot,object)
                        && (object.getItemId()<=0||!server.ItemInformationProvider.getInstance().isPickupRestricted(object.getItemId())
                        ||bot.getItemQuantity(object.getItemId(),true)==0)){
                    BotZakumPqRun.stepTowardsAndPickUp(entry,bot,object,ai);return true;
                }
                if(System.currentTimeMillis()-c.clearedAt<15_000)return BotOpqRun.hold(entry,bot,ai);
            }
            return talk(entry,bot,2030010,0,ai);
        }
        if(!bot.getMap().getAllMonsters().isEmpty())return BotZakumCombat.tick(entry,bot,ai);
        if("true".equals(event.getProperty("summoned")))return BotOpqRun.hold(entry,bot,ai);
        if(!BotZakumSupport.prepared(bot,c.bots)){
            if(bot.getPosition().distanceSq(BotZakumSupport.STAGING)>40*40){BotZakumPqRun.walkTo(entry,bot,BotZakumSupport.STAGING,ai);return true;}
            return BotOpqRun.hold(entry,bot,ai);
        }
        if(c.human)return BotOpqRun.hold(entry,bot,ai);
        if(bot!=human)return BotOpqRun.hold(entry,bot,ai);
        var reactor=bot.getMap().getReactorById(2111001);
        if(reactor==null)return BotOpqRun.hold(entry,bot,ai);
        var dropPosition=bot.getMap().calcDropPos(bot.getPosition(),bot.getPosition());
        if(!reactor.getArea().contains(dropPosition)||entry.inAir||entry.climbing){BotZakumPqRun.walkTo(entry,bot,reactor.getPosition(),ai);return true;}
        if(ready(bot)&&BotManager.getInstance().issueDropItem(entry,InventoryType.ETC,4001017,(short)1))
            ACTIONS.put(bot,System.currentTimeMillis()+8000);
        return BotOpqRun.hold(entry,bot,ai);
    }
    private static boolean ready(Character bot) {
        long now=System.currentTimeMillis();if(now<ACTIONS.getOrDefault(bot,0L))return false;
        ACTIONS.put(bot,now+1200);return true;
    }
    private static boolean talk(BotEntry entry,Character bot,int npc,int selection,boolean ai) {
        var approach=BotTravelManager.tickApproachNpc(entry,bot,bot.getMapId(),npc,4,ai,false,100);
        if(approach==BotTravelManager.ApproachStatus.ARRIVED){
            BotOpqRun.hold(entry,bot,ai);
            if(ready(bot)){if(bot.getClient().getCM()==null)BotNpcInteraction.talk(bot,npc);else BotNpcInteraction.next(bot,selection);}
        }
        return true;
    }
}
