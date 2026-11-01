# bump-openspec-cli-2026-11-01 process defects

### bump-openspec-cli-2026-11-01#1 — The created worktree was not prepared for IntelliJ

- **Recorded**: 2026-11-01, by the implementing session.
- **Stage**: proposal.
- **Expected**: the workspace procedure's "Prepare a created tree before it is opened" step runs
  `./scripts/setup-idea.sh` in a tree the procedure created, when the primary checkout carries `.idea/`, before the tree
  is opened.
- **Observed**: the sibling worktree was created from `origin/main`, the session moved into it, and the unit's edits
  began without the script run. The owner raised it in the working session ("Опять забыл настроить IDEA."), and the
  script was then run in the tree.
- **Evidence**: `git worktree list --porcelain` listed the new tree at branch `bump-openspec-cli-2026-11-01` before any
  `./scripts/setup-idea.sh` run; the first run afterwards printed
  `Settings: CONFIGURED (created, created, created, created, created)` and
  `Python: CONFIGURED — wrote module and registration`, with the plugin install skipped because IntelliJ was running.
- **Consequence**: the tree was written to before it was prepared; the configuration, the Python module and the
  inherited `.python-version` now exist there, and the formatter plugins remain uninstalled until IntelliJ is closed and
  the script is re-run.
- **Knowledge boundary**: observed the missing run and its later output. Did not check whether the IDE had the tree open
  during the interval, and did not verify the plugin install, which the skipped stage did not attempt.

- **Decision** — shipped: https://github.com/aanbrn/axon-showcase/pull/793 — the created-tree wrapper creates and
  prepares a tree in one operation.

### bump-openspec-cli-2026-11-01#2 — The `lesson-capture` analysis ran inside an unfinished unit

- **Recorded**: 2026-11-01, by the implementing session, on the technical advisor's finding.
- **Stage**: review.
- **Expected**: `AGENTS.md`'s "Workflow: Generalize process defects" places a defect-driven improvement in its own unit,
  from a freshly fetched `origin/main`, consuming records that are already merged — the routing
  `2026-10-27-separate-lesson-capture-from-implementation` put in place when it replaced the inline capture.
- **Observed**: task 4.4, inherited from the archived analogue's task set, ran the `lesson-capture` subagent inside this
  unfinished unit. Its proposal was withheld from the branch and parked in `docs/ideas.md` (2026-11-01) rather than
  applied, so the guidance shipped unchanged.
- **Evidence**: task 4.4's own record (one durable proposal, one authoring-system recommendation, applied net delta 0)
  and the parked entry in `docs/ideas.md`; the workflow text quoted above; the analogue's task list carrying the step.
- **Consequence**: one extra subagent invocation and analysis pass whose product could not be applied in this unit, and
  a task that read as owed when the current workflow owes no analysis here. Nothing shipped wrongly: no guidance
  changed, and the proposal survives the archive in `docs/ideas.md`.
- **Knowledge boundary**: observed the run and its output. Did not check whether another artifact besides the analogue's
  task list carries the inline-capture step forward.

- **Decision** — proposed: applied as the task-floor reconciliation — an inherited task a current rule supersedes is
  dropped, not only unioned. The recurrence is the five archived 2026-11-01 task sets carrying the inherited
  `lesson-capture` step: four ran it (two applied durable proposals, one a change-level correction only, one parked its
  proposal) and one was rewritten to skip, so the step's outcome varied across units rather than being one occurrence.
  Its link is added in a follow-up commit once the pull request exists.
