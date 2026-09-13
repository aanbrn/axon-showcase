## Why

The repository records architecture decisions as ADRs (under `docs/adr/`) and embodies a design in its service
boundaries, module graph, and the spec corpus's role groups. No agent audits that artifact: the review agents are scoped
to a single change, `specs-auditor` owns the spec corpus's internal structure and explicitly not behavior-vs-code, and
`agents-auditor` owns the agent tooling. So an ADR whose Decision the code has outgrown, a `Status` that should read
Superseded, a cross-cutting choice made in code with no ADR, and boundary erosion all pass unnoticed.
`experience-analyzer` cannot see them either — it reads history, not the current system.

## What Changes

- Add an `architecture-auditor` subagent (`.opencode/agent/architecture-auditor.md`) pinned to
  `opencode-go/deepseek-v4-pro` that audits the architecture artifact — `docs/adr/` plus the architectural surface
  (service boundaries, the module dependency graph, and the spec corpus's capability decomposition) — and returns two
  clearly separated sections: **findings** (verified against the repository, actionable) and **advisory** (design
  judgment with no reference to diff against — no severity, not defects).
  - Findings cover, at least: an ADR's Decision contradicted by the code; a `Status` that is stale, or a supersession
    not recorded as `Superseded by ADR-NNNN`; a cited `ADR-NNNN` that does not exist; a cross-cutting structural choice
    made in code with no ADR; a dependency or service-boundary direction the architecture does not sanction; and a spec
    role-group decomposition that no longer matches the module/service structure.
  - Advisory covers cohesion/coupling, decomposition, and apparent gaps in the ADR set, surfaced for the user's judgment
    rather than reported as defects to fix.
  - The auditor proposes changes without applying them.
- Add an `/audit-architecture` command (`.opencode/commands/audit-architecture.md`) as the subagent's trigger.
- Explicitly out of scope: behavior-vs-code drift (the review loop and the archive-time spec sync own it), the spec
  corpus's internal structure (`specs-auditor` owns it), and any check an existing gate already enforces.
- Capture the new subagent in the `showcase/quality/agent-skills` spec.
- Record the convention in `AGENTS.md` (subagent section) and add the `architecture-auditor` row to the README agent
  table plus the `/audit-architecture` row to the slash-command table.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: adds a requirement that an `architecture-auditor` subagent audits the architecture
  artifact (`docs/adr/` plus the service boundaries, module dependency graph, and spec-corpus decomposition) for drift,
  separating verified findings from advisory design observations, verifying its findings against the repository, and
  reporting them without editing files.

## Impact

- `.opencode/agent/architecture-auditor.md` — new subagent definition.
- `.opencode/commands/audit-architecture.md` — new command trigger.
- `openspec/specs/showcase/quality/agent-skills/spec.md` — added requirement (delta spec in this change), plus a
  `## Purpose` refresh in the archive commit (a delta cannot carry a Purpose for an existing capability).
- `AGENTS.md` (subagent convention bullet) and `README.md` (agent-table row, slash-command-table row, and a sentence in
  the architecture paragraph surfacing the audit) — docs that are the change, shipped in this change's PR.
- `docs/ideas.md` — the scheduled (unattended) architecture audit is already parked by its own docs PR (#187). This
  change's branch additionally carries the change-caused reword of the parked README-auditor idea's auditor ordinal,
  which the fourth auditor this change adds falsifies.
