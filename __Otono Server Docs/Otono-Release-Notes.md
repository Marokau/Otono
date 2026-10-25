# Otono release notes

## GPQ and MVPQ bot crews - October 25, 2026

Commit title: `PQ bots for MVPQ and GPQ added.`

### Added

- **Sharenian Guild PQ:** assemble five dedicated companions through Shuang's BotPQ Menu. If you do not have a guild, the menu helps you reach Heracle with cofounders; you choose the name and pay the normal guild-creation fee. Companions join your guild for GPQ and leave when dismissed. Your guild remains yours. The crew includes a level-30-and-under scout for the restricted route and the sacrifice.
- **MV's Lair PQ:** assemble five dedicated companions through Charles's BotPQ Menu. Supports levels 140-255, normal letter collection and leader submission, MV and the treasure room. Normal entry and pit resets now use the connected starting platform so ordinary classes can traverse the course.
- Both crews explain their objectives, wait for human leader actions, use normal stage requirements and preserve boss/bonus rewards for the player. Dedicated crews remain separate from the autonomous population.

### Verification

- **MVPQ: human-client verified.** Earlier mixed-class bot testing also completed the quest through actual treasure pickups and normal exit. Repeat-entry supplies, AFK-safe letter handoffs, human-led waiting and cleanup were checked. I observed the Bishop getting knocked back during the client audit.
- **GPQ: human-client verified with an Ergoth survivability caveat.** Earlier bot-only testing completed two runs through the normal puzzles, sacrifice, boss, rewards and exit. During my client audit, some companions died against Ergoth. Gathering before the summon, prioritizing Hyper Body/Magic Guard after Dispel and checking both potion controls were added afterward. These changes passed automated regressions and were applied live, but I have not repeated the client boss audit. Companions can still be vulnerable to some attacks; verification is not a promise of a death-free run.

### Other fixes in this update

- Course navigation corrections for rope exits and airborne collision-region tracking, with shared movement regressions.
- Concurrent native portal-script invocation is serialized per script.
- Clearer GPQ directions to Sharen III's Grave and additional spacing between WhereLevel's activity categories.

### Current GPQ audit caveats

- Leave offering items for the crew while it gathers supplies. Human-held offerings are not yet included in its supply count. Native GPQ rules also prevent companions picking up an item dropped by a different player; simply dropping it back does not resolve the shortage. Pet involvement in the observed pickup was not confirmed.
- Wait for the crew before starting the Gatekeeper's statue display. If the mage misses the flashes, check the incomplete answer with the Gatekeeper, then start a fresh round through the same NPC.
- The support mage is a Priest, not a Bishop; this crew does not provide Resurrection.

### Updating an existing installation

Pull the update, rebuild the server with Maven and restart using your normal launcher. ZIP users can download the updated source instead. Retain your own local configuration and database. Updated navigation caches rebuild as needed.

Implementation details and audit findings are in [the development journal](Otono-PQ-dev-Journal.md), [the content catalog](Otono-PQ-catalog.md), [GPQ notes](kb%20Otono%20Bots/kb_bot_gpq.md) and [MVPQ notes](kb%20Otono%20Bots/kb_bot_mvpq.md).
