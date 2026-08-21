# ADR-0007: Dependency update report noise from build-environment constraints

Date: 2026-08-21

Status: Accepted

## Context

The `dependencyUpdates` report (ben-manes `io.github.ben-manes.versions` 0.61.0) shows a spurious update row for
`org.apache.logging.log4j:log4j-core [2.17.1 -> 2.26.1]` even though `log4j-core` is already resolved at `2.26.1`
everywhere. The false positive comes from `checkBuildEnvironmentConstraints = true`, which makes the plugin read
*external* build-tooling constraints and report the constraint's range floor as the "current version". The culprit is
`com.github.spotbugs:spotbugs-annotations:4.10.3`, which publishes a `{strictly [2.17.1, 3[}` constraint on
`log4j-core` as a Log4Shell (CVE-2021-44228) guard.

Three experiments confirm the mechanism:

- `log4j-core` resolves to `2.26.1` on every classpath; no `2.17.1` artifact exists.
- `filterConfigurations` excluding `spotbugs*` configurations does **not** remove the row — it comes from the
  build-environment-constraint path, not a project configuration.
- Disabling `checkBuildEnvironmentConstraints` removes the row, confirming the source.

## Decision

Treat the row as a documented known plugin limitation and keep the build behavior unchanged. The row is benign:
`log4j-core` is resolved at `2.26.1` everywhere, and the row only surfaces an external Log4Shell guard. The plugin's
maintainer confirms in upstream ben-manes/gradle-versions-plugin#755 that constraint upper bounds are used to determine
the "current version" and that the plugin deliberately reports possible constraint bumps — it is not a defect to fix
locally.

Alternatives considered and rejected:

- *Disable `checkBuildEnvironmentConstraints`* — removes the row but also drops visibility into genuine external
  constraint updates (e.g. a tool raising its minimum version). Too broad a trade-off for one noisy row.
- *`filterConfigurations` excluding `spotbugs*`* — proven ineffective: the row persists because the constraint is read
  via the build-environment path, not a project configuration.
- *Per-coordinate suppression in `config/dependency-updates/major-disabled.properties`* — inapplicable: `2.26.1` vs
  `2.17.1` is same-major, so the major-blocking rule cannot reject it.

## Consequences

- Future readers recognize the `log4j-core [2.17.1 -> 2.26.1]` row as known noise instead of chasing it as a real
  update.
- The `dependencyUpdates` output itself is unchanged; the noise row stays and is now explained.
- If the plugin changes behavior in a later version (e.g. resolving constraint floors to the resolved version), the
  documented finding becomes stale but harmless; the note is worth revisiting on a plugin upgrade.
