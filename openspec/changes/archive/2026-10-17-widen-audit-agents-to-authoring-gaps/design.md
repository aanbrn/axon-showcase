# Design

## Context

The `/audit-agents` audit reconciles the project-owned agent tooling along two axes — consistency and conciseness — and
reports three separate classes: the advisory class for an excluded file that contradicts our usage, the accretion class
for meta rules with their origins, and the merge/removal/route candidates. Its structured-writing check is one clause in
the consistency axis: an in-scope passage that violates the Structured Writing Standard is "reported as a `structural`
finding with the restructure" — a location and a rewrite, with no practical impact and no minimality guard. And it never
asks whether the mechanism that **produces** an artifact communicates the standard, so a defect a skill keeps causing is
fixed document by document instead of at its source.

The change rests on that asymmetry and on the task's own statement of it, not on a recorded mis-fire: the audit has no
authoring-system-gap class today (`grep` for the term in the auditor and the spec returns nothing). It complements, and
must not duplicate, the `lesson-capture`'s new routing (`capture-authoring-system-gaps`): the capture routes a
mechanism-caused lesson to its mechanism; the audit finds the gap in that mechanism. Two different subjects — a routing
and a finding.

## Goals / Non-Goals

**Goals:**

- Give the structured-writing check an actionable shape: the passage's practical impact, a minimally invasive
  restructure, and a guard against a style preference or an invented interpretation.
- Add an **authoring-system gap** class: a recurring defect traced to the mechanism that produces the artifact, with the
  smallest correction at that mechanism, bounded by evidence and by the audit's fix scope.

**Non-Goals:**

- No new audit axis. The structured-writing depth and the one-authoritative-definition judgment live in the consistency
  axis; the authoring-system gap class is a fourth reported class, alongside advisory and accretion.
- No widening of the audit's fix scope. The scope stays `AGENTS.md` + the project-authored `.opencode/` files, so an
  in-scope mechanism gets the correction directly, `openspec/config.yaml`'s rules get it routed to the change workflow,
  and a generated OpenSpec instruction file is an advisory item — never a local edit by the audit.
- No duplication of the capture's routing, and no restatement of the Structured Writing Standard: the definition points
  at the standard, and the class references it rather than copying it into the mechanism.

## Decisions

**One class paragraph plus scenarios in the audit requirement; one scenario extended in the structured-writing
requirement.** The authoring-system gap is a class the audit _reports_, so it belongs in the audit requirement's body
beside the advisory and accretion paragraphs, with its own scenarios (traced, one-off-not-a-gap, excluded-mechanism
boundary). The finding's impact and minimality refine the structured-writing requirement's own scenario — that
requirement already owns what a structured-writing finding carries — rather than restating the preservation rule in the
audit requirement.

**The class is bounded by evidence and by scope.** A defect in a single artifact with no established link to the
mechanism that produced it stays a consistency finding; a gap whose mechanism is excluded is an advisory item. This
keeps the class from becoming a route for speculative rules and keeps the auditor inside its provenance partition.

**The definition carries the operational detail, the spec the outcome.** The usability dimensions (requirements,
procedures, conditions and branches, exceptions, verification steps, the observation/hypothesis/cause/resolution
distinction), the one-authoritative-definition judgment, the mechanism investigation, and the ambiguity rule are the
auditor's control flow; the spec states the class and the finding's shape.

**The auditor's describing sites are swept.** `AGENTS.md`'s bullet, `README.md`'s prose and two table rows, the
`/audit-agents` command, the `/apply-audit-reductions` command's out-of-scope classes, and the `docs/ideas.md` trend
entry each enumerate what the audit reports, so each is updated; the frontmatter `description` and the verdict line name
the new class.

**Apply the capture's merges and its one retirement.** This unit's own `lesson-capture` run returned two additions
merging into the "recurring unit type" bullet (a definition's own sections are separate edit passes; and the set's
member grep points at the "the only X" bullet) and one retirement — the verified-false "(the swept-surface set is
enumerated in exactly those four places)" count, which the bullet's own next sentence contradicts and README's auditor
prose exceeds. Its authoring-system recommendation (a single-sourcing clause for `openspec/config.yaml`'s `rules.tasks`)
is parked in `docs/ideas.md`, its fix belonging in the config's rules rather than in another `AGENTS.md` sentence.

## Risks / Trade-offs

- **A judgment, not a gate** → a false authoring-system gap costs the owner a review line, not a broken build; the class
  is reported without severity and is a candidate for the owner's decision.
- **Overlap with the capture's routing** → the two state different subjects (finding the gap vs routing a lesson); a
  future consolidation can merge them if the duplication proves real.
- **A definition edit is served from the pre-change copy until a reload or a new session** → the smoke-run of the edited
  definition is a precondition for the archive, per `AGENTS.md`'s subagent-reload gotcha.

## Migration Plan

None: the change takes effect on the next audit run — the surfaces are the proposal's Impact. A `## Purpose` refresh is
owed at the archive: the capability's Purpose enumerates the auditor's reported classes and now omits the
authoring-system gap class (a delta cannot carry a Purpose).

## Claims table

| Claim                                                                                | Source                                                                                                                                                                                      |
| ------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The audit has no authoring-mechanism adequacy check                                  | `git show HEAD:.opencode/agent/agents-auditor.md` — the two axes and the advisory/accretion classes name no mechanism-adequacy check                                                        |
| The structured-writing check yields a location and a rewrite only                    | `git show HEAD:.opencode/agent/agents-auditor.md` (the structured-writing clause) and the main spec's "Dense prose is restructured on review" scenario                                      |
| The capture routes a mechanism-caused lesson; the audit would find the gap           | the main spec's capture clause ("it proposes the smallest correction at that mechanism and reports it as an authoring-system recommendation")                                               |
| The auditor's fix scope excludes `openspec/config.yaml` and the generated files      | `git show HEAD:.opencode/agent/agents-auditor.md` — the scope block (`AGENTS.md` + project-authored `.opencode/`) and the generated/vendored exclusion                                      |
| `apply-audit-reductions.md` and `docs/ideas.md` enumerate the audit's report classes | `git show HEAD:.opencode/commands/apply-audit-reductions.md` (its out-of-scope classes) and `git show HEAD:docs/ideas.md` (the trend entry's counts)                                        |
| The retired count was false                                                          | `git show HEAD:AGENTS.md` (the "(the swept-surface set is enumerated in exactly those four places)" parenthetical) against `git show HEAD:README.md`'s auditor prose (a further sweep site) |
| The auditor is described in `AGENTS.md`, `README.md`, and its command                | `git show HEAD:AGENTS.md` (the `agents-auditor` bullet), `git show HEAD:README.md` (the audit prose and the two table rows), `git show HEAD:.opencode/commands/audit-agents.md`             |
| The Purpose enumerates the auditor's reported classes                                | the `agent-skills` spec's `## Purpose` — "the meta rules it reports with their origins, and the merge, removal, and route candidates it reports"                                            |

**Dismissals** (scan candidates resolved to neither a row nor a fact, grouped by kind, one reason per kind): **absolute
quantifiers and counts** are modal or structural wordings, not facts about the repository; **the `spec.md` delta's
blocks** are the main spec's requirements copied verbatim, so their quantifiers and cross-references are the
pre-existing spec's text, not claims this unit states; **paths, commands, and cross-references** are name, path, or
command references rather than claims about the repository; **the `README.md` table rows other than `agents-auditor`**
appear in the diff only because widening that cell re-padded the tables (a formatter-owned reflow), and their text is
unchanged; **the `docs/review-rounds.tsv` candidates** are this unit's own appended record rows, a round number and a
class label in a one-line record, not a claim.
