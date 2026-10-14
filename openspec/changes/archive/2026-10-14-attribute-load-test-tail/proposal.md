# Proposal

## Why

The committed baseline reference (recorded 2026-10-08) carries write-lifecycle p99s its own Notes explain unevenly:
`PollShowcase` 24 → 56 ms by the cache eviction's intended freshness/load trade, and `StartShowcase` 18 → 45 as "tail
noise … within the aggregate p99 range of the prior runs". The per-request series — which no record carries, git does —
rises 10 → 18 → 45 over three independent runs, so that attribution compares a per-request figure against whole-plateau
aggregates, and no measurement has distinguished a pipeline degradation from the measurement environment. The list read
does not share the trend (15 → 24 → 14). The figures are in this change's claims table (`design.md`).

## What Changes

- **A layered measurement over one baseline plateau** at the reference's operating point: the gateway's server-side
  response time per write route, the command-service's command bus, and the host JVM's GC and CPU as the process-wide
  control — because the client-side figure alone cannot place a tail.
- **The recorded explanation**: a `docs/load-tests/` record stating the attribution, its evidence, and its limits, and
  correcting the "tail noise" reading against the per-request series.
- **The durable consequence**, decided by the measurement and parked in `docs/ideas.md` either way (this change owes no
  delta): a change carrying a clause in the drift requirement's own sensitivity if the environment dominates the figure,
  or a park of the pipeline fix if the degradation is real.
- **`docs/ideas.md`** — the entry this change answers is removed; issue #430 reports the finding.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. An attribution changes no spec'd behavior, so this change owes no delta — the analogue is
`2026-10-05-refresh-projection-writes`, another measurement unit, which declared `skip_specs`. If the measurement's
consequence turns out to require a check change, that consequence is a follow-up change rather than a late delta here.

## Impact

- Artifacts: a `docs/load-tests/` record, this change's own claims table, the committed reference (copied aside and
  restored, since the run rewrites it), and `docs/ideas.md`.
- No runtime or build change: the measurement reads the series the deployment already exports and retains.
- The run targets the cluster the repository's own script already points at (`scripts/load-test-baseline.sh`,
  rate-matched at `CALIBRATE_MAX_RATE=200`), and its figures are host state, so the record states the environment it
  measured and the stack it measured it on.
