## 1. Audit subagent and command

- [x] 1.1 Add `.opencode/agent/architecture-auditor.md` pinned to `opencode-go/deepseek-v4-pro` (`mode: subagent`,
      temperature 0) with the two-section contract (verified findings, advisory observations), the drift classes (ADR
      Decision vs code, `Status` integrity and unrecorded supersession, missing `ADR-NNNN` cross-references, unrecorded
      cross-cutting decisions, dependency/service-boundary direction, spec-decomposition fit), the
      verify-against-the-repository rule, the report-don't-edit rule, and the explicit out-of-scope list
      (behavior-vs-code, spec-internal structure, already-enforced gates)
- [x] 1.2 Add `.opencode/commands/audit-architecture.md` that triggers the subagent and asks which findings to apply

## 2. Spec capture

- [x] 2.1 Add the `architecture-auditor` requirement (with its scenarios) to the `showcase/quality/agent-skills` delta
      spec
- [x] 2.2 Refresh the capability's `## Purpose` in the archive commit to cover the third auditor — a delta cannot carry
      a Purpose for an existing capability

## 3. Docs

- [x] 3.1 Add an `architecture-auditor` / `/audit-architecture` convention bullet to `AGENTS.md` (subagent conventions
      section)
- [x] 3.2 Add the `architecture-auditor` row to the README agent table and the `/audit-architecture` row to the
      slash-command table
- [x] 3.3 Add an `architecture-auditor` sentence to the README's ADR/architecture paragraph, so a human reader meets the
      audit alongside the ADR convention that motivates it
- [x] 3.4 Reword the parked README-auditor idea's "third auditor" ordinal in `docs/ideas.md` (the fourth auditor this
      change adds falsifies it) — committed on this branch as the change-caused `docs/ideas.md` edit

## 4. Verification

- [x] 4.1 Run `openspec validate --all` and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (the PR gate — the
      0.80 coverage baseline is calibrated on integration-test coverage, which `-PskipITs` skips by design, so the gate
      must be disabled for a Docker-free run)
- [x] 4.2 Confirm the subagent is discoverable (`.opencode/agent/architecture-auditor.md` present with `mode: subagent`
      and the pro model) and the command is present (`.opencode/commands/audit-architecture.md`)
- [ ] 4.3 Smoke-run `/audit-architecture` against the current repository and review the findings — run after a session
      restart, since a subagent added mid-session is not registered until OpenCode reloads its agent list
