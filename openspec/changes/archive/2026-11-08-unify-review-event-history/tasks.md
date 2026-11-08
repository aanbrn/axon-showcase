# Tasks

The unit's record is `docs/units/unify-review-event-history/unit.md`.

## 1. Implementation

- [x] 1.1 `scripts/commit-hygiene.py`: add the register's `event` field and the boundary width (thirteen before
      `2026-11-09`, fifteen after).
- [x] 1.2 `scripts/commit-hygiene.py`: admit `review-quick` for a post-retirement unit — scope the unit, opinion `-`,
      the round it reports, the invocation pass, and the `round-<N>` link resolved by the `--handover` predicate — and
      reject it for a pre-retirement unit.
- [x] 1.3 `scripts/commit-hygiene.py`: record a stalled round's invocations — the stalled attempt's row at its deadline
      and the replacement's after its own report, each sharing the round — and validate the sequence.
- [x] 1.4 `scripts/commit-hygiene.py`: state the replacement reason set here, and make `--session-continuity` judge
      every unit's width while judging the session fields only for the nine-field units.
- [x] 1.5 `scripts/commit-hygiene.py`: cross-check a post-retirement unit's rounds and its quick `report` rows — exactly
      one per recorded round — reading both records as committed or staged.
- [x] 1.6 `scripts/commit-hygiene.py`: admit `deep-review.md` in `UNIT_LAYOUT_FILES`; the layout test's admitted-kind
      fixture gains it. Verification: a unit holding `deep-review.md` passes `--unit-layout`, and an unadmitted file
      still fails.
- [x] 1.7 `scripts/test-commit-hygiene.py`: a known-bad and a known-good case per rule 1.1–1.5 adds — the event gap and
      duplicate; a fourteen-field row on either side of the boundary, a thirteen-field row after it, and a fifteen-field
      row before it; a quick row on a post-retirement unit and on a nine-field unit; a historical unit first rounded
      before the retirement date whose six-field rows carry no register — the completeness rule must not reach it; the
      quick link's `Round <N>` resolution; a quick row without a round; a non-quick row carrying a round; a round with
      two stalled attempts and the reporting row; the round/report correspondence's missing, duplicate, and unmatched
      cases, and a staged ledger round with no staged register report row; a six-field row in a nine-field unit and the
      reverse; and the session-field skip for a post-retirement unit. Verification:
      `python3 scripts/test-commit-hygiene.py` passes with each new case reaching its assertion.
- [x] 1.8 `scripts/test-commit-hygiene.py`: a CLI-level control on a disposable post-boundary fixture unit — a six-field
      ledger and a quick register with `created`, `continued`, and a stalled-then-replaced round — through the real
      modes. Verification: each mode's outcome is recorded.
- [x] 1.9 `scripts/review-rounds.py`: read both widths and join the register's quick rows by `(unit, round)` for the
      echoed session, selecting the round's resolved `report` row rather than a stalled one, and never requiring it.
      Verification: `python3 scripts/test-review-rounds.py` covers a six-field row with and without a register, and a
      stalled round whose rows resolve to the replacement's session.
- [x] 1.10 `.opencode/skills/review-gate/SKILL.md`: for a post-retirement unit the quick preflight resolves the session
      and writes the hand-over section, the register row is appended when the outcome is known, and the round's outcome
      goes to the ledger; a nine-field unit keeps the ledger's session fields. Verification: the preflight states both
      paths and the gates' unchanged inputs.
- [x] 1.11 `.opencode/agent/review-quick.md`: the caller appends the session fields to the register's quick row for a
      post-retirement unit, and to the ledger row for a nine-field unit. Verification: the record contract names both.
- [x] 1.12 `docs/contracts/process-bindings.md`: the `agent-session-record` role holds the unit's event sequence.
- [x] 1.13 `docs/contracts/process-bindings.md`: the `review-ledger` role keeps the quick loop's outcome.
- [x] 1.14 `docs/contracts/process-bindings.md`: the consumer trace names `agent-session-record` for
      `.opencode/agent/review-quick.md`.
- [x] 1.15 `docs/contracts/process-bindings.md`: the non-adopter sentence names `review-rounds.py` reading the register.
      Verification: all four are stated.
- [x] 1.16 `README.md`: the review paragraph and the Code review bullet name the one sequence, the two records' split,
      and the unit's deep-review record. Verification: both state it and claim no gate change.
- [x] 1.17 `docs/ideas.md`: remove the implemented entry. Verification: `grep` finds no remaining entry for the unified
      history.
- [x] 1.18 `.opencode/commands/review-thorough.md` and `.opencode/skills/review-gate/SKILL.md`: record the routing
      decision and each pass's result on the unit's `deep-review.md` — the decision and its signals, the pass's kind,
      revision, outcome, and limits, each finding's status and evidence, and an `ACCEPTED` finding's provenance — while
      the change's task or the pull-request checklist carries the obligation and a pointer. Verification: both documents
      name the record and no longer make the change's task the statuses' home.
- [x] 1.19 `docs/contracts/process-bindings.md`: add the `deep-review-record` role (`docs/units/<unit>/deep-review.md`,
      owned by the `agent-skills` deep-review requirement) and the consumer-trace rows for the review gate and
      `/review-thorough`.

## 2. Verification

- [x] 2.1 `openspec validate --all` passes with both deltas at their mirrored capability paths.
- [x] 2.2 `python3 scripts/test-commit-hygiene.py` and the digest's suite pass.
- [x] 2.3 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` passes.
- [x] 2.4 `./gradlew spotlessApply` then `spotlessCheck` pass after the last edit.
- [x] 2.5 Run the pre-write derivation pass and re-derive the claims table in `design.md` after the last edit.

## 3. Review gate

- [x] 3.1 Run the proposal quick review and fix every finding until it reports no new observations. **Done** — the
      proposal quick review ran to a clean round before the manual pass.
- [x] 3.2 Run the implementation quick review and fix every finding until it reports no new observations.
- [x] 3.3 Derive the routing decision with `python3 scripts/review-routing.py`, record it and the pass's result in
      `docs/units/unify-review-event-history/deep-review.md`, and run the pass it selects.
- [x] 3.4 Ask the owner for the manual review pass and end the turn there.
- [x] 3.5 Write `docs/units/unify-review-event-history/process-defects.md`, or state that none was observed.

## 4. Archive

- [x] 4.1 Refresh the `showcase/quality/commit-hygiene` Purpose in the archive commit — it enumerates the checks and the
      records — and tick this task in the archived `tasks.md`.
- [x] 4.2 Run `openspec archive unify-review-event-history --yes` and stage both the change-dir move and the
      `openspec/specs/**` sync.
