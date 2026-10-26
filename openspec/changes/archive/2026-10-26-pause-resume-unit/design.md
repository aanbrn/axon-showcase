# Design

## Context

See `proposal.md` for the motivation. The constraints that shape the approach:

- A unit's recorded state already lives in three records — the unit's `tasks.md` (its workspace record, tasks, the
  pre-write record, the capture record), `docs/review-rounds.tsv` (round rows and the reviewer's session identifier),
  and the reviewer's own report (the finding instances) — while `AGENTS.md`'s change lifecycle owns the rules those
  records follow.
- What none of those records holds: the unit's next step, and the outcome of an operation that was still running when
  the machine stopped.
- The handovers that do exist serve a replacement reviewer and a review round (`AGENTS.md:59`,
  `.opencode/skills/review-gate/SKILL.md:93,175`, `.opencode/agent/review-quick.md:13`); `AGENTS.md:61` covers reloading
  subject packs after compaction. The generated apply command and skill stop the stage on a user interruption and print
  a status, holding no record to resume from.
- `checkpoint` is already taken: `showcase/quality/agent-skills` calls the review-gate's round record "the ledger row
  and its checkpoint". The new record therefore carries a qualified name, `Resume checkpoint`, in the durable artifacts
  — the spec delta, the skill, and the root entry point — while this design says "the checkpoint" for it.
- A session is a persisted object — `opencode v2.1.4`, `opencode debug paths` names `opencode.db`, `opencode session`
  offers `list`/`delete`/`export`/`import` — but a resume may start in a fresh session, so the procedure must not depend
  on the session surviving. `docs/ideas.md:267` records the complementary fact from an earlier feasibility pass: v1
  continues a session by id and fails on a missing one, while v2 silently creates a new one.
- A tree's write right is a **discipline, not a mechanism**: ADR-0024
  (`docs/adr/0024-tree-write-right-is-not-session-bound.md`, Accepted) records that no surface the running v2 binary
  loads binds a write to a session, and leaves the v1 leg "not established" rather than impossible, so nothing
  mechanically stops a second session writing and a matching working tree does not prove the prior writer stopped. The
  workspace record's `writer=<ses_…>` is therefore the only handle a resume has on ownership.
- The owner's account adds a second entry: a unit is often left **waiting for the operator** with no pause ever
  announced, so the resume must serve an unfinished unit whether or not a checkpoint exists.
- The live case for the shape: the primary checkout held another unit with a staged change dir and an `AM` `tasks.md` —
  exactly the state a pause leaves and a resume must re-enter.

## Goals / Non-Goals

**Goals:**

- Name the resume checkpoint's fields and home once, so no artifact restates them.
- Make a pause cheap enough to perform in the seconds before a lid closes.
- Make a resume verify the state it can see — the records and the live tree — instead of assuming it, whether or not a
  checkpoint exists.

**Non-Goals:**

- Durability against losing the device: the record is local, and a commit on the same disk is not a backup either.
- Stopping builds, containers, or daemons on pause.
- Replacing the review gate's session preflight or the round ledger.
- A new automated check (see D8).

## Decisions

### D1 — The procedure's only home is a new project skill

`.opencode/skills/pause-resume-unit/SKILL.md` carries the checkpoint's field set, the pause's ordered steps, the
resume's ordered steps, decision points, failure handling, and completion criteria. `AGENTS.md` gains one entry-point
bullet naming the skill, its path, when it loads, and the stop-and-report rule for a missing skill, and carries no
procedure step.

Alternatives considered:

- **An `AGENTS.md` workflow section** — rejected: the root is always loaded and its growth is bounded, while a pause
  procedure is needed rarely; the repository already moved the review-gate and shipping procedures out on exactly this
  ground.
- **Extending `review-gate`** — rejected: the gate's trigger is a finished proposal or implementation, while a pause can
  happen before the gate opens, during a round, or after the manual ask.
- **Extending `ship-change`** — rejected: that skill owns delivery, and a pause is explicitly not a delivery step.

### D2 — Two owner-facing triggers, not one

`.opencode/commands/pause-unit.md` and `.opencode/commands/resume-unit.md` each load one section of the skill and start
no unit. `/resume-unit` serves any unfinished unit, from the session that left it or from a new one, whether or not a
pause was announced.

Alternatives considered:

- **One `/pause-unit` covering both** — rejected: a single name for two opposite entry points, and the resume is the one
  a fresh session needs.
- **No command** — rejected: the owner's announcement ("I have to go") must resolve deterministically; inference from
  prose is the failure mode this change exists to remove, and the repository's rule that a documented capability needs a
  trigger applies.

Neither command starts a mutating unit, so the lifecycle-preflight requirement for mutating command definitions does not
fire; each instead states that it starts no new unit, while establishing the workspace stays `AGENTS.md`'s rule — a
continuation may take a tree on the unit's existing branch. `consult.md` is the precedent for that block.

### D3 — The checkpoint lives in the unit's own record, updated in place

One `Resume checkpoint` section in the unit's `tasks.md`; for a unit with no change dir, in its hand-off. It is created
or updated, never appended a second time.

Alternatives considered:

- **A separate file in the worktree** — rejected: a second copy of the unit's state that drifts from `tasks.md`, and the
  review pass reads the unit's own artifacts.
- **A commit** — rejected: see D4.
- **`docs/ideas.md`** — rejected: a prunable scratchpad, not a unit record.

### D4 — A pause changes no delivery state

- No commit, stash, push, archive, or merge is made for a pause.
- The staged set is recorded as it is, neither cleaned nor added to.
- The tree stays assigned to the unit.
- No container, build, or daemon is stopped for it.

Alternatives considered:

- **A checkpoint commit on pause** — rejected: it contradicts the approval stop and the pre-commit guard's stale-staged
  rule, and turns a pause into a delivery step. An owner's explicit commit request stays the only route.

### D5 — A resume verifies and stops, it does not repair

The resume has two branches, and neither is skipped. It verifies the state it can see — the workspace records and the
live tree, including branch, revision, and the working-tree change set — and, when the unit carries a checkpoint,
compares that state with it and stops on a mismatch. Without a checkpoint the unknowns stay unknown and are reported as
unrecovered; the checkpoint's absence is not a mismatch. A decision the unit is waiting on is restored, never treated as
given.

Alternatives considered:

- **Repairing the tree to match the record** — rejected: the repository's destructive-command gotchas are a catalogue of
  repair-by-default losing work.

### D6 — The boundary with the review gate is a pointer, not a copy

- Where a pause interrupts a review loop, the checkpoint carries the prior finding instances by pointing at the gate's
  handover rule, which already requires a context-losing caller to write them down.
- A pause starts no replacement reviewer session, since the gate's recovery cases do not include one.

### D7 — The checkpoint is written at stage transitions and on an announced pause

Not after every task.

Alternatives considered:

- **Continuous updates** — rejected: churn in a reviewed artifact with no added safety, because the tasks, the ledger
  rows, and the capture record already land at those points.

An unannounced interruption is covered by what the unit already wrote: the resume reads the artifacts, and states what
they do not hold. A missing checkpoint is an expected variant of that case, not evidence that the interruption went
wrong; a stale one is reported as stale.

### D8 — No new automated check

Whether a resume checkpoint is true is not decidable from the repository, and the only automatable property — a heading
exists — does not distinguish a written pause from a stale one. The change therefore adds no gate, and this decision is
recorded so its absence is not read as an omission.

### D9 — Four ADDED requirements, no MODIFIED block

`showcase/quality/agent-skills` gains requirements for the procedure's home, the checkpoint a pause records, the
verification a resume owes, and the writer handover (D10). No existing requirement is amended: the lifecycle-preflight
requirement covers commands that start a mutating unit, which these do not (D2).

### D10 — The resume distinguishes the recorded writer from a new one

The handover is an **act with one evidence**: the owner's confirmation that the prior writer has ended — carried by the
continuation request when it says so, asked for otherwise, while a bare resume request is not a confirmation. A session
whose identity differs from the record's `writer=<ses_…>` verifies the available state first (D5, read-only), then
obtains that confirmation, then records itself as the writer, and only then writes work in the tree. The handover is the
resume's first write; no work write precedes it, and it does not depend on a checkpoint existing. Ownership it cannot
establish — no writer in the record, or a required confirmation withheld — stops the resume for the owner's decision,
and a matching working tree is not evidence of ownership.

Alternatives considered:

- **Automatic takeover** — rejected: a concurrent writer's edit is lost silently, which is the failure this decision
  exists to prevent.
- **Trusting a clean or matching tree** — rejected: a tree can be identical while the prior session is still writing,
  and ADR-0024's Consequences record that a foreign write leaves a trace only until it is deleted.
- **A mechanical ownership check** — rejected: ADR-0024's `Revisit when:` gate is a **disjunction** — a mechanism that
  binds a write decision to a session (absent from the v2 surface the binary loads), or a probe that sees such a
  mechanism deny before the write (the v1 leg, unverified). Neither clause is met, so no check can be built yet.
- **Stopping every build or container to free the tree** — rejected: ownership is about the writer, and the pause's own
  rules already stop nothing.

## Risks / Trade-offs

- [A local record does not survive device loss] → the skill states the limit explicitly, so a paused unit is not read as
  a backup; durability needs a separate mechanism and is out of scope.
- [A stale checkpoint misleads the resume] → the resume verifies the live state and stops on disagreement (D5), and the
  checkpoint records its time and the revision it observed.
- [A resume with no checkpoint reads as a complete recovery] → the branch reports the context no record holds as
  unrecovered, and the absent record is distinguished from a stale one and from an unknown writer (D5, D7, D10).
- [A confirmation is read into a bare request] → the requirement states that only a request saying the prior writer has
  ended counts, and the skill's step asks otherwise.
- [The word `checkpoint` collides with the review gate's round checkpoint] → the qualified name `Resume checkpoint` and
  the collision recorded here, so a reviewer reads it as deliberate.
- [A wrapped command `description` can acquire an unquoted `: ` and silently drop the definition] → keep each
  description a single clause with no colon, run `spotlessApply` before the review round, and re-read the frontmatter it
  produced.
- [`tasks.md` accretes pause sections] → one section, updated in place (D3).
- [A new session writes while the previous one still holds the tree] → the handover gate stops it for the owner's
  decision (D10); ADR-0024 records that nothing enforces exclusivity, so the rule is the only control available.
- [Root growth] → one bullet pointing at the skill (D1).

## Claims table

Base revision: `git merge-base HEAD origin/main` → `02f839d9be8950ccd437a74e46175b5248fe116c` (the branch follows
`origin/main`; the unit has no commits of its own). The rows were first derived at `61bcdee2` and re-checked after the
fast-forward to `02f839d9`, which added ADR-0024 (now a cited source), the compound-gate and negative-claim rules this
design applies, a `docs/ideas.md` entry the parked-idea row re-greps, and eight ledger rows.

| Claim                                                                                                                                                   | Source or command that derives it                                                                                                                                                                                                                                                                                                                                                                                                         |
| ------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Three records hold a unit's recorded state, under rules `AGENTS.md`'s lifecycle owns                                                                    | `AGENTS.md`'s "Workflow: Change lifecycle" **Workspace** (the record's home) and **Completion criteria**, plus "Workflow: The review gate" completion criteria (the ledger) and `review-gate/SKILL.md` §2                                                                                                                                                                                                                                 |
| No requirement covers a pause or resume of a unit                                                                                                       | `grep -rn -i "resume\|pause\b\|checkpoint" openspec/specs/` → only the pack-reload clause at `agent-skills:1582`, the review gate's round checkpoint at `:1984,:2006`, and unrelated load-test and SSE uses                                                                                                                                                                                                                               |
| No parked idea covers a pause or resume procedure                                                                                                       | `grep -n -i "pause\|resume\|checkpoint\|interrupt" docs/ideas.md` → 7 hits, all the round checkpoint, the OpenSearch clone checkpoint, or the 2026-10-23 consolidation pause                                                                                                                                                                                                                                                              |
| The stated handovers serve a reviewer replacement and a review round                                                                                    | `grep -rn -iE 'hand-?over' AGENTS.md .opencode/` → `AGENTS.md:59,1400`, `review-gate/SKILL.md:93,95,138,144,175,176,181`, `review-quick.md:13,22`                                                                                                                                                                                                                                                                                         |
| `checkpoint` already names the review gate's round record                                                                                               | `grep -rn -i checkpoint openspec/specs/` → `agent-skills:1984,2006` ("the ledger row and its checkpoint")                                                                                                                                                                                                                                                                                                                                 |
| A session is persisted and listable, and v2 creates one silently when an id is missing                                                                  | `opencode --version` → `2.1.4`; `opencode debug paths` → `db …/opencode.db`; `opencode session --help` → `list`, `delete`, `export`, `import`; the v2 behaviour is `docs/ideas.md:267`, a recorded feasibility pass rather than a probe of this session                                                                                                                                                                                   |
| This change's pull request runs the definition probe                                                                                                    | `.github/workflows/ci.yml`, step "Probe the OpenCode config's V1 read path", `if: github.event_name == 'pull_request'`, gated on a `.opencode/` diff                                                                                                                                                                                                                                                                                      |
| The markdown formatter covers the new skill and command files                                                                                           | `build.gradle.kts:25-27` (`target(… ".opencode/commands/**/*.md", ".opencode/skills/**/*.md")`)                                                                                                                                                                                                                                                                                                                                           |
| A new skill directory needs no registry edit                                                                                                            | `ls .opencode/skills` (19 directories, project and vendored) and `build-logic/src/main/kotlin/UpstreamReferenceScope.kt`, whose prefixes cover generated and vendored trees only                                                                                                                                                                                                                                                          |
| The lifecycle-preflight requirement covers commands that start a mutating unit that ships                                                               | `openspec/specs/showcase/quality/agent-skills/spec.md:1324`                                                                                                                                                                                                                                                                                                                                                                               |
| A resume can know the recorded writer, while nothing mechanical enforces exclusivity; the handover gate stops a second session for the owner's decision | `docs/adr/0024-tree-write-right-is-not-session-bound.md` (`Status: Accepted`: its Decision, its disjunctive `Revisit when:` gate, and its Consequences) and `AGENTS.md`'s **Workspace** bullet, which carries `writer=<ses_…>`                                                                                                                                                                                                            |
| A resume does not lift an approval its continuation request does not answer                                                                             | `AGENTS.md`'s decision point "IF the user's reply does not address the approval request" (`:361`)                                                                                                                                                                                                                                                                                                                                         |
| No artifact records an unfinished unit's next step or an in-flight operation's outcome                                                                  | `grep -rn -iE 'next step\|in-?flight\|interrupt' AGENTS.md README.md docs .opencode openspec/specs --include='*.md'` → `next step` hits only a consultation's question (`AGENTS.md`, `README.md`, `consult.md`, `agent-skills`), `in-flight` only a branch's work, an in-flight batch metric, and a stalled-test snapshot, and `interrupt` only the generated apply stage's stop-and-status; none of the three is a record to resume from |
| The local binary cannot reproduce the definition probe                                                                                                  | `opencode --version` → `2.1.4`; `opencode debug --help` → subcommands `agents`, `config`, `paths`, `heap-dump`, with no `skill`; `opencode debug skill` prints that help; `opencode debug config` prints a source list; the CI probe calls `opencode debug skill` and reads a resolved `agent`/`command` map from `opencode debug config`, both v1 shapes                                                                                 |

Dismissed `claim-scan.py` candidates, one reason per kind:

- **`path`** — the change's own file citations: the sites `proposal.md`, `design.md`, and `tasks.md` name, and the
  anchors the rows above carry. Each is verified by the row or task that names it.
- **`count`** — task-number identifiers the scan reads as figures, plus the two figures the rows above already state
  with the command that produced them. No separate row is owed.
- **`absolute`** — a quantifier inside task or requirement prose rather than a claim about the repository, except in the
  rows above, where the row itself carries the evidence.
- **The ledger row** — the `docs/review-rounds.tsv` line this unit appended is a record, not a claim; the gate's
  `--unique-rounds` and `--session-continuity` checks are its verification.

## Implementation records

### Task 3.3 — the sweep's hits and dispositions

Swept for `pause-resume-unit`, `Resume checkpoint`, `pause-unit`, and `resume-unit` over `AGENTS.md`, `README.md`,
`openspec/specs/`, `.opencode/`, and `openspec/config.yaml`.

| Site                                                               | Disposition                                                                                    |
| ------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------- |
| `AGENTS.md`'s **Workspace** block                                  | The new entry-point bullet, and the only root site.                                            |
| `README.md`'s automation list and slash-command table              | The new bullet and the two rows.                                                               |
| `.opencode/skills/pause-resume-unit/SKILL.md` and the two commands | The procedure's home and its triggers.                                                         |
| `openspec/specs/`                                                  | No hit: the requirement arrives with the archive commit's delta sync.                          |
| `openspec/config.yaml`                                             | No hit: neither the `context` block nor the per-artifact `rules` enumerate commands or skills. |
| `docs/audits/2026-09-20.md`                                        | Left as recorded: a dated audit reports the command count it found then, not the current one.  |

## Open Questions

None that would change the specs, the approach, or the task breakdown.
