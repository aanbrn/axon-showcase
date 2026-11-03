# correct-readme-mechanism-claims

### correct-readme-mechanism-claims#1 — A formatter verdict was read from the pipe's exit status

**Recorded** — 2026-10-29, by the unit's writing session.

**Stage** — implementation.

**Expected** — the formatter gate's result is read from the Gradle invocation's own exit status.

**Observed** — after the three README edits, the unit reported the formatter green from
`./gradlew spotlessCheck -q 2>&1 | tail -3; echo "rc=$?"`. The status echoed was `tail`'s, not Gradle's, so the unit
carried a green formatter claim while the file it created afterwards
(`docs/process-defects/correct-readme-mechanism-claims.md`) was still unformatted. The full `check` then failed at
`:spotlessMarkdownCheck`.

**Evidence** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` reported `> Task :spotlessMarkdownCheck FAILED`
with `BUILD FAILED in 46s`; `./gradlew spotlessApply` followed by `./gradlew spotlessCheck > log 2>&1; echo "rc=$?"`
reported `rc=0`, and the check ran green afterwards.

**Consequence** — one full `check` invocation was spent on a defect the formatter would have fixed, and the green claim
was false for the interval between the two runs. Repaired by running `spotlessApply` and re-reading the status without a
pipe; nothing else remains broken.

**Knowledge boundary** — observed: both command shapes and their statuses. Not checked: whether any other script or
workflow in this repository reads a tool's status through a pipe, which is a grep over the scripts rather than a fact of
this episode.

- **Decision** — no-change: 'A verdict echo is not a check' and the CI rule to read the run's own exit status already
  require the command's status, not a pipe's.

### correct-readme-mechanism-claims#2 — A reviewer hand-off named no subject packs, so the pass reviewed nothing

**Recorded** — 2026-10-29, by the unit's writing session.

**Stage** — review.

**Expected** — `AGENTS.md`'s subject-pack rule states that a delegated agent does not inherit a pack its caller loaded,
so the caller names every applicable pack in the reviewer's prompt.

**Observed** — the unit's round-1 hand-off named no pack. The reviewer stopped at its definition's input contract and
returned a missing-input report: it classified `deployment-operations` and `jvm-testing` as matching and declined to run
the pass. The re-issued hand-off, naming both packs, produced the first real review round; the ledger records that round
as the unit's round 1.

**Evidence** — the first report opens "**Missing input — cannot judge the artifact.** No subject pack was named in the
hand-over", and ends "Packs loaded: none (no pack was named and I did not search for one)". The second invocation's
report opens "1 finding" and lists the two packs it loaded in full.

**Consequence** — one reviewer invocation produced no evidence about the artifact, and the unit's ledger carries a round
whose number belongs to the real pass rather than to the aborted one (the row's transition had to be `created` for that
reason, which round 2 then caught as a second defect). Repaired by naming the packs and re-invoking the same session;
nothing in the artifact changed.

**Knowledge boundary** — observed: both prompts, both reports, and the ledger rows. Not checked: whether the reviewer
would have searched for an applicable pack had the hand-off stayed silent — its definition makes silence a stop, and
this episode is one instance of the stop, not a study of the alternative.

- **Decision** — no-change: the subject-pack rule already states that a delegated agent does not inherit a pack its
  caller loaded and that the caller names every applicable pack.
