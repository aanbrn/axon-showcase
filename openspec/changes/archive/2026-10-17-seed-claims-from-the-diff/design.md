# Design

## Context

See `proposal.md` — Why. The pre-write pass and its claims table are specified in `showcase/quality/agent-skills`'s
"Quick review runs after proposal and implementation" scenario (the claims-table `AND` clause) and recorded as a task by
the "A skipped pre-write pass is a missing record" scenario; the actionable form is `AGENTS.md`'s pre-write bullet. The
pass's rows are author-seeded, and the reviewer checks the table for a stated fact it omits. The parked idea prescribes
the shape: "a `scripts/` tool plus an `agent-skills` delta". The repository's model for a tool with tests is
`scripts/commit-hygiene.py` plus `scripts/test-commit-hygiene.py`, wired into `check` by `testCommitHygiene`; a tool an
agent drives gets a `.opencode/commands/` trigger. The authoring workflow keeps the work uncommitted (the change dir is
untracked until the first commit), which is when the pass runs.

## Goals / Non-Goals

**Goals:**

- Turn the pass's weakest step into a search: enumerate the claim shapes in the author's own diff, so a claim the author
  did not notice stating is still listed for a row.
- Keep the scan advisory. Its hits are candidates, never findings: resolving one needs judgment, so a hit cannot fail a
  build without either false-blocking or being routinely ignored.

**Non-Goals:**

- **Gating `check` on the scan.** Only the tool's tests join `check` (as `testCommitHygiene`'s do). A gate over shapes
  would block on prose style, not correctness.
- **Scanning every backticked symbol.** Code identifiers are verified by the compiler and the gates; of the four kinds,
  `cross-ref` reaches a repo path, a spec capability id, or an `owner/repo#NNN`, and `path` a backticked path, so a bare
  class or test name a doc cites — the citation the rename gotcha records as resolved by nothing — stays the author's
  derivation. Flagging every backticked token would bury the enumerable shapes — counts, scopes, and cross-artifact
  references — in noise, and the record's two largest single classes (`contradiction`, `wording`) are semantic and not
  shape-detectable either.
- **The idea's second mode** (a claim restating a fact the diff corrected). Separating a corrected fact's identity from
  a coincidental numeral change is not a shape match; it needs judgment the scan does not have.

## Decisions

**Scan the authored set, not the tree.** The claims at issue are what this change states, so the tool reads the added
lines of `git diff <base>` (default `origin/main`) plus untracked, non-ignored files — the change dir is untracked while
the pass runs, and it is the pass's primary input. Alternative rejected: scanning the whole tree, which re-reports every
pre-existing claim on every unit.

**Four kinds, each a pinned rule with fixtures.** `count` (a numeral with a referent), `absolute` (`only`, `every`,
`all`, `never`, `always`, `none`), `cross-ref` (a repo path, a spec capability id, or an `owner/repo#NNN`), and `path`
(a backticked path). This is the idea's own enumeration minus the bare symbol, for the noise reason in Non-Goals. Each
kind is a function over a unified diff, tested against a fixture, so a pattern change is visible.

**A parser over diff text, and a thin CLI.** `find_claims(diff_text)` is the testable core; the CLI resolves the base,
runs git, and prints `file:line  [kind]  text` per candidate. Alternative rejected: a regex over files, which cannot
tell an added line from pre-existing text and gives no new-side line number.

**The dismissal is recorded beside the table.** The idea requires each candidate to be resolved "into a row or
explicitly dismissed", and a judgment-only dismissal leaves the scan's output invisible to the reviewer's control. So
the pass records the dismissed candidates beside the claims table, grouped by kind where the dismissal is uniform, with
the reason — a short list, not a second table. Alternative rejected: dismissal in judgment only, which cannot be
distinguished from a candidate the author never saw.

**One command trigger, `/seed-claims`.** The tooling-placement rule requires it for a `scripts/` tool an agent drives;
the command runs the scan and hands the author the candidates.

## Risks / Trade-offs

- **The scan over-produces.** Tuning is part of the implementation: run it over a few recent merged changes' diffs and
  narrow any kind whose output buries the others, recording what was tuned. The advisory status bounds the cost of a
  noisy pattern to the author's dismissals.
- **A clean scan reads as a complete table.** The scan enumerates shapes, not facts; the spec clause states that it
  seeds the table and never replaces the derivation, so the reviewer still checks the table against the artifacts.
- **A pattern drifts as prose changes.** Each kind's fixture is the control; a pattern that stops matching fails its
  test rather than going quiet.

## Claims table

| Claim                                                                       | Source                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| --------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The pre-write pass's output is a claims table, reviewer-checked             | `showcase/quality/agent-skills`'s "Quick review runs after proposal and implementation" (the claims-table `AND` clause)                                                                                                                                                                                                                                                                                                                           |
| Claim-shaped findings are the largest grouped cluster in the record         | `git show origin/main:docs/review-rounds.tsv \| grep -v '^#' \| cut -f4 \| tr ',' '\n' \| grep -vx -- '-' \| sort \| uniq -c`: 542 class occurrences; the ten claim classes (claim-unreproducible, over-claim, documented-numbers, claim-missing, claims-table, -source, -omission, -anchor, imprecise-claim, unverifiable-claim) sum to 175 (32%); the claims-table family is 47; the largest single classes are contradiction 54 and wording 49 |
| The scanned shapes are the idea's own list minus the bare symbol            | `docs/ideas.md` at the change's base (`git show origin/main:docs/ideas.md`): "a number with a referent, `only`/`every`/`all`/`never`, a backticked path or symbol, a cross-artifact reference"                                                                                                                                                                                                                                                    |
| The `agent-skills` Purpose describes the capability's scope, not this pass  | `openspec/specs/showcase/quality/agent-skills/spec.md`'s `## Purpose`, which names the skills and subagents and no loop mechanism                                                                                                                                                                                                                                                                                                                 |
| The parked idea prescribes a `scripts/` tool plus an `agent-skills` delta   | `docs/ideas.md` at the change's base (`git show origin/main:docs/ideas.md`), the "Mechanically enumerate … claim shapes" entry, removed here                                                                                                                                                                                                                                                                                                      |
| A `scripts/` tool an agent drives gets a command trigger                    | `AGENTS.md`'s "Tooling lives in a stated place" bullet (`doctor.sh` ↔ `/check-tooling`)                                                                                                                                                                                                                                                                                                                                                           |
| `testCommitHygiene` is the model for a script's tests in `check`            | `build.gradle.kts`'s `testCommitHygiene` registration and its `check` dependency                                                                                                                                                                                                                                                                                                                                                                  |
| The pass runs over uncommitted work, the change dir included                | `AGENTS.md`'s workflow agreement: the change dir is untracked until a commit is forced; its `pre-write` bullet names the artifacts the pass reads                                                                                                                                                                                                                                                                                                 |
| A definition edit is implementation, not planning                           | `AGENTS.md`: "A definition edit that changes spec'd behavior is a change, not a docs edit"; the planning stage is read-only                                                                                                                                                                                                                                                                                                                       |
| A changed definition body is served from the pre-change copy until a reload | `AGENTS.md`'s reload gotcha: "a changed definition body, like a changed `model` pin, is served from the pre-change copy until then"                                                                                                                                                                                                                                                                                                               |

### Dismissed scan candidates

`scripts/claim-scan.py --base origin/main` over this change's own diff lists candidates for all four kinds. Each
candidate not given a row above is dismissed as part of its kind, listed here with the reason. The run is deliberately
not quoted as a count: the diff grows while the change is in flight, so a frozen figure would be falsified by the next
edit.

- **`absolute`** — the quantifiers sit in the change's own artifact prose: the delta's clauses, the design's decisions,
  and the round narratives. The loop's over-claim class reviews them, and corrected three instances across rounds 1-3.
- **`count`** — the numerals and numerals-with-a-word are the record's round counts and class labels, task ids, the
  tool's docstring examples (`12 files`), and the tuning measurements; the figure that carries a claim is row 2's.
- **`path`** — the backticked paths are the capability id this change edits (`showcase/quality/agent-skills`), git refs
  and commands inside the artifacts (`origin/main`, `git show …`, `./gradlew …`), interpreter and script paths in the
  command text (`/usr/bin/python3`, `setup-idea.sh`), and the reference shape `owner/repo#NNN`; a path naming a file the
  authored set writes is already dropped by the scanner's narrowing.
- **`cross-ref`** — the references are the round ledger's `#606`, the change's own new files (`scripts/claim-scan.py`,
  `scripts/test-claim-scan.py`), the conventions it cites (`AGENTS.md`), URLs inside command text (`opencode.ai/v2`),
  and the regexes' own prose false positives (`Debian/Ubuntu`, `usr/bin/env`, `guard/check`, `w./-`).

None of these groups is a claim about prior evidence, so no row is owed for them.
