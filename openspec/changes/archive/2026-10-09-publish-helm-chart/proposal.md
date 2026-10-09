# Proposal

## Why

The release publishes the five images to the GitHub Container Registry (ADR-0018) but not the Helm chart, so the app is
not installable from a release: the chart lives only in the repository and is built locally. The chart version already
tracks the project version (`chartName = rootProject.name`, `version: ${chartVersion}`, `appVersion: ${projectVersion}`)
and the build already packages it, so publishing it is the missing half of "a release is installable". A published chart
must also resolve its images from the registry rather than the local daemon to work out of the box.

## What Changes

- **`.github/workflows/release.yml`** — package the chart (`:helm:chart:helmPackageMainChart -Pversion=<version>`) and
  push it to GHCR as an OCI artifact (`oci://ghcr.io/<owner>/charts`) with the released version, before the tag and
  release; a `dry_run` pushes a throwaway version; a backfill publishes none.
- **`helm/chart/src/main/helm/values.yaml`** — default each service's `image.registry` to `ghcr.io` so a published
  install pulls the published images (the global registry stays empty, so the metrics-exporter sidecar keeps Docker
  Hub); **`helm/values/axon-showcase/values-local.yaml`** and **`values-ci.yaml`** — set each service's
  `image.registry: ""` so the local and `ci` targets keep using locally-built daemon images.
- **`helm/chart/examples/values-external.yaml`** (new) — a commented example values file covering the infrastructure
  connections, the Secrets, the ingress hostnames, and the UI/CORS values, so a manual install is copy-and-fill rather
  than reverse-engineering ~20 keys.
- **`docs/adr/0019-publish-helm-chart.md`** — a new ADR recording the OCI/GHCR choice, the versioning, the registry
  default, and the install prerequisites.
- **`README.md`**, **`AGENTS.md`**, **`openspec/config.yaml`** — installing from the published chart (pointing at the
  example values file, with the required `webUi.apiBaseUrl` and CORS values), the PostgreSQL/Kafka/OpenSearch
  prerequisite, the chart's registry default, and the updated local values path; the `webUi.apiBaseUrl` "same-origin"
  claim in `values.yaml`/the web-ui template is corrected to its fail-fast behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/releases`: adds a requirement that the release workflow publishes the Helm chart as an OCI artifact.
- `showcase/deployment/helm-chart`: adds a requirement that the chart defaults each service's image registry to GHCR
  (with the local and `ci` values overriding it), and corrects the `webUi.apiBaseUrl` scenario, which described an empty
  value as "same-origin" while the web-UI image fails fast on it.

## Impact

- **Release workflow**: packages and pushes the chart; a chart failure blocks the release (as an image failure does).
- **Chart**: each service's image registry defaults to `ghcr.io` (the global registry stays empty, so the
  metrics-exporter sidecar keeps Docker Hub); the local and `ci` targets override it, and the manual `helm install` in
  `AGENTS.md` is corrected to the packaged chart and given the local values file.
- **Registry**: a new `ghcr.io/<owner>/charts/axon-showcase` OCI artifact, versioned with the release; its visibility
  (like the image packages') is a per-package setting, verified on first publish.
- **Unchanged**: application code, the service images, and the infrastructure releases.
- **Docs** refreshed: README, `AGENTS.md`, `openspec/config.yaml`, ADR-0018, a new ADR, and a new example values file.
