package server.bots;

import client.BotClient;
import client.Character;
import client.Job;
import client.inventory.InventoryType;
import client.inventory.manipulator.InventoryManipulator;
import config.YamlConfig;
import constants.id.MapId;
import io.netty.buffer.Unpooled;
import net.packet.ByteBufInPacket;
import net.server.channel.handlers.GuildOperationHandler;
import net.server.coordinator.matchchecker.MatchCheckerListenerFactory.MatchCheckerType;
import net.server.world.Party;
import server.maps.MapItem;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/** Dedicated GPQ fixtures. Guild creation/invites/withdrawal use the ordinary player handler. */
public final class BotGuildCompanions {
    public static final int RECRUIT = 101030104, HQ = MapId.GUILD_HQ;
    static final int EARRING = 1032033, WAIT = 990000000, OUTSKIRTS = 990000100, EXIT = 990001100;
    private static final Map<Integer, Crew> CREWS = new ConcurrentHashMap<>();
    private static final Map<Character, Crew> BOTS = Collections.synchronizedMap(new WeakHashMap<>());
    static final class Crew {
        Character leader;
        List<Character> bots = List.of();
        boolean founders, test;
        final Set<String> said = ConcurrentHashMap.newKeySet();
        final Map<Integer, Long> next = new HashMap<>();
    }
    private BotGuildCompanions() {}
    public static String requirements() {
        return "Guild PQ requires six guild members, led by a guild master or junior master. "
                + "Creating your own guild is a one-time step: " + YamlConfig.config.server.CREATE_GUILD_COST
                + " mesos and " + YamlConfig.config.server.CREATE_GUILD_MIN_PARTNERS
                + " eligible founders at Heracle in Orbis. We supply five dedicated cofounders. "
                + "You choose a name of 3-12 letters and confirm the normal fee. Your guild remains after the bots leave. "
                + "The crew includes a runner for the level-30-and-under room; you do not need a low-level character yourself.";
    }
    public static synchronized String prepareGuild(Character leader) {
        return prepare(leader, true, false);
    }
    public static synchronized String assemble(Character leader) {
        return prepare(leader, false, false);
    }
    // Package-only live harness entry. The test leader is an isolated fixture, never a player's account.
    static synchronized String prepareTest(Character leader, boolean founders) {
        return prepare(leader, founders, true);
    }
    static String eligibility(Character leader, boolean founders, boolean test) {
        if (leader == null || !test && leader.getClient() instanceof BotClient
                || leader.getEventInstance() != null || leader.getMapId() != RECRUIT && leader.getMapId() != HQ)
            return "Please speak to Shuang at the GPQ camp or Heracle at Guild Headquarters, outside an active PQ.";
        if (founders) {
            if (leader.getGuildId() > 0) return "You already have a guild. Use Assemble a GPQ bot crew at the camp.";
            if (leader.getMeso() < YamlConfig.config.server.CREATE_GUILD_COST)
                return "Bring " + YamlConfig.config.server.CREATE_GUILD_COST + " mesos for Heracle's normal guild creation fee.";
            if (YamlConfig.config.server.CREATE_GUILD_MIN_PARTNERS != 6)
                return "This five-bot cofounder crew currently requires the standard six-founder setting.";
        } else if (!leader.isGuildLeader()) return "You must be your guild's master or junior master to register GPQ.";
        var crew = CREWS.get(leader.getId());
        if (leader.getParty() != null && (leader.getParty().getLeaderId() != leader.getId()
                || leader.getParty().getMembers().stream().anyMatch(m -> m.getId() != leader.getId()
                && (crew == null || crew.bots.stream().noneMatch(b -> b.getId() == m.getId())))))
            return "Please leave your current party first; I will make you leader of the dedicated GPQ crew.";
        return null;
    }
    private static String prepare(Character leader, boolean founders, boolean test) {
        String error = eligibility(leader, founders, test); if (error != null) return error;
        if (!founders && leader.getMapId() != RECRUIT) return "Return to Shuang at the GPQ camp to assemble your party.";
        var existing = CREWS.get(leader.getId());
        if (existing != null) {
            if (founders && existing.founders) return "Your cofounders are waiting at Heracle. Choose Create a Guild there and enter your name.";
            String problem = joinAndParty(existing); if (problem != null) return problem;
            return readyText();
        }
        if (!founders && leader.getGuild().getCapacity() - leader.getGuild().getMembers().size()
                + BotGuildMembership.reusableSeats(leader) < 5)
            return "Your guild needs five free membership slots for this temporary GPQ crew. Expand it or free slots first.";
        var c = new Crew(); c.leader = leader; c.founders = founders; c.test = test;
        var bots = new ArrayList<Character>(); var manager = BotManager.getInstance();
        try {
            for (var job : List.of(Job.PRIEST, Job.HERMIT, Job.RANGER, Job.DRAGONKNIGHT, Job.BOWMAN)) {
                String prefix = switch (job) {
                    case PRIEST -> "GPqMage"; case HERMIT -> "GPqThief"; case RANGER -> "GPqArcher";
                    case DRAGONKNIGHT -> "GPqSpear"; default -> "GPqScout";
                };
                int id = BotPqCompanionPool.acquire(leader, "gpq", job, prefix, RECRUIT);
                var result = manager.spawnPqCompanion(leader, id);
                if (!result.success()) throw new IllegalStateException(result.errorMessage());
                var bot = result.bot(); bots.add(bot); BOTS.put(bot, c);
                manager.applyOperatorCommand(manager.getEntryByBotCharId(id), BotEntry.OperatorCmd.IDLE, -1, 0);
                if (bot.getGuildId() > 0 && bot.getGuildId() != leader.getGuildId()) leaveGuild(bot);
                BotMagatiaRun.provision(manager.getEntryByBotCharId(id), bot);
            }
            c.bots = List.copyOf(bots); CREWS.put(leader.getId(), c);
            if (founders) {
                if (leader.getParty() != null) Party.leaveParty(leader.getParty(), leader.getClient());
                // Explicit NPC travel offer, outside all instances. This is not a puzzle shortcut.
                leader.changeMap(HQ, 0);
                for (var bot : bots) { if (bot.getParty() != null) Party.leaveParty(bot.getParty(), bot.getClient()); bot.changeMap(HQ, 0); }
            } else {
                error = joinAndParty(c); if (error != null) throw new IllegalStateException(error);
            }
            for (var bot : bots) { var e = manager.getEntryByBotCharId(bot.getId()); manager.resumeFromOperatorCommand(e); manager.issueStop(e); }
            return founders ? "Your five cofounders are here. Talk to Heracle: Create a Guild, confirm the fee, and enter a name of 3-12 letters. We accept our normal founder confirmations. Then use WhereLevel to return to GPQ and select Assemble again; we regroup at the camp." : readyText();
        } catch (RuntimeException failure) {
            org.slf4j.LoggerFactory.getLogger(BotGuildCompanions.class).warn("GPQ crew preparation failed for {}", leader.getId(), failure);
            for (var b : bots) dismiss(b);
            CREWS.remove(leader.getId());
            return "I could not prepare the crew: " + failure.getMessage();
        }
    }
    private static String readyText() {
        return "You lead this six-person GPQ party. Our five companions have joined your guild, including a scout for the level-30-and-under room. Register your guild with Shuang: you enter when called and we join normally. After the three-minute gathering timer, enter the lower portal first. Earn and equip your own Protector Rock earrings, follow our stage guidance and make the leader NPC checks. Dismiss us at camp when finished; your guild and its points remain yours.";
    }
    private static String joinAndParty(Crew c) {
        var leader = c.leader;
        if (!leader.isGuildLeader()) return "Only your guild master or junior master can register this crew.";
        long missing = c.bots.stream().filter(b -> b.getGuildId() != leader.getGuildId()).count();
        if (leader.getGuild().getCapacity() - leader.getGuild().getMembers().size() < missing)
            return "Your guild does not have enough free slots for the crew.";
        for (var bot : c.bots) {
            if (bot.getEventInstance() != null) return "Wait for all companions to leave their current GPQ first.";
            if (bot.getGuildId() != leader.getGuildId()) {
                if (bot.getGuildId() > 0) leaveGuild(bot);
                invite(leader, bot);
                if (bot.getGuildId() != leader.getGuildId()) return "Could not complete the normal guild invitation for " + bot.getName() + ".";
            }
            var entry = BotManager.getInstance().getEntryByBotCharId(bot.getId());
            if (entry == null) return "A companion has logged off. Dismiss this crew and assemble it again.";
            BotMagatiaRun.provision(entry, bot);
            if (bot.getMapId() != RECRUIT) bot.changeMap(RECRUIT, 0);
            BotManager.getInstance().partyUp(leader, bot);
        }
        c.founders = false; c.said.clear();
        return leader.getParty() != null && leader.getParty().getMembers().size() == 6 ? null : "The six-person party is not ready.";
    }
    static void invite(Character leader, Character bot) {
        var request = Unpooled.buffer(); request.writeByte(0x05); string(request, bot.getName()); handle(leader, request);
        var answer = Unpooled.buffer(); answer.writeByte(0x06).writeIntLE(leader.getGuildId()).writeIntLE(bot.getId()); handle(bot, answer);
    }
    private static void handle(Character who, io.netty.buffer.ByteBuf bytes) {
        try { new GuildOperationHandler().handlePacket(new ByteBufInPacket(bytes), who.getClient()); }
        finally { bytes.release(); }
    }
    static void leaveGuild(Character bot) {
        if (bot.getGuildId() <= 0) return;
        var bytes = Unpooled.buffer(); bytes.writeByte(0x07).writeIntLE(bot.getId()); string(bytes, bot.getName()); handle(bot, bytes);
    }
    private static void string(io.netty.buffer.ByteBuf bytes, String text) {
        byte[] data = text.getBytes(java.nio.charset.StandardCharsets.US_ASCII); bytes.writeShortLE(data.length).writeBytes(data);
    }
    static boolean belongsTo(Character bot, int id) { var c = BOTS.get(bot); return c != null && c.leader.getId() == id; }
    public static boolean inInstance(Character bot) { return bot != null && bot.getEventInstance() != null && bot.getMapId() >= WAIT && bot.getMapId() <= EXIT; }
    public static boolean tick(BotEntry e, Character b, boolean ai) {
        var c = BOTS.get(b); if (c == null) return false;
        e.grinding = false;
        if (b.isAlive()) b.triggerAutopotAfterStatLoss(-1,-1);
        if (inInstance(b) && BotLpqCures.tryCure(b)) return BotOpqRun.hold(e,b,ai);
        var leader = c.leader;
        if (leader.getWorldServer().getPlayerStorage().getCharacterById(leader.getId()) != leader) {
            if (!inInstance(b)) dismiss(b);
            return BotOpqRun.hold(e, b, ai);
        }
        if (!inInstance(b) && !c.founders && (leader.getParty() == null || b.getParty() == null || leader.getParty().getId() != b.getParty().getId())) {
            dismiss(b); return true;
        }
        if (c.founders && b.getMapId() == HQ) {
            var coordinator = b.getWorldServer().getMatchCheckerCoordinator();
            if (b.getGuildId() == 0 && coordinator.getMatchConfirmationLeaderid(b.getId()) == leader.getId()
                    && coordinator.getMatchConfirmationType(b.getId()) == MatchCheckerType.GUILD_CREATION && ready(c, b)) {
                var bytes = Unpooled.buffer(); bytes.writeByte(0x1e).writeIntLE(0).writeByte(1); handle(b, bytes);
            }
            return BotOpqRun.hold(e, b, ai);
        }
        if (b.getMapId() == RECRUIT) {
            if (inInstance(leader) && leader.getEventInstance().getIntProperty("canJoin") == 1) return joinEvent(e, b, c, ai);
            BotTravelManager.tickApproachNpc(e,b,RECRUIT,9040000,2,ai,false,100);
            return true;
        }
        if (inInstance(b) && b.getMapId() >= OUTSKIRTS && b.getMapId() < EXIT && b.isAlive() && !BotGuildBoss.sacrificing(b)) {
            var worn = b.getInventory(InventoryType.EQUIPPED).getItem((short)-4);
            if (worn == null || worn.getItemId() != EARRING) {
                for (var item : b.getInventory(InventoryType.EQUIP).list()) if (item.getItemId() == EARRING) {
                    InventoryManipulator.equip(b.getClient(), item.getPosition(), (short)-4);
                    return BotOpqRun.hold(e,b,ai);
                }
            }
        }
        if (b.getMapId() == WAIT) {
            if (c.said.add("waiting")) BotPqChat.queue(e, "Gathering Room: We must wait three minutes for guild members. The lower portal opens when gathering ends; enter first and we will follow.", false);
            long until = Long.parseLong(b.getEventInstance().getProperty("entryTimestamp"));
            if (System.currentTimeMillis() < until || !c.test && leader.getMapId() == WAIT) return BotOpqRun.hold(e, b, ai);
            return BotMagatiaRun.portal(e, b, "join00", ai);
        }
        if (b.getMapId() == OUTSKIRTS || b.getMapId() == OUTSKIRTS + 100) {
            String key = "earrings-" + b.getMapId();
            if (BotGuildRun.intro(e,b,BotGuildRun.state(b),key,"Outskirts: Break glowing rocks and equip the Protector Rock earrings before going onward. We will collect our own pairs. You need to collect and equip yours too.",ai)) return true;
            var equipped = b.getInventory(InventoryType.EQUIPPED).getItem((short)-4);
            if (equipped == null || equipped.getItemId() != EARRING) {
                for (var item : b.getInventory(InventoryType.EQUIP).list()) if (item.getItemId() == EARRING) {
                    InventoryManipulator.equip(b.getClient(), item.getPosition(), (short)-4); return BotOpqRun.hold(e,b,ai);
                }
                MapItem drop = b.getMap().getDroppedItems().stream().filter(d -> !d.isPickedUp() && d.getItemId() == EARRING && d.canBePickedBy(b))
                        .min(Comparator.comparingDouble(d -> b.getPosition().distanceSq(d.getPosition()))).orElse(null);
                if (drop != null) { BotZakumPqRun.stepTowardsAndPickUp(e,b,drop,ai); return true; }
                return BotGuildActions.smash(e,b,r -> (r.getId()==9202000 || r.getId()==9202009) && r.getState()<5,ai);
            }
            if (!c.test && leader.getMapId() <= b.getMapId()) return BotOpqRun.hold(e,b,ai);
            return BotMagatiaRun.portal(e,b,"next00",ai);
        }
        if (b.getMapId() == 990000300) return BotGuildStatues.tick(e,b,leader,c.bots,c.test,ai);
        if (b.getMapId() == EXIT) return exit(e,b,c,ai);
        if (inInstance(b)) return BotGuildRun.tick(e,b,c,ai);
        return BotOpqRun.hold(e, b, ai);
    }
    private static boolean joinEvent(BotEntry e, Character b, Crew c, boolean ai) {
        var status = BotTravelManager.tickApproachNpc(e,b,RECRUIT,9040000,2,ai,false,140);
        if (status == BotTravelManager.ApproachStatus.ARRIVED && ready(c,b)) {
            if (b.getClient().getCM() == null) BotNpcInteraction.talk(b,9040000); else BotNpcInteraction.next(b,1);
        }
        return true;
    }
    private static boolean exit(BotEntry e, Character b, Crew c, boolean ai) {
        var approach=BotTravelManager.tickApproachNpc(e,b,EXIT,9040001,2,ai,false,100);
        if(approach==BotTravelManager.ApproachStatus.ARRIVED && ready(c,b)) {
            if (b.getClient().getCM()==null) BotNpcInteraction.talk(b,9040001); else BotNpcInteraction.next(b,1);
        }
        return true;
    }
    private static boolean ready(Crew c, Character b) {
        synchronized(c) { long now=System.currentTimeMillis(); if(now<c.next.getOrDefault(b.getId(),0L))return false;
            c.next.put(b.getId(),now+ThreadLocalRandom.current().nextLong(850,1250));return true; }
    }
    public static synchronized String release(Character leader) {
        var c=CREWS.get(leader.getId());if(c==null)return "You have no dedicated GPQ crew to dismiss.";
        if(leader.getEventInstance()!=null || c.bots.stream().anyMatch(BotGuildCompanions::inInstance))return "Finish or leave GPQ before dismissing the crew.";
        for(var b:List.copyOf(c.bots))dismiss(b);
        CREWS.remove(leader.getId());return "Your GPQ bots have left your guild, been saved and logged off. Your guild remains yours.";
    }
    static void dismiss(Character b) {
        var c=BOTS.remove(b);if(c==null)return;
        var manager=BotManager.getInstance();var e=manager.getEntryByBotCharId(b.getId());
        if(e==null){if(c.bots.stream().noneMatch(BOTS::containsKey))CREWS.remove(c.leader.getId(),c);return;}
        manager.applyOperatorCommand(e,BotEntry.OperatorCmd.IDLE,-1,0);
        var coordinator=b.getWorldServer().getMatchCheckerCoordinator();
        if(coordinator.getMatchConfirmationLeaderid(b.getId())==c.leader.getId())coordinator.answerMatchConfirmation(b.getId(),false);
        leaveGuild(b);
        if(b.getParty()!=null)Party.leaveParty(b.getParty(),b.getClient());
        b.saveCharToDB(true);b.getClient().disconnect(false,false);
        if(c.bots.stream().noneMatch(BOTS::containsKey))CREWS.remove(c.leader.getId(),c);
    }
}
