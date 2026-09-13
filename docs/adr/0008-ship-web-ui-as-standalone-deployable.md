# ADR-0008: Ship the web UI as a standalone deployable service

Date: 2026-09-08

Status: Accepted

## Context

The web UI is a React + Vite single-page app (Feature-Sliced Design) that browsers must load from somewhere, and it
talks to the API gateway cross-origin. The four JVM services each ship a container image built by Spring's
`bootBuildImage`. A static SPA fits neither Spring's image task nor the gateway's process: the gateway is a reactive
Java service, and serving compiled browser assets from it would couple the UI's release cycle and static-asset serving
to the gateway's runtime.

This ADR is recorded retrospectively (2026-09-13), when the architecture auditor flagged the decision as unrecorded.

## Decision

Ship the web UI as its own deployable service: a dedicated nginx container image (`aanbrn/axon-showcase-web-ui`) that
serves the built bundle, deployed as its own Helm Deployment/Service with the same external-exposure, availability, and
network-policy templates as the other services.

- The image is built with the Paketo buildpack pipeline (`pack` CLI) rather than a committed `Dockerfile`, for the same
  security posture as the JVM services (CVE-patched run images, SBOM, reproducible rebuilds). `frontend-conventions`
  registers a generic `dockerBuildImage` task that builds `build/dist` with the version-pinned Paketo NGINX + Procfile
  buildpacks; the NGINX buildpack generates the server configuration, so no Dockerfile is committed. The run image is
  deliberately left floating so base-OS patches keep flowing.
- The gateway's API base URL is **not baked into the image**: it is injected at container start from
  `SHOWCASE_API_BASE_URL` and rendered into `config.js`, failing fast if it is unset. Only the deployment knows the
  browser-reachable gateway URL (compose uses `http://localhost:8080`; the chart uses `webUi.apiBaseUrl`), so a baked
  default would be wrong in every environment but one.
- nginx exposes `stub_status` on a second port, and the Helm chart can run a gated `nginx-prometheus-exporter` sidecar
  so the UI's metrics join the other services' scrape targets.
- The gateway allows the UI's origin via `SHOWCASE_CORS_ALLOWED_ORIGINS` (fail-closed by default).

_Alternatives considered:_ a committed `nginx:alpine` Dockerfile — rejected (a hand-pinned base image and configuration
to patch, diverging from the buildpack-everywhere approach); serving the built assets from the API gateway — rejected
(couples the static UI's release cycle and asset serving to the gateway's runtime, and the gateway is not a static file
server); Spring's `bootBuildImage` registered manually for the frontend module — rejected (it is a Spring class
requiring a placeholder `archiveFile` even when `applicationDirectory` overrides it).

## Consequences

- The system gains a fifth deployable image and a fifth Helm Deployment. Every image-build enumeration (the compose
  build-first tasks and the app release's `installDependsOn`) must include the web UI's `dockerBuildImage` task, not
  only `bootBuildImage` — omitting one deploys a pod with an image that was never built.
- The UI and the gateway version independently, communicating only over the gateway's HTTP API, so their release cycles
  are decoupled.
- The browser-facing gateway URL is a runtime deployment input, and the UI's origin must be allowed by the gateway's
  CORS configuration in every environment.
- `pack` becomes a build prerequisite, like Helm and Snyk.
