# Review hand-over — reject-ignored-openspec-delta-sections

One section per quick-review invocation, recorded before the caller waits on it.

## Round 1 — proposal screen

- **Unit** — `reject-ignored-openspec-delta-sections`, an OpenSpec change. It adds a CI guard that fails the `build`
  check when `openspec validate --all`'s output carries the ignored-delta warning, because a malformed delta section is
  silently dropped at archive when the change also carries a valid one.
- **Revisions** — base `origin/main` `1adfc2ce`; the work is uncommitted on the branch.
- **Workspace** — `/Users/afanasyev/Development/axon-showcase`, branch `reject-ignored-openspec-delta-sections`.
- **Packs** — `none`: the change edits a CI gate and a spec, no JVM, test, build, or deployment subject.
- **Supplied artifacts** — the change dir (`proposal.md`, `design.md` with the probe's two fixtures and the proposed
  step text, `tasks.md` with the claims table, and the delta on `merge-governance`'s two gate requirements).
- **Scope** — whether the probe's evidence supports the guard, whether the proposed step is correct shell and fails on
  the bad shape without misfiring on the corpus's >500-character warning, whether the delta's `MODIFIED` blocks preserve
  every scenario in order, and whether the parked entry's removal is complete.
- **Prior findings** — none; the unit's first invocation.
- **Completed checks** — `openspec validate --all` 27 items; `spotlessApply`.
- **Known limitations** — the probe ran in a throwaway copy of `openspec/` under the session's scratch directory, and
  its transcript lives only in this session; the guard's behaviour on the live gate is exercised by this change's own
  `build` run after implementation.

## Round 2 — proposal screen, after round 1's six findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `1adfc2ce`.
- **Packs** — `none`.
- **Supplied artifacts** — the proposal revised: the gate now calls a committed script (`--validate-output` predicate,
  `--probe` CLI check) with its own test and a `check` task, instead of an inline grep; the step keeps the diagnostic on
  a failing validation; the enforced class is "content the CLI would drop at archive" (a malformed section or unowned
  prose, the prose shape probed too); the parked entry's removal touches only its bullet because a sibling remains; the
  claims row records the corpus's 91 long-requirement warnings; the five-space pattern artefact is gone.
- **Prior findings with their instances** — all six in `findings/round-1.md`.
- **Completed checks** — `openspec validate --all` 27 items; `spotlessApply`.
- **Known limitations** — the probe's transcript is session-only; the CLI-dependent half is exercised by CI.

## Round 3 — proposal screen, after round 2's five findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `1adfc2ce`.
- **Packs** — `none`.
- **Supplied artifacts** — the proposal with round 2's fixes: the fused clause removed from the fast-gate requirement;
  the fast scenario names both shapes and the matched line; the probe runs in CI only, so `check` keeps its
  prerequisites and the design says so; What Changes and Modified Capabilities restated against the script/test/task
  design; a claims row records the `bash -e` capture premise.
- **Prior findings with their instances** — all five in `findings/round-2.md`.
- **Completed checks** — `openspec validate --all` 27 items; `spotlessApply`; ledger checks for rounds 1–2.
- **Known limitations** — nothing is implemented yet; the script, its test and the Gradle task are judged as plans.

## Round 4 — proposal screen, after round 3's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `1adfc2ce`.
- **Packs** — `none`.
- **Supplied artifacts** — the proposal with round 3's fixes: both remaining class statements now say content the CLI
  would drop (a non-recognised section or text outside any delta section), and both gate requirements carry a scenario;
  the design points at `AGENTS.md`'s prerequisites instead of re-listing them.
- **Prior findings with their instances** — both in `findings/round-3.md`.
- **Completed checks** — `openspec validate --all` 27 items; `spotlessApply`; ledger checks for rounds 1–3.
- **Known limitations** — nothing is implemented yet; the script, its test and the Gradle task are judged as plans.

## Round 5 — proposal screen, after the advisor's amendment

- **Unit, tree, branch, base** — unchanged; base `origin/main` `1adfc2ce`.
- **Packs** — `none`.
- **Supplied artifacts** — the proposal with the advisor's amendment: the gap is reported at `Fission-AI/OpenSpec`
  through the review gate (task 4.1), its reference and `retires when` condition recorded in `AGENTS.md`'s Continuous
  Integration section, which the `upstreamReferences` report scans (task 4.2; the corpus is
  `UpstreamReferenceScope.kt`'s); the design's decision 7 states the prose case as conservative policy, not proof of
  requirement loss; the tasks are renumbered around the new section.
- **Prior findings with their instances** — round 4 was clean; this round covers the amendment.
- **Completed checks** — `openspec validate --all` 27 items; `spotlessApply`.
- **Known limitations** — nothing is implemented yet; posting the issue is a later, reviewed step.

## Round 6 — proposal screen, after round 5's three findings and the sweep

- **Unit, tree, branch, base** — unchanged; base `origin/main` `1adfc2ce`.
- **Packs** — `none`.
- **Supplied artifacts** — the proposal swept rather than patched: the title and Why name the class (a non-recognised
  section or text outside any delta section); the Consequences are shape-neutral, keeping "unchanged requirement" for
  the section only; task 4.2 names the `Fission-AI/OpenSpec#<n>` form the report extracts; a claims row records the npm
  metadata resolving the owning repository; the unit's process-defect record gains `#1` for the patch-at-the-named-line
  pattern the reviewer named.
- **Prior findings with their instances** — round 5's three (`findings/round-5.md`).
- **Completed checks** — `openspec validate --all` 27 items; `spotlessApply`; ledger checks for rounds 1–5.
- **Known limitations** — nothing is implemented yet; posting the issue is a later, reviewed step.

## Round 7 — implementation screen

- **Unit, tree, branch, base** — unchanged; base `origin/main` `1adfc2ce`. The owner approved the proposal; the
  implementation applies its plan, plus the owner's instruction that the upstream issue goes past the advisor before
  posting.
- **Packs** — `none` (the routing decision names `.github/workflows/ci.yml` as infrastructure, but no subject pack owns
  a workflow's shell; the `gradle-build` pack owns the build task).
- **Supplied artifacts** — `scripts/verify-openspec-delta-guard.py` (the predicate and the three-fixture probe),
  `scripts/test-verify-openspec-delta-guard.py` (11 tests), `build.gradle.kts` (`testOpenSpecDeltaGuard` in `check`),
  `.github/workflows/ci.yml` (the capture, the two script calls, the retained config probe), `AGENTS.md` (the CI bullet
  with `Fission-AI/OpenSpec#2195` and its `retires when`), `docs/ideas.md` (the parked bullet removed, heading kept),
  and the delta.
- **Prior findings with their instances** — rounds 1–6; round 6 was clean.
- **Completed checks** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green (403 tasks, including
  `testOpenSpecDeltaGuard`); `openspec validate --all` 27 items; the script's own probe and transcript modes run green
  locally; the issue was posted after the advisor's review (`Fission-AI/OpenSpec#2195`).
- **Known limitations** — the deep review is `REQUIRED` (the workflow file) and runs next; the CI step's live behaviour
  is exercised by this change's own `build` run.

## Round 8 — implementation screen, with `gradle-build`

- **Unit, tree, branch, base** — unchanged; base `origin/main` `1adfc2ce`.
- **Packs** — **`gradle-build`**: the change registers a `check` task and edits `build.gradle.kts`, which that pack's
  subject row owns. Round 7 stopped on the missing classification rather than screening; the pack's task-registration
  rules were applied to the edit (the `testCommitHygiene` shape, inputs declared, `outputs.upToDateWhen { false }` for a
  verdict task, no script-level value captured into a task action).
- **Supplied artifacts** — as round 7, unchanged: the script, its test, the `build.gradle.kts` registration, the CI
  step, the `AGENTS.md` bullet with `Fission-AI/OpenSpec#2195` and its `retires when`, the ideas removal, and the delta.
- **Prior findings with their instances** — rounds 1–6 (round 7 produced no screen; the unit's process-defect record
  gains `#2` for the classification miss).
- **Completed checks** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green (403 tasks,
  `testOpenSpecDeltaGuard` included); `openspec validate --all` 27 items; the script's probe passed locally; routing
  `REQUIRED`.
- **Known limitations** — the deep review (the workflow file) runs after this screen.

## Round 9 — implementation screen, after round 8's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `1adfc2ce`.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — the implementation with round 8's fixes: the probe asserts the recognised requirement lands
  **and** that content the CLI should drop does not, a failing archive fails the probe for every shape (the prose
  exemption is gone), the probe takes an injectable runner, and seven new tests exercise the retirement note, a missing
  warning, applied content, a lost requirement, a failing archive and a validating malformed-only fixture (18 tests
  total); the script's five over-long lines are wrapped.
- **Prior findings with their instances** — both in `findings/round-8.md`.
- **Completed checks** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green (403 tasks); 18 tests pass; the
  probe passes against the real CLI; the 120-character scan over both scripts is clean; `openspec validate --all` 27.
- **Known limitations** — the deep review (the workflow file) runs after this screen.

## Round 10 — implementation screen, after round 9's finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `1adfc2ce`.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — the implementation with round 9's fix: the probe checks the archive's return code before
  reading the archived spec, and its stub writes that file only on success, so the failing-archive case models what the
  CLI actually does; 18 tests pass and the live probe is green.
- **Prior findings with their instances** — round 9's `test-control` (`findings/round-9.md`).
- **Completed checks** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green; the probe against the real CLI
  passes; `openspec validate --all` 27 items.
- **Known limitations** — the deep review (the workflow file) runs after this screen.

## Round 12 — implementation screen, after the advisor's correction

- **Unit, tree, branch, base** — unchanged; base `origin/main` `1adfc2ce`.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — the probe corrected: a fourth `valid-only` fixture distinguishes a strict CLI from a broken
  fixture environment; a non-zero validation exit counts as a retirement signal only when the CLI's own rejection
  diagnostic is present, and otherwise fails the probe; the retirement note is attributed to **both** loss-producing
  shapes being rejected; the test suite grew to 21 cases, including an unexpected failure without the diagnostic, a
  broken valid-only fixture, and a one-shape case that is not yet a retirement.
- **Prior findings with their instances** — round 11's `test-control` (`findings/round-11.md`).
- **Completed checks** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green; 21 tests pass; the live probe
  passes and prints the malformed-only rejection as expected; the 120-character scan is clean; `openspec validate --all`
  27 items.
- **Known limitations** — task 6.2 needs the pull request's own `build` run.

## Round 13 — implementation screen, after round 12's docs-residue sweep

- **Unit, tree, branch, base** — unchanged; base `origin/main` `1adfc2ce`.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — the four-fixture probe described at all five sites: the script's docstring and `--probe`
  help, the `AGENTS.md` bullet (no count, the valid control and the broken-environment case named), design decision 2
  and task 2.1 (both now stating the valid-control gate, the diagnostic-gated rejection and the both-shapes retirement).
- **Prior findings with their instances** — round 12's `docs-residue` (`findings/round-12.md`).
- **Completed checks** — 21 tests pass; the live probe passes; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`
  green; `grep "three fixtures"` over the scripts, `AGENTS.md` and the change dir returns none;
  `openspec validate --all` 27 items.
- **Known limitations** — task 6.2 needs the pull request's own `build` run.

## Round 14 — implementation screen, after round 13's formatting fix

- **Unit, tree, branch, base** — unchanged; base `origin/main` `1adfc2ce`.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — the implementation with round 13's fix: the docstring sentence and the `--probe` help string
  wrapped, so no line in either script exceeds 120 characters.
- **Prior findings with their instances** — round 13's `formatting` (`findings/round-13.md`).
- **Completed checks** — the per-line length scan over both scripts peaks at 119; 21 tests pass; the live probe passes;
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green; `openspec validate --all` 27 items.
- **Known limitations** — task 6.2 needs the pull request's own `build` run.
