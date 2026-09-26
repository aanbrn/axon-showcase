# Design

## Context

See `proposal.md` — Why. Current state: the six performance profiles hard-code absolute peaks and holds (`200`/`400`/
`4000`/`40000` users/s; `5m`/`30m`/`8h`/`2h`); the read stream fetches a detail every iteration (1:1 list:detail); the
write stream always runs the full schedule→start→finish→remove lifecycle; there is no pacing pause. The local stack's
calibration reached its `200`-unit/s ceiling **without the response time departing**, so the knee was never measured
(`docs/load-tests/2026-09-26-whole-stack.md`), and the runnable plateau held `120` units/s. The `load-tests` spec pins
the current curves, mix, and assertions, so this change owes a delta.

## Goals / Non-Goals

**Goals:**

- Make every profile runnable and meaningful against a configured knee-rate.
- Model a realistic mix (mostly-list reads; a write funnel that terminates) and client pacing.
- Make the knee-rate, mix, and pacing configurable and documented.

**Non-Goals:**

- No closed-model (fixed concurrent users) rewrite — the open/rate model is kept so the measurement profiles stay
  precise; think time is intra-iteration pacing, not a concurrency control.
- No change to `smoke`, `calibrate`, or `baseline` beyond accepting the new properties.

## Decisions

- **D1 — Profiles are multiples of a `kneeRate` property.** `average` = `0.6×` (5m ramp / 30m hold / 5m down); `soak` =
  `0.6×` (5m / `hold` / 5m); `stress` = `0.9×` (10m / 30m / 5m); `spike` bursts to `1.5×` users and ramps down;
  `breakpoint` ramps `0→1.5×` over 20m. The name is **`kneeRate`, not `knee`**, because the wrapper and the
  `load-testing-conventions` plugin already use `-Pknee` for the `kneeFinder` **output-file path**; reusing it would
  forward a path where the simulation parses an integer. The default is `200` (the previous ceiling), so a run without
  the property keeps a sane intensity. The alternative — keep absolute numbers and allow only a scale factor — leaves
  the unreachable extremes in place.
- **D2 — Weighted, terminating branches.** The read stream always fetches the list and fetches a detail with probability
  `detailShare` (`0.15`); the write stream always schedules and removes but starts with probability `startShare` (`0.6`)
  and, when started, finishes with probability `finishShare` (`0.5`). Every write iteration still removes its showcase
  (the aggregate finishes a started one before removing it), so the read model stays bounded; the alternative —
  abandonment without a guaranteed remove — leaks data over a long run.
- **D3 — Think time is intra-iteration pacing.** A configurable `thinkTime` (`PT1S`) pause sits between a stream's
  actions, modelling a client that pauses rather than looping back-to-back. In the open/rate model it shapes concurrency
  (a user lives longer), not throughput (the injected rate is the request rate); a closed/concurrent-user model with
  external think time is rejected as it would break the measurement profiles' rate precision.
- **D4 — New properties, defaults stated; the SSE hold follows the profile.** `kneeRate` (`200`), `thinkTime` (`PT1S`),
  `detailShare` (`0.15`), `startShare` (`0.6`), `finishShare` (`0.5`), `hold` (`PT2H`, replacing the `8h` soak),
  forwarded through the existing `systemProperties` mapping. The SSE connections hold for the profile's run length — for
  the soak, the ramps plus `hold` — not the `duration` property, so they stay open through the whole profile.
- **D5 — The assertion rule splits at the knee.** The below/at-knee profiles (`average`, `stress`, `soak`) keep the
  response-time percentiles; `smoke` keeps zero failures; `baseline` keeps its configured thresholds; `spike` and
  `breakpoint` sit above the knee, so they probe the ceiling and carry **no** pass assertions (like `calibrate`).
- **D6 — The delta removes and re-adds `Injection profiles`, and modifies three others.** The profile scenarios' names
  carry their curves, so a `MODIFIED` block cannot keep them accurate; a `REMOVED`+`ADDED` pair replaces them (carrying
  the calibrate/baseline scenarios). `Simulation exercises the gateway's read and write streams`,
  `Configurable target, profile, rate, ratio, and duration`, and `Pass assertions` are `MODIFIED` (their scenarios are
  preserved).
- **D7 — The docs are refreshed in the same change.** `AGENTS.md`'s load-test section enumerates the forwarded
  properties and `README.md` describes the profiles; both change with the property set.

## Risks / Trade-offs

- **The `kneeRate` default is arbitrary.** → It only applies when the property is unset; the docs name the measured knee
  as the intended input.
- **A weighted write funnel changes the action mix.** → The lifecycle still terminates, and the shares are properties;
  the recorded baselines predate the change and stay as records.
- **Think time inflates concurrency without changing throughput.** → Stated in D3; the measurement profiles' semantics
  are unchanged.
- **Dropping the above-knee assertions loses a hard failure signal.** → Those profiles exist to find the ceiling, so a
  degraded run is the finding; the below-knee profiles still gate.
- **Spec churn for the profile requirement.** → The `REMOVED`+`ADDED` pair carries every calibrate/baseline scenario and
  the migration names the new curves.
