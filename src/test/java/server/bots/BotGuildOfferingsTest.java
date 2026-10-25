package server.bots;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class BotGuildOfferingsTest {
 @Test void feedbackHandlesDuplicateOfferings(){assertEquals(100,BotGuildOfferings.score(0,0));assertEquals(20,BotGuildOfferings.score(27,228));assertEquals(4,BotGuildOfferings.score(0,85));assertEquals(27,BotGuildOfferings.fromDecimal(123));}
 @Test void publicCluesSolveEveryCombinationWithinSevenChecks(){var all=new ArrayList<Integer>();for(int n=0;n<256;n++)all.add(n);assertTrue(depth(all,1)<=7);}
 private int depth(List<Integer> remaining,int at){assertFalse(remaining.isEmpty());assertTrue(at<=7);int guess=BotGuildOfferings.choose(remaining),max=at;var groups=new HashMap<Integer,List<Integer>>();for(int c:remaining)if(c!=guess)groups.computeIfAbsent(BotGuildOfferings.score(c,guess),k->new ArrayList<>()).add(c);for(var group:groups.values())max=Math.max(max,depth(group,at+1));return max;}
 @Test void clothingMustBeReturnedBottomToTop(){assertEquals(0,BotGuildWaterway.clothingOrder(4001032));assertEquals(1,BotGuildWaterway.clothingOrder(4001031));assertEquals(2,BotGuildWaterway.clothingOrder(4001033));assertEquals(3,BotGuildWaterway.clothingOrder(4001034));}
}
