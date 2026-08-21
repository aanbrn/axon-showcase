## Why

The project docs claim the Helm prerequisite is "Helm 3.x", but that version belongs to the Gradle
helm **plugin** (`helm-plugin = "3.1.2"`), not the Helm CLI. The Helm CLI the project actually uses
is 4.x: `gradle/libs.versions.toml` pins `helm = "4.2.3"` (downloaded by
`helm-conventions.gradle.kts` `downloadClient.version`), and the locally installed CLI is `v4.2.4`.
The stale "Helm 3.x" references in `README.md` and `AGENTS.md` are therefore misleading.

Separately, the pinned CLI version `4.2.3` lags the installed `4.2.4`.

## What Changes

### `gradle/libs.versions.toml`

- Bump `helm` from `4.2.3` to `4.2.4` (the Helm CLI distribution the build downloads), aligning it
  with the locally installed CLI.

### `README.md`

- Update the "Helm 3.x" prerequisite (line 74) to "Helm 4.x".

### `AGENTS.md`

- Update the "Helm 3.x" prerequisite (line 31) to "Helm 4.x".

## New Capabilities

- None.

## Modified Capabilities

- None (doc corrections + version bump; no behavioral change).

## Impact

- **Build**: the `helm` downloadClient version bumps from 4.2.3 to 4.2.4; Helm tooling in the build
  and `helmInstallToLocal` now uses 4.2.4.
- **Tests**: no test changes.
- **Deployment**: no impact beyond the CLI version bump.
