# widen-readme-auditor-with-language-axis

### widen-readme-auditor-with-language-axis#1 — A universal quantifier survived three review rounds before its cause was addressed

**Recorded** — 2026-10-29, by the unit's writing session.

**Stage** — review.

**Expected** — `AGENTS.md`'s review-gate rule says that when the loop reports the same class of observation round after
round, the loop is not converging and the root cause is re-derived rather than patched.

**Observed** — a `contradiction` was recorded against the design's goal sentence in rounds 3, 4 and 5. Each round's
quoted passage differed: the sentence's universal claim survived two rewrites that changed which site it named, and the
class stopped after round 5's report named the universal itself as the fault and both design passages were rewritten
without one.

**Evidence** — `docs/review-rounds.tsv` rows 1686–1690 record `contradiction` in rounds 3, 4 and 5 for this unit and
none in round 6; each round's report quoted the design's goal sentence as the site.

**Consequence** — three review rounds on one sentence; the design's goal and risk bullet were rewritten in round 6.
Nothing else remains broken.

**Knowledge boundary** — observed: the three reports, their quoted passages, and the diff between the fixes. Not
checked: whether the same universal-claim shape appears in other changes' design documents, which is a grep over the
archive rather than a fact of this episode.

- **Decision** — no-change: the review gate's non-convergence rule already requires re-deriving the root cause when the
  same class repeats, not patching another instance.

### widen-readme-auditor-with-language-axis#2 — A markdown append after the last formatter run failed `spotlessMarkdownCheck`

**Recorded** — 2026-10-29, by the unit's writing session.

**Stage** — implementation.

**Expected** — the formatter gate is re-run after the final write to a markdown file, and a checklist tick or a result
note is such a write.

**Observed** — after the implementation edits and a green `spotlessApply`/`spotlessCheck`, the unit appended the
smoke-run expectation table to `tasks.md` and later ticked its tasks. The next full `check` failed at
`:spotlessMarkdownCheck`, whose report named `openspec/changes/widen-readme-auditor-with-language-axis/tasks.md` as the
file with format violations.

**Evidence** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` printed `> Task :spotlessMarkdownCheck FAILED`
and listed that file with a diff at its line 71; `./gradlew spotlessApply` followed by `spotlessCheck` and the same
`check` then passed.

**Consequence** — one `check` invocation was spent on a formatting defect, and the intervening green claim covered a
tree that no longer existed. Repaired by re-running the formatter after the appends; nothing else remains broken.

**Knowledge boundary** — observed: the failing task's output and the two commands that cleared it. Not checked: whether
this is the third recurrence of the class in this session's units (the mechanism-claims unit recorded the same shape),
since that is a comparison across records rather than a fact of this episode.

- **Decision** — no-change: the formatting rule already requires `spotlessApply` and `spotlessCheck` after the final
  write, a checklist tick included.

### widen-readme-auditor-with-language-axis#3 — Smoke-run seeds were anchored on wrapped prose and did not match

**Recorded** — 2026-10-29, by the unit's writing session.

**Stage** — implementation.

**Expected** — a seed is inserted by an anchor that survives the formatter's wrapping, since markdown prose is
re-wrapped and a multi-line anchor is a coordinate that moves.

**Observed** — two seed scripts anchored on sentence fragments spanning a source line break ("The projection service
consumes Kafka events", "The gateway applies CORS for the web UI origin"); both returned a match count of 0 and aborted
before writing, so no seed was applied and no wrong state reached the file.

**Evidence** — the scripts printed `anchor count 0: The projection service consumes Kafka events` and
`0 | The gateway applies CORS for the web UI origin` for the same strings that read correctly in the rendered document;
the successful run instead inserted each seed after a heading line, whose exact text is stable.

**Consequence** — two wasted script runs; the third attempt seeded five passages correctly and the restore was verified
byte-identical. Nothing remains broken.

**Knowledge boundary** — observed: the anchors, the counts, and the rendered text. Not checked: whether any other probe
in the repository anchors on wrapped prose, which a grep over the scripts would establish.

- **Decision** — no-change: already parked — the same wrapped-prose entry.
