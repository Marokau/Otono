package server.bots;

import client.Character;
import client.status.MonsterStatus;
import net.server.world.Party;
import server.expeditions.ExpeditionType;
import server.life.Monster;
import java.util.*;

/** Native expedition registration, five statue waves, real reactor and Pink Bean combat. */
public final class BotPinkBeanRun {
    static final int RECRUIT=270050000,ARENA=270050100;
    static final class Crew {
        Character leader;final List<Character> bots=new ArrayList<>();boolean human;
        final Set<String> notices=java.util.concurrent.ConcurrentHashMap.newKeySet();
        final Map<Integer,Long> next=new java.util.concurrent.ConcurrentHashMap<>();
        long arrival,clearedAt;int rewardPickups;
    }
    private static final Map<Character,Crew> RUNS=Collections.synchronizedMap(new WeakHashMap<>());
    private BotPinkBeanRun() {}
    static Crew crew(Character b){synchronized(RUNS){var c=RUNS.get(b);if(c!=null)return c;for(var x:RUNS.values())if(x.leader==b)return x;return null;}}
    static boolean belongsTo(Character b,int owner){var c=RUNS.get(b);return c!=null&&c.leader.getId()==owner;}
    static void unregister(Character b){RUNS.remove(b);}
    public static boolean inInstance(Character b){return b!=null&&b.getMapId()>=ARENA&&b.getMapId()<=270050300&&b.getEventInstance()!=null&&b.getEventInstance().getName().startsWith("PinkBean");}
    static boolean boss(int id){return id>=8820001&&id<=8820006;}
    static boolean canTarget(Character b,Monster mob){
        if(!inInstance(b))return true;
        int id=mob.getId();
        var c=crew(b);
        if(id==8820002&&c!=null&&c.notices.contains("body-rally")&&!c.notices.contains("body-rally-ready")
                &&mob.getHp()<=Math.max(1,mob.getMaxHp()/20))return false;
        if(id>=8820000&&id<=8820030&&!boss(id)&&id!=8820007)return false;
        boolean magic=BotEquipManager.isMageJob(b.getJob());
        return !BotPinkBeanCounterWarning.blocked(b,mob,magic,System.currentTimeMillis())
                &&!mob.isBuffed(magic?MonsterStatus.MAGIC_REFLECT:MonsterStatus.WEAPON_REFLECT);
    }
    static boolean safeToHeal(Character b){
        if(!inInstance(b))return true;
        if(b.hasDisease(client.Disease.ZOMBIFY))return false;
        int level=b.getSkillLevel(constants.skills.Cleric.HEAL);
        if(level<=0)return b.getPartyMembersOnSameMap().stream().noneMatch(m->m.hasDisease(client.Disease.ZOMBIFY));
        var effect=client.SkillFactory.getSkill(constants.skills.Cleric.HEAL).getEffect(level);
        var box=effect.calculateBoundingBox(b.getPosition(),b.isFacingLeft());
        for(var mate:b.getPartyMembersOnSameMap())if(mate.isAlive()&&mate.hasDisease(client.Disease.ZOMBIFY)&&box.contains(mate.getPosition()))return false;
        return true;
    }
    static List<Character> all(Crew c){var out=new ArrayList<>(c.bots);if(!out.contains(c.leader))out.add(c.leader);return out;}
    static int partyIndex(Crew c,Character b){int i=c.bots.indexOf(b);return i<5?0:1+(i-5)/6;}
    static Character partyLeader(Crew c,Character b){int group=partyIndex(c,b);return group==0?c.leader:c.bots.get(5+(group-1)*6);}
    static boolean rightSide(Crew c,Character b){return c!=null&&(c.notices.contains("body-rally")||BotPinkBeanAssignments.rightParty(b.getEventInstance()==null?0:b.getEventInstance().getIntProperty("stage"),partyIndex(c,b)));}
    static void start(Character leader,List<Character> bots,boolean human){
        if(bots.size()!=BotPinkBeanRoster.JOBS.size())throw new IllegalArgumentException("Twenty-nine companions required");
        var c=new Crew();c.leader=leader;c.bots.addAll(bots);c.human=human;var m=BotManager.getInstance();
        for(int i=0;i<bots.size();i++){
            var b=bots.get(i);var pl=partyLeader(c,b);
            if(pl==b){if(b.getParty()!=null)Party.leaveParty(b.getParty(),b.getClient());if(!Party.createParty(b,true))throw new IllegalStateException("Raid party creation failed");}
            else m.partyUp(pl,b);
            RUNS.put(b,c);var e=m.getEntryByBotCharId(b.getId());e.buffConsumablesEnabled=true;e.buffCheapMode=false;e.autoBuffEngaged=false;m.resumeFromOperatorCommand(e);m.issueStop(e);
        }
        if(!human){RUNS.put(leader,c);var e=m.getEntryByBotCharId(leader.getId());e.buffConsumablesEnabled=true;e.buffCheapMode=false;e.autoBuffEngaged=false;m.resumeFromOperatorCommand(e);m.issueStop(e);}
        for(var b:all(c))if(b.getParty()==null||b.getParty().getMembers().size()!=6||b.getParty().getLeaderId()!=partyLeader(c,b).getId())throw new IllegalStateException("Five parties of six required");
    }
    static boolean talk(BotEntry e,Character b,Crew c,int npc,int selection,boolean ai){
        var a=BotTravelManager.tickApproachNpc(e,b,b.getMapId(),npc,3,ai,false,150);
        if(a==BotTravelManager.ApproachStatus.ARRIVED){BotOpqRun.hold(e,b,ai);long now=System.currentTimeMillis();if(now>=c.next.getOrDefault(b.getId(),0L)){c.next.put(b.getId(),now+1200);if(b.getClient().getCM()==null)BotNpcInteraction.talk(b,npc);else BotNpcInteraction.next(b,selection);}}
        return true;
    }
    public static boolean tick(BotEntry e,Character b,boolean ai){
        var c=RUNS.get(b);if(c==null)return false;e.grinding=false;var leader=c.leader;
        boolean online=leader.getWorldServer().getPlayerStorage().getCharacterById(leader.getId())==leader;
        var expected=partyLeader(c,b);
        boolean valid=online&&leader.getParty()!=null&&b.getParty()!=null&&expected.getParty()!=null&&b.getParty().getId()==expected.getParty().getId()&&leader.getParty().getLeaderId()==leader.getId();
        if(c.human&&(!valid||inInstance(b)&&!inInstance(leader))){if(inInstance(b))return talk(e,b,c,2141002,1,ai);BotPinkBeanCompanions.dismiss(b);return true;}
        if(b.isAlive()&&BotHorntailSeduce.canUsePotions(b))b.triggerAutopotAfterStatLoss(-1,-1);
        if(b.getMapId()==RECRUIT){
            var exp=b.getClient().getChannelServer().getExpedition(ExpeditionType.PINKBEAN);
            if(exp!=null&&exp.isInProgress()&&!inInstance(leader))return BotOpqRun.hold(e,b,ai);
            if(exp!=null&&exp.isLeader(leader)){
                if(!exp.contains(b)||exp.isInProgress())return talk(e,b,c,2141001,0,ai);
                if(!c.human&&b==leader&&exp.getMemberList().size()==all(c).size())return talk(e,b,c,2141001,2,ai);
            }else if(exp==null&&!c.human&&b==leader)return talk(e,b,c,2141001,1,ai);
            return BotOpqRun.hold(e,b,ai);
        }
        if(b.getMapId()==270050300 || b.getMapId()==270050200)return BotMagatiaRun.portal(e,b,"in00",ai);
        if(b.getMapId()==ARENA&&b.getEventInstance()==null)return talk(e,b,c,2141002,1,ai);
        if(!inInstance(b))return BotOpqRun.hold(e,b,ai);
        var event=b.getEventInstance();
        BotPinkBeanReflectSpeech.tick(e);
        if(BotLpqCures.tryCure(b))return BotOpqRun.hold(e,b,ai);
        if(BotHorntailRevive.tick(e,b,event.getPlayers(),ai))return true;
        if(BotPinkBeanBodyReturn.tick(e,b,ai))return true;
        if(event.isEventCleared()){
            if(c.clearedAt==0)c.clearedAt=System.currentTimeMillis();
            if(c.notices.add("clear"))leader.dropMessage(5,"Pink Bean: Victory! All loot is yours. Speak to the Forgotten Temple Manager when you are ready to leave.");
            if(!c.human&&b==leader&&collect(e,b,c,ai))return true;
            if((c.human||b!=leader)&&leader.getMap()==b.getMap()||!c.human&&System.currentTimeMillis()-c.clearedAt<15000)return BotOpqRun.hold(e,b,ai);
            return talk(e,b,c,2141002,1,ai);
        }
        int wave=event.getIntProperty("stage");
        if(c.notices.add("wave-"+wave)){
            c.arrival=System.currentTimeMillis();
            String text=switch(wave){
                case 1->"Statue Wave One: Solomon the Wise awakens on the far left. We will buff, then defeat him. Stay off his body and follow our approach to the platforms.";
                case 2->"Statue Wave Two: Solomon returns and Rex awakens on the right. We will clear the left statue, then cross underneath to the right. Stay with your party for protection.";
                case 3->"Statue Wave Three: Both sages return with Hugin, the left bird. We will defeat Solomon, then Hugin, then Rex together. Stay with your party, avoid body contact, and stop for damage reflection.";
                case 4->"Statue Wave Four: Munin joins on the right. We will split between the left and right statues. Stay on your party's outer ground beyond Munin's Seduce reach. Thieves will jump to shoot when needed; the rest of us will attack when in range and support our party.";
                default->"Statue Wave Five: Ariel joins above the center. We will clear the side statues before attacking her. Stay with your party. Keep pickup pets away from the middle so the earned item can activate the reactor.";
            };
            BotPqChat.queue(e,text,false);
        }
        if(System.currentTimeMillis()-c.arrival<6500)return BotOpqRun.hold(e,b,ai);
        boolean warning=true;for(var m:b.getMap().getAllMonsters())if(m.isAlive()&&boss(m.getId())){warning=false;break;}
        boolean nearbyBird=b.getMap().getMonsterById(b.getPosition().x<0?8820005:8820006)!=null;
        if(!e.inAir&&!e.climbing&&(warning||nearbyBird)){
            var outer=BotPinkBeanPlatforms.adjustment(b.getPosition(),wave);
            if(outer!=null){BotPinkBeanMovement.walkTo(e,b,outer,ai);return true;}
        }
        if(BotPinkBeanOuterFire.required(b)&&BotPinkBeanOuterFire.tick(e,b,ai))return true;
        if(BotPinkBeanBodyRally.tick(e,b,ai))return true;
        if(BotPinkBeanColumnWarning.tick(e,b,ai))return true;
        if(BotHorntailSupport.tick(e,b,BotPinkBeanParticipation.readyMembers(c),c.notices,leader,"Pink Bean",ai))return true;
        if(BotPinkBeanFormation.tick(e,b,ai))return true;
        if(BotPinkBeanOuterFire.tick(e,b,ai))return true;
        var staging=BotPinkBeanTransition.destination(b);
        if(staging!=null){
            boolean finishing=false;for(var m:b.getMap().getAllMonsters())if(m.isAlive()&&m.getId()>=8820002&&m.getId()<=8820006)finishing=true;
            String key="staging-"+wave+"-"+(rightSide(c,b)?"right":"left")+"-"+finishing;
            if(b.getPosition().distanceSq(staging)>35*35&&c.notices.add(key))BotPqChat.queue(e,"Get ready on the "+(staging.x>0?"right":"left")+". "+(finishing?"The other party will finish this statue. ":"The next statues will appear soon. ")+"Stay with your party.",false);
            if(b.getPosition().distanceSq(staging)>35*35)BotPinkBeanMovement.walkTo(e,b,staging,ai);else BotOpqRun.hold(e,b,ai);
            return true;
        }
        boolean melee=b.getJob()==client.Job.HERO||b.getJob()==client.Job.DARKKNIGHT;
        boolean right=rightSide(c,b);
        boolean waitingForMunin=wave>=4&&melee&&b.getMap().getMonsterById(8820006)!=null&&b.getMap().getMonsterById(8820004)==null
                &&(right||b.getMap().getMonsterById(8820003)==null&&b.getMap().getMonsterById(8820005)==null);
        if(waitingForMunin){
            var waiting=new java.awt.Point(610,-42);
            if(b.getPosition().distanceSq(waiting)>35*35)BotPinkBeanMovement.walkTo(e,b,waiting,ai);else BotOpqRun.hold(e,b,ai);
            return true;
        }
        var hugin=b.getMap().getMonsterById(8820005);
        if(b.getJob()==client.Job.DARKKNIGHT&&b.getSkillLevel(constants.skills.DragonKnight.SPEAR_CRUSHER)>0
                &&!e.inAir&&!e.climbing&&b.getPosition().y==-42&&hugin!=null
                &&BotPinkBeanStrategy.targets(b,java.util.List.of(hugin)).contains(hugin)){
            BotPinkBeanMovement.walkTo(e,b,new java.awt.Point(-530,-99),ai);return true;
        }
        boolean earnedItem=false;
        for(var drop:b.getMap().getDroppedItems())if(!drop.isPickedUp()&&drop.getItemId()==4001193)earnedItem=true;
        if(wave==5&&earnedItem&&b.getMap().getAllMonsters().stream().noneMatch(m->m.isAlive()&&m.getId()>=8820002&&m.getId()<=8820006)&&b.getMap().getMonsterById(8820000)!=null){
            if(c.notices.add("reactor"))leader.dropMessage(5,"Pink Bean: All five statue waves are clear. The earned item is at the central reactor. Let it activate; the real Pink Bean fight follows.");
            // The native script drops the earned item directly inside the reactor area.
            return BotOpqRun.hold(e,b,ai);
        }
        if(b.getMap().getMonsterById(8820001)!=null&&c.notices.add("body"))BotPqChat.queue(e,"Pink Bean: The statues are down. We will attack the real body, stop for damage reflection, and clear summoned Mini Beans. Avoid body contact. If Zombify is active, we will use potions instead of Heal.",false);
        return BotHorntailCombat.tick(e,b,ai);
    }
    private static boolean collect(BotEntry e,Character b,Crew c,boolean ai){
        var d=b.getMap().getDroppedItems().stream().filter(x->!x.isPickedUp()&&x.canBePickedBy(b)&&(x.getMeso()>0||client.inventory.manipulator.InventoryManipulator.checkSpace(b.getClient(),x.getItemId(),x.getItem().getQuantity(),x.getItem().getOwner()))&&System.currentTimeMillis()-x.getDropTime()>1000).min(Comparator.comparingDouble(x->x.getPosition().distanceSq(b.getPosition()))).orElse(null);
        if(d==null)return false;
        if(b.getPosition().distanceSq(d.getPosition())<=30*30){b.pickupItem(d);if(d.isPickedUp())c.rewardPickups++;return BotOpqRun.hold(e,b,ai);}
        BotZakumPqRun.stepTowardsAndPickUp(e,b,d,ai);return true;
    }
}
