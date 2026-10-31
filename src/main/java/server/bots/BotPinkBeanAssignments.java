package server.bots;

/** Keep complete supported parties together while working on both statue sides. */
final class BotPinkBeanAssignments {
    private BotPinkBeanAssignments() {}
    static boolean rightParty(int wave,int party) {
        if(wave==3)return false;
        if(wave==4)return party==0||party==1||party==3;
        return (party&1)==1;
    }
}
