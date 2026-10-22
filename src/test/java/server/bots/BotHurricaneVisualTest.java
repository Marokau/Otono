package server.bots;
import client.Character;
import client.Disease;
import constants.skills.Bowmaster;
import org.junit.jupiter.api.Test;
import server.maps.MapleMap;
import net.packet.Packet;
import java.awt.Point;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotHurricaneVisualTest {
    private BotCombatManager.AttackPlan plan(int stance) {
        return new BotCombatManager.AttackPlan(Bowmaster.HURRICANE,30,1,null,List.of(),
                BotCombatManager.AttackRoute.RANGED,0,12,12,stance,4,0,120,null);
    }
    @Test void heldEffectStartsOnceAndStopsOnMovement() {
        var bot=mock(Character.class);var map=mock(MapleMap.class);
        when(bot.getMap()).thenReturn(map);when(bot.getPosition()).thenReturn(new Point(400,-530));when(bot.isAlive()).thenReturn(true);
        var entry=new BotEntry(bot,null,null);
        BotHurricaneVisual.refresh(bot,plan(0x80),1000);
        BotHurricaneVisual.refresh(bot,plan(0x80),1120);
        var packets=org.mockito.ArgumentCaptor.forClass(Packet.class);
        verify(map).broadcastMessage(eq(bot),packets.capture(),eq(false));
        var bytes=packets.getValue().getBytes();
        assertEquals(14,bytes.length);assertEquals(30,bytes[10]&255);
        assertEquals(12,bytes[11]&255);assertEquals(0x80,bytes[12]&255);assertEquals(4,bytes[13]&255);
        clearInvocations(map);
        when(bot.getPosition()).thenReturn(new Point(401,-530));BotHurricaneVisual.tick(entry,1200);
        verify(map).broadcastMessage(eq(bot),any(Packet.class),eq(false));
        clearInvocations(map);BotHurricane.cancel(bot);verifyNoInteractions(map);
    }
    @Test void stunAndIdleTimeoutCancelAndNextHoldCanRestart() {
        var bot=mock(Character.class);var map=mock(MapleMap.class);
        when(bot.getMap()).thenReturn(map);when(bot.getPosition()).thenReturn(new Point());when(bot.isAlive()).thenReturn(true);
        var entry=new BotEntry(bot,null,null);
        BotHurricaneVisual.refresh(bot,plan(0),1000);clearInvocations(map);
        when(bot.hasDisease(Disease.STUN)).thenReturn(true);BotHurricaneVisual.tick(entry,1100);
        verify(map).broadcastMessage(eq(bot),any(Packet.class),eq(false));
        when(bot.hasDisease(Disease.STUN)).thenReturn(false);clearInvocations(map);
        BotHurricaneVisual.refresh(bot,plan(0),1200);BotHurricaneVisual.tick(entry,2000);
        verify(map,times(2)).broadcastMessage(eq(bot),any(Packet.class),eq(false));
        BotHurricane.cancel(bot);
    }
}
