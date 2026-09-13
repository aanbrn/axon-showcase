## Why

The `architecture-auditor` verifies that the repository matches its **recorded** decisions, but nothing checks that a
decision's **rationale** is recorded at all. A deliberate choice whose _why_ lives only in the owner's head reads as a
recorded decision and survives every audit — the no-Axon-Server rationale did, until the owner supplied it in
conversation. Widening the auditor's advisory output to report where clarification of intent is missing makes that gap
systematic instead of accidental.

## What Changes

- The `architecture-auditor` subagent's **advisory** output gains a class of item: a deliberate choice or deliberate
  absence whose rationale is not recorded, reported as a question for the owner.
- The auditor sweeps four named surfaces where a deliberate decision implies a rejected alternative, so a rationale must
  exist somewhere: dependency `exclude(...)` declarations; major-version-suppressed coordinates
  (`config/dependency-updates/major-disabled.properties`); suppression annotations that encode a design choice
  (`@SuppressWarnings`) and deprecated-API usages the project still carries; and deferrals or band-aids recorded in an
  ADR or parked in `docs/ideas.md`.
- The output contract is stated: the auditor enumerates candidates and poses the question whose answer would record the
  rationale; only the project owner can say which matter, so the result is a candidate list for their decision, never a
  defect list to fix.
- The items stay **advisory** (not findings), so the existing "never fixed without the user's decision" rule applies
  unchanged.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: adds a requirement that the architecture audit reports where design intent is
  unexplained — the intent-clarification advisory class and the surfaces it sweeps.

## Impact

- `.opencode/agent/architecture-auditor.md` — the widened advisory section, method, and frontmatter `description`.
- `.opencode/commands/audit-architecture.md` and `.opencode/commands/audit-agents.md` — the first names the
  intent-clarification surfaces in its read-list; both carry a corrected 120-character-check scope.
- `AGENTS.md` and `README.md` — the architecture-auditor descriptions name the new advisory class.
- `docs/ideas.md` — the candidate list this sweep surfaced is parked so it is not lost (its own docs PR).
- `openspec/specs/showcase/quality/agent-skills/spec.md` — purpose refreshed and requirement synced at archive.
- No code, no runtime behavior, no dependency change.
