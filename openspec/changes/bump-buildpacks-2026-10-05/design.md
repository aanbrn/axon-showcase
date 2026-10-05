# Design

## Context

See `proposal.md` — Why. The web UI image is built by `pack` (`frontend-conventions`' `dockerBuildImage`, a
`PackBuildImageTask`) over `build/dist`, passing the version-pinned Paketo NGINX and Procfile buildpacks; the builder
(`paketo-builder-jammy-base`) is also pinned. The pins are catalog-owned, and the builder and the buildpacks it bundles
must be bumped together.

## Goals / Non-Goals

**Goals:**

- Move the builder and its bundled buildpacks to the versions the check reports, as a set, and verify the image runs.

**Non-Goals:**

- A source or application change; the image's contents come from the built bundle, unchanged.

## Decisions

### Decision: bump all three pins together, after confirming the new builder bundles the new buildpack versions

The builder and the buildpacks it bundles are one unit: a `paketo-nginx` pin the builder does not bundle makes `pack`
add it from the registry, and the added buildpackage can resolve to an **arm64** slice even though the builder is
published `linux/amd64` only — the mismatched pair fails at runtime with `exec: nginx: not found` while the build
succeeds and the metadata label names the pinned version. So before bumping, inspect the new builder
(`pack builder inspect paketobuildpacks/builder-jammy-base:0.4.653`) and confirm it bundles the reported buildpack
versions. Confirmed: 0.4.653 bundles `paketo-buildpacks/nginx@1.3.0` and `paketo-buildpacks/procfile@5.15.2` — the
reported pins — so `nginx 1.3.0` / `procfile 5.15.2` / builder `0.4.653` are a matched set. The verification is then to
**run** the built image (`docker run`, `GET /`, and check the `nginx` binary's architecture), not to trust a green build
or the metadata label.

- **Alternative — trust the build and the label:** the recorded A/B showed both green while the image exited 127; a
  build is not a run.
- **Alternative — bump only the builder, holding nginx at 1.2.2:** `paketo-nginx` is moving a minor (1.2 → 1.3), and the
  new builder no longer bundles `1.2.2`, so holding it would make `pack` add the 1.2.2 buildpackage from the registry —
  the mismatched-pair hazard. Bump the set.

## Risks / Trade-offs

- **A mismatched pin builds cleanly and fails at runtime** → confirm the builder bundles the buildpack versions before
  bumping, then run the image and check the binary's architecture.
- **A host without amd64 emulation fails the image build in shifting ways** → a host-state failure is named as such, not
  read as a pin defect. (This host is Docker 29.5.2 on the `overlayfs`/containerd store, where the build _succeeds_ —
  the store alone is not the trigger; if the build fails with the layer-read error, set the daemon to `overlay2` or
  build on the amd64 CI path.)
