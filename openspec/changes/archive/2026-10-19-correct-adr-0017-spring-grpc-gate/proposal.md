# Proposal

## Why

The 2026-10-19 architecture audit reported ADR-0017's `Revisit when:` condition as **appearing met**, and the reading
was genuinely open: the recorded signal was "a first-party Spring gRPC release targeting Boot 3.x", and Spring's `0.x`
line does target Boot 3 — `0.12.0` (2025-10-24) is "the last version to support boot 3" — while the ADR's rejection
reasoning cited only the `1.0` line's Boot 4 target. That `0.x` line is end-of-life, so whether an EOL first-party
release satisfies the ADR's "the community starter can be replaced" intent was the owner's call, not a predetermined
change. The owner resolved it on 2026-10-19: **a maintained Boot-3 line**, not any release. The deferral therefore
stands, and the gate's wording must say so — otherwise the next check reads the same superseded release as meeting it.

## What Changes

- `docs/adr/0017-grpc-query-transport.md`: `Revisit when:` states the condition as a **maintained** first-party Boot-3
  line, labels the clause as an external coordinate, and records why the `0.x` line does not meet it.
- `docs/adr/0017-grpc-query-transport.md`: the `_Alternatives considered:_` sentence for Spring gRPC, which cited only
  the `1.0` line, carries the alternative's rejection reason (the `0.x` line is end-of-life) and points at the gate for
  the qualifier's full evidence.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: `skip_specs: true`. No capability spec owns ADR-0017 or its gate (verified: no spec names `ADR-0017`,
`Spring gRPC`, or `org.springframework.grpc`), so the correction is a docs edit to the ADR itself.

## Impact

- Files: `docs/adr/0017-grpc-query-transport.md`.
- No code, build, test, or deployment impact. The decision the ADR records is unchanged — the deferral stands; only the
  wording that says when it reopens is corrected.
