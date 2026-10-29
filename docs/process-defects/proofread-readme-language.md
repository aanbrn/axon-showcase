# proofread-readme-language

### proofread-readme-language#1 — The proofread delegation read the wrong tree's target file

**Recorded** — 2026-10-29, by the unit's writing session.

**Stage** — implementation.

**Expected** — a delegation to the `technical-advisor` for a whole-file proofread hands it the file the unit will edit,
and the first pass reads the version at the unit's base.

**Observed** — the advisor session was started while the calling session was still in the primary checkout, so it
inherited that workspace. The primary's `README.md` carries the in-flight `guard-the-ledger-at-commit` unit's
uncommitted edits to lines 846–860 (`seven`→`eight` commit-hygiene slips, plus the review-round-ledger clause) and
therefore differs from the unit's `origin/main` copy. The first pass was blocked outright (`read` denied with
`external_directory` when the prompt named the worktree path), so no proposal was produced against either tree.

**Evidence** — `diff` of the two working trees' `README.md` reports exactly two changed hunks, at 846 and 849–860; the
first pass returned "The requested file is outside this session's workspace; `read` was denied with
`external_directory`." The unit's base hash and the primary's working-tree hash for the file differ (`00df8649…` vs
`a10df06a…`).

**Consequence** — one advisor pass was spent on the denial, and the recovery was a `session_move` of the advisor session
into the unit's worktree before re-issuing the pass. No wrong proposal reached the file, and no fact was corrupted; the
cost was one round trip, and the hazard it exposed would have been silent had the read succeeded.

**Knowledge boundary** — observed: the two trees' `README.md` differ, and the denial message. Not checked: whether the
advisor would have used the sibling tree's line numbers had the read been permitted, and whether any other subagent
definition resolves its file reads against the session workspace rather than an absolute path.

### proofread-readme-language#2 — A PR body carried a count no command produced

**Recorded** — 2026-10-29, by this unit's writing session, while working in the next unit's tree.

**Stage** — ship.

**Expected** — every count in a pull-request body is derived from a command whose output is quoted, per the pre-write
pass's claims-table rule.

**Observed** — the first draft of this unit's PR body stated "23 prose corrections across 20 sites". Neither figure came
from a command: `23` was a tally of the editor's item list, which lives outside the repository, and `20` was a hand
estimate of the affected sentences. Both stood beside a claims-table row that claimed the row was reproducible from
`git diff --numstat`, which reports line counts, not corrections.

**Evidence** — the draft read "23 prose corrections across 20 sites — …" and, in the claims table, "23 prose corrections
at 20 sites | `git diff --numstat origin/main -- README.md`, read hunk by hunk | 67/63 lines". The committed body
(`gh pr view 737`) reads "a set of prose corrections (21 hunks against `origin/main`)" with the row sourced to
`git diff -U0 … | grep -c '^@@'`, and the diff then measured 75/71 because a later formatter pass reflowed more lines
than the 67/63 the draft had recorded.

**Consequence** — caught before publication, so nothing false reached the tracker or the repository. The extra work was
re-deriving the two figures and rewriting the row. Nothing remains broken.

**Knowledge boundary** — observed: both drafts and their text. Not checked: whether the same hand-counted style appears
in another unit's PR body in this repository, and whether a gate could read a body at all, since a body is composed
outside the working tree.
