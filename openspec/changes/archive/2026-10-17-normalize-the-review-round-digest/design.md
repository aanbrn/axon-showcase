# Design

## Context

See `proposal.md` — Why. The digest's review-round block is a single inline `awk` filter in
`scripts/experience-analysis.sh` (`$1 >= s`), and no other tool aggregates the record into window figures
(`commit-hygiene.py` verifies its rows' uniqueness instead). Six constraints shape the approach:

- The record's rows are measured data. Rows recorded before the reviewer's canonical vocabulary landed (with #577,
  merged 2026-10-15) keep the labels they were recorded with, so they are normalized at read time, never rewritten.
- The ledger grows, so a figure quoted from a past run is historical. The filter prints one inclusive line (130/203 when
  the 2026-10-15 retrospective measured it), and that retrospective derived 87/145 for the window proper by subtracting
  the `since`-date rows; the same commands give 335/494 and 292/436 today.
- A count over the live ledger also moves when this unit appends its own rounds, so the commands deriving it exclude
  them (`$2 != "normalize-the-review-round-digest"`) — the retrospective's baseline applies the same exclusion to its
  own rows — which is why the aggregation's verification is fixture-led rather than a re-run of those figures.
- The reviewer's class list is extensible in the change that raises an unclassifiable finding, so a label the record
  carries may be one the tool has never seen.
- The digest is agent input, not a gate: it prints figures, has no verdict, and `/retrospective` reads its output.
- The record's figures are quoted in durable artifacts (a retrospective records the baseline), so a mis-normalized label
  is a wrong number in a committed document, not a cosmetic defect.

## Goals / Non-Goals

**Goals:**

- State the window's review-round figures for the window proper beside the `since`-inclusive ones.
- Present the classes raised under one vocabulary, so a window's classes compare with the previous baseline's.
- Make the aggregation testable, so its outputs are verified by a committed test rather than by reading the digest.

**Non-Goals:**

- Changing the record's historical rows or the reviewer's contract: the record keeps its labels, and the vocabulary
  stays owned by `.opencode/agent/review-quick.md`.
- Recomputing the reviewer's `repeated` marking or the repeat share the retrospective quotes. The record's `repeated`
  column is the reviewer's per-round marking, and the retrospective derives the share it quotes from the rows; a second
  computed copy would be a second model of one fact.
- Giving the digest a verdict. It reports figures; a label it cannot normalize is reported, not failed on.
- Making the mapping a live vocabulary source (the parked companion idea covers a check that every recorded label is in
  the reviewer's list).

## Claims table

| Fact                                                                                       | Source (re-run)                                                                                                                                                                                                                                                                              |
| ------------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| the digest aggregates the record inline, inclusive of the `since` date                     | `git show HEAD:scripts/experience-analysis.sh \| sed -n '87,93p'` → the `$1 >= s` filter, printing each row's classes as recorded and the `window:` line                                                                                                                                     |
| the 2026-10-15 retrospective wrote 130/203 for the filter and 87/145 for its baseline      | `git show HEAD:docs/retrospectives/2026-10-15.md \| sed -n '78,88p'` → the 87/145 baseline with its own records excluded (`$2 != "retrospective-2026-10-15"`), and the 130/203 `window:` line with the one-day subtraction it names                                                          |
| those figures are historical: the same commands give 335/494 today for `since=2026-10-13`  | `awk -F'\t' '$1 !~ /^#/ && $1 >= "2026-10-13" && $2 != "normalize-the-review-round-digest" {r++; m+=$6} END{print r+0, m+0}' docs/review-rounds.tsv` → `335 494`; the `>` variant → `292 436`                                                                                                |
| the repeat share moves 70% → 74% under the recorded mapping                                | `grep -n "70% → 74%" docs/retrospectives/2026-10-15.md` → line 93                                                                                                                                                                                                                            |
| the canonical vocabulary landed with #577, merged 2026-10-15                               | `git log -1 --format='%h %ad %s' --date=short -- openspec/changes/archive/2026-10-15-give-the-review-a-canonical-class-list` → `c5c461d2 2026-10-15 … (#577)`                                                                                                                                |
| the reviewer's contract extends the class list in the change that needs a new class        | `sed -n '45p' .opencode/agent/review-quick.md` → "A finding that fits none of these: the calling agent adds a class here, in the same change"                                                                                                                                                |
| the record line's columns are the reviewer's own marking, appended verbatim                | `sed -n '74,78p' .opencode/agent/review-quick.md` → the six tab-separated fields and "The calling agent appends it to `docs/review-rounds.tsv` verbatim"                                                                                                                                     |
| the record's rows dated on or before 2026-10-15 carry 57 distinct labels                   | `awk -F'\t' '$1 !~ /^#/ && $1 <= "2026-10-15" {print $4}' docs/review-rounds.tsv \| tr ',' '\n' \| tr -d ' ' \| grep -v '^-$' \| grep -v '^$' \| sort -u \| wc -l` → `57`                                                                                                                    |
| #577's mapping table holds 54 pairs over 18 classes                                        | `grep -cE '^\| \`[a-z-]+\`[[:space:]]*\|' openspec/changes/archive/2026-10-15-give-the-review-a-canonical-class-list/design.md`→`18`; the same rows' second column, split on commas and summed → `54`                                                                                        |
| the pre-boundary labels the mapping omits are two canonical classes plus `compliance-miss` | the mapping's 54 labels against the 57 pre-boundary labels → `claim-missing` and `claim-unreproducible` (already canonical, so identity rows) and `compliance-miss`, which #577's design states is the record's flag column and not a class                                                  |
| `compliance-miss` appears once as a class label                                            | `grep -n compliance-miss docs/review-rounds.tsv` → one row (`bound-the-review-loop`, dated 2026-10-13)                                                                                                                                                                                       |
| every label in rows dated 2026-10-16 or later is in the reviewer's canonical list          | the 19 distinct labels in those rows against `sed -n '/canonical list/,/A finding that fits none/p' .opencode/agent/review-quick.md \| grep -oE '`[a-z-]+`' \| sort -u` → the same 19                                                                                                        |
| the reviewer's canonical list holds 19 classes                                             | the command in the row above → `19`                                                                                                                                                                                                                                                          |
| `structured-writing` is a post-boundary class in no pre-canonical mapping row              | `grep -n structured-writing docs/review-rounds.tsv` → rows dated 2026-10-16 and later; absent from #577's mapping table                                                                                                                                                                      |
| the ledger's header names `scripts/experience-analysis.sh` as its aggregator               | `git show HEAD:docs/review-rounds.tsv \| sed -n '1,8p'` → line 5                                                                                                                                                                                                                             |
| no other tool aggregates the record into window figures                                    | `grep -rln review-rounds scripts/ .opencode/ build.gradle.kts` → `commit-hygiene.py` (row uniqueness), `experience-analysis.sh` (the aggregation), the reviewer's and analyzer's definitions, and the build task declarations                                                                |
| a tool may declare its own table and have its test read it                                 | `grep -n probe_table scripts/doctor.sh scripts/test-doctor.sh` → declared in `doctor.sh`, extracted by `test-doctor.sh`                                                                                                                                                                      |
| the claim scan emits exactly four candidate kinds                                          | `sed -n '5,13p' scripts/claim-scan.py` → `count`, `absolute`, `cross-ref`, `path`                                                                                                                                                                                                            |
| the doctor's spec requires it to run with nothing but a POSIX shell                        | `sed -n '15p' openspec/specs/showcase/quality/toolchain-check/spec.md` → "SHALL run with nothing but a POSIX shell"                                                                                                                                                                          |
| the commit-hygiene guard's modes are the build's `verify*` checks                          | `python3 scripts/commit-hygiene.py --help` → its eight mode flags (`--markers`, `--tracked-ignored`, `--conflict-markers`, `--executable-bits`, `--unique-crons`, `--unique-rounds`, `--large-files`), which are the corresponding `check` members, and `--staged`, which runs the formatter |
| the repository requires a gate's control to be a committed test                            | `grep -n "A check's control is a committed test" openspec/specs/showcase/quality/code-quality/spec.md` → line 459                                                                                                                                                                            |
| a test task's registration is `testClaimScan`'s shape                                      | `git show HEAD:build.gradle.kts \| sed -n '491,508p'` → `group`, `description`, `inputs.file`, `outputs.upToDateWhen { false }`, `commandLine`, with `testDoctorCharter` beside it declaring its `README.md` input the same way                                                              |
| the `check`-member enumeration naming the commit-hygiene tasks is AGENTS.md lines 759–760  | `git show HEAD:AGENTS.md \| sed -n '759,760p'`                                                                                                                                                                                                                                               |
| the README's Python prerequisite row names the same reasons AGENTS.md's list does          | `git show HEAD:README.md \| sed -n '545p'` → `scripts/setup-idea.sh`, the commit-hygiene guard/check, and `claim-scan.py`                                                                                                                                                                    |
| the requirement the delta modifies is `agent-skills`'s experience-analysis block           | `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md \| sed -n '59,101p'` → the requirement and its four scenarios the delta copies                                                                                                                                           |
| the removed idea is the 2026-10-17 S1 entry at `docs/ideas.md` line 18                     | `git show HEAD:docs/ideas.md \| sed -n '18,27p'`                                                                                                                                                                                                                                             |

Scan candidates (`python3 scripts/claim-scan.py --base origin/main`) are resolved by the kind the scan emits — its four
shapes `count`, `absolute`, `cross-ref`, `path` — with one reason per kind, so a candidate is disposed of by its kind
rather than by its instance:

- `path` and `cross-ref`: a name, and a name is not a fact the table's rows state, so a candidate of either kind is
  disposed of by that reason whatever it names.
- `count`: a numeral, and a numeral is not a fact the table's rows state; where a row states the figure, that row
  accounts for it.
- `absolute`: a quantifier in prose, and the schema's `None` answer, neither of which is a checkable quantity.

## Decisions

**D1 — A new tool owns the aggregation; the digest delegates to it.**

- Alternative: keep the `awk` block inline and test it through a function extracted by the test, the way
  `test-doctor.sh` extracts `validate_probe_table` — rejected because the block grows from a two-variable sum into a
  window split, a 56-entry mapping and a tally, which `awk` expresses only opaquely, while `doctor.sh` is bash-only
  because its spec requires it to run with nothing but a POSIX shell.
- Alternative: extend `scripts/commit-hygiene.py` — rejected because its modes _are_ the build's `verify*` checks; this
  reads the ledger for a report.
- The digest keeps the block's heading and calls the tool, so the digest's other sections and its usage are unchanged.

**D2 — The tool declares the mapping as its own table.**

- Alternative: read #577's mapping table out of its archived `design.md` at run time — rejected because the digest would
  then depend on a historical artifact's markdown layout, and a silent parse failure would under-match, the failure mode
  this change exists to remove.
- Alternative: a separate data file beside the tool — rejected as a third surface (the tool, the data file, and the
  archive record) for one consumer, when the tool's declaration and its test are the pattern the doctor already uses.
- The 54 recorded pairs are transcribed from #577's table plus two identity rows for the canonical labels it omits
  (`claim-missing`, `claim-unreproducible`); the tool's test re-derives the 54 from the archived table and asserts them
  equal, so the transcription is verified rather than recalled.

**D3 — A date boundary, not a label set, decides which rows are normalized.**

- The boundary is 2026-10-15 (rows dated on or before it are normalized); it is the day the canonical list landed, and
  the record's post-boundary rows carry only canonical labels.
- Alternative: normalize whenever a label is in the mapping and report anything else as unmapped — rejected because the
  reviewer's list is extensible, so the next class added to the contract would be reported as unmappable in every row
  that uses it (`structured-writing`, added after the boundary, is exactly that case today).
- Alternative: declare the reviewer's canonical list in the tool — rejected as a second copy of a list the contract owns
  and the parked companion idea exists to check.

**D4 — The window-proper figures are printed beside the inclusive ones, never instead of them.**

- The retrospective's baseline states its own boundary as the inclusive digest line minus the `since`-date rows, so a
  reader comparing against it needs both figures; replacing the line would break the recorded comparison's arithmetic.

**D5 — A pre-canonical label the mapping cannot normalize is reported under its own heading.**

- It is listed as unmapped and kept as recorded, so a reader can tell a canonical class from a historical label the tool
  could not classify, rather than reading both out of one tally.
- The requirement scopes the report to the labels the digest attempts to normalize, since a post-boundary label is taken
  as recorded (D3): reporting every unmapped label would flag the next class the contract gains.

**D6 — A committed test, wired into `check` as `testReviewRounds`, covers the aggregation.**

- Alternative: assert the aggregation in a task's checklist — rejected by the repository's rule that a gate's control is
  a committed test.
- The test's fixture ledgers carry the known-good and known-bad cases: the inclusive/window-proper split, a
  normalization, an unmapped label, and a post-boundary label taken as recorded.
- The real ledger is read for the mapping's completeness against its pre-boundary labels, and the archived table for the
  declared pairs' equality with what #577 recorded — each derived from the test that reads it. The split and the
  normalization are not re-asserted over the real ledger: the retrospective's figures are unreproducible from the grown
  ledger (Context), and the relative runs over it are the tasks' verification, not the test's.
- The task declares `docs/review-rounds.tsv` and the archived table's `design.md` as inputs (both are read) in addition
  to the tool and its test, so a ledger or archive change re-runs it.

## Risks / Trade-offs

- A label recorded after the boundary that is not canonical would pass through unnormalized → the spec scopes the
  unmapped report to the pre-canonical labels the tool normalizes, the ledger's post-boundary rows are canonical today
  (claims table), and the parked companion idea's check is the mechanism that keeps them so.
- A mistyped pair among the 54 would silently mis-normalize a quoted figure → the test derives the pairs from #577's
  archived table and asserts equality, so a transcription error fails the build.
- `compliance-miss` in a pre-boundary row's classes column reads as a class → the tool reports it under "unmapped",
  naming that it is kept as recorded, and the record's header already states that the column is a flag.
- The digest now needs `python3` on `PATH` → it is already a documented prerequisite and the tool is stdlib-only.
- A new `check` member widens every live enumeration of the members → `AGENTS.md`'s enumeration is swept in this change.

## Migration Plan

Additive and build-only: the tool, its test and the `testReviewRounds` task land together, and the digest's block
changes to call the tool in the same commit, so no window is aggregated by both. The record's historical rows are not
edited. Rollback is reverting the change; the digest's output then returns to the inclusive-only line, which no artifact
depends on being absent.
