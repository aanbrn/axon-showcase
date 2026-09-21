# Resolve the recorded intent questions

## Why

The `/audit-architecture` sweep reports deliberate choices whose rationale is not recorded. Three such questions were
parked in `docs/ideas.md` awaiting the owner's answers, and a fourth arrived with the audit reports themselves (the FSD
layering). All four are now resolved — two with durable records (ADRs), one by recording the convention, and one by
correcting a false premise and removing redundant code. Leaving them open leaves the audit's questions unanswered and
the corpus carrying `AGENTS.md` claims the owner has since refined.

## What Changes

- **The Lombok-vs-records choice (Q1)** — recorded in the `Lombok` convention bullet: the builder pattern is required
  (`builder()` in the aggregate and saga, `toBuilder()` in tests), which a record's canonical constructor cannot
  express. The open question of whether records would be faster is recorded as **untested**, not as a claim.
- **The OpenSearch client choice (Q2)** — resolved as **ADR-0012**: the typed `opensearch-java` client over the
  deprecated high-level REST client, with the two-part arrangement (per-module `exclude` of the transitive coordinate;
  `platform` version alignment) documented so the exclusion set is not re-derived.
- **The FSD layering (Q4)** — resolved as **ADR-0013**: the six-layer Feature-Sliced Design arrangement and its one-way
  import rule, recorded with the rejected alternatives and the note that the direction is convention, not a gate (the
  parked `eslint-plugin-boundaries` idea is its enforcement mechanism).
- **The cache-write suppression (Q3)** — refactored, and the premise corrected. The suppression was **redundant**, not a
  hidden bug: Caffeine's `AsyncCache.getIfPresent` returns `null` for an exceptionally-completed future, so a failed
  cached future is indistinguishable from a miss and the suppressed callbacks only ever run on success. The two
  `@SuppressWarnings("FutureReturnValueIgnored")` on `ShowcaseRestController` are deleted and both fallback paths use
  `Mono.fromFuture(...)`, which observes the future (no dangling return) — recorded as a Gotcha with the Caffeine
  evidence.
- **The parked ideas** — the intent-questions entry leaves `docs/ideas.md` (answered); its siblings stay.

## Impact

- **Specs**: none — `skip_specs`. The `Cache fallback on transient query failures` requirement in `gateway/rest-api`
  describes the hit/miss/missing-showcase scenarios, all of which the refactor preserves (a failed cached future takes
  the miss branch, already specified), and no spec describes the suppression or the `thenAccept` mechanism.
- **Code**: `ShowcaseRestController`'s two fallback paths; behaviour-preserving, covered by the existing 76 component
  tests (`ShowcaseRestControllerCT`).
- **Docs**: `AGENTS.md` (the Lombok bullet gains the builder rationale; a Gotcha records the Caffeine semantics),
  `docs/adr/0012`, `docs/adr/0013`, `docs/ideas.md` (one entry removed).
