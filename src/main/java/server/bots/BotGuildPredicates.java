package server.bots;
import java.util.function.IntPredicate;
/** Explicit role pickup filters shared by GPQ stage intentions. */
final class BotGuildPredicates {
 static final IntPredicate SPEAR=id->id==4001025;
 static final IntPredicate ANY_ITEM=id->id>0;
 private BotGuildPredicates(){}
}
