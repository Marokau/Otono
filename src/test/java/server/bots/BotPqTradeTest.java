package server.bots;

import client.BotClient;
import client.Character;
import client.Client;
import org.junit.jupiter.api.Test;
import server.Trade;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotPqTradeTest {
    @Test void dedicatedCrewDoesNotRequestRockSharingEvenWhenEmpty() {
        var bot=mock(Character.class);var human=mock(Character.class);
        var entry=new BotEntry(bot,human,null);BotPqDialogue.register(bot);
        entry.rockShareRequested=true;
        BotRockManager.tickRockShareCheck(entry,bot);
        assertFalse(entry.rockShareRequested);
        assertFalse(BotRockManager.requestRockShare(entry,bot,4006000,true));
        assertFalse(BotRockManager.requestRockShare(entry,bot,4006001,false));
    }

    @Test void dedicatedCrewDoesNotAskForOwnerEquipment() {
        var bot=mock(Character.class);var human=mock(Character.class);
        var entry=new BotEntry(bot,human,null);BotPqDialogue.register(bot);
        clearInvocations(human);
        BotOfferManager.notifyOwnerGainedEquip(entry,bot,null);
        BotOfferManager.requestBestUpgradeFromOwner(entry,bot);
        assertFalse(BotOfferManager.hasOfferReservation(entry));
        assertTrue(entry.requestedUpgradeItemIds.isEmpty());
        verifyNoInteractions(human);
    }
    @Test void dedicatedCrewCancelsExistingOwnerEquipmentRequest() {
        var bot=mock(Character.class);var human=mock(Character.class);
        var entry=new BotEntry(bot,human,null);BotPqDialogue.register(bot);
        entry.pendingLootOfferBotRequesting=true;
        entry.pendingLootOfferItem=mock(client.inventory.Item.class);
        entry.pendingLootOfferRecipientId=123;
        entry.pendingLootOfferExpiresAt=System.currentTimeMillis()+45000;
        entry.msgQueue.add(new BotChatManager.QueuedMessage("Can I have your GM hat?",false));
        entry.msgQueue.add(new BotChatManager.QueuedMessage("Ready for the boss.",false));
        BotOfferManager.expirePendingOffer(entry);
        assertEquals(1,entry.msgQueue.size());
        assertEquals("Ready for the boss.",entry.msgQueue.peek().text);
        assertFalse(entry.pendingLootOfferBotRequesting);
        assertFalse(BotOfferManager.hasOfferReservation(entry));
    }

    @Test void clearsQueuedBotTradesAndRetriesWithoutOpeningAnotherInvite() {
        var bot=mock(Character.class);var entry=new BotEntry(bot,bot,null);
        BotPqDialogue.register(bot);entry.pendingTradeCategory="pot_share";
        entry.pendingBotTradeRetry=()->fail("PQ bot retried a trade");
        BotInventoryManager.tickTrade(entry,bot);
        assertNull(entry.pendingTradeCategory);assertNull(entry.pendingBotTradeRetry);
    }
    @Test void rejectsIncomingPeerBotTradeButStillAcceptsHumanInitiatedTrade() {
        var bot=mock(Character.class);var human=mock(Character.class);var peer=mock(Character.class);
        var manager=mock(BotManager.class);var trade=mock(Trade.class);var partner=mock(Trade.class);
        var entry=new BotEntry(bot,human,null);BotPqDialogue.register(bot);
        when(bot.getTrade()).thenReturn(trade);when(trade.getPartner()).thenReturn(partner);
        when(partner.getChr()).thenReturn(peer);when(peer.getClient()).thenReturn(new BotClient(0,1));
        try(var managers=mockStatic(BotManager.class);var trades=mockStatic(Trade.class)) {
            managers.when(BotManager::getInstance).thenReturn(manager);
            when(manager.commanderOrOwner(entry)).thenReturn(human);
            BotInventoryManager.tickManualTrade(entry,bot);
            trades.verify(()->Trade.cancelTrade(bot,Trade.TradeResult.NO_RESPONSE));
            trades.clearInvocations();
            when(partner.getChr()).thenReturn(human);when(human.getClient()).thenReturn(mock(Client.class));
            when(human.getTrade()).thenReturn(partner);when(partner.getPartner()).thenReturn(trade);
            when(trade.getNumber()).thenReturn((byte)1);
            entry.manualTradeRef=trade;entry.manualTradeTimeoutMs=10000;entry.manualTradeAcceptDelayMs=1;
            BotInventoryManager.tickManualTrade(entry,bot);
            trades.verify(()->Trade.visitTrade(bot,human));
            trades.verify(()->Trade.cancelTrade(bot,Trade.TradeResult.NO_RESPONSE),never());
        }
    }
}
