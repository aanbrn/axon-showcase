# Proposal

## Why

The digest's review-round block misreports the two figures every future window is compared against. Its single
`$1 >= <since>` filter folds the previous window's same-day tail into the printed `window:` line — the 2026-10-15
retrospective measured 130 rounds / 203 compliance misses inclusive against 87 / 145 for the window proper, and had to
subtract the day by hand — and it prints each row's classes as recorded, never normalizing the pre-canonical labels the
record still carries, so the drifted vocabulary goes uncompared: recomputed by hand through the recorded mapping, the
repeat share moved 70% → 74%, meaning the drift had _understated_ the loop's non-convergence. The next retrospective's
whole recommendation is to compare the next window against that baseline, so the numbers must be stated correctly and
comparably by the digest rather than re-derived per retrospective.

## What Changes

- Add `scripts/review-rounds.py` — the digest's review-round aggregation, which prints the window's rows, the
  `since`-inclusive figures and the window-proper figures beside them, and the classes raised tallied with every
  pre-canonical label normalized to the reviewer's canonical vocabulary through the recorded mapping (and a
  pre-canonical label the mapping cannot normalize reported as such rather than passed through silently).
- Change `scripts/experience-analysis.sh` to delegate its review-rounds block to that tool instead of summing inline.
- Add `scripts/test-review-rounds.py` — the tool's tests: the inclusive/window-proper split, the label normalization
  (with an unmapped label), and the mapping's completeness against the record's pre-canonical labels.
- Add the `testReviewRounds` task to `build.gradle.kts` as a `check` member, so the aggregation's tests run in the
  build.
- Change `docs/review-rounds.tsv`'s header to name the aggregator the rows are aggregated by.
- Change `AGENTS.md`: the `check`-member enumeration, the untriggered-`scripts/`-tool examples, the Python
  prerequisite's reasons, and the sentence naming the aggregation's owner are each widened to the new tool.
- Change `README.md`'s prerequisite table: its Python row names the same reasons `AGENTS.md`'s prerequisite list does,
  so it gains the new tool too.
- Remove the implemented 2026-10-17 idea from `docs/ideas.md`.

## Capabilities

### New Capabilities

None — this change adds a mechanism under an existing requirement; no new capability.

### Modified Capabilities

- `showcase/quality/agent-skills`: the "Experience analysis is available to agents" requirement's derived-facts clause
  gains the review-round figures stated for the window proper beside the `since`-inclusive ones, and the classes raised
  normalized to the reviewer's canonical vocabulary.

## Impact

- Affected code: `scripts/review-rounds.py` (new), `scripts/experience-analysis.sh`, `scripts/test-review-rounds.py`
  (new), `build.gradle.kts`, `docs/review-rounds.tsv` (header comment), `AGENTS.md`, `README.md` (its Python
  prerequisite row), `docs/ideas.md`, and the `agent-skills` delta spec.
- Build: `check` gains the `testReviewRounds` member (a Python test task, Docker-free), so `AGENTS.md`'s enumeration of
  the commit-hygiene `check` members gains it. `README.md`'s task list needs no edit: it lists the guard's build-side
  counterparts and the reporter tasks, not the `test*` members.
- Tests: new `scripts/test-review-rounds.py`; no existing test changes.
- Deployment: none — no production code, image, chart, or configuration is touched.
