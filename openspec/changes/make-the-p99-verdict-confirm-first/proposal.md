# Proposal

## Why

The drift check compares a run's client-side per-request p99 against the committed reference with one relative
tolerance, and a p99 beyond it fails the run and withholds the reference. That tolerance sits inside the figure's own
noise: at a matched operating point the poll request's p99 read 12, 24, 56 and 22 ms across the four recorded runs
while, over the three committed ones, its mean read 5/5/6 ms and its p95 7/8/11 ms. The check is the tighter tracker of
the tail, so it cannot drop the figure — the read-through-cache change's intended freshness/load trade moved that p99 24
→ 56 ms against a ~1 ms mean and a ~3 ms p95 move, and the owner then accepted it with a refresh, baking the excursion
into the reference for the next run to clear. The figures are in this change's claims table (`design.md`), each with the
source that derives it.

## What Changes

- **The verdict splits by the evidence each figure carries.** A mean or 95th-percentile figure beyond the tolerance is a
  **regression**: the run fails, naming it, as now. A p99 beyond the tolerance with no mean or 95th-percentile figure
  beyond it is an **unconfirmed excursion**: the reference is withheld so the next run compares against the same
  figures, the figures are named with the confirmation path, and the run does not fail — because one run cannot
  distinguish the two and a second run at the same operating point can.
- **The script derives its verdict from the reference file** rather than from the comparison task's status alone, so it
  never reports a write that was withheld.
- **The spec and the docs** carry the distinction.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/load-tests`: the "A baseline run reports its measurements" requirement, whose drift comparison and
  failure bracket this refines.

## Impact

- Code: `load-tests/src/main/java/showcase/loadtests/BaselineDrift.java` (the rules object, and its `BaselineDriftTests`
  cases), `load-tests/src/gatling/java/showcase/loadtests/BaselineStats.java` (the verdict and its message), and
  `scripts/load-test-baseline.sh` (the verdict message).
- Docs: `AGENTS.md`'s load-test paragraph and the README's load-test note, which state the current verdict; the two
  dated records whose present-tense claims it falsifies; the promoted idea entry in `docs/ideas.md` it implements; and a
  new parked entry for the gate hole its own work surfaced (`spotlessCheck` silently skipping a non-converging markdown
  file).
- Unchanged: the reference's format and figures, the profiles' derived thresholds, the `smoke` and performance profiles'
  assertions, and the tolerance's default.
