## Context

See `proposal.md` for the motivation. The current `verifyInfraImageVersions` gate (in
`build-logic/src/main/kotlin/VerifyInfraImageVersionsTask.kt`) compares the official `*-image-tag` against the Bitnami
chart's preconfigured `image.tag` by stripping trailing `.0` segments from both sides before an exact compare. Docker
Hub reality: postgres publishes only two-segment tags (`17.6`, frozen per minor — no `17.6.0`, no `17.6.1`), while
Kafka/OpenSearch publish full-patch tags (`3.9.0`, `3.2.0`); a bare-major tag (`17`) is a floating reference that
re-points to the latest 17.x. The current pins are `17.6`, `3.9.0`, `3.2.0` — all valid and unchanged by this design.

## Goals / Non-Goals

**Goals:**
- Make the comparison follow the granularity the official tag declares: two-segment tag ↔ chart truncated to two
  segments; three-segment tag ↔ exact chart match.
- Reject bare-major official tags as floating references.
- Keep the cacheable gate, current pins, and all existing pass/fail outcomes for the current pins unchanged.

**Non-Goals:**
- Not changing the current pins or chart versions.
- Not pinning postgres by digest (unnecessary: `postgres:17.6` is already immutable under postgres's frozen-minor
  release model).
- Not adopting range/prefix semantics that would accept a bare `17` against any 17.x chart.

## Decisions

**Decision: truncate the chart, never the official tag.**
The chart app version's numeric prefix is truncated to the official tag's numeric segment count, then exact-compared
against the official tag's numeric prefix. The official tag is compared as-is (only its numeric prefix is taken). This
replaces the current `stripTrailingZeroSegments` on both sides. Rationale: the strip is meaningful only for the chart
side (Bitnami always writes the full app version, e.g. `17.6.0`); stripping the official tag erases its declared
precision and is what allowed the bogus `17` ≡ `17.0.0` equivalence. Alternatives considered:
- *Strip both sides* (current) — kept the `17` ≡ `17.0.0` false equivalence and required post-hoc justification for
  why `17.6` ≡ `17.6.0`.
- *Prefix/range match* (`17` matches any 17.x) — weakens the gate; a floating test tag plus any chart on the same
  major line would pass, defeating the drift check.
- *Digest pinning* — unnecessary for postgres (frozen minors) and breaks the tag-based coordinate convention.

**Decision: reject bare-major official tags (fewer than two numeric segments).**
A guard runs before the comparison and fails with a message stating the tag is a floating reference and must declare at
least the minor version. Rationale: `postgres:17` re-points quarterly; a floating tag cannot be a single source of
truth. The truncation model makes this looseness explicit (a one-segment tag would truncate any chart to `17`), so the
guard is mandatory, not optional. Alternatives considered: silently accept `17` as "the 17 line" — rejected because it
makes the gate's drift guarantee meaningless for major-line tags.

**Decision: keep the truncation implementation small and local.**
Replace `stripTrailingZeroSegments` with a helper that truncates a numeric version to N segments, and add a
segment-count check on the official tag. The `@CacheableTask` wiring, `@InputFiles` values files, `@OutputFile` result
marker, and embedded `helm repo add`/`update` from the prior change are untouched.

## Risks / Trade-offs

- [Postgres publishes no three-segment tag, so its official pin can only ever express minor granularity] → Accepted
  constraint; `17.6` is already the exact pin under postgres's frozen-minor model, and the spec documents that the
  comparison is at the declared granularity.
- [A chart app version with a non-zero patch on a frozen-minor component (e.g. `17.6.2`) would be rejected] → Cannot
  occur for postgres (frozen minors); for Kafka-style components that is exactly the drift the gate should catch.
- [The bare-major guard changes behavior for any future `17`-style pin from "silent pass against a 17.0.0 chart" to
  "fail with a clear message"] → Intended; this is the bug being fixed.

## Migration Plan

1. Implement the guard and truncation in `VerifyInfraImageVersionsTask.kt`.
2. Update the main spec and `AGENTS.md` wording.
3. Verify the current pins still pass, a bare `17` now fails with the clear message, and a deliberate chart mismatch
   still fails; then run `check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`.

## Open Questions

None.