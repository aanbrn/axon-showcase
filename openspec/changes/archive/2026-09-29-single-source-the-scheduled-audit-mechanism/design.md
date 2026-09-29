# Design

## Context

See `proposal.md` — Why. The 2026-09-28 `specs-auditor` found the scheduled-audit mechanism stated in both
`agent-skills` ("The scheduled run SHALL perform the audits through the OpenCode GitHub action's scheduled path — which
requires a `prompt` input, authenticates by OIDC (`id-token: write`), and produces a branch or pull request rather than
an issue — and SHALL batch them into one run…") and `merge-governance` (the workflow "SHALL invoke the audits unattended
through the OpenCode GitHub action's scheduled-run mechanism (a `prompt` input … and a single batched run…)", with the
write scopes), and flagged it for judgment rather than as an automatic defect. The split is legitimate — `agent-skills`
owns which audits are schedulable and which agents stay on-demand; `merge-governance` owns the CI workflow — but the
mechanism detail is duplicated. The same report flagged `merge-governance`'s Purpose as ambiguous: it enumerates the
report-producing scheduled workflows and omits the scheduled e2e, dependency-security, and deployment-smoke runs.

## Goals / Non-Goals

**Goals:**

- Single-source the scheduled-audit mechanism in `merge-governance` (the workflow owner) and leave `agent-skills` a
  pointer, so a mechanism change has one spec site.
- Refresh `merge-governance`'s Purpose to name every scheduled workflow.

**Non-Goals:**

- Merging the two requirements: they live in genuinely different capabilities and each keeps its own subject.
- Changing the mechanism or any workflow.

## Decisions

### Decision: `merge-governance` owns the mechanism; `agent-skills` points at it

`merge-governance` already states the mechanism as the workflow's requirement, so it becomes the single source;
`agent-skills`'s requirement replaces its mechanism clause with a pointer to that requirement and keeps everything it
uniquely owns (the three schedulable audits, the cadence's reconciliation purpose, and the
`readme-auditor`/`experience-analyzer` on-demand rationale).

- **Alternative — keep the mechanism in `agent-skills` and point `merge-governance` at it:** backwards — the workflow
  requirement is the one that constrains the workflow file, and its scenarios exercise the mechanism.
- **Alternative — merge the two requirements into one:** they sit in different capabilities; `agent-skills` owns the
  agent set, `merge-governance` the CI workflow.

### Decision: the Purpose refresh rides the archive commit, recorded as a task

A delta cannot carry a `## Purpose`, and the repo forbids editing the main spec before archive, so the refresh is an
explicit task applied in the archive commit, as the docs-refresh convention prescribes. The refresh drops the
report-producing qualifier and names the scheduled e2e, dependency-security, and deployment-smoke runs alongside the
update checks, the upstream-reference report, and the audits.

- **Alternative — a sentence naming the trio instead of enumerating:** the qualifier is what misleads; naming all the
  scheduled workflows is the smaller, unambiguous edit.

## Risks / Trade-offs

- **`agent-skills`'s pointer must carry what it uniquely owns** → the delta keeps the agent set, the cadence purpose,
  and the on-demand rationale; only the mechanism clause becomes a pointer.
- **A stale Purpose if the archive task is missed** → recorded as its own task (a delta cannot carry it).
