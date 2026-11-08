# Unit: unify-review-event-history

Workspace:
`unit=unify-review-event-history; path=/Users/afanasyev/Development/axon-showcase; branch=unify-review-event-history; writer=ses_e4add21daffe0vW0OISMmtQS3i`

State: active

A change unit: its planning artifacts live under `openspec/changes/unify-review-event-history/`.

The owner's authorization (2026-11-08) approves the parked idea "One continuous unit-scoped history of typed review and
consultation events" for implementation, its revisit condition now met: `gate-review-handover-section` (#864), the first
unit after the register landed, recorded both a quick loop and registered deep-review and consultation invocations,
produced by the mechanism rather than backfilled.

The design target the idea states, and the technical advisor's selection agreed: one continuous event spine with a
unit-wide event number and numbered result references, agent-specific report contents, typed gate projections (the
latest quick round for `--expect-clean`, quick rounds for the routing signal and the digest, full passes for the deep
budget), the deep-review statuses' single authoritative home on the unit's `deep-review.md` (amended 2026-11-08 by the
owner's approval of the fold: a unit with no change directory has no review task), and an explicit forward boundary
rather than reconstructed history.
