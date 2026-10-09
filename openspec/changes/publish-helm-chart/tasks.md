# Tasks

## 1. Default the service images to GHCR and make an install copy-and-fill

- [x] 1.1 In `helm/chart/src/main/helm/values.yaml`, set each of the five services' `image.registry: "ghcr.io"` (keep
      `global.imageRegistry: ""`); verify by re-packaging (`./gradlew :helm:chart:helmPackageMainChart`) and rendering a
      service
      (`helm template axon-showcase helm/chart/build/helm/charts/axon-showcase --show-only templates/api-gateway/deployment.yaml`)
      and confirming the image resolves from `ghcr.io` (re-package first — the render reads the filtered copy).
- [x] 1.2 Verify the metrics-exporter sidecar is unaffected by the per-service change (no values edit needed — the
      global registry stays empty): render with the sidecar enabled —
      `helm template axon-showcase helm/chart/build/helm/charts/axon-showcase` plus
      `-f helm/chart/src/test/helm/helm-lint-full.yaml --show-only templates/web-ui/deployment.yaml` — and confirm the
      sidecar resolves from Docker Hub, not `ghcr.io`.
- [x] 1.3 Add each service's `image.registry: ""` to `helm/values/axon-showcase/values-local.yaml` and
      `helm/values/axon-showcase/values-ci.yaml`; verify the rendered service image has no registry prefix.
- [x] 1.4 Correct the manual `helm install` command in `AGENTS.md` (~line 1476) — its chart path `./helm/chart` has no
      `Chart.yaml` — to target the packaged chart (`helm/chart/build/helm/charts/axon-showcase`) and apply
      `-f helm/values/axon-showcase/values-local.yaml`, so the local manual path keeps using local images; verify by
      reading the command.
- [x] 1.5 Add `helm/chart/examples/values-external.yaml` — a commented example covering the `dbEvents`/`osViews`/`kafka`
      connections, the password Secrets, the ingress hostnames, `webUi.apiBaseUrl` and `apiGateway.cors.allowedOrigins`,
      with the ServiceMonitors off — and verify it renders by templating the packaged chart
      (`helm template axon-showcase helm/chart/build/helm/charts/axon-showcase`) with the example values
      (`-f helm/chart/examples/values-external.yaml`).

## 2. Publish the chart from the release workflow

- [x] 2.1 In `.github/workflows/release.yml`, add a step after the image push and before the tag/release that packages
      the chart and pushes it to GHCR: package with
      `./gradlew :helm:chart:helmPackageMainChart -Pversion="${RELEASE_VERSION}"`
      (`-Pversion=0.0.0-dryrun.${GITHUB_RUN_ID}` on a `dry_run`), then push with the plugin-downloaded Helm client
      (`helm_bin="$(find .gradle/helm/client -name helm -type f -perm -u+x | head -1)"`) to
      `oci://ghcr.io/<lowercased owner>/charts`; skip the whole step on a backfill (`publish_tag`); verify with
      `./gradlew workflowLint` and by reading the step order and the owner-lowercasing.
- [x] 2.2 Confirm a chart packaging/push failure blocks the release: the chart step runs before the
      `Create the tag and the release` step, so a non-zero exit fails the run first; verify by reading the step order.

## 3. Documentation and records

- [x] 3.1 Add `docs/adr/0019-publish-helm-chart.md` recording the OCI/GHCR choice, the version single-sourcing, the GHCR
      registry default, and the deferrals (signing/provenance, Artifact Hub indexing) with a `Revisit when:` line naming
      the condition that would reopen each; verify it follows the ADR format in `docs/adr/README.md`.
- [x] 3.2 In `README.md`, add an install-from-the-published-chart walkthrough that points at
      `helm/chart/examples/values-external.yaml` and states the prerequisites (the infrastructure, the Secrets, and the
      required `webUi.apiBaseUrl` / `apiGateway.cors.allowedOrigins` values), and refresh the Continuous Integration
      release paragraph (~line 866) to name the chart; verify.
- [x] 3.3 Refresh `AGENTS.md` — the `release.yml` bullet (the chart is published too) and the Helm/Docker Images notes
      (the chart's registry default and the OCI artifact); verify by grepping `oci://ghcr.io`.
- [x] 3.4 Refresh the `Docker images:` context line in `openspec/config.yaml` to name the published chart; verify the
      file still parses (`openspec instructions proposal --change publish-helm-chart` emits no parse warning).
- [x] 3.5 Correct the stale claims the change surfaces: the `webUi.apiBaseUrl` "empty = same-origin" statement (the
      `deployment/helm-chart` delta's `MODIFIED` block, the web-ui template comment, and the `values.yaml` comment) and
      the chart-registry statements in `AGENTS.md`'s Docker-Images note and `docs/adr/0018` (Context + Consequences);
      verify by grepping `same-origin` (none left) and `local daemon`.
- [ ] 3.6 In the archive commit, edit `openspec/specs/showcase/quality/releases/spec.md`: refresh the `## Purpose` to
      name the chart it publishes (a delta cannot carry a Purpose for an existing capability), and correct the
      standalone `Release` in the "A release is created only after its images publish" requirement
      (`the tag or the Release` → `the tag or the release`). Record the deferral in the change's report; verify the
      Purpose names the chart and no standalone `Release` remains in the spec.
- [x] 3.7 Run `./gradlew spotlessApply` and `./gradlew spotlessCheck` after the final edit; verify both pass.

## 4. Verification

- [x] 4.1 Confirm the deltas validate (`openspec validate publish-helm-chart --strict`) and `./gradlew workflowLint`
      passes.
- [x] 4.2 Package the chart locally (`./gradlew :helm:chart:helmPackageMainChart`) and confirm the `.tgz` exists and
      carries the vendored `common` subchart; render it with default values (image from `ghcr.io`), with
      `values-local.yaml` (no registry), and with the example values file.
- [ ] 4.3 On the pushed branch, exercise the chart publish pre-merge: dispatch it with the dry-run input
      (`gh workflow run release.yml --ref <branch> -f version=<v> -f dry_run=true`), then confirm the chart appears at
      `oci://ghcr.io/aanbrn/charts/axon-showcase` under the throwaway version, no `v<version>` tag or release was
      created, and the `charts` package is public (make it public if it starts private).
- [x] 4.4 Run the `lesson-capture` subagent over the diff and the review findings, apply its durable proposals, and
      record the applied net `AGENTS.md` delta on this task (applied: net ≈ +3 lines — a "read a shared helper's
      precedence" clause added to the premise-interrogation bullet; the duplicated registry-default sentence was merged
      into a cross-reference, which was line-neutral).

## Workflow follow-up

- If the `ghcr.io/<owner>/charts` package is not public after its first publish, make it public for anonymous installs
  (a per-package setting, no file diff).
- Delete the dry-run throwaway chart version (`0.0.0-dryrun.<run-id>`) after verifying.
- Archive the change after the project's review requirements are satisfied.
