- [docs-residue] `AGENTS.md:845` — the ship section's Ordering bullet still required an archive unconditionally.
- [test-control] `build-logic/src/main/kotlin/VerifyCommandLifecycleTask.kt:28` — the task cached a verdict whose
  owner-resolution predicate reads files it does not declare as inputs.
- [incomplete-enumeration] `build-logic/src/main/kotlin/CommandLifecycleRules.kt:29` — four preflight anchors stood in
  for the eight the requirement's preflight names, and the claims around them overstated the check.
- [dropped-content] `.opencode/skills/begin-unit/SKILL.md:24` — the retired command's no-`.idea/` readiness branch was
  lost.
- [single-sourcing] `build-logic/src/main/kotlin/CommandLifecycleRules.kt:14` — the stage, invocation, and effect
  vocabularies were hardcoded against their owners.
- [coherence] `AGENTS.md:316` — the stage table sent a named-direct-edit unit to Implement while the Plan entry said it
  starts at Implementation review.
- [wording] `AGENTS.md:342` — "a stage's procedure belongs to the skill named here" contradicted the Plan and Implement
  entries this file owns.
- [missing-task] `openspec/changes/make-unit-lifecycle-explicit/tasks.md:87` — task 6.4's composed routes were not
  exercised.
- [missing-task] `openspec/changes/make-unit-lifecycle-explicit/tasks.md:14` — task 1.3 was complete but unticked.
- [wording] `docs/adr/0030-the-unit-lifecycle-is-the-enclosing-process.md:40` — "closed marks entry into delivery"
  misdated the write, which is the pre-merge commit.
