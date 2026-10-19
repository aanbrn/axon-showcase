# Proposal

## Why

The project-authored command definitions under `.opencode/commands/` are the repository's most-invoked surface, and they
have drifted from the rules the repository states elsewhere. Four of their five apply paths run "spotlessApply, then
re-run `review-quick`" — a fraction of the review gate `AGENTS.md` requires — so an audit's approved findings can ship
without a claims table, a routing decision, a deep review, or a capture. No command states the lifecycle a mutating unit
owes (a branch from a freshly fetched `origin/main`, uncommitted work, an explicit approval before commit or push), and
none carries the reviewer-session preflight the review gate now requires. Two commands assert facts they cannot
reproduce: the Snyk command states an external plan quota and reset schedule, and the Gradle-update command falls back
to a report file a failed run may have left stale.

## What Changes

- `.opencode/commands/` (all seventeen project-authored definitions) — restructured to the repository's authoring
  standard: labeled sections for a complex workflow, one actionable rule per item, conditional branches stated, and
  implementation detail left in the script, agent, skill, or spec that owns it.
- The four audit commands and `/apply-audit-reductions` — report generation separated from applying findings; a no-op
  result stops without creating a unit; approved findings split by delivery route and scope; the full review gate on the
  apply path.
- Every command that can mutate repository files — the lifecycle preflight (its own branch from a freshly fetched
  `origin/main`, unrelated dirty changes preserved, work uncommitted, no commit/push/PR/archive/merge without explicit
  approval), pointing at `AGENTS.md`'s lifecycle and review-gate workflows rather than restating them.
- Every command that invokes `review-quick` — the session-continuity preflight the review gate requires, pointing at the
  procedure that owns it.
- `/seed-claims` — an unavailable base ref or a failed scan reported as a visible failure rather than an empty candidate
  list; `/review-thorough` — an ambiguous target stopping to ask, and the active OpenSpec store honored;
  `/retrospective` — an explicit `since` validated as a date; `/setup-agent-tools` — the owner's confirmation required
  before the global configuration is read or edited and before an installer runs; `/dependency-security-check` — the
  npm-audit task's applied-suppression, violation, and unsuppressed-finding outcomes surfaced, with an expired
  suppression stated as suppressing nothing rather than as an outcome of its own.
- `/dependency-updates` — one Gradle invocation where the task graph supports it, the current run's output only, a task
  failure reported apart from an available update, and the web UI report's own error and exit evidence surfaced.
- `/dependency-security-check` — the volatile Snyk plan, quota, and reset-schedule assertions replaced by operational
  guidance drawn from the command's own output.
- `/gradle-update` — a candidate version derived only from a successfully regenerated report, with no stale-file
  fallback, and the wrapper verified across all four files before success is reported.
- `/opsx-tool-update` — a collision-safe probe name, the probe's own removal, the prior `config.yaml` restored on every
  exit path, each positive control confirmed to have changed its source before its diagnostic is read, and the workflow
  pin inventory derived from `toolingUpdates`' declared checks rather than a hard-coded list.

## New Capabilities

None.

## Modified Capabilities

- `showcase/quality/agent-skills` — the standing reduction unit's review scenario states the full review gate, and three
  requirements are added: an audit's approved findings are delivered as separate units, an audit finding's delivery
  route follows its kind, and a project-authored command definition that mutates files carries the lifecycle preflight.

## Impact

The change edits markdown under `.opencode/commands/` plus the `agent-skills` delta, and touches no code, build file, or
workflow. It changes what the commands require of a unit that applies findings or mutates files — a behavior change the
delta carries — while leaving every command's trigger, its script and subagent authorities, and its report-only or
mutating character as they are.
