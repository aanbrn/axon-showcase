## Context

`showcase-web-ui` is a build-only module: `npmBuild` → `build/dist`, run via `viteDev`/preview, absent from
docker-compose and Helm. It is a standalone SPA (no client-side router — no history fallback needed) that talks to the
gateway over REST + SSE using `BASE = window.__API_BASE_URL__ ?? import.meta.env.VITE_API_BASE_URL ?? ''`
(same-origin by default, overridable at build time and at runtime via the served `config.js`). The four JVM services
build images via Spring Boot `bootBuildImage` (Paketo), named
`aanbrn/axon-showcase-<service>:${project.version}`, and the `docker-conventions` `composeBuildAndUp`/
`composeBuildAndRestart` tasks depend on all `bootBuildImage` tasks.

## Goals / Non-Goals

**Goals:**
- Ship the UI as a standalone static image (nginx serving `build/dist`), tagged
  `aanbrn/axon-showcase-web-ui:${version}`.
- Make the UI reachable in docker-compose and via a Helm Deployment/Service, with the gateway CORS allowing its origin.
- Keep the UI app's features and bundle unchanged — same `npmBuild` output; the only app change is reading the base
  URL at runtime (`window.__API_BASE_URL__`, defaulting to `VITE_API_BASE_URL`) instead of relying on the build-time
  value alone.

**Non-Goals:**
- Not served by the API gateway (decided; standalone deployable).
- No client-side routing/history-fallback support (the SPA has no router today — a simple `try_files` fallback is
  harmless and future-proof, but no dedicated config for it).
- No TLS/ingress hardening beyond what the chart's existing ingress mechanism provides.
- No client-side (RUM) observability — web vitals, JS errors, and browser-side trace propagation are a separate UI
  change (recorded as an idea); server-side nginx metrics via stub_status + ServiceMonitor are included.

## Decisions

### D1: nginx image via the Paketo `pack` CLI (buildpacks, non-Spring)

Use the Paketo buildpack pipeline for the same security benefits as the JVM services (CVE-patched run images, SBOM,
reproducible rebuilds), but driven by the **`pack` CLI** rather than Spring's `bootBuildImage` — the latter is
Spring-specific (wired to `bootJar` + the `java` plugin) and doesn't fit a pure frontend module. Following the repo's
convention/mechanism split: `frontend-conventions` registers a **generic** `dockerBuildImage` task (the `pack` build
mechanism — `pack build --path build/dist --builder paketobuildpacks/builder-jammy-base --buildpack
paketo-buildpacks/nginx --buildpack paketo-buildpacks/procfile --env BP_WEB_SERVER=nginx --env
BP_WEB_SERVER_ROOT=/workspace`, `imagePlatform` passthrough via `--platform`, depends on `npmBuild`), and each
frontend module's `build.gradle.kts` configures its own `imageName`
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

Add a `web-ui` service publishing the static site on `8084:8080` (the next free host port after the four services'
8080/8081/8082/8083), image `aanbrn/axon-showcase-web-ui:${PROJECT_VERSION}`, no healthcheck (static). The gateway's
`SHOWCASE_CORS_ALLOWED_ORIGINS` in compose gains `http://localhost:8084`. The compose service explicitly sets the
`SHOWCASE_API_BASE_URL` env var to `http://localhost:8080` (the gateway's published host port) — the browser-reachable
gateway URL, which the image does not bake (compose needs host access to the gateway). The browser loads the UI
from `http://localhost:8084` and reaches the gateway cross-origin via the runtime-rendered `config.js`.

### D4: Helm `webUi` values + Deployment/Service + ingress/route + gateway CORS

Mirror the existing per-service chart structure, following the `apiGateway` pattern exactly: a `webUi` values block
(image, replicaCount, service port 8080, resources, autoscaling, pdb, networkPolicy), a
`templates/web-ui/deployment.yaml`
+ `service.yaml` (no JGroups, no management port), **and the external-exposure, network, and availability templates the
api-gateway provides: `templates/web-ui/ingress.yaml`, `route.yaml`, `hpa.yaml`, `vpa.yaml`, `pdb.yaml`,
`networkpolicy.yaml`** (an `ingress` block, a `route` HTTPRoute block, an `autoscaling` block with `hpa`/`vpa`, a
`pdb` block, and a `networkPolicy` block in `webUi` values — all disabled by default like the gateway's). The web UI
is a static server but can still need horizontal scaling under traffic spikes, so it ships the same HPA/VPA/PDB
templates as every other service, defaulted off. The Deployment follows the shared service conventions: references
the common `serviceAccountName` (with `automountServiceAccountToken`), mounts an `emptyDir` at `/tmp` (nginx writes
its pid to `/tmp/nginx.pid`), sets pod/container securityContexts, and defines a readiness probe (nginx serving `/`).
The web-UI Service declares named ports — `http` (the UI) and `http-metrics` (the stub-status port) — so the
ServiceMonitor can target the metrics port by name. The gateway's CORS env (`SHOWCASE_CORS_ALLOWED_ORIGINS`) in the
chart adds the UI's in-cluster origin (`http://<release>-web-ui:<port>`), driven by a `webUi` value so operators can
override. The deployed UI's `SHOWCASE_API_BASE_URL` env var is set from `webUi.apiBaseUrl` — the
externally-visible gateway URL, since the in-cluster Service name is unresolvable from the browser — and is rendered
to `config.js` at container start; the chart never bakes a per-environment base URL into the image.

### D4c: Web-UI NetworkPolicy

Add a `webUi.networkPolicy` block (mirroring the api-gateway's) and a `templates/web-ui/networkpolicy.yaml`. For the
static nginx serving a public UI, the policy is: **ingress** allows HTTP on port 8080 from anywhere (the public UI via
Ingress/HTTPRoute) and the stub-status/metrics port only from monitoring peers (the `monitoring` namespace, matching
how the gateway's management port is gated); **egress** allows DNS and same-namespace traffic only (the static UI
makes no outbound calls — the browser talks to the gateway, not the pod).

### D4a: Local target exposure of the web UI

The local values (`helm/values/axon-showcase/values-local.yaml`) currently expose the api-gateway via ingress
(`apiGateway.ingress.enabled: true`, hostname `axon-showcase`). With the web UI deployed as a standalone unit, the
local stack must also make the UI reachable. Two coherent options:
- **UI via its own ingress** (preferred — mirrors the gateway): `webUi.ingress.enabled: true` with a sibling hostname
  (e.g. `axon-showcase-ui`), so the UI is reached at its own host like the gateway is; the gateway CORS origin for
  local then uses that host. This requires the same local hostname resolution as the gateway's ingress already needs.
- **UI via the gateway's ingress path**: add the UI as an extra path on the gateway ingress — rejected, it couples the
  two and contradicts the standalone-deployable decision.
The design adds a `webUi` ingress to the local values, with the UI origin used in the gateway CORS.

### D4b: Local hostname naming and resolution

With two public surfaces, the local ingress hostnames should be unambiguous. Rename the gateway's local ingress
hostname from `axon-showcase` to `axon-showcase-api` (a sibling pair: `axon-showcase-api` for the gateway,
`axon-showcase-ui` for the web UI). The current `axon-showcase` is a virtual host on Traefik (the local ingress
controller, a colima LoadBalancer), not a real DNS name — it is referenced only in
`helm/values/axon-showcase/values-local.yaml`, so the rename is contained and zero-risk.

Local access today uses `curl -H "Host: axon-showcase" ...` because `/etc/hosts` has no entry for the hostname. For
convenience, add a `setup-hosts.sh` helper that detects the local cluster's ingress-controller LoadBalancer address
generically — against the current kube context, finding LoadBalancer Services across namespaces and accepting an IP or
hostname (so it works on colima + Traefik, kind/minikube + ingress-nginx, etc.) — and manages the `/etc/hosts` entries
for both hostnames idempotently (`./setup-hosts.sh setup` / `remove`), so curl/httpie/browser can use
`http://axon-showcase-api/...` and `http://axon-showcase-ui/...` directly — no Host-header workaround. The address can
change on cluster restart; re-running `setup` refreshes the entries. This is also what makes the deployed UI usable
from a browser (the browser needs `http://axon-showcase-ui` to resolve).

### D5: `SHOWCASE_API_BASE_URL` is a runtime env var (SPA rendered at container start)

Vite inlines `import.meta.env.*` at build, so the base URL cannot be a plain runtime env the browser reads directly —
but the project's convention is env-var configuration, not ConfigMaps/mounts, and the gateway already names its
downstream URLs `SHOWCASE_*` (`SHOWCASE_QUERY_SERVICE_URL` for `showcase.query.api-url`). The image bakes **no**
default for the base URL: the browser needs the externally-visible gateway URL, which only the deployment knows (the
in-cluster Service name is unresolvable from the browser), so `SHOWCASE_API_BASE_URL` is configured per deployment.
The container renders `config.js` from the `SHOWCASE_API_BASE_URL` env var at start, and **fails fast if it is
unset/empty** (`start.sh` exits non-zero, so the container is restarted until configured) rather than serve a UI
silently calling the wrong origin. The UI reads `window.__API_BASE_URL__` at runtime, falling back to the build-time
`import.meta.env.VITE_API_BASE_URL`, then `''`. Operators configure the deployment with a plain `SHOWCASE_API_BASE_URL`
env var (no ConfigMap/mount). The browser still consumes the served `config.js` (a static SPA cannot read container
env), but the configuration surface is a runtime env var following the gateway's `SHOWCASE_*` naming.

**Concrete wiring (verified against the Paketo nginx + Procfile buildpacks):** the nginx buildpack sets the default
`web` process to `nginx -p /workspace -c /workspace/nginx.conf -g "pid /tmp/nginx.pid;"` (workingDir + the generated
`nginx.conf`). To run `start.sh` first: add `paketo-buildpacks/procfile` (the nginx buildpack alone does not include
it) to the buildpacks list, ship a `Procfile` with `web: ./start.sh` (overriding the default `web` process), and have
`start.sh` render `config.js` from the `SHOWCASE_API_BASE_URL` into the served root (`/workspace/config.js`,
matching `BP_WEB_SERVER_ROOT=/workspace`), then `exec` the same nginx command (nginx is on PATH from the buildpack),
keeping nginx as PID 1.

### D6: Server-side nginx metrics via stub_status + ServiceMonitor

The UI container's nginx can expose basic server metrics via the Paketo nginx buildpack's
`BP_NGINX_STUB_STATUS_PORT` (the `ngx_http_stub_status_module`, `/stub_status` on a dedicated port: active/accepted/
handled connections, reading/writing/waiting). Add a `webUi` `serviceMonitor` block (mirroring the api-gateway's) and
a `templates/web-ui/servicemonitor.yaml` that scrapes the UI Service on the `http-metrics` port, so nginx liveness and
traffic are visible in the existing Prometheus/Grafana stack. The stub_status endpoint is plaintext (not Prometheus
format), so the ServiceMonitor scrapes `/metrics` and the conversion is left configurable via the `webUi.sidecars`
value (an NGINX Prometheus Exporter sidecar) — the chart does not pin a specific converter. The stub-status/metrics
port is gated by the web-UI NetworkPolicy to monitoring peers (D4c). This is server-side nginx observability only —
client-side (RUM: web vitals, JS errors, trace propagation) is explicitly out of scope (see Non-Goals).

## Risks / Trade-offs

- **Runtime base URL via env-rendered `config.js`** → The container must render `config.js` from
  `SHOWCASE_API_BASE_URL` at start (a `start.sh` before nginx), and a missing/empty value is a deployment error.
  Mitigation: `start.sh` fails fast (exits non-zero when the env var is unset/empty, so the container is restarted
  until configured), and deployments set the env var explicitly — compose sets `http://localhost:8080`, the chart's
  `webUi.apiBaseUrl` (empty default = same-origin, like the gateway's CORS allow-list). The image bakes no default, so
  a deployment without configuration is visibly broken rather than silently mispointed. This is the same runtime
  env-var configuration surface as the JVM services (no ConfigMap/mount).
- **Paketo-generated nginx config** → The auto-generated `nginx.conf` (serving the bundle at `BP_WEB_SERVER_ROOT`)
  differs from `vite preview`. Mitigation: point `BP_WEB_SERVER_ROOT` at the built bundle and verify the served page;
  the e2e suite still exercises the built bundle via Vite preview, so app behavior parity is covered. If SPA push-state
  routing is ever added, `BP_WEB_SERVER_ENABLE_PUSH_STATE=true` covers it without a committed config.
- **CORS origin mismatch** → If the UI origin isn't in the gateway allow-list, the browser blocks REST/SSE.
  Mitigation: single-source the origin in compose values and chart values; the gateway CORS spec already covers the
  fail-closed default.
- **Port collision** → `8084` is free today; a conflicting local port would break compose. Mitigation: document the
  port; it is the next free slot after the four service ports.