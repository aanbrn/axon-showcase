## Context

The `/retrospective` command gathers a digest with `scripts/experience-analysis.sh` and hands it to the
`experience-analyzer` subagent. The analyzer's definition tells it to reuse the units' `docs/process-defects/` records
(`.opencode/agent/experience-analyzer.md:34`); the digest and the command never supply them, and the record files are
not reachable from anything the digest prints.

The latest report shows both halves of the defect. Its causal material includes incident details the digest never
carried (`docs/retrospectives/2026-10-31.md:83–85` cites the 2026-10-30 pass's defects, which live only in
`docs/process-defects/generalize-process-defects-2026-10-30.md`), so some session read the repository — but the contract
neither supplies nor requires that read, and the retrospective unit's own defect file records nothing
(`docs/process-defects/measure-the-two-mechanisms.md` is another unit's). Its top-class figure, `claim-unreproducible`
275 (20% of all misses), divides a count of round-label occurrences by the compliance-miss total: `tally()` increments
once per label (`scripts/review-rounds.py:123–134`) while `figures()` sums the miss column separately (`:137–139`), so
the numerator and the denominator count different things.

The repository's recorded direction constrains the fix: `docs/decisions/2026-10-31-mechanism-measurement.md:45` decides
"No third mechanism", so this change adds no measurement, ledger, or auditor. It repairs an input and an aggregation
unit, which is what that decision leaves room for.

## Goals / Non-Goals

**Goals**

- Make the window's process-defect records a standard part of the handoff, as an index that states its own count,
  revision, and boundary, so an empty selection is visible rather than silent.
- Require a causal claim that rests on a record to have read that record's body and to name it.
- Count one contribution per canonical class per round, and state the unit in the digest's own output.
- Correct the 2026-10-31 report's four dependent statements to the supported unit, with a dated note.
- Control both behaviours with tests that fail on the pre-change behaviour.

**Non-Goals**

- Inlining the record bodies: the last eight-day window selects 60 files and 3,287 lines on top of a digest that already
  carries `AGENTS.md`'s gotchas and all of `docs/ideas.md`. Revisit only if a run shows targeted retrieval failing.
- A canonical-label validation gate (`docs/ideas.md:565–571`) — parked, and a separate mechanism.
- The causal-contract gap (`docs/ideas.md:386–407`) — its recorded reopening condition is not met.
- Trending the audit reports' verdicts (`docs/ideas.md:817–829`), recording the capture's rejected proposals
  (`:831–835`), a per-consultation record, or any effectiveness ledger.
- Changing the retrospective's on-demand trigger or its model pin.

## Decisions

### D1 — An index with a mandatory targeted read, not inlined bodies

The digest prints, per selected record file, its path and its `###` incident headings read at the indexed revision,
preceded by a count line (unique files, incident headings, the revision the index was derived at) and followed by the
files the `since` date's own commits touched, with a selected path absent at that revision marked rather than dropped
and an empty selection stated, mirroring the archive block's boundary resolution. The definition requires the body to be
read before a causal claim cites the record.

- **Alternative — inline the bodies.** Rejected on the measured volume (3,287 lines for one week) against a digest that
  is already large; the advisor's change condition keeps it available if a controlled run shows retrieval failing.
- **Alternative — index only, no read obligation.** Rejected: it reproduces today's failure, where the material is
  reachable but the contract does not require reaching it.

### D2 — One contribution per canonical class per round

`tally()` builds the round row's canonical class set first, then increments each class once. The classes line states
"one contribution per canonical class per round — not a count of findings".

- **Alternative — keep label occurrences.** Rejected: it double-counts two labels that normalize to one class on one row
  (`scripts/test-review-rounds.py:110–112` currently asserts `wording 2` for `wording,ambiguity`), so the figure
  measures the label vocabulary's redundancy as well as the class's incidence.
- **Alternative — count findings per class.** Rejected as not derivable: the ledger records classes per round, not a
  per-class finding tally, so any per-finding figure would be invented.

### D3 — A shell integration test over the real entry point

`scripts/test-experience-analysis.sh` creates a temporary git repository with records committed on the day before the
window, on the boundary day, and after it; runs the real `scripts/experience-analysis.sh` from that directory with a
stub `gh` that answers the two filter shapes the digest uses over an empty list (`length` yields `0`, `.[] | …` yields
nothing) and fails on any other shape; and asserts the emitted block.

- **Alternative — extract the selection into a Python helper for testability.** Rejected: it adds production surface to
  serve the test, and the repository already drives shell entry points from shell tests (`test-doctor.sh`).
- **Alternative — no test.** Rejected: the zero-selection, boundary, duplicate-touch, deleted-path, and non-ASCII cases
  are exactly what a hand-run on the real window cannot discriminate.

### D4 — Correct the report in this PR, with a dated note, without recalculating

The four occurrences (`docs/retrospectives/2026-10-31.md:14,78,98,130`) lose the percentage and state the round
occurrence; the originally reported totals stay as recorded, and a dated correction note names the unit defect and the
second reason a recomputation today differs (the ledger has grown since the report ran).

- **Alternative — a separate docs PR.** Rejected: the change causes the correction, so it rides this branch.
- **Alternative — replace the figures with a present-day recalculation.** Rejected: it would fold every later unit's
  rows into a historical window and call the result the same measurement.

### D5 — The spec delta owns the block and the unit

The `MODIFIED` block on "Experience analysis is available to agents" adds the index to the derived-facts enumeration and
states the classes unit, because that requirement already enumerates the block's contents and separates the classes from
the miss count.

- **Alternative — fix the digest's wording only.** Rejected: the block's contents are spec'd, so a wording-only fix
  leaves the requirement describing a handoff that no longer exists.

## Amended clauses

| Clause (current main spec)                                                                                                                                     | Replacement                                                                                                                                                                                                                                                                                                                                                                                                               | Why                                                                                                            |
| -------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------- |
| "…the window's review rounds from `docs/review-rounds.tsv` — its round count, the classes raised, and the compliance-miss count stated for the window proper…" | The same enumeration, followed by "…and the process-defect records the window's commits touched, as an index naming each record file with its incident headings and stating the unique-file count, the incident-heading count, the revision the index was derived at, the files the `since` date's own commits touched, a selected path absent at that revision rather than dropping it, and an empty selection as such…" | The analyzer is told to reuse those records; the handoff must supply them or the instruction is unenforceable. |
| "…its round count, the classes raised, and the compliance-miss count…"                                                                                         | "…its round count, the classes raised — each counted once per round that raised it, regardless of how many labels on that round normalized to it — and the compliance-miss count…"                                                                                                                                                                                                                                        | The two figures count different events; the report divided one by the other.                                   |
| "…drawing the evidence from the digest's review-round data rather than restating the symptom…"                                                                 | The same clause, followed by "When a causal claim rests on a process-defect record, it SHALL have been drawn from that record's body, not from the index's heading, and SHALL name the record it read."                                                                                                                                                                                                                   | An index makes records discoverable; it does not make a heading evidence for a cause.                          |

## Risks / Trade-offs

- **An analyzer cites an index heading without reading the record.** Mitigated by the definition's obligation, the
  report contract's requirement to name the record, and the review's existing `claim-unreproducible` class; the change's
  smoke-run verifies that retrieval happens, not that it improves conclusions.
- **The digest grows by the index.** Measured at one line per record plus its headings (60 files in the last window),
  against 3,287 lines had the bodies been inlined.
- **The `gh` stub answers shapes, not filters.** It returns what the real CLI would for an empty list on the two shapes
  the digest uses (`length` and `.[] | …`), and an unknown shape fails the stub; a change to a field list inside an
  `.[]` call is not detected, so the guarantee covers each call's shape, not its field selection.
- **A selected path deleted in the window cannot be read.** The block marks it `(absent at <revision>)`, so the analyzer
  states the gap instead of inventing the record's content.
- **The report correction reads as rewriting history.** Mitigated by the dated note, the retained original totals, and
  the unchanged window definition.

## Claims table

| Claim                                                                                           | Source / command                                                                                                                                  | Figure                                      |
| ----------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------- |
| The definition names the records while the digest and command do not                            | `git grep -c process-defects <base> -- .opencode/agent/experience-analyzer.md scripts/experience-analysis.sh .opencode/commands/retrospective.md` | definition 1; script 0 (no line); command 0 |
| One eight-day window selects 60 record files totalling 3,287 lines                              | `git log --since=2026-10-24T00:00:00 --name-only --format= -- docs/process-defects/ \| sort -u \| wc -l` then `wc -l` over that set               | 60 files; 3,287 lines                       |
| A row carrying two labels that normalize to one class currently contributes two                 | `git grep -c 'assertIn("wording 2"' <base> -- scripts/test-review-rounds.py`                                                                      | 1 assertion (fails after the change)        |
| The report states the share in four places                                                      | `git grep -c '20% of' <base> -- docs/retrospectives/2026-10-31.md`                                                                                | 4                                           |
| The same boundary reports 281 occurrences / 1,426 misses today against the report's 275 / 1,399 | `python3 scripts/review-rounds.py --since 2026-10-23`                                                                                             | 281 / 1,426 (window proper)                 |
| The repository records "No third mechanism" as its direction                                    | `git show <base>:docs/decisions/2026-10-31-mechanism-measurement.md`                                                                              | line 45 states it                           |

Base revision: `bff517ebae1189117b23b458583ffe688446515b`.

Re-derived after the edits: every row reproduces and no figure moved. The aggregation change leaves
`claim-unreproducible` at 281 in this ledger, because no window-proper row carries two labels that normalize to one
class; the collapse is exercised by `scripts/test-review-rounds.py`'s fixture instead.

## Dismissals (claim-scan candidates)

- **`path` and `cross-ref`** — each names a file, heading, or anchor the change's code reads or its prose cites: the
  digest test creates and reads every fixture path (a wrong one fails the test), the definition and command cite paths
  their own prose introduces, and the design's citations were read at the base revision. The executing check or that
  read is the evidence, so no row is owed.
- **`absolute`** — a modal word in a rule, a condition, or an emitted label ("never divide it", "only if a controlled
  run", "(none selected)"), not a claim quantified over a corpus.
- **`count`** — the design's 60 files / 3,287 lines / 281 / 1,426 figures are rows above, the test's are its own
  asserted fixture expectations, and "27 passed / 0 failed" is `openspec validate --all`'s recorded outcome. The rest
  are task numbers.

## Migration Plan

None. The next `/retrospective` run receives the block; no stored state, schema, or invocation changes. The corrected
retrospective keeps its window and its original totals.

## Open Questions

None.
