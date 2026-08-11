## Context

See proposal.md - Why. The `helm:chart` module uses the citi gradle-helm-plugin (`helm-conventions`), which already
wires a `helmLintMainChart` task into the package flow. The lint task passes `valueFiles` to `helm lint`; the plugin
also supports multiple named lint `configurations`, each rendered with its own value set. The chart's default
`values.yaml` only exercises the default branches, so optional/disabled branches need extra value files.

## Goals / Non-Goals

**Goals:**

- Catch template errors in non-default branches via a fast, cluster-free `helm lint` gate.
- Cover both enabled and disabled conditional branches.

**Non-Goals:**

- No change to chart templates or rendered manifests.
- No cluster-based validation (`helm template --validate`) or `helm test` hooks.

## Decisions

- **Use lint `configurations` instead of a single merged value file.** Each configuration produces its own lint task
  (`helmLintMainChartFull`, `helmLintMainChartMinimal`) and the umbrella `helmLintMainChart` runs both. A single
  merged file would cancel out opposing values (e.g. ingress enabled vs. disabled). Alternative considered: one
  `valueFiles` entry on the lint block only - rejected because it cannot express mutually exclusive value sets.
- **Enable `strict` and `withSubcharts`.** `strict` turns linter warnings into failures; `withSubcharts` also lints
  the Bitnami `common` dependency (0 failures verified).
- **Place value files under `src/test/helm/`.** Follows the plugin's documented convention for lint value files and
  keeps them out of the packaged chart sources.

## Risks / Trade-offs

- [citi gradle-helm-plugin tasks are configuration-cache incompatible] → Run lint tasks with `--no-configuration-cache`
  (documented in AGENTS.md or the task invocation); acceptable since lint is a build-gate, not a hot-path task.
- [Strict lint may break on future chart changes] → That is the intent: warnings surface as build failures during
  development, not at release time.
