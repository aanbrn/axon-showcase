## Context

The first retrospective (`docs/retrospectives/2026-09-16.md`) measured the era's meta-work: 121 of 252 merged PR titles
are capture/park/record work, and `AGENTS.md` reached 1534 lines / 101 bullets in five weeks. Its finding names the
capture-after-every-merge rule as the growth engine and the `agents-auditor` as the only brake — and that brake runs on
demand, after the drift exists. Its suggestion 2 is the structural fix: bound growth at the source.

The retrospective also exposed a routing gap while it was being reviewed: applying the suggestion to the subagent
definition inside a docs PR was wrong, because the `agent-skills` spec describes that definition's behavior — yet the
nearest recorded guidance pointed at a docs PR ("an `AGENTS.md`/`README.md`/agent-tooling clarification lands as a docs
PR"). The rule that decides the route was unrecorded.

## Goals / Non-Goals

- **Goal:** the spec and the subagent's own contract require a captured rule to be placed against the corpus — merged
  into an existing bullet or replacing one, or justified as covering new ground.
- **Goal:** the routing rule (a definition edit owes the `agent-skills` delta) is recorded where the decision is made.
- **Non-Goal:** renumbering, restructuring, or pruning `AGENTS.md`'s existing content. This bounds future growth; the
  retrospective applies the one consolidation it recommended, and the auditor remains the tool for the existing corpus.
- **Non-Goal:** changing any other subagent's contract.

## Decisions

**The obligation rides the existing lesson-capture scenario rather than a new requirement.** The scenario already
describes when the subagent runs and what it proposes; the missing piece is the shape of what it proposes. Adding a
requirement would grow the spec corpus for a clause, which is the pattern this change exists to counter.

**The merge test is stated as a duty, not a prohibition.** "Name the bullet it extends, or state that none covers it"
gives the subagent a positive move and keeps the arbiter's evidence obligation — a rule that genuinely covers new ground
is still proposable; it just has to say so.

**A process suggestion's destination widens to include a subagent definition.** The spec and `AGENTS.md` both mapped a
process suggestion to `AGENTS.md` alone, which read as barring a fix like this one, whose whole subject is a subagent
contract. The retrospective's own wording already assumed the wider destination, so the spec catches up to it.

**The routing rule is qualified by whether the spec describes the behavior, not by the file it touches.** An edit to a
definition the `agent-skills` spec describes owes that spec's delta; an edit the spec does not describe — a model-pin
bump, which `switch-flash-agent-to-v4-1` shipped as `skip_specs: true` — is still a change, but owes none. Stated
unqualified, the rule would misroute the next pin bump.

**The routing rule is one clause on the existing subagent/command bullet, not a new gotcha.** The bullet already
presupposes a change dir (it lists the proposal's capability subsections); a reader deciding whether a docs PR may carry
a definition edit never reaches that, so the clause goes first in the bullet — where the decision is made — and the
bullet's existing content is untouched.

## Risks / Trade-offs

- **Risk:** the obligation adds friction to a cheap, frequent pass. Mitigation: it is one sentence in the subagent's
  contract and one clause in `AGENTS.md`, and it replaces work the auditor would otherwise do later at review cost.
- **Risk:** "merge into or replace one" could be read as forbidding genuinely new rules. Mitigation: the duty's second
  branch ("or state that no bullet covers it") explicitly permits them.
- **Trade-off:** the spec now describes a subagent's internal output shape in more detail than before. Accepted: the
  report contract set this precedent (a shared output contract is specified), and the growth it bounds is measurable.

**Purpose refresh: none owed.** The change bounds a subagent's output shape and a suggestion's destination — not which
subagents the capability provides — so no delta carries it and none is needed.

## Migration Plan

Applied as the change's implementation: the definition, the four destination copies, the `AGENTS.md` clauses, and the
README edit commit with the code-free diff; the main spec is synced at archive, per the repo rule that the main spec is
only edited then. No deployment or data concern.

**The widening must be swept, not applied where it was noticed.** Four artifacts restate a process suggestion's
destination (`AGENTS.md`, `experience-analyzer.md`, `retrospective.md`, `README.md`), and a spec edit alone would leave
all four contradicting it — the multi-artifact sweep the subagent/command bullet already mandates, caught in this
change's own proposal review.

## Open Questions

None. The retrospective fixed the scope (its suggestion 2), and the routing clause records the decision its own review
made.
