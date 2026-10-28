# Process defects — exempt-merge-commits-from-assignment

### exempt-merge-commits-from-assignment#1 — A probe ran the guard against the worktree instead of the scratch repository

- **Recorded** — 2026-10-28, by the implementing agent.
- **Stage** — implementation.
- **Expected** — the first guard probe of the defect runs `commit-hygiene.py --staged` inside a scratch repository and
  reports that repository's staged set.
- **Observed** — the probe invoked the checker by its absolute path from the scratch repository without `--repo`, so the
  checker read the worktree its own file sits in: it reported this unit's claim path and assignment while the scratch
  repository held its own claim. The output was first read as the checker resolving a foreign repository.
- **Evidence** — from `$TMPDIR/opencode/probe-exempt/repo` the guard printed
  `No commit assignment: the branch 'exempt-merge-commits-from-assignment' is not a change name and /Users/afanasyev/Development/axon-showcase/.git/worktrees/axon-showcase-exempt-merge-commits-from-assignment/axon-showcase-change does not exist`;
  `grep -n '"--repo"' scripts/commit-hygiene.py` shows the default is `REPO_ROOT`, the script's parent. Re-running with
  `--repo .` reported the scratch repository's own refusal.
- **Consequence** — two probes re-run; no repository state changed and no artifact carried the misread result. The
  claims table's probe rows were written from the corrected runs.
- **Knowledge boundary** — observed: the default subject and the misread output. Not checked: whether another scripted
  probe in this repository shares the same default.

- **Decision** — no-change: the wrong-resolution-context rule already requires a reproduction to run from the context
  that fails, and the script's default subject is part of that context.

### exempt-merge-commits-from-assignment#2 — The startup install's pin rewrite reached the review undecided

- **Recorded** — 2026-10-28, by the implementing agent.
- **Stage** — review.
- **Expected** — before the unit is reviewed, `.opencode/package.json` is diffed against `HEAD` and its pin read as the
  startup install rather than a deliberate change, the decision recorded, as `AGENTS.md`'s installer-state bullet
  requires.
- **Observed** — the file carried `@opencode-ai/plugin` `1.19.4` → `1.19.3`, rewritten by the OpenCode startup install
  in this worktree. It stayed modified and undecided through the first quick round, which reported it as an unplanned
  edit in the working tree (`impact-omission`).
- **Evidence** — `git diff -- .opencode/package.json` showed `-    "@opencode-ai/plugin": "1.19.4"` and
  `+    "@opencode-ai/plugin": "1.19.3"`; round 1's report lists the finding, and its record line counts two compliance
  misses for the round.
- **Consequence** — reverted to `HEAD` in the same session before any commit or stage; no artifact or pull request
  carried the pin. One review finding would not have been raised had the decision been taken before the round.
- **Knowledge boundary** — observed: the file state and the round's finding. Not checked: whether the install rewrites
  the pin again the next time this worktree's session starts.

- **Decision** — proposed: applied as the earlier decision moment in the `.opencode/package.json` installer-state bullet
  ([`AGENTS.md`](../../AGENTS.md)), since the rewrite was already in the tree when round 1 read it.

### exempt-merge-commits-from-assignment#3 — A claims-table row asserted a grep's result without running it

- **Recorded** — 2026-10-28, by the implementing agent.
- **Stage** — review.
- **Expected** — each claims-table clause is written from the named command's output, re-run at the base revision.
- **Observed** — the row for `find_foreign_change_paths` ended "the same grep over the tests prints nothing", asserted
  from expectation. At the base revision that grep prints one line, the failed-read case list this unit renames, so the
  table's header claim that every row reproduces was falsified by its own row.
- **Evidence** —
  `git grep -n "find_foreign_change_paths\|staged_change_paths" 7ca39c1a -- scripts/test-commit-hygiene.py` prints
  `1445:            ("staged_change_paths", "--diff-filter=D", commit_hygiene.staged_change_paths),`; round 1 reported
  the row as `claim-unreproducible`.
- **Consequence** — the row now states that one line and what it is; the row's substance (a single judge and a single
  caller) was unaffected, and the review round was spent on the correction.
- **Knowledge boundary** — observed: the clause, the command's output, and the finding. Not checked: whether any other
  row's negative clause is asserted rather than derived — the remaining rows' sources were re-run for this pass.

- **Decision** — no-change: review-gate §1 and the evidence rule already require a claim to be derived from the named
  command's output, re-run at the base revision.
