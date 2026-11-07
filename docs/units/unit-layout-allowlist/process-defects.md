# Process defects — unit-layout-allowlist

### unit-layout-allowlist#1 — The reviewer hand-over omitted the applicable pack, for the second unit running

- **Recorded** — 2026-11-06, by the main agent, on the reviewer's missing-input report.
- **Stage** — proposal.
- **Expected** — the prompt for a delegated reviewer names every pack the reviewer's classification turns on, since a
  delegated agent does not inherit a pack its caller loaded.
- **Observed** — the hand-over for round 1 stated `Packs: none` and excluded the four rows without addressing the build
  one, while the artifact set registers a `check` task; the reviewer returned a missing-input report without reading the
  diff or recording a round. Re-invoked with `gradle-build` named, the pass ran.
- **Evidence** — the report's opening line, "Missing input — `gradle-build` pack not named", and its correction request
  naming `.opencode/skills/gradle-build/SKILL.md`.
- **Consequence** — one reviewer invocation was spent on a report that judged nothing; no review round was recorded for
  it, and the next invocation continues that session.
- **Knowledge boundary** — observed: this session's ledger, and the merged rows against their instance counts; not
  checked: what each merged round's compliance-miss figure was. A later finding-by-finding assessment would be a dated
  retrospective reclassification, not recovery of the original figures.
- **Decision** — proposed: https://github.com/aanbrn/axon-showcase/pull/859 — applied as P1 in `AGENTS.md`'s
  subject-pack section — the caller checks a change's planned or actual edited paths for a subject its decisions did not
  name.

### unit-layout-allowlist#2 — The ledger's sixth field was written as the finding count

- **Recorded** — 2026-11-06, by the main agent, on round 2's finding.
- **Stage** — proposal.
- **Expected** — the review-round record's sixth field is the compliance-miss count: the number of findings that missed
  an existing rule — `AGENTS.md`, a subject pack, the specs, or the reviewer's definition — as the reviewer's definition
  states it. The `commit-hygiene` requirement names the field without defining it.
- **Observed** — round 1's row recorded `7`, the round's whole finding count, where the reviewer's line stated `1`: the
  field was written with the wrong quantity. In the two merged units every row's sixth field equals that round's number
  of finding-instance entries, which is consistent with the same counting method; that equality does not show the
  figures were wrong, since every finding might have qualified as a compliance miss and a clean round's zero is right.
- **Evidence** — round 2's report, "the compliance-miss field records `7`, the round's whole finding count; round 1's
  report stated `1`"; the rows themselves; the merged units' `findings/round-<N>.md` entry counts beside their rows; and
  the reviewer's definition, which asks how many findings "were a compliance miss of an existing rule".
- **Consequence** — this unit's row is corrected to `1`. The merged units' values stay as recorded and are unverified,
  not proven wrong; that correction was parked in `docs/ideas.md`, and `correct-merged-unit-records` — pending its own
  merge — records the sixth-field correction as two dated notes and, separately, `assert-paketo-pin-coverage`'s
  formatter incident as `#6`.
- **Knowledge boundary** — observed: this session's ledger and the two merged units' rows, each against its round's
  finding-instance count; not checked: what compliance-miss figure each merged round would actually have carried, since
  no stored report preserves it.
- **Decision** — no-change: already covered by `review-quick.md`, which defines the compliance-miss count the reviewer's
  report line carries.
