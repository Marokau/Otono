# Ambient bot megaphones

`BotAmbientMegaphone` emits regular and avatar megaphones from registered, online, self-owned bots.
It runs every five seconds from `BotScheduler.start`, independently of whether the population
scheduler is enabled. Each world has one randomized broadcast deadline, so population changes
do not multiply the rate. Worlds without an online human stay quiet. Owned companions, logging-out
bots, dead bots and event participants cannot be chosen. The sender's actual world/channel is used,
and immediate sender/message repeats are avoided when alternatives exist.

The packet and world broadcast reuse the player path in `UseCashItemHandler`:
`PacketCreator.serverNotice(3, channel, "name : message", true)`. No item is consumed for this
requested ambient simulation. It does not create bots, alter their routines, or use an LLM.
By default 75% are regular and 25% are avatar messages, matching SoloMapling. Avatar messages use
Diablo, Cloud 9, Loveholic, Cute Tiger or Roaring Tiger styles (IDs verified in `handbook/Cash.txt`).
They choose a complete line of at most 52 characters from the selected category, split into four
13-character rows, and use `getAvatarMega` plus the player's ten-second `byeAvatarMega` cleanup.
If a future category contains no short lines, that message falls back to regular text. Both types
share the existing timer. Item megaphones are not emitted.

The bundled `src/main/resources/bots/MegaphoneDialogue.yaml` is the original fixed dialogue pool
from [SoloMapling](https://github.com/MadaraGameDev/SoloMapling/blob/main/src/main/java/soloMapling/ArtificialPlayer/BotDialoguePack/MegaphoneDialogue.yaml).
The eight category weights match its `SocialHotPotatoManager`: birthdays 5%, item sales 7%,
guild recruitment 7%, PQ recruitment 7%, RWT flavor spam 19%, flex 19%, announcements 18%, social
messages 18%. These are scripted atmosphere, not live sale listings/recruitment or verified bot
achievements. The original pool includes contemporary slang and references that may not match
this server. No claim is made about the original source of each line.

Loading accepts string rows and `{line: ..., emote: ...}` rows. Emotes are ignored. Smart
punctuation is converted and other non-ASCII characters stripped for the v83 text encoding.

Existing `BotManager.cfg` reflection exposes these settings in the admin dashboard and `!botcfg`:

- `AMBIENT_MEGAPHONES_ENABLED`: true by default.
- `AMBIENT_AVATAR_MEGAPHONE_PERCENT`: 25 (0 disables avatar messages; clamped to 0-100).
- `AMBIENT_MEGAPHONE_MIN_MS`: 30000.
- `AMBIENT_MEGAPHONE_MAX_MS`: 90000.

The timer enforces a minimum ten-second interval and tolerates reversed interval settings.
Disabling clears pending deadlines; enabling starts a new randomized wait. The settings follow
the existing manager configuration's runtime semantics. Dialogue is loaded once from the jar;
editing it requires a rebuild and restart. No MySQL changes are required.

`BotAmbientMegaphoneTest` verifies loading/ASCII, original category weights, sender eligibility,
population-independent cadence, silent empty worlds, and the enable switch.
