# Tasks — adopt-spec-delta-role

Workspace: unit=adopt-spec-delta-role; path=/Users/afanasyev/Development/axon-showcase; branch=adopt-spec-delta-role;
writer=ses_e731f42aaffeKQAMVYfJ2KxAYN

Pause: ADR-0025's standing consolidation pause is lifted for this unit by the owner's direction on 2026-11-02 ("давай
unresolved-секцию сделаем").

## 1. The role and the adopters

- [x] 1.1 The `spec-delta` role and its three trace rows in `docs/contracts/process-bindings.md`.
- [x] 1.2 The three audit commands resolve it and carry the missing-binding stop.
- [x] 1.3 ADR-0028's dated amendment.
- [x] 1.4 ADR-0025's dated amendment recording the owner's lift for this unit.

## 2. Verification

- [x] 2.1 The renaming counterfactual and its control; record the tool sequences.
- [x] 2.2 Sweep for the literal and prove the sweep fails when one returns.
- [x] 2.3 `openspec validate --all` passes.

## 3. Delivery

- [x] 3.1 `docs/process-defects/adopt-spec-delta-role.md`.
- [x] 3.2 The review gate's stages through the pre-write pass and the quick loop: the claims table, the loop's rounds as
      `docs/review-rounds.tsv` records them, and the routing outcome (`RECOMMENDED`, `repeated-class`).
- [x] 3.3 The routing decision (`RECOMMENDED`, `repeated-class`) and the deep review the owner resolved to run: one
      pass, `SDR-1` (the misattributed ADR-0026 test) and `SDR-2` (two inferred inputs), both fixed.
- [ ] 3.4 The owner's manual pass.
- [ ] 3.5 Commit, push, open the pull request, wait for `build`, archive, and merge on the owner's delivery request.
