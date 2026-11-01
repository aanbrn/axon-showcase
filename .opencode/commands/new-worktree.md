---
description:
  Create a unit's sibling worktree and prepare it for IntelliJ in one operation, and report whether it is ready
---

Create a unit's tree and prepare it in one step, so creation and preparation cannot be separated by a unit edit.

**Trigger** — the owner invokes `/new-worktree <unit>` (or `--continue <branch>`), or the Workspace procedure needs a
tree the primary cannot carry.

**Procedure**

1. Run `./scripts/new-worktree.sh <unit>` (or `--continue <branch>`). The script's own header states the sequence it
   runs and its exit contract.
2. Read its report: the tree path, its branch, the stage lines, and the readiness line — `Tree ready:`,
   `Tree NOT ready:`, or, when the primary carries no `.idea/`, the statement that no IntelliJ preparation was owed.
3. IF the tree is ready, THEN move the session into it (`session_move`) before writing anything in it; the script never
   moves the session.

**Decision points**

- IF the primary carries no `.idea/`, THEN the script reports that no IntelliJ preparation was owed and exits zero; the
  tree is still created.
- IF it reports `Tree NOT ready:`, THEN the tree is kept: hand the owner the Python line and the remedy it names — for
  `NEEDS_ACTION`, the `setup-idea` skill's block; for a failed run, the stage that failed — and write nothing in the
  tree until the preparation succeeds and its Python stage reports `CONFIGURED`.
- IF the destination already exists or the branch is already checked out, THEN stop and ask; never delete or take over a
  tree.

**Failure handling** — a non-zero exit means the tree exists but is not ready (1), or the invocation was refused (2) by
a usage error, an occupied destination, or a git failure, in which case no tree was created by this invocation. A
refused or failed run never deletes a tree.

**Verification** — the script's own report: the tree path and branch, the `Settings:` and `Python:` lines, and the
readiness line.

**Completion criteria** — the tree exists, its readiness is reported, and the next step — move the session into it, or
fix the cause it names — is stated.
