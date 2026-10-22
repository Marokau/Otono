package server.bots;

import client.BotClient;
import client.Character;
import client.Job;
import client.inventory.InventoryType;
import scripting.event.EventInstanceManager;
import server.life.NPC;
import server.maps.MapItem;
import server.maps.Reactor;
import java.awt.Point;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntPredicate;

/** Dedicated Romeo/Juliet crews using normal inputs, observed puzzle results and real drops. */
public final class BotMagatiaRun {
    public static final int ZENUMIST = 261000011, ALCADNO = 261000021;
    static final int LIQUID = 4001132, KEY = 4001133, LEFT_FILE = 4001134, RIGHT_FILE = 4001135;
    private static final Map<Character, State> RUNS = Collections.synchronizedMap(new WeakHashMap<>());
    static final class Crew {
        Character leader, mage, thief, collector;
        List<Character> bots;
        boolean human;
        int base, recruit;
        EventInstanceManager event;
        final Set<Integer> books = new HashSet<>();
        final Map<Integer, Character> bookClaims = new HashMap<>();
        final Set<String> notices = new HashSet<>();
        final int[][] learned = new int[4][10];
        final int[] rooms = new int[4];
    }
    static final class State {
        Crew crew;
        long nextAction, pendingUntil;
        int pendingItem, room = -1, puzzleRow = -1, puzzleColumn = -1;
        final int[] attempts = new int[10];
        final int[] known = new int[10];
        boolean finished, rewardSeen;
        boolean fileDelivered;
        int book = -1;
        State() { Arrays.fill(known, -1); }
    }
    private BotMagatiaRun() {}
    public static boolean inInstance(Character bot) {
        if (bot == null || bot.getEventInstance() == null) return false;
        int id = bot.getMapId();
        return id >= 926100000 && id <= 926100700 || id >= 926110000 && id <= 926110700;
    }
    static boolean belongsTo(Character bot, int leader) {
        var s = RUNS.get(bot); return s != null && s.crew.human && s.crew.leader.getId() == leader;
    }
    static void unregister(Character bot) { RUNS.remove(bot); }
    public static void startCrew(List<Character> bots) { start(bots.getFirst(), bots, false); }
    static void startHuman(Character leader, List<Character> bots) { start(leader, bots, true); }
    private static void start(Character leader, List<Character> bots, boolean human) {
        if (bots.size() + (human ? 1 : 0) != 4 || bots.stream().map(Character::getId).distinct().count() != bots.size()
                || human && (leader.getClient() instanceof BotClient || bots.contains(leader)))
            throw new IllegalArgumentException("Magatia requires four distinct participants");
        if (leader.getMapId() != ZENUMIST && leader.getMapId() != ALCADNO || leader.getLevel() < 71
                || leader.getLevel() > 85 || leader.getEventInstance() != null) throw new IllegalArgumentException("Ineligible Magatia leader");
        var ids = new HashSet<>(bots.stream().map(Character::getId).toList()); ids.add(leader.getId());
        var manager = BotManager.getInstance();
        for (var bot : bots) {
            if (!(bot.getClient() instanceof BotClient) || bot.isGM() || bot.getLevel() < 71 || bot.getLevel() > 85
                    || bot.getMapId() != leader.getMapId() || bot.getEventInstance() != null || bot.getWorld() != leader.getWorld()
                    || bot.getClient().getChannel() != leader.getClient().getChannel()
                    || manager.getEntryByBotCharId(bot.getId()) == null
                    || bot.getParty() != null && bot.getParty().getMembers().stream().anyMatch(p -> !ids.contains(p.getId())))
                throw new IllegalArgumentException("Ineligible Magatia companion");
        }
        var crew = new Crew(); crew.leader = leader; crew.bots = List.copyOf(bots); crew.human = human;
        crew.recruit = leader.getMapId(); crew.base = crew.recruit == ZENUMIST ? 926100000 : 926110000;
        crew.mage = bots.stream().filter(b -> b.getJob().isA(Job.MAGICIAN)).findFirst().orElseThrow();
        crew.thief = bots.stream().filter(b -> b.getJob().isA(Job.THIEF)).findFirst().orElseThrow();
        crew.collector = bots.stream().filter(b -> b != crew.mage && b != crew.thief).findFirst().orElseThrow();
        for (var bot : bots) {
            var entry = manager.getEntryByBotCharId(bot.getId());
            manager.resumeFromOperatorCommand(entry); manager.issueStop(entry);
            BotBuildManager.setAutoApBuild(entry, bot); provision(entry, bot);
            var s = new State(); s.crew = crew; RUNS.put(bot, s);
            if (bot != leader) manager.partyUp(leader, bot);
        }
        if (leader.getParty() == null || leader.getParty().getLeaderId() != leader.getId()
                || leader.getParty().getMembers().size() != 4) throw new IllegalStateException("Could not assemble Magatia party");
    }
    static void provision(BotEntry entry, Character bot) {
        BotPqProvisioner.provision(bot);
        BotZakumSupplies.stock(bot, 2050004, 100);
        BotZakumCompanions.provisionSkillConsumables(entry, bot);
        if (bot.getJob().isA(Job.ASSASSIN)) {
            for (var item : List.copyOf(bot.getInventory(InventoryType.USE).list()))
                if (item.getItemId() / 10000 == 207 && item.getItemId() != 2070006)
                    client.inventory.manipulator.InventoryManipulator.removeFromSlot(bot.getClient(), InventoryType.USE,
                            item.getPosition(), item.getQuantity(), false);
            BotZakumSupplies.stock(bot, 2070006, 6000);
        }
    }
    public static void refresh(Character leader) {
        if (leader.getEventInstance() != null) throw new IllegalArgumentException("Prepare companions before entering");
        if (leader.getParty() == null) return;
        for (var member : leader.getParty().getMembers()) {
            var e = BotManager.getInstance().getEntryByBotCharId(member.getId());
            if (e != null && belongsTo(e.bot, leader.getId())) {
                if (e.bot.getEventInstance() != null || e.bot.getMapId() != leader.getMapId())
                    throw new IllegalStateException("Wait for your companions to return first");
                provision(e, e.bot);
            }
        }
    }
    static boolean canLoot(Character bot, int item) {
        var s = RUNS.get(bot); if (s == null) return false;
        if (item == LEFT_FILE || item == RIGHT_FILE) return !s.crew.human && bot == s.crew.leader
                || bot.getMapId() == s.crew.base + 201 || bot.getMapId() == s.crew.base + 202;
        return bot == s.crew.collector && !(s.pendingItem == item && System.currentTimeMillis() < s.pendingUntil);
    }
    public static boolean tick(BotEntry entry, Character bot, boolean ai) {
        var s = RUNS.get(bot); if (s == null) return false;
        var c = s.crew; entry.grinding = false;
        if (bot.isAlive()) bot.triggerAutopotAfterStatLoss(-1, 0);
        if (bot.getMapId() == c.base + 700) return portal(entry, bot, "out00", ai);
        // Completed crews must finish ordinary rewards/exits even if their human
        // has returned to town or replaced/disbanded the party.
        if (inInstance(bot) && bot.getEventInstance().isEventCleared()) {
            int exitRoom = bot.getMapId() - c.base;
            if (exitRoom == 600) { s.rewardSeen = true; return talk(entry, bot, 2112018, ai); }
            if (exitRoom == 500) return talk(entry, bot, c.base == 926100000 ? 2112001 : 2112011, ai);
            if (exitRoom == 401) return talk(entry, bot, c.base == 926100000 ? 2112006 : 2112005, ai);
        }
        if (c.human && !validHuman(c, bot)) {
            if (bot.getMapId() == c.recruit) { BotMagatiaCompanions.dismiss(bot); return true; }
            if (inInstance(bot) && !bot.getEventInstance().isEventCleared() && bot.getParty() != null)
                net.server.world.Party.leaveParty(bot.getParty(), bot.getClient());
            if (bot.getMapId() == c.base + 700) return portal(entry, bot, "out00", ai);
            return BotOpqRun.hold(entry, bot, ai);
        }
        if (c.human && inInstance(bot) && BotPqStageGuide.tick(entry,bot,c.leader,bot.getEventInstance(),"mpq",System.currentTimeMillis()))
            return BotOpqRun.hold(entry,bot,ai);
        int guideRoom=bot.getMapId()-c.base;
        if(c.human && inInstance(bot) && c.leader.getMapId()<bot.getMapId() && guideRoom!=201 && guideRoom!=202
                && !(guideRoom>=301 && guideRoom<=304))
            return BotOpqRun.hold(entry,bot,ai);
        if (!inInstance(bot)) {
            if (s.rewardSeen && bot.getMapId() == c.recruit) s.finished = true;
            if (!c.human && !s.finished && bot == c.leader) return talk(entry, bot, c.recruit == ZENUMIST ? 2112004 : 2112003, ai);
            return BotOpqRun.hold(entry, bot, ai);
        }
        synchronized (c) {
            if (c.event != bot.getEventInstance()) reset(c, bot.getEventInstance());
        }
        if (c.human && c.leader.getMapId() >= c.base + 301 && c.leader.getMapId() <= c.base + 304)
            for (int row = 0; row < 10; row++) shareClue(c, row);
        if (BotLpqCures.tryCure(bot)) return BotOpqRun.hold(entry, bot, ai);
        int room = bot.getMapId() - c.base;
        if (room == 700) return portal(entry, bot, "out00", ai);
        if (room == 500) return talk(entry, bot, c.base == 926100000 ? 2112001 : 2112011, ai);
        if (room == 600) { s.rewardSeen = true; return talk(entry, bot, 2112018, ai); }
        if (c.event.isEventCleared() && room == 401) return talk(entry, bot, c.base == 926100000 ? 2112006 : 2112005, ai);
        if (System.currentTimeMillis() < s.pendingUntil) return BotOpqRun.hold(entry, bot, ai);
        return switch (room) {
            case 0 -> books(entry, bot, s, ai);
            case 1 -> c.event.getIntProperty("statusStg2") == 1 ? advance(entry, bot, "pt00", ai) : BotOpqRun.grind(entry, bot, ai);
            case 100 -> beakers(entry, bot, s, ai);
            case 200 -> laboratory(entry, bot, s, ai);
            case 201, 202 -> research(entry, bot, s, ai);
            case 203 -> office(entry, bot, s, ai);
            case 300 -> chooseRoom(entry, bot, s, ai);
            case 301, 302, 303, 304 -> puzzle(entry, bot, s, ai);
            case 400 -> advance(entry, bot, "pt00", ai);
            case 401 -> battle(entry, bot, s, ai);
            default -> BotOpqRun.hold(entry, bot, ai);
        };
    }
    private static boolean validHuman(Crew c, Character bot) {
        var h = c.leader;
        return h.getWorldServer().getPlayerStorage().getCharacterById(h.getId()) == h
                && h.getClient().getChannel() == bot.getClient().getChannel()
                && h.getParty() != null && bot.getParty() != null && h.getParty().getId() == bot.getParty().getId()
                && h.getParty().getLeaderId() == h.getId()
                && (!inInstance(bot) || bot.getEventInstance().isEventCleared() || h.getEventInstance() == bot.getEventInstance());
    }
    private static void reset(Crew c, EventInstanceManager event) {
        c.event = event; c.books.clear(); c.bookClaims.clear(); c.notices.clear(); Arrays.fill(c.rooms, 0);
        for (var row : c.learned) Arrays.fill(row, -1);
        for (var b : c.bots) {
            var s = RUNS.get(b); s.book = -1; s.room = -1; s.puzzleRow = -1;
            s.pendingUntil = 0; s.finished = false; s.rewardSeen = false; s.fileDelivered = false;
            Arrays.fill(s.known, -1); Arrays.fill(s.attempts, 0);
        }
    }
    private static boolean books(BotEntry e, Character b, State s, boolean ai) {
        var c = s.crew;
        if (c.event.getIntProperty("statusStg1") == 1) {
            int letter = c.base == 926100000 ? 4001131 : 4001130;
            boolean found = c.leader.getItemQuantity(letter, false) > 0;
            for (var member : c.bots) if (member.getItemQuantity(letter, false) > 0) found = true;
            if (found) return advance(e, b, "pt00", ai);
        }
        // Inspect visible books, never the event's hidden book-result table.
        synchronized (c) {
            if (s.book < 0) {
                var nearest = b.getMap().getMapObjects().stream().filter(o -> o instanceof NPC n && (n.getId() == 2112007 || n.getId() == 2112013))
                        .filter(o -> !c.books.contains(o.getObjectId()) && !c.bookClaims.containsKey(o.getObjectId()))
                        .min(Comparator.comparingDouble(o -> b.getPosition().distanceSq(o.getPosition()))).orElse(null);
                if (nearest != null) { s.book = nearest.getObjectId(); c.bookClaims.put(s.book, b); }
            }
        }
        if (s.book >= 0) {
            var book = b.getMap().getMapObject(s.book);
            if (book != null && b.getPosition().distanceSq(book.getPosition()) > 100 * 100) {
                BotZakumPqRun.walkTo(e, b, book.getPosition(), ai); return true;
            }
            BotOpqRun.hold(e, b, ai);
            if (ready(s) && BotNpcInteraction.talkObject(b, s.book)) {
                synchronized (c) { c.books.add(s.book); c.bookClaims.remove(s.book); s.book = -1; }
            }
            return true;
        }
        return c.event.getIntProperty("statusStg1") == 1 ? advance(e, b, "pt00", ai) : BotOpqRun.hold(e, b, ai);
    }
    private static boolean beakers(BotEntry e, Character b, State s, boolean ai) {
        if (s.crew.event.getIntProperty("statusStg3") == 3) return advance(e, b, "pt00", ai);
        if (b == s.crew.collector) {
            var beaker = b.getMap().getAllReactors().stream().filter(r -> r.getId() == 2618000 && r.getState() < 7)
                    .min(Comparator.comparingInt(Reactor::getObjectId)).orElse(null);
            if (beaker != null && b.getItemQuantity(LIQUID, false) > 0
                    && (beaker.getState() > 0 || b.getItemQuantity(LIQUID, false) >= 7))
                return dropAt(e, b, beaker, LIQUID, ai);
            if (collect(e, b, item -> item == LIQUID, ai)) return true;
        }
        return BotOpqRun.grind(e, b, ai);
    }
    private static boolean laboratory(BotEntry e, Character b, State s, boolean ai) {
        var c = s.crew;
        if (c.event.getIntProperty("statusStg4") == 1) return advance(e, b, "pt00", ai);
        int npc = c.base == 926100000 ? 2112006 : 2112005;
        int letter = c.base == 926100000 ? 4001131 : 4001130;
        if (b.getItemQuantity(letter, false) > 0 && c.event.getIntProperty("npcShocked") == 0) return talk(e, b, npc, ai);
        if (b == c.mage && !s.fileDelivered && b.getItemQuantity(RIGHT_FILE, false) == 0 && c.leader.getItemQuantity(RIGHT_FILE, false) == 0)
            return portal(e, b, "in01", ai);
        if (b == c.thief && !s.fileDelivered && b.getItemQuantity(LEFT_FILE, false) == 0 && c.leader.getItemQuantity(LEFT_FILE, false) == 0)
            return portal(e, b, "in00", ai);
        if (b == c.mage || b == c.thief) {
            int file = b == c.mage ? RIGHT_FILE : LEFT_FILE;
            if (b.getItemQuantity(file, false) > 0 && b != c.leader) return deliver(e, b, file, ai);
        }
        if (b == c.collector) {
            for (int side = 0; side < 2; side++) {
                var specialist = side == 0 ? c.thief : c.mage;
                int file = side == 0 ? LEFT_FILE : RIGHT_FILE;
                if (specialist.getMapId() == c.base + 200 && (RUNS.get(specialist).fileDelivered
                        || specialist.getItemQuantity(file, false) > 0 || c.leader.getItemQuantity(file, false) > 0)) continue;
                var door = b.getMap().getReactorByName((c.base == 926100000 ? "rnj" : "jnr") + "3_out" + (side + 1));
                // The card door stays open only two seconds. Spend a key when the
                // specialist is actually waiting to cross, not while searching boxes.
                if (specialist.getMapId() == c.base + 200) {
                    if (specialist.getPosition().distanceSq(door.getPosition()) > 120 * 120) continue;
                } else if (specialist.getMapId() == c.base + 201 + side && specialist.getItemQuantity(file, false) > 0) {
                    var exit = specialist.getMap().getPortal("out00");
                    if (exit == null || specialist.getPosition().distanceSq(exit.getPosition()) > 120 * 120) continue;
                } else continue;
                if (b.getItemQuantity(KEY, false) > 0) return dropAt(e, b, door, KEY, ai);
            }
            if (collect(e, b, item -> item == KEY, ai)) return true;
        }
        if (!c.human && b == c.leader) {
            if (collect(e, b, item -> item == LEFT_FILE || item == RIGHT_FILE, ai)) return true;
            if (b.getItemQuantity(LEFT_FILE, false) > 0 && b.getItemQuantity(RIGHT_FILE, false) > 0) return talk(e, b, npc, ai);
        }
        return b == c.mage || b == c.thief ? BotOpqRun.hold(e, b, ai) : BotOpqRun.grind(e, b, ai);
    }
    private static boolean research(BotEntry e, Character b, State s, boolean ai) {
        int file = b.getMapId() == s.crew.base + 201 ? LEFT_FILE : RIGHT_FILE;
        if (b.getItemQuantity(file, false) > 0) return portal(e, b, "out00", ai);
        if (collect(e, b, item -> item == file, ai)) return true;
        var box = b.getMap().getAllReactors().stream()
                .filter(r -> r.isActive() && r.getName().equals("box") && r.getState() < 4 && r.getId() != 2518000)
                .min(Comparator.comparingDouble(r -> b.getPosition().distanceSq(r.getPosition()))).orElse(null);
        if (box == null) return BotOpqRun.hold(e, b, ai);
        if (Math.abs(b.getPosition().x - box.getPosition().x) <= 70 && Math.abs(b.getPosition().y - box.getPosition().y) <= 45) {
            BotOpqRun.hold(e, b, ai); if (ready(s)) BotZakumPqRun.hitReactor(b, box);
        } else BotZakumPqRun.walkTo(e, b, box.getPosition(), ai);
        return true;
    }
    private static boolean office(BotEntry e, Character b, State s, boolean ai) {
        var c = s.crew;
        if (b.getMap().getReactorByName((c.base == 926100000 ? "rnj" : "jnr") + "6_out").getState() > 0)
            return advance(e, b, "out00", ai);
        if (c.event.getIntProperty("yuleteTalked") == 0) {
            // Talk from outside his 310px detection radius, just as a human can.
            var npc = b.getMap().getNPCById(c.base == 926100000 ? 2112000 : 2112010);
            if (npc != null && b == c.collector) {
                var p = new Point(npc.getPosition().x - 340, npc.getPosition().y);
                if (b.getPosition().distanceSq(p) > 30 * 30) { BotZakumPqRun.walkTo(e, b, p, ai); return true; }
                BotOpqRun.hold(e, b, ai); if (ready(s)) BotNpcInteraction.talk(b, npc.getId()); return true;
            }
            return BotOpqRun.hold(e, b, ai);
        }
        return BotOpqRun.grind(e, b, ai);
    }
    private static boolean chooseRoom(BotEntry e, Character b, State s, boolean ai) {
        synchronized (s.crew) {
            int occupied = s.crew.event.getIntProperty("statusStg5");
            // A human can enter a reserved room before its bot reaches the door.
            // Reservations are ours; actual occupancy belongs to the portal script.
            if (s.room >= 0 && (occupied & 1 << s.room) != 0) {
                s.crew.rooms[s.room] = 0; s.room = -1;
            }
            if (s.room < 0) {
                for (int i = 0; i < 4; i++) if ((occupied & 1 << i) == 0 && s.crew.rooms[i] == 0) {
                    s.room = i; s.crew.rooms[i] = b.getId(); break;
                }
            }
        }
        if (s.room < 0) return BotOpqRun.hold(e, b, ai);
        String marker = "puzzle-entrance-marker-" + s.room;
        if (s.crew.human && !s.crew.notices.contains(marker) && b.getMeso() >= 10) {
            var entrance = b.getMap().getPortal("pt0" + s.room);
            if (entrance == null) return BotOpqRun.hold(e, b, ai);
            var position = b.getPosition(); var target = entrance.getPosition();
            if (e.inAir || e.climbing || Math.abs(position.x - target.x) > 18 || Math.abs(position.y - target.y) > 45) {
                BotZakumPqRun.walkTo(e, b, target, ai);
                return true;
            }
            BotOpqRun.hold(e, b, ai);
            if (!ai || !ready(s)) return true;
            int before = b.getMeso();
            var packet = io.netty.buffer.Unpooled.buffer().writeIntLE(0).writeIntLE(10);
            try { new net.server.channel.handlers.MesoDropHandler().handlePacket(new net.packet.ByteBufInPacket(packet), b.getClient()); }
            finally { packet.release(); }
            if (b.getMeso() != before - 10) return true;
            s.crew.notices.add(marker);
        }
        return portal(e, b, "pt0" + s.room, ai);
    }
    static int rowAt(int y) { return Math.max(0, Math.min(10, (120 - y) / 300)); }
    static int chooseColumn(State s, int row) {
        if (s.known[row] >= 0) return s.known[row];
        int excluded = s.attempts[row];
        synchronized (s.crew) {
            for (int room = 0; room < 4; room++) if (room != s.room && s.crew.learned[room][row] >= 0)
                excluded |= 1 << s.crew.learned[room][row];
        }
        // Each row uses all four columns exactly once across the four rooms.
        for (int col = 0; col < 4; col++) if ((excluded & 1 << col) == 0) return col;
        return 0;
    }
    private static boolean puzzle(BotEntry e, Character b, State s, boolean ai) {
        int row = rowAt(b.getPosition().y);
        if (s.puzzleRow >= 0 && row != s.puzzleRow) {
            if (row > s.puzzleRow) {
                s.known[s.puzzleRow] = s.puzzleColumn;
                synchronized (s.crew) { s.crew.learned[s.room][s.puzzleRow] = s.puzzleColumn; }
                shareClue(s.crew, s.puzzleRow);
            } else s.attempts[s.puzzleRow] |= 1 << s.puzzleColumn;
            s.puzzleRow = -1;
        }
        if (row == 10) return portal(e, b, "out00", ai);
        int col = chooseColumn(s, row);
        s.puzzleRow = row; s.puzzleColumn = col;
        Point before = new Point(b.getPosition());
        long enteredAt = e.followTravelEnteredAtMs;
        int map = b.getMapId();
        boolean handled = portal(e, b, "pt" + row + col, ai);
        // A wrong first-row portal returns to row zero too. Observe the actual
        // portal use and landing, rather than requiring the row number to change.
        if (e.followTravelEnteredAtMs != enteredAt && b.getMapId() == map
                && !before.equals(b.getPosition()) && rowAt(b.getPosition().y) <= row) {
            s.attempts[row] |= 1 << col;
            s.puzzleRow = -1;
        }
        return handled;
    }
    private static void shareClue(Crew c, int row) {
        if (!c.human) return;
        int humanRoom = c.leader.getMapId() - c.base - 301;
        if (humanRoom < 0 || humanRoom >= 4) return;
        int mask = 0;
        synchronized (c) { for (int i = 0; i < 4; i++) if (i != humanRoom && c.learned[i][row] >= 0) mask |= 1 << c.learned[i][row]; }
        if (Integer.bitCount(mask) == 3) {
            int col = Integer.numberOfTrailingZeros((~mask) & 15);
            notice(c, "row-" + row, "MPQ: Your puzzle row " + (row + 1) + " is platform " + (col + 1) + " from the left, based on our confirmed portals. Each row uses all four platform positions exactly once across the four rooms. We share our discoveries to help you find yours.");
        }
    }
    private static boolean battle(BotEntry e, Character b, State s, boolean ai) {
        var c = s.crew;
        if (c.event.getIntProperty("statusStg7") == 0) {
            if (c.human) {
                boolean together = c.leader.getMapId() == c.base + 401;
                for (var member : c.bots) if (member.getMapId() != c.base + 401) together = false;
                if (!together) {
                    notice(c, "boss-wait", "MPQ: We will wait for everyone to finish the puzzle before the boss. Please hold off on Yulete until our ready message.");
                    return b == c.mage ? guard(e, b, ai) : BotOpqRun.hold(e, b, ai);
                }
                notice(c, "boss", "MPQ: Everyone is through. Talk to Yulete to start Frankenroid when ready. Our Priest will guard Romeo/Juliet upstairs.");
                return b == c.mage ? guard(e, b, ai) : BotOpqRun.hold(e, b, ai);
            }
            if (c.bots.stream().allMatch(m -> m.getMapId() == c.base + 401)) return talk(e, b, c.base == 926100000 ? 2112000 : 2112010, ai);
            return b == c.mage ? guard(e, b, ai) : BotOpqRun.hold(e, b, ai);
        }
        return b == c.mage ? guard(e, b, ai) : BotOpqRun.grind(e, b, ai);
    }
    private static boolean guard(BotEntry e, Character b, boolean ai) {
        var threat = b.getMap().getAllMonsters().stream().filter(m -> m.isAlive() && m.getId() == 9300150)
                .min(Comparator.comparingDouble(m -> b.getPosition().distanceSq(m.getPosition()))).orElse(null);
        if (threat == null) { BotZakumPqRun.walkTo(e, b, new Point(-414, -116), ai); return true; }
        var plan = BotCombatManager.planAttack(e, b, threat);
        if (plan != null && BotCombatManager.isTargetInAttackRange(plan, b, threat)) {
            if (e.attackCooldownMs <= 0) BotCombatManager.attackMonster(e, b, plan);
            return BotOpqRun.hold(e, b, ai);
        }
        BotZakumPqRun.walkTo(e, b, threat.getPosition(), ai); return true;
    }
    private static boolean deliver(BotEntry e, Character b, int item, boolean ai) {
        var s = RUNS.get(b); var leader = s.crew.leader;
        if (leader.getMapId() != b.getMapId()) return BotOpqRun.hold(e, b, ai);
        if (b.getPosition().distanceSq(leader.getPosition()) > 30 * 30) { BotZakumPqRun.walkTo(e, b, leader.getPosition(), ai); return true; }
        BotOpqRun.hold(e, b, ai);
        if (ready(s) && BotManager.getInstance().issueDropItem(e, InventoryType.ETC, item, (short)1)) s.fileDelivered = true;
        return true;
    }
    private static boolean collect(BotEntry e, Character b, IntPredicate wanted, boolean ai) {
        var s = RUNS.get(b);
        MapItem item = b.getMap().getDroppedItems().stream().filter(d -> !d.isPickedUp() && wanted.test(d.getItemId())
                        && d.canBePickedBy(b) && !(s.pendingItem == d.getItemId() && System.currentTimeMillis() < s.pendingUntil)
                        && System.currentTimeMillis() - d.getDropTime() >= 400)
                .min(Comparator.comparingDouble(d -> b.getPosition().distanceSq(d.getPosition()))).orElse(null);
        if (item == null) return false;
        BotZakumPqRun.stepTowardsAndPickUp(e, b, item, ai); return true;
    }
    private static boolean dropAt(BotEntry e, Character b, Reactor reactor, int item, boolean ai) {
        if (reactor == null || b.getItemQuantity(item, false) == 0) return BotOpqRun.grind(e, b, ai);
        var s = RUNS.get(b);
        if (!reactor.getArea().contains(b.getPosition()) || e.inAir || e.climbing) {
            BotZakumPqRun.walkTo(e, b, reactor.getPosition(), ai); return true;
        }
        BotOpqRun.hold(e, b, ai);
        if (ready(s) && BotManager.getInstance().issueDropItem(e, InventoryType.ETC, item, (short)1)) {
            s.pendingItem = item; s.pendingUntil = System.currentTimeMillis() + 5400;
        }
        return true;
    }
    private static boolean advance(BotEntry e, Character b, String name, boolean ai) {
        var c = RUNS.get(b).crew;
        if (c.human && c.leader.getMapId() < b.getMapId()) return BotOpqRun.hold(e, b, ai);
        return portal(e, b, name, ai);
    }
    static boolean portal(BotEntry e, Character b, String name, boolean ai) {
        var p = b.getMap().getPortal(name);
        if (p == null) return BotOpqRun.hold(e, b, ai);
        BotTravelManager.walkToPortalAndEnter(e, b, p, System.currentTimeMillis(), ai); return true;
    }
    private static boolean talk(BotEntry e, Character b, int npc, boolean ai) {
        var status = BotTravelManager.tickApproachNpc(e, b, b.getMapId(), npc, 2, ai, false, 140);
        if (status == BotTravelManager.ApproachStatus.ARRIVED) {
            BotOpqRun.hold(e, b, ai);
            if (ready(RUNS.get(b))) { if (b.getClient().getCM() == null) BotNpcInteraction.talk(b, npc); else BotNpcInteraction.next(b, 0); }
        }
        return true;
    }
    private static boolean ready(State s) {
        if (System.currentTimeMillis() < s.nextAction) return false;
        s.nextAction = System.currentTimeMillis() + ThreadLocalRandom.current().nextLong(850, 1150); return true;
    }
    private static void notice(Crew c, String key, String text) {
        synchronized (c) { if (c.human && c.notices.add(key)) c.leader.dropMessage(5, text); }
    }
}
