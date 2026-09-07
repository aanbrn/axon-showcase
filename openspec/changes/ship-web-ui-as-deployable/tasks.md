## 1. Build the web-UI image

- [ ] 1.1 In `frontend-conventions`, register a generic `dockerBuildImage` Exec task: runs the `pack` CLI
      (`pack build --path build/dist --builder paketobuildpacks/builder-jammy-base --buildpack
      paketo-buildpacks/nginx --buildpack paketo-buildpacks/procfile --env BP_WEB_SERVER=nginx
      --env BP_WEB_SERVER_ROOT=/workspace --env BP_NGINX_STUB_STATUS_PORT=8080`), honoring `imagePlatform`
      (`--platform`) like the JVM services. It depends on `npmBuild` and reads the image name from the module
      (configurable), leaving the module's `build.gradle.kts` to set it — mirroring how `bootBuildImage`'s `imageName`
      is module-owned. Group under `build`.
- [ ] 1.2 In `showcase-web-ui/build.gradle.kts`, set the image name for the generic task to
      `aanbrn/axon-showcase-web-ui:${project.version}`.
- [ ] 1.3 In `showcase-web-ui/src/shared/api.ts`, read the base URL at runtime from `window.__API_BASE_URL__` first,
      falling back to `import.meta.env.VITE_API_BASE_URL` and then `''`. Add a `Procfile` (`web: ./start.sh`) and a
      `start.sh` that reads the `SHOWCASE_API_BASE_URL` env var, renders `config.js` into the served root
      (`/workspace/config.js`, matching `BP_WEB_SERVER_ROOT=/workspace`), then `exec`s the nginx command the Paketo
      nginx buildpack uses (`nginx -p /workspace -c /workspace/nginx.conf -g "pid /tmp/nginx.pid;"`).
- [ ] 1.4 Configure the `dockerBuildImage` task with `buildpacks` including `paketo-buildpacks/procfile` (the nginx
      buildpack alone does not include it) and `BPE_DEFAULT_SHOWCASE_API_BASE_URL` (the runtime env default, e.g.
      `http://localhost:8080` for compose), so `start.sh` has a baked default and deployments override via a plain
      `SHOWCASE_API_BASE_URL` env var — matching the gateway's `SHOWCASE_*` naming and the JVM services'
      `BPE_DEFAULT_*` pattern.
- [ ] 1.5 Run `./gradlew :showcase-web-ui:dockerBuildImage` and verify the image builds and serves the bundle when run
      (curl the container's port, including `/config.js`).
- [ ] 1.6 In `docker-conventions`, extend the build-first compose tasks (`composeBuildAndUp`/`composeBuildAndRestart`)
      to also depend on the generic `dockerBuildImage` task (all `bootBuildImage` + all `dockerBuildImage`).

## 2. Wire the compose stack

- [ ] 2.1 Add a `web-ui` service to `docker-compose.yml`: image `aanbrn/axon-showcase-web-ui:${PROJECT_VERSION}`,
      host port `8084:80`, no healthcheck, with `SHOWCASE_API_BASE_URL: http://localhost:8080` (or rely on the baked
      `BPE_DEFAULT_SHOWCASE_API_BASE_URL`).
- [ ] 2.2 Add `http://localhost:8084` to the gateway's `SHOWCASE_CORS_ALLOWED_ORIGINS` in docker-compose.
- [ ] 2.3 Build the UI image and run `composeUp`; verify the UI loads at `http://localhost:8084` and can call the
      gateway (list shows showcases, SSE events stream).

## 3. Add the Helm deployment

- [ ] 3.1 Add a `webUi` values section to `helm/chart/src/main/helm/values.yaml` (image registry/repository/tag,
      replicaCount, service port 80, resources, `containerPorts` incl. the stub-status port, readiness probe,
      pod/container securityContexts, `autoscaling` block with `hpa`/`vpa`, `pdb` block, `networkPolicy` block,
      `ingress` block, `route` HTTPRoute block), following the `apiGateway` pattern.
- [ ] 3.2 Add `templates/web-ui/deployment.yaml`, `service.yaml`, `ingress.yaml`, `route.yaml` (HTTPRoute),
      `hpa.yaml`, `vpa.yaml`, `pdb.yaml`, and `networkpolicy.yaml`, mirroring the `apiGateway` templates but for the
      static nginx container (port 80, no JGroups/management port, no `JAVA_OPTS`); the `ingress`/`route`/
      `autoscaling`/`pdb`/`networkPolicy` values blocks are disabled by default like the gateway's, and the
      NetworkPolicy allows public HTTP ingress, monitoring-only access to the metrics port, and DNS + same-namespace
      egress. The Deployment follows the shared service conventions: common `serviceAccountName` +
      `automountServiceAccountToken`, `emptyDir` at `/tmp`, pod/container securityContexts, and a readiness probe;
      the Service declares named ports `http` (UI) and `http-metrics` (stub-status) for the ServiceMonitor to target.
- [ ] 3.3 Update the gateway CORS env in the chart (`SHOWCASE_CORS_ALLOWED_ORIGINS`) to include the web-UI origin,
      driven by a `webUi` value, and add a `webUi` `SHOWCASE_API_BASE_URL` env var to the Deployment (default the
      in-cluster gateway URL `http://<release>-api-gateway:8080` via `webUi` values), overridable per deployment — no
      ConfigMap/mount.
- [ ] 3.4 In `helm/values/axon-showcase/values-local.yaml`, expose the web UI in the local stack (`webUi.ingress`
      with hostname `axon-showcase-ui`), rename the gateway ingress hostname `axon-showcase` → `axon-showcase-api`,
      and set the local gateway CORS origin to the UI's local host; confirm the local deployment reaches both surfaces
      at their hosts.
- [ ] 3.5 Add a `webUi` `serviceMonitor` block to values and a `templates/web-ui/servicemonitor.yaml` (mirroring the
      api-gateway's) that scrapes the UI Service on the stub-status port, so nginx server metrics appear in the
      Prometheus/Grafana stack; confirm the stub-status → Prometheus conversion mechanism (exporter sidecar or
      metric-relabeling scrape) and that the web-UI NetworkPolicy lets the `monitoring` namespace reach the metrics
      port.
- [ ] 3.6 Update the Helm lint value files (`helm/chart/src/test/helm/helm-lint-full.yaml`,
      `helm-lint-minimal.yaml`) to cover the new `webUi` template branches.
- [ ] 3.7 Run `./gradlew :helm:chart:helmLintMainChartFull :helm:chart:helmLintMainChartMinimal` and confirm lint
      passes; render the chart with `helm template` and confirm the web-UI Deployment/Service/ingress/route/hpa/vpa/
      pdb/networkpolicy/servicemonitor render.

## 4. Docs and verify

- [ ] 4.1 Add `setup-hosts.sh` (resolves the local Traefik LoadBalancer IP and manages the `/etc/hosts` entries for
      `axon-showcase-api` / `axon-showcase-ui`), and update `AGENTS.md` / `README.md`: the web-UI image name, the
      `pack` CLI prerequisite, the `webUi` chart values, the compose `web-ui` service and its `8084` port, the
      `SHOWCASE_API_BASE_URL` runtime env-var mechanism (rendered to `config.js` at container start,
      `BPE_DEFAULT_*` baked default), and the `./setup-hosts.sh setup` usage for direct hostname access instead of
      the `Host:`-header workaround.
- [ ] 4.2 Run `openspec validate --all` and the module checks (`./gradlew :showcase-web-ui:check`), confirm the
      change passes.