# Decision record — Air's worktree session mode and this repository's tree assignment

A dated record of a probe and what it decided. The change `document-air-worktree-safety` needed to know what JetBrains
Air's **New Local Worktree** session mode does to a repository whose unit owns one named tree with its work deliberately
uncommitted; the probe measured it in two runs on a disposable clone and the answer bounds the guidance the change
ships. It is not a unit record, so it lives in `docs/decisions/` rather than `docs/units/`.

## The question

Does Air's worktree mode leave a unit session outside the tree its record names, and does the commit guard refuse a
commit made there?

## The probe

A throwaway clone of this repository with no remote and no reachable origin.

## Pre-state (task 1.1)

- **Clone**: `/var/folders/rd/nw7pt03523dc_z_pzq1_rdjw0000gn/T/opencode/air-worktree-exercise`
- **HEAD**: `805f30ef8d748826054abbfb83f7ca58cd0f455c`, branch `document-air-worktree-safety`
- **Guard armed**: `core.hooksPath` = `scripts/git-hooks`; `scripts/git-hooks/pre-commit` present and executable
- **Commit identity**: `Air Worktree Exercise <air-worktree-exercise@example.invalid>`
- **Markers**:
  - staged new file `exercise-staged.txt` — `A `, content `staged marker: air-worktree-exercise`: 36 content bytes + 1
    newline = 37 bytes
  - unstaged edit of the tracked `.gitattributes` — ` M`, appending a blank line and
    `# unstaged marker: air-worktree-exercise`: 39 bytes at `805f30ef` + 1 (blank-line newline) + 40 (line) + 1 (its
    newline) = 81 bytes, the size `wc -c` reported
  - untracked `exercise-untracked.txt` — `??`, content `untracked marker: air-worktree-exercise`: 39 + 1 newline = 40
    bytes
- **Registry**: the clone's `git worktree list` names only the clone.

## Observation, run 1 (task 1.2)

- **Registry**: the clone now lists a second worktree — `~/.air/worktrees/temporary/air-worktree-exercise-amber-otter`,
  branch `air/document-air-worktree-safety-amber-otter`, both at `805f30ef`. The record Air keeps
  (`air/managed-worktrees-v1/records/81ed9e62-….json`) names the clone as `sourceRepositoryPath` and
  `sourceProjectPath`, the same `exactRevision`, and `snapshotAvailable: true`.
- **The clone**: unchanged — ` M .gitattributes`, `A  exercise-staged.txt`, `?? exercise-untracked.txt`, all three
  contents intact.
- **Air's tree**: `git status --porcelain` empty, both diffs empty, `.gitattributes` at the committed 39 bytes (the
  clone's is 81), `exercise-staged.txt` and `exercise-untracked.txt` absent. A clean checkout at `805f30ef`.
- **Air's own snapshot**: `~/.air/worktrees/archives/81ed9e62-…/` holds a 0-byte `index.patch`, a 0-byte
  `worktree.patch`, and a manifest with `statusFingerprint: ""` and `untrackedEntries: []` — Air captured no change at
  all, while the source project held three classes.
- **Conclusion (run 1)**: Air's worktree is a new branch and a separate tree, and the unit's uncommitted work did
  **not** reach it. The plugin string promising a copy did not hold in this run; one run cannot tell a defect from a
  condition, so a second run follows before the wording is settled.

## Observation, run 2 (task 1.2) and the guard check (task 1.3)

- **Registry after run 2**: a third tree, `~/.air/worktrees/temporary/air-worktree-exercise-zesty-badger`, branch
  `air/document-air-worktree-safety-zesty-badger`, at `805f30ef`.
- **Air's tree (run 2)**: `git status` empty, both diffs empty, `.gitattributes` at the committed 39 bytes, and none of
  `exercise-staged.txt`, `exercise-untracked.txt`, `exercise-fresh.txt` present. Its snapshot archive (`3cdb2449-…`)
  holds 0-byte patches and `statusFingerprint: ""`, exactly as run 1's did.
- **The clone after run 2**: `git status --porcelain` lists ` M .gitattributes`, `A  exercise-staged.txt`,
  `?? exercise-fresh.txt`, `?? exercise-untracked.txt`; the cached diff is `A exercise-staged.txt` and the worktree diff
  `M .gitattributes`; the four files are 81, 37, 66, and 40 bytes. Every marker and its content survived both runs.
- **The fresh marker**: created after the clone's project had been open and indexed, with the content
  `fresh marker, created after the IDE opened: air-worktree-exercise`: 65 + 1 newline = 66 bytes. It is in the clone's
  status above and absent from Air's tree. Why Air captured no changes remains undetermined.
- **Guard check in Air's tree**: the clone's `core.hooksPath=scripts/git-hooks` is shared into the worktree, and
  `scripts/git-hooks/pre-commit` is present and executable; branch `air/document-air-worktree-safety-zesty-badger`;
  `git rev-parse --git-path axon-showcase-change` resolved to
  `<clone>/.git/worktrees/air-worktree-exercise-zesty-badger/axon-showcase-change` and that file is absent; `HEAD`
  `805f30ef`; a marker staged as `A exercise-commit-attempt.txt`; `git commit` exited 1 with `No commit assignment:`
  naming the branch `air/document-air-worktree-safety-zesty-badger` and the claim path; `HEAD` stayed `805f30ef`.
- **Conclusion**: two runs agree. Air's worktree is a new branch and a separate, clean tree at the source commit; the
  unit's uncommitted work does not reach it, despite the creation dialog's copy promise. The guard refuses a commit
  there for the missing assignment, naming the branch and the per-worktree claim path.

## Cleanup (task 1.4)

- Removed both Air worktrees (`amber-otter` with `git worktree remove`, `zesty-badger` with `--force` because the commit
  attempt had staged a file), deleted both branches, and pruned; the clone's registry then listed only the clone.
- Deleted the clone directory; `test ! -e` holds, and this repository's `git worktree list` names only the primary and
  the unit's tree.
- Residual outside the repository: Air's own bookkeeping under `~/.air/worktrees/archives/` and
  `~/Library/Caches/JetBrains/IntelliJIdea2026.3/air/managed-worktrees-v1/` still records the two removed worktrees,
  whose repository no longer exists. They are Air's state, not the repository's, and the project `air-worktree-exercise`
  stays in the IDE's recent projects until the owner closes it.

## Reopening condition

Reopen this decision when any one of the following holds:

- **Air starts carrying the work** — a later Air build's creation flow transfers a source project's staged, unstaged, or
  untracked changes into its worktree (signal: the same exercise finds the markers in Air's tree). A non-empty snapshot
  patch under `~/.air/worktrees/archives/<id>/` is an investigation trigger, not the signal: it establishes capture, not
  transfer. When the destination-tree observation holds, the guidance has to describe the copy, and the recovery steps
  have to preserve a duplicate as well.
- **The mode moves or is renamed** — the worktree mode's label or placement changes in the bundled `air-plugin`
  (external coordinate: `messages/AirWorktreeBundle.properties`, key `worktree.run.mode`), since the guidance names it.
- **Local mode stops keeping the unit's tree** — an observation shows a Local-mode session working outside the tree its
  unit record names, which withdraws the recommendation rather than this record's premise.
