package server.bots;

import client.Character;
import client.Client;
import client.Job;
import org.junit.jupiter.api.Test;
import server.maps.MapleMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotTrainingGuideTest {
    private Character character(int level, Job job, int world) {
        Character character = mock(Character.class);
        when(character.getLevel()).thenReturn(level);
        when(character.getJob()).thenReturn(job);
        when(character.getWorld()).thenReturn(world);
        return character;
    }

    private BotEntry grinder(int level, Job job, int world, int mapId, int channel) {
        Character bot = character(level, job, world);
        when(bot.isLoggedinWorld()).thenReturn(true);
        when(bot.getMap()).thenReturn(mock(MapleMap.class));
        when(bot.getMapId()).thenReturn(mapId);
        Client client = mock(Client.class);
        when(bot.getClient()).thenReturn(client);
        when(client.getChannel()).thenReturn(channel);
        BotEntry entry = new BotEntry(bot, null, null);
        entry.grinding = true;
        return entry;
    }

    @Test
    void groupsByMapAndChannelAndFiltersLevelWorldAndJobBranch() {
        Character player = character(75, Job.HERMIT, 0);
        var entries = List.of(grinder(65, Job.ASSASSIN, 0, 610020002, 1),
                grinder(85, Job.HERMIT, 0, 610020002, 1),
                grinder(75, Job.HERMIT, 0, 610020002, 2),
                grinder(75, Job.PRIEST, 0, 610030011, 1),
                grinder(86, Job.HERMIT, 0, 610020002, 1),
                grinder(75, Job.HERMIT, 1, 610020002, 1));
        var spots = BotTrainingGuide.spots(player, true, entries);
        assertEquals(2, spots.size());
        assertEquals(new BotTrainingGuide.Spot(610020002, 1, 2, 65, 85), spots.getFirst());
        assertEquals(3, BotTrainingGuide.spots(player, false, entries).size());
    }

    @Test
    void excludesOfflineFollowingAndTravellingBotsWithoutChangingTheirState() {
        Character player = character(75, Job.HERMIT, 0);
        BotEntry offline = grinder(75, Job.HERMIT, 0, 610020002, 1);
        when(offline.bot.isLoggedinWorld()).thenReturn(false);
        BotEntry following = grinder(75, Job.HERMIT, 0, 610020002, 1);
        following.following = true;
        BotEntry travelling = grinder(75, Job.HERMIT, 0, 610020002, 1);
        travelling.autopilotMapId = 610030011;
        assertTrue(BotTrainingGuide.spots(player, false, List.of(offline, following, travelling)).isEmpty());
        assertEquals(610030011, travelling.autopilotMapId);
        assertTrue(following.following);
    }
}
