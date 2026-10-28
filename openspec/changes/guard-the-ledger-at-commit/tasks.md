# Tasks

## 1. The guard

- [x] 1.1 In `scripts/commit-hygiene.py`, make `check_staged` run the ledger's duplicate/contiguity check and its
      session-continuity check when the ledger is staged or its deletion/rename is staged (a ledger-scoped query, not
      the change-path helpers), reading the staged blob through `staged_file_text` (an absent blob reads as empty) and
      counting their failures with the guard's own.
- [x] 1.2 Add the append-only check: a staged removal of a data row fails unless that `(unit, round)` remains in the
      staged ledger, so a duplicated row's repair passes and a lost row fails.
- [x] 1.3 Report an unparseable ledger row as a named guard failure, not a traceback.
- [x] 1.4 Extend the hook's header comment to name the ledger checks.

## 2. The controls

- [x] 2.1 A staged ledger with a duplicated `(unit, round)` row → refused, naming the pair.
- [x] 2.2 A staged ledger with a contiguity gap → refused, naming the missing round.
- [x] 2.3 A staged ledger row contradicting the session-continuity rule → refused, naming the row.
- [x] 2.4 A staged diff that removes a unit's only row → refused, naming it.
- [x] 2.5 A staged diff that removes a duplicated row, whose `(unit, round)` remains → passes.
- [x] 2.6 A staged ledger row that cannot be parsed → refused by name, no traceback.
- [x] 2.7 A staged clean append passes.
- [x] 2.8 A commit that does not stage the ledger runs no ledger check, even with a defective working-tree ledger.
- [x] 2.9 Stage a duplicated row, fix the working-tree copy, and confirm the staged duplicate is still named (a
      working-tree read would pass).
- [x] 2.10 Delete or rename away the staged ledger and confirm the removal is refused.
- [x] 2.11 Break the staged gating (run the ledger checks unconditionally) and confirm 2.8 fails; restore it.

## 3. The spec delta

- [x] 3.1 Write the `MODIFIED` block for the pre-commit requirement: copy its description and all 25 scenarios verbatim,
      add the five ledger SHALLs, and add the eight scenarios (duplicate, gap, continuity, removed row, duplicate
      repair, unparseable row, clean append, and a commit that does not stage the ledger).

## 4. The human-facing records

- [x] 4.1 `README.md`'s guard enumeration gains the ledger check and its count.
- [x] 4.2 The `AGENTS.md` `git add <dir>` gotcha's enumeration of the guard's refusals gains it.
- [x] 4.3 Remove the 2026-10-24 `docs/ideas.md` entry this change implements.
- [ ] 4.4 Refresh the `showcase/quality/commit-hygiene` Purpose in the archive commit: a delta cannot carry it, and the
      Purpose attributes the ledger checks to the build alone.

## 5. Verification

- [x] 5.1 Run the pre-write derivation pass and record its claims table (`design.md`).
- [x] 5.2 Run `openspec validate --all`, `./gradlew spotlessApply`, `./gradlew testCommitHygiene`, and the Docker-free
      `./gradlew check`, and record the results.
- [x] 5.3 Write the unit's process-defect record.
- [x] 5.4 Run the review gate: the quick loop (nine rounds, closing clean) and the routing decision (`RECOMMENDED`; the
      owner ran the deep review, whose pass 1 findings and the consultation's predecessor-probe addition were fixed and
      verified in pass 2), then the manual pass (the owner approved shipping).
