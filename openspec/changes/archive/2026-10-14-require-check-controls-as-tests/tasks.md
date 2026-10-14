# Tasks

## 0. The pre-write derivation pass (this change carries its own record and claims table)

- [x] 0.1 Ran the pre-write derivation pass and recorded its output as the claims table in `design.md` — each fact this
      change states about its evidence, with the source or the deriving command; every row re-runs to its figure.

## 1. The control is a test, not a task

- [x] 1.1 In `openspec/config.yaml`, the check-control rule now requires a **committed test** (the known-bad input
      fails, the known-good passes) instead of a task, preserving the rule's second obligation — a task to read the
      output of any scratch command used as evidence. The config parses (a clean `openspec new change`).
- [x] 1.2 In `AGENTS.md`, the check-control gotcha ("A check is evidence only once it has been shown to fail") now says
      the control is a committed test and names the `*RulesTests` shape.
- [x] 1.3 Confirmed every rules-object-based gate's test carries a known-bad case: all nine
      `build-logic/src/main/kotlin/*Rules.kt` have a matching `*RulesTests.kt`, and each asserts a known-bad input (e.g.
      `DashboardJsonRulesTests` "Every malformed dashboard is reported", `ModuleDependencyRulesTests` "Depending on a
      service application is forbidden"). The six Exec-based commit-hygiene gates carry theirs in
      `scripts/test-commit-hygiene.py`. No fixture was missing.

## 2. Capture the behavior in the spec

- [x] 2.1 The delta for `showcase/quality/code-quality`: an `ADDED` requirement, "A check's control is a committed
      test", with a scenario per gate shape (rules-object and inline) plus the general case.

## 3. Docs the change owns

- [x] 3.1 Read `README.md` and the capability's `## Purpose`: no edit owed — the README does not enumerate the build's
      check conventions, and the Purpose already covers "the project's code style, static quality conventions, and
      structural shape through the build".

## 4. Verification

- [x] 4.1 Re-ran every claims-table row with its source: the config rule at `:74`; `verify-documented-pins` 13 rounds,
      raising `test-control` in 6; the nine `*Rules.kt` against nine `*RulesTests.kt`; the `build-logic` rule at
      `AGENTS.md:965`.
- [x] 4.2 Ran `./gradlew spotlessApply` then `spotlessCheck`, `openspec validate --all` (27 passed, 0 failed), and the
      manual 120-character check over the changed non-formatter files (`openspec/config.yaml` clean); the config's clean
      create emits no warning.
- [x] 4.3 Ran `:build-logic:test` (BUILD SUCCESSFUL); no fixture was added, so no pre-fix proof was owed.
- [ ] 4.4 Run the implementation `review-quick` pass.
- [x] 4.5 Ran the per-unit `lesson-capture`; applied its one addition (a claims-table row about an artifact the change
      edits anchors to its pre-change state), the change's own pipe escape, and the config `design` rule's stability
      clause. Applied net `AGENTS.md` delta: +2 lines (+ the unit's own 2).
- [ ] 4.6 Request the user's manual review pass.
