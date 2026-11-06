# Keep Air-hosted units on the repository's worktree process

## Why

Air can start an agent session in a temporary Git worktree of its own: it creates its own branch and a tree under
`~/.air/worktrees/temporary/`. In the two recorded disposable-clone runs on IDEA 2026.3, Air created clean checkouts at
the source commit, and the source retained all staged, unstaged, and untracked markers
(`docs/decisions/2026-11-06-air-worktree-exercise.md`). A repository unit owns one tree — the primary checkout while it
is free, otherwise a sibling worktree created and prepared by `scripts/new-worktree.sh` — named by its record, with
review-stage work left uncommitted. A session started in Air's worktree mode therefore works outside that tree, on a
branch no repository record assigns, without the unit's uncommitted work. The missing-assignment check can reject a
commit from an unfamiliar branch when the installed hook runs, but it neither prevents nor undoes the separation.

## What Changes

- **`AGENTS.md`** — one warning at the Workspace decision point: an Air-hosted unit runs in **Local** mode in its
  assigned tree, never in **New Local Worktree**, with a pointer to the setup skill for session selection and recovery.
- **`.opencode/skills/setup-agent-tools/SKILL.md`** — a short safety block in the Air section, version-qualified. Before
  a unit session starts:
  - choose **Local** in the assigned tree;
  - verify the session's absolute path and branch against the unit record before the first write;
  - reach for another tree only through `scripts/new-worktree.sh`;
  - keep Air's automatic worktree cleanup disabled.

  If **New Local Worktree** was selected by accident:
  1. stop the session's work;
  2. change nothing — no commit, push, reset, or deletion of either tree or Air's branch;
  3. find both trees in the registry;
  4. preserve their unique files and commits, staged and untracked work included;
  5. present the differences and a recovery proposal to the owner;
  6. stop for their decision, and never add a claim or rewrite the unit record to make the stray tree pass the guard.

### New Capabilities

None.

### Modified Capabilities

None. No capability spec covers the setup skill or the workspace rule this changes —
`grep -rn "setup-agent-tools" openspec/specs/` returns no hits, and the unit-record requirements under
`showcase/quality/commit-hygiene` describe the record's shape, which this change leaves alone — so it is
`skip_specs: true`.

## Impact

- **Files**: `AGENTS.md`, `.opencode/skills/setup-agent-tools/SKILL.md`, and the change's own artifacts.
- **Code, APIs, dependencies**: none.
- **Guard, wrapper, README**: deliberately unchanged. Air creates its worktree without `new-worktree.sh`, so a check
  there cannot intercept it, and the guard's consequence checks are a backstop with stated boundaries rather than a
  prevention; the README is a showcase, not reference material.
- **Verification**: one disposable exercise before the wording is final — a throwaway clone with distinct staged,
  unstaged, and untracked markers plus one created after the IDE opened, two Air worktrees created from it, both trees
  compared in index and worktree state, and the missing-assignment refusal observed in Air's own tree with the guard
  armed.
