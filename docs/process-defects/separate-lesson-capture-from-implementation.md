# separate-lesson-capture-from-implementation

Process defects observed while working this change. Recorded under `AGENTS.md`'s "Workflow: Record process defects";
analysis is a later, owner-triggered unit; `generalize-process-defects-2026-10-27` appended each record's `Decision`
line.

### separate-lesson-capture-from-implementation#1 — A site list maintained by hand kept missing sites

- **Recorded**: 2026-10-27, by the planning session.
- **Stage**: review.
- **Expected**: the plan's "which files state the retired behavior" set would name every file each task edits.
- **Observed**: the set was hand-written from the prose of the artifacts. Review rounds 1–3 each found a site the list
  omitted — one added by a merge that landed while the unit was paused, and two the author had passed over — and rounds
  2–6 repeated the same class.
- **Evidence**: round classes `impact-omission` (rounds 1, 2, 3, 8) and `incomplete-enumeration` (rounds 2, 3, 4, 5, 6)
  in `docs/review-rounds.tsv`; round 2 named the omitted `AGENTS.md:454` and round 3 `spec.md:1089`, while
  `AGENTS.md:1419` is a live-reading survivor recorded in rounds 16–17 rather than an omission.
- **Consequence**: four review rounds spent on one class; the fix was a structural change — closing the _scope_ (the
  durable surfaces the behavior can live in) and giving one task per-file ownership of a sweep, instead of widening the
  list. Nothing remains broken; the cost is the rounds.
- **Knowledge boundary**: observed — the omissions and the class repetition. Assumed — that the site set is now closed;
  only a future edit against this plan tests that.
- **Decision** — no-change: the catch-all-disposition mode in `check-and-test-authoring` and the single-sourcing scope
  rule already cover it.

### separate-lesson-capture-from-implementation#2 — A review round's ledger row was written late

- **Recorded**: 2026-10-27, by the planning session.
- **Stage**: review.
- **Expected**: each round's row is appended when its report arrives, before the next round's findings are fixed.
- **Observed**: round 4's row was never appended; the gap surfaced only when round 5's `--unique-rounds --expect` check
  failed with "missing round 4".
- **Evidence**: the failing check output
  (`Unit 'separate-lesson-capture-from-implementation' is missing round 4: its rounds must be contiguous from 1`), and
  the row inserted afterwards.
- **Consequence**: the record was incomplete for one round; a later reader would have seen rounds 3 and 5 with no 4. The
  row is restored and the incident is noted in `review-handover.md`; no other artifact was affected.
- **Knowledge boundary**: observed — the missing row and the check that caught it. Unchecked — whether any other unit's
  ledger has an acknowledged gap of the same shape.
- **Decision** — proposed: applied by `review-gate` §2's numbered "When the report arrives" step;
  https://github.com/aanbrn/axon-showcase/pull/715.

### separate-lesson-capture-from-implementation#3 — A premise was read from another unit's uncommitted tree

- **Recorded**: 2026-10-27, by the planning session.
- **Stage**: proposal.
- **Expected**: the planning artifacts describe the tree at the unit's base revision.
- **Observed**: the capture placement was first described from `review-gate` §5 as read in the primary checkout, whose
  working tree carried another unit's uncommitted change to that file; at the base revision §5 stated no capture
  precondition at all.
- **Evidence**: `git -C <primary> diff --stat` showing `M .opencode/skills/review-gate/SKILL.md`; the base revision's §5
  opening, which named only the quick review and any required deep review.
- **Consequence**: the design's Context and a task were written against unmerged text and had to be re-derived after the
  refresh; the sibling change then merged (`61bcdee2`), so the clause exists now, at three sites rather than one.
- **Knowledge boundary**: observed — the read and its source tree. Unchecked — whether any other premise in the first
  draft came from the same tree; the claims table was re-anchored to `ea1ee3b5` afterwards.
- **Decision** — no-change: withheld — one occurrence, and the "Interrogate the premise" `HEAD` obligation substantially
  covers it; reopen on a second occurrence.

### separate-lesson-capture-from-implementation#4 — The unit's review record trailed its rounds

- **Recorded**: 2026-10-27, by the resumed writing session (`ses_e84c9b955ffeY6V3z6ZN1VELva`).
- **Stage**: review.
- **Expected**: the unit's review record — the hand-over's per-round table and the `Resume checkpoint` — is brought to
  each round as that round closes, as the workflow this unit ships requires.
- **Observed**: the record trailed the ledger through the unit. Rounds 6 and 7 found the checkpoint or the hand-over a
  round or more stale, rounds 13, 14, 16, 17 and 18 repeated it, round 20 restated it, and round 22 found the
  checkpoint's next step and pending decision describing different rounds. The deep review's replacement pass then found
  the checkpoint trailing the closed round 23, and rounds 26, 27 and 28 each found the hand-over missing the round that
  had just closed.
- **Evidence**: `docs/review-rounds.tsv` rows 6, 7, 13, 14, 16, 17, 18, 20, 22, 26, 27, 28 and 30 carry `missing-task`,
  `claim-unreproducible` or `contradiction` on this subject, and `review-handover.md`'s table for each of those rounds
  names the omission; the deep pass's instance is its table's finding 2, which carries no ledger row.
- **Consequence**: the class was raised in thirteen quick-review rounds, the last being round 30, and in that one
  deep-review pass, and each occurrence was repaired by the pass that raised it; the record was brought current at
  round 31. Nothing else remains broken.
- **Knowledge boundary**: observed the rounds and their findings. Did not check whether any occurrence went unreported,
  since the ledger records only what a round raised.
- **Decision** — proposed: applied by `review-gate` §2's replacement of the round-state bullet;
  https://github.com/aanbrn/axon-showcase/pull/715.
