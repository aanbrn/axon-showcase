- [test-control] `build-logic/src/main/kotlin/CommandLifecycleRules.kt:107` — the vocabulary-presence guard read the
  whole of `AGENTS.md` rather than the lifecycle section.
- [test-control] `build-logic/src/test/kotlin/CommandLifecycleRulesTests.kt` — the vocabulary-presence predicate and the
  stage-table derivation had no asserted known-bad case, and the hand-over's case count did not reproduce.
- [missing-task] `openspec/changes/make-unit-lifecycle-explicit/tasks.md:82` — the composed routes remain unexercised;
  the task is open and the owner's acceptance or a later run is owed.
