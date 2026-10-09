# Design

## Context

See `proposal.md` for motivation and the two deltas for the requirements.

Current state that shapes the approach:

- `.github/workflows/release.yml` builds and pushes the five images to GHCR on a release dispatch, then creates the tag
  and release; it has a `dry_run` input (throwaway image tag) and a `publish_tag` backfill. It does not touch the chart.
- The chart is packaged by `:helm:chart:helmPackageMainChart` into
  `helm/chart/build/helm/charts/axon-showcase-<version>.tgz`, with the Bitnami `common` subchart vendored under
  `charts/`. Its name is `axon-showcase` (`chartName = rootProject.name`), its `version` is `${chartVersion}` and its
  `appVersion` is `${projectVersion}` — both the project version, already single-sourced.
- The chart's default is `global.imageRegistry: ""`, and each service's `image.registry` is `""`. The Bitnami
  `common.images.image` helper resolves `default .imageRoot.registry ((.global).imageRegistry)` — a **non-empty**
  `global.imageRegistry` overrides every per-service registry (it is a global mirror), and with it empty the per-service
  registry is used. The web UI's metrics-exporter sidecar (`webUi.metricsExporter.image`) renders through the same
  helper, so defaulting the _global_ registry would misroute it — it lives on Docker Hub, not GHCR — while defaulting
  each _service's_ registry leaves the sidecar untouched. `helm/values/axon-showcase/values-local.yaml` and
  `values-ci.yaml` have no `global:` section, and `AGENTS.md`'s manual `helm install axon-showcase ./helm/chart` applies
  no values file (and its path is already invalid — the `helm/chart` module dir has no `Chart.yaml`; the chart source is
  at `helm/chart/src/main/helm`, and only the packaged chart at `helm/chart/build/helm/charts/axon-showcase` is
  installable).
- The Gradle helm plugin downloads a catalog-pinned Helm client to `.gradle/helm/client/<version>/<os-arch>/helm`
  (`downloadClient`), and the repo's Helm CLI version is single-sourced as `libs.versions.helm` (read by `helmUpdates`).

## Goals / Non-Goals

**Goals:**

- A release publishes the chart to GHCR as an OCI artifact, versioned with the release.
- A published install pulls the published images out of the box.
- The publish path is verifiable before merge, and the local/`ci` targets are unchanged.

**Non-Goals:**

- Bundling PostgreSQL/Kafka/OpenSearch into the chart (documented as a prerequisite).
- Signing/provenance (deferred).
- Indexing on Artifact Hub (an OCI chart can be indexed via an `artifacthub-repo.yml` metadata tag; adding that is
  deferred).

## Decisions

### Publish as an OCI artifact on GHCR

`helm push helm/chart/build/helm/charts/axon-showcase-<version>.tgz oci://ghcr.io/<owner>/charts`. GHCR is already the
registry the images use, so this adds no new hosting, no `index.yaml`, and no Pages branch; versions are immutable and
match the release tag. **Rejected:** attaching the `.tgz` to the GitHub Release (no `helm repo`/OCI ergonomics); a
classic repo on GitHub Pages (needs index regeneration and Pages machinery — and Artifact Hub indexes OCI charts too, so
Pages is not required for discoverability).

### Provision Helm for the push from the plugin-downloaded client

`helm push` needs a Helm CLI. The Gradle helm plugin already downloads the catalog-pinned Helm (`downloadClient`), so
the push uses that binary — located under `.gradle/helm/client/<version>/<os-arch>/helm` — rather than installing a
second, separately-pinned Helm, keeping the version single-sourced (ADR-0015). `helm push` reuses the
`docker login ghcr.io` credentials the image step already establishes, so no `helm registry login` step is needed.
**Rejected:** installing a pinned Helm via a setup action (a workflow pin duplicating the catalog version, which would
then need its own update-check entry).

### Default each service's image registry to GHCR

Each service's `image.registry` defaults to `ghcr.io`, so `helm install` with default values pulls the published images;
`values-local.yaml` and `values-ci.yaml` set each service's registry empty so those targets keep resolving the
locally-built daemon images (the `ci` smoke builds and `kind load`s them). The `global.imageRegistry` stays empty, so
the metrics-exporter sidecar — not a published image — keeps its own registry (Docker Hub) rather than inheriting a
global. **Rejected:** defaulting `global.imageRegistry` to `ghcr.io` (the helper's global override would also misroute
the sidecar to `ghcr.io/nginx/nginx-prometheus-exporter`, which does not exist); keeping the registry empty and
documenting `--set global.imageRegistry=ghcr.io` (a bare install would pull `aanbrn/axon-showcase-*` from Docker Hub and
`ImagePullBackOff`).

### Publish on the release dispatch; `dry_run` pushes a throwaway version; backfill none

The chart is packaged and pushed on the release path only, before the tag and release, so a chart failure blocks the
release (as an image failure does). A `dry_run` packages the chart under a throwaway prerelease version
(`0.0.0-dryrun.<run-id>`) and pushes it, verifying the packaging and the OCI push before merge as the images' dry run
does (it is push-only: the throwaway chart's image references name that version, not the pushed dry-run images, so it is
not installed); a backfill publishes no chart (it completes an existing release, whose chart is already published).
**Rejected:** release-only with no `dry_run` (the push would first run post-merge); publishing the chart on a backfill
(a released chart cannot be re-versioned).

### Pass the release version to the chart packaging

The chart version is the project version, but on a release dispatch the checkout is `main`, whose `gradle.properties` is
`X.Y.Z-SNAPSHOT`, so the packaging must pass `-Pversion=<release>` (as the image build does) to name the chart `X.Y.Z`;
without it the chart would package and push as `X.Y.Z-SNAPSHOT` and the release tag, the image tags, and the chart
version would disagree. The `dry_run` path passes `-Pversion=0.0.0-dryrun.<run-id>`.

### Ship a commented example values file

A published install needs far more than the two UI values: the three infrastructure connections (`dbEvents`, `osViews`,
`kafka`), the Secrets holding their passwords, the ingress hostnames, `webUi.apiBaseUrl` and
`apiGateway.cors.allowedOrigins`, and the monitoring toggles — the chart's defaults point at the local release names, so
almost everything must be set. The README therefore points at `helm/chart/examples/values-external.yaml`, a commented
copy-and-fill example, rather than enumerating the keys as `--set` flags. The example leaves the ServiceMonitors off
(they need the Prometheus-operator CRDs) so a bare cluster installs. **Rejected:** enumerating every key in the README
(accurate but unusable); bundling the infrastructure (rejected above).

### Document the infrastructure as a prerequisite

The chart deploys the five services and the migration/index jobs; PostgreSQL, Kafka, and OpenSearch are separate Bitnami
releases. A published install therefore documents the install order (or the `helm install` commands for the infra)
rather than bundling the subcharts. **Rejected:** adding postgres/kafka/opensearch as optional chart dependencies (a
much larger packaged chart, subchart value wiring, and resource sizing).

## Risks / Trade-offs

- **The registry default changes shared chart behavior.** The local and `ci` values now set each service's
  `image.registry: ""`, and the manual `helm install` in `AGENTS.md` gains the local values file (and its chart path is
  corrected to the packaged chart). The deployment smoke installs the chart through the `ci` target and would catch a
  registry regression; the two e2e suites do not install the chart.
- **The plugin's client path is an internal layout.** Locate it with a glob
  (`find .gradle/helm/client -name helm -type f`); if the layout changes, fall back to installing a pinned Helm via a
  setup action (and route that pin into the update check).
- **A `dry_run` leaves a throwaway OCI chart version.** It is a prerelease (`0.0.0-dryrun.<run-id>`) and deletable via
  the package API; harmless if left.
- **The new `ghcr.io/<owner>/charts` package's visibility is unverified until its first publish.** The image packages
  are public, and the chart package is published the same way; verify it after the `dry_run` and make it public if it
  starts private (a per-package setting with no file diff).
- **Artifact Hub indexing is deferred, not foreclosed.** An OCI chart is indexed by adding an `artifacthub-repo.yml`
  metadata tag to the repository; the install path works without it.
- **The GHCR owner path must be lowercase.** As with the images, normalize with a lowercase transform.
