# Otono release notes

## Horntail and Pink Bean PQ bot crews added and verified - October 31, 2026

Release title: `Horntail and Pink Bean PQ bot crews added and verified`

### Added

- Dedicated Horntail trial companions and a twelve-participant Horntail expedition crew through the activity NPC menus. Human prerequisite requirements remain in place; companion-only preparation supplies the bots' prerequisites.
- Horntail raid preparation, supported parties, boss positioning, recovery after Dispel, native ailments and damage over time, and Hero's Will/Resurrection handling. The main-body timer is twelve hours.
- Pink Bean expedition companions: 29 bots plus the human leader, grouped into five supported parties. The roster has 14 Night Lords and five each of Bishops, Dark Knights and Bowmasters. Finished raid equipment, native scrolling, HP preparation, ammunition and consumables are retained between attempts.
- Statue-wave assignments, reflect activation warnings, safe return routes, and human participation without requiring the crew to wait for the human's formation position. Native boss stats and the 60-minute Pink Bean timer remain unchanged.
- Compact local Pink Bean result logging in `logs/pinkbean-audit.log` for unattended client runs.

### Verification and limitations

- **Horntail expedition: human-client verified.** The successful audit finished the main body in about 46 minutes with no observed deaths. The subsequent upper-right-head projectile-height correction passed automated checks but still needs explicit client firing confirmation.
- **Horntail trials:** implemented and exercised in headless testing; do not infer a separate human-client trial audit from the raid audit.
- **Pink Bean: human-client verified October 31.** The native event recorded a clear at 59m53s with four aggregate participant deaths and 29 living participants at clear. This was the 29-bot Night Lord-heavy roster, plus the human. Timer margin is narrow; a repeatable 50-minute clear has not been established.
- Throwing-star projectiles can still be invisible on upper platforms despite accepted damage. The attempted packet-display change was reverted after it failed the client check.
- All crews remain separate from the autonomous population. Ordinary human entry requirements and native skill costs/cooldowns still apply.

### Updating

Pull the update, rebuild with Maven and restart the server. Keep your own configuration and database. No local runtime logs, recordings, personal configuration or development Git history are included in the release.


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
