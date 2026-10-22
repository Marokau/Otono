# Magatia (Romeo and Juliet) companions

The owner's `MapleStory PQ summaries.docx`, chapter 16, is a historical mechanics
reference, not executable instructions. Local MagatiaPQ_A/Z event, NPC, reactor,
portal scripts and WZ geometry are authoritative. Entry requires exactly four
participants, levels 71-85, with a 45-minute event timer. Romeo (2112004) in
Zenumist's hidden room (261000011) and Juliet (2112003) in Alcadno's hidden room
(261000021) expose assembly/dismissal alongside the original entry menu.

`BotMagatiaCompanions` prepares three dedicated fixtures: Priest (`MPqMage`),
Hermit (`MPqThief`), Crusader (`MPqSword`). The human remains party/event leader.
The shared `bot_pq_companion` pool (`mpq`) reuses eligible saved fixtures; owned
and managed population characters are excluded. Creation follows ordinary
level-up, AP/SP build allocation and job advancement to level 71. The gear
optimizer grants normal obtainable class equipment. Preparation supplies 200
Ginger Ales, 100 All Cures, Ilbi stars and learned-skill consumables. Normal entry
refreshes supplies for registered companions; it does not supply the human or
other parties. There is no item injection inside the event.

`BotMagatiaRun` owns only explicitly registered crews, including their lobby
waiting and ordinary failed-run/reward exits. Instance maps always use full
simulation. Ordinary respawn-camping doctrine is disabled inside events: a PQ
must clear required mobs across the room rather than camp one grinding ledge.
Combat, movement, pickups, inventory drops, NPC dialogue and portals
use existing player/bot paths. It never writes event stage/combo properties or
forces reactor state. Loss of human leadership/connection ends participation
through ordinary party departure and exit; it does not town-warp a live instance.

## Mechanics and guide differences

- Books are repeated NPC objects: 2112007 on the Zenumist side, 2112013 on the
  Alcadno side. `BotNpcInteraction.talkObject` addresses each visible object through
  NPCTalkHandler. Crew members claim different books and inspect them, rather
  than reading the hidden `stg1_b*` result table. The NPC awards the real letter
  and opens the stage-one reactor. The dark tunnel clears by killing its mobs.
- There are **three** beakers, not the guide's four. Each needs seven individual
  suspicious-liquid drops (4001132). Reactor `hit()` runs before the state change:
  its state-six check counts the seventh input, not the sixth. Each normal item
  activation takes five seconds; intermediate states leak after 9.5 seconds.
  The collector gathers seven before starting and feeds single drops without
  re-looting the pending item. Completed beakers reach terminal state eight.
- Card keys (4001133) are actual Neo Huroid drops. The left research room's
  2612002 box drops 4001134; the right room's 2612001 box drops 4001135. The Hermit
  searches left using Haste and the Priest searches right using Teleport. Magatia
  is included in the shared empty-map buff gates, so Haste renews during puzzles.
  Required movement skills retain their normal MP and field restrictions while
  exempt from the unrelated 500k-meso travel reserve.
  Both break ordinary visible boxes rather than targeting the prize by its template.
  A card opens a door for only two seconds: the collector checks that the
  specialist is waiting at the corresponding entrance/exit before spending it.
- The files are dropped beside the human. Bots reserve those handoffs from
  passive pickup and wait for the human's normal Romeo/Juliet turn-in. A found
  letter is delivered normally. In Yulete's office the collector approaches a
  point outside the NPC's 310px detection radius and talks within the ten-second
  window; the other bots wait at the entrance. The human should also stay back
  until that conversation finishes. Actual scripts decide normal/angry boss.
- Each player takes a distinct puzzle room. Bots learn by trying portals and
  observing their resulting landing row, retaining successful columns and failed
  trials. Every row uses the four columns once across the four rooms, as generated
  by both local event scripts. Confirmed teammate columns allow elimination;
  bots never inspect `stage6_comb*`. Once three bot columns are confirmed, the
  human receives the remaining platform (1-based from the left), including when
  the human enters after those observations were made.
- Bots wait for the human's boss start at Yulete. The Priest stays upstairs and
  targets 9300150 threats to the friendly Romeo/Juliet; the other companions fight
  Frankenroid. All Cures use normal consumption. Marble, Yulete rehabilitation
  dialogue, random reward and return use the original NPC scripts; humans claim
  their own rewards.

## Environmental HP drain

Research rooms apply periodic HP loss through `Character.doHurtHp` / `addHP`,
which does not call the shared stat-loss autopot trigger. This killed the first
Hermit despite its carried Ginger Ales and ended the four-person event normally.
`BotPotionManager.tickPotionCheck` now observes live environmental-drain maps
and invokes `Character.triggerAutopotAfterStatLoss`, honoring the existing key
binding, threshold, inventory and ordinary potion-consumption path. The Magatia
controller also checks carried HP recovery between actions, including after a
failed attempt. No HP is awarded directly and death remains effective.

## Verification boundaries

Server-side tests cover explicit registration isolation, observed puzzle trials,
cross-room elimination, delayed human hints, both event combo generators, the
real seven-drop reactor callback, both assembly menus, normal entry resupply,
human room-reservation races, normal movement-skill restrictions and environmental
potion polling. Nearby PQ/manager regressions are included.

A dedicated four-bot Zenumist run completed through **Angry Frankenroid**, normal
marbles, Yulete rehabilitation dialogue, individual random rewards and the normal
return to Romeo. All four were alive, each held one Zenumist marble (4001159),
and the reward dialogue took the successful-escort branch. They started at 71
and finished at 76 after the development retries and successful run. No GM
revives, stage edits, prize grants or boss kills were used. The test fixtures
were absent from both `managed_bot` and `bot_owners`. Evidence is in ignored
`target/magatia-live-test/rewards.txt`, `angry-boss-midfight.txt` and `puzzle.txt`.
Both recruitment scripts and their normal entry/resupply paths have automated
GraalJS coverage; the full live clear above is the Zenumist variant. A non-GM
regular-Client stand-in also selected Romeo's assembly option, reused the saved
Priest/Hermit plus `MPqSword1`, became leader of the four-person party, entered
through the original NPC check and progressed through stage one. That partial
human-role run ended through the normal retreat dialogue; all participants were
saved offline in the lobby and temporary input/diagnostic hooks were restored.
The owner verified the complete human-led MPQ run on 2026-10-21 and explicitly confirmed poison-fog tick damage works in the client.

The incoming-attack observer covers supported Zakum and Ellin attacks; MPQ poison-cloud contact uses the separate shared mist handler. Frankenroid's
projectile 1/1 and airborne-avoidable ground attacks have different WZ shapes;
this implementation does not guess collision reports for those. Server-owned mob
skills and contact/environmental damage retain their normal paths. The owner accepted the human-led encounter; that acceptance does not establish simulation coverage of every distinct client-owned attack shape.

## Headgear provisioning
MPQ crews follow the shared PQ special-headgear exclusions, including Wild Wolf Bandana (1003016), Zakum helmet variants, and the existing excluded special helmet. Existing excluded hats are removed during lobby provisioning and excluded from future gear candidates. Replacement hats must meet normal level/stat requirements; ordinary all-class hats remain eligible. Provisioning explicitly equips the selected replacement through InventoryManipulator.equip. Verified live on MPqMage1, MPqThief1 and MPqSword1 in the recruit room: all wore Maple Hat (1002511) afterward. Population bots are outside this change.


MPQ assembly provisioning also tops The Summoning Rock (4006001) up to 200 per companion. Existing learned-skill resupply does not reduce that stock. Owner explicitly requested this supply, matching the 200 Ginger Ale target. Live crew supply refresh uses ordinary inventory additions and is scoped to registered MPQ companions.


Navigation cache warmup/fallback notices are suppressed for bots in event instances. Existing OPQ-specific suppression is retained; graph warmup and movement behavior continue unchanged.


Stage six: each human-led companion walks to its reserved puzzle entrance and drops 10 of its own mesos through MesoDropHandler before entering. A per-event room key prevents repeated markers; event binding clears those keys for a new run. Assembly guarantees a minimum 10-meso marker budget. Already occupied rooms are not marked remotely.


Maze failure recognition: wrong first-row portals return to row zero, so row-number changes alone cannot detect failure. After an actual portal-use timestamp change, compare observed positions and landing row; a same-map landing on the same/lower row records that attempted column as failed. Walking alone is not an attempt. The solver never reads the hidden stage6 combination. Regression tests cover same-row failure and ordinary walking.


Human puzzle clues explain that each corresponding row uses all four platform positions exactly once across the rooms. Hints identify the human room from the leader's actual maze map, not stale occupancy flags, and stop once the leader leaves the four puzzle-room maps.


MPQ learned-skill consumables now refill to 200, dynamically from skill itemCon (including Magic Rocks 4006000 as well as Summoning Rocks 4006001). Both Romeo and Juliet entry scripts call BotMagatiaRun.refresh before starting a new event; crew recreation is not required. The shared low-rock warning threshold is 100, so the earlier 100-item Magic Rock target caused a warning immediately after consumption. Other crews retain their existing supply targets.


Player-dropped mesos used as stage-six route markers do not expire while the MPQ event remains active/uncleared. Applies to both sides' stage-six entrance map and four puzzle rooms (926100300-304 / 926110300-304), for human and bot drops of any positive meso amount. Monster money, item drops and other maps keep normal expiration. Once cleared, normal expiration resumes; normal instance disposal also clears markers. Pickups remain ordinary.


## Ailments and poison fog
MPQ uses the common BotPqCureDelay / BotLpqCures path: each newly observed curable ailment must remain visible for at least three seconds before an All Cure is consumed. Stun is not cured by an All Cure. The owner requires this timing for PQs, bosses and expeditions; future implementations must reuse the common policy.

Monster poison Mist contact damage is now simulated for active event-instance bots, including Frankenroid's AREA_POISON level 6 clouds. BotPoisonMistDamage previously excluded all non-Ellin instances and non-Poison-Golem owners. It now checks real monster poison clouds overlapping bot touch bounds, reads each cloud's skill damage and reports one contact tick per second through TakeDamageHandler, retaining Magic Guard/autopotions and overlap deduplication. Player-cast mists are excluded. Mist contact damage is separate from curable poison disease. Live redefinition succeeded, and the owner explicitly confirmed MPQ poison-fog tick damage in the client on 2026-10-21.


## Completed-crew cleanup and reuse
Completed MPQ reward/boss-exit dialogue runs before human-party validation and leader-position briefing waits. A player returning to recruitment or changing/disbanding the party must not strand companions in reward map +600. Companions finish their normal reward NPC dialogue, return to recruitment, and detached companions are then saved/dismissed. Verified first crew IDs 609/611/612 saved in 261000011 with loggedin=0, no manager registration, jobs 111/231/411 and levels 71/76/77: eligible for reuse, not overleveled.


Special-lab/research-room keys and research files (4001133-4001135) remain on the ground while the MPQ event is active and uncleared, for both sides' maps +200..202. Protection includes monster/box drops and player/bot redrops, so an AFK handoff does not brick the run. Ordinary pickup/reactor consumption still works; cleared/disposed events do not retain these items indefinitely. The normal event timer still applies.

