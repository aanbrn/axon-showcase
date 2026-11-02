# Process defects — verify-workspace-record

### verify-workspace-record#1 — the new guard clause refused seven pre-existing fixtures

- **Recorded** — 2026-11-02, by the unit's agent.
- **Stage** — implementation.
- **Expected** — the new `--staged` clause refuses a commit whose staged change `tasks.md` carries no workspace record.
- **Observed** — after the clause was wired into `check_staged`, seven tests that predate this unit failed, because
  their fixtures stage a `tasks.md` (live or archived) written as a placeholder with no record: four in
  `ArchivedTasksGuardTests`, three in `CommitAssignmentTests`.
- **Evidence** — `python3 scripts/test-commit-hygiene.py` printed seven failures, the first reading
  `The workspace record does not verify (openspec/changes/archive/2026-10-01-unit-a/tasks.md): no Workspace: record`.
- **Consequence** — the suite was red until the fixtures carried a record naming the fixture tree and branch (a shared
  `workspace_record` helper, and a `_tasks` writer in `CommitAssignmentTests`); no shipped code was wrong and no commit
  was made while it was red.
- **Knowledge boundary** — observed which fixtures failed and why; did not survey the other guard tests for fixtures
  that stage a `tasks.md` through a path the helper does not cover.
