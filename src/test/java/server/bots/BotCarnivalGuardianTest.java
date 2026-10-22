package server.bots;

import client.Client;
import org.junit.jupiter.api.Test;
import scripting.reactor.ReactorScriptManager;
import server.maps.MapleMap;
import server.maps.Reactor;
import server.maps.ReactorStats;
import server.partyquest.GuardianSpawnPoint;
import java.awt.Point;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotCarnivalGuardianTest {
    @Test void fourthPlayerHitDispelsGuardianAndFreesItsSpawnForBothTeams() {
        for (int id : new int[]{9980000,9980001}) {
            var stats=new ReactorStats();
            stats.addState((byte)0,999,null,(byte)1,-1,(byte)0);
            for(byte s=1;s<5;s++)stats.addState(s,0,null,(byte)(s+1),-1,(byte)0);
            stats.addState((byte)5,999,null,(byte)-1,-1,(byte)0);
            var r=new Reactor(stats,id);r.setPosition(new Point());
            var map=mock(MapleMap.class);r.setMap(map);
            var spawn=new GuardianSpawnPoint(new Point());r.setGuardian(spawn);
            var client=mock(Client.class);var scripts=mock(ReactorScriptManager.class);
            doAnswer(i->{r.destroy();return null;}).when(map).destroyReactor(r.getObjectId());
            try(var factory=mockStatic(ReactorScriptManager.class)) {
                factory.when(ReactorScriptManager::getInstance).thenReturn(scripts);
                r.hitReactor(client); // normal spawn animation, not a player's damaging hit
                for(int i=0;i<3;i++)r.hitReactor(true,0,(short)0,0,client);
                verify(scripts,never()).act(client,r);assertTrue(spawn.isTaken());
                r.hitReactor(true,0,(short)0,0,client);
                verify(scripts).act(client,r);verify(map).removeMapObject(r);
                assertFalse(spawn.isTaken());assertFalse(r.isActive());
                r.hitReactor(true,0,(short)0,0,client);
                verify(scripts,times(1)).act(client,r);
            }
        }
    }
}
