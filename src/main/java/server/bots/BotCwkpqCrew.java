package server.bots;

import client.Character;
import client.Job;
import client.inventory.InventoryType;
import client.inventory.manipulator.InventoryManipulator;
import net.server.world.Party;
import server.expeditions.Expedition;
import server.expeditions.ExpeditionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** Dedicated Crimsonwood Keep roster and native expedition entry. */
public final class BotCwkpqCrew {
    static final int CREW_LEVEL = 140;
    static final int RECRUIT = 610030020;
    private static final int HALL = 610030100;
    private static final int SIGILS = 610030200;
    private static final int JACK = 9201113;
    private static final List<Job> PARTY = List.of(Job.DARKKNIGHT, Job.BISHOP, Job.BOWMASTER, Job.NIGHTLORD, Job.BUCCANEER);
    private static final Map<Character, Crew> MEMBERS = new ConcurrentHashMap<>();
    private static final Map<Character, Crew> LEADERS = new ConcurrentHashMap<>();

    private record Crew(Character leader, List<Character> bots, AtomicBoolean hallwayHint,
                        BotCwkpqStages.State stages) {
        Crew(Character leader, List<Character> bots) {
            this(leader, List.copyOf(bots), new AtomicBoolean(), new BotCwkpqStages.State());
        }
    }

    private BotCwkpqCrew() {}

    public static synchronized String assemble(Character leader) {
        var type = ExpeditionType.CWKPQ;
        if (leader.getMapId() != RECRUIT || leader.getEventInstance() != null)
            return "Assemble the Crimsonwood Keep crew outside the altar with Jack.";
        if (leader.getLevel() < type.getMinLevel() || leader.getLevel() > type.getMaxLevel())
            return "Crimsonwood Keep requires level 90 to 255.";
        if (LEADERS.containsKey(leader)) return ready();
        if (leader.getParty() != null && (leader.getParty().getLeaderId() != leader.getId()
                || leader.getParty().getMembers().size() > 1))
            return "Leave your current party before assembling this dedicated crew.";

        var bots = new ArrayList<Character>();
        var manager = BotManager.getInstance();
        try {
            var jobs = new ArrayList<>(PARTY);
            jobs.addAll(PARTY);
            jobs.add(Job.NIGHTLORD);
            jobs.addAll(List.of(Job.DARKKNIGHT, Job.FP_ARCHMAGE, Job.IL_ARCHMAGE,
                    Job.BOWMASTER, Job.BUCCANEER, Job.NIGHTLORD));
            for (var job : jobs) {
                String prefix = switch (job) {
                    case DARKKNIGHT -> "CWKPqSpear";
                    case BISHOP -> "CWKPqPriest";
                    case FP_ARCHMAGE -> "CWKPqFire";
                    case IL_ARCHMAGE -> "CWKPqIce";
                    case BOWMASTER -> "CWKPqArcher";
                    case NIGHTLORD -> "CWKPqThief";
                    case BUCCANEER -> "CWKPqBrawl";
                    default -> throw new IllegalStateException("Unsupported Crimsonwood class");
                };
                int id = BotPqCompanionPool.acquire(leader, "cwkpq", job, prefix, RECRUIT);
                var spawn = manager.spawnPqCompanion(leader, id);
                if (!spawn.success()) throw new IllegalStateException(spawn.errorMessage());
                var bot = spawn.bot();
                bots.add(bot);
                manager.applyOperatorCommand(manager.getEntryByBotCharId(id), BotEntry.OperatorCmd.IDLE, -1, 0);
                prepareLevel(bot, job);
                BotZakumBooks.prepare(bot);
                BotPqProvisioner.provision(bot);
                BotPinkBeanWeapons.prepare(bot);
                restock(bot);
                BotPotionManager.setupAutopotForBot(bot);
            }
            Character partyLeader = leader;
            for (int i = 0; i < bots.size(); i++) {
                var bot = bots.get(i);
                if (i == 5 || i == 11) {
                    if (bot.getParty() != null) Party.leaveParty(bot.getParty(), bot.getClient());
                    if (!Party.createParty(bot, true)) throw new IllegalStateException("Additional party failed");
                    partyLeader = bot;
                } else {
                    manager.partyUp(partyLeader, bot);
                }
            }
            for (var partyOwner : List.of(leader, bots.get(5), bots.get(11))) {
                if (partyOwner.getParty() == null || partyOwner.getParty().getMembers().size() != 6)
                    throw new IllegalStateException("Crimsonwood parties were not assembled");
            }
            var crew = new Crew(leader, bots);
            LEADERS.put(leader, crew);
            for (var bot : bots) MEMBERS.put(bot, crew);
            for (var bot : bots) {
                var entry = manager.getEntryByBotCharId(bot.getId());
                manager.resumeFromOperatorCommand(entry);
                manager.issueStop(entry);
            }
            return ready();
        } catch (RuntimeException error) {
            org.slf4j.LoggerFactory.getLogger(BotCwkpqCrew.class).warn("Crimsonwood crew assembly failed", error);
            for (var bot : bots) dismiss(bot);
            LEADERS.remove(leader);
            return "I could not prepare the full Crimsonwood Keep crew. Please try again.";
        }
    }

    private static void prepareLevel(Character bot, Job target) {
        var entry = BotManager.getInstance().getEntryByBotCharId(bot.getId());
        entry.apAuto = true;
        if (target == Job.NIGHTLORD) entry.spVariant = "claw";
        while (bot.getLevel() < CREW_LEVEL) {
            if (bot.getLevel() >= 120 && bot.getJob() != target) {
                var previous = bot.getJob();
                bot.changeJob(target);
                BotBuildManager.handleJobAdvance(entry, bot, previous, target);
            }
            bot.levelUp(false);
            BotBuildManager.autoAssignSp(entry, bot);
            BotBuildManager.maybeRecomputeAutonomousApBuild(entry, bot);
            BotBuildManager.autoAssignAp(entry, bot);
        }
    }

    private static void restock(Character bot) {
        BotZakumSupplies.stock(bot, 2002023, 600);
        BotZakumSupplies.stock(bot, 2050004, 200);
        BotZakumSupplies.stock(bot, 4006000, 200);
        BotZakumSupplies.stock(bot, 4006001, 200);
        if (bot.getJob() == Job.NIGHTLORD) {
            // Native ranged attacks consume the first eligible inventory stack.
            // Remove both starter star types so the prepared Ilbis are actually used.
            for (var item : List.copyOf(bot.getInventory(InventoryType.USE).list())) {
                if (item.getItemId() == 2070000 || item.getItemId() == 2070015)
                    InventoryManipulator.removeFromSlot(bot.getClient(), InventoryType.USE,
                            item.getPosition(), item.getQuantity(), false);
            }
            BotZakumSupplies.stock(bot, 2070006, 12000);
        }
    }

    private static String ready() {
        return "Your seventeen companions are ready, with three archers and two damage mages for the Twisted Masters. Register the expedition with Jack. "
                + "We will register ourselves. Start once all eighteen are listed. "
                + "The crew will explain its current objective as you progress.";
    }

    public static boolean readyToStart(Character leader, Expedition expedition) {
        var crew = LEADERS.get(leader);
        if (crew == null) return true;
        if (!crew.bots.stream().allMatch(bot -> bot.getMap() == leader.getMap() && expedition.contains(bot)))
            return false;
        for (var bot : crew.bots) {
            BotRaidCooldowns.prepare(bot);
            restock(bot);
        }
        crew.hallwayHint.set(false);
        crew.stages.reset();
        return true;
    }

    static boolean belongsTo(Character bot, int owner) {
        var crew = MEMBERS.get(bot);
        return crew != null && crew.leader.getId() == owner;
    }

    public static synchronized String release(Character leader) {
        var crew = LEADERS.get(leader);
        if (crew == null) return "You have no active Crimsonwood Keep crew.";
        if (leader.getEventInstance() != null || crew.bots.stream().anyMatch(b -> b.getEventInstance() != null))
            return "Leave the expedition before dismissing the crew.";
        LEADERS.remove(leader);
        for (var bot : crew.bots) dismiss(bot);
        return "Your Crimsonwood Keep companions have been saved and logged off.";
    }

    private static void dismiss(Character bot) {
        MEMBERS.remove(bot);
        BotCwkpqRelics.unregister(bot);
        var expedition = bot.getClient().getChannelServer().getExpedition(ExpeditionType.CWKPQ);
        if (expedition != null && expedition.isRegistering() && expedition.contains(bot))
            expedition.removeMember(bot);
        BotHorntailCompanions.dismiss(bot);
    }

    public static boolean tick(BotEntry entry, Character bot, boolean ai) {
        var crew = MEMBERS.get(bot);
        if (crew == null) return false;
        entry.grinding = false;
        var leader = crew.leader;
        var event = bot.getEventInstance();
        if (event == null && (!leader.isLoggedin() || leader.getParty() == null
                || leader.getParty().getLeaderId() != leader.getId()
                || leader.getMapId() != RECRUIT && leader.getEventInstance() == null)) {
            release(leader);
            return true;
        }

        // A scout arriving ahead must not consume the human's stage briefing.
        // Reuse the common reading pause before the legacy stage controllers run.
        if (event != null && leader.getEventInstance() == event
                && leader.getMapId() <= bot.getMapId()
                && BotPqStageGuide.text("cwkpq", bot.getMapId()) != null) {
            if (leader.getMapId() != bot.getMapId()) return BotOpqRun.hold(entry, bot, ai);
            crew.stages.announced.add(bot.getMapId());
            if (bot.getMapId() == HALL) crew.hallwayHint.set(true);
            if (BotPqStageGuide.tick(entry, bot, leader, event, "cwkpq", System.currentTimeMillis()))
                return BotOpqRun.hold(entry, bot, ai);
        }

        if (bot.getMapId() == RECRUIT) {
            var expedition = bot.getClient().getChannelServer().getExpedition(ExpeditionType.CWKPQ);
            if (expedition != null && expedition.isLeader(leader) && expedition.isRegistering()
                    && !expedition.contains(bot)) {
                var approach = BotTravelManager.tickApproachNpc(entry, bot, RECRUIT, JACK, 3, ai, false, 150);
                if (approach == BotTravelManager.ApproachStatus.ARRIVED && ai
                        && bot.getClient().getCM() == null) {
                    synchronized (crew) {
                        if (!expedition.contains(bot)) BotNpcInteraction.talk(bot, JACK);
                    }
                }
                return true;
            }
        } else if (event != null && bot.getMapId() == HALL) {
            if (crew.hallwayHint.compareAndSet(false, true))
                BotPqChat.queue(entry, "Stage One: We will open the hidden passage at the far right. "
                        + "Once it opens, take the portal partway back on the right.", false);
            if (event.getIntProperty("glpq1") != 0)
                return BotMagatiaRun.portal(entry, bot, "next00", ai);
            if (bot == crew.bots.get(0))
                return BotMagatiaRun.portal(entry, bot, "next01", ai);
        } else if (event != null && bot.getMapId() >= SIGILS) {
            return BotCwkpqStages.tick(entry, bot, crew.stages, crew.bots, leader, event, ai);
        }
        return BotOpqRun.hold(entry, bot, ai);
    }
}
