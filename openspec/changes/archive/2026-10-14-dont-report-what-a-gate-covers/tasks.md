# Tasks

## 0. The pre-write derivation pass (this change carries its own record and claims table)

- [x] 0.1 Run the pre-write derivation pass and record its output as the claims table in `design.md`.

## 1. Route a defect the applying command rewrites

- [x] 1.1 In `.opencode/agent/review-quick.md`'s instructions list, the convention-violation bullet routes a defect the
      applying command rewrites to `spotlessApply`/`npmFormat` rather than reporting it.

## 2. Capture the behavior in the spec

- [x] 2.1 The delta for `showcase/quality/agent-skills`: a `MODIFIED` block carrying the requirement's description and
      its scenarios, with the route clause added to the quick-review scenario.

## 3. Docs the change owns

- [x] 3.1 Read `README.md`, `AGENTS.md`'s reviewer references, and the capability's `## Purpose`; record whether any
      needs an edit.

## 4. Verification

- [x] 4.1 Re-run the claims-table rows with their sources and confirm each reproduces.
- [x] 4.2 Run `./gradlew spotlessApply` then `spotlessCheck`, `openspec validate --all`, and the manual 120-character
      check over the changed non-formatter files.
- [x] 4.3 Run a `review-quick` pass over the implementation, passing the prior round's instances.
- [x] 4.4 Run the per-unit `lesson-capture` and record the applied net `AGENTS.md` delta (+3 lines: one added
      claims-table root-cause shape in the review-loop bullet; no rule retired).
- [x] 4.5 Request the user's manual review pass.
