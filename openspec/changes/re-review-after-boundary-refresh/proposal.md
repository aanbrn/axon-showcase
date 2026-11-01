# Proposal

## Why

The review gate's §5 tells the agent to refresh the unit's branch, re-stage and re-run its readiness check before
requesting the manual pass — but not to re-run the quick round, whose clean closing round the refresh may have
invalidated. `retry-infra-verification-fetches#1` records exactly that sequence: a closing clean round, a `/sync-branch`
with two conflicts resolved by hand, and a manual-pass request before any round saw the resolved tree. `/sync-branch`'s
lifecycle already requires the full gate over the result; §5 is the silent boundary the agent follows when asking.

## What Changes

- **`.opencode/skills/review-gate/SKILL.md`** — §5's readiness bullet gains the return branch: a refresh that changed a
  file the closing round reviewed re-opens the gate, so its stages run over the result before the ask — §1's claims
  table re-derived when a planning artifact changed, then the quick loop, then the routing decision reassessed on the
  changed tree and any deep review §3 and §4 own — and this step is re-run after it. The branch points at
  `/sync-branch`'s lifecycle line rather than restating the sync procedure.
- **`openspec/changes/re-review-after-boundary-refresh/specs/showcase/quality/agent-skills/spec.md`** — a `MODIFIED`
  requirement: "The quick review loop runs on every unit" gains the boundary-refresh run point and one scenario.
- **`docs/ideas.md`** — the 2026-10-28 entry is removed: this change implements it.

No other artifact changes: the correction belongs in the skill, as the `agent-skills` requirement states, and the
`/sync-branch` command's lifecycle already carries the obligation the branch points at.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

`showcase/quality/agent-skills` — one `MODIFIED` requirement. Its "The quick review loop runs on every unit" requirement
states the loop's run points ("after a unit's proposal and after its implementation") and that it repeats "before a
manual review is requested"; the change adds the boundary-refresh run point and one scenario beside the existing four.
The step itself lives in `.opencode/skills/review-gate/SKILL.md`, as the same capability's maintenance requirement
states; `showcase/quality/commit-hygiene` specifies the `--review-ready` check, not what follows a refresh.

## Impact

- **The review gate**: every unit that refreshes its branch at the manual-pass boundary follows the return branch.
- **No runtime, build or deployment path**: the change edits a skill's procedure step, adds one spec requirement
  scenario, and removes the implemented idea.
