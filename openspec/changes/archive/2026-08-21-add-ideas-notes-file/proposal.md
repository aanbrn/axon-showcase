# Proposal: Add an ideas notes file and /ideas command

## Why

There is no lightweight place to capture emerging development ideas, so they are easy to forget. A single notes file
with dated bullets, plus a small `/ideas` command to append entries, gives a zero-friction scratchpad that is
versioned with the code. An idea becomes an OpenSpec change only when acted on — the file is explicitly not a backlog
of planned work.

## What Changes

- `docs/ideas.md`: new file with a short intro and dated bullet sections; the initial entry documents the
  Spring Data calendar-versioning gap in the `majorOf()` heuristic surfaced during exploration.
- `.opencode/commands/ideas.md`: new command that appends a dated bullet to `docs/ideas.md` from the given text, and
  (when called without a note) prints the current contents for review.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. No spec-level requirement changes; this is documentation and tooling. `skip_specs: true`.

## Impact

- **Code**: `docs/ideas.md` (new), `.opencode/commands/ideas.md` (new).
- **Docs**: AGENTS.md / README.md do not enumerate the ideas file; no change required.
- **Build**: no build impact.
- **Tests**: no test changes.