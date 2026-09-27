# Proposal: Tighten the load-test streams

## Why

Three gaps make the load-test streams weaker than they read. `stressPeakUsers(1.5 × kneeRate × share)` passes a **rate**
where Gatling takes a **user count** (`HeavisideOpenInjection` spreads that many arrivals over the duration), so the
`spike` profile injects a couple of hundred arrivals over two minutes — roughly 1 % of the intended `1.5×` burst. The
SSE stream asserts one event and then only holds, and an SSE failure alone does not fail a run, so a stream that dies
after its first event passes silently. And the read stream skips its detail fetch while the showcase list is empty, so a
cold target never exercises `GET /showcases/{showcaseId}`. Each makes a run quieter than the load it claims.

## What Changes

- `ShowcaseSimulation`'s `spike` profile injects `1.5×` the knee-rate as a **rate** — a constant users-per-second burst
  held for 2 minutes, then a ramp down — rather than as a user count.
- The SSE stream re-arms its check on the open connection and, mid-hold, checks for a further showcase event, for
  profiles whose write-lifecycle stream runs for the hold. The profiles that carry assertions also assert the SSE
  connection has no failed requests, so a stalled stream fails the run rather than passing silently.
- The read stream creates one showcase (once per run) when the list is empty, so the detail path is exercised on a cold
  target rather than skipped.
- The delta spec modifies the four requirements that describe those behaviors, and the implemented `docs/ideas.md` entry
  is removed.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/load-tests`:
  - `Pass assertions` — the profiles that carry assertions also assert the SSE connection's failures.
  - `Simulation exercises the gateway's read and write streams` — the read stream seeds one showcase on a cold target so
    the detail path is exercised.
  - `Simulation exercises the SSE event stream` — a further showcase event is checked during the hold, and asserted
    where the profile carries assertions.
  - `Knee-relative injection profiles` — the `spike` scenario states a rate (workload units per second), not a user
    count.

## Impact

- **Build**: `ShowcaseSimulation` only (Spotless/ErrorProne-gated); no new dependency, no gate change.
- **Tests**: verified with short `gatlingRun` runs — the `spike` burst's observed rate is the intended order of
  magnitude, the SSE check passes on a sustained profile and, as a positive control, fails the run against a matcher
  that cannot match, and a cold target exercises the detail path.
- **Docs**: the implemented `docs/ideas.md` entry; no `AGENTS.md`/`README.md` change is owed unless the sweep finds one
  describing the old `spike` semantics.
- **Deployment**: none. A cold target gains one showcase per run (the write stream still removes its own).
