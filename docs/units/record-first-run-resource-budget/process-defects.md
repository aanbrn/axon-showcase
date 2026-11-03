# Process defects — record-first-run-resource-budget

### record-first-run-resource-budget#1 — A hand-over claimed an edit the tree did not carry

**Recorded** — 2026-10-30, by the unit's writing session.

**Stage** — review.

**Expected** — a review hand-over describes the tree as it is: `AGENTS.md`'s "Never chain an edit to a commit" rule and
the review gate's fix-verification step both put the re-read of the edited text before the claim.

**Observed** — the round-3 hand-over told the reviewer that the decision record's Command cell now stated the sampling
origin ("from the moment `composeUp` was invoked"). The cell read only `docker stats --no-stream` over the eight
containers. The edit that was meant to change it had aborted earlier — its `str.replace` anchor carried the table's
padding and matched nothing — and the later fragment-level fix repaired the Result column only.

**Evidence** — the applying script exited on an assertion (its message began
`AssertionError: (0, '| Memory, sampled every 2 s for 208 s |`) and wrote nothing; a read of the record afterwards
(`sed -n '30,34p'`) showed the Command cell still unchanged; the reviewer then reported the origin as absent at
`docs/decisions/2026-10-30-first-run-resource-passport.md:32`.

**Consequence** — one review round reported a claim about the unit's own work as unfixed; the cell and the caveat are
corrected in the same unit.

**Knowledge boundary** — observed: the aborted script, the unchanged cell, the reviewer's finding. Not checked: whether
any other claim in that hand-over described an edit rather than the tree (the round-2 hand-over was written from the
applied diffs).

- **Decision** — no-change: the 'Never chain an edit to a commit' rule and the review gate's fix-verification step
  already require the edited text re-read before a hand-over claims it.

### record-first-run-resource-budget#2 — Removing a section's last entry left its heading behind

**Recorded** — 2026-10-30, by the owner, who found the residue at `docs/ideas.md:27` after the merge; recorded here by
the unit's session.

**Stage** — implementation, surfacing after the merge.

**Expected** — `docs/ideas.md` groups ideas under dated sections and keeps only open ideas, so removing a section's last
entry leaves no heading behind.

**Observed** — the change removed the "First-run resource passport" entry, the only one left under `## 2026-10-29`, and
left that heading with no entries under it. It shipped to `main` that way and the owner spotted it afterwards.

**Evidence** — the merged change's diff removes the entry's four lines and nothing else in that file
(`docs/ideas.md | 4 --` in `b4b331f8`, whose full stat is 5 files); a section scan over the merged file reported 28
dated sections with one empty, at line 27.

**Consequence** — an empty dated section reached `main`; it is removed in its own docs pull request (2 deleted lines),
which also leaves 27 sections and 98 entries with none empty.

**Knowledge boundary** — observed: the empty section, the merged diff, and the scan. Not checked: whether any other
document keeps a grouping whose last item can disappear the same way (the scan covered `docs/ideas.md` only).

- **Decision** — no-change: `verifyIdeasSections` now fails the build on an empty dated section, so the prose rule is
  enforced.
