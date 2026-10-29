# Process defects — deversion-readme-archive-count

### deversion-readme-archive-count#1 — A live guidance-evidence record quoted the wording the change deletes

**Recorded** — 2026-10-29, by the unit's writing session.

**Stage** — implementation.

**Expected** — `AGENTS.md`'s docs-refresh convention treats `docs/guidance-evidence/` records as live docs, refreshed in
the change that falsifies a statement there; a change removing a quoted string must therefore grep for the string first.

**Observed** — the README edit deleted `(120+ and counting)`, and
`docs/guidance-evidence/documentation-and-durable-claims.md` still cited "the README's \"120+ and counting\"" in the
present tense as an exemplar of the shape-describing form. The unit's first pass did not grep `docs/` for the string;
round 1's `docs-residue` finding named the stale citation.

**Evidence** — round 1's report quotes `docs/guidance-evidence/documentation-and-durable-claims.md:68` and reports
`rg '120\+' README.md` → no match; the citation was then rewritten to rely on `specs-auditor`'s wording alone, which
reproduces at `.opencode/agent/specs-auditor.md:12`.

**Consequence** — the exemplar set lost one member and the record now cites one instead of two; the guidance-evidence
record reproduces against a live file again. Nothing else remains broken.

**Knowledge boundary** — observed: the stale sentence, the deleted README text, and the corrected citation. Not checked:
whether other guidance-evidence records quote wording that has since changed, which a sweep over the directory would
establish.

### deversion-readme-archive-count#2 — A review finding required the record earlier than any rule does

**Recorded** — 2026-10-29, by the unit's writing session.

**Stage** — review.

**Expected** — `AGENTS.md`'s review gate asks for the manual pass only once the unit's process-defect record exists; no
rule sets an earlier deadline for it.

**Observed** — `review-quick` round 1 raised `missing-task` against the absent record file, calling it "the reason the
record file is owed at all", and the unit accepted that reading and wrote a record before the second round. The gate's
own text places the record at the manual-pass boundary, which the unit had not reached.

**Evidence** — round 1's report names `docs/process-defects/deversion-readme-archive-count.md` as absent under
`missing-task`; `.opencode/skills/review-gate/SKILL.md` §5 lists the record among the preconditions of the _manual
review pass_, and `docs/review-rounds.tsv` row 1 carries that class.

**Consequence** — no artifact was wrong and no work was lost: the record exists, and the acceptance cost one round's
attention. The unit's first version of this file then called the absence a process defect, and the consultation showed
the deadline it cited does not exist; that episode is removed rather than kept as a miss.

**Knowledge boundary** — observed: the round-1 report, the gate's §5 text, and the ledger row. Not checked: whether
other units have accepted a `missing-task` class for this same reason, which the ledger's occurrences would show.
