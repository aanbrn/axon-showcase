# Proposal

## Why

The audit holds an ADR's `Revisit when:` condition against the repository, but tests a parked `docs/ideas.md` entry only
for whether its work has landed — never whether the entry's own reopening condition has been met. A parked decision can
therefore age past the point its own text says to reopen it, and nothing reports it. The gap is already recorded as a
parked entry with its remedy (2026-10-27, "Read a parked idea's own `Revisit when:` condition"): a sibling condition
class, the same four-way classification, a `MODIFIED` delta, and the capture rule whose closing reason it falsifies.
This change implements that entry rather than inventing a second mechanism.

## What Changes

- **`.opencode/agent/architecture-auditor.md`** — the advisory premise-met class gains its third member: a parked entry
  whose own recorded reopening condition appears met, read in whatever form the entry states it (a `Revisit when:` label
  or plain prose), classified by the same four-way rule as an ADR condition, and reported as a recurrence that is due
  rather than an instruction to remove the entry. Its frontmatter `description` names the added shape.
- **`.opencode/commands/audit-architecture.md`** — the frontmatter `description`'s scope parenthetical names
  `docs/ideas.md`, which it omits while the auditor sweeps the file. The trigger itself is unchanged: the task list's §2
  names the remaining describers.
- **`AGENTS.md`** — the architecture-auditor bullet's premise-met summary, swept-surface clause, and classification
  sentence gain the parked-entry condition; the `docs/ideas.md` reach sentence in the docs-refresh bullet gains it; and
  the generalization's capture Decision point drops the now-false "no other reader tests a parked entry's condition".
- **`README.md`** — the architecture-auditor prose paragraph gains the added shape; the two tables are generic and the
  task list records why.
- **`showcase/quality/agent-skills`** — the "Unexplained design intent is surfaced for clarification" requirement states
  the third class member, widens its classification and parked-entry clauses, and gains three scenarios (condition met,
  condition not met, condition the repository cannot settle).
- **`docs/ideas.md`** — the entry this change implements is removed.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the intent-clarification requirement's premise-met class covers a parked entry's own
  recorded reopening condition beside its landed work and the ADR gate, reported as a due recurrence and never as a
  removal instruction.

## Impact

- A future audit reports a parked decision whose own condition has been met, instead of that condition being read only
  when an incident happens to meet it between runs.
- **Build / tests / services**: no code, build, dependency, or deployment change — a definition, a command, and prose.
- The verification is a **smoke-run** of the widened subagent with seeded controls: a parked entry whose condition is
  met must be reported as due, and entries whose condition is unmet, absent, or unsettleable must not. With the file
  watcher on and the auditor invoked as a project subagent in this session — the surface the reload gotcha probes;
  `opencode run` is unverified — V2 serves the edited definition mid-session. The **positive control doubles as the
  registered-body confirmation**, since a `revisit due` item for a prose-stated condition cannot come from the
  pre-change definition; if it yields nothing, the body may be stale and a restart is the recovery. The run happens
  before the archive.
- The capability `## Purpose` names the class this change widens; a delta cannot carry a Purpose, so the refresh lands
  in the archive commit and the task list records it.
