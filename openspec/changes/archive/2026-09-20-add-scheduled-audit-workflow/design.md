# Design — add a scheduled audit workflow

## Context

Five parked entries (`docs/ideas.md`) converged on one mechanism — a scheduled, unattended OpenCode run: the three
zero-touch scheduled-auditor variants, the off-peak cost note, and the upstream-reference watcher. The 2026-09-19
retrospective measured why it matters — the capture/prune imbalance — and the parked notes assumed the mechanism "fires
issues". **Verifying that assumption against the action's shipped source and its own documentation corrected it**, and
the correction changes the design:

- `schedule` **is** a supported trigger: `github.handler.ts` defines `REPO_EVENTS = ["schedule", "workflow_dispatch"]`,
  and the docs confirm a `prompt` input is required for it.
- But a scheduled run **does not fire issues**: the docs state "Output goes to logs and PRs (no issue to comment on)",
  and the handler comments "REPO_EVENTS: no actor/issueId, output to logs/PR only". It checks out an
  `opencode/schedule-<hex>-<timestamp>` branch, runs the agent, and pushes a branch and PR when the agent left changes.
  `issues` is a separate `USER_EVENTS` trigger, not the schedule path.
- Scheduled runs have **no actor to permission-check**, so the workflow must grant `contents: write` and
  `pull-requests: write` explicitly, plus `id-token: write` — the action authenticates by OIDC by default
  (`use_github_token` defaults to `false`), and without `id-token: write` it fails with "Could not fetch an OIDC token"
  (`github.handler.ts`), a requirement its own docs' example and this repo's `opencode.yml` both show.

So the mechanism is **schedule → prompt → agent run → branch/PR**. For the audit riders that is a _better_ fit than an
issue (findings are reviewed then applied through the normal workflow), and for the upstream-reference watcher it is a
_mismatch_ (monitoring external state produces no repo change to open a PR from) — see D3.

## Goals / Non-Goals

**Goals**: one weekly `schedule:` + `workflow_dispatch` workflow that runs the three audits unattended and reports their
findings into one reviewable PR; the spec capture; the rider removals/re-park.

**Non-Goals**: firing issues from the schedule (the mechanism does not support it); a separate workflow per audit (the
whole point of the convergence is one run); replacing the on-demand `/audit-*` commands (they stay).

## Decisions

### D1 — One workflow, one batched prompt, one PR

The three audits are one agent run with a prompt that performs them in sequence, rather than three workflows or three
matrix jobs — the convergence entry's reason, and it keeps the pro-model cost to one run per week. **The output is a PR
only if the agent commits the report**: the scheduled path checks out a fresh `opencode/schedule-<hex>-<timestamp>`
branch each run and opens a PR from that branch's commits, so a report that is merely printed produces no PR (and cannot
"update" a prior run's PR, which lives on a different branch). The prompt therefore writes the report to a dated file
under `docs/audits/` and commits it, and commits nothing when the audits are clean. It runs no Gradle command: the
runner provisions none, and the audit PR is reviewed rather than merged, so its build check is not a gate.

### D2 — The cadence keys to the auditor's accreted count, not a bare schedule

The retrospective's finding is that the _prune_ is too infrequent relative to accretion, and the cadence idea resolved
that the trigger should key to the `agents-auditor`'s own accreted count (its verdict line already reports
`<n> accreted`) rather than a bare Nth capture or a wall-clock cadence. Weekly is the wall-clock floor — the prompt
tells the agent to report `nothing to report` and skip the PR when the audits are clean, so a quiet week costs one run
and no noise, while a week of accretion produces a consolidation PR.

### D3 — The upstream-reference watcher is re-parked, not implemented here

It was one of the five converged entries, but its shape is different: it monitors _external_ state (upstream issue
closures) and has no repository change to open a PR from, so the schedule→PR mechanism does not fit. Its correct shape
is a `gh`-based issue workflow like the four update checks (`gh api` each pinned reference, open or update an issue) —
the mechanism the parked note already half-described. It is re-parked with that corrected premise, and the change does
not implement it.

### D4 — Observational, never a merge gate

The workflow is scheduled and `workflow_dispatch`-only, with no `pull_request`/`push` trigger, mirroring the four update
checks and the nightly `e2e`. It cannot fail a PR.

## Risks / Trade-offs

- **A scheduled agent PR could be noisy.** Mitigated by D2's clean-run skip (a quiet week commits nothing and opens no
  PR) and by the dated report file, which keeps each run's findings self-contained rather than accumulating.
- **The no-actor permission model is a real hazard**: the workflow grants write scopes to a run nobody triggered. It is
  the documented requirement for the mechanism, and the run is limited to opening a PR (the merge still needs a human),
  so the blast radius is a branch and a PR.
- **The prompt is the contract** — it must name the three audits and the report shape, or the run produces unfocused
  output. The tasks treat the prompt as the deliverable to verify by dispatch.
