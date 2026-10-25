package server.bots;
import client.Character;
import server.maps.MapItem;
import scripting.event.EventInstanceManager;
import java.awt.Point;
import java.util.*;

/** Four-vassal Mastermind: learns only the same feedback the NPC gives the player. */
public final class BotGuildOfferings {
 static final class Puzzle {
  final List<Integer> candidates=new ArrayList<>();
  int guess=-1; boolean recovering,started; long settle;
  Puzzle(){for(int i=0;i<256;i++)candidates.add(i);}
 }
 private static final Map<EventInstanceManager,Puzzle> PUZZLES=Collections.synchronizedMap(new WeakHashMap<>());
 private BotGuildOfferings(){}
 static Puzzle puzzle(EventInstanceManager event){synchronized(PUZZLES){return PUZZLES.computeIfAbsent(event,k->new Puzzle());}}
 public static void reset(Character leader){if(leader.getEventInstance()!=null) synchronized(PUZZLES){var p=new Puzzle();p.started=true;PUZZLES.put(leader.getEventInstance(),p);}}
 public static void feedback(Character leader,int visibleGuess,int correct,int misplaced,int unknown){
  if(leader.getEventInstance()==null)return;var p=puzzle(leader.getEventInstance());
  synchronized(p){int g=fromDecimal(visibleGuess);int answer=correct*25+misplaced*5+unknown;p.candidates.removeIf(c->score(c,g)!=answer);p.guess=-1;p.recovering=true;p.settle=System.currentTimeMillis()+1200;}
 }
 public static void awaitStart(Character leader){if(leader.getEventInstance()!=null)synchronized(PUZZLES){PUZZLES.put(leader.getEventInstance(),new Puzzle());}}
 static int fromDecimal(int n){int out=0,scale=1;for(int i=0;i<4;i++){out+=(n%10)*scale;scale*=4;n/=10;}return out;}
 static int digit(int guess,int slot){return (guess>>(2*(3-slot)))&3;}
 static int score(int secret,int guess){int exact=0,shared=0;int[] a=new int[4],b=new int[4];for(int i=0;i<4;i++){int x=digit(secret,i),y=digit(guess,i);if(x==y)exact++;else{a[x]++;b[y]++;}}for(int i=0;i<4;i++)shared+=Math.min(a[i],b[i]);return exact*25+shared*5+(4-exact-shared);}
 static int choose(List<Integer> candidates){int best=-1,bestWorst=257;for(int g=0;g<256;g++){int[] groups=new int[126];int worst=0;for(int c:candidates)worst=Math.max(worst,++groups[score(c,g)]);if(worst<bestWorst || worst==bestWorst && candidates.contains(g) && !candidates.contains(best)){best=g;bestWorst=worst;}}return best;}
 static boolean tick(BotEntry e,Character b,BotGuildCompanions.Crew c,BotGuildRun.State s,boolean ai){
  int m=b.getMapId();var event=b.getEventInstance();var main=event.getMapInstance(990000500);
  if(main.getReactorByName("watergate").getState()>0){
   if(s.notices.add("offerings-clear"))c.leader.dropMessage(5,"GPQ: The vassals accepted our offerings. Enter the opened waterway when ready.");
   if(m!=990000500)return BotMagatiaRun.portal(e,b,"out00",ai);return BotGuildRun.advance(e,b,c,"next00",ai);
  }
  if(BotGuildRun.intro(e,b,s,"offerings-intro","Stage Three: We will gather medals, scrolls, food and wine, then arrange four offerings. The statue tells us how many are correct or misplaced. Please speak to the Guardian Statue when our arrangement is ready; we use its clues for the next attempt.",ai))return true;
  var p=puzzle(event);synchronized(p){
   var crew=c.bots;int medals=quantity(crew,4001027),scrolls=quantity(crew,4001028),food=quantity(crew,4001029),wine=quantity(crew,4001030);
   boolean supplied=medals>=4&&scrolls>=4&&food>=4&&wine>=4;
   if(!supplied && p.guess<0 && !p.recovering){
    boolean mage=b.getJob().isA(client.Job.MAGICIAN),thief=b.getJob().isA(client.Job.THIEF);
    if(m==990000501||m==990000502){int id=m==990000501?4001029:4001030;int have=m==990000501?food:wine;if(have>=4)return BotMagatiaRun.portal(e,b,"out00",ai);if(BotGuildRun.collect(e,b,x->x==id,ai))return true;return BotGuildActions.smash(e,b,r->r.isActive()&&(r.getId()==9202003||r.getId()==9202004)&&r.getState()<5,ai);}
    if(mage&&food<4)return BotMagatiaRun.portal(e,b,"in00",ai);
    if(thief&&wine<4)return BotMagatiaRun.portal(e,b,"in01",ai);
    if(b==c.leader||b.getLevel()<=30)return BotOpqRun.hold(e,b,ai);
    if(BotGuildRun.collect(e,b,x->x==4001027&&medals<4||x==4001028&&scrolls<4,ai))return true;
    if(main.getAllMonsters().stream().anyMatch(x->x.isAlive()))return BotOpqRun.grind(e,b,ai);
    return BotOpqRun.hold(e,b,ai);
   }
   if(m!=990000500)return BotMagatiaRun.portal(e,b,"out00",ai);
   if(!p.started){
    if(c.test&&b==c.leader)return BotGuildRun.talk(e,b,s,9040006,ai);
    if(s.notices.add("offerings-start"))c.leader.dropMessage(5,"GPQ: Our supplies are ready. Speak to the Guardian Statue to begin the offering puzzle.");return BotOpqRun.hold(e,b,ai);
   }
   if(System.currentTimeMillis()<p.settle)return BotOpqRun.hold(e,b,ai);
   var ground=main.getDroppedItems().stream().filter(x->!x.isPickedUp()&&x.getItemId()>=4001027&&x.getItemId()<=4001030).toList();
   if(p.recovering){if(!ground.isEmpty()){if(b!=c.leader&&BotGuildRun.collect(e,b,x->x>=4001027&&x<=4001030,ai))return true;return BotOpqRun.hold(e,b,ai);}p.recovering=false;}
   if(p.guess<0){if(p.candidates.isEmpty()){if(s.notices.add("offerings-conflict"))c.leader.dropMessage(5,"GPQ: Those clues conflict with our placements. Please pause while we check the offerings.");return BotOpqRun.hold(e,b,ai);}p.guess=choose(p.candidates);}
   for(int slot=0;slot<4;slot++){
    final int k=slot,item=4001027+digit(p.guess,slot);var area=main.getArea(slot);
    if(ground.stream().anyMatch(d->d.getItemId()==item&&area.contains(d.getPosition())))continue;
    if(ground.stream().anyMatch(d->area.contains(d.getPosition()))){p.recovering=true;return BotOpqRun.hold(e,b,ai);}
    var worker=crew.stream().filter(x->x.getMapId()==990000500&&x.getItemQuantity(item,false)>0).min(Comparator.comparingInt(Character::getId)).orElse(null);
    if(worker==null)return BotOpqRun.hold(e,b,ai);
    if(worker!=b)return BotOpqRun.hold(e,b,ai);
    Point target=new Point((int)area.getCenterX(),(int)area.getMaxY()-10);
    if(!area.contains(b.getPosition())||e.inAir||e.climbing){BotZakumPqRun.walkTo(e,b,target,ai);return true;}
    BotOpqRun.hold(e,b,ai);if(BotGuildRun.ready(s,b)){BotManager.getInstance().issueDropItem(e,client.inventory.InventoryType.ETC,item,(short)1);p.settle=System.currentTimeMillis()+1400;}return true;
   }
   if(s.notices.add("offerings-ready-"+event.getProperty("stage3attempt")))c.leader.dropMessage(5,"GPQ: Our offering arrangement is ready. Ask the Guardian Statue to check it.");
   if(c.test&&b==c.leader)return BotGuildRun.talk(e,b,s,9040006,ai);
   return BotOpqRun.hold(e,b,ai);
  }
 }
 static int quantity(List<Character> crew,int item){return crew.stream().mapToInt(b->b.getItemQuantity(item,false)).sum();}
}
