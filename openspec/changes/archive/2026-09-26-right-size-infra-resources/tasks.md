# Tasks

## 1. Measure

- [x] 1.1 Extend `scripts/load-test-baseline.sh` to sample the whole cluster — `kubectl top pods -A` (all namespaces)
      instead of `-n "$NAMESPACE"`, removing the now-unused `NAMESPACE` variable — and update the report's resource
      heading. Verify by re-running and reading the report's resource section.
- [x] 1.2 Re-run `./scripts/load-test-baseline.sh` against the local Helm cluster and record the whole-stack measurement
      as `docs/load-tests/2026-09-26-whole-stack.md` (target shape, method, per-service numbers including the
      `monitoring` and kube-system namespaces). Verify it matches the run's `report.md`.

## 2. Size

- [x] 2.1 Size the infrastructure local values (`helm/values/axon-showcase-db-events/values-local.yaml`,
      `helm/values/axon-showcase-kafka/values-local.yaml`, `helm/values/axon-showcase-os-views/values-local.yaml`) from
      the recorded measurement — CPU requests near the measured steady state, memory requests at the working set, memory
      limits with headroom, and no `limits.cpu`.
- [x] 2.2 Size the observability local values (`helm/values/kps/values-local.yaml`,
      `helm/values/tempo/values-local.yaml`) from the recorded measurement the same way.
- [x] 2.3 Render each changed chart with its local values — the repository's Helm client, or `helm template` with the
      release's values file — and confirm the sized containers show the new requests and no `limits.cpu` (the charts'
      init/auxiliary containers keep their defaults); no gate validates the resource values (`verifyInfraImageVersions`
      reads these files only for `image.tag`), so the render is the check.

## 3. Verification

- [x] 3.1 Install the resized releases
      (`./gradlew helmInstallKpsToLocal helmInstallTempoToLocal     helmInstallAxonShowcaseDbEventsToLocal helmInstallAxonShowcaseKafkaToLocal     helmInstallAxonShowcaseOsViewsToLocal`)
      and then the app release (`helmInstallAxonShowcaseToLocal`) so its pre-upgrade hooks re-run — the infra upgrade
      restarts the ephemeral state stores and wipes their data. Confirm every pod is Ready and the gateway serves a
      request.
- [x] 3.2 Re-run the baseline plateau against the resized stack (`./scripts/load-test-baseline.sh`) and confirm it stays
      at 0 failed requests with no OOMKilled or restart spikes — the trim's real risk is a memory limit under load,
      which an idle install cannot show.
- [x] 3.3 `./gradlew spotlessApply` then `spotlessCheck` pass; `openspec validate --changes` and
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` pass.
