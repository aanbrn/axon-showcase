# Tasks

## 0. The pre-write derivation pass (this change carries its own claims table)

- [x] 0.1 Run the pre-write derivation pass and record its output as the claims table in `design.md`.

## 1. The comparison's verdict

- [x] 1.1 `BaselineDrift` distinguishes a **regression** (a mean or 95th-percentile figure beyond the tolerance) from an
      **unconfirmed excursion** (only p99 figures beyond it), withholds the reference for both, and exposes each set of
      figures for the report — a regression failing the run and an excursion not.
- [x] 1.2 `BaselineDriftTests` carries the known-bad and known-good cases for the split: a p99-only excursion is
      unconfirmed, withholds the reference or not per a refresh, and is not a regression; a mean regression and a
      95th-percentile regression each fail and withhold; a mean regression accompanied by a p99 excursion reports both;
      an all-within-tolerance run writes the reference; and each case asserts what the old behavior got wrong (the p99
      excursion was a regression), so the case would fail against the pre-change code.
- [x] 1.3 `BaselineStats` reports the verdict and exits non-zero only for a regression or a figure-less measurement, so
      an unconfirmed excursion leaves the run passing while the reference is withheld.
- [x] 1.4 `scripts/load-test-baseline.sh` derives its closing verdict from the reference file (before/after), so it
      never reports a write that was withheld, its message names the confirmation path for an excursion, and its header
      comment (which states the old one-figure verdict) is corrected.
- [x] 1.5 Verify the verdict without a cluster by driving the comparison task directly (`:load-tests:baselineStats`)
      with the run's own log and a **doctored reference** (the effect a doctored log would have, and simpler): with the
      p99 figures lowered so only they exceeded the tolerance, it exited zero, left the reference byte-identical, and
      reported the excursion as unconfirmed, naming the six figures; with `ScheduleShowcase.meanMs` lowered so it
      regressed, it exited non-zero and named that figure. Then the whole script against the local cluster with
      shortened phases (`CALIBRATE_DURATION=PT1M BASELINE_DURATION=PT2M`): its closing message matched the file's state,
      printing "reference written to …" on a pass — the withhold branch is covered by the doctored-reference cases
      above. Both files the run rewrites (the committed reference and the dated record) were copied aside and restored
      byte-identical afterwards.

## 2. Capture the behavior in the spec

- [x] 2.1 The delta for `showcase/quality/load-tests`: a `MODIFIED` block carrying the requirement's description and all
      twelve existing scenarios **by name and in the main spec's order**, with the regression scenario's body scoped to
      the mean and 95th percentile and one added sibling scenario for the unconfirmed p99 excursion — never a renamed
      scenario.

## 3. Docs the change owns

- [x] 3.1 Correct every statement of the one-figure verdict and of the settled idea. Enumerate the set with
      `git grep -nE 'beyond the tolerance|fails the run|leaves the reference unchanged|beyond that re-flags|parked in .docs/ideas.md. rather' -- ':!openspec/changes' ':!openspec/specs'`
      — the archives are recorded history and the main spec's text is the delta's to sync at archive. Read every hit:
      some are not verdict statements (an unsupported profile name, a smoke request failure). Correct each verdict
      statement — a dated record's in the past tense with a superseding pointer (`15ad680f` is the precedent) — and
      record in the report the set the command returned.

- [x] 3.2 Record the capability's `## Purpose` outcome: **no refresh owed** — it already names the drift comparison and
      the record the run writes, and the split changes neither.

- [x] 3.3 Remove the promoted idea entry in `docs/ideas.md` — this change implements the issue it was promoted to (#571)
      — and park the gate hole its own work surfaced: `spotlessCheck` printed "Skipping … because it does not converge"
      for a non-converging markdown file and exited zero, so this change's own `tasks.md` was silently ungated until its
      nested fenced block became an inline span.

## 4. Verification

- [x] 4.1 Re-run the claims-table rows with their sources and confirm each reproduces.
- [x] 4.2 Run `./gradlew spotlessApply` then `spotlessCheck`, `openspec validate --all`, and the manual 120-character
      check over every changed file's content the formatter does not own — Javadoc prose, string literals, the shell
      script, and the record rows (the formatter leaves those alone, so only the manual check catches them; four such
      lines were found and wrapped at round 13).

- [x] 4.3 Run a `review-quick` pass over the implementation, passing the prior round's instances.
- [x] 4.4 Run the per-unit `lesson-capture` and record the applied net `AGENTS.md` delta (**+7 lines**: a skip is not a
      pass, merged into the Formatting convention's markdown sub-bullet, and the per-round record append is made at the
      round's report, merged into the capture bullet; no rule retired).
- [ ] 4.5 Request the user's manual review pass.
