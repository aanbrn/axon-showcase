# Tasks — consolidate-governance-contracts

Workspace: unit=consolidate-governance-contracts; path=/Users/afanasyev/Development/axon-showcase;
branch=consolidate-governance-contracts; writer=ses_e731f42aaffeKQAMVYfJ2KxAYN

## 1. The contracts

- [x] 1.1 Create `docs/contracts/knowledge-authoring.md` with the operational rules (representation, the procedure and
      gotcha shapes, the evidence rules, the validation pass) and its provenance.
- [x] 1.2 Create `docs/contracts/shared-report.md` with the report contract (the verdict line, the per-item budget, the
      verified-and-set-aside summary, the bounded pass, no alternatives, the contract-bounds-the-report note) and its
      provenance.

## 2. The consumers

- [x] 2.1 Point the consumers at the contract files: the eight definitions (`review-quick`, `review-thorough`,
      `lesson-capture`, `agents-auditor`, `specs-auditor`, `architecture-auditor`, `readme-auditor`,
      `technical-advisor`), the three audit commands keep the short name, and `.github/workflows/audit.yml` points at
      the file.
- [x] 2.2 Point `AGENTS.md`'s references at the contract files.

## 3. The delta

- [x] 3.1 Write the `MODIFIED` blocks for the two owner requirements and the six requirements that reference the
      budget's owner (eight blocks, 90 scenarios): the descriptions retarget to the contracts; the scenarios are
      preserved verbatim.
- [ ] 3.2 Record the `agent-skills` Purpose refresh as the archive commit's task: the Purpose SHALL point at the
      contract rather than listing the standard's rules (a delta cannot carry a Purpose).
- [x] 3.3 Park the `agents-auditor` scope gap in `docs/ideas.md`: no audit covers `docs/contracts/` yet, so the contract
      and the spec can drift unmonitored until the auditor's scope widens.

## 4. Verification

- [x] 4.1 `openspec validate --all` passes.
- [x] 4.2 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `spotlessCheck` pass after the final edit.
- [x] 4.3 The unit's process-defect record is written — `docs/process-defects/consolidate-governance-contracts.md`.
- [ ] 4.4 The review gate: the pre-write pass and claims table, the quick loop, the routing decision, the advisor, the
      manual pass.
- [ ] 4.5 Commit, push, open the pull request, wait for `build`, archive with the Purpose refresh, and merge.
