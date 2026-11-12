package server.bots;

import client.Character;
import client.Job;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import server.life.Monster;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BotCwkpqBossTargetsTest {
    private Character bot(Job job, int map) {
        var bot = mock(Character.class);
        when(bot.getJob()).thenReturn(job);
        when(bot.getMapId()).thenReturn(map);
        when(bot.getEventInstance()).thenReturn(mock(EventInstanceManager.class));
        return bot;
    }

    private Monster monster(int id) {
        var mob = mock(Monster.class);
        when(mob.getId()).thenReturn(id);
        return mob;
    }

    @Test void damageMageWorksOnHsalfWhileOtherMastersRemain() {
        var hsalf = monster(9400593);
        var mobs = new ArrayList<>(List.of(monster(9400590), hsalf, monster(9400594)));
        BotZakumCombat.selectCrimsonwoodTargets(bot(Job.FP_ARCHMAGE, 610030600), mobs);
        assertEquals(List.of(hsalf), mobs);
    }

    @Test void physicalAttackerClearsOtherMastersBeforeHsalf() {
        var rellik = monster(9400592);
        var mobs = new ArrayList<>(List.of(monster(9400593), rellik));
        BotZakumCombat.selectCrimsonwoodTargets(bot(Job.BOWMASTER, 610030600), mobs);
        assertEquals(List.of(rellik), mobs);
    }

    @Test void physicalAttackerCanHelpFinishTheLastMaster() {
        var hsalf = monster(9400593);
        var mobs = new ArrayList<>(List.of(hsalf));
        BotZakumCombat.selectCrimsonwoodTargets(bot(Job.BUCCANEER, 610030600), mobs);
        assertEquals(List.of(hsalf), mobs);
    }

    @Test void OtherEncountersKeepTheirTargets() {
        var mobs = new ArrayList<>(List.of(monster(9400590), monster(9400593)));
        var original = List.copyOf(mobs);
        BotZakumCombat.selectCrimsonwoodTargets(bot(Job.FP_ARCHMAGE, 280030000), mobs);
        assertEquals(original, mobs);
    }
}
