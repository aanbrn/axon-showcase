# Proposal

## Why

The unit layout — `unit.md`, `rounds.tsv`, `review-handover.md`, `process-defects.md`, and `findings/round-<N>.md` — is
recorded in prose and enforced for its `findings/` subtree alone, so an agent invented a file beside `unit.md` and the
gates stayed green. The previous change closed the finding store only, and its own record shows the miss:
`continuity-violation.md` sat at the unit's root, where nothing looks. A convention with no check is how the class
recurs.

## What Changes

- One requirement states the unit layout: the exact paths a unit's directory admits — four files at its first level and
  `findings/round-<N>.md` at its second — with presence left to the requirements that own it.
- One check enforces it: a `commit-hygiene.py` mode fails on any tracked path under a unit's directory that the two
  levels do not admit — by name and by position — naming the unit, the path and that the layout does not admit it.
- The finding store's requirement points at that layout for its file set instead of restating it, so the kinds have one
  owner.
- The store-only check the previous change added gives way to the layout check: one predicate, one mode, one `check`
  task.
- The review gate's pointer names the layout requirement rather than the store's.
- The idea for this work is removed from `docs/ideas.md`.

## Capabilities

### New Capabilities

none

### Modified Capabilities

- `showcase/quality/commit-hygiene`: a unit's directory holds only the record kinds the layout names, verified over the
  tracked records, and the finding store's requirement delegates its file set to that requirement.

## Impact

- `openspec/specs/showcase/quality/commit-hygiene/spec.md` — one added requirement and one modified, via the delta.
- `scripts/commit-hygiene.py` — the layout predicate, its mode, and the dispatch and flag declarations.
- `scripts/test-commit-hygiene.py` — the layout's tests and the coverage mapping entry; the finding store's two
  stray-file arms move to them.
- `build.gradle.kts` — the verification's `check` task.
- `.opencode/skills/review-gate/SKILL.md` — the writer step's pointer.
- `docs/ideas.md` — the idea's removal.
