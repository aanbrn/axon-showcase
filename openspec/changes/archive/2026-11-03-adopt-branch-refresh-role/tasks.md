# Tasks — adopt-branch-refresh-role

Workspace: unit=adopt-branch-refresh-role; path=/Users/afanasyev/Development/axon-showcase;
branch=adopt-branch-refresh-role; writer=ses_e731f42aaffeKQAMVYfJ2KxAYN

Pause: ADR-0025's standing consolidation pause is lifted for this unit by the owner's direction on 2026-11-03
("Продолжай с оставшимся").

## 1. The role and the adopters

- [x] 1.1 The `branch-refresh` role and its trace row in `docs/contracts/process-bindings.md`.
- [x] 1.2 `/sync-branch` resolves it and carries the stop clause.
- [x] 1.3 ADR-0028's and ADR-0025's dated amendments.
- [x] 1.4 The inventory: one row settled by this diff, two reclassified as entry pointers, two left as candidates.

## 2. Verification

- [x] 2.1 The renaming counterfactual and its control; record the tool sequences.
- [x] 2.2 Sweep for the literal: `git grep -c 'Refreshing a branch' ee7bdc58 -- .opencode/` returns 1 and the tree 0.
- [x] 2.3 `openspec validate --all` passes.

## 3. Delivery

- [x] 3.1 `docs/process-defects/adopt-branch-refresh-role.md`.
- [x] 3.2 The review gate's stages through the routing decision: the pre-write pass and claims table, the quick loop
      (`docs/review-rounds.tsv`), the routing outcome (`RECOMMENDED`, `repeated-class`).
- [x] 3.3 The routing decision: the owner resolved to skip the deep review, on the advisor's finding that no question
      remained open; the decision is recorded here rather than inferred.
- [x] 3.4 The owner's manual pass.
- [x] 3.5 Commit, push, open the pull request, wait for `build`, archive, and merge on the owner's delivery request.
