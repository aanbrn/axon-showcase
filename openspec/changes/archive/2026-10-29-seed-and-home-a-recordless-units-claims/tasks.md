# Tasks

## 1. The scan

- [x] 1.1 In `scripts/claim-scan.py`, read an untracked file and an included file through one function, and add
      `--include <path>` scanning that file's lines as all-added candidates beside the ordinary diff scan, with its own
      path and 1-based line numbers in the output.
- [x] 1.2 Surface an included file's read failure (missing, unreadable, undecodable) as a non-zero exit naming the path,
      where the untracked scan swallows it, and state the input and the empty-versus-failed rule in the module docstring
      and `main`'s help.
- [x] 1.3 Keep a path-shaped candidate of the included text even when it names a modified path, and keep the default
      scan's filtering unchanged.
- [x] 1.4 Keep the default scan otherwise unchanged: the diff against the base plus the untracked, non-ignored files.

## 2. The controls

- [x] 2.1 A deletion-only diff plus a saved body containing claims: the body's claims appear with its path and 1-based
      line numbers.
- [x] 2.2 An included line naming a path the unit modifies is still a candidate, while the same line inside the ordinary
      diff keeps the default filtering.
- [x] 2.3 `--include` beside a diff scan prints both sets, and an empty included file keeps the diff's candidates.
- [x] 2.4 A missing, unreadable, or undecodable included file exits non-zero and names the path; an empty one prints
      nothing and exits zero; a failing included file leaves the diff's candidates printed before the failure is
      reported.
- [x] 2.6 An untracked file that cannot be read is still swallowed, as it is today, while the same failure on an
      included file surfaces — the asymmetry the two readers must keep.
- [x] 2.5 Mutate the input (drop it; make it replace the diff scan; swallow its read failure; apply the path filter to
      it; surface an untracked file's failure) and confirm 2.1, 2.3, 2.4, 2.2 and 2.6 fail respectively; restore.

## 3. The procedure

- [x] 3.1 In `.opencode/skills/review-gate/SKILL.md`'s pre-write step, state that a unit whose claims-table home is the
      body drafts and saves it from the template before the pass runs, that drafting is not publication, and that a
      change-dir unit needs no early body step; widen the step's "candidates for the unit's diff" phrase to the diff
      plus the included body.
- [x] 3.2 In `.opencode/commands/seed-claims.md`, state the same ordering and that such a unit passes the saved body
      with `--include` beside the ordinary scan.

## 4. The human-facing record

- [x] 4.0 Update `README.md`'s `/seed-claims` row, which says the command scans the unit's diff, to name the saved body
      the scan also accepts.

## 5. The spec delta

- [x] 5.1 Write the `MODIFIED` block for "The quick review loop runs on every unit": copy its description and all its
      scenarios verbatim, and extend the skipped-pre-write scenario with the ordering and the included body.

## 6. Verification

- [x] 6.1 Run the pre-write derivation pass and record its claims table (`design.md`), with `claim-scan.py`'s candidates
      resolved into rows or dismissals beside it.
- [x] 6.2 Run `openspec validate --all`, `./gradlew spotlessApply`, `./gradlew testClaimScan`, and the Docker-free
      `./gradlew check`, and record the results.
- [x] 6.3 Write the unit's process-defect record.
- [x] 6.4 Run the review gate: the quick loop (eight rounds on one reviewer session, the last closing clean) and the
      routing decision — `RECOMMENDED` (`[repeated-class]`), which the owner resolved to **skip** on the technical
      advisor's recommendation: the change is a localized CLI input with controls for the coexistence and failure cases,
      and the consultation found no material uncertainty left; its one condition (the two implemented `docs/ideas.md`
      entries still read "parked") was completed in this unit. The manual pass follows.
