# Design

## Context

The `architecture-auditor` audits `docs/adr/` plus the architectural surface for drift, unrecorded intent, and a due
record, and reports in two sections — findings and advisory. Its method says to treat every claim as a hypothesis and
verify it against the repository, and to respect deliberate structures. What it does not say is how to resolve a
disagreement between records: an accepted ADR, the implementation, the spec decomposition, and `AGENTS.md` can each be
the authority for a different claim, and the audit currently asserts a violation without establishing which one governs
— so a stale supersession or a deliberate exception can read as drift. It also allows a dependency edge to be reported
before the boundary rule that forbids it is located, repeats one root cause across both sections, and leaves its
exploration and its cohesion/coupling advisories unbounded. The intent requirement likewise gives no conclusion for a
checkable `Revisit when:` condition whose verification cannot be completed, and states no partly-landed parked-idea
case.

The repository does have an authority precedent to follow rather than invent: ADR-0002 makes the Java
`@ConfigurationProperties` the surface that owns a property's default, and `AGENTS.md` carries the rule that "an
authority rule names the source of truth, not the winning value".

## Goals / Non-Goals

**Goals:**

- Resolve a disagreement between records by authority, and report an unresolvable one as advisory rather than as a
  verified violation.
- Require both sides of a claimed mismatch to be inspected, and a dependency edge to be judged against a recorded rule.
- Report one mismatch once, bound the exploration to stages, and bound the cohesion/coupling advisory.
- Classify a `Revisit when:` condition before reporting it, and distinguish a partly-landed parked idea.

**Non-Goals:**

- No new agent, no new section, no severity, and no scope transfer from the `specs-auditor`, the review loop, or an
  existing gate.
- No precedence rule invented where the repository states none: an unresolvable authority question is an advisory item.
- No general cohesion, coupling, or decomposition review: the advisory is bounded to a concrete inconsistency or a
  meaningful missing decision.

## Decisions

**Two `MODIFIED` requirements, one per subject.** The authority model, the evidence precision, the dedup rule, the
bounded advisory, and the staged investigation refine the audit requirement that owns the two sections; the non-ADR
rationale search, the condition classification, and the partial-landing distinction refine the intent requirement that
owns the sweep. A new requirement would split either subject across two permanent entries.

**The bounded advisory changes a spec'd authorisation, so it is a `MODIFIED` delta, not a definition edit.** The audit
requirement currently authorises "cohesion and coupling, decomposition, and apparent gaps in the ADR set" as advisory
subjects; the change keeps them but requires a concrete inconsistency or a meaningful missing decision, so the audit
stops being a general design review.

**The authority model reports the unresolved case as advisory.** The spec already separates findings from advisory and
forbids treating an advisory as a defect; the change routes a two-record contradiction, a stale supersession, and a
deliberate exception into that section rather than asserting a violation, and forbids inventing a precedence rule.

**The definition carries the operational form; the spec the outcome.** The definition names the stages, the authority
cases, the dedup rule, and the condition classification in its own words; the spec states the obligations and their
scenarios.

**The describing sites are swept.** `AGENTS.md`'s bullet, `README.md`'s prose, and the `/audit-architecture` command
each enumerate what the audit reports — the bullet carrying the routing-relevant rules and pointing at the spec for the
rest, the prose and the command naming the routing-relevant rules — so each is updated. A `## Purpose` refresh **is**
owed: the Purpose names the parked-entry outcome this change widens ("a parked `docs/ideas.md` entry whose work has
landed"), and it lands with the archive commit (a delta cannot carry a Purpose) — the route the three prior
`architecture-auditor` widenings recorded.

**Apply the captures and their routed edits.** This unit's `lesson-capture` runs returned three additions, applied as
`AGENTS.md` clauses — the review gate's step 2, the spec-delta workflow's two Purpose-refresh bullets, and the
boundary-wording failure-handling bullet — plus a widening of the failure-handling self-records bullet, a matching
`openspec/config.yaml` `rules.design` clause, and two routed `docs/ideas.md` edits (the partly-landed deferral idea and
the extended config-rules idea); the second run also parked one idea (the review-loop session trigger) and skipped one
low-confidence retirement. The captures' applied net delta is +12 `AGENTS.md` lines (per-hunk
`git diff -U0 origin/main -- AGENTS.md`: +3, +7, +1, +1, +0); the whole-file delta is 53 insertions / 38 deletions, the
describing-site sweep contributing the remaining +3. The first run also caught the live defect this change fixes: the
Purpose-refresh determination was wrong, so a refresh task is recorded (4.3).

## Risks / Trade-offs

- **A judgment, not a gate** → a misjudged authority or a missed exception costs a report line, not a broken build; the
  audit remains read-only and every item is the owner's decision.
- **The bounded advisory could under-report** → the bound admits any observation that names a concrete inconsistency or
  a meaningful missing decision, so a genuine cohesion problem with an architectural consequence is still reported; the
  risk is a design concern with no such anchor, which the owner's own review of the ADR set can raise.
- **A definition edit is served from the pre-change copy until a reload or a new session** → the smoke-run of the edited
  definition is a precondition for the archive, per `AGENTS.md`'s subagent-reload gotcha.

## Migration Plan

None: the change takes effect on the next audit run — the surfaces are the proposal's Impact. A `## Purpose` refresh is
owed at the archive (the Purpose names the parked-entry outcome the change widens), so the implementing PR's diff
carries no main-spec edit beyond the delta; the refresh lands with the archive commit.

## Claims table

| Claim                                                                                                              | Source                                                                                                                                                                                                                |
| ------------------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The audit's method does not resolve a disagreement between records                                                 | `git show HEAD:.opencode/agent/architecture-auditor.md` — the method (hypothesis, deliberate structures) and the two sections name no authority rule                                                                  |
| The advisory authorises cohesion/coupling without a bound                                                          | the main spec's architecture requirement ("cohesion and coupling, decomposition, and apparent gaps in the ADR set")                                                                                                   |
| The deferral check gives no conclusion for an unverifiable condition, and the parked-idea check no partial landing | the main spec's intent requirement (the `Revisit when:` clause holds a condition against the repository but concludes nothing when the check cannot be completed, and the parked-entry clause states no partial case) |
| The repository has an authority precedent to follow                                                                | ADR-0002 (the `@ConfigurationProperties` owns the default) and `AGENTS.md`'s "An authority rule names the source of truth, not the winning value" bullet                                                              |
| The auditor is described in `AGENTS.md`, `README.md`, and its command                                              | `git show HEAD:AGENTS.md` (the `architecture-auditor` bullet), `git show HEAD:README.md` (the prose and the two table rows), `git show HEAD:.opencode/commands/audit-architecture.md`                                 |
| A `## Purpose` refresh is owed                                                                                     | the `## Purpose` names the parked-entry outcome ("a parked `docs/ideas.md` entry whose work has landed"), which this change widens to fully/partly landed or a different mechanism                                    |
| The main spec already separates findings from advisory and forbids treating an advisory as a defect                | the main spec's architecture requirement (the advisory clause and its no-severity, not-defects rule)                                                                                                                  |
| Three prior `architecture-auditor` widenings recorded the Purpose-refresh route                                    | the archived `widen-architecture-auditor-to-intent-gaps`, `widen-architecture-auditor-to-revisit-triggers`, and `widen-architecture-auditor-to-stale-ideas` task lists                                                |

**Dismissals** (scan candidates resolved to neither a row nor a fact, grouped by kind, one reason per kind): **absolute
quantifiers and counts** are modal or structural wordings, not facts about the repository; **the `spec.md` delta's
copied requirement bodies and retained scenarios** are the main spec's text, so their quantifiers and cross-references
are pre-existing, not claims this unit states; **paths, commands, and cross-references** are name, path, or command
references rather than claims about the repository; **the capture's diff figures** are resolved on the capture record
(task 3.4), not a fact this table states; **a command outcome's count** (the validation total) is the command's result,
reproduced by re-running it, not a claim this table states; **the README table rows** name no member this change widens
(their "a due record" is the class name, not the parked-entry outcome the delta widens), so they need no edit; **the
`docs/review-rounds.tsv` candidates** are this unit's own appended record rows, a round number and a class label in a
one-line record, not a claim.
