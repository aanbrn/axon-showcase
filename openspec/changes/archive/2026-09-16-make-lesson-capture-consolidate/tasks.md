## 1. Record the delta

- [x] 1.1 Carry the requirement "Per-change quality-gate and analysis subagents are available" in the delta in full (its
      description and all five scenarios), extending only the lesson-capture scenario with the consolidation obligation
- [x] 1.2 Carry "Experience analysis is available to agents" in the delta in full as well (its description and all three
      scenarios), widening a process suggestion's destination to include a subagent definition
- [x] 1.3 Confirm both MODIFIED headers are verbatim-identical to the main spec's, and that no scenario either
      requirement still has is omitted

## 2. Implement

- [x] 2.1 State the consolidation obligation in `.opencode/agent/lesson-capture.md`'s output contract
- [x] 2.2 Add the matching clause to `AGENTS.md`'s capture bullet — both halves of the delta's obligation: every
      proposal names the bullet it extends (or that no bullet covers it), and a new rule merges into or replaces one
      rather than accreting
- [x] 2.3 Sweep the widened process destination across the four copies that carried the narrow form:
      `.opencode/agent/experience-analyzer.md`, `.opencode/commands/retrospective.md`, `AGENTS.md`'s own
      experience-analysis bullet, and `README.md`'s retrospective bullet (`:312-313`); and give `README.md`'s two
      `lesson-capture` descriptions — the agent-table row (`:231`) and the Lesson-capture bullet (`:243`) — the
      consolidation clause, or record the no-edit decision (its `:135` and `:299` mentions are the cadence, untouched)
- [x] 2.4 Add the routing rule to `AGENTS.md`'s subagent/command sweep bullet, qualified: a definition edit that changes
      spec'd behavior is a change, not a docs edit, and owes that spec's delta — never riding a docs, audit-fix, or
      retrospective PR — while a definition-only edit the spec does not describe (a model-pin bump) still ships as a
      change with no delta
- [x] 2.5 Correct `AGENTS.md`'s architecture-auditor bullet, whose "agent-tooling clarification lands as a docs PR"
      reads as sanctioning the route the new clause forbids
- [x] 2.6 Point the retrospective's suggestion table at this change for suggestion 2, and confirm the capability's
      `## Purpose` needs no refresh (this bounds a subagent's output shape and a suggestion's destination, not the
      capability's scope) — record the finding rather than leaving it implicit

## 3. Verify

- [x] 3.1 `openspec validate --all` passes; `./gradlew spotlessApply` and `spotlessCheck` are green
- [x] 3.2 `review-quick` is clean over the implementation, and a grep for the narrow destination (`process` →
      `AGENTS.md` alone) over the four swept artifacts returns nothing — scoped to them, since the main spec's own
      narrow copy is expected to survive until the archive commit and must never be edited to satisfy the grep
- [x] 3.3 Smoke-run the edited subagent as an archive precondition: reload OpenCode, then run `lesson-capture` over a
      seeded diff with one case an existing bullet covers (it must name that bullet) and one genuinely new (it must say
      no bullet covers it) — the precedent both analogous definition edits set (`concise-agent-reports`,
      `widen-agents-auditor-to-third-party`), and the only check that exercises the contract rather than greps its text
