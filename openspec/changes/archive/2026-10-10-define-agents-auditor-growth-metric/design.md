# Design

## Context

See `proposal.md` — Why. The definition belongs in the `agents-auditor` subagent's behavior, so it lands in two coupled
places per the multi-artifact sweep convention: the agent definition (`.opencode/agent/agents-auditor.md`), which the
running subagent reads, and the `agent-skills` spec requirement that describes the audit's accretion class, which is the
durable contract. The `/audit-agents` command is the trigger and names what the audit reports, so it is the third site.

The five existing reports (`docs/audits/2026-09-20.md`, `2026-09-21.md`, `2026-09-28.md`, `2026-10-04.md`,
`2026-10-04-scheduled.md`) are historical records. Their verdict lines read `24`, `16`, `87+`, `4`, and `~71` accreted —
the raw `captured:` marker count in some (`24`; the `89` markers behind `87+`), attributed rules or distinct units in
others (`16`, `~71`), and the representative clusters the report lists in another (`4`, in the report that notes `118`
markers) — so no two are a like-for-like series. These are not to be rewritten — a historical record is corrected by
stating, if anything, what it counted, never by back-filling a new number (the docs-refresh convention's past-tense
rule).

## Goals / Non-Goals

**Goals:**

- One metric, computable by the subagent, that makes the `accreted` figure in the verdict line comparable across
  successive reports, so the value the capture rule's growth control leans on can form a trend.
- A second figure — the growth since the newest prior report — so a level and a delta are both stated.

**Non-Goals:**

- Back-filling or rewriting the existing `docs/audits/*.md` verdict lines to the new metric.
- Adding a build/CI check or a report-extraction tool (the parked "Trend the audit counts across reports" idea is a
  separate unit; this change only unblocks it by fixing the definition).
- Changing what the accretion class is for, or its treatment as non-defect context.

## Decisions

**Metric: the number of distinct meta rule units carrying at least one `captured:` marker.** A _rule unit_ is one
distinct meta rule in the audited files (a bullet or a bold-lead rule and its continuation), counted once however many
markers it carries.

- Alternatives considered and rejected. The **raw `captured:` marker count** — `24` in 2026-09-20, `87+` over `89`
  markers in 2026-09-28, and the `118` markers 2026-10-04 notes — double-counts merged captures: a bullet that absorbed
  several captures carries several markers but is one rule, so the count grows with consolidation as well as accretion,
  precisely backwards for a growth control. The **attributed-rule count** (`16`) and the **distinct-unit count** (`~71`)
  depend on how finely origin groups are drawn, so they measure introductions rather than rules. The **number of items
  the report lists** (`4`) is the report's presentation (representative clusters), not a population, and shrinks as the
  report condenses. Distinct marker-carrying rule units count the accretion the capture loop produces and nothing else.

**The metric is the accretion class's own definition, in the agents-auditor requirement — not the shared report
contract.** The shared contract already requires the verdict to name each class's count; _which_ number a class counts
is that class's definition. Placing the specific metric in the generic contract would either force every report class to
carry an accreted-style metric or bloat the contract with an auditor-specific rule.

**Rules written before the marker convention fall outside the metric, and that is deliberate.** The metric counts
post-convention growth — the accretion the capture loop produces — which is what the growth control bounds. The
pre-convention corpus is a fixed baseline that does not grow; folding it in would require a judgment call with no marker
to anchor it and would make the figure drift as rules are merged. The definition states the exclusion so a reader does
not read the number as the total meta-rule count.

**The growth-since figure names its reference and its fallbacks.** The prior report is the newest file under
`docs/audits/`; the figure counts the metric's rule units added since that report. Where no prior report exists the
report says `none recorded`; where the delta cannot be determined (e.g. a shallow clone with no history to date the
markers), the report says so rather than substituting a number — a fallback must be distinguishable from a measurement.

## Risks / Trade-offs

- **The metric still needs a judgment call (what is one rule unit).** → The definition anchors it to the marker and the
  rule, not to paragraph shape: a bullet carrying several markers is one unit; the count is stated once per report, so a
  boundary disagreement surfaces as a visible shift, not a silent one.
- **A consolidation (merging two marker-carrying rules into one) lowers the count** while the corpus loses nothing. →
  This is the intended direction: the metric exists to reward consolidation, so a fall is the control working.
- **The growth-since figure needs a prior report and history.** → The fallbacks above keep it honest; the figure is
  supplementary to the level, not a replacement for it.

## Migration Plan

The definition takes effect on the next audit run; no report is rewritten and no gate changes, so nothing is deployed or
rolled back. The `/audit-agents` trigger, the definition, and the spec all land in this one change.
