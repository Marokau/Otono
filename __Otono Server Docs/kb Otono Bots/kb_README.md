# Bot knowledge base

Durable, project-scoped notes for facts that are expensive to rediscover. These are incident and
subsystem references, not task status, handoffs, or release checklists. Prefer the specialized skills
for broad combat/navigation workflows and open a KB entry only for the specific failure mode at hand.
The only actionable backlog is [`../ROADMAP.md`](../Otono-Roadmap.md).

Maintain entries as CURRENT TRUTH: when a fact changes, edit or replace the stale text in place â€”
never append dated "UPDATE:" blocks at the bottom that leave obsolete claims standing above them.
Every line of an entry must be true as written; git history (keep commit hashes as provenance where
useful) records how it evolved.

## Navigation and movement

- [`kb_bot_nav_oscillation_rootcauses.md`](kb_bot_nav_oscillation_rootcauses.md): recurring loop classes
  and the builder/executor mismatches behind them.
- [`kb_bot_map_partition_travel.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_map_partition_travel.md): arrival-dependent reachability on
  split maps.
- [`kb_bot_swim_and_forbidfalldown.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_swim_and_forbidfalldown.md) and
  [`kb_bot_slippery_ground_physics.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_slippery_ground_physics.md): special physics.
- [`kb_bot_downjump_eligibility.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_downjump_eligibility.md): down-jump evidence and limits.
- [`kb_bot_nav_selfloop_portal_unfollowable.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_nav_selfloop_portal_unfollowable.md),
  [`kb_bot_empty_committed_route_replan.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_empty_committed_route_replan.md), and
  [`kb_bot_nav_search_scaling.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_nav_search_scaling.md): planner/cache edge cases.
- [`kb_bot_town_nav_airborne_target.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_town_nav_airborne_target.md),
  [`kb_bot_logout_loiter_unreachable_anchor.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_logout_loiter_unreachable_anchor.md), and
  [`kb_bot_sleepywood_trap_return_scroll.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_sleepywood_trap_return_scroll.md): travel recovery.

Do not copy a graph version from a KB note. The current value and full schema history live only in
`BotNavigationGraphProvider.GRAPH_VERSION`.

## Travel and errands

- [`kb_bot_eventmanager_transit_rides.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_eventmanager_transit_rides.md): scripted transit.
- [`kb_bot_oneway_map_and_fare_deadlocks.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_oneway_map_and_fare_deadlocks.md): conditional-edge
  trap audit rules.
- [`kb_bot_npc_dwell_walk_in_place.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_npc_dwell_walk_in_place.md),
  [`kb_bot_errand_dwell_clobber.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_errand_dwell_clobber.md), and
  [`kb_bot_errand_progress_ssot.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_errand_progress_ssot.md): dwell/progress ownership.
- [`kb_bot_npc_hop_hail_map_wide.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_npc_hop_hail_map_wide.md) and
  [`kb_bot_job_errand_bagfull_ferry_deadlock.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_job_errand_bagfull_ferry_deadlock.md): transport
  fallback and detour priority.
- [`kb_bot_job_change_walk_to_npc.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_job_change_walk_to_npc.md) and
  [`kb_bot_fetch_quests.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_fetch_quests.md): legal NPC/quest execution.

## Combat, skills, and progression

- [`kb_bot_cleric_heal_architecture.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_cleric_heal_architecture.md): healing/buff path.
- [`kb_bot_aoe_cluster_target_bias.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_aoe_cluster_target_bias.md) and
  [`kb_bot_aoe_reposition_before_fire.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_aoe_reposition_before_fire.md): AoE selection.
- [`kb_bot_ranged_spacing_weapons.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_ranged_spacing_weapons.md) and
  [`kb_bot_thief_claw_dagger_split.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_thief_claw_dagger_split.md): ranged/no-ammo behavior.
- [`kb_bot_mage_cast_speed.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_mage_cast_speed.md): binary-proven â€” magic casts ignore
  wand/staff speed (always Normal(6) + Booster); mage weapon scoring is speed-blind.
- [`kb_bot_quest_commitment_and_danger_targeting.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_quest_commitment_and_danger_targeting.md):
  quest and survival biases.
- [`kb_bot_skill_classification.md`](kb_bot_skill_classification.md),
  [`kb_bot_missing_class_sp_builds.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_missing_class_sp_builds.md), and
  [`kb_bot_passive_mp_hp_regen.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_passive_mp_hp_regen.md): skill/build/recovery facts.
- [`kb_bot_alert_stance_emulation.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_alert_stance_emulation.md): alert presentation.
- [`kb_bot_temple_of_time.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_temple_of_time.md): the quest-gated Temple of Time corridor â€”
  the per-bot reachability overlay (`QUEST_GATED_ENTRANCES`/`unlockedTempleGates`) and the
  `BotTempleProgressionManager` questline driver (3500-3521).
- [`kb_bot_zakum_prequest.md`](kb_bot_zakum_prequest.md): the Zakum prequest driver
  (`BotZakumPrequestManager`) â€” 100200/100201 custom-quest mechanics, the solo Zakum PQ
  (7-key item-trigger Giant Chest), Breath of Lava, the gold-tooth grind, and the
  script-only quest-item sell guard shared with the Temple driver.
- [`kb_bot_zakum_battle.md`](kb_bot_zakum_battle.md): dedicated Adobis crews, normal
  expedition/summoning paths, separated arm bounds, supplies and headless-test limits.
- [`kb_bot_pq_supervised_run_stucks.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_pq_supervised_run_stucks.md): player-led PQ runs â€”
  map-change housekeeping must precede tick-consuming hooks (frozen-midair class) and follow must
  not warp into an event instance the bot isn't registered in (doorâ†”instance bounce class).
- [`kb_bot_pq_guidance.md`](kb_bot_pq_guidance.md): stage introductions, human reading pauses and per-instance speech for the remaining dedicated crews.
- [`kb_bot_gpq.md`](kb_bot_gpq.md): guild creation/crew membership, GPQ through puzzles, sacrifice, Ergoth, rewards and normal exit; human-client accepted with an Ergoth survivability caveat; latest protection changes not yet re-audited.
- [`kb_bot_mvpq.md`](kb_bot_mvpq.md): normal entry, earned letters, Beginner-accessible traversal, hazards and human-led waiting; full bot-only clear through rewards/exit and human-client verified 2026-10-25.
- [`kb_bot_horntail.md`](kb_bot_horntail.md): human-client clear in about 46 main-body minutes with no observed deaths; native trial/raid, individual Seduce targeting and upper-head firing follow-up.
- [`kb_bot_pinkbean.md`](kb_bot_pinkbean.md): thirty-person headless full clear within the native hour; approximately 59m27s from observed entry, 45 deaths and seven drop pickups. Two narrow native-hour clears; latest rerun eliminated logged body-contact fatalities but still had 58 observed deaths. Five-minute safety margin, attack/Seduce survival, safe return positioning and human-client verification remain unresolved.
- [`kb_bot_kpq.md`](kb_bot_kpq.md): Kerning PQ gameplay entry and NPC paths, companion-only
  automation, question-table mismatch, leader/pass collection, and instance simulation limits.
- [`kb_bot_lpq.md`](kb_bot_lpq.md): Ludibrium PQ script requirements, supplied guide differences,
  verified six-bot completion through rewards, skill-room/ranged/puzzle behavior,
  human-led NPC assembly, and current live-verification limits.
- [`kb_bot_carnival.md`](kb_bot_carnival.md): dedicated opposing crews, normal CP scoring,
  recovery-item restrictions, NPC challenge flow and guardian terminal states.
- [`kb_bot_ellin.md`](kb_bot_ellin.md): Forest of Poison Fog dedicated crew, poison reactors, player-handler captures and human turn-ins.
- [`kb_bot_magatia.md`](kb_bot_magatia.md): Romeo/Juliet dedicated crews, real research-room handoffs,
  observed portal learning, environmental recovery and verification limits.
- [`kb_bot_lmpq.md`](kb_bot_lmpq.md): Ludibrium Maze PQ room/portal contracts, dedicated crews,
  human NPC assembly, complete coupon handoffs, and repeat completion through rewards.
- [`kb_bot_opq.md`](kb_bot_opq.md): Orbis PQ guide/script differences, dedicated crew,
  human NPC assembly, room mechanics, legal reactor approaches, and verified completion.

## Items, economy, and trading

- [`kb_bot_equip_optimizer.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_equip_optimizer.md) and
  [`kb_bot_grind_gear_valuation.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_grind_gear_valuation.md): equipment decision seams.
- [`kb_bot_use_value_shelf.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_use_value_shelf.md): USE inventory tiers.
- [`kb_bot_shop_search_need_coverage.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_shop_search_need_coverage.md): two-ring, need-first shop
  discovery and return-scroll runway stocking.
- [`kb_bot_skill_books.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_skill_books.md): fourth-job book demand, use, sharing, and market flow.
- [`kb_bot_market_price_discovery.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_market_price_discovery.md): clearing-seeded consensus,
  hourly unsold/fast-sale pressure, and equilibrium damping.
- [`kb_bot_market_shout_haggle.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_market_shout_haggle.md): `B>` criteria shouts and the S4
  trade-chat haggle protocol â€” floor-priced criteria, spoken-position parsing, structural commitments.
- [`kb_bot_market_stall_funnel.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_market_stall_funnel.md): tripâ†’publish funnel losses â€”
  permit NX economics, placement geometry (spacing, multi-floor strips), restart seeding.
- [`kb_bot_trade_dupe_loss_audit.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_trade_dupe_loss_audit.md): open-trade concurrency hazards.
- [`kb_bot_market_event_time_tz.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_market_event_time_tz.md): read the tape's `at` TIMESTAMP via
  `UNIX_TIMESTAMP` â€” `getTimestamp().getTime()` is skewed by the JVM-vs-MySQL timezone offset.

## Population, social, and operations

- [`kb_bot_ambient_megaphones.md`](kb_bot_ambient_megaphones.md): fixed SoloMapling dialogue on a
  shared world timer, existing autonomous senders, and runtime settings.

- [`kb_client_ui_wz_map_crashes.md`](kb_client_ui_wz_map_crashes.md): clean v83 UI.wz
  replacement fixes the locally reproduced third-job boss-room client crash;
  the customized F8 bot inventory interface is lost.
- [`kb_bot_player_party_social.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_player_party_social.md): real-player party safeguards.
- [`kb_bot_chair_sit_and_shout_padding.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_chair_sit_and_shout_padding.md): idle chair sitting
  (owned-via-gacha, sprite-size hog bias) and obnoxious `@@@` shout-bubble padding â€” the `obnoxiousness`
  / `sitAppetite` personality traits and the stand-before-acting invariant.
- [`kb_bot_self_owned_owner_assumptions.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_self_owned_owner_assumptions.md): ownership audit.
- [`kb_bot_double_register_botpop_race.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_double_register_botpop_race.md): population registry race.
- [`kb_bot_poisoned_map_zombie_respawn_loop.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_poisoned_map_zombie_respawn_loop.md): disconnect vs
  map-change race leaves a disposed ghost in `mapobjects` (map poisoned, zombie respawn loop) + the
  `canWarpMap` latch leak that silently freezes a character's warps.
- [`kb_bot_inert_autopilot_recovery.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_inert_autopilot_recovery.md): leaked-off autopilot,
  recovery, and stuck classification.
- [`kb_bot_ops_console.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_ops_console.md) and
  [`kb_bot_response_overlay.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_response_overlay.md): operator/prompt surfaces.
- [`kb_bot_chat_charset_ascii_only.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_chat_charset_ascii_only.md): client text encoding.

## Performance and persistence

- [`kb_save_throughput_and_deadlocks.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_save_throughput_and_deadlocks.md): save batching and the
  resolved InnoDB deadlock pattern.
- [`kb_bot_cold_decide_gc_storm.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_cold_decide_gc_storm.md) and
  [`kb_bot_coldstart_queststatus_fullscan.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_coldstart_queststatus_fullscan.md): boot warm-up.
- [`kb_bot_population_cpu_hotspots.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_population_cpu_hotspots.md): steady-state CPU hot-spot
  classes at population scale (per-candidate A* probes, cache scans, per-tick WZ parses) and the
  JFR-vs-`/api/perf` profiling workflow that finds them.
- [`kb_live_values_view_cme_in_bot_ticks.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_live_values_view_cme_in_bot_ticks.md): unsafe live-view
  iteration.
- [`kb_wz_xml_deferred_dom_races.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_wz_xml_deferred_dom_races.md): shared WZ XML `Data` trees +
  Xerces deferred DOM = permanent subtree corruption under concurrent reads (the "im at map <id>"
  name-lookup flakes); fixed by materializing documents at parse.
- [`kb_bot_lod_abstract_grind_calibration.md`](../Archive%20NuTNNuT%20Docs/docs/bot/kb/kb_bot_lod_abstract_grind_calibration.md): the unobserved-map
  abstract grind measured a peak kill rate instead of a sustained one; signatures, the corrected
  estimator, and how to measure ground truth without starving the tick pool.

## Maintenance

- Keep one file per reusable fact or tightly coupled failure class.
- Remove commit ledgers, stale line numbers, completed checklists, live process state, and handoff text.
- Correct an existing entry instead of appending a chronological correction trail.
- Link stable symbols/files. Avoid duplicating large source tables or configuration defaults.
- Describe unresolved facts as known limitations, not promises. Only `../ROADMAP.md` defines tasks.

- [`kb_bot_pq_names.md`](kb_bot_pq_names.md): dedicated crew naming conventions and future prefixes.
- [`kb_bot_pq_boss_loot.md`](kb_bot_pq_boss_loot.md): human-first boss loot, deliberate redrops and stage-item exceptions.

- [`kb_bot_raid_equipment.md`](kb_bot_raid_equipment.md): native raid gear goals, Gelt/Apple supplies, secondary AP readiness and saved external class-build references.
