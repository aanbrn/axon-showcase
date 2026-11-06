- [coherence] openspec/changes/assert-paketo-pin-coverage/design.md:73 — four sites called the new input the catalog's
  Paketo key set, while the build passes every version alias and the rule selects the prefix.
- [claim-unreproducible] openspec/changes/assert-paketo-pin-coverage/design.md:105-106 — the rename step named the
  unknown-entry failure, where a rename reports the uncovered key first.
- [test-control] build-logic/src/test/kotlin/PaketoPinCoverageRulesTests.kt — no arm exercised the rules' `sorted`
  contract, so deleting `.sorted()` stayed green.
- [wording] .opencode/skills/gradle-build/SKILL.md:39-41 — the inserted coverage clause displaced the qualifier that
  belonged to `buildLogicUpdates`.
- [docs-residue] docs/units/assert-paketo-pin-coverage/unit.md:23 — the proposal-approval pending decision survived the
  finished-unit line and the recorded approval.
- [coherence] build-logic/src/test/kotlin/PaketoPinCoverageRulesTests.kt:1 — the new class used `kotlin.test` without
  display names where the module's other nineteen classes use JUnit 5, AssertJ, and `@DisplayName`.
