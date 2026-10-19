# Tasks — review capture proposals against their evidence

## 1. The reviewer contract

- [x] 1.1 `.opencode/agent/review-quick.md` — replace the capture check with one that compares the proposal with the
      supplied existing obligation and screens semantic duplication, evidence and scope, recurrence versus cause,
      preservation, ownership, and growth; keep the durability check and point at the `lesson-capture` contract.
      **Done** — the check is one lead applying the contract's gate, plus three points: the screen (durability and the
      subjects), the finding shape, and the limitation.
- [x] 1.2 `.opencode/agent/review-quick.md` — the hand-over names the capture materials: the existing rules (or the
      statement that none covers it), the round rows and change record, and the definition where its gate is checked.
      **Done** — the opening paragraph carries the clause.

## 2. The caller and the spec delta

- [x] 2.1 `AGENTS.md` — the review gate's **Run** step supplies those materials for a capture. **Done** — one clause
      added; no gate text copied.
- [x] 2.2 `openspec/changes/sharpen-capture-review/specs/showcase/quality/agent-skills/spec.md` — copy the current
      "Per-change quality-gate and analysis subagents are available" requirement whole and add the corresponding AND
      clauses, keeping every scenario in order. **Done** — the block is the main spec's requirement; `diff` against
      `sed -n '140,554p'` shows only the intended hunks.

## 3. Pre-write derivation pass

- [x] 3.1 The pre-write derivation pass ran: `python3 scripts/claim-scan.py` over the authored set, each candidate a row
      of the claims table in `design.md` or a by-kind dismissal beside it. **Done** — re-run after the final edit to the
      change's own records; every candidate resolves to a table row or one of the by-kind dismissals.

## 4. Validation against the motivating example

- [x] 4.1 The pointer-source clause is assessed against the source-derivation and pointer-target rules: same action,
      same condition, no decision difference. **Done** — recorded in `design.md`'s validation table.
- [x] 4.2 The recurring-class candidate is assessed against the supplied round rows: recurrence evidenced, shared cause
      not established, so the blanket rewrite is withheld and the conditioned rule is assessed on its own evidence.
      **Done** — recorded in `design.md`'s validation table.
- [x] 4.3 The limitation reporting is exercised: supplied materials that cannot establish a comparison yield a stated
      limitation, not a confirmed finding. **Done** — the validation table's fourth row.
- [x] 4.4 A distinct, evidence-backed preventive action is confirmed acceptable. **Done** — the validation table's third
      row.

## 5. Verification and the review gate

- [x] 5.1 `./gradlew spotlessApply`, then `spotlessCheck` and `openspec validate --all`; confirm clean. **Done** — both
      formatter tasks exit 0 and `openspec validate --all` reports 27/27 after the final edit.
- [x] 5.2 The 120-character check over the change's whole changed set, using the `perl -CSD -lne` recipe. **Done** —
      only the pre-existing long `AGENTS.md` Gradle command and the formatter-exempt table rows exceed 120.
- [x] 5.3 The review gate ran: `review-quick` until clean, each round's row in `docs/review-rounds.tsv` with the
      identifier the invocation returned. **Done** — the loop's rounds, classes, and sessions are the unit's rows in
      `docs/review-rounds.tsv`; every finding a recorded round raised was applied before the next, and the note states
      the ledger's outcome rather than a count.
- [x] 5.4 The deep-review routing decision is recorded on this task, derived with
      `python3 scripts/review-routing.py --change sharpen-capture-review`. **Done** — reports `NOT_NEEDED` with no
      matched signals; re-derived after the smoke-run's fix, with the same outcome.
- [x] 5.5 The edited definitions were smoke-checked after an OpenCode reload, and the seeded capture cases produced the
      expected screen (a restatement flagged, a cause-inferred rewrite flagged, a conditioned preventive action not
      flagged). **Done** — after the reload the fixed body flagged the restatement as `single-sourcing`, flagged the
      cause-inferred rewrite as `premise`, and left the unit's own valid `AGENTS.md` addition unflagged. The first
      suppression seed was a verbatim existing rule, which the screen correctly flagged as a restatement; the valid seed
      replaced it.

## 6. Capture

- [x] 6.1 The `lesson-capture` subagent ran, or the skip condition held in full with its reason recorded, with the
      applied net `AGENTS.md` delta on this task. **Done** — the subagent ran (the authoring surface and the round
      classes fail the skip conjunction) and reports `nothing durable (net 0)`: every candidate is already covered by an
      existing rule, and the unit's own edges carry the new obligations. Re-checked after the post-reload smoke-run with
      the same verdict; the one new observation (a verbatim rule is a must-report suppression seed, not a valid one) is
      below the gate and parked in `docs/ideas.md`. Applied net `AGENTS.md` delta: **0 lines**.

## 7. Manual review

- [x] 7.1 The owner's manual review pass was requested; the work stays uncommitted until the owner answers. **Done** —
      requested with the unit's report; nothing is committed, pushed, or archived.

## Workflow follow-up

- Nothing committed, pushed, or archived before the owner approves the implementation.
