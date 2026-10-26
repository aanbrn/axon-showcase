# Proposal

## Why

The state a resume needs is spread across a unit's `tasks.md`, the review-round ledger, and a reviewer's report, and
none of them says where the work stopped. The owner works from a carried laptop and often has to close it mid-unit, and
a unit is also often simply left waiting for the operator with no pause announced, so the next session reconstructs the
next step from a diff; the handovers the repository states serve a reviewer replacement and a review round, and neither
covers an unfinished unit.

## What Changes

- **`.opencode/skills/pause-resume-unit/SKILL.md`** — new project skill, the procedure's only owner: the resume
  checkpoint's field set and home, and the resume's ordered steps, decision points, failure handling, and completion
  criteria.
- **`.opencode/commands/pause-unit.md`** — the owner-facing `/pause-unit` trigger, which loads the skill's pause section
  and starts no unit.
- **`.opencode/commands/resume-unit.md`** — the `/resume-unit` trigger, which continues any unfinished unit from the
  session that left it or from a new one and loads the skill's resume section.
- **`AGENTS.md`** — the change lifecycle gains one entry-point bullet naming the skill, its path, when it loads, and the
  stop-and-report rule for a missing skill; the procedure's steps stay in the skill.
- **`README.md`** — the slash-command table gains both rows, and the development-workflow prose names pausing a unit and
  returning to one left unfinished.
- **`openspec/specs/showcase/quality/agent-skills/spec.md`** — via a delta: ADDED requirements for the procedure's home
  and root entry point, for the checkpoint a pause records, for the state verification a continued unfinished unit owes,
  and for the writer handover a session that is not the recorded writer establishes before it writes work in the tree.
- **`docs/ideas.md`** — no entry is removed or added; no parked idea covers this procedure.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: four ADDED requirements — the pause-and-resume procedure lives in its project skill
  with the root carrying only the entry point; a pause records a resume checkpoint and alters no delivery state; a
  continued unfinished unit verifies the state available to it, comparing it with the checkpoint only when one exists;
  and a session that is not the recorded writer establishes the handover before it writes work in the tree, stopping for
  the owner's decision when it cannot.

## Impact

- **Process and tooling**: one new project skill, two new command definitions, one `AGENTS.md` bullet, two README rows
  plus a prose sentence, and the `agent-skills` spec delta.
- **Gates**: `spotlessApply`/`spotlessCheck` format the new markdown, `openspec validate --all` checks the delta, and
  the `build` job's file-driven OpenCode probe covers the new definitions on this change's own pull request.
- **The capability's `## Purpose`**: it names the project-authored guidance skills by member, so it gains
  `pause-resume-unit` in the archive commit — a delta cannot carry a Purpose, so that edit is deferred and recorded as a
  task.
- **Code, builds, dependencies, deployments, tests**: unchanged; no new CLI, script, gate, or vendored file.
