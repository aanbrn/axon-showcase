# Tasks

## 1. Build identity in one place

- [x] 1.1 Move the `springBoot { buildInfo { excludes.set(listOf("time")) } }` block into
      `build-logic/src/main/kotlin/spring-boot-conventions.gradle.kts` and remove the gateway's inline copy from
      `showcase-api-gateway/build.gradle.kts`; verify each of `./gradlew :showcase-command-service:bootBuildInfo`,
      `:showcase-query-service:bootBuildInfo`, `:showcase-projection-service:bootBuildInfo`, and
      `:showcase-api-gateway:bootBuildInfo` writes a `build-info.properties` carrying the version and no `build.time`.
- [x] 1.2 Assert the baked build identity in each JVM service's integration test — the `BuildProperties` bean reports
      the project version and no build time — since the convention plugin's Gradle DSL is not unit-testable without
      TestKit; run each module's `integrationTest`.

## 2. Runtime exposure and its test

- [x] 2.1 Add `info` to `management.endpoints.web.exposure.include` in the `application.yml` of all four JVM services
      (`showcase-command-service`, `showcase-query-service`, `showcase-projection-service`, `showcase-api-gateway`).
- [x] 2.2 Add a per-service test asserting the runtime surface: the gateway (a real server) and query-service (its mock
      web client) request `/actuator/info` and assert it reports the version; the command and projection services, whose
      ITs boot with no web server, assert the `info` endpoint is exposed (`management.endpoints.web.exposure.include`
      contains `info`). Run each module's `integrationTest` and confirm the new cases pass.

## 3. Documentation

- [x] 3.1 Review `docs/adr/0016-releases-and-versioning.md` and conclude explicitly whether it needs an edit, a new ADR
      is owed, or no change — its Decision is the release process and version scheme, not a claim that the gateway is
      the only service with build info. Conclusion: **no change** — its Decision (the release process/version scheme,
      and the gateway's OpenAPI build version) is neither altered nor a landed follow-on, and it does not claim the
      gateway is the only service with build info, so no ADR edit and no new ADR are owed.
- [x] 3.2 Check `AGENTS.md` and `README.md`: whether the gateway's build info is described as gateway-only (widen it),
      and whether the runtime `/actuator/info` access path belongs in the README's observability section (surface the
      human-visible capability, or record why not). Done: `AGENTS.md`'s only build-info mention is the general
      `buildInfo()` mechanics rule (not gateway-only), so it needs no change; the README's observability section gains a
      "Build version" bullet naming the `/actuator/info` access path.
- [x] 3.3 Refresh the `showcase/quality/releases` capability `## Purpose` in
      `openspec/specs/showcase/quality/releases/spec.md` to name the runtime build-identity surface — its enumeration
      ("the tag, the build, and the served OpenAPI document") gains the actuator `info` endpoint. A delta cannot carry a
      Purpose, so the edit lands in the archive commit; record the deferral in the change report.
- [x] 3.4 Remove the `Identify a running service's build at runtime` idea from `docs/ideas.md`, since this change
      implements it.
- [x] 3.5 Run `./gradlew spotlessApply` after the final edit to any formatter-owned file.

## 4. Verification

- [x] 4.1 Run the four service checks and `spotlessCheck` and confirm green:
      `./gradlew :showcase-command-service:check :showcase-query-service:check` and
      `:showcase-projection-service:check :showcase-api-gateway:check spotlessCheck`; confirm `openspec validate --all`
      passes.
- [x] 4.2 Run the `lesson-capture` subagent over this unit's diff, review findings, and change dir; apply the durable
      proposals to `AGENTS.md` and record the applied net `AGENTS.md` delta on this task. Applied one addition (a
      `build-logic` convention plugin's effect is verified through a consuming module, since `build-logic` has no
      TestKit). Net `AGENTS.md` delta: **+5 lines** (5 insertions, 0 deletions).
