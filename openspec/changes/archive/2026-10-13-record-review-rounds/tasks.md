# Tasks

## 1. The record line

- [x] 1.1 Added the record-line clause to `.opencode/agent/review-quick.md`'s report contract: every round ends with one
      tab-separated line — date, unit, round, the classes raised, which repeated a prior class, and the count of
      findings that were a compliance miss of an existing rule (`AGENTS.md`, the specs, or this definition) — for the
      calling agent to append verbatim.
- [x] 1.2 Wrote the delta for `showcase/quality/agent-skills` — two `MODIFIED` blocks: "Per-change quality-gate and
      analysis subagents are available" (description + all eight scenarios, adding the record-line clause to the
      quick-review scenario) and "Experience analysis is available to agents" (description + all four scenarios, adding
      the window's review rounds to the digest's countable claims).

## 2. The record and its aggregation

- [x] 2.1 Added `docs/review-rounds.tsv` — a comment header naming the fields and one row per review round (this
      change's own proposal rounds, then each implementation round as it ran).
- [x] 2.2 Extended `scripts/experience-analysis.sh`'s derived-facts block to summarize the window's
      `docs/review-rounds.tsv` rows (listing each row and reporting the round and compliance-miss totals), and updated
      its header comment — which enumerates the block's contents — to name the review rounds.
- [x] 2.3 Recorded the append as a step in the review loop via the `AGENTS.md` auto-review pointer, which now names the
      per-round record line alongside the finding classes and clauses (the pointer points at the spec for the
      obligation).
- [x] 2.4 Widened the digest-block enumerations where they live, derived by grepping the block's descriptions:
      `.opencode/commands/retrospective.md`'s digest step and `.opencode/agent/experience-analyzer.md`'s input list both
      now name the window's review rounds, and the record line is named in `.opencode/agent/review-quick.md`'s report
      contract. `AGENTS.md`'s experience-analyzer bullet and the `README.md` `/retrospective` descriptions are
      deliberately left (capability-level, no sub-enumeration).

## 3. The parked entry

- [x] 3.1 Removed the 2026-10-13 "Record per-unit review rounds" entry from `docs/ideas.md`, per the file's convention.

## 4. Verification

- [x] 4.1 Ran `./scripts/experience-analysis.sh 2026-10-13` over the seeded record — the block lists every row and
      reports the window's round and compliance-miss totals, which grow as each review round's row is appended — and
      `2026-10-14`, which excludes all rows (their date is `2026-10-13`), so rows outside the window are dropped.
- [x] 4.2 Ran `./gradlew spotlessApply` then `spotlessCheck` (BUILD SUCCESSFUL), `openspec validate --all` (27 passed /
      0 failed), and the manual 120-character check over `scripts/experience-analysis.sh` and the change dir (clean; the
      `.tsv` is data and is excluded by design).
- [x] 4.3 Ran a `review-quick` pass over the implementation: 5 rounds, each raising a different class
      (`missing-artifact` +`ambiguity`, `stale-claim`, `incomplete-enumeration`, `contradiction`), round 5 clean — each
      round's row appended to `docs/review-rounds.tsv`.

## 5. Capture

- [x] 5.1 Ran the `lesson-capture` subagent; applied three clause-level merges (no new bullet): the recurring-unit-type
      bullet now says the artifact set is the **union** of the sites every artifact-bearing task names (the
      `missing-artifact` repeat's root cause); the documented-numbers bullet's point-in-time clause is corrected — a
      count of the change's own growing artifact stale-claims a checked task; and the process-count sentence now excepts
      review rounds, which `docs/review-rounds.tsv` makes verifiable. Applied net `AGENTS.md` delta: **+6 lines** (2974
      → 2980), including the auto-review pointer and the two `captured:` markers.
- [ ] 5.2 Ask the owner for the manual review pass, after 4.3 is clean and 5.1 is done.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit and the main-spec sync ride this PR.
- The remaining unit follows, parked in `docs/ideas.md` by title: the loop bound ("Bound the review loop for a record
  artifact", 2026-10-13), which depends on this record and on the derived-facts digest. Its implementing change removes
  its entry.
