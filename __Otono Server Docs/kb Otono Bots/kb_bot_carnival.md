# Monster Carnival dedicated crews

## Rules and reference

Owner-selected references for future audits (2026-10-21):

- CPQ1: [Hidden Street - Monster Carnival](https://global.hidden-street.net/party-quest/monster-carnival).
- CPQ2: [Hidden Street - Monster Carnival 2](https://global.hidden-street.net/party-quest/monster-carnival-2).

Both pages returned HTTP 403 during retrieval. The owner subsequently supplied
the relevant field-size excerpts directly on 2026-10-21.
Keep local implementation evidence distinct from historical guide claims.

The supplied *MapleStory PQ summaries.docx*, chapter 15, describes two competing teams,
earned CP, monster/debuff/guardian purchases, dropped recovery items and ordinary rewards.
Carnival 1 admits levels 30-50. Owner-supplied reference excerpts specify
CPQ1 fields 1-4: 2-4 per team; fields 5-6: 3-6 per team. CPQ2 fields 1-2:
2-4 per team; field 3: 3-6 per team. Local scripts checked entry minimums and
generic party size but did not establish these field-specific historical maxima.
The dedicated bot menus use four per team in the larger fields. Carnival 2 is a separate level-51..70 bracket with
its own lobby, challenge API and reward NPC.

`MonsterCarnival.complete()` snapshots **total earned** team CP before the result sequence.
Spending CP and the death CP deduction reduce available CP, not this total. Ties extend
the match. `2042002.js` gives the normal result EXP, using team total to select rank.
No winner is assigned by the bot controller, and no win trading is implemented.

## Recruitment and execution

Spiegelmann's Carnival 1 and Carnival 2 lobby menus offer assembly, start,
dismissal and the original field selection. `BotCarnivalCompanions` recruits
three human teammates (Cleric, Spearman, Assassin) and an opposing party with
those classes plus a Hunter. Both parties have four members. New CPQ1 fixtures
start at level 40; CPQ2 fixtures start at 55. The human's class, level and actions
can affect match balance. Scoring and rewards remain ordinary Carnival rules.

The `cpq` and `cpq2` pools reuse eligible offline `CPqClassN` / `CPq2ClassN`
fixtures (Mage, Spear, Thief, Archer).
Population and personally owned bots are excluded. New fixtures receive ordinary
level-based AP/SP, obtainable class equipment and ammunition. Existing autonomous
population scheduling is unchanged. Over-level fixtures cannot enter another match.

Start selects a free large field (CPQ1 field index 4 or 5; CPQ2 index 2), sends the opposing party through the ordinary NPC
lobby path, sends the human's normal challenge and has the opposing leader accept its
actual accept/decline dialogue. Challenge delivery and acceptance share the crew lock:
Graal contexts cannot be entered concurrently by the human packet thread and bot tick.
No direct construction of a Carnival instance, score changes or timer changes occur in
the bot controller.

`BotCarnivalRun` owns only explicitly registered companions. It waits outside the match,
uses full simulation inside the active Carnival (which has no EIM), collects recovery
drops, fights eligible monsters and spends earned CP through `MonsterCarnivalHandler`.
Summoned monsters and guardians of the bot's own team are excluded from attacks;
neutral initial monsters remain eligible. Target acquisition, projectile resolution,
AoE secondary targets, Heal attacks and contact damage use the same team eligibility.

Carried HP/MP potion keys are removed for the registered crew, and the normal potion
scanner cannot reinstall them during Carnival. Carried buff consumables are also
suppressed. Recovery drops use `Character.pickupItem` / `applyConsumeOnPickup`; ordinary
skill healing and passive regeneration still work. Meso and missing-item map drops
must be excluded before resolving a pickup's item effect.

Bots use the existing ordinary death respawn path and the recovery room's `MCrevive`
portal. Result NPC interaction uses template **2042002** for CPQ1 and **2042007** for
CPQ2, including their exit rooms; lobby templates 2042000/2042005 are absent there.
`BotCarnivalFormat` supplies bracket-specific levels, lobby maps and NPC templates. Humans claim their own reward. The crew
waits in the lobby while its human retains the original party leadership. It
automatically saves and dismisses both teams after party departure, party replacement,
leadership loss, channel change or disconnect. Bots finish normal exit dialogue
before dismissal; detached crews do not wait for human stage briefings. Assembly
refuses a second crew for the same character while an earlier crew is still active.

## Guardian terminal state

WZ guardians 9980000/9980001 spawn at state 1 and require four player hits to state 5.
State 5 is animation-only. The generic `Reactor` terminal handling excludes these IDs,
so the original path never called their dispel script or released their spawn point.
`Reactor.hitReactor` now invokes the ordinary guardian action on the last required hit,
frees the spawn and removes the destroyed map object. This shared correction applies
to human and bot hits. Controllers ignore inactive guardians rather than camping at
their old position. The focused guardian test verifies both teams and exact hit count.

## Validation limits

A normal timed two-versus-two bot-only match reached both rank-A result dialogues,
awarded the usual winner/loser EXP and returned all four participants alive to the
lobby. The same fixtures were reused in regular-`Client` stand-in tests of human
assembly, leadership, challenge acceptance, battlefield entry and ordinary party-leave
cancellation/exit. Carried Ginger Ales were unchanged during the full match. Live
guardian destruction was observed after the shared terminal-state correction.

Headless tests cannot establish the real client's Carnival window presentation or
visual team transparency. Owner verified both Carnivals in the real client on 2026-10-21. Both bot menus now offer four versus four, including Carnival 2. Twenty-two
focused tests cover recruitment, correct large-field challenge APIs, NPC menus,
team targeting, potion restrictions and guardian destruction. Both live channel-2 four-versus-four matches completed on normal timers: all
16 bots reached grade-A result dialogues, claimed rewards and returned alive to
their respective lobbies, with all carried Ginger Ale counts still 200. Owner verified both larger-team formats in the client on 2026-10-21. The completed match did not require a
death/re-entry, so that path has code-path review rather than an observed live death.


CPQ2 fixtures share OPQ/LMPQ's exclusion of Zakum helmets and special MV STR hats.
The CPQ2 lobby script uses an explicit 2..6 party-size check instead of comparing
the Java member collection with a number. Only the opt-in bot crew is recruited;
existing autonomous population and ordinary match timers are unchanged.


Regular-Client stand-in checks also opened both actual BotPQ menus, assembled
three allies and four opponents with the human as leader, entered each bracket's
large battlefield through ordinary challenges, and returned via normal party-leave
cancellation and exit dialogue. The same fixtures were reused after the full
matches; no temporary tick hook was installed. This verifies server-side human
entry flow, not the real client's presentation.
Party-departure cleanup and exact same-ID reuse were verified live with online regular-client stand-ins in both brackets on 2026-10-21; 22 focused controller/team/menu tests passed. Test fixtures were saved and dismissed.


### Real-client CPQ2 NPC override

NPC 2042005 has a native WZ mc2_enter script. The server's 2042005.js BotPQ Menu
requires its entry in config.yaml NPCS_SCRIPTABLE (USE_NPCS_SCRIPTABLE enabled).
PlayerLoggedinHandler sends the ordinary SET_NPC_SCRIPTABLE packet on login;
the same full override list can be sent live to an already connected client.
This client path is not verified by headless NPCTalk/menu tests alone. The override
was configured and sent to NelBot on 2026-10-21; the owner subsequently confirmed CPQ2 works in the client.


Field-list labels state 2-4 players per team on small fields and 3-6 per team
on large fields, matching the owner's supplied guide excerpts. Four-versus-four
fits either range. Bot auto-selection uses CPQ1 field 5 or 6 and CPQ2 field 3;
it is not restricted to CPQ1 field 6 specifically.


### Normal field selection after bot assembly

On 2026-10-21 the owner reported: assemble bot teams, choose Normal Carnival
field selection, select Field 1; the human's team enters but the opposing crew
does not join. Bracket was not specified in this report. This alternate route
has not been independently reproduced or diagnosed. The currently verified
bot entry route is Start bot Carnival match, which explicitly reserves a field
for the opposing party and sends/accepts the human's challenge. The report does
not establish whether the teams had already reached an actual battlefield or
only its waiting lobby. Retained at the owner's request as deferred work in
[the roadmap](../Otono-Roadmap.md), ID carnival-normal-entry. No gameplay change was
made for this finding.
