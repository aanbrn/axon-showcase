- [missing-task] openspec/changes/gate-review-handover-section/tasks.md — no reference to the unit record, so
  `python3 scripts/commit-hygiene.py --workspace-records` exits 1 naming
  `docs/units/gate-review-handover-section/unit.md`, and `check` cannot pass; add the reference line.
- [claim-unreproducible] docs/units/gate-review-handover-section/review-handover.md:85 — the completed-checks line
  claims the staged set holds every unit file with no unstaged edit, but `git status --porcelain` shows the Round 1
  section only in the working tree and `--review-ready` exits 1 naming the path; re-stage and make the claim reproduce.
- [test-control] scripts/test-commit-hygiene.py:1203 — the mode-iteration control invokes every `MODE_FLAGS` value with
  no operand, so the value-taking `--handover` exits 2 at the parser before the intended `--expect` assertion; extend
  task 1.2 to supply an operand per parameterized mode and name the parser group, `MODE_FLAGS`, and
  `MODE_TEST_COVERAGE`.
- [missing-scenario] openspec/changes/gate-review-handover-section/specs/showcase/quality/commit-hygiene/spec.md:8 — the
  requirement makes an unreadable file a failure, but no scenario and no task case covers it; add the scenario and its
  test case, with an invalid-UTF-8 control if the read decodes.
- [incomplete-enumeration] openspec/changes/gate-review-handover-section/specs/showcase/quality/commit-hygiene/spec.md:7
  — the predicate leaves undefined the accepted `<unit>` form (empty, separators, traversal, whitespace, an extra
  colon), the round token's canonical form (`Round 30` or `Round 3x` versus round 3), the suffix's separator, and
  whether the mode reads the working tree or the index; pin each and add it to task 1.2.
- [contradiction] openspec/changes/gate-review-handover-section/specs/showcase/quality/commit-hygiene/spec.md:12 — the
  paragraph says a heading copied from an earlier round SHALL pass, while the predicate and the different-round scenario
  fail an unchanged earlier-round heading; state that copied content beneath a matching current-round heading passes.
- [missing-task] openspec/changes/gate-review-handover-section/tasks.md:13 — task 1.3 adds the invocation but not the
  writer-side heading form: `review-gate/SKILL.md` §2 names no heading, and this unit's own round-1 section (`## \`Round
  1\` — proposal quick review`) would be rejected by the delta's plain-text predicate; require §2 to state the accepted
  form.
- [claim-unreproducible] openspec/changes/gate-review-handover-section/design.md:108 — the claims-table row says "the
  file and the preflight use that form", but the preflight names no heading at all; anchor the row to the observed files
  and the corpus's divergence, or make §2 prescribe the form.
- [claim-missing] openspec/changes/gate-review-handover-section/design.md:5 — the Context's prior-artifact facts (the
  preflight, `review-quick.md`'s input contract, the archived change's handling of early hand-overs) have no
  claims-table row, and the archived-change clause misstates it: the early hand-overs stay where recorded rather than
  being moved; add the rows and reword the clause.
- [single-sourcing] docs/units/gate-review-handover-section/unit.md:27 — the checkpoint's Workspace field copies the
  recorded writer `ses_e4add21daffe…` instead of pointing at the `Workspace:` line, which now names the resumed writer
  `ses_e4960ba02ffe…`; replace the copy with a pointer.
- [missing-task] scripts/commit-hygiene.py:1 — the module docstring enumerates every mode and its purpose, but task 1.1
  adds the mode without it, leaving the file's own inventory stale; add the docstring sentence to task 1.1.
