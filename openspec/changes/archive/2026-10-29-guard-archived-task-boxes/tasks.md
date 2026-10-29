# Tasks

## 1. The guard

- [x] 1.1 In `scripts/commit-hygiene.py`, add a finder that reads each staged `openspec/changes/archive/**/tasks.md`
      through `staged_file_text` and returns its unticked task lines, with the file and the line; select by the
      `tasks.md` basename only, and skip a path whose index blob equals a merged revision's blob for it.
- [x] 1.2 Call it from `check_staged` beside the archive-sync check, counting each file and task line as one failure and
      naming both.
- [x] 1.3 Extend the hook's header comment to name the archived-tasks refusal.

## 2. The controls

- [x] 2.1 A staged archived `tasks.md` with an unticked task, including one indented under a heading → refused, naming
      the file and the task.
- [x] 2.2 A staged archived `tasks.md` whose tasks are all ticked → passes.
- [x] 2.3 An archived `tasks.md` with an unticked task that the commit does not stage → not judged.
- [x] 2.4 A task ticked with `X` passes; `- []` (an empty bracket) is unticked and refused; a bracket holding `~` or `-`
      is refused as unsupported; the one-character link `- [ ](url)` is not read as a task — removing the `(` branch
      must fail this control.
- [x] 2.5 Break the staged gating (read the working tree's archive instead) and confirm 2.3 fails; restore it.
- [x] 2.6 Discriminate the indexed content from the working tree's: the staged blob carries an unticked box while the
      worktree copy is ticked → refused with the task diagnostic, and flipping the two (staged ticked, worktree
      unticked) passes; break the blob read for the worktree read and confirm the control fails.
- [x] 2.7 Move a live change directory into the archive in one commit with an unticked task unchanged → refused at its
      archived path, which is the state `openspec archive` produces.
- [x] 2.8 An archived file other than `tasks.md` carrying a `- [ ]` example is staged → passes, proving the basename
      selection.
- [x] 2.9 A merge commit whose staged archived `tasks.md` blob equals a merged revision's blob, carrying an unticked box
      → exempt; the same path edited after the merge (a different index blob) is still refused; a path the merged
      revision does not hold is judged rather than aborting the hook; and path-only membership (comparing names rather
      than blobs) must fail this control.

## 3. The spec delta

- [x] 3.1 Write the `MODIFIED` block for the pre-commit requirement: copy its description and all its scenarios
      verbatim, add the archived-tasks refusal, and add the seven scenarios (unticked refused, unsupported marker
      refused, ticked passes, unstaged not judged, indexed content judged, a link bullet not being a task, a merge
      exemption); the description SHALL state the staged-path selection, the entire indexed content with no working-tree
      substitution, the three checkbox dispositions (empty or whitespace unticked, `x`/`X` ticked, any other content
      unsupported), the task form, and the blob-equality merge exemption.

## 4. The human-facing records

- [x] 4.1 `README.md`'s guard enumeration gains the refusal and its count.
- [x] 4.2 The `AGENTS.md` `git add <dir>` gotcha's enumeration of the guard's refusals gains it.
- [x] 4.3 Remove the 2026-10-19 `docs/ideas.md` entry this change implements.
- [x] 4.4 Mark the suggestion this change ships **Applied** in the two retrospectives that proposed it
      (`docs/retrospectives/2026-10-15.md` S4 row and prose, `docs/retrospectives/2026-10-23.md` S4 row and P2 prose).

## 5. Verification

- [x] 5.1 Run the pre-write derivation pass and record its claims table (`design.md`).
- [x] 5.2 Run `openspec validate --all`, `./gradlew spotlessApply`, `./gradlew testCommitHygiene`, and the Docker-free
      `./gradlew check`, and record the results.
- [x] 5.3 Write the unit's process-defect record, including the workspace recovery this unit performed.
- [x] 5.4 Run the review gate: the quick loop (eleven rounds on one reviewer session from round 2, closing clean on
      round 11, `--expect-clean` green; round 1's caller restart recorded and resolved by the owner), the routing
      decision (`RECOMMENDED` for repeated classes, with the deep pass deferred from the proposal stage to this one by
      the owner), the deep pass (pass 1's `R1` — the two retrospectives still called the guard parked — is
      `FIXED_AND_VERIFIED` by round 11), and the manual pass (the owner approved shipping).
