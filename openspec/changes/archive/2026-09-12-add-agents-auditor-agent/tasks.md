## 1. Audit subagent and command

- [x] 1.1 Add `.opencode/agent/agents-auditor.md` pinned to `opencode-go/deepseek-v4-pro` (`mode: subagent`,
      temperature 0) with the two-axis contract (consistency, conciseness), the verify-against-the-repository rule, the
      report-don't-edit rule, and the deliberate-asymmetry caveat
- [x] 1.2 Add `.opencode/commands/audit-agents.md` that triggers the subagent and asks which findings to apply

## 2. Spec capture

- [x] 2.1 Add the `agents-auditor` requirement (with its scenarios) to the `showcase/quality/agent-skills` delta spec
- [ ] 2.2 Refresh the capability's `## Purpose` in the archive commit to cover the subagent requirements it now holds —
      the audit subagent, plus the already-stale `experience-analyzer` and per-change entries (a delta cannot carry a
      Purpose for an existing capability)

## 3. Docs

- [x] 3.1 Add an `agents-auditor` / `/audit-agents` convention bullet to `AGENTS.md` (subagent conventions section)
- [x] 3.2 Add the `agents-auditor` row to the README agent table and the `/audit-agents` row to the slash-command table
- [x] 3.3 Add the AGENTS.md-audit bullet to the README's "The Self-Learning Loop" section, framing the audit as the
      memory's maintenance alongside its accretion

## 4. Verification

- [x] 4.1 Run `openspec validate --all` and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (the PR gate — the
      0.80 coverage baseline is calibrated on integration-test coverage, which `-PskipITs` skips by design, so the gate
      must be disabled for a Docker-free run)
- [x] 4.2 Confirm the subagent is discoverable (`.opencode/agent/agents-auditor.md` present with `mode: subagent` and
      the pro model) and the command is present (`.opencode/commands/audit-agents.md`)
- [x] 4.3 Smoke-run `/audit-agents` against the current `AGENTS.md` and review the findings (run after a session restart
      — a subagent created mid-session is not registered until OpenCode reloads its agent list; it returned real,
      verified findings, e.g. the stale `#`-header-drift example and the architecture heading's "4 services + a gateway"
      double-count)
