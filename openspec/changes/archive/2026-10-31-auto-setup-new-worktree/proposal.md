## Why

Every new working tree starts with no IntelliJ configuration, and its Python stage resolves whatever `python3` the
tree's own `PATH` finds. A contributor who configured Python once in the primary — the interpreter registered and
`.python-version` written — must repeat it in each tree, and a tree that resolves a different, unregistered interpreter
reports `NEEDS_ACTION` and opens without Python analysis. The workspace procedure creates the tree and moves the session
there, but says nothing about preparing the IDE before the tree is opened.

## What Changes

- `AGENTS.md`'s Workspace procedure gains the preparation step: after the tree is created and its path and branch
  verified, the agent inherits the primary checkout's interpreter selection into the tree when the tree has none, runs
  `./scripts/setup-idea.sh` there, and records the stage outcomes; the tree is reported IDE-ready only when the Python
  stage reports `CONFIGURED`.
- `.opencode/skills/setup-idea/SKILL.md` carries the step's details: when to inherit, never to overwrite a selection the
  tree already has, what to report when the primary has no selection or the stage cannot complete, and that only trees
  created by the process are prepared this way.
- `openspec/specs/showcase/quality/ide-config/spec.md` gains a `MODIFIED` delta for "The Python analysis interpreter is
  the one the build runs": a tree the workspace procedure creates inherits the primary's interpreter selection before
  the setup resolves it, and one scenario pins it.
- `README.md`'s Python-analysis bullet states the inheritance ahead of its one-run claim.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/ide-config` — the interpreter requirement gains the new tree's inherited selection.

## Impact

- Guidance: the `Workspace` rule in `AGENTS.md`, the `setup-idea` skill's initialization section, and `README.md`'s
  Python-analysis bullet.
- Specs: a `MODIFIED` delta on `showcase/quality/ide-config` only; the main spec changes at archive.
- No code, script, test, build, dependency, image, chart, or deployment change: the procedure reuses
  `scripts/setup-idea.sh`, and a manually created worktree stays outside the obligation.
