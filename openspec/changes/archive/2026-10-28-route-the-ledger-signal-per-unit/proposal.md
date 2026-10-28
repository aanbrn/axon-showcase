# Proposal

## Why

The routing floor silently loses the ledger signal for a unit with no change directory: `review-routing.py` returns no
repeated-class signal unless `--change` is supplied, and `--change` must name an existing change directory. The review
gate tells such units to omit the flag, so a unit whose recorded rounds show a non-converging loop still derives
`NOT_NEEDED`. On 2026-10-28 the generalization unit reached `RECOMMENDED` only because the caller read the ledger by
hand.

## What Changes

- `scripts/review-routing.py` gains `--unit <name>`, the identity the ledger rows record, separate from `--change`,
  which stays the change directory whose delta is inspected.
- `--unit` defaults to `--change` when only the latter is passed, so every change-unit invocation keeps its behaviour.
- A derivation with no unit identity fails and reports `REQUIRED`, rather than returning an empty signal that reads as
  "nothing recorded".
- `scripts/test-review-routing.py` replaces the test that encodes the missing-signal boundary and adds one case per
  control: the helper cases; a `--change`-only CLI run; a `--unit`-only run without a change directory; a change delta
  with no block kind whose different `--unit` decides; a change delta with an `ADDED` block whose different `--unit`
  does not; and an invocation naming neither identity, with and without a pre-empting `REQUIRED` signal.
- `docs/ideas.md` loses its 2026-10-26 entry for this defect.
- `.opencode/skills/review-gate/SKILL.md` states that a unit passes `--unit` with the name its ledger rows record
  whenever that differs from its change name — a no-change-dir unit always — so the omission of `--change` no longer
  reads as a lower floor.

## Capabilities

### New Capabilities

none

### Modified Capabilities

none

## Impact

- `scripts/review-routing.py`, `scripts/test-review-routing.py`, `docs/ideas.md`, and the review-gate skill's routing
  section.
- No JVM code, build script, deployment, or API change; the routing rule, its outcomes, and the signal classes are
  unchanged.
- The existing `testReviewRouting` task runs the new controls.
