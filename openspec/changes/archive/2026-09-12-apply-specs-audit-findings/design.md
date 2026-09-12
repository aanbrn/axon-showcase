## Context

`specs-auditor` (added in `add-specs-auditor-agent`) audits `openspec/specs/` for structural consistency — title ↔
capability-path match, Purpose ↔ requirements fit, requirement conventions, cross-spec duplication, dead
cross-references — because `openspec validate` gates a spec's well-formedness but not the corpus's mutual consistency.
Its first run reported seven verified findings. This change applies them.

## Goals / Non-Goals

**Goals:**

- Apply the audit's verified findings as spec-text edits, routed through the change workflow (a spec edit is a change).
- Fix the two stale `#` titles the way `fix-stale-spec-headers` (#105) did — a direct first-line edit, since a title is
  not a requirement and no delta block covers it.

**Non-Goals:**

- Re-running the audit or widening its scope — this applies the findings already in hand.
- Behavior change. Every edit is spec text; where a requirement is added (`Time limiter` for the command client) it
  formalizes behavior the code and the Purpose already have.
- The `dependency-management`/`code-quality` spacing and `MUST` fixes are cosmetic/convention; they are included because
  they are part of the same audit report, not because they are urgent.

## Decisions

- **One change for all seven findings.** They are homogeneous (spec-text corrections from one audit report), small, and
  reviewed together; splitting into seven changes would be process overhead with no benefit. Each finding is a separate
  task for reviewability.
- **Title fixes are title-only, no delta.** Precedent: `fix-stale-spec-headers` (#105) was a `skip_specs` change that
  edited the first lines directly. This change is not `skip_specs` (it carries real deltas), so the title edits ride the
  same change as ordinary main-spec edits recorded in tasks — the delta mechanism does not apply to a `#` title.
- **`agent-skills`: drop the enumeration, don't extend it.** The
  `Per-change quality-gate and analysis subagents are available` requirement lists a fixed subagent set that a later
  change outgrew (it omits `experience-analyzer` and the two auditors, which have their own requirements). Adding the
  missing names would create a fourth list to keep in sync; instead the requirement describes the per-change set
  _without naming a closed list_, pointing at its scenarios and this capability's own requirements, so it stops
  enumerating a set that grows. (This is a `MODIFIED` requirement, so the delta carries every existing scenario verbatim
  — the scenarios still name their own agents, which is inherent to what each verifies.)
- **`command-client`: add the `Time limiter` requirement.** The Purpose and the code (`@TimeLimiter` on
  `ShowcaseCommandClient`) both have it; the spec does not. Mirroring the query-client's `Time limiter` requirement
  keeps the two client specs symmetric. This is the one edit that adds a requirement rather than correcting text —
  called out because it formalizes existing behavior, it does not introduce it.
- **`helm-chart` is a Purpose refresh, not a delta.** Its `Service deployments` requirement already names five services;
  only the Purpose is stale ("four"). A delta cannot carry a `## Purpose` for an existing capability, so it is refreshed
  in the archive commit (task 3.2), not as a delta block.

## Risks / Trade-offs

- **A `MODIFIED` requirement must carry every scenario the main spec still has** (`openspec validate --changes` rejects
  a delta that drops one). Each modified requirement's delta is copied from the current main spec and then edited, never
  hand-written from memory.
- **The `Time limiter` addition could be seen as behavior scope.** It is not: the client already enforces a time
  limiter; the requirement documents it. Verifying it against the code is part of the task.
- **Title edits are un-gated** — nothing validates a `#` title against its path, which is why the drift exists; the fix
  is manual and the audit is what catches a recurrence.
