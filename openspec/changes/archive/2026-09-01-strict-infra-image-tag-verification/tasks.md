## 1. Comparison semantics

- [x] 1.1 Replace `stripTrailingZeroSegments` (applied to both sides) in `VerifyInfraImageVersionsTask.kt` with a
      truncation helper that cuts a numeric version to a given segment count, and compare the official tag's numeric
      prefix against the chart app version's numeric prefix truncated to the official tag's segment count; verify the
      current pins (`17.6`, `3.9.0`, `3.2.0`) still report consistent
- [x] 1.2 Verify a genuine patch mismatch still fails (e.g. official `3.9.0` with chart app version `3.9.1`) and that
      the postgres pairing (`17.6` ↔ chart `17.6.0`) passes at minor granularity

## 2. Bare-major guard

- [x] 2.1 Add a guard that fails with a clear message when an official `*-image-tag` has fewer than two numeric
      segments, before the comparison runs; verify a bare `17` fails with the floating-reference message while `17.6`
      still passes
- [x] 2.2 Verify cache invalidation still works after the change: unchanged inputs are `UP-TO-DATE`/`FROM-CACHE`, and
      a coordinate change re-runs the verification

## 3. Docs and verification

- [x] 3.1 Update `openspec/specs/showcase/quality/infra-image-versions/spec.md` and `AGENTS.md` wording to replace the
      `17` ≡ `17.0.0` zero-padding equivalence with the declared-granularity model (chart truncated to official tag's
      segment count; bare-major tags rejected), keeping lines within 120 characters
- [x] 3.2 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`, confirming the
      gate, all module checks, and spec validation pass