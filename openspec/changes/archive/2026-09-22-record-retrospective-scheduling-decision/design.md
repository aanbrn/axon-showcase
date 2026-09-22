## Context

The `audit` workflow runs the three repository audits unattended on a weekly schedule. `agent-skills` records why
`readme-auditor` is _not_ among them, but the scheduling requirement never mentions `experience-analyzer`, so its
on-demand-only status is undocumented at the spec level. The question arose when reviewing the audit's precedent and
asking whether the retrospective should be scheduled too.

## Goals / Non-Goals

**Goals:**

- State, in the spec that owns the scheduled set, that the retrospective is on-demand only and why.
- Record the rejected alternatives durably so the question is not re-litigated.

**Non-Goals:**

- Changing `experience-analyzer`'s behavior, its trigger, or the `audit` workflow.
- Adding any workflow, tracker issue, or schedule.

## Decisions

### The retrospective stays on-demand; only the audits are scheduled

The audits are scheduled "so the reconciliation they perform does not depend on a human asking". A retrospective is not
a reconciliation: an audit's findings are **claims about the repository's current state**, so a false one is refuted by
reading the repo, while a retrospective is a **narrative judgment about a period** whose richest input — what went wrong
that no diff captures — exists only in the session that lived it. `experience-analyzer` takes a short context note from
the main agent, and `/retrospective` asks which suggestions to apply; a scheduled run has neither. The existing human
trigger works: `docs/retrospectives/` holds two entries run three days apart, on the judgment that enough had
accumulated.

### Rejected: a scheduled run with a PR-count threshold

Proposed as a middle path (run on a schedule, check the delta since the last retrospective, proceed past a threshold).
Rejected for two reasons. First, the threshold discriminates little at this repo's rate: daily merge counts over the
fortnight to 2026-09-22 ranged from 2 to 33 with **no** quiet day, so a useful threshold fires nearly every run, while a
high one measures a day's throughput rather than the accumulation since the last retrospective. Second, it automates the
decision rather than the trigger: a threshold-fired run still has no session-only context — the input that makes a
retrospective worthwhile — so the human must supply it asynchronously anyway, which is the on-demand flow with extra
steps and a scheduled job.

### Rejected: scheduling the retrospective like an audit

Would produce a thinner artifact than the on-demand runs (no context note) and would oblige a suggestion set every
window whether or not the window warrants one, against the repo's anti-accretion discipline. If a passive trigger is
ever wanted, the shape that fits is a threshold **reminder** — a scheduled check reporting what has accumulated since
the newest retrospective, otherwise silent — not a scheduled retrospective.

### Fold the pointer technique into the growth-discipline clause

Writing the `experience-analyzer` bullet as a rule with a one-line summary and a pointer, rather than the thirteen lines
of duplicated rationale an earlier, uncommitted draft of this change carried, is a worked example of a technique the
growth-discipline clause does not yet name: `AGENTS.md` is loaded on every invocation while a spec is loaded only when
its capability is worked on, so rationale that is normative in a spec is pure overhead in `AGENTS.md`, and summarizing
it there is a second copy that drifts. The rule is "keep what a reader needs to act, and point at the spec; a pointer,
not a condensed copy". Folding the sentence in costs +4 lines now and is the durable technique that keeps future bullets
flat — the file's net growth is recorded in the proposal's Impact.

## Risks / Trade-offs

- **Recording a negative in the spec adds a clause that must not rot.** The clause names a subagent and its trigger; if
  the retrospective is ever scheduled, the clause is the thing to update. The `readme-auditor` clause it sits beside is
  the precedent for that contract.

## Migration Plan

None — documentation only.

## Open Questions

None.
