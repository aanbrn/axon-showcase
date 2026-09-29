# Design

## Context

See `proposal.md` — Why. The capture's "no larger" discipline lives in `AGENTS.md`'s capture bullet and in the
`agent-skills` spec's capture scenario; the capture's verdict (`.opencode/agent/lesson-capture.md`) reports additions
and retirements counts but no size, and the applying agent records only the applied rules' provenance markers, not the
net delta. Growth is therefore observed only by the periodic `/audit-agents` count, after the fact.

## Goals / Non-Goals

**Goals:**

- Make each capture state its proposed net `AGENTS.md` delta and the applying agent record the applied one, so net
  growth is a stated justification the merge-time read can see.

**Non-Goals:**

- Adding a CI line-count gate: the `no larger` bound is a justification the applying agent owes, which the delta makes
  visible — no check can tell a justified 20-line addition from an unjustified one.
- Trending the audit's accreted-rule counts across reports, or carrying audit findings forward — those are separately
  parked (`docs/ideas.md`).

## Decisions

### Decision: measure the net delta in lines, proposed at capture and recorded at apply

The capture's verdict states the proposed net delta (additions minus retirements, in lines of `AGENTS.md`), and the
applying agent records the applied net delta after `spotlessApply`. Lines capture size, which the additions/retirements
item counts already in the verdict do not.

- **Alternative — count items only:** already reported; two captures with one addition each read the same whether the
  clause is one line or twenty.
- **Alternative — a CI check on a line budget:** brittle under markdown reflow and a cap rather than a stated
  justification — the substance is the justification, not the number.

The measure is approximate by design: the proposed count is as authored and the applied count is after Prettier's
reflow, so the two can differ by wrapping. That is acceptable for a visibility measure, and the design records it rather
than pretending the figure is exact.

### Decision: the applied delta rides the capture record

The capture record has three copies — `AGENTS.md`'s capture bullet, `openspec/config.yaml`'s injected `tasks` rule, and
`.github/PULL_REQUEST_TEMPLATE.md`'s checklist line — and the applied delta is recorded on whichever the unit uses: the
change's `tasks.md` task, or beside the pull-request checklist line for a unit with no change dir, so the merge-time
read already reads it and no new surface is added. The checklist line gains room for the delta, which it currently has
no place for, and the `tasks` rule states that the capture task records it.

- **Alternative — a new per-capture file:** no reader, and it duplicates the record.

### Decision: the spec delta refines the capture scenario, not the shared report contract

The net-delta obligation is lesson-capture-specific, so it joins the capture scenario's growth clause as an `AND`; the
shared report contract is about every report-opening verdict and does not gain a lesson-capture-specific field.

- **Alternative — add it to the shared report contract:** over-broad; the audit reports do not measure an `AGENTS.md`
  delta.

## Risks / Trade-offs

- **A line count is imprecise under reflow** → accepted above; the justified-decision requirement is the substance, and
  the count is directional.
- **One more capture output** → small, and it complements the periodic audit's after-the-fact accreted-rule count with a
  figure stated at the moment the growth is decided.
