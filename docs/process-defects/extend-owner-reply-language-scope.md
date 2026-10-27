# Process defects — extend-owner-reply-language-scope

### extend-owner-reply-language-scope#1 — A claims-table row's command did not reproduce its stated result

- **Recorded** — 2026-10-27, main agent session, during implementation.
- **Stage** — proposal (the claims table in `design.md`).
- **Expected** — every claims-table row's command reproduces its stated result at the recorded base, so a reader can
  re-derive the claim; the unit's own task 1.1 states this.
- **Observed** — two rows named line-range reads of a working-tree file whose ranges came from an earlier ad-hoc read
  rather than from the artifact. The scenario-count row (`sed -n '2478,2516p' … | grep -c '^#### Scenario'`) returned 7,
  not the 8 the row claimed; the body-pointer row's range (`2474,2477`) returned the first scenario instead of the
  requirement body. After `AGENTS.md` was edited, the first row's working-tree read also stopped showing the pre-change
  bullets, since the claim is about the state before the change.
- **Evidence** — `sed -n '2478,2516p' openspec/specs/showcase/quality/agent-skills/spec.md | grep -c '^#### Scenario'`
  prints `7`; `sed -n '2474,2477p' …` prints `#### Scenario: An owner-facing reply uses the owner's language`; an `awk`
  block scoped from `/^### Requirement: Agent output language/` through the next requirement's header, piped to
  `grep -c '^#### Scenario'`, prints `8`.
- **Consequence** — caught while re-deriving the table before the report and fixed before the manual pass: all three
  pre-change rows now read the base revision (`git show 50799b60:…`), and each command was re-run and reproduced its
  stated result. No artifact shipped a claim its command does not support when this record was written; record #3 below
  is a separate recurrence.
- **Knowledge boundary** — observed for this unit's table. Not checked: whether earlier changes' claims tables carry
  line-range commands that no longer reproduce at their recorded base.
- **Decision** — no-change: `review-gate` §1 already requires reproduction, base anchoring, and re-derivation after
  edits, so the incident needs no new rule.

### extend-owner-reply-language-scope#2 — An unescaped table pipe split three claims-table rows

- **Recorded** — 2026-10-27, main agent session, during implementation (the review gate's pre-write pass).
- **Stage** — implementation.
- **Expected** — a markdown table row carries one unescaped `|` per column boundary, so a command written into a cell
  keeps its pipes escaped; `AGENTS.md`'s programmatic-edit gotcha states that a bare `|` splits the cell.
- **Observed** — while pinning three claims-table rows to the base revision, the commands (`git show … | grep -A9 …`,
  `… | awk … | grep -c …`, `… | sed -n …`) were written with bare pipes. The rows split into extra columns: the site
  roles table beside them still rendered with two columns, the claims table's three affected rows rendered with five to
  seven. `spotlessApply`/`spotlessCheck` stayed green, and `scripts/claim-scan.py` reported nothing about the table
  shape.
- **Evidence** — the 120-character check's output shows the split rows, e.g. `| The owner-reply bullet scopes … | `git
  show 50799b60:AGENTS.md | grep -A9 "Output language follows"`  | the six sub-bullets; …`; a per-row count of unescaped
  pipes before the fix reported 6 to 8 for the three rows where 4 is correct.
- **Consequence** — fixed by escaping every pipe inside the three command cells and re-checking each row's
  unescaped-pipe count (4 for the three-column table, 3 for the two-column site table); verified after `spotlessApply`.
  No other table in the unit's artifacts was affected.
- **Knowledge boundary** — checked this unit's artifacts only; not checked whether other change dirs carry rows split
  this way, which no gate reads.
- **Decision** — proposed: routed to the parked table-integrity check, which now carries this incident (`docs/ideas.md`,
  "Add a markdown table-integrity check"); the PR link replaces this one before merge.

### extend-owner-reply-language-scope#3 — A claims row and a Done note were not re-run after their own edit

- **Recorded** — 2026-10-27, main agent session, at review round 2.
- **Stage** — review.
- **Expected** — the command a claims-table row or a task's Done note states is run after the edit that introduces it,
  and its recorded output is that run's output; the unit's task 6.3 now makes this the last act before the manual pass.
- **Observed** — round 2 found two instances, both of edit-then-report rather than run-then-record. A claims row added
  in the round-1 fix quoted `grep -n "One probe evidences the case"`, which is case-sensitive and printed nothing, while
  the row claimed two hits; the two lines exist only under `-i`. Task 4.2's Done note claimed the sweep returned "only
  the convention and the delta's scenario titles", but the named paths exclude `openspec/changes/`, so the delta could
  not be in that output, and the output the command does produce includes the main spec's scenario title at
  `openspec/specs/showcase/quality/agent-skills/spec.md:2501`.
- **Evidence** — `grep -n "One probe evidences the case" …/unify-agent-language-policy/tasks.md` exits 1 with no output;
  `grep -in "evidences the case" …` prints lines 113 and 117. The task-4.2 grep, run as written, prints
  `AGENTS.md:1127`, `AGENTS.md:1134`, `AGENTS.md:1135`, and `openspec/specs/showcase/quality/agent-skills/spec.md:2501`;
  adding `openspec/` to the path set returns the change's own artifacts as well.
- **Consequence** — the claims row now uses `grep -in` and records 113/117; task 4.2 now states the sweep's actual four
  lines, the two direct reads, and that the Purpose paraphrase is task 5.1's site; task 6.3 re-derives rows and Done
  notes immediately before the ask. Nothing is committed, so no artifact shipped the unverified claims.
- **Knowledge boundary** — observed in this unit's round 1 and round 2. Not checked: whether other units' claims tables
  or Done notes carry commands their stated output does not reproduce.
- **Decision** — no-change: covered by `review-gate` §1's re-run rule and that unit's task 6.3, which re-ran every
  claims row and Done note; no new rule is owed.
