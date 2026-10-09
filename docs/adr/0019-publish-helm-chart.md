# ADR-0019: Publish the Helm chart to the GitHub Container Registry

Date: 2026-10-09

Status: Accepted

Revisit when: signing/provenance or Artifact Hub indexing is wanted, or the chart needs a version independent of the app
release — the signal that the "chart ships with the app" simplification no longer fits.

## Context

ADR-0018 gave the release a set of published service images, but not the chart: it lived only in the repository and was
built locally, so an outsider could not install the app from a release. The chart version is already single-sourced to
the project version — the helm Gradle plugin's source-filtering resolves `version: ${chartVersion}` and
`appVersion: ${projectVersion}` from `project.version` — so the chart and the release tag name the same version by
construction. A published chart must also resolve its images from the registry rather than the local daemon: the chart
defaulted every image registry to empty, and `common.images.image` resolves
`default .imageRoot.registry ((.global).imageRegistry)`, so a non-empty `global.imageRegistry` overrides every
per-service registry.

## Decision

On a release dispatch, `.github/workflows/release.yml` packages the chart
(`:helm:chart:helmPackageMainChart -Pversion=<release>`) and pushes it to the GitHub Container Registry as an OCI
artifact at `oci://ghcr.io/<owner>/charts/axon-showcase`, versioned with the release, before it creates the tag and the
GitHub Release. A `dry_run` dispatch packages the chart under a throwaway version (`0.0.0-dryrun.<run-id>`) and pushes
it; a backfill dispatch publishes no chart.

Each service's `image.registry` defaults to `ghcr.io`, so an install with the chart's default values pulls the published
images; the local and `ci` values set each service's registry empty so those targets keep using the locally-built daemon
images, and the `global.imageRegistry` stays empty so the web UI's metrics-exporter sidecar (not a published image)
keeps its own registry on Docker Hub. The chart version is the project version — there is no independent chart-version
lifecycle.

Alternatives considered and rejected: attaching the `.tgz` to the GitHub Release (no `helm repo`/OCI ergonomics); a
classic repository on GitHub Pages (needs index regeneration and Pages machinery, and Artifact Hub indexes OCI charts
too); defaulting `global.imageRegistry` to `ghcr.io` (its global override would also misroute the metrics-exporter
sidecar to a nonexistent `ghcr.io/nginx/nginx-prometheus-exporter`); bundling PostgreSQL/Kafka/OpenSearch as chart
dependencies (a much larger packaged chart, subchart value wiring, and resource sizing).

## Consequences

- A released version's chart is pullable and installable —
  `helm install axon-showcase oci://ghcr.io/aanbrn/charts/axon-showcase --version X.Y.Z` — and its
  `version`/`appVersion` equal the release tag.
- The chart carries no infrastructure; PostgreSQL, Kafka, and OpenSearch remain a documented prerequisite of an install,
  and an install must set `webUi.apiBaseUrl` (the UI image fails fast on an empty value) and
  `apiGateway.cors.allowedOrigins` (the UI's origin, since an empty list denies all browser origins).
- Anonymous installs need the GHCR packages public — the image packages are, and the chart package is verified on its
  first publish (a per-package setting with no file diff).
- Publishing is triggered only by a release or a `dry_run`; signing/provenance and Artifact Hub indexing are deferred,
  and a chart cannot be versioned apart from an app release.
