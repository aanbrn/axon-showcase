# Tasks — widen-agents-auditor-to-contracts

Workspace: unit=widen-agents-auditor-to-contracts; path=/Users/afanasyev/Development/axon-showcase;
branch=widen-agents-auditor-to-contracts; writer=ses_e731f42aaffeKQAMVYfJ2KxAYN

## 1. The scope

- [x] 1.1 Add `docs/contracts/` to `.opencode/agent/agents-auditor.md`'s description, intro, scope list, and Method
      read-list.
- [x] 1.2 Add `docs/contracts/` to `AGENTS.md`'s agents-auditor bullet.
- [x] 1.3 Correct the four scope summaries: `README.md`'s agent table row, prose bullet, and slash-command table row,
      and `.opencode/commands/audit-agents.md`'s description.

## 2. The delta and the parked entry

- [x] 2.1 Write the `MODIFIED` block for "Project-owned agent tooling is audited for consistency and conciseness": the
      scope sentence and the "An agent-tooling audit is produced" scenario body add `docs/contracts/`; all 34 scenarios
      are retained in order and none is renamed.
- [x] 2.2 Remove the `docs/ideas.md` entry the change implements.
- [x] 2.3 Refresh the `agent-skills` Purpose to name `docs/contracts/` in the audit scope, applied in the archive
      commit.

## 3. Verification and delivery

- [x] 3.1 `openspec validate --all` passes.
- [x] 3.2 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `spotlessCheck` pass after the final edit.
- [x] 3.3 The unit's process-defect record is written — `docs/process-defects/widen-agents-auditor-to-contracts.md`.
- [x] 3.4 The smoke-run: a seeded `docs/contracts/` contradiction is detected and an explained control is not reported;
      the run's second finding (the dropped "of development activity" qualifier) is fixed.
- [x] 3.5 The review gate: the pre-write pass and claims table, the quick loop, the routing decision, the routed deep
      review when the routing decision requires it, and the manual pass.
- [x] 3.6 Consult the advisor before the manual pass.
- [x] 3.7 Commit, push, open the pull request, wait for `build`, archive, and merge.
