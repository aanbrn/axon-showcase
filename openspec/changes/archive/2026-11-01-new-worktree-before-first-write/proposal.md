# Proposal

## Why

Two 2026-11-01 process-defect records show a created worktree written to before its IntelliJ preparation, and a third
shows the setup run before the primary's interpreter selection was inherited. The Workspace bullet's deadline — "before
it is opened" — is one the agent cannot observe, and creation and preparation are two separate steps it must remember.
The owner has had to raise the omission himself. Implementing the parked `docs/ideas.md` 2026-11-01 entry replaces those
steps with one command whose supported path creates the tree, inherits the primary's interpreter selection, prepares it,
and fails loudly unless the preparation succeeds and its Python stage reports `CONFIGURED`.

## What Changes

- **`scripts/new-worktree.sh`** — new wrapper: fetches `origin/main`, creates the sibling tree against the primary
  explicitly with `--no-track`, copies the primary's `.python-version` when the tree has none, runs
  `./scripts/setup-idea.sh` there, prints both stage lines as it runs, and — only when the primary carries `.idea/` —
  exits non-zero unless the preparation succeeds and its Python stage reports `CONFIGURED`. A `--continue <branch>` form
  creates a tree on an existing branch. It never moves a session and never deletes or recreates a tree it could not
  prepare.
- **`scripts/test-new-worktree.sh`** — POSIX `sh` controls over throwaway repositories and clones: the new-tree path,
  the continued-branch path (including a branch name carrying `/` and a branch that exists only on the remote), a tree
  already carrying `.python-version`, the primary-without-`.idea/` case, `SKIPPED` with exit zero, and a failed
  preparation that keeps the tree and reports it.
- **`.opencode/commands/new-worktree.md`** — the trigger for the tool, since the tooling-placement rule gives a
  `scripts/` tool the agent drives its own command.
- **`AGENTS.md`, Workspace** — the deadline is the tree's first unit-content write (not the checkout, not the setup's
  own writes), and the ordinary creation and preparation instructions become the wrapper's invocation.
- **`.opencode/skills/setup-idea/SKILL.md`** — the initialization trigger line follows the first-write deadline and
  points the creation case at the wrapper.
- **`build.gradle.kts`** — registers `testNewWorktree` in `check`.
- **`docs/ideas.md`** — the 2026-11-01 created-tree entry is removed: this change implements it.
- **`README.md`** — a `/new-worktree` row in the slash-command table, and the wrapper named in Parallel Units.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

`showcase/quality/ide-config`: the created-tree requirement gains the first-unit-write readiness boundary and the
unsuccessful-preparation outcome. The wrapper's filename and its command are not specified there — the spec states the
outcome, not the mechanism.

## Impact

- **Workspace procedure**: the change alters the guidance every unit follows when it creates a tree, and the setup
  skill's initialization section.
- **No runtime, deployment or data path**: the wrapper only drives `git worktree` and the existing setup script.
- **Honest limit**: the wrapper enforces the sequence when it is invoked. It cannot prevent a caller that runs
  `git worktree add` directly, and this change claims sequencing automation, not a measured reduction in omissions.
