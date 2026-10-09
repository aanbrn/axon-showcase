# Design

## Context

See `proposal.md` for motivation and the `showcase/quality/releases` delta for the requirement.

Current state that shapes the approach:

- `.github/workflows/release.yml` (`workflow_dispatch` only, `main`-guarded, `contents: write`) validates the dispatched
  version against `gradle.properties`, requires the tag to be free, then creates the tag `v<version>` and a GitHub
  Release. It builds no images and has no JDK/Gradle/Docker setup.
- The five images are built by `./gradlew bootBuildImage :showcase-web-ui:dockerBuildImage` to the local Docker daemon
  as `aanbrn/axon-showcase-<service>:${project.version}`. `bootBuildImage` builds via Paketo; the web UI's
  `dockerBuildImage` is a `PackBuildImageTask` running `pack build` (no publish; `--platform` only).
- `gradle.properties` declares `version=0.2.0-SNAPSHOT`, so a bare build tags `:0.2.0-SNAPSHOT`; `-Pversion=<version>`
  overrides `project.version` (verified: `./gradlew properties -Pversion=9.9.9` reports `9.9.9`), so the build can emit
  the released tag directly.
- `.github/workflows/deployment-smoke.yml` already shows the runner prerequisites for this build: JDK 21, the `pack` CLI
  (`buildpacks/github-actions/setup-pack`), and a Docker `overlay2` step the `pack`-built web UI image needs on the
  runner's Docker store.
- The Helm chart resolves each image from `image.registry` (default empty) + `image.repository`
  (`aanbrn/axon-showcase-*`) + `image.tag`, with a `global.imageRegistry` override. Both local and `ci` targets build to
  the local daemon (the `ci` smoke `kind load`s them), so they do not pull from a registry.

## Goals / Non-Goals

**Goals:**

- Every released version's five images are pullable from GHCR under `<version>` and `latest`.
- A GitHub Release is never created for a version whose images failed to publish.
- The publish path is exercisable in CI before merge, not only after it.

**Non-Goals:**

- Changing the local or `ci` deployment paths, the chart's image defaults, or `image.repository` — those keep resolving
  the local-daemon images.
- Multi-arch images, a per-commit/per-`main` publish, or publishing any artifact other than the five images.
- Automating GHCR package visibility (a settings toggle, not a workflow step).

## Decisions

### Registry: GitHub Container Registry (`ghcr.io`)

GHCR is discoverable from the repository, needs no separate account, and auth uses the run's `GITHUB_TOKEN` with
`packages: write`. **Rejected:** Docker Hub (a second credential/secret, and the empty `aanbrn` namespace would need the
owner to create repositories first) and no registry (the status quo the proposal rejects).

### Trigger: the release dispatch, publishing before the tag/Release

Image publication is a step of the existing `release` workflow, ordered before the tag and Release creation. This keeps
one "publish a version" surface, and the ordering makes the spec's invariant — a published Release implies its images
published — true by construction. **Rejected:** publishing on every push to `main` (registry churn, and a `latest` that
moves between releases); a separate workflow (a second dispatcher to keep in sync with the version validation).

### Build with the existing Gradle tasks, then tag and push

Run `./gradlew bootBuildImage :showcase-web-ui:dockerBuildImage -Pversion=<version>` as the smoke does, then
`docker tag` each local image to `ghcr.io/<owner>/<image>:<version>` and `:latest` and `docker push`. This reuses the
exact build path the local and `ci` targets use, so what is published is what those paths build. `-Pversion=<version>`
is passed so the built tag already equals the released version (the declaration is `-SNAPSHOT`), and the push needs no
snapshot retag. **Rejected:** `bootBuildImage`'s native `publish = true` and `pack build --publish` (two different
publish mechanisms with their own registry-credential wiring, and the `PackBuildImageTask` has no publish option);
building inside the workflow with `docker buildx` (a third build path); building the snapshot tag and retagging every
image (a second version mapping to keep right).

### Route the new `pack` pin into the `toolingUpdates` check

The workflow pins the `pack` CLI version, as the deployment smoke does. `AGENTS.md` requires a workflow tool pin to join
that check's declared list — its only detector of a release — and the `pack-cli` check reads only `e2e.yml`, so
`release.yml` and (closing the same pre-existing gap) `deployment-smoke.yml` each join it, and every entry's
`workflowFile` is registered in `pinFiles` (an unregistered file fails the task). The check renders
`"<name>: <pinned> -> <latest>"` with no file in the line, so every entry is named per file — `pack-cli (e2e.yml)`,
`pack-cli (release.yml)`, `pack-cli (deployment-smoke.yml)` — giving one row per workflow rather than duplicate bare
`pack-cli` rows. **Rejected:** omitting the pins from the check (they would drift undetected, and a version diverging
from `e2e.yml`'s would read as current).

### Tags: `<version>` and `latest`, `linux/amd64`

`<version>` (the released version, from the same single declaration the tag uses) plus `latest` — the familiar Docker
convention, with `latest` only moved by a release. Images target `linux/amd64`, the runner's and the current build's
default. **Rejected:** version-only (no convenient `latest`), moving `major`/`major.minor` tags (more tags to manage, no
consumer yet), multi-arch (a buildx pipeline for ARM users who are not yet a target).

### Verify the publish path pre-merge with a `dry_run` dispatch input

A boolean `dry_run` input builds and pushes the images under a throwaway tag (no `latest`, no `<version>` tag, no
tag/Release), and the `main`-branch guard applies only to the release-creation path. This makes the build-and-push path
runnable from the change's branch (`gh workflow run release.yml --ref <branch> -f version=<v> -f dry_run=true`), so the
path is exercised before merge rather than first at a real release. **Rejected:** leaving the whole workflow
`main`-guarded (the publish path first runs post-merge, which the repo's "verify as part of the merge" rule
discourages); a throwaway registry other than GHCR (another credential).

### Record the decision as a new ADR (0018)

ADR-0016 scoped itself to tags/Releases — its Context notes the images existed "but had published no git tag" — and its
Decision never chose a registry. Publishing to a registry is a fresh cross-cutting decision, so it gets its own ADR
rather than editing 0016.

## Risks / Trade-offs

- **The `pack`-built web UI image fails on the runner's Docker store.** The same failure the smoke works around
  (`imgutil` cannot read layers the new store produces, `buildpacks/pack#2527`) → mirror the smoke's `overlay2` step
  before the build.
- **GHCR packages default to private, so `docker pull` is denied anonymously.** A per-package visibility toggle with no
  file diff → name the required setting in the change's report and README, and make the packages public once after the
  first publish.
- **The release run grows** (JDK/Gradle/`pack` setup plus five image builds). Releases are infrequent and interactive →
  acceptable; no merge-gate impact.
- **A dry run leaves throwaway package versions.** They are deletable via the package API and are never tagged `latest`
  → document the tag and clean up after verification.
- **The GHCR owner path must be lowercase.** The owner (`aanbrn`) already is → normalize with a lowercase transform in
  the tag step so a future owner rename cannot silently break the push.
