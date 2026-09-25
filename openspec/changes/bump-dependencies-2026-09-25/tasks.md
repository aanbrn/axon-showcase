# Tasks

## 1. Implementation

- [x] 1.1 Bump the 19 `[versions]` entries in `gradle/libs.versions.toml`: `lz4-java` `1.11.3`,
      `elasticsearch-client-java` `9.5.4`, `spotless-plugin` `8.10.2`, `caffeine` `3.3.0`, `handlebars` `4.5.5`,
      `zstd-jni` `1.5.7-19`, `protobuf` `4.36.2`, `nullaway` `0.14.1`, `opentelemetry-bom` `1.66.0`, `swagger` `2.2.55`,
      `httpcore5` `5.4.4`, `jetty` `12.1.13`, `kotlin` `2.4.20`, `mockito` `5.24.0`, `spring-data-opensearch` `2.0.8`,
      `lombok` `1.18.48`, `springdoc-openapi-starter` `2.9.1`, `swagger-ui` `5.32.15`, and `wiremock-spring-boot`
      `4.4.2`. Verify `./gradlew dependencyUpdates` no longer lists any of those coordinates as an available update in
      `build/dependencyUpdates/report.txt` (they then appear under `using the latest release version`; the held-back
      `opensearch-java` row and the spurious `log4j-core` row remain available updates — read the report).
- [x] 1.2 Hold `opensearch-java` at `3.9.0` (do **not** bump to `3.10.0`): its `Hit.matchedQueries()` return-type change
      is binary-incompatible with `spring-data-opensearch` 2.x (`NoSuchMethodError` in `DocumentAdapters.from`). Verify
      `:showcase-query-service:integrationTest` passes with `spring-data-opensearch` `2.0.8` + `opensearch-java`
      `3.9.0`.
- [x] 1.3 Bump the web UI's resolved `vitest` to `5.0.2` (`cd showcase-web-ui`, `npm update vitest`; the manifest's
      `^5.0.1` already admits it, so only `package-lock.json` moves — a plain `npm install` keeps the locked `5.0.1`).
      Verify `showcase-web-ui/build/npm-outdated.txt` (via `./gradlew :showcase-web-ui:npmOutdated`) lists only
      `typescript` (the deferred major).

## 2. Docs

- [x] 2.1 Refresh the two claims this bump falsifies: the Caffeine gotcha's verification anchor in `AGENTS.md`
      (`3.2.4`→`3.3.0`, re-verified) and the NANOS idea's `spring-data-opensearch` line reference in `docs/ideas.md`
      (`2.0.7`→`2.0.8`, which declares `spring-data-elasticsearch` 5.5.13 directly).
- [x] 2.2 Park the held-back `opensearch-java` follow-up in `docs/ideas.md` (bundled into this change's branch, as the
      owner directed): no mechanism holds back a same-major coordinate, so the weekly tracker re-lists `opensearch-java`
      `3.10.0` until `spring-data-opensearch` 3.x (SB4, ADR-0004) or a hold-back mechanism lands.

## 3. Verification

- [x] 3.1 Run the full `./gradlew check` **with integration tests** (requires Docker) — the OpenSearch/Elasticsearch
      client bumps and the minors (caffeine, kotlin, mockito, wiremock) are exercised against real infrastructure; it
      passes.
- [x] 3.2 Run `./gradlew :showcase-web-ui:check` (Vitest `5.0.2`) and `./gradlew :showcase-web-ui:build`; both pass.
- [x] 3.3 Run `openspec validate --changes` and `./gradlew spotlessApply` followed by `./gradlew spotlessCheck`; both
      pass.
