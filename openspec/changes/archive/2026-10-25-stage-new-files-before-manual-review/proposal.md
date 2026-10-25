# Proposal

## Why

A unit's work stays uncommitted until the owner approves it, and its new files stay **untracked** — the change dir plus
any new source or test file. Untracked files do not appear in IntelliJ IDEA's Changes panel, so the owner's manual
review silently omitted every new file while showing every modified one. The owner confirmed the mechanism: after
`git add` the new files appear in the panel as Added (green), distinct from modified (blue). The gap is already recorded
in `AGENTS.md` — `git diff --name-only` omits the untracked change dir — but nothing closed it for the human pass.

## What Changes

- **`.opencode/skills/review-gate/SKILL.md`** — §5 (manual review pass) gains the staging step; the skill owns the
  caller's procedure, so the step lives there.
  - Before the pass is requested, the unit's own new files are staged with `git add -- <explicit paths>`, planning
    artifacts included.
  - Those files are re-staged after any later edit to them.
  - `git add -A`, `git add .`, and `git add -N` are never used for this.
  - A unit with no new files adds no step.
  - The ask names the review surface the owner is shown.
- **`AGENTS.md`** — every site `design.md` decision 6 marks _edited_ is updated, in three obligations:
  - The lifecycle step states the review surface — the unit's workspace, with its new files staged for the pass — and
    points at the skill for the staging step rather than implying that a plain `git diff` covers the work.
  - Every statement that relies on the change dir being untracked is scoped to the state before the manual pass stages
    it.
  - Of those, each statement asserting reset or checkout safety states the staged-case effect and the condition it holds
    under; the ones other than the statement that names the preserve → reset → restore ordering point at it.
- **`README.md`** — the development narrative says the agent stages a unit's new files so the manual review shows them.

## Capabilities

### New Capabilities

none

### Modified Capabilities

none — no requirement changes. `showcase/quality/agent-skills` owns the review gate only as _ownership_: its requirement
"The review gate's caller procedure is maintained in its project skill" sends a step correction into the skill
(`spec.md:2328`) and states no review surface or staging behavior, so a step added there changes no scenario's outcome.
Grepped for the behavior across `openspec/specs/`: `manual review` matches two clauses (a gate member and "before a
manual review is requested"), `untracked` only `showcase/quality/commit-hygiene`'s spec-sync check, and `reset --hard`
nothing. The change therefore carries `skip_specs: true`, as `fix-advisor-worktree-read` did for the same class of
skill/root correction.

## Impact

- Documentation and agent tooling only: two project-authored guidance files and the README. No code, API, dependency,
  build task, or deployment change.
- The pre-commit guard is unchanged but newly relevant: a path staged and then edited keeps refusing the commit
  (`showcase/quality/commit-hygiene` already specifies it), which is what makes the re-stage rule necessary.
- No new tool, command, or skill file. `spotlessCheck` and the root `check` must stay green over the edited markdown and
  the guidance-file scans (`verifyCapturedMarkers`, `verifyInstallCommands`) it runs.
