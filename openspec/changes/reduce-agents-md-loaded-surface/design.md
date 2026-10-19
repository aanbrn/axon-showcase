# Design

## Context

### The problem, measured

`AGENTS.md` at `a36c3f7e` (`git rev-parse HEAD`, clean working tree) is **356,723 bytes across 3,623 lines**
(`wc -c -l AGENTS.md`). OpenCode loads the root `AGENTS.md` in full into every session; `.opencode/opencode.json`
configures no `instructions` array and no size cap, so the only bound is the discipline `AGENTS.md` states itself
("applying a capture should leave the file no larger than it was"). The 2026-10-19 audit's byte and line figures
(363,767 / 3,681) describe its own checkout; its ~71%-of-context and ~91k-token figures are **estimates**. Neither is a
constraint this change may adopt, and this change adopts neither.

Size by top-level section. Convention: a section's range runs from its `##` heading to its last non-blank line before
the next heading; the 15 title and inter-section blank lines fall outside every range, so the rows sum to 356,697 of the
file's 356,723 bytes and 3,608 of its 3,623 lines.

| Section                          |   Bytes | Lines |
| -------------------------------- | ------: | ----: |
| `## Gotchas`                     | 157,119 | 1,520 |
| `## OpenSpec Workflow Agreement` |  70,168 |   752 |
| `## Conventions`                 |  69,603 |   621 |
| `## Continuous Integration`      |  12,771 |   144 |
| `## Build & Test`                |  11,219 |   153 |
| `## Kubernetes Deployment`       |  10,670 |   123 |
| `## Local Development`           |   7,326 |    83 |
| `## Docker Images`               |   6,741 |    68 |
| `## Structured Writing Standard` |   4,146 |    55 |
| `## Prerequisites`               |   2,362 |    28 |
| `## Architecture`                |   1,775 |    27 |
| `## Project Overview`            |   1,213 |    14 |
| `## Key Environment Variables`   |   1,184 |    14 |
| `## Line Length`                 |     400 |     6 |

### Why the current material sits at root

Three separate decisions put it there, and all three still hold:

1. **The placement rule.** `showcase/quality/agent-skills` scopes root guidance to "what an agent must see before safely
   starting work", and admits a move only where a narrower task scope governs the rule, the destination is durable,
   project-authored, owned, and reachable at the trigger, and the retained root text leaves no warning, imperative,
   exception, or target-less fact to be inferred.
2. **The agent's own surface.** The README design-intent convention splits labour deliberately: `AGENTS.md` carries "the
   agent's full command surface and its environment/port reference", the README the human onboarding path.
   `## Build & Test` and `## Key Environment Variables` are therefore root **by decision**, not by accident.
3. **Three gates read the file.** `verifyCapturedMarkers` scans its `captured:` markers, `verifyInstallCommands` reads
   its helm install block (`build.gradle.kts:599`), and `verifyDocumentedPins` reads the Paketo pins from it and from
   `README.md` (`build.gradle.kts:610`). Removing a block a check reads empties the check.

### Prior decisions recovered (and what they forbid)

| Prior unit                                                        | What it settled                                                                                                                                                                                       |
| ----------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `extract-load-test-commands`, `extract-ci-narrative` (2026-10-19) | The audit's two extraction opportunities are **already shipped**; the current file is post-extraction.                                                                                                |
| `add-extraction-procedure` (2026-10-19)                           | The audit's three remaining "safe to load on demand" rules (`## Kubernetes Deployment`, `## Docker Images`, `## Key Environment Variables`) were verified to **fail** their retrieval paths and stay. |
| `trim-restated-agents-md-rules` (2026-10-11)                      | The capture section's one-word promotion-gate list is **deliberately retained** by the owner: the spec's full definitions are not always loaded, and the list is what a reader needs to act.          |
| `add-extraction-procedure` (2026-10-19)                           | A check that reads a block makes it extraction-coupled: retarget the check in the same change or keep the block.                                                                                      |
| `optimize-agents-auditor-for-instruction-budget` (2026-10-18)     | The budget is measured, not capped; no threshold is configured and none is to be invented.                                                                                                            |

### Parked ideas reconciled

`docs/ideas.md` was read in full at `a36c3f7e`. The entries below constrain, overlap with, or offer an alternative to
this change; the remainder (dependency pins, workflow triggers, the review loop's model bounds, the k8s staging
material, the OpenCode config trivia) were read and are unrelated. **No entry is removed from `docs/ideas.md` by this
planning follow-up**, and none is implemented merely because it is related.

| Parked idea                                                                                 | Relationship to this change                                                                            | Disposition                                         | Evidence and rationale                                                                                                                                                                                                                                                                                                                                                                                                                     | Effect on scope or acceptance criteria                                                                                                                |
| ------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------ | --------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| Carry the retrieval-path result in the audit ledger's "safe to load on demand" class        | Constrains how a ledger entry may be trusted; the 2026-10-19 ledger is this change's starting evidence | Constraint applied                                  | That ledger lists five "safe to load on demand" rules without a retrieval result, and `add-extraction-procedure` later verified the three that remain fail their paths. This design therefore records an evidence class per path (demonstrated, proposed, residual) instead of treating a named destination as proof. The idea's own fix — a spec requirement plus an `agents-auditor` refresh — is a different artifact and stays parked. | Every recommended move must carry a path with its class; a move whose path is only "proposed" must say so (design's retrieval table).                 |
| "Sharpen route-candidate verification the way merge candidates were sharpened"              | Moves 3 and 4 are pointer-to-a-mechanism reductions, the class this idea governs                       | Constraint applied; mechanism parked                | Applied to the _exact_ obligation, not a sibling: the doctor/README pair carries the list (every named tool is a README row or a declared probe, and `testDoctorCharter` asserts the agreement), the build file literally holds the `check` member set, and each pointer keeps what its target does not cover (the Gradle wrapper's absence, the doctor-out-of-`check` rationale). The auditor clause change stays parked.                 | Each pointer's acceptance criterion is coverage of the exact obligation, with the uncovered item retained at root — not "a related mechanism exists". |
| "Trend the audit counts across reports"                                                     | Governs how a reduction may be measured; overlaps this change's size estimate                          | Constraint applied; tooling deferred                | The reduction is reported as three distinct quantities (below), not one number, and the withdrawn round-1 figure is removed rather than restated. Cross-report trending is a separate report and is not built.                                                                                                                                                                                                                             | The estimate must state its basis and must not present a root-scoped count as a corpus change.                                                        |
| "Record the lesson-capture's rejected proposals"                                            | Overlaps the maintenance loop: both concern what a capture does with a proposal                        | Deferred as independent work                        | It persists _rejected_ proposals for retrospective analysis. This change fixes where an _accepted_ proposal lands and what it merges into. Neither depends on the other, and the capture's output persistence is a different artifact.                                                                                                                                                                                                     | None. The maintenance loop's acceptance criteria do not require rejected-proposal recording.                                                          |
| "Make a capture confirm the rule excludes the incident that produced it"                    | Overlaps the maintenance loop: both concern the quality of a captured rule                             | Deferred as independent work; prerequisite recorded | The eight modes are relocated verbatim, so their incident evidence travels intact; the loop adds no evidence test for a _new_ mode, which is exactly this idea's subject. Building it would change the capture gate, a different unit.                                                                                                                                                                                                     | A new mode's evidence quality is out of scope; the loop guarantees destination, not evidentiary strength.                                             |
| "Widen the reviewers' 120-character check to every changed file"                            | Overlaps this change's own verification (its files must pass the manual check)                         | Deferred as independent work                        | The reviewers' scope is its own change to two definitions. This unit applies the recipe to its own changed set instead (the round-1 review caught a 122-character fenced line this way).                                                                                                                                                                                                                                                   | Task 3.8 keeps the manual sweep over the change's files; no reviewer-definition edit.                                                                 |
| "Enforce the command lifecycle preflight with a check rather than prose in seventeen files" | Alternative philosophy: enforce a repeated rule mechanically instead of copying prose                  | Unrelated                                           | It concerns command definitions, not guidance placement. It is cited here only as precedent for the decision _not_ to add a gate: a mechanically checkable rule belongs in a check, and prose placement is not mechanically checkable.                                                                                                                                                                                                     | Confirms the non-goal "no new automated gate"; no task added.                                                                                         |
| "Name a durable home for a smoke-run's beyond-seed findings"                                | Same class — a durable home for guidance that is currently lost                                        | Deferred as independent work                        | It governs smoke-run findings, not the guidance corpus; this change's destination questions do not touch it.                                                                                                                                                                                                                                                                                                                               | None.                                                                                                                                                 |

**Which entries the implementation would resolve:** none. Each of the eight stays open after this change; the
implementation removes nothing from `docs/ideas.md`, because none is implemented and none becomes obsolete.

#### Source incidents against the proposed trigger

For each moved mode: its incident, the mistake, and whether "before you write or change a test, a check, or a gate"
would have fired before the mistake was made.

| Moved mode (leading text)                                                                 | Source incident (`captured:`)                                                                                                           | The mistake                                                                 | Would the trigger have fired first?         |
| ----------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------- | ------------------------------------------- |
| A check whose bad state the task it depends on already rejects can never fire             | `verify-chart-packaging`                                                                                                                | an assertion over a chart the package task had already rejected             | Yes — the assertion was being authored      |
| A declaration transcribed from a recorded artifact drifts silently                        | `normalize-the-review-round-digest`                                                                                                     | a table transcribed by hand into a test                                     | Yes                                         |
| A parser that returns "no exception" is not a gate                                        | `validate-dashboard-json`, `verify-documented-pins`                                                                                     | a gate trusted a lenient parse                                              | Yes                                         |
| A control must perturb the surface the check actually reads                               | `widen-config-probe-to-guidance`, `enforce-web-ui-naming-conventions`, `check-toolchain-prerequisites`, `retry-the-actionlint-download` | a control perturbed the wrong surface, or a pipeline read the sink's status | Yes                                         |
| A positive control's input must be one the subject does not itself exclude                | `scan-source-for-secrets`                                                                                                               | a control input the scanner's own allowlist excused                         | Yes                                         |
| A check whose input set is narrower than what it reads is not a check                     | `enforce-web-ui-import-boundaries`, `guard-archive-spec-sync`, `diagnose-rest-controller-ct-hang`                                       | a check read less than it claimed                                           | Yes for the fix; see counterexample 1       |
| An assertion about a declaration's content cannot see a value that never reaches the code | `declare-probes-with-shape-and-behavioural-checks`, `align-dependency-report-version-authority`                                         | an assertion over a declaration, not the emitted value                      | Yes for the assertion; see counterexample 2 |
| A deliberately conservative check owes the false-negative surface it accepts              | `check-unique-cron-schedules (#436)`, `track-review-session-continuity`                                                                 | a narrowed check's accepted gap left unrecorded                             | Yes                                         |

**Counterexample 1 — a diagnosis, not an author.** The zsh word-split mode sits in the moved input-set bullet, and it
was discovered while diagnosing a hang (`diagnose-rest-controller-ct-hang`) — a task the trigger does not cover. The
rule's _action_ (widen the check) is authoring, which the trigger does cover. The incident's other lesson, running a
reproduction from the failing context, is the resolution-context rule retained at root, though that bullet does not cite
this marker.

**Counterexample 2 — a report consumer, not an assertion.** `align-dependency-report-version-authority` was a report
whose filter emptied a section, not a test, and it is cited by the moved assertion bullet. The moved rule carries the
authoring half (assert what the subject emits); the reading half is the class the benign-outcome rule states at root,
though that rule does not cite this marker either. The boundary is drawn by the action, not by the citation.

**Counterexample 3 — a reviewer, not an author.** A reviewer judging a change that adds a check does not author one, so
the trigger does not fire for it. `review-quick.md` carries its own `test-control` class ("a check's known-bad case is
missing or unexercised"), so the reviewer's route is its own contract, not this skill. No change is needed, and none is
proposed.

**Boundary correction made because of this pass:** none of the eight modes moved across the boundary as a result; the
incidents confirm the split in "Why one trigger cannot cover the whole cluster": each of the two incidents with a
reading half is covered by a root rule of that class and by the moved rule for its authoring half.

#### What the reduction measures

Three different quantities, kept apart:

- **Root-file size**: 356,723 B → 338,964 B (−17,759 B, −4.98%) after the capture, measured. This is the always-loaded
  surface.
- **Root-scoped marker count**: `AGENTS.md` carried 297 `captured:` markers; the eight moved modes carry 16 (now in the
  skill) and the moved diagram bullets 3 (now in the definition), so the root-scoped count measured **278**. That is a
  change in _where_ the provenance is recorded, not in how much exists.
- **The maintained rule corpus**: unchanged. The 13 check/test rules still exist — 8 relocated, 5 at root — and the
  diagram rules still exist, in the definition. Nothing is retired, so a falling root marker count must not be read as
  consolidation.

### How the loading actually works (verified, not assumed)

- **Root `AGENTS.md` only, per session.** opencode "looks for rule files by traversing up from the current directory"
  and "the first matching file wins in each category" (`https://opencode.ai/docs/rules/`). A nested `AGENTS.md` is
  therefore **never** loaded by a session whose working directory is the repository root — and a session started inside
  that subtree would load the nested file **instead of** the root one. `git ls-files | grep -E '(^|/)AGENTS\.md$'`
  returns `AGENTS.md` alone, so no nested file exists today; creating one is not a reduction mechanism here.
- **A pointer is manual.** "opencode doesn't automatically parse file references in `AGENTS.md`"; the documented way to
  reach another file is an explicit instruction that tells the agent to read it. A pointer without a stated trigger is
  not a retrieval path.
- **An `instructions` array widens, never narrows.** Those files are "combined with your `AGENTS.md` files" every
  session.
- **A skill narrows, and skills are permitted here.** Its `description` (≤1024 characters) is listed in every session's
  `<available_skills>`, and the body loads only when the `skill` tool is called (`https://opencode.ai/docs/skills/`).
  Permissions "start from permissive defaults … most permissions default to `allow`"
  (`https://opencode.ai/docs/permissions/`), and `.opencode/opencode.json` declares no `permission.skill` (nor a global
  `permission` scalar), so a project skill loads without a prompt.
- **A subagent definition loads when the subagent is invoked.** That is **after** the routing decision, which is why a
  definition cannot own knowledge the routing itself needs.
- **A project command loads when it is invoked.** `.opencode/commands/diagram.md` exists and carries the diagram
  procedure the caller follows once `/diagram` runs.

## Why one trigger cannot cover the whole cluster

The cluster proposed for extraction is `## Gotchas → Writing checks and tests that can actually fail`. Reading each
bullet for the decision it governs — not for the section it sits in — splits it in two. Sizes are the bullet's own
measured bytes (`python3` over the bullet ranges in `AGENTS.md`).

| Bullet (leading text)                                                                        | Bytes | Decision it governs                              | Needed in                                                |
| -------------------------------------------------------------------------------------------- | ----: | ------------------------------------------------ | -------------------------------------------------------- |
| A check whose bad state the task it depends on already rejects can never fire                |   451 | whether a new check can ever fail                | authoring a check                                        |
| A declaration transcribed from a recorded artifact drifts silently                           |   842 | whether a transcribed table is verified          | authoring a check over a copy                            |
| **A glob or filter that matches nothing is vacuous, not clean**                              | 1,709 | whether an empty match means "absent"            | any search, audit, review, or exploration                |
| **A search that under-matches returns a plausible empty or partial result**                  | 3,221 | whether a search result is complete              | any search, audit, review, or exploration                |
| **A derivation whose failure maps onto the benign outcome reads as a clean result**          | 2,674 | whether "nothing to report" is real              | reading any tool's report, update check, or scan         |
| A parser that returns "no exception" is not a gate that rejects the input class it claims to | 1,764 | which input classes a parser gate must reject    | authoring a gate                                         |
| A control must perturb the surface the check actually reads                                  | 3,337 | where a control's input is injected              | authoring or repairing a check's control                 |
| A positive control's input must be one the subject does not itself exclude                   |   935 | which input proves a check fires                 | authoring a control                                      |
| A check whose input set is narrower than what it reads is not a check                        | 2,558 | what a check reads                               | authoring or scoping a check                             |
| An assertion about a declaration's content cannot see a value that never reaches the code    | 1,452 | whether an assertion observes the emitted value  | authoring a check over a declaration                     |
| A deliberately conservative check owes the false-negative surface it accepts                 | 1,055 | what a narrowed check must state and where       | authoring or narrowing a check                           |
| **A verdict echo is not a check**                                                            |   649 | whether a command's verdict is derived or canned | writing or reading any script, task, or workflow step    |
| **A clean run from the wrong resolution context is not a reproduction**                      | 2,581 | whether a reproduction is valid                  | debugging, incident investigation, verifying a behaviour |

**Split:** the five **bold** rows (10,834 B) are evidence-hygiene rules an agent needs _while_ exploring, searching,
auditing, reviewing, debugging, and reading any tool's output. The eight plain rows (12,394 B) are check-authoring
rules, needed when a check, test, or gate is written.

**The proposed trigger covers only the second group.** "Before you write or change a test, a check, or a gate" fires
before every one of the eight. It does not fire for the tasks that most need the five — an audit, a repository search, a
review of someone else's claim, a report read, or a diagnosis — so under the round-1 wording four of the five would have
moved behind a trigger that never fires for them, and the fifth (the verdict rule) would have been reached only for its
authoring half. That is the semantic loss the round-1 design did not catch.

**Broadening the trigger instead is rejected.** Keying the skill to "before you search, expand a glob, reproduce a
behaviour, or read a report" would fire on nearly every turn of an exploratory task, and each firing reloads the whole
body. That is strictly worse than carrying 10.8 KB at root once per session. The frequency is the reason, and it is a
qualitative one — no measurement of turns-per-session is claimed.

**Chosen correction: split the source unit.** The five evidence rules stay at root under their own heading; the eight
check-authoring modes move to the skill; the invariant stays with the trigger. The heading that keeps the trigger is
`### Writing checks and tests that can actually fail`; the retained five move under `### Evidence and search`.

## Goals / Non-Goals

**Goals:**

- Retire the material whose subject a task narrows to one moment, keeping at root the decision that task must still make
  before it acts — and keeping at root the rules whose trigger cannot be narrowed at all.
- Close the maintenance loop, so a new or corrected check/test mode reaches the skill through the existing capture
  mechanism rather than through the applying agent's memory.
- Prove, per obligation, which retrieval path carries it, and label each path's evidence class honestly.
- Preserve every imperative, warning, exception, identifier, gate condition, and `captured:` provenance marker.

**Non-Goals:**

- A hard size cap, a new automated gate, or a broad agent-tooling redesign.
- Broadening the skill's trigger to cover searching, globbing, reproduction, or report reading.
- Nested `AGENTS.md` files, an `instructions` array, or converting always-needed procedures into skills.
- Moving `## Build & Test`'s command surface, `## Key Environment Variables`, the `## OpenSpec Workflow Agreement`, or
  the gate-coupled `## Kubernetes Deployment` / `## Docker Images` blocks.
- The audit's stale vision-pin and task-enumeration findings and its Redgate and gRPC findings: already shipped
  (`apply-2026-10-19-audit-findings`, `remove-unrecorded-redgate-repository`, `correct-adr-0017-spring-grpc-gate`).
- Editing `AGENTS.md`, `.opencode/`, or a main spec while this change is being planned.

## Candidate decision table

Anchors are a heading plus the span's own leading text, never a line number alone. "Net" is an estimate derived from the
**measured** span bytes minus the retained replacement; the implementation records measured figures.

| Source span (heading + quoted text)                                                                                                                                                                                                                                                                                | Classification                                                                | Decision needed                                                     | Proposed destination                                                                                               | Retained root instruction                                                                                                                                              | Verified coverage or missing content                                                                                                                                                                                                                  | Retrieval trigger                                                          | Est. net reduction |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------- | ------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------- | -----------------: |
| `## Gotchas → Writing checks and tests that can actually fail`, the eight check-authoring bullets from "A check whose bad state the task it depends on already rejects can never fire" through "A deliberately conservative check owes the false-negative surface it accepts"                                      | Task-scoped procedure (authoring a check, test, or gate)                      | How to make a check fail, and what it must read and assert          | **New** `.opencode/skills/check-and-test-authoring/SKILL.md`, one section per mode, bullets moved verbatim         | Three bullets: the invariant, the control-is-a-committed-test rule, and the trigger naming the skill                                                                   | Destination created by this change; inside `agents-auditor`'s scope (`.opencode/skills/`); every mode, identifier, and `captured:` marker moves verbatim                                                                                              | "Before you write or change a test, a check, or a gate"                    |           −12.0 KB |
| The same section's five evidence-hygiene bullets: "A glob or filter that matches nothing is vacuous", "A search that under-matches…", "A derivation whose failure maps onto the benign outcome…", "A verdict echo is not a check", "A clean run from the wrong resolution context…"                                | Cross-cutting rule needed while acting, not before one named task             | Whether an empty result, a report, or a reproduction may be trusted | — (stays at root, under a new `### Evidence and search` heading)                                                   | The five bullets unchanged                                                                                                                                             | **Rejected as an extraction**: the only trigger that would reach them ("before you search or conclude") fires many times per session and would reload the body each time; no narrower trigger covers audits, reviews, debugging, and report reading   | —                                                                          |                  0 |
| `## Gotchas → Documentation & durable claims`, the five diagram bullets from "Align ASCII/Unicode diagram comments by character width" through "A `mermaid` fence is un-gated by Spotless"                                                                                                                         | Task-scoped mechanic + diagnostic evidence                                    | What a renderer/host actually does, and how to draw                 | `.opencode/agent/diagrammer.md` (loaded when `/diagram` invokes it); `agent-skills` already states the obligations | Seven bullets: delegation (every edit), the definition's rules, mapping confirmation, the un-gated fence, the `http(s)`-only rule, measurement, the human-edit caution | Definition already carries measurement, box alignment, asymmetry, fallback derivation, subgraph direction, toolbar clearance, render+handoff; **must be added**: `diagrammer.md`'s missing mechanics (listed in Move 2)                               | Every edit is delegated via `/diagram`; the definition holds the mechanics |            −6.3 KB |
| `## Conventions`, "Vision subagent for structured visual review" / "Diagrammer subagent for ASCII and Mermaid diagrams" / "Confirm a diagram's semantic mapping with the user before iterating its geometry."                                                                                                      | Routing rule + restated mechanics                                             | Whether and how to delegate diagram work                            | Those seven bullets replace the three in `## Conventions`; mechanics → `diagrammer.md`                             | The retained bullets' caller-side clauses                                                                                                                              | All three bullets' caller decisions retained; `diagrammer.md`'s "recorded in `AGENTS.md`" sentence is retargeted to its own text                                                                                                                      | "A diagram needs to be created/aligned/fixed/reviewed"                     |     included above |
| `## Prerequisites`, the tool list from "Java 21+" through "…`review-routing.py`…" and the doctor paragraph                                                                                                                                                                                                         | Reference inventory restating the README table and the doctor's `probe_table` | What a machine needs, and whether the doctor belongs in `check`     | `scripts/doctor.sh`'s `probe_table` and the README's table, whose agreement `testDoctorCharter` asserts            | Run the doctor; the optional-path map; the hooks step; **the doctor stays out of `check`**                                                                             | Every named tool is a declared probe or a README row, except the Gradle wrapper (README prose, no probe); the doctor also probes `gh` and `opencode`; **the doctor-out-of-`check` rationale is in neither destination and stays at root**             | "A tool is missing", or "what does this task need installed"               |            −1.2 KB |
| `## Build & Test`, the `check` run line's task-name list "…verifyInfraImageVersions, verifyInstallCommands, … verifyLargeFiles)"                                                                                                                                                                                   | Restatement of a mechanism file; governs no decision                          | —                                                                   | `build.gradle.kts`'s `check` block                                                                                 | "plus the chart packaging, the workflow lint, and the repository's verification tasks — the `check` block enumerates them"                                             | The build file is authoritative and already lists `testReviewRouting`, which this list omitted until `apply-2026-10-19-audit-findings`                                                                                                                | Reading the build file, which any change to `check` requires               |            −0.4 KB |
| `## OpenSpec Workflow Agreement → Workflow: Capture lessons`, "Every proposal names the existing bullet it extends" … "A proposal passes the promotion gate…"                                                                                                                                                      | Restatement of `lesson-capture.md` + the `agent-skills` spec                  | —                                                                   | `lesson-capture.md`; `agent-skills`                                                                                | Unchanged                                                                                                                                                              | **Rejected**: `trim-restated-agents-md-rules`' design records the owner keeping this list deliberately as the always-loaded reminder                                                                                                                  | —                                                                          |                  0 |
| `## Conventions`, the per-subagent descriptor bullets ("Agents-auditor subagent…", "Specs-auditor…", "Architecture-auditor…", "Readme-auditor…", "Thorough-review subagent…", "Experience-analyzer…")                                                                                                              | Routing reference                                                             | Which specialist to invoke, and what its boundary is                | The definitions' frontmatter `description`                                                                         | Unchanged                                                                                                                                                              | **Rejected**: the description loads always but is a summary; the definition — which carries the boundary — loads only **after** the routing decision                                                                                                  | —                                                                          |                  0 |
| `## Kubernetes Deployment`, "Re-applying an infrastructure release's local values wipes its data…" and "A chart Deployment addition must extend the app release's `installDependsOn`"                                                                                                                              | Reference procedure + rule                                                    | How to deploy locally, and what a chart change must extend          | `helm-chart` spec, `build.gradle.kts`, values files                                                                | Unchanged                                                                                                                                                              | **Rejected**: `add-extraction-procedure` verified the destinations lack the data-wipe warning, the `installDependsOn` rule, and the read-timeout guidance; `verifyInstallCommands` also reads this section                                            | —                                                                          |                  0 |
| `## Docker Images`, "The builder and the buildpacks it bundles must be bumped together" and the CNB timestamp note                                                                                                                                                                                                 | Reference procedure + diagnostic evidence                                     | How to bump the builder/buildpacks                                  | `dependency-management` spec, the catalog, the buildpack gotcha                                                    | Unchanged                                                                                                                                                              | **Rejected**: `add-extraction-procedure` verified the catalog and the gotcha lack the pairing rule; `verifyDocumentedPins` reads the pins here                                                                                                        | —                                                                          |                  0 |
| `## Key Environment Variables`, "`SHOWCASE_CORS_ALLOWED_ORIGINS` — …"                                                                                                                                                                                                                                              | Reference inventory                                                           | What each variable means                                            | ADR-0002's four-surface rule                                                                                       | Unchanged                                                                                                                                                              | **Rejected**: the ADR names the surfaces, not each variable's meaning; the README convention assigns this reference to `AGENTS.md`                                                                                                                    | —                                                                          |                  0 |
| `## Build & Test`, the command examples "`./gradlew :showcase-command-service:check`"                                                                                                                                                                                                                              | The agent's command surface                                                   | Which command verifies what                                         | README "Development Workflow"                                                                                      | Unchanged                                                                                                                                                              | **Rejected**: the README design-intent convention assigns `AGENTS.md` "the agent's full command surface"                                                                                                                                              | —                                                                          |                  0 |
| `## Architecture`, "CQRS with four services and a web UI"                                                                                                                                                                                                                                                          | Reference inventory                                                           | Which module is which, before choosing where a change goes          | README, specs                                                                                                      | Unchanged                                                                                                                                                              | **Deferred**: 1.8 KB, needed before task discovery; the README's copy is human-facing and the specs describe behavior, not the service map                                                                                                            | —                                                                          |                  0 |
| `## OpenSpec Workflow Agreement`, "**Trigger** — you are starting, implementing, or shipping any unit of work"                                                                                                                                                                                                     | Global lifecycle procedure                                                    | Every branch, commit, review, capture, and archive decision         | The generated `opsx-*` commands                                                                                    | Unchanged                                                                                                                                                              | **Rejected**: the lifecycle binds before any task is discovered, and `openspec update` overwrites the commands that would host it                                                                                                                     | —                                                                          |                  0 |
| A nested `AGENTS.md` for a subtree (e.g. `showcase-web-ui/AGENTS.md`)                                                                                                                                                                                                                                              | Placement alternative                                                         | —                                                                   | —                                                                                                                  | —                                                                                                                                                                      | **Rejected**: opencode traverses **up** from the working directory and the first match wins, so a root session never sees it and a subtree session loses the root file                                                                                | —                                                                          |                  0 |
| `.opencode/opencode.json` `instructions: [...]`                                                                                                                                                                                                                                                                    | Placement alternative                                                         | —                                                                   | —                                                                                                                  | —                                                                                                                                                                      | **Rejected**: the field combines the listed files into every session                                                                                                                                                                                  | —                                                                          |                  0 |
| Converting top-level procedures into project skills                                                                                                                                                                                                                                                                | Placement alternative                                                         | —                                                                   | `.opencode/skills/*/SKILL.md`                                                                                      | —                                                                                                                                                                      | **Deferred**: a skill's description loads always and its body on invocation, but turning always-needed procedures into skills is the redesign this investigation does not establish as necessary                                                      | —                                                                          |                  0 |
| A new `docs/checks-and-tests.md` instead of a skill                                                                                                                                                                                                                                                                | Placement alternative                                                         | —                                                                   | `docs/`                                                                                                            | —                                                                                                                                                                      | **Rejected for Move 1**: a `docs/` file has no maintenance owner — no auditor's scope reaches it and no requirement names it — whereas a project skill is inside `agents-auditor`'s scope and the `agent-skills` capability owns the guidance surface | —                                                                          |                  0 |
| `## Conventions`, the "Test display names" bullet's assertion modes ("a test green on both sides verifies nothing", "an enumerated name with one asserted case is an unverified claim", "a silently dropped subject leaves a test that still passes", "`callback?.()` no-ops … so the assertion passes vacuously") | Task-scoped assertion rules                                                   | How a test's assertion must be shaped                               | The same skill, in a further pass                                                                                  | Unchanged                                                                                                                                                              | **Deferred**: the modes share the trigger but are interleaved with naming rules in one convention bullet, so moving them needs their own inventory and a split of that bullet; the delta scopes its claim to the modes the skill holds                | —                                                                          |                  0 |

## Recommended first unit

The four recommended moves share one theme: **a rule whose subject a task narrows to one moment leaves root for the
artifact that task already reaches, and root keeps the decision that must be made before acting.** Moves 2–4 are
retirements with no spec delta; Move 1 is the one that changes spec'd behaviour, because it creates an agent-guidance
destination and routes future captures to it — which is why its maintenance loop also rewrites the root capture clauses
that assume an `AGENTS.md` bullet and closes the destination in the capture's own contract. They are one unit because
they are one edit discipline over one file plus its destinations, share one verification protocol, and are reviewed as
one diff. If the owner prefers a smaller first diff, Move 1 with the maintenance loop is the separable half (see "Unit
coherence").

### Move 1 — the eight check-authoring modes → `.opencode/skills/check-and-test-authoring/SKILL.md`

**Why a skill.** The placement rule requires the destination to be durable, project-authored, **owned**, and reached at
the trigger. `agents-auditor` audits `.opencode/skills/` (project-authored files), so a skill has a maintenance owner
without widening any auditor's scope; its `description` is listed in every session. A `docs/` reference would have
neither. "Owned" and "audited" are different questions: the skill's owner is the `agent-skills` capability and the
auditor that scans it; the `docs/` alternative had neither.

**Inventory.** The eight bullets move **verbatim** (one `##` section each), so every identifier travels with its rule.

| Rule (leading text)                                                                       | Identifiers and gate conditions that must survive                                                                                                                                              | `captured:` provenance                                                                            |
| ----------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------- |
| A check whose bad state the task it depends on already rejects can never fire             | `helm package`, the strict lint                                                                                                                                                                | `verify-chart-packaging`                                                                          |
| A declaration transcribed from a recorded artifact drifts silently                        | `scripts/test-doctor.sh`, `probe_table`                                                                                                                                                        | `normalize-the-review-round-digest`                                                               |
| A parser that returns "no exception" is not a gate                                        | `DashboardJsonRules`, `mapper.readTree`, `MissingNode`, `{"a":1}x`, `{}{}`, `FAIL_ON_TRAILING_TOKENS`, mutation battery                                                                        | `validate-dashboard-json`, `verify-documented-pins`                                               |
| A control must perturb the surface the check actually reads                               | `applicationYmlPlaceholdersBindDocumentedDefaults`, `*.spec.{ts,tsx}`, `*.spec.tsx`, `operations.*.guidance`, `could not parse`, `verifyExecutableBits`, `chmod 644`, `cmd \| tail`, `bash -n` | 4 markers (see the moved text)                                                                    |
| A positive control's input must be one the subject does not itself exclude                | `AKIAIOSFODNN7EXAMPLE`, gitleaks, `api_key = "a1b2…"`                                                                                                                                          | `scan-source-for-secrets`                                                                         |
| A check whose input set is narrower than what it reads is not a check                     | `verifyModuleDependencies`, `testing { suites { … } }`, `npmTest`, `eslint.config.js`, `npmBuild`, `package-lock.json`, zsh `${(f)files}`                                                      | `enforce-web-ui-import-boundaries`, `guard-archive-spec-sync`, `diagnose-rest-controller-ct-hang` |
| An assertion about a declaration's content cannot see a value that never reaches the code | `scripts/test-doctor.sh`, `kind` `tool`→`state`, "13 probes", `set +e`, `DependencyUpdateRules`, `dependencyUpdates`                                                                           | `declare-probes-with-shape-and-behavioural-checks`, `align-dependency-report-version-authority`   |
| A deliberately conservative check owes the false-negative surface it accepts              | day-of-week `7`-as-`0`, `commit-hygiene` spec, `=======`, `verifyReviewSessionContinuity`                                                                                                      | `check-unique-cron-schedules (#436)`, `track-review-session-continuity`                           |

The five evidence rules stay at root verbatim, under a new `### Evidence and search` heading: the vacuous-glob rule
(1,709 B), the under-matching-search rule (3,221 B), the benign-outcome-derivation rule (2,674 B), the verdict-echo rule
(649 B), and the resolution-context rule (2,581 B).

**Retained root text** (the section keeps its heading, keeps the opening bullet's invariant and control rule as two
bullets, and gains a trigger bullet; the five evidence bullets relocate under their own heading):

> ### Evidence and search
>
> <the five bullets, verbatim and in their current order>
>
> ### Writing checks and tests that can actually fail
>
> - **A check is evidence only once it has been shown to fail.** A clean run, an empty result, or an unmoved control
>   proves nothing until the check hits a known positive.
> - **A gate's control is a committed test, not a task.** A gate whose logic lives in a rules object carries its
>   known-bad and known-good cases in that object's `*RulesTests`; an inline gate carries them in its consuming module,
>   since a checklist task asserting the control ran can be ticked without it running.
> - **Before you write or change a test, a check, or a gate, load the `check-and-test-authoring` skill.** It records
>   each way such a check passes without verifying anything — an unreachable bad state, a lenient parser, a control that
>   misses the surface the check reads, an input set narrower than what it reads — with the confirmation and incident
>   evidence for each. Loading it at that trigger is mandatory, not reference.

**Destination structure** (new `SKILL.md`; `description` within the 1024-character cap):

```
---
name: check-and-test-authoring
description: Authoring a check, test, or gate that can actually fail — such as an unreachable bad state, a lenient
  parser, a control that misses its read surface, an input set narrower than what it reads, or an assertion that
  cannot see the emitted value — each with its confirmation and incident evidence.
---
# Checks and tests that can actually fail
<intro: one section per authoring mode; a new mode is recorded here, a correction updates its section, an
evidence-hygiene rule belongs in AGENTS.md's `### Evidence and search`, and a lesson with both halves is split so
neither destination restates the other>
## The invariant
## The bad state the check rejects is unreachable
## A transcribed declaration drifts silently
## "It parses" is not "it is one valid document"
## The control must perturb the surface the check actually reads
## A positive control must not use an input the subject excludes
## A check's input set must be as wide as what it reads
## Assert what the subject emits, not only what it declares
## A conservative check owes the false negatives it accepts
```

### Move 2 — the diagram mechanics → `diagrammer.md`

**The five cases, and the path each takes.** "The definition" is `.opencode/agent/diagrammer.md`; "the command" is
`.opencode/commands/diagram.md`, which already carries the caller procedure (mapping, ASCII end, Mermaid render review)
and loads when `/diagram` runs.

| Case                                                                  | Obligations that apply                                                                     | Path                                                                                       |
| --------------------------------------------------------------------- | ------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------ |
| Add a diagram via `/diagram`                                          | Mapping confirmation, the drawing rules, the handoff, the render review                    | `/diagram` loads the command, which invokes the definition — both before anything is drawn |
| Small edit to an existing Mermaid fence (a label, a comment, an edge) | The un-gated fence rule always; the definition's mapping, contract, and label rules        | **Delegated** — the definition is the only place those rules live                          |
| Edit an ASCII diagram or its comments                                 | Decoded-width measurement, comment alignment, box edges, re-measure, asymmetry             | **Delegated**, for the same reason                                                         |
| Review a diagram without changing it                                  | The mapping (what each span should mean), the render inspection, measurement-as-hypothesis | The retained bullets; the review criteria live in `vision.md`, which the delegation loads  |
| Repair a host-rendering defect                                        | `viewscreen` cap, toolbar clearance, live-render verification                              | **Delegated** — the mechanics are only in the definition                                   |

**Delegation covers every edit — the conservative application of the specification, not a quotation of it.** The
`agent-skills` scenarios name the operations: a diagram "created, aligned, or fixed" (ASCII) and "created, fixed, or
reworked" (Mermaid), performed by the `diagrammer` with the pro model. A comment-only edit is **not** named in that
wording. This design applies the contract conservatively rather than narrowing it — every edit goes through `/diagram`,
a comment or label tweak included, and the caller may describe the change precisely and apply the returned source — so
no exception is introduced and no specification text changes. Reading a diagram, and reviewing its render, are not edits
and need no delegation.

**Inventory.**

| Item                                                                                                                           | Disposition                                                                          |
| ------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------ |
| Measure ASCII columns by decoded character width, not bytes (`len(line[:idx]) + 1`)                                            | Already in the definition; root keeps nothing, since every edit is delegated         |
| Align diagram comments to the longest entry (README tree, target column 42)                                                    | **Must be added** to the definition                                                  |
| Verify with the same decoded measurement; `awk length` cannot see the misalignment                                             | Already in the definition                                                            |
| The README OpenSpec-flow mapping (`approve`, `spec → main` as edge labels)                                                     | Already in the definition                                                            |
| Nested subgraphs bottom-align the outer title in a ~20 px band                                                                 | Already in the definition                                                            |
| A block diagram's bars left the terminal node under GitHub's toolbar (evidence)                                                | **Must be added** — the incident, not just the rule                                  |
| A body-pipes alignment flattened two spans and was reverted (evidence)                                                         | **Must be added** beside the asymmetry rule                                          |
| Single range = one non-nested labelled subgraph; `direction` honoured only when the outside edge leaves the subgraph           | Already in the definition                                                            |
| A member node's edge makes Mermaid ignore the subgraph's `direction`                                                           | Already in the definition                                                            |
| `viewscreen` caps the SVG at natural width, scales/centers it; toolbar ≈100 px; `mermaid-js/mermaid#7117` closed               | **Must be added** in full                                                            |
| `diagramPadding` reserves symmetric margins whose width shrinks with the column; differs between blob and tree views           | **Must be added**                                                                    |
| Prefer natural width under the column: vertical `TB`, compact multi-row grid                                                   | Already in the definition                                                            |
| **Redrawing a diagram into a new medium makes the old one a starting point — re-derive it from the code or config it depicts** | **Must be added**                                                                    |
| Re-derive a fallback edge-by-edge from the Mermaid's node/edge/label list                                                      | Already in the definition                                                            |
| Re-check every label after a rename (`approve`/`CI green`, `search`, the event store; an arrowhead pointing back)              | **Must be added**                                                                    |
| The lifecycle example (`SCHEDULED -.-> REMOVED`) and `ShowcaseAggregate`'s removal from any state                              | **Must be added** as the worked example                                              |
| A `mermaid` fence is un-gated by Spotless → render and have `vision` read the render                                           | **Stays at root** — a gate fact and an imperative that fires when a fence is written |
| Treat a hand-edited asymmetry as intentional; never regenerate over a human edit without diffing                               | **Stays at root**                                                                    |
| Confirm the semantic mapping with the user before iterating geometry                                                           | **Stays at root**                                                                    |
| A visual claim needing a measurement is a hypothesis; only `http(s)` URLs load                                                 | **Stays at root**                                                                    |
| Provenance: `mermaid-event-flow-draft`, `rework-readme-lifecycle-diagram`, `rework-readme-diagrams-for-github-mermaid`         | Move with the fence bullet onto the definition's rendering section                   |
| Provenance: the vision bullet's `rework-readme-lifecycle-diagram`                                                              | Stays on the retained fence bullet                                                   |

**Retained root text** (seven bullets replace the three; one rule per bullet):

> - **Diagrams are delegated.** The main agent runs on the cheap flash model and is weak at diagram work — ASCII
>   geometry and Mermaid's rendering pitfalls each repeatedly cost effort and review cycles. Every creation, alignment,
>   or fix goes through the `/diagram` command and its `diagrammer` subagent, a comment or label tweak included.
> - **The `diagrammer` establishes the mapping and applies its own rules.** It derives the semantic mapping, applies the
>   geometry and rendering rules recorded in its definition, verifies its output, and returns the source plus a
>   visual-review handoff.
> - **Confirm a diagram's semantic mapping with the user before iterating its geometry.** A diagram is a rendering of a
>   fixed mapping — which span starts where and ends where; once the mapping is agreed, alignment is mechanical. State
>   the intended mapping in the change's report and have it confirmed before re-rendering.
> - **A `mermaid` fence is un-gated by Spotless, so a render must be inspected before the diagram ships.** Render it to
>   an image and delegate the image to the `vision` subagent with the diagrammer's handoff; a local render proves only
>   that the source parses and lays out, never the host renderer's layout or toolbar clearance.
>   `captured: rework-readme-lifecycle-diagram`
> - **The `vision` subagent loads only an `http(s)://` URL** — serve a local artifact and pass its URL.
> - **A visual claim that needs a measurement is a hypothesis**, settled by measuring before it acts.
> - **Never normalize a deliberate asymmetry, and never regenerate over a human edit without diffing against it.** When
>   a diagram or any doc has been hand-edited, establish what each element starts and ends at before "fixing" it; if the
>   meaning is unclear, leave the edit and ask.

**Destination edit**: the definition gains the eight marked items (seven mechanics and the worked example), its
"recorded in `AGENTS.md` — … read them before choosing an approach" sentence is retargeted to its own text, and the
three provenance markers land on the rendering section. No spec delta is owed: the `agent-skills` scenarios "ASCII
diagrams are drawn by the pro-model diagrammer", "Mermaid diagrams are drawn by the pro-model diagrammer", and "A
Mermaid render is visually reviewed before the diagram ships" already state these obligations.

### Move 3 — the prerequisites list → the doctor and the README

**Inventory.**

| Item                                                                                                                          | Disposition                                                                                                              |
| ----------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| Java 21+, Docker & Compose, Helm 4.x, Snyk, actionlint, `pack`, Python 3                                                      | Each is a README row and a declared probe                                                                                |
| Gradle wrapper                                                                                                                | No probe and no README row by design (README prose); the retained map does not need it                                   |
| The doctor also probes `gh`, `opencode`, `git-hooks`, `docker-daemon`                                                         | Not the list's subject; they stay the doctor's and the README's                                                          |
| Git hooks: `./scripts/install-git-hooks.sh` once per clone; `--no-verify` for a deliberate exception                          | **Stays at root** — an imperative an agent must act on; the guard's checks stay enumerated in the `git add <dir>` gotcha |
| The doctor's declaration (`key\|class\|kind\|readme`), `test-doctor.sh`, shape/behaviour/agreement                            | Deleted: the `toolchain-check` spec states the declaration, its validation, and the check's assertions                   |
| `testDoctorCharter` is a `check` member                                                                                       | Deleted: the build file owns it                                                                                          |
| The doctor stays out of `check` — the host is not a build input, so gating it would fail PRs for causes they cannot remediate | **Stays at root** — the spec does not state it and it governs a decision                                                 |

**Retained root text** (five bullets replace the body):

> ## Prerequisites
>
> - **Toolchain**: `./scripts/doctor.sh` (or `/check-tooling`) probes every prerequisite — presence, version floors, and
>   repo state — and prints the platform-appropriate install command for anything missing.
> - **The README owns the documented list and the install columns**; the doctor owns the probe, and `testDoctorCharter`
>   keeps the two in step.
> - **Only Java and Docker with Compose are needed for the default build-and-test path.** The optional tools gate
>   optional paths: `Helm`/`kubectl` for deployment, `Snyk` for `dependencySecurityCheck`, `pack` for the web UI image,
>   `actionlint` for `workflowLint` (part of `check`), `Python 3` for the `scripts/*.py` tools.
> - **Git hooks**: run `./scripts/install-git-hooks.sh` once per clone to activate the pre-commit guard (the checks the
>   `git add <dir>` gotcha enumerates); bypass a deliberate exception with `git commit --no-verify`.
> - **The doctor is not part of `check`**: the host is not a build input, so gating it would fail pull requests for
>   causes they cannot remediate. The check asserts the declaration and the reporting instead, and ignores that exit
>   code.

### Move 4 — the `check` task-name list

**Inventory.** The line names `:helm:chart:helmPackageMainChart`, `workflowLint`, `verifyInfraImageVersions`,
`verifyInstallCommands`, `verifyDocumentedPins`, `verifyModuleDependencies`, the `build-logic` tests,
`verifyDashboardJson`, `verifyPrettierAlignment`, and the thirteen commit-hygiene tasks. All are declared in
`build.gradle.kts`'s `check` block. Retained:

```
# Check runs: compile → spotless/checkstyle/spotbugs/errorprone → test → componentTest → integrationTest, plus the
# chart packaging, the workflow lint, and the repository's verification tasks — the `check` block in
# `build.gradle.kts` enumerates them.
```

## The maintenance loop

**What the current text says.** `lesson-capture.md:34` fixes the subagent's output: "For each proposed addition, give
the exact AGENTS.md text and where it belongs (Conventions section, Gotchas section, or a specific subsection)". The
`agent-skills` capture requirement's scenario says the subagent "proposes `AGENTS.md` gotchas/conventions from the
unit's lessons" (`spec.md:416`). Neither knows a skill destination, so a check/test mode captured after this change
would be proposed as root text — the exact regrowth the requirement forbids.

**Generic routing does not cover it.** The capture's routing vocabulary is `AGENTS.md` sections plus, for a
_failed-gate_ rule, "a check, a spec, an ADR, or the change dir" (`lesson-capture.md:87`). A _passing_ check/test mode
is neither, so the applying agent would place it in `AGENTS.md` and the next review would flag it. Correct placement
would rest on the agent's memory of this change — not a loop.

**The correction (smallest that closes the loop).**

1. `lesson-capture.md` — the destination becomes the owning artifact: "for each proposed addition, give the exact text
   and the artifact it belongs to — an `AGENTS.md` bullet, a section of the `check-and-test-authoring` skill for a
   check/test authoring mode, or a pair when the lesson has both halves".
2. `lesson-capture.md` — the "name the existing bullet it extends" clause becomes "the existing rule it extends — an
   `AGENTS.md` bullet, or a mode already in the skill — or state that none does", so a correction merges into the
   existing mode rather than adding a second one.
3. `agent-skills` delta — `MODIFIED` on "Per-change quality-gate and analysis subagents are available": the two clauses
   above, changed in place (the requirement's other 19 scenarios are carried verbatim; the block is copied whole because
   a `MODIFIED` block replaces the requirement).
4. `agent-skills` delta — `ADDED` "Check and test authoring guidance is maintained in its project skill": the skill
   exists, each mode carries its action, confirmation, evidence and provenance, root carries the trigger and does not
   restate a rule, a new mode is recorded there, and a correction updates its section.

**Root capture contract.** The root capture procedure also assumes every accepted proposal targets an `AGENTS.md`
bullet: its step 2 says the subagent "proposes `AGENTS.md` additions", its decision point requires that "every proposal
names the existing bullet it extends", its restated-remedy branch speaks of "the target bullet", and its marker bullet
says "a pre-commit guard enforces" marker placement (that guard's sentence named `AGENTS.md` only, and the retarget it
owes is part of this change), its step 4 reports a candidate "with the bullet it would extend", and its marker paragraph
speaks of "a bullet the capture merged into". Six clauses change; everything else stays — the skip condition, the
promotion gate and its criteria, the merge-versus-accretion discipline, the decision-difference test, the
untrusted-source verification, the growth bound, and the completion criteria.

| Clause (leading text)                                                                                   | Now                                           | After                                                                                                                                              |
| ------------------------------------------------------------------------------------------------------- | --------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| Procedure step 2, "It proposes `AGENTS.md` additions — gotchas and conventions…"                        | every addition is an `AGENTS.md` entry        | "It proposes additions to the artifact that owns each lesson — an `AGENTS.md` gotcha or convention, or a check/test authoring mode in the … skill" |
| Decision point, "**Every proposal names the existing bullet it extends**…"                              | the target is always a bullet                 | "**Every proposal names the existing rule it extends** — an `AGENTS.md` bullet, or a mode already in the … skill — or states that none covers it"  |
| Decision point, "IF the addition restates a remedy the **target bullet** already carries"               | the target is a bullet                        | "the **target rule** already carries"                                                                                                              |
| "How a captured rule is recorded", "a pre-commit guard enforces the mechanically checkable part"        | implies the guard checks every destination    | "enforces that part in `AGENTS.md` and in the project-authored agent and skill definitions"                                                        |
| Procedure step 4, "report any candidate lesson it leaves with the bullet it would extend"               | a merge-time proposal always targets a bullet | "with the rule it would extend (an `AGENTS.md` bullet, or a mode in the skill)"                                                                    |
| Marker paragraph, "On a bullet the capture merged into rather than authored, the end-of-bullet marker…" | a merged rule is always a bullet              | "On a rule the capture merged into … the end-of-rule marker … the rule's total origin"                                                             |

**The six clauses, exactly** (the text the implementation applies):

1. Step 2: "It proposes additions to the artifact that owns each lesson — an `AGENTS.md` gotcha or convention, or a
   check/test authoring mode in the `check-and-test-authoring` skill — the rules the unit makes obsolete or redundant…".
2. "**Every proposal names the existing rule it extends** — an `AGENTS.md` bullet, or a check/test authoring mode
   already in the `check-and-test-authoring` skill — or states that none covers the lesson, so a new rule merges into or
   replaces one rather than accreting."
3. "IF the addition restates a remedy the target rule already carries…".
4. "…a pre-commit guard enforces that part in `AGENTS.md` and in the project-authored agent and skill definitions
   (`.opencode/agent/*.md`, `.opencode/skills/*/SKILL.md`) — a marker lies inside a rule block, never on a plain
   bullet."
5. Step 4: "…report any candidate lesson it leaves with the rule it would extend (an `AGENTS.md` bullet, or a mode in
   the skill), then ask the user for explicit confirmation…".
6. "On a rule the capture merged into rather than authored, the end-of-rule marker records only the latest captured
   contribution, not the rule's total origin…".

**Routing criterion — by the decision the lesson governs, not its label.** A lesson is not routed because it is "a
failure mode"; it is routed by what a future agent must decide.

| Lesson                                                                                                                                                     | Destination                                                                                                     |
| ---------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------- |
| How to author or verify a check, test, or gate — whether it can fail, what it must read, where a control is injected                                       | The `check-and-test-authoring` skill                                                                            |
| A correction to a mode the skill already holds                                                                                                             | That section of the skill, in place                                                                             |
| How to interpret evidence without authoring a check — a search's completeness, a glob's vacuity, a reproduction's validity, a report's "nothing to report" | `AGENTS.md`'s `### Evidence and search`                                                                         |
| Both halves — an authoring rule that rests on an evidence rule (or the reverse)                                                                            | Split: the authoring half to the skill, the evidence half to root, and the skill does not restate the root rule |

**The `MODIFIED` block, verified mechanically.** The delta's replacement requirement was compared with the main spec's
requirement (`showcase/quality/agent-skills`, the block beginning "Per-change quality-gate and analysis subagents are
available") by a line-level diff: **one hunk**, at the two clause lines; every line before and after it is
byte-identical, and both sides carry 20 scenarios. The intentional differences are exactly those two clauses — the
destination (now permitting a split pair) and the rule a proposal extends — with line reflow inside them. Nothing else
differs, so the block neither drops nor adds an obligation. If the main requirement changes before implementation, the
block is regenerated from the then-current text and the diff re-run (task 3.11).

**Why both halves.** The definition edits make the caller and the capture subagent agree on the destination; the `ADDED`
requirement makes the destination and its no-restatement rule a durable outcome rather than a convention in a definition
the spec does not describe. No automated gate is added: prose placement is not mechanically checkable, and the
requirement's own scenarios are what the review and the audit read.

**The complete path after the change.** A unit's lesson is routed by the decision it governs: an authoring mode → the
capture proposes it with the skill as its destination and names the mode it extends (or states that none does) → the
main agent verifies and applies it in the skill section → the mode keeps its `captured:` marker → `AGENTS.md` gains
nothing. An evidence rule → the capture proposes it at root under `### Evidence and search`, and the skill is untouched.
A mixed lesson → the capture splits it, and neither destination restates the other's rule. A correction → it updates the
existing mode's section rather than adding a second rule.

## Ownership of the delta

| Obligation                                                                   | Owner after this change                                      | Why                                                                                                 |
| ---------------------------------------------------------------------------- | ------------------------------------------------------------ | --------------------------------------------------------------------------------------------------- |
| A check's control is a committed, failing test                               | `code-quality` (unchanged)                                   | The capability's quality contract; this change does not touch it                                    |
| Where check/test authoring guidance lives, and that root does not restate it | `agent-skills` (added)                                       | Placement and loading of agent guidance is this capability's subject (as is `AGENTS.md`'s standard) |
| How a capture proposes a destination, and how a correction merges            | `agent-skills` (`MODIFIED` capture requirement)              | The capture is specified there                                                                      |
| Which files the `captured:` marker guard scans                               | `commit-hygiene` (`MODIFIED`: the guard and the build check) | The marker-placement verification and its pre-commit counterpart are specified there                |

The round-1 proposal put the destination requirement in `code-quality`; that duplicated nothing but owned the wrong half
— the quality contract is about what a check must do, not about which artifact carries its authoring guidance. One delta
file per capability now owns its half: `agent-skills` the destination and its routing, `commit-hygiene` the widened
marker guard, and `code-quality` its control rule.

## Unit coherence

The four moves still form one implementation unit, on three tests:

- **One artifact.** Every move edits the same file, and three of the four edits interleave in its `## Gotchas` /
  `## Conventions` body; splitting them means two diffs over the same spans.
- **One verification protocol.** Each move is checked by the same four passes — obligation inventory, pointer
  resolution, retrieval scenario, measured reduction — and one review boundary covers them together.
- **One decision.** They apply one rule: a rule whose subject a task narrows to one moment leaves root for the artifact
  that task reaches. Move 1 is the only spec-bearing one; moves 2–4 are guidance-only.

What the corrections changed is Move 1's size, not the unit's justification: it fell from ~23 KB to ~12 KB because five
of its bullets are evidence rules that must stay at root, and it gained the capture-loop edits that make its destination
maintainable.

**Fallback, explicitly deferred unless the owner asks.** Move 1 (the skill, the two definition edits, and the delta) can
ship alone, with moves 2–4 following as a second guidance-only change. That split is defensible — Move 1 changes spec'd
behaviour and moves 2–4 do not — but it doubles the review cost for ~8 KB of trims over the same spans, so it is not the
recommendation.

**Not in this unit, by the same rule:** the five evidence-hygiene rules (kept at root: no trigger reaches them without
firing on most turns), the `## Conventions → Test display names` assertion modes (deferred: interleaved with naming
rules), `## Architecture`, and the gate-coupled deployment sections.

## Destination coverage map

| Destination                                                | Owns after this change                                                                     | Reached from                                                                    |
| ---------------------------------------------------------- | ------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------- |
| `.opencode/skills/check-and-test-authoring/SKILL.md` (new) | The eight check-authoring modes, with their evidence and the provenance markers they carry | Root trigger; the skill's always-listed description; `agents-auditor` audits it |
| `AGENTS.md` `### Evidence and search`                      | The five evidence-hygiene rules, unchanged                                                 | Always loaded                                                                   |
| `.opencode/agent/diagrammer.md`                            | Diagram geometry, host-renderer mechanics, fallback derivation, the visual-review handoff  | `/diagram` (always) and the retained diagram bullets                            |
| `.opencode/commands/diagram.md`                            | The caller procedure for drawing and reviewing a diagram                                   | `/diagram`                                                                      |
| `scripts/doctor.sh` (`probe_table`) and `README.md`        | The prerequisite list, version floors, and install commands                                | The retained `## Prerequisites` bullets; CI and local setup                     |
| `build.gradle.kts`                                         | The `check` member set                                                                     | The retained pointer; any `check` edit                                          |
| `agent-skills` spec / `lesson-capture.md`                  | The capture gate, the review loop, the report contracts, and now the skill's routing       | Unchanged plus this change                                                      |

## Retrieval-path validation

Each path is labelled by what has actually been exercised:

- **Demonstrated** — the mechanism exists in this checkout and was read: always-loaded root text, an existing command or
  definition, an existing spec statement.
- **Proposed** — the path depends on an artifact this change creates, so it cannot be exercised until implementation.
- **Residual reliance** — even a demonstrated path depends on an agent obeying an explicit instruction. A named
  destination is _not_ proof that the agent reads it.

| Task (does not name a destination)                           | Rules needed                                          | Where they are                    | Earliest decision                   | Fires before it?                                       | Evidence class |
| ------------------------------------------------------------ | ----------------------------------------------------- | --------------------------------- | ----------------------------------- | ------------------------------------------------------ | -------------- |
| "Determine whether this dependency is used anywhere."        | Under-matching search; catalog alias → accessor       | Root, `### Evidence and search`   | whether "no matches" means "unused" | Yes — always loaded                                    | Demonstrated   |
| "Audit the workflow permissions."                            | Search at any indentation; completeness counting      | Root, `### Evidence and search`   | whether the result set is complete  | Yes                                                    | Demonstrated   |
| "Explain why this reproduction disagrees with CI."           | Resolution context; clean-checkout state              | Root, `### Evidence and search`   | whether the reproduction is valid   | Yes                                                    | Demonstrated   |
| "Review this proposal's claim that no matching files exist." | Vacuous glob; under-match; `:(glob)` vs `**`          | Root, `### Evidence and search`   | whether to accept the claim         | Yes                                                    | Demonstrated   |
| "Add a control for the new gate."                            | The eight check-authoring modes                       | The skill                         | which input injects the control     | Yes — the trigger names this task                      | Proposed       |
| "Add a new event-flow diagram."                              | Mapping, drawing rules, render review                 | `/diagram` → command → definition | what the diagram must show          | Yes — the command loads before drawing                 | Demonstrated   |
| "Change an edge label in an existing Mermaid fence."         | The definition's label and contract rules             | `/diagram` → definition           | whether the edit is delegated       | Yes — the retained delegation bullet covers every edit | Demonstrated   |
| "Review the rendered diagram."                               | Mapping, render inspection, measurement-as-hypothesis | Retained bullets → `vision.md`    | whether the render may ship         | Yes                                                    | Demonstrated   |

**What a "proposed" row still needs, and what is verified now.**

- The skill's existence, its `description` in `<available_skills>`, and the `skill` tool's behaviour are
  **implementation-time** checks. The mechanism is demonstrated by the skills this session already lists with their
  descriptions; this skill's own visibility is not.
- Permission is verified statically: `.opencode/opencode.json` declares no `permission.skill` and no global `permission`
  scalar, and opencode's defaults are permissive for most actions, so no prompt stands between the agent and the skill.
  A subagent's access to the `skill` tool is **implementation-time** (no subagent overrides `tools` today).
- The trigger's firing is **residual reliance**: the root bullet says "load … before you write or change a test, a
  check, or a gate", and an agent that skips it writes the check without the modes. That is the same contract the two
  shipped extractions use, and it is stated here rather than described as deterministic.

**Failure cases, stated rather than assumed.** A candidate fails retrieval when the destination loads _after_ the
decision (the per-subagent descriptors), when it loads _instead of_ root guidance (nested `AGENTS.md`), when it widens
the surface (`instructions`), or when its trigger never fires for the task that needs it (the evidence rules behind a
check-authoring trigger — the round-1 defect this revision corrects).

## Estimated before/after size

The table is the **pre-implementation estimate**; the measured result is recorded in `tasks.md` task 3.7 and repeated
below it.

Measured spans minus drafted replacements; the implementation records the measured result. The "basis" column states
what the estimate rests on, so a figure can be re-derived rather than trusted.

| Move | Removed span(s)                                                | Measured |     Replacement (drafted) |       Est. net | Basis                                                                            |
| ---- | -------------------------------------------------------------- | -------: | ------------------------: | -------------: | -------------------------------------------------------------------------------- |
| 1    | 8 check-authoring bullets + the opening bullet's dropped intro | 12,460 B | 458 B (trigger + heading) |       −12.0 KB | 12,394 B of bullets and 66 B of dropped intro, minus the new trigger and heading |
| 2    | 5 diagram bullets + 3 `## Conventions` bullets                 |  8,092 B |                   1,784 B |        −6.3 KB | measured spans minus the drafted seven bullets                                   |
| 3    | `## Prerequisites` body                                        |  2,363 B |                   1,134 B |        −1.2 KB | measured span minus the drafted bullets                                          |
| 4    | `check` run line's task-name list                              |    647 B |                     254 B |        −0.4 KB | measured span minus the drafted 3-line summary                                   |
| 5    | root capture clauses: six rewrites (no removal)                |        — |                    +237 B |        +0.2 KB | the six clauses' drafted text measured against the current text                  |
|      | **`AGENTS.md` total**                                          |          |                           | **≈ −19.7 KB** | 23,562 B removed, 3,867 B restored (measured spans and drafts)                   |

**Measured after implementation:** the moves left `AGENTS.md` at 337,508 B / 3,451 lines (−19,215 B, −5.39%); the
capture and the marker-guidance edit that followed added 1,456 B (12 lines), so the unit's final state is **338,964 B /
3,463 lines** — **−17,759 B (−4.98%)**, −160 lines. The seven retained diagram bullets measured 2,316 B (the draft
estimated 1,784), the skill body 14,023 B, and `diagrammer.md` 5,665 → 6,970 B (+1,305).

**Always-loaded cost this change adds**, measured: the skill's `description` is 293 B. Net effect **−17,466 B
(−4.90%)**. The 14,023 B skill body and the +1,305 B definition addition load only on demand.

The earlier drafts' −30.9 KB is **withdrawn**: it counted the five evidence rules as extracted, which the trigger cannot
reach.

## Alternatives

| Alternative                                                          | Net reduction                                                           | Retrieval reliability                                                    | Semantic-loss risk                                                                       | Duplication / drift                                          | Maintenance                               | Placement compatibility                                 |
| -------------------------------------------------------------------- | ----------------------------------------------------------------------- | ------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------- | ------------------------------------------------------------ | ----------------------------------------- | ------------------------------------------------------- |
| **A. Split, one skill, pointers, closed capture loop (recommended)** | −19.2 KB measured (−5.4%); −19.7 estimated                              | High for the eight modes; the five evidence rules need no trigger at all | Low: verbatim moves, retained decisions, and the evidence rules stay where they are read | Low: one skill, one delta, the capture knows the destination | Low: one skill, one definition, one delta | Full: the placement rule's skill class; no rule changes |
| B. Move all thirteen modes behind a broadened trigger                | ≈ −23 KB (the 23.8 KB cluster minus the retained invariant and trigger) | Low: the trigger fires per search, or not at all for audits and reviews  | High: audit, review, and debugging rules would sit behind a check-authoring trigger      | Low                                                          | Low                                       | Breaks the "retrieve before the decision" condition     |
| C. Move the thirteen to a `docs/` reference, no capture change       | ≈ −23 KB (same base as B)                                               | Medium: one pointer, no owner                                            | Medium: same trigger defect as B, plus regrowth                                          | Medium: regrowth until a later fix                           | Medium                                    | Fails the "owned" condition                             |
| D. Keep the mode bullets; trim only moves 2–4                        | ≈ −7.9 KB (−2.2%)                                                       | High                                                                     | None                                                                                     | Grows: the capture keeps accreting root rules                | Low                                       | Full                                                    |
| E. Convert top-level procedures into on-demand skills                | not estimated — qualitative "large"                                     | Medium: model-chosen selection                                           | High: an always-needed procedure could be missed                                         | Low                                                          | High                                      | The redesign the investigation does not establish       |

**Recommendation: A.** It removed 19.2 KB (measured; 19.7 estimated) while every moved rule keeps a trigger that fires
before its decision, every retained rule stays where it is read, and the capture loop closes in the same change. B and C
buy ~3 KB more by losing the evidence rules behind a trigger that misses them. E's figure is withdrawn as unsupported
rather than restated.

## Risks / trade-offs

- **A test or check is authored without loading the skill.** The trigger is one imperative at root and the description
  is always listed; this is the same contract the two shipped extractions use. Residual risk accepted and stated.
- **The skill's own metadata is not yet exercised.** Description length, listing, loading, and subagent access are
  implementation-time checks (tasks 3.4 and 3.8), not claims of this design.
- **`captured:` markers leave `AGENTS.md`, so the marker guard was retargeted.** `verifyCapturedMarkers` now scans
  `AGENTS.md` and the project-authored agent and skill definitions (`.opencode/agent/*.md`,
  `.opencode/skills/*/SKILL.md`, vendored and generated trees excluded), with a known-bad test; the 16 moved markers
  stay placement-checked. Separately, the root-scoped accretion count falls by 19 while the rule corpus is unchanged
  (see "What the reduction measures") — a measurement artifact to state in the change's report.
- **The capture's destination list is now spec'd and grows.** A future non-check destination would owe the same two
  edits (definition plus capture requirement). That is the cost of closing the loop rather than deferring it.
- **`diagrammer.md` becomes the only home of the diagram mechanics.** The retained bullets carry the caller's decisions
  and state that every edit is delegated.

## Rollback

**Before commitment (the unit is uncommitted).** Reversing it means undoing this unit's edits in the files it touched —
not restoring whole files, since unrelated working-tree changes may share them — and removing the paths it added. The
surfaces are:

- `AGENTS.md` — the four moved spans' replacements, the six capture clauses, the `### Evidence and search` split, the
  `## Prerequisites` and `check`-list reductions, and the three extraction-workflow bullets the capture added.
- `.opencode/skills/check-and-test-authoring/` (added).
- `.opencode/agent/diagrammer.md` and `.opencode/agent/lesson-capture.md`.
- `build.gradle.kts` — `verifyCapturedMarkers`' inputs.
- `scripts/commit-hygiene.py`, `scripts/test-commit-hygiene.py`, and `scripts/git-hooks/pre-commit` — the widened marker
  scan, its known-bad test, and the hook's header comment.
- `openspec/changes/reduce-agents-md-loaded-surface/` (added), including both deltas.

The review-record rows this unit appended to `docs/review-rounds.tsv` are a record of the review, not an implementation
surface, and they stay. **Reverse the guidance, the verification, and the specification changes together.** Reverting
the guidance alone would leave the widened marker guard in place, scanning a tree that no longer carries the moved
markers while the deltas still describe that scope; reverting the guard alone would leave the 19 moved markers — 16 in
the skill and 3 in `diagrammer.md` — unverified. The unit is consistent only as a whole.

**After delivery (merged, and archived if the archive has landed).** Reversing the delivered unit means undoing exactly
its changes, not restoring the files it touched to older contents: later unrelated work may sit in the same files. That
covers the root guidance, the skill, both definitions, the Gradle inputs, the marker-checking code and its tests, and
the hook comment. At the specification level it covers these reversals and one removal:

- **Remove** the `agent-skills` requirement this unit added, "Check and test authoring guidance is maintained in its
  project skill".
- **Reverse in place** the two capture-routing clauses this unit changed in the `agent-skills` capture requirement — the
  destination, and the rule a proposal extends.
- **Reverse in place** the `commit-hygiene` scope widening in the pre-commit-guard requirement and in the
  marker-placement verification, and drop the scenario this unit added for a misplaced marker in a project guidance
  file; the activation requirement the delta carries unchanged needs no reversal.
- **Reverse** the archive-time `agent-skills` Purpose refresh (task 4.7) if it has landed with the archive.

The change dir returns from `openspec/changes/archive/`. Other work merged since then is preserved, so the result is
this unit's changes reversed rather than the repository restored. No migration or dependency effect.

## Migration

None. Guidance takes effect on the next read; the new skill, the edited definitions, and the delta (at archive) are
picked up by the next session.

## Claims

Each fact the planning artifacts state about their evidence, with its source or deriving command.

| #   | Claim                                                                                                                                                                                                                                                                                                                                     | Source / deriving command                                                                                                                                                                                                                                          |
| --- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 1   | Baseline is `a36c3f7e` with a clean working tree                                                                                                                                                                                                                                                                                          | `git rev-parse HEAD` → `a36c3f7ea9f5b8e9a38c9eb7e253dd539c16412d`; `git status --porcelain \| wc -l` → `0`                                                                                                                                                         |
| 2   | `AGENTS.md` is 356,723 bytes across 3,623 lines                                                                                                                                                                                                                                                                                           | `wc -c -l AGENTS.md` → `3623   356723 AGENTS.md`                                                                                                                                                                                                                   |
| 3   | The section table reproduces under the stated convention                                                                                                                                                                                                                                                                                  | python3 over `^## ` headings, each range trimmed to its last non-blank line → rows sum to 356,697 bytes / 3,608 lines; `## Gotchas` is `sed -n '2104,3623p' \| wc -l` → 1,520                                                                                      |
| 4   | The audit's figures describe its own checkout                                                                                                                                                                                                                                                                                             | `docs/audits/2026-10-19.md` "Instruction-budget ledger" states 363,767 bytes / 3,681 lines against row 2's 356,723 / 3,623                                                                                                                                         |
| 5   | The cluster's `sed` range is 213 lines / 23,833 bytes, its bullet ranges sum to 23,832 without the heading's blank line, and each per-bullet figure is as the split table states                                                                                                                                                          | `sed -n '2394,2606p' AGENTS.md \| wc -l -c`; python3 over the bullet ranges → 451, 842, 1,709, 3,221, 2,674, 1,764, 3,337, 935, 2,558, 1,452, 1,055, 649, 2,581                                                                                                    |
| 6   | The eight moved bullets total 12,394 B; the five retained total 10,834 B                                                                                                                                                                                                                                                                  | python3 sum over the two groups from row 5's census                                                                                                                                                                                                                |
| 7   | The five diagram gotcha bullets are 50 lines / 5,170 bytes                                                                                                                                                                                                                                                                                | `sed -n '3238,3287p' AGENTS.md \| wc -l -c`                                                                                                                                                                                                                        |
| 8   | The three `## Conventions` bullets total 26 lines / 2,922 bytes                                                                                                                                                                                                                                                                           | `sed -n '1424,1429p;1552,1571p' AGENTS.md \| wc -l -c`                                                                                                                                                                                                             |
| 9   | `## Prerequisites` is 29 lines / 2,363 bytes; the `check` task-name list 8 lines / 647 bytes                                                                                                                                                                                                                                              | `sed -n '834,862p' AGENTS.md \| wc -l -c`; `sed -n '887,894p' AGENTS.md \| wc -l -c`                                                                                                                                                                               |
| 10  | The two audit opportunities shipped, so the current file is post-extraction                                                                                                                                                                                                                                                               | `ls -d openspec/changes/archive/2026-10-19-extract-load-test-commands openspec/changes/archive/2026-10-19-extract-ci-narrative`; `git log --oneline` shows `afeb27a9` and `cc59a892`                                                                               |
| 11  | The three remaining "safe to load on demand" rules were verified to fail retrieval and stay                                                                                                                                                                                                                                               | `openspec/changes/archive/2026-10-19-add-extraction-procedure/design.md`, Context                                                                                                                                                                                  |
| 12  | The capture section's promotion-gate list is deliberately retained by the owner                                                                                                                                                                                                                                                           | `openspec/changes/archive/2026-10-11-trim-restated-agents-md-rules/design.md`, "Candidate 4"                                                                                                                                                                       |
| 13  | `verifyCapturedMarkers` now reads `AGENTS.md` and the project-authored guidance files                                                                                                                                                                                                                                                     | `build.gradle.kts`'s `verifyCapturedMarkers` inputs; `scripts/commit-hygiene.py`'s `MARKER_GLOBS`/`marker_paths`                                                                                                                                                   |
| 14  | Three gates read `AGENTS.md`; none of the moved spans holds the helm install block or the Paketo pins                                                                                                                                                                                                                                     | `build.gradle.kts:478`, `:599`, `:610`; `grep -c 'helm install\|paketo-nginx'` over the four spans → 0                                                                                                                                                             |
| 15  | The doctor probes the tools the list named, plus `gh` and `opencode`, and the README documents its own rows                                                                                                                                                                                                                               | `sed -n '115,130p' scripts/doctor.sh` → `java docker compose actionlint pack helm kubectl snyk python3 gh opencode git-hooks docker-daemon`; `sed -n '547,580p' README.md` (no Gradle row, by design)                                                              |
| 16  | `testDoctorCharter` asserts the doctor/README/spec agreement and is a `check` member                                                                                                                                                                                                                                                      | `build.gradle.kts:502-509` (`inputs.file("README.md")`), `build.gradle.kts:647`                                                                                                                                                                                    |
| 17  | `agent-skills` already states the diagrammer's and vision's obligations                                                                                                                                                                                                                                                                   | `grep -n 'ASCII diagrams are drawn\|Mermaid diagrams are drawn\|Mermaid render is visually reviewed' openspec/specs/showcase/quality/agent-skills/spec.md` → 533, 541, 554                                                                                         |
| 18  | `diagrammer.md` already carries most mechanics but not the eight marked items                                                                                                                                                                                                                                                             | `grep -ci 'character width\|diagramPadding\|asymmetry\|fallback\|viewscreen\|non-nested' .opencode/agent/diagrammer.md`; no match for a comment-column rule, the toolbar width, a label re-check, or "re-derive … from the code"                                   |
| 19  | `vision.md` already carries the `http(s)`-only and measurement-as-hypothesis rules                                                                                                                                                                                                                                                        | `.opencode/agent/vision.md:15-16`, `:44-45`                                                                                                                                                                                                                        |
| 20  | `.opencode/commands/diagram.md` carries the caller's diagram procedure (mapping, ASCII end, Mermaid review)                                                                                                                                                                                                                               | `.opencode/commands/diagram.md` "**Procedure**", steps 1–6                                                                                                                                                                                                         |
| 21  | The placement rule scopes root guidance to "before safely starting work" and blesses a durable destination the trigger reaches                                                                                                                                                                                                            | `openspec/specs/showcase/quality/agent-skills/spec.md:740-774`                                                                                                                                                                                                     |
| 22  | `agents-auditor` owns project-authored files under `.opencode/agent/`, `.opencode/commands/`, and `.opencode/skills/`                                                                                                                                                                                                                     | `openspec/specs/showcase/quality/agent-skills/spec.md:690-694`                                                                                                                                                                                                     |
| 23  | The capture requirement states the subagent "proposes `AGENTS.md` gotchas/conventions", and `lesson-capture.md` fixes the output to "the exact AGENTS.md text and where it belongs"                                                                                                                                                       | `grep -n 'proposes `AGENTS.md` gotchas/conventions' openspec/specs/showcase/quality/agent-skills/spec.md` → 416; `sed -n '34p' .opencode/agent/lesson-capture.md`                                                                                                  |
| 24  | The capture requirement block is 428 lines / 36,861 bytes with 20 scenarios, so the `MODIFIED` delta copies it whole and preserves all 20                                                                                                                                                                                                 | python3 over `spec.md[139:567]` → 428 lines, 36,861 bytes, 20 `#### Scenario:` lines; the delta's block carries 20 after the edits                                                                                                                                 |
| 25  | The capture requirement's failed-gate routing names "a check, a spec, an ADR, or the change dir"                                                                                                                                                                                                                                          | `sed -n '87p' .opencode/agent/lesson-capture.md`                                                                                                                                                                                                                   |
| 26  | `code-quality` owns "A check's control is a committed test" and no placement obligation                                                                                                                                                                                                                                                   | `grep -n "A check's control is a committed test" openspec/specs/showcase/quality/code-quality/spec.md` → 459                                                                                                                                                       |
| 27  | opencode loads the first `AGENTS.md` found traversing up from the working directory; no nested file exists                                                                                                                                                                                                                                | `https://opencode.ai/docs/rules/`; `git ls-files \| grep -E '(^\|/)AGENTS\.md$'` → `AGENTS.md` alone                                                                                                                                                               |
| 28  | A skill's description is listed in every session and its body loads on invocation; an `instructions` entry loads every session                                                                                                                                                                                                            | `https://opencode.ai/docs/skills/`; `https://opencode.ai/docs/rules/`                                                                                                                                                                                              |
| 29  | Skills are permitted without a prompt in this repository                                                                                                                                                                                                                                                                                  | `https://opencode.ai/docs/permissions/` ("most permissions default to `allow`"); `.opencode/opencode.json` has no `permission.skill` and no global `permission` scalar                                                                                             |
| 30  | A project skill's `SKILL.md` is Spotless-gated and its directory name must match its `name`                                                                                                                                                                                                                                               | `build.gradle.kts:27` (`".opencode/skills/**/*.md"`), `:37-38`; the skills documentation's name rule                                                                                                                                                               |
| 31  | The `check` list had already drifted once, by omitting `testReviewRouting`                                                                                                                                                                                                                                                                | `docs/audits/2026-10-19.md`, agent-tooling finding 2; `git show 0f70b824 --stat`                                                                                                                                                                                   |
| 32  | No `instructions` array and no size cap is configured                                                                                                                                                                                                                                                                                     | `.opencode/opencode.json` (keys: `$schema`, `model`, `small_model`, `permission`, `mcp`)                                                                                                                                                                           |
| 33  | The cluster carries 30 `captured:` occurrences: 16 markers in the eight moved modes, 13 markers plus one inline-code mention of the token in the five retained evidence rules; the diagram gotchas carry 3 and the file carries 297                                                                                                       | python3 census over the bullet ranges, and `re.findall(r'captured:')` over the whole file                                                                                                                                                                          |
| 34  | `docs/ideas.md` is 496 lines and the eight reconciled entries were read at their quoted text                                                                                                                                                                                                                                              | `wc -l docs/ideas.md` → 496; the eight entries quoted in "Parked ideas reconciled"                                                                                                                                                                                 |
| 35  | `review-quick.md` carries its own `test-control` class, so a reviewer's route is not the skill                                                                                                                                                                                                                                            | `grep -n 'test-control' .opencode/agent/review-quick.md` → 101, 112                                                                                                                                                                                                |
| 36  | `agent-skills` requires the diagrammer for a diagram "created, aligned, or fixed" (ASCII) and "created, fixed, or reworked" (Mermaid), so delegation is the contract                                                                                                                                                                      | the spec's scenarios "ASCII diagrams are drawn by the pro-model diagrammer" and "Mermaid diagrams are drawn by the pro-model diagrammer"                                                                                                                           |
| 37  | The six root capture clauses sit at the capture procedure's step 2, its "Every proposal names the existing bullet it extends" decision point, "the target bullet" in the restated-remedy branch, the marker bullet's guard sentence, step 4's "the bullet it would extend", and the marker paragraph's "a bullet the capture merged into" | `grep -n 'AGENTS.md additions\|existing bullet it extends\|target bullet already carries\|pre-commit guard enforces\|with the bullet it would extend\|On a bullet the capture merged into' AGENTS.md` → lines 501, 508, 523, 538, 593, 594 (and an unrelated 2181) |
| 38  | The delta's `MODIFIED` block differs from the main requirement in one hunk, with every other line byte-identical and 20 scenarios on both sides                                                                                                                                                                                           | `difflib.SequenceMatcher` over `spec.md[139:567]` and the delta's block → `hunks 1`, `pre-hunk identical: True`, `post-hunk identical: True`, scenarios 20/20                                                                                                      |
| 39  | The six capture-clause rewrites add 237 B                                                                                                                                                                                                                                                                                                 | python3 over the six clause pairs (99 + 104 − 2 − 7 + 49 − 6)                                                                                                                                                                                                      |
| 40  | The rollback surface list matches the unit's changed files: eight tracked modifications plus two added trees                                                                                                                                                                                                                              | `git status --porcelain` (8 `M` rows, `docs/review-rounds.tsv` the review record) and the two untracked dirs                                                                                                                                                       |
| 41  | The marker guard scans `AGENTS.md` plus 13 project-authored files and excludes the vendored and generated skills                                                                                                                                                                                                                          | `python3` over `marker_paths` → 14 paths, none under `axon4to5-`/`openspec-`; `build.gradle.kts`'s `exclude("axon4to5-*/**", "openspec-*/**")`                                                                                                                     |

**Dismissals**: **path candidates** name a file, task, or workflow the text points at rather than asserting a fact about
the evidence; **cross-reference candidates** name a repository path or a change name as a pointer; **absolute-quantifier
candidates** are quantifier words inside a rule's own sentence, not a universal claim about a corpus; and **count
candidates** are numerals inside the unit's own drafted replacements and recorded spans, not counts of a corpus the
change rests on.
