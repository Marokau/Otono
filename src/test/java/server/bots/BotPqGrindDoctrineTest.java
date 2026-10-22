package server.bots;
import client.Character;
import org.junit.jupiter.api.Test;
import java.awt.Point;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotPqGrindDoctrineTest {
 @Test void instancesDoNotCampARespawnSpotWhileOtherStageMobsRemain() {
  var bot=mock(Character.class);when(bot.getEventInstance()).thenReturn(mock(scripting.event.EventInstanceManager.class));
  var entry=new BotEntry(bot,bot,null);entry.grindDoctrineStyle=BotGrindDoctrine.Style.CAMP;
  entry.grindSpotAnchor=new Point(-358,24);entry.grindSpotRadius=100;
  var targets=List.of(mock(server.life.Monster.class));
  assertSame(targets,BotGrindDoctrine.filterCandidates(entry,targets,System.currentTimeMillis()));
  BotGrindDoctrine.update(entry,bot,System.currentTimeMillis());
  assertFalse(BotGrindDoctrine.active(entry));
  assertSame(targets,BotGrindDoctrine.filterCandidates(entry,targets,System.currentTimeMillis()));
 }
}
