# Consolidate the agents-auditor's reconciliation findings

## Why

A full `agents-auditor` pass over `AGENTS.md` returned four reconciliation items — two near-duplicate gotchas, one rule
that governs no decision, and one structural item — plus one wording-only trim in the auditor's own definition. They are
the low-risk, no-new-mechanism half of the auditor's standing analyses: pruning it already verified, needing no widened
scope.

## What Changes

- **`AGENTS.md`** — apply the three pruning findings:
  - **Merge (Docker Images ↔ host-state gotcha)**: the Docker Images hold-back paragraph restates the NGINX episode —
    the `could not find label 'io.buildpacks.buildpackage.metadata'` signature, the architecture symptom, the
    `paketo-buildpacks/nginx#1340` reference — that the host-state gotcha below already owns, cross-referring to it _and
    then re-stating it_. Collapse the paragraph to the config-relevant facts, including the "holds a machine working,
    not a repo-wide defect" scoping that appears nowhere else, plus a pointer.
  - **Merge (the two git-commit gotchas)**: the "`git add` sweeps untracked artifacts" gotcha and the "never chain an
    edit to a commit" gotcha converge on the same staged-set remedy. Keep both lessons; in the second, point at the
    first for the _files_ check while keeping its own _content_ check (whether the edit landed), which the first does
    not cover.
  - **Remove (`helmInstallToLocal` tags `"*"` → Helm release order)**: the `tags "*"` clause is a one-off task-flag fact
    that governs no decision, and the `mustInstallAfter`/`mustUninstallAfter` clause restates the existing Helm
    release-order rule. Fold the mechanism name into that rule and delete the bullet.
- **`AGENTS.md`** — the structural finding: the "A check is evidence only once it has been shown to fail" gotcha is the
  file's longest entry. Its seven sub-modes each already carry a bold lead and are distinct anchor-bearing lessons, so
  none is removed. The auditor proposed promoting the block to a `###` subsection; that was tried and **reverted** — a
  mid-`## Gotchas` heading scopes every following gotcha to the subsection (no closing heading returns to `##`), and its
  subtitle duplicated the block's own lead sentence. The block keeps its bullet shape; the promotion is dropped as an
  improvement that does not hold.
- **`.opencode/agent/agents-auditor.md`** — wording only, and only if a concrete repetition exists; the one the audit
  named (a "trigger" restatement) is not one, so this item is dropped unless an implementer finds a real duplicate.

## Impact

- **Build**: none — `AGENTS.md` and one `.opencode/` markdown file, both formatter-gated, so `spotlessCheck` covers the
  result.
- **Tests**: none.
- **Workflow**: each merge preserves every anchor it keeps, and each removal leaves the survivor carrying the mechanism
  it named, so no rule loses its evidence.

## New Capabilities

None.

## Modified Capabilities

None — the definition edit is wording-only and does not change what the audit reports, so the `agent-skills` spec's
description of the `agents-auditor`'s behavior still holds and no delta is owed. This change is `skip_specs`.
