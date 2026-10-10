# Design

## Context

See `proposal.md` — Why. The gateway enables Spring Boot `buildInfo()` inline in `showcase-api-gateway/build.gradle.kts`
so its OpenAPI document reports the version via a `BuildProperties` bean (ADR-0016); the other three JVM services enable
none. Every JVM service's `application.yml` exposes only `health,prometheus` on its management port, so `/actuator/info`
is unreachable. `build-logic/src/main/kotlin/spring-boot-conventions.gradle.kts` is applied by exactly the four JVM
services (`showcase-command-service`, `showcase-query-service`, `showcase-projection-service`, `showcase-api-gateway`).
The captured `buildInfo()` rule in `AGENTS.md` records the pinned-Spring-Boot mechanics: the doc's `time.set(null)` is a
no-op on 3.5.16 (which needs `excludes`), and `build-info.properties` is a `BuildProperties` bean, not an `Environment`
property source.

## Goals / Non-Goals

**Goals:**

- Bake the build identity into every JVM service, cacheably, from one place.
- Expose it at runtime via the actuator `info` endpoint.

**Non-Goals:**

- The web UI (a static nginx image with no actuator) — its identity is its image tag.
- Any new endpoint or response contract beyond the standard actuator `info`.

## Decisions

### Own `buildInfo()` once in the shared convention plugin, not per service

`spring-boot-conventions` is applied by exactly the four JVM services, so the block belongs there once — and the
gateway's existing inline copy is removed, so the four do not drift. The alternative (copy the gateway's block into the
three services) was rejected as duplication the "avoid redundancy" convention forbids. `excludes = ["time"]` is what
keeps `bootBuildInfo` cacheable — a timestamped file would differ on every build and invalidate the cache — and
`build.version` defaults to the project version, single-sourced like the gateway's today.

### Expose `info` through the actuator exposure list

Spring Boot's `info` endpoint is enabled by default; only the `management.endpoints.web.exposure.include` list gates it.
Adding `info` to each service's list is the minimal change and matches how `health`/`prometheus` are already exposed.
The management port is already restricted by the chart's NetworkPolicy (reachable from the release, plus whatever
management-ingress peers a deployment configures — the local target adds `monitoring`, while the chart default and the
`ci` target set none), so exposing `info` widens no external surface.

### Home the behavior in `quality/releases`, not a new capability

`quality/releases`'s Purpose is "the single version declaration that the tag, **the build**, and the served OpenAPI
document share," and it already houses the gateway's runtime-served build version ("The OpenAPI document reports the
build version"). This change generalizes that thread to every JVM service and adds the runtime surface, so it is ADDED
requirements there rather than a new capability.

## Risks / Trade-offs

- **A stale `build-info.properties` in a cached build** → excluded `build.time` makes the file depend only on the
  version, so a version bump regenerates it and an unchanged version reuses it, as intended.
- **The info endpoint leaking more than the version** → the actuator `info` endpoint reports only the contributors it is
  configured with (the `build` contributor here); no `env`/`configprops` is exposed.
- **Moving the block changes the gateway's build** → it is the same block relocated; the gateway's OpenAPI version
  behavior is unchanged (its test still asserts the served version).

## Migration Plan

None — additive to the build and the management exposure.
