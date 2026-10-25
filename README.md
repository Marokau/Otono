# Otono v83

Otono is my personal MapleStory v83 server, built to revisit the game at my own pace, even when a full party is not available.

Its main additions are guided bot parties for party quests and expeditions. You can assemble a crew through the activity's NPC, lead the party yourself, and let the bots explain objectives, handle their roles, and wait when your participation is needed.

The `!wherelevel` GM command opens an NPC guide that brings leveling locations, party quests, challenges, bosses, and entrance teleports into one menu.

I am sharing this project so others can enjoy it, learn from the development notes, or use the work in their own projects. Development continues as I play through and audit more content.

## What Otono adds

- **NPC-based bot crews:** supported entry NPCs have a **BotPQ Menu** for assembling and dismissing dedicated crews. The existing autonomous population continues separately.
- **Guided party quests:** stage explanations, progress updates, puzzle help, and readiness notices help players learn as they go.
- **Prepared companions:** activity-appropriate equipment, skills, stats, ammunition, and supplies make crews ready for their assigned content.
- **Player access to rewards:** dedicated loot rules leave boss gear and bonus-room rewards for the human player, while stage-item handoffs support normal progression.
- **Boss behavior fixes:** work on ailments, damage over time, client-observed hazards, summons, attack visuals, and positioning is documented alongside the encounters where it was tested. Read the [Otono PQ development journal](__Otono%20Server%20Docs/Otono-PQ-dev-Journal.md) for the observations, fixes, and verification.
- **The WhereLevel guide:** the `!wherelevel` GM command offers leveling chapters, class guides, activity summaries, and entrance teleports. Teleport access follows the command's permission checks; normal activity entry requirements still apply to preserve the original gameplay experience.
- **All In One Shop:** `!aioshop` opens an NPC shop with item-name search and categories for weapons, equipment, scrolls, potions, arrows, throwing stars, bullets, and rocks.
- **Customizable progression:** the example configuration uses 2x base EXP with level-based multipliers intended to ease late-game leveling. Rates and other preferences can be changed in `config.yaml`.

## Player-verified bot content

These activities passed my human-led client audits:

| Activity | Dedicated crew status |
| --- | --- |
| Kerning PQ | Verified |
| Ludibrium PQ | Verified |
| Carnival PQ 1 and 2 | Verified; four participants per team |
| Ellin Forest PQ | Verified |
| Orbis PQ | Entire PQ verified through restoring Minerva; bot support for the optional bonus jail rooms afterward is deferred |
| Ludibrium Maze PQ | Verified |
| Magatia AKA Romeo and Juliet PQ | Verified |
| Sharenian Guild PQ | Human-client verified; companions can be vulnerable to Ergoth attacks. Latest protection changes await a repeat client boss audit |
| MV's Lair PQ | Human-client verified |
| Zakum | Verified with twelve participants: one human and eleven bots |

Verification reflects the runs documented in this project, not a guarantee that every possible party composition or edge case has been tested. Horntail, Pink Bean, and other activities listed in the guide do not yet have the same dedicated, verified bot support.

See the [content catalog](__Otono%20Server%20Docs/Otono-PQ-catalog.md) for the broader activity inventory and distinctions between existing game content and dedicated bot support.

## How the EXP multipliers work

The original v83 EXP requirements rise steeply at higher levels. Otono keeps those requirements and increases the EXP rate at configured level thresholds to soften the late-game slowdown.

| Level range | Level multiplier | Rate with the example's 2x base |
| --- | --- | --- |
| 1-29 | 1x | 2x |
| 30-69 | 1.5x | 3x |
| 70-119 | 2x | 4x |
| 120-149 | 2.5x | 5x |
| 150-159 | 3x | 6x |
| 160-169 | 3.5x | 7x |
| 170-179 | 6x | 12x |
| 180-189 | 10.5x | 21x |
| 190-200 | 18x | 36x |

The highest applicable threshold replaces the previous multiplier; the thresholds do not multiply together. The level multiplier multiplies the character's EXP rate, which already includes the world's base rate. For example, a 2x base with the level-190 multiplier gives 36x before any other applicable bonuses.

The goal is a more even-feeling journey through levels 150-200. These settings do not mathematically flatten the EXP table or establish that 150-200 takes the same time as 1-150. Actual leveling time also depends on monsters, gear, skills, and activities. Adjust `exp_rate` and `level_exp_rate_multipliers` in `config.yaml` to your preference.

## Getting started

These steps cover running the Otono server on your own Windows computer. Obtain a compatible client and game data separately using the upstream references below.

### 1. Requirements

- A Java 21 JDK, available through your terminal's `PATH`.
- MySQL 8 or later, running locally.
- The Otono source code, including its Maven Wrapper.
- Compatible v83 server WZ XML data and a compatible client, supplied separately; consult the upstream setup references below.

Run `java -version` in a terminal to confirm Java 21 is selected. An IDE is optional.

### 2. Create the database

Connect to MySQL with HeidiSQL, MySQL Workbench, or another MySQL client. Create an empty database named `cosmic`:

```sql
CREATE DATABASE cosmic;
```

You do not need the developer's backups. On the first successful startup, the server uses Liquibase to create its tables and load the included initial data. Do not manually import every SQL file.

### 3. Configure Otono

Copy `config.example.yaml` to `config.yaml` in the server folder. Set `DB_HOST`, `DB_USER`, and `DB_PASS` to match your MySQL installation. The example uses `localhost`, `root`, and `1234`; those values do not create or change your MySQL login.

The database name in `DB_URL_FORMAT` remains `cosmic`. The repository and server folder can still be named Otono.

The example is intended for local play: `HOST`, `LANHOST`, `LOCALHOST`, and `TAILSCALEHOST` use `127.0.0.1`. Playing from another computer requires a separate network configuration; local-only setup does not need your LAN address.

Otono's example uses 2x base EXP plus the included level-based EXP multipliers, America/Chicago time, and pets that never get hungry. Comments marked `# Otono:` explain my custom choices. Edit these settings to suit your game.

### 4. Supply server data and build

Place the compatible server WZ XML data in `wz` beside `pom.xml`. Client WZ files are not a substitute for the server's expected XML exports.

Open a terminal in the Otono folder and run:

```bat
mvnw.cmd package
```

This downloads build dependencies and creates `target\Cosmic.jar`. The inherited JAR name is expected. WZ data is not bundled inside the JAR.

### 5. Start the server

Double-click the publication's `launch.bat`. During preparation, the simplified publication launcher is named `launch-example.bat`.

The simple launcher allows up to 2 GB of Java heap. Autonomous population and automatic bot generation remain enabled, matching the inherited defaults. I use an 8 GB Java heap for my local server. The included launcher defaults to a 2 GB maximum (`-Xmx2048m`); you can raise it if needed for your machine and bot population, for example to `-Xmx8g`.

Leave the server window open. The inherited startup message `Cosmic is now online` means startup reached its online state.

### 6. Connect and log in

#### Install the client

I used the following workflow. The base steps are also described in the [Cosmic client installation guide](https://github.com/P0nk/Cosmic-client#installation).

1. Obtain `MapleGlobal-v83-setup.exe` and install the original v83 game in a folder of your choice.
2. Following the upstream guide, remove the installed `HShield` directory and the original `ASPLnchr.exe`, `MapleStory.exe`, and `Patcher.exe` from that client installation.
3. Obtain the compatible Cosmic client WZ files (`cosmic-wz`) and copy them into the MapleStory installation folder, replacing the installed WZ files.
4. Place `HeavenMS-localhost-WINDOW.exe` in the same client folder. It is the modified executable used for local connections.
5. Start the Otono server, then run that client executable to connect to `127.0.0.1`.
6. Optional: after confirming the base client works, apply a compatible HD/resolution client package according to its own instructions. I use a modified version of [Kaentake](https://github.com/iw2d/kaentake), a resizable HD client for v83. My version differs from upstream Kaentake; this is an optional customization, and the upstream client has not been verified here as an exact replacement for my modified version.

Windows Security may flag or quarantine the modified executable, as noted by upstream. An upstream warning does not establish that a particular download is safe; review the source and detection before deciding how to handle it. This guide does not require broad antivirus exclusions.

The client repository uses Git LFS for large files. If cloning it, install Git LFS so you obtain the actual files rather than small pointer files. Its release packages omit the original game installer, which must be obtained separately. See the [Cosmic client repository](https://github.com/P0nk/Cosmic-client) for files and acquisition instructions.

Client `.wz` files belong in the game installation. Server WZ XML exports belong in the server's `wz` folder. They serve different readers and cannot simply be swapped.


The included starter account is:

- Username: `admin`
- Password: `admin`
- PIN, if prompted: `0000`
- PIC: `000000`
- Starter character: `Admin`, with GM level 6.

#### Use the Admin character to adjust your other character, such as character XYZ

The starter Admin character has GM level 6. To adjust another character without running a second client, my workaround is:

1. Log in as character XYZ and enter `@botme`. Your client disconnects while character XYZ remains online under bot autopilot.
2. Log in as the Admin character and select the same channel as character XYZ.
3. Enter `!setgmlevel XYZ 6` to give character XYZ GM level 6. Replace `XYZ` with the actual character name and `6` with the desired rank.
4. Use `@help` to open the available commands menu. It also lists other GM tools, including commands for adjusting NX or mesos; check each command's syntax and target requirements.
5. Log back in as character XYZ when ready to take control again.

`@botme` hands your existing character to autopilot; it does not enroll that character in the generated population scheduler. The GM-level command requires its target to be online on the Admin character's channel. The Admin character already has the rank required to use it.


Use `!wherelevel` to explore the activity guide and entrance teleports. Supported PQ NPCs offer a BotPQ Menu for assembling their dedicated crews. See [Otono's PQ catalog](__Otono%20Server%20Docs/Otono-PQ-catalog.md) for implemented content and verification status.

### Common setup problems

- Java is not found: install a Java 21 JDK and configure `PATH`.
- `target\Cosmic.jar` is missing: build successfully before launching.
- Database connection fails: check MySQL is running, the `cosmic` database exists, and the configuration matches your MySQL credentials and port.
- Missing WZ data: check the extracted XML directory structure and the `wz` folder location.


### Upstream setup references

- [Original Cosmic server setup](https://github.com/P0nk/Cosmic#getting-started)
- [Original Cosmic client setup](https://github.com/P0nk/Cosmic-client#installation)
- [NuTNNuT fork setup and bot foundation](https://github.com/nutnnut/Cosmic#getting-started)

Both server guides cover database, server, client, and login setup; both direct client installation to Cosmic-client. Otono's guide brings those steps together and documents its configuration and bot-party additions. For client downloads, use Cosmic-client; for preparing server WZ XML, consult Cosmic's WZ instructions. Client WZ files must be exported into the server's expected XML format when suitable exports are not already available.

## Documentation and development history

The documentation lives in **`__Otono Server Docs`**:

- [Otono bot knowledge base](__Otono%20Server%20Docs/kb%20Otono%20Bots/kb_README.md): implementation details and established fixes.
- [PQ development journal](__Otono%20Server%20Docs/Otono-PQ-dev-Journal.md): the journey from observations and failed attempts to fixes and client verification.
- [Roadmap](__Otono%20Server%20Docs/Otono-Roadmap.md): retained work, including deferred encounter features.
- [Archived upstream documentation](__Otono%20Server%20Docs/Archive%20NuTNNuT%20Docs): reference material inherited from Cosmic and NuTNNuT.

The journal includes recurring problems such as expiring stage items stranding an AFK player, movement shortcuts, missing stage explanations, and boss hazards that initially did not affect headless bots correctly.

## Credits and lineage

- [P0nk/Cosmic](https://github.com/P0nk/Cosmic): the Cosmic v83 server foundation and its contributors, continuing the OdinMS/HeavenMS lineage.
- [NuTNNuT's Cosmic fork](https://github.com/nutnnut/Cosmic): Otono's direct foundation, autonomous bots, and dashboard.
- [MadaraGameDev/SoloMapling](https://github.com/MadaraGameDev/SoloMapling): messenger bots incorporated into Otono.
- **Togain's YouTube videos:** learning resources used while revisiting MapleStory and developing the project. Specific reference links will be added once confirmed.

Please credit Marokau/Otono and the relevant upstream contributors when adapting this work. Preserve the license and source notices.

## License

[GNU Affero General Public License v3](LICENSE).
