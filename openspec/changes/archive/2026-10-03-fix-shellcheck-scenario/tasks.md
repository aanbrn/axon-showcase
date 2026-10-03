# Tasks

## 1. The spec correction

- [x] 1.1 Confirm the delta's `MODIFIED` block carries the main spec's requirement description and all four unchanged
      scenarios verbatim, in the main spec's order, with only "Broken run script fails the build" rewritten. Verify with
      `./gradlew spotlessApply spotlessCheck` and `openspec validate --changes` (both pass), then diff the delta against
      `openspec/specs/showcase/quality/code-quality/spec.md` by eye.
- [x] 1.2 Reproduce the corrected scenario against the pinned actionlint with shellcheck disabled
      (`actionlint -shellcheck ""`): an invalid step key exits non-zero with a `syntax-check` error and an undefined
      context variable exits non-zero with an `expression` error, while a `run:` shell-syntax error exits zero (the case
      only shellcheck reports). Record the commands and exit codes on this task; delete any scratch workflow files
      (`git status --porcelain` clean of them).
- [x] 1.3 Remove the implemented idea from `docs/ideas.md` (the 2026-09-30 "Correct the `code-quality` spec's shellcheck
      scenario" entry). Verify `grep -n "shellcheck scenario" docs/ideas.md` returns nothing and the section heading is
      removed if it is left empty.

## 2. Verification

- [x] 2.1 Run `./gradlew spotlessApply spotlessCheck` after the last edit, and `openspec validate --changes`, and
      confirm all pass.
- [x] 2.2 Run the per-unit `lesson-capture` subagent over the change and apply its durable proposals; record the applied
      net `AGENTS.md` delta on this task (expected: none — the gate's shellcheck status is already recorded). Verdict:
      **nothing durable, net 0 lines** — the unit's defect class is already recorded at `AGENTS.md:2147` ("a claim
      patched at the instance is still wrong one level down", whose worked example at `:2144-2146` names this very
      `actionlint + shellcheck` spec scenario), and the actionlint shellcheck status is at `:1882-1883`/`:2144-2145`.
