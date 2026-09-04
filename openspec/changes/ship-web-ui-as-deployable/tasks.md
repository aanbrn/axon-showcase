## 1. Build the web-UI image

- [ ] 1.1 Add `showcase-web-ui/Dockerfile` (nginx serving `build/dist`) and `showcase-web-ui/nginx.conf` (serve the
      bundle, `try_files $uri /index.html` fallback, gzip). Verify locally with `docker build` that the image serves
      the built bundle.
- [ ] 1.2 In `frontend-conventions`, add a `dockerBuildWebUiImage` task (or equivalent): runs `docker build -t
      aanbrn/axon-showcase-web-ui:${project.version}`, depends on `npmBuild`, honors `imagePlatform` (`--platform`)
      like the JVM services. Group under `build`.
- [ ] 1.3 In `docker-conventions`, make the build-first compose tasks (`composeBuildAndUp`/`composeBuildAndRestart`)
      also depend on the web-UI image task (all `bootBuildImage` + web-UI image).

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

- [ ] 4.1 Update `AGENTS.md` / `README.md`: the web-UI image name, the `webUi` chart values, the compose `web-ui`
      service and its `8084` port, and the per-target `VITE_API_BASE_URL` build-time parameter.
- [ ] 4.2 Run `openspec validate --all` and the module checks (`./gradlew :showcase-web-ui:check`), confirm the change
      passes.