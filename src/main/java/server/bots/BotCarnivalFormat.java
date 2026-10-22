package server.bots;
import client.Character;

/** Carnival bracket metadata shared by recruitment and match lifecycle. */
final class BotCarnivalFormat {
    private BotCarnivalFormat() {}
    static boolean second(Character player) {
        return player.getName()!=null&&player.getName().startsWith("CPq2")
                ||player.getMapId()>=980030000&&player.getMapId()<980040000;
    }
    static int recruit(Character player) {return second(player)?980030000:980000000;}
    static int minimum(Character player) {return second(player)?51:30;}
    static int maximum(Character player) {return second(player)?70:50;}
    static int lobbyNpc(Character player) {return second(player)?2042005:2042000;}
    static int resultNpc(Character player) {return second(player)?2042007:2042002;}
}
