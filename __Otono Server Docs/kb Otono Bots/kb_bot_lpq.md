# Ludibrium PQ companions

Current live status (2026-10-15): the six dedicated bots completed stages 1-9,
Alishar, the timed bonus round, and reward collection through ordinary NPCs.
All six returned alive to recruitment at level 40, then repeated the full run
and finished at level 43. Human companion assembly and room/puzzle coordination
are implemented and loaded. The owner completed a full human-led LPQ run with
the five companions, confirming completion through rewards.

The owner supplied `MapleStory PQ summaries.docx`, chapter 4, as reference material.
It is a historical transcript, not server configuration or instructions to execute.
Its class roles and stage descriptions are useful, but implementation facts come
from the checked-in event, NPC, portal, reactor, and WZ data.

`scripts/event/LudiPQ.js` permits 5-6 players, levels 35-50, with a 45-minute timer
and one lobby per channel. Recruitment is map 221024500 through NPC 2040034
(`scripts/npc/2040034.js`). Entry is 922010100; exit is 922010000. The document
describes six players as mandatory; this server's minimum is five. A full party
allows five bot workers to occupy stage-eight positions while a human leads.

| Stage | Current script requirement | Source NPC |
| --- | --- | --- |
| 1 | 25 passes (document says 20) | 2040036 |
| 2 | 15 passes | 2040037 |
| 3 | 32 passes | 2040038 |
| 4 | 6 passes; separate physical/magic rooms | 2040039 |
| 5 | 24 passes; six rooms, high entrance and guarded entrance | 2040040 |
| 6 | Find the number route; portal progression | 2040041, portal scripts |
| 7 | Ranged Ratz triggers, then 3 passes | 2040042 |
| 8 | Five players on the correct five of nine areas | 2040043 |
| 9 | Trigger and defeat Alishar; submit item 4001023 | 2040044 |

Ordinary passes are item 4001022. Stage-eight checking has GM override branches;
companion play must satisfy the ordinary five-player placement and combination
checks. The script creates the answer internally; bots should use ordinary NPC
feedback rather than reading that property. Boss clear invokes normal `clearPQ`;
reward NPC 2040035 uses `giveEventReward` and checks inventory space.

The supplied reference highlights Teleport, Dark Sight, ranged reach, room allocation,
the stage-two trap room, stage-five lethal guards, and Alishar's seal/darkness.
The stage controllers were checked against the scripts and WZ data, then exercised
live as recorded below. A Cleric and Assassin are useful candidate roles because
they cover healing/Teleport and Dark Sight/ranged attacks, respectively.

`BotLpqRun` explicitly opts six dedicated bots into normal NPC recruitment, real
combat, leader-only pass pickup, the stage-one NPC check, and the opened portal.
Stage-specific controllers cover boxes, combat rooms, skill rooms, numbered
portals, ranged triggers, the placement puzzle, boss, bonus, and rewards. LPQ
instance membership forces full simulation and excludes LOD motion plans/abstract
kills. `BotPqHooks` dispatches this controller alongside KPQ and Zakum. The existing
`BotZakumPqRun` reactor interaction path remains a reuse point for box stages.
NPC 2040034 now offers human companion assembly and dismissal, described below.

The dedicated level-35 crew is LPqSpear1 (Spearman), LPqCleric1 (Cleric), LPqMage1
(Ice/Lightning wizard), LPqThief1 (Assassin), LPqArcher1 (Hunter), and LPqSword1
(Fighter). `BotPqCrewFactory` shares ordinary creation, level-ups, job changes,
and AP/SP assignment for KPQ and LPQ fixtures. LPQ fixtures advance their second
job at level 30 and plan initial physical accuracy against stage-one Black Ratz.
Provisioning resolves the first-job ammunition kit for advanced classes as well.
This crew had spent AP/SP, legal optimized gear, 5,000 ammo, 200 Ginger Ales and
100 All Cures each before entry. Automatic curing now uses the normal item handler
and has regression coverage; the owner-led Alishar inspection later confirmed carried cure consumption.

The current Cleric build prioritizes Heal and does not learn Teleport at level 35;
the I/L build learns Teleport 1 immediately. The Assassin has Dark Sight 20 and
Keen Eyes 8. Dark Sight is deliberately excluded from ordinary automatic combat
buffing, so the dangerous room requires an explicit legal traversal role, not
removing that blacklist for the general population.

One live run verified registered NPC entry, 25 real monster kills, all passes
submitted through the ordinary NPC, and `1stageclear=true`. All six stayed alive
and level 35; calculated hit chance against stage-one Black Ratz was 100%. Five
crossed into stage two. The archer reached the isolated lower foothold region at
(-205,542) in stage one and could not reach the exit at (-38,-180). The exact
speed-120/jump-100 graph reports no path from region 58 to 56. The main floor at
y=130 has `forbidFallDown=1`, while the isolated foothold at y=542 lies below
VRBottom=360. The cause was subsequently captured and fixed (see the collision diagnosis below);
do not bypass it by repositioning the bot. The capture is under
`logs/bot-nav/pathlog-lpmArcher1-2026-10-15T125040.txt` (post-failure, zero movement
ticks); endpoint evidence is in ignored `target/lpq-live-test`.
All six exited through Sgt. Anderson and were saved offline in recruitment map
221024500 for reuse. The server remains running. That was the first-session result; subsequent live results are recorded below.

Stage-two WZ contains eleven pass reactors (2202003) plus trap reactor 2200002.
The trap script calls ordinary `warpMap(922010201)`, moving everyone currently
in that map. The trap room contains four additional pass boxes and an ordinary
portal back to stage two. Stage three has eight 2201001 reactors; each script
spawns three Bloctopus. WZ supplies eight initial Bloctopus, accounting for all 32 passes.

Preparation should preserve the established human-led behavior: registered entry,
normal stage requirements, reusable dedicated bots outside the population, accurate
physical builds, adequate ammunition/potions/cures, and waiting for human checks
and portal moves. At EXP rate 15, eligibility headroom matters after the owner's
KPQ character rose from level 21 to 28 during one completion.

## Tower floor collision diagnosis (2026-10-15)

Live captures `pathlog-lpmArcher1-2026-10-15T125720.txt` and
`pathlog-lpmArcher1-2026-10-15T125808.txt` showed the descent during combat.
Temporary integrator logging caught previous=(-264,113), next=(-265,149):
a wall collision at progress=1 won against a floor collision also reported as
progress=1, although the solid y=130 floor was crossed earlier. The wall response
placed the bot below it. This is separate from down-jump eligibility.

`BotPhysicsEngine.landingAtProbeY` now reports the actual vertical contact time
for flat floors. Existing sampled landing coordinates are preserved. Live physics
and graph simulations share the function; graph version 74 replaces old arcs.
The incident regression and physics/movement/navigation/fallback tests pass
(161 tests across seven relevant suites; the movement simulation lab also passed
its 12 tests during investigation). No forced reposition or stage property edits
were used. New stage-two tests cover arrival waiting, approaching distant boxes,
and retaining the crew in the trap room until its passes are collected.

`BotLpqBoxes` uses the existing ordinary reactor-hit path, delayed actions,
leader pass pickup, normal trap return and stage-check NPC. `BotLpqStageThree`
uses normal combat, leader pass pickup and normal box spawns/NPC checks. Stages
four onward hold. Live stage verification follows below.

The next live run cleared stage one with all six members alive and all 25 passes
submitted normally; every member crossed into stage two. The ordinary trap
reactor then moved all six together into 922010201. No bot was repositioned.

A separate profile issue appeared while the leader collected the last stage-one
passes: cached speed-120 JUMP r37->r39 was reused while live speed was 110, causing
a repeated short landing. Exact-profile warming restored progress. The existing
fresh-search profile gate was missing from committed-edge reuse; `isEdgeUsable`
now applies it to reused JUMP/FLASH_JUMP edges. Regression
`BotNavigationManagerTest#shouldRejectReusedLpqJumpAfterSpeedChanges` passes,
along with all 43 navigation-manager tests. This runtime eligibility change does
not alter serialized graph authoring. It was hot-reloaded without another restart.

## Verified stage-two result (2026-10-15)

The same crew collected the four trap passes, returned through the ordinary
portal, broke all eleven main-room boxes, and submitted all 15 passes through
NPC 2040037. Live event state recorded `1stageclear=true` and `2stageclear=true`;
the crew then used the opened stage-three portal. All members were alive and
levels 35-36. These results used real combat/reactors/drops/NPCs/portals, with no
GM flags, stage-property writes, item injections during the run, or map snapping.
Provisioning occurred before entry as authorized. Stage-three verification is
recorded separately; stage-two clear does not establish later-stage completion.

## End of 30-minute session

Stopped at 2026-10-15 18:25 UTC. Stage-three's initial eight monsters were reduced
to one, with seven legitimately collected passes; no stage-three clear or box-spawn
verification yet. Three members reached stage three, while Mage/Thief/Archer
looped at stage-two CLIMB r11->r45, launch (32,-2123), rope entry (24,-2184),
exact speed-120/jump-100 graph. Capture
`logs/bot-nav/pathlog-lpmArcher1-2026-10-15T132448.txt` shows repeated zero-horizontal
rope-entry jumps, no rope grab, then `no-region` and landing back on r11. This is
the remaining transition blocker; do not snap the bots past it. Next investigate
the authored rope-entry envelope against actual airborne steering/grab behavior,
add an incident regression, and retest all-six stage-two exit before stage-three
completion and stage-four role allocation.

All six exited normally through Sgt. Anderson and were saved offline at recruitment
221024500, level 36, retaining 196-200 Ginger Ales each. The server remains running
with the autonomous population enabled. Build succeeded. Relevant regression suites
passed: physics 59, movement 46, navigation manager 43, directional drops 5, graph
fallback 3, LPQ run 3, LPQ boxes 3, simulation lab 12, KPQ run 6, KPQ puzzles 3
(183 tests). Dedicated crew names/IDs are unchanged. Human NPC assembly and stages
four through rewards remain unimplemented; stage three is partially live verified.

## Continued session: verified progression and skill roles

The stage-two rope incident now reproduces in the movement simulation lab:
a zero-horizontal ground-to-rope jump at (32,-2123) toward rope x=24 drifted
toward the eventual portal after an unmapped airborne tick cleared its edge.
`BotMovementManager.initiateRopeJump` now marks the arc as fixed, retaining the
input assumed by the graph even for a vertical hop. This is runtime execution
alignment, not an authoring change. The new real-map regression failed before
and passes after; all 13 simulation-lab tests, movement and navigation tests pass.
The repeated live run moved all six members out of stage two.

Stage three was fully cleared with all 32 passes from the eight initial mobs and
eight ordinary three-mob spawn boxes. Stage four then cleared all five rooms and
submitted six passes normally. `BotLpqRooms` keeps the group together through
rooms and checks each room's expected pass total (the fourth room has two mobs).
A regression covers continuing the exit after advancing the next-room assignment;
otherwise the leader had resumed idle grinding in the previous cleared room.

Stage five assigns the Teleport mage to 501, Dark Sight thief to 506, and four
remaining members to 502-505. Workers collect their own box passes, return normally,
and drop them near the leader through `InventoryManipulator.drop` via the existing
bot command; the leader picks them up and checks NPC 2040040. Dark Sight is cast
only for the explicit assigned guarded-room role through the existing support
SPECIAL_MOVE pipeline. The general combat blacklist remains intact. Headless
physical contact checks now honor Dark Sight and update their last checked position
while protected, so expiry/box-attack cancellation cannot sweep a long old movement
segment through guards. The controller cancels Dark Sight for each safe box attack
and waits for its normal rebuff before traversing again.

The mage's required movement skill ignores the general travel economy's 500k meso
reserve inside LPQ, while retaining skill/MP/field-limit gates. A direct cross-map
portal's reachability now includes eligible skills when the exact movement-profile
graph is loaded; previously travel rejected the high portal despite a legal TP
route in `/api/navprobe?skills=1`. Long-distance partition route fallback remains
conservative. The live mage completed its high room and delivered four passes.

Stage five's live run cleared all 24 passes, with legal Teleport entry/traversal,
Dark Sight traversal and ordinary four-pass handoffs. Stage six used the normal
number-route portals, and stage seven completed all three Ratz/reactor/Rombard
sequences with real ranged attacks and three submitted passes.

Stage seven exposed a general target-selection inconsistency: an immediately
shootable island target passed `isReachableGrindTarget` but grind target scoring
still assigned infinite walking cost. `scoreTargetRegions` now reuses the existing
immediate-projectile verdict and assigns zero movement cost when already in range.
An isolated-platform regression failed before and passes after the change; all
93 combat tests passed. `BotLpqRanged` selects reachable firing ledges using the
shared projectile range and WZ monster hitboxes, and the thief can make a normal
vertical jump throw for the high Ratz. Actual firing uses the normal attack path.

Stage eight enumerates five-of-nine visible placements and advances only upon the
ordinary `FIELD_EFFECT` mode-3 `quest/party/wrong_kor` packet observed by BotClient.
No winning-combination property is read. A Cleric landed on the very steep right
boundary foothold (450), which the graph groups with the high upper platform;
walking toward the group's high launch point could not progress. The existing
normal recovery-jump watchdog is now available to LPQ bots only while a held
launch gate is blocked. General population ENABLE_UNSTUCK stays unchanged, and
correctly placed workers (no blocked nav edge) stay still. The live Cleric recovered
without position edits and the group resumed NPC checks. The steep-region model
itself has not been changed; this is ordinary stalled-navigation recovery.

LPQ bots also use carried All Cure Potions for seal/darkness and other curable
ailments through `UseItemHandler.consumeUseItem`, never by directly removing a
disease. Item consumption and no-waste behavior have focused regression coverage.

## First full live completion (2026-10-15, 19:12 UTC)

All six cleared stage eight on the 99th ordinary NPC check. The controller only
observed wrong-answer packets; it did not read the script's winning combination.
In stage nine, the ranged crew killed the isolated Ratz through ordinary attacks,
triggered Alishar, defeated it, and the leader picked up the actual necklace.
The ordinary boss NPC accepted that item and opened the timed bonus round.

All six subsequently returned to recruitment through reward NPC 2040035. Its
script only warps there after `giveEventReward` succeeds, providing a positive
reward-collection check rather than merely observing that the boss disappeared.
All six were alive and level 40; their remaining Ginger Ales were respectively
186, 197, 191, 183, 193 and 180. All Cures remained at 100 because no boss ailment
was observed in this fight. They were saved normally before starting a repeat
with the same character IDs and the final controller code already loaded.

The selected regression suite and package build passed. Coverage includes legal
projectile target selection, movement/Teleport travel, Dark Sight contact history,
placement feedback, cure consumption, and existing KPQ behavior. Runtime changes
were loaded without restarting the server or changing autonomous-population
settings. No stage requirements, hidden answers, mob spawns, or bot positions
were overridden.

Human entry remains a separate adaptation: room assignment currently assumes six
bot workers, and puzzle checking currently observes a bot leader's NPC packets.
An NPC assembly option must coordinate five companions around a human leader,
allow ordinary human checks/portal actions, and avoid waiting on bot-only state
for the human's room. This has not been claimed as human-ready.

The immediate repeat (about 19:14-19:29 UTC) also completed all nine stages, the
bonus timer, and reward NPC for all six, without any code changes or gameplay
intervention during that run. Its stage-eight arrangement happened to pass on the
first normal check; the prior run exercised 99 checks. The same IDs finished alive
at level 43 and were saved offline in recruitment. Thus two live reward completions
are verified, including one uninterrupted run with the final implementation loaded
from entry. The packaged build and 466 selected regression tests pass.

## Human-led NPC entry (2026-10-15)

The Red Sign (2040034) on the 101st Floor now offers **Assemble a bot party for
me** and **Dismiss my bot party**, including when the old PQ NPC style is enabled.
The player must be level 35-50, outside an event, and alone or leading a solo party.
Assembly creates the ordinary six-member party with the player as leader. Entry
still uses the original participate option and `LudiPQ` eligibility/instance path.

`BotLpqPool` selects only registered, offline, eligible LPQ fixtures. Migration 33
creates `bot_lpq_companion` and adopts the six existing, unowned test fixtures;
managed population characters are excluded. Live preflight selected IDs 547-551
(Spear, Cleric, I/L Mage, Assassin, Hunter). Sword1 remains offline. A new suffix
is created only if no eligible fixture is available. Setup uses ordinary AP/SP
builds and the shared equipment/ammo/potion provisioning before entry.

`BotLpqHuman` explicitly registers only these companions. A designated collector
picks up stage items, accounts for passes the human collected, and drops its stack
beside the human through the ordinary inventory action. Delivered piles are not
picked back up. The human supplies stage NPC checks and opened-portal transitions;
the bots wait instead of initiating the next combat stage ahead of the human.
Leaving/disconnecting or changing the party leader causes normal NPC exit and
companion cleanup. Re-entering with the same crew resets per-event delivery state.

Stage four retains the verified physical/magic room sweep and brings six passes
to the human. In stage five, the five bots handle all six rooms: after delivering
its first room's four passes, the worker assigned room 502 takes the spare room
505. There is no human room-completion flag to wait on. The player can stay in
the main room and collect each delivery until holding all 24 passes. Pass piles
in this main room are excluded from all passive bot loot.

`BotLpqHumanPuzzle` places the five bots on the nine visible areas. It announces
when each arrangement is ready; the human stays off the boxes and checks the NPC.
Only the designated collector observes the ordinary wrong-answer broadcast to
advance once. Early checks while moving do not skip arrangements, and the winning
combination is never read by bot code. NPC 2040043 retains ordinary placement and
answer requirements for companion leaders even if the human has GM permissions;
this is necessary because the owner's prepared level-35 NelBot has GM level 6.
Other GM permissions and unrelated NPC behavior are unchanged.

The build and 38 focused LPQ/KPQ tests pass, including real GraalJS execution of
the menu and rejection of an incorrect puzzle answer for a GM companion leader.
The registry migration and class updates were loaded without a server restart.
Preflight reused all five existing fixtures, left NelBot unpartied at recruitment,
and did not start a PQ. The owner subsequently completed the full human-led run
and reported that it went well. Stage six currently gives only a generic numbered
portal hint: bots traverse the route, but do not share the successful number order
with the human. The owner spent about ten minutes finding it independently.

Human-led stage one uses ordinary bot speech bubbles to explain the Ratz/25-pass objective, collector handoff and Red Balloon check. The crew waits seven seconds for the introduction before setting off. One collector announces actual held-pass milestones and leader readiness once per instance; obsolete stage-one messages expire on clear or map change.

LPQ guidance follows the same two-channel convention as KPQ: role/progress/action descriptions are bot speech bubbles; stage-item deliveries, NPC-check readiness and stage clears use private type-5 pink/purple LPQ notices. Stage-eight box-arrangement readiness uses normal bot speech and bubbles; stage clears remain private pink/purple notices.

Ground handoff limitation: normal stage-item drops expire after three minutes, so an AFK human can miss a delivery. KPQ has scoped protection for its bot-earned stage-one passes; LPQ and other PQ expiry behavior is unchanged.

Dedicated PQ crews do not initiate item, supply-share or market trades, including
with sibling bots. Queued transfer retries are discarded, active initiated
sequences are cancelled through normal Trade cleanup, and incoming peer-bot
trades are declined. Humans can still open an ordinary trade with a companion
to supply potions or other items. Normal autonomous population trade behavior is
unchanged except that dedicated PQ companions are not eligible offer recipients.

BotLpqStageGuide supplies one collector-spoken introduction for each main stage from two through nine, plus bonus and rewards. Each describes the objective, companion roles and the human action. The crew waits seven seconds for a new introduction before continuing; room submaps do not repeat introductions. Stage-one guidance remains in BotLpqDialogue. Earlier room/vault/number notices were consolidated into this guide to avoid duplicates. BotPqChat now checks clear flags across all nine LPQ stages so old guidance expires on clear as well as map/instance changes.

Stage-four dark rooms 922010401-405 reject BotManager's forced recovery teleport,
including calls from distance/out-of-bounds and frozen-air watchdog recovery.
BotLpqRooms already uses normal tickTravel to return to out00 after collecting a
room's passes; recovery must not turn that return walk into a destination snap.
Learned movement skills retain their normal skill/range/MP checks. The owner
reported an apparent immediate return to the room exit; the recovery path was
found by code inspection, not captured firing during that completed room visit.
Regression tests cover the guard in all five rooms and long-distance fallback.
A subsequent client room-return observation is still needed.

Stage six offers an optional fixed-route hint instead of revealing the answer automatically. The human can say "box order", "box order?", "share box order" or "what is the box order" in normal chat. Owner routing reaches the dedicated collector even if it already waits in stage seven; the handler checks the human is in stage six and both remain in the same LPQ instance. One collector replies directly to the human chat log with its name (type 6), avoiding offscreen bubble dependence. Repeated explicit requests are allowed. BotLpqBoxHint shares the fixed visible-label sequence 133 221 333 123 111. Portal index within a row is not its printed box number. A WZ-backed regression follows correct portal destinations and matches their positions to the map box-label decorations to verify the player-facing sequence. No movement or portal requirements are bypassed.

A designated collector also sends one direct chat-log hint reminder after the human spends a minute in stage six without requesting the order. It does not reveal the sequence. The check still runs while the collector waits in stage seven; requesting the order suppresses the reminder, and leaving the maze prevents it from firing. Reminder state is scoped to the LPQ instance.

Stage-seven Ratz drop item 4001156 onto reactors 2201002, whose type-100 trigger consumes it after five seconds and summons Rombard 9300010. This is distinct from pass 4001022, dropped by Rombard. Bots must not loot 4001156 in this stage: a live human run found the collector holding one trigger while the middle reactor remained state 0; only two Rombards had been summoned, and the collector held two passes. BotLootEligibility now excludes that trigger from passive pickup and loot targeting. The collector correctly waits for three passes before handing them off. The original trigger pickup position was not recorded, so its apparent visual reach cannot be established retrospectively. The boss-loot announcement is suppressed in stage seven because these summon-wave monsters use a boss data flag but are not the final boss encounter. Recovery of the stranded run would require returning the existing trigger to an isolated reactor; current navigation probes found no legal crew route, so no pass, monster or position was forced.

Owner-led Alishar inspection on October 19 found Spear at 96 All Cures and Cleric/Mage at 98 each, from the assembly target of 100, with no active disease at the sample. BotLpqCures consumes a carried All Cure immediately on a tick with a curable disease; brief icons can therefore be hard to observe. Alishar skill data includes Seal (120) and Darkness (121); inventory consumption proves curable effects reached bots but does not identify which disease consumed each potion. Archer and Thief retained 100, so this observation does not establish that every bot was hit. No gameplay behavior changed for this inspection.

Human-led LPQ bonus map 922011000 reserves every ground drop for the human,
including mesos, consumables, equipment and deliberate player redrops. The shared
eligibility gate blocks both passive pickup and loot targeting for registered
human-led companions; BotLpqLater still walks to and hits bonus boxes normally.
This bonus-room rule is stricter than boss-loot redrop permission. The introduction
explains that bots help open boxes and leave everything for the player.

The inspected boss-specific live drop table for Alishar (9300012) contains Key of
Dimension (4001023) and Alishar Card (2388004), with no equipment entry. Arturo
(2040035) in reward map 922011100 calls giveEventReward before returning the player
to recruitment; bonus-box items are separate ground loot. King Slime (9300003)
has Squishy Shoes (1072369), King Slime Card (2388001) and Pass (4001008) in its
boss-specific table; KPQ also has the ordinary Cloto event reward before bonus.
These are verified table/script facts, not a record of what dropped in a given run.
