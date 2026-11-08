# Process defects — action-upstream-references-2026-11-09

### action-upstream-references-2026-11-09#1 — Packs classified by edited paths, not by the decisions the plan makes

- **Recorded** — 2026-11-09, by the implementing agent.
- **Stage** — proposal.
- **Expected** — the subject table loads every pack whose row matches the decisions the change makes, and the hand-over
  names them before the first governed decision.
- **Observed** — the hand-over named `jvm-development`, `jvm-testing`, and `gradle-build`, because the edited files are
  two `build-logic` Kotlin files. The draft body's "Not changed" table also decided a builder-pin question
  (`paketo-buildpacks/builder-noble-java-tiny#344`), a `deployment-operations` subject, and the first quick-review
  invocation stopped at classification with a missing-input report instead of screening.
- **Evidence** — the first round-1 invocation's reply: "Missing input (0 findings — screen stopped at classification):
  subject pack `deployment-operations` is not supplied, and a supplied artifact decides a subject that pack owns." The
  same round's `sessions.tsv` row records the invocation.
- **Consequence** — one quick-review invocation was spent on a missing-input stop; the hand-over was amended,
  `deployment-operations` was loaded, and the same session continued. No artifact was left wrong.
- **Knowledge boundary** — observed the stop and the hand-over's pack list; did not audit every row of the "Not changed"
  table for further subject matches at the time. The later rounds screened the table and raised no further missing pack.

### action-upstream-references-2026-11-09#2 — Unit records written in shapes the build's verifiers reject

- **Recorded** — 2026-11-09, by the implementing agent.
- **Stage** — implementation.
- **Expected** — the unit's `sessions.tsv` and `findings/round-<N>.md` follow the formats
  `showcase/quality/commit-hygiene` states, so `verifyAgentSessionContinuity` and `verifyFindingInstances` pass on the
  first check.
- **Observed** — the first Docker-free `check` failed at `verifyAgentSessionContinuity` ("round 1 carries 2 quick
  `report` row(s), not exactly one"), then at `verifyFindingInstances` ("round 1 carries 3 line(s) outside the instance
  shape", and similarly for rounds 2-5). The rows were re-encoded and the instance locations reduced to one `file:line`
  token per entry.
- **Evidence** — the two verifier lines quoted above, from the failed `./gradlew check` runs.
- **Consequence** — two extra full-check runs; the records are now accepted by both verifiers. No unit content was lost.
- **Knowledge boundary** — observed the verifier failures and the corrections; did not re-read the format requirement
  before writing the records the first time.
