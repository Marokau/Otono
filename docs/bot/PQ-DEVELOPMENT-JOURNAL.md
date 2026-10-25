# Growing the PQ crews

A journal of building party-quest companions together: what I noticed in
MapleStory, what I discovered on the server, and why we changed the bots.

Started October 19, 2026.

Earlier milestones below are backfilled from our conversation; their exact dates are intentionally not reconstructed. 
This is a selected history, not a claim that every bug or client test has been recorded.

The [Otono bot knowledge base](kb/README.md) documents the current implementation, built on NuTNNuT’s bot system and extended through Otono’s development.


## Earlier milestones: from spectators to companions

### Kerning: first prove that a crew can finish

We began with a small eligible test party and stage one, rather than assuming a
whole PQ would work. The owner's reference point was baseline commit `6a18c88ef`.
The constraints shaped everything after that: use normal gameplay paths, leave
the autonomous population alone, and prioritize reliable completion over speed.

The eventual goal was already clear: click the PQ NPC, assemble a prepared crew,
become its leader, and play comfortably with bots that wait when a human needs
to act. The owner completed KPQ with the crew. One early run climbed from level
21 to 28 while gathering coupons, making level eligibility and fixture reuse
practical concerns rather than theoretical ones.

Preparation became part of the experience: appropriate class weapons, assigned
AP/SP, ammunition, and 200 Ginger Ales. The owner chose warrior, magician, archer
and thief roles instead of spending more time on a troublesome pirate fixture.
Existing eligible bots were to be reused, not replaced with a new batch each run.

Technical reference: [Kerning companions](kb/kb_bot_kpq.md).

### Ludibrium: class roles matter

The supplied *MapleStory PQ summaries.docx* helped identify the small mechanics
that distinguish LPQ: magic and physical rooms, Teleport, Dark Sight, numbered
portals, and the box puzzle. Local scripts and map data remained authoritative
when the guide differed from this server.

The owner completed a human-led LPQ. Stage six also revealed a usability gap:
the bots knew their route while the human spent roughly ten minutes solving it
alone. Completing the mechanics and communicating useful knowledge are separate
parts of being a good companion.

Technical reference: [Ludibrium companions](kb/kb_bot_lpq.md).

### Orbis and the Maze: a PQ crew needs its own lifecycle

At Wonky, the owner saw newly assembled OPQ bots disappear or announce that they
were heading off to grind Jr. Cellions. Dedicated PQ companions needed to remain
dedicated instead of inheriting normal population errands. Seeing mages and a
Cleric use snowboards also reinforced that readiness must include suitable gear.

The owner completed OPQ and LMPQ. OPQ's ascent and hidden jail/treasure behavior
were acknowledged limitations the owner was comfortable setting aside at that
point; completion did not mean every optional behavior was polished.

References: [Orbis](kb/kb_bot_opq.md), [Ludibrium Maze](kb/kb_bot_lmpq.md).

### Zakum: watching the client exposed what server progress could hide

The owner recorded the fight when screenshots and server state were not enough.
Pillars appeared to hit bots without stunning them, lightning effects seemed
absent, and flying mobs accumulated while the crew focused on the body.
These observations drove work on headless-client boss effects, combat priorities,
buffs, summons, equipment and supply replenishment.

Long fights made stability just as important as damage. The owner reported client
crashes and clarified that they began before a later summon-packet correction;
that chronology matters, and the packet fix alone must not be presented as proof
of the original crash's cause. The Bishop was also revived with a GM command
after being stunned and pushed into Zakum: that intervention is not evidence of
an unaided clear.

Zakum companion work was committed as `7a3f22765`. Full human-led stability and
survival validation remained distinct from the substantial working behavior.
Reference: [Zakum battle](kb/kb_bot_zakum_battle.md).

### Magatia and Carnival: readiness has different kinds of evidence

Magatia preparation used the reference guide and local mechanics for its rooms,
escort and boss. Carnival required opposing teams and normal earned-CP outcomes,
rather than assigning the human a win. Automated/live fixture runs supplied
evidence, but actual owner-client acceptance was still pending in this chat.

References: [Magatia](kb/kb_bot_magatia.md), [Carnival](kb/kb_bot_carnival.md).

## October 19, 2026: making the crews understandable

### Familiar names, reusable characters

The owner replaced difficult generated names with readable PQ/class names:
`KPqMage1`, `LPqThief1`, `OPqSpear1`, `LMPqArcher1`, `MPqSword1`,
`CPqArcher1`, and `ZakumBishop1`. Existing offline characters were renamed while
preserving their IDs, levels, equipment and builds. Future prefixes were recorded,
with `Horn` chosen for Horntail to fit the 12-character naming limit.

Names became something a player could recognize and remember between runs.
Reference: [crew naming](kb/kb_bot_pq_names.md).

### The bots should explain themselves

The owner described a niece or nephew learning these PQs: a working party is more
useful when a newcomer understands what it is doing. KPQ gained coupon progress,
personal pass handoffs and stage guidance. Unsolicited gear offers to the human
were suppressed for dedicated companions, while supply requests remained useful.

A delayed puzzle line reached the boss room and incorrectly asked the player to
have Cloto check the formation. Guidance was tied to its originating instance and
stage so an old queued message could expire before being spoken out of context.

LPQ stage one then made the same need obvious: everyone rushed upstairs without
explaining the pass collection. The crew gained a short introduction, a pause
before departure, progress and handoff guidance.

The owner settled on two channels: ordinary bot speech and bubbles for actions
and progress; pink/purple PQ notices for important readiness checks and stage
clears. KPQ and LPQ were updated together. The owner had already described the
KPQ stages and guidance as working well in the client; subsequent refinements
still needed their own verification.

### The AFK pass bug

**What the owner saw:** a bot dropped its earned stage-one pass at the human's
feet. After stepping away and returning, the pass was gone.

**What we found:** ordinary ground items expire after three minutes. The bots
deliberately left the pass for the human, and Cloto remembered that each worker
had already earned its pass. Nothing automatically replaced the lost item.
A normal expiry rule could therefore make an otherwise successful stage impossible.

We considered having a bot pick the pass up before expiry and drop it again.
The owner preferred the simpler solution: keep the real earned handoff available.
The scoped expiry rule protects stage-one passes dropped by human-led KPQ bots
while they remain in that active instance map. It creates no new passes and
leaves pickup and Cloto's requirements intact.

**Then the final stage exposed the same risk.** The owner was short one pass
after King Slime. It looked like a bot had picked it up, but inspection showed
the human held nine passes, all three bots held zero, and none remained on the
ground. Expiry was the likely explanation; the exact missing drop was not proven.

The owner requested the same protection for this stage. All final-stage passes
are now protected from expiry while a human-led crew remains there, including
upper-platform drops, King Slime's drop and player redrops. The existing missing
pass was not recreated. Other PQs with ground handoffs can have the same AFK
risk; their behavior was documented rather than silently changed.

**Verification:** the stage-one protection passed seven focused tests with the
KPQ controller. The final-stage extension passed nine. Both builds succeeded and
were loaded without restarting the server. A subsequent AFK client run is separate
evidence and was not yet reported when this entry was written.

**Why it mattered:** a companion should wait comfortably for its human. A hidden
three-minute pickup deadline undermined that goal even though combat and NPC
logic were working correctly.

### The pass hidden by the portal: explain a wait before removing it

The owner saw the crew stay on a platform instead of descending to King Slime.
Live inspection found a living boss below and a pass above. The first response
removed the dropped-pass gate so workers would seek the boss.

The owner then spotted the pass near the portal and clarified the desired behavior:
waiting was good; unexplained waiting was the problem. The combat change was
reverted. If the human heads into the boss section while upper passes remain,
one bot now explains that the crew is waiting for those passes and mentions that
they can be hard to see near portals. Collecting the pass lets the crew continue.

The restored behavior and explanation passed 11 focused tests and built successfully;
the update was loaded without a restart. No movement physics or attack range was
changed. This was a useful correction to the design, not merely a failed fix:
the owner's observation identified the experience the bots should preserve.

## Keeping this journal useful

Add an entry after a meaningful discovery or player-tested milestone: what was
seen, what the evidence established, why a fix was chosen, and how it was verified.
Record uncertainty and owner interventions honestly. Use exact dates when known;
do not invent dates for backfilled stories. Keep current technical rules in the KB
and unfinished work in [ROADMAP.md](ROADMAP.md), rather than turning this journal
into a task list. Git history preserves revisions to the journal itself.

### Boss gear belongs to the human

The owner pointed out that bots can pick up boss equipment much faster than a
human can inspect it. Even a successful boss kill feels wrong if the companions
vacuum up the reward. The requested experience was for them to leave the loot
and say that it belongs to the player.

The shared loot eligibility path now reserves original boss drops for human-led
dedicated crews across PQs. Required stage items retain their existing collector
rules. Deliberately picking up and redropping unwanted gear makes it available to
bots, subject to normal pickup rules. Simply leaving an original drop untouched
is not treated as permission: the human may still be deciding.

One bot can announce that the boss loot is the player's and suggest redropping
unwanted gear. This is ordinary companion speech, not a pink stage-clear notice.
The change does not grant gear, alter boss rewards or make equipment drops
permanent. Population bots and bot-only crews retain their existing behavior.
Technical reference: [PQ boss loot](kb/kb_bot_pq_boss_loot.md).

Verification: 20 focused tests passed across boss-loot policy, shared loot eligibility, KPQ, LPQ and pass expiry; a follow-up assertion confirmed human-redropped gear passes the full eligibility gate. The package build succeeded and the update was loaded without restarting. Actual human-client boss reward pickup after this change remains to be observed.

### KPQ bonus room: describe the room that actually exists

The owner caught the crew suggesting breaking boxes in the KPQ bonus room. Local map 103000805 contains monster spawns and no reactors, so the box instruction was incorrect. The line now invites the player to hunt monsters for extra EXP and speak to Nella when ready to leave. This is a dialogue correction; bonus-room gameplay is unchanged. Verification used the local map data and a package build; no additional test was added for a text-only change.

After another owner-run KPQ, the owner saw bots still collect mesos and slime-related loot and asked whether Squishy Shoes had dropped. The dashboard recorded the boss-loot announcement. Inspection found no original King Slime drops remaining; Sword held two Squishy Shoes and Archer one, but without a before-run inventory snapshot or drop history those items cannot be attributed to this run. Remaining observed ground drops came from ordinary Slimes (not flagged as bosses). The reservation is for original boss drops; ordinary monster loot remains eligible. This observation confirms the announcement appeared, not that a particular equipment drop was protected or that a bot stole one.

### LPQ exposed lingering sibling trades

The owner saw bots trying to trade with one another in LPQ and remembered the
same behavior in KPQ. Invitations ended in trade-request timeouts. The earlier
PQ social rule suppressed offers to humans but explicitly left sibling offers
available, and supply-sharing transfers had their own initiation path.

Dedicated PQ companions now suppress gear offers to all recipients, do not start
trade sequences or market trade behavior, discard queued retries and decline
incoming peer-bot trades. Active bot-initiated sequences use ordinary cancellation
and inventory restoration. Human-initiated trades remain available so the owner
can still supply potions and other items. This is scoped to dedicated fixtures,
not a shutdown of the existing autonomous population's trading system.

Verification: 69 focused tests passed, including a dedicated regression that rejects a peer-bot trade while still accepting a human-initiated trade. The build succeeded and the update was loaded without a server restart.

### Every stage needs context, not only the first one

During LPQ the owner noticed that stage two again began without the crew
explaining its work. KPQ had established the desired experience: a newcomer
should understand the objective, the bots' roles and the human's next action.
The owner asked for that standard across future PQ work and offered to audit the
instructions while playing.

LPQ gained an introduction for every main stage after stage one, including the
trap-box stage, monster boxes, magic/physical rooms, skill vaults, numbered maze,
ranged triggers, box arrangement and Alishar. Bonus and reward rooms also have
their own explanations. Existing overlapping role announcements were consolidated.
The crew briefly waits for the introduction before leaving, and messages reset
for a new instance rather than repeating every tick. Clear-flag validation now
expires outdated speech across all nine LPQ stages.

The general guidance convention was saved in CLAUDE.md for future PQ work:
ordinary speech explains actions/progress; pink notices identify important
readiness checks and clears. This entry does not claim all older PQs have already
received a complete dialogue audit.

Verification: nine focused tests passed, including coverage of all ten later-stage/bonus/reward introductions, once-per-instance behavior, the brief wait and suppression in cleared stages and room submaps. The package build succeeded and the guide was loaded without a restart. Owner-client auditing of each stage remains separate from these automated checks.

The owner preferred a capital letter when a complete sentence follows a stage label. LPQ stage two now starts "Stage two: We will break boxes..." and that style is recorded in CLAUDE.md for future dialogue. This is a text-only correction, verified by a package build; it does not change stage behavior.

### LPQ room returns should look like a player walking back

The owner saw bots appear to warp to a stage-four subroom exit immediately after
collecting its pass and rejected that shortcut. The room controller already
requests normal travel to the exit portal, but shared movement recovery could
teleport directly to its active goal if its recovery conditions were met.

Forced recovery teleportation is now disabled in the five active stage-four
rooms, covering both distance recovery and calls from the frozen-air watchdog.
Normal navigation, exit portals and legitimate learned movement skills remain
available. No bot position was manually changed. The suspected recovery trigger
was identified in code, not captured during the owner's room visit; the exact
trigger and visible return behavior still need a subsequent client observation.

Verification: 48 focused recovery, LPQ-controller and portal-travel tests passed. The build succeeded and BotManager was updated live without restarting. The client-visible return walk remains for the owner to audit on another room visit.

The owner clarified the full dialogue style after seeing "Stage one: defeat the Ratz": both the stage label and following sentence should be capitalized, as in "Stage One: Defeat the Ratz...". All nine LPQ stage introductions now follow that format, and the standing instruction was updated. This corrects the earlier narrow change that capitalized only the sentence in stage two.

### Stage six: offer the familiar route without spoiling discovery

The owner initially wondered whether the crew should discover the numbered boxes
by trial and error, then remembered that LPQ stage six always has the same order.
The concern shifted to sharing knowledge: the bots knew the route, while the
human could spend time solving it alone.

We chose an opt-in hint rather than a timer or automatic spoiler. The introduction
says the route is fixed and invites the human to say "box order" when ready.
Only the designated collector answers, directly in the human's chat log with its
name, so distance and an offscreen speech bubble do not hide the answer. The
request also works if the collector already entered stage seven, provided both
are still in the same LPQ instance and the human remains in stage six. The player
can ask again to repeat the order.

The sequence is derived from this server's actual portal destinations rather than
an assumed remembered guide. A local map-data regression checks all 15 choices.
The hint does not teleport the human or bypass any portal.

Verification: 42 focused hint, stage-guide and chat tests passed. The fixed route was checked against local WZ portals, and the cross-stage request path was tested with the human in stage six and collector in stage seven. Build and live reload succeeded without restarting; the owner will test the actual client request.

The owner then requested a gentle follow-up rather than leaving a newcomer to
struggle indefinitely. If the human remains in stage six for one minute without
asking for the order, the collector now offers the hint again directly in the
chat log. This happens once per instance, works while the bot waits in stage
seven, and never reveals the answer automatically. Asking for the order or
leaving stage six suppresses the reminder.

Verification: seven focused reminder, fixed-route hint and LPQ-controller tests passed. The tests check the one-minute boundary, one reminder only, no reminder after requesting the hint, and suppression after leaving the maze. Build and live reload succeeded without a server restart.

### The box hint knew the portals, but read the wrong numbers

The owner tested the Stage Six hint and corrected its answer to
**133 221 333 123 111**. The first implementation had followed the correct portal
connections but reported each portal's index within a row. Those positions are
not the numbers printed on the boxes: the map places different numbered artwork
on different rows. Its original test validated the same mistaken assumption.

The hint now shares the fixed player-facing sequence confirmed by the owner.
The regression was replaced with a map-data check that follows each correct
portal and matches its coordinates to the visible box-label decorations. This
checks the number a human actually sees rather than repeating the algorithm's
internal indexing. The one-minute opt-in reminder and ordinary portal traversal
are unchanged.

Verification: four fixed-hint/reminder tests passed, including the corrected visible-label map-data regression. Build and live reload succeeded without restarting. The owner can request "box order" again to verify the corrected client answer.

### Stage seven: the missing pass was a missing summon

The owner reported three related symptoms: a premature "boss loot is yours"
message during the Rombot wave, items disappearing from the high Ratz platforms,
and a crew that would not deliver passes.

Live inspection separated them. Spear held two real passes and one Ratz trigger
item (4001156). The other party members held neither. Two summon reactors had
fired; the middle reactor was still untouched, and no monsters or ground passes
remained. The crew was waiting for a third pass that had never been earned,
because the middle Rombard had never been summoned.

The high-platform drops are intended to disappear: the normal item-reactor path
consumes them after five seconds to summon Rombards. Bots had no exclusion for
those triggers and could pick one up instead. The exact pickup position was not
recorded, so the report does not establish an item vacuum or a particular warp.
The new eligibility rule leaves trigger items alone for the normal reactor;
the introduction now explains this to the human too.

The generic boss announcement was also suppressed for this stage. Rombard has
the server's boss flag, but the owner correctly distinguished the summon wave
from the actual Alishar boss encounter.

No missing pass was granted and no reactor was forced. Route probes found no
legal path for the crew to return its stolen trigger to the isolated middle
platform, so a fresh PQ attempt was needed to retest normal completion. This
incident was not another proven ground-expiry failure.

Verification: 13 focused trigger-loot, shared loot, LPQ-controller and boss-loot tests passed. The trigger exclusion covers both nearby eligibility and distant targeting, and a regression checks that stage seven queues no boss-loot announcement. Builds and live reloads succeeded without a server restart. A fresh owner-led run remains the full three-summon/handoff acceptance test.

### Box arrangements are the bots speaking

The owner preferred LPQ Stage Eight's "Box arrangement ... is ready" announcement
in general chat instead of pink server notices. It describes the bots' own
positioning, so it now uses normal speech/bubbles through the stage-scoped chat
queue, once per arrangement. Stage-clear notices and other existing stage-item
handoff notices remain pink. This refines the earlier readiness-notice convention
for this specific puzzle; it does not change the arrangement or NPC check rules.

Verification: seven LPQ-controller and stage-chat tests passed, including a regression for one general-chat readiness message per arrangement while clear notices remain pink. Package build and live reload succeeded without a restart.

### Alishar's effects were hard to see because the bots cured them

The owner noticed being afflicted while the bots appeared unaffected. Inspection
found Spear had 96 All Cures left, Cleric and Mage 98 each, from their prepared
100. No active disease remained at that sample. The controller consumes an actual
carried All Cure on the next tick when a curable disease is present, making its
visible duration brief.

Alishar's skill data includes Seal and Darkness. This evidence confirms curable
effects reached at least those three bots, but does not identify each cured
disease or prove that Archer and Thief were hit; both still had 100 cures. No
status immunity was assumed and no gameplay code was changed for this finding.

### LPQ bonus boxes: the crew helps, the human gets the loot

The owner asked whether Alishar only dropped the key and wanted assurance that
bots would leave boss gear and LPQ bonus items alone. The live boss-specific drop
tables showed Alishar's Key of Dimension and monster card, with no equipment
entry; King Slime's table listed Squishy Shoes, its card and the pass. NPC scripts
confirmed that Arturo awards LPQ's separate event reward after bonus, while Cloto
awards KPQ's separate event reward before its bonus room.

Boss drops already had human-first reservation with stage-item exceptions. The
new bonus rule reserves every LPQ bonus-room drop for the human, including mesos,
consumables, equipment and redrops. Bots still help open boxes through ordinary
reactor hits. The room introduction now makes that division explicit. This does
not change reward tables or give the player the bots' individual NPC rewards.

Verification: 14 focused bonus-loot, boss-loot, shared eligibility and LPQ-controller tests passed, including continued bonus-box hits while pickup is blocked. Build and live reload succeeded without restarting. Drop facts came from the live database and reward facts from the local NPC scripts; actual owner-client bonus pickup remains a separate acceptance check.

## October 20, 2026: Carnival belongs in the guide too

The owner confirmed KPQ and LPQ were done and moved toward Carnival testing, then
noticed CPQ was missing from the !wherelevel party-quest list. The list had been
curated around a small set of familiar PQs rather than all available content.

Carnival 1 and Carnival 2 were added with their local level ranges, per-team
player counts, summaries and confirmed entrance teleports. Carnival 1 points to
Spiegelmann's dedicated bot-team menu; Carnival 2 explicitly says bot assembly is
not yet implemented there. Existing GM-only teleport permissions were preserved.

The owner also discovered Ellin Forest PQ. A local entry-script inventory
confirmed Ellin, Amoria, Guild PQ and Crimsonwood Keep among the additional
content, and separated usable entry paths from dormant code such as Nett's
Pyramid, Chaos expedition enum entries and the unreferenced Easy Balrog script.
The full reference is [the content catalog](PQ-CONTENT-CATALOG.md). Script
availability is not being presented as a completed client or bot run.

Verification: both guide tests passed across all ten PQ entries, checking menu
labels, summaries, entrance maps, confirmation/cancellation and the existing
non-GM travel restriction. Catalog file links were checked. NPC scripts load on
reopening the dialogue, so no server restart or Java rebuild was needed.

### A complete activity guide, with expeditions on their own page

The owner wanted Carnival PQ's title shortened to fit the menu and wanted every
available PQ listed as a future companion goal. The guide now calls it "Carnival
PQ" and uses a compact per-team count. The PQ/challenge page has 26 entries,
including Ariant, Amoria, Guild PQ, CWKPQ, MV's Lair, Horntail's trial, Cafe variants,
Holiday brackets, Boss Rush, Rescue Gaga, Dojo and the quest-linked skill trials.

A separate Expeditions & Bosses main-menu page contains Zakum, Horntail, Pink Bean,
Balrog, Scarlion/Targa, Showa, Papulatus and Captain Latanica. Summaries distinguish
NPC registration from portal entry and state whether a dedicated bot crew is
implemented. Map entrances and NPC placement were checked against local scripts
and map data; no unknown NPC was invented for portal-only entrances. Unavailable
Pyramid and entry-less Chaos/Easy Balrog leftovers remain excluded.

The owner can browse future activities without being told that all those bot
crews already exist. Teleports still require confirmation and GM access and lead
to recruitment, never directly into a boss instance. Existing chapters and class
guides remain available through the main menu.

Verification: three guide tests passed, exercising all 26 PQ/challenge summaries and eight expedition summaries, their confirmed entrance destinations, expedition back navigation, cancellation, bot-support labels and non-GM restrictions. The scripts update on reopening the NPC dialogue; no server restart or Java build was required. Client line wrapping remains for the owner to visually audit.

### Credit the foundation when sharing the fork

The owner wants any future public release to clearly credit Nutnnut's bots as
the foundation this fork builds on. The PQ companion work should be described as
extensions and fixes to that existing bot system, not an independently created
bot framework. Preserve the repository's upstream credits and licenses too.

The owner also wants the history to acknowledge improvements outside PQ runs,
such as fixing the autonomous bots' Zakum prequest loop so they could move on to
Temple of Time grinding. This is an owner-identified example to substantiate with
its technical notes and Git history when writing the eventual release description.
No GitHub setup, publishing or release preparation was performed for this note.

### Ellin Forest: reference guide and dedicated crew implementation

The owner confirmed KPQ, CPQ and LPQ are done and requested Ellin Forest next.
The shared Word guide does cover it: Chapter 20, Forest of Poison Fog. The local
server accepts level 44-55 Explorers (the guide says 45-55), with four to six
participants and a 30-minute timer. Local scripts and WZ data take precedence.

Added a reusable prepared Ellin crew and the recruitment NPC's BotPQ Menu,
keeping ordinary entry, details and prize exchange above the bot options. The
controller uses real poison drops and reactor consumption, actual random maze
portals, player-handler captures, ordinary box hits and the three real boss
forms. Humans retain turn-ins, the altar summon, original boss loot and reward
choices. Stage introductions, progress, AFK-safe marbles/stone handoffs and
readiness notices follow the KPQ/LPQ conventions. No population bots are borrowed.

An isolated four-bot level-45 crew entered through the recruiting NPC and cleared
the 50-tree first stage. The first poison test diluted real drops and reached
three of four barrier steps, but the collector subsequently fell below the map.
A newly introduced reactor target used the item area's geometric center, which
can lie below the floor. Replaced that invalid movement goal with a real floor
point inside the item area and added real-map regressions for pond and altar.
The exact falling transition was not recorded on that attempt, so this correction
is not proof of the entire fall's cause; a repeat run records collector movement.
The failed run was aborted by ordinary party departure, not repaired by warping
or advancing its stage. The same fixtures returned normally and were reused.

Initial verification: compilation passed; 211 focused tests passed across combat,
attack data, manager, Ellin capture/loot rules, recruitment scripts and handoff
policy. Additional real-map approach and existing LPQ recovery regressions passed.
Live completion and human-client acceptance are not established by these tests.

The repeated crew cleared all four poison-barrier steps and entered the maze.
Its collector movement audit stayed within the map (recorded Y range -56 to
150), with ordinary jumps over the obstruction and no recovery teleport. Maze
success initially left bots trying another portal: the arrival point is roughly
400 pixels from Ellin, outside the new controller's 220-pixel recognition test.
Navigation probing confirmed three legal jumps to her. The controller now
recognizes the successful compartment it actually reached, then climbs to Ellin;
it still learns destinations by using the real random portals. A regression
covers this exact arrival. The crew subsequently used Ellin's team transition,
obtained real purifiers and began successful player-handler captures.

The isolated repeat run completed through normal rewards: all 50 trees, all four
poison inputs, a real random maze success, 20 captures and Ellin turn-in, the
physical jump climb and box search, Sprite transition, altar stone consumption,
all three Poison Golem forms, boss-marble pickup, exit-fountain activation and
normal return to Deep Fairy Forest. At the final audit all four fixtures were
level 45, alive, outside the event, and each held one Altaire Fragment (4001198).
Remaining Ginger Ales were 161/192/166/181; all still had 100 All Cures. This does
not prove client-side boss status presentation; no human client was in that run.

The original Spearman, Cleric and Assassin were saved and dismissed. A pool audit
returned the same IDs 623, 624 and 625, rather than creating a second crew. Exit-map
saves are also eligible for reuse: those companions use the ordinary exit portal
before entering again. The guide now marks Ellin's crew as implemented.

Human-wait, handoff protection, boss-marble ownership, ordinary exit after event
disposal, NPC assembly/menu placement and guide navigation have automated coverage.
A brief additional human-role packet rehearsal used only the auxiliary Archer
fixture, but the temporary driver did not keep that character under its movement
control and did not reach the recruiting NPC. It was cleaned up; no actual player
account was involved. Therefore live human-led NPC entry and client presentation
remain for the owner to test, not claimed as verified by that rehearsal.

The implementation was loaded without restarting the server, and the final jar
was packaged for subsequent launches. Existing autonomous population entries were
not borrowed or commanded. No GM quest completion, stage-property writes, direct
monster kills, puzzle-item grants or position-recovery teleports were used in the
successful run.

Assembly also now holds every new companion explicitly idle while provisioning
and forming the party, publishes all controller states, and only then resumes
any bot. This closes a preparation race where an unregistered fixture could take
an ordinary autonomous tick before its dedicated controller existed. A regression
verifies the complete party is registered before the first resume. Human acceptance
remains pending; this safeguard is not described as a client-confirmed incident.

### Consistent introductions before the remaining PQ crews begin work

The owner asked for the LPQ-style explanations across every other implemented
PQ, explicitly excluding the already verified KPQ and LPQ. Inspection found
useful Orbis notices, but several were pink instructions delivered after work
had started; Magatia, Maze and Carnival lacked a consistent stage-start pause.
Ellin already had the requested introduction/pause behavior and was left alone.

Added a shared guide for Orbis, both Magatia variants, Maze rooms, Carnival and
Zakum. Each introduction explains the objective, bot roles and human action in
ordinary speech, then holds tasks for seven seconds plus queued-speech time.
Introductions deduplicate per stage/instance, wait for the human to arrive and
reuse the existing stale-chat expiry. Existing OPQ/MPQ introductory notices move
into those speech introductions; important readiness and handoff notices remain
private. KPQ/LPQ controllers, dialogue and mechanics were not edited.

Maze bots now wait at a new work room for their human. Magatia main stages also
wait, while research specialists and the four independent portal puzzles remain
free to work in parallel. This exception matters: requiring every bot to share
the human's puzzle room would prevent the crew from producing the shared clues.
A regression exercises a bot solving room two while the human solves room one.
Carnival opponents pause during the initial briefing but only a teammate speaks;
repeat matches reset the guide. Zakum pauses only before summoning; active arm
and body reminders never pause attacks, healing or resurrection.

Verification: focused guide and existing Orbis, Magatia, Maze, Carnival and Zakum
tests passed, including speech coverage, ASCII/capitalization, shared pause and
queue timing, deduplication, human arrival, Carnival roles, repeat reset and
parallel Magatia puzzles. The existing Maze coupon-handoff test mocks the new
reading pause so it continues testing handoff behavior independently. The code
was loaded without restarting; the owner was in Orbis's Center Tower and the
crew was waiting in the Walkway, so its next introduction occurs upon joining
them. This is not a claim of client acceptance or a full new run of each PQ.

Final verification completed: all 41 focused tests passed across the seven guide/
PQ test classes. The packaged build passed and the final guide/parallel-room
refinements were loaded into the running server without a restart.

### 2026-10-20 - OPQ clouds: legal movement and visible weapon swings

The owner saw OPqSpear1 warp while retrieving clouds and saw clouds being hit
without weapon animations. Confirmed animation cause: OPQ invoked reactor damage
directly, bypassing the character attack broadcast. Added an OPQ-only basic swing
through existing weapon-action data and player close-range/reactor handlers;
physical reach and weapon cooldown gate each hit. Bows/claws use their legitimate
point-blank melee action. Other PQ reactor helpers were not changed.

Disabled generic recovery position snaps while inside OPQ. This closes an allowed
illegal movement path, although the exact recovery trigger in the reported case
was not captured. A passive spear pathlog showed waiting rather than another
retrieval. Learned mage Teleport and normal navigation remain intact. KPQ and LPQ
were left untouched.

Verification: all 203 focused tests passed (OPQ run, new reactor/recovery checks,
combat, attack data and manager); packaged successfully and loaded without server
restart. Runtime remained active after loading. Client animation appearance and
fresh cloud traversal are pending the owner's next observation, not claimed as
verified. Evidence: target/opq-cloud-fix-tests.log and
 target/opq-cloud-fix-package.log.

### 2026-10-20 - OPQ portal directions in player language

Replaced numeric Center Tower portal coordinates with visible location wording:
Walkway top left; Storage upper right; Lobby bottom right; Sealed Room upper
left below Walkway; Lounge upper middle below the very top; On the Way Up very
top middle. Verified the portal names/positions against the Center Tower map
and the existing room mapping. Owner will audit the phrasing in their client.
KPQ and LPQ remain unchanged. Build/package verification passed; loaded live.

### 2026-10-20 - OPQ cloud warp traced to a phantom portal entrance

Owner clarified the remaining mage warp was not a Teleport cast and stood on its
origin. Read-only live inspection placed NelBot at (-467,-200). The instance's
np00 shortcut actually sits at (-468,-1332), but findGroundPoint projects that
floating portal onto (-468,-200). The graph builder therefore authored a portal
edge from a floor 1,132 pixels below the real portal, and the executor trusted the
edge's projected start. This is an established geometry error; a recorded warp
was not captured after the owner report.

OPQ entrance graphs now omit same-map portal edges so the crew must navigate
cloud platforms using physical routes and ropes. Graph version 76 invalidates
persisted versions of the false link. Only map 920010000 changes edge policy;
KPQ and LPQ behavior is unchanged. The tentative movement-skill restriction was
removed before deployment after the owner's clarification: learned skills remain
unchanged. No global movement executor workaround was added.

Verification: the real cloud-map geometry regression confirms climbing edges
remain and portal edges are absent; all three existing OPQ reactor/recovery tests
also pass. Loaded without restarting and rebuilt only the active OPQ cloud crew's
two movement profiles (125/100 and 140/120); both report zero portal edges. Cleared
those bots' stale routes without changing their positions or stage state. Client
confirmation of a fresh rope traversal remains pending. Evidence:
target/opq-cloud-fix-live/inspect.txt, portal-reload.txt,
target/opq-cloud-portal-test.log. Packaged for the next normal launch.

### 2026-10-20 - OPQ hats should match ordinary adventurers

Owner confirmed corrected cloud movement, then flagged the level-50 Zakum helmets
as visually inappropriate for this crew. OPQ provisioning now excludes/removes
all three Zakum helmet variants and the special MV STR hat (which the unrestricted
optimizer otherwise chose as an alternative). Normal population, KPQ and LPQ
provisioning are unchanged.

Reused the current five fixtures and verified their actual equipped slots:
OPqSpear1 Silver Crusader Helm (1002028); OPqCleric1 and OPqMage1 Maple Hat
(1002510); OPqThief1 Brown Sonata (1002210); OPqArcher1 Green Maro (1002213).
The pure-INT mages cannot meet regular mage hats' LUK requirements after removing
the Zakum stats, so use the wearable common Maple Hat rather than changing AP.
A trial apprentice hat was rejected by normal equipment requirements and was
replaced before final verification. Current hats were equipped with normal
inventory handlers, appearance refreshed, and all five characters saved.

Verification: packaged successfully; provisioning rule loaded without restart;
live equipped-slot audit saved at target/opq-cloud-fix-live/hat-audit.txt. No
combat or movement code changed for this request.

### 2026-10-20 - OPQ technical notices and puzzle chat channels

Owner saw a navigation-cache warmup message during OPQ and requested consistency
with the accepted LPQ arrangement messages. The nav cache version change made
rooms warm their routes again; notifyWarmup exposed that implementation status
through dropMessage(5). Suppressed that technical notice inside OPQ instances;
cache building and physical fallback behavior are unchanged.

LPQ's existing arrangement announcements already use ordinary bot chat. OPQ's
puzzle readiness keys now also queue ordinary bot speech, covering the Sealed
Room and Ascent arrangements. Other readiness/clear notices retain their existing
pink channel. No KPQ or LPQ source or behavior changed for this request.

Verification: packaged successfully and both method changes loaded into the active
server without restart. Existing per-attempt deduplication remains; an arrangement
already announced before loading is not repeated retroactively.

### 2026-10-20 - OPQ ascent portal trials shared by the crew

Owner observed four companions waiting for the thief to discover every ascent
row and suggested taking turns. Confirmed cause: unknown rows explicitly chose
one Haste scout and held everyone else. Removed the fixed scout. On an unknown
row, workers currently assigned to that row and in the same map rotate trials
based on its observed failures. A failed worker replays the learned lower route
while another present worker can try the next untested column. Success remains
shared by the event, so teammates follow the observed correct column. A lone
worker can still advance if the rest are catching up; no artificial all-party
barrier, warps, hidden-answer reads or stage shortcuts were added.

Failed-column feedback and learned routes use ordinary bot speech. The stage
introduction now explains taking turns and sharing columns. Existing checkpoint
replay and lever logic remain intact. KPQ and LPQ were not changed.

Verification: 10 focused tests passed across Ascent, the ordinary portal script
and stage guidance. The new regression observes three different workers trying
columns one/two/three, two actual failure verdicts, a success, and a fourth worker
following column three without retesting rejected columns. The pre-existing
checkpoint test now exercises a general worker rather than a privileged thief;
an unstubbed mock trial outcome was corrected to APPROACHING. Final package
passed and body changes loaded without restart, preserving current event state.
Dashboard confirmed OPqThief1 active in On the Way Up afterward. Client teamwork
appearance is for the owner's ongoing audit, not claimed as a fresh full run.
Evidence: target/opq-ascent-team-package.log.

### 2026-10-20 - Ten-meso markers for On the Way Up

Owner requested the familiar 10-meso piles on working portal columns and room
names rather than internal Ascent terminology. Ascent discovery messages now say
On the Way Up. After an actual successful trial, the next teammate approaching
that known column physically walks beside the portal and drops ten mesos via
MesoDropHandler before entering. The successful tester is already upstairs, so
we never move it back artificially or spawn money remotely. One shared marker
per observed row avoids every follower dropping another pile. Existing learned
state is preserved; already-passed rows are not marked retroactively.

OPQ provisioning supplies a 160-meso marking purse (16 rows), also topped up on
the current five fixtures when loading. Drops debit real bot mesos. Bots ignore
player-dropped 10-meso piles in this room; companion markers stay through normal
expiry while their dropper remains in the active room/instance, allowing a human
to climb later. Human pickup remains allowed. No PQ requirements, answers or
movement positions are bypassed. KPQ/LPQ branches retain their existing behavior.

Verification: nine focused tests passed across markers, ascent and the ordinary
portal script. Marker tests execute the real player meso-drop handler, assert the
10-meso debit and single shared drop, require physical approach, and check expiry/
bot-loot protection. Packaged successfully; new helper and body changes loaded
without restart. Client-visible placement awaits the next known-portal traversal.
Evidence: target/opq-portal-markers-test.log.

### 2026-10-20 - Lobby record courier must be present

Owner reported OPqSpear1 standing still instead of retrieving records. Live state
showed today's record 4001058 already dropped from the broken weekday reactor;
Spear had no remaining matching reactor to hit. The global courier selection had
chosen OPqCleric1 (lowest-ID Teleport bot), still at the cleared entrance, while
OPqMage1 stood beside the record. The room therefore had no active courier.

Courier selection now considers companions in the Lobby and retains a companion
already carrying today's record when a lower-ID mage arrives later. After live
loading, OPqMage1 retrieved/offered the real record: music reactor advanced from
state zero to one, statusStg3 changed from -1 to 0. The human's ordinary Eak
check remains required. No item/stage grants or movement bypasses were used.

The lagging cleric's task was alive and ticking (no pending future failure), but
generic travel was producing no movement toward the entrance exit. Added the
existing in00 entrance-to-Center-Tower door to OPQ's explicit portal mapping,
using walkToPortalAndEnter like the other room links. This is the real visible
exit, not the earlier rejected intramap cloud shortcut. The precise generic
travel no-route cause was not established. Initial live observation switched the
cleric from idle to moving; arrival remains to be checked.

Verification: eight focused tests passed (record selection/carrier retention,
portal mappings, OPQ run); packaged successfully and loaded without restart.
Evidence: target/opq-record-courier-test.log and
 target/opq-cloud-fix-live/record-inspect.txt. KPQ/LPQ unchanged.

### 2026-10-20 - Entrance straggler needs Eak, not an impossible climb

Owner could not continue the Sealed Room because OPqCleric1 remained at entrance
(-519,-1004), at the top of rope [-1005,-617]. Its scheduled task was healthy,
portal in00 open, no attack lock. Exact-profile graph (110/100) showed the upper
exit platform isolated from all lower regions, including that rope: no physical
route exists. scripts/npc/2013001.js explicitly transfers nonleaders from this
entrance to portal 2. The cleared-stage bot logic incorrectly tried ordinary
travel directly to the isolated upper exit and the human-leader NPC gate also
prevented a companion using Eak's non-stage-check transfer.

Cleared-entrance bots still below the exit now physically approach and talk to
Eak, following his existing normal NPC script; only this completed-entrance
transfer is exempted from the human NPC-check rule. Other stage checks remain
human-led. Same-map NPC transfers reset bot physics at the actual scripted
arrival, and the cleared entrance reconciles external scripted position changes
before old rope physics can rewind them. The earlier team-warp rewind is a
supported mechanism, but was not recorded at the initial clear itself.

Verification: eleven focused entrance-recovery, OPQ run/portal/record tests passed;
packaged and loaded without restart. Immediately after loading the cleric left
the stalled rope using normal movement; full room arrival is being observed.
No GM teleport, answer writes, stage bypass or population changes. KPQ/LPQ unchanged.
Evidence: target/opq-entrance-recovery-test.log,
target/opq-cloud-fix-live/cleric-graph.txt and scripts/npc/2013001.js.

Live verification completed: OPqCleric1 used Eak's normal transfer, entered Center
Tower, climbed through it, entered Sealed Room 920010500 and resumed moving toward
its assigned puzzle platform. No manual movement/warp was applied. Diagnostic
recording stopped; logs/bot-nav/pathlog-OPqCleric1-2026-10-20T155111.txt.

### 2026-10-20 - Lounge lift directions use pink messages

Owner requested OPQ: Lounge lift row directions in pink server text. Added that
specific route-key exception to OPQ notice routing. On the Way Up discoveries and
puzzle arrangements retain ordinary bot speech; no movement or puzzle logic changes.

Verification: packaged successfully and loaded live without restarting. Previously announced rows are not replayed.

### 2026-10-20 - Room 303 spear outside playable geometry

Owner reported Spear not collecting rocks in Room 303. Live state: Spear at
(338,41568), airborne and far below map bounds [-436,-2325,774,2102], holding
20 lounge pieces. All monsters dead; ten pieces remained on floors -1046/-1444/
-1845. No pickup action can bridge that distance. Earlier generic recovery warps
are disabled for OPQ, so this invalid state cannot silently teleport back.

Reproduced a relevant collision defect against the actual Room 303 map: stepping
right off a floor into its attached upward wall returned nonblocked, treating an
enclosed wall junction as a ledge. The regression failed before the correction.
The exact initial escape was not captured, so this reproduction establishes a
real escape mechanism rather than proving every movement preceding this incident.
Room 303 ground stepping now checks the standing foothold's linked upward wall
junction before walking past its end. This is chain geometry, not an all-map wall
scan or runtime trajectory prediction. Other maps retain existing behavior.
Graph version 77; eight focused tests pass, including both boundaries at all five
room floors, existing wall/ledge physics tests and OPQ run regressions. Packaged
and loaded without restart; rebuilt four active profiles for Room 303 only.

Spear was not repositioned. Salvaging this run requires either an owner-approved
one-time recovery to the ordinary room entrance or a fresh PQ attempt. Pending
owner choice because direct recovery is an exception to their normal-movement
constraint. Stage state and inventory remain intact. Evidence:
target/opq-303-wall-before.log, opq-303-wall-after.log,
target/opq-cloud-fix-live/room303.txt and room303-reload.txt.

### 2026-10-20 - Lounge lift markers and earned-rock expiry protection

Owner chose to restart the PQ after Room 303 rocks expired; no one-time Spear
reposition was authorized or performed. Added the same physical ten-meso marking
input to learned Lounge lift portals. Marker identity now includes map and row,
so Lounge row one and On the Way Up row one do not suppress each other's piles.
Dedicated OPQ preparation budgets 180 mesos for the eighteen rows. Lift route
announcements remain pink as requested earlier.

Lounge statue pieces (4001052), including original monster/box drops in rooms
301-304 and human handoffs in the main Lounge, are exempt from expiry while the
instance is active and contains an OPQ bot participant. This also protects an
empty side room while everyone temporarily leaves it. No expired items are
regenerated and no counts or stage requirements are bypassed. Meso markers in
both puzzle rooms are protected from bot looting and remain through expiry while
their dropper remains in the active instance, even after moving to a side room.
Human pickup is unchanged. KPQ/LPQ behavior is unchanged.

Verification: all seven focused marker, Lounge drop/lift and room tests passed;
packaged successfully and loaded live without restart. Tests cover independent
map/row markers, real ten-meso spending, approach before dropping, active empty-
room rock retention, release after event clear, and marker retention after their
bot leaves the main Lounge. A nested Mockito stub in the new test was corrected
before the final successful run. Client placement awaits the fresh run.
Evidence: target/opq-lounge-markers-expiry-test.log.

### 2026-10-20 - Quiet On the Way Up meso marking

Owner verified Lounge lift meso markers in the client, then requested removing
On the Way Up's "I marked the working portal" chatter. Suppressed only that
confirmation in map 920010700. Real meso drops and other route/trial feedback
remain; Lounge confirmation behavior is unchanged.

Verification: packaged successfully; helper method loaded live without restart.

### 2026-10-20 - Quiet failed portal trials

Owner also requested suppressing On the Way Up's failed-row/column chatter.
Removed that speech only; rejected columns, turn rotation and learned route
messages remain unchanged.

Verification: packaged successfully and loaded live without restarting.

### 2026-10-20 - Owner verifies OPQ; Papa Pixie EXP explanation

Owner completed the full client audit and declared OPQ finished and verified,
including enjoyable On the Way Up teamwork/markers and verified Lounge markers.
Recorded this acceptance; no boss EXP or eligibility rules changed.

Read-only EXP inspection: NelBot level 51; Spear/Cleric/Mage/Thief level 57 and
Archer level 58. Runtime USE_ENFORCE_MOB_LEVEL_RANGE=true and both EXP split
intervals=5. Papa Pixie 9300039 is level 65, base EXP 17000. Monster's party EXP
logic builds eligibility from monster level +/-5 and every damaging member level
+/-5. Thus a non-damaging level-51 observer receives no kill EXP from this crew:
lowest contributing level 57 implies minimum passive recipient level 52. A
successful damaging hit would put the player into the contributor interval and
make them eligible. The death's actual damage ledger was not captured, so lack
of a successful hit is an inference from the reported result, not a logged fact.
Stage/completion script EXP is separate. Evidence:
target/opq-cloud-fix-live/exp-inspect.txt, Monster.java party distribution,
wz/Mob.wz/9300039.img.xml. No gameplay change requested or applied.

### 2026-10-20 - Grass of Life placement wording

Owner read "base of Minerva's statue" as the pedestal flare. Changed that human
handoff instruction to "Drop your Grass of Life at Minerva's statue's body."
Only wording changes; actual placement and NPC behavior are unchanged.

Verification: packaged successfully and loaded live without restarting.

### 2026-10-20 - Restored Minerva bonus-room reminder

Owner almost missed the bonus room after restoring Minerva. Updated the existing
restoration notice to invite the player to check the bonus room below first,
then talk to Minerva when ready to leave and claim rewards. The crew explicitly
says it will wait. No exit or bonus-room gameplay behavior changes.

Verification: packaged successfully and loaded live without restart.

### 2026-10-20 - Ellin poison provenance and stale inventory cleanup

Owner saw Spear drop poison quickly and asked whether it was earned. The stage
machine collects concentrate 4001161 from monster drops, offers it to pond
3002000 for diluted poison 4001162, then feeds the barrier four diluted poisons.
Neither provisioning nor the bot stage controller grants poison. NPC 2133001's
stage explanation confirms the poison-bearing monster; the pond's normal reactor
drop produces the diluted version.

Found a carryover gap: EllinPQ's exclusive-item list contains diluted poison but
omits concentrate. Reusable companions could retain surplus concentrate from an
earlier attempt. No before-entry inventory snapshot exists for the observed pile,
so its exact provenance cannot be established. Dedicated EPq provisioning now
removes leftover concentrate and diluted poison outside the event, covering both
assembly and re-entry refresh. It does not remove current-run earned items or
alter human inventories, rewards, reactor delays or the required four inputs.

Verification: packaged successfully and loaded live without restart. No provisioning was invoked on the active crew; current-run inventory was preserved.

### 2026-10-20 - Ellin capture-stage introduction wording

Owner requested the Stage Four explanation distinguish Purification/Purifier
marbles used to capture weakened monsters from the 20 Monster marbles handed to
Ellin. Replaced the introduction with the owner's supplied wording. Capture
threshold, consumables, handoffs and NPC checks are unchanged.

Verification: packaged successfully and loaded live without restart. An introduction already spoken is not repeated.

### 2026-10-20 - Ellin purple stone handoff reclaimed by collector

Owner confirmed Spear reached the human, then repeatedly dropped and reclaimed
the purple stone faster than the human could pick it up. Live inspection showed
Spear within 25 pixels holding stone 4001163, human holding none. Stage Five's
collector could take the item through both passive loot eligibility and the
explicit collect helper; the handoff lacked reservation against either path.

Human-led Ellin player-dropped purple stones are now reserved against passive
bot looting, and explicit collection skips these handoffs while still accepting
the original box drop. Once a stone lies on the ground, the bots announce pickup
and wait instead of continuing to break boxes or reclaiming it. Existing expiry
protection remains. No items were granted or manually transferred.

Verification: focused Ellin tests and package passed, including a new regression
covering both loot paths and preserving original-drop collection. Initial live
redefinition failed because a changed lambda capture order changed its synthetic
method signature; reordered the equivalent predicate, reran verification, then
loaded successfully without restarting. Current run preserved. KPQ/LPQ unchanged.
Evidence: target/ellin-stone-handoff-test.log and
 target/opq-cloud-fix-live/ellin-stone.txt.

### 2026-10-20 - Three-second PQ All Cure reaction time

Owner saw ailments vanish too quickly in Ellin, OPQ and possibly Alishar and
explicitly requested a three-second cure delay. Shared BotLpqCures now waits via
BotPqCureDelay before invoking the existing UseItemHandler with a carried All
Cure. Per-character/per-disease first-seen times are weakly held; every active
curable ailment gets at least three seconds, including a new ailment arriving
while another is pending. Repeated ticks do not restart its timer. Resolved
ailments clear their clocks so later episodes receive a fresh delay.

Applies to shared-cure callers: LPQ (including human controller), OPQ, Ellin,
LMPQ, Magatia and Zakum. This specific LPQ behavior change is now explicitly
requested despite its earlier accepted status. No KPQ stage code changes.
Healing/potions and normal actions continue while the delay runs; stun is not
made All-Cure-curable, and no ailments are artificially applied or extended.

Verification: fifteen tests passed across the delay, existing carried-potion
handler and Ellin run tests. Exact 2999/3000ms boundary, overlapping ailments,
episode reset and noncurable stun are covered without sleeping. Packaged and
loaded the helper/cure method live without server restart. Actual client effect
visibility awaits the owner's next ailment. Evidence: target/pq-cure-delay-test.log.

### 2026-10-20 - Ellin falling-rock recording review (unfixed coverage gap)

Owner supplied the supplied Ellin Forest recording, referring to a
20-second hit. This file is 12.77 seconds; its visible rock wave occurs around
8-9 seconds. Extracted frames show rock impacts overlapping Spear's position
while he continues attacking. Visual overlap should enter normal collision,
accuracy and invulnerability checks; the recording alone cannot prove that a
particular impact must deduct HP, because misses, invulnerability and potion
recovery can obscure damage.

Code inspection establishes BotMobAttackObserver.observe accepts only Zakum
IDs 8800000-8800010 and rejects Poison Golem forms 9300180-9300182. Thus their
client-side area attack is not covered by this observer. Final-form attack3 is
type 3, PADamage 290, attackAfter 2640, areaCount 23; its WZ info has no disease
or disease level, so do not invent a stun. Existing server-cast ailments still
apply independently. No attack behavior change in this review; area-damage
support remains a known Ellin audit blocker. Evidence:
target/ellin-video-review/rocks-overview.png and rock-wave.png.


### 2026-10-20 - Ellin falling rocks enter the headless damage path

Owner requested implementation after repeated visible rock overlaps caused no
bot reaction. Two independent Zakum-only gates excluded Poison Golem: BotClient
packet forwarding and BotMobAttackObserver admission/queue processing. Both now
accept active Ellin instances and Golem forms 9300180-9300182. Existing WZ-driven
collision delay, hit bounds, accuracy/defense rolls, hit invulnerability and the
real TakeDamageHandler now apply to supported Golem area attacks. Final-form
attack3 uses PADamage 290 and a 2640 ms delay; damage is calculated per bot,
not hardcoded to the owner's observed 57. No stun was added: this rock attack
has no WZ disease. Ground-only jumpAttack tremors remain excluded.

The observer preserves all four attack-option bytes and can select all 23 Golem
columns instead of truncating at 16. This extends the existing column-mask model;
upper-bit interpretation is covered by packet-layout tests but still needs a
live Golem capture/client comparison. Zakum retains its existing 16-column limit.
Fourteen focused tests passed, including Golem WZ geometry/delay, upper option
bits, BotClient forwarding, actual TakeDamageHandler HP loss without invented
stun, and existing Zakum regressions. Package succeeded; observer and BotClient
were redefined in the running server without restart. Live inspection found the
human and crew already back at the Ellin entrance (300030100), so no subsequent
live rock hit or client confirmation is claimed. The next boss run is the visual
verification step.


### 2026-10-20 - Ellin poison-cloud damage audit finding (not yet fixed)

Owner reports taking 200 poison damage approximately once per second while bots
appear unaffected. Golem forms 9300181/9300182 use AREA_POISON (131), level 5;
its WZ x value is 200. Initial form 9300180 uses level 8, x 50. MobSkill creates
a monster Mist and MapleMap broadcasts it, but spawnMist schedules no player
damage for this branch. BotMobAttackObserver handles announced mob attacks,
not Mist objects, and no bot-side monster-mist damage handling was found.
This establishes a separate missing headless-client path, not an All Cure timing
issue. The owner's one-second cadence and exact client damage packet type have
not yet been captured; no poison-cloud behavior change or live verification is
claimed in this investigation.


### 2026-10-20 - Ellin poison-cloud contact damage implemented

Owner authorized fixing the cloud immediately for the current client audit.
BotPoisonMistDamage now checks the bot's existing touch bounds against active
monster Mist rectangles in an Ellin event. It reads the exact skill and level
from each cloud's normal spawn packet and uses MobSkillFactory's WZ x damage:
50 for first-form level 8, 200 for later level 5. A cloud left after a boss phase
change retains its own skill level. Contact is checked on the existing attack
observer tick, including when no falling-rock attacks are queued.

Damage uses the normal TakeDamageHandler mist packet (-4), including normal
Magic Guard and bound autopotions. No poison disease or All Cure delay is added:
the danger is standing in a cloud. Overlapping clouds share one contact clock
and use the largest active damage, rather than multiplying by cloud count.
The one-second interval follows the owner's observation; exact client cadence,
first-contact timing and overlap behavior remain assumptions for this client
audit rather than reverse-engineered guarantees. Leaving the cloud, expiration,
death or exiting the event stops further contact damage. Ordinary bot population
and shared human gameplay handlers are unchanged.

Four new tests cover real cloud packets/WZ levels, one-second contact timing,
non-stacking overlap, leaving/removal, inactive/dead exclusions, and the real
player handler's 200 damage with Magic Guard (40 HP, 160 MP). All 18 focused
mist/observer tests passed and package succeeded. Appended the new helper and
redefined the observer in PID 16944 without restart. Live inspection found the
human and all three bots in 930000600, but no active cloud at that snapshot;
subsequent visible client confirmation is still pending.


### 2026-10-20 - Owner verifies Ellin Forest PQ

Owner confirmed falling rocks work, then waited for the final-form Golem to
cast poison again. Read-only live inspection showed poison available with
438 MP and a 15-second cooldown; no cast was forced. After the next natural
cast, owner confirmed poison-cloud damage works and declared Ellin PQ verified.
This is client confirmation of the human-led run and both recently added damage
paths, complementing the 18 passing focused tests. It does not establish exact
packet-level cloud cadence or every upper-column mask interpretation.


### 2026-10-20 - Clarify Ellin fountain bonus wording

Owner saw the fountain sparkle but expected a visible item reward. Reactor
3008000.js grants 52,000 base EXP to players present when activated; exit portal
party6_stage800.js grants one Altaire Fragment (4001198) only if the fountain's
state is greater than zero and the character has ETC space. NPC 2133000 exchanges
10 fragments for Altaire Earrings, then 10 plus the prior earrings for each of
the Glittering and Brilliant upgrades. Thus fragments are conditional on fountain
activation, not unconditional clear rewards. At the owner's request, changed
Spear's fountain explanation from "extra reward" to "bonus EXP". Reward mechanics
are unchanged; verification is by reading the reactor, portal and NPC scripts.


### 2026-10-20 - LMPQ guide teleport lands on the entrance platform

Owner requested the wherelevel LMPQ teleport land where NelBot was standing.
Read-only live inspection established map 220000000, position (315,-705), on the
platform near LMPQ NPC 9103001 at (76,-712). The guide previously used portal 0
for every destination. Added an optional landing coordinate to the LMPQ row and
a party-guide confirmation path using the normal Character.changeMap(map,Point)
overload when coordinates are present. Other destinations retain portal 0;
existing GM eligibility and confirmation remain in place. No character was moved
during implementation, and no restart is needed for this NPC script change.
Verified all three existing guide-menu tests, covering PQ and expedition destinations, exact LMPQ landing coordinates, cancellation and non-GM travel restrictions. All passed.


### 2026-10-20 - LMPQ visual route markers and a human escort

Owner requested 10-meso markers at recommended portals to supplement the room
messages, and noted the Tauro-room rush strategy. The existing room-6 coupon
strategy remains. The human crew's collector now escorts the human whenever
the human is still en route to the current objective. Other workers retain
normal objective travel. The guide reaches the human via ordinary maze portals,
walks onto the next route portal, and uses MesoDropHandler to drop real 10 mesos.
It waits there for the human rather than passing through immediately. When the
human changes rooms, it normally retrieves its old marker before following;
misplaced old markers are also normally retrieved before replacement. At the
objective it resumes its existing collection/handoff work.

Passive bot loot excludes 10-meso player drops in LMPQ, while explicit guide
recovery uses normal pickup. Markers remain protected from expiry while their
bot dropper remains in that active maze room. Provisioning supplies a minimum
160 mesos to LMPq fixtures; the already-assembled four idle companions were
checked for NelBot ownership, recruit map and no active event, then supplied
the same budget. No human inventory or autonomous population was changed.

Twelve focused tests passed and package succeeded: physical approach, real
MesoDropHandler debit/spawn, no duplicate marker while waiting, old-marker
recovery, unrelated worker exclusions, existing maze travel/coupon handoffs and
drop-policy regressions. Loaded guide helper and controller/loot/drop/provisioner
changes into PID 16944 without restart. Live inspection found the crew waiting
outside; actual client marker confirmation awaits the next entry.


### 2026-10-20 - Restore original LMPQ routing after owner review

After discussing LMPQ's normal random starting rooms, the owner preferred the
original room/portal notices and independent optional box fights. Removed the
new collector escort, 10-meso marker behavior, marker-only loot/expiry exceptions,
and marker provisioning budget. Deleted its now-unused helper and tests. The
normal event spawn assignments were never changed. Existing Tauro-room routing,
human readiness waits, room directions, coupon delivery and rewards remain.
The 160 mesos already provisioned to the four fixtures were left untouched;
this restoration does not remove earned or carried currency. The earlier marker
entry is retained as history of the experiment and why it was abandoned.
Eight focused maze/drop-policy tests and package passed. Restored controller, loot policy, expiry policy and provisioning live without restarting the server.


### 2026-10-20 - LMPQ accepted; replace implausible raid helmets

Owner verified LMPQ gameplay, with the remaining cosmetic/progression objection
that level-50-range companions wore Zakum helmets. Applied the existing OPQ
provisioning restriction to LMPq fixtures: remove Zakum hats 1002357/1002390/1002430
and special MV STR hat 1002858 from equipment inventories, exclude them from
candidate grants, then use the shared gear optimizer. OPQ's 180-meso portal-marker
budget remains OPQ-only. No AP/SP changes or custom hat stat boosts were made.

Package passed and provisioner was redefined without restart. The active-run
ownership guard initially prevented refresh because the completed crew was no
longer registered to that run; no equipment was changed by that attempt. Confirmed
the known dedicated fixture IDs 594-597, recruit map and no event, then refreshed
and saved all four through the normal provisioner. Equipped head slots verified:
Spear Blue Bamboo Hat (1002090), Cleric Maple Hat (1002510), Thief Maple Hat
(1002510), Archer Green Maro (1002213). Verified no excluded hats remained in their
equipped or equipment inventories. Existing autonomous population was untouched.


### 2026-10-20 - Four-player Carnival teams and Carnival 2 bot entry

Owner requested four players per side for both Carnival 1 and Carnival 2,
rather than the previous two-player Carnival 1 teams. Carnival 2 already had
ordinary server gameplay but lacked dedicated companion recruitment/lifecycle
support. Both menus now assemble the human plus Cleric, Spearman and Assassin,
against those three classes plus a Hunter. The same isolated pools reuse saved
CPq/CPq2 fixtures; CPQ1 starts new fixtures at 40 and CPQ2 at 55, with normal
AP/SP, class gear/ammunition and ordinary headgear restrictions for CPQ2.

BotCarnivalFormat supplies the distinct lobby maps, eligibility levels and NPCs.
Start uses a free larger CPQ1 field (index 4/5) or CPQ2 field (index 2), the real
bracket-specific lobby/challenge APIs, and the existing opponent acceptance
handler. CPQ2 results/exit use NPC 2042007, not CPQ1's 2042002. Normal carried
potion restrictions, team target eligibility, CP spending, scores and timers
remain. CPQ2's lobby also replaces a Java-collection numeric comparison with
an explicit legal 2..6 party-size check. The wherelevel guide advertises both
four-versus-four crews, and both NPC menus retain normal field selection above
BotPQ Menu.

Twenty-one focused tests passed (including both actual script menus and mocked
assembly/challenge routing) and package succeeded. Loaded the helper and changed
classes without restarting. Initial live channel-2 tests admitted eight dedicated
bots into each bracket through normal NPC challenges and earned CP on both teams;
no manual score, timer, spawn or reward changes were used. Full timed outcome
and human-facing entry verification are recorded below when completed.


Live follow-up: both normal ten-minute four-versus-four matches completed. All
16 bots reached their bracket's winner/loser result NPC, claimed grade-A rewards
and returned alive to their proper recruit maps with no active Carnival. CPQ2
winners advanced from level 55 to 56; carried Ginger Ales remained 200 on every
participant. Ignored evidence: target/opq-cloud-fix-live/carnival-four-results.txt.
The added bracket-specific reward-NPC/lobby-reset regression also passed, bringing
the focused suite to 22 tests. No match timer or score was manually altered.


Human-facing follow-up used the same saved fixtures with one regular Client
subclass as a non-GM human-role stand-in. Actual NPCTalk/NPCMoreTalk inputs opened
each bracket's BotPQ Menu, assembled three allies plus four opponents, and kept
the stand-in as leader. Normal challenges admitted all eight to CPQ2 980033100
and CPQ1 980000501. The test driver initially retried an NPC click too quickly;
waiting for the normal interaction delay let Start proceed, with no gameplay
code change. Normal party-leave cancellation moved each crew through its real
exit map/NPC and back to the lobby. No temporary production tick hooks were
installed. Actual human client UI/feel remains the owner's audit step.
Both regular-client stand-ins and all companions were saved and dismissed after verified lobby return. No test match or temporary proxy client remains active.


### 2026-10-21 - Carnival party departure now dismisses both teams

Owner observed CPQ1 companions remaining online after leaving their party, so
later assembly allocated Thief3/Thief4 instead of reusing the prior fixtures.
BotCarnivalRun's cleanup checked only whether the human disconnected. It did
not recognize an online human losing/leaving the original allied party.

The shared CPQ1/CPQ2 controller now validates human presence, channel, party
leadership and party identity against the original first allied companion.
Once detached, bots skip human-dependent stage briefings, complete the ordinary
exit NPC path, and save/disconnect in their bracket lobby. This also prevents
a no-party human's exit-room briefing from holding bots indefinitely. Valid
parties still retain their crew for another match. Assembly now refuses a second
crew while an earlier one for that character is active, including its return
window. Pool level eligibility and existing saved characters are unchanged;
no higher-numbered fixtures were deleted or characters de-leveled.

Twenty-two focused controller/team/menu tests and package passed, covering both
sides/brackets, online party departure, party replacement, valid repeat crews,
exit-room guidance bypass and duplicate recruitment prevention. Loaded both
classes into PID 5644 without restart. Live CPQ1 regular-client stand-in stayed
online, assembled seven companions, and left through normal Party.leaveParty;
all seven became unregistered/offline and reassembly reused exactly IDs
615/639/640/616/617/641/642. Saved and dismissed the reused crew and stand-in.
CPQ2 follow-up verification is recorded below. No autonomous population or
navigation cache changes were made.
CPQ2 live follow-up also passed: all seven companions logged off while the regular-client stand-in stayed online, and reassembly reused IDs 632-638 exactly. All test fixtures were saved/dismissed. Evidence: target/opq-cloud-fix-live/carnival-leave-cpq1-pass.txt and carnival-leave-cpq2-pass.txt.


### 2026-10-21 - Expose the CPQ2 bot menu to a real client

Owner reached The 2nd Monster Carnival lobby but Spiegelmann lacked the BotPQ
Menu despite the implemented server dialogue. Read-only live inspection confirmed
map 980030000 and NPC 2042005, matching scripts/npc/2042005.js. WZ NPC info also
contains the built-in client script mc2_enter; config NPCS_SCRIPTABLE omitted
2042005. Thus the ordinary server-script menu existed, but the actual client had
a native dialogue path the previous NPCTalk/headless checks did not exercise.

Added 2042005 ("Carnival 2 and BotPQ Menu") to the existing scriptable-NPC override
configuration. Updated the running configuration map and sent NelBot the complete
normal PacketCreator.setNPCScriptable list, the same packet used at login, without
moving the player, spawning bots or restarting. Future logins load the persisted
configuration. The send was verified on the player's current 980030000 client;
actual visible menu confirmation remains the owner's next check. No Java source
change or JAR rebuild was needed for this configuration-only fix.


### 2026-10-21 - Owner verifies both Carnivals; clarify field-size labels

Owner verified CPQ1 and CPQ2 in the actual client, including the CPQ2 menu after
the scriptable-NPC override. The opposing CPQ2 crew won decisively. Playing a
Beginner without class gear/skills instead of the opposing Hunter is a plausible
contributor; no balance cause was measured and no scoring or gear balance change
was made.

Owner then questioned four-versus-four legality because normal field menus said
2x2 or 3x3. Local entry scripts check minimum two for small fields and three for
large fields; normal parties support up to six. These labels were misleading
minimum-size shorthand, not enforced exact-team-size limits. Updated both
NPCConversationManager field lists to say "2-6 per team" and "3-6 per team".
Four-player dedicated crews and ordinary entry/challenge/scoring rules remain.
This is a label-only Java change; package/live-load verification follows below.
Package passed; NPCConversationManager redefined live in PID 5644 without restart. Source inspection confirms both CPQ1 and CPQ2 field-list labels changed; no entry size, timer or score logic changed.


Quick-fix reference note (2026-10-21): owner selected
https://global.hidden-street.net/party-quest/monster-carnival as the reference for
future CPQ1/CPQ2 audits and accepted the field-menu clarification. The 2x2/3x3 to
2-6/3-6-per-team change is recorded above as a menu-label bug fix, without changing
four-player crews or gameplay rules. The linked site returned HTTP 403 in this
check; no claim that its text was read or independently confirms CPQ2 rules is made.


Reference correction (2026-10-21): owner clarified that the first Hidden Street
page is for CPQ1 only. CPQ2 has its own reference:
https://global.hidden-street.net/party-quest/monster-carnival-2.
The knowledge base now maps each bracket to its corresponding page. No gameplay
or menu changes were made for this reference correction.


### 2026-10-21 - Correct small-field maxima from owner-supplied references

Owner supplied the actual guide excerpts: CPQ1 fields 1-4 permit 2-4 per team,
fields 5-6 permit 3-6; CPQ2 fields 1-2 permit 2-4, field 3 permits 3-6. The previous
2-6 small-field label overgeneralized the local generic party cap and minimum
entry checks; those checks were not evidence of the historical field-specific
maximum. Corrected both small-field list labels to 2-4 per team and retained
3-6 for large fields. Four-player crews fit either range. Existing bot selection
already uses CPQ1 field 5 or 6, and CPQ2 field 3, so no route or team-size change
was needed. Earlier journal entries remain as the history of this correction;
the knowledge base now records the supplied ranges rather than the inference.
Package passed and corrected menu labels were loaded live into PID 5644 without restart. Close/reopen the field list to display the new ranges.


### 2026-10-21 - Alternate Carnival field-selection route (deferred finding)

Owner tried Spiegelmann > Assemble bot teams > Normal Carnival field selection
> Field 1, rather than Start bot Carnival match. Owner observed entering the
field while the opposing bot crew never entered. Exact bracket and waiting-lobby
versus battlefield state were not captured; no root cause or independent live
reproduction is claimed. The supported start option coordinates opposing-party
reservation and challenge acceptance, so the alternate path warrants a separate
entry-flow audit. Owner requested retaining it for a future bug master sheet;
added deferred roadmap item carnival-normal-entry and a corresponding KB finding.
No gameplay changes made. Completed-fix history stays in this journal; unfinished
work stays in ROADMAP.md so a future master view can draw from both.

## 2026-10-24 - GPQ: a guild of our own, then Gatekeeper

**Goal:** I wanted to choose GPQ without repeatedly organizing a guild or recruiting strangers. The answer is a persistent human-led guild with five temporary dedicated companions, rather than bypassing guild requirements.

**Implemented:** Shuang explains the standard fee and six founders, then offers confirmed travel to Heracle. I choose the name and approve the normal creation process. The crew accepts normal confirmations, returns to camp, joins my guild through invitations and assembles a party. Dismissal removes the bot memberships and logs them off while my guild remains. Reassembly restocks and reuses eligible fixtures. A level-25 scout reserves the later low-level role; its stage and sacrifice are still pending.

**Live discovery:** The first headless crew earned earrings and solved all three visible statue sequences, then four members died entering the hallway. The ordinary gear optimizer had swapped their mandatory Protector Rock earrings for stronger stat earrings. That hallway applies 30,000 HP drain without the required item. The fix preserves the earned earring in GPQ during both optimizer selection and application, without weakening the map hazard.

**Verified:** Normal guild creation and fee, five invitations, real three-minute gathering, ordinary rock attacks/pickups, four/five/six-flash Gatekeeper rounds and normal NPC clear. After the failure, the normal death response ended the run. Dismissal left the master alone in the guild; the same five companions rejoined without another creation fee. A repeat run cleared Gatekeeper and all six survived the hallway wearing their earned earrings, including after a forced normal optimizer pass. The autonomous population was left alone.

**Regression and cleanup:** 113 focused tests passed and the server package built successfully. Disconnecting the isolated test leader through normal client cleanup ended the repeat run; all five companions withdrew and logged off, leaving the master in the persistent guild. The server and its autonomous population remain running.

**Regression and cleanup:** 113 focused tests passed and the server package built successfully. Disconnecting the isolated test leader through normal client cleanup ended the repeat run; all five companions withdrew and logged off, leaving the master in the persistent guild. The server and its autonomous population remain running.

**Still pending:** The rest of GPQ, including the low-level route, sacrifice, Ergoth and rewards. This is a verified first-stage milestone, not a completed or human-audited GPQ. MVPQ has not been started. Publication waits for a reviewed, ready update.

## 2026-10-24 - GPQ: spear routes and visible offering clues

**Live findings:** A jump-course worker aimed at a nearby decorative ledge and looped at a rope top instead of taking the real jumps to the spear rock. The GPQ reactor caller now targets the rock's supporting floor when it lies within ordinary attack reach; floating targets still use the existing ledge approach. The worker then reached and collected the spear through normal movement. The four altar assignments also reserve the specialist tower for the mage rather than asking the low-level scout to cross an unreachable gap.

The thief repeatedly dropped and reclaimed the metal-door key before the ordinary five-second item-reactor timer consumed it. Shared GPQ item-offering actions now wait through that timer before resuming pickup or travel. Dedicated passive loot cannot reclaim stage offerings, and earned GPQ items remain available during an active run instead of expiring during a pause.

**Offering puzzle:** The solver considers all 256 arrangements, filters them using the same correct/misplaced/unknown counts shown by the Guardian Statue and chooses the next test from those public clues. It does not inspect the hidden combination. Exhaustive decision-tree testing covers all combinations within the seven-check limit. Ordinary drops at the four vassals and the normal leader NPC check still determine success.

**Status at this point:** Clothing routes, the normal earring sacrifice, Ergoth and timed bonus behavior have been implemented but still require live verification. One spear attempt ended before completion; its cause has not yet been established. The repeat records actual death and map transitions for diagnosis. This entry is not a full-clear claim.

## 2026-10-24 - GPQ: the guarded spear belongs to the thief

**Established cause:** Transition sampling captured the mage dying in the Dark Muscle Golem corridor with both HP and MP depleted. The mandatory earring was still equipped: this was real golem contact, not the earlier map-protection bug. The normal death response subsequently ended the failed run.

**Correction:** The inner spear room is reachable with ordinary movement in this server's baked graph. The thief entered it through the unlocked door, climbed to the spear rock and returned through the real portals. The revised assignment keeps the mage outside the lethal corridor: the thief brings the earned spear back and drops it beside the mage, who handles its Teleport tower. Dark Sight now reserves the thief's support-action window in the corridor and is renewed before its expiry; unrelated support casts wait until the thief leaves. LPQ's existing Dark Sight policy is unchanged.

**Verification so far:** Actual thief entry into map 990000431 and arrival at the spear platform were recorded. All three ordinary spears have been retrieved and placed through real jump courses. A fresh normal-entry run is now testing the complete corrected handoff and fourth tower; no resurrection or map-warp shortcut rescued the failed run.

## 2026-10-24 - GPQ: the fourth spear, Haste escort and boss preparation

**Verified live:** The thief retrieved the guarded spear and handed it to the mage outside the golem corridor. The mage reached and activated the fourth tower through normal Teleport movement. Together with the three jump-course offerings, the real spear gate reached state four and opened the next hallway. Initial fountain testing also produced the normal monster medals/scrolls and food/wine box drops.

**Movement policy gap:** GPQ had been omitted from the existing PQ exemption to the autonomous bots' 500k-meso movement-skill reserve. A fully trained mage with full MP therefore refused Teleport. Adding GPQ to that same exemption retains skill possession, MP and map restrictions. The normal movement-skill eligibility regression covers GPQ, poor ordinary population bots, insufficient MP and maps that forbid the skill. The thief now accompanies a remaining physical jumper after handing over his spear so normal party Haste can support the route.

**Boss preparation:** Ergoth's WZ attacks include 900ms magic casts (one with the deadly/1-HP flag) and a 2040ms magic-column cast with nineteen possible columns. GPQ now forwards these real casts into the existing client-attack observer, uses all nineteen mask bits, and reports intersecting hits through the player damage handler. Unit checks verify the metadata and packet-forwarding scope. This is implemented; a live Ergoth fight is still pending at this point.

To give the level-100 crew legitimate survivability rather than damage immunity or inflated stats, the warrior slot is now a Dragon Knight with ordinary Hyper Body. The exploratory crew left through the normal exit NPC; four dedicated companions were reused with the new warrior. During the next gathering period, actual HP increases from Hyper Body were observed. The source and KB distinguish this current crew from the original Crusader test fixture.

## 2026-10-24 - GPQ: the fountain clears, but a one-pixel jump blocks the trousers

**Verified:** The normal fountain puzzle cleared in four checks using public correct/misplaced/unknown clues. A human-mode checkpoint confirmed that the crew waited for its leader to start the NPC puzzle. The first checkpoint's movement command failed to reach the statue's elevated origin; releasing that command and correcting the NPC approach let the ordinary interaction continue. No answer or stage-clear property was injected.

Wine resting above the floor required a real jump before pickup. The GPQ collector now jumps into the existing pickup radius instead of trying forever from below. A regression checks that the distant item remains uncollected until the actor is actually close enough.

**Clothing progress:** The mage recovered the shoes, the thief recovered the shirt, and the level-25 scout recovered the crown through its restricted room. The body accepted the shoes normally. The other pieces waited because the required next item, trousers, had not arrived.

**Why the trousers worker could not finish:** Map 990000611 forbids movement skills. Its staircase includes an overlapping y=7 to y=-70 step: exactly 77 pixels. The shared 50ms jump simulation has a continuous apex of -70.00625 but samples only down to -69.5, rounded to -69. The collision sweep misses that ledge. Across 1,313 launch-position/direction combinations on the overlapping platforms, the current shared simulator produced no upper landings. Live navigation reproduced the same unreachable boundary.

We did not warp the worker, grant the trousers, alter the stage state, boost the jump or enable a forbidden skill. The next task is a shared physics/collision regression and correction, followed by graph validation and another normal-entry GPQ run. Changing the shared movement foundation needs broader validation than a room-specific workaround.

The test ended through normal disconnect/event cleanup; the five companions left the guild and logged off, while the guild remained. The dedicated Dragon Knight's temporary test naming mismatch was corrected offline to GPqSpear1. The autonomous population and server process were left running. Sacrifice, Ergoth and rewards remain implemented but **not live verified**. GPQ is still in development.

## 2026-10-24 - GPQ: correcting the shared jump-apex sweep

A focused regression reproduced the trousers failure before the fix: a legal 77-pixel upper platform was missed. The correction splits the collision sweep at the actual ballistic apex when a tick crosses it, rather than treating the whole tick as one straight segment. Both halves retain their real collision ordering/progress. The same helper serves ordinary live movement, baked landing predictions and rope-grab simulations. Jump height, gravity and movement-skill restrictions are unchanged.

All 60 physics tests passed after the change, including vertical, left and right launches that must agree between live physics and baked predictions. The same test rejects a 78-pixel ledge. The rebuilt real GPQ trousers map now has an ordinary base-profile path from the entrance to the clothing box. Existing OPQ cloud, ascent, lounge and directional-drop navigation tests also passed. Graph version 78 invalidates the older baked data.

This is an offline regression result, not a live Stage Four clear. The dedicated crew was dismissed normally. After validation, the server completed its normal saves/shutdown and restarted with the fix. A read-only runtime check confirmed graph version 78, ordinary trousers-route reachability, the population scheduler enabled and all dedicated test accounts offline. The next live run must exercise the rebuilt course, then the sacrifice, Ergoth, Rubian and bonus reward sequence.

**Executor replay:** The complete course now passed from the normal entrance floor and from the actual fallen worker position, using the full movement stack. Broader checks passed: 60 physics, 47 graph-builder, 44 planner and 14 existing movement-lab tests, alongside the two GPQ course tests. These remain separate from the next live event audit.

## 2026-10-24 - GPQ: the first complete journey through rewards

**The crew crossed the finish line.** The native boss clear was recorded at 21:39:37 UTC. The leader picked up the Rubian, handed it to the knight and entered the thirty-second bonus room. Actual inventory changes confirmed weapon, scroll and potion rewards, and all six participants returned to the ordinary camp alive. Guild points grew from 170 to 1041 through the native stage and final NPC rewards. This was a bot-only gameplay test; a human client audit is still pending.

**The low-level role worked:** the scout cleared its restricted clothing course, delivered the crown, then removed and offered its own earned earrings at the final gate. The ordinary map damage killed it; the normal reactor opened the gate and enabled revival. It revived in the quest's return map, earned a replacement pair and walked back through the cleared routes. We did not supply a sacrifice item or use a resurrection command.

**A real boss casualty found another integration gap:** the thief died to Ergoth. Generic PQ handling told it to remain at the tomb while teammates were alive, even though GPQ's opened gate specifically permits normal revival. The GPQ-only correction respects that native flag and invokes the existing death-popup/return-map behavior. Before the sacrifice, and in other PQs, the old wait remains. The thief revived normally before the turn-in. This improves recovery; it does not make the crew invulnerable.

**Healing/support parity:** GPQ was missing from the boss crews' action-ready support checks. It now allows the same due healing/rebuff evaluation between attacks, including idle stage support, with ordinary MP, skill, cost, disease and animation locks. The focused regression checks that an idle GPQ priest can reach the skill-cost gate and that ordinary population idle behavior remains unchanged.

**The Teleport tower had a Haste-dependent trap:** a two-pixel cast window lay at a ledge's edge. While waiting for the next normal Teleport cooldown, the mage walked into that window, built momentum and coasted off even after releasing the movement key. The full-stack replay reproduced a fall from y=-9 to y=134. Merely targeting the current point after arrival was too late to brake. The final correction waits on the current platform before approaching a vertical Teleport window during cooldown. It retains horizontal cooldown travel, strict cast windows, natural ground braking and the normal cast interval. The corrected replay passes, and the clean live repeat subsequently reached the top and placed its spear in roughly twenty seconds.

**Repeatability check underway:** all five companions were dismissed normally, withdrew from the guild and logged off. The existing guild retained its master and points. Reassembly selected the identical five character IDs, refreshed the carried supplies and entered through Shuang again without another guild-creation charge. The new run uses fresh puzzle patterns and all final gameplay fixes from the start.

The final focused validation passed **308 tests**, with zero failures/errors/skips, across GPQ, support/combat, revival, movement, observer, loot protection and three-second cure handling. Earlier shared-physics and graph-builder suites also passed. The experimental GPQ work has not been pushed to GitHub.

## 2026-10-24 - GPQ: the clean repeat reaches the finish line too

The second run completed through the ordinary knight turn-in at **22:02:33 UTC** and returned to camp after the timed bonus at approximately **22:03:05 UTC**. All six were alive at exit. This run used the final gameplay corrections from the start, with fresh puzzle patterns and no mid-run code changes or intervention.

Normal dismissal/reassembly reused the exact same five companion IDs, replenished supplies and retained the guild without another creation fee. Both the mage's Teleport tower and the warrior's trousers course completed normally on the repeat. The scout completed its restricted room and made the ordinary earring sacrifice again. Ergoth and every guard were defeated; the Rubian was picked up and returned through the normal NPC. The repeat's reward observer recorded 218 real item/meso drop objects, and actual inventory pickups were recorded. Guild points reached 1964 through native rewards.

The test leader then disconnected normally. The five companions left the guild, saved and logged off, leaving the master and guild points intact. The server is online with the final fixes and unchanged population configuration. Player-facing assembly text now explains the complete normal entry flow instead of warning that later stages are unfinished. The implementation is **ready for a human client audit**, not yet human verified. Nothing has been published to GitHub from this development session.

### 2026-10-24 - MVPQ starts with real letters and exposes two movement mismatches

We began MV's Lair with a dedicated six-person test party and the ordinary Charles entry. The mixed crew earned all 15 Secret Letters from real shuffled reactors and their native respawns, then dropped and delivered them to the leader. No letter grants or stage properties were used. Readiness uses the shared gear/AP/SP/supply routines and leaves the autonomous population alone.

Several workers first selected boxes they could not reach. Claim selection now checks each character's actual graph and learned movement skills. A separate letter-delivery issue appeared when the required total included an uncollected reactor drop: workers stopped searching before retrieving it. They now continue collecting natural floor letters during handoff, while refusing to reclaim player-dropped delivery piles. A later live attempt gathered and delivered all 15 after that correction.

A thief repeatedly jumped off a ladder into a wall although the baked rope edge promised a crossing. The graph had simulated the exit with an unknown collision group, while the real character retained the group from its last platform. Rope exits now require a consistent landing across relevant wall groups; graph version 79 invalidates stale edges. Scoped memoization avoids repeating identical launch-window simulations. The concrete blocked rope exit has a real-map regression.

Another lower-platform jump succeeded in isolated physics but failed in the complete navigation loop. Near its apex, the bot passed just under an overhead platform; navigation prematurely treated it as attached there and changed the collision group, causing a wall collision. Two tests reproduced that failure before the fix. Authored jumps now retain their airborne/takeoff state until physical landing; both tests, the existing 44 navigation tests and the GPQ jump-course tests passed afterward.

Simultaneous falls also exposed a native cached-JavaScript concurrency error at the reset portal. Each cached portal script now has an execution lock, with a separate cache lock for creation/reload. A two-thread regression verifies a script is never entered concurrently. Different portal scripts remain independent.

The mixed crew still exposes a starting-area traversal limitation: Flash Jump has a route, but the currently available ordinary ground/Teleport paths do not leave that component. We did not hide this with a warp or alter the map. A separate Flash Jump-capable diagnostic party submitted the earned letters through the real guide and reached MV's room; further completion testing and human confirmation remain pending.

### 2026-10-24 - MVPQ becomes accessible to ordinary classes and completes through treasure

The inherited course spawn sat on isolated central islands. An ordinary ground character could not leave that component; Flash Jump could. I wanted to audit as a Beginner, so after discussing the geometry I approved using the connected bottom-right platform beside the existing ladders. Normal event entry and the actual fall-reset portal now share that one spawn definition. No letters, guide check, boss requirement or reward was skipped, and no client WZ edit is needed. A complete unbuffed Beginner movement replay reaches the guide using normal walking/jumps/ropes.

The first broad escape intent was too eager: reaching an island box could trigger recovery before the box had been opened. Recovery now evaluates the current box, floor item, handoff or exit goal. It opens/picks up reachable work first and otherwise holds ordinary directional input until the real pit portal catches the fall. A bounded-motion regression proves this is walking/falling, and another ensures a reachable island box is not preempted. The subsequent live course completed again with the mixed crew.

A full bot-only mixed-party run earned and delivered 15 letters, submitted them at the real guide, gathered and broke the actual summoning rock, defeated MV, reached the native treasure room, collected four actual chest drops and returned through the normal exit. All six survived. The initial all-thief diagnostic had also cleared MV and collected real treasure, but suffered deaths and eventually used the ordinary death-popup return; it is separate diagnostic evidence.

One fixture lesson matters for honest reporting: the test leader spawned as a Beginner but generic ownerless job logic advanced it; a later inspection found an I/L Archmage. That full clear is therefore recorded as a mixed-party clear. Beginner navigation is independently verified by the complete skill-less/unbuffed replay, and the human-entry regression verifies that a real player's class and build are never modified. Future headless Beginner fixtures should select the existing permanentBeginner personality before being registered.

The same six characters then re-entered normally. Ginger Ales returned to 200; learned-skill supplies replenished, including both rock types for the Bishop and Summoning Rocks for the thief. With a controlled leader idle in human-led mode, the five companions gathered and dropped all 15 letters. The pile remained intact for over 2.5 minutes while the leader held none and the guide remained uncleared. Normal close-range pickups and the real guide conversation advanced the stage. All five bots gathered beside the tunnel and stayed in the course for 15 seconds while the leader waited. They escort the letter holder and give a ready notice before the human enters.

Finally, ordinary party leave returned the leader to Charles and left no companions online. Cancellation uses the native party/event paths, and departed/unregistered members no longer block the remaining native-eligible crew's gathering checks. The boss notice also warns about the native ten-second loot window rather than telling the player to take unlimited time. WhereLevel now identifies MVPQ's crew as available (and corrects GPQ's older availability flag). These are server-side/live fixture checks; the client audit remains for me to perform.

### 2026-10-25 - MVPQ pit recovery stays quiet during the client audit

During stage one, I found the repeated dead-end island/pit-reset explanation distracting. Removed that recovery announcement while preserving the ordinary movement and native pit reset. Stage objectives and useful progress guidance remain. Verified the announcement is absent from source and rebuilt the server; the human audit is still in progress.

### 2026-10-25 - MVPQ passes the human-client audit

I marked MVPQ human-client verified after playing through it. Stage one was enjoyable, and the unnecessary pit-reset announcement was removed during the audit. I questioned whether MV was knocking the bots back: live bonus-room diagnostics confirmed native attack detection, but the captured thief hit rolled a miss and did not prove successful-hit recoil. I then personally saw the Bishop get knocked back and accepted MVPQ as verified. No knockback gameplay change was needed for this confirmation. Temporary diagnostics were restored to the original observer. GPQ is next for a separate human audit.

### 2026-10-25 - GPQ offering collection exposes a human-held supply gap

During my human audit, I collected one medal while the bots held the other three. The offering driver counted only companion inventories and waited silently. Dropping it did not solve the shortage: native GPQ pickup rules allow only the dropper to retrieve player-dropped items, even though the companions approached it. The initial advice to drop it for them was incorrect. With my authorization and my character standing one pixel from the medal, normal pickup recovered it; a normal nearby trade, including the player interaction handler's item validation and both confirmations, transferred the single medal to the archer (three to four). No items were generated, pickup ownership was bypassed, or stage progress injected. The run was recovered, but automatic guidance for human-held offerings remains unresolved; this is not a completed gameplay-code fix.

### 2026-10-25 - GPQ clothing notice names the grave explicitly

During the human audit, I saw the clothing-ready notice while still in the waterways and could not find Sharen's Soul. The crew was correctly waiting in Sharen III's Grave, a separate map reached through the far-end door of a clothing room. Updated the notice to name both the grave and Sharen III's Soul and explain that route. Verified the native portal scripts lead there; no portal or movement rules changed. Meso markers were discussed but not added.

### 2026-10-25 - Ergoth preparation and Dispel recovery after the human-run deaths

During my GPQ audit, several companions died at Ergoth and began the normal return journey; the spearman survived. Live inspection after revival found roughly 198-200 Ginger Ales remaining, a Priest with 1,290 base maximum HP, ranged companions around 2,900-3,000 HP and no Hyper Body on those returning members. Ergoth has magic attacks and nearby guards can Dispel. These observations identify survival risks; they do not reconstruct every fatal hit or prove which attack killed each bot.

The crew now gathers with the human at the throne-room entrance before its bots break the Rubian crystal. It checks that everyone is alive and present, both Hyper Body stats are active, and the mage has Magic Guard. Preparation is latched per event so the first bot walking toward the crystal does not strand the others in a gathering loop. In this encounter, missing learned Magic Guard or an in-range party member's missing Hyper Body takes priority over ordinary rebuff deadlines, including after Dispel. Casts still use the existing support pipeline, costs, cooldowns, animation locks, status restrictions and native skill hitboxes. Unprotected combatants regroup with the living spearman instead of continuing normal offense; he approaches allies who need his buff. No levels, HP, gear or invulnerability were granted, and the Priest was not changed to a Bishop.

GPQ also checks both bound potion keys while alive. A regression passed Ergoth's real deadly attack through the shared player damage handler: HP and MP became one and the observer notified the ordinary potion controls of both losses. This verifies the recovery handoff, not a newly observed live deadly-attack survival. Four focused support tests cover gathering requirements, missing Magic Guard despite a future rebuff deadline, cast-cost/animation constraints, Hyper Body's real bounds and both potion-key checks. Relevant GPQ, combat, potion and observer regressions and the packaged build passed. The changes were applied live without a restart; the next human Ergoth attempt remains the survival acceptance test.

### 2026-10-25 - GPQ accepted with a documented Ergoth caveat

I accepted GPQ as human-client verified while explicitly noting that the companions are squishy against some Ergoth attacks. I did not have time for another boss audit after the gathering/protection/potion changes. The catalog, README and knowledge-base indexes now reflect that acceptance without presenting those last changes as repeat client-tested. MVPQ remains human-client verified. Release notes for `PQ bots for MVPQ and GPQ added.` preserve both statuses and the known offering-ownership and early-statue-start audit cases. Publication is a separate step through the clean publication checkout; preparing these notes does not push private development history.
