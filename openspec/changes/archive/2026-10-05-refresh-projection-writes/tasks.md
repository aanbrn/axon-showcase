# Tasks

## 1. Projector refresh policy

- [x] 1.1 In `showcase-projection-service/src/main/java/showcase/projection/ShowcaseProjector.java`, extract the bulk
      request construction from `processEvents` into a package-private `BulkRequest` factory and set `Refresh.True` on
      it (import `org.opensearch.client.opensearch._types.Refresh`). Verify
      `./gradlew :showcase-projection-service:compileJava` passes.
- [x] 1.2 Add `showcase-projection-service/src/test/java/showcase/projection/ShowcaseProjectorTests.java` asserting the
      extracted `bulkRequest` factory's request carries `Refresh.True` — the factory is the only place a `BulkRequest`
      is constructed. Verify `./gradlew :showcase-projection-service:test` passes, and that it fails against
      `Refresh.False` (positive control, confirmed). The `processEvents` call site (`ShowcaseProjector::bulkRequest`) is
      not separately pinned by a test; it is a one-line, reviewed edge.
- [x] 1.3 Docs sweep: verify whether `AGENTS.md`, `README.md`, or `docs/adr/` describe projection refresh/visibility
      timing, and sweep `docs/ideas.md` for a stale "proposed" reference to this mechanism — tick after the pass and
      record that no edit was needed (expected).

## 2. Verification

- [x] 2.1 Run `./gradlew :showcase-projection-service:check -PskipITs -Pcoverage.gate.enabled=false` and confirm the
      unit, component, and static-analysis gates pass.
- [x] 2.2 Run `./gradlew :showcase-projection-service:integrationTest` (requires Docker; boots a real OpenSearch) and
      confirm the projector suite passes.
- [x] 2.3 Load-test re-measure — **the change's acceptance gate**. With the local Helm cluster installed
      (`./gradlew helmInstallToLocal`), run `./scripts/load-test-baseline.sh`, and read
      `showcaseProjector.projectionLag` plus the run's resource samples for the projector-side cost — the
      request-latency comparison alone does not establish acceptance, since a per-batch refresh taxes the projector and
      OpenSearch, not the query request path. The run records the new reference automatically when no read/write figure
      regresses beyond the 50% tolerance; use `REFRESH_BASELINE=1` only to deliberately accept (and record) a
      regression. If a figure regresses, fall back to a shorter `showcases` index `refresh_interval` and re-measure. The
      change is not reported done or archived without this result. **Result (before `Refresh.False` → after
      `Refresh.True`, same environment, operating point 124 units/s at a 200-unit/s ceiling): no read/write request
      figure regressed (all within ±2 ms; mean throughput 150.3 → 152.2 rps); `showcaseProjector.projectionLag` was
      unchanged (mean 55.9 → 55.7 ms, p99 110.8 → 110.9 ms) and the projector's ingest and batch rates were flat; the
      projector's bulk write latency rose as expected (`successTimer` mean 4.4 → 18.4 ms, p99 11 → 47 ms) with mean CPU
      flat (54 → 52m; max 75 → 89m) — absorbed without the projector falling behind. The knee was not probed above the
      200-unit/s ceiling, so a higher-load regression is not excluded. No fallback needed; the reference was left
      unchanged.**
- [x] 2.4 Run the `lesson-capture` subagent over the implementation diff and review findings, apply its durable
      proposals, and record the applied net `AGENTS.md` delta on this task; re-run `./gradlew spotlessApply` +
      `spotlessCheck` after the final edit. Applied: one Gotcha (attribute a runtime-cost effect with a before/after
      pair on the same environment, reading the operation's own timer — `projectionLag` measures ingest, not write
      cost); applied net `AGENTS.md` delta **+10 lines**; `spotlessApply`/`spotlessCheck`/`verifyCapturedMarkers` green.
