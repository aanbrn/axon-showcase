## MODIFIED Requirements

### Requirement: Process defects are captured during a unit and generalized separately

The repository SHALL separate recording a unit's process defects from generalizing them: the unit's session records what
it observes, and analysis, improvement design, and implementation happen in a separate unit that the owner triggers. The
unit's own review SHALL gate the record's currency, not the analysis, and the generalizing unit SHALL keep the analysis
contract this requirement carries in full. An owner-reported defect SHALL be verified against the episode's evidence
before recording.

#### Scenario: A unit records the process defects it observes

- **WHEN** a unit's session observes a process defect — a rule it missed, an action that failed, wasted work, a misread
  artifact
- **THEN** it appends a record to `docs/process-defects/<unit>.md` when it observes it, before the context is lost
- **AND** the record is self-contained for a reader outside that session: it carries the provenance, the expected action
  and the observed deviation, the evidence, the observed consequence and the recovery made, and the boundary of the
  author's knowledge, whose field set `AGENTS.md`'s recording workflow owns and states once
- **AND** the record does not require a cause diagnosis, a proposed improvement, or an assessment of the defect's cost
- **AND** a unit that observed no process defect states that in its record, and that statement is not a claim that none
  occurred
- **AND** immediate damage is repaired in the unit regardless — a lost file, a broken result, or a wrongly closed
  tracker is not left to a later analysis
- **AND** the record ships with the unit's pull request rather than in the unit's change directory, so it survives the
  change's archive and a later unit can read it
- **AND** a merge appends to that record any defect the merge alone reveals — a non-diff effect such as an agent pull
  request closing its own tracker — since the record outlives the change it describes

#### Scenario: The product unit's gate is the hand-off, not the analysis

- **WHEN** a unit is prepared for the manual review pass at the implementation stage
- **THEN** the review gate requires the unit's record to exist and to reflect what the unit observed, or to state that
  it observed none
- **AND** it requires no analysis of that record, no proposed improvement, and no applied guidance change
- **AND** generalization does not block the unit's review, its archive, or its merge

#### Scenario: Generalization runs as its own unit on merged records

- **WHEN** the owner decides the accumulated records are due for generalization
- **THEN** the on-demand command `/generalize-process-defects` selects the records that carry no `Decision` line and
  hands them, with the existing guidance a candidate would extend, to the `lesson-capture` subagent
- **AND** the calling unit designs, reviews, and implements the approved improvements as a unit of its own, on its own
  branch and working tree from a freshly fetched `origin/main`
- **AND** it consumes records that are already merged, never an unfinished unit's branch or working tree
- **AND** it appends one `Decision` line per record — `no-change` with its reason, `proposed` with a link, or `shipped`
  with a merged pull request's link — so a reader can tell an unanalyzed record from a decided one
- **AND** a link to an open pull request is recorded as `proposed`, never as `shipped`
- **AND** the moment of generalization is the owner's decision, so no schedule, queue, drain guarantee, or automatic
  assignment is implied
- **AND** the generalizing unit goes through the same review gate and the owner's approval as any other unit, and being
  assigned the work grants no self-review or merge authority

#### Scenario: The generalizing unit keeps the analysis contract

- **WHEN** the generalizing unit analyzes the selected records
- **THEN** the owning destinations and their routing are `AGENTS.md`'s generalization workflow's
- **AND** the generalization stage's own consolidation is a later unit
- **AND** it invokes the `lesson-capture` subagent, which proposes each durable lesson as an addition to the artifact
  that owns it — an `AGENTS.md` gotcha or convention, a check/test authoring mode in the `check-and-test-authoring`
  skill, a shipping-procedure correction in the `ship-change` skill, a stage-procedure correction in the `review-gate`
  skill, or a section of the subject pack (`jvm-development`, `jvm-testing`, `gradle-build`, or `deployment-operations`)
  whose subject owns the lesson's decision — or a pair of additions when the lesson has both halves, which the calling
  agent verifies and applies
- **AND** each proposed addition names the existing rule it extends — an `AGENTS.md` bullet, a check/test authoring mode
  already in the skill, a `ship-change` or `review-gate` procedure, or a subject-pack section — or states that none
  covers it, so a new rule merges into or replaces one rather than accreting
- **AND** each proposed addition passes a promotion gate before it is proposed — true (supported by a check, an
  authoritative source, or repeated observation), actionable, not automatable as a lint/test/CI check at reasonable
  cost, material (it prevents real breakage, risk, wasted work, or review churn), general enough for a class of future
  tasks rather than one file or incident, and high-confidence with a known scope — and a rule that fails the gate is
  routed to a check, a spec, an ADR, or the change dir instead of the always-loaded file
- **AND** the gate separates a repeated observation from its cause — a repeated review-class label establishes that
  label's recurrence, not a shared drafting defect, and a remedy resting on a class's recurrence (a section rewrite, or
  a rule generalized from that class) requires the shared cause confirmed from the evidence, with a class whose
  instances have different causes taking the cause-specific shape instead
- **AND** the caller supplies the existing guidance a candidate would extend and the evidence for a claimed recurrence —
  the records, and the review evidence they cite — and where either is unavailable the analysis states the limitation
  and withholds the unsupported candidate
- **AND** before proposing an entry, it traces the lesson to its origin — where the cause is a mechanism that produces
  the artifact (an OpenSpec template or schema, an `openspec/config.yaml` rule, a skill or command that shapes drafting,
  or the authoring standard itself), it proposes the smallest correction at that mechanism and reports it as an
  authoring-system recommendation, separate from its additions and retirements, since it changes how a future artifact
  is produced rather than what an agent reads
- **AND** each proposed addition names the decision its rule governs, and answers whether a future change would
  plausibly hit it and whether the cost of not knowing it is material — a proposal that governs no decision is trivia,
  not a rule, and is not proposed
- **AND** the gate's evidence threshold is two independent occurrences or one severe verified incident with a clear
  preventive action
- **AND** the gate treats the claim's source as part of it — a lesson sourced from untrusted content (a web page, an
  issue or PR comment, tool output, or a file the unit did not author) is verified against the repository before it is
  proposed, never promoted on the source's word
- **AND** applying the analysis should leave `AGENTS.md` no larger than it was, preferring a merge or a replacement over
  an addition, and any net growth is a justified decision stated with the proposal — not a side effect of accumulating
  prose
- **AND** each proposed addition demonstrates the **decision difference** it makes — the action a future agent takes
  under the existing guidance, and the action the proposed text requires or prohibits under an explicit condition
- **AND** that comparison names the existing obligation, its explicit condition, and the different action, so different
  phrasing does not establish a difference
- **AND** that difference carries a repository-backed example of why it matters
- **AND** an addition whose action the existing guidance already determines is reported as **already covered** and does
  not expand `AGENTS.md`
- **AND** an additional incident, example, citation, or provenance marker alone is `already covered`, and the analysis
  records that evidence in the generalization unit's change record rather than expanding the rule's text
- **AND** a partial restatement takes the **split** disposition — the new clause is kept, the restatement dropped
- **AND** it prefers an exact replacement of the existing rule over appending qualifications
- **AND** a replacement or rewrite preserves every valid obligation, warning, exception, and identifier, the established
  scope, and the normative strength, and may correct verified errors or remove established duplication and obsolete
  content
- **AND** the proposed action and its scope are derived from the verified evidence, so a remedy that worked in one place
  is not stated as a universal instruction
- **AND** the incident evidence a rule rests on stays recoverable behind its `captured:` marker rather than in the
  rule's text
- **AND** the analysis's verdict states the proposed net guidance delta in lines (`AGENTS.md` and the packs; additions
  minus retirements) and its authoring-system recommendation count, and the applying agent records the applied delta, so
  net growth is stated when it is decided, alongside the periodic audit's accreted-rule count
- **AND** when a rule's rationale is normative in a spec, the bullet keeps only what a reader needs to act and points at
  the spec — a pointer, not a condensed copy, since `AGENTS.md` is loaded on every invocation while a spec is loaded
  only when its capability is worked on
- **AND** when the specs describe a rule's subject only as an outcome, that mechanism belongs in the capability spec
  only when changing it would change a scenario's outcome — verified against the code before moving it; a mechanism
  whose alternatives yield the same result stays in `AGENTS.md` as internal control flow
- **AND** the control on that growth is the periodic `/audit-agents` pass, whose verdict already reports the
  accreted-rule count, rather than mass deletion to hit a number
- **AND** each proposed addition carries its origin in a greppable `captured: <unit>` marker naming the record's unit,
  so a reader can tell where the rule came from without consulting git, and a markdown reflow cannot take it
- **AND** every analyzed record carries its `Decision` line, so an unanalyzed record is visible as one rather than
  reading as an analysis that found nothing

#### Scenario: A change that obsoletes a rule yields a retirement candidate

- **WHEN** the records the generalizing unit analyzes show that a rule is obsolete (the mechanism it describes is
  removed) or redundant (a new enforcement subsumes it)
- **THEN** the `lesson-capture` subagent reports a retirement or replacement candidate naming the rule, why it is so,
  and the retirement or replacement it proposes — a candidate for the calling agent, not an action
- **AND** it reports those candidates separately from the additions, since an addition grows the file and a retirement
  shrinks it
- **AND** the analysis's verdict names the retirement count alongside its durable-proposal count

#### Scenario: A lesson whose cause is an authoring mechanism is routed to it

- **WHEN** a record's lesson traces to the mechanism that produces a technical artifact rather than to the artifact's
  content — a template or schema that encourages the wrong structure, a `config.yaml` rule that is silently dropped, a
  skill that prompts a shape the standard forbids
- **THEN** the `lesson-capture` subagent proposes the smallest correction at that mechanism and reports it as an
  authoring-system recommendation, naming the mechanism, the gap, the proposed change, and why an `AGENTS.md` entry
  would not suffice — separate from its additions and retirements, since it changes how a future artifact is produced
  rather than what an agent reads

#### Scenario: The process work may be performed by a different person

- **WHEN** the generalizing unit is performed by someone other than the session that recorded the defects
- **THEN** the record's fields, the product unit's gate, and the analysis contract are unchanged — only the assignment
  varies, so one participant and several follow the same obligations
- **AND** the assignment grants no approval, self-review, or merge authority, and the owner remains the approver
- **AND** `AGENTS.md` names the owner as the default generalizer, and a unit's tasking names any other assignee rather
  than a standing registry of roles

#### Scenario: An owner-reported defect is verified before it is recorded

- **WHEN** the owner reports a process defect the unit's session did not notice
- **THEN** the `/flag-process-defect` command resolves the episode the report points at — the named one, or the nearest
  action in the current context
- **AND** it asks one clarifying question rather than guessing when that episode is ambiguous
- **AND** it compares the action the rule or plan in force at the episode required against the sequence of events that
  actually occurred, within that episode and its immediate sources, and does not keep searching until a violation
  appears
- **AND** it reports exactly one outcome: confirmed, refuted by the available evidence, or not establishable from it
- **AND** a confirmed outcome appends a record that names the owner's report as the signal, states as the observed
  action the one established rather than the owner's interpretation, and cites the verified fragment as its evidence
- **AND** the record is appended to the file of the unit the episode belongs to, not to whichever unit the session is
  working in
- **AND** an episode that file already records is extended rather than recorded a second time
- **AND** a refuted outcome names the specific grounding for the refusal and writes no record
- **AND** an unestablished outcome names what was checked and which fragment is missing, and asserts neither the defect
  nor its absence
- **AND** the record carries no `Decision` line
