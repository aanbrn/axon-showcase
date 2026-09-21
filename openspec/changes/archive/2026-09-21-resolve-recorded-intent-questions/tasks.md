# Tasks — resolve the recorded intent questions

## 1. Q3: the cache fallback refactor

- [x] 1.1 Establish the actual behaviour first: Caffeine's `AsyncCache.getIfPresent` returns `null` for an
      exceptionally-completed future (verified directly against Caffeine 3.2.4), so a failed cached future is a miss and
      the suppression was redundant rather than masking a bug.
- [x] 1.2 Rewrite both fallback paths to `Mono.fromFuture(...)` — the future is observed (no dangling return), so
      `@SuppressWarnings("FutureReturnValueIgnored")` is unnecessary; both method-level suppressions deleted.
- [x] 1.3 Confirm `compileJava` is clean with no `FutureReturnValueIgnored` warning and no suppression.
- [x] 1.4 `ShowcaseRestControllerCT` green at 76 tests, covering the hit/miss/missing-showcase scenarios the spec
      specifies.

## 2. Q1, Q2, Q4: the durable records

- [x] 2.1 `AGENTS.md` — the `Lombok` bullet records the builder requirement (`builder()` in the aggregate and saga,
      `toBuilder()` in tests) as the reason for value types over records, and states the performance question is
      untested.
- [x] 2.2 `docs/adr/0012-prefer-typed-opensearch-java-client.md` — the typed-client decision and the two-part
      exclusion/version-alignment arrangement.
- [x] 2.3 `docs/adr/0013-feature-sliced-design-layering.md` — the layer arrangement, the one-way import rule, the
      rejected alternatives, and the "convention, not a gate" consequence.
- [x] 2.4 `docs/ideas.md` — the answered intent-questions entry removed.

## 3. Verification

- [x] 3.1 `spotlessCheck` green; no line over 120 in the touched files.
- [x] 3.2 `openspec validate --all` passes with the change present.
- [x] 3.3 The two ADRs follow the Nygard template (Status/Context/Decision/Consequences) and `docs/adr/README.md` needs
      no index row (it carries no ADR list).
