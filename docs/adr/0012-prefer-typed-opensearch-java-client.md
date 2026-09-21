# ADR-0012: Use the typed `opensearch-java` client over the deprecated high-level REST client

Date: 2026-09-21

Status: Accepted

## Context

The read side talks to OpenSearch through `spring-data-opensearch`, which historically arrived with the legacy
`org.opensearch.client:opensearch-rest-high-level-client` on the classpath. That client is the OpenSearch fork of the
Elasticsearch high-level REST client: its `RestHighLevelClient` and the `RestClientBuilder` that constructs it were
carried over from the pre-split Elasticsearch API and are now effectively superseded upstream, while the newer
`org.opensearch.client:opensearch-java` client is the typed, maintained surface for the same requests.

The repository's build files reflect a deliberate swap, and the reason was recorded nowhere — the first architecture
audit surfaced the exclusions as a choice whose rationale was missing. Both halves of the arrangement are easy to
misread: the high-level client is **excluded** in four modules but **version-managed** in `platform`, so a reader sees
the coordinate in two places with opposite treatment and no explanation.

## Decision

Use the typed `org.opensearch.client:opensearch-java` client for OpenSearch access, and keep the legacy
`opensearch-rest-high-level-client` off the runtime classpath of every module that talks to the cluster.

The arrangement has two parts:

- **Exclusion (every module that pulls `spring-data-opensearch`)** — `showcase-projection-model`,
  `showcase-projection-service`, `showcase-query-service`, and `showcase-query-client` each declare the dependency with
  an `exclude` on `opensearch-rest-high-level-client`. The exclusion is necessary because the high-level client arrives
  **transitively** through `spring-data-opensearch`; without it the deprecated client would ride in unnoticed. Which
  modules then add the typed client explicitly depends on whether they issue requests: `showcase-projection-service` and
  `showcase-query-service` add `opensearch-java` in their main source set, `showcase-query-client` adds it in its
  `componentTest` suite (its main code builds requests rather than talking to the cluster), and
  `showcase-projection-model` only excludes — it holds the shared mapping types and has no client code of its own.
- **Version alignment (`platform`)** — the platform BOM manages `opensearch-java`, `opensearch-rest-client`, and
  `opensearch-rest-high-level-client` together, so the transitive coordinate is pinned to a single reviewed version
  rather than whatever `spring-data-opensearch` happens to request. Managing the version and excluding the artifact are
  not in tension: the constraint keeps the coordinate consistent for anything that still resolves it, while the
  exclusions keep it off the modules' classpaths.

Alternatives considered and rejected: keeping the high-level REST client (it is the deprecated surface the swap exists
to leave, and mixing it with `opensearch-java` would put two client models on one classpath); dropping
`spring-data-opensearch` and using the typed client directly (it supplies the repository and `@Document` mapping layer
the read side relies on, which the typed client does not replace); and excluding the client without managing its version
in `platform` (the coordinate would then resolve to whatever `spring-data-opensearch` requests, unreviewed).

`spring-data-opensearch` itself stays: it supplies the repository and `@Document` mapping layer, and only its choice of
transport client is overridden. It still needs the low-level `opensearch-rest-client` (kept, and pinned separately), so
the swap is from the _high-level_ client only, not from the REST transport.

## Consequences

- The read side issues OpenSearch requests through the typed client's request/response model, which is the surface
  upstream maintains; the deprecated high-level client is not a dependency any module's code compiles against.
- A future `spring-data-opensearch` bump that changes which client it drags in transitively is absorbed by the existing
  `exclude` blocks rather than silently altering the runtime classpath — but the exclusions are declared per module and
  per configuration (main and test suite), so a **new** module that pulls the starter must repeat them.
- The version-alignment half is invisible in the four modules: bumping `opensearch-client-rest` in the catalog moves the
  high-level coordinate for anything that resolves it, independently of the exclusions, so the two must be read together
  when reasoning about the OpenSearch dependency graph.
- The deprecated low-level `RestClientBuilder`/`RestClient` still surface in the codebase's history as a separately
  parked concern; this ADR covers the high-level client only.
