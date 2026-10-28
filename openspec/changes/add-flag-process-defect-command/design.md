# Design

## Context

See `proposal.md`. The change adds one mechanism and refines one requirement.

Today the recording half of the process-defect loop starts at the agent: `AGENTS.md`'s "Workflow: Record process
defects" triggers when "you observe a process defect while working", and the record is a file per unit under
`docs/process-defects/` with a field set that workflow owns. The other halves already have owners: `/ideas` captures a
note unverified, and `/generalize-process-defects` selects the records that carry no `Decision` line and hands them to
`lesson-capture`. What no artifact owns is the step between an owner's observation and a record — the verification that
decides whether there is a defect to record at all.

An independent consultation recommended the direction: an owner-facing entry point that verifies a signalled episode and
reports a proven outcome, rather than a command that records the owner's diagnosis. The owner adopted that
recommendation; the report is a conversation artifact, so this design rests on it as the owner's decision, and on the
repository for every claim about the current state.

Premise checks, at the unit's base revision `eefe254372637d2f45c7909bbc1e789dd0102266`:

- no parked idea proposes an owner-flag mechanism;
- no spec sentence describes an owner-reported defect;
- the record corpus is one file per unit with no index, so a record's location is decided by the unit it belongs to;
- a new command file needs no registration beyond the markdown target that already globs `.opencode/commands/**/*.md`.

## Goals / Non-Goals

**Goals**

- Give the owner one low-friction way to signal a suspected process defect.
- Make the agent establish the defect from the episode's evidence before any record exists.
- Reuse the existing record format, so a confirmed defect enters the existing generalization queue.
- Keep the procedure in the command definition and `AGENTS.md`'s growth to a trigger pointer.

**Non-Goals**

- No new subagent, no new review gate, and no automatic generalization.
- No change to the record's field set.
- No cause diagnosis or improvement proposal in the command — those stay the generalization's work.
- No repository-wide search for violations: the pass is bounded to the signalled episode.

## Decisions

### A command, not a convention and not a variant of an existing command

Alternatives considered:

- **A convention only** (the owner says it in prose): rejected because it defines no outcome, so the agent's default
  under an owner's assertion is to agree and transcribe.
- **Routing through `/ideas`**: rejected because that file is a scratchpad of unverified notes, and a defect parked
  there loses both the verification step and the unit-keyed record.
- **Folding it into `/generalize-process-defects`**: rejected because that command consumes records that already exist;
  making it also produce them couples verification to an analysis pass the owner schedules separately.

### Three outcomes, with "not establishable" as a first-class result

Confirmed, refuted by the available evidence, and not establishable from it. The alternative — two outcomes — forces the
agent to convert a missing fragment into agreement or refusal. The third outcome is what keeps the owner's framing
("find the defect") from biasing the result toward confirmation. What it separates is the owner's assertion from
evidence, not conversation from evidence: a single claim by the owner establishes nothing, while an episode's available
messages, tool results, and sequence of actions can establish it, as the recording workflow's `Evidence` field already
allows.

### The investigation is bounded to the episode

The command resolves the episode — the one the owner names, or the nearest action in the current context — and compares
the required action against what actually happened within that episode and its immediate sources. When the pointer does
not resolve one episode, it asks one clarifying question and stops rather than choosing the most plausible candidate.
For a past episode, the rule it compares against is the one in force at the time, read from that revision where the text
has changed since. The alternative, a repository-wide search until a violated rule turns up, manufactures findings and
contradicts the invariant that a finding is a claim to verify rather than an instruction to apply.

### The confirmed record reuses the existing field set

No new record shape and no marker field. `Recorded` keeps its stated meaning — the date and who recorded it. The
distinction lives in the other fields: `Observed` states the action established rather than the owner's interpretation,
`Evidence` names the owner's report as the signal beside the verified fragment, and `Knowledge boundary` separates
independently verified material from owner-reported material and unavailable evidence. The fragment is of whatever kind
the recording workflow's `Evidence` field allows — a report's excerpt, a command's output, or the episode's own event
sequence — so it need not be re-runnable, and a past episode's evidence usually is not.

### The record carries no `Decision` line

`Decision` is the generalization's record that a record was analyzed. Using it to mark verification would make a
flagged-but-unanalyzed record read as decided, and the next `/generalize-process-defects` would skip it.

### The record lands with the unit the episode belongs to

A live unit's file, an archived unit's file, or a slug naming the work when the unit has no change dir — the
file-per-unit contract the recording workflow already states. The alternative, always appending to the current session's
unit, misfiles an episode that predates the unit or belongs to one already merged; a fresh slug per flag fragments one
unit's defects across files. An episode the file already records is extended rather than duplicated.

### `AGENTS.md` gains a trigger pointer, not the procedure

The always-loaded file names the owner-reported path in the recording workflow's trigger and points at the command. The
steps stay in the definition and the outcomes in the spec; a copy in `AGENTS.md` would be the drift surface, since the
file loads on every invocation while a command loads only when it is invoked.

### A `MODIFIED` delta on the existing process-defect requirement

The behaviour refines the requirement that already owns recording, so the delta adds one scenario to it. The
alternative, a new capability for the signal, buys a permanent corpus entry for a refinement and a second place
describing records.

### The command states the repository lifecycle

An append made while no unit's branch can carry it is a documentation unit of its own that ships, so the command carries
the preflight the `agent-skills` requirement binds to a project-authored command that starts a mutating unit.

## Risks / Trade-offs

| Risk                                                                                                                                                                | Mitigation                                                                                                                                                                                       |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| The command becomes a confirmation machine, producing the record the owner implied                                                                                  | The third outcome is stated as a valid result, the pass is bounded to the episode, and the seed run exercises a false signal, an unestablishable one, and an ambiguous request beside a true one |
| `AGENTS.md` grows for a mechanism most sessions never invoke                                                                                                        | One clause and a pointer in the existing trigger; the applied net delta is recorded on the unit's tasks                                                                                          |
| An episode only the owner's assertion attests — no tool output, no report excerpt, no observable sequence — yields no record, so the observation leaves no artifact | The unestablished outcome names what would establish the defect, so the owner can supply the fragment; writing the claim as a fact is the defect this change removes                             |
| A new definition fails to load because of its frontmatter                                                                                                           | The pull request's `build` probe asserts that every `.opencode/commands/*.md` file resolves with its description; the seed runs prove the procedure, not the dispatch                            |
| A record appended to a merged unit's file lands without a branch to ride                                                                                            | The lifecycle section states that such an append is a documentation unit of its own, never a commit on `main`                                                                                    |

## Claims table

Rows about the state before the change anchor to the base revision `eefe254372637d2f45c7909bbc1e789dd0102266`.

| Claim                                                                  | Source, re-run                                                                                                                                                                                                                                                                   |
| ---------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The recording workflow triggers on the session's own observation       | `git show eefe2543:AGENTS.md` — line 466, "**Trigger** — you observe a process defect while working"                                                                                                                                                                             |
| The record's field set and its no-diagnosis rule belong to `AGENTS.md` | `git show eefe2543:AGENTS.md` — line 481, "3. Diagnose nothing, propose nothing, and estimate no cost"                                                                                                                                                                           |
| `Evidence` admits an excerpt, a command's output, or an event sequence | `git show eefe2543:AGENTS.md` — lines 477–478, "`Evidence` — a finding's excerpt, the material line of a command's output, or the event sequence"                                                                                                                                |
| Generalization consumes records that already exist                     | `git show eefe2543:.opencode/commands/generalize-process-defects.md` — line 14, "records that carry no `Decision` line"                                                                                                                                                          |
| `/ideas` records a note without verifying it                           | `git show eefe2543:.opencode/commands/ideas.md` — line 11, "append it as a dated bullet"                                                                                                                                                                                         |
| No parked idea proposes an owner-flag mechanism                        | `git grep -n -E "flag-process\|owner-reported defect\|owner flags" eefe2543 -- docs/ideas.md` — no match                                                                                                                                                                         |
| No spec sentence describes an owner-reported defect                    | `git grep -n -E "owner report\|owner-reported\|flag-process-defect" eefe2543 -- openspec/specs` — no match                                                                                                                                                                       |
| The record corpus is one file per unit with no index                   | `git ls-tree -r --name-only eefe2543 -- docs/process-defects` — one file per unit, none an index                                                                                                                                                                                 |
| The owning requirement is the process-defect one                       | `git show eefe2543:openspec/specs/showcase/quality/agent-skills/spec.md` — `### Requirement: Process defects are captured during a unit and generalized separately`                                                                                                              |
| A new command file needs no registration list                          | `git show eefe2543:build.gradle.kts` — the markdown target globs `.opencode/commands/**/*.md`; its exclusions are the generated `opsx-*` instruction files, the vendored and generated skill trees, and `openspec/changes/archive/**`, so a project-authored command is in scope |
| A project-authored command that ships owes the lifecycle preflight     | `git show eefe2543:openspec/specs/showcase/quality/agent-skills/spec.md` — `### Requirement: Project-authored command definitions that start a mutating unit carry the lifecycle preflight`                                                                                      |
| The build already probes every definition's registration               | `git show eefe2543:.github/workflows/ci.yml` — the probe loops over `.opencode/commands/*.md` and fails when a definition is absent from the resolved inventory or resolves without its description                                                                              |
| The README already describes the recording loop in two places          | `git show eefe2543:README.md` — the Self-Learning Loop bullet "Every unit records the process defects it observes", and the "Recording, then generalizing" bullet under _What the Agent Automates_                                                                               |

### Dismissals

The scan's candidates resolve by kind, not one row at a time:

- **`path`** — the candidates are references rather than claims about a path's content: the claims table's own source
  commands, the file paths the change's artifacts name, and the tasks file's workspace record.
- **`absolute`** — the candidates are the change's own shipped rules and prohibitions — the command's steps and
  lifecycle, the README clauses, the `AGENTS.md` trigger clause, the record's boundary — together with the design's
  stated decisions, whose sentences name the instances they cover, and the preserved scenario text the delta carries.
  The absence claims are carried by their own rows: the two `git grep` rows for the parked-idea and spec searches, and
  the sole-consumer claim about the new file by the rows for `build.gradle.kts` and `ci.yml`, the two sites that read
  `.opencode/commands/*.md` at the base revision.
- **`cross-ref`** — the pattern matches any `a/b` or `x#n` token, so its candidates are corpus text the delta quotes
  from the preserved scenarios, record and task identifiers, and file paths the artifacts name directly. None is a
  reference to a document the change left unread.
- **`count`** — the candidates are numeric tokens that identify or record something — a task's number, a ledger column,
  a quoted line — none of which is a count of a repository set.
