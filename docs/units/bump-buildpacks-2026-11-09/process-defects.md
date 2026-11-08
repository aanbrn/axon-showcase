# Process defects — bump-buildpacks-2026-11-09

### bump-buildpacks-2026-11-09#1 — a hand-over's readiness claim falsified by its own append

- **Recorded** — 2026-11-09, by the unit's writer session.
- **Stage** — review.
- **Expected** — a review hand-over section states a check's verification as of its own step, because appending the
  section changes a staged path.
- **Observed** — the Round 5 and Round 7 sections each asserted that `--review-ready` reports the index current; each
  append left `review-handover.md` staged-and-edited, so the command exited 1 at the next read. Round 5 fixed the line
  by rewording it; the Round 7 section was written with the original wording again.
- **Evidence** — `findings/round-5.md` and `findings/round-7.md` each record a `claim-unreproducible` instance on that
  line; the Round 7 report quotes
  `A path is staged and then edited, so the manual pass would show the index's older copy`.
- **Consequence** — two quick-review rounds carried a finding the rewording pattern already answered; both are repaired
  in the unit by stating the pre-append pass and the gate's later re-stage/re-run.
- **Knowledge boundary** — observed in this unit's own rounds; not checked against other units' hand-overs.

### bump-buildpacks-2026-11-09#2 — a task's Done note cited a comparison the artifacts do not carry

- **Recorded** — 2026-11-09, by the unit's writer session.
- **Stage** — implementation.
- **Expected** — a task's `Done` note states a result its named source reproduces.
- **Observed** — task 2.1's note said the five rows were "identical to the tracker issue's body"; the comparison was
  made in the writer's session, and the issue body is not among the artifacts supplied to a reviewer.
- **Evidence** — the Round 7 report lists, under its limitations, that the tracker issue's body was not supplied and
  that task 2.1's comparison is therefore not independently reproducible.
- **Consequence** — one review finding; repaired by restating the five rows themselves in the Done note and dropping the
  comparison.
- **Knowledge boundary** — the comparison did hold when the writer read the issue body on 2026-11-09; what is recorded
  is that no artifact carries it.
