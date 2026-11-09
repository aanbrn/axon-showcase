# Tasks

The unit record — its `Workspace:`, state, and pause checkpoint — is
`docs/units/apply-2026-11-07-audit-doc-findings/unit.md`.

## 1. Implementation

- [x] 1.1 `AGENTS.md`: rewrite the config-guard tracking bullet so the `could not parse` arm reads as retired — the pin
      reached `v1.16.0` (`bump-openspec-cli-2026-11-01`) and `openspec validate --all` fails the gate on that shape
      directly — add the applying unit's `captured:` marker, and leave the `must be an array of strings` arm as the only
      live guard, keyed to `#1894`. Verification: the bullet names the retired arm in the past tense, `ci.yml` carries
      only the array-shape grep, and the bullet's `captured:` markers still end it.
- [x] 1.2 Run the pre-write derivation pass and record the claims table in `design.md`; re-derive it after the last
      edit.

## 2. Verification

- [x] 2.1 `./gradlew spotlessApply` then `spotlessCheck` pass after the last edit.
- [x] 2.2 `./gradlew verifyCapturedMarkers` passes.
- [x] 2.3 `openspec validate --all` passes, and the change is valid with `skip_specs`.
- [x] 2.4 The manual 120-character check over the changed non-formatter files is clean.

## 3. Review gate

- [x] 3.1 Run the implementation quick review and fix every finding until it reports no new observations.
- [x] 3.2 Derive the routing decision with `python3 scripts/review-routing.py`, record it in
      `docs/units/apply-2026-11-07-audit-doc-findings/deep-review.md`, and run a deep review only as that outcome
      requires.
- [x] 3.3 Ask the owner for the manual review pass and end the turn there.
- [x] 3.4 Write `docs/units/apply-2026-11-07-audit-doc-findings/process-defects.md`, or state that none was observed.

## 4. Archive

- [x] 4.1 Run `openspec archive apply-2026-11-07-audit-doc-findings --yes` (a `skip_specs` change carries no spec sync)
      and tick this task in the archived `tasks.md`.
