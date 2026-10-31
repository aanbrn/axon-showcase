# Tasks — add-claims-execution-verify

## 1. Pilot survey and probe

- [x] 1.1 Read the four units' tables — the three archived claims tables and #711's body, saved to a scratch file — and
      classify every row by D2–D4: literal-output, prose, session-store/external, unsafe.
- [x] 1.2 Run a scratch probe, not the mode, over a sample of the machine-checkable subset, and record the false-fail
      and false-pass observations.
- [x] 1.3 Decide the verdict (D9) — proceed, narrow, or stop — and record it before the mode is implemented.

## 2. The `--verify` mode (conditional on the verdict)

- [x] 2.1 IF the verdict is proceed or narrow, THEN implement `--verify <table>` with D2's raw-cell/one-command rule,
      D3's explicit literal-only eligibility, D4's local predicate, D5's evidence context, D6's timeout, and D7's
      statuses and coverage report.
- [x] 2.2 Keep the default scan unchanged; the test suite gains the mode's cases.
- [x] 2.3 Document the mode in the script's docstring and `--help`.

## 3. Tests

- [x] 3.1 Add the mode's cases: a correct explicit assertion (verified), a wrong count and a wrong emitted line number
      (mismatch), a prose Result and a two-column row (advisory), an ambiguous `\|` cell (advisory), an unsafe form
      (`sed -i`, `git push`, `git --output`), a working-tree measurement (`git diff HEAD`, advisory), a zero literal
      (verified from stdout), an execution failure, and a timeout.
- [x] 3.2 Run `python3 scripts/test-claim-scan.py` and the new cases.

## 4. Pilot run and report

- [x] 4.1 Run `--verify` over the four tables; the historical prose rows are advisory, and the report records the
      checked and skipped counts.
- [x] 4.2 Write `docs/decisions/2026-10-31-claims-execution-pilot.md` with the compatibility counts, the contracts
      (D2–D7), the fixture results (a correct assertion succeeds; a wrong count and a wrong emitted line number fail),
      and the verdict.

## 5. Documentation and the idea

- [x] 5.1 IF the mode lands, THEN add a pointer to it in `.opencode/skills/review-gate/SKILL.md` §1, stating that it is
      opt-in and that the default pass is unchanged.
- [x] 5.2 Remove the parked idea from `docs/ideas.md` if the mode lands; otherwise update it with the verdict.
- [x] 5.3 Run the docs-refresh sweep over `AGENTS.md`, `README.md`, and `docs/adr/`.

## 6. Review and records

- [x] 6.1 Record the pre-write derivation pass and its claims table in `design.md`.
- [x] 6.2 Run the quick-review loop and record each round's row in `docs/review-rounds.tsv`.
- [x] 6.3 Record the deep-review routing outcome on the change's review task. Outcome: `RECOMMENDED` (`[repeated-class]`
      11 rows repeat a prior class); the owner's decision was to run (after the advisor's recommendation), and the deep
      pass found B1/B2/S1/S2/S3 and N1, all fixed and verified.
- [x] 6.4 Write the unit's process-defect record, or state that none was observed.

## 7. Verification

- [x] 7.1 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, `openspec validate --all`, and
      `spotlessApply`/`spotlessCheck`.
