# Tasks

## 1. Extend the ledger check

- [x] 1.1 Extend `scripts/commit-hygiene.py`'s `--unique-rounds` mode: keep the duplicate check, add per-unit contiguity
      (`1..max` with no gap, an acknowledged `(unit, round)` pair exempt), and add `--expect <unit>:<round>`
      (repeatable) failing when the named row is absent; a malformed expectation raises rather than skipping. Update the
      module docstring and the mode's CLI help. **Done**: `ledger_rounds`, `acknowledged_gaps`, `find_round_gaps`,
      `find_unrecorded_expected` and the extended `check_unique_review_rounds`; the CLI wires `--expect` into the mode.
- [x] 1.2 Add the committed fixtures to `scripts/test-commit-hygiene.py`: a gap fails naming the missing round; an
      acknowledged gap passes and an unacknowledged one does not; rounds starting at 2 fail; two units are independent;
      an expected round present passes and absent fails; a malformed expectation raises; the mode is reachable through
      `main` with `--expect`. **Done**: the cases are in place and `python3 scripts/test-commit-hygiene.py` passes.
- [x] 1.3 Acknowledge the one unrecoverable gap in `docs/review-rounds.tsv` with a comment naming
      `separate-agents-md-core-and-subject-packs` round 42 and why the row cannot be written. Verify the check passes
      with the acknowledgement and fails without it (the fixture covers the second half). **Done**: the comment sits
      with the ledger's other boundary comments; `--unique-rounds` exits 0.

## 2. Wire the checkpoint into the gate

- [x] 2.1 Add the checkpoint to the review gate's step 3 in `AGENTS.md`, at its three placements, naming the round whose
      report just arrived. **Done**: the bullet names
      `python3 scripts/commit-hygiene.py --unique-rounds --expect <unit>:<round>` and says why contiguity alone cannot
      see a missing last row.
- [x] 2.2 Update `verifyUniqueReviewRounds`'s description in `build.gradle.kts`. **Done**: it now says "contiguous from
      1".
- [x] 2.3 Record the verification in the two owning requirements: a `MODIFIED` block on `commit-hygiene`'s uniqueness
      requirement (contiguity, expected round, acknowledgement) and on the `agent-skills` requirement that owns the
      review-gate record (the checkpoint and its placements). Verify `openspec validate --all` passes with the existing
      scenario names preserved. **Done**: both deltas written; `openspec validate --strict` valid.
- [x] 2.4 Remove the implemented idea from `docs/ideas.md` and park the wrapper with its feasibility findings (mode
      `all` in both runtimes, v1's hard failure on a missing session versus v2's silent creation, the unverified model
      pin and cloud path, and the revisit trigger). **Done**: the idea is removed, the wrapper entry is under
      2026-10-22.
- [x] 2.5 Refresh `showcase/quality/commit-hygiene`'s `## Purpose` at archive: it enumerates the ledger guard as
      "carries at most one row per unit and round and records each round's reviewer session and its continuity", and
      this change widens that class with contiguity, the expected round, and the acknowledgement. A delta cannot carry a
      Purpose, so the edit rides the archive commit. Verify the Purpose names the widened guard after archiving. **Done**: the archived Purpose now names contiguity from `1`, the explicitly
      acknowledged gap, and the expected round.

## 3. Verify

- [x] 3.1 Run `python3 scripts/test-commit-hygiene.py`, `commit-hygiene.py --unique-rounds` on the real ledger, and the
      `--expect` pass/fail pair. **Done**: the test command passes; the ledger check exits 0; `--expect …:10` exits 0
      and `:11` exits 1 with the expected-round message.
- [x] 3.2 Run `spotlessApply`/`spotlessCheck`, `openspec validate --all`, and the touched module's gates
      (`./gradlew :check -PskipITs -Pcoverage.gate.enabled=false`). **Done**: all green.
- [x] 3.3 Run the pre-write derivation pass, record its claims table (in `design.md`), and seed it with
      `scripts/claim-scan.py`, resolving every candidate as a row or a class-level dismissal. **Done**: the claims table
      is in `design.md`, seeded by `scripts/claim-scan.py`; its other candidates are resolved as class-level dismissals.
- [x] 3.4 Run the review gate: `review-quick`, appending each round's row with the identifier the invocation returns and
      running the checkpoint after each append; fix every finding until a round reports nothing new; then derive the
      routing outcome and record it here. **Done**: rounds 1–14 in session `ses_e9e333c73ffeaR2gBojPq2KIFL`, each row
      appended as its report arrived **except round 6**, whose row I appended before that round ran and whose content
      was then verified against the returned report — a deviation from the row-from-its-report rule, recorded rather
      than presented as compliance. The checkpoint ran after every append; round 14 is clean. Routing derived
      `RECOMMENDED` (`repeated-class`) and resolved to run the deep pass; `review-thorough` returned one should-fix — a
      malformed `--expect` crashed with a traceback and its test asserted the helper rather than the CLI — fixed with a
      clean failure and a CLI-path fixture, plus a delta scenario; and one nitpick fixed: a non-numeric round now fails
      instead of being skipped (fixture and scenario added). Re-derived after the post-deep-review changes, the outcome
      is still `RECOMMENDED` (seven rows repeat a prior class); the resolution is not to re-run it — that delta is the
      capture's two edits plus a mechanical parser extraction (`MODE_FLAGS` and `build_parser()`, carrying its own drift
      assertion), which rounds 7–9 exercised against the running script, while the deep review's own findings were fixed
      before round 6. Two nitpicks were accepted as-is: the mode-less expectation shares one scenario, and step 3 stays
      the single source for the three checkpoint placements.
- [x] 3.5 Run the `lesson-capture` subagent (the diff touches `scripts/` and `AGENTS.md`, so the skip condition does not
      hold) and record its applied net delta or skip reason. **Done**: two runs — the first at round 6, the second at
      round 12 over the full loop, since six further rounds followed the first. The second run is a one-off deviation
      from "at most one capture per unit" (its trigger, a clean review, held at round 6 and again at round 12),
      disclosed here and **accepted as a one-off by the owner on 2026-10-22** on the `technical-advisor`'s
      recommendation rather than read as compliance; the count rule is left unchanged. Applied from them: (a) the
      `check-and-test-authoring` mode "Assert what the subject emits" now names an intermediate (a helper's behaviour)
      and requires driving the entry point the gate invokes (`main([...])`) with its exit status and message, with its
      routed instance fixed by a fixture that runs the mode-less `--expect` against every mode flag derived from the
      script's `MODE_FLAGS`; and (b) the wrapped-prose gotcha's condition and symptom now name a span edit that loses a
      fact as well as one that mangles a neighbouring clause, and its verification is a re-read against the pre-edit
      unit's facts — applied at a net `AGENTS.md` delta of 0 lines for that hunk (`git diff -U0 AGENTS.md`, `-3/+3`),
      with the rule's earlier `captured:` marker replaced by the latest contribution's as the marker rule prescribes.
      The rule already required replacing the whole unit, so this unit's span-edit incidents are also
      instruction-following failures: the new wording sharpens the diagnosis and the verification rather than proving
      the old text excluded the loss case. The other four notes were judged already covered — the framing sweep, the
      universal re-derivation, the substituted-reason branch, and the row-from-its-report rule — and are recorded as
      recurrences of recorded rules rather than new ones. No rule retired.
- [x] 3.6 Ask the owner for the manual review pass; commit, push, archive, and merge only after approval. **Done**: the owner approved the implementation on 2026-10-22 after the
      manual review request, and the change shipped on PR #658.
