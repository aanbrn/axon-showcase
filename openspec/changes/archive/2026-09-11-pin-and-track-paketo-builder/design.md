## Context

See proposal.md — Why. `PackBuildImageTask` (build-logic) exposes `builder` and `buildpacks` inputs but no run image.
The web-UI image was introduced (#67) against the floating `paketobuildpacks/builder-jammy-base:latest`; that builder
publishes a tag almost daily (`latest` = `0.4.639` on 2026-09-11) and its own run image is the floating
`paketobuildpacks/run-jammy-base:latest`. The update-report precedent is `helmUpdates` (registered in the root
`build.gradle.kts`, implemented by `HelmUpdatesTask`), surface as a weekly `helm-updates` workflow, and specified in
`showcase/quality/merge-governance`; no check covers Paketo versions.

## Goals / Non-Goals

**Goals:**

- Pin the builder toolchain to a concrete version, closing the upstream-drift failure class. (The run image stays
  floating — see D5.)
- Ensure the pin is surfaced (and can notify) when it goes stale, rather than sitting silently outdated.

**Non-Goals:**

- Pinning the run image (it stays `:latest`, so base-OS security patches keep flowing) — end-to-end pinning was not
  chosen.
- Making the new check a merge gate (it is observational, like the dependency and Helm update reports).

## Decisions

**D1: Pin a concrete builder version tag (`builder-jammy-base:0.4.639`), not a digest and not a floating tag.** A tag is
legible and bumpable and matches the repo's concrete-version convention; a digest is neither. _Alternative considered:_
keep the builder floating and pin only the buildpacks (rejected — that is #147's state, and the builder's lifecycle and
bundled buildpacks can still drift).

**D2: Track the pin with a `buildpackUpdates` Gradle task plus a weekly `buildpack-updates` workflow, mirroring
`helmUpdates`/`helm-updates.yml`.** The builder moves almost daily, so a hand-audited pin (the Snyk-CLI approach) would
go stale fast; a report that opens/updates an issue closes the "no update check covers Paketo" gap the #147 lesson
flagged. _Alternative considered:_ a manual-audit note only (rejected — too easy to miss at this cadence).

**D3: The report covers the pinned builder and the two pinned buildpacks.** They are all Paketo pins owned by the
catalog with the same staleness problem, so one task/report/workflow covers them together. There is no major-version
suppression: unlike the bitnami charts, a Paketo major bump does not diverge a chart-pinned `image.tag`, so every newer
version is actionable and reported.

**D4: Query the Docker Hub tags API for the latest version, building on the `HelmUpdatesTask` HTTP pattern.** The
builder and the buildpacks all resolve there, so one pure-HTTP mechanism suffices (no new CLI prerequisite). Each check
carries its **Docker Hub repository** explicitly (`paketobuildpacks/builder-jammy-base`, `paketobuildpacks/nginx`,
`paketobuildpacks/procfile`): a buildpack's CNB id (`paketo-buildpacks/nginx`, with a hyphen) is **not** its Docker Hub
repository, so the id and the lookup target must be decoupled. "Latest" is the highest numeric version among the fetched
tags (`page_size=100&ordering=last_updated`), not the most recently pushed tag.

**D5: Leave the run image floating.** Pinning it would freeze the runtime base-OS layer and stop automatic security
patches; the chosen scope is a reproducible _builder_ plus a tracked pin.

## Risks / Trade-offs

- [The check depends on the Docker Hub API; a rate-limit or outage yields a false "no updates"] → a failed lookup is
  treated as "no update" (as the Helm check does) and the report is observational — a miss only defers a bump by a week.
- [The pinned builder falls behind while the run image floats] → both are jammy-base; the weekly report surfaces newer
  builder tags so the pin is bumped deliberately, and the issue notifies the owner.
- [A pinned builder bundles specific buildpack versions, while the task also passes explicit ones] → the explicit
  buildpacks win (they are passed with `--buildpack`); the builder pin governs the lifecycle and build-time base.
