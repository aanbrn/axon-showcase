# Tasks

## 1. ADR-0017 — the gate's qualifier and the site that cited only the 1.0 line

- [x] 1.1 In `docs/adr/0017-grpc-query-transport.md`'s `Revisit when:`, state the condition as a **maintained**
      first-party Boot-3 line, label the clause as an external coordinate, and record why the `0.x` line does not meet
      it (its last Boot-3 release and its end-of-life status), with the owner's 2026-10-19 resolution. Verify by reading
      the line back. **Done** — the gate now states the **maintained** qualifier, labels the clause an external
      coordinate, and records the `0.x` line's exclusion and the owner's 2026-10-19 resolution.
- [x] 1.2 In the same file's `_Alternatives considered:_` sentence for Spring gRPC, add the `0.x` disposition as the
      alternative's rejection reason and point at the gate for the qualifier's full evidence, so the sentence states why
      the alternative was rejected while the gate owns the evidence. Verify the sentence carries both. **Done** — the
      sentence carries the `0.x` rejection reason and points at the gate.
- [x] 1.3 Read the whole ADR back and confirm no stale claim survives — grep it for the tokens `first-party` and
      `targeting` and confirm every occurrence carries the corrected qualifier, since a multi-word phrase is split by
      the file's line wrapping and checks nothing. **Done** — the token grep returns one occurrence each of
      `first-party` and `targeting`, both carrying the corrected qualifier.

## 2. Pre-write derivation pass

- [x] 2.1 The pre-write derivation pass ran over the planning artifacts: each fact the artifacts state about their
      evidence resolved into a row of the claims table in `design.md` or a dismissal recorded beside it. **Done** — the
      table is in `design.md` under `## Claims`, seeded with `python3 scripts/claim-scan.py`, whose candidates are each
      resolved into a row or recorded as a by-kind dismissal beside the table.

## 3. Verification and the review gate

- [x] 3.1 `./gradlew spotlessApply` then `./gradlew spotlessCheck` and `openspec validate --all`; confirm clean.
      **Done** — `spotlessCheck` clean after `spotlessApply`; `openspec validate --all` 27/27 and `--changes` 1/1 with
      the `skip_specs` notice.
- [x] 3.2 The 120-character check over the change's whole changed set, derived with
      `git status --porcelain --untracked-files=all`, using the `perl -CSD -lne` recipe. **Done** — the recipe reports
      nothing over 120 in the ADR, `proposal.md`, `tasks.md`, or `.openspec.yaml`; `design.md`'s over-120 lines are its
      formatter-owned table rows.
- [x] 3.3 The review gate ran: `review-quick` over the proposal and the implementation, repeating until clean, with each
      round's row in `docs/review-rounds.tsv` carrying the identifier the invocation returned. **Done** — four rounds in
      one session (`ses_eacb8c106ffehXZtQF7FyMh4IS`), each row in `docs/review-rounds.tsv` carrying the identifier the
      invocation returned: round 1 `wording,over-claim,formatting` (3), round 2 `wording,over-claim` (2 — both sibling
      sites of round 1's, the remedy being the sweep rather than the named line), round 3 `claim-unreproducible` (1),
      round 4 clean. The loop converged 3 → 2 → 1 → 0.
- [x] 3.4 The deep-review routing decision is recorded on this task, derived with
      `python3 scripts/review-routing.py --change correct-adr-0017-spring-grpc-gate`. **Done** —
      `python3 scripts/review-routing.py --change correct-adr-0017-spring-grpc-gate` reports `NOT_NEEDED` with no
      signal: the changed paths are the ADR and the unit's own records, and no declaration or requirement-block signal
      matched. No deep-review session is created.

## 4. Capture

- [x] 4.1 The `lesson-capture` subagent ran over the diff and the review findings, and its durable proposals were
      applied — or the skip condition held in full and its reason is recorded — with the applied net `AGENTS.md` delta
      on this task. **Done** — it ran (session `ses_eaca35195ffefLQc2GbmMwkfSf`): **1 durable proposal applied** (a
      deferral gate states the qualifier that narrows its condition in the gate's own line, merged into the existing
      ADR-gate clause, whose `captured:` marker now carries this unit), **applied net `AGENTS.md` delta +1 line** (the
      `@@ -1415,5 +1415,6 @@` hunk), 0 retirements, 0 authoring-system recommendations. Its `already covered` verdicts —
      every one a compliance miss of an existing rule: the sweep discipline, the clause-level claims-row rule the
      previous unit added, the universal-claim re-check, and the inline-code-span line-break rule.

## 5. Manual review and archive

- [x] 5.1 The owner's manual review pass was requested after the loop was clean and the capture ran. **Done** —
      requested after the loop was clean and the capture ran; the owner approved.
- [x] 5.2 The change was archived with `openspec archive correct-adr-0017-spring-grpc-gate --yes`, the change-dir move
      staged with any `openspec/specs` sync, and the archive commit ridden on this unit's PR.
 **Done** — archived as `2026-10-19-correct-adr-0017-spring-grpc-gate` in this commit, the change-dir move staged with
      the `openspec/specs` sync (empty: `skip_specs: true` held, since no capability spec names the ADR or its
      coordinate).
## Workflow follow-up

- Nothing committed, pushed, or archived before the owner approves the implementation.
