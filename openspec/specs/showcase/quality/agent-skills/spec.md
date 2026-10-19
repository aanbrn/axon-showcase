# showcase/quality/agent-skills Specification

## Purpose

Provides the repository's agent capabilities: curated, vendored skill sets under `.opencode/skills/` (so agents can run
the AxonIQ Axon 4→5 migration recipes against this codebase), the project-authored guidance skills there beside them
(the `check-and-test-authoring` reference and the `setup-*` skills, loaded on demand at the trigger `AGENTS.md` names),
the knowledge-authoring standard `AGENTS.md` entries follow (representation, one authoritative definition per subject,
the procedure and gotcha shapes, the evidence rules, and the validation pass — applied by the capture when authoring and
the audit when reconciling), the structured writing standard every agent-authored technical artifact follows (stated
once in `AGENTS.md`, applied while drafting and checked by the review and audit subagents), and the locally-defined
quality-gate and analysis subagents under `.opencode/agent/` — the per-unit review and lesson-capture agents, the
experience-analyzer, the visual and diagram agents, and the on-demand auditors of the project-owned agent tooling (the
guidance and project-authored `.opencode/` files, plus any generated or vendored file that contradicts how the
repository uses it, the always-loaded instruction budget it measures with the extraction opportunities that surface
where guidance has no destination yet, the meta rules it reports with their origins, the merge, removal, route, and
extraction candidates it reports, and the authoring-system gaps it traces to a mechanism that produces an artifact), of
the `openspec/specs/` corpus, of the architecture (the ADRs, the service, module, and spec-decomposition surface, where
a deliberate decision's rationale is not recorded, and where a record's recorded premise appears met — a deferral's
`Revisit when:` condition, or a parked `docs/ideas.md` entry whose work has landed, fully or partly, or whose objective
a different mechanism achieves), of the human-facing `README.md` (its claims against the repository, its shape against
the README convention, and its coverage of the human-visible capabilities), and the scheduled unattended run that
performs the audits without a human asking, together with the standing owner-gated unit that applies an audit report's
reduction candidates, and the project-authored command definitions under `.opencode/commands/` — their authoring shape,
the repository lifecycle a mutating unit owes, and how an audit's approved findings are delivered as separate units by
the route each finding's kind requires.

## Requirements

### Requirement: Vendored agent skills are available to agents

The repository SHALL vendor the AxonIQ `axoniq-migration` plugin skills (version 0.2.2) from the `AxonIQ/agent-skills`
repository under `.opencode/skills/`, so that opencode discovers them as project skills. The vendored set SHALL include
`axon4to5-migrate-code`, `axon4to5-openrewrite`, and `axon4to5-isolatedtest`. Vendoring SHALL NOT add the skill files to
any Gradle module's classpath or to any built Docker image.

#### Scenario: Migration skills are discoverable by agents

- **WHEN** an agent lists the project's skills in `.opencode/skills/`
- **THEN** it finds `axon4to5-migrate-code`, `axon4to5-openrewrite`, and `axon4to5-isolatedtest`, each with a `SKILL.md`
  entry point and its referenced assets

#### Scenario: Vendored skills do not affect the application build

- **WHEN** the project build runs any Gradle task (compile, test, package)
- **THEN** the vendored skill files under `.opencode/skills/` are not part of any module's source set, classpath, or
  Docker image

### Requirement: Vendored skills carry version provenance

The vendored skills SHALL record their upstream source repository and the exact plugin version they were copied from, so
that an upstream update is a deliberate, reviewable change rather than silent drift.

#### Scenario: Provenance is recorded

- **WHEN** a maintainer inspects the vendored skills or the `agent-skills` capability spec
- **THEN** the upstream source (`AxonIQ/agent-skills`), plugin (`axoniq-migration`), and version (0.2.2) are documented

#### Scenario: Updating to a new plugin version is an explicit change

- **WHEN** a new `axoniq-migration` plugin version is published upstream
- **THEN** refreshing the vendored skills is performed as a reviewed change that updates the recorded version, rather
  than applied silently

### Requirement: Experience analysis is available to agents

The repository SHALL provide an `experience-analyzer` agent subagent that, given a gathered digest of recent experience
(merged pull requests, archived changes, `AGENTS.md` gotchas, `docs/ideas.md`, and a derived-facts block), produces a
retrospective and forward-looking improvement suggestions. The derived-facts block SHALL carry the window's countable
claims already resolved — the merged-PR boundary facts (the search's count, each PR merged on the `since` date, and the
previous retrospective's file), the archived changes scoped to the window's commits, `AGENTS.md`'s line,
top-level-bullet, and `captured:` metrics at HEAD and just before each `since`-date PR (so the window's baseline is read
off the first counted PR), each `captured:` marker added since the window start with its introducing commit, and the
window's review rounds from `docs/review-rounds.tsv` — its round count, the classes raised, and the compliance-miss
count stated for the window proper (the rows dated after the `since` date) beside the `since`-inclusive figures, with
the classes normalized to the reviewer's canonical vocabulary so a label recorded before that vocabulary landed compares
with the ones that follow it, and a pre-canonical label that cannot be normalized reported as such rather than passed
through silently — so the retrospective is written around derived facts rather than re-deriving them by hand. The
retrospective SHALL group shipped PRs by theme, summarize lessons learned, and record went-well/went-wrong observations.
Each improvement suggestion SHALL be classified as a system suggestion (addressed via `docs/ideas.md` or an OpenSpec
proposal) or a process suggestion (addressed via `AGENTS.md` or a subagent definition). The agent SHALL propose the
retrospective and suggestions without editing files; the main agent verifies and applies them.

Beyond grouping the shipped work, the retrospective SHALL trace each significant failure it reports to a root cause and
name the earliest development stage at which the problem could reasonably have been prevented or detected — drawing the
evidence from the digest's review-round data rather than restating the symptom — so an improvement can move prevention
earlier. It SHALL evaluate the previous retrospective's recommendations when their outcomes can be observed, stating for
each whether it was applied and whether the available evidence shows it helped, or that the window cannot yet show it,
rather than presuming effectiveness from the absence of the failure. Each improvement suggestion SHALL state the failure
pattern it targets, the observable outcome expected, and how that outcome would be verified, so a later window can judge
whether it worked.

#### Scenario: A retrospective is produced from gathered experience

- **WHEN** the main agent gathers a digest of recent experience and invokes the `experience-analyzer` subagent
- **THEN** it returns a retrospective grouping shipped PRs by theme, summarizing lessons, and recording went-well and
  went-wrong observations

#### Scenario: The digest carries the window's countable claims

- **WHEN** the main agent gathers the digest for a retrospective window
- **THEN** the digest includes a derived-facts block stating the window's merged-PR boundary facts (the search's count,
  each PR merged on the `since` date, and the previous retrospective's file), the archived changes scoped to the
  window's commits with the `since`-date dirs named for the same resolution, `AGENTS.md`'s line, top-level-bullet, and
  `captured:` metrics at HEAD and just before each `since`-date PR (so the window's baseline is read off the first
  counted PR), each `captured:` marker added since the window start with its introducing commit, and the window's review
  rounds from `docs/review-rounds.tsv` — its round count, the classes raised, and the compliance-miss count for the
  window proper stated beside the `since`-inclusive figures, with the classes normalized to the reviewer's canonical
  vocabulary and a pre-canonical label that cannot be normalized reported as such — so the retrospective's countable
  claims are read from the block rather than authored from memory

#### Scenario: Improvement suggestions are classified

- **WHEN** the `experience-analyzer` subagent proposes improvements
- **THEN** each suggestion is labeled as a system change (→ `docs/ideas.md` or a proposal) or a process change (→
  `AGENTS.md` or a subagent definition), so the main agent can route it

#### Scenario: The agent does not edit files itself

- **WHEN** the `experience-analyzer` subagent runs
- **THEN** it returns proposed text and suggestions without modifying files; the main agent verifies and applies them

#### Scenario: A significant failure is root-caused to its earliest prevention point

- **WHEN** the retrospective reports a significant failure or a recurring review-finding class
- **THEN** it names the underlying cause and the earliest development stage at which the problem could reasonably have
  been prevented or detected, drawn from the digest's review-round data, so the improvement targets that stage rather
  than the symptom

#### Scenario: The previous retrospective's recommendations are evaluated

- **WHEN** the digest names the previous retrospective and its recommendations' outcomes can be observed
- **THEN** the retrospective states, for each, whether it was applied and whether the available evidence shows it
  helped, or that the window cannot yet show it — never presuming effectiveness from the absence of the failure

#### Scenario: Each suggestion carries a verifiable expected outcome

- **WHEN** the `experience-analyzer` subagent proposes an improvement
- **THEN** the suggestion states the failure pattern it targets, the observable outcome expected, and how that outcome
  would be verified, so a later window can judge whether it worked

### Requirement: Per-change quality-gate and analysis subagents are available

The repository SHALL provide locally-defined agent subagents under `.opencode/agent/` for the per-unit quality gates and
analysis workflows — the per-unit review and lesson-capture agents, and the visual and diagram agents — each described
by the scenarios below or by its own requirement in this capability. Each SHALL be invocable by the main agent, with its
purpose described in its agent definition and (where relevant) in `AGENTS.md`. A reviewer invocation SHALL be bounded
and recoverable: the pass it runs stops when what it was given has been examined, and an invocation that stops
responding is replaced from the caller's own record — so a reviewer's duration is bounded by what it was given rather
than by the repository, and a stall costs the caller a wait rather than the review.

Every item the report carries SHALL also carry the shared report contract's per-item budget, which that requirement
owns, not this one.

#### Scenario: Quick review runs after proposal and implementation

- **WHEN** a unit's proposal (planning artifacts) or its implementation is finished
- **THEN** the `review-quick` subagent reviews it against the unit's planning artifacts, and the loop repeats until it
  reports no new observations before a manual review is requested
- **AND** the pass is a bounded screen over what it is given — the supplied diff and the explicitly supplied planning
  artifacts, and the risk focuses the caller supplies, and nothing else — so a repository-wide search is not part of it,
  and a local defect does not trigger one
- **AND** the caller supplies the screen's inputs as a list it can state and a reader can check — the artifacts to
  screen and the checks to run over them — and the pass's report names that list, so which surfaces were screened is
  verifiable from the report rather than inferred from its prose
- **AND** for a capture, the supplied list includes the existing rules each proposal claims to extend (or its statement
  that none covers it), the capture evidence (the round rows and the change record), and the `lesson-capture` definition
  where compliance with its gate is checked, so the comparison rests on materials the caller supplied rather than a
  search the screen would have to run
- **AND** the risk focuses are the caller's, stated with the invocation, and they widen which of the supplied surfaces
  the screen looks at closely rather than adding surfaces to screen — so a focus is a direction of attention within the
  bounded set, not a second scope
- **AND** its fixed scope is: coherence between the supplied proposal, design, tasks, delta specs, and diff; a completed
  task that visibly does not correspond to the supplied work; an obvious implementation-to-delta-spec mismatch; an
  explicit broken reference; an obvious convention violation in a changed file; the formatting of a changed file no
  formatter owns; and the prior-round finding instances the caller supplies
- **AND** the pass is not required to search for every instance of a finding class, to reproduce every claims-table
  source or inspect every scan candidate, to analyse tests mutation-style, or to analyse boundary conditions, error
  paths, callers, configuration, or documentation exhaustively beyond the supplied scope — that depth belongs to the
  deep review the routing decision below selects
- **AND** a risk the supplied scope cannot settle is escalated to `review-thorough` — through the routing decision below
  or by the caller raising it — rather than absorbed by widening this pass, so the quick screen stays bounded and the
  depth it cannot afford runs in the pass that has the budget for it
- **AND** its stop condition scales with what it is given rather than with the repository: the pass ends when each
  supplied artifact and each changed file has been examined once, so a larger diff is more work and a repository the
  change does not touch is none, and no second sweep of the same file is part of the screen
- **AND** it does not expand its own scope: it adds no surface beyond the supplied list and the checks its own contract
  names, and where a check cannot establish its evidence within that scope it reports the limitation rather than
  searching for more — so an unresolvable question narrows the report instead of extending the pass, and the pass
  returns its best completed report rather than treating uncertainty as a reason for unbounded investigation
- **AND** the pass's report states that scope and stop condition rather than implying its analysis was unlimited, so a
  bounded screen is not read as a thorough pass
- **AND** the governance the pass does not replace stays in force — the pre-write derivation pass, the mandatory
  quick-review gate, the tests, the manual review, and every other check
- **AND** for a unit whose diff adds or changes `AGENTS.md` rules (a capture), it also challenges each new rule's
  durability — the decision the rule governs, and whether a future change would plausibly hit it — so trivia is caught
  at review time rather than by a later consolidation audit
- **AND** it applies the capture scenario's promotion gate and dispositions to each proposal, from the supplied list
  above, so the reviewer compares the proposal with the existing obligation and its evidence rather than the gate's
  criteria being restated a second time, and a proposal the gate disposes of as `already covered`, or one governing no
  decision, is a finding against the capture
- **AND** each capture finding names the proposed text, the relevant existing obligation or supplied evidence, and the
  concrete defect
- **AND** where the supplied materials cannot establish a comparison or a causal claim, the review reports that specific
  limitation and whether the missing material is one the capture contract requires — a capture defect — or a limit of
  the bounded screen, and an uncertainty alone is not reported as a confirmed finding
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
  parameter, never a fresh invocation labelled as a continuation; and it starts a replacement only after the invocation
  it replaces — a continuation attempt, or the unit's first round's fresh invocation — has produced one of the recovery
  cases below, recording the concrete reason and both identifiers — the session that failed and its replacement's —
  before it replaces
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

#### Scenario: A stalled reviewer is replaced from a recorded handover

- **WHEN** a reviewer invocation has not responded by the deadline the caller's policy supplies
- **THEN** the caller records the failure as `stopped-responding`, naming the stalled session's identifier and its
  replacement's, and creates the replacement session only after that record
- **AND** the handover it gives the replacement is the record it wrote **before** waiting — the unit's identity, its
  base and current revisions, the supplied artifacts, the scope, the prior findings with their instance-level evidence,
  the completed checks, and the known limitations — so the context a replacement needs never depends on the stalled
  invocation reporting, and cancelling it loses no progress that record does not already hold
- **AND** the deadline comes from the invocation or the caller's policy, never a universal figure stated as a measured
  fact, because no measurement of a reviewer's duration is reproducible from the repository
- **AND** the replacement reviews the current state independently, treating the handover as context rather than as proof
  that a result is correct, and re-derives what it reports from the state it inspects
- **AND** the record keeps only facts the caller can verify — the replacement's reason, both session identifiers, the
  handover, and the round's outcome — and never a duration, a token count, or a session count asserted as a measurement,
  so the ledger trends what a gate can re-derive rather than a figure it cannot

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
- **AND** its scope is the change's artifacts and the checks its own contract names rather than the repository at large,
  and a question its evidence cannot settle within that scope is reported as a limitation rather than pursued
  indefinitely — so a deep pass returns its best completed report rather than treating uncertainty as a reason for
  unbounded investigation

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
- **AND** the `lesson-capture` subagent proposes each lesson as an addition to the artifact that owns it — an
  `AGENTS.md` gotcha or convention, or a check/test authoring mode in the `check-and-test-authoring` skill, or a pair of
  additions when the lesson has both halves — which the main agent verifies and applies
- **AND** each proposed addition names the existing rule it extends — an `AGENTS.md` bullet, or a check/test authoring
  mode already in the skill — or states that none covers it, so a new rule merges into or replaces one rather than
  accreting
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
- **AND** the gate separates a repeated observation from its cause — a repeated review-class label establishes that
  label's recurrence, not a shared drafting defect, and a remedy resting on a class's recurrence (a section rewrite, or
  a rule generalized from that class) requires the shared cause confirmed from the evidence, with a class whose
  instances have different causes taking the cause-specific shape instead
- **AND** the caller supplies the existing guidance a candidate would extend and the review evidence for a claimed
  recurrence, and where either is unavailable the capture states the limitation and withholds the unsupported candidate
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
- **AND** each proposed addition demonstrates the **decision difference** it makes — the action a future agent takes
  under the existing guidance, and the action the proposed text requires or prohibits under an explicit condition
- **AND** that comparison names the existing obligation, its explicit condition, and the different action, so different
  phrasing does not establish a difference
- **AND** that difference carries a repository-backed example of why it matters
- **AND** an addition whose action the existing guidance already determines is reported as **already covered** and does
  not expand `AGENTS.md`
- **AND** an additional incident, example, citation, or provenance marker alone is `already covered`, and the capture
  records that evidence in the unit's change record rather than expanding the rule's text
- **AND** a partial restatement takes the **split** disposition — the new clause is kept, the restatement dropped
- **AND** it prefers an exact replacement of the existing rule over appending qualifications
- **AND** a replacement or rewrite preserves every valid obligation, warning, exception, and identifier, the established
  scope, and the normative strength, and may correct verified errors or remove established duplication and obsolete
  content
- **AND** the proposed action and its scope are derived from the verified evidence, so a remedy that worked in one place
  is not stated as a universal instruction
- **AND** the incident evidence a rule rests on stays recoverable behind its `captured:` marker rather than in the
  rule's text
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

### Requirement: The spec corpus is audited for structure and consistency

The repository SHALL provide a `specs-auditor` agent subagent that audits the `openspec/specs/` corpus for **structure
and consistency**, and reports its findings without editing files. The audit SHALL cover, at least: each spec's `#`
title matching its capability path; each Purpose describing the requirements the spec holds; requirement-header and
requirement-body conventions (a declarative header, a SHALL body, WHEN/THEN scenarios); requirements duplicated or
near-duplicated across specs; and dead cross-references to renamed classes, files, or configuration the specs cite. The
audit SHALL NOT verify behavior against the implementation — that remains with the change workflow's review loop and the
archive-time spec sync. Each finding SHALL be verified against the repository, and SHALL be reported grouped by severity
with its location and a concrete suggested rewrite. A requirement header reused across two genuinely different
capabilities SHALL be reported for judgment rather than treated as a defect. The subagent SHALL propose its findings
without modifying files; the main agent verifies and applies those the user approves.

A reference SHALL be reported as dead only where the referenced artifact is verified absent or obsolete — its exact name
and variants searched and its rename, relocation, replacement, and current context checked — and SHALL NOT be reported
where it merely cannot be located, where a filename differs in case or extension from an informal mention, where the
class is generated or produced by a build, where the command belongs to an external tool, where the configuration key is
defined indirectly, where the name appears only in documentation, or where the reference is conceptual rather than an
exact symbol. A stale reference SHALL be corrected to its verified successor, or removed where the referenced concept no
longer applies, without rewriting the requirement's behavior.

Two similar requirements SHALL be compared semantically — the obligation, the actors and components, the conditions, the
observable outcomes, and the scope, ownership, and exceptions — and SHALL be reported only where the obligations are
substantially identical or their overlap leaves ownership or precedence materially ambiguous; a requirement that is
related but distinct (the same concept with a different actor, boundary, condition, outcome, or responsibility) and an
intentional repetition (a shared convention repeated so each capability stays self-contained) SHALL NOT be reported as a
defect. Before recommending a consolidation the audit SHALL read both requirement bodies and their scenarios and the
surrounding Purposes, establish the owners and scopes, and establish what a merge would lose.

The audit SHALL NOT audit the corpus's capability decomposition (whether the role groups still match the module and
service structure), which the `architecture-auditor` owns; SHALL NOT re-report an issue an existing gate already
enforces (the `openspec validate` well-formedness, the review loop's behavior check); and SHALL NOT audit the internal
quality of a proposed change or change delta, which the review loop owns.

A requirement heading, a Purpose, or a requirement body SHALL be reported only for a violation of a stated convention: a
heading containing a verb, a technical term, or an established expression is not malformed, a valid normative body is
not reported for carrying explanatory prose, an incidental occurrence of `SHALL` that expresses no obligation is not
sufficient, and a scenario is checked by its Markdown structure rather than by a pattern match alone.

Confidence SHALL be an internal filter, not a report category: the audit SHALL NOT attach confidence or severity scores,
and SHALL omit a candidate whose evidence shows an intentional, coherent choice.

Symptoms sharing a root cause and a correction SHALL be reported as one finding, and independent defects SHALL NOT be
merged, repeated across severity groups, or padded with a placeholder.

The audit SHALL proceed in stages — inventory, local structure, cross-spec consistency, reference verification, report
validation — reusing the evidence an earlier stage gathered.

Every item the report carries SHALL also carry the shared report contract's per-item budget, which that requirement
owns, not this one.

#### Scenario: A spec-corpus audit is produced

- **WHEN** the main agent invokes the `specs-auditor` subagent (e.g. via the `/audit-specs` command)
- **THEN** it returns findings grouped by severity, covering title/path match, Purpose fit, requirement conventions,
  cross-spec duplication, and dead cross-references, each with a location and a suggested rewrite

#### Scenario: Findings are verified against the repository

- **WHEN** the `specs-auditor` subagent flags a title, cross-reference, or apparent duplicate
- **THEN** it checks the claim against the repository (the capability path, the referenced file or symbol, the bodies of
  the requirements involved) before reporting it, so a still-true title or a legitimately shared header is not reported
  as a defect

#### Scenario: Behavior drift is out of scope

- **WHEN** the `specs-auditor` subagent audits the corpus
- **THEN** it does not verify requirement scenarios against the implementation, leaving behavior-vs-code drift to the
  change workflow's review loop and the archive-time spec sync

#### Scenario: The auditor does not edit files itself

- **WHEN** the `specs-auditor` subagent runs
- **THEN** it returns proposed findings and rewrites without modifying files; the main agent verifies and applies those
  the user approves

#### Scenario: A dead reference is verified before it is reported

- **WHEN** the `specs-auditor` subagent finds a class, file, configuration key, or command a spec names that it cannot
  locate
- **THEN** it searches the exact name and its variants and checks renames, relocations, generated artifacts,
  replacements, and the reference's current context before reporting it, and it does not report a case/extension
  difference, a generated class, an external tool's command, an indirectly-defined configuration key, a
  documentation-only name, or a conceptual reference as dead

#### Scenario: A candidate duplication is classified before it is reported

- **WHEN** two requirements across specifications appear similar
- **THEN** the audit compares the obligation, actors, conditions, outcomes, scope, and exceptions, and reports the
  candidate only where the obligations are substantially identical or their overlap is materially ambiguous — not a
  related-but-distinct requirement or an intentional repetition

#### Scenario: The out-of-scope surfaces are not audited

- **WHEN** the `specs-auditor` subagent audits the corpus
- **THEN** it does not audit whether the role groups still match the module or service structure (the
  `architecture-auditor` owns that), does not re-report an issue an existing gate already enforces, and does not audit
  the internal quality of a proposed change or change delta (the review loop owns that)

#### Scenario: A correction preserves the intended behavior

- **WHEN** the `specs-auditor` subagent proposes a rewrite, merge, split, or reference update
- **THEN** the recommendation names the exact text to change and the smallest correction that repairs the problem,
  preserving the requirements' conditions, outcomes, exceptions, and the distinctions between capabilities, and stating
  for a merge why the requirements are semantically equivalent

#### Scenario: Confidence is a filter, not a report category

- **WHEN** the `specs-auditor` subagent weighs a candidate whose evidence shows an intentional, coherent choice, or
  several symptoms of one root cause
- **THEN** it omits the coherent choice, reports the shared symptoms as one finding, and attaches no confidence or
  severity score — repeating no item across groups and adding no placeholder for a category with no findings

#### Scenario: The audit proceeds in bounded stages

- **WHEN** the `specs-auditor` subagent audits the corpus
- **THEN** it inventories the corpus, checks local structure, compares related requirements across specs, verifies the
  questionable references, and validates the report against the scope — reusing the evidence an earlier stage gathered

### Requirement: Project-owned agent tooling is audited for consistency and conciseness

The repository SHALL provide an `agents-auditor` agent subagent that audits the project-owned agent tooling —
`AGENTS.md` and the project-authored files under `.opencode/agent/`, `.opencode/commands/`, and `.opencode/skills/` —
for **consistency** and **conciseness**, and reports its findings without editing files. The audit SHALL exclude files
the repository does not author: the generator-owned OpenSpec instruction files (the commands and skills written by
`openspec update`) and the vendored `axon4to5-*` skills (copied verbatim from the upstream AxonIQ repository); a
project-authored file that shares a generated file's name prefix (e.g. the `opsx-tool-update` command) SHALL remain in
scope. The audit SHALL cover, at least: contradictions between entries or files, stale claims and enumerations the
repository has outgrown, dead cross-references, and drift from the code/workflows/specs the prose describes
(consistency); and duplicated or near-duplicate entries, one-off trivia, over-long or over-specific entries, and
misplaced entries (conciseness). Within conciseness the audit SHALL report these analyses as standing findings rather
than only under a scoped run: **merge candidates**, each naming the overlapping or complementary entries and the merged
text that preserves every anchor and piece of evidence the originals carried — and never blending two distinct lessons
into one — and where the merged text would only restate a rule the file already carries, the candidate is a deletion of
the duplicate rather than a merge, reported as a `remove` so the verdict's count includes it, and the audit SHALL say
which it is; **removal candidates**, each naming a rule that governs no decision (trivia, not a rule — the same test the
capture's filter applies), what would be lost, and whether git preserves it; and **route candidates**, each naming a
rule whose subject a deterministic mechanism already enforces — a build or CI gate, a lint or test, a CLI validation, or
a deterministic workflow step such as the archive-time spec sync, but never a review, whether a human's or a review
subagent's — and proposing to reduce the rule to a pointer at that mechanism, or move its content to the spec or ADR
that owns it, naming the mechanism, what would be lost, and confirming the mechanism exists and covers the rule's
subject; and **extraction candidates**, each a verified proposal to partition one `AGENTS.md` unit into a minimal
retained root entry point, an authoritative destination that loads or is discoverable when its trigger occurs, and an
explicit retrieval path from that trigger to that destination, as the conditions below define. All are candidates for
the owner, not actions. Each finding SHALL be verified against the repository rather than inferred from the prose alone,
and SHALL be reported with its location and a concrete suggested rewrite. A reduction candidate — a merge, removal,
route, or extraction — SHALL carry seven fields: its location; the decision its rule changes; the task or condition that
requires it; the unique content the change would otherwise lose (every imperative, warning, exception, identifier,
rationale, provenance marker, and verification); its current loading cost; its disposition; and the repository evidence
that the surviving guidance preserves the same obligation. A merge candidate whose merged text delegates content to a
target — a spec or an ADR it points the reader to — SHALL have that delegation verified against the target: its behavior
**and** the identifiers, declarations, and gate conditions the delegated content names, not the behavior alone. A
delegated item the target does not carry SHALL stay in the merged text, and the candidate SHALL say which items it kept
for that reason. The subagent SHALL propose its findings without modifying files; the main agent verifies and applies
those the user approves.

The audit SHALL additionally report an **instruction-budget ledger** for `AGENTS.md`, measuring the always-loaded
instruction surface rather than judging it: the file's total size in bytes and lines; its size by top-level section; the
effective bound on that surface together with the source establishing it, stating whether the loading mechanism applies
a limit of its own; the first rule that falls beyond a derived effective threshold, expressed as an estimate with its
derivation rather than as a position at which content is dropped; the growth since the newest prior report under
`docs/audits/`; and the sections and `captured:`-marked rule units responsible for that growth. The ledger SHALL
estimate the bytes safely removable from the always-loaded surface, SHALL list the rules that must remain loaded with
the risk their unavailability would carry, SHALL list the rules safe to load on demand with their trigger and retained
pointer, SHALL tabulate the extraction candidates with their destination type and retrieval-path result, SHALL carry a
separate **extraction opportunities** subsection after the ready candidates — excluded from the verdict counts, stating
that its items require the owner's selection and a dedicated design or implementation change, distinguishing an existing
destination that failed retrieval from a destination that does not yet exist, and collapsing "no opportunity" to one
line — and SHALL state the post-change verification: re-measurement, pointer-target verification, retrieval-path tests,
a search for lost unique terms, and the review gate. The ledger SHALL NOT invent a limit the loading mechanism does not
apply, and a bound it reports as configured SHALL cite the configuration that sets it.

The audit SHALL keep root `AGENTS.md` guidance scoped to what an agent must see before safely starting work: a rule a
narrower task scope governs, and whose destination is durable, project-authored, owned, and loaded or deterministically
discoverable when that scope's trigger occurs, is a candidate to move out of the always-loaded surface. The audit SHALL
NOT propose a move that would leave the reader to infer a warning, an imperative, an exception, or a target-less fact
the retained root text does not state, and SHALL NOT report a rule as a removal candidate merely for being old,
uncommon, or lengthy — the accretion class's rule for a meta rule governs that case. Where the passage's intended
structure or meaning cannot be recovered confidently, the audit SHALL report the ambiguity rather than invent a rewrite.

The audit SHALL propose an **extraction candidate** only where every one of these holds: the candidate governs a
narrower task scope than root `AGENTS.md`; its destination is durable, project-authored, and owned; that destination is
loaded or deterministically discoverable when the trigger occurs; the retained root text lets an agent identify the
trigger and the required next action without reconstructing them; the move preserves every unique imperative, warning,
exception, identifier, verification, and provenance marker; the destination already carries, or the proposed change to
it will carry, the complete transferred contract; the move reduces the always-loaded surface by a material, measured
amount; and it does not split a procedure across files such that a reader must guess an order, condition, or failure
path. The audit SHALL derive the preferred destination type from the content's own class before selecting one: a global
safety, branch, review, or repository-wide workflow rule stays at root `AGENTS.md`; guidance needed only while working
below one unambiguous directory goes to a nested `AGENTS.md`; behavior that changes a capability outcome to an existing
or proposed capability spec; a durable architecture decision, rationale, or trade-off to an existing or proposed ADR; a
procedure invoked through a named task entry point to an existing command or skill; incident history, diagnostic
evidence, or a large reference inventory used by several future tasks to an existing durable reference document; and a
subject a deterministic mechanism enforces to that mechanism's authoritative configuration or test, with a concise root
pointer. The audit SHALL NOT propose a generic "documentation" destination and SHALL NOT create a catch-all
extracted-guidance directory, and a proposed new destination SHALL have a clear owner, a stable scope, and evidence that
more than one future task can discover and use it. Internal control flow whose alternatives yield the same result SHALL
stay in `AGENTS.md` even where a spec describes the same outcome; only a mechanism whose change would change the
capability's specified behavior is a candidate. Before reporting a candidate the audit SHALL test the retrieval path —
starting from the root instruction set, stating the task trigger, following only the retained root text and normal
project discovery, and confirming the destination is reached before the governed decision must be made and carries every
transferred requirement and verification step. A failed retrieval path to an **existing** destination remains a reason
to retain the source content, and is not an extraction opportunity. For an opportunity the audit SHALL NOT claim that a
destination which does not exist passed the test: instead it SHALL verify whether the proposed retained root entry point
could name the trigger and the destination deterministically, record the missing target or discovery mechanism as the
blocking precondition, and require the dedicated change to create the target, add the retained root pointer, and run the
full test before the extraction becomes applicable.

The audit SHALL additionally report, as an **extraction opportunity** kept separate from its findings, a source unit
that is likely too narrow or too costly for root `AGENTS.md` but whose destination does not yet exist — and which
therefore cannot yet pass the retrieval-path test. An opportunity is not a finding: it SHALL NOT be counted in the
verdict line, SHALL NOT be reported as a removal or a route candidate, and SHALL NOT be applied by the standing
reduction unit — it is a design recommendation for the owner, who selects it and carries it out in a dedicated change.
The audit SHALL report an opportunity only where the source is materially costly in root guidance and the destination
type can be justified from the content's class, and it SHALL NOT report one merely to reduce the file's size: where the
source is global, cross-cutting, or cannot be retrieved before its decision is needed, the audit SHALL retain it at
root. Each opportunity SHALL state that its proposed destination is a design hypothesis rather than an existing source
of truth, and SHALL carry the source unit and its bounded content, the decision it governs and when it is needed, its
current loading cost and why that cost is material, its content classification, its recommended destination type, the
exact existing target or a proposed path with its owning artifact type, the missing precondition that keeps it from
being a candidate, the content that must remain at root until a destination is created and verified, the intended
root-to-destination retrieval path once a future change creates the target, and one recommended next action — retain,
create the destination in a dedicated change, or reject the extraction.

The audit SHALL additionally report, as an **advisory** class kept separate from its fix findings and reported without
severity, third-party inconsistency: a file the audit excludes (a generated `openspec-*` instruction file, or a vendored
`axon4to5-*` skill) that contradicts how this repository uses it. The class SHALL be bounded by a harm test — reported
only where the contradiction would mislead a workflow driven by the file, or instruct a pattern the repository's code or
conventions contradict, never a mere textual difference from the repository's own prose. Each item SHALL name the harm
and the decision it invites (report it upstream, re-vendor at a newer version, or change the repository's usage) and
SHALL NOT propose a local edit to the excluded file, which the exclusion rule forbids.

The audit SHALL additionally report, as an **accretion** class kept separate from both its fix findings and its advisory
class, the in-scope rules that are meta rather than product — a rule about the agent, its tooling, the per-change
workflow, or the documentation, as opposed to a fact about the product. Each item SHALL name the rule, the origin that
introduced it (established from the in-prose `captured:` marker where present, and from `git blame` / `git log -S`
otherwise), and the source used to establish it. The class SHALL be reported without severity and is not a defect: an
accreted rule may be correct and load-bearing, so the audit SHALL NOT propose removing or merging it for being meta — a
meta rule that governs no decision is a removal candidate under the conciseness analysis, not a victim of its class —
that is the user's decision on the report. The class's count SHALL use one fixed metric, so successive reports are
comparable: the number of distinct meta rule units carrying at least one `captured:` marker, each counted once however
many markers it carries — never the raw marker count, and never the number of items the report lists — and a rule
written before the marker convention carries none and falls outside the metric. The audit SHALL also state how many of
those rule units are new since the newest prior report under `docs/audits/`, reporting `none recorded` when no prior
report exists and that the figure is unavailable when it cannot be determined.

The audit SHALL additionally report, as an **authoring-system gap** class kept separate from its fix findings and
reported without severity, a recurring defect whose cause is a mechanism that produces or maintains the artifact rather
than the artifact's content. Each item SHALL name the mechanism, the gap in how that mechanism communicates or enforces
the applicable standard, the smallest correction at the mechanism, and how that correction would prevent or reduce the
recurrence. The class SHALL be bounded by evidence: a defect reported from a single artifact with no established link to
the mechanism that produced it is a content defect under the consistency axis, not an authoring-system gap, and the
class SHALL NOT become a route for speculative rules. The correction's handling SHALL follow the mechanism's scope: for
an in-scope mechanism (an agent definition, command, or skill under `.opencode/`) the audit proposes the correction
directly; for an editable project-owned mechanism outside the audit's scope (the `openspec/config.yaml` per-artifact
rules) it reports the gap with the correction routed to the change workflow, never applied as a local edit; and for an
excluded mechanism (a generated OpenSpec instruction file, or a vendored skill) it reports the item under the advisory
class and never proposes a local edit to it. A correction SHALL reference the authoritative standard rather than copy it
into the mechanism.

Every item the report carries SHALL also carry the shared report contract's per-item budget, which that requirement
owns, not this one.

#### Scenario: An agent-tooling audit is produced

- **WHEN** the main agent invokes the `agents-auditor` subagent (e.g. via the `/audit-agents` command)
- **THEN** it returns findings grouped by severity, covering consistency (contradictions, stale claims, dead
  cross-references, drift) and conciseness (duplication, trivia, length, placement) across `AGENTS.md` and the
  project-authored `.opencode/` files, each with a location and a suggested rewrite — plus, separately, any advisory
  third-party inconsistency, the accreted meta rules with their origins, and any authoring-system gap, with the merge,
  removal, and route candidate counts named in the verdict line

#### Scenario: Generated and vendored files are excluded

- **WHEN** the `agents-auditor` subagent selects what to audit
- **THEN** it excludes the OpenSpec instruction files written by `openspec update` and the vendored `axon4to5-*` skills,
  whose correct state is defined by their generator or upstream rather than by this repository — while a
  project-authored file with a similar name (the `opsx-tool-update` command) stays in scope

#### Scenario: Findings are verified against the repository

- **WHEN** the `agents-auditor` subagent flags a claim, enumeration, or cross-reference
- **THEN** it checks the claim against the repository (the referenced files, workflows, or specs) before reporting it,
  so a deliberate or still-true entry is not reported as stale

#### Scenario: The auditor does not edit files itself

- **WHEN** the `agents-auditor` subagent runs
- **THEN** it returns proposed findings and rewrites without modifying files; the main agent verifies and applies those
  the user approves

#### Scenario: An excluded file inconsistent with our usage is reported as advisory

- **WHEN** an excluded file (a generated instruction file or a vendored skill) contradicts how this repository uses it —
  for example a vendored migration skill instructing a pattern the codebase no longer follows
- **THEN** the audit reports it in a separate advisory class, naming the harm and the decision it invites (report
  upstream, re-vendor, or change our usage), and never proposes a local edit to that file

#### Scenario: A mere textual difference is not reported

- **WHEN** an excluded file differs from the repository's own prose but would not mislead a workflow or instruct a
  contradicted pattern
- **THEN** the audit does not report it, so the advisory class stays limited to contradictions that would change what a
  reader does

#### Scenario: Accreted meta rules are reported with their origin

- **WHEN** the `agents-auditor` subagent audits `AGENTS.md`
- **THEN** it reports the in-scope rules that are meta rather than product as a separate accretion class, each with the
  origin that introduced it (the `captured:` marker where present, `git blame` / `git log -S` otherwise) and the source
  used, without proposing that the rule be removed or merged for being meta

#### Scenario: The accretion count uses one fixed metric

- **WHEN** the `agents-auditor` subagent reports its accretion class
- **THEN** its verdict states the count as the number of distinct meta rule units carrying at least one `captured:`
  marker, each counted once regardless of how many markers it carries, together with the number of those rule units new
  since the newest prior report under `docs/audits/` (`none recorded` when no prior report exists), so the count is
  comparable across successive reports

#### Scenario: Merge and removal candidates are standing findings

- **WHEN** the `agents-auditor` subagent audits `AGENTS.md`
- **THEN** it reports merge candidates (with a merged text that preserves every anchor and piece of evidence the
  originals carried, and never blending two distinct lessons — or, where that text would only restate a rule the file
  already carries, a deletion of the duplicate, still counted as a `remove`, saying which) and removal candidates (rules
  that govern no decision — trivia, not a rule — with what would be lost and whether git preserves it) — as findings for
  the owner, not actions

#### Scenario: A merge candidate's pointer target is verified

- **WHEN** a merge candidate's merged text points at a spec or an ADR for content it removes
- **THEN** the audit verifies the target carries the delegated content — its behavior and the identifiers, declarations,
  and gate conditions the delegated text names — and keeps in the merged text any delegated item the target does not
  carry, saying which it kept for that reason

#### Scenario: A rule enforced by a deterministic mechanism is a route candidate

- **WHEN** the `agents-auditor` subagent finds a rule whose subject an in-place deterministic mechanism already enforces
  (a build or CI gate, a lint or test, a CLI validation, or a deterministic workflow step such as the archive-time spec
  sync)
- **THEN** it reports a route candidate naming the mechanism and proposing to reduce the rule to a pointer at it, or to
  move the rule's content to the spec or ADR that owns it, with what would be lost — as a candidate for the owner, not
  an action

#### Scenario: A rule detectable only by review is not a candidate

- **WHEN** the only detector of a rule's violation is a review — a human's or a review subagent's — and no deterministic
  mechanism enforces it
- **THEN** the audit does not report the rule as a removal or route candidate, because review is not a gate and the rule
  may itself be what makes the violation visible to the reviewer

#### Scenario: A recurring defect is traced to the authoring mechanism

- **WHEN** the `agents-auditor` subagent finds a defect that recurs across the artifacts one mechanism produces — a
  skill that keeps emitting dense procedural prose, or a command that omits a step the workflow needs
- **THEN** it reports an authoring-system gap naming the mechanism, the gap in how that mechanism communicates or
  enforces the standard, the smallest correction at the mechanism, and how the correction would prevent or reduce the
  recurrence — separate from its fix findings and without severity

#### Scenario: A one-off defect is not an authoring-system gap

- **WHEN** a defect appears in a single artifact and no link to the mechanism that produced it is established
- **THEN** it is reported as a content defect under the consistency axis, not as an authoring-system gap

#### Scenario: An authoring-system gap in an excluded mechanism is advisory

- **WHEN** the mechanism responsible for a recurring defect is excluded (a generated OpenSpec instruction file, or a
  vendored skill)
- **THEN** the audit reports the gap under the advisory class, naming the harm and the decision it invites, and never
  proposes a local edit to the excluded mechanism

#### Scenario: An instruction-budget ledger is reported

- **WHEN** the `agents-auditor` subagent audits `AGENTS.md`
- **THEN** it reports an instruction-budget ledger naming the file's total size, its size by top-level section, the
  effective bound on the always-loaded surface and its source, the first rule beyond the derived effective threshold,
  the growth since the newest prior report under `docs/audits/`, and the sections and `captured:`-marked rule units
  responsible for that growth

#### Scenario: The ledger states the bound's source and invents no limit

- **WHEN** the loading mechanism applies no size limit of its own
- **THEN** the ledger says so and reports the effective bound with the source that establishes it, rather than reporting
  a configured limit that does not exist

#### Scenario: A reduction candidate carries its decision and its cost

- **WHEN** the auditor reports a merge, removal, route, or extraction candidate
- **THEN** the candidate names its location, the decision its rule changes, the task or condition that requires it, the
  unique content the change would otherwise lose, its current loading cost, its disposition, and the evidence that the
  surviving guidance preserves the same obligation

#### Scenario: An extraction candidate meets every condition

- **WHEN** the auditor proposes an extraction candidate
- **THEN** every condition the extraction analysis states holds, and the candidate names its destination type, the
  retained root text, and the authoritative destination

#### Scenario: A failed retrieval path is a reason to retain the content

- **WHEN** the retrieval-path test cannot reach the destination before the governed decision must be made, or the
  destination omits a transferred requirement or verification step
- **THEN** the audit reports the failure and retains the content rather than proposing the extraction

#### Scenario: Internal control flow is not extracted

- **WHEN** a spec describes the same outcome but changing the rule's mechanism would not change the capability's
  specified behavior
- **THEN** the audit keeps that control flow in `AGENTS.md` and does not propose extracting it

#### Scenario: A rule is not a removal candidate for being lengthy

- **WHEN** a rule is old, uncommon, or lengthy but governs a decision
- **THEN** it is not reported as a removal candidate, and the reason it stays is stated

#### Scenario: Guidance is not moved where the reader would infer it

- **WHEN** the retained root text would leave a warning, an imperative, an exception, or a target-less fact to be
  inferred
- **THEN** the audit keeps the rule at root rather than proposing the move

#### Scenario: An extraction opportunity is reported where no destination exists

- **WHEN** a source unit is materially costly in root guidance but no existing project-authored destination owns its
  subject
- **THEN** the audit reports an extraction opportunity naming its content classification, its recommended destination
  type, the proposed destination with its owning artifact type, and the missing precondition, and states that the
  proposed destination is a design hypothesis rather than an existing source of truth

#### Scenario: An extraction opportunity is not a finding and is never applied

- **WHEN** the audit reports an extraction opportunity
- **THEN** it is excluded from the verdict line, is not reported as a removal or a route candidate, and the standing
  reduction unit never applies it

#### Scenario: A global rule is retained rather than extracted

- **WHEN** the source is global, cross-cutting, or cannot be retrieved before its decision is needed
- **THEN** the audit retains it at root and reports no extraction opportunity for it

#### Scenario: A failed path to an existing destination is not an opportunity

- **WHEN** a destination exists but the retrieval path to it fails
- **THEN** the audit reports the failure as a reason to retain the source content, not as an extraction opportunity

#### Scenario: A proposed new destination needs an owner and a scope

- **WHEN** the audit proposes a destination that does not exist yet
- **THEN** it names the owning artifact type, a stable scope, and evidence that more than one future task can discover
  and use it, and it proposes no generic documentation destination and no catch-all extracted-guidance directory

#### Scenario: The ledger carries the opportunities subsection

- **WHEN** the audit reports its instruction-budget ledger
- **THEN** a separate extraction-opportunities subsection follows the ready candidates, is excluded from the verdict
  counts, states that its items need the owner's selection and a dedicated design or implementation change,
  distinguishes an existing destination that failed retrieval from a destination that does not yet exist, and collapses
  "no opportunity" to one line

### Requirement: The architecture is audited for drift from its recorded decisions

The repository SHALL provide an `architecture-auditor` agent subagent that audits the project's architecture — the ADRs
under `docs/adr/` together with the architectural surface (the service boundaries, the module dependency graph, and the
spec corpus's capability decomposition) — for **drift**, and reports its findings without editing files. The audit SHALL
report in two clearly separated sections: verified findings and advisory observations. Verified findings SHALL cover, at
least: an ADR's Decision contradicted by the implementation; an ADR `Status` that is stale, or whose supersession is not
recorded as `Superseded by ADR-NNNN`; a cross-reference to an `ADR-NNNN` that does not exist; a cross-cutting structural
decision made in the code or configuration with no ADR; a dependency or service-boundary direction the architecture does
not sanction; and a spec role-group decomposition that no longer matches the module or service structure. Each finding
SHALL be verified against the repository rather than inferred from the documents alone, and SHALL be reported with its
location and a concrete suggested correction. Advisory observations SHALL carry design judgment for which the
architecture provides no reference to check against — cohesion and coupling, decomposition, and apparent gaps in the ADR
set — and SHALL be reported separately from findings, without severity, and SHALL NOT be treated as defects to fix. The
audit SHALL NOT verify behavior against the implementation (the change workflow's review loop and the archive-time spec
sync own that), SHALL NOT audit the spec corpus's internal structure (the `specs-auditor` owns that), and SHALL NOT
re-check a property an existing gate already enforces. The subagent SHALL propose its findings without modifying files;
the main agent verifies and applies those the user approves.

A finding SHALL establish which artifact is authoritative for the disputed claim under the project's documented
conventions before it asserts a violation: the implementation does not automatically override an accepted decision, and
an accepted decision does not automatically make the implementation correct. Each finding SHALL be established by
inspecting both sides — the recorded decision or convention, and the implementation or configuration it names — and
SHALL NOT be inferred from naming, file placement, or superficial similarity; a dependency edge is a finding only where
a recorded rule forbids it, so the applicable boundary rule is established first. Where authority cannot be established
— two records contradict each other, a later decision supersedes an earlier one but the record is stale, or a deliberate
exception explains the current state — the item SHALL be reported as an advisory observation, not as a verified
violation, and the audit SHALL NOT invent a precedence rule the repository does not state. Symptoms of one underlying
mismatch SHALL be reported as one item when they share a root cause and a correction, and independent violations SHALL
NOT be merged; where one root cause yields both a contradiction and a missing rationale, it SHALL be one finding with a
short note on the documentation gap rather than the same issue repeated in the advisory section. A cohesion, coupling,
or decomposition observation SHALL be bounded: it is reported only where it identifies a concrete inconsistency or a
meaningful missing decision within the audit's scope, never as a general review. The audit SHALL proceed in stages,
reusing what an earlier stage collected rather than re-reading the same files: orientation (the ADR conventions, the
ADRs, the architectural guidance, the module build files, and the spec role groups); structural verification (the
dependency edges, the service boundaries, the UI layer constraints, the cross-references, and the spec-decomposition
fit); targeted verification (only the discrepancies and missing-rationale candidates the earlier stages identified,
searching for exceptions, consumers, implementation evidence, and relevant external facts); and report validation
(re-checking every item against the evidence and the exclusions).

Every item the report carries SHALL also carry the shared report contract's per-item budget, which that requirement
owns, not this one.

#### Scenario: An architecture audit is produced

- **WHEN** the main agent invokes the `architecture-auditor` subagent (e.g. via the `/audit-architecture` command)
- **THEN** it returns findings and advisory observations in two separated sections, the findings covering ADR drift, ADR
  `Status` integrity, unrecorded decisions, dependency/service-boundary direction, and spec-decomposition fit, each with
  a location and a suggested correction

#### Scenario: Findings are verified against the repository

- **WHEN** the `architecture-auditor` subagent flags an ADR, a cross-reference, or a boundary
- **THEN** it checks the claim against the repository (the ADR file, the module and service configuration, the
  referenced code or spec) before reporting it, so a still-true decision or a deliberate structure is not reported as
  drift

#### Scenario: Advisory observations are reported separately from findings

- **WHEN** the `architecture-auditor` subagent forms a design judgment (cohesion, decomposition, ADR-set gaps)
- **THEN** it reports it in the advisory section, without severity and not as a defect, so the main agent does not treat
  it as actionable without the user's decision

#### Scenario: Behavior and intra-corpus checks are out of scope

- **WHEN** the `architecture-auditor` subagent audits the architecture
- **THEN** it does not verify behavior against the implementation, does not audit the spec corpus's internal structure,
  and does not re-check a property an existing gate already enforces

#### Scenario: The auditor does not edit files itself

- **WHEN** the `architecture-auditor` subagent runs
- **THEN** it returns proposed findings and corrections without modifying files; the main agent verifies and applies
  those the user approves

#### Scenario: A disagreement between records is resolved by authority

- **WHEN** the implementation and a recorded decision disagree, or two records contradict each other
- **THEN** the audit establishes which artifact is authoritative for the disputed claim under the project's documented
  conventions before asserting a violation, and where authority cannot be established — a stale supersession, a
  deliberate exception, or a genuine contradiction between records — it reports the item as an advisory observation
  rather than a verified violation

#### Scenario: A dependency edge is a finding only where a recorded rule forbids it

- **WHEN** the `architecture-auditor` subagent finds a dependency edge that looks undesirable
- **THEN** it establishes the applicable boundary rule before reporting, and reports the edge as a finding only where a
  recorded decision, convention, or module-graph rule forbids it — never from the edge's shape alone

#### Scenario: Symptoms of one mismatch are reported as one item

- **WHEN** several symptoms share one underlying architectural mismatch and one correction
- **THEN** the audit reports them as one item, and it does not merge independent violations that merely concern the same
  ADR or module; where one root cause yields both a contradiction and a missing rationale, it is one finding with a
  short note on the documentation gap, not the same issue repeated in the advisory section

#### Scenario: The audit proceeds in bounded stages

- **WHEN** the `architecture-auditor` subagent audits the architecture
- **THEN** it orients first (the ADR conventions, the ADRs, the architectural guidance, the module build files, the spec
  role groups), verifies the structure next (the dependency edges, the boundaries, the UI layers, the cross-references,
  the spec-decomposition fit), investigates only the discrepancies and candidates those stages identified, and validates
  the report against the evidence and the exclusions — reusing what earlier stages collected

#### Scenario: A general cohesion or coupling observation is not reported

- **WHEN** a cohesion, coupling, or decomposition observation identifies no concrete inconsistency and no meaningful
  missing decision within the audit's scope
- **THEN** the audit does not report it, so the advisory section stays a list of concrete items rather than a general
  design review

### Requirement: Unexplained design intent is surfaced for clarification

The `architecture-auditor` subagent SHALL report, in its advisory section, the deliberate choices and deliberate
absences whose rationale is not recorded in the repository. It SHALL sweep the surfaces where a deliberate decision
implies a rejected alternative: dependency `exclude(...)` declarations; major-version-suppressed coordinates in
`config/dependency-updates/major-disabled.txt`, coordinates held back at a version line in
`config/dependency-updates/hold-back.txt`, and the web UI packages for which the npm report suppresses majors in
`config/web-ui-updates/major-disabled.txt`; suppression annotations that encode a design choice (`@SuppressWarnings`)
and deprecated-API usages the project still carries; deferrals or band-aids recorded in an ADR or parked in
`docs/ideas.md`; and a record whose premise appears to have expired — a decision deferred in an ADR whose recorded
`Revisit when:` condition appears met, or a parked `docs/ideas.md` entry whose work has already landed. It SHALL hold
the condition against the repository where it names a checkable fact (an external coordinate or a consumed artifact's
adoption), and the parked entry against the change, commit, workflow or mechanism the entry describes rather than an
artifact it merely cites, and SHALL NOT report one that names no such fact. For the first class it SHALL verify by
searching the repository that no rationale is recorded before reporting an item, and SHALL NOT report a choice whose
rationale is already recorded; the deferred-and-parked-record class is the inverse — its rationale is recorded, and it
is reported only when that record's premise appears to have expired. Each item SHALL name the deliberate choice or
absence with its location, and SHALL state the question whose answer would record the missing rationale — for a record
whose premise appears to have expired, the recorded condition or parked entry and the signal that appears to meet it.
The items SHALL be advisory: reported without severity, and SHALL NOT be treated as defects to fix, because only the
project owner can say which unrecorded rationales matter.

A rationale need not be located in an ADR to count as recorded: the search SHALL cover the ADRs, the specs, `AGENTS.md`,
the build files, the configuration, the source, and the established conventions, and a choice SHALL be reported only
where its rationale's absence is consequential enough for the owner to decide — a low-impact local implementation detail
is not an architectural decision. Each `Revisit when:` condition SHALL be classified before it is reported —
repository-checkable, externally checkable, dependent on human judgment, or not currently verifiable — and the deferral
SHALL be reported only where the condition is demonstrably satisfied; a newer published version alone does not satisfy a
condition that requires compatibility, adoption, or support, and where external verification is unavailable or
inconclusive the audit SHALL state no conclusion about the condition, since an inability to verify SHALL NOT become an
expiry claim. An expired condition triggers a review, not a predetermined change. A parked entry SHALL distinguish its
proposed work having landed, only part of it having landed, and a different mechanism achieving the same objective; an
artifact the entry merely cites SHALL NOT establish completion.

Every item the report carries SHALL also carry the shared report contract's per-item budget, which that requirement
owns, not this one.

#### Scenario: An unexplained deliberate choice is surfaced as a question

- **WHEN** the `architecture-auditor` subagent sweeps a deliberate choice (a dependency exclusion, a suppressed
  major-version coordinate, a held-back coordinate, a suppression annotation, a retained deprecated API, or a recorded
  deferral) and finds no rationale recorded for it
- **THEN** it reports the choice and its location in the advisory section together with the question whose answer would
  record the rationale, so the owner can triage it

#### Scenario: A recorded rationale is not reported

- **WHEN** the `architecture-auditor` subagent sweeps a deliberate choice whose rationale is already recorded (a
  suppressed major-version coordinate with a requirement in a spec, a deferral with an ADR, or a suppression convention
  in `AGENTS.md`)
- **THEN** it does not report that choice, so the audit's output stays a list of open questions

#### Scenario: Intent-clarification items are advisory

- **WHEN** the `architecture-auditor` subagent reports where clarification of intent is missing
- **THEN** it reports those items in the advisory section, without severity and not as defects, so the main agent does
  not "fix" them without the user's decision

#### Scenario: A deferred decision whose condition appears met is surfaced

- **WHEN** the `architecture-auditor` subagent finds an ADR carrying a `Revisit when:` condition that appears met and
  the deferral's recorded decision still stands — an external coordinate gate whose pinned coordinate has since
  published a newer major, or a third-party-adoption gate the consumed artifacts now satisfy
- **THEN** it reports the ADR, the recorded condition, and the signal that appears to meet it, in the advisory section,
  so the owner can decide whether to act

#### Scenario: A deferral whose condition is not repository-decidable is not reported

- **WHEN** an ADR's `Revisit when:` condition names no repository fact the auditor can check (for example a capacity
  trigger) and no such fact has changed
- **THEN** the auditor does not report that ADR, since the condition is not one the repository's own state can meet or
  refute

#### Scenario: A parked idea whose work has landed is surfaced

- **WHEN** the `architecture-auditor` subagent sweeps `docs/ideas.md` and finds a parked entry whose own work exists in
  the repository (its change, commit, workflow or mechanism is there)
- **THEN** it reports the entry and its location in the advisory section together with the question whose answer would
  retire it, so the owner can confirm and remove it

#### Scenario: A parked idea that merely cites an artifact is not reported

- **WHEN** a parked entry only names an artifact as context (a companion change, a workflow, or a file it refers to) and
  the work the entry itself describes does not exist
- **THEN** it is not reported, so the audit's output stays a list of open questions

#### Scenario: A deferral's condition is classified before it is reported

- **WHEN** the `architecture-auditor` subagent evaluates an ADR's `Revisit when:` condition
- **THEN** it classifies the condition (repository-checkable, externally checkable, dependent on human judgment, or not
  currently verifiable) and reports the deferral only where the condition is demonstrably satisfied; a newer published
  version alone does not satisfy a condition requiring compatibility, adoption, or support, and where external
  verification is unavailable or inconclusive it states no conclusion rather than turning the gap into an expiry claim

#### Scenario: A parked idea whose work only partly landed is reported as partial

- **WHEN** a parked entry's proposed work has only partly landed, or a different mechanism achieves the same objective
- **THEN** the audit says which part appears to have landed (or that a different mechanism does), and does not report
  the entry as fully implemented or silently unpark it

### Requirement: Report-producing subagents follow a shared report contract

Every subagent that reports findings to the main agent — the per-unit review and lesson-capture agents and the on-demand
auditors of the agent tooling, the spec corpus, the architecture, and the README — SHALL follow one shared output
contract, so a report is skimmable and its verdict is read first. The contract SHALL bound the report's shape; the
analysis a subagent performs is bounded by that subagent's own stated scope, not by this contract — an auditor or the
deep reviewer verifies its claims as thoroughly as its scope requires, while a bounded screen such as the quick review
performs only the pass its scope defines and says so.

#### Scenario: A report opens with its verdict

- **WHEN** a report-producing subagent returns its findings
- **THEN** the first line states the outcome — whether anything remains and how many items — and names the count of
  every class the report carries (findings, and any advisory or accretion class it reports)
- **AND** when the caller's request named a prior round's findings, that first line also names which of the report's
  classes repeat a class the prior round already raised
- **AND** a report with nothing to report says so in that first line rather than in a closing sentence

#### Scenario: Each item is budgeted

- **WHEN** the subagent reports a proposed addition, a finding, or an observation
- **THEN** the item names itself, its anchor (`file:line` where applicable), and one line of evidence
- **AND** the budget is per item, not a cap on the report's total, so a report with many genuine findings is longer than
  one with few

#### Scenario: Verified-and-set-aside candidates are summarized

- **WHEN** the subagent has verified a candidate and concluded it is already covered, rejected, or not a defect
- **THEN** it reports that in one line, or collapses several into one summary line, rather than a paragraph each

#### Scenario: A bounded pass does not present itself as an unbounded analysis

- **WHEN** a report-producing subagent's scope bounds the analysis it performs — the quick review's fixed scope and stop
  condition, for example
- **THEN** its report states that bound rather than implying the analysis was unlimited, so a reader does not read a
  bounded screen as a thorough pass

#### Scenario: No alternatives are offered

- **WHEN** the subagent proposes a change or a correction
- **THEN** it states the recommendation, not a menu of alternatives — the calling agent makes the choice

### Requirement: The README is audited for accuracy, design intent, and experience coverage

The repository SHALL provide a `readme-auditor` agent subagent that audits `README.md` — the human-facing showcase and
onboarding guide — for the drift no existing auditor can see, and reports its findings without editing files. The
auditor's scope SHALL be `README.md` only; the other documents (the ADRs, the retrospectives, `docs/`, and the
`openspec/specs/` corpus) are covered by other owners. The audit SHALL cover three axes:

- **Accuracy / consistency**: every claim in the README — commands, ports, versions, image and task names, links, and
  the OpenSpec-flow diagram's semantics — matches the repository, cross-checked against `AGENTS.md` and the spec corpus
  rather than trusted from the prose.
- **Design-intent fidelity**: the README keeps the shape its convention fixes — the section order, the step-by-step
  Getting Started path, Gradle tasks in preference to raw `docker compose`/`helm install` commands, curl as the single
  API example CLI, and the prompting-exercise narrative.
- **Coverage / experience surfacing**: the Cool Story and every human-visible capability the system offers (for example
  the saga auto-start, the live SSE timeline, the `setup-hosts` hostnames, and the Grafana access path) is mentioned in
  the README, cross-checked against what the system does.

Each finding SHALL be verified against the repository rather than inferred from the README's prose, and SHALL be
reported with its location and a concrete suggested rewrite. The audit SHALL report subjective quality — prose,
structure, redundancy, jargon, and flow beyond the documented shape — in a separate **advisory** class for the user's
judgment, never as a defect, because the README is hand-curated by design and its intended shape is to be preserved on
every edit. The subagent SHALL propose its findings without modifying files; the main agent verifies and applies those
the user approves, and the report SHALL follow the shared report contract.

Every item the report carries SHALL also carry the shared report contract's per-item budget, which that requirement
owns, not this one.

#### Scenario: A README audit is produced

- **WHEN** the main agent invokes the `readme-auditor` subagent (e.g. via the `/audit-readme` command)
- **THEN** it returns findings across the accuracy/consistency, design-intent fidelity, and
  coverage/experience-surfacing axes, each with a location and a suggested rewrite, plus a separate advisory class for
  subjective quality, and names the finding and advisory counts in its verdict line

#### Scenario: A claim is verified against the repository

- **WHEN** the `readme-auditor` subagent flags a claim in the README (a count, a port, a version, a task name, or a
  diagram's semantics)
- **THEN** it checks the claim against the repository — the build files, the Helm values, the spec corpus, or
  `AGENTS.md` — before reporting it, so a deliberate or still-true statement is not reported as wrong

#### Scenario: A human-visible capability missing from the README is reported

- **WHEN** the system offers a capability a person can see or experience and the README does not mention it
- **THEN** the audit reports it under coverage/experience surfacing, naming the capability and where the README should
  surface it

#### Scenario: Subjective quality is advisory, not a defect

- **WHEN** the auditor finds a prose, structure, or redundancy concern beyond the README's documented design intent
- **THEN** it reports the concern in the advisory class for the user's judgment rather than as a finding to fix, since
  the README is hand-curated by design

#### Scenario: The auditor does not edit files itself

- **WHEN** the `readme-auditor` subagent runs
- **THEN** it returns proposed findings and rewrites without modifying files; the main agent verifies and applies those
  the user approves

### Requirement: The on-demand audits are also available on a schedule

The repository's audits of its agent tooling, its spec corpus, and its architecture SHALL be runnable unattended on a
schedule, in addition to their on-demand `/audit-*` triggers, so the reconciliation they perform does not depend on a
human asking. The scheduled run SHALL use the scheduled-audit mechanism `showcase/quality/merge-governance` specifies
(the "Repository audits run on a schedule and on demand" requirement). Its cadence SHALL serve the reconciliation the
audits exist for (the consolidation that counters the accretion the capture loop produces) rather than being a bare
reminder. The `readme-auditor` SHALL remain on-demand only rather than joining the scheduled set: it verifies the
human-facing `README.md` against the repository, an accuracy-and-coverage check distinct from the reconciliation of the
agent's own machinery the schedule exists for. The `experience-analyzer` SHALL likewise remain on-demand only: its
retrospective is a narrative judgment about a period rather than a reconciliation verifiable against the repository, and
its richest input — what went wrong that no diff captures — is available only in the session that lived it, so a
scheduled run could not supply it. A scheduled run that _produces_ the retrospective SHALL NOT be used in its place,
because it automates the decision rather than the trigger and still lacks the session-only context; a scheduled check
that only _reports_ what has accumulated since the newest retrospective, and otherwise stays silent, is not such a run —
it surfaces the trigger while leaving the decision and the analysis to the on-demand invocation.

#### Scenario: The audits run unattended on their schedule

- **WHEN** the scheduled audit workflow fires
- **THEN** it runs the agents-auditor, specs-auditor, and architecture-auditor audits

#### Scenario: The scheduled audits do not replace the on-demand triggers

- **WHEN** a maintainer invokes an audit on demand (e.g. `/audit-agents`)
- **THEN** the on-demand trigger still runs that single audit, and the scheduled workflow's existence does not change it

### Requirement: Audit reduction candidates are applied by a standing owner-gated unit

The repository SHALL provide a standing, owner-gated unit, invoked by an `/apply-audit-reductions` command, that applies
an audit report's reduction candidates (its merge, removal, route, and extraction findings), routing a content-move to
the change workflow, so they do not age. It SHALL never apply an **extraction opportunity**, which is not a candidate:
an opportunity is a design recommendation the owner carries out in a dedicated change. It SHALL present the candidates
for the owner's selection, apply only the approved ones, and pass the per-unit review gate before the change ships; it
SHALL NOT run automatically.

#### Scenario: A report's reduction candidates are presented and applied

- **WHEN** the owner invokes the unit for the newest report under `docs/audits/` (a date carrying more than one report
  is offered for the owner to choose), or for one they name
- **THEN** it presents that report's reduction candidates and applies only the ones the owner approves

#### Scenario: An applied merge applies the candidate's merged text

- **WHEN** the unit applies an approved merge candidate
- **THEN** it applies the candidate's merged text, or deletes the duplicate the candidate flags

#### Scenario: An applied route reduces the rule to a verified pointer

- **WHEN** the unit applies an approved route candidate that reduces the rule to a pointer
- **THEN** it reduces the rule to a pointer at the target the candidate names, following the capture bullet's
  pointer/trim rule

#### Scenario: A route candidate that moves content into a spec or ADR is routed to a change

- **WHEN** an approved route candidate proposes moving the rule's content into the spec or ADR that owns it
- **THEN** the unit routes that edit through the normal change workflow rather than editing the spec or ADR inline

#### Scenario: An applied removal deletes a rule that governs no decision

- **WHEN** the unit applies an approved removal candidate
- **THEN** it deletes the rule the candidate names, recording what the deletion loses and whether git preserves it

#### Scenario: An applied reduction clears its parked entry

- **WHEN** the report's candidate was parked in `docs/ideas.md`
- **THEN** the unit removes the applied entry

#### Scenario: Application is owner-gated and reviewed

- **WHEN** the unit applies reductions
- **THEN** it does so only on the owner's selection, and it runs the full per-unit review gate over the result — the
  pre-write pass and its claims table, the formatter and mechanical checks, the `review-quick` loop with the
  session-continuity preflight, the deep-review routing decision, a required deep review, the capture decision, and the
  owner's manual review pass — before the change ships

#### Scenario: An extraction candidate's application is routed to a change

- **WHEN** the owner approves an extraction candidate
- **THEN** the unit routes the move to the normal change workflow rather than editing the destination inline, since the
  move creates or changes a destination that the change owns

#### Scenario: An extraction opportunity is never applied

- **WHEN** the report carries an extraction opportunity
- **THEN** the unit does not present or apply it, because an opportunity is a design recommendation rather than a
  candidate

### Requirement: AGENTS.md entries follow a stated authoring standard

`AGENTS.md` SHALL be maintained as an operational guide for future agents — not a narrative log of development activity
or a general knowledge repository — so every entry states reusable knowledge that constrains behavior, guides a
decision, defines a reusable procedure, prevents a known failure, or supplies the context such a decision needs. Before
adding knowledge, an author SHALL choose the entry's representation — a rule or invariant (behavior to preserve), a
procedure (a repeatable, ordered sequence), a decision rule (observable conditions that select an action), a gotcha (a
non-obvious failure mode, constraint, or recurring pitfall), or reference knowledge (architectural rationale or
background) — keep one authoritative definition for each subject and reference it rather than duplicating its contents,
and update an existing entry before adding a new one (for a capture's proposal, the capture requirement's
existing-bullet rule). An entry SHALL NOT record transient debugging detail, generic advice, or a fact that does not
change what a future agent does.

A procedure SHALL state, where applicable: its **Trigger** (when it applies), **Prerequisites** (what must be known or
available), **Procedure** (the steps in order), **Decision points** (how observable conditions select the next action),
**Failure handling** (what to do when an action fails or the evidence is inconclusive), **Completion criteria** (when it
may stop), and **Verification** (how the intended result is established). A gotcha SHALL take the smallest structure
that makes it actionable — Trigger→Action→Verification, Symptom→Diagnostic→Resolution, Condition→Decision→Action,
anti-pattern→correct-pattern, or Rule→Required-behavior — and SHALL state, where applicable, its **Trigger**, **Risk**,
**Cause** (only once confirmed), **Required action**, **Resolution** (only once established), **Verification**, and
**Scope**. A gotcha SHALL distinguish an observation, a hypothesis, a confirmed cause, and a verified resolution, and
SHALL present only the last as established: it SHALL NOT state an assumption as fact, SHALL preserve the observation and
a diagnostic approach when the cause is unknown rather than asserting an invented fix, and SHALL NOT generalize a
workaround beyond the conditions and versions under which it was shown to work.

Every project-specific claim SHALL be derived from repository evidence — contents, reproducible behavior, tests, or
configuration — and never invented, and a lesson sourced from untrusted content SHALL be verified against the repository
before it is recorded, whatever route the entry takes into the file (the capture's promotion gate already treats a
claim's source as part of it). Established guidance SHALL NOT be removed because it appears unusual or has not been
encountered recently; it is revised or removed only once its obsolescence is established. Where a lesson can be
incorporated into an existing workflow — as a preventive rule, a diagnostic step, a verification criterion, or a
targeted review check — it SHOULD be, rather than left as an isolated warning, and a procedure SHALL NOT be duplicated
across sections. After adding, modifying, consolidating, or removing instructions, the author SHALL re-read the affected
section and its context and check for duplication, contradiction, and unclear scope, confirm the surviving constraints
and knowledge remain, confirm each new project-specific claim is evidence-backed, and confirm a procedure's steps,
decisions, failure paths, and completion criteria can be executed without the reader inventing an essential step or an
undocumented assumption.

The standard SHALL be applied when `AGENTS.md` knowledge is authored and when it is audited: the `lesson-capture`
subagent (and the main agent's capture workflow) applies it to each proposed entry, and the `agents-auditor` subagent
applies it to each `AGENTS.md` entry it reads.

Every item the report carries SHALL also carry the shared report contract's per-item budget, which that requirement
owns, not this one.

#### Scenario: An entry is recorded in the right representation, once

- **WHEN** an author adds knowledge to `AGENTS.md`
- **THEN** it chooses the representation — a rule or invariant, a procedure, a decision rule, a gotcha, or reference
  knowledge — keeps one authoritative definition for the subject and references it rather than duplicating, and updates
  an existing entry before adding a new one

#### Scenario: A knowledge item that changes no future action is not recorded

- **WHEN** a candidate entry would record transient debugging detail, generic advice, or a fact that does not change
  what a future agent does
- **THEN** it is not added to `AGENTS.md`

#### Scenario: A procedure is executable without inventing steps

- **WHEN** an entry states a procedure
- **THEN** it carries, where applicable, its Trigger, Prerequisites, Procedure, Decision points, Failure handling,
  Completion criteria, and Verification, so a reader can execute it without reconstructing the intended order or
  inventing a missing decision

#### Scenario: A gotcha separates the observed from the proven

- **WHEN** an entry states a gotcha
- **THEN** it takes the smallest actionable structure and distinguishes an observation, a hypothesis, a confirmed cause,
  and a verified resolution, presenting only the last as established

#### Scenario: An unknown cause is preserved, not invented

- **WHEN** a failure's cause is unknown
- **THEN** the entry preserves the observation and a diagnostic approach rather than asserting an invented fix, and does
  not generalize a workaround past the conditions under which it was shown to work

#### Scenario: A claim is derived from repository evidence

- **WHEN** an author records a project-specific claim, or a lesson sourced from untrusted content
- **THEN** the claim is derived from repository evidence (or the untrusted lesson verified against the repository)
  before it is recorded

#### Scenario: A lesson is folded into the workflow it belongs to

- **WHEN** an author records a lesson that can be incorporated into an existing workflow — as a preventive rule, a
  diagnostic step, a verification criterion, or a targeted review check
- **THEN** it is incorporated there rather than left as an isolated warning, and a procedure is not duplicated across
  sections

#### Scenario: Existing guidance is revised or removed only once obsolete

- **WHEN** an author considers removing an existing instruction
- **THEN** it removes or revises the instruction only once its obsolescence is established, not because it appears
  unusual or has not been encountered recently

#### Scenario: An edit is validated before it is finished

- **WHEN** an author finishes adding, modifying, consolidating, or removing instructions
- **THEN** it re-reads the affected section and its context and checks for duplication, contradiction, unclear scope,
  surviving constraints, evidence-backed claims, and an executable procedure

#### Scenario: A capture proposal follows the standard

- **WHEN** the `lesson-capture` subagent (or the main agent's capture workflow) proposes an `AGENTS.md` entry
- **THEN** the proposal follows this standard — its representation, its shape, its evidence, and its validation — in
  addition to the capture requirement's promotion gate

#### Scenario: The audit reports an entry that violates the standard

- **WHEN** the `agents-auditor` subagent reads an `AGENTS.md` entry that violates this standard — a procedure or gotcha
  leaving a required slot unfilled, a claim stated as fact without evidence, or a subject whose representation is
  duplicated
- **THEN** it reports the entry as a finding with its location and a concrete rewrite that conforms it

### Requirement: Agent-authored technical artifacts follow a structured writing standard

Agent-authored technical artifacts — `AGENTS.md`, OpenSpec proposals, specifications, designs, and task lists, plans,
code reviews, and other repository documentation — SHALL follow the structured, scannable shape `AGENTS.md`'s
"Structured Writing Standard" section states, applied while the artifact is drafted rather than only in a later cleanup
pass, and each SHALL keep its own required template and sections.

Every item the report carries SHALL also carry the shared report contract's per-item budget, which that requirement
owns, not this one.

#### Scenario: A requirement is stated as its own scannable unit

- **WHEN** an agent authors an artifact that states several independent requirements, actions, or decisions
- **THEN** each is a separate bullet or numbered step rather than several packed into one paragraph, and an ordered
  procedure is numbered

#### Scenario: A condition is explicit

- **WHEN** the next action depends on a condition
- **THEN** the artifact states the condition and its branch (`IF`/`THEN`-style, or a decision-table row) rather than
  leaving the dependency implied

#### Scenario: The mandatory is separated from the explanatory

- **WHEN** an artifact states a normative requirement together with its rationale, background, or an example
- **THEN** the obligation is distinguishable from the explanation, so a reader finds what must be done without reading
  why

#### Scenario: The standard governs drafting, not only a later pass

- **WHEN** an agent creates or substantially edits an artifact
- **THEN** it organizes the intended content into its sections and lists before writing the prose, instead of leaving
  the structure to a formatting pass after the fact

#### Scenario: Artifact-specific conventions are preserved

- **WHEN** the standard is applied to an artifact with its own required format (an OpenSpec proposal, specification,
  design, or task list, an ADR, or a review report)
- **THEN** the artifact keeps every required section and semantic distinction (SHALL statements, `####` WHEN/THEN
  scenarios, task checkboxes), the standard shaping the content within those sections

#### Scenario: Dense prose is restructured on review

- **WHEN** an artifact is reviewed (`review-quick`, `review-thorough`) or audited (`agents-auditor`) and a passage packs
  several independently actionable statements into one paragraph or bullet, implies a condition, or merges mandatory
  text with its rationale
- **THEN** it is reported as a finding, with its location, the practical impact of the current structure, and a
  minimally invasive restructure, without changing the passage's technical meaning, normative strength, or required
  format, and without introducing a requirement the passage did not state

### Requirement: An audit's approved findings are delivered as separate units

An audit command SHALL separate report generation from applying findings: the report is presented and the owner selects,
and the applying is a unit of its own. A run with nothing actionable or approved SHALL report that and stop, creating no
unit. Approved findings whose routes or scopes differ SHALL be split into separate units, and unrelated fixes SHALL NOT
be batched because one report carried them.

#### Scenario: Report generation is separated from applying findings

- **WHEN** an audit command has presented its findings and the owner has selected from them
- **THEN** applying the selection is a unit of its own, not a continuation of the reporting run

#### Scenario: A run with nothing actionable stops without a unit

- **WHEN** an audit reports no finding the owner approves, or none that is actionable
- **THEN** the command reports that and stops, creating no unit

#### Scenario: Findings with different routes are separate units

- **WHEN** approved findings differ in delivery route or in scope
- **THEN** they are split into separate units, each scoped to its own artifacts and diff

#### Scenario: Unrelated fixes are not batched

- **WHEN** one report carries findings of unrelated subjects
- **THEN** they are not batched into one unit merely because the same report carried them

#### Scenario: An applying unit runs the full review gate

- **WHEN** a unit applies approved audit findings
- **THEN** it runs the full per-unit review gate, not the formatter and a quick review alone

### Requirement: An audit finding's delivery route follows its kind

An approved audit finding SHALL be delivered by the route its kind requires. The main specs SHALL stay immutable outside
archive: a spec change routes through an OpenSpec change with a delta spec, and a code change, or a command or
subagent-definition change altering spec-covered behavior, through the normal change workflow. An advisory observation
is the owner's decision, never applied automatically.

#### Scenario: A spec change routes through a change with a delta

- **WHEN** an approved finding changes a capability's behavior
- **THEN** it routes through an OpenSpec change carrying a delta spec, and the main spec is edited only at archive

#### Scenario: A definition change altering spec-covered behavior routes through a change

- **WHEN** an approved finding edits a command or subagent definition whose behavior a capability spec describes
- **THEN** it routes through the normal change workflow with that capability's delta, not a docs edit

#### Scenario: An advisory observation is the owner's decision

- **WHEN** a report carries an advisory observation
- **THEN** it is presented for the owner's decision and is never applied automatically

#### Scenario: A reduction candidate keeps its own route

- **WHEN** an approved finding is a merge, removal, route, or extraction candidate
- **THEN** it is applied by that kind's route, and an extraction opportunity is never applied at all

### Requirement: Project-authored command definitions that start a mutating unit carry the lifecycle preflight

A project-authored command that starts a mutating unit that ships SHALL state the lifecycle its unit owes: its own
branch from a freshly fetched `origin/main`; unrelated dirty changes preserved; work uncommitted until the manual
approval pass; and no commit, push, pull request, archive, or merge without that approval. It SHALL point at the
workflow owning the lifecycle and the review gate, and every `review-quick` invocation SHALL carry the
session-continuity preflight.

#### Scenario: A mutating command states the branch rule

- **WHEN** a project-authored command starts a mutating unit that ships
- **THEN** it states that the unit starts on its own branch from a freshly fetched `origin/main`

#### Scenario: A mutating command preserves unrelated work

- **WHEN** a unit the command starts finds unrelated dirty-worktree changes
- **THEN** the command preserves them rather than reverting or committing them

#### Scenario: Work stays uncommitted until approval

- **WHEN** a mutating command's unit has finished its edits
- **THEN** the work stays uncommitted until the owner's explicit approval, and no commit, push, pull request, archive,
  or merge happens before it

#### Scenario: A review invocation carries the session preflight

- **WHEN** a mutating command invokes `review-quick`
- **THEN** the invocation carries the session-continuity preflight the review gate's procedure owns

### Requirement: Check and test authoring guidance is maintained in its project skill

A check-and-test authoring reference SHALL exist as the project skill
`.opencode/skills/check-and-test-authoring/SKILL.md`, not as a section of the always-loaded `AGENTS.md`. Each mode the
skill records — how a check, test, or gate can fail, what it must read, and where its control is injected — SHALL carry
its required action, the confirmation that established it, the incident evidence behind it, and, where the rule has one,
its `captured:` provenance. `AGENTS.md` SHALL carry, at the point where a check, a test, or a gate is authored, the
instruction to load that skill before acting; it MAY name a mode as an example of what the skill holds, and SHALL NOT
restate a mode's rule.

A lesson SHALL be routed by the decision it governs, not by its label — authoring, correction, evidence, or both — as
the scenarios below define, and the skill SHALL never restate an evidence rule that lives at root.

#### Scenario: The skill records each mode with its evidence

- **WHEN** a maintainer inspects how a check or a test that can actually fail is authored
- **THEN** `.opencode/skills/check-and-test-authoring/SKILL.md` records each mode with its required action, the
  confirmation that established it, and its incident evidence, and preserves the `captured:` provenance a rule it holds
  carries

#### Scenario: Root guidance names the skill at the trigger

- **WHEN** an agent is about to write or change a test, a check, or a gate
- **THEN** `AGENTS.md`'s root guidance instructs it to load the `check-and-test-authoring` skill before acting

#### Scenario: A mode's rule is not restated at root

- **WHEN** a mode's rule is recorded in the skill body
- **THEN** `AGENTS.md`'s root guidance does not restate the rule — it may name the mode as an example — so the skill
  body is where that mode is maintained and corrected

#### Scenario: A new authoring mode is recorded in the skill

- **WHEN** a lesson's decision is how to author or verify a check, test, or gate
- **THEN** it is recorded as a section of the skill body, naming the mode it extends or stating that none covers it

#### Scenario: A correction updates the mode in place

- **WHEN** a recorded mode turns out to be incomplete or wrong
- **THEN** its section in the skill body is corrected, rather than a second rule being added to `AGENTS.md` or beside it

#### Scenario: An evidence rule stays at root and is not duplicated in the skill

- **WHEN** a lesson changes how evidence is interpreted — a search's completeness, a glob's vacuity, a reproduction's
  validity, or a report's "nothing to report" — and it is needed without authoring a check
- **THEN** it is recorded in `AGENTS.md`'s evidence rules, and the skill body does not repeat it

#### Scenario: A mixed lesson is split by decision

- **WHEN** a lesson carries an authoring half and an evidence half
- **THEN** the authoring half is recorded in the skill and the evidence half at root, so neither destination restates
  the other's rule
