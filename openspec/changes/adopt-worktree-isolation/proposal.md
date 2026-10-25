# Proposal

## Why

Parallel units collide in one checkout: the primary checkout holds an in-flight unit's uncommitted work, so a second
unit's first write can land in the same tree — a scratch test did exactly that in this session, and only the owner's
intervention stopped it. The parked worktree idea (`docs/ideas.md`, 2026-10-10) already chose the remedy; the process
never adopted it.

## What Changes

- **`AGENTS.md`** — the change lifecycle gains the workspace rule: one active unit per working tree, the primary
  checkout counted as a unit's tree, a sibling `../axon-showcase-<unit>` worktree for a unit that finds the primary
  occupied, and a workspace preflight recorded before the first write.
- **`.opencode/skills/ship-change/SKILL.md`** — cleanup after merge: verify no unsaved work, move the session out,
  `git worktree remove` without `--force`, and `git branch -D` only after verifying the PR merged with no unshipped
  commits left.
- **`.opencode/agent/review-quick.md`** — the reviewer's hand-over enumeration gains the unit's assigned working tree
  and branch, so a reviewer reads the tree the unit actually wrote in.
- **`.opencode/commands/*.md`** — the mutating commands' lifecycle preflight states the workspace rule beside the branch
  rule.
- **`openspec/specs/showcase/quality/agent-skills/spec.md`** — via a delta: the lifecycle-preflight requirement extends
  to the unit's assigned working tree, and the quick-review requirement's hand-over field set names that working tree
  and branch.
- **`README.md`** — a short note on parallel units and where their trees live.
- **`docs/ideas.md`** — remove the implemented idea and park the follow-up that verifies the rule on the first real
  parallel units, with its completion criteria.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the lifecycle-preflight requirement now covers the unit's assigned working tree, not
  only its branch and uncommitted state; and the quick-review requirement's hand-over field set names that working tree
  and branch, so a reviewer or a replacement reviewer reads the tree the unit actually wrote in.

## Impact

- **Process and tooling**: `AGENTS.md`, the `ship-change` skill, the `review-quick` definition, the mutating commands'
  preflight, and the README; no source, build, or dependency change.
- **Repository layout**: a parallel unit's tree lives beside the primary checkout as `../axon-showcase-<unit>`; the
  primary stays the sequential unit's working tree, so no `.gitignore` entry and no build output move.
- **Disk**: in a historical measurement (`docs/ideas.md`, 2026-10-12) a sibling tree occupied ~628 MB after its first
  Docker-free `check`; sequential units reuse the primary's prepared tree, and a parallel unit pays that cost only while
  its tree exists.
- **Tests, builds, deployments**: unchanged; no new CLI, vendored skill, or tooling check.
