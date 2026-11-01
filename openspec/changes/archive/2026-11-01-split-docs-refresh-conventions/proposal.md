# Proposal

## Why

`AGENTS.md`'s "Docs refresh on change" convention is one 66-line bullet carrying seven obligation families and ten
`captured:` markers — the version-bump sweep, the change's own idea, the un-audited docs, the guidance-evidence records,
the causation boundary, the un-gated copies, and a file that depends on a surface outside the repository. A reader scans
all of it to reach the clause their change triggers, which the Structured Writing Standard's "one independently
actionable statement per unit" forbids; the 2026-10-31 dispatch audit run found it, and `docs/ideas.md` parked the
split. That report lives only on #781's branch, which cannot merge as it stands, and the merged
`docs/audits/2026-10-31.md` records a clean verdict without it.

## What Changes

- **`AGENTS.md`** — the convention keeps its bold lead (when to verify, the ADR cases, and "update them before reporting
  the change done") and splits its body into one labeled container per obligation family, each carrying one bullet per
  independently actionable obligation, with every clause's content carried and its `captured:` markers kept on the
  clause they evidence.
- **`docs/ideas.md`** — the 2026-11-01 entry is removed, since this change implements it. The one follow-up its
  implementation surfaced is parked in its place: recovering the stranded 2026-10-31 audit report.

No other artifact changes: the content stays at root, since it is a repository-wide rule that fires on every change.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: no capability spec states the docs-refresh obligations. Verified rather than assumed — a grep of `openspec/specs/`
for "docs refresh" and "docs-refresh" returns no match; the obligation lives in `AGENTS.md`, which
`showcase/quality/agent-skills` describes as guidance, not as a spec'd behavior.

## Impact

- **Every unit**: the convention's obligations are unchanged, but its shape is, so a future edit must place a new clause
  beside the obligation it belongs with, under the family's container.
- **No runtime, build or deployment path**: one guidance bullet restructured, and one parked idea replaced by the
  recovery its implementation surfaced.
