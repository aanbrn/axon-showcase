# add-claims-execution-verify

## Why

The claims table's rows are prose: a row's command and its expected result are written by hand, and nothing executes
them. `claim-unreproducible` recurs across units — 17 rounds in the four units the parked idea names, three more in the
two units of 2026-10-30 — and each new cause produced another prose rule while the next row repeated the failure. The
cause-specific rules exist; the missing piece is a mechanism that runs a row and reports a mismatch — if a bounded pilot
finds enough rows it can run.

## What Changes

- `scripts/claim-scan.py` — an opt-in `--verify <table>` mode, conditional on the pilot's verdict: parse a claims table,
  execute each row's command at the revision it names, compare the output with the row's expected literal, and exit
  non-zero on a mismatch. The default scan stays advisory; `check` runs no execution of it.
- `scripts/test-claim-scan.py` — the mode's cases: a matching row, a wrong count, a stale line, a prose-result row
  (advisory), an unsafe command, a write hidden inside an allowed first word, a multi-span command, a multi-literal
  result, and a timeout.
- `docs/decisions/2026-10-31-claims-execution-pilot.md` — the pilot's report: the tables run, the machine-checkable
  subset, the contracts, the false-fail/false-pass observations, and the verdict.
- The pilot's tables — the four units the parked idea names: the three archived claims tables
  (`2026-10-27-extend-owner-reply-language-scope`, `2026-10-27-hand-over-unfinished-work`,
  `2026-10-27-separate-lesson-capture-from-implementation`) and `record-the-settle-step-defect`'s table from #711.

## New Capabilities

None.

## Modified Capabilities

None. No capability spec mentions `claim-scan.py` (`grep -rn 'claim-scan' openspec/specs/` returns nothing); the
`agent-skills` claims-table requirement states the outcome, which the mode verifies without changing it. `skip_specs`
records that position.

## Impact

- Tooling: `scripts/claim-scan.py`, `scripts/test-claim-scan.py`, and the new decision record.
- No API, dependency, build, test, deployment, or runtime change; the mode is opt-in and absent from `check`.
