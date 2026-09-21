# Tasks — record why the README audit is not scheduled

## 1. The spec delta

- [x] 1.1 A `MODIFIED` delta to `showcase/quality/agent-skills`'s
      `The on-demand audits are also available on a schedule` stating that the README audit is deliberately on-demand,
      with the accretion-cadence reason, carrying all scenarios of the requirement.

## 2. The docs

- [x] 2.1 `AGENTS.md` — the readme-auditor bullet records that it is on-demand only and why, beside the subagent's
      description.

## 3. Verification

- [x] 3.1 `spotlessCheck` green; no line over 120; `openspec validate --all` passes with the delta.
- [x] 3.2 Confirm the reason is consistent with the requirement's existing purpose clause (the accretion-reconciliation
      cadence) and does not add a behaviour the workflow does not have.
