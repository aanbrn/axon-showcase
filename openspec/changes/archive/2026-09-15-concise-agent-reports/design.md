## Context

The repo's six report-producing subagents each emit a free-form prose report. Their length is not incidental: each is
asked to verify its claims against the repository, and each interprets that as license to show the verification work.
The capture reports during the `#1892` chain ran to several hundred words apiece, most of it the arbiter narrating
candidates it had verified and set aside, plus alternatives it did not recommend.

The existing instructions do ask for brevity — `lesson-capture` ends "Be concise: report findings as a short bullet
list", the auditors "Lead with the highest-value fixes, and end with a one-line overall assessment". Those lines do not
bind: a report that is correct, verified, and thorough satisfies them while still running long, because nothing bounds
the _shape_ of the output. The failing is one of contract, not of effort.

## Goals / Non-Goals

**Goals**

- A uniform output contract across the six report-producing agents, so a report is skimmable and its verdict is the
  first thing read.
- Preserve the verification depth: the reports' value is the check the agent ran, so the contract bounds the report, not
  the analysis.
- Capture the contract as a spec requirement, so it is a durable property of the tooling and not six divergent
  restatements.

**Non-Goals**

- Trimming any agent's _analysis_ duties (the surfaces an auditor sweeps, the classes a review checks).
- Changing what a report contains in substance (findings, severity, file/line references, suggested rewrites).
- A word-count cap: a hard number is arbitrary across agents whose reports differ in kind (a one-line "nothing durable"
  verdict versus an audit's grouped findings), and invites gaming the count over reporting the truth.

## Decisions

**A verdict line, not a summary paragraph.** Every report opens by stating its outcome — for `lesson-capture`, a count
of durable proposals (or that there are none); for the reviewers, whether anything remains; for the auditors, the
section structure that agent uses (only `architecture-auditor` returns two separated sections; the others return one
severity-grouped list). This is the one line a reader must see, and it lets the main agent act without parsing the body.
`review-quick`'s existing "If everything looks consistent, say so in one line" is the pattern, generalized.

**A per-item budget, not a total.** Each proposed addition, finding, or observation gets a few lines: the item itself,
its anchor (`file:line`), and one line of evidence. A total word-count cap would punish an audit with many genuine
findings and reward omitting one; a per-item budget keeps the report proportional to what it found.

**Coverage in one line each, or one summary line.** The capture arbiter's duty to verify candidates and set them aside
is valuable and stays; the _report_ of it collapses to "checked X, Y, Z — covered" rather than a paragraph apiece. This
is where most of the length was.

**One home for the shape.** A subagent cannot import a shared contract at runtime, so each definition must state it; the
trigger commands, by contrast, can and should defer to the definition rather than restate the grouping. Four commands do
restate it today, and they are edited to defer.

**No alternatives.** The agents propose; the calling agent decides. An "Alternative:" or "you could also…" belongs in a
proposal artifact, not in a report whose reader wants the recommendation. The auditors already describe a single
suggested rewrite per finding — the contract makes that uniform.

**Thorough analysis, brief report — stated explicitly.** Because the naive reading of "be concise" is "do less work",
the contract says the opposite: verify as thoroughly as before, then report in the contract's shape. Without that
sentence the change would trade a cheap report for an unreliable one.

## Risks / Trade-offs

- **Over-trimming → fewer real findings.** Mitigated by the explicit "bounds the report, not the analysis" clause, and
  by budgeting per item rather than in total.
- **Six copies drifting.** Mitigated by the spec requirement and by each definition stating the contract once; where a
  definition already carries a report-shape line, it is rewritten rather than duplicated.
- **A verdict line read as the whole answer.** The verdict is an index, not a substitute — the contract keeps the items
  below it.

## Migration Plan

Not applicable — no behavior, code, or deployment change. The definitions take effect on the next OpenCode reload; the
change ships as agent tooling and is verified by reading the six definitions and the spec, plus a smoke-run of a capture
and an auditor to confirm the shape.

## Open Questions

- Whether `experience-analyzer` (a retrospective, a different report kind) should adopt the same contract. Not included:
  its output is a document with its own shape, not a findings report, and the owner has not asked for it here.
