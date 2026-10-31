# Raid equipment goals and external build references

## Scope

These notes cover dedicated Horntail and Pink Bean companions only. They do not change the autonomous population, lower-level PQ equipment, human prerequisites, boss HP, timers, damage rules or encounter positioning. The four references below are saved for a future WhereLevel class-guide review; that UI has not been changed.

## Reference material

All four forum pages exposed their spoiler text during the October 30, 2026 review. This was a text review, not a claim that every embedded equipment screenshot was inspected. These are MapleRoyals guides, so their custom items, skill changes, HP assumptions and economy are not automatically Otono v83 rules.

- [Darko's Night Lord guide](https://royals.ms/forum/threads/darkos-night-lord-guide-2024.227431/): useful goals include a strong claw, attack gloves/cape/shoes, secondary-stat accessories, Balanced Fury and avoiding unnecessary base DEX. Its pendant comparison uses different Mark of Naricain stats from Otono.
- [Nany's Bowmaster guide](https://royals.ms/forum/threads/new-source-nanys-bowmaster-guide-road-to-lord-sniper.121936/): useful themes are weapon attack equipment and funding weapon STR through gear. Hurricane cadence is distinct from ordinary bow attack speed; a faster ordinary bow animation is not a Hurricane speed upgrade.
- [Dark Knight guide](https://royals.ms/forum/threads/a-guide-to-dark-knight-2026.230387/): weapon speed and attack matter together, with accuracy funded appropriately. Its Horntail cleave recommendations do not supersede Otono's tested late-arm rule against premature mass Seduce. This equipment change does not introduce low-HP Berserk potion management.
- [Bishop / Horntail guide](https://royals.ms/forum/threads/bishops-guide-2022-horntail-guide.125406/): INT, magic attack, total rather than base LUK requirements, and support responsibilities are relevant. Magic Guard, Hyper Body and the unbuffed survival floor remain mandatory in Otono's preparation.

## Native item and consumable findings

- Gelt Chocolate (2022121) supplies 120 weapon attack for ten minutes, but no magic attack. Dedicated physical attackers receive twenty Gelts; Bishops retain twenty Onyx Apples (2022179), which supply 100 magic attack as well as weapon attack. Preparation removes the other attack consumable so the bot cannot rotate between the two buffs. Use goes through the normal item handler.
- The saved crews wore Mark of Naricain (1122059), not Horntail Necklace (1122000). Otono's native Mark starts with 10 weapon attack, 10 magic attack and 300 HP before normal configured rolls. A necklace has seven all-stats before its native Dragon Stone's fifteen all-stats. A necklace is therefore not an automatic damage or survival upgrade; this change does not forcibly replace Marks.
- Compatible native goals include Timeless class weapons, Zakum Helmet (1), Stormcaster Gloves, Pink Adventurer Cape and Facestompers. Native configured drop rolls produce the candidates; the shared equipment optimizer chooses among them while retaining the raid HP goal. Guide target numbers are not assigned to items.
- Only selected upgrades enter inventory. Owned goal items and their consumed scroll slots are reused on subsequent preparation. Weapon and physical glove scrolling uses obtainable non-cursed attack scrolls through ScrollHandler, including real failure and slot consumption. No forced perfect scrolls, restored slots or direct stat bonuses are introduced.

## Preparation fixes

The saved-equipment audit found several level-180 Night Lords still holding low-tier claws, as well as many Night Lords with 160 base DEX. The preparation path now considers actual rolled/owned/scrolled weapons when planning secondary AP, excludes a worn weapon's own secondary stats and funds the independent requirements of the HP armor set, and reclaims secondary AP after selecting the set. Bishops similarly fund staff LUK requirements with equipment contributions before spending earned AP. Neither HP washing nor extra AP is introduced.

The existing bot-only rosters already have two Night Lords in each six-person party, except the fifth Pink Bean party, which has three. A human occupies the first party's leader slot and can change that count by choosing another class. The roster remains twelve for Horntail and thirty for Pink Bean.

Final actual-stat readiness is checked after native equip and scroll operations, with normal HP preparation used if an equip dependency leaves the worn set short. Gear verification is separate from a full encounter clear. The latest Pink Bean full attempts still timed out; upgraded equipment alone is not evidence of a successful raid.

## October 30 live verification

All forty-two existing fixtures passed final actual unbuffed HP and normal consumable-use checks. Seven selected returning fixtures also passed a repeated preparation without a same-weapon attack downgrade. Sample Night Lord claws improved 42 to 85, 45 to 68 and 57 to 83 weapon attack. Actual scroll failures remain; this is not a claim that every claw meets a forum's perfect-equipment target. Both raids supply Balanced Fury. Fixtures were saved and logged off; a fresh full timed encounter remains necessary to qualify the damage change.

After successful preparation/resupply, spare EQUIP stacks are removed through InventoryManipulator. Worn gear, ammunition, recovery supplies, rocks and entry items remain. A regression checks that EQUIPPED and ETC inventories are not touched.

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
