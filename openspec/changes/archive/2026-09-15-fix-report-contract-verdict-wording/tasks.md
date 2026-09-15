## 1. Reword the verdict scenario

- [x] 1.1 Add the delta spec for `showcase/quality/agent-skills` carrying the report-contract requirement in full (its
      description and all four scenarios) with only the verdict scenario's `THEN` bullet reworded
- [x] 1.2 Confirm the MODIFIED header is verbatim-identical to the main spec's report-contract requirement header, and
      that no scenario the main spec still has is omitted (all four carried)

## 2. Verify

- [x] 2.1 Reword `AGENTS.md`'s own report-contract mention (the multi-artifact-sweep bullet) to match the count verdict
      — the same stale framing the delta fixes — and confirm no agent definition or command edit is needed, since every
      definition already opens with a count
- [x] 2.2 Confirm the capability's `## Purpose` needs no refresh (this narrows one scenario's wording, not the
      capability's scope), and record the finding rather than leaving it implicit
- [x] 2.3 `openspec validate --all` passes; `./gradlew spotlessApply` and `spotlessCheck` are green for the change dir
