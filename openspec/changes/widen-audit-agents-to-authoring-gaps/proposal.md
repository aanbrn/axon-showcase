# Proposal

## Why

The `/audit-agents` audit reconciles the project-owned agent tooling: it reports drift between artifacts, rules a
mechanism already enforces, and — for a passage that violates the Structured Writing Standard — a location and a
rewrite. Two things are missing. Its structured-writing check yields a location and a rewrite with no practical impact
and no minimality guard, so a finding reads as a style preference rather than a defect a reader can weigh. And it never
asks the other half of the question: whether the mechanism that **produces** an artifact adequately communicates the
standard — so a defect a skill keeps causing is fixed document by document instead of at its source. The capture now
_routes_ a mechanism-caused lesson (`capture-authoring-system-gaps`); the audit should _find_ the gap. Widening the
audit closes both halves without duplicating the capture's routing.

## What Changes

- **`showcase/quality/agent-skills`** — the audit requirement gains an **authoring-system gap** class (a body paragraph
  and three scenarios); the structured-writing requirement's "Dense prose is restructured on review" scenario gains the
  finding's practical impact and a minimally invasive rewrite.
- **`.opencode/agent/agents-auditor.md`** — the consistency axis names the structured-writing dimensions and the
  one-authoritative-definition judgment; a new authoring-system gap class; the method's mechanism-investigation step;
  the verdict line, the report budget, and the frontmatter `description`.
- **`.opencode/commands/audit-agents.md`** — its `description`, its step-1 read-list, and its step-2 report list.
- **`AGENTS.md`** — the `agents-auditor` bullet names the new class; and this unit's own capture merged two clauses into
  the "recurring unit type" bullet (a definition's own sections are separate edit passes; and the set's member grep
  points at the "the only X" bullet) and retired a verified-false completeness count from the "the only X" bullet.
- **`README.md`** — the audit prose, the agent table row, and the slash-command table row.
- **`.opencode/commands/apply-audit-reductions.md`** and **`docs/ideas.md`** — the two further live enumerations of the
  audit's report classes (the reduction unit's out-of-scope classes, and the trend entry) name the new class.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the audit requirement gains the authoring-system gap class, and the
  structured-writing requirement's review scenario gains the finding's impact and minimality.

## Impact

- A future audit reports a recurring structural defect at the mechanism that causes it, and a structured-writing finding
  carries the impact and a minimal rewrite; nothing about the product, its build, or its deployment changes.
- **Build / tests / services**: no code, build, dependency, or deployment change — one agent definition, two commands,
  `AGENTS.md`, `README.md`, `docs/ideas.md`, and the spec delta.
- **Verification**: `openspec validate --all` and `spotlessApply`/`spotlessCheck` for the artifacts; a definition's
  behavior is only exercised by a run, which needs the edited definition picked up (a reload or a new session).
