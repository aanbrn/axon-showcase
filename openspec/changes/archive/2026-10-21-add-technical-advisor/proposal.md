# Add an independent technical advisor

## Why

The owner can obtain an independent second opinion today only by moving context by hand to an external advisor. The
repository's own reviewers are scoped by their artifacts: `review-quick` screens what it is given, `review-thorough`
reviews an implementation, and `architecture-auditor` audits recorded decisions — none answers "given this objective, is
this the right direction, and what should happen next?" while staying independent of the work. An advisor that reads the
relevant evidence itself, verifies the claims that change the decision, and recommends one action would return that
judgement without making the owner re-explain the work or making the reviewers approve their own subject.

## What Changes

- `.opencode/agent/technical-advisor.md` (new) — an on-demand, read-only subagent pinned to `openai/gpt-6.1-sol#high`
  that answers a concrete decision question: it checks the premise against repository evidence, compares the result with
  the objective, verifies the claims that change the decision, names unnecessary scope or duplicated mechanisms,
  separates confirmed problems from hypotheses and open questions, and recommends one next action (retention or stopping
  included). It inspects the working tree with its read tools, receives diff hunks and revision or history excerpts in
  the focused handoff, and runs no shell commands.
- `.opencode/commands/consult.md` (new) — the `/consult` command: `/consult <question>` evaluates that question;
  `/consult` with no argument evaluates the current objective, progress, and next step; an ambiguous target produces one
  focused clarification; a fresh decision uses a fresh child session and a follow-up may continue the saved session; the
  handoff may carry selected relevant excerpts and prior findings with their provenance, but not the full conversation
  or an undifferentiated tool-output dump; the invoking agent presents the report without turning a recommendation into
  authorization.
- `openspec/changes/add-technical-advisor/specs/showcase/quality/agent-skills/spec.md` — thirteen `ADDED` requirements
  for the advisor, its bounded read-only contract, its focused handoff and report, and its model/effort/access pin; plus
  a `MODIFIED` block adding the advisor to the shared report contract's bearer list.
- `AGENTS.md` — the minimal routing instruction an agent needs before consulting the advisor (when to use it, that it
  recommends rather than gates, and that the definition owns the contract); the model-pin bullet names the advisor's
  direct-OpenAI pin as a deliberate exception to the OpenCode Go line.
- `README.md` — a row in the agent table, a row in the slash-command table, and one short paragraph covering the
  advisory role, a practical `/consult` example, the focused child-session context, the default effort with a link to
  the override mechanism, owner-facing language behavior, and a short OpenAI sign-in comparison (ChatGPT OAuth vs API
  key) with a connection instruction, an included-usage and conditional-credits note, and the primary-doc links; the
  "pins its agents to `opencode-go/*`" sentence is corrected. The provider setup lives in README only.
- `docs/ideas.md` — the parked vendored-skill model-pin note, whose "every project-authored model pin is
  `opencode-go/deepseek-*`" premise this change makes wrong again, is corrected to name the deliberate exceptions.

## Impact

- No application code, API, dependency, build, test, or deployment change.
- Two new `.opencode/` markdown definitions; the `showcase/quality/agent-skills` capability gains requirements and needs
  its `## Purpose` refreshed in the archive commit (a delta cannot carry a Purpose).
- The advisor is invoked only on demand: no automatic consultation is added and the review gate's routing is unchanged.
- The README documents the two OpenAI sign-in routes, included plan usage, conditional additional credits, and
  separately billed API-key usage from the primary docs. The owner reports ChatGPT Plus, automatic credit replenishment,
  and connected-app credit use; the stored credential's method and per-request accounting are not inspected. The exact
  pin is unchanged. Implementation uses two short smoke invocations.
- The installed V2 enforces the advisor's tool-policy restrictions — no shell, edit, MCP, web, or Code Mode access; a V1
  consumer loads the definition but treats the `permissions` key as an unknown agent option, so the advisor is a
  V2-local agent (recorded as a limitation).

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills` — thirteen `ADDED` requirements for independent technical consultation, its bounded
  read-only contract, its focused handoff and report, and its model/effort/access pin; and a `MODIFIED` block adding the
  advisor to the shared report contract's bearer list.
