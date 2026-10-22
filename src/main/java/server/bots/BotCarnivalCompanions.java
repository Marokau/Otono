package server.bots;

import client.BotClient;
import client.Character;
import client.Job;
import net.server.world.Party;
import scripting.npc.NPCConversationManager;
import java.util.*;

/** Spiegelmann's opt-in two-team crew. Never recruits population bots. */
public final class BotCarnivalCompanions {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BotCarnivalCompanions.class);
    private BotCarnivalCompanions() {}
    public static synchronized String assemble(Character leader) {
        if (leader.getClient() instanceof BotClient
                || leader.getMapId() != 980000000 && leader.getMapId() != 980030000
                || leader.getLevel() < BotCarnivalFormat.minimum(leader)
                || leader.getLevel() > BotCarnivalFormat.maximum(leader) || leader.getEventInstance() != null)
            return "Enter Carnival 1 at level 30-50 or Carnival 2 at level 51-70 first.";
        synchronized (BotCarnivalRun.CREWS) {
            for (var existing : BotCarnivalRun.CREWS.values())
                if (existing.leader == leader)
                    return "Your previous Carnival crew is still active. Dismiss it here, or wait for it to return and leave after you quit its party.";
        }
        boolean second=BotCarnivalFormat.second(leader);
        if (leader.getParty() != null && (leader.getParty().getLeaderId() != leader.getId()
                || leader.getParty().getMembers().size() > 1)) return "Leave or dismiss your current party first.";
        var bots = new ArrayList<Character>(); var manager = BotManager.getInstance();
        try {
            // Both sides have Cleric, Spearman and Assassin; a Hunter fills the human's opposing slot.
            for (var job : List.of(Job.CLERIC,Job.SPEARMAN,Job.ASSASSIN,Job.HUNTER,Job.CLERIC,Job.SPEARMAN,Job.ASSASSIN)) {
                String prefix = (second?"CPq2":"CPq") + (job==Job.CLERIC?"Mage":job==Job.SPEARMAN?"Spear":job==Job.ASSASSIN?"Thief":"Archer");
                int id = BotPqCompanionPool.acquire(leader,second?"cpq2":"cpq",job,prefix,BotCarnivalFormat.recruit(leader));
                var b = manager.loadOfflineBot(id,leader.getWorld(),leader.getClient().getChannel());
                manager.registerSpawnedBot(id,b,b); bots.add(b);
                manager.issueStop(manager.getEntryByBotCharId(id));
            }
            for(int i=0;i<3;i++)manager.partyUp(leader,bots.get(i));
            for(int i=4;i<7;i++)manager.partyUp(bots.get(3),bots.get(i));
            BotCarnivalRun.register(leader,bots.get(3),bots);
            return "Four-versus-four teams are ready. Your companions are a Cleric, Spearman and Assassin. The opposing team has those classes plus a Hunter. You are party leader.\r\n\r\nTalk to me again and choose Start bot Carnival match. Both teams earn CP normally; higher total CP wins. Spend CP with the Carnival window. Carried potions cannot be used; collect recovery drops. Claim your own reward afterward, then dismiss the crew here.";
        } catch (Exception failure) {
            log.warn("Could not assemble Carnival crew for {}",leader.getId(),failure);
            for (var b : bots) dismiss(b);
            return "I could not prepare both teams. Please try again shortly.";
        }
    }
    public static synchronized String begin(Character leader, NPCConversationManager cm) {
        var crew = BotCarnivalRun.CREWS.values().stream().filter(c -> c.leader == leader).findFirst().orElse(null);
        if (crew == null) return "Assemble a bot Carnival party first.";
        if (crew.started || leader.getMapId() != BotCarnivalFormat.recruit(crew.opponent)) return "A match is already being prepared or played.";
        if (leader.getLevel() < BotCarnivalFormat.minimum(crew.opponent) || leader.getLevel() > BotCarnivalFormat.maximum(crew.opponent) || leader.getParty() == null
                || leader.getParty().getLeaderId() != leader.getId() || leader.getParty().getMembers().size() != 4
                || crew.opponent.getParty() == null || crew.opponent.getParty().getMembers().size() != 4
                || crew.bots.stream().anyMatch(b -> b.getMapId() != BotCarnivalFormat.recruit(b) || b.getLevel() < BotCarnivalFormat.minimum(b) || b.getLevel() > BotCarnivalFormat.maximum(b)))
            return "Both teams must be back in their Carnival lobby, with four eligible players each. Dismiss an over-level crew and assemble again.";
        if (crew.opponent.getParty().getLeaderId() != crew.opponent.getId())
            return "The opposing party changed. Please dismiss the crew and assemble again.";
        for (var b : crew.bots)
            if (b.getParty() == null || b.getParty().getId() != leader.getParty().getId()
                    && b.getParty().getId() != crew.opponent.getParty().getId()
                    || b.getWorldServer().getPlayerStorage().getCharacterById(b.getId()) != b)
                return "The teams changed. Please dismiss the crew and assemble again.";
        boolean second=BotCarnivalFormat.second(crew.opponent);
        int field=-1;
        for(int i=second?2:4;i<(second?3:6);i++)
            if (!(second?cm.fieldTaken2(i):cm.fieldTaken(i))) {field=i;break;}
        if (field < 0) return "All large Carnival fields are busy. Please try again later.";
        for (var b : crew.bots) {
            BotPqProvisioner.provision(b);
            BotPotionManager.setupAutopotForBot(b);
            b.triggerAutopotAfterStatLoss(-1,-1); // prepare normally in the safe lobby
            // The client disables these keys in Carnival. Do so before any combat/stat loss.
            BotCarnivalRun.disableCarriedPotions(b);
        }
        if (crew.opponent.getClient().getCM() != null) crew.opponent.getClient().getCM().dispose();
        if (!BotNpcInteraction.talk(crew.opponent,BotCarnivalFormat.lobbyNpc(crew.opponent))) return "The opposing leader could not talk to Spiegelmann.";
        BotNpcInteraction.next(crew.opponent,field);
        if (!(second?cm.fieldLobbied2(field):cm.fieldLobbied(field))) return "The field could not be reserved. Please try again.";
        crew.field=field; crew.started=true;
        // Challenge delivery enters a Graal script on the opponent client. Its tick must
        // not answer the same context while delivery is still executing on this thread.
        synchronized (crew) { if(second)cm.challengeParty2(field);else cm.challengeParty(field); }
        return null;
    }
    public static synchronized String release(Character leader) {
        var crew = BotCarnivalRun.CREWS.values().stream().filter(c -> c.leader == leader).findFirst().orElse(null);
        if (crew == null) return "You have no Carnival crew to dismiss.";
        if (leader.getMapId() != BotCarnivalFormat.recruit(crew.opponent) || crew.bots.stream().anyMatch(b -> b.getMapId() != BotCarnivalFormat.recruit(b)
                || BotCarnivalRun.inInstance(b))) return "Finish the match and let both teams claim rewards and return first.";
        for (var b : crew.bots) dismiss(b);
        return "Both Carnival teams have been saved and dismissed.";
    }
    static void dismiss(Character b) {
        BotCarnivalRun.CREWS.remove(b);
        var manager=BotManager.getInstance(); var e=manager.getEntryByBotCharId(b.getId()); if(e==null)return;
        manager.applyOperatorCommand(e,BotEntry.OperatorCmd.IDLE,-1,0);
        if(b.getParty()!=null)Party.leaveParty(b.getParty(),b.getClient());
        b.saveCharToDB(true); b.getClient().disconnect(false,false);
    }
}
