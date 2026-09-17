# Proposal: Hold the NGINX buildpack back at 1.2.0

## Why

The web UI image can no longer be built on the primary development machine. Since #271 took `paketo-nginx` to 1.2.1, the
build there fails (`could not find label 'io.buildpacks.buildpackage.metadata'`) or, when it did build, produced an
AArch64 `nginx` inside an amd64 image; the identical build with 1.2.0 succeeds and serves, and x86 CI builds 1.2.1 fine.
The cause is unsettled — the build failure's signature is the one the host-state gotcha documents, and the architecture
symptom is tracked upstream as paketo-buildpacks/nginx#1340 — so this holds a machine working rather than reporting a
repo-wide defect.

## What Changes

- Pin `paketo-nginx` back to `1.2.0` in `gradle/libs.versions.toml`; the builder and procfile pins are unchanged.
- Record why in `AGENTS.md`, with the upstream reference and the close-out: re-test the arm64 build after a
  container-runtime change or when #1340 is resolved, and re-take the bump if it passes.

## Capabilities

### New Capabilities

<!-- none — `skip_specs: true`; no requirement states a buildpack version. -->

### Modified Capabilities

<!-- none — the buildpack pins are catalog-owned and reported by `buildpackUpdates`; no spec describes them. -->

## Impact

- **Build**: `gradle/libs.versions.toml` — one pin; the image builds again on arm64 hosts.
- **Docs**: `AGENTS.md` — the rationale, the reference, and the close-out.
- **Behavior**: the image ships the nginx 1.2.0 buildpack (nginx 1.31.4) instead of 1.31.5, and `buildpackUpdates` keeps
  naming the coordinate until the hold is retired.
