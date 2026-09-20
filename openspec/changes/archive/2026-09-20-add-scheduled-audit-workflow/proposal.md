# Add a scheduled audit workflow

## Why

The three audits — `agents-auditor`, `specs-auditor`, and `architecture-auditor` — run only when a human asks. The
2026-09-19 retrospective measured the cost of that: `AGENTS.md` grew +220 net across seventeen captures against −16 net
across six consolidations, so the prune fires far less often than the accretion it counters. Five parked entries
converged on one mechanism — a scheduled, unattended OpenCode run: the three zero-touch scheduled-auditor variants, the
off-peak cost note, and the upstream-reference watcher. The mechanism is now verified against the action's shipped
source and docs.

## What Changes

- **`.github/workflows/audit.yml`** (new) — a weekly `schedule:` (plus `workflow_dispatch:`) run of
  `anomalyco/opencode/github@latest` with a `prompt` that runs the three audits and reports their findings. Per the
  action's documented schedule contract the output is a branch/PR (there is no issue to comment on), and a PR appears
  only if the agent commits the report — so the prompt writes the findings to a dated file under `docs/audits/` and
  commits it, producing one audit PR rather than an issue.
- **`openspec/changes/add-scheduled-audit-workflow/specs/showcase/quality/agent-skills/spec.md`** — an `ADDED`
  requirement: the audits run on a weekly schedule, unattended, reporting into a single reviewable artifact.
- **`openspec/changes/add-scheduled-audit-workflow/specs/showcase/quality/merge-governance/spec.md`** — an `ADDED`
  requirement for the scheduled audit, since that capability models each scheduled workflow as its own requirement
  (there is no generic observational-workflow requirement to modify).
- **`docs/ideas.md`** — five entries resolved: the three zero-touch scheduled-auditor variants, the
  consolidation-cadence entry, and the convergence entry that tied them together are removed; the off-peak note is
  edited (its cross-reference now names the implemented workflow); and the upstream-reference watcher is **re-parked
  with a corrected premise** (its mechanism is a `gh`-based issue workflow, not an OpenCode schedule run — see the
  design).
- **Build**: none — a new workflow file.
- **Tests**: none; the workflow is observational and never a merge gate.
- **Cost**: one weekly pro-model agent run. The prompt batches the three audits so it is one run, not three, and the
  cadence keys to the audits' own findings rather than being a bare reminder: a clean run prints that and commits
  nothing.
- **Security**: scheduled runs have no actor to permission-check, so the workflow grants `id-token: write` (the action
  authenticates by OIDC by default), `contents: write`, and `pull-requests: write` explicitly — the action's documented
  requirement, and `issues: write` is not needed since the scheduled path does not comment on issues.

## New Capabilities

None.

## Modified Capabilities

- `showcase/quality/agent-skills` — an `ADDED` requirement for the scheduled audit run.
- `showcase/quality/merge-governance` — an `ADDED` requirement for the scheduled audit workflow (observational, never a
  merge gate).
