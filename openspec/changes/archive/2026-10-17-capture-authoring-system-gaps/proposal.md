# Proposal

## Why

The `lesson-capture` subagent decides _whether_ a lesson becomes an `AGENTS.md` entry, but not whether an entry is the
right home. Its promotion gate routes a **failed-gate rule** to a check, a spec, an ADR, or the change dir; a lesson
whose cause is the mechanism that **produces** an artifact — an OpenSpec template or schema, a `config.yaml` rule, a
skill that shapes drafting — is fixed at that mechanism, not with another bullet the agent reads on every invocation.
The repo already routes fixes this way by hand: `docs/ideas.md` parks two items in the `lesson-capture` definition
rather than in `AGENTS.md`, and `establish-structured-writing-standard` fixed the drafting mechanisms (the config rules,
the review checks, a spec requirement) rather than only prose. The capture itself has no step that diagnoses the origin
before proposing. Stating it makes the choice explicit, so the always-loaded file does not absorb a fix its generator
should carry.

## What Changes

- **`showcase/quality/agent-skills`** — the capture's "Lessons are captured after implementation" scenario gains a
  clause and a scenario: before proposing an entry, the capture traces the lesson to its origin, and where the cause is
  a mechanism that produces the artifact it proposes the smallest correction there and reports it as an authoring-system
  recommendation, separate from its additions and retirements; the verdict names that count; and the quick-review
  scenario's scan clause is widened to mirror the capture's applied `AGENTS.md` merges (a row's source states a reason
  by class; the table is re-derived after the unit's last record append).
- **`.opencode/agent/lesson-capture.md`** — the same obligation in the definition's own words, plus the report-contract
  group that carries it and the frontmatter `description`.
- **`AGENTS.md`** — the "Capture lessons" workflow's step 1 names the third output kind, so the main agent knows to
  route it rather than apply it as an entry; the review gate's step-2 claims-table clauses are widened with this unit's
  own capture (a row's source states a reason by class; the table is re-derived after the unit's last record append).
- **`README.md`** — the agent table row, the "Lesson capture" bullet, and the Self-Learning Loop prose name the routing.
- **`openspec/config.yaml`** — the capture task rule names the routing, and the `design` rule's claims-table clause is
  synced with the widened `AGENTS.md` clauses.
- **`docs/ideas.md`** — the capture's authoring-system recommendation (derive a proposal's touched sites from the
  change's tasks) is parked here, its fix belonging in the `proposal` rules.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: refines "Per-change quality-gate and analysis subagents are available" — the capture
  diagnoses a lesson's origin and reports an authoring-system recommendation for a fix that belongs in a mechanism
  producing the artifact, rather than proposing an `AGENTS.md` entry.

## Impact

- A future capture routes a mechanism-caused lesson to that mechanism instead of adding prose to `AGENTS.md`; nothing
  about the product, its build, or its deployment changes.
- **Build / tests / services**: no code, build, dependency, or deployment change — one agent definition, two `AGENTS.md`
  edits, `README.md`, two `openspec/config.yaml` rules, a parked idea, and the spec delta.
- **Verification**: `openspec validate --all` and `spotlessApply`/`spotlessCheck` for the artifacts; a definition's
  behavior is only exercised by a run, which needs the edited definition picked up (a reload or a new session).
