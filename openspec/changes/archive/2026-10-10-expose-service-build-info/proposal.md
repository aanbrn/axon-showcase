# Proposal

## Why

The gateway bakes Spring Boot build info (`META-INF/build-info.properties`) so its served OpenAPI document reports the
build version, but the command, query, and projection services bake none, and no JVM service exposes `/actuator/info`
(each `application.yml` exposes only `health` and `prometheus`). A running deployment therefore cannot report which
build a pod is running without inspecting its image tag.

## What Changes

- **Build logic**: move the `springBoot { buildInfo { excludes.set(listOf("time")) } }` block into
  `build-logic/src/main/kotlin/spring-boot-conventions.gradle.kts` (applied by exactly the four JVM services), removing
  the gateway's inline copy — so every JVM service bakes its build identity once, with the build time excluded so
  `bootBuildInfo` stays cacheable.
- **Actuator exposure**: add `info` to `management.endpoints.web.exposure.include` in all four JVM services'
  `application.yml`, so each exposes `/actuator/info` on its management port.
- **Spec**: ADDED requirements under `showcase/quality/releases`, plus a task to refresh its `## Purpose` in the archive
  commit (it enumerates the version-sharing surfaces, which this change extends with the runtime one).
- **Tests**: a per-service integration test asserting the baked build identity (the `BuildProperties` bean reports the
  version and no build time), and — for the gateway and query-service, which boot a web client — a `/actuator/info`
  request asserting the version; the command and projection services boot with no web server, so they assert the `info`
  endpoint is exposed instead.
- **Docs**: review ADR-0016, check `AGENTS.md`/`README.md`, and remove the idea from `docs/ideas.md`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/releases`: ADDED requirements — every JVM service bakes its build identity and reports it at runtime
  via the actuator `info` endpoint.

## Impact

- `build-logic`'s `spring-boot-conventions` plugin and the gateway's `build.gradle.kts`; four `application.yml` files;
  new service integration tests; the `quality/releases` spec; `docs/adr/0016-releases-and-versioning.md`, `AGENTS.md`,
  `README.md`, `docs/ideas.md`.
- No change to any existing endpoint; the management port is already probed and scraped. The web UI is a static nginx
  image with no actuator, so it is out of scope (its identity is its image tag).
