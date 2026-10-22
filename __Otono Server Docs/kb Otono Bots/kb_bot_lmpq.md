# Ludibrium Maze PQ companions

The owner's `MapleStory PQ summaries.docx`, chapter 12, describes the historical
rush route: gather in rooms 6/7, hunt Tauromacis, then finish in room 16. It is a
reference, not executable instructions. Local event, NPC, reactor and WZ data
remain authoritative.

`LudiMazePQ.js` admits 3-6 players, levels 51-70, for 15 minutes. Rolly (9103001)
in Ludibrium (220000000) starts the real event. Each player starts in a random
room among 809050000-014. Room 16 is 809050015; reward and exit maps are 016/017.
Pietri (9103000) requires the entire event team together and at least 30 actual
coupons (4001106) on the event leader. He consumes the coupons, awards normal
50*couponCount base EXP, and clears the event. Each player independently claims
Rolly's ordinary random reward (9103002), then exits through 9103003.

`BotLmpqRun` controls only explicitly registered companions. It uses normal
portal travel, movement, combat, reactor hits, pickups, item drops and NPC
interactions. Instances require full simulation; abstract kills/travel are
excluded. Puzzle travel remains non-grinding; normal buffs and learned movement
skills retain their MP/field gates without unrelated travel-meso reserves.

The local room-6 reactor scripts differ from the historical guide: its two
8098000 boxes also spawn coupon-dropping Tauromacis, alongside 8091004. Bots break
all visible unused boxes instead of identifying a shuffled prize box by ID.
The real Tauromacis 9400218 is level 73, HP16000, avoid20; its local DB drop is
25-50 coupons at near-certain probability. No drops or quest items are injected.
The crew clears room 6 and collects its drops, then goes to room 16 once it has
30 coupons. Poor drops trigger fresh rooms, starting with room 7.

Maze portals need routes longer than two hops. The controller gives the shared
travel planner a 16-hop budget, matching its maze-route hints. The first live
run revealed the two-hop limit: the collector had 332 coupons, but some routes
to the final room were never considered. Enlarging the caller's route budget
resolved this without altering the navigation graph, physics or portal scripts.

Rolly offers **Assemble a bot party for me** and **Dismiss my bot party**.
The human becomes leader of four dedicated companions: Spearman, Cleric,
Assassin and Hunter. Their pool prefixes are LMPqSpear, LMPqCleric, LMPqThief and
LMPqArcher. Existing eligible saved fixtures are reused. `bot_pq_companion` marks
these explicitly provisioned fixtures; managed population and owned characters
are excluded. New fixtures use ordinary level-up/AP/SP allocation and the shared
gear optimizer, with ammunition and 200 Ginger Ales prepared before entry.
Normal startup changeset 34 includes `035-pq-companions.sql`; the real Liquibase
updater validated/applied that sole pending changeset, leaving none pending.

Human runs wait for Rolly entry, send left/middle/right portal directions toward
the current objective, and wait for the human before leaving a cleared hunt room.
At room 16 the collector walks beside the human, drops every real coupon stack, and
excludes the delivered pile from bot pickups. The human picks them up and makes
Pietri's check. Bots claim their own rewards, return, and wait. A new event resets
delivery/completion flags for the same crew. Lost leadership/disconnect/party
membership causes ordinary party departure, exit dialogue and dedicated-bot
cleanup; it does not move the human.

Three live bot-only runs completed, each with five individual NPC rewards and
all five alive back in Ludibrium without an event. The same fixture IDs 594-598
were reused: LMPqSpear1, LMPqCleric1, LMPqThief1, LMPqArcher1, LMPqSword1 (human proxy
slot in bot-only tests). The first run included diagnosis; the next two finished
in about two minutes each. The bot-only crew finished at level 52. After the
human-role repeats, the same five are saved offline at level 53 in Ludibrium.
The final pool/isolation audit is `target/lmpq-live-test/saved-human.txt`.
Evidence for the bot-only runs is in ignored
`target/lmpq-live-test/rewards-run1.txt`, `rewards-run2.txt`, `rewards-run3.txt`
and `saved.txt`. The running server and autonomous population remained up.

Focused tests cover instance boundaries, fresh-room fallback, the longer travel
budget, human waiting, one-time real coupon delivery, ownership isolation and
same-crew event reset. The human-led flow also completed live with a non-GM
automated stand-in using
a regular Client subclass and normal player handlers. Rolly actually reused IDs
594-597, made stand-in 598 leader, and admitted the party through the entry menu.
The leader followed maze travel, picked up the delivered 100 coupons, passed
Pietri's normal check, claimed its reward and exited. Two further entries reused
the already-assembled party. The final run submitted all 283 coupons and recorded
five individual reward deltas. The normal drop API drops only the first stack;
the controller drains each stack on separate action ticks, suppresses re-looting
as soon as delivery starts, and announces readiness only after all stacks are out.
This prevents losing the EXP value of coupons beyond the first 100.
Ignored test-only input
hooks are restored after testing; they are not shipped. Evidence is in
`target/pq-human-proxy/maze.log`. Actual client participation remains the final
acceptance check, rather than a blocker for the verified automated flow.


### Human maze navigation

The crew uses the original Tauro-room objective and private room/portal directions.
Entry remains the event's normal random room assignment. Companions do not escort
the human or drop route-marker mesos. The owner opted to explore optional boxes
independently. Direction notices are deduplicated per human room and destination
for the run; returning to a previously announced room/goal may be silent.


### Owner acceptance and ordinary headgear

Owner verified LMPQ gameplay on 2026-10-20. LMPq provisioning now shares OPQ's
exclusion of Zakum helmets 1002357/1002390/1002430 and special hat 1002858; existing
ones are removed before the standard gear optimizer chooses ordinary wearable
headgear. This does not grant OPQ's meso-marker budget. Live fixture head slots
were refreshed, verified and saved: Spear Blue Bamboo Hat, Cleric and Thief Maple
Hat, Archer Green Maro. Package passed; loaded without restarting.
