# Kerning PQ bot coordination

`BotKpqRun.startCrew` explicitly opts four headless characters into a run. It validates
level 21-30, channel/world, and party membership before assembling them. It does not
recruit or reconfigure autonomous population members. `BotPqProvisioner` supplies the
owner-authorized crew with 200 Ginger Ales (2002023), existing first-job ammunition,
and clean gear selected by the existing equipment optimizer from normal drop/shop
and starter-kit catalogs. Equipment requirements, AP/SP, and levels stay intact.

`BotNpcInteraction` reads NPC dialogue packets delivered to `BotClient` and submits
NPC talk/continuation inputs through player handlers. Stage-one workers interpret
Cloto's actual question wording, collect exact coupon counts, exchange through the
NPC, and drop their pass near the registered party leader. The leader collects
passes and submits the normal clear check. Completed workers continue combat support.
There are no bot-side question-grid writes or coupon-to-pass grants.

`BotKpqPuzzles` enumerates three-worker placements and uses Cloto's ordinary response
to judge them. `KerningPqGeometry` shares unchanged placement rectangles with the NPC
script; the bot does not read the winning combination. Stage-five combat/pass
collection and NPC reward continuation are implemented in the same coordinator.
Two complete live runs verified all five stage clears and all four NPC rewards,
with all four characters reaching bonus map 103000805 alive. The dedicated crew
was KpqGuard (warrior), KpqMage (magician), KpqArrow (bowman), and KpqShade (thief),
starting at level 21 and finishing at levels 26/27/27/26 with world EXP rate 15.
Both crews used Nella to return to Kerning and were saved offline after completion.
The second crew completed without deaths and retained usable ammunition through
King Slime. Further repeats were stopped at the owner's request to begin human play.

Registered KPQ instances stay at full simulation, and are excluded from LOD motion
plans/abstract kills. Consuming PQ hooks run after shared skill-cache, buff, and
action-lock prerequisites; putting them earlier starves those updates. Completed
loot movement pins must be cleared before returning to normal combat.

Live verification established party assembly, Lakelis entry, registered instance
membership, actual Cloto questions, real kills with zero abstract kills, exact coupon
exchange, dropped-pass handoffs, puzzle placements, portals, King Slime combat, and
reward continuation. No in-instance revive or stage bypass was used. An earlier
under-supplied crew failed stage one; dedicated provisioning resolved that failure.

Final-stage enemies span more height than the normal 800-pixel grind search radius.
With no local target, `BotKpqRun` navigates toward the nearest remaining live enemy
using shared movement, then hands combat back to the existing grind controller.
Without this map sweep, bots idle on the upper floor with nine passes while King
Slime remains untouched below.

Equipment candidate pruning must preserve different weapon families: raw WATK
cannot establish dominance across different damage formulas and skill/preference
ranks. `pruneDominatedWithReqs` compares weapon types before discarding candidates.
Live dedicated first-job fixtures now select a polearm, wand, crossbow, and claw;
crossbow provisioning includes bolts as well as the ordinary bowman starter arrows.
The starter 500 stars and 1,000 bolts exhausted during the completed run; subsequent
pre-entry provisioning supplies 5,000 rounds, with rechargeable ammunition split
according to the item provider's legal stack maximum. This larger supply was built
and verified through a second complete live run.

`BotPqCrewFactory` creates explicitly requested level-21 explorer fixtures through
normal character creation, level-up, job-advance, AP, and SP paths. Their initial
saved spawn is Kerning City; they are not enrolled in the managed population, so
the normal population scheduler does not own their sessions. This is owner-authorized
fixture setup outside the PQ, separate from stage execution.

Warrior fixtures defer AP spending until level 21 and plan against King Slime's
actual level/avoidability using the shared accuracy inversion. Explicit PQ preparation
and human-led crews target 90% hit chance; the population's 25% floor stays unchanged.
Allocating AP on the beginner map produced 7 DEX/8 accuracy and a measured 1% hit
chance against level-32 KPQ Ligators. The original bot-only runs concealed this because
the warrior was the non-fighting stage-one leader. A freshly prepared level-21 warrior
with legal optimized gear measured 70 accuracy and 100% calculated hit chance against
both Ligators and King Slime. Other classes retain AP spending during leveling,
including INT-dependent magician MP growth.

Lakelis (9020000) offers assembly and dismissal. `BotKpqCompanions` reuses eligible
dedicated companions before creating level-21 replacements, omitting the player's
explorer class family where possible, and uses shared party creation/join paths to
make the player leader. Existing multi-member parties are never replaced. Players
level 21-30 in Kerning can assemble, reopen Lakelis, and choose normal participation.
`bot_kpq_companion` persistently records dedicated crew IDs, separate from population
and player ownership. `BotKpqPool` selects the lowest available named companion on
the same world, level 21-30, saved in Kerning or Nella's exit room, and offline in both account and runtime
state. Managed population members and player-owned characters are excluded.
An eligible exit-room fixture logs in there and uses the existing Nella dialogue to
return to Kerning; recruitment does not warp it directly or change its saved location.
Characters disconnected inside KPQ can be saved at this forced-return map, so requiring
Kerning alone incorrectly skipped reusable companions and created new suffixes.
The original six numbered fixtures were registered from verified NPC creation records.
Repeated live selection returned the same number-one IDs without creating characters
or disturbing the active number-two crew. Equipment, AP management, potions, and ammo
are prepared through the existing assembly path when a reused bot logs in.
Names use `KPqSword`, `KPqMage`, `KPqArcher`, or `KPqThief`; a new suffix is allocated
only when no existing registered companion is available and eligible. Active
companions keep their existing names until dismissed.
The player collects passes, performs Cloto checks, enters opened portals first, and
claims their own reward. Companions do not auto-enter. They follow opened portals
after the human, announce ready puzzle placements, and change arrangements only
after an ordinary failed NPC check while all workers are in position. They leave
bonus after the human, and dismiss on party loss/disconnect or the explicit NPC option.
`BotPqDialogue` gives human-led crews one shared stage guide, individual coupon
milestones, successful pass-drop announcements, and a Cloto prompt once the human
actually holds every required pass. Milestones reset by instance/map and are not
repeated every tick. Individual progress counts coupons; the leader needs three passes.
KPQ guidance goes through `BotPqChat` into the ordinary speech-bubble queue. At delivery
it checks the originating instance/map and, for unfinished-stage hints, the clear flag.
Queued puzzle readiness therefore expires on clear or portal travel instead of being
spoken in King Slime's room. Ordinary social lines and supply requests are not tagged.
Dedicated provisioned PQ companions suppress unsolicited loot/gear offers to humans
through `BotOfferManager`; sibling-bot offers and bot-initiated trades are also disabled. Human-initiated trades and separate supply requests remain available.
New companions use a dedicated login path without population autopilot; shared AP
auto-assignment is explicitly enabled. Normal assembly, waiting for entry, registered
instance entry, early exit, and dismissal were smoke-tested with a separate human-client
fixture on channel 2. The owner subsequently completed human-led KPQ and confirmed
the stages and guidance worked; later dialogue and expiry refinements have separate test coverage.

Existing read endpoints (`/api/live`, `/api/botdebug`,
`/api/mapinfo`) remain unchanged. An ordinary spectator is not registered merely by
warping to an instance's map ID.

Companion guidance uses two channels consistently with LPQ: ordinary bot speech for objectives, actions, progress and personal handoffs; private type-5 pink/purple KPQ notices for all passes delivered, leader pass readiness and stage clears. Notices are emitted immediately and once per stage/run state, avoiding delayed stale clear announcements.

Earned stage-one passes dropped by a human-led KPQ companion are protected from the normal three-minute ground expiry while that companion remains in the same active instance map. Shared map expiry consults BotPqDropPolicy; ordinary drops, coupons and other PQs retain normal expiry. Stage-one human drops retain normal expiry. Protection ends when the companion leaves the stage, exits the instance or is dismissed. No pass is generated or replaced; normal pickup and Cloto submission still apply. Already-expired passes cannot be restored by this policy.

Final-stage workers wait while an uncollected pass remains above instead of descending to King Slime. If the human enters the boss section while the designated speaker remains above and passes are still on the upper platforms, that bot explains the wait once and warns that passes can hide near portals. The boundary is the WZ in01_1 portal at y=-743. Collection clears the wait and workers resume seeking remaining monsters. Regression: bossStageWorkersWaitForUpperPassThenSeekSlimeAfterPickup.

Final-stage map 103000804 also protects every earned pass (monster drops and player redrops) while a human-led KPQ companion remains in that instance map. This covers all upper-platform and King Slime passes regardless of who killed the monster. Normal pickup and the ten-pass Cloto check are unchanged. Protection ends when the crew leaves; already-expired passes are not recreated.

Bonus-room guidance describes hunting monsters for extra EXP, then talking to Nella to leave. Map 103000805 has monster spawns and no box reactors.
