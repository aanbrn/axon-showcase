# Proposal: Refactor the Docker compose conventions into a native Gradle convention

## Why

`docker-conventions.gradle.kts` shells out to `docker compose` through `/bin/sh -c` (or `cmd.exe /c` on Windows) with
six near-identical `Exec` task registrations that each repeat the descriptions, environment variables, `workingDir`,
and lock wiring. It also fails opaquely (`docker: command not found`) when the Docker CLI is missing. Mirroring the
Snyk integration's CLI-presence guard (`dependency-security-conventions.gradle.kts`), the convention should use a
structured command line and fail with a clear `GradleException` when `docker` is not on `PATH`.

## What Changes

- Refactor the six `compose*` task registrations into a single `registerComposeTask(taskName, action, systemDescription,
  serviceDescription, buildFirst)` helper that sets the group, description, `dependsOn` (`bootBuildImage` when
  `buildFirst`), structured `commandLine("docker", "compose", ...)`, `workingDir`, environment variables, lock, and
  task-gating `onlyIf` once.
- Replace the `/bin/sh -c` and Windows `cmd.exe /c` indirection with a structured argument list, so Gradle spawns
  `docker compose ...` directly (resolves `docker` on `PATH`; no shell, no quoting bugs, no Windows branch).
- Add a `doFirst` guard (mirroring the Snyk pattern) that throws a `GradleException` with a helpful message when the
  `docker` executable is not found on `PATH` — reusing `SystemUtils` for the Windows executable name, as
  `dependency-security-conventions.gradle.kts` does.
- Preserve task names, descriptions, `buildFirst` semantics, the shared lock, and the `onlyIf` task-gating behavior.
- No behavior change to the `compose*` tasks; no change to `docker-compose.yml`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — `skip_specs: true`. A build-tooling refactor with no externally observable behavior change.

## Impact

- **Code (build-logic only)**: `build-logic/src/main/kotlin/docker-conventions.gradle.kts`.
- **Docs**: `AGENTS.md` / `README.md` (the `compose*` task names and usage are unchanged, so likely no doc change).
- **Build**: `compose*` tasks behave identically; verified via task listing and a dry inspection.
- **Tests**: none; verified by compiling `build-logic` and listing the `compose*` tasks.