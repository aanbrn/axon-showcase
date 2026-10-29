# Process defects — widen-the-claim-scanner-absences

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### widen-the-claim-scanner-absences#1 — A narrowing excluded the incident it was designed for

- **Recorded** — 2026-10-29, by the writing session (`ses_e7e0a3d04ffepyFcMfYYisYm9y`), during the proposal stage.
- **Stage** — proposal (the design's first pattern).
- **Expected** — a pattern written to surface a recorded incident matches that incident's sentence.
- **Observed** — the first design restricted `no` to a sixty-character predicate window whose verb list omitted
  `reduced`, so the incident it cited ("no finding reduced `AGENTS.md`") would not have matched; the consultation found
  the omission and the list's other gaps.
- **Evidence** — the design's D1 before the revision, naming `reduces` and not `reduced` while the Context quoted the
  recorded sentence with `reduced`; the revised D1, which drops the window and matches bare `no`.
- **Consequence** — one design revision before implementation; nothing shipped with the narrow pattern.
- **Knowledge boundary** — observed this design. Did not check whether the other predicate verbs in the list would have
  matched their own motivating sentences.

### widen-the-claim-scanner-absences#2 — The change directory was staged and then edited

- **Recorded** — 2026-10-29, by the writing session, during the implementation.
- **Stage** — implementation (the gates after the pattern change).
- **Expected** — the staged set is the final content, so the manual pass shows what will be committed.
- **Observed** — the change directory was staged before the design and proposal were revised, so `--review-ready`
  refused with "A path is staged and then edited, so the manual pass would show the index's older copy" for both files.
- **Evidence** — the two refusal lines naming `design.md` and `proposal.md`; the re-staged files, after which the check
  passed.
- **Consequence** — one refused readiness run and one re-stage; nothing shipped stale.
- **Knowledge boundary** — observed these two files. Did not check whether any other unit's staged set carried the same
  staleness for longer than one run.
