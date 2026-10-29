## Context

The proposal widens `readme-auditor` with a language axis. The evidence that the audit cannot carry a language defect is
the contract itself: the three axes omit language, and the advisory rule sends every prose concern to the advisory
class. The defect class is real and recent — `proofread-readme-language` (#737) landed a proofread pass over `README.md`
whose merged description is "a set of prose corrections (21 hunks against `origin/main`)", and
`correct-readme-mechanism-claims` (#738) corrected three statements about how the system works. A language error found
by either route today reaches the owner only if a human reads the file, because no auditor axis and no gate reports one.

### Claims table

| Claim the design rests on                                                | Source that derives it                                                                                                                                                   | Result                                                                                                                                                                                                             |
| ------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| The audit's axes omit language                                           | `git grep -n "Audit along three axes" 1842b105 -- .opencode/agent/readme-auditor.md`, read at the base revision                                                          | line 26 at the base; the tree now says "four axes"                                                                                                                                                                 |
| A prose concern cannot be a finding today                                | `git show 1842b105:.opencode/agent/readme-auditor.md` lines 69-71 ("Subjective quality is advisory, never a finding") and the pre-change requirement's advisory sentence | both stated advisory-only; the tree now carries the carve-out                                                                                                                                                      |
| The recent unit landed a prose pass, not a defect count                  | `gh pr view 737 --json body`                                                                                                                                             | "21 hunks against `origin/main`"; the `23` the unit's record retracts is not used here                                                                                                                             |
| The behaviour lives in five repository files                             | `git grep -ln "readme-auditor\|/audit-readme\|README audit\|readme audit" -- .opencode AGENTS.md README.md openspec/specs/showcase/quality/agent-skills`                 | `.opencode/agent/readme-auditor.md`, `.opencode/commands/audit-readme.md`, `AGENTS.md`, `README.md`, `openspec/specs/showcase/quality/agent-skills/spec.md`; the tasks add the Purpose refresh inside the last one |
| `.opencode/agent/` and `.opencode/commands/` markdown is formatter-gated | `build.gradle.kts`'s `markdown` target includes `.opencode/agent/**/*.md` and `.opencode/commands/**/*.md`                                                               | gated; no manual 120-character check owed                                                                                                                                                                          |
| A rule-based checker was considered                                      | the owner's session probe of LanguageTool (public API), which is not a repository artifact                                                                               | see D4: no figure is claimed, and the probe's numbers are not used as evidence                                                                                                                                     |

## Goals / Non-Goals

- **Goal**: a provable language error is reported as a defect the owner can act on, with a concrete rewrite.
- **Goal**: the hand-curated protection survives — a preference stays advisory.
- **Goal**: the provable-fault set is stated twice by necessity — the delta owns its normative wording, and the
  definition implements it for the model that reads it at run time. The alignment tasks name the axis in the command, in
  `AGENTS.md`, and in the three `README.md` texts without restating the set; a site that needs the boundary points at
  the definition or at `showcase/quality/agent-skills`.
- **Non-goal**: the auditor edits. The definition forbids it and the fixing unit is unchanged.
- **Non-goal**: a mechanical checker, a spell-check gate, or a scheduled language pass.
- **Non-goal**: any change to how findings are applied, triaged, or split into units.

## Decisions

### D1 — A language error is classified by provability, not by subject

The alternative was to keep language entirely advisory (today's state) or to make all language a finding. The first is
the gap this change exists for. The second makes the auditor a style arbiter on a hand-curated document: a run would
report "this paragraph is long" as a defect.

The boundary is therefore **provable vs debatable**, and a reader can decide it: the defect has an identifiable fault —
the agreement is wrong, the clause has no verb, the article is missing, the word repeats — while a preference or an
unclear intent does not. The line is drawn conservatively on purpose: a grammatical sentence whose first parse misleads
is advisory, not a finding, because "hard to read" is not a fault a rule can name. The delta owns the enumeration of
provable faults; this design points at it.

### D2 — A meaning-changing correction is a routing decision with three outcomes, not a third class

A language fix can change a claim ("each is a `linux/amd64` image" → "each publishes a `linux/amd64` image" reads as
grammar and changes the assertion). Treating it as a third output class would give the auditor a category with one
obligation and no distinct report shape. Instead the requirement states the route, and the route has three outcomes
because a meaning-changing correction does not prove the claim wrong: a source that refutes the README's assertion makes
the site an accuracy finding; an assertion that holds means the meaning-changing correction is **rejected**; an
assertion whose intent cannot be established either way is **advisory**. The check protects the hand-curated text, which
is the point of the existing advisory rule.

### D3 — Retitle the requirement via REMOVED + ADDED rather than MODIFIED

A `MODIFIED` block is matched to the main spec by its verbatim header, and the header names three axes. Retitling inside
a `MODIFIED` block fails `openspec validate --changes`; keeping the header would leave a title that contradicts a body
listing four axes. `AGENTS.md`'s route for this case is `REMOVED` + `ADDED` with a per-obligation destination trace.

### D4 — Widen this auditor rather than add one, and rather than add a tool

The artifact is the same (`README.md`), and a language error sits inside the same property the audit already owns —
whether the human-facing text says what the repository means. `AGENTS.md`'s rule widens an auditor when its artifacts
are coupled and adds one only when the artifact or property is distinct. A separate language auditor would read the same
file twice.

A rule-based checker was the other candidate and is rejected on this repository's material: the owner's session probe
against the public API of one such tool caught a minority of these defects while flagging the project's own product
names, and the figures live nowhere in the repository, so no durable claim rests on them. A tool gate would also have to
admit it under the gate-admission rule and maintain an allowlist of those names. The axis keeps the judgement with the
model that already reads the file.

### D5 — Applying stays outside the auditor

The auditor never edits (its definition, its command, and the spec all state it). The fixing path is unchanged: the main
agent verifies, the owner approves, and a README-only fix is a docs unit under the review gate. The axis changes what
the report may contain, not who acts on it.

## Obligation trace — the REMOVED block to the ADDED block

The old requirement is replaced wholesale, so each obligation is mapped by hand; a group-level mapping would have no row
for a clause that moved inside one.

| Old obligation                                                              | Destination in the ADDED block                                                                                                                                                                                      |
| --------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The subagent exists and audits `README.md` without editing                  | Retained, first sentence                                                                                                                                                                                            |
| Scope is `README.md` only; other documents have owners                      | Retained verbatim                                                                                                                                                                                                   |
| Axis: accuracy / consistency                                                | Retained verbatim                                                                                                                                                                                                   |
| Axis: design-intent fidelity                                                | Retained verbatim                                                                                                                                                                                                   |
| Axis: coverage / experience surfacing                                       | Retained verbatim                                                                                                                                                                                                   |
| Axis: language correctness                                                  | **Added** — a provable error is a finding, with location, the fault, and a minimal rewrite                                                                                                                          |
| Each finding verified against the repository, with location and rewrite     | **Amended** — the evidence rule is split: an assertion is verified against the repository, a language finding is evidenced by the passage and the rule it violates; the location-and-rewrite obligation is retained |
| A language fix that changes a claim                                         | **Added** — three outcomes: a refuted assertion is an accuracy finding, a meaning-changing correction to a holding assertion is rejected, and an unestablishable intent is advisory                                 |
| Subjective quality is advisory, never a defect                              | Retained; the clause now covers preference, structure, redundancy, jargon, and flow, and states that a provable error is the language axis's finding                                                                |
| The subagent proposes without modifying files; the main agent applies       | Retained verbatim                                                                                                                                                                                                   |
| The report follows the shared report contract                               | Retained verbatim                                                                                                                                                                                                   |
| Each item carries the shared per-item budget, owned elsewhere               | Retained verbatim                                                                                                                                                                                                   |
| Scenario: A README audit is produced                                        | Retained under the same name; extended to four axes                                                                                                                                                                 |
| Scenario: A claim is verified against the repository                        | Retained verbatim                                                                                                                                                                                                   |
| Scenario: A human-visible capability missing is reported                    | Retained verbatim                                                                                                                                                                                                   |
| Scenario: Subjective quality is advisory, not a defect                      | Retained under the same name; body gains a wording preference and an unclear intent as conditions, and the requirement body states the carve-out                                                                    |
| Scenario: The auditor does not edit files itself                            | Retained verbatim                                                                                                                                                                                                   |
| Scenario (new): A departure from the README's design intent is reported     | **Added** — the axis the delta's body enumerates now has a scenario of its own                                                                                                                                      |
| Scenario (new): A provable language error is a finding                      | **Added**                                                                                                                                                                                                           |
| Scenario (new): A language correction that would change a claim is verified | **Added**                                                                                                                                                                                                           |

## Risks / Trade-offs

- **A run reports more, and some of it will be argued.** The mitigation is the boundary: a finding must be provable, so
  a report that argues about style has misclassified rather than demonstrated the axis.
- **A model can assert a rule it does not know.** The boundary limits this, and the existing guard stands: findings are
  proposals the owner approves, and a README-only fix goes through the review gate with a manual language pass — the
  only guard that caught a confidently wrong correction in the proofread unit.
- **Duplication across sites.** The axis is named in four documents and the set is stated in two; a site that needs the
  boundary points at the definition or the spec. A sweep that restated the set at each site would drift.
- **The Purpose refresh is deferred to the archive commit**, so the implementation diff reads as prose edits only; the
  change's report says so.
