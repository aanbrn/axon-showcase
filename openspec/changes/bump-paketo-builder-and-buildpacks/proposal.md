# Proposal: Bump the Paketo builder and buildpacks

## Why

The weekly `buildpackUpdates` check reports all three pinned Paketo coordinates stale. The open "Buildpack updates"
issue (#211) named only the builder, at an already-stale target (`0.4.641`), because it was last refreshed before the
two buildpack releases; re-resolving shows `paketo-builder-jammy-base` `0.4.639 → 0.4.642`, `paketo-nginx`
`1.2.0 → 1.2.1` and `paketo-procfile` `5.14.0 → 5.15.0`, each confirmed against Docker Hub. A buildpack pin is verified
by a live image build, and the web UI image builds locally again — an earlier round of failures was host state, colima
falling back to QEMU after the macOS 27 upgrade removed Rosetta, not a pin defect — so this change is verified the way
the pin is actually used.

## What Changes

- Bump `paketo-nginx` `1.2.0 → 1.2.1`, `paketo-procfile` `5.14.0 → 5.15.0` and `paketo-builder-jammy-base`
  `0.4.639 → 0.4.642` in `gradle/libs.versions.toml`.
- Update the versions `AGENTS.md` quotes: both buildpack pins and the builder pin in the Docker Images section, and the
  alias-tag example in the buildpack-CNB-id gotcha.
- Verify by rebuilding the web UI image and reading the buildpack versions back out of the built image's
  `io.buildpacks.build.metadata` label — the label carries no builder, so `0.4.642` is attested by the build log and the
  catalog wiring — then re-running `buildpackUpdates` until its report is empty.

## Capabilities

### New Capabilities

<!-- none — a pure dependency bump; `skip_specs: true` is set. -->

### Modified Capabilities

<!-- none — no requirement states a buildpack or builder version; the pins are catalog-owned and reported by
`buildpackUpdates`. -->

## Impact

- **Build**: `gradle/libs.versions.toml` — three version bumps; the pins feed `frontend-conventions`' `dockerBuildImage`
  task automatically.
- **Docs**: `AGENTS.md` — the quoted pins and the alias-tag example track the bump.
- **Behavior**: the web UI image ships nginx 1.2.1 and procfile 5.15.0, built by `builder-jammy-base:0.4.642`;
  `buildpackUpdates` reports nothing.
- **Not changed**: the run image stays floating (`paketobuildpacks/run-jammy-base:latest`), deliberately, so base-OS
  security patches keep flowing.
