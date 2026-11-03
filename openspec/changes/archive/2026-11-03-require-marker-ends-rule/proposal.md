# Proposal

## Why

`AGENTS.md` requires a `captured:` provenance marker to sit "at the end of the rule it records", and said at the base
revision that a pre-commit guard enforces that part. The guard enforced only half of it: `_marker_is_in_rule_block`
accepts a marker anywhere in a bold-lead block, so a marker inserted mid-rule passed while reading as part of the
sentence around it. Thirteen rule blocks in the corpus carried exactly that shape, and the capability spec described
only the half that is enforced.

## What Changes

- `scripts/commit-hygiene.py` requires the marker chain to end its block: no rule text of that block may follow it. The
  block ends at the next bullet, numbered step, bold-lead paragraph, heading, table row, fence, or blank line; the chain
  may wrap, chain several markers, and carry a citation after a name; an `evidence:` tag line may follow it.
- The thirteen blocks whose markers sat mid-block have their chains moved to the block's end, so the corpus satisfies
  the rule it states.
- `AGENTS.md`'s marker rule keeps the authoring obligation and now names what the guard checks — the marker ends its
  bullet or paragraph and lies inside a rule block — instead of claiming the whole-rule placement is enforced.
- `scripts/test-commit-hygiene.py` flips its mid-item case to a failure and gains cases for a mid-paragraph marker, a
  wrapped name, a citation, and a chain at the end.
- The delta widens the placement requirement and its "correctly placed" scenario, and adds the mid-rule and fenced-block
  scenarios.
- The parked idea is removed from `docs/ideas.md`.

## Capabilities

### New Capabilities

none

### Modified Capabilities

`showcase/quality/commit-hygiene`: the marker-placement verification now requires the marker to end its rule block, not
merely to sit inside one.

## Impact

- `scripts/commit-hygiene.py`, `scripts/test-commit-hygiene.py`, `AGENTS.md` (its marker rule's enforcement clause and
  the marker moves), three guidance files (the marker moves), `docs/ideas.md`, and the unit's process-defect record.
- No change to which files are scanned or what the guard does when it finds a misplaced marker.
