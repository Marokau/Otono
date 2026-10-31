# Growing the PQ crews

A journal of building party-quest companions together: what I noticed in
MapleStory, what I discovered on the server, and why we changed the bots.

Started October 19, 2026.

Earlier milestones below are backfilled from our conversation; their exact dates are intentionally not reconstructed.
This is a selected history, not a claim that every bug or client test has been recorded.

The [Otono bot knowledge base](kb/README.md) documents the current implementation, built on NuTNNuTâ€™s bot system and extended through Otonoâ€™s development.


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

### 2026-10-25 - Horntail begins with a real trial clear and the first head

I asked for level-150 Horntail companions, with prerequisite awards confined to dedicated bots and human requirements preserved. The existing native trial is six-person and the expedition supports multiple parties, so the implementation starts with five trial companions and can expand to eleven companions at the raid entrance. Each raid party has a Bishop and Dark Knight. The fixture pool is separate from the autonomous population; bot-only certificate preparation, normal goal gear/AP/SP, mastery-book use and finite consumables prepare these characters without awarding human prerequisites.

On channel two, an isolated six-person crew earned the crystal keys from native monsters, relayed them through the real pedestals, left a Bishop to collect their arrivals, returned with the leader's blue key, made the normal Schedule check, selected the light route, earned six red keys and cleared through the native NPC to the raid entrance. No keys or stage properties were supplied by the harness. Separately, twelve isolated level-150 characters formed two parties, registered through the normal expedition NPC, defeated the 330-million-HP first head and reached the second head with all twelve alive. This is a headless milestone; neither the full Horntail clear nor human-client behavior is verified at this point.

The trial leader exposed a shared Flash Jump graph defect: the authored jump landed at a narrow ledge's lip, then ordinary ground movement immediately walked off, repeatedly returning to the same rope. The focused regression reproduced the real (864,-160) launch and failed before the correction. Flash Jump authoring now checks the same three post-landing ground ticks already used for normal jumps; unstable landings are excluded from launch windows. The regression passes, and the live leader traversed the cave and completed the trial after a targeted graph rebuild. Production graph version is 80. The running process retains version 79 with the corrected authoring and rebuilt trial maps, avoiding a population-wide cold-cache rebuild in the middle of testing; the next normal restart uses the packaged version-80 caches.

Initial combat inspection also found Dark Knights wasting their finite potion load on Dragon Roar and Bowmasters evaluating Hurricane with ordinary bow timing. The established Zakum policies now extend to Horntail: no repeated percentage-HP attacks, normal Triple Throw against boss parts, fixed-rate held Hurricane and its visible effect, and ordinary safe repositioning for a stronger reachable skill. Incoming attack handling includes Horntail's native area attacks, its full column masks and ground-only legs/tail attacks. Seduce's native left/right/jump values interrupt normal bot control and potion use rather than allowing unaffected attacks. These hazard changes require unit and later client validation; the headless clear alone does not prove every boss attack works.

### 2026-10-25 - Horntail repeat entry, raid expansion and native crystal activation

The same five companions completed a second trial through the native Schedule exit without mid-run intervention. Assembly at the raid entrance retained their identities, added six dedicated companions, formed two ordinary parties of six and restored each companion to 500 Ginger Ales; both Bishops had 200 of each rock. A human-mode waiting check left all eleven companions outside the event until the leader started it, and ordinary dismissal logged them off. Separately, the original twelve-person raid defeated the second preliminary head and entered the main chamber with everyone alive.

The main crystal initially stopped at state five. Its WZ data requires six real hits, with state five still accepting another attack. The controller had reused an arbitrary five-state cutoff. It now follows the native reactor's attackable type, preserving the real swing, hit range, jumps and reactor handler. A regression checks state five remains eligible and the terminal animation does not; after loading the fix, the sixth ordinary hit summoned the real body and started its unchanged 60-minute timer.

Two targeting distinctions matter in the main chamber. A living part under weapon or magic immunity still has dangerous body contact, so immunity excludes it from offensive choices while retaining it in stance safety checks. Conversely, the native defeated-part decorations (8810010-8810017) retain 66 million HP but are not living objectives; the crew initially wasted attacks on the defeated legs. Those decorations, the invisible aggregate and summon animations are excluded from direct/cleave/summon targeting. Focused regressions cover both cases. This does not reduce real part HP or generate stage progress.

The first sustained twelve-person level-150 damage sample measured about 426,000 damage per second across real parts, below the roughly 581,000 required to remove 2.09 billion HP in an hour. I was asked whether to retain twelve participants at a higher level or increase the level-150 raid size. No levels, roster size, boss HP or timer were changed while that decision was pending. Full raid victory and human-client behavior remain separate acceptance checks.

### 2026-10-25 - Horntail dark trial, long-raid supplies and early exit

The same trial fixtures also cleared the dark route. The test leader selected the bulb with ordinary movement, jumps and real reactor-hit packets; native portals, actual boxes and normal earned-key hand-ins completed the route. Initial test-harness movement to the floating bulb could not reach its height, and an operator-only attack stopped its ordinary cooldown ticking. Returning control to the normal companion loop allowed two further normal jumping swings, selecting dark without assigning the bulb state. The dark boxes now stop when their native attackable state ends, preventing repeated hits on terminal animations; WZ links show four required hits, unlike the main raid crystal's six. Both light and dark routes reached the native clear map and the crew saved/logged off normally.

The initial long raid exhausted all four Night Lords' 6,000 Ilbis. Their attack plans became unavailable and weapon attack dropped by the stars' contribution, explaining the later damage collapse. Future normal assembly now retires empty rechargeable slots and stocks 12,000 Ilbis or arrows, alongside the existing 500 Ginger Ales and learned skill supplies. The three-stage raid sample is not a full clear, and no ammunition was injected into its active encounter. The Bishops also prioritize genuine summons even if that requires leaving an in-range boss part; real-part hazards remain in their movement safety checks. A simultaneous stun overrides Seduce inputs through the existing stunned-physics path. Focused regressions cover these policies.

Ending the partial test through the Crystal of Roots exposed an independent lifecycle issue: the inherited expedition deliberately continues after its leader exits. The crew's party checks therefore remained valid and companions continued fighting. They now explicitly detect the leader leaving the event and walk to/use the same ordinary exit NPC themselves, without changing the native expedition rules. The normal-exit regression and the relevant combat, observer, menu and summon checks passed with the packaged build. All twelve subsequently followed the normal exit alive, were restocked outside the event and saved/logged off; all test IDs were absent from the live bot registry afterward. This remains bot-only verification; full victory, rewards and client-side Horntail attacks require their own acceptance tests.

### 2026-10-25 - Same twelve Horntail companions raised to 180 with raid skill priorities

After the level-150 partial run, I authorized level 180 while retaining twelve participants and leaving gear unscrolled. The same twelve characters were raised through normal level-up/AP/SP paths outside the encounter. An opt-in Horntail SP order prioritizes maxed core attacks, party buffs, defenses and summons before farming utility; ordinary population and Zakum builds keep their previous orders. Core priorities fit the normal level-180 SP budget, so optional class skills are not all forced to maximum. Mastery-book requirements still run through the shared book processor.

The pre-entry skill audit found both Bishops' Bahamut capped at 10 despite available SP. WZ quest actions establish that quests 6120-6124, rather than books, unlock its cap through 10/15/20/25/30. Companion readiness now awards the required materials and executes those native quest actions, consuming the materials and retaining the quest rewards; subsequent preparation skips completed quests. Human skill quests and cave requirements are unchanged. The repeated readiness audit confirmed all twelve at level 180 with their core raid skills maxed, including Bahamut 30, Resurrection 10, Holy Shield 30, Hurricane 30, Sharp Eyes 30, Triple Throw 30 and the warriors' principal attacks/defenses. Each was resupplied before the new normal NPC registration. The new full raid is a separate live test; the upgrade and successful build do not establish a victory.

### 2026-10-25 - Horntail rope stances and the limits of the existing ranged gear goals

The level-180 retest cleared both preliminary heads normally, then started the unchanged 60-minute main-body timer through the crystal. Its first sustained body sample measured about 693,000 damage per second, versus the earlier level-150 sample of about 426,000. These are phase samples, not a guarantee of finishing the whole raid at a fixed rate.

Checking the new upper-platform phase exposed a missing controller guard: the generic attack gate does not forbid rope attacks. Horntail now treats a rope strictly as transit and continues ordinary navigation to a ground attack stance before planning attacks, matching the established Zakum rule. A focused regression verifies the groundward movement and absence of attack planning while climbing; relevant build/combat tests and the package passed. No reach, jump strength or damage values changed.

The equipment audit also corrected an assumption about readiness. The existing low-secondary-stat gear policy equips the Night Lords with Bain Biters (level 58, base weapon attack 33) and the Bowmasters with Maple Kandiva Bows (level 64, base weapon attack 78). The warriors have Timeless weapons. These are legal goal-selected unscrolled items, but the ranged weapons are not the highest-attack endgame options. I asked to leave scrolling/gear work alone and raise levels for now, so no weapon/AP-goal redesign was applied during this attempt. The core raid skills were verified at their maxima through earned SP and native unlocks; gear quality remains a separate factor in raid damage.

### 2026-10-25 - Horntail warriors were prepared for the wrong evasion target

The level-180 run reached the final heads with all twelve alive, but the damage pace dropped. A live audit through the existing combat formula found both Heroes at about 23.5% hit chance against the middle head and the Dark Knights at about 54.6-63.7%, despite 100% against the preliminary head. Preparation used the first head's evasion of 18; the main heads have 43-45. The default companion accuracy floor also targets 90%, rather than complete reliability.

Two regressions reproduced the issues before correction: the prepared reference was 18 instead of the WZ maximum 45, and the inverse formula bought about 90% rather than 100% hit chance. Future Horntail preparation now uses the most evasive main head, buys the normal DEX required for 100% unbuffed hit chance and checks AP again after gear/SP preparation. It ignores temporary accuracy buffs when setting that floor so expiry or Dispel does not undo readiness. Damage rolls, boss evasion, gear scrolls and the hit formula are unchanged; other PQ/population accuracy targets remain unchanged. The fixed regressions and relevant build/combat tests passed. This correction is for outside-event preparation; the running attempt was not given an in-fight AP respec. The subsequent outside-event live readiness check passed; its result is recorded below.

### 2026-10-25 - Level-180 native timeout and corrected readiness verified

The same twelve completed both preliminary heads and defeated seven of the eight main parts. The unchanged 60-minute main-body timer expired with only the middle head alive at 147,625,434 HP, about 7.1% of the original aggregate HP. All twelve survived; the native event reported no clear and no victory rewards. We did not extend the timer, adjust HP or respec the crew during the encounter.

After normal exit, the corrected preparation allocated ordinary AP against the main head's evasion. Every companion passed the live level-180/core-skill audit and the combat formula's 100% hit check against level 160/evasion 45 without temporary accuracy bonuses. All four warriors reached permanent accuracy 164; the Heroes had previously been hitting at only about 23.5%. Supplies were restored, including 12,000 Ilbis per Night Lord, and all dedicated test characters were saved and logged off. The package and focused regression suite passed. A fresh full raid is still needed to establish victory with corrected preparation; client-side Horntail mechanics remain pending human audit.

### 2026-10-25 - Corrected accuracy narrowed the Horntail shortfall; normal Resurrection recovered losses

The same twelve entered again with corrected main-head accuracy and unchanged gear, boss HP and timers. Both preliminary heads and seven main parts fell. The native 60-minute main-body timer expired with the middle head at 47,023,625 HP (about 2.25% of the aggregate starting HP), versus 147,625,434 in the prior attempt. The event did not clear and awarded no victory loot.

Snapshots caught HornSpear2 and HornThief2 fallen during the final-head phase. Both Bishops had already used native Resurrection, with real 30-minute cooldowns; the second Bishop recovered both fallen party members when that cooldown expired near the end. All twelve were alive on normal exit, then resupplied, saved and logged off. This establishes normal cooldown recovery, not flawless survival: individual death causes still need closer evidence. No GM resurrection, cooldown reset or in-fight stat adjustment was used.

### 2026-10-25 - Fund the weapon goal before checking whether it is wearable

I supplied two historical Horntail guides and requested more time for an independent clear with strong unscrolled equipment. The existing provisioner only considered gear wearable with the current stats. Its AP planner only considered owned weapons. Together those policies stranded Night Lords on Bain Biters and Bowmasters on Maple Kandiva Bows: the better weapons were excluded before their ordinary secondary-stat requirements could be funded.

Horntail preparation now evaluates the same normal drop/shop weapon catalog through the existing AP/DPS chooser before granting gear. It pays requirements from the existing AP budget, then lets the normal provisioner and equipment handlers select and equip legal items. Hurricane bow comparison uses its actual fixed 120ms shot cadence for these raid fixtures, rather than penalizing a slower normal bow animation. Damage formulas, scroll levels, entry rules and the ordinary population's gear policy are unchanged.

The focused regression proves that a stat-gated Timeless claw can become a paid AP goal instead of being discarded, rejects that weapon below its level requirement, and checks that fixed Hurricane cadence remains scoped to the Horntail build. Equipment/combat regressions and packaging passed. The live pre-entry audit selected Timeless Lampions (56 attack) and Timeless Engaws (107 attack), with legal requirements and zero scroll levels; warriors retained their Timeless weapons and Bishops retained their goal-selected Elemental Wands. Every core skill and 100% main-head hit check passed. The same Night Lords naturally reached 181 during earlier combat; no further levels were awarded. A fresh normal raid is testing this preparation.

### 2026-10-25 - Corrected accuracy narrowed the Horntail shortfall; native Resurrection recovered losses

The same twelve entered again with corrected main-head accuracy and unchanged gear, boss HP and timers. Both preliminary heads and seven main parts fell. The native 60-minute main-body timer expired with the middle head at 47,023,625 HP (about 2.25% of aggregate starting HP), versus 147,625,434 previously. There was no native clear or victory loot.

Snapshots caught HornSpear2 and HornThief2 fallen during the final-head phase. Both Bishops had used native Resurrection, with real 30-minute cooldowns; the second Bishop recovered both fallen party members when its cooldown expired near the end. All twelve were alive on normal exit, then resupplied, saved and logged off. This establishes normal cooldown recovery, not flawless survival; individual death causes still need closer evidence. No GM resurrection, cooldown reset or in-fight stat adjustment was used.

### 2026-10-25 - Fund the weapon goal before checking whether it is wearable

I supplied two historical Horntail guides and requested more time for an independent clear with strong unscrolled equipment. Existing provisioning considered only gear wearable with current stats, while AP planning considered only owned weapons. Together those policies stranded Night Lords on Bain Biters and Bowmasters on Maple Kandiva Bows: better weapons were excluded before their ordinary requirements could be funded.

Horntail now evaluates the same normal drop/shop weapon catalog through the shared secondary-stat AP/DPS chooser before provisioning. It pays requirements from existing AP, then uses normal gear selection and equipment handlers. Hurricane bow comparison uses its actual fixed 120ms shot cadence for these fixtures rather than normal bow animation speed. Damage formulas, scroll levels, entry rules and population gear policy are unchanged.

Regressions verify paid AP for a stat-gated Timeless claw, rejection below its level requirement, and the scoped Hurricane cadence. Equipment/combat checks and packaging passed. The live pre-entry audit selected Timeless Lampions (56 attack) and Timeless Engaws (107 attack), legally equipped with zero scroll levels. Warriors retained Timeless weapons; Bishops retained goal-selected Elemental Wands. Every core skill and 100% main-head hit check passed. The same Night Lords naturally reached 181 during earlier combat; no further levels were awarded. A fresh normal raid is testing this preparation.

### 2026-10-25 - Distinguish the inherited one-hour timer from historical Horntail

I questioned the one-hour main-body deadline after comparing old raid videos. The unchanged inherited scripts started the expedition at 120 minutes but reset it to 60 when the crystal summoned the main body. That explained our test deadline; it did not prove the historical game used one hour. Official MapleSEA version 0.98 notes from January 2011 explicitly changed normal Horntail from twelve hours to two hours. This is primary evidence for the historical twelve-hour rule in that region, not proof of every GMS version's exact behavior: https://www.maplesea.com/news_events/view/patch_notes_for_version_098/.

I chose twelve hours for future Otono raids. Event setup and the main-body crystal now share the 720-minute duration through an instance property, with the NPC advertising twelve hours. The real script regression reproduced the old behavior, then verified both the setup timer and crystal reset at 43,200,000 milliseconds. The already-spawned test body retained its old one-hour deadline for an honest comparison. The acceptance goal is a reliable independent clear and measured completion time, rather than forcing an unscrolled crew into an inherited speed limit.

### 2026-10-25 - Resurrection needs a casting platform, not the corpse's exact coordinates

The upgraded-weapon test exposed another recovery problem: HornSpear1 was dead at (-2,-136), while his Bishop stood directly below at (0,260). The live audit showed Resurrection learned, affordable and off cooldown, but its normal bounding box reached only to y=-90 from that floor. Repeatedly navigating to the corpse's projected floor could never bring it into range. The second Dark Knight later presented the same suspended-corpse case.

A Horntail-only approach helper now searches existing reachable ground regions for a safe position whose native Resurrection bounding box contains the corpse, then uses ordinary navigation to that platform. Once in range it delegates to the existing support-special-move handler. No corpse repositioning, enlarged range, free skill cost or cooldown reset is involved; Zakum behavior is unchanged. The regression rejects the floor below the corpse and an unreachable nearer platform, selects a reachable casting platform, and verifies native-handler delegation and cooldown refusal. Relevant combat/controller tests and packaging passed.

Live observation after applying the approach fix confirmed HornSpear2 alive, back on a combat platform and supplying Hyper Body again. Both Bishops used normal Resurrection; another later snapshot still had fallen members, so this is recovery verification rather than a claim of flawless survival. The raid remained under its original one-hour test timer.

### 2026-10-25 - The twelve-person Horntail crew completed the full raid

The same dedicated fixtures entered through normal expedition registration in two parties of six, defeated both 330-million-HP preliminary heads, broke the real crystal and defeated all eight main parts. The native event reported `clear=true`; the headless leader made 20 successful loot pickups and used the normal exit. Main-body elapsed time was 3,454 seconds (57 minutes 34 seconds), about 1 hour 22 minutes for the complete raid including preliminary heads. Boss HP, damage formulas, skill costs and cooldowns were unchanged. Unscrolled class gear, earned AP/SP, ordinary attacks, consumables and native Resurrection carried the encounter.

During this run I explicitly chose to apply the twelve-hour rule to the active instance as well. Adding the difference through the event timer API preserved all elapsed time. The observer nevertheless confirmed native victory before the original one-hour deadline, so the extra time was not needed for the kill. After exit, the Horntail event managers alone were refreshed on channels 1-3 and their NPC requirements showed twelve hours. There was no server restart or population-wide event reload.

This was not a deathless clear: ten members were alive at the kill, and the other two completed ordinary death returns to Leafre. All twelve had positive saved HP afterward. The final audit found no test IDs online and no remaining Horntail expedition. Every fixture had 500 Ginger Ales; Night Lords had 12,000 Ilbis and both Bishops had 200 of each rock. Native combat EXP left the test leader at 182 and the other eleven at 181; these were earned levels, not a further preparation grant. Supplies, saving and logout completed normally.

Builds and focused gear, skill-budget, combat, recovery, NPC and real-script timer regressions passed. Horntail is now bot-only verified through victory, loot and exit. A human-client audit is still needed for encounter presentation, client-observed hazards and the human-led flow; unsupported type-4 attack geometry is not being claimed as verified. Nothing was committed or pushed for this test session.

### 2026-10-26 - Pink Bean begins with twelve companions and native statue waves

After Horntail's headless clear, I asked to begin Pink Bean with twelve participants. I had never fought it and offered to find historical references. The inherited event currently allows 140 minutes, with five statue waves, a real final reactor activation, a 2.1-billion-HP body and termination on the fifth death. Those rules were recorded rather than silently replaced with the remembered one-hour limit.

The new BotPQ Menu assembles eleven dedicated companions around the human, in two six-person parties with a Bishop and Dark Knight each. Reusable Pink fixtures receive level-180 raid preparation through the existing earned AP/SP, mastery-book, gear and consumable paths. Human Temple prerequisites remain; the normal expedition NPC handles actual registration and entry. The first live readiness audit confirmed legal unscrolled class weapons, 500 Ginger Ales, 12,000 Ilbis for Night Lords and both mage rocks at 200. Existing population bots were not recruited or changed.

The first statue exposed a positioning/aim mismatch: all statues share a central map origin although their real WZ bodies are offset. Thieves moved onto a legitimate platform but aimed toward the map origin instead of the visible statue. The bot-only aiming adapter now uses the actual statue rectangle for facing and projectile checks. Live observation confirmed the Night Lords attacking afterward. Hurricane's held-shot cadence is enabled in this encounter too; Bishops use Angel Ray against bosses and keep their add-clearing role.

The test also exposed lethal contact along a shortest jump toward the firing platform. Endpoint safety alone did not make the route safe. The approach now walks under the destination on the existing floor before jumping up through ordinary navigation. Regression tests cover that movement intent and the real offset coordinates; live repeat verification is still pending. Reflection stops attacks without suppressing normal body contact; Zombify suppresses Heal, and ordinary cure delays/Seduce restrictions remain in force. This is implementation and first-wave evidence, not a completed Pink Bean claim.

### 2026-10-26 - Godly drop gear replaces HP washing for Pink Bean readiness

The baseline crew cleared the first two native statue waves and defeated Hugin in wave three. Both Bishops used native Resurrection; all twelve were alive again before the test ended through the normal NPC and exit portals. Solomon still remained in wave three, so this was deliberately recorded as partial progress, not a clear or earned reward.

I did not want HP washing. When the body-damage readiness check showed ranged HP below the roughly 10,700-11,400 incoming magic estimate, I pointed out the existing Godly Stats HP/MP settings near line 500 of config.yaml. The plain WZ/catalog audit had omitted that existing NuTNNuT feature. We used the normal drop-stat generator instead: the same configured 20% gate, normal stat ranges and real unscrolled item copies, with bounded preparation sampling rather than forced maximum values. This preparation is awarded gear, not simulated claims of hours of farming. Global settings and intrinsic HP were unchanged.

The equipment optimizer also needed to retain HP candidates: pure damage pruning would discard a lower-damage piece before a survival target could select it. A registered Pink-only HP goal now participates in item and DP pruning/deduplication. Ordinary bots and previously verified crews keep their original damage policy. The new regression chooses the HP piece only in the opted-in case and preserves ordinary selection otherwise. A smaller bounded frontier keeps the new survival search from expanding across the entire catalog; this remains best-effort optimization.

One live character had 148 DEX and a group of mutually dependent 160-DEX pieces. The final proposed set looked feasible, but there was no legal first item to wear. Preparation now funds secondary requirements from existing earned AP before using normal equipment handlers; it never forces gear past its requirements. Four real ring slots also need four actual item instances rather than two rolled rings and two plain leftovers, while preserving normal pickup restrictions.

The second readiness audit confirmed the same twelve fixtures, unchanged intrinsic HP, zero scroll levels and all six ranged members at 12,011-12,260 Hyper Body HP. A fresh native entry confirmed those HP pools in the live event. This closes the gear-readiness gap without HP washing. Full Pink Bean completion and client-side behavior remain unverified while the repeat continues.


### 2026-10-26 - Wave-three Bishops expose support routing and a second HP gap

The Godly-equipped repeat cleared the first two native statue waves with zero deaths. A one-minute read of the actual monster damage ledger confirmed all twelve contributing about 696,000 combined damage per second. Later-statue stance planning also found reachable positions for every class, but this was only a planning audit, not live completion of Ariel or the body.

In wave three, both Bishops died near the left bird while the other ten continued. The first corpse was near the lower platform; the second was inside Hugin's rendered body. The exact fatal packets were not captured, so these positions alone do not establish every death's cause. Two concrete gaps were established: the estimated 20,500-point contact hit leaves roughly 4,100 after Magic Guard, above the Bishops' roughly 4,000 HP; and support walks toward Hyper Body still bypassed the safer encounter movement adapter. A safe combat stance did not guarantee a safe rebuff trip.

Bishops now opt into the same normal Godly gear preparation, targeting 6,000 HP with Hyper Body and keeping ordinary Magic Guard costs. Support and Resurrection trips also use the Pink-specific movement adapter. A lower-platform trip toward the center descends outward before crossing beneath an active bird; all movement still uses the normal navigation graph and physics. Regressions verify the Bishop gear goal and outward support waypoint. Neither intrinsic HP nor encounter rules are changed.

Resurrection remains party-bound: a surviving Bishop in the other six-person party cannot simply revive the fallen Bishop. We did not waive that restriction or restore anyone with a GM command. The repeat's final outcome and outside-event readiness audit are recorded below; full completion and client verification are still separate acceptance steps.


The bounded repeat ended with waves one/two completed, both wave-three sages defeated, Hugin at 167,410,442 HP and two Bishops dead. Native clear was false and reward pickups were zero. The development-session cutoff, rather than a changed encounter timer or a forced stage, ended this attempt. The new Bishop gear and outward rebuff route still need another live wave-three test.

Recovery exposed two further bugs. Revived bots could use the normal resurrection portal back to the arena after losing their event reference, but the exit NPC dereferenced that missing event. NPC 2141002 now offers its normal confirmation-and-exit for that state. Also, companions that had exited to the recruiter immediately rejoined the still-active expedition, preventing withdrawal from settling. The driver now waits outside while the leader is outside an in-progress encounter. Real-script/controller regressions cover both; the same two Bishops subsequently completed normal recovery and NPC exits without a forced warp or GM resurrection.

Final outside-event preparation verified unchanged intrinsic HP and zero scroll levels. The two Bishops' gear now calculates to 6,028 and 6,008 Hyper Body HP, and all six ranged fixtures retain at least 12,000. All twelve received 500 Ginger Ales; relevant stars and rocks were restored. The saved-state audit found positive HP for every fixture, no test IDs online and no Pink Bean expedition remaining. Native logout saves these characters at the Temple of Time return map (270000000), so Pink-only fixture reuse now includes that map instead of making a new batch. A regression preserves the population/owned-bot exclusions while accepting the verified return location.

Builds and focused combat, equipment, native-event, NPC and recovery checks passed. This session improved readiness and verified two complete statue waves, not a full Pink Bean kill. The next test must exercise the new Bishop preparation/support route, then continue through the remaining statues, native reactor, body and rewards. Client-observed hazards remain a separate human audit. No changes were committed or pushed to GitHub.


### 2026-10-26 - Use the supplied Pink Bean guides as tactics, not replacement rules

I supplied a retrospective video transcript and a MapleRoyals guide excerpt, then asked for another two hours of Pink Bean development. Both describe the repeated statue waves and phase-specific positioning, but they mix regions, private-server assumptions and later mechanics. We use them as tactical references rather than replacing inherited encounter rules. Local WZ data configures the body's reflection at a 90% HP threshold, a 60-second interval and a 20-second duration; the supplied guides give different timing. Actual reflection status remains the attack gate. No fixed guide countdown, chain-Crash buff lock, HP washing or timer change was added.

The same twelve level-180 fixtures were reused. A fresh normal entry confirmed the Bishops' larger equipment-derived HP pools. A Pink-only target selector now prioritizes Solomon, Hugin, then Rex in wave three. In later statue waves, the two existing six-person parties start on opposite sides and can help the other side when finished. Melee characters leave Munin to ranged attackers, and Ariel is held until all side statues are defeated. Adds remain eligible, reflection can pause the chosen target, and actual damage/range/navigation handlers remain unchanged. These are AI choices, not boss invulnerability or stage changes. Regression tests cover order, paired sides, reflection waiting, melee waiting for Munin, Ariel last, and preservation of other encounters.

The earlier deaths did not have captured fatal-packet evidence. Scoped Pink Bean diagnostics now record fatal contact/observed attacks with source, damage, prior HP/MP, Magic Guard and position, without changing damage. Live results from this fresh attempt are recorded after the run.


### 2026-10-26 - Apple readiness, summon discipline and a captured Hugin contact death

I approved twenty Onyx Apples per Pink Bean bot after the supplied guides highlighted strong bossing potions. They were introduced into the already-running second test at about 03:42 UTC, so earlier timings are not described as apple-assisted. Future assembly/retry preparation stocks twenty, and the dedicated crew enables the existing best-buff-consumable mode. Consumption goes through UseItemHandler; no damage formula or permanent-stat grant substitutes for the item.

The live introduction exposed a selection bug: the Bishops consumed an apple, while physical attackers kept weaker +12/+25 attack buffs and left all twenty apples unused. The selection check treated any existing buff as sufficient. Pink Bean best mode now compares attack strength and permits an actual stronger WATK/MATK consumable, while keeping equal/stronger buffs, cheap mode and other encounters unchanged. The regression covers these cases. A live audit then found nineteen apples on every bot and the real +100 WATK/MATK effects active.

A third timestamped video annotation warned that autonomous summons could trigger an unwanted bird fight. Phoenix and Bahamut are now actually dismissed through the normal buff-cancel path during statue waves; the casting loop waits for the real body to resummon. Beholder support remains. Direct attack selection also rejects an entire wide attack plan if it would hit a non-priority statue, allowing a narrower attack instead. It does not strip legitimate collateral from an executed hit. Tests cover real cancellation, body resummoning eligibility and unchanged collateral lists.

The fresh test's first death was recovered before the fatal-hit diagnostics were installed. Its second death was captured: Thief2 received 20,784 physical contact damage from Hugin with 12,162 HP and no Magic Guard near the lower platform's inner entry. Normal party Resurrection restored him; all twelve were alive again. This established an actual source rather than guessing from the corpse. The graph audit showed several legal entries into the same lower platform, including an inner-side approach. Floor-first routing alone did not select a safe approach.

Pink-specific movement now chooses the outer half of the lower firing platforms while the adjacent bird lives and moves existing inner-platform occupants outward before they resume combat. Authored jump windows, graph version, physics and real damage remain unchanged. The regression verifies the outer firing destination and ordinary movement delegation. These route changes still need live crossing verification, and neither a narrow safe endpoint nor an HP upgrade is being claimed as immunity to bird contact.


### 2026-10-26 - Three statue waves clear; wave-four spawning needs earlier positioning

The apple-assisted guide-informed trial completed the first three native statue waves and reached Munin in wave four. A sixty-second read of Hugin's actual damage ledger measured approximately 1.42 million combined damage per second, with all twelve contributing. This is a real sample against that statue, not a controlled same-boss comparison with the earlier Solomon sample.

Wave four exposed the difference between reacting to a bird and preparing for its spawn. Archer1 was near the inner right platform when Munin awakened and took a captured 20,716-point contact hit. Normal Resurrection restored him, but a further 20,929-point Munin contact killed him again. The native counter reached four deaths and was not reset. The exact movement after resurrection still needs careful interpretation; the captured source is established, while a safe resurrection path cannot be inferred merely from his later position.

Outer positioning now uses the native inter-wave warning: the wave counter is known before the next statues awaken, so lower-platform occupants can move outward while the bird is still dormant. A new regression verifies this without requiring a live bird object. Another regression covers ordinary arrival tolerance: stopping a few pixels short of an outer waypoint must count as arrived, rather than endlessly suppressing combat. The right-side Dark Knight and Hero also wait near their ranged party during Munin instead of crossing away and leaving Hyper Body behind. These are bot movement/target decisions; the event's fifteen-second warning, monster skills and graph physics remain unchanged.

Bishops now decline a Pink-only Resurrection attempt when the corpse's position overlaps a lethal boss body, retaining native range, costs, party restrictions and cooldown. Potion preparation uses the normal 95-percent HP alert in Pink Bean: the default 70-percent alert can stop topping up below the next body hit after a 1/1 attack or halved Ginger Ale recovery during Zombify. Other activities retain their previous threshold. Regressions cover the scoped settings and recovery decisions; live testing continues.


### 2026-10-26 - Wave-four supply failure: attack selection must include real HP costs

The guide-informed attempt ended through the unchanged fifth-death rule in wave four. Both sages and Munin were defeated; Hugin still had roughly 155 million HP in the final pre-exit sample. Spear1's recorded supply fell from 500 Ginger Ales to zero, then he died. There was no clear and no reward pickup. The normal return, resupply and logout completed for all twelve.

The existing raid HP-cost guard checked negative HP-rate effects, but Sacrifice's real cost is applied after damage in CloseRangeDamageHandler as a percentage of damage dealt. It can therefore look like a damage upgrade to the planner without exposing that cost through the guard. Stronger apple-assisted hits and the high HP alert make this a particularly expensive choice. The final attack was not captured, so the pot-exhaustion death is established separately from this source-code explanation of the unsafe available rotation.

Pink Bean now rejects Sacrifice in attack planning, damage-potential comparison and repositioning. A regression verifies refusal before the skill is cast; neither Sacrifice's normal effect nor the player handler was changed. Dark Knights also move onto the outer left firing platform when Hugin is their assigned target, putting the learned Spear Crusher into normal range instead of relying on a costly fallback from the floor. Pink-only readiness now stocks 1,000 Ginger Ales as reserve, plus the approved twenty apples; other crews retain their supplies.

A fresh normal attempt reused IDs 721-732 again. The first wave cleared without deaths. The readiness audit confirmed real apple buffs, Spear Crusher plans and a small initial potion use. This is a new instance after an actual failure, not a reset of the old death counter. The remaining session observes this attempt; a healthy expedition can continue under its unchanged native timer afterward, with the local harness recording the result and ordinary cleanup.


### Pink Bean: let the safe stance end when its hazard ends

The fresh retry reached wave three with no deaths and defeated Hugin. Then everyone hesitated on the old platform: the new outer-stance rule still applied just because it was wave three, so it opposed their crossing toward Rex. I narrowed that rule to a live nearby bird or the native warning before a new wave. The regression test now checks that a defeated bird releases the crossing. The same twelve bots then walked across and resumed damaging Rex without a restart or a death. The longer attempt is still in progress; a complete Pink Bean clear and human-client audit remain pending.


### 2026-10-27 - Four Pink Bean waves clear without a death

The fresh retry reached a new milestone: waves one through four completed with all twelve bots alive and the native death counter at zero. Wave five began normally at about 05:28 UTC, with each party on its own outer platform before the birds appeared. Spear1 still had 943 Ginger Ales and Spear2 had 956. The safer transitions and removal of the damage-scaled self-cost skill held up through the wave that ended the previous attempt.

The two-hour continuation ended observation here rather than discarding a healthy run. The same in-game AI keeps working under the existing 140-minute limit, and the local test harness records the eventual native outcome and normal cleanup. Wave five, the body and rewards remain unverified until that outcome is inspected. The monster-status samples also do not establish client-hazard coverage; a human-client audit is still needed. The build and focused regressions passed. No restart or GitHub push was needed.


### 2026-10-27 - An interrupted run is not a failed run

On returning to Pink Bean, the server was stopped. The final snapshot from the prior night had everyone alive in wave five with zero deaths, but no final outcome or cleanup was recorded. I could not establish why the process stopped, so that attempt remains interrupted rather than won or lost. We started the current packaged build and reused the same twelve characters, resupplied and registered normally. Eighteen remains an option if twelve actually fall short on damage; the existing evidence did not justify adding more people yet. The supplied guides helped with movement between sides, preparing before bird spawns, summon control and watching real reflection status.


### 2026-10-27 - Ariel exposes an overbroad safety approach

The same twelve cleared four waves without a death, then finished the last wave's sages and birds. Ariel was taking damage, but only part of the crew had reached her. The others kept walking on the floor. A captured pathlog showed the normal rope route was available; our safety adapter kept cancelling it by asking them to stand below the final upper-platform destination instead of letting them walk to the actual ropes.

The correction was small: use that floor-first instruction only for the low statue platforms it was designed to protect. The upper platforms now use normal navigation. Tests passed, and the same live crew climbed without a restart or warp. The previously waiting melee fighters joined in and Ariel began losing HP faster. More members would have hidden this movement bug rather than solved it. The real body and rewards were still pending at this checkpoint.


### 2026-10-27 - The Bishop can use Pink Bean's real resurrection room

All five statue waves finally fell and the earned item activated the native reactor. The real Pink Bean spawned. Ariel had cost two lives: Spear1 took fatal contact damage while crossing near the middle, Bishop1 resurrected him, then she died too. The remaining bots kept fighting. We preserved those deaths and their logs rather than resetting the event.

The general PQ behavior told her to lie at the tomb while anyone else was alive. Pink Bean is different in this server: its normal death popup leads to a resurrection room, and the existing portal returns to the same expedition. I checked the map data, event script, player handler and portal before changing the bot decision. A safe available party Resurrection is still preferred; without one, the bot can press OK through the existing shared respawn path and walk through the real portal. Other PQs keep their previous behavior.

Bishop1 returned alive and joined the body fight. All twelve were alive again, with the death counter still two. A separate Ariel route safeguard now directs cross-side support trips beneath the statue rather than through her occupied upper gap. That guard has unit coverage but was installed after Ariel died, so its next live crossing still needs checking. The body and rewards remained the next verification steps.


### 2026-10-27 - Pink Bean: the first complete headless victory

The same twelve finally completed the whole encounter. They registered normally, cleared all five statue waves, let the earned item activate the native reactor, and defeated the real 2.1-billion-HP Pink Bean. The native clear flag was true. Entry to victory took about 105 minutes against the inherited 140-minute limit. Eight actual equipment drops were picked up; the observed meso pile is not counted as collected.

This was not a deathless run. Ariel caused two contact deaths, both kept on the native counter. Spear1 received normal party Resurrection. Bishop1 later used the normal death popup, resurrection room and existing portal after the generic wait policy was corrected. All twelve were alive again and contributing during the body fight. A thirty-second actual-damage sample totaled about 1.22 million damage per second; Shadow Partner, Phoenix, Bahamut and Beholder were present where appropriate. No boss HP, timer, stage or life count was reset to get the clear.

The crew used the normal exit, returned outside, resupplied and logged off. The final audit found zero test fixtures online, no expedition left behind, positive saved HP and unscrolled equipment. Their naturally earned level gains were retained. The server remains available for a human-client audit; nothing was committed or pushed to GitHub.

The guides helped reveal safe side changes, pre-spawn positioning and summon risks. They did not replace local WZ/native rules. Twelve were enough for this headless run under 140 minutes; it is not evidence for a historical one-hour clear. The next audit still needs actual client hazards and the new Ariel cross-side route, which was added after Ariel died in this test. Headless completion and player-client verification remain separate milestones.


### 2026-10-27 - Four Pink Bean parties and a historical one-hour test

After comparing old videos with the twelve-person result, I chose a twenty-four-person trial: me plus twenty-three companions, with a headless leader filling my spot during testing. We reused the original twelve and added only twelve dedicated fixtures. Four real parties each have a Bishop and Dark Knight; two parties work each side. The event timer is now sixty minutes. No stage requirement, boss HP, death count or human prerequisite was removed.

I also asked for Balanced Fury. The local item data corrected my expectation: Ilbis give 27 attack, Crystal Ilbis 29, and Fury 30. Three extra attack helps, but it is not a huge increase alongside a weapon, gear and Apple. The Pink thieves now carry Fury through normal inventory/ammo handling; older stars are removed during preparation.

The larger crew's preflight caught one Bishop and two thieves slightly below the HP equipment goal. We held everyone outside, repeated ordinary gear-roll preparation, and checked again. No base HP or HP washing was used. That check now exists in normal assembly too, with a bounded retry and failure outside the expedition rather than a false ready message. All twenty-four passed and entered the normal encounter. The one-hour result was still pending at this entry.


### 2026-10-27 - Twenty-four finish Pink Bean inside the hour

The larger crew passed: about 52 minutes 51 seconds from the first recorded entry to native victory, with zero deaths and all twenty-four alive. The sixty-minute timer was not extended. They completed the five statue waves, earned-item reactor and real body normally, then made thirteen reward pickups and used the normal exit. That left roughly seven minutes on the original clock.

The fresh Ariel phase also passed without the prior contact deaths. All twenty-four contributed in a thirty-second actual-damage sample against the body, totaling about 2.26 million damage per second. All eight Night Lords had Balanced Fury as their only stars and had used its stock normally. Shadow Partner and the appropriate class summons were present. The party-size increase drove most of the improvement; Fury's local WZ value is 30 attack versus Ilbis' 27, not a huge boost to total attack.

The crew returned, resupplied, saved and logged off. The final audit found no online fixtures or remaining expedition, positive saved HP for all twenty-four and no scrolled equipped gear. Existing characters were reused, natural levels retained, and the ordinary population was left alone. No boss HP, damage, stage or life count was altered to obtain the clear.

This is the headless milestone for a human plus twenty-three bots, not the player-client sign-off. Actual reflection, Seduce and other client-driven hazards still need the human audit; they can affect the remaining seven-minute margin. For this test, scrolling was unnecessary. The server is available for that audit; nothing was committed or pushed.

### Horntail: leaving the party must log off the whole crew

During the human-client audit on October 29, 2026, leaving OuchMyToe's party left three companions idle at the cave entrance. Live logs established concurrent dismissal failures: one companion disbanded the shared party while others still tried to leave its removed world entry. Crew tracking had already been removed, so those failures could not retry through the Horntail lifecycle.

Horntail dismissal now serializes party departures, clears stale references to an already-disbanded party, and keeps crew tracking until the character has saved. Normal party-leave and client-disconnect paths remain in use. The focused Horntail suite passed all 19 tests, including stale-party recovery. The fix was applied without restarting; the three confirmed orphan fixtures were saved and disconnected, and the dashboard showed zero remaining Horntail companions. A fresh human party-leave audit is still needed to confirm the full lifecycle end to end.

### Horntail ice breath: recoil must stop at the real map edge

During the October 29 human-client audit, ice breath knocked companions beyond the left platform edge while Nelbot stopped at the wall. Live inspection found two Night Lords at x=0, tens of thousands of pixels below the room. The real room and its leftmost ground begin at x=90. Bot physics had treated FootholdTree's zero-initialized item-drop minimum as the collision limit; on an entirely positive-coordinate map that minimum is not a real foothold.

The bot physics boundary calculation now uses actual cached ground extents, with the mirrored correction for entirely negative-coordinate maps. Shared player/item-drop behavior and knockback strength are unchanged. Navigation graph version 81 invalidates authored trajectories on the next startup because boundary collisions also participate in build-time simulations. The current live physics update did not restart the server or rebuild the autonomous population's caches.

All 82 physics, observed-recoil and Horntail tests passed. A live diagnostic on map 240060000 measured the old boundary at x=0 and the corrected boundary at x=90; a simulated leftward launch from (120,-40) landed at (90,-40). This verifies collision behavior, not a fresh client-observed ice-breath hit. The player authorized returning the two stranded thieves to the entrance. By the guarded recovery check they were already back on valid ground in the same room: HornThief3 at (288,-40), HornThief4 at (344,-40). The helper skipped both, so no recovery warp or healing was performed. How their earlier invalid positions recovered was not established.

### Horntail: safer approach-side fighting and proper raid consumables

During the October 29 client audit, the player reported Dark Knights repeatedly dying on the upper platform behind a preliminary head and a Bishop walking toward the head before death. The exact fatal Dispel/damage sequence was not captured. Earlier live samples had shown valid footholds and attack reach there, but that did not establish a sensible raid position. The audit corrected that overly narrow safety assessment.

Horntail's preliminary-head policy now keeps attackers on the entrance approach side, using the native head bounds and entrance direction rather than a fixed left/right rule. Ranged classes retain additional contact clearance even when their HP might tolerate a hit. Hyper Body regrouping chooses the entrance when chasing a provider or recipient would violate that policy. Resurrection rejects unsafe approaches and delegates only its validated corpse to the ordinary support handler, so that handler cannot select a different out-of-range corpse and walk toward it. These changes do not give immunity, extra weapon reach or movement shortcuts. The main-body route remains to be audited.

The player also authorized Onyx Apples for Horntail. Outside-event preparation now includes twenty per companion, and the registered crew uses the existing best-consumable mode. The active crew received its initial twenty through normal inventory stocking, without healing fallen members or changing boss HP. Initial live inspection exposed the need to extend Pink Bean's stronger-attack-buff replacement policy to Horntail; otherwise weaker class buffs prevented Apple consumption. Final consumption verification is recorded separately below.

The initial positioning, provisioning, revival, Pink Bean strategy and consumable regression suite passed 41 tests. Several bots, including both Bishops, had already fallen during the ongoing first-head attempt before the update; no GM resurrection or successful fresh survival audit is claimed.

Live Apple follow-up: the stronger-buff policy passed the focused consumable regression. Four active-run inventories showed an Apple consumed (twenty down to nineteen); the two surviving Night Lords had native WATK=100/MATK=100 active. The other previously fallen members retained their reserves. By this snapshot nine companions were dead, including both Bishops and both Dark Knights; this compromised attempt is not a post-fix survival success. Restarting the encounter is recommended for a clean client audit. No fallen bot was healed or resurrected by the diagnostic.

### Horntail follow-up: stun and lethal contact are separate from the initial hit

The player reported ordinary head attacks around 2,000-5,000 damage, contact damage above 14,000, and knockback followed by death while stunned. Live corpse positions around x=684-719 were at or within the preliminary head's contact area (left edge x=696, including the character's width). This is consistent with attack -> recoil -> contact, but no timestamped fatal sequence was captured, so neither Dispel nor incorrect recoil direction is established as the exclusive cause.

Code inspection confirms stun deliberately cancels steering/actions while preserving ordinary airborne recoil and collision; freezing stunned characters would hide the danger. Observed-attack recoil currently chooses direction from the monster's position anchor, and this head's anchor is offset from its contact bounds. That direction needs comparison against the human client's same attack before changing it. The earlier positioning update and static tests do not certify full route safety or a fresh client run; no new immunity, forced reposition, damage reduction or revival was applied for this observation.

### Horntail repeat: eleven companions survive, full clear in under an hour

After the first human audit exposed repeated deaths, I requested another full headless run with reliable survival and a roughly one-hour pace. The isolated channel-two test used a twelve-person dedicated crew, normal expedition registration, both preliminary heads, the real crystal and all eight main parts. Eligible fixtures were reused; one missing Dark Knight slot was filled with a dedicated placeholder. No autonomous-population or player-owned character was borrowed. The crew began at level 180 or higher, with ordinary earned AP/SP, unscrolled gear, normal skill/potion supplies and twenty Onyx Apples each.

**The first useful finding was an idle melee crew.** The legal front platform has a short final segment. Sampling it every 35 pixels considered x=548 but skipped its safe endpoint near x=572, which can reach the head through the existing attack checks. Stance enumeration now includes that endpoint; weapon range, physics and launch windows are unchanged. A focused regression covers the missed position. Live native damage accounting then confirmed all twelve members contributing during a thirty-second second-head sample. Both preliminary heads cleared with zero deaths.

**One real contact death exposed route safety.** At 17:38:45 UTC the test leader died to the right main head: 15,175 contact damage with 8,609 HP before the hit, at a reported floor position (19,260). Swept contact can include the path before that position, so the corpse coordinate alone is not a stationary-hitbox diagnosis. The Bishop used normal same-party Resurrection; all twelve were alive again. This was the run's only death. Safe endpoints had not guaranteed a safe route while crossing between raised platforms.

The main-chamber movement adapter now asks native navigation to descend on the outer side, cross on the existing bottom floor below the measured head/wing contact areas, and then climb near the destination. It does not teleport, predict new physics or alter graph windows. A regression covers the staged descent/cross/climb and confirms the policy leaves Pink Bean unchanged. The fix was applied at 17:42:43 UTC without restarting, resetting the event, healing anyone or changing boss HP. No further deaths occurred. This is a completed run with mid-run fixes, not a claim that every change had a fresh full-run verification from entry.

**Native victory was recorded at 18:09:51 UTC (October 29, America/Chicago).** Prepared-crew registration through victory took 2,756 seconds (45m56s); actual main-body spawn through victory took 2,011 seconds (33m31s). All eleven companions survived the entire run, both Bishops remained alive, and all twelve participants were alive at the kill. The headless leader made twenty native reward pickups. The crew used the Crystal of Roots normally, resupplied only after leaving, saved positive HP and logged off. A separate persistence audit found zero test members online, no channel-two Horntail expedition, positive saved HP on all twelve rows, twenty Apples per saved character and no scrolled equipped items. Naturally earned levels were retained.

Focused positioning, endpoint, crossing, revival, consumable, observer and Horntail regressions and packaging passed. Ordinary boss HP, contact damage, the chosen twelve-hour timer, prerequisites, earned stage transitions and potion/cooldown rules remained intact. No GM heal/revive, death reset, damage scaling or in-fight supply replenishment was used; the authorized initial Apple grant for the previous human attempt is a separate historical action.

This headless result does not certify client-driven breath, stun, Dispel or recoil direction. The next step is a fresh human-client audit of the current code, concentrating on contact avoidance, stunned knockback and support regrouping. Nothing was committed or pushed for this repeat; publication follows the client audit.

### Horntail originals should remain reusable after a Leafre return

During the next client assembly, I noticed suffixes three, four and five and asked where the original companions had gone. A live ownership check confirmed the headless channel-two crew had logged off; the newly online crew belonged to Nelbot on channel one. The original suffix-one/two fixtures still existed, offline and registered to the Horntail pool, but their saved map was Leafre (240000000), the native preliminary-room return destination. The pool's saved-map filter omitted that return map and therefore skipped them.

The Horntail-only reuse query now includes Leafre. Existing per-class suffix ordering prefers the originals when they are offline and otherwise eligible; managed population bots and player-owned characters remain excluded. The build passed, the filter was applied live without restarting, and a read-only eligibility query confirmed the original Spear, Sword, Thief, Archer and Bishop fixtures are included. The current party was not swapped, renamed or dismissed. After dismissing that crew, the next assembly can reuse the originals; a fresh assembly itself was left to the player's next attempt.

### Horntail readiness should be announced once per room

During the client audit, the same Hyper Body/Magic Guard readiness notice appeared four times. Multiple companion ticks could pass the shared set's contains check before any inserted the room key, then each sent the notice. The send now depends on the atomic result of adding that key: only the first successful insertion announces readiness. Readiness checks and actual buffs are unchanged. The build passed and the notice-only class update was applied live without restarting or clearing existing notice keys. The shared helper also prevents the same duplicate in Pink Bean. Fresh client observation remains the final check.

### Human audit: Sword1's death was lethal tail contact

The next human-client audit reported HornSword1 dying despite the eleven companions surviving the preceding headless clear. The fatal-contact record at 14:32:46 America/Chicago identifies tail 8810009, damage 25,116, prior HP 16,600 and position (215,260). His later buffed maximum was about 20,600, below that single hit; potion timing cannot make that hit survivable. Sword1 was alive again when inspected, but the recovery method was not established by this investigation.

The live tail contact rectangle covered x=191..498 and y=120..262, including that floor position. The floor-crossing adapter added during the repeat handles elevated head/wing hazards but does not distinguish a living floor-level tail. That is an established tactical gap. Without a movement capture from the fatal moment, whether the initiating movement was combat repositioning, buff regrouping or knockback remains uncertain. No recoil-direction change, damage reduction, forced revival or new gameplay fix was applied during this investigation. The next correction should account for the living tail before permitting a floor crossing; the prior headless clear remains evidence of one run, not proof of universally safe movement.

### Living-tail floor-crossing guard and Archer1's separate death

I authorized a final tactical fix without committing to another Horntail client audit, because Pink Bean still needed testing and my time was limited. The main-chamber movement adapter now checks the living tail's actual contact bounds with a conservative horizontal clearance. If a requested movement corridor would take a currently safe bot through that area, it holds its current side. Normal combat can still select and attack reachable parts. Same-side approaches remain available, and ordinary floor crossing resumes once the tail is dead. This is encounter movement intent, not a new physics simulation, teleport or knockback immunity; it cannot guarantee survival after external recoil puts a character in danger.

All 37 focused crossing, positioning, Horntail and Pink Bean strategy tests passed, and packaging succeeded. The guard was applied live at 19:39:31 UTC without restarting or altering the active event. It has not received a fresh full-run or human-client verification. The player's request not to repeat the audit is preserved rather than converting unit-test success into a client sign-off.

During implementation, Archer1 also died. This was a different cause: the observed-attack record at 14:36:01 America/Chicago identifies middle head 8810003, attack index 2, 5,443 damage against 5,382 HP at (572,200). He was at his then-current maximum; the live character maximum was 5,382 rather than the roughly 8,600 previously observed with Hyper Body. Missing Hyper Body left him vulnerable to a single ordinary hit. Whether Dispel or expiration removed it was not captured. The tail movement guard does not fix protection loss; that remains a factual limitation of this client audit, and no HP grant, damage reduction or forced resurrection was applied.

### Human Horntail audit: HP readiness, Seduce recovery and dangerous hand timing

The client run continued to lose companions after the initial movement changes. I asked for everyone to have more than 5,500 HP without Hyper Body, preferably through the same Godly HP gear used for Pink Bean. Archer1's established one-hit death was 5,443 against 5,382 HP without Hyper Body; the later recorded thief deaths were likewise around 5,200-5,500 against reduced or unbuffed HP. I also observed a roughly 10,000 damage number, but the investigation did not establish that number as a normal raw hit: Horntail has native HP/MP-to-one attacks that can display large losses. The minimum is preparation against the recorded unbuffed one-hit gap, not immunity to every attack.

**The far-left deaths were a separate failure mode.** Spear1 died to 3,155 damage with only 28 HP and zero MP remaining; Bishop1 died to a 2,010 hand attack with 982 HP despite Magic Guard being active. Local WZ identifies that hand attack's 3,000 MP burn, and the inherited normal damage handler bypasses Magic Guard when MP attack damage is nonzero. Low HP/MP plus forced Seduce movement is consistent with the reported sequence, but the exact disease state at those old fatal timestamps was not captured and is not reconstructed as certainty.

**Hero's Will was learned but the forced-Seduce path bypassed the ordinary support loop.** Horntail bots now attempt their own learned Hero's Will through the shared SPECIAL_MOVE adapter after three visible seconds. Native MP costs, learned levels, stun/seal restrictions and cooldowns apply. The local level-five Bishop skill costs 30 MP and has a 360-second cooldown. It is an emergency escape, not a permanent answer to repeated Seduce. Successful removal resumes normal carried-potion use. Bishops also prioritize reaching a safe normal Heal stance for low-HP seduced party members, using the existing reachability/skill-range selection and Heal implementation. Potions remain blocked during Seduce; there is no direct disease clear or HP setter. Horntail now uses the existing 95% HP potion threshold already used for Pink Bean. The generic support packet dispatcher was extracted into BotSupportCast so ordinary buffs and the emergency action share the player handler path.

**Healthy hands should not be driven into mass Seduce while heads remain.** Native hand skills 128/1-3 target one player on a 180-second interval; levels 4/5 target ten players for ten seconds on a 60-second interval once the hand is below 30% HP. The previous reachable-part order could lower both hands early. Horntail tactics now defer healthy hands while any head lives, reject whole attacks whose collateral would hit deferred hands, and normally dismiss attacking summons if they would hit one. Contact damage from those hands remains enabled. If a hand is already at or below 30%, bots may finish it instead of leaving the mass-Seduce source alive. Boss data, target counts, cooldowns and Seduce duration were not modified. This is a tactical correction supported by WZ and code; it does not claim a timestamped trace proved every observed Seduce came from those mass skills.

**The first gear check was insufficient, so preparation was improved rather than putting AP into HP.** A test Bishop initially reached only 4,663 unbuffed HP even though its equipment already had real Godly rolls. Pink Bean's Bishop goal had been 6,000 with Hyper Body (about 3,750 without it), whereas this client finding needs an unbuffed minimum. The HP preparation catalog had omitted new shields and the normal fourth-job Master Adventurer medal (quest 29903, item 1142110), and checked mage equipment requirements before allowing the existing ordinary LUK allocation to unlock higher-level gear. Candidate ranking also considered base HP instead of the configured Godly HP potential. These omissions were corrected. Preparation still samples the real 20% Godly gate and configured stat ranges, grants actual unscrolled results, and uses the existing optimizer/equip handlers. No AP went into HP, and no HP/MP points were recycled.

The target remains 6,000 where gear permits; the required minimum is 5,501. A bounded preparation that cannot meet the minimum refuses the weak crew instead of silently claiming readiness. A real outside-event check then prepared and saved all eleven original companions: Spear1 12,833; Bishop1 5,604; Archer1 7,175; Thief1 6,933; Sword1 12,875; Bishop2 5,540; Spear2 12,845; Archer2 7,362; Thief2 7,419; Sword2 12,840; Thief3 7,553 unbuffed HP. All equipped scroll levels were zero, and the fixtures were logged off afterward. These measurements prove actual preparation, not a new raid clear. HP gear was not injected into the active human encounter.

The final focused tactics, Seduce, HP optimizer, Horntail, combat and Pink Bean strategy suite passed 139 tests and packaging succeeded. Earlier potion/revival checks also passed. Live class updates required no server restart. One diagnostic briefly changed the human observer's potion bindings; their original saved bindings were restored, and production recovery behavior remains bot-scoped.

This human run exposed failures that the earlier 45m56s headless clear did not exercise. I will not repeat the client audit because my remaining time is for Pink Bean. The latest HP/tactics/recovery changes have tests and real preparation checks, but no fresh full client or headless clear. They must not be published as a newly deathless or fully human-verified Horntail raid. No commit or push was made during these fixes.

### Reference integration ready for the next Horntail audit

After comparing the supplied MapleRoyals discussion with local data, I requested that any missing tactics be integrated and said I would begin a fresh audit when ready. This supersedes the earlier plan not to repeat the client test; it does not erase that earlier decision or certify a new result.

Inspection found that the broad hands-last filter was present, but the soft priority still preferred legs before tail, and healthy hands became available after the heads even if wings or other main parts remained. The preference is now tail, legs, wings, heads, hands. Healthy hands remain deferred while any other main part lives; already-below-30% hands may still be finished to remove an active mass-Seduce source. Direct and collateral attack safeguards, real summon cancellation and unchanged contact hazards remain in place. This is reachable-part prioritization rather than granting every class access to a selected part.

The party-support gap was proactive positioning. Bishops now choose an actively fighting ranged member of their own party and, when out of normal Heal range, approach the nearest reachable safe Heal stance before waiting for an emergency. Low-HP seduced allies retain higher healing priority. Dark Knights regroup for missing Hyper Body using the existing reachable casting-stance selection, instead of unnecessarily walking onto a teammate's exact position. Normal learned skill bounds, costs, cooldowns, movement and the living-tail crossing guard remain authoritative; no fixed-distance heal or remote buff is granted.

The main-chamber introduction now explains the preferred part order, why healthy hands are saved, and staying close for Hyper Body/healing. The existing above-5,500 unbuffed HP gear minimum, original-fixture reuse, three-second Hero's Will response, 95% HP potion threshold and normal Seduce restrictions remain enabled. Native Dispel/Seduce thresholds were already present and were not rewritten to copy another private server. A guaranteed first-entrant tank was not introduced; native range, entry order and Holy Shield continue to influence targeting.

All 143 focused tactics, cohesion, Seduce, HP, Horntail, revival, combat and Pink Bean strategy regressions and packaging passed. A live class update verified the active priorities without restarting or spawning a new raid. The eleven originals remain prepared and offline for the player to assemble. This final integration is ready for the requested client audit; no new full-run clear, successful client audit, commit or push is claimed.

### Navigation warmup diagnostics stay in the server log

During the next Horntail audit, the pink navigation-cache warmup notice appeared again. The old suppression covered event instances but not every location where a companion can follow its leader. Replaced the player message at its source with the existing throttled DEBUG log. Cache construction, fallback movement, and ordinary stage guidance are unchanged. Verification: packaged successfully and applied the method change to the running server without restarting; client confirmation remains pending.

### Horntail ranged firing positions and a second tail-contact death

During the next client audit, I reported HornThief2 firing from an implausible distance, HornThief3 apparently shooting an arm across the body, and HornSpear1 walking into the tail. Live inspection exposed the generic 600px projectile rectangle (400px plus Keen Eyes) accepting tiny intersections with a large boss hitbox. An earlier second-head position at (431,-40) could overlap its hitbox by only about nine pixels. That demonstrates the model's permissiveness, not that the real client permits the shot. The later HornThief3 snapshot was beside the left hand, without a current target; it does not establish which hand the earlier visible shot hit.

Added a Horntail-only firing-position policy for claws, bows, crossbows, and guns: side heads/hands require the same side, head/hand shots use a conservative 350px approach envelope and a narrow band near projectile launch height. Normal skill hitboxes still apply. This is deliberately stricter positioning, not a newly measured client range formula. Candidate stances, target collection, the reach gate, and final attack dispatch apply it. Melee, magic, and other encounters keep their existing reach; tail/legs/wings retain their previous reach checks.

The fatal log confirms tail contact for HornSpear1: 25,029 damage against 20,532 HP at (583,260). A subsequent snapshot still contained Seduce, but the old fatal log did not capture disease state or the initiating movement, so forced movement versus deliberate routing is not conclusively reconstructed. The movement review independently found that floor-waypoint conversion occurred after the tail hazard check, and airborne combat continued directly toward its cached goal. The guard now checks the converted waypoint, and airborne combat uses the same raid approach adapter. This does not cancel native Seduce/recoil, promise a hazard-free graph path, or grant immunity. Future fatal-contact diagnostics include Seduce, Stun, and the previous touch-check position.

Verification: 206 focused tests passed, including captured-distance, same-side, horizontal-height, and converted-waypoint regressions. Packaged and applied live without restarting or changing current HP, reviving casualties, or replacing the crew. Live diagnostics confirmed HornThief3 could not target either hand from his then-current floor position. Client recheck and full encounter result remain pending.

### Hero's Will is an emergency response

I asked for Hero's Will within one second of Seduce because waiting three seconds could already carry someone into lethal contact. Changed Horntail's reaction threshold to 500ms, allowing a normal scheduler tick to attempt the learned skill within that first second when casting is legal. The earlier three-second visibility delay remains appropriate to ordinary ailment cures, not this emergency escape. Native MP cost, six-minute level-five cooldown, and existing cast restrictions still apply; no cooldown resets or Seduce immunity. Two focused tests passed for the reaction boundary and cost/cooldown rejection. Packaged and applied live without restarting; next client Seduce event remains the visual confirmation.

### Client confirmation: Horntail forced movement and poison ticks

During the continuing Horntail audit, I confirmed that knockback, Seduce, and damage-over-time effects were working visibly on the crew. Poison appeared to deal approximately 2,000 HP per tick to everyone observed. This is client-observed behavior, not a claim that every class, resistance, or Horntail attack has identical damage. No gameplay change was needed for this observation. The full raid outcome and recent firing-position changes remain separate audit checks.

### Horntail Bishop rope loop: competing support and combat goals

I noticed HornBishop2 repeating a rope climb. The live pathlog and a short state trace confirmed right-rope ascent/descent cycles while healing approach targets alternated with older cached combat stances. Once an ally entered Heal range mid-climb, support returned control to combat; combat could replace the still-active approach with its old stance. Support itself also reselected different stances as the transient range changed, so both handoff directions require the same stable intent. The exact version-81 graph was present, so changing rope physics, widening windows, or warping the Bishop would address the wrong layer.

Horntail support and combat now preserve an active movement target while climbing or airborne, rather than resurrecting its cached stance during that handoff. Ordinary navigation still executes the route, with the tail guard intact. Regression coverage exercises both rope and airborne handoffs. Ten focused tests passed and the packaged change was applied live without restart; subsequent observations are recorded separately rather than assuming a full raid clear.

Live follow-up after both handoff guards: HornBishop2 left the rope and was grounded, progressing across the floor through ordinary navigation (including his learned movement skill). The sampled post-fix trace no longer showed the repeated rope cycle. This is short live recovery evidence, not a completed-raid or permanent-stability claim.

### Horntail audit: strict hands last, real potion counts, and floor staging oscillation

I reported Thief2 asking for HP potions, more mass Seduce, arm attacks with heads alive, and looping melee. Live inventories showed Thief2 had 420 Ginger Ales, so the quoted chat was not reliable supply telemetry. Sword1, Sword2 and Spear2 actually had zero; the current attempt needs ordinary player trades to sustain them. No mid-instance supplies were awarded. Horntail's outside-event preparation now requests 2,000 Ginger Ales rather than 500, leaving the shared Pink Bean preparation default unchanged. Actual insertion remains subject to normal inventory capacity.

The right hand was already at 19,082,537 / 230,000,000 HP while heads remained. The previous policy deliberately allowed finishing already-enraged hands below 30%; additionally, the Night Lord Triple Throw fast path returned before the general candidate-policy guard, and final attack dispatch did not recheck the hand policy. Changed the rule to defer both hands at every HP level until heads, wings, legs and tail are dead. Added a primary planning guard before shortcut paths and a whole-plan final dispatch guard for cached/collateral attacks. Existing summon policy consumes the same stricter filter. This prevents further bot hand damage while other main parts remain; it cannot undo mass Seduce already enabled in this instance, stop a human from damaging a hand, or suppress native single-target Seduce.

Melee traces alternated (392,260) floor staging and (392,-160) upper goals as their x crossed a 35px threshold, while the actual rope approach needed movement toward x451/510. Floor staging was cancelling its own approach. It now applies to opposite-side transitions only; a same-side upper destination remains intact for native graph navigation. No warp, altered rope physics, graph rebuild, or immunity was added.

Verification: 140 focused tests passed across the tactics, combat, preparation, movement, support, Pink Bean strategy, and two new shortcut/final-dispatch regressions. Both live hands were rejected by the policy and produced no Night Lord attack plan, including the right hand already below 30%. Applied without restart; current mass Seduce and depleted inventories were left unchanged.

### Explicitly authorized mid-run potion refill

As the client audit approached a wipe under recurring mass Seduce, I explicitly requested another 1,000 Ginger Ales for each companion. Added them through normal inventory insertion to all eleven registered Horntail bots, including casualties, and verified each before/after count increased by 1,000. No human inventory, boss HP, death state, disease, or cooldown was changed. This is an authorized test intervention and must not be described as an uninterrupted self-sufficient clear. The already-enraged hand can continue native mass Seduce despite the new hands-last targeting policy.

### Spear2 death: Hero's Will had already been used

I asked why HornSpear2 did not escape Seduce with Hero's Will. Runtime inspection confirmed Dark Knight Hero's Will level five with a native cooldown starting at 4:31:46 PM America/Chicago, October 29, 2026, lasting 360,000ms. The fatal contact at 4:34:35 PM occurred within that cooldown: legs (8810008) dealt 10,735 damage against 1,841 HP while Seduce was active and Stun was not. The earliest legal reuse was 4:37:46 PM. This establishes that a prior cast was recorded and reuse was unavailable at death; it does not independently reconstruct every preceding hit or confirm the prior cast's exact visible cure. No cooldown reset, revival, or new gameplay change was made.

### Why Spear2 was not resurrected immediately

The live check found both Bishops alive, learned Resurrection level ten, able to afford it, and on cooldown. Spear2 belongs to Bishop2's native six-person party; Bishop1 cannot resurrect across expedition parties. Bishop2's current cooldown expires at approximately 4:55:16 PM America/Chicago, October 29, 2026. Local WZ level-ten Resurrection costs 40 MP and has a 1,800-second (30-minute) cooldown. Snapshot cooldown start/length may represent restored remaining cooldown, so they are not proof of a new cast at login.

Normal resurrection also requires the same map/party, a legal grounded cast without rope/air/action lock or disabling ailments, a safe caster and revival destination, and actual skill range (or a safe reachable approach). At the sampled position Spear2's corpse was safe but out of immediate range; a safe approach stance existed. Cooldown, not lack of a learned skill or MP, was the immediate blocker. No cooldown reset or revival was performed.

### Fresh raid preparation must not inherit previous cooldowns

I pointed out that the current run was only about six minutes old, so the Bishops should not have entered with Resurrection still cooling from earlier attempts. The prior explanation identified the immediate blocker but missed the preparation defect. Added a bot-only, outside-event preparation reset through Character's native cooldown API, preserving the Battleship hull pseudo-cooldown. Zakum retry restocking and shared Horntail/Pink Bean preparation call it; real skill cooldowns earned during the active fight remain unchanged, and human clients/active-event preparation are rejected. This includes fresh Hero's Will and Resurrection availability at prepared entry, not unlimited reuse during combat.

For this already-running audit, matched and removed only the two specifically observed inherited Bishop Resurrection cooldown records by character, skill, start time, and remaining duration. Did not clear new in-fight cooldowns or cast Resurrection directly. This is an explicit correction of stale entry state and an audit intervention. Both stale records were cleared; normal bot logic decides subsequent casts.

Verification: 54 focused tests passed, including bot-only/outside-event guards and existing raid preparation coverage; packaged and applied live without restart. A broader test exposed an older assertion that Horntail still used the ordinary 70% HP potion threshold. Updated that test to reflect the already-implemented 95% Horntail/Pink Bean threshold and separately verify the ordinary-map default; no new potion behavior changed in this fix.

Live follow-up: Bishop2 naturally cast Resurrection at 4:42:21 PM after the stale cooldown was removed, acquired the normal new 30-minute cooldown, and Spear2 was alive at the next check. No direct revival command was used.

### Idle thieves: projectile retargeting contradicted hands-last selection

I noticed that the thieves were not attacking. Live checks showed ample stars/MP, no disabling ailments, safe firing positions, and usable Triple Throw plans against the wings. The captured plan revealed the contradiction: a request to attack wings (8810007) actually contained the protected left hand (8810005). `resolveEffectivePrimary` searched the generic projectile rectangle using monster-anchor distance; Horntail's parts all share the same anchor, so iteration order could substitute a hand. The newly added final hands-last guard correctly rejected that substituted plan, but the combat loop then repeated it and appeared idle. The earlier missing guard had allowed this same substitution to damage hands.

Projectile primary selection now respects the hand policy and ranged firing-position policy before choosing a replacement. Final/collateral safeguards stay enabled; this is a target-selection correction, not an increase in reach, damage, or immunity. A regression uses the captured shared-anchor hand/wings geometry.

Verification: 105 focused tests passed and the change was applied live without restart. All three thieves then sent normal Triple Throw attacks to the wings with real HP loss and 20 MP consumption per sampled attack. Thief1 sampled 135,889,151 -> 135,745,110 HP, Thief2 136,199,512 -> 136,103,467, and Thief3 136,199,512 -> 135,912,430 (concurrent combat means these snapshots are not isolated damage benchmarks). Full raid outcome remains pending.

### Bishops' second platform loop and a client exit without an error

The next client audit again showed both Bishops cycling between platforms. The earlier movement-intent fix preserved an individual jump/climb, but did not prevent completed combat approaches from leaving native Heal range. Captured combat stance (-302,80) excluded a ranged teammate at y=-160 from Heal, while support stance (-357,20) covered that teammate. Finishing each movement still led to a new conflicting movement in the opposite direction. By the next snapshot the phase had advanced and both Bishops were attacking the left head; that recovery did not prove the wing-phase conflict was fixed.

Added one shared Horntail healing-anchor selection for support and combat. Bishop combat stance candidates, better-reach reposition points, and cached stance reuse must retain native Heal coverage of the chosen active ranged teammate. Normal safety/reach/navigation remain in force, and no-position-suitable means waiting rather than manufacturing range. The 34-test focused run passed, including the captured platform/Heal geometry and existing movement/support/Pink Bean coverage. Packaged and applied live, but the client had already disconnected, so a fresh live wing-phase check remains pending.

During this work, MapleStory closed without an error message. The server remained online; at 4:51:27 PM America/Chicago on October 29, 2026, it recorded NelBot's socket connection reset. The client process was gone and no matching recent Windows Application Error/WER entry was found. The healing-anchor update was still building and had not been applied when the client exited. This timing excludes that particular not-yet-loaded change, not all earlier changes or packets; the crash cause is unresolved. No new raid or restart was initiated, and findings were retained so the audit does not need to be repeated to preserve its history.

Post-disconnect cleanup: one registered HornSword2 remained online after the owner left. Saved and disconnected only that companion through the existing dismissal path; the subsequent live listing showed no Horn crew online. No heal/revival or autonomous-population change was performed.

### Pink Bean resilience comparison exposed a missing client-attack hook

I noticed Pink Bean's crew seemed more resilient than Horntail's and asked whether its gear/setup could be reused. I also reported Rex's lightning apparently hitting only my character. Source inspection found a real omission: `BotMobAttackObserver` recognized Pink Bean/statue attacks and its tick accepted Pink Bean instances, but `BotClient.sendPacket` never forwarded Pink Bean-instance packets to it. Thus client-driven attack collision reports were missing for this raid, despite the downstream support existing. This invalidates using the earlier headless deathless clear as proof that the crew survives real client attacks.

Added Pink Bean to the existing BotClient observer gate, with a regression for forwarding its packets. No boss damage, range, timer, human handler, or immunity was changed. The native observed attack still determines affected bots through its WZ area, normal hit roll, defense, invulnerability, and shared TakeDamageHandler. Forty-three focused tests passed and the hook was applied live without restarting. This verifies wiring, not every Pink Bean attack shape or a completed client audit.

The preparation comparison also matters: Pink Bean has twenty-four total participants versus Horntail's twelve. Both use shared level-180 preparation and ordinary earned AP/SP/unscrolled gear, but Pink Bean's ranged HP goal is 12,000 with Hyper Body while Horntail's target is 6,000 unbuffed (with a bounded minimum fallback). Pink Bean Bishops target only 6,000 buffed HP; Horntail Bishops receive the higher unbuffed target. Live Pink Bean ranged members sampled around 12,020-12,307 buffed HP and Bishops around 6,033-6,361. Wholesale copying therefore does not make every Horntail class more resilient. No active equipment or crew swap was performed.

### Pink Bean client confirmation after enabling real attacks

After the missing packet-forwarding gate was enabled, I confirmed that attacks were landing on the companions, several were suffering or dying, and a Bishop resurrected a casualty. This visually confirms that the observer correction affects the live encounter and that ordinary Resurrection can operate. It also exposes a survival problem that the earlier headless result did not exercise. Pink Bean is not human-client verified or established deathless; survival, damage calibration, positioning, and recovery need assessment with the observer enabled. No new invulnerability, damage reduction, forced revival, or equipment grant was made in response to this report.

Fatal-log evidence from this follow-up: PinkArcher4 took 16,587 Solomon contact damage against 12,190 HP while Stunned; PinkThief6 took 20,732 Hugin contact damage against 12,120 HP; PinkArcher2 took 20,528 Hugin contact damage against 12,188 HP. These establish lethal body-contact hits; only the first explicitly records Stun, and the full initiating movement sequence is not yet reconstructed.

### Pink Bean expedition ended at the inherited five-death limit

I reported PinkSword2 repeatedly appearing to warp onto a head, then being expelled with the "Too many players have fallen" message. The exact message comes from inherited `PinkBeanBattle.js.playerDead`: `fallenPlayers` increases on every death, warns at three/four, and ends the expedition at five cumulative deaths. Resurrection does not decrement that counter. `end` exits every registered player and disposes the instance. This is the local server rule; it is not independently established here as an original-GMS mechanic. The threshold was not changed.

PinkSword2's abnormal movement remains unverified. The live check was already outside at Forgotten Twilight, and there was no saved PinkSword2 pathlog for the reported moment. The raid-specific movement adapters inspected do not intentionally warp him onto a statue; that absence alone does not exclude a lower-level movement/packet problem. Do not claim this symptom fixed, or invent a forced reposition to conceal it. This run failed after real observed attacks were enabled and does not establish Pink Bean survival readiness.


### October 29: test the attacks that the earlier headless run missed

The human audits exposed a gap that a clean headless clear had hidden: Pink Bean's client-driven attacks were not being forwarded to bot observers. Once they landed, both raids needed stronger preparation and safer tactics. We kept Horntail at twelve participants and Pink Bean at twenty-four, preserving the larger historical roster as a reference instead of changing several variables at once.

I approved normal HP scrolling if ordinary Godly gear rolls were not enough. Each scroll now goes through the native player handler, including failed slots and normal item consumption. No HP washing was used. Ring pruning was also discarding useful second, third and fourth rings; keeping legal copies closed some readiness gaps. The final few Bishop HP points took real retries. When a bag filled, old equipment was preserved in account storage, not erased.

The new tests use native monster-controller input so lightning, pillars and other supported attacks do not disappear simply because no human client is present. This is simulated client input, not another human audit. Horntail keeps hands until other parts are gone; Pink Bean's fourth wave starts with Rex and Munin on the right, with melee waiting safely for Munin. Full-run results remain pending at this checkpoint.


The first retest caught a second hidden distinction between a client audit and a bot-only fight: the map deliberately skips visual broadcasts when nobody is watching. Monster movement packets were caught in that optimization, although the bots need them as attack input. The pending-cast probe made the gap visible. We preserved that input for Horntail and Pink Bean, then saw actual attack deaths instead of trusting a low potion count and a clean-looking run. Pink Bean failed its normal fifth-death limit in wave four. That failure led to a missing Hero's Will gate and a stale pre-wave positioning rule; a fresh attempt started after both were corrected. The early, partially exercised run is not a success claim.


Pink Bean then taught us why a safe destination is not enough. Ranged fighters could be knocked off a middle platform toward a sage, or take a diagonal jump through one while heading to a safe firing spot. Keeping everyone on the floor was safer but left ranged classes unable to reach the raised target, so that experiment was rejected. We kept valid ground attacks for melee and used the inner top platform for ranged attacks, with ordinary rope approaches and outer descents for the birds.

One apparent contact death also revealed a stale collision sample: during invulnerability the last checked position stayed behind, and the next sweep could span the whole jump around the statue. Updating the sample during those ticks removes that false rectangle while preserving real body collisions. These were failures with useful evidence, not extra verified clears. The fifth-death limit and boss attacks stayed intact throughout.


### October 29 results: a qualified Horntail clear and a useful Pink Bean failure

Horntail finished in 68m58s, with 50m09s in the main chamber. Sword1 died twice; normal same-party Resurrection brought him back both times, including waiting out the thirty-minute cooldown for the second recovery. All twelve were alive for victory and nineteen reward pickups were recorded. The hands stayed untouched until the other parts were gone; mass Seduce still fired on the last low-health hand without causing another death. Normal exit, resupply, saving and logout followed. The early broadcast gap means this remains a qualified headless clear, not proof that the entire crew experienced every attack from entry.

Pink Bean's final test made it through the first three waves with one cumulative death and everyone alive again. The actual-damage ledger caught three Dark Knights doing no damage in one sample: the candidate search missed the raised sage and the cached preferred attack was the deliberately excluded Sacrifice. Valid ground candidates plus the learned weapon-matched Crusher restored their contribution. Choosing a safe destination also had to remain a requirement after arrival; otherwise bots stopped at the first hittable spot near a platform edge and gave up their recoil room.

Wave four still ended the run at five cumulative deaths after about 25 minutes. The clear evidence was Seduce, HP/MP reduced to one, then an ordinary attack. Bigger HP gear cannot fix that combination by itself. We stopped before inventing a meaning for Munin's higher Seduce modes or pretending the test controller reproduces the client's yellow-orb activation rules. A short client check is the next useful piece of evidence. Both original crews were saved, replenished and logged off; the autonomous population was left alone.

### October 29 continuation: stay outside the dangerous bird's reach

The previous failure was Seduce followed by HP/MP-to-one and an ordinary hit. More HP alone would not solve that. We used Munin's actual native effect area to choose an outer-floor firing position, keeping ordinary walking, jumping and attack reach. Night Lords can jump and throw; Bowmasters cannot shoot airborne in this implementation, so they keep legal ground attacks and support instead. Heal now checks whether a Zombified party member is actually inside its healing area, rather than refusing a safe cast because someone is on a distant platform.

I approved the native maximum of thirty participants. The original twenty-four remain, joined by one party with its own Bishop and Dark Knight. All thirty passed preparation, and the new hazard-enabled run started with the same one-hour timer and five-death limit. Its result will be recorded after the run; this is not another premature survival claim.

The first thirty-person test found a different failure before Munin: one unbuffed Night Lord died to Rex, then three ranged fighters crossed Rex's still-living body while staging for the next wave. Normal recovery brought everyone back, but four cumulative deaths left little margin. We exited normally and kept that attempt as diagnostic evidence. The new route descends through the rope-side gap first; Hyper Body recovery lets the caster approach a safe casting stance while unprotected members wait safely. A fresh run started with both changes present from entry. Native damage tracing had already confirmed that the outer-floor Night Lord jump shots really reached Munin.

A second diagnostic attempt showed why a fast Hyper Body rebuff was not enough: Rex could hit the top platform before the caster could climb into range. Four members died in the same attack. We tried a higher unbuffed gear target, found its ordinary scrolling limits, and checked actual equipped HP against the incoming-hit model instead of quietly granting stats. The final ranged minimum is 8,400; the prepared crew reached 8,428-8,638, above each member's calculated maximum Rex hit. Pink now consumes a normal potion at 99 percent HP so small poison losses do not erase that margin. Unused gear stays in inventory or native account storage.

The body also gets a normal response to its visible Seduce-column warning: after a short reaction delay, walk to a nearby gap on the same platform if possible. No warning means no advance knowledge; an unreachable gap still means taking the hit. The final fresh test began with these changes ready for their relevant phases. Its outcome remains to be recorded.

The first three waves then passed with no deaths. Wave four caught two melee members still crossing inside Munin's range; Seduce and HP/MP-to-one killed both despite their stronger HP gear, and both recovered normally. The lesson was to gather before killing the previous statue, not hope everyone could outrun the next cast. The new rally starts at the last statue's quarter-health point and waits for all living members on their next outer-floor positions before finishing it. We applied it before the next wave transition in the ongoing diagnostic run; the earlier transition still needs a fresh check. The status trace also confirmed that higher Seduce values really occurred, while their exact client movement remains unverified.

### October 29 result: Ariel exposed the next formation problem

The thirty-person integration run cleared the first four statue waves and reached Ariel in the fifth. The new outer-floor rally safely opened wave five without another Seduce cluster. At about 50m16s, Ariel hit five ranged members for roughly 12.8k-13.3k while Hyper Body was absent; their HP before the hits was around 8.5k. The inherited expedition-wide death cutoff ended the run. Seven distinct deaths were observed in total, including the earlier two recovered melee casualties. Ariel still had about 359 million HP. The real body was not reached, and no victory or reward claim is made.

I then supplied another 2009-video observation: a level-187 Night Lord at about 10.5k HP with Hyper Body, a sixty-minute timer, and more than five deaths across the expedition. We replaced the inherited aggregate cutoff with my chosen five-lives-per-character rule. A character's fifth death now leaves them watching until clear/end; ordinary Resurrection, Wheels and return-room entry cannot bypass it. Other participants keep fighting. Native timer, boss stats, earned-item activation and human requirements stay unchanged. Tests exercise independent counters, whole-crew exhaustion, recovery guards and native Resurrection refusal. The exact historical five-per-person claim is not independently established by this observation alone.

I also approved normal weapon scrolling. All thirty existing weapons received a native non-cursed scrolling pass: 210 slots consumed, including ordinary failures; physical attack gains were 6-14 and magic attack gains 6-14. Failed slots were not restored. This preparation happened after the failed raid, so it is not credited with an untested clear. The original fixtures were saved and logged off again. The next combat task is proactive Dark Knight proximity during Ariel's Dispel, followed by a fresh raid with the final life rule and scrolled weapons. Stronger gear alone must not replace that formation fix.

Final checks: focused Pink Bean, Horntail and Zakum regressions and packaging passed. The per-character event setup/death functions were refreshed and read back on all three channels without restarting. The test controller was stopped. The final audit found all forty-two Horntail/Pink fixtures saved with positive HP, none online, and no Horntail or Pink test expedition on channel two. Weapon preparation succeeded for all thirty Pink weapons. No fresh raid with the new life rule and scrolled weapons was claimed, and nothing was committed or pushed.

### October 29 continuation: keep protection close before Dispel

Ariel's last batch of deaths showed that chasing Hyper Body after Dispel was too late. We changed the final-phase formation so each Dark Knight stays near its party's ranged anchor before the buff is removed. The same native Hyper Body rectangle bounds the party positions; ordinary movement and actual attack reach still decide what each class can do. The change is scoped to Pink's Ariel/body phases, leaving the audited PQs and Horntail positions alone.

Repeat preparation exposed full bags trying to accept new sidegrades before scoring their already-owned HP set. Registering Pink's HP goal earlier let those existing sets remain useful. One fixture needed paid native storage expansion and deposits; unused gear was kept, not erased. The original thirty fixtures and their scrolled weapons were reused.

The first live counter query caught another useful testing lesson: the real event API throws for a missing integer property, while our mock had returned zero. We added entry initialization and safe missing-counter reads, preserving every existing count. The current raid was still deathless when that correction went live. A fresh thirty-person run started with the final-phase formation and native one-hour timer; its result will be recorded after the attempt.

One Bishop died during Hugin's counter transition and recovered normally. The cast timing and native reflection handler pointed to the warning-to-buff interval, although the fatal cause was not independently logged. We added a response to the received counter animation: stop only the matching attack type after a short reaction delay, then continue to respect the native reflection buff. The change went live during wave four, so the ongoing attempt remains an integration test. Focused Pink, Horntail, observer and general combat regressions passed.

Reviewing the victory path caught a full-bag loop before the boss was defeated: the headless leader could keep selecting a drop that its inventory could not hold. We now ask the native inventory check before selecting loot, leaving those items in place and allowing the normal exit. This does not change human-led loot behavior. The full-bag regression passed; a live clear is still needed to verify the complete reward path.

### All statues cleared, then the body caught the crossing route

The next thirty-person attempt cleared all five statue waves and activated the real body through the earned item/reactor. It did not win: the native one-hour timer expired with about 1.74 billion body HP remaining. Twelve deaths were observed, spread across participants; all thirty were alive at the last sample, and nobody had reached five deaths. This did verify that more than five aggregate deaths no longer end the expedition.

During Ariel, damage tracing found thieves waiting on the left while their archer could shoot. Their learned reach was valid, but the old 350-pixel coarse stance estimate excluded nearby platforms inside the new party formation. Using the existing projectile envelope for Pink's search restored their attacks without extending an actual hitbox. Two Dark Knights were also stopping halfway up ropes as soon as they entered buff range. They now finish the committed approach and land before choosing their next action.

The body then punished the late crossing: left-side members came down and walked through Pink Bean while trying to join the right. We moved that coordination earlier. At Ariel's last quarter the raid gathers on the far-right floor while the body has not spawned, then resumes normal attacks. The human-led version explains the wait and includes the player in the readiness check.

The fatal body traces also showed Seduce plus HP/MP-to-one. Reviewing the dodge code found that a bot could reach a gap, return to ordinary formation movement, and walk back into the warning before it landed. We now keep the safe gap through impact while allowing a normal stationary attack when its actual plan can reach. Otherwise the bot waits; it does not return to the formation anchor early. Native attacks still apply if no reachable gap is found. This is a movement-response fix, not immunity or an assumed cure for every possible Seduce death.

The timed-out harness hit an unset property after native disposal and briefly let its crew start another automatic attempt. We cancelled that incidental retry with the normal exit NPC, corrected the harness's end detection, and recorded the original timeout honestly. I then authorized clearing unused inventory. Worn gear, potions, Apples, ammunition and skill rocks were retained; account storage was untouched. The same thirty fixtures began the next native attempt around 2026-10-30T04:59:42Z with the transition fixes active before entry. The full result is still pending.

The next run also exposed a conflict that the earlier successful-looking rally snapshot had missed. At the end of wave three, everyone really was on the right. During wave four's warning, older staging code then sent the left parties back left. Munin caught that crossing. Before it, two thieves had already died to Rex contact during the rally descent; both recovered. Thirteen total deaths had been observed by the left-sage section, with all thirty alive again and the highest individual count at three. We kept those deaths in the record.

The warning now preserves the intended next-wave side instead of undoing the rally. Returnees also stay on their current safe outer side while Munin lives, and support approaches do not cross its active effect box to reach a distant party member or corpse. Native nearby buffs and in-range Resurrection remain available. The focused regressions passed. This fix went into the ongoing fourth wave; it is not retroactive proof that the earlier transition succeeded.

### Overnight result: the body still outlasted the timer

The second native attempt finished its full hour without a clear. All five statue waves and the earned-item reactor worked; the real body still had about 1.87 billion HP at the last sample. Everyone was alive again. The native individual counters recorded eighteen deaths, with nobody above three. Our quarter-second HP poll saw seventeen because one Hero recovered between polls. We used the native counters for the final count.

The early thirteen deaths came from the Rex retreat contact and the warning/return conflict already recorded above. Wave five then opened safely, Ariel passed without another death, and the new pre-body rally moved everyone right before the body appeared. That avoided the first attempt's initial crossing cluster. Five later body deaths were real Seduce/HP-to-one combinations: four Heroes and a Bishop, all recovered. We did not call this reliable completion.

A final live position check caught Bishops waiting on the floor for Hyper Body while their Dark Knights were already committed to the upper ranged formation. The earlier statue waiting rule now yields to regrouping once the final formation is active. The short body observation exercised the change, while a fresh full attempt is still needed. A death-room return still begins on the left, so the initial rally does not certify all later re-entry paths.

The last fatal traces led back to WZ: Pink Bean has visible Stun columns as well as visible Seduce columns. We had only taught the warning response about the latter. Both now use the same ordinary dodge/hold-and-fire behavior. Tests cover both attack indices, actual reach rejection and staying in the gap. This last expansion was made after the timed run and has not yet been tested through another full raid.

I had also asked to clear the spare inventories. The final database check confirmed zero spare EQUIP stacks on all thirty Pink fixtures. Worn gear, recovery items, Apples, ammunition and skill rocks were kept, with normal preparation restoring required supplies. All forty-two Horntail/Pink fixtures were saved with positive HP and logged off; the test expeditions and controller were stopped. The server and autonomous population stayed running. No publication or commit was made. Full completion inside the native hour is still pending.

## October 30: build guides exposed a readiness gap

I brought Night Lord, Bowmaster, Dark Knight and Bishop guides to the equipment review. All four exposed their spoiler text; their recommendations still needed checking against Otono's native items and rules. We saved the references in the [raid equipment notes](kb/kb_bot_raid_equipment.md) for the future WhereLevel class-guide work.

The saved characters revealed a more concrete problem than a missing necklace: several level-180 Night Lords held claws with only 42-57 weapon attack, and many had 160 base DEX. Bishops had 165 base LUK. The preparation path could pay a weapon's raw requirement as base AP, or refund enough AP to strand the HP armor that made the bot survivable. We now budget real rolled/owned weapons and each worn armor piece's independent requirement, then use the existing equipment optimizer and normal earned-AP build path. The armor check must exclude an item's own secondary stats.

The native gear pass includes compatible high-level weapons, Zakum helmets and attack accessories. It does not paste MapleRoyals equipment numbers into items. Normal configured rolls and ordinary non-cursed weapon/glove scrolls retain their real results and spent slots. Scrolling happens after restoring the final HP set so an unscrolled replacement cannot quietly become the final weapon. The saved gear is reused on retry.

Gelt Chocolate provides 120 weapon attack in our WZ, but no magic attack. Physical attackers now receive twenty Gelts; Bishops retain twenty Apples and their 100 magic attack. Both raids' Night Lords use Balanced Fury. The existing rosters already supply two Night Lords per bot-only party, with three in Pink Bean's fifth party; we kept the support and melee roles. A human can replace the first party's Night Lord leader with another class.

The missing Horntail Necklace was real, but the proposed automatic swap was not an upgrade we could assume. These bots wear native Marks of Naricain with HP and substantial attack. Our Mark's base stats differ from the forum comparison, so we retained them rather than trade away survival and damage for the necklace's name.

The final live preparation passed all forty-two saved Horntail/Pink Bean characters: their actual unbuffed HP met their floors, normal item consumption produced the expected 120 weapon attack or 100 magic attack, and they were resupplied and saved offline. Seven returning fixtures passed another preparation without a same-weapon attack downgrade. Example claws improved from 42 to 85, 45 to 68, and 57 to 83 weapon attack; many Night Lords reclaimed 50-80+ base DEX, and Bishops reclaimed substantial base LUK for INT. This is equipment/readiness verification, not a fresh full boss clear. Pink Bean still needs its new damage pace measured under the unchanged one-hour timer. The autonomous population and human prerequisites were not changed; nothing was committed or published.

## October 30: remove the preparation leftovers

During the concurrent Horntail client audit, the companions reported cluttered inventories. The bags contained rejected equipment candidates and swapped-out pieces from the gear/scroll preparation: sixteen to seventy-six spare EQUIP items on each of the eleven active companions. We removed those through normal inventory removal without changing worn gear, supplies, HP, position or the human player's inventory.

Dedicated raid preparation now clears its spare EQUIP bag after optimization, scrolling and resupply have finished. It preserves the EQUIPPED inventory and all recovery, ammunition, skill rocks and entry items. The regression verifies that only spare gear and the replaced attack consumable are removed. The following Pink Bean test uses the finalized worn set without another gear-roll pass, on channel two with a controller restricted to that Pink Bean instance; it cannot drive attacks in the concurrent human Horntail audit.

## October 30: the right head had no accepted firing platform

During the concurrent Horntail client audit, I noticed that the Night Lords never attacked the upper-right head. The live target check showed the head (8810004) was allowed by the heads-before-hands policy, while healthy hands were correctly excluded. The thieves were on lower floors with no legal attack from there, and their stance search returned no destination.

A graph/geometry snapshot isolated the problem. The native upper-right platform at y=-220 was reachable and safe. The head hitbox spanned y=-216 through -99, so the ordinary client projectile envelope from the platform overlapped its upper edge. Our extra Horntail policy instead centered a thirty-pixel vertical band at the player's mid-body and rejected every safe upper platform. The one lower position that passed that band was too close to the boss body to be safe.

The main-head policy now uses the existing native projectile envelope for vertical overlap. The 350-pixel conservative distance rule, correct-side restriction, ordinary attack hitbox checks and body clearance remain. Hands and preliminary heads retain their existing policies, and Pink Bean is unaffected. The regression covers the actual upper-right coordinates, rejection from the floor, cross-body rejection and excessive distance. The change was applied without restarting the server, but the head had already fallen; actual client-observed attacks from the corrected platform remain to be checked on a future visit.

## October 30: Horntail clears the human audit

The human-led raid defeated Horntail with eleven hours fourteen minutes left on its twelve-hour main-body timer: approximately forty-six minutes, with no deaths reported. We recorded the clear as human-verified. The upper-right-head ranged firing fix had been installed after that head fell, so its actual attack behavior remains a specifically qualified follow-up rather than a reason to discard the rest of the successful audit.

I also noticed that individual Seduce kept selecting me while the companions only visibly received mass Seduce. The source explains this: it selects the first eligible in-range member in map-entry order, rather than rotating targets. Holy Shield protection is skipped. The human leader normally enters first. Native hand Seduce targets one at levels one through three; levels four and five target up to ten below the native thirty-percent HP threshold. We documented the current behavior without changing targeting rules.

## October 30: upgraded Pink Bean crew reaches the body, then times out

The thirty saved Pink fixtures ran on channel two while the human audited Horntail separately. The simulated client controller was explicitly restricted to PinkLead1's channel-two Pink Bean map; it could not drive Horntail. It submitted ordinary attacks/skills through MoveLifeHandler with native MP, cooldown, HP and range checks. As before, this exercises native hazard delivery but is not proof of the real client's exact AI activation choices.

The test preserved the finalized worn gear without another rolling/scrolling pass. It cleared all five statue waves and activated the real body through the earned-item reactor. Wave five began about twenty-seven minutes in. The first sampled real body was at fifty minutes twenty-four seconds, leaving only about ten minutes of the native hour. The statue phases had one death: Ariel hit a Bishop for 13,247 while Magic Guard was absent. The Bishop recovered normally.

The body exposed the remaining failure. Seduce, HP/MP-to-one, Seal/Stun and observed lightning attacks caused repeated deaths, including unsafe return positions on the left. The native counters recorded thirty-six deaths total by timeout: one in the statues and thirty-five afterward. The body still had 986,401,494 HP at the last sample. The clear flag was false, no rewards were collected, and twenty-five members were alive at the outcome sample. Individual five-life exhaustion contributed to members staying dead; the run itself ended on its normal timer. This remains an Otono rule, not a verified historical v83 claim.

We kept the timer, boss HP, hazard delivery and gear unchanged throughout. Better gear and the completed staging fixes improved progress over the previous timed attempt, but did not qualify completion. The next work should address body-phase survival during forced movement and HP-to-one, safe re-entry, and damage uptime. The precise real-client Seduce activation conditions still need validation; weakening the test controller just to obtain a clear would not resolve that question.

After the native event ended, exhausted dead fixtures used ordinary post-run death return, followed by native potion recovery. The Pink crew was restocked and saved offline with its worn gear retained and spare equipment removed. Horntail's human character and crew were not included in that cleanup. No commit or push was made.

## October 30: a shorter Pink Bean All Cure reaction

For the Pink Bean client audit, I asked for the curable-ailment display window to be one second instead of three. BotPqCureDelay now selects one second only within a Pink Bean event instance; other PQs and expeditions keep three seconds. Each new curable ailment still starts its own window, and cleared episodes reset. Normal All Cure consumption and native cure restrictions remain unchanged. Boundary regressions cover 999/1,000 milliseconds for Pink Bean, episode reset and the unchanged three-second default. The class was installed in the running server without restarting it.

## October 30: explain the pre-wave safety wait repeatedly

During the Pink Bean human audit, the entire crew stopped at the far-right floor with Rex at 74,973,599 HP. The live snapshot showed no reflect buff; all companions had reachable attack stances, but the pre-Munin rally held them while the human remained short of the gathering point. The one-time instruction was easy to miss.

The safety wait remains, but a shared crew timer now repeats a clear gathering reminder every fifteen seconds. It names the far-right or far-left bottom floor by the wall and explains the next wave's Seduce risk. Only one bot can claim each reminder interval; the timer does not restart every tick. If the human is already present, it instead explains that other crew members are still arriving. Boundary/repeat regressions cover the shared timer and suppression between reminders.

The reflect-duration question was checked independently: Hugin's physical, magical and combined reflect skills each specify twenty seconds in this server's WZ, and the normal monster-buff service schedules expiry using that duration. No reflect duration was changed. A seventeen-to-eighteen-second video observation does not by itself establish a different authoritative duration or explain the cast/icon timing offset.

## October 30: measure attacks before changing the strategy

We stopped guessing about equipment and measured one Night Lord and one Bowmaster for ninety seconds. The thirty saved fixtures entered Pink Bean normally on channel two with their finalized worn gear and existing target order. The opening Solomon fight gave a clean single-target sample without bird reflection; the window extended into the next wave's Solomon. The native client-input controller remained scoped to this test, with normal attacks, MP costs, cooldowns and hazards. No gameplay, gear, damage-formula or kill-order changes were made.

Existing sent-attack timestamps were sampled every ten milliseconds and independently checked against native MonsterListener damage callbacks. PinkArcher1 sent 360 Hurricane packets (3121004), and the monster received exactly 7,463,274 damage in 360 callbacks. PinkThief1 sent 108 Triple Throw packets (4121007), and the monster received exactly 18,682,669 damage in 108 callbacks. In both cases summed packet damage equalled native received damage exactly; the polling did not miss any sent attacks in this window. Hurricane averaged 20,731 damage per shot. Triple Throw averaged 172,988 total per attack, mostly six lines including Shadow Partner; this must not be quoted as damage per star.

The measurable cadence gap was Hurricane: its configured cooldown was 120 ms, but uninterrupted attacks arrived about 150 ms apart, approximately 400 instead of 500 shots/minute. Triple Throw's 625 ms cooldown appeared as about 650 ms between settled attacks. BotCombatManager ticks down positive cooldowns through BotMovementManager.tickDown, subtracting the fixed 50 ms physics tick and flooring at zero; rearming the full cooldown discards the fractional-tick remainder. The observed intervals match that rounding. Stuns, knockback/airborne movement, buff actions and the initial approach caused additional pauses; whole-window attack counts are not sustained firing rates.

Gelt and Sharp Eyes were present throughout the sampled observations. Shadow Partner appeared shortly after the Night Lord's first attack and stayed active. The ammunition hypothesis was ruled out: Character.recalcLocalStats already adds the first usable projectile's WATK to total attack. No missing Balanced Fury attack was established.

The native formula sampler predicted original Night Lord critical lines around 33,289-70,206 against Solomon, and Bowmaster critical lines around 20,114-39,314, at the observed buffs/stats. These are generated predictions from the current model, not an independent real-client per-line validation or a claim that the damage formula is historically correct. Actual live totals matching packet damage only rules out downstream damage loss in this sample.

All thirty fixtures exited through the normal NPC, were saved offline, and the test expedition/controller were confirmed absent/stopped. Recommendation before a new full run: correct held Hurricane's average cadence without changing damage values, then repeat this short measurement. Party-assignment and body-facing work remain separate steps. No third human character was needed for this diagnosis.

## Held Hurricane timing correction and human comparison preparation (October 30)

Held raid Hurricane now carries its next-shot deadline across bot ticks instead of restarting a full 120 ms cooldown after each shot. Startup, movement/facing resets and interruption handling remain; stalled time cannot bank extra shots. Other skills and autonomous population combat are unchanged. Combat regression tests and the packaged build passed; both changed classes were installed into the running server without a restart.

The repeat ninety-second native diagnostic recorded 534 Hurricane shots and 10,935,736 received damage, versus 360 shots in the earlier window. Settled intervals alternate around 100/150 ms, preserving the intended average; differing stuns and movement mean whole-window counts are not a controlled DPS ratio. All 534 native callbacks matched packet sums exactly. The Night Lord recorded 93 attacks and 15,902,384 received damage, also matching packets, with roughly 650 ms settled intervals. All thirty fixtures exited normally and were saved offline.

For a real-client comparison, the requested GM test character was prepared from PinkThief1 at level 180: identical worn gear, base stats, skills, base HP/MP and Balanced Fury ammunition, with Gelt and recovery supplies. Original character records were backed up privately and original worn items preserved in its equipment bag. Unbuffed weapon attack, LUK, DEX and maximum HP matched exactly. Native packet monitoring was enabled for the two characters. The client comparison remains pending; this preparation does not validate the damage formula or establish a full Pink Bean clear.

## Read-only attack-slot / HP washing assessment (October 30)

Client inspection found the matched Night Lord reference had zero attack on its gloves/cape. Matching that reference did not prove its loadout met the intended damage goals. A saved-state audit assessed all 42 Horntail/Pink Bean fixtures without modifying them. Conservative scenario: remove all HP from physical gloves, cape and shoes, retain other equipment, and reserve those slots for attack gear (16–21 glove, about 10 cape, 7 shoe total attack). These are target totals, not newly granted or verified rolled replacements; native Godly HP on actual attack pieces may reduce the shortfall. Secondary-stat equip legality still needs a final check.

Native APResetAction gives NL/Bowmaster +16 HP for 12 base MP; Hero/Dark Knight +20 HP for 4 MP. Only base MP above getMinMp(job,level) is spendable. All saved bots have zero remaining AP and zero HP/MP AP allocated, so damage-neutral washing needs an ordinary-stat→HP seed and a final MP→ordinary-stat restoration: N HP gains require N+1 reset items and N MP deductions. No stat loss is required if the full cycle is feasible; no resets were applied.

With current HP floors, all twelve Horntail fixtures have enough spare MP in this conservative scenario. Pink Bean deficits after reserving the attack slots are 579–850 HP for Night Lords (37–54 gains / 444–648 MP), 762–830 for Bowmasters (48–52 / 576–624 MP), 440–532 for Heroes (22–27 / 88–108 MP), and 566–689 for Dark Knights (29–35 / 116–140 MP). Bishops retain their existing gear and have no deficit. Current MP covers 5/11 Pink Night Lords, 1/5 Bowmasters, 4/4 Heroes and 0/5 Dark Knights; the largest additional spare MP requirements are 106, 68 and 76 respectively. INT added now does not retroactively create past level-up MP. This assessment supports a preparation decision, not a washing authorization or proof of a successful encounter.

## Native attack-slot candidates prepared (October 30)

The requested follow-up used bounded normal configured drop rolls and native non-cursed glove attack scrolling, including real failure and slot consumption. All 35 physical Horntail/Pink Bean fixtures received three spare candidates; seven Bishops retained their existing gear. Stormcaster Gloves reached 16–21 attack, Blackfist Cloaks 10–15, and Facestompers 7–8. The native Pink Adventurer Cape roll ceiling falls below the target 10 attack; Blackfist is the alternative already present in the supplied reference. No Chaos scrolls, guaranteed successes, slot restoration or direct attack/HP assignment were used.

Actual HP rolls reduced the remaining shortfalls substantially. Horntail Night Lords, Bowmasters and Heroes meet their floors with the candidates; its Dark Knights need 12–41 HP. Pink Bean Night Lords need 61–333 HP (4–21 HP gains / 48–252 MP), Bowmasters 237–315 (15–20 / 180–240 MP), Dark Knights 44–185 (3–10 / 12–40 MP), and Heroes 0–16 (0–1 / 0–4 MP). Every saved fixture now has enough spare base MP for its projected remaining washing cost. Add one AP Reset item to any nonzero HP-gain count for the seed/restoration cycle. PinkThief1's actual candidates leave 208 HP to recover: 13 gains, 156 MP and 14 reset items.

All original worn sets and base HP/MP/AP values were independently re-read from saved state and matched the pre-preparation snapshot, including the GM comparison character. Replacements remain spare inventory items: final equipping, secondary-stat legality, washing and mirrored human preparation have not been applied. Existing provisioning can remove spare gear, so this is a staged preparation result, not entry-ready equipment or a successful raid. No restart or production gameplay-code change was needed.

## Approved washing and final attack-slot equipment (October 30)

Following approval, all 35 physical Horntail/Pink Bean fixtures equipped their prepared Stormcaster Gloves, Blackfist Cloaks and Facestompers. The native UseCashItemHandler performed each AP Reset, respecting its cash-item cooldown and consuming real reset items; no HP/MP values or scroll outcomes were assigned directly. Each nonzero wash used an ordinary-stat→HP seed, MP→HP gains and an MP→original-stat restoration. Assertions verified the expected base HP increase, MP decrease, unchanged primary/secondary stats across washing, restored HP/MP AP counter and unchanged remaining AP. In total the operation consumed 303 reset items and 3,056 base MP. Seven Bishops needed no equipment changes or washing.

Native equip operations and the existing secondary-AP planner then funded actual equipment requirements; every final worn piece passed its independent stat requirements without borrowing its own stats. Washing itself lost no damage-stat AP. Changed equipment requirements can still move earned AP between primary and secondary stats. All physical fixtures met their existing HP floors alongside gloves 16–21 attack, capes 10–15 and shoes 7–8.

The earlier broad preparation could replace a finished attack set with HP-heavy equipment. A bot-only readiness check now preserves the finalized three-piece attack set once it also meets the raid HP floor. It skips broad re-provisioning/optimization for that completed set while retaining skill books/SP preparation, cooldown reset and supplies. The normal preparation path remains for incomplete sets and Bishops. Regression/build checks passed. A live repeat of each of the 35 fixtures' ordinary raid preparation preserved its exact equipment and base HP/MP/AP, cleared spare equipment, and saved it offline; no repeat washing or reroll occurred. The change was installed without restarting. This preservation check does not implement automatic washing or the new candidate-generation procedure for fresh fixtures.

The GM comparison character was updated to match PinkThief1 exactly, including its earned level 183, skill/AP setup, HP/MP AP counter, equipment and ammunition. Their unbuffed weapon attack/LUK/DEX/maxHP matched at 167/940/183/8,400. Its gloves/cape/shoes are 16/12/8 attack. Original human equipment and private backups remain preserved. Native packet monitoring remains armed for the pending real-client comparison.

This completes equipment preparation and repeat-entry verification, not a Pink Bean clear. The next proposed measurement is the guide-informed split assignment test (Rex assistance in wave three and both sides in wave four), with equipment held fixed, followed by a focused human body-facing/Seduce audit. Existing boss HP, defenses, timer and encounter requirements remain unchanged. A run with the new gear plus new assignments cannot by itself isolate the strategy improvement from the gear improvement.

## Split statue assignments under test (October 30)

Step-two testing keeps the finished attack/HP gear, existing boss stats and native 60-minute timer fixed. Wave three sends the first complete supported party to Rex while four parties work Solomon/Hugin; it joins the left after Rex falls. Wave four sends parties 1/2/4 right and 3/5 left (one-based party numbering). The current fifth-wave split is unchanged. Bishops and Dark Knights remain in their own complete six-person parties. Right-side melee still waits rather than entering Munin's Seduce area, while left-side melee may continue its assigned left targets.

The shared side assignment now also supplies warning-stage and pre-wave rally destinations. Because parallel damage can change which statue dies last, the outer-floor rally triggers when any final side statue in wave three/four reaches 25 percent, rather than hardcoding Rex/Hugin. Everyone gathers at the next wave's assigned safe side before the final statue falls. Live Munin still uses its native effect rectangle and a member's current side, preventing a returning member from crossing through the active effect box. Normal skill reach, reflection waits, native skills/cooldowns and movement remain authoritative. Strategy/rally regressions and the package build passed; changed class bodies were installed without restarting.

Preflight found PinkThief8 needed six additional HP to preserve the existing 99-percent-potion safety margin against the maximum modeled native Rex hit after the defense change. One authorized native HP-gain seed/restoration cycle added 16 HP, consumed 12 MP and two reset items, restoring its primary AP and HP/MP AP counter. No equipment was rerolled or changed. This happened before encounter entry.

The thirty existing Pink fixtures entered through the normal NPC on channel two at 2026-10-30T21:59:07Z. The test-only controller supplies native monster packets, including ordinary attack masks and skills with native eligibility/cost/cooldown checks, and a controller-local collision echo. It remains a simulation, not real-client validation of exact monster AI, body facing or Seduce semantics. Native MonsterListener callbacks measure damage by wave, target and character. The completed outcome is recorded below; Nelbot's client damage comparison is deferred at the player's request.

## First completed headless Pink Bean clear (October 30)

The step-two thirty-person test completed all five statue waves, the normal earned-item/reactor transition, the real Pink Bean body, drop collection and exit within the unchanged native 60-minute event. Native MonsterListener callbacks sum to exactly 2,100,000,000 damage on the real body. The event's native clear flag was true; the test leader recorded seven drop pickups. All thirty fixtures returned to recruitment, were restocked/saved with positive HP and logged off. Final verification found zero fixture characters/manager entries online, the controller stopped and no channel-two Pink Bean expedition remaining. A stray idle test-only registration created after clear was canceled with the same native disposal/channel-removal operations used by the NPC. The autonomous population was not changed and the server was not restarted.

Timing: harness setup began at 21:58:55Z; wave-one/event entry was observed 12 seconds later at 21:59:07Z. Native clear was observed at harness second 3,579 (59m39s including setup), approximately 59m27s after observed event entry, leaving roughly 33 seconds. The body began at harness second 2,652 and took 927 seconds (15m27s). This is one narrow success, not a reliability claim.

| Milestone | Seconds after harness setup |
|---|---:|
| Wave one / observed entry | 12 |
| Wave two | 106 |
| Wave three | 293 |
| Wave four | 969 |
| Wave five | 1,671 |
| Real body | 2,652 |
| Native clear | 3,579 |

Native death counters and observed transitions both recorded **45 deaths: one statue-phase death and 44 body-phase deaths**. Seven members each reached the existing five-death allowance: Bishops 1/3/4/5 and Swords 1/2/4. The statue death was Bishop1 during the wave-four crossover; a conclusive damage-source attribution was not captured for that one. Body fatal records explicitly show central body contact around 23,500 damage, re-entry without Hyper Body, an observed 18,737-damage hit, and Seduce/Confuse-related clusters. Some returning Bishops had Magic Guard active but insufficient unbuffed MP to absorb the 80-percent transfer from a roughly 23,000 contact hit; do not diagnose every return death as missing Magic Guard. Original worn gear and attack rates were not changed during the encounter, and no lives, HP, timer or damage were bypassed.

The split assignment is not proven faster on its own. Wave three took about 11m16s versus about 8m27s in the earlier geared run; wave four took 11m42s versus roughly 12m31s. The new run reached the body around six minutes earlier overall, but finished attack gear, corrected Hurricane cadence, random native-controller spell timing and later-wave differences prevent attributing that gain solely to the split. Opening Solomon samples gave PinkThief1 about 188,214 damage per complete attack (98 native callbacks) and PinkArcher1 about 22,055 per shot (332 callbacks), including normal live interruptions; these are not per-star client validation or controlled DPS ratios.

Status: **headless clear verified; human-client audit pending; body survival/re-entry remain fragile**. The test-only monster controller supplies native packets/collision observations but does not establish authentic client AI selection, facing or Seduce activation. The next useful work is safer body approach/return routes and verifying body-facing/Seduce behavior with a human client, rather than assuming more gear alone fixes the fatalities. Nelbot's packet/damage comparison remains deferred; the reference crew can earn levels during runs, so refresh the match before a controlled comparison. No public commit or release was made.

## Voluntary Pink Bean body contact: focused route fix (October 30)

The player asked to avoid another expensive full-hour test and distinguish Seduce deaths from bad movement. Matching the previous run's fatal-hit logs to its death ledger identified all 44 body-phase deaths: 29 contact hits with Seduce absent, 13 observed attacks while Seduced, and two other observed attacks. Several contact records explicitly show returning members walking from x approximately -40 toward the body at x=0, including Bishops whose Magic Guard was active but whose unbuffed MP could not absorb the full transfer. These counts do not prove knockback was absent from every contact; they rule out Seduce as the universal explanation.

The established route defect was that safe combat/support destinations were checked without keeping the approach clear. A returned member on the left was directed toward a right-side party anchor, and ordinary shortest navigation could walk through the real body's lethal floor hitbox. Tower descents could likewise select an inward drop. A short replay on native map geometry reproduced contact without Seduce. Simply requesting the central stepping platforms was rejected: the existing graph could choose a launch point inside the body even when the destination platform was safe. No graph edges, launch windows, jump physics or population cache version were changed to manufacture a crossing.

BotPinkBeanBodyApproach now redirects cross-body and body-overlapping goals to a safe position on the member's current side. Pink's real-body formation accepts that side rather than insisting the member cross back through the boss. Tower descents first use the existing outer edge/rope approach, and central-platform exits first step outward. Movement remains ordinary navigation and physics; no teleport, immunity, damage suppression, forced healing, boss changes or Seduce cancellation is introduced. Voluntary approaches are constrained; forced Seduce/knockback can still cause legitimate contact. The historical preference for one-sided body positioning still needs client observation; this change deliberately favors not crossing a living boss merely to regain a formation.

Verification: focused policy/formation tests and real-map movement replays passed for left/right floor returns, a right-tower descent, and central/side-platform exits, with zero sampled body-hitbox intersections. Combat regression checks passed before the final focused extension; the final focused suite passed and the packaged build completed. Changed Pink-only class bodies were installed without restart. No full encounter was repeated and no new human-client verification is claimed. The earlier 59-minute headless clear therefore predates this route change.

## Reliability pass: contact fixed, margin still insufficient (October 30 local time)

The player approved five bounded steps: short body recovery/uptime checks, concentrated wave-three targeting, measuring class contribution, examining Seduce recovery, and one fixed full attempt. Work remained confined to dedicated Pink Bean behavior; normal boss stats, 60-minute event timer, equipment, native skill costs/cooldowns and individual death allowance were retained. Existing fixtures could earn levels through ordinary kills.

### Changes and focused verification

- Wave three again concentrates on Solomon, Hugin and Rex in that order. Wave four retains its supported-party split. Actual native entry/phase timestamps, rather than assumed improvements, are retained below.
- Pink's protection recovery now runs before ordinary formation movement, so formation cannot repeatedly interrupt a Dark Knight moving into native Hyper Body range of an unprotected member.
- Real-body ranged stances are kept within 300 horizontal pixels of the body, with a common intended anchor 250 pixels to the current side. Dark Knights may use the full native Hyper Body casting rectangle around that intended anchor rather than the ranged half-box, permitting legal melee stances. Actual hitboxes and safety checks still gate attacks; buff range is not enlarged. Short body-control tests confirmed attack dispatch from either side, and the live run measures contribution below.
- The visible-column gap search now continues past a candidate rejected by boss safety, looking for another clear point on the same continuous ground segment. Candidate destinations and walking corridors cannot cross the live body's hitbox. Reaction delay, warnings, impact timing, actual diseases and damage are unchanged.
- Hero's Will retains normal MP cost, status/action restrictions and cooldown. Existing recovery regressions passed; no automatic cooldown refresh or status immunity was added.

Focused body/formation/dodge/strategy checks, ordinary combat regressions and the package build passed. No physics, graph authoring/version or other audited PQ rules changed; no server restart was needed.

### Diagnostic interruption and staging regression

The first reliability diagnostic exposed a regression from the earlier split-wave work: during the short interval after Ariel died but before the real body spawned, BotPinkBeanTransition resumed party-side staging and sent parties left, overriding the completed right-side body rally. Progress snapshots showed the first party on the right before Ariel died and on the left when the body appeared. Repeated left-side heavy attacks then killed ranged members whose Hyper Body HP was below that attack's approximately 18,000 damage. This is distinct from body contact. The diagnostic was ended without claiming a clear, using native NPC exit where available and native disconnect for exhausted members who could no longer interact. Its interrupted harness/result is not included in the completed rerun.

BotPinkBeanTransition now honors the body-rally notice throughout the reactor delay. A regression test checks the exact no-Ariel/no-body interval and then the real body spawn. A fresh full attempt started with the corrected transition installed; no strategy or gear changes were made during that measured rerun.

### Fixed rerun result

The rerun entered normally at 2026-10-31T01:19:01Z (October 30 locally) and again **cleared the full encounter within the native hour**. Harness clear was at 3,594 seconds including 12 seconds of setup: approximately **59m42s from observed event entry**, only about **18 seconds spare**. The real body took **16m00s**. Native body damage callbacks sum to exactly 2,100,000,000; seven drops were collected. All thirty fixtures returned, were restocked/saved with positive HP and logged off. Final verification found no fixture manager entries, no online fixtures, the controller stopped and no channel-two expedition remaining.

The polling ledger observed **58 deaths**, six before the body and 52 during it. The sampled native counter peaked at 57; fatal logs account for 57 damage deaths, so the single-count difference is retained rather than silently reconciled. **No Pink Bean body-contact fatal was logged**, versus 29 in the prior clear. The one encounter contact fatal was Rex during the statue phase. Recorded body fatalities were 34 attack-index-zero hits and 17 attack-index-one hits. Matching recorded diseases at death found 30 while Seduced and 21 without Seduce. The left-side heavy-attack/re-entry problem remains even when contact itself is prevented.

| Class | Native damage to body | Previous clear |
|---|---:|---:|
| Night Lord | 1,499,241,922 | 1,609,610,864 |
| Bowmaster | 329,381,052 | 395,161,258 |
| Hero | 130,059,805 | 73,937,716 |
| Dark Knight | 118,170,556 | 5,382,404 |
| Bishop | 23,146,665 | 15,907,758 |

Dark Knight contribution increased about 22-fold, confirming that the previous support formation excluded useful attack positions. The lost ranged damage and additional attack deaths offset those gains: overall body time did not improve. Native callback counts include summons/passives; separately sampled main attack packets show 9,370 Triple Throws, 16,889 Hurricanes and 1,946 Spear Crushers against the body. Sampling is not an independent client-formula verification. Body activity samples distinguish death/return rooms, Seduce, Stun, Seal, missing Hyper Body, recent attacks and movement; an idle sample can include legitimate reflection/buff waiting and must not all be labeled a bug.

Wave three lasted about 9m31s in this rerun versus 11m16s in the prior split clear; wave four was about 12m00s versus 11m42s. Body began only about 18 seconds earlier overall. Random native-controller spell timing and ordinary progression mean this is not a controlled causal speed comparison.

At 87 observed Seduce onsets, Hero's Will was cooling on 44. All onset samples could pay its MP cost and none was already Stunned/Sealed; that does not prove those conditions stayed valid until the attempted cast. These samples establish cooldown pressure, not a reason to bypass it.

**Outcome: a second narrow headless clear, not the desired five-minute safety margin or reliable survival.** Contact avoidance and melee contribution improved; status survival, column avoidance and safe return positioning remain unresolved. The test-only controller is still a native-packet simulation, not proof of authentic client AI/facing/Seduce activation. Human-client verification is still pending. No further full attempt was started and no commit/public release was made.

### Pink Bean: return above the body, rather than fighting from the dangerous left

Following the two narrow headless clears, I chose a short return-position check before another full audit. The previous floor guard eliminated recorded body-contact deaths, but returning bots stayed on the left inside a heavy attack that could exceed their buffed HP. A native-map replay found a legal upper route using the existing left ropes, upper ledge, central high step and right step. It avoids the lower shortcut whose shortest path entered the body.

Returning bots now take that upper route before ordinary formation movement, finish each authored jump/climb, and restart the climb if forced back onto the left floor. An observed heavy-attack warning makes them wait on safe ground until its impact resolves. After reaching the right, the existing protection recovery and attack handlers take over. No warp, immunity, altered boss stats, new movement physics or cooldown reset was introduced.

The short real-map checks passed at normal and Haste speeds with no sampled body contact. They took about 8-11 seconds and briefly entered the heavy attack area; warning timing reduces that risk but does not make the route invulnerable. Protection gating and attack dispatch checks also passed. These are automated route/controller results, not a third complete raid or a human-client pass. The next useful observation is whether actual live returns survive forced movement and rejoin their party as intended.

The final six route cases all arrived without sampled body contact, including recovery from a displaced floor position and a central-step start. The build passed and the fix was installed without restarting the server. No new full encounter was started.

### Pink Bean audit: upper ranged stances and missing Triple Throw stars

I saw the thieves throwing from the upper platforms without visible stars. The statue stance policy explicitly places ranged attackers on the high inner platforms for Solomon/Rex, away from contact damage; this observation alone does not establish that position as invalid. Live packets carried Balanced Fury and damage while the client still lacked stars. Matched visible human Triple Throw broadcasts used display byte 0x08, whereas the bot emitted 0x00.

The bot's Triple Throw display field was matched to that observed client value as a focused visual trial. Damage, projectile item, attack direction, range and positioning were unchanged. Combat/attack-data/manager checks and the package build passed, and the field change was installed without restart. Client confirmation of projectile visibility is pending; the packet difference is evidence for the trial, not yet a proven visual root cause.

### Pink Bean: announce actual reflect activation, without an all-clear message

Repeated reflect casts looked nearly continuous during the human audit. I requested one ordinary bot-speech announcement naming physical, magic or combined reflect when it becomes active, and explicitly omitted a reflect-down message. The crew now reads the actual monster status effect, not the earlier cast warning. It deduplicates by the applied effect object across the crew, names the affected boss, and speaks directly in map chat so a queue cannot deliver an obsolete warning after expiration. A new application can be announced again, including an identical reflect type. No reflect timing, avoidance or attack rule changed.

Focused tests cover all three types, thirty repeated crew observations without duplicates, no message on expiration, repeated applications, and combined-status insertion without double announcements. Chat/strategy regressions and build passed; installed without restart. Human observation of the new speech remains pending.

The client also confirmed that the earlier Triple Throw display-byte trial still left stars invisible on the upper platform. It was reverted rather than retained as a claimed fix. Projectile visibility remains unresolved; a same-platform human attack sample was requested to compare actual client reach and packet behavior.

### Pink Bean: include the human without making formation wait for them

During the timed human audit, I asked the crew not to wait for me to reach formation, including between phases. The readiness checks previously included the human in the protection gather, outer-floor rally and pre-body rally. A human could also become a formation anchor or send a Dark Knight chasing distant missing Hyper Body, spending raid time on an optional human movement choice.

Those readiness checks now count the bot participants only; a headless raid still counts its bot leader. Formation anchors and required Hyper Body providers come from the crew. The human remains a normal party/expedition member, receives ordinary nearby buffs and healing, and keeps the raid guidance. A Dark Knight does not chase a distant human just to rebuff them. Safety regrouping among the bots, reflect waits, boss mechanics and the timer remain intact. Guidance now invites the human to join while the bots continue, rather than saying they are waiting for the human.

Focused checks verified readiness versus membership, headless-leader inclusion, excluding a human Bowmaster as formation anchor, and Hyper Body still including a nearby human without chasing a distant one. Strategy/formation/return-route, chat and Horntail regressions passed, the build passed, and the update was installed during the running audit without a restart. Human confirmation of the revised progression remains pending.

### Pink Bean: keep Ariel damage going during the pre-body crossing

During the human audit, I saw the whole raid stop for the pre-body rally, including attackers already on the right. The old rule sent everyone to one far-right floor point and waited for all arrivals before resuming attacks. Those already fighting from the right upper platform had to descend and later climb back up, adding a pointless detour on top of the full damage pause.

The rally now sends only left-side bots across, using ordinary movement to the connected right floor. Bots already on the right continue their normal legal attacks, buffs and reflection handling from their existing positions. Readiness accepts right-side platforms instead of one floor marker. If a bot is still crossing when Ariel reaches the final five percent, bots hold that finish reserve until it is clear; there is no damage cap or boss change, and the human is not gated. Human position still does not count toward bot readiness. The explanation now describes continuing attacks during the crossing.

Focused regressions verify that a right upper-platform attacker is neither moved nor held while left bots cross, only the low-HP finish reserve waits, upper-right arrivals satisfy readiness, an absent human does not hold the crew, and left bots use the ordinary movement adapter. Strategy, participation and formation checks and the package build passed. Installed without restart; time saved and behavior in a fresh live Ariel transition remain unverified.

### Pink Bean: a 20-second poison cloud was lasting 200 seconds

During the human body-phase audit, I noticed a poison cloud that seemed never to disappear while the crew was forced onto the right. A live map check identified two overlapping clouds owned by Ariel, not Pink Bean: skill131/4, 2,000 contact damage, covering x105-555 and y-290 to-44. Their area overlapped the normal right-side body positions.

The native monster-mist spawner used x*100 for lifetime. In this skill x is the 2,000-damage value, so it scheduled 200,000ms instead of the WZ time20's 20,000ms. Ariel can cast again after40seconds, making those ten-times-long clouds accumulate and persist into the body phase. This was a shared monster-skill lifetime defect, not evidence that the bots should ignore poison or that right-side positioning needed a damage exemption.

The spawner now uses the existing parsed skill duration. Damage, tick handling, cloud bounds and cast interval remain unchanged. A regression checks all nine native monster-poison skill levels against their configured durations; the existing poison contact/Magic Guard and skill-factory checks passed, as did the package build. Installed without restarting. Already spawned clouds retain their previously scheduled expiration; they were not removed manually. Newly spawned Ariel clouds use20seconds. Live expiration under the corrected code still needs observation.

### Pink Bean: stop relying on the human slot, then measure the class gap

I asked for a50-minute clear with the29 companions alone, leaving the human free to contribute or watch. The earlier narrow clears used30 active bots and had not proven that requirement. Bowmasters now prefer Hurricane where a valid native plan exists. Inspection also found a second Ariel target gate still blocking attacks despite the rally movement fix; that missed gate was corrected and directly regression-tested before the measured attempt.

The29-bot test completed every statue wave but timed out with16.97% of Pink Bean's body HP remaining. The body began at46m02s; the observer dealt zero damage. Native first-wave Solomon callbacks accounted for the full300million HP, confirming that the missing upper-platform star visuals did not mean the attacks were fake. The bird fights exposed the larger class gap: safe outer-floor positions left Bowmasters and Bishops without a valid grounded attack plan for long stretches, while Night Lords could jump-throw. Hurricane preference helped selection where reachable but did not solve that geometry.

There were29 deaths:4 Bishops lost Magic Guard on ropes against Ariel; all25 body deaths occurred while Seduced and were logged as attack0. No body-contact or left-heavy-attack fatal was logged in the measured run. Native body damage shares were66.3% Night Lords,18.4% Bowmasters,8.4% Heroes,5.7% Dark Knights and1.2% Bishops; support roles and legitimate reflection/immunity waits are not all wasted time. The50-minute target remains unmet.

The setup itself needed two harness corrections: an unregistered idle observer was evicted by the native watchdog, and its replacement already had a native party, making a duplicate creation fail. These diagnostics were excluded from the full run. The measured observer was registered with AI paused; normal hazards and the native controller continued, and no gear/stat/timer or combat changes were installed during that attempt. Final cleanup found no online fixtures or manager entries and the controller stopped.

I approved trying a Night Lord-heavy roster next, replacing the four Hero slots while keeping each supported party's Dark Knight, Bishop and Bowmaster. The original Heroes will be preserved. This is a new test direction, not a claimed clear or a published feature change.

### Pink Bean: try more Night Lords, preserve the Heroes, then stop for shutdown

After the mixed29-bot timeout, I approved replacing four Hero slots with Night Lords while retaining the Dark Knight, Bishop and Bowmaster in every party. The four original Heroes and the other25fixtures were preserved. Four dedicated Night Lords were prepared at the replaced Heroes' levels180/181 with ordinary native gear generation, attack scrolling, AP-reset/HP-washing mechanics and Balanced Fury. Their final unbuffedHP was8400-8411, with the same attack-slot thresholds as the existing crew.

The experimental14-Night-Lord crew reached wave3 at4m44s,44seconds earlier than the mixed roster; wave1 improved from1m39s to1m31s. There were no deaths and the observer dealt zero damage. I then clarified that I needed to shut down the computer, so the test was intentionally ended at6m46s through native leader disconnect. The partial timing is saved, but it is not a full-clear result or proof of a50-minute finish. The production NPC roster remains unchanged pending a complete test. No commit or public release was made.

### A client run without an hour of chat monitoring (October 31, 2026)
I wanted to conserve weekly usage while leaving Pink Bean running in my client. We changed the NPC selection to the approved Night Lord-heavy roster (14 Night Lords and five each of Dark Knights, Bishops and Bowmasters), preserving the original Heroes. A compact read-only event log records progress every 30 seconds and the native clear, timeout or exit result, so the result can be inspected once afterward. Focused roster and event-hook tests cover the preparation; full completion remains unverified pending the client run. The human should stay connected and avoid deliberately dying, which could consume resurrection attention.

### Pink Bean's last-second human-client clear (October 31, 2026)
I left the client running with the 29-bot Night Lord-heavy crew and returned for the finish. I reported the kill with about three seconds left; the server's native CLEAR record independently gives 3,593 seconds after the audit start (59m53s), four aggregate participant deaths and 29 participants alive. The screenshot captures six seconds remaining immediately before the finish, rather than the kill itself. The native timer and server logging start can differ slightly from the displayed countdown. This is a human-client verified completion, not proof of repeatability or the desired 50-minute margin. The four deaths include human and bot deaths; the aggregate log does not split them. The revised crew can complete with the human present without requiring a matched player damage test. We should not claim zero human damage from this log, which does not record per-character damage.
