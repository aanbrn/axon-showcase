- [coherence] .opencode/skills/pause-resume-unit/SKILL.md:123 — the resume records the replacement before starting it,
  while the delta starts it from the hand-over and records the row at outcome time
- [missing-scenario] scripts/commit-hygiene.py:1885-1890 — the mode enforces a per-agent opinion that the delta's format
  does not state and no scenario covers
- [test-control] scripts/commit-hygiene.py:1884 — the delta's per-agent scope rule is unenforced and untested; only an
  empty scope fails
- [test-control] scripts/commit-hygiene.py:1077-1110 — the staged block is never exercised: no `check_staged` case
  stages a register with a violation, a removed row, a rewritten row, or an index/tree disagreement
- [test-control] scripts/test-commit-hygiene.py:4612 — three delta scenarios have no exercising case: a stalled/failed
  row carrying a returned identifier, a mismatch row without a returned identifier, and a reason on a non-replacement
  row
- [test-control] scripts/test-commit-hygiene.py:4410 — the layout fixture claims every admitted kind but omits
  `sessions.tsv`
- [formatting] scripts/commit-hygiene.py:1923 — five added lines exceed 120 characters in formatter-unowned files (also
  commit-hygiene.py:1942, test-commit-hygiene.py:4623 and :4674, and git-hooks/pre-commit:9)
  scripts/git-hooks/pre-commit:9 — five added lines exceed 120 characters in formatter-unowned files
