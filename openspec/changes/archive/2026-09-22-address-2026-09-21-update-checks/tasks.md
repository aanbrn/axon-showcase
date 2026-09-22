# Tasks

## 1. Take the observability chart bump

- [x] 1.1 Bump `prometheus-community-stack` from `91.4.0` to `91.4.1` in `gradle/libs.versions.toml`
- [x] 1.2 Verify with the live install + smoke test the spec requires: `helmInstallKpsToLocal` deploys
      `kube-prometheus-stack-91.4.1`, the monitoring pods are Ready, Prometheus targets are up, and Grafana datasources
      are wired
- [x] 1.3 Uninstall the verification release and remove the `monitoring` namespace, restoring the cluster to its
      pre-change state
- [x] 1.4 Update the manual install command in `AGENTS.md` to `--version 91.4.1`

## 2. Take the Paketo bumps together

- [x] 2.1 Bump `paketo-builder-jammy-base` to `0.4.644` and `paketo-nginx` to `1.2.1` in `gradle/libs.versions.toml`
      (together — the mismatch is the defect)
- [x] 2.2 Verify by building and **running** the web UI image with the matched pair: the container runs, `GET /` returns
      200, and the image's nginx is x86-64 (`od -An -tx1 -j18 -N2` gives `3e 00`)
- [x] 2.3 Record the contrast that justifies moving them together: the matched pair (`0.4.644` + `1.2.1`) runs, while
      the mismatched pair (`0.4.644` + `1.2.0`, forcing the registry-add path) exits 127

## 3. Record and report

- [x] 3.1 Correct the `AGENTS.md` Paketo paragraph: the constraint was the mismatch between an `nginx@1.2.0` pin and a
      builder bundling `1.2.1` (registry-add → arm64 slice), not `1.2.1` "not working"
- [x] 3.2 Append the verified contrast to the "buildpack pin is verified by running the image" gotcha
- [x] 3.3 Report upstream as a comment on `paketo-buildpacks/nginx#1340` with a runnable reproduction and the resolved
      arm64 digest

## 4. Verify

- [x] 4.1 `./gradlew spotlessApply` and `spotlessCheck`
- [x] 4.2 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`
- [x] 4.3 `openspec validate --all` passes (23 items — this change, with `skip_specs`)
- [x] 4.4 Confirm the builder's bundled nginx matches the pin (`paketobuildpacks/builder-jammy-base:0.4.644` carries
      `paketo-buildpacks_nginx/1.2.1`)
