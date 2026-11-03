# Process defects — adopt-heading-roles

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### adopt-heading-roles#1 — The heading sweep's first enumeration missed a wrapped reference

- **Recorded** — 2026-11-02, by the unit's agent.
- **Stage** — implementation.
- **Expected** — the enumeration of components that cite the lifecycle or review-gate heading covers every one of them,
  so the change's scope statement ("thirteen commands") is derived from the population, not from a search that can miss.
- **Observed** — the first sweep used `git grep -l "Workflow: Change lifecycle\|Workflow: The review gate"`, which
  requires the heading on one line. `generalize-process-defects.md` wraps between "Change" and "lifecycle", so it was
  absent from the twelve-file list and was edited only after a later `git grep -E 'Workflow: Change|Workflow: The'`
  surfaced it.
- **Evidence** — the first sweep listed twelve files; the wrapping-tolerant sweep lists thirteen, the extra being
  `.opencode/commands/generalize-process-defects.md:57-58`; the change's scope counts were corrected from twelve to
  thirteen afterwards.
- **Consequence** — one command would have kept its literal while the change, its proposal, and its trace said the sweep
  was complete. Repaired in the same change before the review gate; no work shipped with the defect.
- **Knowledge boundary** — observed: both sweeps' file lists and the wrapped line. Assumed: that no other file wraps a
  heading reference in a way neither pattern reaches. Not checked: a corpus-wide scan for patterns split across three or
  more lines.

- **Decision** — no-change: the `### Evidence and search` rule already records that a phrase `spotlessApply` split
  across a line break returns zero, and that a pattern is proved on a known positive before its zero is read.
