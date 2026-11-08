---
name: begin-unit
description: Establish an owner-authorized unit's workspace, record, and claim before its first content write
---

# Skill: begin-unit

The workspace-establishment procedure `AGENTS.md`'s lifecycle names at its **Begin** stage. This skill owns the ordered
steps; `AGENTS.md` owns the authorization, occupancy, preservation, and release rules the steps apply, and
`docs/contracts/process-bindings.md` locates their declared roles.

Source: `AGENTS.md` "Workflow: Unit lifecycle" → Workspace.

**Trigger** — the owner has authorized a unit to begin, or a session continues an unfinished unit that needs a tree.

## Steps

1. Establish the authorization and the unit's identity: its name, whether it is new or a continuation, and whether a
   change directory is owed. An authorization that names each direct edit is still an authorization to begin.
2. Read the registered trees (`git worktree list --porcelain`) and the assignment evidence. A tree with a unit branch is
   occupied whether clean or dirty; an unknown branch, a detached HEAD, or contradicting evidence is occupied until the
   owner resolves it. Never take over or remove an occupied tree.
3. Choose the route the registry supports:

   - **A free primary** — no other unit's branch is checked out there — is the unit's workspace. No tree is created:
     establish the unit's branch in it, `git switch -c <unit> --no-track origin/main` for a new unit (after fetching
     `origin/main`) or `git switch <branch>` for one continuing an existing branch, and leave every unrelated change
     preserved.
   - **A primary that belongs to another unit**, or whose ownership cannot be established, takes the **sibling route**:
     create and prepare a tree with `./scripts/new-worktree.sh` — `<unit>` for a new unit, `--continue <branch>` for a
     continuation. The wrapper resolves the primary from its own repository, never from the caller's cwd, opens a
     freshly fetched `origin/main`, and prepares the tree.
   - **A continuation whose branch already has a registered tree** takes no new tree: verify that tree's path, branch,
     and writer, and continue there.

4. Read the wrapper's report when the sibling route created a tree. When the primary carries no `.idea/`, the report
   states that no IntelliJ preparation was owed and the tree is ready once it exists. When the primary carries `.idea/`,
   the tree is ready only when the preparation succeeded and its Python stage reported `CONFIGURED`; a `Tree NOT ready:`
   line keeps the tree — hand the owner the remedy it names (`NEEDS_ACTION` points at the `setup-idea` skill's block; a
   failed run at the stage that failed) and write nothing in the tree until that succeeds. A refused invocation created
   no tree. The primary route owes no report: its branch is established directly.
5. Move the session into the workspace (`session_move`) when the session is not already in it; the wrapper never moves
   the session. Then verify the absolute path and branch before writing anything.
6. Write the unit record at `docs/units/<unit>/unit.md`: the `Workspace:` line naming the unit, the absolute path, the
   branch, and the writing session's own identifier, and the state `active`.
7. Write the claim at `git rev-parse --git-path axon-showcase-change` when the branch's name is not a change name; a
   change-named branch may omit it.
8. Set the session's title to the unit's record directory name with `opencode_session_rename` on the current main
   session, then read the title back. This applies whether the session began this unit or worked on another unit before.
   A failed rename or a readback that does not match is reported and does not block the unit; an explicit owner
   instruction to keep a different title is honoured.
9. Report the established stage, the pending decision, and the next permitted operation — the proposal for a two-pass
   unit, the implementation for a unit whose authorization names each direct edit.

## Decision points

- IF the primary is occupied or its ownership cannot be established, THEN use the sibling route; never switch an
  occupied primary to this unit.
- IF unrelated changes are already dirty in the chosen workspace, THEN preserve them and never revert or commit them.
- IF the sibling route's destination exists or the branch is already checked out there, THEN stop and ask; never delete
  or take over a tree.
- IF the route chosen needs a tree the registry does not have and the wrapper is unavailable, THEN stop and report it.

## Failure handling

- A non-zero wrapper exit means the tree exists but is not ready (1), or the invocation was refused (2) by a usage
  error, an occupied destination, or a git failure, in which case no tree was created. A refused or failed run never
  deletes a tree.
- A missing or unreadable `AGENTS.md` Workspace rule stops the step: report it rather than inventing the rule.

## Verification

- The workspace's path and branch (`git -C <workspace> rev-parse --abbrev-ref HEAD`) name the unit's branch.
- The record carries its four fields and the state `active`.
- The session's title, read back after the rename, is the unit's record directory name; the owner's explicit title
  preference was honoured instead; or the rename's failure is reported.
- On the sibling route, the wrapper's report also carries the readiness line, and — when the primary carries `.idea/` —
  the `Settings:` and `Python:` lines.

## Completion criteria

The workspace is established and prepared, the record and the applicable claim are written, the session's title names
the unit, the owner's explicit title preference was honoured, or the rename's failure is reported, and the next
permitted operation is stated.
