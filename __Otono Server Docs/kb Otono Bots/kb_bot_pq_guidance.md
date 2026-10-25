# Human-led PQ stage introductions

`BotPqStageGuide` supplies ordinary bot speech introductions for Orbis, Magatia,
Ludibrium Maze and Carnival companions, plus Zakum's altar briefing and phase
reminders. KPQ and LPQ keep their separately owner-verified guidance unchanged;
Ellin already has its own stage introductions and reading pause.

Introductions state the objective, what the crew will do and what the human
needs to do. One teammate speaks per stage/instance, using `BotPqChat` so a queued
line expires after its speaker changes maps or events. The crew holds its stage
tasks for seven seconds plus the estimated wait behind already queued speech.
A bot arriving before its human cannot consume that stage's introduction.

Orbis covers the entrance, Center Tower, six side stages, optional Lounge
subrooms, Garden, exit and rewards. Existing introductory pink notices are
replaced by speech; readiness, NPC checks and handoff notices stay private.
Magatia covers both event variants, main stages, research/puzzle subrooms and
rewards. Main stages wait for the human; parallel research rooms and individual
portal puzzles keep working without requiring the human to enter each bot's room.
Maze rooms wait for their human before working; every visited room, coupon
handoff and reward stage explains the next action. Carnival explains scoring,
CP spending, recovery drops, recovery-room exit and results. Opposing bots do
not explain the human team's tasks, and repeat matches reset introductions.

Zakum's reading pause applies only before the summon, while no monsters are
present. Arm/body reminders are queued without pausing combat, healing or
resurrection. Stage guidance does not change navigation physics, attacks,
requirements, puzzle solutions, drops or rewards. Bot-only runs are unchanged.

Automated coverage checks mapped stages, ASCII/capitalization, per-instance
speech deduplication, shared pause timing including queued speech, human arrival,
Carnival speaker roles and repeat reset. A Magatia regression ensures separate
portal puzzles still run in parallel. Client presentation is audited by the owner;
loading the code live does not establish completion of every human-led PQ.

## Cure reaction timing
Owner policy: all PQ, boss and expedition bots wait at least three seconds before actively curing a newly observed curable ailment. Reuse BotPqCureDelay through BotLpqCures rather than adding instant All Cure paths. Natural expiry, legitimate prevention, and uncurable stun are distinct from active potion cures. Current MPQ, LPQ, OPQ, LMPQ, Ellin, GPQ and Zakum controllers use the shared helper.


## Dedicated crews do not request player equipment (2026-10-22)

Dedicated PQ and expedition registration suppresses owner-equipment upgrade requests as well as unsolicited gear offers and peer trades. `BotOfferManager` checks the shared `BotPqDialogue.allowsGearOffer` policy before inspecting newly acquired equipment, choosing an owner upgrade, or creating a request. Its regular expiration pass also cancels existing owner-item reservations and removes queued equipment-begging lines while retaining stage guidance. Human-initiated trades and supply requests remain supported. Ordinary autonomous bots retain their existing policy.
