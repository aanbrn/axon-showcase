# Design

## Context

The proposal's subject is the two reviewers and the loop that drives them. Their current shape was traced from the
repository, not from the prose about them:

| Fact                                                                                                                                | Where it lives                                                            | Deriving command                                                                                        |
| ----------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| `review-quick` is the loop's only automatic pass; `review-thorough` is manual only                                                  | `AGENTS.md`'s review-gate workflow and its thorough-review bullet         | `git show HEAD:AGENTS.md \| grep -n "Thorough-review subagent"`                                         |
| The quick pass's obligations today include class exhaustion, claims-table re-derivation, and boundary/caller/documentation analysis | the quick-review scenario of the `agent-skills` per-change requirement    | `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md`                                    |
| There is no `/review-quick` command; `review-quick` is invoked directly by the main agent                                           | `.opencode/commands/` (no such file)                                      | `ls .opencode/commands/`                                                                                |
| The loop's recorded cost                                                                                                            | `docs/retrospectives/2026-10-15.md`                                       | its baseline line: 10 units, 87 rounds, 145 compliance-miss findings, 1.67 per round                    |
| Class exhaustion was deliberately added to the quick pass                                                                           | `openspec/changes/archive/2026-10-13-exhaust-review-finding-classes/`     | `ls openspec/changes/archive/ \| grep exhaust`                                                          |
| The round ledger is quick-only and gated                                                                                            | `docs/review-rounds.tsv`, `build.gradle.kts`'s `verifyUniqueReviewRounds` | `git show HEAD:docs/review-rounds.tsv`'s header row and `build.gradle.kts`'s `verifyUniqueReviewRounds` |
| No risk-acceptance process exists                                                                                                   | nowhere in the repository                                                 | `git grep -niE "risk.accept\|accepted risk" HEAD -- '*.md' '*.yaml'` → no hits                          |

### The premise interrogation, and the conflicts the owner confirmed

This change is a review-loop change, and the repository records a decision about exactly that:

- `docs/ideas.md`'s 2026-10-15 "Park the review loop's rejected levers" entry parks **a fixed iteration cap** as a
  rejected lever (`bound-the-review-loop` rejected one), and states that "the comparison is what should decide any
  further review-loop change, not a fresh proposal".
- `docs/retrospectives/2026-10-15.md`'s recommended next direction is "**Measure the next window before changing the
  loop again**", against the baseline it commits.
- Class exhaustion was **added** to the quick pass, because a class otherwise surfaced one instance per round.
- `AGENTS.md` states the thorough reviewer "is intentionally **not auto-scheduled** (the expensive pass)".

Three points of the requested design meet those records, and the owner was asked before this change was written. The
owner confirmed proceeding as specified, with each difference recorded here rather than decided silently:

| #   | The design                                                                        | The record                                                                                                   | Why they differ                                                                                                                                                                                                                                                                                                                                                                                            |
| --- | --------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | A default budget of **two deep-review passes**, extensible by a recorded decision | A fixed iteration cap is a rejected lever; the baseline comparison should decide further review-loop changes | The rejected cap is the **quick loop's** — a count would stop a productive convergence loop — and this change leaves the quick loop uncapped, bounding it by scope instead. The two-pass budget bounds the **opt-in deep pass**, which is not the convergence loop, and it is a default with an explicit recorded extension rather than a hard stop. This is an owner decision, and it is recorded as one. |
| 2   | The quick pass no longer exhausts a finding class                                 | Class exhaustion was added to the quick pass as a remedy                                                     | The clause **moves** to the deep pass rather than disappearing: exhaustion is costly verification, and the requested design moves costly verification to the deep or deterministic mechanism. A class raised in a deep pass is still surfaced whole.                                                                                                                                                       |
| 3   | Deep review becomes risk-routed                                                   | `AGENTS.md` records it as deliberately not auto-scheduled                                                    | What is automated is the **decision**, not the review: `NOT_NEEDED` creates no session and spends no expensive-model invocation, so the cost the manual-only rule protected is now bounded by the routing floor instead of by a human remembering. The manual `/review-thorough` invocation is unchanged.                                                                                                  |

The baseline comparison the record asks for still applies, and this change keeps it measurable: the round ledger stays
the quick loop's record (below), so the committed baseline keeps comparing like with like.

### The fixed cost of a reviewer invocation

A reviewer's own instructions are small next to what the session loads around them:

| Input                                                                                                                                                                                                                          | Size                        |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | --------------------------- |
| `AGENTS.md` (delivered to a session as instructions; the installed binary's instruction plugin loads the nearest `AGENTS.md` for a session, and the OpenCode rules documentation states it is included in the model's context) | 3,610 lines / 355,624 bytes |
| `.opencode/agent/review-quick.md`                                                                                                                                                                                              | 103 lines / 9,445 bytes     |
| `.opencode/agent/review-thorough.md`                                                                                                                                                                                           | 36 lines / 2,331 bytes      |

So the pass's _scope_ — not its prompt — is what makes a round expensive, which is why the bound this change adds is a
scope and a stop condition rather than another paragraph of instructions.

### Capabilities the design relies on, verified rather than assumed

| Capability                                                                                                                         | Evidence                                                                                                                    |
| ---------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------- |
| A subagent runs in a child session, and passing its session id continues that conversation; a fresh call starts with fresh context | the OpenCode agents documentation (subagents create child sessions) and the Task tool's own contract                        |
| There is no state channel between sessions other than the prompt text, and no session-fork API                                     | the same contract: the hand-over _is_ the prompt; continuity _is_ the session id                                            |
| `permission.task` gates which subagents an agent may invoke, and `deny` removes one from the Task description                      | the OpenCode agents documentation                                                                                           |
| `steps` bounds an agent's agentic iterations, statically                                                                           | the OpenCode agents documentation — which is why it is **not** used as this design's budget (it cannot scale with the diff) |

## Goals / Non-Goals

**Goals:**

- `review-quick` is a bounded screen whose cost scales with the diff and the supplied artifacts.
- `review-thorough` is a risk-proportionate deep review, reached by one deterministic decision.
- The deep pass reuses the loop's proven session and hand-over mechanism instead of a parallel framework.
- The deep pass is bounded by a budget that cannot silently accept residual risk.
- The change is verifiable where the repository supports verification, and explicit about what it cannot verify.

**Non-Goals:**

- Replacing the round ledger or changing the digest's population.
- Changing `lesson-capture` or `experience-analyzer` triggers.
- Making the deep pass mandatory for every change.
- Adding a risk-acceptance process beyond the owner's recorded decision.

## Decisions

### Decision: the quick pass is bounded by a scope and a stop condition, not a number

The scope is the seven subjects the delta lists; the stop condition is that each supplied artifact and each changed file
is examined once. That scales with the diff and with the supplied set, which is what the requirement asks for, and it
needs no number to derive.

- **Alternative — a `steps` cap on the reviewer's agent:** rejected. `steps` is static, so it cannot scale with the
  diff; a small change would waste budget and a large one would be truncated mid-inspection.
- **Alternative — a per-file time or token budget:** rejected. The repository records a process count as a
  non-verifiable fact, and neither is derivable from the supplied set.

### Decision: costly verification moves to the deep pass rather than being deleted

Class exhaustion, exhaustive boundary/error-path/caller/configuration/documentation analysis, and claims-table
re-derivation leave the quick pass. Each becomes the deep pass's job: the delta adds a scenario requiring a deep pass to
exhaust a class it raises, and the deep pass's scope is the change, not a screen.

- **Alternative — keep exhaustion in the quick pass and bound it by the stop condition:** rejected. Exhaustion is a
  search of the artifact for every instance of a class's shape, which is precisely the repository-wide sweep the
  requested scope forbids; keeping it would make the stop condition a fiction.

### Decision: the routing decision has a deterministic floor the agent may only raise

`scripts/review-routing.py` derives a floor from the change's own facts — its changed paths and the declarations in its
diff, its delta spec's block kinds, and its recorded rounds — and returns `REQUIRED`, `RECOMMENDED`, or `NOT_NEEDED`
with the signals it matched. The agent's decision must be at or above that floor, and records the signals it relied on.
A missing, malformed, or failed derivation yields `REQUIRED`.

| Floor         | Signal class (derived from the change's own facts)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| ------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `REQUIRED`    | a changed path under a public contract (`showcase-command-api/`, `showcase-query-api/`, `showcase-query-proto/`, `showcase-projection-model/`); under an Axon domain surface (`showcase-command-service/.../showcase/command/`, `showcase-projection-service/.../showcase/projection/`, `showcase-query-service/.../showcase/query/`); matching persistence (`*.sql`, `db/**`); declaring a security filter chain (`SecurityWebFilterChain` or `SecurityFilterChain` — the gateway's is a bean in `showcase-api-gateway/.../ShowcaseApiApplication.java`); or under infrastructure and deployment (`helm/**`, `.github/**`, `config/**`, `docker-compose.yml`, `**/Dockerfile`); or a delta carrying an `ADDED`, `REMOVED`, or `RENAMED` requirement block |
| `RECOMMENDED` | no `REQUIRED` signal, but the unit's recorded rounds repeat a class (a non-`-` repeated field on at least two rows), which is evidence the quick loop is not converging                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| `NOT_NEEDED`  | no signal matched — a floor only: the agent still judges the change localized, low risk, and adequately verified                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |

- **Alternative — let the agent decide unaided:** rejected. It is not explainable or reproducible, and the fail-safe
  cannot be tested.
- **Alternative — derive the whole decision mechanically:** rejected. "Major specification deviation" and "a material
  defect an earlier review missed" are judgments, so the script supplies a floor and the agent supplies the judgment
  above it.

### Decision: the deep pass reuses the quick loop's session and hand-over mechanism

- The hand-over carries only the task-relevant context: the change identity, the current diff, the applicable artifacts,
  the prior findings with their statuses, the current pass, the known limitations, and whether the pass is an initial
  review or a fix verification.
- The same session serves follow-up passes on one change; a different change starts a fresh one.
- The failed-hand-over detection reuses the loop's existing recovery — a fresh session plus the transferred findings —
  and stops with a reported blocker when no safe recovery exists.

- **Alternative — a parallel orchestration framework for the deep pass:** rejected. The session and hand-over mechanism
  already exists and is specified for the quick loop; a second one would duplicate it and drift.

### Decision: the review gate owns whether a deep pass runs, and the `/review-thorough` command owns how

The routing decision is made in the review-gate workflow, which is the automatic path; the dedicated command carries the
procedure both paths need — creating or reusing the session, the hand-over contents, the pass count, and the status
record — and states that a manual invocation is an explicit run rather than a routing bypass.

- **Alternative — put the routing step in the command:** rejected. A human invoking `/review-thorough` has already
  decided, so routing there makes the command two-headed: route-then-review for the gate, review-only for a human.
- **Alternative — delete the command and put the procedure in `AGENTS.md`:** rejected. The procedure would join the
  always-loaded file — 3,610 lines / 355,624 bytes, loaded into every session — and the human would lose the trigger the
  repository's own rule requires a documented subagent to have.

### Decision: the two-pass budget is a default with a recorded extension, and it is the deep pass's

- The default budget is two full passes: the initial deep review, then an independent verification after the valid
  findings are fixed.
- A third full pass needs a recorded decision resting on the residual risk and the expected value.
- A targeted re-review may verify a localized fix and its impact radius, and it may not conceal an unresolved material
  finding.
- The budget never accepts residual material risk by itself: an unresolved blocking finding prevents normal completion,
  and an unresolved should-fix finding needs an explicit resolution or a recorded acceptance.

- **Alternative — no budget:** rejected by the requested design, and it would let the deep pass cost as much as the
  quick loop's worst unit, which the baseline already records as the system's most expensive failure.
- **Alternative — a hard cap with no extension:** rejected. A cap that cannot be exceeded would accept residual risk by
  construction, which the requirement forbids.

### Decision: the deep-review status has one record, and the round ledger stays the quick loop's

The deep pass's routing outcome, pass count, and each finding's location and status live on the change's review task in
its `tasks.md` — the same place the repository already records the capture's applied delta and the pre-write pass. The
deep pass does **not** append to `docs/review-rounds.tsv`.

- **Alternative — record deep passes in the round ledger:** rejected. The ledger's columns carry finding classes and
  compliance misses, which the deep pass does not report (it reports severities), and adding its rows would change the
  digest's population — invalidating the 2026-10-15 baseline that the record says must decide further review-loop
  changes.

### Decision: `ACCEPTED` is the owner's recorded decision, because no risk-acceptance process exists

The repository has no authorized risk-acceptance process (verified: no hits for a risk-acceptance term). The delta
therefore defines `ACCEPTED` as an explicit owner decision recorded with the reason and what it accepts — the pattern
the repository already uses for a deliberate trade (the archived `bound-the-review-loop` records its trade the same
way).

- **Alternative — cite an "existing authorized process":** rejected. Naming a mechanism with no referent is what the
  repository forbids; the delta defines the acceptance instead.

### Decision: the report contract's report-bound is distinguished from the analysis-bound

The shared contract currently reads "The contract SHALL bound the report, not the analysis". With a bounded quick pass
that is no longer true of the quick reviewer, and leaving it would let a bounded screen present itself as an unbounded
analysis. The delta changes the body to bound the report's shape and to state that a subagent's analysis is bounded by
its own scope, and adds a scenario requiring a bounded pass to say so.

- **Alternative — leave the shared contract and state the bound only in `review-quick.md`:** rejected. The shared
  contract is the requirement that owns the report shape, and a contradiction between it and the reviewer's own contract
  is exactly the drift the corpus is meant not to carry.

## Risks / Trade-offs

- **A bounded quick pass can miss a defect the old contract would have caught.** Mitigation: the routing floor sends any
  change touching a contract, a domain surface, persistence, security, infrastructure, or a new/removed requirement to
  the deep pass, and the ledger's repeated-class signal raises a change the quick loop is not converging on.
- **The mechanical floor can over-trigger.** Accepted deliberately: a false `REQUIRED` costs one deep pass, while a
  false `NOT_NEEDED` costs an unverified defect. The signals are listed in the report, so an over-trigger is visible and
  the signal list is correctable.
- **The record's "measure first" direction is not followed.** Recorded, not hidden: this is an owner decision, the
  baseline's comparability is preserved, and the quick-loop bound's effect remains measurable by the next window's
  digest.
- **Moving exhaustion to the deep pass loses it for a `NOT_NEEDED` change.** Accepted: a `NOT_NEEDED` change is
  localized and low-risk by the routing decision, and the quick pass still reports every instance it sees within its
  scope.
- **A deep pass's session can fail or stall.** Mitigation: the failed-hand-over detection is a scenario, and the
  recovery is the loop's existing one (a fresh session with the findings transferred, the lost continuity recorded).

## Claims table

| Claim                                                                                                                                           | Source / deriving command                                                                                                                                                                                   |
| ----------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The quick pass carries class exhaustion, claims-table re-derivation, and boundary/caller/documentation analysis today                           | `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md`, the quick-review scenario; `git show HEAD:.opencode/agent/review-quick.md`                                                            |
| `review-thorough` is manual only, and `AGENTS.md` records it as deliberately not auto-scheduled                                                 | `git show HEAD:AGENTS.md`, the thorough-review bullet                                                                                                                                                       |
| There is no `/review-quick` command                                                                                                             | `ls .opencode/commands/` (no such file; `review-thorough.md` exists)                                                                                                                                        |
| The loop's recorded cost is 10 units / 87 rounds / 145 compliance-miss findings (1.67 per round)                                                | `docs/retrospectives/2026-10-15.md`, its verbatim baseline line                                                                                                                                             |
| The 2026-10-15 parked entry rejects a fixed iteration cap and directs the baseline comparison to decide further changes                         | `git show HEAD:docs/ideas.md`, the 2026-10-15 entry                                                                                                                                                         |
| Class exhaustion was added to the quick pass                                                                                                    | `openspec/changes/archive/2026-10-13-exhaust-review-finding-classes/proposal.md`                                                                                                                            |
| The quick-loop bound was deliberately not a cap                                                                                                 | `openspec/changes/archive/2026-10-13-bound-the-review-loop/design.md`, its rejected "general loop cap" alternative                                                                                          |
| Session continuation, `permission.task`, and `steps` are real OpenCode capabilities, and `steps` is static                                      | the OpenCode agents documentation; the Task tool's own contract                                                                                                                                             |
| `AGENTS.md` is delivered to a session as instructions                                                                                           | the OpenCode rules documentation; the installed binary's instruction plugin                                                                                                                                 |
| The fixed-context sizes are 3,610 lines / 355,624 bytes for `AGENTS.md`, 103 / 9,445 for `review-quick.md`, 36 / 2,331 for `review-thorough.md` | `git show HEAD:.opencode/agent/review-quick.md \| wc -l -c`, `git show HEAD:.opencode/agent/review-thorough.md \| wc -l -c`, and `git show HEAD:AGENTS.md \| wc -l -c` (all three are edited by the change) |
| The round ledger is quick-only and gated                                                                                                        | `git show HEAD:docs/review-rounds.tsv`'s shape (one row per unit and round); `build.gradle.kts`'s `verifyUniqueReviewRounds`                                                                                |
| No risk-acceptance process exists                                                                                                               | `git grep -niE "risk.accept\|accepted risk" HEAD -- '*.md' '*.yaml'` → no hits                                                                                                                              |
| The routing signals name real repository surfaces                                                                                               | the module list (`ls -d showcase-*/`), the domain packages (`find showcase-command-service/src/main/java -type d`), and the security declaration (`grep -rn "SecurityWebFilterChain" --include='*.java' .`) |
| The delta carries the two requirement blocks verbatim apart from the stated edits                                                               | diff the delta against `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md`                                                                                                                 |
| No invocation path bypasses the gate                                                                                                            | `git grep -n "review-quick\|review-thorough" HEAD -- .opencode AGENTS.md README.md` (the tree's own copy changes with the change)                                                                           |
| The retrospective's recommended next direction is to measure the next window before changing the loop again                                     | `docs/retrospectives/2026-10-15.md`, its "Recommended next direction" (line 195)                                                                                                                            |
| The session and hand-over mechanism already exists and is specified for the quick loop                                                          | `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md`, the quick-review scenario's session clause                                                                                            |
| The repository's rule requires a documented subagent to ship a command trigger                                                                  | `git show HEAD:AGENTS.md`, the "A subagent is only invocable through a trigger" bullet                                                                                                                      |
| The change's review task is where the capture's applied delta and the pre-write pass are already recorded                                       | `git show HEAD:openspec/config.yaml`, its two `tasks` rules — "Include the per-unit `lesson-capture` decision as a task" and "Carry the pre-write derivation pass as a task" (task 3.3 edits that list)     |
| The archived `bound-the-review-loop` records its deliberate trade the same way                                                                  | `grep -n "A deliberate trade" openspec/changes/archive/2026-10-13-bound-the-review-loop/proposal.md` → 1                                                                                                    |
| The shared contract's current body reads "The contract SHALL bound the report, not the analysis"                                                | `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md`, the report-contract body                                                                                                              |

`scripts/claim-scan.py --base origin/main` also returns candidates that are not claims about evidence, dismissed by
kind: the change's own normative text and the sites it names (the delta's verbatim carry of the two requirement blocks
included); this unit's own appended `docs/review-rounds.tsv` rows; the signal table's path patterns, whose row is the
one deriving the routing signals from the repository's real surfaces; and the design's own deriving commands, quoted as
the rows' sources.
