# Design

## Context

The pre-commit guard's assignment check reads the commit's assigned change (the claim at the worktree's
`axon-showcase-change`, or a change-named branch) and refuses a staged path under another change's directory:
`find_foreign_change_paths` returns every staged change path whose name differs from the assignment, and `check_staged`
prints it. A merge commit stages the merged revision's content, so a refresh by merge that carries another change's
archived directory is refused; the documented workaround, `git commit --no-verify`, disables every check for that commit
— the conflict-marker and stale-index checks a conflict resolution needs most.

The guard already separates the staged states of a change path: `staged_change_paths` composes the added, modified, or
renamed-to read (`staged_paths`) with a deleted read and the rename sources, so each path it reports is either held by
the index or absent from it. The merge state is git's `MERGE_HEAD`, present for the whole construction of a merge commit
and absent for an ordinary one.

## Goals and non-goals

- **Goal** — a merge commit may carry the change directories and archive moves its merged revisions hold, without
  `--no-verify`.
- **Goal** — every other refusal keeps working: an ordinary commit, a foreign path staged during a merge that no merged
  revision holds, and a foreign deletion a merge does not make.
- **Non-goal** — a new mode, configuration key, or build task.
- **Non-goal** — a squash merge: it records no merged revision, and the documented refresh routes are a fast-forward, a
  rebase, or `gh pr update-branch`.

## Decisions

### The exemption is per path, not per commit

A blanket "a merge commit skips the assignment check" is two lines and wrong: the index during a merge accepts staging
too, so that rule would let any foreign change directory ride a merge commit. The exemption is therefore per path, and
only for a path a merged revision accounts for: a path the index holds when that revision holds it too, and a path the
index lacks only when a fork held it and the merged revision removed it.

### Presence, with the merge base as a deletion's evidence

Comparing the staged blob with the merged revision's blob would refuse two legitimate states: a conflict resolution
inside a merged-in foreign path (rare, but the ledger-style files a merge touches are exactly where conflicts land) and
the rename git reports for a delete-plus-add. Presence covers additions, modifications, and rename destinations with one
rule: the path is exempt exactly when a merged revision holds it.

A deletion needs one more fact, because presence alone cannot tell a deletion the merge made from one the commit made: a
path absent from the index and absent from the merged revision gives the same two answers whether the merge removed it
or never held it, so under a bare presence comparison a commit could delete any foreign change directory during a merge
— a hole the first implementation had and a review round reproduced. A deletion is therefore exempt only when a fork
held the path (`merge-base HEAD <revision>`) and no merged revision does.

### The merge state is `MERGE_HEAD`, read as a file

`git rev-parse --verify --quiet MERGE_HEAD` answers only the first parent of an octopus merge — probed: a three-parent
merge leaves three lines in `MERGE_HEAD` while `rev-parse` printed one. The exemption must cover every merged revision,
so `merge_revisions` resolves the file through `git rev-parse --git-path MERGE_HEAD` (which a linked worktree resolves
to its own git directory) and reads all of its lines. A missing file is the answer "no merge is in progress"; any other
read failure raises `GitFailure`, so an unreadable merge state stops the check instead of reading as an ordinary commit.

### What stays refused, and why a squash merge is not recognized

The mechanism keys on the merge state, not on "the staged content came from somewhere else", so:

- an ordinary commit is judged exactly as before;
- a foreign path staged during a merge that no merged revision holds is refused;
- deleting a foreign path is refused unless a fork held it and the merge removed it, so both a path the merged revision
  still holds and one the merge never had are refused;
- `git merge --squash` writes no `MERGE_HEAD` (probed: the staged foreign archive and exit 1 from the guard), so a
  squash merge is refused and needs `--no-verify`. The repository's documented refresh routes are a fast-forward, a
  rebase, or `gh pr update-branch`, none of which is a local squash merge, so this is the accepted boundary rather than
  a gap to close with a second mechanism.

## Accepted false positives and negatives

- **A foreign path the commit also edits.** If the merge brings a foreign path and the commit stages different content
  for it, presence still matches and the path is exempt. A unit must not edit another change's artifacts, so the state
  is not a legitimate one to protect.
- **A staged foreign path a merged revision happens to hold.** The same predicate exempts locally staged content for a
  path the merge brought. Distinguishing the two would take a content comparison, whose cost is the false refusals the
  previous decision rejects.
- **A squash merge is refused.** Recorded above as the accepted boundary.

## Claims table

Every row's source reproduces at the base revision `eefe254372637d2f45c7909bbc1e789dd0102266`; the pre-change rows use
`git show`/`git grep` at that revision, since the working tree no longer shows the state they describe.

- The assignment check prints one message form for a foreign change path —
  `git grep -c "Path belongs to change" eefe2543 -- scripts/commit-hygiene.py` prints
  `eefe2543:scripts/commit-hygiene.py:1`.
- `staged_change_paths` composes the added/modified/renamed-to read, a deleted read, and the rename sources —
  `git show eefe2543:scripts/commit-hygiene.py | sed -n '/^def staged_change_paths/,/^$/p'` prints the three terms.
- `find_foreign_change_paths` is the only judge of ownership and `check_staged` its only caller —
  `git show eefe2543:scripts/commit-hygiene.py | grep -n "find_foreign_change_paths\|staged_change_paths"` prints four
  lines: the two definitions, the loop, and the call in `check_staged`. The same grep over the tests prints one line,
  the failed-read case list this unit renames, so it names neither judge nor caller.
- `MERGE_HEAD` exists for the whole construction of a merge commit and is gone once it is created — probed in a scratch
  repository: after `git merge incoming --no-commit`, `git rev-parse --verify --quiet MERGE_HEAD` printed the merged
  revision and exited 0; after the commit it exited 1 and the file was absent.
- A missing `MERGE_HEAD` is exit 1, not an error — `git rev-parse --verify --quiet MERGE_HEAD` on a repository with no
  merge in progress exits 1, while the same command outside a repository (and `--git-path` there) exits 128.
- `git rev-parse` answers only the first parent of an octopus merge — probed: a three-parent `--no-commit` merge left
  three lines in `MERGE_HEAD`, and `git rev-parse --verify --quiet MERGE_HEAD` printed one.
- `git merge-base` answers "no common ancestor" with exit 1 and a fatal read with a higher code — probed in a scratch
  repository: two orphan roots exit 1, and an unresolvable revision prints `fatal: Not a valid object name no-such-rev`
  and exits 128; `merge_base_of` splits the two, and `merge_base_paths` reads a fork's tree only when one exists.
- A squash merge writes no merge state — probed: after `git merge --squash incoming` the staged foreign archive was
  present, `git rev-parse --verify --quiet MERGE_HEAD` exited 1, and the guard refused the staged set.
- `git ls-tree -r -z --name-only <revision>` lists every path a revision holds — `git ls-tree --help` names `-r`
  (recurse into subtrees) and `--name-only` (print only the name) — and probed on the scratch repository it printed the
  merged archive path, while a path the revision does not hold was absent from the output.

`scripts/claim-scan.py --base eefe2543` seeded this table; one dismissal per candidate kind covers the rest of its
output:

- `count` — a task number, or a test assertion's expected exit status, never a claim about a corpus.
- `absolute` — an adverb (`only`, `every`, `none`) inside a statement whose own sentence fixes its scope, or the guard's
  literal `none` assignment.
- `cross-ref` — a fixture path the unit's own test creates, or the unit's workspace path.
- `path` — the delta's copied requirement text, and the commands this table cites.

## Alternatives rejected

- **Exempt the assignment check for any commit with a merge state.** Rejected: the index accepts staging during a merge,
  so the rule would let a foreign change directory through a merge commit — the silent hole this check exists to close.
- **Compare the staged blob with the merged revision's blob.** Rejected: it refuses a legitimate conflict resolution in
  a merged-in path and the delete-plus-add pair, for no additional precision on the states that matter.
- **Read `MERGE_HEAD` with `rev-parse --verify` instead of the file.** Rejected: it answers only the first parent, so an
  octopus merge would still be refused; the file is what git writes and holds every parent.
- **Recognize a squash merge by `SQUASH_MSG`.** Rejected: a squash merge produces an ordinary commit with no relation to
  the revisions it squashed, so the exemption would cover no merged revision and every path would be exempt — a blanket
  bypass under another name.
