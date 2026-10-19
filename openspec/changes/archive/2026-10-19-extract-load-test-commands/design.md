# Design

## Context

See `proposal.md` — Why. The audit's opportunity carried a precondition, and this change runs it before anything moves.
Each fact below was measured on this branch, not taken from the report:

- The block the opportunity names is `AGENTS.md:854-881` — 28 lines, 3,006 bytes. The report's range (`~844-980`, "~136
  lines ≈ ~14KB") spans the whole `Build & Test` tail: 56 comment lines spanning every block the range covers, not only
  the load-test one.
- `openspec/specs/showcase/quality/load-tests/spec.md` already names twelve properties — `baseUrl`, `profile`, `rate`,
  `ratio`, `duration`, `kneeRate`, `thinkTime`, `detailShare`, `startShare`, `finishShare`, `hold`, `baselineFile` —
  each with its default and its property-provided scenario, and the SSE connection count by scenario alone (it carries
  neither an identifier nor a default); the spec also owns the tolerance, refresh, and record semantics.
- The `-P` spelling is the build's, not the spec's: `load-tests/build.gradle.kts` forwards a whitelist of Gradle
  properties into the simulation's system properties.
- `scripts/load-test-baseline.sh`'s docstring already states the run mechanics — the calibration ramp, the plateau,
  `TOLERANCE`, `REFRESH_BASELINE`, the withheld-reference rules, `report.md`, and the resource sampling.
- `baselineTrend` appears in neither the spec nor the script; its invocation and `-Precords` stay at root, where
  `README.md` also documents them.

## Goals / Non-Goals

**Goals:**

- Give every identifier the block carries a home before removing it: the spec for the command surface, the script's
  docstring for the floors, and the retained root pointer for what a reader must act on.
- Reduce the always-loaded surface by the block's measured size, not the report's estimate.
- Keep the root pointer carrying what a reader acts on — the invocation and the `:load-tests:test` warning.

**Non-Goals:**

- The second opportunity (the CI narrative): a separate unit with a boundary of its own.
- Moving the `check` note, the dependency reports, or the frontend block: the report's range included them by accident,
  and no opportunity names them.
- A mechanism that keeps the pointer and the spec in step: the specs-auditor and the doc-consistency sweep own that.

## Decisions

**Move the command surface, delete the semantics, and keep the pointer.** The spec already owns the threshold
derivation, the fallback values, and the baseline-replacement rules, so the block's prose restating them is deleted
rather than copied — the capture rule's "keep only what a reader needs to act and point at the spec". The `-P` spelling
and the `sseConnections` identifier have no home, so the delta adds them, while the trend task's invocation stays at
root with its pointer.

**Derive the boundary from the identifier audit, not from the report's description.** The report listed the `-P`
property list as missing when the spec already carries the properties; the boundary is drawn from what the audit shows
each identifier has, so the delta adds only what is genuinely absent.

## Risks / Trade-offs

- The `-P` spelling in the spec duplicates the build's whitelist → the spec states the spelling and the build stays the
  mechanism; a future property is added in both, which the `dependency-management`-style currency checks do not read, so
  the load-tests spec owns the list.
- The reduction is ~3KB, a quarter of what the opportunity was selected on → recorded in the proposal, and the unit is
  taken as its own small change rather than folded into a larger one.

## Migration Plan

None: guidance prose, a script docstring, and a spec delta, taking effect on the next read.

## Claims

Each fact the planning artifacts state about their evidence, with its source or deriving command. Seeded with
`python3 scripts/claim-scan.py`; its candidates are each resolved into a row or dismissed by kind below the table.

| #   | Claim                                                                                                                                        | Source / deriving command                                                                                               |
| --- | -------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| 1   | The block the opportunity names is `AGENTS.md:854-881`, 28 lines and 3,006 bytes                                                             | `git show HEAD:AGENTS.md \| sed -n '854,881p' \| wc -l -c`                                                              |
| 2   | The report's `844-980` range carries 56 comment lines across unrelated subjects                                                              | `git show HEAD:AGENTS.md \| sed -n '844,980p' \| grep -c '^# '`                                                         |
| 3   | Before this change the spec named twelve properties with their defaults and property-provided scenarios, and the SSE count by scenario alone | `git show HEAD:openspec/specs/showcase/quality/load-tests/spec.md \| grep -c "system propert"` (11)                     |
| 4   | The `-P` spelling is the build's: a whitelist forwarded into system properties                                                               | `load-tests/build.gradle.kts:25-26`                                                                                     |
| 5   | Before this change the script's docstring stated the run mechanics                                                                           | `git show HEAD:scripts/load-test-baseline.sh \| sed -n '1,12p'`                                                         |
| 6   | `baselineTrend` appears in neither the spec nor the script                                                                                   | `grep -c "baselineTrend" openspec/specs/showcase/quality/load-tests/spec.md scripts/load-test-baseline.sh` (0, 0)       |
| 7   | The CI opportunity is a separate unit with its own boundary                                                                                  | `docs/audits/2026-10-19.md`, its second extraction opportunity                                                          |
| 8   | The spec owns the tolerance, refresh, and record semantics                                                                                   | `grep -n "configured tolerance\|configured floor" openspec/specs/showcase/quality/load-tests/spec.md`                   |
| 9   | The reference records the tolerance floors as 50, 100, and 200 ms                                                                            | `grep -n 'floor' load-tests/src/gatling/resources/baseline.properties` (floorMeanMs=50, floorP95Ms=100, floorP99Ms=200) |

**Dismissals** (the scan's candidates resolved to neither a row nor a fact, one reason per kind the scan classifies):
**path candidates** name a file, task, or property — the sites themselves, not evidence claims; **cross-reference
candidates** name a record's own field or a comparison procedure rather than asserting a fact; **absolute-quantifier
candidates** are modal or structural words inside a rule's or a row's own sentence, not an independent universal claim
about the corpus; **count candidates** are numerals inside the unit's own artifacts and records, never a count of a
corpus the change rests on.
