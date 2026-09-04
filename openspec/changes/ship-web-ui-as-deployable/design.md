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

### D1: nginx image via a committed `Dockerfile`, built by a Gradle task

A static SPA is not a Paketo/Spring Boot image. Use a committed `showcase-web-ui/Dockerfile` based on `nginx:alpine`,
with `COPY build/dist /usr/share/nginx/html` and a minimal `nginx.conf` (`try_files $uri /index.html;` for
future-proof SPA fallback, and `gzip` on for the JS bundle). A new task in `frontend-conventions` (e.g.
`dockerBuildWebUiImage`) runs `docker build` with `-t aanbrn/axon-showcase-web-ui:${project.version}`, depends on
`npmBuild`, and honors the `imagePlatform` property (`--platform`) like the JVM services.
*Alternative considered:* Paketo static-site buildpack — rejected, it adds buildpack machinery to a module that is not
a JVM app and complicates the image-name/digest plumbing already done via `bootBuildImage`.

### D2: `docker-conventions` compose build-first tasks also depend on the web-UI image

`composeBuildAndUp`/`composeBuildAndRestart` currently depend on every `bootBuildImage`. Extend the dependency to
also include the web-UI image task (all `bootBuildImage` + the `frontend-conventions` web-UI image task), so the
UI image is built before the compose stack starts. `composeUp` (no build) stays as-is — it expects pre-built images.

### D3: docker-compose `web-ui` service and CORS

Add a `web-ui` service publishing the static site on `8084:80` (the next free host port after the four services'
8080/8081/8082/8083), image `aanbrn/axon-showcase-web-ui:${PROJECT_VERSION}`, no healthcheck (static). Build-time
`VITE_API_BASE_URL` for compose is set to `http://localhost:8080` (the gateway's published host port) so the browser
reaches the gateway cross-origin; the gateway's `SHOWCASE_CORS_ALLOWED_ORIGINS` in compose gains
`http://localhost:8084`.

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
- **nginx config drift** → A committed `Dockerfile`/`nginx.conf` can drift from the Vite preview behavior.
  Mitigation: keep the nginx config minimal and matching `vite preview` (serve `dist`, gzip); the e2e suite still
  exercises the built bundle via Vite preview, so visual parity is covered.
- **CORS origin mismatch** → If the UI origin isn't in the gateway allow-list, the browser blocks REST/SSE.
  Mitigation: single-source the origin in compose values and chart values; the gateway CORS spec already covers the
  fail-closed default.
- **Port collision** → `8084` is free today; a conflicting local port would break compose. Mitigation: document the
  port; it is the next free slot after the four service ports.