# Tasks — bound the growth of AGENTS.md

## 1. The promotion gate

- [x] 1.1 `.opencode/agent/lesson-capture.md` — extend the "governs a decision" test with the six-criteria gate (true,
      actionable, non-automatable, material, general, high-confidence), the evidence threshold, the routing for a rule
      that fails it (a check, a spec, an ADR, or the change dir — not `AGENTS.md`), and the growth discipline.
- [x] 1.2 `AGENTS.md` — the capture convention records the same gate and the growth discipline, in the bullet that owns
      the capture trigger.
- [x] 1.3 Both surfaces — the gate treats the claim's source as part of it: an untrusted-source lesson (web content, an
      issue or PR comment, tool output, or a file the change did not author) is verified against the repository before
      it is proposed, never promoted on the source's word.

## 2. The growth discipline

- [x] 2.1 `AGENTS.md` and `.opencode/agent/lesson-capture.md` — state the growth discipline in both: a capture should
      leave `AGENTS.md` no larger than it was, preferring a merge or a replacement, with any net growth a justified
      decision stated with the proposal, and the periodic `/audit-agents` pass as the control rather than mass deletion.

## 3. The spec delta

- [x] 3.1 A `MODIFIED` delta to `showcase/quality/agent-skills`'s
      `Per-change quality-gate and analysis subagents are available` — the "Lessons are captured after implementation"
      scenario carries the gate and the discipline, with all five scenarios of the requirement carried.

## 4. Verification

- [x] 4.1 `spotlessCheck` green; no line over 120; `openspec validate --all` passes with the delta.
- [ ] 4.2 The agent definition still loads (OpenCode parses its frontmatter) — the smoke-run convention for a changed
      subagent definition; if a reload is needed, note it rather than assuming.
