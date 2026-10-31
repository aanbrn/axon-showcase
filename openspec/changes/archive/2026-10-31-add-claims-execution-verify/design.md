# Design — add-claims-execution-verify

## Context

`proposal.md` states the problem: a claims table's rows are prose, and nothing executes them. The parked idea
(`docs/ideas.md` 2026-10-27) defines the shape — an opt-in `claim-scan.py --verify`, advisory default preserved, no
execution in `check` — and requires a bounded pilot before the mode lands.

The pilot's first phase ran a scratch classifier/executor over the four units the idea names: the three archived claims
tables and `record-the-settle-step-defect`'s table from #711. A scratch run (not committed; superseded by the advisor's
review) split the 44 rows as 11 literal-containment successes, 10 mismatches, 21 advisory (the 2-column hand-over table
and the prose results), 1 unsafe, and 1 external. The advisor's review of that data found the 11/10 split does not
establish ten row defects: several mismatches are comparison false fails (a locator beside printed prose, an abbreviated
literal, a derived range, a parser-changed `\|`), and the mode must not decide eligibility from whether the actual
output happens to contain the literal. The pilot's verdict is **narrow**: the mode lands for an explicit, literal-only
Result format; the historical prose rows are advisory compatibility data.

## Goals / Non-Goals

**Goals**

- An opt-in `--verify <table>` mode for a supported row: a three-column table, one command, and an explicit literal-only
  Result.
- Every explicit literal is compared before the row is read as verified; a missing literal is a mismatch, never
  reclassified.
- The report states checked versus skipped coverage; zero mismatches is not whole-table verification.
- The contracts recorded where the next author reads them.

**Non-Goals**

- Semantic comparison of prose results; recovering a historical prose row heuristically.
- A command family outside the local, revision-anchored forms D4 admits.
- Rewriting the historical tables or the claims-table authoring rules.
- A `check` gate; the mode stays opt-in.
- Executing a command the predicate does not admit.

## Decisions

### D1 — the mode is opt-in, and the default scan is unchanged

`claim-scan.py --verify <table>` runs only when invoked. The default scan's behavior is unchanged; the test suite gains
the mode's cases and stays in `check`. Alternatives: a `check` gate (rejected — the rows include unsafe commands, live
reads, and transient sources); a separate script (rejected — the mode extends the seeder that already knows the shapes).

### D2 — a supported row is a three-column table with one command; raw cells are preserved

The parser recognizes a three-column table (Claim | Source or command | Result). The Source cell must be one command: a
single backticked span, or two spans joined only by `piped through` (→ `|`). A cell containing `\|` is **advisory** —
the mode does not guess whether it is the table's pipe escape or grep's BRE alternation — and so is a loop or a
non-connector prose step; the parser preserves the raw cell. Alternatives: the first span only (rejected — it executes
half a command); a blanket unescape (rejected — it changed a row's regex in the pilot).

### D3 — eligibility is decided before execution, from the Result's explicit format

A supported Result is an explicit literal-only assertion: one or more backticked literals separated only by whitespace
and commas, with no prose. A prose Result, a locator/range beside printed text, or a mixed explanation is **advisory**.
For a supported row, every explicit literal is compared as a token in the command's stdout; a missing literal is a
**mismatch** and is never reclassified as advisory. The false-negative surface — a prose or locator expectation is
unchecked — is stated in the report. Alternatives: eligibility from the actual output (rejected — a wrong expected count
would become advisory exactly when it should fail); the first literal only (rejected — it verifies one of four stated
line numbers).

### D4 — the predicate admits the local, revision-anchored forms

The command's first word and form must be one of the read-only `git` subcommands (`show`, `grep`, `diff`, `log`,
`rev-parse`, `ls-files`, `merge-base`, `cat-file`); a `grep`/`sed`/`wc` first word is **advisory** (a working-tree
measurement, D5). The `git` write subcommands (`push`, `commit`, `reset`, `clean`, `checkout`, `tag`, `config`, `-c`,
`--output`) and `sed`'s write/execute forms (`-i`, `w`, `W`, `e`, `s///w`, `s///e`) are refused. `awk`, `gh`,
`opencode`, `openspec`, `perl`, and `python3` are deferred: a row using them is **unsafe** or **external**, not
executed. A `>`/`>>` redirection or a loop is **unsafe** or **advisory** as D2 says. The predicate is a conservative
denylist of the forms this pilot needs, not a security boundary: a command it does not name may still write, execute a
loop, or reach the network; the mode is opt-in and the author is trusted. Alternatives: the broad per-tool table
(rejected — the pilot does not need it and every added tool widens the predicate); a sandbox (rejected —
disproportionate for a read-only check).

### D5 — the command's evidence context decides, not the table's location

An immutable-revision command (`git show <sha>:<path>`, and a `git grep` with its pattern quoted and a tree-ish named)
is executable. A `git diff` in any form, a `git grep` without a quoted pattern and a tree-ish, and a bare
`grep`/`sed`/`wc` are **advisory**; the mode does not check the run's working state. A PR body's location is not the
predicate, so #711's `git diff HEAD` row is advisory. The mode never substitutes the base for the working tree or the
reverse. Alternatives: "run at the PR's head" (rejected — a working-tree diff is not reconstructed from a clean
checkout).

### D6 — a per-row timeout is a mismatch

Each command runs with a 10-second timeout; a timeout or a non-zero exit is a **mismatch** naming the row, because a row
that never completes — or one whose command fails after printing part of its output — is not verified. Alternatives: no
timeout (rejected — a hung command blocks the run); a timeout as advisory (rejected — it would pass a row that cannot be
checked).

### D7 — the statuses, the coverage, and the exit code

A row is `verified`, `mismatch`, `advisory`, `unsafe`, or `external`. The mode exits non-zero when any row is a
`mismatch`; the other statuses are reported and do not change the exit code. The report states the checked and skipped
counts; zero mismatches is not whole-table verification; an execution failure is a mismatch (D6). Alternatives: fail on
any non-verified row (rejected — it would fail a table for its prose rows); report only mismatches (rejected — the
skipped coverage would be invisible).

### D8 — the pilot's scope and the historical tables

The pilot's compatibility data is the four units' tables: the three archived tables and #711's body, saved to a scratch
file. The historical rows are advisory where their Result is prose, a locator, or a mixed explanation; the mode does not
recover them heuristically. The mode's effectiveness evidence is a bounded fixture run through the actual CLI: a correct
assertion succeeds, and a wrong count or emitted line number fails. Alternatives: the whole ledger (rejected —
unbounded); the three archived tables only (rejected — it drops one of the idea's four units).

### D9 — the verdict is consequential

The pilot's verdict is **narrow**: the mode lands with the explicit literal-only format; the historical rows are
advisory, and the decision record states the subset, the compatibility data, and the fixture results. Alternatives:
proceed with the historical format (rejected — the advisor found the mismatches were mostly false fails); stop (rejected
— the fixture evidence shows the explicit format is checkable).

### D10 — `skip_specs`

No capability spec mentions `claim-scan.py` (`git grep -n 'claim-scan' 11164568 -- openspec/specs/` returns nothing);
the `agent-skills` claims-table requirement states the outcome, which the mode verifies without changing it. The change
therefore records `skip_specs: true` and owes no delta.

## Risks / Trade-offs

- **The supported subset is small** — the historical tables' prose rows are advisory; the mode's value is for rows
  written in the explicit format. The report states the skipped coverage, and the fixture run is the effectiveness
  evidence.
- **The predicate may refuse a legitimate command** — a form outside D4's list is unsafe/external; the pilot measures
  how many.
- **The comparison may false-fail** — a literal with a separator, a moved line. The report prints the output beside the
  literal, and the author judges.
- **The sample is four tables** — the compatibility verdict is bounded to it; a wider run is a later unit.

## Claims table

Base: `111645680f81376740511bb20e3bcdf508d57db5` (`git merge-base HEAD origin/main`).

| Claim                                                                 | Source                                                                                                                                                                                                                                                                        | Result                                                                                        |
| --------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------- |
| The parked idea names four units and 17 `claim-unreproducible` rounds | `git show 11164568:docs/ideas.md \| grep -n 'claim-unreproducible'`                                                                                                                                                                                                           | line 149                                                                                      |
| The three archived tables carry 9, 11, and 17 data rows               | `for d in 2026-10-27-extend-owner-reply-language-scope 2026-10-27-hand-over-unfinished-work 2026-10-27-separate-lesson-capture-from-implementation; do git show 11164568:openspec/changes/archive/$d/design.md \| sed -n '/## Claims table/,/^## /p' \| grep -c '^\| '; done` | 11, 13, and 19 pipe rows (9, 11, and 17 data rows)                                            |
| #711's body carries 7 data rows                                       | `gh pr view 711 --json body \| grep -c '^\| '`                                                                                                                                                                                                                                | 9 pipe rows (7 data rows)                                                                     |
| The four units' tables carry 44 data rows                             | the three counts above and #711's                                                                                                                                                                                                                                             | 9 + 11 + 17 + 7 = 44                                                                          |
| No capability spec mentions `claim-scan`                              | `git grep -n 'claim-scan' 11164568 -- openspec/specs/`                                                                                                                                                                                                                        | no output                                                                                     |
| `check` depends on `testClaimScan`; no task invokes the scanner       | `git show 11164568:build.gradle.kts \| grep -n 'dependsOn("testClaimScan")\|claim-scan.py'`                                                                                                                                                                                   | 767; 533–534 are the test's inputs, 536 the test suite's command; no task invokes the scanner |
| The default scan is advisory                                          | `git show 11164568:scripts/claim-scan.py \| grep -n 'never fails on a candidate'`                                                                                                                                                                                             | 16 — "The tool never fails on a candidate -- it is an authoring aid, not a gate"              |
| The scan's default modes                                              | `python3 scripts/claim-scan.py --help`                                                                                                                                                                                                                                        | `--base`, `--repo`, `--include`, `--diff-file`                                                |

## Dismissals

- The four row classes are a taxonomy for the pilot, not a claim about a count.
- The 10-second timeout is D6's decision, not a claim — it carries no source.
- The two recent bodies' exclusion is D8's scope decision, not a claim about their rows.
- The `gh api` read-only form is D4's predicate, not a claim.
- The scratch probe's 11/10/21/1/1 split is not committed; the decision record's 6/5/38 is the mode's run, and the
  scratch figures are the session's superseded data.

## What the pilot must establish

- The compatibility subset of the 44 rows, by D2–D5 class (the decision record carries the counts).
- The fixture run: a correct explicit assertion succeeds; a wrong count and a wrong emitted line number fail.
- The verdict (D9) and its consequence for the change.
