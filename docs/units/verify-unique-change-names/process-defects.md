# Process defects — verify-unique-change-names

### verify-unique-change-names#1 — The reviewer's invocation omitted the subject pack the hand-over named

- **Recorded** — 2026-11-02, by the unit's agent.
- **Stage** — review.
- **Expected** — the review gate's hand-over names every applicable subject pack, and the invocation prompt carries that
  classification, since a delegated agent inherits no pack its caller loaded.
- **Observed** — the hand-over file stated `Packs: gradle-build; the check-and-test-authoring skill`, and the
  `review-quick` invocation prompt did not name `gradle-build`. The reviewer classified the change, found that pack
  matching, and stopped before screening, because its definition forbids searching the repository for a pack the
  hand-over omits.
- **Evidence** — the reviewer's round-1 report: "Missing pack: `gradle-build` … the invocation names no subject packs at
  all", with the record line `2026-11-02 verify-unique-change-names 1 - - 0`.
- **Consequence** — one reviewer invocation produced no screen; the round is recorded in `docs/review-rounds.tsv` with
  no finding class, and the screen resumed as round 2 on the same reviewer session.
- **Knowledge boundary** — observed the omission and the stop; did not check whether the hand-over file's `Packs:` line
  would have sufficed had the reviewer been told to read it first.

- **Decision** — no-change: the delegation rule already requires the caller to name every applicable subject pack in the
  subagent's prompt.

Implementation stage: no process defect observed beyond entry #1 — the mode, its controls, the build wiring, and the
shared archive walk landed with all gates green, and the three mutations reproduced the failures they claim.
