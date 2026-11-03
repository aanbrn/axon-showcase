# Tasks

The unit record — its `Workspace:`, state, and pause checkpoint — is `docs/units/move-process-defect-records/unit.md`.

## 1. The move

- [x] 1.1 Every record moves to `docs/units/<unit>/process-defects.md` with `git mv`, its unit name taken from the
      file's basename, and a unit directory created where the record is its first artifact. The three records that link
      out with `../../` are retargeted to `../../../` in the same commit.
- [x] 1.2 The move is verified before anything is retargeted: 111 records moved, every blob identical apart from the
      three retargeted links (the staged diff reports renames), no unit name lost, and `docs/process-defects/` empty.

## 2. The readers

- [x] 2.1 `scripts/review-routing.py`'s record-artifact class recognizes `docs/units/<unit>/process-defects.md`, and
      `scripts/test-review-routing.py` covers a record-only diff of one.
- [x] 2.2 `scripts/experience-analysis.sh`'s git-log filters select both the retired directory and the new path; a
      record met under both is identified by its unit and counted once; and a record the index does not hold keeps the
      path it was selected under, so a record deleted before the move still names its original path.
      `scripts/test-experience-analysis.sh` covers a touch recorded before the move and a record deleted before it.

## 3. The docs

- [x] 3.1 `docs/contracts/process-bindings.md` names the new path as the `process-defect-records` role value, its
      consumer trace unchanged.
- [x] 3.2 `AGENTS.md`, `.github/PULL_REQUEST_TEMPLATE.md`, `README.md`, `openspec/config.yaml`, and the `agent-skills`
      delta's requirements name the new path; the sweep finds no live document stating the old directory as the
      location.
- [x] 3.3 ADR-0028 gains a dated `## Amendment` naming the location change, with an inline pointer at its Decision's
      `process-defect-records` bullet, the Status left `Accepted`.
- [x] 3.4 The parked idea is removed from `docs/ideas.md`, and the two moved records it cites as evidence elsewhere in
      the file are retargeted to their new paths.

## 4. The gates

- [x] 4.1 The claims table in `design.md` is re-derived after the last edit, and each row reproduces.
- [x] 4.2 `openspec validate --all` passes, and `./gradlew spotlessApply` runs after the final edit.
- [x] 4.3 The Python suites run under the declared floor, and the touched build's `check` passes.
- [x] 4.4 `docs/units/move-process-defect-records/process-defects.md` records what the unit observed.
- [x] 4.5 The proposal-stage quick round ran over the planning artifacts before the implementation screen.
- [x] 4.6 The implementation-stage quick round runs, the routing decision is recorded, any required deep review
      completes, and the owner's manual pass approves the implementation. Quick rounds 1–7 with a clean closing round;
      routing is `REQUIRED` (`infrastructure-or-deployment` on `.github/PULL_REQUEST_TEMPLATE.md`); thorough pass 1
      found `N1` (the boundary loop deduped by path, not unit), `FIXED_AND_VERIFIED` at pass 2, which reports nothing
      remains.
