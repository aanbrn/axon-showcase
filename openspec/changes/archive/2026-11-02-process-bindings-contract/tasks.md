# Tasks — process-bindings-contract

Workspace: unit=process-bindings-contract; path=/Users/afanasyev/Development/axon-showcase;
branch=process-bindings-contract; writer=ses_e731f42aaffeKQAMVYfJ2KxAYN

## 1. The decision and the declaration

- [x] 1.1 Write `docs/adr/0026-process-bindings-are-declared.md`.
- [x] 1.2 Write `docs/contracts/process-bindings.md` with the six roles, this repository's values, the referenced
      owners, the missing-binding behavior, and what stays explicit.

## 2. The adopting slices

- [x] 2.1 Adopt the bindings in `.opencode/commands/pause-unit.md`, `.opencode/commands/resume-unit.md`, and
      `.opencode/skills/pause-resume-unit/SKILL.md`.
- [x] 2.2 Adopt the review-ledger binding in `.opencode/agent/lesson-capture.md`.
- [x] 2.3 Record the consumer trace in the design: the adopters and the deliberate non-adopters.

## 3. The spec delta

- [x] 3.1 Add the `agent-skills` requirement for the declared bindings: the read routes, the roles, the missing-binding
      stop, the non-adopter, with the valid/renamed/missing/non-adopter scenarios.

## 4. Verification

- [x] 4.1 Run the renaming counterfactual and the missing-binding control against the constructed target; record the
      actual reads and the static sweep.
- [x] 4.2 `openspec validate --all` passes.
- [x] 4.3 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `spotlessCheck` pass after the final edit.

## 5. Delivery

- [x] 5.1 The unit's process-defect record is written — `docs/process-defects/process-bindings-contract.md`.
- [x] 5.2 The review gate: the pre-write pass and claims table, the quick loop, the routing decision, the routed deep
      review when required, the advisor, the manual pass.
- [x] 5.3 Refresh the `agent-skills` Purpose to name the `docs/contracts/process-bindings.md` contract, applied in the
      archive commit.
- [x] 5.4 Commit, push, open the pull request, wait for `build`, archive, and merge on the owner's delivery request.
