## Context

`showcase-web-ui` is a build-only module: `npmBuild` → `build/dist`, run via `viteDev`/preview, absent from
docker-compose and Helm. It is a standalone SPA (no client-side router — no history fallback needed) that talks to the
gateway over REST + SSE using `BASE = import.meta.env.VITE_API_BASE_URL ?? ''` (same-origin by default, overridable at
build time). The four JVM services build images via Spring Boot `bootBuildImage` (Paketo), named
`aanbrn/axon-showcase-<service>:${project.version}`, and the `docker-conventions` `composeBuildAndUp`/
`composeBuildAndRestart` tasks depend on all `bootBuildImage` tasks.

## Goals / Non-Goals

**Goals:**
- Ship the UI as a standalone static image (nginx serving `build/dist`), tagged
  `aanbrn/axon-showcase-web-ui:${version}`.
- Make the UI reachable in docker-compose and via a Helm Deployment/Service, with the gateway CORS allowing its origin.
- Keep the UI app unchanged — same `npmBuild` output, same `VITE_API_BASE_URL` mechanism.

**Non-Goals:**
- Not served by the API gateway (decided; standalone deployable).
- No client-side routing/history-fallback support (the SPA has no router today — a simple `try_files` fallback is
  harmless and future-proof, but no dedicated config for it).
- No TLS/ingress hardening beyond what the chart's existing ingress mechanism provides.
- No observability wiring (ServiceMonitor etc.) for the UI unless trivially covered by existing chart patterns.

## Decisions

### D1: nginx image via the Paketo `pack` CLI (buildpacks, non-Spring)

Use the Paketo buildpack pipeline for the same security benefits as the JVM services (CVE-patched run images, SBOM,
reproducible rebuilds), but driven by the **`pack` CLI** rather than Spring's `bootBuildImage` — the latter is
Spring-specific (wired to `bootJar` + the `java` plugin) and doesn't fit a pure frontend module. Following the repo's
convention/mechanism split: `frontend-conventions` registers a **generic** `dockerBuildImage` task (the `pack` build
mechanism — `pack build --path build/dist --builder paketobuildpacks/builder-jammy-base --buildpack
paketo-buildpacks/nginx --env BP_WEB_SERVER=nginx --env BP_WEB_SERVER_ROOT=/workspace`, `imagePlatform` passthrough
via `--platform`, depends on `npmBuild`), and each frontend module's `build.gradle.kts` configures its own `imageName`
(as `showcase-api-gateway/build.gradle.kts` sets `bootBuildImage`'s `imageName`). The NGINX buildpack auto-generates an
`nginx.conf` and serves the built bundle; no committed `Dockerfile`. The `pack` CLI becomes a build prerequisite
(like Helm/Snyk).
*Alternatives considered:* a committed `nginx:alpine` Dockerfile — rejected for security and consistency (hand-pinned
base image + config to patch/maintain, diverging from the buildpack-everywhere approach); Spring's `bootBuildImage`
task registered manually — rejected (it's a Spring class requiring a placeholder `archiveFile` even when
`applicationDirectory` overrides it, a hack that reuses a JVM-oriented task for a static site).

### D2: `docker-conventions` compose build-first tasks also depend on the web-UI image

`composeBuildAndUp`/`composeBuildAndRestart` currently depend on every `bootBuildImage` (the JVM services). The
web-UI image is built by the generic `dockerBuildImage` task (`frontend-conventions`), which is not a
`bootBuildImage` — so the `allprojects` `bootBuildImage` scan won't pick it up. Extend the build-first dependency to
also include the `frontend-conventions` `dockerBuildImage` task (all `bootBuildImage` + all `dockerBuildImage`).
`composeUp` (no build) stays as-is — it expects pre-built images.

### D3: docker-compose `web-ui` service and CORS

Add a `web-ui` service publishing the static site on `8084:80` (the next free host port after the four services'
8080/8081/8082/8083), image `aanbrn/axon-showcase-web-ui:${PROJECT_VERSION}`, no healthcheck (static). The gateway's
`SHOWCASE_CORS_ALLOWED_ORIGINS` in compose gains `http://localhost:8084`. The `VITE_API_BASE_URL` baked at
`npmBuild` time stays the default (same-origin `''`) for the compose image — the browser loads the UI from
`http://localhost:8084` and must reach the gateway at `http://localhost:8080`; because `BASE` is baked at build time,
the compose image is built with `VITE_API_BASE_URL=http://localhost:8080` (the gateway's published host port) so
REST/SSE work cross-origin.

### D4: Helm `webUi` values + Deployment/Service + gateway CORS

Mirror the existing per-service chart structure: a `webUi` values block (image, replicaCount, service port 80,
resources, ingress toggle), and `templates/web-ui/{deployment,service,ingress}.yaml` (no JGroups, no management port,
no HPA/VPA/PDB by default — static). The gateway's CORS env (`SHOWCASE_CORS_ALLOWED_ORIGINS`) in the chart adds the
UI's in-cluster origin (`http://<release>-web-ui:<port>`), driven by a `webUi` value so operators can override. The
deployed UI's `VITE_API_BASE_URL` is baked at image build time; chart values document that the image must be built
with the in-cluster gateway URL for a given environment.

### D5: `VITE_API_BASE_URL` is build-time, not runtime

Vite inlines `import.meta.env.*` at build. So the UI image is built per-target with the right base URL (compose →
`http://localhost:8080`; Helm → the in-cluster gateway service URL). This is a documented build-time parameter, not a
runtime env the chart can change post-build. The default (`''` same-origin) remains for dev-server/proxy use.

## Risks / Trade-offs

- **Build-time base URL** → A single image cannot serve both compose and Helm targets. Mitigation: document that the
  UI image is built per environment (like the JVM images already carry `BPE_DEFAULT_*` defaults); the chart's
  `webUi.image` values make the tag/env explicit.
- **Paketo-generated nginx config** → The auto-generated `nginx.conf` (default root `public`; we set
  `BP_WEB_SERVER_ROOT`) differs from `vite preview`. Mitigation: point `BP_WEB_SERVER_ROOT` at the built bundle and
  verify the served page; the e2e suite still exercises the built bundle via Vite preview, so app behavior parity is
  covered. If SPA push-state routing is ever added, `BP_WEB_SERVER_ENABLE_PUSH_STATE=true` covers it without a
  committed config.
- **CORS origin mismatch** → If the UI origin isn't in the gateway allow-list, the browser blocks REST/SSE.
  Mitigation: single-source the origin in compose values and chart values; the gateway CORS spec already covers the
  fail-closed default.
- **Port collision** → `8084` is free today; a conflicting local port would break compose. Mitigation: document the
  port; it is the next free slot after the four service ports.