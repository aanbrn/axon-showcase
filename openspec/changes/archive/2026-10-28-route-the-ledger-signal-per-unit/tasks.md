# Tasks

## 1. The routing tool

- [x] 1.1 Add `--unit <name>` to `scripts/review-routing.py`, defaulting to `--change` when only that is given, and pass
      the effective identity to `floor` (not `gather`, which owns repository reads).
- [x] 1.2 Validate the identity in `main` before `floor`, so a missing unit reports `REQUIRED` and exits non-zero even
      when a path or block signal would short-circuit; `ledger_signal` takes the unit and raises as the second layer.
- [x] 1.3 Update the module docstring and the argument help to state the two identities.

## 2. The controls

- [x] 2.1 Replace `test_no_change_name_carries_no_signal` with the missing-identity failure case.
- [x] 2.2 Keep the existing helper cases (two repeated rows recommend; one does not; another unit's rows are ignored)
      and adapt them to the renamed parameter.
- [x] 2.3 Add a `--change`-only CLI run whose change unit has two repeated rows, asserting `RECOMMENDED`, its signal,
      and exit 0.
- [x] 2.4 Add a `--unit`-only CLI run in a scratch repository without a change directory, asserting `RECOMMENDED`, the
      `repeated-class` line, and exit 0.
- [x] 2.5 Add a CLI run whose `--change` delta carries no block kind and whose `--unit` names another identity with two
      repeated rows, asserting `RECOMMENDED`, the `repeated-class` line, and exit 0.
- [x] 2.6 Add a CLI run whose `--change` delta carries an `ADDED` block and whose `--unit` names another identity,
      asserting `REQUIRED`, a signal source naming the change's spec, and exit 0.
- [x] 2.7 Add a CLI run with neither identity, asserting the `REQUIRED` failure message and a non-zero exit, and one
      with a `REQUIRED` path signal plus no identity, asserting the failure message and a non-zero exit (no
      short-circuit).

## 3. The caller's guidance

- [x] 3.1 Correct the review-gate skill's routing section: a unit passes `--unit` with the name its ledger rows record
      whenever that differs from its change name, a no-change-dir unit always, and the omission of `--change` is no
      longer described as not a lower floor.
- [x] 3.2 Remove the 2026-10-26 `docs/ideas.md` entry this change implements.

## 4. Verification

- [x] 4.1 Run the pre-write derivation pass and record its claims table (`design.md`, seven rows).
- [x] 4.2 Run `openspec validate --all`, `./gradlew spotlessApply`, `./gradlew testReviewRouting`, and the Docker-free
      `./gradlew check`, and record the results.
- [x] 4.3 Write the unit's process-defect record.
- [x] 4.4 Run the review gate: the quick loop and the routing decision. `RECOMMENDED` on
      `[repeated-class] 4 rows repeat a prior class`; the owner resolved it as **skip, by explicit decision** — the
      effective-unit plumbing, the missing-identity boundary, the change-directory validation and the routing precedence
      carry focused scratch-repository controls, and the reported mutation check shows the controls' sensitivity.
      Residual risk: a caller may supply a valid but incorrect ledger identity, reading another unit's rows or none; the
      skill states the obligation, and this decision is not a completed deep-review pass.
