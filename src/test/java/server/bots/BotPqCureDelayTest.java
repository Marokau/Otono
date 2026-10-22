package server.bots;
import client.Character;
import client.Disease;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BotPqCureDelayTest {
 @Test void waitsThreeSecondsAndDoesNotRestartTheClockEveryTick() {
  var b=mock(Character.class);when(b.getDiseasesSize()).thenReturn(1);when(b.hasDisease(Disease.WEAKEN)).thenReturn(true);
  assertFalse(BotPqCureDelay.ready(b,1000));assertFalse(BotPqCureDelay.ready(b,3999));assertTrue(BotPqCureDelay.ready(b,4000));
 }
 @Test void eachNewAilmentGetsItsOwnVisibleWindowAndCuredEpisodesReset() {
  var b=mock(Character.class);when(b.getDiseasesSize()).thenReturn(1);when(b.hasDisease(Disease.SEAL)).thenReturn(true);
  assertFalse(BotPqCureDelay.ready(b,1000));when(b.hasDisease(Disease.CURSE)).thenReturn(true);
  assertFalse(BotPqCureDelay.ready(b,3500));assertFalse(BotPqCureDelay.ready(b,4000));assertTrue(BotPqCureDelay.ready(b,6500));
  when(b.getDiseasesSize()).thenReturn(0);assertFalse(BotPqCureDelay.ready(b,6600));
  when(b.getDiseasesSize()).thenReturn(1);when(b.hasDisease(Disease.CURSE)).thenReturn(false);
  assertFalse(BotPqCureDelay.ready(b,7000));assertFalse(BotPqCureDelay.ready(b,9999));assertTrue(BotPqCureDelay.ready(b,10000));
 }
 @Test void stunAloneDoesNotTriggerAnAllCure() {
  var b=mock(Character.class);when(b.getDiseasesSize()).thenReturn(1);when(b.hasDisease(Disease.STUN)).thenReturn(true);
  assertFalse(BotPqCureDelay.ready(b,1000));assertFalse(BotPqCureDelay.ready(b,5000));
 }
}
