# Design: Document dependency update report noise from build-environment constraints

## Context

See proposal.md — Why. The `dependencyUpdates` report (ben-manes `io.github.ben-manes.versions` 0.61.0) emits a
spurious `log4j-core [2.17.1 -> 2.26.1]` row. The change captures the root cause and records the decision to treat it
as a known plugin limitation rather than change build behavior.

The empirical evidence (see proposal.md) establishes the mechanism: `checkBuildEnvironmentConstraints = true` makes the
plugin read external constraints published by build-tooling modules and report the constraint's range floor as the
"current version".

## Goals / Non-Goals

**Goals:**
- Document the root cause and evidence so the noise row is understood and not chased as a real update.
- Record the explored alternatives and why they were rejected.

**Non-Goals:**
- Changing `dependencyUpdates` output (the noise row stays).
- Disabling `checkBuildEnvironmentConstraints`.
- Filtering configurations via `filterConfigurations`.
- Upgrading or patching the plugin.

## Decisions

**D1 — Treat this as a documented known plugin behavior, not a build change.**
The false positive is benign: `log4j-core` resolves to `2.26.1` everywhere, and the row only surfaces an external
Log4Shell guard. No behavior change is warranted. This matches the plugin's documented intent — the maintainer
confirms in upstream ben-manes/gradle-versions-plugin#755 that constraint upper bounds are used to determine the
"current version" and the plugin deliberately reports possible constraint bumps; it is not a defect to fix locally.
- Alternatives rejected:
  - *Disable `checkBuildEnvironmentConstraints`* — removes the row but also drops visibility into genuine external
    constraint updates (e.g. a tool raising its minimum version). Too broad a trade-off for one noisy row.
  - *`filterConfigurations` excluding `spotbugs*`* — proven ineffective: the row persists because the constraint is
    read via the build-environment path, not a project configuration.
  - *Per-coordinate suppression in `major-disabled.properties`* — inapplicable: 2.26.1 vs 2.17.1 is same-major, so the
    major-blocking rule cannot reject it.

**D2 — Capture the evidence trail in the docs.**
Record the three experimental proofs (resolution correct, filter ineffective, disabling the flag removes the row) and
the constraint source (`spotbugs-annotations` module metadata with the Log4Shell reason) so the finding is
reproducible without re-running the investigation.

## Risks / Trade-offs

- [A future reader sees the `[2.17.1 -> 2.26.1]` row and treats it as a real update] → The documentation names the
  exact mechanism and source so the row is immediately recognizable as noise.
- [The plugin changes behavior in a later version (e.g. resolving constraint floors to the resolved version)] → The
  documented finding becomes stale but harmless; the note is worth revisiting on a plugin upgrade.
- [Documentation-only means no automated guard] → Acceptable: the finding is explanatory, and no build failure or
  security issue is at stake.
