## 1. Audit subagent and command

- [x] 1.1 Add `.opencode/agent/specs-auditor.md` pinned to `opencode-go/deepseek-v4-pro` (`mode: subagent`,
      temperature 0) with the structure/consistency contract, the verify-against-the-repository rule, the judgment-aware
      duplicate check, the behavior-vs-code exclusion, and the report-don't-edit rule
- [x] 1.2 Add `.opencode/commands/audit-specs.md` that triggers the subagent and asks which findings to apply

## 2. Spec capture

- [x] 2.1 Add the `specs-auditor` requirement (with its scenarios) to the `showcase/quality/agent-skills` delta spec
- [ ] 2.2 Refresh the capability's `## Purpose` in the archive commit to mention the specs auditor (a delta cannot carry
      a Purpose for an existing capability)

## 3. Docs

- [x] 3.1 Add a `specs-auditor` / `/audit-specs` convention bullet to `AGENTS.md` (subagent conventions section)
- [x] 3.2 Add the `specs-auditor` row to the README agent table and the `/audit-specs` row to the slash-command table
- [x] 3.3 Add the spec-audit sentence to the README's Spec-Driven Development section (the corpus's maintenance)

## 4. Verification

- [x] 4.1 Run `openspec validate --all` and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (the PR gate)
- [x] 4.2 Confirm the subagent is discoverable (`.opencode/agent/specs-auditor.md` present with `mode: subagent` and the
      pro model) and the command is present (`.opencode/commands/audit-specs.md`)
- [x] 4.3 Smoke-run `/audit-specs` and review the findings (run after a session restart — a subagent created mid-session
      is not registered until the agent list reloads; it returned real, verified findings — two stale `#` titles, the
      helm-chart Purpose's "four services", the agent-skills enumeration outgrown by `specs-auditor`, and it correctly
      classified the reused client/extension requirement headers as legitimate rather than duplicates)
