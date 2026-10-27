# Design

## Context

The repository generalizes a unit's process lessons inline. `AGENTS.md`'s "Workflow: Capture lessons" (143 lines,
`AGENTS.md:463–605`) triggers the `lesson-capture` subagent once the implementation's quick review is clean, and its
trigger names the moment "before you ask for the manual review pass". The `agent-skills` spec owns the analysis contract
in the requirement "Lessons are captured after implementation" (`spec.md:2133–2250`, four scenarios).

Four facts about that state, derived from the repository rather than assumed:

- **Nothing records why the analysis must be inline.** `docs/adr/` has no decision about it, `docs/ideas.md` has no
  parked entry asking for the separation, and the spec states the placement as an obligation with no rationale.
- **The gate enforces that sequence, and only recently.** `review-gate` §5 requires the unit's capture record at the
  implementation stage, beside the pre-write pass, the closing round with no finding class (`--expect-clean`), and the
  "base has not moved" check (`review-gate/SKILL.md:158`, `:270`). The parked idea that named the missing precondition
  was removed by the change that added it (`61bcdee2`). The merged base states that precondition in three places — §5's
  clause (`:158`), the skill's completion criteria (`:270`), and the review-gate bullet in `AGENTS.md` (`:454`) — and
  this change edits all three rather than adding a precondition to a gate that lacked one.
- **The hand-off the analysis needs is already written down, but not durably.** `lesson-capture.md` requires "a short
  note from the main agent describing what went wrong or was learned during the work (process mistakes often leave no
  trace in a diff, so this note is essential)" (`:26`). The spec states the same property of the experience-analyzer's
  input — "available only in the session that lived it" (`spec.md:1027`) — which holds for this note by analogy rather
  than by a statement about it. Today that note exists only as an argument of one invocation.
- **The existing per-unit ledger records classes, not instances.** `docs/review-rounds.tsv` carries a round's classes,
  and the review gate states that "the instances live only in that report, not the class-only record line"
  (`review-gate/SKILL.md:186`), so a later analysis reading only the ledger would reconstruct what it needs.

The owner's constraint: one participant today, a team later; the process must be configuration-independent, and the
moment for generalizing accumulated defects is the owner's decision. The separation therefore has to remove a sequential
dependency without inventing team machinery that nothing exercises yet.

## Decisions

### D1. A defect is recorded in a per-unit file under `docs/process-defects/`

One file per unit: `docs/process-defects/<unit>.md`, where `<unit>` is the change name for a unit with a change dir and
a slug naming the work for one without.

Alternatives rejected:

- **A section in the change dir** — the archive relocates and hides it; `openspec/changes/archive/` is outside the
  formatter target and no gate reads it, so a record there is invisible to the next person.
- **One rolling log** — every unit appends to one file, so concurrent units conflict on every write, and a record's
  identity is positional rather than stable.
- **The pull request body** — squashed at merge, so the record inherits the lifetime of a message.

The record is committed with the product unit's pull request, so it is on `main` before any generalization pass reads
it, and it is not archived with the change.

### D2. `AGENTS.md` owns the record's format; the spec owns the outcome

The recorder acts while implementing, from the always-loaded file, so the field set, the path, and the naming rule live
in `AGENTS.md`'s recording workflow. The spec states what the record must let a reader establish — provenance,
deviation, evidence, consequence and recovery, and the boundary of the author's knowledge — without restating the field
names, since that is the property the hand-off depends on.

This keeps one authoritative field list (the recording site) while leaving the spec's scenario testable.

### D3. The product-unit gate is the hand-off, not the analysis

The `review-gate` skill's §5 replaces its capture-record clause with the record-currency precondition: the unit's record
exists and reflects what the unit observed, or states that it observed none. Analysis completion is not a precondition,
and the record's presence is not a claim that no defect occurred. The gate's other preconditions stay as they are, and
`AGENTS.md`'s trigger loses its manual-review coupling and names the observation moment instead.

Rejected: keeping the capture-record clause beside the new one — the two would require a record of an analysis the
change removes, so the clause is superseded rather than augmented.

Rejected: gating on "records analyzed" — it would re-couple the product unit to the analysis it is being separated from.

### D4. Generalization is an on-demand unit, selected by the owner

A new command, `/generalize-process-defects`, passes selected merged records to `lesson-capture`; the calling session
designs and implements the approved improvements. The owner decides when the moment is; no schedule, no queue-drain
guarantee, and no automatic assignment.

Rejected: running it from the scheduled `audit` workflow — that automates a decision the owner reserves, and the
`agent-skills` spec already rejects a scheduled substitute for analysis whose richest input is session-only
(`spec.md:1021–1027`).

### D5. The analyzer stays `lesson-capture`

No new subagent: the existing one owns the promotion gate, the evidence threshold, the dispositions, and the
destinations. What changes is its trigger and its inputs — selected defect records, the existing guidance a candidate
would extend, and the prior findings — not its contract.

Rejected: `experience-analyzer` — its subject is a period's narrative, and merging the two would make every
generalization pass a retrospective.

### D6. The record rides the product unit; the improvement is its own unit

The record ships with the product change. The improvement is a unit of its own, on its own branch and tree from a fresh
`origin/main`, consuming records that are already merged.

This is a narrow exception to "docs the change causes while it is in flight ship with its pull request": the
process-improvement edit is caused by the product unit's record but is chosen, analyzed, and approved later, so it
cannot ride a branch that no longer exists. Product fixes and the documentation describing them are not excepted.

### D7. Roles are functional and configuration-independent

`AGENTS.md` states who records (the unit's session), who generalizes (the process owner; the owner by default, otherwise
the person the unit's tasking names), who chooses the moment (the owner), and who approves (the owner, unchanged). No
role registry, no `CODEOWNERS` change, and no delegation of approval: being named the generalizer grants no merge or
self-approval authority.

The record's fields, the gate, and the analysis contract hold for one participant and for N; only the assignment varies.

### D8. What retires, and why

| Retired                                                     | Why                                                                                                              |
| ----------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| The four-clause skip condition                              | It decided whether an inline analysis was owed; no inline analysis remains.                                      |
| The per-unit capture record (applied delta or skip)         | Replaced by the defect record, which carries facts rather than an analysis outcome.                              |
| The merge-time capture and its own docs pull request        | The record already ships with the unit; a merge appends any defect the merge alone reveals to the unit's record. |
| The capture-record net-delta figure in the review gate's §1 | It measured an inline capture's `AGENTS.md` delta; the generalizing unit records its own.                        |

### D9. The retitled requirement keeps the word "captured"

The corpus refers to "the capture requirement" and "the capture's promotion gate". Keeping "captured" in the new header
leaves those references resolvable, where removing it would cost a `MODIFIED` block each for a one-word change. The one
reference that does not survive — "the main agent's capture workflow" — is repointed by a `MODIFIED` block, since
"capture" now names the analysis's own unit rather than a per-unit step.

## Record format

Authoritative definition: `AGENTS.md`'s "Workflow: Record process defects" (implementation task 1.1). Its shape:

- one `### <unit>#<n> — <title>` heading per defect, numbered within the file;
- `Recorded` (date and author), `Stage` (proposal, implementation, review, ship, or merge);
- `Expected` and `Observed` — the action a rule or the plan called for, and what happened instead;
- `Evidence` — a finding excerpt, the material line of a command's output, or the event sequence; never a session
  identifier alone, and never a secret or a full diagnostic dump;
- `Consequence` — the damage or extra work, what is already repaired, and what is still broken;
- `Knowledge boundary` — what the author observed, what they assume, and what they did not check.

The generalizing unit appends one `Decision` line per record: `no-change` with its reason, `proposed` with a link, or
`shipped` with a merged pull-request link. A link to an open pull request is never recorded as `shipped`, and a record
without a `Decision` line is the definition of unanalyzed.

## Obligation trace

Every obligation of the requirement removed by this change, clause by clause, and where it goes. `Retained` means the
clause survives in the added requirement's "The generalizing unit keeps the analysis contract" scenario; `replaced`
means a new clause covers the same property under the new mechanism; `retired` means the obligation's subject no longer
exists.

| #    | Obligation (old requirement)                                                                                     | Destination                                                                                    |
| ---- | ---------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------- |
| D1   | Capture durable lessons into the owning artifact                                                                 | Retained — analysis contract                                                                   |
| D2   | After the implementation's quick review is clean                                                                 | Retired — the analysis no longer follows a unit's review                                       |
| D3   | Invoke unless the skip condition holds in full                                                                   | Retired with the skip conjunction (D8)                                                         |
| D4   | Record the applied capture, or the skip and its reason, on the unit's record                                     | Replaced — the defect record and its `Decision` line                                           |
| D5   | Owning destinations and routing are `AGENTS.md`'s workflow's                                                     | Retained                                                                                       |
| D6   | The capture stage's own consolidation is a later unit                                                            | Retained                                                                                       |
| 1.1  | Invoke after a clean quick review; a skip records its reason                                                     | Retired — recording is unconditional and has no skip                                           |
| 1.2  | Proposes each lesson as an addition to the owning artifact, or a pair                                            | Retained                                                                                       |
| 1.3  | Names the existing rule it extends, or states none covers it                                                     | Retained                                                                                       |
| 1.4  | Reports retirement or replacement candidates, separately from additions                                          | Retained — in the retirement-candidate scenario                                                |
| 1.5  | Promotion gate (six criteria), a failing rule routed elsewhere                                                   | Retained                                                                                       |
| 1.6  | Separates a repeated observation from its cause                                                                  | Retained                                                                                       |
| 1.7  | Caller supplies the existing guidance and the recurrence evidence; unavailable → withhold                        | Retained — the caller is the generalizing unit                                                 |
| 1.8  | Traces the lesson to its origin; an authoring mechanism gets the smallest correction there                       | Retained                                                                                       |
| 1.9  | Names the decision the rule governs, with plausibility and materiality                                           | Retained                                                                                       |
| 1.10 | Evidence threshold: two occurrences or one severe verified incident                                              | Retained                                                                                       |
| 1.11 | The claim's source is part of the gate; untrusted content is verified first                                      | Retained                                                                                       |
| 1.12 | Leaves `AGENTS.md` no larger; net growth is justified                                                            | Retained                                                                                       |
| 1.13 | Demonstrates the decision difference                                                                             | Retained                                                                                       |
| 1.14 | The comparison names the existing obligation, condition, and different action                                    | Retained                                                                                       |
| 1.15 | The difference carries a repository-backed example                                                               | Retained                                                                                       |
| 1.16 | An addition the existing guidance determines is `already covered`                                                | Retained                                                                                       |
| 1.17 | An incident, example, citation, or marker alone is `already covered`                                             | Retained                                                                                       |
| 1.18 | A partial restatement takes the split disposition                                                                | Retained                                                                                       |
| 1.19 | Prefers an exact replacement over appended qualifications                                                        | Retained                                                                                       |
| 1.20 | A replacement preserves obligations, warnings, exceptions, identifiers, scope, and strength                      | Retained                                                                                       |
| 1.21 | The action and its scope are derived from verified evidence                                                      | Retained                                                                                       |
| 1.22 | Incident evidence stays behind the `captured:` marker                                                            | Retained                                                                                       |
| 1.23 | The verdict states the net delta and the authoring-system count; the applying agent records the applied delta    | Retained — grounded in the records analyzed rather than in a unit's diff                       |
| 1.24 | A rationale normative in a spec becomes a pointer, not a copy                                                    | Retained                                                                                       |
| 1.25 | A mechanism the specs describe only as an outcome moves only if it changes a scenario's outcome                  | Retained                                                                                       |
| 1.26 | The control on growth is the periodic `/audit-agents` pass                                                       | Retained                                                                                       |
| 1.27 | Each addition carries `captured: <unit>`, and the PR when a merge-time detection found the lesson                | Retained, narrowed: the merge-time capture retires, so the marker names the record's unit only |
| 1.28 | The unit leaves a record the merge-time read can check                                                           | Replaced — the defect record and its `Decision` line are that record                           |
| 2.1  | A unit that obsoletes or makes a rule redundant yields a retirement or replacement candidate                     | Retained                                                                                       |
| 2.2  | The verdict names the retirement count alongside the durable-proposal count                                      | Retained                                                                                       |
| 3.1  | At a merge, an unrun capture is reported as unrun rather than reconstructed                                      | Retired — there is no merge-time capture to run or report                                      |
| 3.2  | A recorded skip is re-verified at the merge, and the capture runs when the condition no longer holds             | Retired with the skip conjunction                                                              |
| 4.1  | A lesson whose cause is an authoring mechanism is routed to that mechanism as an authoring-system recommendation | Retained                                                                                       |

## Site sweep

**One sweep task owns completeness; the pattern is only a finding aid.** Three review rounds were spent trying to make
one `git grep` prove that no site was missed, and each found a sense the pattern could not express. The mechanism is
single-sourced: task 4.2 sweeps every durable file the plan edits, proves the pattern on a known line of each file
before trusting a zero, and records each survivor's sense. No other artifact restates that rule.

The durable surfaces the behavior can live in, and therefore the files the plan must account for:

- `AGENTS.md` (its workflows, conventions, and gotchas);
- `README.md`;
- `.opencode/**` — the agent and skill definitions that act on the capture, and the commands that describe the review
  gate;
- `openspec/config.yaml` and `.github/PULL_REQUEST_TEMPLATE.md`;
- `openspec/specs/**` — the requirement and every requirement that refers to it;
- `docs/ideas.md` — its header and its live entries.

The sweep's pattern is a finding aid:

```bash
git grep -nE -i "(lesson-capture|capture record|capture workflow|capture decision|capture requirement|capture.s|capture loop|capture evidence|Capture lessons|skip condition|merge-time|per-unit review|the capture|a capture)" <base> -- <file>
```

Prove it before trusting a zero: it must return a known line from the file under sweep (`pause-resume-unit`'s Stage
field is the control that caught a `\b`-anchored pattern returning nothing). A survivor is allowed when its sense is one
of:

- **a live reading** — `capture` still names the analysis stage (`a capture proposal`, `the capture's verdict`), or
  `per-unit review gate` names the gate;
- **a historical citation** — a past unit's name, a filename (`lesson-capture.md`), or a quoted incident;
- **a dated record** — a retrospective, an audit, the round ledger, or a guidance-evidence record, which keep the
  wording they ran under;
- **generated or vendored** — a file the repository does not author;
- **a different sense of the word** — a participle ("captured specs", "a captured mock callback"), an OpenSpec behavior
  capture, or a diagnostic log capture, none of which names this stage.

A survivor that fits none of those is the task's failure, not a judgement call, and the task records the sense it
allowed.

## Claims table

Each row is a fact the planning artifacts state, with the command that derives it. All are run against the unit's base
revision `ea1ee3b5797e6987e4de8d8cd3e462d1749121ad` — a fixed commit, so they reproduce after the implementation lands
and after a later refresh moves `origin/main`.

| Claim                                                         | Source                                                                                                                                                                                                                                                  | Result                                                                                          |
| ------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------- |
| The capture's trigger names the manual-review ask             | `git show ea1ee3b5:AGENTS.md` piped through `sed -n '465,466p'`                                                                                                                                                                                         | the trigger sentence, `:465–466`                                                                |
| `review-gate` §5 requires the unit's capture record           | `git show ea1ee3b5:.opencode/skills/review-gate/SKILL.md` piped through `sed -n '155,161p'`                                                                                                                                                             | the clause, `:158`                                                                              |
| The gate's completion criteria repeat it                      | `git grep -n 'capture record mean' ea1ee3b5 -- .opencode/skills/review-gate/SKILL.md`                                                                                                                                                                   | `:270`                                                                                          |
| The capture section occupies 143 lines                        | `git grep -n '^### Workflow:' ea1ee3b5 -- AGENTS.md`                                                                                                                                                                                                    | capture at `:463`, next workflow at `:606`, so `:463–605`                                       |
| The skip condition has four clauses                           | `git grep -n 'The capture is owed unless' ea1ee3b5 -- AGENTS.md`                                                                                                                                                                                        | `:491`, its clauses at `:492–501`                                                               |
| The capture's key input is a note about what no diff captures | `git grep -n 'note from the main agent' ea1ee3b5 -- .opencode/agent/lesson-capture.md`                                                                                                                                                                  | `:26`                                                                                           |
| The ledger stores classes, not instances                      | `git grep -n 'instances live only in that report' ea1ee3b5 -- .opencode/skills/review-gate/SKILL.md`                                                                                                                                                    | `:186`                                                                                          |
| The removed requirement has four scenarios                    | `git grep -nE -e '^#### Scenario: Lessons are captured' -e '^#### Scenario: A change that obsoletes' -e '^#### Scenario: A skipped capture' -e '^#### Scenario: A lesson whose cause' ea1ee3b5 -- openspec/specs/showcase/quality/agent-skills/spec.md` | requirement at `:2133`, scenarios at `:2141`, `:2225`, `:2233`, `:2241`                         |
| The spec calls the analysis input session-only                | `git grep -n 'richest input' ea1ee3b5 -- openspec/specs/showcase/quality/agent-skills/spec.md`                                                                                                                                                          | `:1027`, stated of the experience-analyzer's input                                              |
| Another requirement states the per-unit model                 | `git grep -n 'the per-unit review and lesson-capture' ea1ee3b5 -- openspec/specs/showcase/quality/agent-skills/spec.md`                                                                                                                                 | the report contract at `:916`, and the Purpose at `:15`                                         |
| Every change's tasks must carry the capture decision          | `git grep -n 'per-unit .lesson-capture. decision' ea1ee3b5 -- openspec/config.yaml .github/PULL_REQUEST_TEMPLATE.md`                                                                                                                                    | `config.yaml:110`, `PULL_REQUEST_TEMPLATE.md:27`                                                |
| The README describes the inline capture in five places        | `git grep -n -i 'lesson-capture\|lesson capture' ea1ee3b5 -- README.md`                                                                                                                                                                                 | `:242`, `:389`, `:439`, `:460`, `:501`                                                          |
| Shipping names a capture as an in-flight docs edit            | `git grep -n 'a capture from its implementation' ea1ee3b5 -- .opencode/skills/ship-change/SKILL.md`                                                                                                                                                     | `:46`                                                                                           |
| No ADR records the inline placement                           | `git grep -c -i 'capture\|lesson' ea1ee3b5 -- docs/adr`                                                                                                                                                                                                 | four lines in two files, both about ADRs recording decisions generally                          |
| No parked idea asks to separate recording from generalization | `git grep -c -i 'separate\|parallel\|decoupl' ea1ee3b5 -- docs/ideas.md`                                                                                                                                                                                | 10 hits, none about capture timing; the idea that parked the gate gap was removed by `61bcdee2` |
| Review notification routes to one person                      | `git show ea1ee3b5:.github/CODEOWNERS`                                                                                                                                                                                                                  | one rule, `* @aanbrn`                                                                           |
| An inline capture's delta is measured from `AGENTS.md`        | `git grep -n 'the capture record.s net delta' ea1ee3b5 -- .opencode/skills/review-gate/SKILL.md`                                                                                                                                                        | `:37`                                                                                           |

## Non-goals

- **No schedule, queue, or drain guarantee.** The owner decides when generalization happens.
- **No role registry, access control, or automatic assignment.** Assignment is per unit, in the unit's own tasking.
- **No change to approval or merge authority.** The owner approves; a non-administrator still requests a reviewer.
- **No author/reviewer separation requirement** and no record locking.
- **No retention or pruning mechanism for analyzed records.** Revisit when the corpus's size starts to burden a reader
  or the audits; nothing ships for it now.
- **No change to the review gate's other stages**, the round ledger, the audits, or the retrospective.
- **No consolidation of the retitled requirement's scenarios.** The parked idea asking for it stays open; this change
  carries the scenarios it inherits rather than reshaping the requirement.
- **No new subagent**, and no change to `CODEOWNERS`.

## Risks

- **A lossy hand-off.** IF the generalizing unit has to return to the recording session for missing context, THEN the
  record's contract is the gap — fix the field set before considering an inline analysis again.
- **Records may accumulate unanalyzed.** Accepted: the moment is the owner's decision, and no mechanism promises
  otherwise.
- **The change reads as weakening capture enforcement.** The analysis contract is carried verbatim; the gate checks the
  hand-off, not the analysis, and the retirement table states each dropped clause's substitute.
- **A sibling unit edited the same gate, and has now merged.** `61bcdee2` added the capture-record precondition to
  `review-gate` §5 and the review-gate bullet in `AGENTS.md`, and removed the parked idea that named the gap. This unit
  refreshed onto that revision, and its plan replaces that clause at all three sites (`review-gate` §5, the skill's
  completion criteria, and `AGENTS.md`'s review-gate bullet) while keeping the gate's other preconditions, including the
  "base has not moved" check and `--expect-clean`.
