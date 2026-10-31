package server.bots;
import client.Character;
import java.util.List;
/** The human remains a party member without becoming a timed-raid readiness gate. */
final class BotPinkBeanParticipation {
 private BotPinkBeanParticipation() {}
 static boolean human(Character b){var c=BotPinkBeanRun.crew(b);return c!=null&&c.human&&c.leader==b;}
 static List<Character> readyMembers(BotPinkBeanRun.Crew c){return c.human?List.copyOf(c.bots):BotPinkBeanRun.all(c);}
}
