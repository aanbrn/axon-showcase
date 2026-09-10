# Proposal: Build the web-UI image in helmInstallToLocal

## Why

`./gradlew helmInstallToLocal` deploys the full stack to the local cluster, but its app-release task depends only on the
four JVM `bootBuildImage` tasks — it never builds the **web-UI image** (`:showcase-web-ui:dockerBuildImage`). Since the
Helm chart ships a web-UI Deployment (added in `ship-web-ui-as-deployable`), a fresh `helmInstallToLocal` would try to
pull an image that was never built, and the web-UI pod fails. The compose tasks build it (via `composeBuildAndUp`), but
the Helm path misses it.

## What Changes

- Add `:showcase-web-ui:dockerBuildImage` to the app release's `installDependsOn` in `build.gradle.kts`, so
  `helmInstallToLocal` builds all five service images (four JVM + the web UI) before deploying.
- No chart change — the web-UI Deployment already exists; this fixes the local-deploy task to satisfy its image
  dependency.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `showcase/deployment/web-ui` — the "The UI is deployed by the Helm chart" requirement gains a scenario: the local
  deploy task (`helmInstallToLocal`) builds the web-UI image before installing, so the deployed pod has an image.

## Impact

- **Build**: `build.gradle.kts` app-release `installDependsOn` gains `:showcase-web-ui:dockerBuildImage`.
- **Docs**: `AGENTS.md` — update the per-release-tasks note ("additionally depends on the four `bootBuildImage` tasks")
  to include the web-UI `dockerBuildImage`.
- **Behavior**: `helmInstallToLocal` now builds the web-UI image before deploying; a fresh local deploy works end to
  end.
