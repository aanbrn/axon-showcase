# Proposal

## Why

The review system has one reviewer doing too much and one doing too little. `review-quick` is the loop's only automatic
pass, yet its contract obliges it to exhaust a finding class across the artifact, reproduce every claims-table source,
and analyse boundary conditions, callers, and documentation — so a screen costs what a deep audit costs, and the
2026-10-15 baseline recorded 10 units over 87 rounds at 1.67 compliance misses each. `review-thorough` is the expensive
pass and is purely manual, so a change that plainly needs deep review gets none unless someone asks for it. The fix is
one system: a bounded quick screen, a risk-routed deep review, and a single decision that selects it.

## What Changes

- `showcase/quality/agent-skills` (delta) — the quick pass's fixed scope and stop condition; class exhaustion and the
  costly verifications move to the deep pass rather than disappearing; the deep-review routing decision, session
  hand-over, two-pass budget, and finding statuses; the report contract's report-bound distinguished from its
  analysis-bound.
- `.opencode/agent/review-quick.md` — the fixed scope and stop condition, and the clauses it no longer carries.
- `.opencode/agent/review-thorough.md` — the risk dimensions, the finding requirements, and the hand-over and pass
  expectations.
- `.opencode/commands/review-thorough.md` — the deep-review procedure's home: the session create/reuse, the hand-over
  contents, the pass count, and the status record; a manual invocation is an explicit run that does not route.
- `scripts/review-routing.py` + `scripts/test-review-routing.py` — the deterministic routing floor derived from the
  change's own facts, with its known-bad and known-good cases.
- `build.gradle.kts` — the `testReviewRouting` task and its `check` membership.
- `AGENTS.md` — the review-gate workflow's routing step and the deep-review run it selects, and the thorough-review
  bullet's auto-scheduling statement.
- `README.md` — the reviewer table rows, the slash-command row, and the prose describing the two passes.
- `openspec/config.yaml` — the `tasks` rule naming the routing decision beside the capture decision.
- `docs/ideas.md` — the 2026-10-15 "rejected levers" entry updated to scope its rejected cap to the quick loop and to
  record the deep pass's budget as the owner's decision.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the per-change quality-gate requirement's quick-review scenario gains a bounded scope
  and a stop condition and loses class exhaustion, which moves to eight new deep-review scenarios in the same
  requirement — the routing decision, the fail-safe, the low-risk bypass, the session and hand-over, the failed-handover
  detection, the two-pass budget, the finding statuses, and class exhaustion; the shared report contract's requirement
  distinguishes the report's bound from the analysis's bound.

## Impact

- Files: the delta spec, the two agent definitions, the `/review-thorough` command, the new script and its test,
  `build.gradle.kts`, `AGENTS.md`, `README.md`, `openspec/config.yaml`, and `docs/ideas.md`.
- Build: one new `check` member (`testReviewRouting`); no module, service, dependency, or deployment change.
- The round ledger stays the quick loop's record, so the 2026-10-15 baseline it holds stays comparable — the deep pass
  records its status on the change's own review task instead.
- The parked premise conflict is recorded in `design.md`: the record rejected a fixed iteration cap and directed the
  baseline comparison to decide further review-loop changes; the owner confirmed proceeding, and each difference is
  stated there.
