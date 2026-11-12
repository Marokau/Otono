# In-game training and build guide

## Class guides - current implementation

**Audit status:** Night Lord is the only class guide finalized to my satisfaction. All other guides are still being audited and tweaked as I play their classes; they are starting references.

Class Guides lists **Permanent Beginner first**, followed by **Night Lord, Dark Knight, Bowmaster, Ice/Lightning Mage, Hero, Bishop and Corsair**. The Bishop path includes Magician, Cleric and Priest; the other advanced classes likewise include their earlier jobs.

Each uses the established SP/gear layout: choose a job stage or level, see that level's additions and cumulative targets, and compare with your character's current points when its class matches. Gear opens at your level range with all slots on one scrollable page and a range selector. Changing classes resets the stage and gear range. The menu does not assign SP/AP or grant items; skill unlocks and mastery books still apply.

| Path | Suggested SP route and useful notes | Gear coverage |
| --- | --- | --- |
| Night Lord | Existing saved build retained; Shadow Partner supplies noted. | Existing eight saved gear ranges retained; added LUK/DEX and attack-accessory context. |
| Permanent Beginner | Six ordinary SP at levels 2-7; Nimble Feet/Three Snails route, Recovery as an alternative. | Beginner-only weapons, STR requirements, all-class armor, accuracy accessories, shields and boss-survival limits. |
| Dark Knight | Early HP Increase, hybrid spear/pole arm, no Final Attack, Crusher/Fury and Berserk potion management. | Accuracy-to-STR progression, Maple weapons, Sky Ski, Purple Surfboard and attack accessories. |
| Bowmaster | No Final Attack, Arrow Rain training route, then Sharp Eyes/Bow Expert/Hurricane. | Maple bows to Nisrock/Dragon Shiner, STR budgeting, arrows and optional Timeless Engaw. |
| Ice/Lightning Mage | Early MP Increase/Magic Guard, Thunder Bolt mobbing, Ice Strike/Amplification/Booster, mixed Blizzard/Chain Lightning progression. | No-LUK Maple staffs/wands, Elemental Wands 3/7, INT/magic-attack shields, gloves, capes and accessories. |
| Hero | Sword route, no Final Attack, Combo Attack and finishers, then Brandish/Advanced Combo. | Early swords through Stonetooth/Dragon Claymore/Timeless, one-handed/shield tradeoffs, STR/accuracy and attack accessories. |
| Bishop | Heal-first Cleric, Holy Symbol-first Priest, then a mixed Genesis/support route. | Cleric/Priest/Bishop weapons, INT/LUK tradeoffs, Maple Shield, magic-attack accessories and pendant comparisons. |
| Corsair | Gun-pirate route, Mastery/mobility, Outlaw prerequisites, Battleship Cannon/Bullseye and dismounted Rapid Fire. | Maple guns to Peacemaker/Concerto/Dragonfire/Timeless, level-gated bullets, STR/DEX overall choices and attack accessories. |

### Sources and scope

All eight supplied Togain transcript files were reviewed for relevant skill, gear and mechanic guidance: `Night Lord.txt`, `Permanent Beginner.txt`, `Dark Knight.txt`, `BowMaster.txt`, `Ice Lightning Mage.txt`, `Hero.txt`, `Bishop.txt` and `Corsair.txt`. These are source references, not files needed by the running NPC.

The added allocations are **suggested Otono routes**, not purported verbatim level-by-level builds from video visuals. Optional skills and fourth-job priorities can change with training style, available books and budget. Saved Night Lord targets remain as previously supplied. Later-era features and Royals-specific class changes are not imported. Equipment existence and the stated level/stat requirements were checked against the installed handbook/WZ data; this does not guarantee an item's availability from a particular drop, quest or shop.

Local details checked include the six-point Beginner limit, Magician's level-8 start, skill caps/prerequisites, 50% maximum-level Berserk threshold, Beginner-only weapon STR requirements, Elemental Wand levels/LUK, Stonetooth's DEX requirement and gun/bullet progression. Historical elemental-wand advice for Bishops is phrased as a comparison rather than an unconditional best-in-slot claim. The Corsair route uses verified local combat skills and does not rely on the inherited anomalous label for skill 5221010.

Supplementary references:

- [Nany's Bowmaster guide](https://royals.ms/forum/threads/new-source-nanys-bowmaster-guide-road-to-lord-sniper.121936/)
- [Donn1e's Dark Knight guide](https://royals.ms/forum/threads/a-guide-to-dark-knight-2026.230387/)
- The supplied archived MapleAnime page was unavailable during the initial update; its contents have not been used as verified guidance.

### Verification

Seven focused tests passed using the real GraalJS NPC script. They cover all eight class menus, complete scrollable gear ranges and back links, job-stage/current-point comparisons (including Cleric and Priest), and every new build level against local WZ skill caps, prerequisites and exact SP budgets. Existing PQ and expedition menu tests also pass. A human client review of the expanded class-guide pages remains pending. Close and reopen the dialogue to load menu changes; no server restart, gameplay Java change or client patch is required.


Type `@wherelevel` to open the guide in an NPC dialogue. No client patch is needed.
The Java command must already be installed; the menu is `scripts/npc/wherelevel.js`.
Menu-only changes load when a fresh dialogue opens; they do not require a rebuild.

- **Browse all chapters** opens the complete chapter list:
  1–20, 21–29, 30–49, 50–69, 70–89, 90–119, 120–149, or 150–200.
  Each chapter lists its two regional choices, then its optional destinations directly
  under a black bold **Optional episodes** header on the same scrollable page. Opening a region
  shows a short summary, **Monsters & training maps**, and relevant **Quests & discoveries**.
  Story-only regions omit the empty monster option. Individual mobs open their map details
  directly. Every chapter remains available through **Browse all chapters**, including
  later revisits to lower-level favorites. The redundant current-level and numeric-level
  browsing options have been removed.
  **Ludibrium — Deeper into the Clocktower** keeps the reviewed six-mob list, side labels,
  quest story and Papulatus pages. Quest introductions reveal their full text through
  **More details** on one scrollable page, with no artificial pagination.
  **Party activities — for later** is separate from the solo route. Its local event
  windows are references, not guarantees of admission. Early story introductions flag
  the Mushroom Kingdom (30–38) and Kerning Square (35–50) start windows.
  It is intentionally not an EXP/hour ranking. On entries with a destination map, GM
  characters receive a separate **Teleport to [map]** action with a confirmation prompt;
  the displayed map name itself remains informational.
  Papulatus's chapter teleport goes to Deep Inside the Clocktower (220080000), not
  the encounter instance. Its local event allows a one-person party; the normal
  portal still requires the player to create that party and enter as leader.
  Zakum, Horntail and Pink Bean are expedition stories rather than instance shortcuts.
  Horntail's story offers travel to Chief Tatamo's town; Pink Bean's offers the recruiter
  approach. Zakum currently describes preparations without a teleport.
- The live bot training menu has been removed at the owner's request. Bot behavior
  and the localhost dashboard are unchanged.
- The first menu lists **Browse all chapters**, **Class Guide**, **Party Quests**, and
  **Close** in that order, with one blank line between choices.
  Submenus group their Back/Browse links, Main menu and Close together under a
  black bold **Menu navigation** header, separated from the content by one blank line.
  Party activities are linked only from this first menu, not from chapter pages.
- **Class Guides → Night Lord → Skill points (SP)** opens at the character's job stage and level. Browse stages or
  levels to compare build targets against current points. Targets assume on-time
  advancement and the earlier stages of the displayed build; no points are spent.
- **Class Guides → Night Lord → Gear** opens at the character's level range, with all slots
  on one scrollable page and a level-range selector. All eight supplied gear tables (10–200+)
  are preserved as reference targets, including scrolled stats and Royals-specific
  items; availability and equipment requirements are not locally verified. This
  page does not compare equipped items. Empty source slots say “No recommendation
  in the source.” Levels 31–34 and 51–54 use the preceding range; shared boundaries
  use the newer range. AP guidance has been removed.

The training route was assembled for this server from the owner's nostalgia list.
Mob levels and spawn maps were checked against local WZ and handbook data. Regional
content follows `docs/guide-drafts/v83-nostalgia-tour-draft.md`; the Word document remains
the review snapshot. All previous unique mobs remain reachable, either in regional
monster lists or boss pages, with five additional verified Leafre training stops.
Pepe and Yeti move to the 50–69 chapter; early Fire Boars move to 30–49. Lower-level
favorites are revisited through their original regions rather than repeated in each
later chapter. Ulu 1 and
Ulu 2 are omitted because those maps are not present locally. Night Lord SP and gear
remain separate class references. The SP build's four-SP level 60 / two-SP level 70
Assassin rows are corrected by allocating three SP per level through the same priorities,
preserving final totals. Venom precedes Taunt in the displayed fourth-job alternative.
Mastery books and skill unlocks still apply.

Checks:

```text
node tools/scripts/test-wherelevel.cjs
java --class-path target/Cosmic.jar tools/scripts/WhereLevelScriptSmoke.java
mvnw.cmd -q -Dtest=BotTrainingGuideTest test
mvnw.cmd -q -DskipTests package
```

The Node check covers point budgets, advancement boundaries, class/gear navigation,
every chapter boundary and region, every mob and story route, return navigation,
preservation of old destinations, scrollable details, teleport cancellation and GM
permission checks (including permission loss before confirmation), and boss approach
destinations. The packaged-runtime smoke check loads the NPC script and opens its
menus using the same GraalJS bundled in the server jar. This deployment lacks the
regex language, so keep this script's formatting logic free of regular expressions;
Node alone does not catch that runtime incompatibility. The Maven checks apply when
changing Java code, including the retained BotTrainingGuide service. For script-only
changes, run the Node check and open a fresh dialogue for an in-client smoke test:
open the command, browse each option, and check Back/Main menu/Close behavior.
