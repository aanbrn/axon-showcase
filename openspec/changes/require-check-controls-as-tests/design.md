# Design

## Context

See `proposal.md` — Why. The config rule requires the control as a task; the repo's tests are the pattern that works.
The figures this change rests on are in the table below.

## Claims table

Each row is a fact this change states about its evidence — the prior artifacts it rests on — with the source that
derives it:

| Fact                                                                                   | Stated in   | Source (re-run)                                                                                                                                                                                   |
| -------------------------------------------------------------------------------------- | ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| the config rule requires the control as a task                                         | `design.md` | `git show HEAD:openspec/config.yaml                                                                                                                                                               | grep -n 'proving the check fails'` |
| `verify-documented-pins` ran 13 rounds, raising `test-control` in 6                    | `design.md` | `awk -F'\t' '$2 == "verify-documented-pins" {n++; if ($4 ~ /test-control/) t++} END {print n, t}' docs/review-rounds.tsv`                                                                         |
| every rules-object-based `verify*` gate already carries a `*RulesTests`                | `design.md` | `ls build-logic/src/main/kotlin/*Rules.kt` against `ls build-logic/src/test/kotlin/*RulesTests.kt` (9 each; the Exec-based commit-hygiene gates carry theirs in `scripts/test-commit-hygiene.py`) |
| `build-logic`'s effect is verified through a consuming module, not a TestKit unit test | `design.md` | `grep -n "verified through a consuming module" AGENTS.md`                                                                                                                                         |

The filter excludes this unit's own rows: the corpus is live, so an unfiltered count would move every time this unit's
review appends a row.

## Goals / Non-Goals

**Goals:**

- Make a gate's known-bad control a committed test, red in the author's own run rather than a ticked box.
- Name the shape so a gate's control is discoverable where its logic lives.

**Non-Goals:**

- Replacing a gate's own logic or its consuming-module verification: the split follows the existing build-logic rule.
- Requiring a control where the gate is infrastructure-bound and a unit test cannot reach it: that control stays a
  consuming-module test.

## Decisions

**The control is a committed test at the rules level, where the logic lives.** `build-logic`'s own rule is that a
convention plugin's effect is verified through a consuming module, not a TestKit unit test — so the check's _decision_
(a known-bad input fails, a known-good passes) is what a committed `*RulesTests` case asserts, and the task's wiring is
verified by the consuming module. That is the existing `DashboardJsonRules`/`InstallCommandRules` shape made required.

**The rule names the test, not the task.** A task can be ticked without the control running; the test cannot — it is red
in the author's own run. The `*RulesTests` fixture is named so the shape is discoverable.

## Risks / Trade-offs

- A rule whose check has no separate rules object (a `verify*` task doing its work inline) → the convention names the
  rules-object shape; a check without one carries its fixture in the task's own consuming-module test.
- A control that needs infrastructure (Docker) cannot be a `build-logic` unit test → such a check's control stays a
  consuming-module test, named as its shape is.
