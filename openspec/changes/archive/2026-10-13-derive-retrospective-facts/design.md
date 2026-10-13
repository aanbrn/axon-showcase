# Design

## Context

See `proposal.md` — Why. The evidence is the review of `docs/retrospectives/2026-10-13.md`: eleven rounds (the session's
own count; the rounds are recorded nowhere), almost every finding about a countable claim in prose. The fixes that
converged the later rounds were **replacing an enumeration with a derivation** ("derive the pin set by grepping the five
live docs"; "derive the lesson set by grepping `captured:` against the window's change names") — which is what this
change does once, in the tool, instead of once per review round.

The digest today (`scripts/experience-analysis.sh`, 35 lines) prints: the window line, the merged PRs
(`gh pr list --state merged --search "merged:>=<since>"`), the git log, the archive dir listing (un-windowed), the
`AGENTS.md` gotchas section, and `docs/ideas.md`. Every fact the retrospective needs is derivable from those sources;
none is computed.

Four facts about the window's shape drive the design:

- **The PR boundary and the archive boundary must share one window.** The 2026-10-13 retrospective counted 54 PRs
  (`#504–#558`) but 30 archived changes (dated 2026-10-06→13), while six more changes dated 2026-10-05 were archived by
  those same PRs — the two figures used different windows, which the review flagged (the session's account, as with the
  round count above).
- **The `merged:>=<since>` search is not the window.** It is inclusive of the `since` date, so it also returns that
  date's merges, and open-ended, so a later run includes later PRs. Verified: `merged:>=2026-10-05` returned 59 at the
  retrospective's authoring and returns 60 now, and 2026-10-05's merges are `#499–#513` — of which `#499–#503` belong to
  the previous retrospective's window, so the counted window opens at `#504`. The split is a per-window judgment:
  hard-coding `#499–#503` would encode one window's answer and be wrong the next, and the search never returns a
  non-merged PR, so an exclusion keyed on a PR's state has nothing to fire on. The block states the facts and the author
  resolves the split against the previous retrospective's file.
- **The archive set carries the same `since`-date overlap as the PR set.** `ls openspec/changes/archive/` is current
  state, so the windowed set is what the window's commits added — and a `--since`-scoped `git log` is inclusive of the
  `since` date too. Verified: the raw
  `git log --since=2026-10-05T00:00:00 --diff-filter=A --name-only -- openspec/changes/archive/` emits every added path,
  so filtered to the added `proposal.md` paths it yields 39 change dirs against the window's 36 — the three extra being
  the previous window's `since`-date archives, the same split as the PR side, so the block names the `since`-date dirs
  for the author to resolve.
- **The git commit-date window and the PR merge-date window differ.** A squash-merged commit's date can differ from its
  PR's merge date — verified: `#494` is committed 2026-10-05 but merged 2026-10-04 — so a `git log --since` baseline at
  the raw window's start (2672 lines, `#494`'s parent) is not the resolved window's start (2693, `#504`'s parent). The
  block therefore prints the `AGENTS.md` metrics just before each `since`-date PR, and the author reads the baseline off
  the first counted PR.

## Goals / Non-Goals

**Goals:**

- Print every countable claim a retrospective makes, already resolved, so the prose is written around the block.
- State the same `since` boundary for both counts, so the author resolves one split (the `since`-date items) rather than
  reconciling two differently-scoped figures; print the `AGENTS.md` metrics just before each `since`-date PR, so the
  window's baseline is read off the first counted PR.

**Non-Goals:**

- A CI check or lint that re-derives the retrospective's numbers: the record is a frozen point-in-time artifact, so a
  re-deriving check fights its purpose. Generation, not a gate.
- Deriving the **judgment** claims (theme grouping, a misattributed lesson, prose contradicting a number) — those stay
  with the author and the review.
- Changing the review contract: the class-exhaustion, per-round-record, and loop-bound changes are separate units. Only
  the loop bound depends on this one — its parked entry makes it safe "only once the digest derives the countable
  claims" — so the review trusts a block that exists before the loop is capped.
- Widening the two `README.md` `/retrospective` descriptions: they summarize the digest at capability level and already
  omit `docs/ideas.md`, so the block is an internal detail they need not carry; the omission is stated rather than left
  silent (the README's design intent keeps it a showcase, not a reference dump).

## Decisions

**Compute the facts in the digest rather than in a lint.** The digest is where the analyzer's inputs are gathered; a
generated block is _the_ input, so the author reads a number instead of asserting one. A lint would have to re-derive
counts from an artifact whose whole point is to freeze them.

**Scope the git side to the window with a time-explicit `since`.** The archive set is the archive dirs the window's
commits added (`git log --since="${SINCE}T00:00:00" --diff-filter=A --name-only -- openspec/changes/archive/`, filtered
to the added `proposal.md` paths), and the git-log section uses the same `"${SINCE}T00:00:00"` — the time-explicit
`since` matters: a date-only `--since=2026-10-05` includes only commits after the wall-clock time on that date, so the
count (and the log) would vary run to run.

**State the boundary facts in the block, not only the totals.** The search's count, each PR merged on the `since` date,
the previous retrospective's file, and the `AGENTS.md` metrics just before each `since`-date PR — so a reader can check
the resolution rather than trust a figure.

**Derive each added `captured:` marker's introducing commit with `git log -S`.** The lesson list's attributions are the
class the review caught twice; the block carries each added marker's introducing commit with its date and PR subject
(the pull request is in the subject), so the list is read from the block and filtered to the window's own PRs.

## Risks / Trade-offs

- `gh` and `git` are network- and history-dependent → the block states its sources and the boundary decisions, so a
  reader can re-run any line; a stale local clone shows up as a mismatch against the retrospective it is checked on.
- The clause count (lines → clauses) is a judgment the block approximates → the block prints the lines and their
  introducing commits, and the clause count is stated as derived from the lines, not asserted.
- The script grows from 35 to ~102 lines → it stays POSIX-friendly bash with the existing `set -euo pipefail`, and its
  failure mode is printing fewer facts, never wrong ones.

## Migration Plan

None: the block is additive to the digest; the retrospective template is not changed (the author reads the block).
