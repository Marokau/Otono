package server.bots;
import client.Character;
import client.Job;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
/** Dedicated Pink preparation context; Horntail retains its existing HP floors. */
final class BotPinkBeanReadiness {
 private static final Set<Character> MEMBERS=Collections.newSetFromMap(Collections.synchronizedMap(new WeakHashMap<>()));
 private BotPinkBeanReadiness() {}
 static void mark(Character b){if(b.getClient() instanceof client.BotClient&&!b.isGM()&&b.getEventInstance()==null)MEMBERS.add(b);}
 static int rangedGoal(Character b){return MEMBERS.contains(b)&&(b.getJob()==Job.NIGHTLORD||b.getJob()==Job.BOWMASTER)?8400:0;}
}
