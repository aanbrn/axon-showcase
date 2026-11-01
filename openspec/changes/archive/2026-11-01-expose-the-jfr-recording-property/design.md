# Design

## Context

`instrument-the-gateway-ct-stall` (merged as `521a93ee`) added the stall diagnostics and left the JFR recording opt-in
via `-Dshowcase.diagnostics.jfr=true`, with the cold-run cost comparison as the prerequisite for defaulting it on
(`docs/ideas.md`, `## 2026-10-06`). This unit measures it **on a workstation** and adds only the plumbing that result
leaves useful; the paired cold-run comparison the parked entry names stays open, because a local slice cannot establish
what the recording costs on a loaded four-vCPU runner.

**The local measurement** —
`:showcase-api-gateway:componentTest --tests showcase.api.telemetry.TelemetryControllerCT --rerun`, three runs each on a
10-CPU workstation, suite time summed from the JUnit XML so the JFR control (which only runs when the flag is on) is
excluded from the comparison:

| Configuration                    | Runs (s)            | Median |
| -------------------------------- | ------------------- | ------ |
| recording off                    | 3.270, 3.016, 3.182 | 3.18   |
| recording on, all three events   | 3.646, 3.333, 3.737 | 3.65   |
| recording on, park event removed | 4.263, 3.723, 3.989 | 3.99   |

The overhead is about **0.47 s, ~15% of the suite**, and removing the heaviest event (park, with stack traces) did not
lower it in these runs. Whether the recording itself dominates is a hypothesis: three runs cannot isolate the dominant
cost, and settling it was not needed to decide the default. The full suite (135 tests) reads 8.34–8.90 s off against
9.69–10.06 s on, but that delta includes the JFR control and double-counts.

## Goals / Non-Goals

**Goals:**

- Make the recording requestable per run with one flag, without an init script or a tree edit.
- Keep the default unchanged: off.

**Non-Goals:**

- Defaulting the recording on: the stall this instruments appears on a loaded 4-vCPU CI runner, where the recording's
  cost is unmeasured, and the recorded workstation overhead above is already more than a run should add blind to the
  load the stall depends on — the one local stall observed during the previous unit's development happened on a heavier
  recording.
- A `workflow_dispatch` input on `ci.yml` that passes the flag: it is a CI change owing a `merge-governance` delta, and
  the next ordinary stall is already covered by the always-on timeline and scheduler aggregates.
- Reducing the event set: dropping the park event gained nothing in these runs, but that comparison cannot say what each
  event costs under load, so trimming it would trade a signal for an unmeasured saving.

## Decisions

**Forward a Gradle property to the suite's JVM args, and only that suite, read the way its siblings are.**
`showcase-api-gateway/build.gradle.kts` already owns the component-test JVM arguments; the hook adds one entry unless
the property is the `false` value, the shape `skipITs` and `coverage.gate.enabled` already use — so
`-Pdiagnostics.jfr=false` leaves the recording off rather than enabling it. The property is read at configuration time
through `providers`, so the configuration cache records it as an input, and no other suite or module changes behaviour.
_Alternatives rejected:_ an environment variable read by the test code (invisible in the build's inputs and untestable
from Gradle), a CI workflow input (a CI change and a spec delta for a need that has not arisen), and defaulting the flag
on (the measurement above).

## Risks / Trade-offs

- **A property that is set but misspelled silently leaves the recording off** → the suite's own two property-gated
  controls make the state visible: the JFR control runs only when the recording is on, and the disabled-render control
  only when it is off, so a run's skip line names which state it was in.
- **Configuration-cache invalidation** → the property is read through `providers`, which the configuration cache records
  as an input; both states were exercised in `tasks.md` 2.1.

## Migration Plan

None: an opt-in flag for a test suite.

## Claims table

| Claim                                                                                                               | Source                                                                                                             |
| ------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------ |
| The recording costs ~15% of the telemetry suite in three runs, and dropping the park event did not lower the median | the Context table; that the recording itself dominates is a hypothesis three runs cannot settle                    |
| The default stays off                                                                                               | `JfrSummary`'s own gate reads `-Dshowcase.diagnostics.jfr`, and the run without the property skips the JFR control |
| The property reaches the test JVM                                                                                   | `tasks.md` 2.1: without it the JFR control skips, with it the disabled-render control skips                        |
| No capability spec covers this hook                                                                                 | `git grep -c` over `openspec/specs/**/spec.md` for `componentTest`, `JFR` and `diagnostics.jfr`                    |

### Dismissals

`scripts/claim-scan.py`'s candidates are dismissed by kind, one reason each:

- `absolute` — the quantifier words sit in statements whose sources the table states.
- `count` — the scanned numerals are the table's own figures, or version strings the scanner reads as counts.
- `path` — a scanned path or command names the repository surface the change edits or the source a row cites.
- `cross-ref` — the scanned identifiers are the property and system-property names this unit defines.
