# Process defects — record-the-figure-defect

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### record-the-figure-defect#1 — A quotation was verified by a token instead of the quoted span

- **Recorded** — 2026-10-29, by the writing session (`ses_e7e0a3d04ffepyFcMfYYisYm9y`), during the review gate.
- **Stage** — review (the unit's second quick-review round).
- **Expected** — a quotation written into a durable artifact is verified as the contiguous span it quotes.
- **Observed** — the record being added for `record-stale-origin-main-check` quoted the merged body's finding with a
  leading `a`, and the verification grepped the token `non-reproducible`, which returns one hit whether or not the
  quoted span exists; the body's line wrap separates `a` from `non-reproducible`, so the span with the leading `a` does
  not exist contiguously.
- **Evidence** —
  - `gh pr view 741 --json body` gives `0` for the quoted phrase with its leading `a` and `1` without it.
  - `docs/review-rounds.tsv` records this unit's round 2 with the classes `claim-unreproducible,wording`.
  - Of the merged body's statements, the record quotes only the contiguous `recorded in the same entry's review history`
    (`grep -c` → `1`).
- **Consequence** — the quotation was replaced by a description before the commit; nothing shipped with it.
- **Knowledge boundary** — observed the under-matching verification for this quotation. Did not check whether the unit's
  other search verifications were token greps; the second round examined this one.

- **Decision** — no-change: the "Documentation & durable claims" rule already requires a quotation to be verbatim, and
  the "Evidence and search" rule records the token grep's under-match; the record is a compliance miss.

### record-the-figure-defect#2 — A moved fact left its dependent clause behind in another field

- **Recorded** — 2026-10-29, by the writing session (`ses_e7e0a3d04ffepyFcMfYYisYm9y`), during the review gate.
- **Stage** — review (the unit's second quick-review round).
- **Expected** — a correction that removes a fact from one field also removes the statements in other fields that depend
  on it.
- **Observed** — the first round's fix made the writer the subject of `record-stale-origin-main-check.md`'s `Observed`
  and dropped its clause about a later loop run printing the value again, while that record's `Knowledge boundary` kept
  the phrase naming both runs; the second round reported a clause with no antecedent in the record and, in the same
  field, an assertion the field's own disclaimer contradicted.
- **Evidence** —
  - `docs/review-rounds.tsv` records this unit's round 2 with the class `wording` among those repeating a prior round.
  - After the fix the record contains no `either run` or `later` clause (`grep -c` → `0`).
  - The removed clause's text exists in no artifact, since the record was corrected before its commit, so it is
    described from the session rather than quoted.
- **Consequence** — the clause was deleted before the commit; the second run it described has no durable evidence, so
  the record no longer asserts it.
- **Knowledge boundary** — observed this residue in `record-stale-origin-main-check.md`. Did not establish whether the
  earlier units' records carry the same shape — the unit checked that record only.

- **Decision** — no-change: the doc-consistency sweep rule already requires a removed fact's dependent statements
  removed with it.

### record-the-figure-defect#3 — A `RECOMMENDED` routing outcome was resolved without the owner

- **Recorded** — 2026-10-29, by the writing session (`ses_e7e0a3d04ffepyFcMfYYisYm9y`), during the review gate.
- **Stage** — review (the routing stage, after the unit's seventh quick round).
- **Expected** — a `RECOMMENDED` routing outcome runs only on the owner's explicit run-or-skip decision; when that
  decision is absent, ask the owner and end the turn.
- **Observed** — `python3 scripts/review-routing.py --unit record-the-figure-defect` printed `ROUTING: RECOMMENDED` with
  `[repeated-class] 3 rows repeat a prior class`, and the deep pass was invoked in the same turn, with no owner question
  between the derivation and the invocation.
- **Evidence** —
  - `python3 scripts/review-routing.py --unit record-the-figure-defect` prints `ROUTING: RECOMMENDED` with a
    `[repeated-class]` signal; that round's reading was `3 rows repeat a prior class`.
  - The `review-thorough` invocation followed the derivation in the same sequence.
  - The rule's own sentence in `.opencode/skills/review-gate/SKILL.md`: "A `RECOMMENDED` outcome runs only on the
    owner's explicit run-or-skip decision — when it is absent, ask the owner and end the turn".
  - The owner's report ("А зачем нам deep review на записях о дефектах?"), which is the signal rather than the proof.
- **Consequence** — one deep pass was started without the decision; the owner questioned it, and its report has not been
  read at recording time. Nothing was committed, and the routing answer still has to be recorded on the pull-request
  checklist line.
- **Knowledge boundary** — observed this sequence in this session. Did not establish whether the pass, once it reports,
  contributes anything, and did not establish whether the same shortcut happened in the earlier units of this session.

- **Decision** — no-change: review-gate §3 already requires a `RECOMMENDED` outcome to run only on the owner's explicit
  decision, asked and recorded.
