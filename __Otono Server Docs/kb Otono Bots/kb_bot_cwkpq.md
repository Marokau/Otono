# Crimsonwood Keep PQ companions

Jack (9201113) registers expeditions in the Hall to Inner Sanctum (610030020).
Otono's native entry supports levels 90–255 and 6–30 participants, with the
Crimsonwood key checked at the normal entrance. The BotPQ Menu prepares
seventeen companions for a human leader. Register with Jack and start after
all eighteen participants appear in the expedition.

## Crew preparation

The crew is prepared to at least level 140 and reuses its dedicated fixtures.
Ordinary leveling and the existing class builds assign earned AP and SP.
Skill preparation uses the shared raid book routine, including native mastery
book requirements and success rolls. Measured level-140 builds include
Triple Throw 30, Hurricane 21 and Sharp Eyes 29.

The three parties contain three Dark Knights, two Bishops, three Bowmasters,
four Night Lords, three Buccaneers, a Fire/Poison Archmage and an Ice/Lightning
Archmage, plus the human leader. Every party includes all five class families.
The eighteen-person roster fits Chapter 18's 10–20 participant guidance.

Equipment comes from the normal shop/drop catalog and the existing optimizer.
Weapons and physical attackers' gloves receive ordinary non-cursed attack
scrolls through ScrollHandler, including real failures and spent slots. Mages'
weapons receive the corresponding magic-attack scrolls.

Before each native start, supplies are topped up to 600 Ginger Ales, 200 All
Cures and 200 each of Summoning Rocks and Magic Rocks. Night Lords receive
12,000 Ilbis. Prepared attempts also reset the dedicated companions' skill
cooldowns outside the event.

## Native stage flow

- Stage One uses the hidden passage and normal portals.
- Stage Two uses learned Dragon Fury, Strafe, Magic Claw, Avenger and Flash
  Fist at the class sigils. Bots stand on the platforms under their sprites.
- Stage Three gathers the parties for Haste and completes the five sigils and
  five required upper-portal crossings.
- Stage Four follows the native randomized layout and active class sigils.
- Stage Five sends each class through its own portal. The native room NPCs
  and pirate chest award the weapons, which are offered inside their matching
  statue reactor areas. Bishops and other mages jump over the steep foothold
  on their portal approach.
- Lockewood's displayed origin is 121 pixels above his foothold. His chamber
  uses a 130-pixel approach tolerance so archers can claim the Ancestral Bow
  after defeating all five Master Guardians.
- Stage Six requires the leader to speak to the Battle Statue on the far
  right to start the guardian wave,
  summon the four Twisted Masters and confirm their defeat. Bots use learned
  Magic Guard, Hyper Body, Heal, Resurrection, cures and ordinary supplies.
- Fire/Poison and Ice/Lightning damage mages prioritize Hsalf from the start.
  His native weapon defense is 8,000, which drives these Bowmasters' physical
  attacks down to minimum damage. Physical attackers handle the other Masters
  first and then help finish Hsalf. Bowmasters approach a Hurricane stance.
- The reward hall leads into the native one-minute treasure room. Companions
  open boxes while the human receives the treasure.

Chapter 18 of the supplied MapleStory PQ summaries informed the class roles,
Haste and menhir course, five portal crossings, class weapons and treasure
room. The installed event scripts define the actual entry gates, four
Twisted Masters, stage timers and rewards.

## Verified on November 10, 2026

The initial level-130 run reached four offerings. A subsequent twelve-person
level-140 run restored all five weapons and defeated three Masters, then
reached the native boss timer with Hsalf still alive. That run exposed the
missing fourth-job book preparation and the high-defense damage bottleneck.

The fresh eighteen-person run completed native registration, every stage,
all five offerings, the guardian wave, all four Twisted Masters, the bonus
room and the normal exit. Clear was recorded at approximately 54 minutes
from entry, including approximately 42 minutes in the boss room.

All seventeen companions and the passive test leader had zero recorded
deaths. The leader made 63 native treasure pickups, including equipment,
and gained 9,925 mesos during the bonus visit. After the bonus ended, the
companions saved and logged off. The live dashboard contained zero CWKPq
characters.

The packaged build and 32 focused regression checks passed. Human-client verification was subsequently confirmed on November 12: the player completed the audit and defeated the bosses.

## November 12 client-audit follow-up

Main-stage briefings use BotPqStageGuide's shared delivery and reading pause.
The leader's arrival triggers the introduction before companions depart.
Stage Two, the Test of Agility, the Test of Wit and the Test of Unity explain
the objectives and the human's actions. Unity explicitly describes the five
statue weapons and the longer five-guardian archer room. Class chambers can
continue independently while the leader waits in the main hall.

The chamber Bowmasters use their learned Hurricane against Master Guardians
through the same native attack pipeline and range gates used in the boss
room. The live audit had shown Strafe with Hurricane 21 learned.

The updated package and 206 focused checks passed. The current boss-stage
briefing reached live map chat after installation. Earlier-stage delivery
and chamber Hurricane await the next client entry.

## Battle Statue and Ilbi selection

The Grandmaster Council Hall's NPC 9201115 is displayed as Battle Statue.
Stage Six briefings direct the human to that statue on the far right for
wave starts and the final clear.

CWKPQ restocking removes Subis (2070000) and Beginner Thief's Throwing Stars
(2070015) from Night Lords before stocking 12,000 Ilbis (2070006). Native
ranged attacks select the first eligible inventory stack, so merely adding
Ilbis behind the starter ammunition left the weaker stars active. The
November 12 live inventory check confirmed all four thieves switched their
first eligible stack from 2070015 to 2070006.

## Client-observed attacks added during the November 12 audit

BotCwkpqAttacks provides the event/map gate shared by BotClient forwarding,
BotMobAttackObserver and BotRaidAttackPackets. Native observed attacks in
the CWKPQ instance now use their WZ attack area, delay and damage flags,
then enter the normal player damage/status handler and bot recoil path.
This includes guardians, class-room monsters and the four Masters.

Red Nirg attack two and Rellik attack three apply native STUN. Poison and
Seal attacks use their native definitions. Rellik's ground-only flagged
attack skips airborne/climbing bots through the existing avoidance policy.
Curable ailments retain the shared three-second All Cure delay. Stun uses
its native expiry, and the common poison-cloud tick handling remains active.

The packaged build and 32 focused attack/hazard checks passed. During the
live human audit, natural casts produced observed stuns, poison, Seal and
recovery on companions. This verifies delivery and status application.
The November 10 headless zero-death result predates this integration and
does not establish survival against the newly delivered client attacks.

## Council Hall attack preferences and pirate prerequisites

Night Lords prefer learned Triple Throw throughout the Council Hall. Class
sigils use their existing required attacks.

Buccaneer attack planning and execution respect the native transformation
and energy prerequisites. Demolition and Snatch require active Super
Transformation, Shockwave requires a pirate transformation, and Energy
Blast, Energy Drain and Energy Orb require the charged energy meter.
Dragon Strike and Barrage remain available with their normal costs.

The November 12 pre-fix execution sample showed actual Dragon Strike damage
and Snatch used while untransformed. The attack-state gate corrects that
eligibility error. The blue visual described during the audit may be the
energy aura; its exact client animation has not been identified from video.


After the final Council Hall wave has no monsters remaining, a once-per-run
bot speech prompts the leader to confirm the clear with the Battle Statue
on the far right, collect boss loot, and take the far-right door toward
the bonus room. This completion message has its own announcement key.

## Council Hall boss loot persistence (November 12 audit)

Mark of Naricain (1122059) is present in the installed drop table for all four Twisted Masters (9400590 through 9400593). Dedicated human-led crew loot reservation already leaves original boss drops for the player. BotPqDropPolicy.keepsForHandoff now protects original boss drops in event map 610030600 from normal ground expiry, including after the event is marked cleared. Protection ends when the item is collected or the instance is disposed; player redrops keep normal expiry and eligibility. Eight focused drop-policy and boss-loot tests passed; the method change was installed live without a restart.

### November 12: Leave all CWKPQ bonus-room drops for the player

The audit clarified that every bonus-room drop belongs to the human, including mesos and player-redropped items. The shared bot pickup eligibility now rejects all drops in the instanced CWKPQ bonus map (610030800). Companions still use their existing chest-breaking behavior. Seven focused loot-eligibility tests passed, and the method change was installed live without restarting the server.

## Human-client acceptance

On November 12, I confirmed that CWKPQ passed my client audit and that we defeated the bosses. The earlier zero-death headless result predates the client-observed hazard integration; the client confirmation does not establish a death-free guarantee or exhaustive edge-case coverage.
