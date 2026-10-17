# Proposal

## Why

`AGENTS.md` is loaded on every agent invocation and governs every workflow, yet the standard for what belongs in it —
which representation an entry takes, the shape a procedure or gotcha must have, what evidence a claim needs, how an edit
is validated — is only implicit, spread across the capture promotion gate and the auditor's conciseness classes. The
recent shapings of the file (#610–#612) established the operational style but recorded no standard, so each capture
re-derives the discipline and drift is caught only after the fact. Stating the standard once, and binding the two
`AGENTS.md`-specific subagents and the capture workflow to it, makes the authoring discipline explicit and auditable.

## What Changes

- **`showcase/quality/agent-skills`** — a new requirement states the knowledge-authoring standard `AGENTS.md` entries
  follow: the representation an entry takes, the procedures and gotchas it may state and their required shape, the
  evidence its claims need, how a lesson is integrated into a workflow, and how an edit is validated. It names the
  `lesson-capture` subagent (and the main agent's capture) as applying it when authoring and the `agents-auditor`
  subagent when auditing.
- **`.opencode/agent/lesson-capture.md`** — its proposal shape follows the standard (representation, shape, evidence,
  validation), in addition to the promotion gate it already carries.
- **`.opencode/agent/agents-auditor.md`** — its audit checks each `AGENTS.md` entry against the standard and reports a
  non-conforming entry as a finding.
- **`.opencode/commands/audit-agents.md`** — the audit's step-1 description names the standard check.
- **`AGENTS.md`** — the "Capture lessons" workflow's "How a captured rule is recorded" block points at the standard, so
  the main agent applies it when it edits the file directly.
- **`README.md`** — the `/audit-agents` prose enumerates the consistency sub-checks the audit reports, so it names the
  standard-conformance check the change adds.

And, at archive, the `agent-skills` `## Purpose` gains the authoring standard it now holds (a delta cannot carry a
Purpose).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: adds the "AGENTS.md entries follow a stated authoring standard" requirement — the
  authoring standard and the two subagents plus the capture workflow that apply it.

## Impact

- A future capture (or a direct `AGENTS.md` edit) is authored to a stated standard instead of re-deriving the shape, and
  the audit reconciles entries against it; nothing about the product, its build, or its deployment changes.
- **Build / tests / services**: no code, build, dependency, or deployment change — two agent definitions, one command,
  `AGENTS.md` and `README.md` prose, and the spec's `## Purpose` at archive.
- **Verification**: `openspec validate --all` and `spotlessApply`/`spotlessCheck` for the artifacts; a definition's
  behavior is only exercised by a run, which needs the edited definition picked up (a reload or a new session).
