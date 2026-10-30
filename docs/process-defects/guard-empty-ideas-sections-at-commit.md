# Process defects — guard-empty-ideas-sections-at-commit

### guard-empty-ideas-sections-at-commit#1 — A divergence fixture collided with an unrelated guard refusal

- **Recorded**: 2026-10-30, main agent.
- **Stage**: implementation.
- **Expected**: a test could drive the new refusal end-to-end through `check_staged` to show that the judgment reads the
  index rather than the working tree.
- **Observed**: a fixture that stages one copy and then rewrites the working tree is itself the state the guard's
  staged-then-edited refusal exists for, so `check_staged` refused for a second reason and the assertion on a clean exit
  could not hold. Two tests failed until the judgment was extracted into its own `check_staged_idea_sections` helper,
  which the divergence tests drive directly.
- **Evidence**: two failures in the class's first run, before the extraction; after it, the judgment's tests —
  `test_the_judgment_reads_the_index_and_a_fixed_working_tree_changes_nothing` (the former first failure's successor,
  renamed when the helper was extracted) and `test_an_emptied_working_tree_over_a_staged_copy_carrying_entries_passes` —
  pass.
- **Consequence**: the judgment gained a seam that is also what the guard calls, so the wiring test and the divergence
  tests exercise the same function. No behavior changed.
- **Knowledge boundary**: observed locally; the probe ran against a temporary fixture repository, not this tree.
