# Tasks

The unit record — its `Workspace:`, state, and pause checkpoint — is
`docs/units/apply-2026-11-07-audit-findings/unit.md`.

## 1. Implementation

- [x] 1.1 Delta, "Review-round ledger rows are unique": copy the main spec's whole requirement block and delete the
      orphaned sentence `That input clause scopes the build's verification.`, leaving one blank line between the two
      surviving paragraphs. Verification: the delta's block differs from the main spec's only by that deletion, and
      every scenario header survives.
- [x] 1.2 Delta, "Review-round rows record the reviewer session's continuity": copy the main spec's whole requirement
      block, move the detached paragraph's retained obligations — "The verification SHALL validate the recorded fields,
      and passing it SHALL NOT attest that the stated cause occurred or authorize the review gate to continue." — into
      the body after the `commit-hygiene.py` mode sentence, and drop the paragraph and its first clause, which the body
      already states. Verification: the delta's block differs only by the added sentence and the removed paragraph,
      every scenario header survives in the same relative order, and the header matches the main spec verbatim.
- [x] 1.3 Re-scope after the owner split the audit findings by route: remove the `AGENTS.md` edit and trim the planning
      artifacts to the spec-only scope; the docs finding ships as `apply-2026-11-07-audit-doc-findings`. Verification:
      `git diff --cached -- AGENTS.md` is empty and no artifact describes the guidance edit as this unit's work.

## 2. Verification

- [x] 2.1 `openspec validate --all` passes with the delta at the mirrored capability path.
- [x] 2.2 The spec's distinctive phrases land where the findings say: `That input clause scopes` appears nowhere in the
      delta, and the retained SHALL sentence appears once, in the body above the scenarios.
- [x] 2.3 `./gradlew spotlessApply` then `spotlessCheck` pass after the last edit.
- [x] 2.4 Run the pre-write derivation pass and record the claims table in `design.md`; re-derive it after the last
      edit.

## 3. Review gate

- [x] 3.1 Run the proposal quick review and fix every finding until it reports no new observations.
- [x] 3.2 Run the implementation quick review and fix every finding until it reports no new observations.
- [x] 3.3 Derive the routing decision with `python3 scripts/review-routing.py`, record it in
      `docs/units/apply-2026-11-07-audit-findings/deep-review.md`, and run a deep review only as that outcome requires.
- [x] 3.4 Run a quick review over the re-scoped tree and fix every finding until it reports no new observations.
- [x] 3.5 Ask the owner for the manual review pass and end the turn there.
- [x] 3.6 Write `docs/units/apply-2026-11-07-audit-findings/process-defects.md`, or state that none was observed.

## 4. Archive

- [ ] 4.1 Run `openspec archive apply-2026-11-07-audit-findings --yes`, stage both the change-dir move and the
      `openspec/specs/**` sync, and tick this task in the archived `tasks.md`. No Purpose refresh is owed: the
      capability's scope does not change.
