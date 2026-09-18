- [x] 1.1 Retitle the capture bullet and rewrite its trigger: one capture after a change's implementation, one more
      after a merge only when the merged PR was not itself a capture, plus the detect-and-ask protocol.
- [x] 1.2 Reword the "the merge itself is the trigger" sentence, keeping its "do not skip the subagent on your own
      judgment" half for the first capture.
- [x] 1.3 Update the README's self-learning loop to match.
- [x] 2.1 `spotlessApply` after the final edit, `spotlessCheck`, and `openspec validate --all`.
- [x] 2.2 `review-quick` until clean before asking for the manual pass.
- [x] 2.3 Sweep for other restatements of the retired trigger (grep `after every merge` across `AGENTS.md`, `README.md`,
      and `.opencode/`) and fix them in this change.
- [ ] 2.4 Run one capture after this PR merges — it is a non-capture merge — and report any candidate lesson rather than
      chaining.
