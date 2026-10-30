# Process defects — widen-commit-hygiene-test-coverage

### widen-commit-hygiene-test-coverage#1 — A no-op mutation read as a passing control probe

- **Recorded**: 2026-10-30, main agent.
- **Stage**: review.
- **Expected**: the control's known-bad case — an entry pointing at a class whose source never mentions its mode — fails
  the suite.
- **Observed**: the first probe rewrote two mapping lines whole, key included, so the mapping was unchanged and the
  suite stayed green: the mutation had not happened. Re-probed by swapping only the entries' values, the control still
  passed, because it asserted that the class source contained a checker-function name somewhere, not the mode's; a class
  tied to the wrong mode therefore read as covered.
- **Evidence**: the values-only swap failed only after the mode-name assertion was added; before it, the suite reported
  `Ran 247 tests … OK` for the same swap.
- **Consequence**: the review loop repeated the `test-control` class for a second round. Fixed by asserting the mode's
  flag and the checker function's text in the class's own source, and by adding a mode-naming docstring to the two
  classes that lacked one.
- **Knowledge boundary**: the probe ran locally; how the reviewer reached its finding was not examined.

### widen-commit-hygiene-test-coverage#2 — A corrected claim was not swept to its repetition

- **Recorded**: 2026-10-30, main agent.
- **Stage**: review.
- **Expected**: the correction to the fourth scenario's wording — the control asserts that the checker function's text
  appears, not that a call is made — reaches every repetition of that claim, the task list's 2.2 included.
- **Observed**: the edit landed in the delta only; the task list still described the control as asserting that the class
  calls its checker function, and the next review round re-found it as a repeat of the `wording` class.
- **Evidence**: after round 3 the delta described the failure condition as the class not naming the mode or not naming
  its checker function, while the task list described the same condition as a call; the round-5 report names that line.
- **Consequence**: one review round repeated the `wording` class, and the task text was corrected to match the delta. A
  later deep-review pass found the sharper defect the class pointed at — the control asserted the function's name rather
  than a call — so the control now requires the call form, and the requirement, the fourth scenario, the task list, and
  the claims table were moved to that lexical predicate.
- **Knowledge boundary**: the sweep covered this change's artifacts, the process-defect record, and the test module; no
  other file stated the claim.
