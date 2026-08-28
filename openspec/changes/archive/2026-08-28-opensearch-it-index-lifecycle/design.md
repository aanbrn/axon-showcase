# Deterministic OpenSearch Index Lifecycle in Integration Tests — Design

## Context

See proposal.md — Why. The two ITs had asymmetric, assumption-heavy index handling. The query service's
`opensearchIndexInitializer` (`ShowcaseQueryApplication.java:81-112`) creates the index at startup — but only when
`showcase.query.index-initialization-enabled` is `true` (the default). The projection service has **no** startup
initializer, so its IT had to create the index itself. The tests also asserted boolean results of `createWithMapping()`
and `delete()` — which return `false` when the index already exists / does not exist respectively — baking in state
assumptions.

## Goals / Non-Goals

**Goals:**
- Make each IT start from a fresh `showcases` index with the entity's mapping, regardless of app startup behavior or
  prior test outcomes.
- Make the lifecycle explicit and symmetric across both suites.

**Non-Goals:**
- Not changing the index mapping or production code (the mapping fix is `fix-date-nanos-precision`).
- Not introducing a shared test helper/base class (two call sites; a helper is premature).

## Decisions

### D1: Own the index lifecycle fully in the test (disable the startup initializer in the query IT)

The query IT disables the app's initializer with
`@SpringBootTest(properties = "showcase.query.index-initialization-enabled=false")`, and manages the index entirely
in `@BeforeEach`/`@AfterEach`.
This removes the hidden dependency on boot-order initialization and on the previous test's `@AfterEach` having
recreated the index.

### D2: Assert both ends of the lifecycle in both suites

- `@BeforeEach`: assert `exists() == false` → `createWithMapping() == true` → assert `exists() == true`.
- `@AfterEach`: assert `exists() == true` → `delete() == true` → assert `exists() == false`.

Asserting the pre-condition makes a leftover index (from a crashed run or an initializer) fail loudly at the start of
the test rather than silently misbehaving. `createWithMapping()` and `delete()` return `false` for the wrong prior
state, so asserting `true` on a clean slate is guaranteed.

### D3: `@AfterEach` deletes only (no recreate)

The old query IT recreated the index in `@AfterEach` so the *next* test could assert it exists. With `@BeforeEach`
now creating the index, the recreate is redundant and removes the cross-test coupling.

## Risks / Trade-offs

- **Disabling the initializer weakens coverage of that production bean in the query IT** → the initializer itself is
  not the subject under test here; a dedicated integration test (`ShowcaseQueryApplicationIT`) covers it. The
  `exists()` pre-condition assertion still verifies the index is truly absent before each test.
- **`@BeforeEach` fails loudly if a prior test crashed leaving the index behind** → that is the intent: surface
  leftover state instead of masking it. Testcontainers provides a fresh container per suite, so within-suite
  leftovers can only come from a broken `@AfterEach`.

## Migration Plan

1. Apply the lifecycle edits to both IT classes (already done in the working tree from the spike branch).
2. Verify both suites pass locally (`:showcase-query-service:integrationTest`,
   `:showcase-projection-service:integrationTest`).
3. Verified in CI via the spike workflow on GitHub Actions (both suites passed).

Rollback: revert the two test files; no production or data impact.

## Open Questions

None.