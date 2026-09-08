## 1. Build the web-UI image

- [x] 1.1 In `frontend-conventions`, register a generic `dockerBuildImage` task (typed `PackBuildImageTask`): runs the
      `pack` CLI (`pack build --path build/dist --builder paketobuildpacks/builder-jammy-base --buildpack
      paketo-buildpacks/nginx --buildpack paketo-buildpacks/procfile --env BP_WEB_SERVER=nginx
      --env BP_WEB_SERVER_ROOT=/workspace --env BP_NGINX_STUB_STATUS_PORT=9090`), honoring `imagePlatform`
      (`--platform`) like the JVM services. It depends on `npmBuild` and the image name defaults to
      `${project.name}:${project.version}`, overridable by the module — mirroring how `bootBuildImage`'s `imageName`
      is module-owned. Group under `build`.
- [x] 1.2 In `showcase-web-ui/build.gradle.kts`, override the image name for the generic task to
      `aanbrn/axon-showcase-web-ui:${project.version}`.
- [x] 1.3 In `showcase-web-ui/src/shared/api.ts`, read the base URL at runtime from `window.__API_BASE_URL__` first,
      falling back to `import.meta.env.VITE_API_BASE_URL` and then `''`. Add a `Procfile` (`web: ./start.sh`) and a
      `start.sh` that reads the `SHOWCASE_API_BASE_URL` env var (failing fast with a non-zero exit if it is
      unset/empty), renders `config.js` into the served root
      (`/workspace/config.js`, matching `BP_WEB_SERVER_ROOT=/workspace`), then `exec`s the nginx command the Paketo
      nginx buildpack uses (`nginx -p /workspace -c /workspace/nginx.conf -g "pid /tmp/nginx.pid;"`).
- [x] 1.4 Configure the `dockerBuildImage` task with `buildpacks` including `paketo-buildpacks/procfile` (the nginx
      buildpack alone does not include it), so `start.sh` runs before nginx. The image bakes **no** default base URL:
      the browser needs the externally-visible gateway URL, which only the deployment knows, so deployments provide
      `SHOWCASE_API_BASE_URL` explicitly (compose: `http://localhost:8080`; the chart: `webUi.apiBaseUrl`, empty
      default = same-origin; the start script fails fast if unset) —
      matching the gateway's `SHOWCASE_*` naming.
- [x] 1.5 Run `./gradlew :showcase-web-ui:dockerBuildImage` and verify the image builds and serves the bundle when run
      (curl the container's port: `/config.js` is served, and the served `index.html` includes a
      `<script src="/config.js">` tag before the app bundle, so the browser sets `window.__API_BASE_URL__` before the
      app boots).
- [x] 1.6 In `docker-conventions`, extend the build-first compose tasks (`composeBuildAndUp`/`composeBuildAndRestart`)
      to also depend on the generic `dockerBuildImage` task (all `bootBuildImage` + all `dockerBuildImage`).

## 2. Wire the compose stack

- [x] 2.1 Add a `web-ui` service to `docker-compose.yml`: image `aanbrn/axon-showcase-web-ui:${PROJECT_VERSION}`,
      host port `8084:8080`, no healthcheck, with `SHOWCASE_API_BASE_URL: http://localhost:8080` (the browser-reachable
      gateway URL).
- [x] 2.2 Add `http://localhost:8084` to the gateway's `SHOWCASE_CORS_ALLOWED_ORIGINS` in docker-compose.
- [x] 2.3 Build the UI image and run `composeUp`; verify the UI loads at `http://localhost:8084` and can call the
      gateway (list shows showcases, SSE events stream). Crucially, verify a **browser** flow from `:8084` (not a
      direct gateway curl): the served `config.js` sets `window.__API_BASE_URL__` and a create-showcase POST from the
      UI reaches the gateway (status 201), not the web-ui's own nginx (which would 404 on `/showcases`).

## 3. Add the Helm deployment

- [x] 3.1 Add a `webUi` values section to `helm/chart/src/main/helm/values.yaml` (image registry/repository/tag,
      replicaCount, service port 8080, resources, `containerPorts` incl. the stub-status port, readiness probe,
      pod/container securityContexts, `autoscaling` block with `hpa`/`vpa`, `pdb` block, `networkPolicy` block,
      `ingress` block, `route` HTTPRoute block), following the `apiGateway` pattern.
- [x] 3.2 Add `templates/web-ui/deployment.yaml`, `service.yaml`, `ingress.yaml`, `route.yaml` (HTTPRoute),
      `hpa.yaml`, `vpa.yaml`, `pdb.yaml`, and `networkpolicy.yaml`, mirroring the `apiGateway` templates but for the
      static nginx container (port 8080, no JGroups/management port, no `JAVA_OPTS`); the `ingress`/`route`/
      `autoscaling`/`pdb`/`networkPolicy` values blocks are disabled by default like the gateway's, and the
      NetworkPolicy allows public HTTP ingress, monitoring-only access to the metrics port, and DNS + same-namespace
      egress. The Deployment follows the shared service conventions: common `serviceAccountName` +
      `automountServiceAccountToken`, `emptyDir` at `/tmp`, pod/container securityContexts, and a readiness probe;
      the Service declares named ports `http` (UI) and `http-metrics` (stub-status) for the ServiceMonitor to target.
- [x] 3.3 Update the gateway CORS env in the chart (`SHOWCASE_CORS_ALLOWED_ORIGINS`) to include the web-UI origin,
      driven by a `webUi` value, and add a `webUi` `SHOWCASE_API_BASE_URL` env var to the Deployment — set from
      `webUi.apiBaseUrl` (the externally-visible gateway URL; the in-cluster Service name is unresolvable by the
      browser), empty default = same-origin, not computed — no ConfigMap/mount.
- [x] 3.4 In `helm/values/axon-showcase/values-local.yaml`, expose the web UI in the local stack (`webUi.ingress`
      with hostname `axon-showcase-ui`), rename the gateway ingress hostname `axon-showcase` → `axon-showcase-api`,
      and set the local gateway CORS origin to the UI's local host; deploy the full stack
      (`./gradlew helmInstallToLocal`) and verify the **browser flow** through the ingress: the served UI loads
      `config.js` (setting `window.__API_BASE_URL__` to the externally-visible `http://axon-showcase-api`), and a
      create-showcase POST from the UI origin reaches the gateway via the ingress (status 201), with CORS
      `Access-Control-Allow-Origin` matching the UI host.
- [x] 3.5 Add a `webUi` `serviceMonitor` block to values and a `templates/web-ui/servicemonitor.yaml` (mirroring the
      api-gateway's) that scrapes the UI Service on the `http-metrics` port (`path: /metrics`), so nginx server
      metrics appear in the Prometheus/Grafana stack; the stub-status → Prometheus conversion is left configurable
      via the `webUi.sidecars` value (an exporter sidecar), and the web-UI NetworkPolicy lets the `monitoring`
      namespace reach the metrics port.
- [x] 3.6 Update the Helm lint value files (`helm/chart/src/test/helm/helm-lint-full.yaml`,
      `helm-lint-minimal.yaml`) to cover the new `webUi` template branches.
- [x] 3.7 Run `./gradlew :helm:chart:helmLintMainChartFull :helm:chart:helmLintMainChartMinimal` and confirm lint
      passes; render the chart with `helm template` and confirm the web-UI Deployment/Service/ingress/route/hpa/vpa/
      pdb/networkpolicy/servicemonitor render.

## 4. Docs and verify

- [x] 4.1 Add `setup-hosts.sh` (detects the local cluster's ingress-controller LoadBalancer address generically — IP
      or hostname, across namespaces, against the current kube context — and manages the `/etc/hosts` entries for
      `axon-showcase-api` / `axon-showcase-ui`), and update `AGENTS.md` / `README.md`: the web-UI image name, the
      `pack` CLI prerequisite, the `webUi` chart values, the compose `web-ui` service and its `8084` port, the
      `SHOWCASE_API_BASE_URL` runtime env-var mechanism (rendered to `config.js` at container start, no
      baked default), and the `./setup-hosts.sh setup` usage for direct hostname access instead of
      the `Host:`-header workaround.
- [x] 4.2 Run `openspec validate --all` and the module checks (`./gradlew :showcase-web-ui:check`), confirm the
      change passes.