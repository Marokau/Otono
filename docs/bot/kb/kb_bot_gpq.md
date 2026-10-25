# Sharenian Guild PQ companions

## Current scope

October 24, 2026: **two complete bot-only live runs have passed**, from normal registration and gathering through all puzzles, the earring sacrifice, Ergoth and guards, Rubian turn-in, actual bonus pickups and normal exit. The second run reused the same five fixtures after ordinary dismissal and resupply and needed no mid-run code changes or intervention. October 25, 2026: I accepted GPQ as human-client verified, with the explicit caveat that the companions remain vulnerable to some Ergoth attacks. The subsequent gathering, protection-recast and potion-recovery changes passed automated checks and were loaded live, but I did not repeat the client boss audit afterward.

## Human entry flow

Use the existing WhereLevel GPQ entrance teleport and speak to Shuang. The separate BotPQ Menu explains guild requirements. For a guildless player it offers travel to Heracle with five dedicated cofounders, after explicit confirmation. The player chooses the guild name and approves the normal creation fee (currently 1.5M mesos); the standard six-person creation process remains in place. No guild is created merely by opening the menu.

Heracle offers return to the excavation camp and crew assembly after creation. The player remains master of the persistent guild. Existing master/junior players can assemble at Shuang directly when there is room for the five companions. Bots join through the normal guild invitation handler and form a normal six-person party. Registration waits until all five reach Shuang. The ordinary three-minute gathering period is unchanged.

Dismiss outside the instance to remove the five bot memberships and log them off. The player's guild persists. Subsequent assembly reuses eligible dedicated fixtures, rejoins normally and replenishes supplies; another guild creation fee is unnecessary. Disconnect cleanup withdraws dedicated bot memberships. Dormant fixtures already occupying guild seats are accounted for during recovery.

## Crew and isolation

Five dedicated companions: level-100 Priest, Hermit, Ranger and Dragon Knight, plus a level-25 Bowman scout for the level-restricted section. Prefixes are GPqMage, GPqThief, GPqArcher, GPqSpear and GPqScout. Shared level-appropriate gear/AP/SP and supply routines are used, including Ginger Ales, all cures, appropriate ammunition and mage skill consumables. These fixtures use the companion registry, never autonomous-population or player-owned bots. The scout's restricted room, ordinary earring sacrifice, actual death, native revival, replacement earring collection and walking return have passed live testing. The Dragon Knight supplies ordinary Hyper Body for Ergoth's real magic damage.

Assigned companions remain under the GPQ controller and use full movement simulation even without a visible human. Assembly travel outside the quest is explicit NPC convenience travel. Inside GPQ, use normal movement, attacks, pickups, equipment actions and portals; no coordinate snapping or stage-property shortcuts.

## Gatekeeper and protective equipment

The crew breaks real earring rocks, picks up the earned item 1032033 and equips it. Generic equipment optimization must retain that earring throughout the active GPQ area: the hallway map drains 30,000 HP without its protection. BotGuildEquipment guards both optimizer candidate collection and plan application; the driver can re-equip an already-earned earring after an external equipment change. It never grants the quest item.

BotGuildStatues observes the ordinary REACTOR_HIT display packets delivered to the assigned mage in map 990000300. It remembers the visible four-, five- and six-statue sequences, chooses a nearby worker and replays them with normal reactor hits/jumps. It does not read the hidden combo or guess properties. Human-led runs leave the NPC checks and progression to the leader; the bot-only fixture leader uses the same NPC interaction path. The spear driver assigns three jump courses, keeps the mage out of the lethal golem corridor, uses a real thief-to-mage spear handoff and sends the mage to the Teleport tower. The thief escorts physical stragglers for ordinary Haste. GPQ movement skills use the existing PQ meso-reserve exemption; skill/MP/map restrictions remain.

## Verification

A disposable headless leader created a guild through the standard handler, paid exactly the normal fee and retained master rank. All five companions joined, entered, waited the real gathering period, earned earrings and cleared all three Gatekeeper rounds. The first run exposed optimizer removal of the protective earring; normal map damage killed four bots. The normal death-popup response ended that failed run. Dismissal removed five guild memberships; reassembly reused the same five characters without a second creation fee.

A second normal-entry run cleared Gatekeeper again. All six reached map 990000301 alive wearing 1032033, and a forced ordinary equipment optimizer pass retained all six earrings. Focused tests cover setup consent, eligibility, guild readiness, observed statue sequences, earring protection and dormant guild seats. At that earlier checkpoint, no human client audit or full GPQ clear had occurred; the later full-clear evidence is recorded below.

## Code entry points

BotGuildCompanions owns setup and lifecycle; BotGuildEntry checks registration readiness; BotGuildActions executes normal movement/attack intents; BotGuildStatues observes and replays visible patterns; BotGuildEquipment preserves mandatory protection; BotGuildMembership counts reusable dormant guild seats. NPC scripts 9040000.js and 2010007.js preserve normal options and add the BotPQ Menu.

## Fountain and clothing checkpoint: October 24, 2026

The real fountain cleared in four ordinary NPC checks (`stage3clear=true`, one public-clue candidate remaining). A human-mode checkpoint left the stocked puzzle unstarted for at least 5.5 seconds until the leader acted. Its initial test movement command could not reach the elevated Guardian Statue origin; after releasing that command, the GPQ NPC approach used the existing normal click path with a 400-pixel interaction approach radius. No stage state or hidden answer was supplied. The subsequent native checks cleared the fountain.

Wine on a small ledge exposed another legitimate pickup case: the worker now jumps normally and collects only after entering the existing pickup radius. It cannot pick up the item from the floor 77 pixels below. A regression verifies the jump and refuses the distant pickup. Item-reactor offerings retain their normal five-second consumption wait.

The mage recovered shoes, the thief recovered the shirt, and the level-25 scout recovered the crown from the actual level-restricted route. Shoes were consumed by Sharen's body, advancing its real reactor to state one. Shirt and crown waited for the trousers; the driver did not skip the required clothing order.

### Resolved blocker: trousers jump course

Map **990000611**, base speed/jump **100/100**, field limit **270974** (movement skills forbidden). The worker can reach the left staircase and the platform at **y=7**, but the next overlapping platform is at **y=-70**, a **77-pixel rise**. The course box is at **(2623,-74)**; walking on the bottom floor does not reach it.

Shared physics uses a 50ms airborne step, jump impulse 27.75 pixels/tick and gravity 5 pixels/tick squared. Its continuous apex from y=7 is **-70.00625**, but the sampled minimum is **-69.5**, which rounds to **-69**. The current collision sweep therefore misses the one-pixel boundary. A diagnostic enumerated **1,313** launch-position/direction combinations along the overlapping ledges and found **zero** upper-platform landings. The live navigation probe agrees: bottom region 23 reaches staircase region 15, but cannot reach upper region 11 or the clothing-box platform.

The next fix belongs in shared airborne physics/collision sampling and corresponding baked graph validation, not a GPQ warp, special jump boost, executor tolerance or forbidden movement skill. Reproduce the boundary in a focused physics test, validate the whole landing envelope and existing movement regressions, then rebuild affected navigation caches under the graph-version rules before another normal-entry run. The source correction now sweeps both sides of the actual ballistic apex in the shared live, baked-landing and rope-grab collision paths. It preserves jump impulse, gravity, movement limits and pickup range. A regression first reproduced the failure, then verified legal 77-pixel landings and rejection of 78-pixel ledges across vertical/left/right launches; baked and live physics must land at the same coordinate. The real-map base-profile graph now reaches the trousers box without movement skills. Graph version is 78 so old movement data will be rebuilt. The server was subsequently saved, shut down normally and restarted with this fix. A read-only check in the new JVM confirmed graph version 78 and ordinary trousers-route reachability. Complete live GPQ repetition remains required.

The blocked run ended through ordinary client/event cleanup. All five companions withdrew from the test guild and logged off; the master retained the guild. A temporary test-only warrior naming mismatch was corrected offline to **GPqSpear1**, updating its matching fixture account atomically after registry, offline, ownership and collision checks. No player or autonomous-population account was renamed.

**Later live result:** clothing clear, sacrifice/death/revival, Ergoth combat, Rubian turn-in, timed bonus pickups and normal exit have now passed. A human-client GPQ audit remains outstanding. The first full-clear evidence below supersedes the earlier movement-blocker checkpoint.

The complete movement-executor replay also passed from both **(-413,7)** (entrance floor) and **(2625,185)** (the actual fallen worker position) to the box at **(2623,-74)**. The final validation included 60 physics, 47 graph-provider, 44 planner and 14 existing movement-lab tests, plus both GPQ course tests and the focused GPQ/observer/cure suites. The movement-lab process logged background timer warnings from its headless test environment; its test assertions passed.

## First full clear and final integration fixes

The first full clear occurred at **2026-10-24 21:39:37 UTC**. The native event reported `bossclear=true`, `stage1clear=true`, `stage3clear=true` and `stage4clear=true`; all four actual spear offerings had already opened the spear gate. The group completed the thirty-second bonus interval and all six reached the normal camp alive at approximately 21:40 UTC. Guild points rose from **170 to 1041** through ordinary stage and knight rewards. The headless leader's inventory actually gained bonus equipment and scrolls (including item IDs 1332053, 2040504 and 2041016), alongside normal potions. No reward was granted by the test harness.

The warrior retrieved the trousers through the repaired 77-pixel course and delivered them normally. The body consumed shoes, trousers, shirt and crown in order. The scout removed and dropped its earned protective earrings, died from ordinary map protection loss, and the real gate reactor enabled `canRevive=1`. The scout revived in map 990000200, earned replacement earrings and walked back through the cleared route.

GPQ now uses the existing boss-crew support checks at action-ready simulation ticks, preventing attacks from starving due healing or buffs. An idle GPQ priest may evaluate ordinary Heal, but retains skill, MP, disease, action-lock and cost gates; ordinary population idle behavior remains unchanged.

A thief death during Ergoth revealed that the generic PQ tomb wait ignored this event's explicit revive permission. When GPQ's own `canRevive` flag is one, the bot now uses the existing ordinary death-popup revival/return-map path even while teammates fight. Before the sacrifice, and in other PQs, the existing wait remains. The live thief revived normally before the final turn-in. No bishop resurrection, GM heal or owner warp was used.

A Haste-dependent Teleport loop also appeared on the final spear tower: a mage approached the two-pixel launch window while the next cast was cooling down, built momentum, and coasted off the edge despite releasing its direction key. The executor now waits on its current platform before approaching a vertical cast while cooldown remains. Horizontal cooldown walking is preserved; the strict launch window, normal cast interval, learned skill and MP rules remain. This is an execution-gate change, so graph version 78 remains appropriate. The real-map regression reproduced the fall to y=134 before the fix and stays on the y=-9 platform after it.

## Human audit route

1. Use WhereLevel's Guild PQ entrance and speak to Shuang.
2. With no guild, choose the BotPQ guild-creation help. Bring the normal 1.5M mesos, travel to Heracle, select Create a Guild, choose its name and confirm creation. The five cofounders supply the ordinary membership requirement; the player remains guild master.
3. Return to the camp with Heracle's BotPQ option or WhereLevel and assemble. Existing guild masters/juniors can assemble directly when five membership slots are available.
4. Select Shuang's normal Register your guild option. The leader enters when the guild is called; the companions use the normal Join option. Wait the real gathering timer and enter the lower portal first.
5. Earn and equip your own Protector Rock earrings. Let the crew explain and perform its roles; make the leader's NPC checks and enter the opened stage exits when ready.
6. After Ergoth, collect the Rubian and speak to the knight. The bonus room is timed; all boss/bonus loot belongs to the human. Bots help open the boxes.
7. At the camp, dismiss the crew when finished. The five bots leave the guild and log off; the guild and its earned points persist.

## Clean repeat completed

The second native clear was recorded at **22:02:33 UTC**, with `bossclear=true`, `stage4clear=true`, all four spear offerings and guild GP **1964**. Normal bonus timeout and return to camp were recorded at approximately **22:03:05 UTC**. The reward observer recorded **218 real bonus drop objects**, including item stacks and mesos, and inventory changes confirmed actual pickups. This is an observed run count, not a promised fixed reward quantity.

The same five fixture IDs (662, 663, 664, 683, 666) were reused. Ordinary dismissal had reduced the test guild to its master; normal reassembly restored six members without another creation fee and verified 200 Ginger Ales, 100 All Cures and the class-required rocks/ammunition. The priest carried 200 of each rock. The second run completed with all gameplay fixes loaded from entry and no mid-run changes or intervention. Only the required scout sacrifice caused a death on that repeat; all six exited alive.

After verification, the stand-in leader disconnected through normal cleanup. All five companions withdrew from the guild, saved and logged off. The master and earned guild points persist. The server remains online with the final fixes loaded; population configuration is unchanged. The final focused run passed **308 tests**, zero failures/errors/skips. No GPQ update was pushed to GitHub.

The clothing-ready notice names Sharen III's Grave and directs the leader through the far-end door of a clothing room to Sharen III's Soul. The soul is not in the main waterways map.

## Ergoth survival preparation (2026-10-25 human-audit fix)

BotGuildBossSupport gates the crew's crystal attack on the whole party being alive, in the throne room and gathered at its native entrance, with Hyper Body HP/MP and mage Magic Guard active. An event-local preparation latch permits normal departure toward the crystal afterward. Missing learned protection in this room is evaluated in BotCombatManager.tickBuffs before generic rebuff deadlines; casts retain the shared support dispatcher, WZ costs/range and ordinary action restrictions. During combat, missing Hyper Body causes regrouping with the living provider, who approaches unprotected allies. The low-level scout retains its noncombat role. Alive GPQ companions evaluate both bound potion controls, including MP depletion after a deadly attack. The shared damage-handler regression verifies both stats reach one and both stat-loss deltas reach ordinary autopot. No boss damage, class/level, resurrection or player mechanics were changed. Build and targeted regressions passed; improved live Ergoth survival awaits the next human audit attempt.
