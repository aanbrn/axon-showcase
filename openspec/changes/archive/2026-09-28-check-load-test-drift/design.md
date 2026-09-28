# Design

## Context

The measurement runs as `scripts/load-test-baseline.sh`: a calibration loop that raises the ceiling until `KneeFinder`
measures a knee, a `baseline` plateau at an operating point below it, and then two derived artifacts — the plateau's
per-request response times written to the committed reference `load-tests/src/gatling/resources/baseline.properties` (by
the `baselineStats` task's `BaselineStats`, reading Gatling's `LogFileReader`), and a dated record under
`docs/load-tests/` (the wrapper's assembled `report.md` plus a `## Notes` section, annotated by hand before commit).

Three facts shape the approach:

- `ShowcaseSimulation` reads the reference from the classpath and derives each performance profile's thresholds as
  `max(floor, factor x baseline)` with `factor = 5` and floors of 50/100/200 ms — deliberately loose, so it catches a
  catastrophic change only. A separate, tighter comparison is needed for drift.
- The reference is read and rewritten by different steps: the simulation reads it, `BaselineStats` writes it —
  unconditionally today, so the run that observes a regression also installs it as the new reference.
- The records are the only history (the reference is a single current file), and the generated ones carry the wrapper's
  Gatling summary — overall mean, 95th, 99th percentile, and throughput per plateau, in a fenced block with the columns
  total/OK/KO — plus the operating point and knee as prose bullets. The committed 2026-09-27 record measures a 6 ms
  mean, 9 ms p95, 14 ms p99 at 229 rps against an operating point of 122 units/s. Of the three committed records that is
  the only wrapper-generated one; the two of 2026-09-26 are hand-authored prose and tables with no summary block, so the
  trend reports them unreadable.

`load-tests` has only a `gatling` source set today; `java-conventions` wires JUnit Jupiter and AssertJ for every test
suite, but `src/test/java` and `src/gatling/java` are sibling source sets, so a unit test cannot see a class declared in
the latter. The pure-logic types — the reference's parse/render, the comparison, and the trend — therefore live in the
module's `main` source set, which both the `test` suite and the `gatling` suite see; only the classes that touch
Gatling's log reader stay in `gatling`.

## Goals / Non-Goals

**Goals:**

- A baseline run reports its figures against the reference it would replace, and a regression beyond a tolerance fails
  the run.
- A regression beyond the tolerance neither overwrites the reference the profiles assert against nor passes as normal,
  while a routine re-measurement still records itself.
- The dated records are readable as a series, so a drift is visible across runs and not only at the moment it trips.

**Non-Goals:**

- No change to the profiles' assertion policy, and no CI gate on the trend: the drift check runs where a baseline run
  runs (a developer's cluster, or a deliberate dispatch), and the trend is a reporting task a person invokes.
- No scheduled drift check: the check's identity is its base URL, since both the reference's name and its `target` guard
  key on it, so a CI run at the deployment smoke's hostname would compare against — and, within tolerance, replace — the
  committed local reference, and a shared runner's timings are host state rather than the system's (the reason the smoke
  records no performance numbers). Scheduling it is parked until a stable environment exists.
- No per-request history in the records, and no second, machine-only record format: the records stay human-first and the
  trend reads them as written.
- No alerting, scheduling, or issue creation — reading the series stays a deliberate act.

## Decisions

**D1 — The comparison and the conditional write live in `BaselineStats`, not the wrapper.** It already reads the
plateau's log and owns the reference; the wrapper would otherwise need a second, shell-side reader of Gatling's log to
compute the same numbers. The wrapper captures that step's status rather than exiting on it, so the run's record is
still written when the comparison fails (D4). _Alternative rejected:_ comparing in the wrapper — impossible without
duplicating the log reading, since the per-request figures are not in the run's stdout, only in the summary block and
the log.

**D2 — The comparison is per read and write request, on mean, 95th, and 99th percentile.** Those are the reference's own
keys and the units the profiles assert in, so the drift report speaks the same language as the artifact it judges. Only
a _higher_ figure is a regression; an improvement is reported but never a failure. _Alternative rejected:_ comparing the
run's overall summary — it mixes the read and write mixes, so a single slow request class would be diluted, and it is
what the record already carries for the trend to read.

**D3 — The tolerance is relative, floored per percentile.** A figure regresses when it exceeds
`max(floor, round(recorded x (1 + tolerance)))`, with the tolerance defaulting to `0.5` and the floors to 5 ms (mean),
10 ms (p95), and 20 ms (p99). Rationale: a fixed millisecond delta cannot fit both a 6 ms and a 90 ms request, while a
percentage alone cannot survive the records' scale — at a 6 ms mean, one millisecond of jitter is 17% — so the floors
hold the smallest figures above the noise the committed records show. Against the committed reference's real figures
(means 4-7 ms, p95 7-10 ms, p99 10-15 ms), the tightest thresholds are 6 ms (a 4 ms mean), 11 ms (a 7 ms p95), and 20 ms
(a 10 ms p99): above the spread the records show, and well below the drift the idea recorded (a p95 of 17-23 ms against
a 9-10 ms reference). The assertion policy's `factor = 5` sits far above this on purpose: it gates a catastrophic
change, while this gates a systematic one. The tolerance is overridable by property, so the numbers are a starting
policy rather than a constant to argue with in code. _Alternative rejected:_ a `tolerance` key in the reference — the
file is the artifact under judgment, and writing the policy that judged a run into the reference it wrote makes the
policy self-propagating and unreviewable in isolation; it also cannot be used to compare against a reference written
before the key existed.

**D4 — A within-tolerance run replaces the reference; a beyond-tolerance regression does not unless refreshed; the
record is written either way.** The reference is the artifact the profiles assert against, so a routine re-measurement
must be able to record itself: otherwise producing the reference would need a flag for the common case, and a first
measurement for a new target could never be recorded. What must not happen is installing a _regressed_ measurement as
the new normal, so the write is withheld exactly when a figure regresses beyond the tolerance and no refresh is intended
(and, even with a refresh intended, when the measurement recorded no request figures at all, since a figure-less
reference would silently send the profiles to their absolute thresholds); `REFRESH_BASELINE=1` accepts a regression
deliberately, reporting it. The record, by contrast, is evidence that the measurement happened, and the trend's value
depends on it: the wrapper therefore writes it above the `baselineStats` invocation, captures that step's status instead
of exiting on it, and propagates it at the end (`REFRESH_BASELINE` and that reorder in `scripts/load-test-baseline.sh`).
_Alternatives rejected:_ writing only on intent (the routine command would stop producing its own artifact, and a new
target could not be recorded without a flag); failing before writing the record (it would hide the very movement the
trend exists to show, so a rejected measurement would leave no trace in the series).

**D5 — The trend is a Gradle task (`baselineTrend`) that parses the records' fenced summary.** The records are the only
history, so the trend must read them; parsing the summary block the generated records carry keeps the record human-first
and needs no change to the wrapper's record writer; the two hand-authored records of 2026-09-26 have no such block and
are reported unreadable, which is required behavior rather than a gap. Each entry names its target from the record's
`- Target:` bullet, and its date from the filename's `YYYY-MM-DD` prefix (the slug only orders records of the same day,
since it names an environment rather than a target), and an unreadable record is reported rather than failing the task.
_Alternatives rejected:_ a machine-readable block appended to each record (a second format to keep in step, and the
record is annotated by hand — annotations would then sit beside a block the tool owns); trending the committed
references (there is only the current one per environment — history is precisely what the records hold).

**D6 — The reference's parse/render and the comparison are their own types.** `BaselineReference` holds the figures and
the parse/render, `BaselineDrift` compares two references and yields the deltas and the verdict, and `BaselineStats`
wires Gatling's reader to them. Only `BaselineStats` and `BaselineTrend` touch Gatling or the filesystem, so the
comparison — the part that is a check — is unit-testable against plain strings, which is what lets its failure be proven
on a known-bad input. _Alternative rejected:_ keeping the logic inline in `BaselineStats` — the check could then only be
exercised by a real Gatling log, which makes the negative control a live run rather than a test.

## Risks / Trade-offs

- **A tolerance that flags noise, or misses drift.** The floors and the relative tolerance are a starting policy, not a
  measured one; a task validates them against the spread the committed records show (and against the drift the idea
  recorded) before the change is reported done, and the property override exists because the right number may only
  emerge from runs on other environments.
- **The trend's parse is coupled to the generated summary's shape.** The wrapper's `SUMMARY_PATTERN` and Gatling's
  labels are the contract; a record whose block changes shape must degrade to "unreadable", which the requirement
  states, rather than failing the whole view.
- **A within-tolerance run still replaces the reference.** The tolerance is therefore what stands between a regression
  and the artifact: too loose, and a real degradation passes into it; too tight, and routine noise fails the run. The
  floors and the relative tolerance are a starting policy that task 4.2 validates against the committed numbers, and the
  run's delta report names what moved.
- **A record can now hold a rejected measurement.** The series then shows a run the check disbelieved; the entry's
  operating point lets a reader see the load it ran at, and the wrapper's output names the rejection.
