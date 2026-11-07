# Unit: link-generalization-decision-records

Workspace:
`unit=link-generalization-decision-records; path=/Users/afanasyev/Development/axon-showcase; branch=link-generalization-decision-records; writer=ses_e4bf7cfceffe4T9Gz77PLL5Mfm`

State: closed

A docs unit with no change directory: it repairs the shipped generalization unit
`generalize-process-defects-2026-11-07`. The five `proposed` `Decision` lines that unit appended were merged without the
pull request's link, and the merged unit record claims the link was added before the merge. The unit adds the link,
appends a dated correction to the merged unit's record and hand-over, and records the miss as that unit's process defect
`#3`.

## Applied

- **Five `Decision` lines** — the `#859` link added to each `proposed` line in `assert-paketo-pin-coverage`,
  `guard-recreate-strategy-across-deployments`, `reject-ignored-openspec-delta-sections`, `surface-projector-backlog`,
  and `unit-layout-allowlist`.
- **The merged unit's record and hand-over** — a dated `## Correction` section is appended to each; the shipped
  sentences stand as written.
- **Its process-defect record** — `#3` records the miss, its evidence, and what the follow-up changes.

## Verification

- `grep -rl "proposed: https://github.com/aanbrn/axon-showcase/pull/859" docs/units/*/process-defects.md` lists five
  files.
- `./gradlew spotlessApply` and `check` pass.
- `openspec validate --all` passes.
- `commit-hygiene.py --review-ready` is green.
- The review gate's closing round is run.

**Spec delta** — none owed: `showcase/quality/agent-skills`'s generalization requirement already states that each
record's `Decision` line is `proposed` with a link. The repair aligns the five records to that existing requirement and
changes no capability behavior.
