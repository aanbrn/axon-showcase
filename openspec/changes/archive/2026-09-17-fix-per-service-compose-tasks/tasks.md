## 1. Map modules to compose services

- [x] 1.1 Add `ComposeServices.kt` to `build-logic`: the module-to-service mapping and `composeServiceFor`, with a unit
      test pinning each service module and the non-service modules that map to nothing.
- [x] 1.2 Use the mapping in `docker-conventions` — derive the service from it instead of `project.name`, register the
      per-service tasks only for modules that have one, and name the service in the task descriptions.

## 2. Verify

- [x] 2.1 The reported failure is gone: `./gradlew :showcase-api-gateway:composeStop` succeeds (it failed with
      `no such service: showcase-api-gateway` before).
- [x] 2.2 The task addresses the right service, not just any service: `./gradlew :showcase-api-gateway:composeUp` starts
      the `api-gateway` container together with its Compose dependency graph (`db-events`, `kafka`, `os-views`,
      `command-service`, `query-service`), and `:showcase-api-gateway:composeDown` stops and removes that container
      while leaving the dependencies up.
- [x] 2.3 The check is not vacuous: `docker compose stop showcase-api-gateway` — the name the task used to pass — still
      fails with `no such service`.
- [x] 2.4 The root tasks are untouched: `./gradlew :composeStop` and `./gradlew :composeDown` succeed, the latter
      leaving no containers (the prior state, since nothing was running before).
- [x] 2.5 `./gradlew :build-logic:test` passes; `openspec validate --all` passes.

## 3. Docs

- [x] 3.1 Describe the per-service form where the compose tasks are documented (`AGENTS.md`, `README.md`).
- [x] 3.2 Remove the implemented idea from `docs/ideas.md`.
