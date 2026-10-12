# Tasks

## 1. AGENTS.md

- [x] 1.1 Add to the auto-review bullet in `AGENTS.md` a pre-write derivation pass — before the first round, derive the
      four claim classes named there, from their sources — and the clause that every re-run passes the prior round's
      class list. Verify by reading the bullet back.

## 2. openspec/config.yaml

- [x] 2.1 Add the per-artifact derivation rules to `openspec/config.yaml`'s `rules` block, following the design's
      class-to-artifact mapping (proposal: the external-artifact and restated-count rules; design: the restated-model
      and mechanism rules; `tasks` already carries the task-check rule, so no addition there). Verify each with
      `openspec instructions <proposal|design|tasks> --change cut-review-loop-rounds --json` (each rule appears in
      `rules`), since a malformed item drops its whole artifact's rule set independently.
- [x] 2.2 Park the reviewer-contract follow-up (intra-round class clustering in `review-quick`, owing the `agent-skills`
      delta) in `docs/ideas.md`, so it survives this change's archive. Verify by reading the entry back.

## 3. Verification

- [x] 3.1 Run `./gradlew spotlessApply` then `./gradlew spotlessCheck`, and `openspec validate --all`; confirm the
      touched files are clean. Run the manual 120-character check over the edited `openspec/config.yaml` (YAML is not
      formatter-covered).
- [x] 3.2 Run a `review-quick` pass over the proposal and implementation, repeating until clean.

## 4. Capture

- [x] 4.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. Applied: one widening of the "Work a change surfaces is parked durably" bullet
      (park a companion change the unit names but will not deliver, not only an external state change) — net +1 line. No
      retirements were proposed.
- [x] 4.2 Ask the owner for the manual review pass, after 3.2 is clean and 4.1 is done.

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied and the owner approves.
- Verify the archived result.
