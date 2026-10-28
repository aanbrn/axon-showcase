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
