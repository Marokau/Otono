package server.bots;

import client.Character;
import constants.skills.Bishop;
import java.util.List;

/** Prefer a usable party Resurrection; otherwise use the native death-room return. */
final class BotPinkBeanRecovery {
    private BotPinkBeanRecovery() {}
    static boolean waitForResurrection(Character dead, List<Character> members) {
        if (server.expeditions.PinkBeanLives.exhausted(dead)) return true;
        if (!BotHorntailCombat.safe(dead, dead.getPosition(), dead.getMap().getAllMonsters())) return false;
        for (var member : members) {
            if (member != dead && member.isAlive() && member.getMap() == dead.getMap()
                    && member.getParty() != null && dead.getParty() != null
                    && member.getParty().getId() == dead.getParty().getId()
                    && member.getSkillLevel(Bishop.RESURRECTION) > 0
                    && !member.skillIsCooling(Bishop.RESURRECTION)) return true;
        }
        return false;
    }
}
