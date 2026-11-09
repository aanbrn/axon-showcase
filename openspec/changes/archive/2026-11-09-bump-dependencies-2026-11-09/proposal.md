# Proposal

## Why

The weekly dependency check (issue #13) reports stable catalog updates, eight in-range npm rows, and `build-logic` pins.
Re-resolved on 2026-11-09 with the repository's own checks rather than read off the issue, because the issue refreshes
weekly: `./gradlew dependencyUpdates :showcase-web-ui:npmOutdated buildLogicUpdates` names a wider set than the issue
carries. Taking the resolved set keeps the pinned set current.

## What Changes

- **`gradle/libs.versions.toml`** — ten catalog coordinates: `lz4-java` `1.12.1`→`1.14.0`, `elasticsearch-client-java`
  `9.5.5`→`9.5.6`, `jackson2-bom` `2.22.4`→`2.23.0`, `jackson3-bom` `3.2.4`→`3.3.0`, `zstd-jni` `1.5.7-25`→`1.5.7-26`,
  `errorprone` `2.51.0`→`2.52.0`, `guava` `33.7.2-jre`→`33.8.0-jre`, `netty-bom` `4.2.19.Final`→`4.2.20.Final`,
  `opentelemetry-bom` `1.67.0`→`1.68.0`, `spotless-plugin` `8.10.4`→`8.11.0`.
- **`gradle/libs.versions.toml`** — one `build-logic` plugin pin: `protobuf-plugin` `0.10.0`→`0.10.1`.
- **`showcase-web-ui/package-lock.json`** — an `npm update` reconciliation across the eight in-range direct rows and the
  graph they re-resolve. `package.json` is unchanged: every moved release already satisfies its caret range.
- **`gradle/libs.versions.toml`'s `spotless-plugin` bump reflows sources**: the formatter ships with the plugin pin, so
  `spotlessApply` runs after the bump and any reflowed file is accounted for rather than reported as no source change.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: a pure dependency bump, which no capability spec covers (`skip_specs`). The `dependency-management` spec states
the reports' scoping and suppression rules, not the pinned versions, and its illustrative examples name no coordinate
this change moves; every floor `dependency-security` states that a taken version moves stays satisfied (Jackson 3
`3.2.3`, Jackson 2 `2.22.3`, `zstd-jni` `1.5.7-14`, `guava` `33.7.2-jre`, `netty-bom` `4.2.18.Final`).

## Impact

- **`lz4-java` and `zstd-jni`** — the Kafka codecs. `kafka-clients` names the maintained `at.yawk.lz4` fork directly,
  and the `org.lz4:lz4-java` substitution ADR-0020 records is protective, firing for any future consumer of the
  vulnerable coordinate. Both are platform constraints, so the integration tier resolves and boots the services with
  them; the codec path itself is not exercised, because no integration configuration selects a compression type.
- **`netty-bom`** — versions the reactor-netty stack the four services carry.
- **`opentelemetry-bom` and `elasticsearch-java`** — version every service's OTLP exporter and the query and projection
  services' and the query client's `componentTest` Elasticsearch client.
- **`guava`** — a platform constraint and a command-service dependency, and the version the `checkstyle` and Error Prone
  `annotationProcessor` tool classpaths resolve.
- **`jackson2-bom` and `jackson3-bom`** — platform imports: Spring Boot's Jackson 2 default and the Axon/Elasticsearch
  Jackson 3 line.
- **Build path**: `errorprone` gates every non-test compile (with `nullaway`), `spotless-plugin` is the formatter, and
  `protobuf-plugin` generates the query side's gRPC sources.
- **Bundle and check path**: the npm set moves the production-bundle packages (`@reduxjs/toolkit`,
  `@tanstack/react-query`, `vite`, `@vitejs/plugin-react`) and the test and lint tooling (`vitest`,
  `@vitest/coverage-v8`, `@playwright/test`, `eslint-plugin-boundaries`), so the frontend `check` and the production
  bundle `build` run.
- **No suppression moves**: no bumped coordinate is in `config/dependency-updates/major-disabled.txt`,
  `config/dependency-updates/hold-back.txt`, or `config/web-ui-updates/major-disabled.txt`, and `.snyk`'s ignores name
  no moved coordinate.
