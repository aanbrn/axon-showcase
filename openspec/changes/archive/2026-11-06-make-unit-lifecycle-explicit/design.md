# Design — make the unit lifecycle explicit

## Context

See `proposal.md` — Why. The repository has just decided that a unit of work is the process's base unit and that an
OpenSpec change is one form it takes (`AGENTS.md:126-129`, ADR-0029's `## Amendment — 2026-11-06`). That decision states
the layering but not the lifecycle: the governing procedure is titled "Workflow: Change lifecycle" while its trigger
governs every unit (`AGENTS.md:307-309`), its eight steps mix unit-generic and change-specific work, workspace
establishment is split between `/new-worktree` and prose, and no entry point declares the stage it serves.

The constraints are recorded rather than re-derived: ADR-0025 (behavior in the capability spec, procedure in the skill,
"a component names its dependencies' owners rather than carrying a second copy"), ADR-0026 (bindings are declared;
role-not-string), ADR-0029 (the unit record lives outside the change plan), ADR-0024 (the tree write right is not
session-bound).

## Goals / Non-Goals

**Goals**

- One published lifecycle with named stages, transitions, exits, and approval boundaries.
- Each stage's procedure has exactly one owner, and each entry point declares what it serves.
- The OpenSpec change's operations sit inside the unit stages they belong to, and the archive is conditional on a change
  directory.
- A mechanical check keeps the declaration from drifting the way the prose did.

**Non-Goals**

- No workflow engine, substitution system, unit registry, or persistent stage ledger.
- No behavioural change to review, delivery authority, or workspace safety.
- No edits to generated `opsx-*` definitions or vendored skill trees.
- No new capability directory; the behaviour belongs to `agent-skills` and the check to `commit-hygiene`.

## Decisions

### 1. Seven stages, with review as stages at the two approval boundaries

`Begin`, `Plan`, `Proposal review`, `Implement`, `Implementation review`, `Deliver`, `Settle`. Review is a stage because
each boundary is an observable hand-off with its own exit (approval, correction, or a material-scope return), not merely
a repeatable step; its internal procedure stays `review-gate`'s.

_Rejected:_ four stages with review as a gate only — it hides the two hand-offs that produce every owner decision, and
the process already fails at them (an unanswered approval is the most common stall). _Rejected:_ a separate `Workspace`
stage beside `Begin` — workspace establishment is Begin's content, not a peer. _Rejected:_ naming stages after the
record's states — states are `active`/`paused`/`closed`, and conflating them loses pause-over-any-stage.

### 2. Stage, state, pending decision, and outcome are distinct; no second ledger

The record keeps the `active`/`paused`/`closed` vocabulary; the state requirement gains only an `active` write at Begin
and the clause that `closed` proves no delivery. A stage is derived from the unit's artifacts, the pending decision, and
the delivery state; a disagreement stops rather than being resolved by guesswork. `closed` is a pre-merge write and
proves neither merge nor settlement.

_Rejected:_ a `Stage:` field — a second state axis to reconcile, and the checkpoint's `Stage` already names the
interrupted operation. _Rejected:_ an event ledger — it would duplicate the records the process already keeps.

### 3. Begin is one procedure with one entry point

A new `begin-unit` skill owns the ordered workspace establishment. Its steps are:

- take the owner's authorization, the unit's identity, and its new or continued mode;
- inspect the registered trees without changing an occupied one, and select the primary or the sibling path;
- establish the branch — a new one from a freshly fetched `origin/main`, or the existing one for a continuation;
- delegate tree creation and preparation to `scripts/new-worktree.sh`;
- place the session and verify its actual path and branch;
- write the unit record, and the claim where the assignment requires one;
- report the established stage, the pending decision, and the next permitted operation.

`/begin-unit` loads the skill and carries the full preflight a unit-starting command owes. `scripts/new-worktree.sh`
keeps its name and its bounded creation-and-preparation function.

_Rejected:_ keeping `/new-worktree` and having the lifecycle point at it — the name describes the mechanism, not the
stage, and the primary path, record, and claim would stay in prose. _Rejected:_ renaming without widening — the name
would promise the whole Begin and deliver the sibling path only. _Rejected:_ putting the procedure in the command alone
— it would give direct invocation and lifecycle invocation two procedural homes.

### 4. A command declares its lifecycle entry in its own body

Each project-authored command carries a small block: **Lifecycle** (a stage, or cross-cutting/outside-unit),
**Invocation** (owner-invoked, procedure-invoked, scheduled), **Effect** (report-only, host operation, unit start, or an
operation within an existing unit), and **Owner** (the procedure that owns it). A branching command declares each
branch. Generated definitions are not edited; their repository mapping lives in the lifecycle section.

_Rejected:_ frontmatter tags — the loader contract is not established here and the file already reads as prose.
_Rejected:_ a root registry table — a second copy that drifts and no gate reads. _Rejected:_ no declaration at all —
that is today's state.

### 5. One narrow mechanical check, with committed controls

`verifyCommandLifecycle` (a `Task` + `CommandLifecycleRules` + `*RulesTests`, in the `VerifyDocumentedPinsTask` shape).
It verifies:

- that every project-authored command carries a declaration;
- that its stage, invocation, and effect values are recognized;
- that every named owning procedure and bound heading resolves;
- that a unit-starting branch carries the preflight anchors;
- that the generated entry mapping covers every generated command;
- that coverage is derived from the tracked file set, so an undeclared new command fails rather than passing as an empty
  extraction.

The rules live as a plain object tested in `build-logic`'s `src/test` — that build has no `gradleTestKit()`, so a
plugin's Gradle DSL is not unit-testable there. The wiring into `check` is verified in two steps:
`./gradlew check --dry-run` lists the task in the graph, and a deliberately undeclared command makes a real
`./gradlew check` run fail before it is removed — a dry run executes no task action, so it cannot prove the failure. Its
claim is deliberately narrow: completeness, values, references, anchors, and coverage — never behaviour.

_Rejected:_ no check — the drift this change repairs (a mixed procedure, an unconditional archive, a split Begin)
survived every existing gate for many changes. _Rejected:_ a behavioural check — it cannot prove that a command's prose
has its declared effect.

### 6. Archive is conditional; `skip_specs` still archives

A unit that carries a change directory archives it inside Deliver, after green CI and before the merge. A unit without
one completes delivery without an archive. `skip_specs` removes the delta obligation, not the archive.

_Rejected:_ leaving the unconditional clauses — they contradict the change-less units the process already runs.

### 7. The narrow publication exceptions are preserved explicitly

The lifecycle's prohibition on publishing before the implementation's approval keeps its existing exceptions: a commit
the owner explicitly requests, and the proposal-stage draft pull request the procedure documents.

_Rejected:_ stating the prohibition unconditionally — it would contradict the documented draft-PR route and the
forced-commit rule.

### 8. `ship-change` keeps its identifier and becomes unit-facing

The skill's document is retitled and re-scoped ("Ship and settle a unit"), its archive step becomes conditional, and its
completion criteria name settlement. The identifier stays for compatibility; renaming established identifiers is not
what makes the model explicit.

_Rejected:_ renaming every skill and binding to the new vocabulary — churn without a behavioural gain.

### 9. One change, not a program of units

The ownership transfers, the Begin entry, the conditional archive, and the enforcement depend on one another: a model
shipped without its entry points would be the glossary-only fix the owner rejected, and an entry point shipped before
the model would codify the old vocabulary.

_Rejected:_ an orientation unit followed by functional units — it would present a "model shipped, implementation later"
intermediate as the answer.

## Amended clauses

A `MODIFIED` block replaces the whole requirement and `openspec validate` checks only scenario preservation, so each
amended clause is quoted beside its replacement and the reason it moves.

### A unit's record carries its state

- **Amended** — "`active` is written when a resume continues the work, after its state verification and the writer
  handover." **Replacement** — "`active` is written when the unit's workspace is established at Begin, and when a resume
  continues the work, after its state verification and the writer handover." **Why** — the requirement left the write at
  Begin unstated, so a new record's initial state rested on convention; the state requirement owns the vocabulary, so
  the initialization belongs there rather than in a second artifact.
- **Amended** — "`closed` is written when the unit is delivered, rejected, or abandoned." **Replacement** — "`closed` is
  written when the unit enters delivery, or is rejected or abandoned." **Why** — the pre-merge write marks entry into
  delivery, not its completion, so the original wording contradicted the added clause.
- **Added** — "A `closed` record SHALL NOT be read as proof that the unit merged, that its delivery completed, or that
  its workspace was released." **Why** — the model's Settle stage and the delivery outcome would otherwise be inferred
  from `closed`, which is a pre-merge write.

### Project-authored command definitions that start a mutating unit carry the lifecycle preflight

- **Amended** — "and no commit, push, pull request, archive, or merge without that approval." **Replacement** — the same
  clause with "apart from a commit the owner explicitly requests and the proposal-stage draft pull request the
  lifecycle's procedure documents." **Why** — the root procedure documents both routes, so the unconditional form
  contradicts it, and a unit-starting command's preflight is where an implementer reads the prohibition.
- **Amended** — the scenario "Work stays uncommitted until approval" ends "and no commit, push, pull request, archive,
  or merge happens before it". **Replacement** — the same sentence followed by "apart from the exceptions this
  requirement's description names." **Why** — without it the retained scenario contradicts the amended description.
- **Added** — "It SHALL … carry the declaration the requirement
  `A project-authored command declares the lifecycle entry it serves` states." **Why** — the declaration is its own
  requirement, so the preflight points at it rather than restating it.

All other scenarios of both requirements are retained unchanged and in their existing order.

## Risks / Trade-offs

- **Prose accretion replaces prose fragmentation** → a structural removal budget: every moved procedure has one
  destination, its old body is deleted, and each remaining repetition is a required safety imperative or an explicit
  pointer.
- **The binding rename breaks a consumer's lookup** → the `lifecycle` role name stays; its declared value changes
  atomically with the heading, and `workspace-record` and `branch-refresh` are retargeted in the same commit. The
  consumers name roles, not literals.
- **Twenty-five project-authored command files gain a block** → only the declaration is added; each command's procedure
  is untouched, and the generated definitions are not edited.
- **The check can drift from reality** → its coverage derives from the tracked set, and its committed controls include
  the undeclared-command and empty-extraction cases.
- **Historical records quote the old heading and the old command name** → left as recorded, per the bindings' historical
  non-adopter class.
- **Worktree safety is lost in the move** → the script keeps its creation, preparation, readiness, and refusal
  behaviour; the skill inherits the command's decision points and failure handling verbatim.

## Migration Plan

1. Decisions and model: ADR-0030, the `agent-skills` lifecycle requirement, and the root section replacing the mixed
   procedure.
2. Begin: the skill, the command, workspace-guidance reconciliation, bindings, consumer trace, README, and the placement
   example.
3. Boundaries and delivery: `review-gate`, `pause-resume-unit`, `ship-change`, the conditional archive, `closed`
   semantics, and the publication exceptions.
4. Projection: the declaration block in every project-authored command, the generated mapping, and
   `verifyCommandLifecycle` with its controls.
5. Cleanup and verification: remove the replaced text and the retired command, sweep the live references, and run the
   composed routes.

## Verification

- The check's committed controls: one case per predicate, including an undeclared command and an empty derived set.
- `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` over the finished tree, with the new task wired into
  `check`.
- Composed routes exercised in disposable repositories: a free primary, an occupied primary (sibling path), a failed
  preparation, a continued unit, an unanswered approval, a change-less delivery, and an archived-but-unmerged
  continuation.

## Claims

Base revisions: the rows below read the state before this change at `805f30ef` — the `git merge-base` when the proposal
was written — so `git show 805f30ef:<path>` reproduces them and their line numbers are that revision's. The
implementation's base is `f45ef735`, after the branch was refreshed.

| Claim                                                                                                                                      | Source                                                                                                                                                                                                          |
| ------------------------------------------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The lifecycle section is titled for the change form while its trigger governs units                                                        | `git grep -n -A 2 '^### Workflow: Change lifecycle' 805f30ef -- AGENTS.md` → the heading and its `any unit of work` trigger                                                                                     |
| Archive is unconditional in two completion surfaces                                                                                        | `git show 805f30ef:AGENTS.md` lines 463-464 → `committed, pushed, and archived`; `git show 805f30ef:.opencode/skills/ship-change/SKILL.md` → `the change archived`, while its unit-without-a-change line stands |
| `/new-worktree` covers the sibling path and `--continue` only                                                                              | `git show 805f30ef:.opencode/commands/new-worktree.md`; the primary path, record, and claim live in `AGENTS.md` lines 327-369 at that revision                                                                  |
| The `lifecycle` binding resolves to that heading, and 16 consumers name the role                                                           | `git grep -n 'lifecycle' 805f30ef -- docs/contracts/process-bindings.md` → the role row plus 16 consumer rows                                                                                                   |
| The heading is cited in two `AGENTS.md` places and three binding rows                                                                      | `git grep -c 'Workflow: Change lifecycle' 805f30ef -- AGENTS.md docs/contracts/process-bindings.md` → 2 and 3                                                                                                   |
| The command set is 31 files, 6 of them generated; 25 are project-authored                                                                  | `git ls-tree --name-only 805f30ef .opencode/commands/` → 31 paths; 7 basenames match `opsx-`, and `opsx-tool-update` is project-authored per the provenance partition                                           |
| A parked design for `verifyCommandLifecycle` exists                                                                                        | `git show 805f30ef:docs/ideas.md` lines 507-517                                                                                                                                                                 |
| `VerifyDocumentedPinsTask` is the task/rules/test shape to copy                                                                            | `build-logic/src/main/kotlin/VerifyDocumentedPinsTask.kt` at that revision                                                                                                                                      |
| The publication prohibition is unconditional in the spec, while the root procedure allows a requested commit and a proposal-stage draft PR | `git show 805f30ef:openspec/specs/showcase/quality/agent-skills/spec.md` lines 1185-1187; `AGENTS.md` lines 394-397 and 408-410 at that revision                                                                |
| The preflight requirement enumerates the anchors a unit-starting command owes                                                              | `git show 805f30ef:openspec/specs/showcase/quality/agent-skills/spec.md` lines 1179-1222                                                                                                                        |
| The state requirement owns the three states and the pre-merge `closed` write                                                               | `git show 805f30ef:openspec/specs/showcase/quality/agent-skills/spec.md` lines 2879-2921                                                                                                                        |
| The pause-and-resume skill-ownership requirement is the pattern for Begin's                                                                | `git grep -n 'pause-and-resume procedure is maintained' 805f30ef -- openspec/specs/showcase/quality/agent-skills/spec.md` → `:2183`                                                                             |
| `scripts/new-worktree.sh` leaves session movement to its caller                                                                            | `git show 805f30ef:scripts/new-worktree.sh` line 15, with the same statement echoed in its reports                                                                                                              |
| The README carries a `/new-worktree` row and the parallel-units narrative                                                                  | `git grep -n '/new-worktree' 805f30ef -- README.md` → the table row and the narrative                                                                                                                           |

### Dismissals

Each reason is stated by class, and the class governs every candidate the scan returns for it.

- `path` — a candidate is a locator: one of this change's own artifacts, a file the base revision holds, or a command
  the tasks prescribe.
- `absolute` — a candidate is the word a scope statement or a prohibition is phrased with, around an obligation this
  change states; a candidate carrying a universal claim about the corpus has its row above.
- `count` — a candidate is a numeral from an identifier or a command's reported output (a total, an exit status, a line
  count); none is a count this change restates as durable.
- `cross-ref` — a candidate is a name, identifier, or heading locating a repository artifact, binding, or record rather
  than citing a requirement this change must resolve.
