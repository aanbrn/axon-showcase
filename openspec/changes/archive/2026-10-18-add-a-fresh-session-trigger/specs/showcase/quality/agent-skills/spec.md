## MODIFIED Requirements

### Requirement: Per-change quality-gate and analysis subagents are available

The repository SHALL provide locally-defined agent subagents under `.opencode/agent/` for the per-unit quality gates and
analysis workflows — the per-unit review and lesson-capture agents, and the visual and diagram agents — each described
by the scenarios below or by its own requirement in this capability. Each SHALL be invocable by the main agent, with its
purpose described in its agent definition and (where relevant) in `AGENTS.md`.

#### Scenario: Quick review runs after proposal and implementation

- **WHEN** a unit's proposal (planning artifacts) or its implementation is finished
- **THEN** the `review-quick` subagent reviews it against the unit's planning artifacts, and the loop repeats until it
  reports no new observations before a manual review is requested
- **AND** for a unit whose diff adds `AGENTS.md` rules (a capture), it also challenges each new rule's durability — the
  decision the rule governs, and whether a future change would plausibly hit it — so trivia and restatements are caught
  at review time rather than by a later consolidation audit
- **AND** each finding is classified from the canonical list the reviewer's contract carries — each class naming what
  must change, and the calling agent extending the list in the same change when a finding fits none — and when the
  caller's request names a prior round's findings, a finding whose class a prior round already raised is reported as a
  repeat of that class, naming it — so the loop's convergence signal comes from the review's output rather than the
  caller's recollection, and the caller re-derives the root cause on that signal rather than patching the instance
- **AND** a class raised on any round — the first included — is **exhausted**: the review searches the artifact for
  every instance of that class's shape and reports them as one clustered finding, so a class surfaces whole rather than
  one instance per round
- **AND** every round emits one machine-readable record line — the date, the unit, the round (continuous per unit across
  its proposal and implementation reviews), the classes raised (comma-separated; `-` when none), which repeated a prior
  class (comma-separated; `-` when none), and the count of findings that were a compliance miss of an existing rule —
  for the calling agent to append to `docs/review-rounds.tsv`, so the digest can trend the loop's cost and the pre-write
  pass can be evaluated
- **AND** with no prior-round list supplied, the review still classifies each finding and states that a repetition is
  unknown rather than inferring one, since the classification cannot be compared against what it has not seen
- **AND** a defect the applying command rewrites — `./gradlew spotlessApply`, or the web module's `npmFormat` — is
  routed to that command rather than reported as a finding; the boundary is the command's own rewrite, not a category of
  violation, so what it leaves alone — an inline code span's interior, a file type no formatter owns — stays reportable
- **AND** for a record artifact (a retrospective, a load-test record), a later round raising only new instances of an
  already-raised class stops and escalates to the owner rather than iterating — the class-exhaustion clause above
  already required that class whole on the round that raised it, so the escalation names the incomplete exhaustion
  rather than continuing
- **AND** every unit carries the pre-write derivation pass as a record — a task in the change's `tasks.md`, or a line in
  the pull-request checklist for a unit with no change dir — so a skipped pass is a missing box rather than looking like
  a pass that found nothing
- **AND** the pass emits a claims table — each fact the change states about its evidence, the prior artifacts it rests
  on, with its source or the deriving command — so the review reports a claim in the artifacts missing from the table, a
  claimed source that does not reproduce, or a scan candidate neither resolved into a row nor listed as a dismissal,
  rather than re-deriving the class from scratch
- **AND** the pass seeds that table mechanically: a scan of the authored set's added lines (`scripts/claim-scan.py`, run
  through `/seed-claims`) lists each claim shape it finds — a count, an absolute quantifier, a cross-artifact reference,
  a backticked path — with its location, and the author resolves each candidate into a row or records its dismissal
  beside the table (grouped by kind where the dismissal is uniform, and a row's source states the reason by class rather
  than a list of a command's output lines), with the reason, so a claim the author did not notice stating is listed
  rather than waiting for a review round to find it, and the scan's output is accounted for; the table is re-derived
  after the unit's last append to its own record, which adds a candidate to the scan; the scan seeds the table and never
  replaces the derivation, since it enumerates shapes, not facts
- **AND** the caller passes each prior finding's instance (`file:line` and the claim), not only its class, so a repeat
  is reported as an unfixed prior instance or as a new instance of an exhausted class
- **AND** the caller continues the prior round's `review-quick` session — passing its session id on the next request —
  for each round after the first, starting a fresh session only when that one has expired, the reviewer's definition
  changed after it began, or it stops responding mid-round (the loop has observed a session stall with no reply, which
  no wait resolves) — so the reviewer keeps its context across rounds rather than re-exploring from scratch; the reuse
  never substitutes for the per-round instance hand-off above, which the caller supplies on every round regardless of
  the reviewer's session
- **AND** the review checks, in one line each, the proposal's coherence and whether every required behavior is
  represented and its tasks cover the tests and documentation; the implementation's boundary conditions, its changed
  semantics (defaults, failure handling, retries, ordering, concurrency), a test that would still pass with the behavior
  broken, and the unchanged callers, configuration, and documentation it affects; and the conventions (naming, dead
  code, missing mandated Javadoc, documentation still describing superseded behavior, and broken cross-references) — so
  the pass names what to look at without becoming a thorough review
- **AND** a finding carries the evidence its class needs — an enumeration against the corpus it summarizes, a
  contradiction against both statements, a missing scenario against the behavior and the delta that omits it, a
  `test-control` against the expected behavior and the failure it should detect, an `impact-omission` against the
  affected site — so a suspicion the evidence cannot establish is not reported as a confirmed defect
- **AND** the report is ordered by practical importance — incorrect behavior and broken contracts; missing required
  behavior, scenarios, or records; contradictions between the implementation, the plan, and the evidence; unsupported
  claims and invalid premises; documentation and rule violations; formatting last — the order guiding presentation, not
  what is reported
- **AND** each finding names its class, its location, the issue, one line of evidence, and the required correction
- **AND** a defect a prior round's fix only partially corrected, moved, or rewrote is still a recurrence of the same
  problem, reported as such

#### Scenario: A skipped pre-write pass is a missing record

- **WHEN** a unit's planning artifacts are finished and the review loop is about to start
- **THEN** the unit's record names the pre-write derivation pass it ran — the change's `tasks.md` task, or the
  pull-request checklist line for a unit with no change dir — so a pass that was skipped is reported as that record
  missing or unticked rather than reconstructed from recollection

#### Scenario: Thorough review is available on demand

- **WHEN** a deep review pass is wanted (drift, correctness, architecture, conventions)
- **THEN** the `review-thorough` subagent reviews the change against its delta specs, tasks, and the surrounding code

#### Scenario: Lessons are captured after implementation

- **WHEN** a unit's implementation quick review is clean
- **THEN** the main agent decides from the unit's own evidence whether it shows a candidate lesson, and invokes the
  `lesson-capture` subagent only when it does — otherwise the capture is skipped and the skip recorded with its reason,
  so a skip is a recorded decision rather than a run that found nothing
- **AND** the `lesson-capture` subagent proposes `AGENTS.md` gotchas/conventions from the unit's lessons, which the main
  agent verifies and applies
- **AND** each proposed addition names the existing bullet it extends, or states that no bullet covers it — a new rule
  merges into or replaces one rather than accreting
- **AND** alongside its additions, it reports the rules this unit makes obsolete or redundant — a rule whose mechanism
  the unit removed, or one the unit's new enforcement subsumes — as retirement or replacement candidates, each naming
  the rule, why the unit makes it so, and the retirement or replacement it proposes, as candidates for the main agent
  rather than actions, reported separately from the additions since an addition grows the file and a retirement shrinks
  it
- **AND** each proposed addition passes a promotion gate before it is proposed — true (supported by a check, an
  authoritative source, or repeated observation), actionable, not automatable as a lint/test/CI check at reasonable
  cost, material (it prevents real breakage, risk, wasted work, or review churn), general enough for a class of future
  tasks rather than one file or incident, and high-confidence with a known scope — and a rule that fails the gate is
  routed to a check, a spec, an ADR, or the change dir instead of the always-loaded file
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
- **AND** applying a capture should leave `AGENTS.md` no larger than it was, preferring a merge or a replacement over an
  addition, and any net growth is a justified decision stated with the proposal — not a side effect of accumulating
  prose
- **AND** the capture's verdict states the proposed net `AGENTS.md` delta in lines (additions minus retirements) and its
  authoring-system recommendation count, and the applying agent records the applied delta, so net growth is stated when
  it is decided, alongside the periodic audit's accreted-rule count
- **AND** when a rule's rationale is normative in a spec, the bullet keeps only what a reader needs to act and points at
  the spec — a pointer, not a condensed copy, since `AGENTS.md` is loaded on every invocation while a spec is loaded
  only when its capability is worked on
- **AND** when the specs describe a rule's subject only as an outcome, that mechanism belongs in the capability spec
  only when changing it would change a scenario's outcome — verified against the code before moving it; a mechanism
  whose alternatives yield the same result stays in `AGENTS.md` as internal control flow
- **AND** the control on that growth is the periodic `/audit-agents` pass, whose verdict already reports the
  accreted-rule count, rather than mass deletion to hit a number
- **AND** each proposed addition carries its origin in a greppable `captured: <unit>` marker — the unit, and its PR when
  a merge-time detection found the lesson rather than the implementation capture, since a capture runs once at
  implementation and a merge only detects and asks — so a reader can tell where the rule came from without consulting
  git, and a markdown reflow cannot take it
- **AND** the unit leaves a record the merge-time read can check — the capture's applied net `AGENTS.md` delta, or its
  recorded skip and reason — so neither an unrun capture nor an unrecorded skip reads as a run that found nothing

#### Scenario: A change that obsoletes a rule yields a retirement candidate

- **WHEN** a unit's implementation makes an existing `AGENTS.md` rule obsolete (the mechanism it describes is removed)
  or redundant (the unit's new enforcement subsumes it)
- **THEN** the `lesson-capture` subagent reports a retirement or replacement candidate naming the rule, why the unit
  makes it so, and the retirement or replacement it proposes — a candidate for the main agent, not an action
- **AND** the capture's verdict names the retirement count alongside its durable-proposal count

#### Scenario: A skipped capture is a missing record at the merge

- **WHEN** a unit is prepared for its merge
- **THEN** the merge-time read reports an implementation capture that neither ran nor recorded a skip — the unit's
  record missing or unticked — as unrun rather than reconstructing it from recollection
- **AND** it re-verifies a recorded skip against the merge's own evidence, running the capture there when the skip's
  condition no longer holds

#### Scenario: A lesson whose cause is an authoring mechanism is routed to it

- **WHEN** a unit's lessons trace to the mechanism that produces a technical artifact rather than to the artifact's
  content — a template or schema that encourages the wrong structure, a `config.yaml` rule that is silently dropped, a
  skill that prompts a shape the standard forbids
- **THEN** the `lesson-capture` subagent proposes the smallest correction at that mechanism and reports it as an
  authoring-system recommendation, naming the mechanism, the gap, the proposed change, and why an `AGENTS.md` entry
  would not suffice — separate from its additions and retirements, since it changes how a future artifact is produced
  rather than what an agent reads

#### Scenario: Screenshots are reviewed visually

- **WHEN** the main agent needs to inspect a screenshot, image, or visual UI state (e.g. web-UI styling)
- **THEN** the `vision` subagent reads the image and returns a description, so the text-only main agent can delegate
  visual review

#### Scenario: ASCII diagrams are drawn by the pro-model diagrammer

- **WHEN** an ASCII diagram needs to be created, aligned, or fixed (e.g. the README project tree or a diagram's
  plain-text fallback)
- **THEN** the `diagrammer` subagent renders it with the pro model: it establishes the semantic mapping (which span
  starts and ends where), aligns by character width, and preserves deliberate asymmetry — so the cheap flash main agent
  does not spend effort on ASCII geometry

#### Scenario: Mermaid diagrams are drawn by the pro-model diagrammer

- **WHEN** a Mermaid diagram needs to be created, fixed, or reworked (e.g. a README flow diagram)
- **THEN** the `diagrammer` subagent produces it with the pro model: it chooses the diagram type and constructs and
  keeps the content clear of the host renderer's controls
- **AND** it returns the diagram **source**, so the cheap flash main agent does not spend its own effort on Mermaid's
  rendering pitfalls
