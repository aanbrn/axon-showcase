# The shared report contract

One output contract for every subagent that reports findings or a recommendation to the main agent, so a report is
skimmable and its verdict is read first. The capability's outcome and its scenarios live in
`showcase/quality/agent-skills`; this file owns the rules, so a target can carry them without copying the capability
spec.

Provenance: copied from `openspec/specs/showcase/quality/agent-skills/spec.md` at `1e532988`, before the ownership
split. The spec keeps the outcome and the scenarios; this file is the portable contract ADR-0025 §7 names.

## The verdict line

The first line SHALL state the outcome:

- for a findings report — whether anything remains and how many items, naming the count of every class it carries
  (findings, and any advisory or accretion class);
- for the technical advisor — its recommendation and the counts of its observation classes.

When the report is a findings report and the caller's request named a prior round's findings, the first line SHALL also
name which of the report's classes repeat a class the prior round already raised.

A report with nothing to report SHALL say so in that first line rather than in a closing sentence.

## The per-item budget

Each item SHALL name itself, its anchor (`file:line` where applicable), and one line of evidence. The budget is per
item, not a cap on the report's total, so a report with many genuine findings is longer than one with few.

## Verified-and-set-aside candidates

When the subagent has verified a candidate and concluded it is already covered, rejected, or not a defect, it SHALL
report that in one line, or collapse several into one summary line, rather than a paragraph each.

## A bounded pass states its bound

When the subagent's scope bounds the analysis it performs — the quick review's fixed scope and stop condition, for
example — its report SHALL state that bound rather than implying the analysis was unlimited, so a reader does not read a
bounded screen as a thorough pass.

## No alternatives

When the subagent proposes a change or a correction, it SHALL state the recommendation, not a menu of alternatives — the
calling agent makes the choice.

## The contract bounds the report, not the analysis

The contract SHALL bound the report's shape; the analysis a subagent performs is bounded by that subagent's own stated
scope, not by this contract. An auditor or the deep reviewer verifies its claims as thoroughly as its scope requires,
while a bounded screen such as the quick review performs only the pass its scope defines and says so.

## Who follows it

The `showcase/quality/agent-skills` requirement names the components that follow it; a component that loads this
contract names itself.
