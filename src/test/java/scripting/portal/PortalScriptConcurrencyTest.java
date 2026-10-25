package scripting.portal;
import org.junit.jupiter.api.Test;import server.maps.Portal;import client.Client;import java.util.*;import java.util.concurrent.*;import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
class PortalScriptConcurrencyTest {
 @Test void cachedScriptIsNeverEnteredConcurrently()throws Exception{
  var manager=PortalScriptManager.getInstance();var f=PortalScriptManager.class.getDeclaredField("scripts");f.setAccessible(true);
  @SuppressWarnings("unchecked") var scripts=(Map<String,PortalScript>)f.get(manager);
  var portal=mock(Portal.class);when(portal.getScriptName()).thenReturn("otono_concurrency_test");
  var active=new AtomicInteger();var peak=new AtomicInteger();var gate=new CyclicBarrier(2);
  PortalScript script=interaction->{int n=active.incrementAndGet();peak.accumulateAndGet(n,Math::max);try{Thread.sleep(100);}catch(InterruptedException e){Thread.currentThread().interrupt();}finally{active.decrementAndGet();}return true;};
  synchronized(scripts){scripts.put("portal/otono_concurrency_test.js",script);}
  var executor=Executors.newFixedThreadPool(2);
  try{Callable<Boolean> run=()->{gate.await(2,TimeUnit.SECONDS);return manager.executePortalScript(portal,mock(Client.class));};var one=executor.submit(run);var two=executor.submit(run);assertTrue(one.get(3,TimeUnit.SECONDS));assertTrue(two.get(3,TimeUnit.SECONDS));assertEquals(1,peak.get());}
  finally{executor.shutdownNow();synchronized(scripts){scripts.remove("portal/otono_concurrency_test.js");}}
 }
}
