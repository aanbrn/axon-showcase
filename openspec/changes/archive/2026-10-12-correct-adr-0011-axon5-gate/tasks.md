# Tasks

## 1. ADR-0011

- [x] 1.1 Rewrite `Revisit when:` (`docs/adr/0011-defer-axon-framework-5-migration.md:7-9`) to name the current
      coordinates — the renamed `org.axonframework:axon-framework-bom` and the `extensions.*` groups — and to state the
      AF5 command-routing decision (the JGroups distributed command bus moved to AxonIQ's commercial offering) alongside
      the Kafka wait. Verify the Kafka clause names a signal a coordinate check can read, and the routing clause names
      its owner-surfaced trigger.
- [x] 1.2 Correct the `Context`: replace the "no 5.x release"/"also 4.x-only"/"preview only" claims with the measured
      landscape, record the 2026-10-12 re-check and its two AxonIQ sources, and correct the "sibling of ADR-0009 …
      independent of whether Axon Server is used" paragraph to state the coupling AF5 introduces.
- [x] 1.3 Correct the `## Decision`'s reopen paragraph (`reopens when the extensions … publish 5.x support`) so the
      JGroups clause reads as a decision rather than an upstream wait, and the `## Related decisions` sentence
      classifying this ADR as "blocked on an external condition", which now carries a decision too.
- [x] 1.4 Read the whole ADR back and confirm no stale claim survives (grep it for the gate's own wording — `4.x-only`,
      `preview`, `no 5.x release`, `reopens when`, `revisiting the direction`, `blocked on an external condition`).

## 2. Spec delta

- [x] 2.1 Write `openspec/changes/correct-adr-0011-axon5-gate/specs/showcase/quality/dependency-management/spec.md` as a
      `MODIFIED` block for "Axon Framework major updates are suppressed until the framework's 5.x dependency surface
      exists", copying the full requirement (description + all three scenarios, by name and order) and rewriting only
      the body. Verify with `openspec validate correct-adr-0011-axon5-gate`.

## 3. Config comment

- [x] 3.1 Correct the `org.axonframework` comment in `config/dependency-updates/major-disabled.txt:4` so it no longer
      says "the extensions/modules used have no 5.x"; keep it naming ADR-0011. Verify by reading it back.

## 4. Verification

- [x] 4.1 Run `./gradlew spotlessApply` then `./gradlew spotlessCheck`, the manual 120-character check over the changed
      files, and `openspec validate --all`; confirm clean.
- [x] 4.2 Run a `review-quick` pass over the proposal and the implementation, repeating until clean.

## 5. Capture

- [x] 5.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. Applied: **net +4 lines** (5 inserted, 1 reflowed) — one clause on the
      Architecture Decision Records bullet (a deferral's gate names each clause's artifact at its _current_ coordinate
      and labels the clause's type; a compound gate is a conjunction, so a check names the clauses still open). It
      routed one item to `docs/ideas.md` (the `architecture-auditor`'s deferral check encodes the literal/single-clause
      read, needing its own `agent-skills` delta). No retirements proposed.
- [x] 5.2 Ask the owner for the manual review pass, after 4.2 is clean and 5.1 is done.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit and the main-spec sync ride this PR.
