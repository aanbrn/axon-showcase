## Why

`adopt-process-bindings` moved the review ledger into the declaration and left the lifecycle and review-gate headings
literal in thirteen commands. Every command's "Repository lifecycle" block then carries two strings a target must
rewrite by hand, and those commands are the ones a target runs first: the audits, the ideation flow, the retrospective,
the branch refresh.

The heading roles already exist in `docs/contracts/process-bindings.md`; the commands simply do not use them. The
renaming counterfactual that `process-bindings-contract` recorded left heading resolution unverified, so this change is
also the first evidence that a renamed heading resolves.

## What Changes

- The thirteen commands in `.opencode/commands/` that cite `AGENTS.md`'s "Workflow: Change lifecycle" or "Workflow: The
  review gate" name the declared lifecycle and review-gate roles instead, with the declaration's path as the read route,
  and carry the missing-binding stop the counterfactual showed they lacked.
- `docs/contracts/process-bindings.md`'s consumer trace gains those thirteen adopters, and its non-adopter classes drop
  the heading literals that no longer exist.
- The renaming counterfactual exercises a target whose lifecycle and review-gate headings are renamed, with a control
  that removes the lifecycle binding.
- No spec delta: the `agent-skills` requirement is generic over "an adopting component" and states no adopter set; the
  trace, a contract, owns it. The change's own record states that.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — no requirement's behavior changes. `skip_specs` is recorded with the reason above: the requirement covers the
behavior already and enumerates no component, so a `MODIFIED` block would carry no edit.

## Impact

- `.opencode/commands/apply-audit-reductions.md`, `audit-agents.md`, `audit-architecture.md`, `audit-readme.md`,
  `audit-specs.md`, `diagram.md`, `flag-process-defect.md`, `generalize-process-defects.md`, `gradle-update.md`,
  `ideas.md`, `opsx-tool-update.md`, `retrospective.md`, `sync-branch.md` — each adopts the roles and carries the stop.
- `docs/contracts/process-bindings.md` — thirteen trace rows, the heading-literal class removed, the entry-pointer
  exception.
- `docs/adr/0025-process-core-and-project-substance.md` and `docs/adr/0027-consolidation-is-evidence-bound.md` — one
  dated amendment each, recording the pause lift and the sweep's admission.
- `docs/ideas.md` — the implemented entry and its empty section removed.
- `docs/process-defects/adopt-heading-roles.md` — the wrapped-search defect.
- No runtime code, build, API, schema, image, chart, or deployment change.
