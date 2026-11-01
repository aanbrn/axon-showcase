# Process defects — expose-the-jfr-recording-property

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

The unit observed no process defect while it was being worked. One is recorded below: it was found after the merge, when
the owner asked what remained, and it is kept here because the sweep that would have caught it belongs to this unit and
this unit has no branch left to carry the correction.

The unit's measurement briefly mixed two populations — the first comparison ran the whole component suite, whose delta
included the JFR control that only executes when the recording is on, and the isolated re-measurement on one test class
replaced it before any figure was reported. That is the probe-correction working inside the unit, not a defect with a
consequence to record.

### expose-the-jfr-recording-property#1 — The docs sweep missed the stale enabling route

**Recorded** — 2026-11-01, by the writing session of the follow-up unit `fix-jfr-enabling-route-in-ideas`.

**Stage** — ship, after the merge of #790.

**Expected** — the docs-refresh convention's sweep for references to what a change shipped includes `docs/ideas.md`.
This unit shipped a new way to enable the JFR recording, so the parked stall entry that named the old one was in scope.

**Observed** — after this unit merged, the entry still named `-Dshowcase.diagnostics.jfr=true` as the enabling route.
The line itself came in with #789 (`git blame`); what this unit did not do is read it. A round-1 `review-quick` finding
(`docs-residue`, `docs/review-rounds.tsv`) had already corrected the same wrong route in the recording's rendered hint
and class Javadoc, so the class was known and was fixed at the site the finding named, and the docs sweep did not
follow. The owner's question about what remained surfaced it.

**Evidence** — `git show 954fd380:docs/ideas.md` at the parked entry names the `-D` form alone;
`git grep -n "diagnostics\.jfr" 954fd380 -- . ':!openspec/changes/archive'` shows the code's own sites already naming
both forms, which leaves that entry as the only prose site still carrying the old route.

**Consequence** — the default branch named an enabling route a Gradle invocation does not deliver to the forked test JVM
until `fix-jfr-enabling-route-in-ideas` corrected it. Nothing was broken: the recording simply stayed off.

**Knowledge boundary** — observed the stale line, the merged hint text, and the blame of the line. Did not check whether
the docs sweep was scoped deliberately to the code, and make no claim about intent.

- **Decision** — no-change: the docs-refresh sweep already names `docs/ideas.md` among the surfaces to sweep for what a
  change shipped.
