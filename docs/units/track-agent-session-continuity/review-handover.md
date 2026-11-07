# Review hand-over — track-agent-session-continuity

## Round 1 — proposal screen

- **Unit, tree, branch, base** — `track-agent-session-continuity`; tree `/Users/afanasyev/Development/axon-showcase`
  (the primary checkout, this unit's tree); branch of the same name; base `origin/main` `8020025f`; nothing committed,
  every unit file staged.
- **Packs** — `gradle-build`, loaded in full: the change registers a `check` task and its Python test task.
- **Supplied artifacts** — `proposal.md`; `design.md` (Context with the probe and the precedents, goals and non-goals,
  decisions D1–D7 with their rejected alternatives, risks, the amended-clause table, the claims table and its
  dismissals); `tasks.md`; `.openspec.yaml`; the two deltas at `specs/showcase/quality/agent-skills/spec.md` and
  `specs/showcase/quality/commit-hygiene/spec.md`; and `docs/units/track-agent-session-continuity/unit.md`, which
  carries the probe evidence.
- **Scope** — a proposal screen over those artifacts: their coherence with one another and the repository, delta-block
  and scenario preservation, the amended clauses, the register's rules' implementability, the pause/resume integration,
  and the claims table. Not a code review, and not a repository-wide search.
- **Risk focuses** — the MODIFIED blocks preserve every scenario and the amended-clause table matches them; the new
  record's fields agree with its verification rules and with the checker tasks; the probe's claims and the design's
  consequences agree; the quick ledger's population is not duplicated by the register; the proposed `check` member and
  its tests are concrete.
- **Prior findings** — none; this is the unit's first round.
- **Completed checks** — `openspec validate --all` passes (27 items, with five requirement-length warnings on the new
  requirements); `./gradlew spotlessApply` has run; the claims table is seeded with `scripts/claim-scan.py` and its rows
  reproduce; `python3 scripts/commit-hygiene.py --review-ready` passes.
- **Known limitations** — the probe ran on OpenCode 2.2.6; the TUI's `--session` route is documented rather than probed;
  the register's checker is specified but not implemented, so its rules are reviewed as a specification; no existing
  unit's register is backfilled.

## Round 2 — proposal screen, after round 1's thirteen findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8020025f`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 1's fixes, applied across every site rather than the named one: the reason model now
  separates the recorded values (the format requirement, including `caller-restarted` as a violation) from the permitted
  cases (the quick-loop requirement and the register requirement); the non-report rows' pass, transition, and reason
  rules are stated as bullets with new scenarios; the owner's approval for a discretionary replacement is recorded in
  the hand-over section; the registered agents are named; task 4.5 records this unit's own consultation and deep pass;
  the test list gains a `report` row without a returned identifier, a transition on an unresolved row, and the rewritten
  row, which 2.4 now detects; `scripts/claim-scan.py` gains its task and the proposal a bullet; `AGENTS.md`'s
  unwrappable-record exception is in 4.2's read list and the proposal; the claims table gains the three absence rows;
  the idea's lever is corrected to the first; D4 is bullets; and the three code spans sit on one source line.
- **Prior findings with their instances** — round 1's thirteen, in `findings/round-1.md`.
- **Completed checks** — `openspec validate --all` passes (27 items, the same five requirement-length warnings);
  `./gradlew spotlessApply` has run; `python3 scripts/commit-hygiene.py --review-ready` passes.
- **Known limitations** — as round 1: the probe ran on OpenCode 2.2.6, the TUI route is documented, and the checker is a
  specification until the implementation tasks land.

## Round 3 — proposal screen, after round 2's eight findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8020025f`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 2's fixes: a fresh screen is no longer a discretionary cause, so every permitted cause
  has a recorded value and the register's `caller-restarted` note matches the quick ledger's; the resume scenario says
  an unresolved row resolves no session; the hand-over requirement gains the backfill clause and its scenario, which
  task 4.5 follows; D3, D5, and D7 state the rules the fixes added; the register format points at the review-round
  format's reason set instead of enumerating it again; the amended-clause table reproduces the continuity clause and
  gains the quick-loop body's row; the claims table gains the pre-wait hand-over row; and the requested/returned rule
  and the mismatch rule are stated with their cases.
- **Prior findings with their instances** — rounds 1–2, in `findings/`.
- **Completed checks** — `openspec validate --all` passes (27 items, the same five requirement-length warnings);
  `./gradlew spotlessApply` has run; `python3 scripts/commit-hygiene.py --review-ready` passes.
- **Known limitations** — as before: the probe is OpenCode 2.2.6, the TUI route is documented, and the checker is a
  specification until its tasks land.

## Round 4 — proposal screen, after round 3's three findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8020025f`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 3's fixes: D5 states the pre-mechanism exception and task 4.5 uses the requirement's
  sentence; the created, stalled/failed, and mismatch rows' identifier rules are stated with three new cases; and the
  undefined "fresh screen" is replaced by the quick-loop requirement's "independent screen" in the delta and D4.
- **Prior findings with their instances** — rounds 1–3, in `findings/`.
- **Completed checks** — `openspec validate --all` passes (27 items, the same five warnings); `./gradlew spotlessApply`
  has run; `python3 scripts/commit-hygiene.py --review-ready` passes.
- **Known limitations** — as before.

## Round 5 — proposal screen, after round 4's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8020025f`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 4's two fixes, both in `proposal.md`: the `agent-skills` bullet now says the quick loop
  names its permitted cases while the record format owns the recorded values, and the hand-over bullet carries the
  pre-mechanism exception.
- **Prior findings with their instances** — rounds 1–4, in `findings/`.
- **Completed checks** — `openspec validate --all` passes (27 items, the same five warnings); `./gradlew spotlessApply`
  has run; `python3 scripts/commit-hygiene.py --review-ready` passes.
- **Known limitations** — as before.

## Round 6 — proposal screen after the advisor's review

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8020025f`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — the advisor's four confirmed problems and its open question, each resolved in the planning
  artifacts: the register requirement now states that a row is appended when its outcome is known and the hand-over is
  written before the invocation; a `continued` row must carry the resolved session as its requested identifier, a
  `replaced` row `-`, with two scenarios and their task cases; the backfill is bounded to the unit whose change
  introduces the mechanism, and earlier units are not reconstructed; the amended-clause table gains the layout row; and
  the undetected wrong initial main-session identifier is disclosed as a bounded restart in D6 and the risks, with the
  caller writing its own identifier rather than the record's. The resume requirement's "before it starts the
  replacement" clause is removed in favour of the outcome-time row, and its scenario follows.
- **Prior findings with their instances** — rounds 1–5, in `findings/`.
- **Completed checks** — `openspec validate --all` passes; `./gradlew spotlessApply` has run;
  `python3 scripts/commit-hygiene.py --review-ready` passes.
- **Known limitations** — as before.

## Round 7 — proposal screen, after round 6's three findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8020025f`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 6's fixes: D5 and the proposal's hand-over bullet carry the backfill's introducing-unit
  bound and the prohibition on reconstructing earlier units; D6 starts the forced replacement from the section and
  records it at outcome time; and D6 and the risks state the wrong-identifier residual — one restart when the resuming
  session differs, a stop for the owner when it coincides, with the record never rewritten.
- **Prior findings with their instances** — rounds 1–6, in `findings/`.
- **Completed checks** — `openspec validate --all` passes; `./gradlew spotlessApply` has run;
  `python3 scripts/commit-hygiene.py --review-ready` passes.
- **Known limitations** — as before.

## Round 8 — the parked idea, after the advisor's consultation on a unified history

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8020025f`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — the only change since round 7 is one parked entry in `docs/ideas.md`: the owner's direction
  of one continuous unit-scoped history of typed review and consultation events, with the typed gate projections, the
  deep statuses' home unchanged, an explicit forward boundary, and the advisor-agreed revisit condition. The proposal,
  design, tasks, and deltas are unchanged.
- **Prior findings with their instances** — rounds 1–7, in `findings/`.
- **Completed checks** — `verifyIdeasSections`, `./gradlew spotlessApply`,
  `python3 scripts/commit-hygiene.py --review-ready`; `openspec validate --all` unchanged.
- **Known limitations** — as before.

## `advisor-design-1` — consultation on the session-continuity design, first invocation

- **Written after the invocation** — this consultation ran before the agent-session mechanism existed; its reports in
  that consultation are this section's source, and it is recorded as the unit's first register row.
- **Authorization and question** — the owner's instruction of 2026-11-07 ("Обсудите это с советником до согласия");
  whether the deep-review and advisor sessions should share the quick loop's recorded continuity, and what shape that
  would take.
- **Recommendation** — extend recorded continuity to the deep review and unit-bound consultations; a separate per-unit
  register rather than the quick ledger; decision-plus-opinion scope for the advisor; and a bounded feasibility probe
  before planning.
- **Packs** — the caller classified none; the advisor loaded `gradle-build`.

## `advisor-proposal-1` — consultation on the proposal before the manual pass, first invocation

- **Written after the invocation** — its reports in that consultation are this section's source.
- **Authorization and question** — the owner's instruction "Отревьювь с советником до согласия" (2026-11-07); whether
  the proposal was ready for the owner's manual pass or a defect required a fix first.
- **Recommendation** — revise before approval: the replacement recording's ordering, a `continued` row that need not
  request its session, an unbounded backfill, a missing amended clause, and the wrong-initial-identifier recovery.
- **Packs** — `gradle-build`.

## `advisor-history-1` — consultation on a unified review history

- **Written after the invocation** — its report in that consultation is this section's source.
- **Authorization and question** — the owner's instruction "Можно обсудить с советником и при согласии припарковать"
  (2026-11-07); whether the three agents should share one continuous history of review rounds, and what shape would keep
  the gates meaningful.
- **Recommendation** — park a unified, typed unit-event history as a separate change, with typed gate projections and
  the deep statuses' ownership unchanged; the entry is in `docs/ideas.md` with the agreed revisit condition.
- **Packs** — none.

## Round 9 — implementation screen, first pass

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8020025f`; every file staged.
- **Packs** — `gradle-build`, loaded in full: the change registers a `check` task and its Python test task.
- **Supplied artifacts** — `scripts/commit-hygiene.py` (the register's constants, `--agent-sessions`, the staged block,
  the layout admission, and the `parent-session-changed` reason); `scripts/test-commit-hygiene.py` (the register class
  and its mode mapping); `scripts/claim-scan.py` and `scripts/test-claim-scan.py` (the data-record exemption);
  `scripts/git-hooks/pre-commit` (its header); `build.gradle.kts` (`verifyAgentSessionContinuity` in `check`);
  `docs/contracts/process-bindings.md`; the review-gate and pause-resume skills; the `/review-thorough` and `/consult`
  commands; `README.md`; `AGENTS.md`; `docs/ideas.md`; and the unit's own `sessions.tsv`, `review-handover.md`, and
  `tasks.md`.
- **Scope** — an implementation screen over the change's tasks and delta specs: does each implemented behavior match the
  delta, do the tests exercise each rule, do the documents point at the spec rather than restate it, is the unit's own
  register honest, and does the layout admit the new kind.
- **Risk focuses** — the mode's bullets against the delta's (agent membership, pass contiguity, requested/returned,
  parentage, slug, outcomes); the staged block's index judgment and its removal/rewrite check; the tests' known-bad
  coverage per rule; the docs' single-sourcing of the reason set; the unit's register rows' fidelity to the three
  consultations.
- **Prior findings with their instances** — rounds 1–8, in `findings/`.
- **Completed checks** — `python3 scripts/test-commit-hygiene.py` (397 tests) and `scripts/test-claim-scan.py` (58
  tests); `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `openspec validate --all` 27/0;
  `spotlessApply`; `--review-ready` passes.
- **Known limitations** — the checker is now implemented; the probe's harness facts remain as recorded.

## Round 10 — implementation screen, after round 9's seven findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8020025f`; every file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 9's fixes: the pause-resume skill starts the replacement before recording its row; the
  delta states the per-agent opinion and scope with two scenarios, and the mode enforces both; `check_staged` gains six
  staged-register cases (a valid append, a violation, a removal, a rewrite, a removed register, and the
  index-versus-tree case); the three missing operand cases are added; the layout fixture admits `sessions.tsv`; and the
  five long lines are wrapped.
- **Prior findings with their instances** — rounds 1–9, in `findings/`.
- **Completed checks** — `python3 scripts/test-commit-hygiene.py` (407 tests) and `scripts/test-claim-scan.py` (58);
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `openspec validate --all` 27/0;
  `spotlessApply`; `--review-ready`.
- **Known limitations** — as before.

## Round 11 — implementation screen, after round 10's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8020025f`; every file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 10's fixes, tests only: a reviewer's wrong scope and a consultation's non-slug scope
  now fail; a mismatch row carrying a transition fails; and a key's first row as `continued` or `replaced` fails.
- **Prior findings with their instances** — rounds 1–10, in `findings/`.
- **Completed checks** — `python3 scripts/test-commit-hygiene.py` (412 tests);
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `openspec validate --all` 27/0;
  `spotlessApply`; `--review-ready`.
- **Known limitations** — as before.

## Round 12 — implementation screen, after round 11's finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8020025f`; every file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 11's fix, a test only: a mismatch row carrying a reason with transition `-` now fails.
- **Prior findings with their instances** — rounds 1–11, in `findings/`.
- **Completed checks** — `python3 scripts/test-commit-hygiene.py` (413 tests);
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `openspec validate --all` 27/0;
  `spotlessApply`; `--review-ready`.
- **Known limitations** — as before.

## Round 13 — implementation screen, after round 12's finding and a base refresh

- **Unit, tree, branch, base** — the branch was fast-forwarded from `origin/main` `8020025f` to `40088b3c` after a docs
  unit merged (`#861`); the merge touched `docs/ideas.md`, which both sides changed, and it applied cleanly with this
  unit's parked entry preserved. The claims table is re-anchored to `40088b3c`, and the unit's files are staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 12's fix, tests only: the eight remaining branches now each carry a known-bad case — a
  malformed date, an empty main session, a non-numeric pass, a zero pass, an unknown outcome, a stalled row carrying a
  reason, a report row with an invalid transition, and a continued row returning another identifier. The cases were
  derived by walking the mode's validation branches, not its scenario list.
- **Prior findings with their instances** — rounds 1–12, in `findings/`.
- **Completed checks** — `python3 scripts/test-commit-hygiene.py` (421 tests);
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `openspec validate --all` 27/0;
  `spotlessApply`; `--review-ready`.
- **Known limitations** — as before.

## Round 14 — implementation screen, after round 13's finding

- **Unit, tree, branch, base** — base `origin/main` `40088b3c`; every file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 13's fix, documentation only: the claims table's base sentence now states the actual
  merge-base `40088b3c5a5fb69d8fd421d1c80002057082d0af`, where it had stated the new prefix with the old base's suffix.
- **Prior findings with their instances** — rounds 1–13, in `findings/`.
- **Completed checks** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL;
  `openspec validate --all` 27/0; `spotlessApply`; `--review-ready`.
- **Known limitations** — as before.

## `advisor-design-2` — the same consultation, follow-up invocation

- **Written after the invocation** — its report in that consultation is this section's source.
- **What it asked** — whether the revised design (a row appended at outcome time, decision-plus-opinion scope, the
  bounded backfill, the parentage-validated register, and the resume rule) was agreed, with two remaining questions: the
  probe's placement and a unit's concurrent keys. The advisor accepted the corrections and required the probe to precede
  the design's finalization rather than the proposal.
- **Packs** — the caller classified none; the advisor loaded `gradle-build`.

## `advisor-proposal-2` — the same consultation, follow-up invocation

- **Written after the invocation** — its report in that consultation is this section's source.
- **What it asked** — whether the four corrections plus the row-appended-at-outcome rule were agreed. The advisor found
  one remaining pre-start recording clause in the resume requirement; the caller removed it, and the follow-up's outcome
  is the agreement recorded in the design's Context.
- **Packs** — `gradle-build`.

## `deep-1` — deep review, pass 1 (routed REQUIRED)

- **Change and stage** — `track-agent-session-continuity`, implementation complete, at its implementation review; the
  routing decision returned `REQUIRED` on two added requirement blocks (`agent-skills`, `commit-hygiene`).
- **Base and tree** — `origin/main` = `HEAD` = `40088b3c5a5fb69d8fd421d1c80002057082d0af`; tree
  `/Users/afanasyev/Development/axon-showcase`; every unit file staged; no commits.
- **Planning artifacts** — `proposal.md`, `design.md` (Context, D1–D7, risks, amended clauses, claims table), `tasks.md`
  (implementation tasks ticked), and the two deltas.
- **The implementation** — `scripts/commit-hygiene.py` (the register's constants, `--agent-sessions`, the staged block,
  the layout admission, `parent-session-changed`); `scripts/test-commit-hygiene.py`; `scripts/claim-scan.py` and its
  tests; `scripts/git-hooks/pre-commit`; `build.gradle.kts`; `docs/contracts/process-bindings.md`; the review-gate and
  pause-resume skills; the `/review-thorough` and `/consult` commands; `README.md`; `AGENTS.md`; `docs/ideas.md`; and
  the unit's own `sessions.tsv` and records.
- **Prior findings and their statuses** — the quick loop's rounds 1–14, all `FIXED_AND_VERIFIED`; the last round
  reported nothing. No deep-review finding exists yet.
- **Pass and limitations** — pass 1 of the two-pass budget, an initial review rather than a fix verification. The
  harness facts underlying the mechanism are the unit record's probe excerpts; the TUI resume route is documented rather
  than probed.
- **Packs** — `gradle-build` (the `check` member and its Python tests); the change also decides process-record
  semantics, for which the caller's classification named no pack.

**Pass 1 findings** (recorded from its report; statuses pending the owner's choice):

- **B1 (blocking)** — the checker never rejects a `replaced` row that returns the identifier it replaced; the spec's
  "otherwise" makes the returned identifier the determinant, and the quick ledger's own rule enforces the mirror case.
  `FIXED_AND_VERIFIED` by pass 2.
- **S1 (should-fix)** — the bindings declaration's non-adopter bullet names only the round-record directory, not the
  agent-session record the checker also embeds. `FIXED_AND_VERIFIED` by pass 2.
- **S2 (should-fix)** — tasks 5.5–5.7 are unticked though the records show the work done. `FIXED_AND_VERIFIED` by
  pass 2.
- **N1 (nitpick)** — the checker's module docstring omits `--agent-sessions` from its mode inventory.
  `FIXED_AND_VERIFIED` by pass 2.

## `deep-2` — deep review, pass 2 (fix verification)

- **Change and stage** — the same change, at its implementation review; pass 1 of the two-pass budget raised B1
  (blocking), S1, S2, and N1, and the owner authorized fixing all four.
- **Base and tree** — `origin/main` = `HEAD` = `40088b3c5a5fb69d8fd421d1c80002057082d0af`; every unit file staged; no
  commits.
- **What was fixed** — B1: the `replaced` branch now rejects a row returning the identifier it replaces, with a
  known-bad case; S1: the bindings declaration's non-adopter bullet names the agent-session record the checker also
  embeds; S2: tasks 5.5–5.7 are ticked with their evidence; N1: the checker's docstring lists `--agent-sessions`.
- **Prior findings and their statuses** — pass 1's four are `OPEN` until this pass verifies them; the quick loop's
  rounds 1–14 are all `FIXED_AND_VERIFIED`.
- **Pass and limitations** — pass 2 of the two-pass budget, a fix verification rather than an initial review. The
  harness facts remain the unit record's probe excerpts; the checker cannot attest a stated cause, an approval, or
  timing.
- **Packs** — `gradle-build`.

## `advisor-readiness-1` — consultation on implementation readiness, first invocation (written late)

- **Written after the invocation** — this section was written once the consultation's first report had returned, not
  before the call as the requirement states; process defect `#2` records it.
- **Authorization and question** — the owner's instruction "Отревьювь с советником до согласия" (2026-11-07); whether
  the implementation was ready for the manual pass and, on approval, delivery.
- **Recommendation** — correct two record/procedure gaps first: the backfill collapsed each consultation's two
  invocations into one row, and the resume ordering put an invocation ahead of in-flight outcome recovery.
- **Packs** — `gradle-build`.

## `advisor-readiness-2` — the same consultation, follow-up invocation

- **Written after the invocation** — its report in that consultation is this section's source.
- **What it asked** — whether the two corrections were agreed. The advisor accepted both and required this
  consultation's own invocation record to be completed, identifying a late hand-over as late; the register and these
  sections are that record.
- **Packs** — `gradle-build`.

## Round 15 — implementation screen, after the advisor's implementation-readiness review

- **Unit, tree, branch, base** — base `origin/main` = `HEAD` = `40088b3c`; every file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — the advisor's corrections, applied: the agent-session register now records each invocation
  (nine rows: the design, proposal, history, readiness, and deep sessions, with their follow-ups), each with its own
  hand-over section; the two readiness sections state they were written late, and process defect `#2` records it; and
  the pause-resume skill establishes in-flight outcomes before any invocation, with the sessions step after it. The rest
  of the implementation is unchanged since round 14.
- **Prior findings with their instances** — rounds 1–14, in `findings/`.
- **Completed checks** — `python3 scripts/test-commit-hygiene.py` (422);
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `openspec validate --all` 27/0;
  `--agent-sessions`, `--unit-layout`, `--expect-clean`, `--session-continuity`, `--review-ready`; `spotlessApply`.
- **Known limitations** — as before.

## Round 16 — implementation screen, after round 15's two findings

- **Unit, tree, branch, base** — base `origin/main` = `HEAD` = `40088b3c`; every file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 15's fixes, in `tasks.md` only: task 2.2's done note no longer states a test count its
  own later additions moved, and task 5.10's cites the process-defect record it wrote instead of a gate's output.
- **Prior findings with their instances** — rounds 1–15, in `findings/`.
- **Completed checks** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `--agent-sessions`,
  `--expect-clean`, `--session-continuity`, `--review-ready`; `spotlessApply`.
- **Known limitations** — as before.

## Round 17 — implementation screen, after round 16's finding

- **Unit, tree, branch, base** — base `origin/main` = `HEAD` = `40088b3c`; every file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 16's fix, in `tasks.md` only: task 5.2's done note states the command's outcome instead
  of a count the later rounds moved.
- **Prior findings with their instances** — rounds 1–16, in `findings/`.
- **Completed checks** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `--agent-sessions`,
  `--expect-clean`, `--session-continuity`, `--review-ready`; `spotlessApply`.
- **Known limitations** — as before.
