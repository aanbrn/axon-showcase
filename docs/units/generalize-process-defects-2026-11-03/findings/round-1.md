- [claim-unreproducible] docs/units/generalize-process-defects-2026-11-03/review-handover.md:27 — the hand-over claims
  `spotlessApply`/`spotlessCheck` ran after the last edit, but `./gradlew spotlessMarkdownCheck` fails on that file,
  which was written afterwards.
- [over-claim] .opencode/skills/review-gate/SKILL.md:169-171 — the bullet says the stale index costs the review "instead
  of screening the work"; the record shows the round screened the working tree and reported the stale index as an
  additional limitation.
- [missing-task] .opencode/skills/check-and-test-authoring/SKILL.md:97-102 — the new mode carries its action and marker
  but neither the confirmation that established it nor its incident evidence, which the `agent-skills` requirement makes
  a mode's required parts.
- [incomplete-enumeration] openspec/config.yaml:122-125 — the item's stage list names three stages and omits the routed
  deep review, which `review-gate` §4 owns.
- [claim-unreproducible] docs/units/generalize-process-defects-2026-11-03/unit.md:25 — "Two more records are parked
  against the wrapper entry for their ledger-row half" names no records, and only one of the 44 carries a parked
  ledger-row half.
- [claim-missing] docs/units/generalize-process-defects-2026-11-03/unit.md:13 — "44 records across 22 units" states a
  unit count the claims table's first row and the count dismissal both leave underived.
- [formatting] docs/units/generalize-process-defects-2026-11-03/process-defects.md:49 — the quoted line nests a backtick
  inside a backtick span, so the span closes early and `-z` renders as prose.
