# PQ and expedition content in this fork

This is a local script/entry-path inventory, not a
claim that every encounter has been completed in the client. Event scripts alone
are insufficient evidence of usable entry. Normal level, party, quest, item,
marriage and channel restrictions still apply. Bot companions are a separate feature.

Latest acceptance update: Zakum twelve-participant human-led audit, October 22, 2026.


## Party quests and competitive party activities

| Content | Local level range | Entry evidence | Dedicated bot crew |
| --- | --- | --- | --- |
| Henesys / Moon Bunny | 10-255 | [1012112](../scripts/npc/1012112.js), HenesysPQ | No |
| Ariant Coliseum | 20-30 | [2101017](../scripts/npc/2101017.js), Java AriantColiseum | No |
| Kerning | 21-30 | [9020000](../scripts/npc/9020000.js), KerningPQ | Yes; owner completed |
| Monster Carnival 1 | 30-50 | [2042000](../scripts/npc/2042000.js), Java MonsterCarnival; USE_CPQ enabled at inspection | Yes; 4 vs 4, owner verified 2026-10-21 |
| Ludibrium | 35-50 | [2040034](../scripts/npc/2040034.js), LudiPQ | Yes; owner completed |
| Amoria / Amorian Challenge | 40-255 | [9201048](../scripts/npc/9201048.js), AmoriaPQ | No |
| Ellin Forest / Poison Forest | 44-55 | [2133000](../scripts/npc/2133000.js), EllinPQ | Yes; bot-only completion through rewards; owner verified human-led run 2026-10-20 |
| Orbis / Tower of Goddess | 51-70 | [2013000](../scripts/npc/2013000.js), OrbisPQ | Yes; owner completed |
| Ludibrium Maze | 51-70 | [9103001](../scripts/npc/9103001.js), LudiMazePQ | Yes; owner completed |
| Monster Carnival 2 | 51-70 | [2042005](../scripts/npc/2042005.js), Java Carnival implementation | Yes; 4 vs 4, owner verified 2026-10-21 |
| Lord Pirate | 55-100 | [2094000](../scripts/npc/2094000.js), PiratePQ | No |
| Magatia / Romeo and Juliet | 71-85 | [2112003](../scripts/npc/2112003.js), [2112004](../scripts/npc/2112004.js), MagatiaPQ_A/Z | Yes; owner verified human-led completion and poison-fog tick damage, 2026-10-21 |
| Sharenian Guild PQ | 1-255; special roles apply | [9040000](../scripts/npc/9040000.js), GuildQuest | Yes; two full bot-only clears through native rewards/exit, 2026-10-24; same-crew reuse/resupply verified; human-client accepted 2026-10-25, with Ergoth survivability caveat; latest protection changes await a repeat client boss audit |
| Crimsonwood Keep PQ | 90-255 | [9201113](../scripts/npc/9201113.js), CWKPQ | No |
| MV's Lair PQ | 140-255 | [9220018](../scripts/npc/9220018.js), TreasurePQ | Yes; mixed-class bot-only clear through real treasure pickups and normal exit, 2026-10-24; Beginner movement and human-led waiting tested; human-client verified 2026-10-25 |

Level ranges are this fork's script values, not remembered official-server ranges.
Carnival reference ranges are 2-4 per team on small fields and 3-6 on large fields;
the dedicated Carnival 1 and Carnival 2 implementations use four players per team.

## Expeditions and instanced boss battles

| Content | Entry evidence |
| --- | --- |
| Zakum | [Adobis](../scripts/npc/2030013.js), ZakumBattle; 12 participants (human + 11 bots), two parties; owner verified human-led completion and accepted the final client audit, 2026-10-22 |
| Horntail | [2083004](../scripts/npc/2083004.js), HorntailBattle; separate HorntailPQ trial via [2083001](../scripts/npc/2083001.js) |
| Pink Bean | [2141001](../scripts/npc/2141001.js), PinkBeanBattle |
| Balrog | [1061014](../scripts/npc/1061014.js), BalrogBattle normal expedition |
| Scarlion and Targa | [9270047](../scripts/npc/9270047.js), ScargaBattle |
| Showa boss encounter | [9120201](../scripts/npc/9120201.js), ShowaBattle |
| Papulatus | [Populatus00](../scripts/portal/Populatus00.js), PapulatusBattle |
| Captain Latanica | [captinsg00](../scripts/portal/captinsg00.js), LatanicaBattle |

The expedition enum also contains CWKPQ and Ariant, already listed above.
Zakum's twelve-participant companion raid passed the owner's human-led client audit
on October 22, 2026. Other listed boss encounters do not thereby have dedicated
human-led bot support.

## Other challenge and quest instances

- Mu Lung Dojo, solo and party: [2091005](../scripts/npc/2091005.js); its local disabled flag is false.
- Boss Rush PQ: [9000037](../scripts/npc/9000037.js), BossRushPQ.
- Kerning Square Cafe PQ variants: [1052013](../scripts/npc/1052013.js), CafePQ_1 through CafePQ_6.
- Holiday PQ variants: [9105004](../scripts/npc/9105004.js), HolidayPQ_1 through HolidayPQ_3;
  seasonal context and entry-map access are separate from script existence.
- Rescue Gaga: [9001105](../scripts/npc/9001105.js), RescueGaga.
- El Nath/skill-quest challenges: [2020008](../scripts/npc/2020008.js) (ElnathPQ),
  [2095000](../scripts/npc/2095000.js) (DelliBattle),
  [2041023](../scripts/npc/2041023.js) (ElementalBattle).
- Mushroom Kingdom: King Pepe/Yetis and Prime Minister encounters via
  [1300013](../scripts/npc/1300013.js) and
  [TD_MC_enterboss2](../scripts/portal/TD_MC_enterboss2.js).
- Neo Tokyo quest battles: [TD_Boss_enter](../scripts/portal/TD_Boss_enter.js), TD_Battle1-5;
  Guardian Nex challenge via [TD_neo_inTree](../scripts/portal/TD_neo_inTree.js).
- Aran/Maha, job-advancement trials, Zakum's Dead Mine and other character-specific
  quest instances also exist; these are not interchangeable with repeatable group PQs.

Ordinary field bosses and their AreaBoss event scripts are not additional party quests.

## Present in code does not mean available

- Nett's Pyramid: [Duarte](../scripts/npc/2103013.js) immediately reports that
  PyramidPQ is unavailable and returns. Its old code below that return is not an enabled entrance.
- Chaos Zakum and Chaos Horntail: entries in
  [ExpeditionType](../src/main/java/server/expeditions/ExpeditionType.java),
  but no corresponding normal NPC/event entry path was found in this inspection.
- Easy Balrog: BalrogBattle_Easy.js exists, but no caller was found; the verified
  normal expedition entry uses BalrogBattle instead.

The `!wherelevel` guide now lists 26 PQ/challenge entries and has a separate eight-entry
Expeditions & Bosses page. Cafe variants share their entrance; Holiday brackets have
separate entries. Character-specific story battles remain outside this group-activity
menu. Both pages include summaries and entrance teleports; teleports remain GM-only,
require confirmation, and do not bypass normal PQ entry.
