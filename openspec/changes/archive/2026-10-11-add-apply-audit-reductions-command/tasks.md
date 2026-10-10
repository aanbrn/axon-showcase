# Tasks

## 1. The command

- [x] 1.1 Create `.opencode/commands/apply-audit-reductions.md`: an owner-gated unit that (a) selects the source report
      (the newest `docs/audits/*.md`, or one the caller names; a date carrying more than one report presents both for
      the owner to choose), (b) presents its merge/removal/route reduction candidates from the report and any parked
      entries, (c) applies only the owner-approved ones following each candidate's suggested edit — a merge's merged
      text or the duplicate it flags, a route's pointer at the named mechanism (referencing `AGENTS.md`'s capture-bullet
      trim rule, with a content-move routed through the change workflow), and a removal's deletion (recording what it
      loses and whether git preserves it) — (d) removes the applied parked entries from `docs/ideas.md`, (e) runs
      `./gradlew spotlessApply`, and (f) runs `review-quick` until clean before asking for the manual review. Verify by
      reading it back and by the PR's OpenCode probe (the CI loader guard asserts every command definition resolves with
      its metadata intact).

## 2. Docs sweep

- [x] 2.1 In `AGENTS.md`'s agents-auditor bullet, name the unit and its per-report cadence (a report's reduction
      candidates are drained by `/apply-audit-reductions`, not left to age), pointing at the `agent-skills` spec for the
      behavior. Verify by reading the bullet back.
- [x] 2.2 Add the `/apply-audit-reductions` row to `README.md`'s slash-command table and update the auditor prose that
      describes what happens to a report's findings. Verify by grep for the row and by reading the prose.
- [x] 2.3 In `docs/ideas.md`, remove the implemented S3 entry ("Give parked reduction candidates an application slot");
      leave the "Carry findings forward across audit reports" idea parked (this change drains candidates, it does not
      make the report name unapplied items). Verify with `grep -n "application slot"` (absent) and by reading the
      carry-forward entry.
- [x] 2.4 Record the `agent-skills` `## Purpose` refresh as its own step: a delta cannot carry a Purpose, so add the new
      unit to the Purpose in the archive commit (the Purpose enumerates the capability's parts). Verify the Purpose
      names the unit after archive.

## 3. Verification

- [x] 3.1 Run `openspec validate --all` and confirm the change and the corpus validate.
- [x] 3.2 After the final edit, run `./gradlew spotlessApply` then `./gradlew spotlessCheck`, and confirm the touched
      markdown is formatter-clean.
- [x] 3.3 Run a `review-quick` pass over the proposal and the implementation, repeating until clean; confirm the command
      references the trim discipline rather than restating it, and the spec/Purpose/AGENTS.md/README all name the unit
      consistently.
- [x] 3.4 Confirm the new command is reachable: the frontmatter parses, and both `AGENTS.md` and the README name it (the
      repo's command surface has no other inventory).

## 4. Capture

- [x] 4.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. **Outcome:** one durable proposal, applied — an in-place widening of the
      auto-review bullet's framing/coherence sentence, so the sweep covers the shipped copies (a command/definition, the
      README) and any claim a single source owns, not only the planning artifacts (the six-round `contradiction` loop
      was a wording gap where the rule said "planning"). The capture's applied net `AGENTS.md` delta is **+2 lines**;
      the branch's whole `AGENTS.md` delta is **+4 lines** (13 insertions − 9 deletions), the remainder being the unit's
      own agents-auditor-bullet sentence.

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied, folding the delta into
  `openspec/specs/showcase/quality/agent-skills/spec.md` and refreshing its `## Purpose` (task 2.4).
- Verify the archived result.
