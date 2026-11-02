## Why

The review ledger's class columns decide the retrospective's convergence figures, and the ledger's header requires a row
recorded after the canonical class list landed to carry a class that list names. Nothing verifies it: four such rows
carry labels the list does not name (`marker-placement`, `output-ordering`, `staged-index-stale`, `staging`), so the
digest counts each as a class while its `unmapped` line stays empty, and one finding kind can split across two labels in
the aggregation. `check` validates the ledger's shape — column count, round contiguity, sessions — never its vocabulary.

## What Changes

- `scripts/commit-hygiene.py` gains a `--class-vocabulary` mode: every class in a ledger row dated after the canonical
  boundary SHALL be in the reviewer's canonical class list, and a label an acknowledgement comment names SHALL pass.
- `scripts/test-commit-hygiene.py` gains `ClassVocabularyTests` and the mode's entry in the coverage mapping.
- `build.gradle.kts` registers `verifyReviewClassVocabulary` over the script, the digest script (the boundary's owner),
  the ledger, and the reviewer's definition, and `check` depends on it.
- `docs/review-rounds.tsv`'s header states the check and the class-acknowledgement comment form, and four
  acknowledgement lines record the labels whose round reports were never reconstructed.
- `docs/ideas.md` removes the idea this change implements and parks the digest follow-up its premise surfaces.
- The three retrospectives that carry suggestion S2 mark its disposition, keeping each report's window text.
- `showcase/quality/commit-hygiene` gains an `ADDED` requirement for the check and the acknowledgement form.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/commit-hygiene` — a new requirement: review-round classes come from the reviewer's canonical list.

## Impact

- Scripts: `scripts/commit-hygiene.py`, `scripts/test-commit-hygiene.py`. Build: the `check` block in
  `build.gradle.kts`.
- Records: the ledger's header and four acknowledgement lines, the ideas scratchpad, and the disposition of one
  suggestion across three retrospective reports.
- No runtime code, API, schema, image, chart, or deployment change. The gate reads two tracked files and the ledger, so
  the Docker-free `check` covers it.
