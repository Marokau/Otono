package server.bots;

import client.Character;
import java.util.*;

/** Read-before-departure introductions for the human-led crews still under client audit. */
final class BotPqStageGuide {
    private static final Map<Object,Map<String,Long>> STARTED=new WeakHashMap<>();
    private BotPqStageGuide() {}
    static synchronized void reset(Object instance) { STARTED.remove(instance); }
    static synchronized boolean tick(BotEntry entry,Character bot,Character human,Object instance,String activity,long now) {
        if(human==null || instance==null || bot.getMapId()!=human.getMapId())return false;
        String text=text(activity,bot.getMapId());if(text==null)return false;
        String key=activity+":"+bot.getMapId();
        var stages=STARTED.computeIfAbsent(instance,ignored->new HashMap<>());
        // A previously premature body notice must not consume the real transition later in this run.
        if(activity.equals("zakum-arms"))for(int body=0;body<3;body++)stages.remove("zakum-body-"+body+":"+bot.getMapId());
        Long until=stages.get(key);
        if(until==null) {
            // Carnival opponents pause too, but only a teammate gives the briefing.
            if(activity.startsWith("cpq") && (bot.getParty()==null || human.getParty()==null
                    || bot.getParty().getId()!=human.getParty().getId()))return true;
            synchronized(entry.msgQueue) {
                long queued=entry.msgSending?(entry.msgQueue.size()+1L)*5200:0;
                until=now+queued+7000;
                stages.put(key,until);
                BotPqChat.queue(entry,text,false);
            }
        }
        return now<until;
    }
    static String text(String activity,int map) {
        if(activity.equals("opq"))return switch(map) {
            case 920010000 -> "Entrance: We will break clouds, collect 20 pieces and use them to summon Eak. Stay here or help break clouds, then speak to Eak when we are ready.";
            case 920010100 -> "Center Tower: We will recover six statue pieces from the side rooms and place them on Minerva's statue. Follow our room announcements; you make the NPC checks and bring any awarded pieces back here.";
            case 920010200 -> "Walkway: We will defeat the Pixies and bring you 30 pieces. You can help fight. Pick up our delivery, check Eak, then return to the Center Tower with the statue piece.";
            case 920010300 -> "Storage: We will defeat the Celions and follow their spawns. Leave their drops on the ground for the room's mechanisms. Help fight if you like; check Eak when we announce readiness.";
            case 920010400 -> "Lobby: We will find today's record, play it and break the reward box. Stay nearby and speak to Eak when we ask; we will bring the statue piece back afterward.";
            case 920010500 -> "Sealed Room: We will arrange three workers across the platforms and try combinations. Please stay off the platforms. Check Eak when we say the arrangement is ready.";
            case 920010600 -> "Lounge: We will search the four small rooms and bring you 40 pieces. You can wait by Eak here. Pick up our delivery, check him and return with the statue piece.";
            case 920010601,920010602,920010603 -> "Lounge Room: We will defeat the monsters and collect this room's pieces. You can help fight or wait by Eak in the main Lounge; we will bring you all forty pieces there.";
            case 920010604 -> "Lounge Room: We will break the boxes and collect the remaining pieces. You can help or wait by Eak in the main Lounge; we will deliver the full set there.";
            case 920010700 -> "On the Way Up: We will take turns testing the numbered portals, share the correct columns and work the upper levers. You can wait beside Eak below. Check him when we announce readiness; we share learned routes if you climb.";
            case 920010800 -> "Garden: We will collect and plant seeds, then fight Papa Pixie. You can help fight. Pick up the Grass of Life we bring you and use the return portal yourself.";
            case 920011200 -> "Exit: This attempt has ended. Speak to Eak to return to recruitment; we will leave through the normal NPC too.";
            case 920011300 -> "Rewards: Talk to Minerva and leave inventory space for your reward. We will collect our own rewards and meet you back at Wonky.";
            default -> null;
        };
        if(activity.equals("mpq")) {
            int room=map>=926110000?map-926110000:map-926100000;
            return switch(room) {
                case 0 -> "Stage One: We will search the books for the switch and letter. You can help inspect them by double clicking on all the book stacks. Once the door opens, go through when ready; we will follow.";
                case 1 -> "Stage Two: We will clear the monsters in this dark passage. You can help fight. Use the exit portal once the room is clear.";
                case 100 -> "Stage Three: We will collect suspicious liquid and fill all three beakers. Leave our drops beneath the beakers alone. You can help fight, then use the open exit when ready.";
                case 200 -> "Stage Four: Our mage and thief will fetch the two research files while the collector opens their doors. Wait here or help fight. Pick up both files we deliver and turn them in to Romeo or Juliet.";
                case 201 -> "Left Research Room: Our thief will use Haste and break boxes for the file. Keep clear of the hazards; we will bring the file back to the main room.";
                case 202 -> "Right Research Room: Our mage will use Teleport and break boxes for the file. We will bring it back to the main room for your turn-in.";
                case 203 -> "Stage Five: Please wait at the entrance. Our scout will speak to Yulete quietly before we clear his laboratory. Follow through the exit when it opens.";
                case 300 -> "Stage Six: Choose the remaining empty puzzle room. We will test our own portals and share confirmed row clues to help you. We wait for everyone before the boss.";
                case 301,302,303,304 -> "Puzzle Room: Try the four platforms to climb each row. We solve our separate rooms and share confirmed clues. Count platforms from the left, and use the exit at the top.";
                case 400 -> "Final Approach: Head through the next portal when ready. We will follow and gather before you start the boss.";
                case 401 -> "Stage Seven: Wait for everyone, then talk to Yulete to start Frankenroid. Our Priest guards Romeo or Juliet upstairs; the others help you fight. Keep the boss loot and speak to the final NPC afterward.";
                case 500 -> "Exit: This attempt has ended. Use the exit NPC to return to recruitment; we will leave normally too.";
                case 600 -> "Rewards: Speak to the reward NPC with space in your inventory. We will collect our rewards and meet you back at Romeo or Juliet.";
                default -> null;
            };
        }
        if(activity.equals("lmpq")) {
            if(map>=BotLmpqRun.FIRST && map<BotLmpqRun.FINISH)return "Maze Room "+(map-BotLmpqRun.FIRST+1)+": We will break boxes, defeat their monsters and collect coupons. You can help fight; leave the coupons to our collector. Follow our portal directions when this room is clear.";
            if(map==BotLmpqRun.FINISH)return "Finish Room: We will pass you every coupon stack. Pick them all up and wait for our ready message, then talk to Pietri to finish. We wait for your check.";
            if(map==BotLmpqRun.REWARD)return "Rewards: Talk to the reward NPC and leave inventory space. We will claim our rewards and return to Rolly.";
        }
        if(activity.equals("cpq-fight"))return "Carnival: Defeat the opposing team's monsters to earn CP; the team with the higher score wins when time runs out. We will fight and spend CP too. Use recovery drops on the field, because carried potions are disabled.";
        if(activity.equals("cpq-recovery"))return "Recovery Room: Use the return portal when you are ready to rejoin the match. We will return through our normal recovery portals too.";
        if(activity.equals("cpq-rewards"))return "Carnival Results: The match is over. Speak to Spiegelmann for your result and reward, then return to the lobby. We will meet you there.";
        if(activity.equals("zakum"))return "Zakum Altar: Drop your Eye of Fire at the altar when you are ready. We will prepare buffs, handle the arms and help fight the three body phases and summoned monsters. Keep potions and All Cures ready; all boss loot is yours.";
        if(activity.equals("zakum-arms"))return "Arm Phase: We will move between platforms to reach the arms. Help attack from a safe platform, keep your potions ready and watch for stuns and seals.";
        if(activity.equals("zakum-body-0"))return "Body Phase One: The arms are down. We will attack the body and handle summoned monsters. Stay out of the body itself and watch for falling pillars and lightning.";
        if(activity.equals("zakum-body-1"))return "Body Phase Two: We will keep attacking and clearing the extra monsters. Keep healing, cure seals and avoid body contact; another body phase follows this one.";
        if(activity.equals("zakum-body-2"))return "Body Phase Three: This is the final body. We will keep the summoned monsters under control while helping you finish it. Keep your potions and All Cures ready; the drops are yours.";
        return null;
    }
}
