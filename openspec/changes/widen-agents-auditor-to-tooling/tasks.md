## 1. Audit scope

- [x] 1.1 Widen `.opencode/agent/agents-auditor.md` to audit `AGENTS.md` plus the project-authored `.opencode/` files,
      with the provenance partition and skip rule (generated files from `openspec update`, vendored `axon4to5-*`; a
      project-authored same-prefix file like `opsx-tool-update` stays in scope), keeping the two axes, the
      deliberate-choice caveat, and the verify/report-don't-edit contract — and update the frontmatter `description`
- [x] 1.2 Update `.opencode/commands/audit-agents.md` (description + body) to describe the widened scope

## 2. Spec capture

- [x] 2.1 Replace the `AGENTS.md is audited for consistency and conciseness` requirement with the widened one in the
      `showcase/quality/agent-skills` delta spec (REMOVED + ADDED, carrying every existing scenario)
- [ ] 2.2 Refresh the `showcase/quality/agent-skills` `## Purpose` in the archive commit — its "on-demand auditors of
      `AGENTS.md` and the `openspec/specs/` corpus" line goes stale (a delta cannot carry a Purpose)

## 3. Docs

- [x] 3.1 Update the `AGENTS.md` subagent-convention bullet for the widened scope
- [x] 3.2 Update the README agent-table row, the `/audit-agents` slash-command row, and the Self-Learning Loop bullet
      for the widened scope

## 4. Verification

- [x] 4.1 Run `openspec validate --all` and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (the PR gate)
- [x] 4.2 Confirm the subagent and command are present with the widened scope and the skip rule
- [x] 4.3 Smoke-run `/audit-agents` over the project-owned `.opencode/` files and review the findings (after an OpenCode
      restart — a subagent created mid-session is not registered until the agent list reloads; the already-registered
      subagent ran without a restart and immediately found a real self-contradiction — `review-quick.md` instructs the
      exact `awk` 120-character check `AGENTS.md` forbids)
