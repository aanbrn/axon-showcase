# Design

## Context

See `proposal.md` — Why. The 2026-10-19 report's agent-tooling findings were verified live on this branch before
planning, each against the set its own rule names as authoritative rather than against the report's prose:

- `AGENTS.md`'s Build & Test `check` comment lists the commit-hygiene members without `testReviewRouting`, while
  `build.gradle.kts` registers it and its `check` block depends on it between `testReviewRounds` and
  `verifyTrackedIgnoredFiles`.
- `AGENTS.md`'s model-pin gotcha says to exclude the vision agent's `-vision-exp` pin and gives
  `deepseek-v4-flash-vision-exp` as the pin a flash/pro bump leaves alone, while `.opencode/agent/vision.md` and
  `README.md` both read `opencode-go/claude-haiku-5-5`. The gotcha's historical sentence (the pin's `-vision-exp` era)
  stays as history.
- `AGENTS.md`'s Prerequisites Python list and `README.md`'s Prerequisites table row both name `claim-scan.py` and
  `review-rounds.py` without `review-routing.py`, while `scripts/review-routing.py` exists and the review gate's step 5
  invokes it. These are the parked 2026-10-18 idea's second site and its README mirror.
- `AGENTS.md`'s tooling-placement clause partitions `scripts/` tools into agent-driven-with-a-trigger, no-agent-caller,
  and hook/Gradle-driven, and `review-routing.py` falls in none of the three: the review gate invokes it inline and
  `testReviewRouting` checks it.

## Goals / Non-Goals

**Goals:**

- Correct each reported enumeration and claim to its full or current set, re-derived from the authoritative source.
- Sweep the sibling copies of the corrected enumerations — the README Prerequisites row and the tooling-placement clause
  — so one corrected fact does not leave a second site stating the old one.
- Retire the `docs/ideas.md` entry the report's finding implements.

**Non-Goals:**

- The report's architecture advisory — ADR-0017's `Revisit when:` condition appearing met by Spring gRPC's EOL 0.x line.
  An advisory is the owner's decision, and whether an EOL first-party release satisfies the ADR's intent is theirs.
- The report's two extraction opportunities (the load-test and CI blocks). An extraction opportunity is a design
  recommendation, never drained by `/apply-audit-reductions`, and needs the owner's selection and its own change.
- A mechanism to keep the enumerations current: the scheduled agent-tooling audit reports exactly this class.

## Decisions

**Apply each finding from the set its rule names as authoritative, not from the report's prose.** The `check` comment is
derived from `build.gradle.kts`'s `check` block, the Prerequisites list from `scripts/*.py`, the tooling-placement
clause from the review gate's own procedure, and the model-pin clause from `.opencode/agent/vision.md`. Each correction
therefore carries the complete set rather than the single member the finding happened to name.

**Sweep the sibling copies in the same edit pass.** The Prerequisites list has a README mirror and the corrected
`review-routing.py` fact has a second AGENTS.md site (the tooling-placement clause), so both are corrected here rather
than left for the next report — the same defect shape two prior units' review rounds caught.

**Keep the model-pin gotcha's historical sentence and correct only its live clause.** The clause that instructs a bump
is what a reader acts on; the sentence recounting the pin's `-vision-exp` era is a record of how the pin reached its
current model and stays accurate as history.

## Risks / Trade-offs

- Each corrected enumeration can go stale again as its set grows → the scheduled agent-tooling audit reports this class,
  so no new guard is added here.
- The model-pin clause names a pin that will move again → the clause's own instruction is to grep the old id when
  bumping, which is what a future sweep follows; the historical sentence is what a reader needs to tell the two eras
  apart.

## Migration Plan

None: guidance prose and a scratchpad entry, taking effect on the next read.

## Claims

Each fact the planning artifacts state about their evidence, with its source or deriving command. Seeded with
`python3 scripts/claim-scan.py`; its candidates are each resolved into a row or dismissed by kind below the table.

| #   | Claim                                                                                                  | Source / deriving command                                                                                                        |
| --- | ------------------------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------- |
| 1   | `testReviewRouting` is registered and named in the `check` block                                       | `grep -n "testReviewRouting" build.gradle.kts` → 526 (registration), 649 (the `check` block)                                     |
| 2   | The vision pin is `opencode-go/claude-haiku-5-5`                                                       | `grep -n "model:" .opencode/agent/vision.md` → 7; `grep -n "claude-haiku" README.md` → 364                                       |
| 3   | In the live guidance `deepseek-v4-flash-vision-exp` appears only at `AGENTS.md:1791`                   | `grep -rn "deepseek-v4-flash-vision-exp" AGENTS.md README.md .opencode/ docs/adr/ docs/ideas.md` → `AGENTS.md:1791`              |
| 4   | `scripts/review-routing.py` exists and no `.opencode/` file names it                                   | `ls scripts/*.py`; `grep -rn "review-routing" .opencode/` → no matches                                                           |
| 5   | The review gate's routing step invokes `scripts/review-routing.py`                                     | `AGENTS.md:361`                                                                                                                  |
| 6   | The parked idea named the two enumerations `#628` left stale                                           | `git show HEAD:docs/ideas.md` → the 2026-10-18 entry                                                                             |
| 7   | `settings.gradle.kts` declares no Redgate repository, and #639 removed the architecture finding's site | `grep -c "red-gate" settings.gradle.kts` → 0; `git show --stat f6fb21ff`                                                         |
| 8   | No capability spec names the corrected sites' identifiers                                              | `grep -rl "testReviewRouting\|review-routing.py\|claude-haiku-5-5" openspec/specs/` → no matches                                 |
| 9   | The analogue audit-fix unit ran as a `skip_specs: true` change with no delta                           | `openspec/changes/archive/2026-10-12-apply-2026-10-11-audit-findings/.openspec.yaml`                                             |
| 10  | The `check` block also carries `build-logic`'s `:test`, which the `check` comment did not name         | `build.gradle.kts:661`; `git show HEAD:AGENTS.md \| sed -n '/^# Check runs:/,/^# (a Docker-free/p' \| grep -c "build-logic"` → 0 |

**Dismissals** (the scan's candidates resolved to neither a row nor a fact, one reason per kind): **path candidates**
name a file, script, or model id — the sites themselves, not evidence claims; **cross-reference candidates** name a
record's own field or a comparison procedure rather than asserting a fact; **absolute-quantifier candidates** are modal
or structural words inside a rule's or a row's own sentence, not an independent universal claim about the corpus;
**count candidates** are numerals inside the unit's own artifacts and records, never a count of a corpus the change
rests on.
