# Process defects — pin-wrapper-distribution-checksum

No process defect observed while working this unit.

The unit's own premise was corrected during the proposal stage — the design claimed a bump erases a pinned checksum,
which a probe disproved before implementation — but that was a hypothesis tested and corrected inside the normal
proposal loop, not a process defect.

### pin-wrapper-distribution-checksum#1 — An advisor pass's findings were recorded outside the unit's layout

- **Recorded** — 2026-11-06, retroactively, by the main agent while reconciling the unit records in
  `reconcile-review-records`. At the time the unit ran, no defect was observed; this one was found later, when the two
  units' record sets were compared against the regulated layout.
- **Stage** — proposal and implementation.
- **Expected** — a pass that is not a review round records its findings in the unit's `review-handover.md`, and the
  unit's directory holds only the regulated five files.
- **Observed** — the advisor's proposal pass and its implementation pass were written to `findings/advisor-1.md` and
  `findings/advisor-2.md`, while the hand-over named them only as "the advisor's pass, in `findings/`".
- **Evidence** — the two files' five and four entries, and the hand-over's round-4 section, which names the same pass.
- **Consequence** — nine finding instances lived outside the regulated set; the proposal pass now sits in the round-4
  section, the implementation pass in its own "no review round" section, and the files are deleted.
- **Knowledge boundary** — observed: the two files and the hand-over's references to them; not checked: what the
  invocation's instructions said about where the findings should go.
- **Decision** — no-change: already covered by `showcase/quality/commit-hygiene`'s unit-layout requirement and the
  `verifyUnitLayout` gate that refuses an unadmitted path.
