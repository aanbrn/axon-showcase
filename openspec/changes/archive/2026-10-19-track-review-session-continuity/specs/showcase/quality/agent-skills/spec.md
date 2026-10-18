# Spec Delta

## MODIFIED Requirements

### Requirement: Per-change quality-gate and analysis subagents are available

The repository SHALL provide locally-defined agent subagents under `.opencode/agent/` for the per-unit quality gates and
analysis workflows — the per-unit review and lesson-capture agents, and the visual and diagram agents — each described
by the scenarios below or by its own requirement in this capability. Each SHALL be invocable by the main agent, with its
purpose described in its agent definition and (where relevant) in `AGENTS.md`.

Every item the report carries SHALL also carry the shared report contract's per-item budget, which that requirement
owns, not this one.

#### Scenario: Quick review runs after proposal and implementation

- **WHEN** a unit's proposal (planning artifacts) or its implementation is finished
- **THEN** the `review-quick` subagent reviews it against the unit's planning artifacts, and the loop repeats until it
  reports no new observations before a manual review is requested
- **AND** the pass is a bounded screen over what it is given — the supplied diff and the explicitly supplied planning
  artifacts, and nothing else — so a repository-wide search is not part of it, and a local defect does not trigger one
- **AND** its fixed scope is: coherence between the supplied proposal, design, tasks, delta specs, and diff; a completed
  task that visibly does not correspond to the supplied work; an obvious implementation-to-delta-spec mismatch; an
  explicit broken reference; an obvious convention violation in a changed file; the formatting of a changed file no
  formatter owns; and the prior-round finding instances the caller supplies
- **AND** the pass is not required to search for every instance of a finding class, to reproduce every claims-table
  source or inspect every scan candidate, to analyse tests mutation-style, or to analyse boundary conditions, error
  paths, callers, configuration, or documentation exhaustively beyond the supplied scope — that depth belongs to the
  deep review the routing decision below selects
- **AND** its stop condition scales with what it is given rather than with the repository: the pass ends when each
  supplied artifact and each changed file has been examined once, so a larger diff is more work and a repository the
  change does not touch is none, and no second sweep of the same file is part of the screen
- **AND** the pass's report states that scope and stop condition rather than implying its analysis was unlimited, so a
  bounded screen is not read as a thorough pass
- **AND** the governance the pass does not replace stays in force — the pre-write derivation pass, the mandatory
  quick-review gate, the tests, the manual review, and every other check
- **AND** for a unit whose diff adds `AGENTS.md` rules (a capture), it also challenges each new rule's durability — the
  decision the rule governs, and whether a future change would plausibly hit it — so trivia and restatements are caught
  at review time rather than by a later consolidation audit
- **AND** each finding is classified from the canonical list the reviewer's contract carries — each class naming what
  must change, and the calling agent extending the list in the same change when a finding fits none — and when the
  caller's request names a prior round's findings, a finding whose class a prior round already raised is reported as a
  repeat of that class, naming it — so the loop's convergence signal comes from the review's output rather than the
  caller's recollection, and the caller re-derives the root cause on that signal rather than patching the instance
- **AND** every round emits one machine-readable record line — the date, the unit, the round (continuous per unit across
  its proposal and implementation reviews), the classes raised (comma-separated; `-` when none), which repeated a prior
  class (comma-separated; `-` when none), and the count of findings that were a compliance miss of an existing rule —
  for the calling agent to append to `docs/review-rounds.tsv`, so the digest can trend the loop's cost and the pre-write
  pass can be evaluated
- **AND** the calling agent appends the round's row with the reviewer-session fields the ledger's contract declares —
  the session identifier, whether the session was created, continued, or replaced, and the replacement's reason when it
  was replaced — supplying the identifier the invocation returned rather than inventing one, so the record evidences the
  transition instead of leaving it to recollection
- **AND** with no prior-round list supplied, the review still classifies each finding and states that a repetition is
  unknown rather than inferring one, since the classification cannot be compared against what it has not seen
- **AND** a defect the applying command rewrites — `./gradlew spotlessApply`, or the web module's `npmFormat` — is
  routed to that command rather than reported as a finding; the boundary is the command's own rewrite, not a category of
  violation, so what it leaves alone — an inline code span's interior, a file type no formatter owns — stays reportable
- **AND** for a record artifact (a retrospective, a load-test record), a later round raising only new instances of an
  already-raised class stops and escalates to the owner rather than iterating — such an artifact has no shipped behavior
  to converge on, so the escalation names the loop that cannot converge rather than continuing
- **AND** every unit carries the pre-write derivation pass as a record — a task in the change's `tasks.md`, or a line in
  the pull-request checklist for a unit with no change dir — so a skipped pass is a missing box rather than looking like
  a pass that found nothing
- **AND** the pass emits a claims table — each fact the change states about its evidence, the prior artifacts it rests
  on, with its source or the deriving command — so the review reports a claim the supplied artifacts state that the
  table omits, a row whose stated source plainly does not reproduce, or a scan candidate neither resolved into a row nor
  listed as a dismissal, without re-deriving every row from scratch
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
- **AND** the caller runs a reviewer-session preflight immediately before each invocation: it creates the reviewer's
  session on the unit's first round with a fresh invocation and retains the identifier that invocation returns; on every
  later round it invokes the reviewer by passing that saved identifier as the invocation's session-continuation
  parameter, never a fresh invocation labelled as a continuation; and it starts a replacement only after a continuation
  attempt has produced one of the recovery cases below, recording the concrete reason and both identifiers — the session
  that failed and its replacement's — before it replaces
- **AND** after a continuation invocation the caller compares the identifier the invocation returns with the one it
  saved, and a difference stops the round and is reported as a session-continuity failure rather than recorded as a
  continuation — so a loop that restarted its reviewer each round is distinguishable from one that continued it, rather
  than resting on the caller's recollection
- **AND** the only cases that justify a replacement are: the prior session expired, the reviewer's definition changed
  after that session began, or the reviewer stopped responding mid-round (the loop has observed a session stall with no
  reply, which no wait resolves)
- **AND** the reuse never substitutes for the per-round instance hand-off above, which the caller supplies on every
  round regardless of the reviewer's session
- **AND** the review checks, in one line each and within the supplied scope, the proposal's coherence and whether the
  required behavior is represented and its tasks cover the tests and documentation; the implementation's obvious
  boundary conditions and changed semantics (defaults, failure handling, retries, ordering, concurrency), a test in the
  diff that would still pass with the behavior broken, and the changed files' effect on callers, configuration, and
  documentation; and the conventions visible in the changed files (naming, dead code, missing mandated Javadoc,
  documentation still describing superseded behavior, and broken cross-references) — so the pass names what to look at
  without becoming a thorough review
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

- **WHEN** a deep review pass is wanted (drift, correctness, architecture, conventions) — a manual `/review-thorough`
  invocation, or an outcome the deep-review routing decision below selects
- **THEN** the `review-thorough` subagent reviews the change against its delta specs, tasks, and the surrounding code,
  proportionately to the risk the decision's outcome names
- **AND** the dimensions it inspects are specification compliance and the delta-spec scenarios; correctness, state
  transitions, failure paths, retries, timeouts, and idempotency; architecture, dependency boundaries, and deviations
  from the approved design; concurrency, reactive behavior, ordering, transaction boundaries, persistence, and
  distributed failure modes; the tests and the verification a concrete risk is missing; security, trust boundaries,
  configuration, deployment, observability, and operational risk; and the applicable repository conventions and
  structured-writing rules
- **AND** every finding it raises identifies a concrete defect or material risk, cites repository evidence and a precise
  location, explains the consequence, gives a corrective direction, and distinguishes a confirmed defect from an
  uncertainty — never a preference, a speculation, or a second instance of a root cause it already reported
- **AND** a manual `/review-thorough` invocation is an explicit run: it is never a substitute for a `REQUIRED` outcome,
  and it does not route

#### Scenario: Deep review is routed by one three-outcome decision

- **WHEN** a unit's implementation is complete and its quick-review loop has run
- **THEN** one routing decision is made before any deep-review session is created, from the completed implementation,
  the diff, the applicable planning artifacts, and the existing review evidence, and it resolves to exactly one of
  `REQUIRED` (a deep review must run before completion), `RECOMMENDED` (elevated risk or uncertainty requires an
  explicit recorded resolution), or `NOT_NEEDED` (the change is clearly low risk and may bypass deep review)
- **AND** `REQUIRED` covers a material change to a public contract, a cross-component boundary, an Axon
  command/event/aggregate/saga/projection, persistence or a migration, a transaction, a retry, idempotency, concurrency,
  ordering, reactive behavior, security, infrastructure, deployment, or an operational failure mode, a major
  specification deviation, and a unit whose recorded rounds show a material defect an earlier review missed
- **AND** `RECOMMENDED` covers credible but non-mandatory risk — a complex invariant, an ambiguous material requirement,
  a broad interaction, substantial design uncertainty, or a plausible defect quick review and the tests are unlikely to
  find — and it is resolved explicitly from evidence rather than escalated or skipped by default
- **AND** `NOT_NEEDED` applies only where the change is localized, low risk, and adequately verified, and never on a
  criterion of line count, changed-file count, or the build agent's own confidence alone — a documentation-only,
  formatting-only, or mechanical change is an example of the class, not an unconditional exemption
- **AND** the decision has a deterministic floor derived from the change's own facts — its changed paths and the
  declarations in its diff, its delta spec's requirement-block kinds, and its recorded rounds — so the decision is
  explainable and reproducible rather than an unaided judgment, and the agent's decision is at or above that floor and
  records the signals it relied on
- **AND** the decision is reassessed only when material new evidence changes the risk

#### Scenario: A missing or failed routing decision cannot skip mandatory review

- **WHEN** the routing decision is absent, malformed, or its derivation fails
- **THEN** the outcome is `REQUIRED` rather than a silent bypass, so an unreadable or failed decision can only make
  review run, never skip it

#### Scenario: A low-risk change bypasses the deep-review lifecycle

- **WHEN** the routing decision resolves to `NOT_NEEDED`
- **THEN** no deep-review session is created, no hand-over is prepared, and no expensive-model invocation is made, while
  the mandatory quick review, the tests, and the manual approval still run

#### Scenario: A routed deep review runs in its own session with a task-relevant hand-over

- **WHEN** the routing decision requires or recommends a deep review and the owner resolves to run it
- **THEN** one dedicated `review-thorough` session is created for that change, its supported session identifier and
  review state are preserved, and it is handed only the task-relevant context — the change identity, the current diff,
  the applicable OpenSpec artifacts, the prior findings and their statuses, the current pass, the known limitations, and
  whether the pass is an initial review or a fix verification
- **AND** the build agent, the quick reviewer, and the deep reviewer stay in separate sessions
- **AND** a follow-up pass on that change reuses the same deep-reviewer session, while a different change starts a fresh
  one
- **AND** the deep reviewer inspects the current state independently on every follow-up, because session continuity is
  not proof that a fix is correct

#### Scenario: A failed deep-review hand-over is detected, not reported as a review

- **WHEN** creating the session, handing over, continuing it, parsing its output, or validating it fails
- **THEN** the failure is detected and reported rather than treated as a successful review, reusing the loop's existing
  recovery where it applies
- **AND** where the session must be replaced, the current findings, their statuses, the diff, and the objective are
  transferred to the replacement, the lost continuity is recorded, and the current state is reviewed independently
- **AND** where no safe recovery is available, the unit stops and reports the blocker

#### Scenario: Deep review is bounded by a two-pass budget

- **WHEN** a routed deep review runs
- **THEN** its default budget is two full passes — an initial deep review, and an independent verification after the
  valid findings are fixed — and a further full pass requires an explicit recorded decision resting on the material
  residual risk and the expected value of the pass
- **AND** a targeted re-review may verify a localized fix and its impact radius, and it does not conceal an unresolved
  material finding
- **AND** at the budget limit an unresolved blocking finding prevents normal completion, an unresolved should-fix
  finding requires an explicit resolution or a recorded acceptance, incomplete verification and uncertainty stay
  visible, and the limit never accepts residual material risk by itself

#### Scenario: A deep-review finding is tracked to a resolved status

- **WHEN** a deep-review pass raises a finding
- **THEN** it is tracked by a stable identifier where the reviewer supports one, or otherwise by a stable root cause,
  description, and location mapping, and its status is distinguished as `OPEN`, `FIXED_AND_VERIFIED`, `REJECTED` with
  the evidence for the rejection, or `ACCEPTED` only by an explicit owner decision recorded with the reason and what it
  accepts
- **AND** the status has one authoritative record rather than a copy in several artifacts, and the quick loop's round
  ledger stays its own record, so a deep pass does not change the population the loop's trend is read from

#### Scenario: Deep review exhausts a class it raises

- **WHEN** a deep-review pass raises a finding that falls in a class
- **THEN** it searches the change's artifacts for every instance of that class's shape and reports them as one clustered
  finding, so a class surfaces whole in the deep pass rather than one instance per quick-review round

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

- **WHEN** the main agent needs to inspect a screenshot, image, or visual UI state (e.g. web-UI styling, or a rendered
  diagram)
- **THEN** the `vision` subagent reads the image and returns a **structured visual review**, so the text-only main agent
  can delegate visual review
- **AND** the review states the artifact it inspected and the scope of the review
- **AND** it reports in three separated groups rather than one description: the **observed facts** (what is visibly
  present), the **visual risks or suspected defects**, and **what the image cannot settle**
- **AND** for a diagram render it checks every visible node and whether its shape matches its intended role, each edge's
  direction and arrowhead, which label belongs to which edge, duplicate, missing, or ambiguous labels, collisions,
  overlap, clipping, cropping, unreadably small content, and the whitespace left near the right edge and near any host
  control overlay
- **AND** each item names the visible element it concerns and its approximate position in the image
- **AND** a visual claim that needs a measurement to settle is reported as a **hypothesis** naming the method that would
  settle it (e.g. `getComputedTextLength()` for SVG text against its container, or a label's `scrollWidth` against its
  container's width) — an apparent clipping, overflow, or overlap is never reported as a confirmed fact
- **AND** it stays read-only and concise: it reports what it sees and never edits a file

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
- **AND** it returns a **visual-review handoff** beside the source: the nodes, edges, directions, labels, and spans the
  render is expected to show, the rendered artifact that must be inspected, and the visual risks a source-level check
  cannot establish
- **AND** its own `mermaid-cli` render is reported as what it proves — that the source parses and lays out — never as
  proof of the host renderer's toolbar clearance or of a browser's layout

#### Scenario: A Mermaid render is visually reviewed before the diagram ships

- **WHEN** the `diagrammer` subagent has returned Mermaid source with its visual-review handoff
- **THEN** the calling agent makes the render `diagrammer` produced inspectable — serving it, since a browser cannot
  open a `file://` path — and delegates it to the `vision` subagent with the handoff, so the render is inspected rather
  than only parsed
- **AND** where the target is GitHub-rendered Markdown and Playwright is available, it also inspects a live GitHub
  render — the host `viewscreen` toolbar collision and browser layout a local render cannot reveal
- **AND** it reports the local render check and the visual review separately
- **AND** a claim the review raises as needing a measurement is settled by one before the diagram ships, since an
  observation is not a measurement
- **AND** an unavailable live GitHub render is stated as a limitation, never treated as equivalent to one that was
  inspected
