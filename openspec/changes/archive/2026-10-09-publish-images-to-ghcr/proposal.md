# Proposal

## Why

`release.yml` publishes a tag and a GitHub Release but never the five images a release names. The images are built only
to the local Docker daemon (`aanbrn/axon-showcase-*:${project.version}`), no workflow pushes them, and the Docker Hub
namespace is empty — so a released version points at images nobody can pull, and the reference app cannot be run from a
release. Publishing the images at release time closes that gap and completes the release capability shipped on
2026-10-04 (ADR-0016), whose Consequences already say "the images the release names are built from it".

## What Changes

- **`.github/workflows/release.yml`** — grant the job `packages: write`; add the JDK, Gradle, and `pack` CLI setup and a
  build step (`-Pversion=<version>`, so the built tag is the released version); build the five images; authenticate to
  GitHub Container Registry; tag and push each image as `ghcr.io/<owner>/<image>:<version>` and `:latest`
  (`linux/amd64`); create the tag and GitHub Release only after every image publishes. Add a `dry_run` dispatch input
  that exercises the same build and push under a throwaway tag without creating a tag or Release, so the path is
  verifiable from a branch before merge.
- **`openspec/changes/publish-images-to-ghcr/specs/showcase/quality/releases/spec.md`** — add requirements that the
  release workflow publishes the service images and creates the tag only after they publish, and narrow the `main`-ref
  guard so a dry-run dispatch may run from another ref.
- **`build.gradle.kts`** — add `release.yml`'s and the deployment smoke's `pack-version` pins to the `toolingUpdates`
  check's declared list, each named distinctly so the report shows one row per file.
- **`docs/adr/0018-publish-service-images-to-ghcr.md`** — a new ADR recording the registry choice (GHCR), the trigger
  (release only), the tag policy (`<version>` + `latest`), the platform (`linux/amd64`), and the public-visibility
  consequence.
- **`README.md`** — a "run from the published images" path, and refresh every README description of the release workflow
  (derive them by grepping `release.yml`): the Docker Images / Getting Started material and the Continuous Integration
  release paragraph, including a note that the packages must be made public for anonymous pulls.
- **`AGENTS.md`** — refresh every live description of the release and every set `release.yml` joins (derive them by
  grepping `release.yml`): the `release.yml` bullet, the web-UI/nodejs-cache workflow enumeration, the Docker Images
  section, and the Kubernetes Deployment sentence that says the images are published to no registry.
- **`openspec/config.yaml`** — refresh the Docker images context line to name the registry.
- **`openspec/specs/showcase/quality/releases/spec.md`** — refresh the capability's `## Purpose` to cover image
  publication; a delta cannot carry a Purpose for an existing capability, so this edit lands in the archive commit (see
  the change's report).
- **`docs/ideas.md`** — remove the "Nothing is published to a container registry" idea (implemented).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/releases`: adds requirements that the release workflow publishes the five images to the GitHub
  Container Registry (tagged with the released version and `latest`, `linux/amd64`) and creates the tag and Release only
  after every image publishes; narrows the `main`-ref guard so a dry-run dispatch may run from another ref; and grants
  the workflow `packages: write`.

## Impact

- **Release workflow**: a longer run (JDK, Gradle, `pack` CLI, five image builds) and a new `packages: write`
  permission; `GITHUB_TOKEN` authenticates the GHCR push; the build runs with `-Pversion=<version>` so the images carry
  the released tag.
- **Build**: distinct `pack-cli` entries in the `toolingUpdates` check for `release.yml` and the deployment smoke,
  keeping their pins detected.
- **Registry**: a new `ghcr.io/aanbrn/axon-showcase-*` package set. Anonymous pulls require the packages to be made
  public — a per-package setting with no file diff, named in the change's report and README.
- **Unchanged**: local and CI image builds keep building to the local daemon (the Helm chart keeps resolving the
  `aanbrn/...` names locally and the `ci` smoke keeps `kind load`-ing them); no application code, tests, or chart
  defaults change.
- **Docs/config surfaces** refreshed: README, `AGENTS.md`, `openspec/config.yaml`, the `showcase/quality/releases`
  Purpose (in the archive commit), a new ADR, and `docs/ideas.md`.
