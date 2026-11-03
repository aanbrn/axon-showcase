# Process defects — retry-infra-verification-fetches

### retry-infra-verification-fetches#1 — The manual pass was re-requested before the resolved tree was reviewed

- **Recorded** — 2026-10-28, by the writing session (`ses_e832e4043ffe6ADYTEa13DvV1s`), during the review stage.
- **Stage** — review.
- **Expected** — after a branch sync, the unit's review runs over the result before the manual pass is requested again,
  as `/sync-branch`'s lifecycle requires ("The full per-unit review gate runs over the result").
- **Observed** — after the hand resolution of two conflicts (`docs/ideas.md`, `docs/review-rounds.tsv`), the readiness
  check and the Docker-free `check` ran, and the manual pass was requested without a new quick-review round over the
  resolved tree.
- **Evidence** — the event sequence: quick round 4 (the closing clean round) → `/sync-branch` with the two conflicts
  resolved by hand → the manual-pass request → the owner's consultation → quick round 5. Round 5's ledger row records
  the later clean round over the resolved tree.
- **Consequence** — the owner received a tree the closing round had not seen; round 5 later reported no observations. No
  data loss is claimed.
- **Knowledge boundary** — one case observed; why the round was skipped and whether it recurs are not established. The
  agent's reading of the review gate's §5 alone as permitting the request is its own account, not a proven diagnosis.

- **Decision** — no-change: already parked — `docs/ideas.md` 2026-10-28 "Give the review gate's §5 a post-refresh return
  branch", whose evidence is this record.

### retry-infra-verification-fetches#2 — A review round's ledger row was appended only after the next round's check failed

- **Recorded** — 2026-10-28, by the writing session (`ses_e832e4043ffe6ADYTEa13DvV1s`), during the review stage.
- **Stage** — review.
- **Expected** — each round's row is appended when its report arrives, and both ledger checks run before the next
  reviewer invocation.
- **Observed** — round 6's report arrived; its record edit (the post-sync defect record) and the parked-idea edit were
  made, and round 7 was invoked without appending row 6; the ledger carried rounds 1–5 and 7.
- **Evidence** — `python3 scripts/commit-hygiene.py --unique-rounds --expect retry-infra-verification-fetches:7` printed
  "Unit 'retry-infra-verification-fetches' is missing round 6: its rounds must be contiguous from 1"; the row's content
  is round 6's record line (`-  -  0`).
- **Consequence** — the row was reconstructed from round 6's report and inserted in order; the ledger checks run before
  shipping, so the gap would have blocked the commit rather than reaching the repository.
- **Knowledge boundary** — observed for this unit. `docs/process-defects/retry-chart-dependency-fetch.md#1` records the
  same class for the previous unit, so this is a recurrence; other units were not checked.

- **Decision** — no-change: already parked — the `docs/ideas.md` 2026-10-22 "reviewer wrapper" entry, whose
  `Revisit when:` condition is not met.
