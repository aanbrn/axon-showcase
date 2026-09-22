## Why

The update-check tracker issues (2026-09-21) named three actionable bumps: the observability chart `91.4.0` → `91.4.1`,
the Paketo builder `0.4.642` → `0.4.644`, and `paketo-nginx` `1.2.0` → `1.2.1`. The nginx bump was held back by a
recorded `AGENTS.md` constraint; testing that constraint showed it was mis-stated — the pins work when bumped together —
so all three are taken.

## What Changes

- Bump `prometheus-community-stack` to `91.4.1`, verified live (release deployed, pods ready, Prometheus targets up,
  Grafana datasources wired).
- Bump `paketo-builder-jammy-base` to `0.4.644` **and** `paketo-nginx` to `1.2.1` **together** — the matched pair builds
  an image that runs and serves an x86-64 nginx.
- Correct the recorded nginx hold-back: the failure was never `1.2.1` "not working" but the **mismatch** between an
  `nginx@1.2.0` pin and a builder bundling `1.2.1`, which forces `pack` to add `1.2.0` from the registry as an **arm64**
  slice and yields an AArch64 `nginx` in an amd64 image. Bumping both together avoids the registry-add path.
- Update the `AGENTS.md` manual install command to `--version 91.4.1`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — the bumps change no spec'd behavior (`skip_specs`). The recorded constraint they correct lives in `AGENTS.md`,
not a capability spec.

## Impact

- **Build**: `gradle/libs.versions.toml` — three version bumps (`prometheus-community-stack = "91.4.1"`,
  `paketo-builder-jammy-base = "0.4.644"`, `paketo-nginx = "1.2.1"`). No build logic changes; the chart pin feeds
  `helm.releases`' `kps` entry automatically, and the buildpack pins feed the web UI `dockerBuildImage` task.
- **Tests**: none — the bumped coordinates are consumed by the Helm deployment and the web UI image build, not the JVM
  test suites; `check` stays green.
- **Deployment**: the Helm deployment moves to `kube-prometheus-stack-91.4.1`; the web UI image is built by the bumped
  builder + buildpacks (verified by running it). No values or template changes.
