# Reject OpenSpec content the CLI would drop at archive

## Why

Content the CLI would drop at archive is silently ignored when the change also carries content it applies: a delta
section it does not recognise, or text outside any delta section. `openspec validate --all` exits 0 with a warning, and
`openspec archive` applies the valid part and drops the rest, so a requirement the author intended to change can ship
unchanged. The parked entry's premise ("validate exits 0, archive drops it") is confirmed live on the pinned CLI 1.16.0
for exactly that mixed case.

## What Changes

- The quality-gate workflow step fails when `openspec validate --all`'s output carries the warning that content would be
  dropped at archive, so content the CLI would drop (a section it does not recognise, or text outside any delta section)
  fails the `build` check instead of reaching `main` as a silent no-op.
- The predicate and its known-bad fixtures live in a committed script, `scripts/verify-openspec-delta-guard.py`, with a
  unit test wired into `check`: the predicate is exercised off the CLI, and a `--probe` runs the pinned CLI over the
  fixtures in CI, where the workflow installs it, so a CLI reword fails the probe rather than silently disarming the
  gate.
- The CI step keeps the validation output on a failure of validation itself, and names the matched line when the
  predicate fires.
- The gap is reported upstream with its reproduction, and the reference and its `retires when` condition are recorded
  where the upstream-reference report scans, so the guard is removed once the CLI itself rejects both loss-producing
  shapes.
- The parked `docs/ideas.md` entry is removed by the change that implements it.

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/merge-governance` — the fast-gate and full-gate requirements gain the verification that validation
  output carries no content the CLI would drop at archive, each with a scenario naming both shapes a delta can carry it
  in.
