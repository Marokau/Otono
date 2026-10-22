# Ellin Forest / Forest of Poison Fog companions

The owner's reference guide covers this PQ in Chapter 20, "Forest of poison fog".
The local event script is authoritative where versions differ: four to six level
44-55 Explorers, 30 minutes, recruitment map 300030100, NPC 2133000.

`BotEllinCompanions` assembles a human leader with reusable level-45 Spearman,
Cleric and Assassin fixtures named EPqSpearN, EPqMageN and EPqThiefN. The isolated
bot-only test also uses an EPqArcherN. The pool never borrows population or owned
bots. Preparation uses the shared class/build equipment optimizer, AP/SP plans,
200 Ginger Ales, 100 All Cures, skill consumables and Ilbi ammunition. Re-entry
refreshes supplies. Outside the event, registered companions wait at recruitment.

`BotEllinRun` uses ordinary movement, attacks, NPC conversations and portals:

- 930000100: defeat all 50 poisoned trees before the exit opens.
- 930000200: collect 4001161; drop one at pond reactor 3002000 to obtain 4001162;
  feed four diluted poisons individually to reactor 3009000 (`spine`). Its normal
  five-second item-consumption delay is respected; bots cannot reclaim pending
  reactor inputs. Completed dilution takes priority over fresh respawning drops.
- 930000300: try actual random maze portals. Recognize Ellin only after arriving
  near her, then use her ordinary NPC team transition. Hidden outcomes are not read.
- 930000400: obtain 2270004 from Ellin, weaken 9300175 below the player handler's
  strict 40% HP threshold, then send the ordinary `UseCatchItemHandler` packet.
  Combat selection and outgoing-hit checks stop attacking capturable flowers.
  Flowers revive if killed ordinarily. Deliver actual 4001169 marbles to the human
  leader, who turns in 20 to Ellin. Never answer the NPC's quit prompt to refill
  while the bot already has purifiers.
- 930000500: climb and break visually identical shuffled boxes, without choosing
  the hidden reward reactor ID. Deliver the actual 4001163 purple stone to the
  human, who speaks to the Sprite (2133004).
- 930000600: human drops the stone at altar 3001000. Fight 9300180, 9300181 and
  9300182 through normal combat/revives; event clear follows the third form.
  Original boss loot remains the player's. Use the right portal for rewards.
- 930000800: wait while the human optionally drops boss marble 4001164 into the
  fountain, then follow the human out through the normal portal. Handle this map
  even after the event manager has unregistered/disposed the crew.

Human stages have ordinary speech introductions before departure, progress and
handoff speech, and private pink readiness/clear notices. Delivered marbles and
stones remain available while their dedicated human crew is active in that map,
following the KPQ AFK-handoff policy. No position recovery teleport is permitted
inside Ellin maps; a bad route must be diagnosed rather than skipped.

Reactor item areas describe drop pixels, not standable player coordinates. Aim
at the real floor within the area using `BotPhysicsEngine.findGroundPoint` from
its top edge. The poison pond's geometric center lies below its floor. A real-map
regression verifies grounded approach points for both pond and boss altar.

Verification status is recorded with live results in the development journal;
implementation alone does not establish client acceptance or full completion.

Live verification: the four-fixture repeat run completed all stages, three boss
forms, real boss-marble fountain activation and the normal exit. Each fixture
returned alive at level 45 with one 4001198 Altaire Fragment. The three human
companions were saved offline at recruitment, and pool acquisition returned the
same character IDs. Human client acceptance is still pending; a temporary
human-role driver did not complete its NPC rehearsal, so it is not entry proof.

### Poison provenance / fresh attempts (2026-10-20)

The bot never grants poison as supplies; Spear collects concentrate from mobs and
uses the real pond reactor to dilute it before dropping at the barrier. However,
EllinPQ's exclusive list omits concentrate, permitting leftover inventory across
runs. EPq provisioning now clears 4001161/4001162 only before event entry, including
re-entry refresh. Current-run items and human inventory are unchanged. No entry
snapshot was available to prove the provenance of the owner's observed early drop.

### Purple stone handoff reservation (2026-10-20)

Stage Five's collector previously reclaimed its dropped purple stone through
passive loot or explicit collection. Human-led player-dropped stones now bypass
both bot pickup paths; original box drops remain collectible. Bots wait beside
the handoff and prompt the human to pick it up and speak to the Sprite. Existing
expiry protection is retained. Focused Ellin regression/build pass; loaded live.

### Visible cure reaction time (2026-10-20)

BotLpqCures uses BotPqCureDelay: wait three seconds after observing each active
curable ailment before consuming a real All Cure via UseItemHandler. Clocks reset
when ailments clear; overlapping new ailments get their own visible window.
This affects all PQ controllers using the shared helper, including Ellin, LPQ,
OPQ and Zakum. Ordinary combat/healing continue while waiting. Fifteen focused
tests and package pass; live loaded without restart.


### Poison Golem falling-rock coverage

BotClient forwards Ellin-instance monster movement to BotMobAttackObserver. The
observer accepts Poison Golem forms 9300180-9300182 during the active event, queues
supported type-0/type-3 attacks using their WZ attackAfter, and reports intersecting
hits through TakeDamageHandler with existing bot defense/accuracy and invulnerability
checks. Final-form attack3: PADamage 290, delay 2640 ms, 23 columns, no disease/stun.
Ground-only jumpAttack tremors remain unsupported by this observer. Golem masks
retain all four attack-option bytes; interpreting upper bits as additional columns
extends the established Zakum model and awaits a live Golem packet/video comparison.
Fourteen focused tests and package passed; loaded live without restart. Client
confirmation received on 2026-10-20: falling rocks work during the owner's run.


### Monster poison-cloud contact damage

BotPoisonMistDamage runs from BotMobAttackObserver.tick before its pending-attack
queue gate. It checks active monster poison Mist boxes against bot touch bounds,
active in event instances for monster-owned poison clouds, including Ellin Poison Golems and MPQ Frankenroid. The normal
cloud spawn packet supplies its exact AREA_POISON skill level; MobSkillFactory
supplies x damage (level 8: 50; level 5: 200). This preserves older clouds across
boss phase transitions without duplicating skill data. Reports use the shared
TakeDamageHandler mist type -4 and normal Magic Guard/autopotions, independently
of the All Cure disease helper and ordinary hit invulnerability.

Contact is limited to once per second per bot/map, with overlapping clouds using
the maximum damage. The cadence follows the owner's observed human damage;
precise client timing/overlap semantics await client comparison. Leaving a cloud
or its removal stops damage; dead bots and characters outside Ellin are excluded.
Four focused tests plus 14 observer regressions passed; loaded without restart.
Owner confirmed poison-cloud damage works in the client on 2026-10-20 and
verified Ellin PQ overall. Exact timing/overlap semantics have not been packet-captured.


### Fountain rewards and fragment exchange

Reactor 3008000 grants 52,000 base EXP on activation. Exit portal
party6_stage800 awards one 4001198 Altaire Fragment only when the fountain has
activated (state > 0), with an ETC-space check. NPC 2133000 exchanges 10 fragments
for Altaire Earrings (1032060); each successive upgrade costs another 10 and
consumes the prior earrings: Glittering (1032061), then Brilliant (1032072).
The bot's exit-map explanation calls the fountain reward "bonus EXP".
