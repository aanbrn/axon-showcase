# Proposal

## Why

The stall diagnostics this repository now prints on a gateway component-test failure include an opt-in JFR recording
(`-Dshowcase.diagnostics.jfr=true`), kept off because its cost competes with the load the stall depends on. Enabling it
today needs an init script or a build edit, so a run cannot ask for it without touching the tree. A measured cost —
about 15% of the suite on a 10-CPU workstation, and no lower with the heaviest event removed — says it must stay off by
default.

## What Changes

- **`showcase-api-gateway/build.gradle.kts`** forwards `-Pdiagnostics.jfr` to the component-test JVM as
  `-Dshowcase.diagnostics.jfr=true`, for that suite only; the default stays off, and `-Pdiagnostics.jfr=false` leaves it
  off as `skipITs` does.
- **`JfrSummary` and `DiagnosticsControlCT`** name that route where a reader meets it: the failure-time hint and the
  class Javadoc say `-Pdiagnostics.jfr=true` (which the build forwards), and the control's assertion matches.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — the build command surface is guidance (`gradle-build`/`jvm-testing` packs), not a capability requirement.

## Impact

- **Test-scope only**: one property hook; no production code, no CI workflow, no dependency.
- **Enabling becomes one flag** (`./gradlew :showcase-api-gateway:componentTest -Pdiagnostics.jfr=true`) instead of an
  init script, and the default run is unchanged.
