# Tasks — adopt-process-defect-records

Workspace: unit=adopt-process-defect-records; path=/Users/afanasyev/Development/axon-showcase;
branch=adopt-process-defect-records; writer=ses_e731f42aaffeKQAMVYfJ2KxAYN

Pause: ADR-0025's standing consolidation pause is lifted for this unit by the owner's direction on 2026-11-02 ("давай
делать следующую единицу"), following the boundary record's candidate; the dated amendment records it.

## 1. The decision and the declaration

- [x] 1.1 `docs/adr/0028-bindings-cover-the-process-defect-records.md`, and ADR-0026's dated amendment.
- [x] 1.2 The `process-defect-records` role and six trace rows in `docs/contracts/process-bindings.md`.
- [x] 1.3 ADR-0025's dated amendment for the scoped pause lift.

## 2. The adopting components

- [x] 2.1 Adopt the role in the six components, each keeping its own record selection.

## 3. The spec delta

- [x] 3.1 `MODIFIED` delta: the requirement points at the declaration for the role set.

## 4. Verification

- [x] 4.1 Renaming counterfactual and control; record the tool sequences.
- [x] 4.2 Sweep for the literal and prove the sweep fails when one returns.
- [x] 4.3 `openspec validate --all` passes.

## 5. Delivery

- [x] 5.1 `docs/process-defects/adopt-process-defect-records.md`.
- [ ] 5.2 The review gate: the pre-write pass and claims table, the quick loop, the routing decision, the routed deep
      review when required, the advisor, the manual pass.
- [ ] 5.3 Refresh the `agent-skills` Purpose's "(the six roles …)" to the declaration's role set, applied in the archive
      commit — a delta cannot carry a Purpose.
- [ ] 5.4 Commit, push, open the pull request, wait for `build`, archive, and merge on the owner's delivery request.
