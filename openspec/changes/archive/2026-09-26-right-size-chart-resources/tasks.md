# Tasks

## 1. Values and docs

- [x] 1.1 Replace the five `resources` blocks in `helm/chart/src/main/helm/values.yaml` with the measured-with-headroom
      set from the proposal — the four JVM services at `cpu` requests `1`/`500m`/`500m`/`100m`, memory `0.5Gi`/`1Gi`,
      and the web UI at `50m`/`64Mi`/`128Mi` — removing every `limits.cpu`. Verify by rendering the chart.
- [x] 1.2 Remove the implemented "Rethink resource requests/limits in the Helm chart and the local target" entry from
      `docs/ideas.md`. Verify no other entry references it.
- [x] 1.3 Update `docs/load-tests/2026-09-26.md`'s Target bullet to describe the chart defaults it ran against in the
      past tense and correct both inaccurate halves of its parenthetical ("0.5–0.75 GiB requests" and "1–1.5 GiB
      limits"), so the record does not read as the current defaults.

## 2. Verification

- [x] 2.1 `./gradlew :helm:chart:helmLintMainChartFull :helm:chart:helmLintMainChartMinimal` pass, and `helm template`
      of the packaged chart shows the new requests and no CPU limits on any container — run
      `./gradlew :helm:chart:helmPackageMainChart` first to produce `helm/chart/build/helm/charts/axon-showcase` (the
      source tree carries Gradle-filtered placeholders, per the deployment gotcha).
- [x] 2.2 `./gradlew spotlessApply` then `spotlessCheck` pass; `openspec validate --changes` passes.
