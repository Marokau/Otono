package server.bots;
import client.Character;
import client.Client;
import client.BotClient;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotPqDialogueTest {
    @Test void explainsUpperPassWaitOnlyAfterHumanHeadsDownToBoss() {
        var bot=mock(Character.class);var human=mock(Character.class);
        var event=mock(EventInstanceManager.class);var map=mock(server.maps.MapleMap.class);
        var drop=mock(server.maps.MapItem.class);var item=mock(client.inventory.Item.class);
        var entry=new BotEntry(bot,human,null);entry.kpq.humanLed=true;
        when(bot.getClient()).thenReturn(new BotClient(0,1));when(bot.getId()).thenReturn(1);
        when(bot.getEventInstance()).thenReturn(event);when(event.getPlayers()).thenReturn(List.of(human,bot));
        when(bot.getMapId()).thenReturn(BotKpqRun.FIRST_MAP+4);when(human.getMapId()).thenReturn(BotKpqRun.FIRST_MAP+4);
        when(bot.getPosition()).thenReturn(new java.awt.Point(0,-1455));
        when(human.getPosition()).thenReturn(new java.awt.Point(0,-1455));when(bot.getMap()).thenReturn(map);
        when(map.getDroppedItems()).thenReturn(List.of(drop));when(drop.getItemId()).thenReturn(4001008);
        when(drop.getPosition()).thenReturn(new java.awt.Point(-222,-2175));when(drop.getItem()).thenReturn(item);
        when(item.getQuantity()).thenReturn((short)1);
        try(var chat=mockStatic(BotChatManager.class)) {
            BotPqDialogue.tick(entry,bot,human,5);
            chat.clearInvocations();
            when(human.getPosition()).thenReturn(new java.awt.Point(0,-435));
            BotPqDialogue.tick(entry,bot,human,5);BotPqDialogue.tick(entry,bot,human,5);
            chat.verify(()->BotChatManager.queueBotSay(entry,"We are waiting for you to collect 1 pass(es) on the platforms above before we head down to King Slime. Check near the portals; a pass can be hard to see there."),times(1));
        }
    }
    @Test void dedicatedCompanionsDoNotOfferHumansGearButPopulationStillCan() {
        var bot=mock(Character.class);var human=mock(Character.class);var sibling=mock(Character.class);
        when(human.getClient()).thenReturn(mock(Client.class));when(sibling.getClient()).thenReturn(new BotClient(0,1));
        assertTrue(BotPqDialogue.allowsGearOffer(bot,human));
        BotPqDialogue.register(bot);
        assertFalse(BotPqDialogue.allowsGearOffer(bot,human));
        assertFalse(BotPqDialogue.allowsGearOffer(bot,sibling));
        assertFalse(BotPqDialogue.allowsGearOffer(sibling,bot));
    }
    @Test void progressUsesRealCouponCountsAndDoesNotRepeatMilestones() {
        var bot=mock(Character.class);var entry=new BotEntry(bot,bot,null);entry.kpq.humanLed=true;
        when(bot.getEventInstance()).thenReturn(mock(EventInstanceManager.class));when(bot.getMapId()).thenReturn(BotKpqRun.FIRST_MAP);
        try(var chat=mockStatic(BotChatManager.class)) {
            BotPqDialogue.progress(entry,bot,3,20);chat.verifyNoInteractions();
            BotPqDialogue.progress(entry,bot,10,20);BotPqDialogue.progress(entry,bot,11,20);
            chat.verify(()->BotChatManager.queueBotSay(entry,"Halfway done! I still need 10 coupons for my pass."),times(1));
            BotPqDialogue.progress(entry,bot,16,20);BotPqDialogue.progress(entry,bot,17,20);
            chat.verify(()->BotChatManager.queueBotSay(entry,"Just 4 more coupons, then I can get my pass."),times(1));
            BotPqDialogue.progress(entry,bot,20,20);BotPqDialogue.progress(entry,bot,20,20);
            chat.verify(()->BotChatManager.queueBotSay(entry,"Got all 20 coupons. Taking them to Cloto for my pass."),times(1));
        }
    }
    @Test void passHandoffAndReadyCueAreOncePerRunAndRequireActualLeaderPasses() {
        var b=mock(Character.class);var h=mock(Character.class);var event=mock(EventInstanceManager.class);
        var entry=new BotEntry(b,h,null);entry.kpq.humanLed=true;
        when(b.getClient()).thenReturn(new BotClient(0,1));when(b.getId()).thenReturn(1);
        when(h.getClient()).thenReturn(mock(Client.class));when(h.getName()).thenReturn("Nelbot");
        when(b.getEventInstance()).thenReturn(event);when(b.getMapId()).thenReturn(BotKpqRun.FIRST_MAP);
        when(event.getPlayers()).thenReturn(List.of(h,b));
        try(var chat=mockStatic(BotChatManager.class)) {
            BotPqDialogue.handedPass(entry,b,h);BotPqDialogue.handedPass(entry,b,h);
            chat.verify(()->BotChatManager.queueBotSay(entry,"Here's my pass, Nelbot. Pick it up from your feet."),times(1));
            BotPqDialogue.tick(entry,b,h,1);
            verify(h).dropMessage(5,"KPQ: All our passes are delivered! Pick them up and talk to Cloto to clear stage one.");
            when(h.getItemQuantity(4001008,false)).thenReturn(1);
            BotPqDialogue.tick(entry,b,h,1);BotPqDialogue.tick(entry,b,h,1);
            verify(h,times(1)).dropMessage(5,"KPQ: You have all 1 passes! Talk to Cloto, then enter the opened portal. We will follow.");
            when(event.getProperty("1stageclear")).thenReturn("true");
            BotPqDialogue.tick(entry,b,h,1);BotPqDialogue.tick(entry,b,h,1);
            verify(h,times(1)).dropMessage(5,"KPQ: Stage clear! Go through the portal first and we will follow.");
            when(b.getEventInstance()).thenReturn(mock(EventInstanceManager.class));
            BotPqDialogue.handedPass(entry,b,h);
            chat.verify(()->BotChatManager.queueBotSay(entry,"Here's my pass, Nelbot. Pick it up from your feet."),times(2));
        }
    }
}
