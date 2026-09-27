# Proposal: Anchor the load tests to a recorded baseline

## Why

The below-knee profiles (`average`, `stress`, `soak`) assert fixed absolute thresholds — mean ≤ 100 ms, p95 ≤ 500 ms,
p99 ≤ 1000 ms — that say nothing about the host, so a regression that triples the committed plateau's p95 (8 ms → 24 ms)
still passes the 500 ms threshold, and a slow host's genuinely-fine run is measured against numbers it never met.
Meanwhile a `baseline` run measures the plateau and then discards the measurement: the committed
`docs/load-tests/<date>.md` records are prose written around hand-transcribed console numbers, and nothing the profiles
assert against is machine-readable.

## What Changes

- A `baseline` run records its plateau's response times per read and write request into a committed reference
  (`load-tests/src/gatling/resources/baseline.properties`), written by a new `baselineStats` task that reads the run's
  Gatling log the way `kneeFinder` already does, together with the derivation policy (the factor and the floors).
- The below-knee profiles derive their thresholds from that reference — `max(floor, factor × baseline)` per request and
  percentile — falling back to the absolute thresholds for a reference that is unusable: missing, not covering a
  request, or recording a target other than the one configured.
- The same run writes a ready-to-commit dated record (`docs/load-tests/<date>.md`) carrying the measured numbers, so the
  record stops being hand-transcribed; the operator annotates it and commits it.
- The write stream's poll conditions stop being Gatling EL strings: a sustained baseline run crashed them, KO-ing polls
  and tripping the failure assertions, so they compare the session attribute in Java. The fix conforms to the existing
  `Polling retries until the expected state` scenario, so it takes no delta.
- A non-local target gets its own artifacts: the wrapper's reference path and record are named for the environment's
  slug (the default target's record keeps the plain date), `-PbaselineFile` points a run at an environment's own
  reference, and the report states the target it measured rather than asserting the local cluster.
- The load-testing module's build specifics move into it: the property whitelist and the `kneeFinder` registration leave
  `load-testing-conventions` for `load-tests/build.gradle.kts`, because a convention must not name a module's own
  interface, and the convention is left applying `java-conventions` and the Gatling plugin.
- Deltas: `Pass assertions`, `Configurable target, profile, rate, ratio, and duration`, and
  `A baseline run reports its measurements`. In `docs/ideas.md`, the assertions-from-a-baseline idea is removed
  (implemented here), and the observational-run idea is rewritten — its record half lands here, and its scheduled half
  is restated as the reshaped availability smoke.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/load-tests`:
  - `Pass assertions` — the below-knee profiles' thresholds derive from the recorded baseline, with the absolute
    thresholds as the fallback for a reference that is missing, partial, or from another target.
  - `Configurable target, profile, rate, ratio, and duration` — a `baselineFile` property selects the reference.
  - `A baseline run reports its measurements` — a run also writes the committed reference and a record identified by the
    environment when it is not the default.

## Impact

- **Build**: `load-tests` (the new stats task, the moved `kneeFinder` registration, and the property whitelist all in
  its own build file, plus the simulation's derivation); `load-testing-conventions` (left applying `java-conventions`
  and the Gatling plugin); `scripts/load-test-baseline.sh`; a new committed, generated reference file.
- **Tests**: verified with a baseline run (the reference and record written from its log), a below-knee run asserting
  against it (logging the derived values and passing), a positive control (an artificially lowered reference fails the
  run), a target-guard run (a foreign reference falls back with its mismatch logged), and a sustained run after the poll
  fix (zero `PollShowcase` KOs, where the pre-fix run had 194).
- **Docs**: `AGENTS.md`/`README.md` (the reference, the derivation, the record) and `docs/ideas.md`.
- **Deployment**: none.
