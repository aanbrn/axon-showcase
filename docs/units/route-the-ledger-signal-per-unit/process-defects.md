# Process defects — route-the-ledger-signal-per-unit

Process defects observed while working this change. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### route-the-ledger-signal-per-unit#1 — Claims-table rows were written from intent instead of re-run

- **Recorded** — 2026-10-28, by the writing session (`ses_e7e0a3d04ffepyFcMfYYisYm9y`), during the proposal round.
- **Stage** — proposal (the claims table, rounds 1–3).
- **Expected** — every claims row is derived by running its command at the unit's base and copying that output,
  including a source path that exists at the base.
- **Observed** — row 6 cited
  `openspec/changes/archive/2026-10-28-generalize-process-defects-2026-10-28/review-handover.md`, which does not exist:
  that unit had no change dir, and its review record lived in its pull-request body. Row 6's figure then named repeated
  rounds 1, 2, 4, 6 and 7, where the ledger's repeats column is non-empty on rounds 2, 3, 4, 6 and 7. Both were caught
  by review rounds, not by the pass.
- **Evidence** — round 1 finding 3 (the absent archive path) and round 2 finding 4 (the figure); the re-run
  `git show 4475a8e4:docs/review-rounds.tsv | grep generalize-process-defects-2026-10-28` prints `-` for round 1 and a
  repeat for rounds 2, 3, 4, 6 and 7.
- **Consequence** — two proposal rounds spent on the table. Row 6 now cites the 2026-10-26 `docs/ideas.md` entry and the
  ledger rows, every row was re-run at the base in round 4, and no claim shipped on the wrong figure.
- **Knowledge boundary** — observed for this table. Did not check whether other changes' claims rows cite artifacts that
  never existed; this unit's rows were re-run, the earlier ones not.

- **Decision** — no-change: already parked — `docs/ideas.md` 2026-10-27 'Give claims-table rows an executable
  verification mode'; §1 covers the row's derivation meanwhile.
