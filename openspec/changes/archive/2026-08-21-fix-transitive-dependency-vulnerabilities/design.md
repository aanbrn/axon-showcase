# Design: Fix transitive dependency vulnerabilities

## Context

See proposal.md — Why. The `platform` module is applied to every module through
`java-conventions.gradle.kts` (`implementation(platform(project(":platform")))`), so constraints declared there
propagate to all four affected projects. All introducing clients (`elasticsearch-java:9.5.1`, `opensearch-rest-client:3.8.0`,
`opensearch-java:3.9.0`) are already at their latest releases and still declare the vulnerable transitives, so there is no
source-side fix to wait for.

## Goals / Non-Goals

**Goals:**
- Clear the Snyk findings by constraining Jackson 3 (`tools.jackson.core`) and `httpclient5` to patched versions.
- Keep the change confined to dependency management (`gradle/libs.versions.toml`, `platform/build.gradle.kts`).

**Non-Goals:**
- Upgrading `elasticsearch-java`, `opensearch-rest-client`, `opensearch-java`, or `spring-data-opensearch` (no newer
  versions exist; a future source-side fix can raise or drop the constraints).
- Changing any service code or runtime behavior.

## Decisions

**D1 — Align Jackson 3 through `tools.jackson:jackson-bom` at `3.1.6`, not per-module constraints.**
Import `api(platform(libs.jackson3.bom))` in the platform next to the existing `jackson2-bom`. This mirrors the
project's established pattern, keeps all `tools.jackson.*` modules (core, databind, annotations) on one version, and
satisfies both fixes (core ≥ `3.1.4`, databind ≥ `3.1.5`).
- Alternatives rejected: (a) individual `constraints { api(...) }` for `jackson-core:3.1.4` and `jackson-databind:3.1.5`
  — minimal but leaves the family unaligned and more entries to maintain; (b) BOM at `3.2.2` (latest) — a minor-version
  jump beyond what `elasticsearch-java:9.5.1` was built against (3.1.x), with no security gain over `3.1.6`.

**D2 — Constrain `httpclient5` to `5.6.3` in the platform `constraints` block.**
Add `api(libs.httpclient5)` next to the existing `httpcore5-h2` constraint. The services already resolve `5.6.3` by
conflict resolution (via `elasticsearch-rest5-client`), but `showcase-projection-model` resolves `5.6.1`; an explicit
constraint floors every module at the patched version regardless of the graph.
- Alternatives rejected: relying on the existing conflict resolution (leaves `projection-model` vulnerable) or bumping
  OpenSearch clients (already at latest, still declare `≤ 5.6.1`).

**D3 — Version entries live in `gradle/libs.versions.toml`.**
Add `jackson3-bom = "3.1.6"` and `httpclient5 = "5.6.3"` to `[versions]`, plus the `jackson3-bom` platform and
`httpclient5` library entries, consistent with how `jackson2-bom` and `httpcore5-h2` are declared.

## Risks / Trade-offs

- [Jackson 3 `3.1.6` was not the version `elasticsearch-java:9.5.1` was built against (`3.1.0`)] → Patch-level 3.1.x
  bumps are API-compatible; verified by the query/projection integration tests (real OpenSearch via Testcontainers) and
  the query-client integration tests, which exercise Jackson 3 deserialization at runtime.
- [Snyk's Gradle analysis may still attribute `httpclient5` to the declaring client's POM version] → The constraint
  makes Gradle resolve `5.6.3` everywhere; verification runs `snyk test --all-sub-projects` to confirm the findings clear.
- [Future client upgrades declare newer versions] → Constraints are floors, not caps; a newer declared version simply
  wins conflict resolution and can be adopted or pinned then.
- [BOM import over-constrains `tools.jackson` modules the project does not use] → Harmless alignment; the BOM only
  affects the modules already present in the graph.