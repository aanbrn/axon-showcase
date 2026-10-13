# Design

## Context

See `proposal.md` — Why. The evidence is the 2026-10-13 retrospective's central finding (the review loop's cost) and its
S3, parked in `docs/ideas.md` with the mechanism named: `review-quick.md` emits a per-round line, and
`scripts/experience-analysis.sh` aggregates them. A count does occasionally survive as prose in an archived change's
`tasks.md`, but never in a machine-aggregable form and never for a unit with no change dir — the
`derive-retrospective-facts` change had to label its own round count "the session's own count; the rounds are recorded
nowhere" — so the loop's cost cannot trend across windows.

Two properties decide the shape: the record must be **machine-aggregable** (the digest reads it, so a markdown table a
human reads is the wrong medium) and it must **cover units with no change dir** (a docs refresh, a standalone fix), so
it cannot live only in a change's `tasks.md`.

## Goals / Non-Goals

**Goals:**

- Record one line per review round — the date, the unit, the round, the classes, which repeated, and how many findings
  were a compliance miss of an existing rule — so the loop's cost trends across windows.
- Make appending it mechanical for the calling agent: the review emits the line, the caller copies it.

**Non-Goals:**

- Acting on the measurement (a check, a gate, or a threshold that fails a build) — this records; a later change decides
  what the numbers should license.
- Backfilling the pre-existing windows: the rounds are recorded nowhere, so the record starts at the change that lands
  it.
- Parsing the review's prose: the line is a defined field set, not a regex over a narrative.

## Decisions

**A single append-only `docs/review-rounds.tsv`, not a per-unit file.** The digest aggregates across units, and a unit
with no change dir has nowhere else to record; one tab-separated file (header + one row per round) keeps both cases
uniform and the aggregation a one-liner. A `.tsv` is not Spotless-owned, so the formatter cannot rewrap the rows.

**`review-quick` emits the line; the main agent appends it.** The review contract forbids the subagent editing files,
and the caller already owns every artifact change, so the subagent emits a field-defined line as the report's last line
and the caller appends it — one write, no re-derivation, and the line's content is the review's own account of the
round.

**The compliance-miss count is a field, not a heuristic.** The retrospective's central question is how much of the
loop's cost is the agent failing to follow an existing rule versus a genuine defect, and only the review can classify
that as it reads the rules — so the line carries the count, and the digest sums it.

## Risks / Trade-offs

- A row the caller forgets to append is a silent gap → the record's absence is itself visible (a unit whose rounds do
  not appear), and the mitigation is the same as any checklist item: the append is a task in the unit's record.
- A `.tsv` accretes a line per round → it is data, not prose, and the digest reads it in the window; the file's growth
  is bounded by the review rounds themselves.
- The line's fields are a schema the digest and the review must agree on → the delta states the field set canonically,
  and the review definition and the `.tsv` header restate it identically, so a drift is a `contradiction` the review can
  catch.

## Migration Plan

None: the record is new and additive; the header line lands with the first rows.
