# Bot roadmap

This is the only repository-level list of unfinished bot work. An item belongs here only when the
owner wants it preserved as future work. KB notes and source comments may describe limitations, but
they are not tasks unless linked from this file.

## Pending

| ID | State | Focus | Objective | Source | Done when |
|---|---|---|---|---|---|
| `party-autopilot-stage4` | Decision needed | General party/autopilot | Replace scattered party/autopilot activity flags with a tested state model. | [`party-autopilot-redesign.md`](party-autopilot-redesign.md#deferred-work), `TODO(bot-party-autopilot-stage4)` in `BotEntry` | Legal transitions and reset/persistence rules are characterized; the replacement passes focused mode-interaction tests. |
| `player-led-party` | Decision needed | General party/autopilot | Define when autonomous party bots follow a real player versus continue or resume their own objective. | [`party-autopilot-redesign.md`](party-autopilot-redesign.md#deferred-work), `TODO(bot-player-led-party)` in `BotAutopilotManager` | Authority and transition behavior are approved, implemented through the existing decision seam, and covered by party-policy tests. |
| `stack-shout-trade` | Deferred | Trading | Extend structured shout trading from equips to quantity-bearing consumable stacks. | [`economy.md`](economy.md#deliberate-gaps), `TODO(bot-stack-shout-trade)` in `BotShoutTradeManager` | Quantity-aware WTP/WTS and atomic partial-stack transfer are implemented and tested without duplicating market valuation. |
| `carnival-normal-entry` | Deferred | CPQ1 / CPQ2 | Make assembled Carnival bot teams work when the human uses normal field selection instead of Start bot Carnival match. | [Carnival KB](kb/kb_bot_carnival.md#normal-field-selection-after-bot-assembly) | In CPQ1 and CPQ2, normal field selection with an assembled crew either coordinates both parties through the normal challenge/accept path or clearly redirects the human before reserving a field; no opposing crew is left idle or duplicate crew created. |
| `zakum-town-portal` | Deferred | Zakum | Investigate mage-created town portal support so the human can recharge stars and return during a run. | [Zakum KB](kb/kb_bot_zakum_battle.md#town-portal-and-crew-size-requests) | Skill/map/expedition restrictions are established; the human can make a normal town round trip back to the same fight while the crew handles their absence. Any required exception is presented for a decision before implementation. |
| `zakum-larger-crew` | Deferred | Zakum | Evaluate support for more participants than the current dedicated crew. Desired size and composition remain undecided. | [Zakum KB](kb/kb_bot_zakum_battle.md#town-portal-and-crew-size-requests) | Owner selects size/composition; entry, party support, combat and cleanup work through normal expedition rules for that size. |
| `opq-jail-rooms` | Deferred | OPQ | Implement companion help in the optional bonus jail rooms omitted from the verified main route. | [OPQ KB](kb/kb_bot_opq.md#optional-jail-rooms) | Bots explain the optional rooms and complete their normal switch/combat/reward mechanics with legal movement, human participation and loot access verified in the client. |

## Maintenance rule

1. Add an item only after the owner explicitly asks to retain it as future work.
2. Use a stable short ID, a Focus naming the PQ/expedition or general subsystem, one objective, one source anchor, and a verifiable completion condition.
3. Keep investigation details in the relevant KB/architecture document; do not turn this into a
   handoff, diary, implementation plan, or commit ledger.
4. Source markers use `TODO(bot-<id>)` and must correspond to a row here. Incidental limitations use
   ordinary comments without `TODO`.
5. When work completes, update the durable architecture/KB if needed, then delete the roadmap row.
   Git history is the completion record.
6. Review this file whenever a bot task changes scope. If an item is no longer intended, delete it.
