# Tasks

## 1. Apply the reductions

- [x] 1.1 Candidate 1 — in `AGENTS.md`'s Prerequisites, replace the pre-commit guard's check enumeration with a pointer
      to the `git add <dir>` gotcha, stating no count. Verify by reading the line back and confirming it names the
      gotcha and carries no enumeration.
- [x] 1.2 Candidate 2 — in `.opencode/agent/review-quick.md`, collapse the 120-character step to the project's check by
      pointer (the `Formatting` convention's recipe) over the files the formatter does not cover. Verify by reading the
      step back and confirming it keeps the operand (which files, report lines over 120) and drops the restated recipe
      and caveats.
- [x] 1.3 Candidate 2 — in `AGENTS.md`'s "A verdict echo is not a check" gotcha, replace the inlined recipe command with
      a pointer to the `Formatting` convention, keeping the gotcha's own lesson and mechanism (let the exit status carry
      the verdict via a wrapper around the recipe; never emit a canned "clean"; the wrapper fails open on a missing
      file, so the check must hit a known positive). Verify by reading the gotcha back.
- [x] 1.4 Candidate 3 — in `AGENTS.md`'s `retro-mark-captured-rules` gotcha, reduce the marker-placement clause to a
      pointer at the provenance rule above (which states what the pre-commit guard enforces), keeping the range-boundary
      lesson and not over-claiming the guard's coverage. Verify by reading the gotcha back and re-reading
      `commit-hygiene.py`'s `find_misplaced_markers` so the pointer matches what the guard checks.
- [x] 1.5 Candidate 4 — in `AGENTS.md`'s capture bullet, replace the growth-bound clause (condensed from the
      `agent-skills` spec / `lesson-capture.md`) with a pointer, keeping the promotion gate's one-word criteria list (a
      deliberate quick reminder the owner retained), the failed-rule routing, the untrusted-source verification, and the
      applying agent's instruction to record the applied net delta. Verify by reading the bullet back.
- [x] 1.6 Sweep every removed line's distinctive tokens against both the trimmed text and the pointer's target, so no
      imperative or target-less fact is lost. Verify by listing the removed tokens and naming where each survives.

## 2. Docs sweep

- [x] 2.1 In `docs/ideas.md`, remove the applied "Trim the AGENTS.md rules the 2026-10-04 audits found restated" entry
      and reword the "application slot" refinement so it no longer quotes or cross-references the removed entry and its
      evidence reflects that these candidates were applied by a one-off owner-gated unit (the refinement stays parked —
      this change applies the candidates, not the standing mechanism). Verify with `grep -n "Trim the AGENTS.md rules"`
      (absent) and by reading the refinement entry.
- [x] 2.2 Confirm `README.md` needs no change (it also lists the guard's checks, but it is a distinct artifact with its
      own owner, the `readme-auditor`); record the grep. **Evidence:** the README lists the guard's checks in prose —
      `README.md:733` reads "a merge conflict marker, a misplaced … marker … and an `openspec archive` move staged",
      `README.md:734` continues "without its spec sync", and `README.md:559` names the marker check — left as a distinct
      presentation.

## 3. Verification

- [x] 3.1 Run `openspec validate --all` and confirm the corpus validates (the change is `skip_specs`).
- [x] 3.2 After the final edit, run `./gradlew spotlessApply` then `./gradlew spotlessCheck`, and confirm the touched
      markdown is formatter-clean.
- [x] 3.3 Run a `review-quick` pass over the diff (proposal and implementation), repeating until clean, and confirm the
      trim kept every fact a reader must act on.

## 4. Capture

- [x] 4.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task (expect a net reduction). **Outcome:** the capture reported `nothing durable` (0
      additions, 0 retirements — the `task-evidence` recurrence and the other review findings are already covered by
      existing rules), so nothing was applied; the capture's net delta is 0. The unit's own `AGENTS.md` net delta is
      **−3 lines** (38 insertions − 41 deletions, the deletions mostly Prettier reflow of the trimmed paragraphs).

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied (`skip_specs`, so no spec sync).
- Verify the archived result.
