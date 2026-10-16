# Proposal

## Why

A parked entry's removal rides the change that implements its idea, but nothing checks that it happened: the
actionlint-retry entry still read "parked; no change yet" two and a half weeks after #463 (`69d26b02`) implemented it,
and it was found only because `retry-verify-infra-image-versions-2026-10-16` read it as that change's own precedent. No
auditor owns `docs/ideas.md` — the four own `AGENTS.md`/`.opencode/`, the specs, `docs/adr/` plus the architectural
surface, and `README.md` — and the `upstream-references` report resolves only `owner/repo#NNN` references, so a stale
entry ages silently in the scratchpad the docs-refresh convention calls prunable. The `architecture-auditor` already
sweeps "deferrals and band-aids recorded in an ADR or parked in `docs/ideas.md`" and holds an ADR's `Revisit when:` gate
against the repository, so it is the coupled owner: this widening adds one sweep item beside the one it has, rather than
a fifth auditor.

## What Changes

- **`architecture-auditor`** (`.opencode/agent/architecture-auditor.md`) — its intent-clarification sweep gains the
  inverse-shaped item: a parked `docs/ideas.md` entry whose work has already landed, held against the repository (the
  change, commit, workflow or mechanism the entry describes) and reported as an advisory question where the entry's own
  work is concerned rather than an artifact it merely cites. Its frontmatter `description` names the added shape.
- **Its trigger** (`.opencode/commands/audit-architecture.md`) — the step-1 read-list and the command's `description`
  carry the added surface.
- **`AGENTS.md`** — the architecture-auditor bullet's swept-surface clause names it.
- **`README.md`** — the three places that say what the audit reports: the slash-command table row, the agent table row,
  and the prose paragraph.
- **`showcase/quality/agent-skills`** — the "Unexplained design intent is surfaced for clarification" requirement gains
  the parked-idea sweep and two scenarios (one per direction), so the audit's surface set is spec'd rather than only
  living in the definition.
- **`docs/ideas.md`** — the entry this change implements is removed.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the "Unexplained design intent is surfaced for clarification" requirement states the
  parked-idea sweep (a `docs/ideas.md` entry whose work has landed, reported as an advisory question) beside the ADR
  gate it already holds.

## Impact

- A future audit reports a stale parked entry instead of leaving it to be stumbled upon; the item is advisory, so a
  false positive is a question for the owner, not a defect to fix.
- **Build / tests / services**: no code, build, dependency or deployment change — a definition, a command, and prose.
- The verification is a **smoke-run** of the widened subagent with positive controls (a seeded stale entry it must
  report, a parked entry it must not), which needs the edited definition picked up — a reload or a new session — so the
  archive waits on it.
