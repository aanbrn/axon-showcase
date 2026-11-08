# Tasks

The unit record — its `Workspace:`, state, and pause checkpoint — is `docs/units/gate-review-handover-section/unit.md`.

## 1. Implementation

- [x] 1.1 `scripts/commit-hygiene.py`: add the `--handover <unit>:<round>` mode — parse the argument, read
      `docs/units/<unit>/review-handover.md` from the working tree, and fail on a missing or unreadable file, a
      malformed argument, or no second-level heading beginning `Round` and the exact round token (a descriptive suffix
      allowed). Add the mode to the module docstring's mode inventory. Verification: the mode passes on a fixture
      heading and exits non-zero on each rejected shape.
- [x] 1.2 `scripts/test-commit-hygiene.py`: a known-bad and a known-good case per predicate — a present heading, a
      descriptive suffix, an exact round-token match (`Round 30` and `Round 3x` do not match round 3), a backticked
      round token, a different round, a missing file, an unreadable file (invalid UTF-8), a third-level heading, and a
      malformed argument (an empty unit, a path separator, `.`, `..`, whitespace, an extra colon, a non-decimal, zero,
      and a leading-zero round) — plus the mode's entry in `MODE_TEST_COVERAGE` and the three sites the mode joins: the
      parser's mutually exclusive group, `MODE_FLAGS`, and `MODE_TEST_COVERAGE`. Adapt the mode-iteration control so a
      parameterized mode is given an operand rather than reaching the parser's missing-argument error. Verification:
      `python3 scripts/test-commit-hygiene.py` passes with each new case reaching its assertion.
- [x] 1.3 `.opencode/skills/review-gate/SKILL.md` §2: invoke the mode after the hand-over section is written and
      immediately before each quick-review delegation, state that its failure stops the call, and state the accepted
      heading form `## Round <N>` so the writer and the predicate share one convention. Verification: the preflight
      names the invocation, its stop rule, and the heading form.
- [x] 1.4 `README.md`: the review paragraph names the pre-delegation check. Verification: the paragraph names it and no
      sentence claims the check proves delivery.
- [x] 1.5 `docs/ideas.md`: remove the implemented entry. Verification: `grep` finds no remaining entry for the guard.

## 2. Verification

- [x] 2.1 `openspec validate --all` passes with the delta at the mirrored capability path.
- [x] 2.2 `python3 scripts/test-commit-hygiene.py` passes.
- [x] 2.3 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` passes.
- [x] 2.4 `./gradlew spotlessApply` then `spotlessCheck` pass after the last edit.
- [x] 2.5 Run the pre-write derivation pass and record the claims table in `design.md`; re-derive it after the last
      edit.

## 3. Review gate

- [x] 3.1 Run the proposal quick review and fix every finding until it reports no new observations.
- [x] 3.2 Run the implementation quick review and fix every finding until it reports no new observations.
- [x] 3.3 Derive the routing decision with `python3 scripts/review-routing.py`, record it, and run the deep review the
      owner requested through the normal procedure. - **Done** —
      `review-routing.py --unit gate-review-handover-section --change gate-review-handover-section` returned `REQUIRED`
      on `[requirement-block-added]` for the delta. Deep-review pass 1 (session `ses_e474f80fcffezjtWW97dpdXLyW`,
      recorded as `deep-1`) reported `nothing remains`, so no fix-verification pass was owed and no finding carries a
      status.
- [ ] 3.4 Ask the owner for the manual review pass and end the turn there.
- [x] 3.5 Write `docs/units/gate-review-handover-section/process-defects.md`, or state that none was observed.

## 4. Archive

- [ ] 4.1 Refresh the `showcase/quality/commit-hygiene` Purpose in the archive commit — it enumerates the checks and now
      gains this one — and tick this task in the archived `tasks.md`.
- [ ] 4.2 Run `openspec archive gate-review-handover-section --yes` and stage both the change-dir move and the
      `openspec/specs/**` sync.
