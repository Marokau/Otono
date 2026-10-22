# Orbis PQ companions

The owner's `MapleStory PQ summaries.docx`, chapter 7, is historical reference,
not executable instructions. Event/NPC/portal/reactor scripts and WZ data remain
authoritative. The guide usefully identifies weekday records, Teleport, room
coordination, numbered portals, seeds and boss ailment curing.

`OrbisPQ.js` admits 5-6 members, levels 51-70, for 45 minutes. Wonky (2013000)
in the Unknown Tower (200080101) recruits; entry is 920010000, hub 920010100,
exit 920011200, and rewards 920011300. Dedicated fixtures are OPqSpear1,
OPqCleric1, OPqMage1, OPqThief1, OPqArcher1 and OPqSword1 (IDs 586-591).
They are separate from managed population and owned bots. Before entry they use
ordinary AP/SP allocation, the shared legal gear optimizer, ammunition,
200 Ginger Ales and 100 All Cures. The actual human's build/inventory is untouched.
`BotPqEquipmentPolicy` restricts provisioned crew weapons to their class/build
types through the shared optimizer's compatibility gate. In particular, mages
use wands/staves even when an all-class snowboard has competitive magic attack.
The policy follows the live character through later auto-equips; it does not
change autonomous population equipment preferences. Verified live: both saved
OPQ mages equip Heart Wand 1372033 instead of Super Snowboard 1442046.

| Room | Server requirement | Statue part / hub reactor |
| --- | --- | --- |
| Entrance | Break 20 clouds; offer 20 item 4001063 to summon Eak; leader checks NPC | Opens hub |
| Walkway 920010200 | Kill Pixies; leader submits 30 item 4001050 (guide says 40) | 4001044 / scar4 |
| Storage 920010300 | Follow actual Celion spawns/death drops and invisible item reactors | 4001045 / scar6 |
| Lobby 920010400 | Offer current weekday record, check Eak, break spawned box | 4001046 / scar1 |
| Sealed 920010500 | Exactly three workers across three areas (guide says five); leader checks | 4001047 / scar3 |
| Lounge 920010600, rooms 601-604 | Collect and submit 40 item 4001052 | 4001048 / scar5 |
| On the Way Up 920010700 | Sixteen random portal rows, then exactly two of five levers | 4001049 / scar2 |
| Garden 920010800 | Seeds, Dark Nependeath, Papa Pixie, Even Stranger Seed, Grass of Life | Restore Minerva at hub |

## Gameplay and movement contracts

`BotOpqRun` is explicit opt-in. It reuses ordinary combat, movement, real drops,
pickups, reactor hits, NPC dialogue and portals. Instances force full simulation;
abstract kills/travel do not run. Hub room links have scripted WZ targets:
`BotOpqPortals` records deterministic script contracts, then the normal
walk-and-enter executor invokes the actual portal script. It does not warp
characters directly or read puzzle answers.

`BotOpqPortalTrial` learns lounge/ascent columns from actual success/failure
arrivals. Within-map portal success resets internal motion normally rather than
being mistaken for `script-no-land`. Ascent scouts prefer the Haste thief;
followers replay learned columns. After a physical fall, grounded height is
compared with the appropriate numbered row/checkpoint and known lower portals
are replayed. Rows 5/9/13 use checkpoint height because the normal arrival sits
below a ladder. Recovery stops after row 16: lever platforms legitimately sit
below the final arrival. Controllers never read `stage6_comb`, `stage6_c` or
`stage4_*` solutions. Sealed and lever candidates use ordinary NPC feedback.

Ascent has MOVEMENTSKILLS field limits, so its live profile is speed100/jump100;
Haste cannot solve that map's geometry. `BotRopeGrabSweep` checks the normal
rope hitbox between 50ms samples, bounded by earlier terrain collisions, in both
build-time simulation and live integration. This fixes the real ladder at x-43,
y[-1623,-878] without enlarging reach. Graph version75 invalidates older graphs
on normal startup. Live work reloaded physics and rebuilt the dedicated test
map under the already-running version74 provider; no population cache sweep or
server restart was required. See the nav ledger's rope-sweep entry and its
real-map graph, live-step/builder and collision-order regressions.

Floating reactors require a reachable attack stance rather than their air
coordinate. Cloud19 (-504,-540) uses its adjacent ledge near (-444,-502).
Cloud17 (201,-443) can require the upper ledge ending at (147,-524), then a
normal down-jump and hit while falling. `BotOpqReactorApproach` selects a legal
stance; ordinary vertical/down jumps and cooldowns perform the hit. Movement is
non-grinding during puzzles so grind-edge margins do not block required ledges.
Normal buffs remain active in empty puzzle rooms. Teleport retains learned
skill, MP and field checks, without an unrelated travel-meso reserve.

Offerings use the reactor's actual activation rectangle, not an arbitrary
35px distance: a legal statue standing floor is 41px below its origin. Real
item offerings wait for normal consumption, with an 8-second pending guard
against re-looting. Celion/Dark Nependeath transparent drops (4001074) are
always left for their invisible reactors. While Papa Pixie (9300039) is alive,
the collector fights instead of planting more ordinary seeds. Actual Even
Stranger Seed 4001054 is planted at the life pot, then grown grass is cut for
Grass of Life 4001055.

Two shared script defects are fixed for players and bots: Minerva's 2006001
reactor now defines its `eim` reference so normal EXP, clear effect and bonus
timer finish; `party3_r6pt.js` floors its existing failure checkpoint before
calling Java's integer warp overload. Neither changes a solution or requirement.
GraalJS regressions reproduce the previous failures and validate the corrections.

## Human-led entry and waiting

Wonky offers **Assemble a bot party for me** and **Dismiss my bot party**, even
when legacy PQ NPC style is enabled. The human becomes leader of five companions:
Spearman, Cleric, I/L Wizard, Assassin and Hunter. The existing saved IDs 586-590
are reused through `BotPqCompanionPool`; the human replaces OPqSword1.
A human leader may retain GM account status; companion eligibility still requires non-GM bots. The normal NPC/event stage requirements remain unchanged. This distinction fixes assembly spawning then dismissing the crew when a level-eligible GM-status human was rejected by the bot-only GM guard.
The explicit `bot_pq_companion` marker excludes population/owned characters.
Startup changeset 34 registers the pool schema and adopts the known OPQ fixtures.
A second crew is created only when an eligible saved fixture is unavailable.
Dedicated PQ logins register under their human leader at runtime, so the ordinary
self-owned idle recovery cannot start independent grinding during assembly.
This runtime association does not add ownership records or recruit population bots.
The launcher copies the build into a unique runtime jar before starting Java:
replacing a running JVM's jar during Maven packaging can break lazy class loading,
interrupt crew setup and strand partially assembled companions. Keep runtime jars
immutable for the life of their process.

`BotOpqHuman` coordinates the human boundary while existing room controllers
solve the stages. `BotOpqRun.isLeader` means the designated bot collector in
this mode; the real party/event leader remains the human. Actual NPC scripts
continue to require that human for checks. Bots wait for the human to enter
rooms and announce the next room/portal and when to return to the Center Tower.
The route is ordered; follow their next-room messages.

Walkway and Lounge counts include pieces the human already carries. The
collector walks beside the human and drops the real remainder. Delivered piles
are excluded from bot pickups. The human collects them and checks Eak. Statue
pieces Eak grants to the human can be brought back and dropped beside the crew:
the collector picks them up and performs the ordinary statue offering.

For Sealed Room, three bots occupy the candidate areas, two park off them and
the human stays off the platforms. Readiness waits for actual placement plus
1.2 seconds. One designated bot observes the broadcast wrong effect; one failed
human check advances one candidate, rather than advancing once per recipient.
Ascent uses the same feedback/readiness protocol for its five lever workers.
The human may stay by Eak below while all five companions climb. Learned lounge
and ascent columns are announced in chat. A human with a lower character ID
cannot become the automated lounge scout or weekday-record courier; those roles
are selected from the bots, preserving any human class.

In the Garden the collector hands actual Grass of Life to the human. The human
picks it up and uses the ordinary return portal, which requires the actual
leader carrying it and returns the team. The human offers it at Minerva's base
and chooses her exit. Bots wait for that choice, then claim their own ordinary
NPC rewards. Optional jail/bonus looting is not automated.

A new event resets handoff/completion/readiness state for the same party.
Lost human presence/leadership/party membership invokes ordinary party departure
and exit dialogue, then saves/disconnects dedicated companions. Dismissal first
checks the entire crew is outside the event back in the recruitment map; it
cannot partially dismiss a ready member while another is still claiming rewards,
or save a fixture inside a reward map. The negative regression covers both
OPQ and LMPQ. Return before dismissing and allow all companions to finish exiting.

## Verification

The original six-bot completion took about 34 minutes including live fixes.
A confirmation run with the same saved six completed in about 20.5 minutes,
including the actual 672,000 HP Papa Pixie, seed/grass sequence, restoration,
and all six NPC rewards. Evidence is in ignored
`target/opq-live-test/rewards-repeat-complete.txt` and `saved-repeat.txt`.

The human-led flow also completed through Wonky's real assembly/entry menu,
all mandatory stages, the leader-only Garden portal, Minerva restoration and
six individual rewards in about 27 minutes. The non-GM stand-in used a regular
Client subclass and ordinary player handlers; it did not fight. The same five
saved companions were recruited. A deliberate human pause exercised waiting;
the Ascent's first lever check failed normally and its second succeeded from
observable feedback. Actual client participation remains the final acceptance
check. Ignored test-only input hooks are restored afterward and are not shipped.
Evidence is in `target/pq-human-proxy/maze.log` and `opq-rewards.txt`:

| Character | Reward item | Quantity |
| --- | --- | --- |
| OPqSword1, human-role stand-in | 4020006 | 15 |
| OPqSpear1 | 2070007 | 1 |
| OPqCleric1 | 4020006 | 15 |
| OPqMage1 | 2040002 | 1 |
| OPqThief1 | 2001001 | 20 |
| OPqArcher1 | 2041019 | 1 |

The final audit (`target/pq-human-proxy/final-saved.txt`) confirms all OPQ
fixtures offline at 200080101, levels 55/56, and all LMPQ fixtures offline in
Ludibrium, level 53. None is GM, managed-population or owned. Test threads ended;
the server and population remained running. The normal `launch.bat` uses the
built `target/Cosmic.jar`, which contains the production classes and migration,
without the temporary test driver. Focused PQ, dialogue, travel/navigation,
combat, loot, LOD, manager and existing LPQ-human regressions pass.

## OPQ cloud movement and visible basic swings (2026-10-20)

Owner observed OPqSpear1 snapping between cloud locations and reactor hits with
no basic attack animation. The missing animation is established: OPQ called the
shared reactor-hit helper directly, which changes reactor state without sending
a character attack broadcast. OPQ now uses BotOpqReactorAttack: equipped-weapon
basic action data, the ordinary close-range attack handler (zero monster targets),
then ReactorHitHandler. Point-blank action selection gives bows/claws their
normal melee swing; cooldown and physical reach checks still apply.

Generic BotManager recovery teleports are now refused inside an OPQ instance.
The exact recovery trigger behind the reported snap was not captured: a short
passive pathlog found the spear idle, not performing another cloud retrieval.
Normal graph movement and learned mage Teleport remain available. No KPQ or LPQ
behavior was changed, and the shared reactor helper remains unchanged.

Verification: 203 focused OPQ/combat/manager tests passed, including swing fields,
ordinary reactor input, cooldown, reach and OPQ recovery refusal. Packaging passed;
helper and method changes were loaded into the active server without restarting.
Client-visible swing and a fresh cloud traversal still require owner confirmation.

### Cloud shortcut projection correction (2026-10-20)

The remaining warp was a separate graph bug, not a learned Teleport cast. Live
np00 is (-468,-1332), but its projected standing point is (-468,-200), matching
the owner's reported warp origin. The builder attached a shortcut to that lower
floor. Version 76 omits intramap portal edges for OPQ entrance 920010000; the
physical rope/platform graph remains. Active crew graphs were rebuilt live and
stale routes cleared. Two profiles report zero portal edges, and a real geometry
regression plus three reactor/recovery tests pass. Skills and KPQ/LPQ are unchanged.
Actual client traversal remains for owner verification.

### Ordinary OPQ headgear (2026-10-20)

Provisioning excludes Zakum helmets 1002357/1002390/1002430 and special MV STR
hat 1002858 for OPq fixtures. Existing raid hats are removed before optimizing.
The live crew uses Silver Crusader Helm, Maple Hats on both pure-INT mages,
Brown Sonata and Green Maro; equipped slots verified and characters saved.
Mage LUK requirements still apply normally; no AP changes to fit an appearance.

### Cooperative ascent scouting (2026-10-20)

Unknown ascent rows no longer belong exclusively to the Haste thief. Workers on
the same tracked row take turns after observed failures; rejected columns and
successful choices are shared as before. Failed workers replay normal checkpoint
routes while teammates can continue testing. Known columns are reused by every
follower. Wrong-choice feedback and learned routes appear in ordinary bot chat.
The ten focused Ascent/script/guidance tests and packaged build pass; loaded live
without resetting the event's existing discoveries.

### On the Way Up meso route markers (2026-10-20)

BotOpqPortalMarkers marks a learned correct column when the next follower reaches
its physical portal. A real MesoDropHandler input spends 10 mesos; no remote drop
or return warp. One pile per row/event; dedicated OPQ provisioning budgets 160
mesos. OPQ bots do not loot those piles, and companion markers survive expiry
while the dropper remains in the active room. Learned-route speech uses the room
name On the Way Up. Existing passed rows are not backfilled during hot loading.

### Record courier presence and late entrance exit (2026-10-20)

The record courier must be a Lobby-present companion; keep today's current record
carrier ahead of a newly arriving lower-ID mage. Otherwise a cleric left at the
entrance can freeze delivery despite another mage beside the dropped record.
Observed live correction: record collected/offered, music state 1, statusStg3 0.
BotOpqPortals also explicitly maps entrance -> Center Tower to its real in00 exit,
so late companions use the ordinary door without generic cross-map route lookup.

### Cleared entrance: use Eak's normal transfer (2026-10-20)

The upper in00 exit sits on an isolated platform; lower ropes cannot reach it.
A straggler at rope x=-519 top=-1005 must talk to spawned Eak. The normal
2013001 script transfers nonleaders to portal 2 even after stage zero is clear.
BotOpqRun now approaches Eak below the exit and BotOpqHuman allows that completed
entrance transfer while retaining all human puzzle checks. Same-map scripted
arrivals reconcile physics so old climbing state cannot pull a bot back.

### Room 303 attached-wall junctions (2026-10-20)

Ground stepping past enclosed floor ends could cross the standing foothold's
attached upward wall chain and fall outside the shaft. A real-map regression
failed before the fix; Room 303 now blocks that chain junction. Scoped to map
920010603, graph version 77; eight focused tests/build pass. Existing off-map
characters are not automatically repositioned, preserving the no-shortcut rule.

### Lounge lift markers and non-expiring earned pieces (2026-10-20)

Lounge lift follows BotOpqPortalMarkers before using a learned portal, just like
On the Way Up. Marker deduplication includes map ID; OPQ preparation budgets 180
mesos. Pink lift-row announcements remain unchanged. Both rooms' 10-meso markers
are left for the human and survive expiry when their dropper enters side rooms
within the same active instance. Lounge pieces 4001052 in maps 920010600-604 are
kept while the event is active with bot participants, including original monster
and box drops and empty rooms. No restoration of already-expired items. Seven
focused tests and package passed; loaded without server restart.

### Client acceptance (2026-10-20)

Owner completed OPQ and explicitly marked it finished and verified after auditing
the stage guidance, physical movement, room recovery, loot, lift markers and
cooperative On the Way Up portal trials. No remaining client acceptance blocker
was reported. Papa Pixie kill EXP still follows the server's ordinary party
eligibility: a non-damaging level-51 observer is below a level-57 attacker's
five-level sharing range, even though eligible to enter the PQ.


### Optional jail rooms

The owner verified the main OPQ route while leaving optional jail-room companion
support unimplemented. On 2026-10-21 they explicitly requested retaining this
omission for later work. The guide's optional rooms need their normal movement,
switch/combat and reward behavior checked before implementation. The retained
item is opq-jail-rooms in [the roadmap](../ROADMAP.md); no gameplay changed here.
