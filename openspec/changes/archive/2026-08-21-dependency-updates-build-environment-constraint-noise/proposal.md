# Proposal: Document dependency update report noise from build-environment constraints

## Why

The `dependencyUpdates` report shows a spurious update row for `org.apache.logging.log4j:log4j-core [2.17.1 -> 2.26.1]`
even though `log4j-core` is already resolved at `2.26.1` everywhere. The false positive comes from
`checkBuildEnvironmentConstraints = true`, which makes the ben-manes `gradle-versions-plugin` read *external*
build-tooling constraints and report the constraint's range floor as the "current version". The culprit is
`com.github.spotbugs:spotbugs-annotations:4.10.3`, which publishes a `{strictly [2.17.1, 3[}` constraint on
`log4j-core` as a Log4Shell (CVE-2021-44228) guard. This is a known plugin behavior, not a real dependency issue —
the plugin's `checkBuildEnvironmentConstraints` is designed to report constraint bumps (see upstream
ben-manes/gradle-versions-plugin#755, where the maintainer confirms the upper bounds of constraints are used to
determine the "current version" and the plugin intentionally reports possible constraint upgrades). We want to record
the finding and the reasoning so future readers do not chase it as a real update.

## What Changes

- No build behavior changes.
- Document the known plugin limitation in the dependency-update reporting docs:
  - `AGENTS.md` (Build & Test commands, `dependencyUpdates` note)
  - `README.md` (Dependency Updates section)
  - Optionally an ADR capturing the reasoning and the explored alternatives.
- Capture the evidence trail so the finding is reproducible:
  - `log4j-core` resolves to `2.26.1` on every classpath (no `2.17.1` artifact exists).
  - `filterConfigurations` excluding `spotbugs*` configs does **not** remove the row — it comes from the
    build-environment-constraint path, not a project configuration.
  - Disabling `checkBuildEnvironmentConstraints` removes the row, confirming the source.
  - The constraint lives in `spotbugs-annotations:4.10.3` module metadata with reason `CVE-2021-44228, ...`.

## Capabilities

### New Capabilities

None. This is a documentation-only change with no spec-level behavior change (`skip_specs: true`).

### Modified Capabilities

None. No requirement changes; the `dependencyUpdates` behavior itself is unchanged.

## Impact

- **Code**: none.
- **Docs**: `AGENTS.md`, `README.md`, and optionally a new ADR under `docs/adr/` (e.g. ADR-0007).
- **Build**: no change to `dependencyUpdates` output; the known noise row remains and is now explained.
- **Tests**: none; this is a documentation-only change.
