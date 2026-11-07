# Process defects — verify-air-model-pin

### verify-air-model-pin#1 — a control that could not fail

- **Recorded**: 2026-11-07, by the implementing agent.
- **Stage**: proposal.
- **Expected**: a check for a model identifier in the added text that shows 1 with an identifier present and 0 without.
- **Observed**: the check read `git diff -- <staged paths>`, which compares the index with the worktree and is empty for
  staged new files, so it printed 0 whether or not an identifier was there.
- **Evidence**: the round-1 finding quoted the same line giving 0 from `git diff --` and 1 from `git diff HEAD --` in a
  scratch repository.
- **Consequence**: one review round; the check now compares against `HEAD` and is proved with a staged planted line.
- **Knowledge boundary**: observed the empty diff on the staged set; did not audit the unit's other verification
  commands for the same shape beyond the ones a round named.
- **Decision** — no-change: already covered by `check-and-test-authoring`'s "A verification step must discriminate the
  edit from its absence": `git diff --` compares the index with the worktree and is empty for a staged new file.

### verify-air-model-pin#2 — a review-gate invocation that cannot run

- **Recorded**: 2026-11-07, by the implementing agent.
- **Stage**: proposal.
- **Expected**: the unit's task lists gate commands that execute.
- **Observed**: `commit-hygiene.py --expect-clean <unit>` was written alone, and it exits 1 because the flag needs
  `--unique-rounds`; a later form folded it into that mode.
- **Evidence**: the round-2 finding quoted the error "--expect-clean needs --unique-rounds: it reads the ledger the same
  mode parses".
- **Consequence**: one review round; task 3.1 now lists three invocations that each run.
- **Knowledge boundary**: observed the failing form and the fix; did not re-read every command in the change's artifacts
  for executability beyond the review's scope.
- **Decision** — no-change: already covered by `review-gate` §1 — a prescribed command sequence is one fact, reproduced
  whole in a scratch tree.

### verify-air-model-pin#3 — a round counter in the record's Stage that decayed

- **Recorded**: 2026-11-07, by the implementing agent.
- **Stage**: proposal and implementation.
- **Expected**: the unit record's `Stage` states the operation and position without a value that the next round
  falsifies.
- **Observed**: the field carried "round 3's corrections are applied and round 4 is pending"; advancing it re-created
  the same staleness at round 5 and again at round 9.
- **Evidence**: rounds 5 and 9 each reported the stale counter; the field now states the pending action with no number.
- **Consequence**: two review rounds.
- **Knowledge boundary**: observed the counter and its removal; did not check whether another checkpoint field carries a
  value that the loop moves.
- **Decision** — no-change: already parked in `docs/ideas.md` 2026-10-27 — correct `pause-resume-unit`'s `Stage` field.
