# Dedicated Zakum companions

## Acceptance status - human verified 2026-10-22

The owner completed the final client audit and explicitly accepted the twelve-participant Zakum raid (human plus eleven dedicated bots, two parties of six). This includes the audited ranged arm/platform behavior and kill order, melee assistance/waiting and movement fixes, Hurricane's visible held bow, stuns/ailments, knockback, resource preparation and phase guidance. Earlier pending-audit notes below preserve the sequence of work and are superseded by this final acceptance for the current crew.

This records the owner's live gameplay acceptance, not a new automated end-to-end test, universal coverage of every WZ attack variation, or support for Horntail/Pink Bean. The deferred mage town-portal feature remains separate. No new Git commit or publication is implied by this status update.

## Reuse for Horntail and Pink Bean

Carry these lessons into future boss crews: ordinary NPC/expedition entry, reusable
dedicated fixtures, human prerequisite checks, class-appropriate gear and earned
AP/SP/books, per-attempt supplies derived from learned skills, and loot reserved for
the human. Never borrow or retask autonomous population bots.

Headless clients need the normal buff-expiry, disease-expiry and skill-cooldown
tasks. Support actions need ready action-boundary checks so repeated attacks cannot
starve healing or rebuffs. Stun/seal must prevent the same inputs as for a player;
summons remain independent after being legally cast. Preserve normal potion costs,
resurrection range/cooldown and event death/exit behavior.

Use the shared player handlers for damage, ailments, attack effects and summon
packets. Supply client-side damage rolls and collision reports in bot adapters.
Attack bytes may pack action/options rather than just direction: verify packet
shapes and real client stability, not only server HP changes. Aim at actual WZ
hitboxes, including offset parts, and assess safe paths as well as safe endpoints.

The current incoming-hit observer, summon adapter and Hurricane timing opt-in are
Zakum-scoped. Horntail/Pink Bean need their own verified WZ templates, cast options,
collision timing, platform tactics and boss-specific status/immunity behavior.
Reuse the mechanisms, not Zakum's IDs, coordinates or damage constants. Explicitly
test their debuffs, damage reflection/cancellation and whole-fight survival before
claiming parity. The unsupported Zakum projectile/tremor cases below are not solved
automatically by reusing the observer.

## Entry and ownership

`BotZakumCompanions` supplies eleven opt-in, reusable, non-GM companions at Adobis
in the altar entrance (211042400), forming a twelve-participant expedition in two
ordinary parties of six. The human leads party one with Dark Knight, Bishop,
Bowmaster, Night Lord and Hero. A second Bishop leads party two with Dark Knight,
Bowmaster, two Night Lords and Hero. The human remains expedition leader.
Adobis creates the ordinary expedition;
the companions register through his dialogue. The human starts it and drops their own
Eye of Fire at the altar. The six-bot test adds an I/L Archmage instead of a human.

Human eligibility still requires level 50+, approval quest 100200 started/completed and
trials quest 100201 completed. Bot preparation grants only the dedicated headless crew
those prerequisites, skill unlocks and normal supplies. Existing portal and expedition
checks are unchanged. Autonomous population characters are not candidates for the pool.

`bot_pq_companion` pool markers and `BotPqCompanionPool` isolate these fixtures. They
start at level 150 and use normal job/level AP and SP, existing build plans, the ordinary
wearable gear optimizer, and actual mastery-book requirements, consumption and success
rolls. Preparation stocks 600 Ginger Ales, 100 All Cure Potions, five Eyes of Fire and
12,000 Ilbi Throwing-Stars for the Night Lord, replacing the shared kit's 15-attack
beginner stars. `BotPqEquipmentPolicy` keeps class-inappropriate
weapons out of dedicated crews without changing population equipment preferences.
Learned skill consumables are stocked from every learned skill's item requirements,
including summons and attacks, 200 per required item. Mages additionally receive both Magic Rocks and Summoning Rocks at 200 each. Live inspection found level-30 Shadow Partner inactive with zero
Summoning Rocks (4006001), despite normal Triple Throw and other rebuffs.
The dedicated Bishop uses the `boss-support` SP variant: early Holy Shield and one
Resurrection point, with the same earned SP and mastery-book rules. The default
population build is unchanged. Holy Shield is classified as party support and uses
normal cast range, duration and cooldown.

`spawnPqCompanion` registers runtime ownership under the human, rather than leaving the
bot self-owned and free to resume autonomous travel. Dismissal saves and disconnects
the crew. The altar and return route retain their dedicated controller even when the
human disappears; return uses Amon and the actual door portal.

The entrance map's saved return location is 211041900. A normal logout there therefore
does not persist 211042400. The Zakum pool accepts saved locations along the approach
(211041900 through 211042400, including cave side passages). After preparation, Adobis
summons each dedicated companion to the entrance's `west00` portal through ordinary
`Character.changeMap`, as explicitly authorized by the owner. This does not enter the
boss instance or transport the human. Requiring the recruiting map in the pool query
silently creates another numbered crew on every assembly despite the original crew
being offline.

## Fight adapters

`BotZakumBattle` owns registration, real reactor offering, combat, victory and exit.
It does not spawn monsters or write event-clear flags. Before summoning, companions
gather for normal Hyper Body and mage Magic Guard casts. `BotZakumSupport` uses the
ordinary SPECIAL_MOVE handler for Bishop resurrection and respects learned skill,
MP and cooldown checks. Human-led victory reserves the drops and waits for the human
to leave through Amon. Bot-only runs use ordinary pickup and exit paths.

`BotZakumCombat` selects reachable platform stances from the baked navigation graph.
Movement stays in the shared motor/physics path, and damage goes through shared attack
planning and player damage handlers. Approximate stance selection does not grant reach:
the exact attack hitbox still gates every planned attack. Percentage-HP attacks such as
Dragon Roar are avoided in the altar because repeated casts exhaust the potion supply.
The Bishop prioritizes summons and moves to reach them rather than accepting an
in-range body attack. Other attackers prefer the body while it is damageable. Matching
weapon/magic immunity excludes targets instead of spending whole immunity windows
on one-damage lines. Stable object-id tie ordering avoids insertion-order changes
at body transitions deciding which equal-priority target gets attacked.

Zakum's separated arms share the same monster position. With valid WZ bounds, allowing
an attack merely because its rectangle contains that position incorrectly hits upper
arms from the floor. `doesHitBoxIntersectMonster` uses actual bounds whenever available;
position is only the missing-bounds fallback. Mob WZ `noFlip`/fixed-stance shapes must
not be mirrored again by facing. The inactive fake body is neither a target nor a
contact-damage source.

The bot client's map-load acknowledgement must clear `isChangingMaps` before bot hooks
run. Otherwise normal death/portal packets can be rejected indefinitely after entry.
This matches the real player's map-load acknowledgement and does not change location.

Shadow Stars has a timed SHADOW_CLAW statup but no WZ action node. It is still an active
support buff; requiring an action node or skillType=2 alone silently omits it and burns
the Night Lord's star supply. The ordinary handler still requires ammunition in a
sufficient individual stack. Several tiny remainders can sum to enough ammunition for
the bot's aggregate count while being unusable for a particular attack.

## Verification limits

Live testing has exercised ordinary expedition registration, entry, Eye-of-Fire reactor
summoning, all eight arms, defeat of the first body and damage to the second body with
all six crew members alive. Full boss defeat and rewards are not yet verified.
Early exploratory damage preceded correction of the arm-origin hitbox bug; that run is
not clean evidence of a complete corrected fight. Focused tests cover human prerequisite
preservation, preparation restrictions, summon buff readiness, fake-body exclusion,
separated arm geometry, NPC menu compatibility and Shadow Stars classification.

The server excludes headless BotClient characters from normal monster-controller
selection. A bot-only fight therefore does not exercise the complete real-client boss
spell/attack loop. Human-led testing is necessary for dispel, seal and other spell
behavior; bot-only damage progression alone is not evidence of full combat equivalence.
Human-led testing observed the Bishop's ordinary Resurrection cooldown active and the
revived companion alive. Victory loot reservation still requires live confirmation.

Human-led testing confirmed real-client monster control, boss status buffs and carried
All Cure consumption. It also exposed an alive Dark Knight stuck with `deadUntil` still
armed: `handleDeadTick` continued waiting for teammates despite positive HP, starving
combat and Hyper Body. Clear this bookkeeping hold when HP is positive, without changing
HP or invoking respawn. Live application resumed attacks and ordinary Hyper Body casts
to the human and nearby companions. Regression coverage distinguishes actual zero-HP
respawn from an already revived character returning to normal ticks.

The human also observed falling pillars stunning their character while companions were
unaffected. Incoming attacks are currently incomplete: bots generate contact/fall hits,
but do not simulate the client-side collision and TAKE_DAMAGE reports for boss area
attacks. Ordinary `TakeDamageHandler` looks up `MobAttackInfoFactory` to apply the attack's
disease through the shared mob-skill path. Zakum body `attack3/info` is type 3, delay
2560 ms, 12 area slots with six attacks, rectangle (-60,-150)..(60,0), and STUN disease
123 level 3. Those declarations alone do not establish which actual pillars were selected
for a particular cast. A completion with bots immune to these hits is not full normal-boss
combat verification. Bot input handling also needs to respect an actual stun once received.

The October 17 client recordings and matching packets established the pillar attack
option mask. For example `0x0658` selected columns 3,4,6,9,10 in the clip. Columns
use the WZ width (120), start (-6), and cell centers; cast position (-9,-216) gives
centers (-309,-189,51,411,531). Captured pillar hits followed casts by approximately
2560 ms, matching `attackAfter`. `MoveLifeHandler` previously zeroed the attack-option
bytes in broadcasts by forwarding only mob-skill ids. Preserve ordinary attack options
so observer clients receive the controller's actual mask.

`BotMobAttackObserver` now queues observed physical area/pillar and deadly attacks,
checks collision at the WZ delay, and reports TAKE_DAMAGE through the ordinary player
handler. Physical attack PAD overrides reuse the touch-defense formula. Shared disease,
Magic Guard, damage and carried autopot handling remain authoritative. Pending attacks
are discarded after map/event departure, and contact/area hits share the hit cooldown.
Stun now blocks actions and clears queued movement inputs while continuing physics and
potion checks; seal blocks skill attacks, heals, buffs and resurrection.

The first adapter deployment covered body physical pillars and deadly attacks only.
October 18 live tracing confirmed a selected pillar dealing 2318 HP and applying STUN
to the archer, while other companions stood in unselected columns. Holy Shield does
not block STUN or SEDUCE in this server's shared `Character.giveDebuff`; it does block
seal. A shield value alone must not be used to explain absent pillar stun.

Incoming magic now uses `BotIncomingMagicDamage` rather than physical PAD. The
[pre-BB client-research formula](https://www.southperry.net/showthread.php?tid=1033)
was cross-checked against eight fresh real-client body-two lightning reports:
MAD 550, human MDEF 258, STR 27 / DEX 111 / LUK 755 predicts 2160..2312 damage;
observed positive hits were 2166..2290, all inside the range. Learned elemental
resistance and the shared character avoid roll are retained. Actual HP/MP changes,
Achilles, Magic Guard and ailments still go through `TakeDamageHandler`.
The observer includes arm templates: arm 8800006 ice columns use PAD 515 and a
3280 ms delay. Projectile attacks (type 1) and grounded-only tremors (`jumpAttack`)
are still excluded pending their distinct client collision rules. This is not a
claim that every boss attack type is supported, nor is column/platform edge parity
fully verified. Recordings are under the supplied Zakum recordings; packet/state evidence
and extracted frames are ignored diagnostics under `target/zakum-video-review`.

The headless login path omitted both `buffExpireTask` and `skillCooldownTask`, although
it started `diseaseExpireTask`. Live Holy Shield remained active at age 1285815 ms
despite its 40000 ms duration, and its 120-second cooldown never cleared. Start the
same expiry tasks used by `PlayerLoggedinHandler` during headless login. Enabling
these ordinary timers on only the five live dedicated companions expired stale buffs
and cooldowns normally, without force-removing effects. This explains the missing seal
in early magic tests. Subsequent lightning reports applied SEAL to the Hero, Dark
Knight and Bishop; their carried All Cures were consumed normally. Live examples:
Night Lord 2277 HP lost; Bishop 1092 raw magic damage split into 219 HP and 873 MP;
shielded Bishop blocked seal, and the same character received seal after expiry.
The final read-only stun sample recorded 720 observations per bot: Night Lord 22,
Dark Knight 47, Hero 53 and Bishop 41 samples with STUN active. No attack-send
timestamp advanced between consecutive stunned observations. Detailed natural
stun/expiry/resumption evidence is in `target/zakum-live-test/stun-watch-r3.csv` and
`stun-proof.json`. Final focused suite: 242 tests passing; next-launch jar packaged.

Magic/ice support and the headless login timer correction were hot-applied during the
existing fight without restarting or changing the autonomous population. The immutable
running jar remains the October 18 initial build; `target/Cosmic.jar` includes the new
classes for the next normal launch. Temporary tracing restores the fixed observer,
not the original jar's older observer; do not use its old class resource as rollback.

Deployment readiness (October 18): 238 focused tests passed and the server was
gracefully restarted onto `server-runtime-20261018-zakum-hits.jar`. Runtime scalar
settings and the enabled 10x population scheduler were preserved. Existing fixtures
602 and 604 were prepared, checked, saved and dismissed without starting an event:
Bishop Holy Shield 30 / Resurrection 10; Night Lord Shadow Partner 30, 100 rocks
before an ordinary SPECIAL_MOVE cast, 99 afterward, SHADOWPARTNER value 80 active.
Equipped weapons were Elemental Wand 6 (1372040, MATK 145) and Timeless Lampion
(1472068, WATK 59), each still with seven upgrade slots. This verifies supplies and
normal casting, not boss-hit parity or an optimized/scrolled damage setup.

During the human's AFK body phase, the human died while all five companions remained
alive. Registered Zakum crews now defer owner death handling to the expedition
controller instead of the generic five-minute companion return-to-town timer.
Resurrection approach uses the shared skill effect bounding box rather than an
arbitrary 80-pixel proximity check; normal MP costs and cooldown still apply.

The archer's Dragon Pulse reach is unverified against the official client: the current
generic percentage-range interpretation turns WZ `range=440` and 520 base/passive reach
into 2288 pixels. A live final-arm plan used that rectangle. The Night Lord's Triple Throw
used a 600-pixel projectile rectangle and overlapped the arm's actual WZ bounds from its
platform. A suspiciously long rectangle is evidence to investigate, not proof of the
official client's interpretation; avoid guessing a replacement range without a capture.

Saved-crew selection and Adobis summoning were verified live with the same six fixtures
all arriving at 211042400, with all preparation supplies restored and no managed/owned/GM
flags. The walking approach remains unreliable: in Cave Within the Cave (211042101), navigation cycled between a lower
platform, a rope grab and an exit back to the lower platform while targeting out00 at
(-2808,-512). The captured trace showed the rope exit being selected before reaching
the upper portal height. No navigation/physics workaround was added. NPC assembly uses
the authorized companion summon and no longer depends on this cave route.

Daily expedition limits are currently disabled (`USE_ENABLE_DAILY_EXPEDITIONS=false`,
also verified in the running JVM). When enabled, `ExpeditionBossLog.BossLogEntry.ZAKUM`
allows two entries per character per day. The ordinary `ZakumBattle.js` instance timer
is 120 minutes; a one-hour development/test session is not a gameplay restriction.

Current skill efficacy and remaining verification:

- Shadow Partner 30 and Shadow Stars were verified active on Night Lord 608;
  Summoning Rocks are consumed normally. All five companions produced live damage.
- Beholder, Phoenix and Bahamut now cast through ordinary positioned SPECIAL_MOVE.
  Current Beholder 1 has no Aura/Hex allocation; spawning alone does not establish
  a measured healing or damage gain. Phoenix/Bahamut attack adapters use ordinary
  summon movement/damage handlers, WZ reach/target counts and 3030 ms cadence.
- Hurricane uses a held state: 300 ms start, 120 ms per arrow, restarted on movement,
  interruption or a casting pause. Weapon animation speed no longer imposes a 661 ms
  minimum on each arrow. These values come from LazyBui's original pre-BB packet
  measurements: https://www.southperry.net/showthread.php?tid=3088. Actual simulation
  ticks may make firing slower; no catch-up bursts are synthesized. Live skill
  selection changed from Strafe to Hurricane. A 30-second sample captured 200 arrows;
  199 continuous intervals had median 155 ms (137..203 ms), without catch-up bursts.
- Boss support actions are checked at ready action boundaries as well as AI scans.
  Previously Bishop scan age reached 65 seconds while Genesis repeatedly took the
  between-scan ready tick. After the fix, Bahamut cast and scan age fell below a
  second. Healing retains priority over support casts.
- Boss combat reuses the ordinary stronger-skill repositioning policy. Its geometry
  now measures hitbox-to-hitbox gaps, including offset arms. Candidate short steps
  that intersect Zakum's body are rejected. This does not change projectile ranges.
- Bishop body tactics retreat at 100-pixel clearance toward 175-pixel stand-off,
  reject nearby endpoints and avoid crossing the body. The owner confirmed a real
  stun/contact death in the earlier run and used GM healmap. That run is not proof
  of unaided recovery; sustained Bishop survival still needs a fresh fight.

Client stability: the owner confirmed an unexpected disconnect at 01:08 Chicago
during the next arm run. New summon attacks were temporarily held. The adapter's
plain 00/01 direction was corrected to packed attack1 action 04/05, matching the
real capture in `PacketCreator.summonAttack`. A regression exercises the normal
handler and inspects the resulting broadcast, including cooldown and stunned-owner
behavior. This is a concrete packet correction, not proof of the disconnect's cause.
Corrected attacks passed a live retest with the human connected: repeated Phoenix
hits reduced arm HP by roughly 5..7k per target, Bahamut by roughly 6..9k. The trace
includes Phoenix attacking two offset arms at once through the normal handler.
This verifies actual damage and short-run client stability, not an entire completed
fight. Bounded diagnostic captures restore the uninstrumented adapter automatically.

Validation: 218 focused tests pass (summons, Hurricane, combat, attack data, manager,
Zakum); package succeeds. Changes were hot-applied without restarting. Navigation
physics and graph data were not changed. Autonomous population was not repurposed.

Retry supplies use `BotZakumSupplies` from both assembly preparation and Adobis'
expedition-start branch, before `startInstance`. Only registered companions belonging
to the human leader qualify; all-human expeditions are unaffected. Targets are 600
Ginger Ales, 100 All Cures, 5 Eyes of Fire, 12000 Ilbi/appropriate arrows, and 100 of
each consumable required by learned skills. Item consumption is read from every
learned skill's WZ effect, not only the buff cache: the latter omitted the Bishop's
Summon Dragon rocks and mage skill Magic Rocks. Ammunition uses legal stack sizes;
empty/beginner star sets are removed. Repeated preparation tops up rather than adds
another full allowance. Failed preparation prevents entry with a normal NPC message.

The missing entry skill consumables were corrected once for the active dedicated
crew: Bishop Summoning/Magic Rocks 0->100 each, archer Summoning Rocks 0->100,
thief Summoning Rocks 95->100. Routine restocking remains outside the fight.
209 focused supply/NPC/Zakum/combat/manager tests and package build passed.
The owner clarified that the reported crash preceded the summon action-byte fix;
the corrected summon behavior remains enabled, with no rollback applied.


### Town portal and crew size requests

On 2026-10-21 the owner asked to retain mage town-portal support for a human
who needs to leave mid-fight to recharge stars and return. Legal skill/map
restrictions and expedition re-entry behavior have not been investigated for
this request; no permission bypass or guaranteed round trip is assumed.
The owner selected twelve participants on 2026-10-22. Two-party assembly,
registration, resupply and dismissal are implemented and smoke-tested; a full
human-led twelve-participant fight remains the acceptance check. The town portal
request remains deferred and is independent of raid size. The owner also selected
twelve as the initial target for future Horntail work; Horntail companions are not
implemented by this Zakum change.


## Twelve-participant raid verification (2026-10-22)

The controller retains the legacy six-person path while new human assembly creates
eleven bots in two six-person parties. Both parties remain owned by the human raid
leader; validation checks each bot's expected party instead of requiring all bots
in the human's party. Registration still uses Adobis and the ordinary expedition.
Resupply and dismissal enumerate registered crew across both parties, not only the
human's party list. Duplicate assembly is refused while a previous crew is active.
Each party has Bishop and Dark Knight support. Resurrection only targets members
of the casting Bishop's own party, matching normal skill behavior. Readiness waits
for the full crew's positioning/buffs and delivers its notice to the human even
when a second-party bot finishes the readiness check first.

Thirty-one focused tests passed, including legal two-party composition, legacy
six-person assembly, second-party ownership, raid-wide supply/dismissal, and
party-scoped resurrection. A regular-Client stand-in on channel 2 assembled eleven
fixtures, created the expedition through normal Adobis dialogue, and observed all
twelve register. Both parties resupplied to at least 600 Ginger Ales per bot.
Normal expedition cancellation and full dismissal succeeded; reassembly reused
exactly IDs 601/603/605/607/608/652/653/654/655/656/657. All test fixtures were saved
offline and the registering expedition removed. No altar entry, summon or combat
was performed in that smoke test. Human client combat/buff validation is pending.
Evidence: target/otono-zakum-raid/smoke.txt. Updated classes were loaded into server
PID 21484 without restart; target/Cosmic.jar contains the same built changes.

## Rock supplies and request suppression (2026-10-22)

Assembly and every Adobis retry top up learned-skill consumables to 200 (previously 100). Mages explicitly receive 200 Magic Rocks (4006000) and 200 Summoning Rocks (4006001), including when one type is absent from the current buff cache. Zakum retains 600 Ginger Ales per attempt, rather than the ordinary PQ baseline of 200. Consumables are spent normally during combat; these targets are entry/retry top-ups, not unlimited in-fight refills.

Dedicated PQ/expedition crews no longer initiate rock-sharing requests, including the misleading generic Summoning Rock line for a Magic Rock shortage. Both periodic and direct request paths check dedicated registration. Ordinary autonomous bots retain their behavior. A one-time authorized live correction topped up the active Zakum crew; both Bishops were observed holding 200 of each rock afterward.

## Exterior arm positions and Night Lord boss skills (2026-10-22)

Ranged weapons (including mage wands/staves) choose a Zakum arm as their primary target only from its exterior side, at least 60 pixels beyond that arm's outer hitbox edge. This is a safety/positioning policy, not a claim that the client blocks all cross-Zakum projectiles. The original range-model comparison was inconclusive because the player's target arm died before a controlled same-position comparison.

The stance search applies the same exterior constraint and keeps ranged characters 100 pixels clear of the body's contact rectangle. Short repositioning for a stronger skill cannot violate the arm-side constraint. When changing sides during arms, they walk/climb to and activate the actual h000/h001 upper-platform portals through the normal travel handler, then approach the new stance. The Bishop's existing restriction against crossing an active body to chase summoned mobs remains intact.

Night Lords cannot select Avenger for Zakum body/arm primary targets inside the event. Triple Throw remains their usual boss attack; normal resource, range and damage planning still applies. Avenger remains available against summoned mobs. This first implementation did not strip multi-target spell overlap; the later eight-platform arm policy supersedes that behavior.

Verification: 28 focused regressions passed, covering exterior-side attack gating, normal portal switching, Avenger exclusion, existing Bishop safety, two-party crew behavior, rock provisioning and trade/request suppression. Rebuilt and live-loaded BotZakumCombat/BotCombatManager without restart. A subsequent live snapshot showed both archers and all three Night Lords on the right upper platform (x=499) while the remaining live arms were right-side arms 8800004/8800008. Full client movement/combat acceptance remains pending.

## Visible Hurricane held bow (2026-10-22)

Hurricane's ordinary ranged attack broadcasts show arrows/damage but do not begin its remote held-skill effect. `BotHurricaneVisual` now sends the normal SKILL_EFFECT preparation at the start of a dedicated Zakum Hurricane hold, refreshes its internal timestamp on continued firing without resending the preparation every arrow, and sends CANCEL_SKILL_EFFECT when the hold ends. Movement, facing changes, map changes, death, stun/seal, another attack, support casting, or a 750ms firing gap terminate the visible hold. The existing 300ms start delay and 120ms shot cadence remain unchanged.

The existing PacketCreator argument names obscure the wire format: the final three payload bytes carry packed body action/direction (little-endian short) followed by attack speed. Use the attack plan's known action and facing, not an invented action value. Packet tests assert the 14-byte prepare packet including action/direction/speed and single-start/cancel behavior. The normal remote skill-effect path is used; no client assets or damage buffs are changed. Scope remains the existing dedicated Zakum Hurricane hold implementation.

Verification: 32 focused Hurricane, packet/lifecycle, Zakum combat, supplies, raid and trade/request tests passed; packaging succeeded. The new helper plus existing lifecycle hooks were loaded live without restarting. Client confirmation of the large glowing bow and correct facing remains pending.

## Triple Throw horizontal firing lane (2026-10-22)

**Superseded later the same day by the eight-platform arm policy below.** The experimental 20-pixel restriction was removed; normal projectile hitboxes now remain subject to the explicit platform permission.

The upper-platform audit exposed a separate vertical issue after same-side positioning was fixed: the generic projectile rectangle extended from feet Y-50 through Y+50. From Y=-530 it overlapped arm 8800008's top edge at Y=-500 despite a star firing line near Y=-560 passing above that arm. Dedicated Zakum Triple Throw now uses a conservative 20-pixel-high lane, Y-40 through Y-20. This is an explicit safety restriction, not a client-measured exact vertical tolerance; ordinary population reach is unchanged.

For a Night Lord with learned Triple Throw, Zakum body/arm planning uses that skill directly through the normal skill gates, so a wider basic/Lucky Seven fallback cannot defeat the restriction. Stance selection uses the same thin projectile lane; a thief must reach an aligned platform for lower arms, with normal movement/portals unchanged.

Verification: 216 tests passed across BotZakumBattleTest, BotHurricaneTest, BotHurricaneVisualTest, BotCombatManagerTest, BotAttackDataProviderTest and BotManagerTest; packaging passed. A regression reproduces the captured upper-platform/lower-arm geometry, rejects the shot, accepts the aligned upper arm, and accepts the lower arm after descending. Live redefinition without restart succeeded. All three live Night Lords on Y=-530 then showed 20-pixel Triple Throw boxes at Y=-570..-550: upper arm 8800004 in range, bottom arm 8800010 out of range. Client movement acceptance remains pending.

## Eight platform-to-arm assignments (2026-10-22, current policy)

The owner requested a strict one-platform/one-arm rule after cross-body hits persisted. `BotZakumArmLanes` applies only to registered dedicated altar crews and arms 8800003..8800010. It reads the real foothold below the character, requires the feet to be within six pixels of that surface, and assigns exactly one arm from the side and main platform. Normal skill/weapon reach, MP/ammo, immunity and action checks still apply; lane permission does not grant damage or range.

| Position | Left arm | Right arm |
|---|---|---|
| Upper platform with teleporter | 8800003 (surface about -556) | 8800004 (about -530) |
| Second platform from bottom | 8800005 (about -488) | 8800006 (about -391) |
| First main platform above floor | 8800007 (about -422) | 8800008 (about -327) |
| Base floor | 8800009 | 8800010 |

The small extra right-side ledge around Y=-266 is not another arm tier. Bots between platforms, on ropes or in the central body area have no permitted arm. Stance selection uses the same tier mapping, and normal upper-platform portals remain the side-switch path. Body phases and summoned-mob targets are outside this arm-only restriction.

The root bypass was shared projectile retargeting: `resolveEffectivePrimary` could replace the requested arm with another overlapping arm, especially because multiple arms share the same monster origin. Guarding only the original request was insufficient. The shared bot adapter now checks lane permission during primary replacement, secondary-target collection, candidate filtering, and again immediately before attack execution. Cached plans and AoE packets cannot retain arms from another tier or side. Phoenix/Bahamut target selection applies the same rule using the owner's current platform.

Verification: 229 tests passed across eight focused/shared combat suites. New tests cover all eight positions against all eight arms, same-origin primary replacement, mixed secondary targets, a cached plan after a platform change, broad summon splash, airborne rejection and unchanged unregistered population behavior. Packaging passed and the helper plus three combat classes loaded live without restart. The live fight had already advanced to body phase two: ongoing body attacks were observed, but there were no live arms left to validate the new policy in that instance. Next arm phase/client audit remains pending.

## Observed attack recoil (2026-10-22)

The observed-attack adapter previously sent TakeDamageHandler reports for recognized boss attacks, which correctly applied damage/status but did not create the client-owned movement recoil. Ordinary bot contact damage already had recoil, so a headless bot could visibly stand through a projectile/area hit despite taking damage.

After the normal damage handler returns, BotMobAttackObserver now invokes BotObservedHitReaction only for a positive hit with actual HP or MP loss and a surviving bot. It uses the existing ground/air knockback physics and configured forces, pushes away from the attacker's position, clears the current movement/action plan, broadcasts movement and cancels Hurricane. Existing Stance percentage resistance and climbing/dead-state exclusions match the contact-hit policy. Misses and fully blocked/no-loss hits do not create recoil, and the helper does not apply damage a second time. Poison mist remains a separate tick-damage path without this recoil hook.

Scope is attacks already recognized by the observer (Zakum and Ellin supported attack types); this does not add unsupported boss attack shapes such as excluded ground tremors. Ordinary autonomous contact handling is unchanged. Verification: 215 tests passed across observed-hit reaction/observer, shared combat/attack data/manager and Hurricane suites; packaging passed. Tests cover normal-handler damage followed by recoil dispatch, grounded/airborne impulses, full Stance resistance and Hurricane cancellation. Live helper load and observer redefinition succeeded without restart. Client confirmation of recoil for the current fight remains pending.

## Confirmed arm kill order (2026-10-22)

Owner numbering is left/right rows 1/2 at the top, 3/4, 5/6, then 7/8 at the bottom. The agreed order is **5 → 7 → 4 → 6 → 8 → 3 → 1 → 2**, corresponding to monster IDs 8800007, 8800009, 8800006, 8800008, 8800010, 8800005, 8800003, 8800004.

The crew commits to the earliest living arm in that sequence rather than attacking a later arm simply because it is in range. Movement stance selection and the shared lane permission agree on this focus; primary replacement, secondary/cached attacks and summons cannot skip ahead. Dead/missing arms are skipped normally. Platform assignment and normal attack gates remain required. Bishops still prioritize spawned adds when present; non-arm targets are outside the sequencing restriction.

WZ checks confirmed arm 5's ailments, arm 6's heal/buff skills (HEAL_M 114), and 33 million HP for each top arm. The sequence itself is the owner's preferred tactic, not an inferred universal optimal strategy. Verification: 31 focused arm-lane/Zakum/summon tests passed and packaging succeeded. A new regression asserts all eight priorities and rejects a conveniently positioned later arm until the earlier living arm dies. Live redefinition of the combat driver and lane policy succeeded without restart; next arm-phase client audit remains pending.

## Melee assistance exception (2026-10-22, current policy)

After the strict platform/order rollout, warriors could wait for a focus arm that their weapon could not reach. At the owner's request, melee classes (warriors, Bandit/Chief Bandit/Shadower and Brawler/Marauder/Buccaneer) are exempt from the artificial platform and strict sequence permission. They keep the preferred arm ordering as a soft target priority, but can attack another arm whose actual weapon/skill hitbox reaches. Their stance search and better-skill repositioning also use ordinary reach instead of a forced platform tier. No extra attack range, damage or movement shortcut is granted.

Ranged characters and their summons retain the eight-platform arm assignment and strict 5 → 7 → 4 → 6 → 8 → 3 → 1 → 2 order. Normal body-contact avoidance, skill costs, immunity, physics and body/add behavior remain unchanged.

Verification: all 33 focused lane/Zakum/summon tests passed; packaging passed. New regressions distinguish a ranged character blocked by the current arm focus from Hero/Dark Knight permission, and exercise a Hero actually executing an available later-arm plan while the earlier arm has no usable plan. Both policy/driver classes were redefined live without restart. Client confirmation of resumed melee contribution remains pending.

## Rope transit with no cached combat stance (2026-10-22)

Spear2 and Sword2 stalled at rope X=-447, Y=-260/-241. Endpoint pathlogs showed a valid CLIMB from rope region 11 to platform region 4, but the actual combat-driver destination was each bot's current rope point every tick. BotZakumCombat's early `inAir || climbing` branch bypassed stance selection and defaulted an absent GOALS entry to the current position. This was a caller destination bug, not missing graph edges.

Airborne travel retains its existing branch. Climbing now acquires/refreshes a real reachable combat stance and delegates to the standard walk/climb handoff. If no combat stance is available, it chooses normal travel toward the same-map leader or the existing staging position instead of self-targeting the rope. It does not snap positions, force rope exit, modify graph reach or widen jump windows.

Verification: 69 focused lane/Zakum/navigation tests passed; packaging passed; the driver was redefined live without restart. Before/after endpoint captures show Spear2 moved from (-447,-260) to (-305,-487) and Sword2 from (-447,-241) to (-332,-488), both grounded on a platform. The remaining live arm was the top-right arm, and neither warrior had a usable melee attack plan for it, so renewed damage from those two was not claimed. Ranged completion of an unreachable upper arm remains normal behavior.

## Stronger-skill steps must stay on a supported surface (2026-10-22)

The owner reported Sword1/Sword2 repeatedly leaving a platform to reach the remaining top-right arm. Captures showed legal traversal/climbing, but then better-skill destinations such as (-225,-488)/(-243,-488) beyond the left platform ending around X=-274. The navigator correctly sought another region for an unsupported destination; the combat caller was asking for an invalid small step. Later read-only checks found no usable attack plan or fresh stance for either sword against the sole remaining top-right arm.

BotZakumCombat now accepts a better-skill reposition only if the actual foothold below that point spans its X and its surface height matches within six pixels. Otherwise it fires the already-valid attack rather than stepping off the ledge for a stronger one. If there is no reachable stance at all, it clears abandoned move pins, committed navigation, fidgets and pending jump/rope inputs, then idles through normal grounded physics. Airborne/rope transit continues through the existing separate branches; no forced landing or position changes are added.

Verification: 70 focused lane/Zakum/navigation tests passed; packaging passed; the single combat driver loaded live without restart. Regressions ensure an available melee plan is executed instead of taking an unsupported upgrade step, and a no-stance state clears the old route and pending launch inputs. The fight advanced to the body during investigation, where both swords were observed sending normal attacks; the upper-arm visual audit must occur on a subsequent arm phase.

## Phase announcements require actual transitions (2026-10-22)

A dormant/fake body is already present during the arm phase. The old phase loop stopped at any body monster, regardless of whether arms still lived or the body was fake, so map iteration order could trigger “Body Phase One: The arms are down” early.

BotZakumBattle now scans living monsters without an early break: any living arm means arm phase; only with no living arms can a living non-fake body supply the body phase. BotPqChat rechecks Zakum phase lines at delivery so queued arm/body hints cannot describe an obsolete or premature phase. BotPqStageGuide clears body-phase consumed markers while arms are detected, allowing a previously premature notice to be announced correctly at the real transition in the same run.

Verification: 14 focused raid/chat/stage-guide tests passed and packaging passed. Regressions cover body-first and arm-first iteration, no body announcement while arms live or the remaining body is fake, actual body activation, queued stale-line rejection and rearming a prematurely consumed notice. All three guidance-related classes loaded live without restart. No boss damage, movement, HP or stage requirements changed; client confirmation of the next transition remains pending.
