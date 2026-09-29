# Design

## Context

See `proposal.md` — Why. The web UI image is built by `pack` (`frontend-conventions`' `dockerBuildImage`, a
`PackBuildImageTask`) over `build/dist`, passing the version-pinned Paketo NGINX and Procfile buildpacks; the builder
(`paketo-builder-jammy-base`) is also pinned. The pins are catalog-owned (`paketo-nginx`, `paketo-procfile`,
`paketo-builder-jammy-base`), and the builder and the buildpacks it bundles must be bumped together.

## Goals / Non-Goals

**Goals:**

- Move the builder and its bundled buildpacks to the versions the check reports, as a set, and verify the image runs.

**Non-Goals:**

- A source or application change; the image's contents come from the built bundle, unchanged.

## Decisions

### Decision: bump all three pins together and verify by running the image

The builder and the buildpacks it bundles are one unit: a `paketo-nginx` pin the builder does not bundle makes `pack`
add it from the registry, and the added buildpackage can resolve to an **arm64** slice even though the builder is
published `linux/amd64` only — the mismatched pair fails at runtime with `exec: nginx: not found` while the build
succeeds and the metadata label names the pinned version. So the verification is to **run** the built image
(`docker run`, `GET /`, and check the `nginx` binary's architecture), not to trust a green build or the metadata label.

- **Alternative — trust the build and the label:** the recorded A/B showed both green while the image exited 127; a
  build is not a run.

## Risks / Trade-offs

- **A mismatched pin builds cleanly and fails at runtime** → run the image and check the binary's architecture, per the
  buildpack-pin gotcha.
- **A host without amd64 emulation fails the image build in shifting ways** → a host-state failure is named as such, not
  read as a pin defect.
