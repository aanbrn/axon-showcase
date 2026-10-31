# Process defects — auto-setup-new-worktree

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### auto-setup-new-worktree#1 — The trigger model was written into three guidance sites before it was settled, and the first fixes patched one copy at a time

- **Recorded** — 2026-10-31, by the writing session (`ses_e6f985602ffeUDJ4DtV0zmSDfc`), during the review stage.
- **Stage** — review.
- **Expected** — a rule's trigger is settled in the artifact that owns it (the delta) and every guidance copy is
  re-derived from that one statement; a finding that reports a divergence is fixed by re-deriving the copies, not by
  patching the site it names.
- **Observed** — the `AGENTS.md` bullet, the `setup-idea` skill section, and the delta each stated a different trigger
  (`.idea/` **and** `.python-version`; `.python-version` alone; no gate), and `design.md` contradicted itself on whether
  the setup runs at all. Round 1 raised the divergence; the round-2 fix settled the model in the guidance but not in the
  delta, and four classes repeated in round 2 (`contradiction`, `over-claim`, `impact-omission`,
  `claim-unreproducible`); round 3 then raised two more instances (`wording`, `formatting`).
- **Evidence** — the round reports' finding lists and their repeated-class markers in `docs/review-rounds.tsv`.
- **Consequence** — two extra review rounds, after which the delta became the single owner and every copy was re-derived
  from it in one pass.
- **Knowledge boundary** — observed the divergence and the rounds; did not measure how much work an up-front
  single-owner draft would have saved.
