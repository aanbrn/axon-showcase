# Design

## Context

See `proposal.md` — Why. The rows were re-resolved on 2026-10-12 (`./gradlew buildpackUpdates`) and are unchanged from
the issue refreshed on 2026-10-10.

The pairing rule is recorded in `AGENTS.md` and comes from a real failure: a `paketo-nginx` pin _absent_ from the
builder makes `pack` add that buildpackage from the registry, and the added one resolves to an **arm64** slice even
though the builder is published `linux/amd64` only — the mismatched pair then fails at `exec: nginx: not found`, while a
matched pair serves a proper x86-64 nginx. So the pins are chosen from what the builder bundles, established by
`pack builder inspect` **before** choosing them.

The inspect result for `paketobuildpacks/builder-jammy-base:0.4.659`:

- `paketo-buildpacks/nginx` **`1.3.0`** (the registry has `1.3.1`; the builder does not bundle it)
- `paketo-buildpacks/procfile` `5.15.1`/`5.15.2`/`5.15.3`, the order resolving to `5.15.3`

The builder's own run image stays the floating `paketobuildpacks/run-jammy-base:latest` (base-OS security is `pack`'s,
not a pin this repo freezes).

## Goals / Non-Goals

**Goals:**

- Move the builder to `0.4.659` and procfile to the version the new builder bundles (`5.15.3`), keeping the pair
  matched.
- Verify by running the built image, which is the only check that catches a wrong-architecture binary.

**Non-Goals:**

- Taking the report's `paketo-nginx 1.3.1` row — the builder bundles `1.3.0`, so the row is not actionable (see Risks).
- Freezing the run image.

## Decisions

**Hold `paketo-nginx` at the builder's bundled `1.3.0` and record why.** The report's job is to name newer registry
releases; the pairing rule decides which are takeable. Pinning `1.3.1` would trade a matched pair for a registry fetch
of a possibly arm64 buildpackage — the exact failure the rule exists to prevent, and one a green build does not catch.

**Verify by running the image, not by the build or its metadata.** A buildpack or builder bump can install a binary of
the wrong architecture and still build cleanly, and the metadata label names the _buildpack version_ while a wrong-arch
binary comes from the resolved buildpackage slice — so a label reading `1.3.0` does not clear a `1.3.0` pin.
`docker run`

- `GET /` + the nginx binary's architecture is the check; the image is amd64-only, so its architecture is inspected
  explicitly.

## Risks / Trade-offs

- The image build runs the amd64 lifecycle under emulation on the arm64 development host → a build failure is host state
  until proven otherwise (the recorded colima/QEMU hazard), which is why the _run_ is the verification and the baseline
  behaviour matters.
- The `paketo-nginx 1.3.0 -> 1.3.1` row persists in every report → recorded here and in the proposal as deliberately not
  actionable, so it is not chased again.

## Migration Plan

None: a toolchain pin, taking effect on the next image build.
