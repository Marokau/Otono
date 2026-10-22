# Human-led PQ boss loot

Dedicated human-led PQ crews reserve original boss drops for the player through
`BotPqBossLoot` and the shared `BotLootEligibility` gate. Both nearby pickup and
loot targeting use that eligibility path. The original drop's `Monster.isBoss()`
flag identifies boss loot; ordinary population bots and bot-only test crews are
unaffected. This policy applies across dedicated PQ controllers, not only KPQ.

Leaving original loot on the ground does not consent to bot pickup. A human can
pick up unwanted gear and redrop it, creating an ordinary player drop. Normal
pickup ownership, inventory capacity, quest checks and bot behavior still apply;
eligibility is not a promise that a bot will collect every unwanted item.
Zakum's existing broad loot reservation also allows deliberate human redrops.

Required stage items retain their controller-specific rules before the gear-loot
gate: KPQ passes, LPQ passes/Alishar's clear item, OPQ statue pieces, LMPQ coupons
and Magatia stage items must remain usable by their assigned collectors.
Normal ground expiry remains unchanged except for the separately documented
KPQ pass protection. Boss gear is not made permanent by this policy.

One companion queues a normal speech-bubble announcement per boss per event when
original loot is observed. It tells the human to take the loot and redrop unwanted
gear. The speech uses instance/map context so it does not follow the party into
an unrelated room. No loot is transferred, generated or removed by the policy.

LPQ stage seven suppresses the generic boss-loot announcement: Rombard uses a boss flag in data, but the stage is a reactor-driven pass wave rather than the final boss encounter. Loot reservation itself is unchanged; required passes retain their collector rules.
