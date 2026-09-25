# Design

## Context

See `proposal.md` — Why. Current state that shapes the approach:

- The catalog (`gradle/libs.versions.toml`) single-sources each coordinate via `version.ref`; several updates share one
  `[versions]` entry (`caffeine` covers `jcache`, `jetty` covers `jetty-ee10-bom`, `swagger` covers the three
  `io.swagger.core.v3` artifacts, `mockito` covers `mockito-bom`/`mockito-core`, `opensearch-client-java` covers
  `opensearch-java`, `spring-data-opensearch` covers its `-starter`/`-testcontainers` variants), so the 26 taken rows
  map to 19 `[versions]` edits.
- All are minor/patch within their current majors; the `major-disabled` suppression already excludes the deferred majors
  (Axon, JGroups, Flyway, the Spring Boot 4-blocked `spring-data-opensearch`/`springdoc` majors).
- The `log4j-core [2.17.1 -> 2.26.1]` row is a `checkBuildEnvironmentConstraints` floor from `spotbugs-annotations`, not
  an update (ADR-0007); the catalog already pins `log4j = "2.26.1"`.
- `opensearch-java` `3.10.0` is **not** a drop-in for `3.9.0`: it changes `Hit.matchedQueries()`'s return type from
  `List<String>` to the new `MatchedQueries` type, and `spring-data-opensearch` 2.x's `DocumentAdapters.from` invokes
  the old signature, so the combination fails at runtime — observed as
  `NoSuchMethodError: 'java.util.List org.opensearch.client.opensearch.core.search.Hit.matchedQueries()'` and a 5s
  request timeout in six `ShowcaseQueryControllerIT` cases. `matchedQueries()` keeps its `List` return in `3.9.0` and
  changes in `3.10.0`; supporting `3.10.0` therefore rides `spring-data-opensearch` 3.x, which targets Spring Boot 4
  (the deferred migration, ADR-0004).

## Goals / Non-Goals

**Goals:**

- Take 26 of the tracker's 27 stable catalog updates (19 catalog entries) and the in-range npm `vitest` patch, staying
  within the current majors.

**Non-Goals:**

- `opensearch-java` `3.10.0` (binary-incompatible with `spring-data-opensearch` 2.x — see Context), any major bump
  (suppressed or deferred), the deferred `typescript` 7, the spurious `log4j-core` row, and the Gradle wrapper (already
  current).

## Decisions

- **Bump the `[versions]` entries in one pass.** The catalog is the single source, so each reported coordinate maps to
  its entry (a shared entry covers its dependants).
- **Hold `opensearch-java` at `3.9.0` and take `spring-data-opensearch` `2.0.8`.** The client bump is
  binary-incompatible with the Spring Data OpenSearch 2.x line (see Context); the gate that caught it is the full
  `check` **with integration tests**, which the OpenSearch client bumps require — a Docker-free check compiles and
  passes while the runtime `NoSuchMethodError` hides. The hold-back is recorded here, not suppressed: the
  `major-disabled` list addresses majors only, so the tracker re-lists `opensearch-java` `3.10.0` until the
  `spring-data-opensearch` 3.x line (SB4, ADR-0004) or a hold-back mechanism lands — parked in `docs/ideas.md`.
- **Bump `vitest` to `5.0.2` via `npm update vitest`.** An in-range patch; `package.json` is unchanged (a plain
  `npm install` keeps the locked `5.0.1`, since the lockfile already satisfies `^5.0.1`). The reconciled graph also
  moves vitest's required `why-is-node-running` `2.3.0`→`3.2.2` and prunes three stale `extraneous` entries — describe
  the lockfile diff by what moved (the AGENTS.md `npm update` gotcha), not as "the patch alone".
- **Sweep the two docs the bump falsifies.** A pinned version appears in doc claims too → refresh the Caffeine
  verification anchor (re-verified at 3.3.0) and the NANOS idea's `spring-data-opensearch` line reference in the same
  change.

## Risks / Trade-offs

- **The OpenSearch/Elasticsearch client bumps** (`spring-data-opensearch` 2.0.7→2.0.8, `elasticsearch-java` 9.5.1→9.5.4)
  affect the projection/query services' real infrastructure paths → run the full `check` **with integration tests**, not
  only the Docker-free form; this is also what caught the `opensearch-java` incompatibility.
- **A minor bump can still change behavior** (caffeine 3.2→3.3, kotlin 2.4.10→2.4.20, mockito, wiremock-spring-boot) →
  the full `check` is the gate.

## Migration Plan

- Apply: the catalog edits, the npm patch, and the two doc refreshes.
- Rollback: revert the catalog/lockfile (the doc refreshes revert with them).
