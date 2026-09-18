- [x] 1.1 Rewrite the capture bullet's trigger in `AGENTS.md`: the implementation capture stays, and the post-merge run
      becomes a detection (read the merge's non-diff effects, report candidates with their bullets, ask before running).
- [x] 1.2 Add the decision test and the recurrence/severity question to that bullet's rubric.
- [x] 1.3 Carry the same trigger and decision test into `.opencode/agent/lesson-capture.md`.
- [x] 1.4 Add the capture worth-check to `.opencode/agent/review-quick.md`.
- [x] 1.5 The delta modifies the per-change-subagents requirement: the capture scenario (trigger + decision test) and
      the quick-review scenario (the worth-check), carrying all five existing scenarios.
- [x] 2.1 `spotlessApply` after the final edit, `spotlessCheck`, and `openspec validate --all`.
- [ ] 2.2 `review-quick` until clean before asking for the manual pass.
- [x] 2.3 Sweep for restatements of the retired post-merge run (`after the PR is merged`, `post-merge capture`) across
      `AGENTS.md`, `README.md`, `.opencode/`, and the spec, and fix them in this change.
- [ ] 3.1 Park the consolidation-cadence idea (an accreted-rules audit after every Nth capture) as its own docs PR.
