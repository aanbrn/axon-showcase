# Proposal

## Why

The 2026-10-19 repository audit's agent-tooling section reported two stale claims — both at sites whose purpose is to
describe the repository, so `AGENTS.md` now under-describes it: the Build & Test `check` comment lists the
commit-hygiene members without `testReviewRouting`, which #628 added; and the model-pin gotcha's "exclude the vision
pin" clause still names `deepseek-v4-flash-vision-exp` as the current pin after it moved to
`opencode-go/claude-haiku-5-5`. The first is also the parked 2026-10-18 idea ("Sweep the two enumerations `#628` left
stale"), whose second site — the Prerequisites Python list — omits `review-routing.py`, and whose premise ("a later run
did not re-report it") this report falsified.

## What Changes

- `AGENTS.md`: the `check` member comment gains `testReviewRouting`.
- `AGENTS.md`: the Prerequisites Python list gains `review-routing.py`.
- `README.md`: the Prerequisites table's Python row — the same list's mirror — gains `review-routing.py`.
- `AGENTS.md`: the tooling-placement clause names `review-routing.py` as driven by the review gate's own procedure.
- `AGENTS.md`: the model-pin gotcha's exclude clause names the current vision pin and its actual reason.
- `docs/ideas.md`: the "Sweep the two enumerations `#628` left stale" entry is removed, now implemented.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: `skip_specs: true`. The change corrects always-loaded guidance and its README mirror; no capability spec owns the
`check` member set, the Prerequisites list, the tooling-placement instance lists, or the model-pin sweep (verified: no
spec mentions `verifyCapturedMarkers`, `review-routing.py`, `deepseek`, or `claude-haiku`).

## Impact

- Files: `AGENTS.md`, `README.md`, `docs/ideas.md`.
- No code, build, test, or deployment impact. The report's architecture finding was already drained by
  `remove-unrecorded-redgate-repository` (#639); its architecture advisory (ADR-0017's `Revisit when:` condition
  appearing met) and its two extraction opportunities are separate routes — an advisory is the owner's decision, and an
  extraction opportunity is never applied at all.
