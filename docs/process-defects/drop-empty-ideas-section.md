# Process defects — drop-empty-ideas-section

### drop-empty-ideas-section#1 — A claims row cited the unstaged diff on a fully staged tree

**Recorded** — 2026-10-30, by the unit's writing session.

**Stage** — review.

**Expected** — a claims row names a command that reproduces its result in the tree as handed over. This unit stages its
work before the pass, so `git diff` reads empty and `git diff --cached` is the diff.

**Observed** — the row for the two removed lines cited `git diff --stat` and reported `1 file changed, 2 deletions(-)`.
The unstaged diff is empty; the staged diff is two files, and the quoted figure holds only with the path the row did not
name.

**Evidence** — the reviewer ran both: `git diff --stat` printed nothing, and the row's figure reproduced under
`git diff --cached --stat -- docs/ideas.md`.

**Consequence** — one finding in the first review round; the row now names the staged, path-scoped command. The same
slip was raised once in the previous unit's claims table.

**Knowledge boundary** — observed: the row, both commands' output, and the correction. Not checked: whether any other
row of this unit's table names a command whose output depends on the index.
