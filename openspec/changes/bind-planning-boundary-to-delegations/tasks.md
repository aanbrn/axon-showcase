# Tasks

## 1. State the planning-stage delegation boundary

- [x] 1.1 Add the paragraph to `AGENTS.md`'s OpenSpec Workflow Agreement: a delegation made while a change is explored
      or planned (before implementation) is read-only against the project's source and configuration — the delegate may
      write only the change's own artifacts under `openspec/changes/<change>/`, its prompt names the paths it may touch,
      and the calling agent verifies it stayed inside them before using its output. Verify by reading the section and
      confirming the paragraph is present and the section's existing paragraphs are intact.
- [x] 1.2 Confirm no spec delta and no config rule are owed: grep `openspec/specs/` for a capability covering
      planning-stage delegation discipline and judge every hit — the `delegat` matches delegate content to a target spec
      or ADR (`agent-skills`), visual review to the `vision` subagent (`agent-skills`), and presence validation to
      complementary constraints (`identifier-extension`), none of which is a caller/delegate read-only boundary — and
      confirm `AGENTS.md` is always loaded, so a config rule would be a redundant second copy. Record the decision in
      the change's report.

## 2. Docs the change owns

- [x] 2.1 Update `docs/retrospectives/2026-09-28.md`'s S2 disposition cell to name this change, as
      `make-the-capture-visible` did for S1.
- [x] 2.2 Verify `README.md`, `docs/adr/`, and `docs/ideas.md` need no edit for this change (the docs-refresh
      convention's per-change check) and record the outcome; S2 lives in the retrospective, not `docs/ideas.md`, so no
      idea removal is owed.

## 3. Verification

- [x] 3.1 Run the implementation `review-quick` loop over the diff against this change's planning artifacts; fix its
      findings and re-run until it reports nothing new.
- [x] 3.2 Run the per-unit `lesson-capture` over this change and apply its durable proposals (dogfooding the record
      `make-the-capture-visible` added).
- [x] 3.3 Run `./gradlew spotlessApply` after the last edit to a Spotless-owned file and confirm `spotlessCheck` passes;
      run the manual 120-character check over the change dir's `.openspec.yaml` (YAML has no formatter).
- [x] 3.4 Request the user's manual review pass — the step before committing.
