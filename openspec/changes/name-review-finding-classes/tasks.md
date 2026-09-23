# Tasks

## 1. Widen the review-quick definition

- [ ] 1.1 In `.opencode/agent/review-quick.md`, add the classification to the review instructions: every finding carries
      a short class label (e.g. `contradiction`, `dropped-content`, `formatting`), so a class can be compared across
      rounds. The repeat mark is a flag on the finding, not a class — a repeated finding keeps its substantive class and
      is additionally marked as a repeat of it.
- [ ] 1.2 State the repeated-class flag and its input: when the caller's request names the prior round's findings, a
      finding whose class a prior round already raised keeps its class and is additionally reported as a repeat of that
      class, naming it; with no prior-round list supplied, the review still classifies each finding and states the
      repetition is unknown rather than inferring one.
- [ ] 1.3 Extend the definition's report contract so the verdict line names the classes the report carries and, when a
      prior round was supplied, which of them repeats it — alongside the existing `<n> findings` / `nothing remains`
      form.
- [ ] 1.4 Verify the definition keeps everything else unchanged (the proposal/implementation/non-OpenSpec instructions,
      the capture-durability challenge, the 120-character check, "do not edit files", and the rest of the report
      contract).

## 2. Align the callers and the artifacts

- [ ] 2.1 Update `AGENTS.md`'s auto-review paragraph: the non-convergence sentence names the reviewer's repeated-class
      signal as the trigger for re-deriving the root cause, so the diagnosis is the reviewer's output rather than the
      caller's recollection. Update the `review-quick` mention it carries; add no new bullet.
- [ ] 2.2 Verify the auto-review paragraph's coherence — the sentence still reads as one argument, its em-dashes pair,
      and the surrounding rules (fix everything the quick review finds; stop only when it comes back clean; a clean
      review is a precondition for the manual pass, not a substitute) are untouched.
- [ ] 2.3 Confirm whether `AGENTS.md`'s separate "review-quick at proposal AND implementation" text or the
      `docs-refresh`-adjacent wording needs the same tweak; grep for the review-loop mentions rather than assuming.
- [ ] 2.4 Confirm `README.md` needs no edit — its quick-review mentions describe the loop's shape, not the report's
      classes. Derive the surface with `grep -niE "quick-review|quick review|auto-review|review-quick" README.md` (the
      narrow command, not a broad `review|class|finding|converg` that returns 25 lines) and judge each match by reading
      it — a match may describe the planning phase's self-review rather than this loop. State the omission in the
      change's report.
- [ ] 2.5 Grep for the report contract's other copies with two searches, since no single phrase reaches both sets. The
      heading search `grep -rln "Report contract" .opencode/agent/` returns the seven definitions (capitalised, bold in
      six, plain in readme-auditor), which carry the contract's sections rather than the phrase `verdict line first`.
      The phrase search `grep -rln "verdict line first" .opencode/commands/` returns the five commands, which restate
      only the verdict-line position. Read both sets: a copy that states only the verdict-line position is not stale,
      since the changed clause is conditional on the caller supplying a prior-round list.
- [ ] 2.6 Read the `showcase/quality/agent-skills` spec's `## Purpose` and decide whether this change falsifies its
      text; record the decision as a task, and if a refresh is owed, apply it in the archive commit (a delta cannot
      carry a `## Purpose`).

## 3. Verification

- [ ] 3.1 Run `openspec validate --changes` and confirm both `MODIFIED` blocks carry every scenario their main-spec
      requirement has, plus the new clauses (the scenario-set equality, not just the new ones).
- [ ] 3.2 Run `./gradlew spotlessApply` after the final edit, then confirm `spotlessCheck` and the PR gate
      (`./gradlew check -PskipITs -Pcoverage.gate.enabled=false`) are green.
- [ ] 3.3 Smoke-test the widened `review-quick` with both controls, supplying a prior-round list: a diff that repeats a
      class the supplied prior round raised (it must flag the repeat and name the class), and a diff raising a fresh
      class (it must not). Also run once with no prior-round list and confirm it classifies findings but says the
      repetition is unknown. Verify each seed's premise against the repository before reading the run, and record which
      branch each run exercised. Note whether the definition is picked up mid-session or needs a reload, and record what
      the run reported about it rather than asserting a mechanism.
- [ ] 3.4 Run `review-quick` over the implementation diff — with the prior round's findings supplied, since the change
      itself is the first user of the flag — and fix its findings, re-running until clean; then request the user's
      manual review pass before committing.
