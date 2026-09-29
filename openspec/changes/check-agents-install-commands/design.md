# Design

## Context

See `proposal.md` — Why. The root build already pairs each Helm chart coordinate with its catalog pin in
`helmChartChecks` (`chartRef` → `pinnedVersion`), and `verifyInfraImageVersions` is the sibling check that resolves a
chart's preconfigured image tag. `AGENTS.md`'s Kubernetes Deployment block lists
`helm install <name> <chartRef> --version <v>` lines (the app chart's `./helm/chart` line carries no `--version`).

## Goals / Non-Goals

**Goals:**

- Fail `check` when an `AGENTS.md` manual install command's `--version` disagrees with the catalog pin for its chart.

**Non-Goals:**

- Resolving anything from a chart repository — the block quotes the catalog pin directly, so no Helm client or network
  is needed (unlike `verifyInfraImageVersions`).
- Verifying the block's namespaces or the app-chart line.

## Decisions

### Decision: a pure rules object parses the block; the task turns a mismatch into a failure

`InstallCommandRules` parses the `AGENTS.md` file's `helm install <name> <chartRef> --version <v>` lines into
`(chartRef, version)` pairs and returns a mismatch reason for a chart whose version differs from its pin (or that has no
pin). The pure logic is testable without Gradle or Helm, mirroring `InfraImageVersionRules`.

### Decision: the check is a separate task, cacheable on the block and the pins

`verifyInstallCommands` takes the `AGENTS.md` file and the chart checks as inputs and writes a result file, so it is
cacheable and re-runs only when the block or a pin changes — it does not need the Helm resolution
`verifyInfraImageVersions` performs, so it is a sibling task rather than folded into it.

- **Alternative — fold it into `verifyInfraImageVersions`:** that task resolves charts over the network and is cacheable
  on different inputs; the doc check shares none of that.
- **Alternative — a build-logic unit test alone:** a test cannot see the real `AGENTS.md` block or the catalog pins; it
  would not catch a stale command.

## Risks / Trade-offs

- **The block's shape changes** → the parser matches the documented `helm install … --version` form; a line without a
  `--version` (the app chart) is skipped, while a `--version` for a chart the catalog does not pin fails rather than
  passing silently.
