## Context

The app Helm release in `build.gradle.kts` declares `installDependsOn` for the four JVM services' `bootBuildImage`
tasks, so `helmInstallToLocal` builds the JVM images before installing. The web-UI image
(`:showcase-web-ui:dockerBuildImage`, a `PackBuildImageTask` registered by `frontend-conventions`) is not in the list,
yet the chart's web-UI Deployment (from `ship-web-ui-as-deployable`) needs it. A fresh local deploy therefore deploys a
pod whose image was never built.

## Goals / Non-Goals

**Goals:**

- `helmInstallToLocal` builds all five service images (four JVM + web UI) before deploying.
- One-line fix in `build.gradle.kts`; no chart or code changes elsewhere.

**Non-Goals:**

- No change to the chart (the web-UI Deployment already exists and is correct).
- No change to the compose tasks (they already build the UI via `composeBuildAndUp`).

## Decisions

### D1: Add the web-UI dockerBuildImage to the app release's installDependsOn

In `build.gradle.kts`, extend the app release's `installDependsOn` list with `:showcase-web-ui:dockerBuildImage`:

```kotlin
installDependsOn(
    ":showcase-command-service:bootBuildImage",
    ":showcase-projection-service:bootBuildImage",
    ":showcase-query-service:bootBuildImage",
    ":showcase-api-gateway:bootBuildImage",
    ":showcase-web-ui:dockerBuildImage",
)
```

The web-UI image is built by the `PackBuildImageTask` (`dockerBuildImage`) from `frontend-conventions`, not a
`bootBuildImage` — so the task name differs from the JVM services. This is the only change needed.

### D2: Spec delta in the web-ui deployment capability

The web-ui deployment spec's "The UI is deployed by the Helm chart" requirement gains a "local deploy builds the web-UI
image" scenario, capturing that the local deploy task satisfies the chart's image dependency.

## Risks / Trade-offs

- **Slower helmInstallToLocal** → the first run now also builds the web-UI image (a `pack` build). Acceptable: the
  deploy already builds four JVM images; the UI build is a precondition the chart requires.
- **Task name coupling** → `:showcase-web-ui:dockerBuildImage` is the module's image task (not `bootBuildImage`); if the
  module's image task is ever renamed, this dependency needs updating (as with any `installDependsOn` entry).
