# ADR-0018: Publish the service images to the GitHub Container Registry

Date: 2026-10-09

Status: Accepted

Revisit when: a consumer needs ARM (`linux/arm64`) images, or a second registry or distribution channel is wanted — the
signal that the amd64-only, GHCR-only choice has become a constraint rather than a settled default.

## Context

ADR-0016 shipped the release workflow — the tag and the generated GitHub Release — but scoped itself to the version and
tag decision. The five service images (`aanbrn/axon-showcase-*:${project.version}`) were built only to a local Docker
daemon, no workflow pushed them, and the `aanbrn` Docker Hub namespace was empty. A release therefore named images
nobody could pull, and the reference app could not be run from a release. The Helm chart resolves each image from
`image.registry` plus `image.repository` and `image.tag` (the registry defaults to `ghcr.io` since ADR-0019), so a
published image is directly deployable.

## Decision

On a release dispatch, `.github/workflows/release.yml` builds the five images and publishes each to the GitHub Container
Registry at `ghcr.io/<owner>/axon-showcase-<service>`, tagged with the released version and `latest` for `linux/amd64`,
before it creates the tag and the GitHub Release. The workflow authenticates the push with its `GITHUB_TOKEN`, granted
`packages: write`. A `dry_run` dispatch input runs the same build and push under a throwaway tag without creating a tag
or a release, so the path is exercisable from a branch before merge. A `publish_tag` dispatch backfills an existing
release instead: it checks out that release's tag, builds its images, and pushes only the `<version>` tag (no `latest`,
and no tag or release), so a release cut before this workflow gained publishing can be completed.

Alternatives considered and rejected: Docker Hub (a second credential and an empty namespace to seed first); publishing
on every push to `main` (registry churn, and a `latest` that would move between releases); `bootBuildImage`'s native
`publish = true` and `pack build --publish` (two publish mechanisms with their own registry-credential wiring, and the
web UI's `PackBuildImageTask` has no publish option); building the `-SNAPSHOT` tag and retagging every image (a second
version mapping to keep right); multi-arch images (a buildx pipeline for a target with no consumer yet).

## Consequences

- A released version's images are pullable (`docker pull ghcr.io/aanbrn/axon-showcase-*:<version>`), and the chart
  resolves them from GHCR by default (ADR-0019).
- GHCR packages published by this repository's workflow are public (a package inherits the repository's visibility), so
  anonymous pulls work; visibility remains a per-package setting.
- The release run grows (JDK, Gradle, `pack`, and five image builds) and needs `packages: write`; the `pack` pin joins
  the `toolingUpdates` check.
- Publishing is amd64-only and triggered only by a release or a backfill dispatch; a per-commit or multi-arch publish
  stays deferred.
