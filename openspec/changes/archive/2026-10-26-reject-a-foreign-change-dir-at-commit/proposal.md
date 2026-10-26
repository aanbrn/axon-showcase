# Proposal

## Why

A unit can stage another unit's change directory and commit it. The pre-commit guard verifies the staged set's mechanics
and its archive/spec pairing, but it has nothing that relates a staged change directory to the commit's own unit.

ADR-0024 rejects "a staged-set ownership check" **as a substitute for the write prohibition** — "it sees the consequence
after the write, and name-based attribution breaks on `opencode/…` branches and on archive moves" — and records the
prohibition itself as unensured. This change takes the other reading of that sentence: the check was rejected as a
_prohibition_, not as a consequence check, and the two reasons it was rejected are answerable — a recorded assignment
covers `opencode/…` continuations that no branch name can attribute, and comparing change identity covers the archive
move without rename detection. It implements that consequence check and claims nothing about the write.

## What Changes

- `scripts/commit-hygiene.py --staged` establishes the commit's **assigned change** before it reads the staged set. An
  added, modified or deleted path under the assigned change's `openspec/changes/<name>/` or
  `openspec/changes/archive/<date>-<name>/` passes, archive transition included; a foreign change's path of any of those
  kinds is refused, and so is a foreign directory deleted without archiving.
- The assignment is read from a recorded source, never inferred from the staged set. The **branch** names it when its
  name is a change name — one with a live `openspec/changes/<name>/` directory **or** an archived
  `openspec/changes/archive/<date>-<name>/` one, so the archive commit still resolves after `openspec archive` has moved
  the directory. Otherwise a **claim** at the worktree's git path `axon-showcase-change` names it, holding either a
  change name or the literal `none`; a claim and a branch that name different changes fail, and so does the absence of
  both.
- A **continued** unit — an `opencode/…` branch or a head pushed from elsewhere, whose name is not a change name —
  records the claim when it establishes its workspace, so the bootstrap replaces a migration.
- A unit that works without a change dir records `none`, and the check then refuses any staged path under
  `openspec/changes/`.
- Archiving stays legitimate for the assigned change: the deleted active directory and the added archived directory must
  carry the same change name, and git's rename detection plays no part.
- Releasing a tree removes the claim (`ship-change`'s settle step for the primary; a removed sibling worktree takes its
  own with it), so a sequential unit in the same checkout cannot inherit it.
- The guard's defect set gains a member, so every enumeration of it is updated: the hook's header, the `AGENTS.md`
  gotcha that lists what the guard refuses, and the README's count and list.
- `scripts/test-commit-hygiene.py` covers the matrix, and its shared fixture records an assignment so the existing
  `check_staged` cases keep their exact expected counts.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/commit-hygiene`: the pre-commit guard's requirement gains the assigned-change check, its recorded
  sources and the archive identity rule, its failure on an unestablished assignment, and the declaration that permits no
  change directory.

## Impact

`scripts/commit-hygiene.py`, `scripts/test-commit-hygiene.py`, `scripts/git-hooks/pre-commit` (its header enumerates the
checks), `AGENTS.md` (the `Workspace` rule's record, the gotcha listing the guard's checks, and a pointer to the settle
step that removes the claim), `.opencode/skills/ship-change/SKILL.md` (its settle step gains the removal), `README.md`
(its count and list of the guard's slips), and `docs/adr/0024-tree-write-right-is-not-session-bound.md` (its Context
says the guard has no notion of path ownership, which this change falsifies). No runtime code, no service behaviour, no
deployment surface; `check` keeps its current invocation, since the assignment check needs an assignment that only the
hook has.
