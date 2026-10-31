package server.bots;
import client.Character;
import client.inventory.InventoryType;
import server.maps.MapItem;
import java.util.Comparator;
import java.util.function.IntPredicate;

/** Native crystal-key relay, light/dark choice and earned red/blue keys. */
final class BotHorntailTrial {
    private BotHorntailTrial() {}
    static boolean collect(BotEntry e,Character b,BotHorntailRun.Crew c,IntPredicate wanted,boolean ai){
        var d=b.getMap().getDroppedItems().stream().filter(x->!x.isPickedUp()&&wanted.test(x.getItemId())&&x.canBePickedBy(b)&&System.currentTimeMillis()-x.getDropTime()>500
                &&BotHorntailTargets.canCollect(b,x))
            .min(Comparator.comparingDouble(x->x.getPosition().distanceSq(b.getPosition()))).orElse(null);
        if(d==null)return false;
        if(b.getPosition().distanceSq(d.getPosition())<=30*30){int n=d.getItemId()>0?b.getItemQuantity(d.getItemId(),false):b.getMeso();b.pickupItem(d);if(b.getMapId()>=240060000&&(d.getItemId()>0?b.getItemQuantity(d.getItemId(),false):b.getMeso())>n)c.rewardPickups++;return BotOpqRun.hold(e,b,ai);}
        BotZakumPqRun.stepTowardsAndPickUp(e,b,d,ai);return true;
    }
    static boolean deliver(BotEntry e,Character b,BotHorntailRun.Crew c,IntPredicate wanted,boolean ai){
        for(var item:b.getInventory(InventoryType.ETC).list())if(wanted.test(item.getItemId())){
            if(c.leader.getMap()!=b.getMap())return BotOpqRun.hold(e,b,ai);
            if(b.getPosition().distanceSq(c.leader.getPosition())>45*45){BotZakumPqRun.walkTo(e,b,c.leader.getPosition(),ai);return true;}
            BotOpqRun.hold(e,b,ai);if(BotHorntailRun.ready(c,b))BotManager.getInstance().issueDropItem(e,InventoryType.ETC,item.getItemId(),item.getQuantity());return true;
        }
        return false;
    }
    static boolean tick(BotEntry e,Character b,BotHorntailRun.Crew c,boolean ai){
        int map=b.getMapId();var event=b.getEventInstance();var keeper=c.bots.get(1);var carrier=c.bots.get(3);
        if(map==240050100){
            if(BotHorntailRun.intro(e,b,c,"Trial One: Our Bishop stays here to receive crystal keys. Follow the other companions through the five caves. We will relay earned keys through their pedestals. In the last cave, pick up the blue key yourself and open the return portal. Come back here for the crystal keys and speak to the Schedule.",ai))return true;
            if(b==keeper){
                if("1".equals(event.getProperty("5stageclear"))){if(deliver(e,b,c,id->id>=4001087&&id<=4001091,ai))return true;}
                if(collect(e,b,c,id->id>=4001087&&id<=4001091,ai))return true;
                if(b.getMap().getSpawnedMonstersOnMap()>0)return BotOpqRun.grind(e,b,ai);
                return BotOpqRun.hold(e,b,ai);
            }
            if("1".equals(event.getProperty("5stageclear"))){
                if(!c.human&&b==c.leader&&collect(e,b,c,id->id>=4001087&&id<=4001091,ai))return true;
                boolean complete=true;for(int id=4001087;id<=4001091;id++)if(c.leader.getItemQuantity(id,false)<=0)complete=false;
                if(complete){if(c.notices.add("crystals-ready"))c.leader.dropMessage(5,"Horntail Trial: You have all five crystal keys. Speak to the Schedule on the right.");if(!c.human&&b==c.leader)return BotHorntailRun.talk(e,b,c,2083001,0,ai);}
                return BotOpqRun.hold(e,b,ai);
            }
            if(keeper.getItemQuantity(4001087,false)>0)return BotMagatiaRun.portal(e,b,"in00",ai);
            return BotOpqRun.grind(e,b,ai);
        }
        if(map>=240050101&&map<=240050104){
            int stage=map-240050100,item=4001087+stage;
            if(BotHorntailRun.intro(e,b,c,"Crystal Cave "+stage+": We will defeat the monsters and place the earned crystal key at the pedestal. Its real relay sends a copy back to our Bishop. Follow the opened portal on the right.",ai))return true;
            if(event.getProperty(stage+"stageclear")!=null)return BotMagatiaRun.portal(e,b,"in00",ai);
            if(b==carrier){
                var reactor=b.getMap().getReactorByName("passKey"+stage);
                if(b.getItemQuantity(item,false)>0){
                    if(!reactor.getArea().contains(b.getPosition())||e.inAir||e.climbing){BotZakumPqRun.walkTo(e,b,reactor.getPosition(),ai);return true;}
                    BotOpqRun.hold(e,b,ai);if(BotHorntailRun.ready(c,b)&&BotManager.getInstance().issueDropItem(e,InventoryType.ETC,item,(short)1))c.next.put(b.getId(),System.currentTimeMillis()+6500);return true;
                }
                if(System.currentTimeMillis()<c.next.getOrDefault(b.getId(),0L))return BotOpqRun.hold(e,b,ai);
                if(collect(e,b,c,id->id==item,ai))return true;
            }
            return b.getMap().getSpawnedMonstersOnMap()>0?BotOpqRun.grind(e,b,ai):BotOpqRun.hold(e,b,ai);
        }
        if(map==240050105){
            if(BotHorntailRun.intro(e,b,c,"Last Crystal Cave: Defeat the monsters. The leader must pick up the blue key and use the right-hand portal first to open our way back. We will wait for you.",ai))return true;
            if(event.getProperty("5stageclear")!=null)return BotMagatiaRun.portal(e,b,"in00",ai);
            if(!c.human&&b==c.leader){if(collect(e,b,c,id->id==4001092,ai))return true;if(b.getItemQuantity(4001092,false)>0)return BotMagatiaRun.portal(e,b,"in00",ai);}
            return b.getMap().getSpawnedMonstersOnMap()>0?BotOpqRun.grind(e,b,ai):BotOpqRun.hold(e,b,ai);
        }
        if(map==240050200){
            if(BotHorntailRun.intro(e,b,c,"Trial Two: Hit the lightbulb to choose the light or dark route, then enter the right-hand portal as leader. We will help collect six red keys in the chosen route. Keep your blue key.",ai))return true;
            if(!c.human&&b==c.leader){var r=b.getMap().getReactorByName("light");if(r.getState()!=1&&r.getState()!=3)return BotGuildActions.smash(e,b,x->x==r,ai);return BotMagatiaRun.portal(e,b,"east00",ai);}
            return BotOpqRun.hold(e,b,ai);
        }
        if(BotHorntailRun.intro(e,b,c,"Final Trial: We will earn six red keys from the monsters or boxes. Pick up the keys we bring you, keep your blue key, then speak to the Schedule on the far right to clear the trial.",ai))return true;
        if(b!=c.leader&&deliver(e,b,c,id->id==4001093,ai))return true;
        if(c.leader.getItemQuantity(4001093,false)>=6){if(c.notices.add("red-ready"))c.leader.dropMessage(5,"Horntail Trial: Six red keys are ready. Bring them and your blue key to the Schedule on the far right.");if(!c.human&&b==c.leader)return BotHorntailRun.talk(e,b,c,2083001,0,ai);return BotOpqRun.hold(e,b,ai);}
        if((!c.human&&b==c.leader||b==carrier)&&collect(e,b,c,id->id==4001093,ai))return true;
        if(map==240050310&&b.getMap().getAllReactors().stream().anyMatch(r->r.isActive()&&r.getId()==2402006&&r.getReactorType()==0))return BotGuildActions.smash(e,b,r->r.getId()==2402006&&r.isActive()&&r.getReactorType()==0,ai);
        return b.getMap().getSpawnedMonstersOnMap()>0?BotOpqRun.grind(e,b,ai):BotOpqRun.hold(e,b,ai);
    }
}
