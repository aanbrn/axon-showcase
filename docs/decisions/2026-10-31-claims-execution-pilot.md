# Decision record — the claims-execution pilot

A dated record of the pilot and what it allows. The parked idea (`docs/ideas.md` 2026-10-27, "Give claims-table rows an
executable verification mode") asked for an opt-in `claim-scan.py --verify` and a bounded pilot before the mode lands.
The change `add-claims-execution-verify` carries the mode; this record carries the pilot's data and verdict.

## The question

Can a claims row's command be executed and compared, and is the machine-checkable subset large enough to be worth the
mode?

## The compatibility run

The four units the idea names: the three archived claims tables and `record-the-settle-step-defect`'s table from #711.
The mode's classification, run on 2026-10-31 at `11164568`:

| Table                                                    | Rows | Checked | Mismatches | Skipped |
| -------------------------------------------------------- | ---- | ------- | ---------- | ------- |
| `2026-10-27-extend-owner-reply-language-scope`           | 9    | 0       | 0          | 9       |
| `2026-10-27-hand-over-unfinished-work` (2-column)        | 11   | 0       | 0          | 11      |
| `2026-10-27-separate-lesson-capture-from-implementation` | 17   | 6       | 0          | 11      |
| #711                                                     | 7    | 0       | 0          | 7       |
| total                                                    | 44   | 6       | 0          | 38      |

The historical tables are prose evidence records: their Results describe an output (`the six sub-bullets`, `:465–466`)
rather than stating a literal, so they are advisory under the explicit-format contract. The six checked rows are the
rows whose Result is an explicit literal or literal list; all six verify. The earlier five mismatches were a comparison
bug — the token boundary rejected a bare `:NNN` locator that a `git grep -n` output carries as `path:NNN:` — which the
deep review found and the fix corrected. The 38 skipped rows are the 2-column table, the prose results, the cells whose
source is not one command (an `and`/`then` connector), the pipeline cells with an ambiguous `\|`, the working-tree
measurements, and the unsafe/external commands.

## The fixture run

The mode's tests exercise the contract end to end: a correct explicit literal verifies; a wrong count and a wrong
emitted line number mismatch; a zero literal verifies from stdout; a prose Result, a 2-column row, an ambiguous `\|`
cell, and a working-tree `git diff HEAD` are advisory; an unsafe form is refused; an external pipeline is reported
external; a read-only `>` pattern is not refused; a non-zero exit with the literal is a mismatch; a multi-literal Result
verifies only when every literal is present; a single-revision `git diff` and a hex pattern without a revision are
advisory; an escaped quote is not a redirection; a timeout and an execution failure are mismatches.
`python3 scripts/test-claim-scan.py` runs 55 tests, all passing.

## What the pilot establishes

- The historical format is not machine-checkable without heuristics the advisor rejected; the explicit literal-only
  format is.
- The mode's value is for new rows: a wrong literal fails, a correct one verifies, and the skipped coverage is reported.
- The comparison's false-fail surface is bounded to the explicit format; the report prints the output beside the
  literal, so a reader judges a mismatch.

## The decision

The verdict is **narrow**: the `--verify` mode lands with the explicit literal-only format; the historical tables stay
advisory compatibility data; the report states the checked and skipped counts; the six checked historical rows verify
after the deep review's comparison fix. The parked idea is removed as implemented.

## Reopening condition

Reopen this decision when any one of the following holds:

- **A deferred command family is needed** — a live table's rows require `awk`, `gh`, `opencode`, `openspec`, or `perl`,
  which D4 defers (internal trigger: a table the mode cannot check).
- **The skipped coverage is material** — a prose row that should be machine-checkable recurs, or a unit's table is
  mostly skipped (internal trigger: the mode's report on a live table).
- **The comparison's false-fail rate is high** — an explicit-format row mismatches for a reason other than its literal
  (internal trigger: the mode's report).
