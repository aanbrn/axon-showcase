## 1. Build the web-UI image

- [ ] 1.1 In `frontend-conventions`, register a `dockerBuildWebUiImage` Exec task that runs the `pack` CLI:
      `pack build aanbrn/axon-showcase-web-ui:${project.version} --path build/dist --builder
      paketobuildpacks/builder-jammy-base --buildpack paketo-buildpacks/nginx --env BP_WEB_SERVER=nginx --env
      BP_WEB_SERVER_ROOT=/workspace`, honoring `imagePlatform` (`--platform`) like the JVM services. It depends on
      `npmBuild`. Group under `build`.
- [ ] 1.2 Run `./gradlew :showcase-web-ui:dockerBuildWebUiImage` and verify the image builds and serves the bundle when
      run (curl the container's port).
- [ ] 1.3 In `docker-conventions`, extend the build-first compose tasks (`composeBuildAndUp`/`composeBuildAndRestart`)
      to also depend on the web-UI `dockerBuildWebUiImage` task (all `bootBuildImage` + the frontend image task).

## 2. Wire the compose stack

- [ ] 2.1 Add a `web-ui` service to `docker-compose.yml`: image `aanbrn/axon-showcase-web-ui:${PROJECT_VERSION}`,
      host port `8084:80`, no healthcheck, built from the same image naming convention as the other services.
- [ ] 2.2 Add `http://localhost:8084` to the gateway's `SHOWCASE_CORS_ALLOWED_ORIGINS` in docker-compose.
- [ ] 2.3 Build the UI image and run `composeUp`; verify the UI loads at `http://localhost:8084` and can call the
      gateway (list shows showcases, SSE events stream).

## 3. Add the Helm deployment

- [ ] 3.1 Add a `webUi` values section to `helm/chart/src/main/helm/values.yaml` (image registry/repository/tag,
      replicaCount, service port 80, resources, ingress toggle), following the `apiGateway` pattern.
- [ ] 3.2 Add `templates/web-ui/deployment.yaml`, `service.yaml`, and (optional) `ingress.yaml`, mirroring the
      existing service templates but for the static nginx container (port 80, no JGroups/management port, no
      `JAVA_OPTS`).
- [ ] 3.3 Update the gateway CORS env in the chart (`SHOWCASE_CORS_ALLOWED_ORIGINS`) to include the web-UI origin,
      driven by a `webUi` value; document that the UI image must be built with the in-cluster gateway base URL.
- [ ] 3.4 Update the Helm lint value files (`helm/chart/src/test/helm/helm-lint-full.yaml`,
      `helm-lint-minimal.yaml`) to cover the new `webUi` template branches.
- [ ] 3.5 Run `./gradlew :helm:chart:helmLintMainChartFull :helm:chart:helmLintMainChartMinimal` and confirm lint
      passes; render the chart with `helm template` and confirm the web-UI Deployment/Service render.

## 4. Docs and verify

- [ ] 4.1 Update `AGENTS.md` / `README.md`: the web-UI image name, the `pack` CLI prerequisite, the `webUi` chart
      values, the compose `web-ui` service and its `8084` port, and the per-target `VITE_API_BASE_URL` build-time
      parameter.
- [ ] 4.2 Run `openspec validate --all` and the module checks (`./gradlew :showcase-web-ui:check`), confirm the change
      passes.