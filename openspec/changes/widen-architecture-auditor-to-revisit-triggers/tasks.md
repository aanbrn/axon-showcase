# Tasks

## 1. Record the condition

- [x] 1.1 Add a `Revisit when:` line to the ADR template block in `docs/adr/README.md` (after `Status:`), and a sentence
      in the "Writing a new ADR" list saying it is required for a deliberate deferral and omitted for a settled
      decision. Verify by reading the template block and the new prose.
- [x] 1.2 Add the `Revisit when:` line to the three deferral ADRs, each naming **its own** gate (not a generic shape):
      `0003-retain-jackson-2-defer-jackson-3.md` — "when the pinned Axon Framework and OpenSearch client resolve Jackson
      3 (a third-party-adoption gate, not a bump of `jackson2-bom`, a 2.x line)";
      `0004-defer-spring-boot-4-     migration.md` — "when there is capacity for the coordinated migration (an internal
      trigger, not repository- decidable)"; `0011-defer-axon-framework-5-migration.md` — "when `axon-bom` publishes a
      5.x release and the extensions/modules the project uses ship 5.x (an external coordinate gate)". Verify by reading
      each ADR's added line and `grep -rn "Revisit when:" docs/adr/` hitting the template and all three.

## 2. Widen the auditor

- [x] 2.1 In `.opencode/agent/architecture-auditor.md`, extend the intent-gap sweep (the "where clarification of intent
      is missing" list) with a deferred-ADR check: for each ADR carrying `Revisit when:`, hold its condition against the
      repository and report it in the advisory section when it appears met — an external coordinate gate by resolving
      the pin, a third-party-adoption gate by reading the consumers' dependency surface — naming the ADR, the condition,
      and the signal; a condition that names no repository fact (a capacity trigger) or a not-met condition is not
      reported. Also update the definition's frontmatter `description` (lines 2-7) so it names the deferred-condition
      sweep alongside "a deliberate choice whose rationale is not recorded", and keep the wording consistent with the
      existing sweep items (advisory, not a defect; verify the condition before reporting). Verify by re-reading the
      edited sweep, the frontmatter, and the advisory wording.
- [x] 2.2 Update `.opencode/commands/audit-architecture.md` so its frontmatter `description` and its read-list/step
      reflect the widened sweep (they enumerate what the audit reports / what it reads). Verify by reading the command
      file.
- [x] 2.3 Refresh `AGENTS.md`'s architecture-auditor bullet (its swept-surface enumeration and the "checks `Status`
      integrity" phrasing) and `README.md`'s prose/table row for the auditor to name the new check. Verify by reading
      the edited passages and `grep -n "Revisit when" AGENTS.md README.md` hitting the new descriptions.

## 3. Verification

- [x] 3.1 Smoke-run the widened `architecture-auditor` (via the `/audit-architecture` command or the subagent) with
      positive and negative controls: a deferral whose condition appears met (verify the seed's premise against the
      repository — the ADR and the coordinate really exist) must be reported in the advisory section with the ADR,
      condition, and signal; a deferral whose condition is not met (or a settled ADR with no `Revisit when:`) must not
      be reported. Record the run's output and which branch each control exercised. Note: a subagent definition edited
      this session is served from a cached copy until OpenCode reloads, so confirm the reload before trusting a run that
      echoes the new text.

      **Run recorded (reload confirmed — the run's report showed the new sweep by reading every `Revisit when:` line and
      enumerating the three deferrals).** Positive control: with ADR-0011's line temporarily seeded to assert its
      `axon-bom` 5.x conjunct already met, the auditor did **not** simply echo "met" — it resolved the coordinate,
      found `axon-bom`'s latest is 4.13.3 (only the core `axon-messaging` is 5.x), and refuted the seed; the seed was
      then restored. Negative control: it reported **none** of the three deferrals as due (ADR-0003's Axon half unmet,
      ADR-0004 non-decidable, ADR-0011's extension/module conjuncts unmet), and reported no settled ADR — the sweep's
      suppression holds. Branch exercised: the `Revisit when:` sweep ran and its negative control held; the positive
      branch is unexercised by a real met condition (none exists today), so the seed's own premise was what the run
      disproved. Beyond-seed finding parked in `docs/ideas.md` (the Elasticsearch-client role).
- [x] 3.2 Remove the implemented idea from `docs/ideas.md` (the 2026-09-13 "ADR revisit triggers for time-bounded
      decisions" entry). Verify `grep -n "ADR revisit triggers" docs/ideas.md` returns nothing.
- [x] 3.3 Run `./gradlew spotlessApply` after the last edit to a Spotless-owned file, then `./gradlew spotlessCheck` and
      `openspec validate --changes`, and confirm all pass.
- [x] 3.4 Run the per-unit `lesson-capture` subagent over the change and apply its durable proposals; record the applied
      net `AGENTS.md` delta on this task. Applied: one clause-level merge into the auto-review bullet — its sweep
      trigger now covers a **framing/coherence** correction, not only a behavior-adding finding (net +3 lines, the
      clause); the other candidate lessons (smoke-run seed premise; the control must fire) were already covered.
- [ ] 3.5 Record the `agent-skills` capability `## Purpose` refresh for the archive commit (a delta cannot carry a
      Purpose): the Purpose currently scopes the architecture audit to "where a deliberate decision's rationale is **not
      recorded**", which does **not** cover the new class (a _recorded_ condition whose premise may be met — the
      opposite subject), so **add** the deferred-decision subject to the Purpose rather than extending that clause.
      Verify the archived main spec's Purpose names it.
