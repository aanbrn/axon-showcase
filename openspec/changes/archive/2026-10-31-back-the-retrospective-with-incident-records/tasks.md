# Tasks — back-the-retrospective-with-incident-records

Workspace: unit=back-the-retrospective-with-incident-records;
path=/Users/afanasyev/Development/axon-showcase-back-the-retrospective-with-incident-records;
branch=back-the-retrospective-with-incident-records; writer=ses_e6f985602ffeUDJ4DtV0zmSDfc

## 1. The digest's process-defect index

- [x] 1.1 Add the window-scoped index block to `scripts/experience-analysis.sh`: the unique record files a commit since
      the window start touched, each with its `###` incident headings; a count line stating the unique-file count, the
      incident-heading count, and the revision the index was derived at; the files the `since` date's own commits
      touched; `(absent at HEAD)` for a selected path deleted in the window; and an explicit zero line when the
      selection is empty.
- [x] 1.2 Add `scripts/test-experience-analysis.sh`: a temporary git repository with records committed the day before
      the window, on the boundary day, and after it (one file touched in both the before and after commits, one record
      updated in the window, one with no `###` heading, one deleted in the window, one whose path holds a space and a
      non-ASCII character), a stub `gh` returning an empty payload, and the real digest run from that directory. Assert
      the emitted index, its counts, the boundary list, the absent marker, the zero line for an empty selection, and the
      run's exit status.
- [x] 1.3 Register the test in `build.gradle.kts` as a `check` verification task, following `testDoctorCharter`'s
      pattern (`inputs.file` for the script, the test, and the files it reads; `outputs.upToDateWhen { false }`;
      `commandLine(shExecutable, …)`), and wire it into the `check` block.
- [x] 1.4 Run the new test, and run the digest over a real window to show a non-empty block with the boundary resolution
      on real history.

## 2. The classes figure's unit

- [x] 2.1 `scripts/review-rounds.py`: build each row's canonical class set before incrementing, so one row contributes
      at most one to a class; state the unit in the classes line.
- [x] 2.2 `scripts/test-review-rounds.py`: replace the assertion that a row's two aliasing labels count twice with the
      one-contribution case, and cover the unit wording.
- [x] 2.3 Run `scripts/test-review-rounds.py` and show the changed case failing on the pre-change module (the control),
      then passing.

## 3. The analyzer contract

- [x] 3.1 `.opencode/agent/experience-analyzer.md`: name the index in the input list; require a causal claim resting on
      a record to have read its body and to name it; state how the classes figure is read.
- [x] 3.2 `.opencode/commands/retrospective.md`: name the index in step 2's digest description.
- [x] 3.3 Delta spec at `specs/showcase/quality/agent-skills/spec.md` (written; `openspec validate --changes` is the
      gate).

## 4. The 2026-10-31 report's correction

- [x] 4.1 Correct the four dependent occurrences (`docs/retrospectives/2026-10-31.md:14,78,98,130`) to the round
      occurrence, keep the originally reported totals, and add a dated correction note naming both reasons a present-day
      recomputation differs.

## 5. Docs refresh

- [x] 5.1 `AGENTS.md`'s experience-analyzer bullet: the digest description names the index.
- [x] 5.2 `README.md`'s retrospectives bullet: the digest description names the incident records.
- [x] 5.3 Sweep `docs/ideas.md`: no parked entry is implemented by this change; the three out-of-scope entries stay
      unchanged, and their status tags are re-read against this change.

## 6. Verification

- [x] 6.1 `./gradlew spotlessApply` after the last edit, then `spotlessCheck`.
- [x] 6.2 `./gradlew testReviewRounds` and the new digest test task. Outcome: `testReviewRounds` and
      `testExperienceAnalysis` pass; the collapsing case failed on the base module's aggregation and the `-z`-removed
      variant failed on the file count, so both controls discriminate. The stub carries no host `jq`: a `PATH` shim
      whose `jq` exits 127 leaves the test green, and an unexpected `--jq` shape fails the stub.
- [x] 6.3 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (or the touched surfaces' tasks where a full run is
      disproportionate), and `openspec validate --all`. Outcome: the Docker-free `check` is BUILD SUCCESSFUL, and
      `openspec validate --all` reports 27 passed, 0 failed; `check` re-ran green on the refreshed tree (52 s) after the
      stub's `jq` dependency was removed.
- [x] 6.4 Re-derive the design's claims table after the edits, and state any figure that moved. Outcome: every row
      reproduces and no figure moved; the collapse leaves `claim-unreproducible` at 281 in this ledger, as recorded in
      `design.md`.
- [x] 6.5 Smoke-run `experience-analyzer` over a synthetic digest that carries the index: it reads a named record's body
      and cites it, states the classes figure as a round occurrence, and returns no file edit. Record the outcome here;
      this is a precondition for archive, not a deferrable task. Outcome: the analyzer read
      `docs/process-defects/add-claims-execution-verify.md`, cited `#1` with its stage, evidence, and consequence,
      checked the second record (which states no defect was observed) and did not cite it, and read the unit's ledger
      rows; it made no file edit.

## 7. Review and records

- [x] 7.1 Record the pre-write derivation pass and the claims table in `design.md` (written at proposal time; re-derived
      in 6.4). Outcome: recorded in `design.md`, seeded with `scripts/claim-scan.py` and every candidate resolved as a
      row or grouped dismissal.
- [x] 7.2 Run the `review-quick` loop and record each round's row in `docs/review-rounds.tsv`. Outcome: every round's
      row is appended as it closes, both ledger checks pass, and `--expect-clean` is green at the closing round.
- [x] 7.3 Record the deep-review routing outcome (`scripts/review-routing.py`) on this task. Outcome: `NOT_NEEDED` — the
      derivation over the unit's closing round rows matched no signal, so no deep review hangs on the unit.
- [x] 7.4 Write `docs/process-defects/back-the-retrospective-with-incident-records.md`, or state that none was observed.
      Outcome: the unit's records are in `docs/process-defects/back-the-retrospective-with-incident-records.md`.
- [x] 7.5 Record `AGENTS.md`'s applied net delta on this task. Outcome: `AGENTS.md` 2,772 → 2,773 lines (+1), from the
      digest description's added index clause.

## 8. Ship

- [x] 8.1 After the owner's approval: commit, push, open the pull request, wait for `build`, archive, and merge.
      Outcome: PR #775 opened and `build` green in 2m52s before this archive commit; the merge follows it.
