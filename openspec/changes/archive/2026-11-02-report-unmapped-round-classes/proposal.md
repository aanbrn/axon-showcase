## Why

The retrospective digest decides a window's convergence figures from `docs/review-rounds.tsv`, and its spec requires a
label it cannot normalize to be reported rather than passed through silently. That holds only for pre-canonical labels:
`scripts/review-rounds.py` returns a post-boundary label unchanged, so the four acknowledged non-canonical labels are
counted as classes while the `unmapped` line reads `none` — the figure the 2026-10-31 retrospective found wrong.

## What Changes

- `scripts/review_vocabulary.py` (new) — the canonical class list's parser, moved out of `scripts/commit-hygiene.py` so
  the gate and the digest read one implementation.
- `scripts/commit-hygiene.py` — imports that module and keeps its API, so its callers and tests are unchanged.
- `scripts/review-rounds.py` — a label no normalization yields a class for is reported in the `unmapped` line instead of
  counted as a class.
- `scripts/review-rounds.py` — an unreadable list prints that line as unestablished with the reason rather than `none`,
  and the block is still produced so the retrospective gather keeps working.
- `scripts/test-review-rounds.py` — a new case covers the non-canonical post-boundary label (the old behaviour had
  none), and the canonical, unreadable-list, pre-boundary, and live-list cases are added or kept.
- `build.gradle.kts` — `testReviewRounds` declares the definition and the shared module as inputs.
- `build.gradle.kts` — `testCommitHygiene` and `verifyReviewClassVocabulary` declare the shared module.
- `build.gradle.kts` — `testCommitHygiene` also declares the definition its live case reads.
- `docs/ideas.md` — the implemented idea is removed.
- `showcase/quality/agent-skills` gains a `MODIFIED` requirement: a label no normalization yields a class for is
  reported as unmapped.
- `showcase/quality/agent-skills` states the unreadable list as unestablished, with the block still printed.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills` — the experience-analysis requirement's normalization clause widens from a
  pre-canonical label to a label no normalization yields a class for, and states the unreadable-list outcome.

## Impact

- Scripts: `scripts/review_vocabulary.py` (new), `scripts/review-rounds.py`, `scripts/commit-hygiene.py` (imports only),
  their tests. Build: the three task input lists above.
- Docs: `docs/ideas.md`; no `AGENTS.md`, `README.md`, or ADR edit is owed (none states the digest's normalization).
- No runtime, API, image, chart, or deployment change. `check` covers it: the digest's tests and the gate's run in the
  Docker-free form.
