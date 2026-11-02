# Tasks — adopt-heading-roles

Workspace: unit=adopt-heading-roles; path=/Users/afanasyev/Development/axon-showcase; branch=adopt-heading-roles;
writer=ses_e731f42aaffeKQAMVYfJ2KxAYN

Pause: ADR-0025's standing consolidation pause is lifted for this unit by the owner's explicit instruction on
2026-11-02; this change is the recording of that resolution.

## 1. The adoption

- [x] 1.1 Adopt the lifecycle and review-gate roles in the thirteen commands that cite the headings literally, each
      carrying the missing-binding stop.
- [x] 1.2 Update the declaration's consumer trace: thirteen adopter rows, the non-adopter classes without the heading
      literals, and the entry-pointer exception.

## 2. The spec decision

- [x] 2.1 Record why no delta is owed: the requirement is generic and enumerates no component.

## 3. Verification

- [x] 3.1 Run the renaming counterfactual and its control against a constructed target; record each run's tool sequence.
- [x] 3.2 Sweep for remaining heading literals and prove the sweep can fail.
- [x] 3.3 `openspec validate --all` passes.

## 4. Delivery

- [x] 4.1 Write `docs/process-defects/adopt-heading-roles.md`.
- [ ] 4.2 The review gate: the pre-write pass and claims table, the quick loop, the routing decision, the routed deep
      review when required, the advisor, the manual pass.
- [x] 4.3 Remove the implemented idea from `docs/ideas.md`.
- [ ] 4.4 Commit, push, open the pull request, wait for `build`, archive, and merge on the owner's delivery request.
