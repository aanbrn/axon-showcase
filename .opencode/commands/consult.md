---
description:
  Ask the technical advisor for an independent recommendation on a proposal, implementation, disagreement, or next step
---

Ask the `technical-advisor` subagent for an independent recommendation. It recommends; it does not implement, approve
delivery, or gate a change. Present its report without turning a recommendation into authorization.

**Trigger** — the owner invokes `/consult <question>`, or `/consult` with no argument to evaluate the current objective,
progress, and next step. A calling agent may also delegate directly to `technical-advisor` with the same handoff; this
command is the owner-facing entry point, not the only path.

**Prerequisite** — OpenCode V2. The advisor's `permissions` ruleset and `provider/model#variant` effort selection are V2
features; V1 does not enforce or interpret them, and this command adds no V1 fallback.

**Procedure**

1. Resolve the decision.
   - With an argument, use it as the question.
   - Without one, state the current objective, the progress against it, and the next step being considered as the
     question.
2. IF the target or question is ambiguous, THEN ask one focused clarification and stop; do not delegate a guess.
3. Build the focused handoff with these fields:
   - the owner's objective and material constraints;
   - the question and the proposed next action;
   - the change identity and stage, when one applies;
   - artifact references, the diff base, and the relevant diff hunks with their changed paths and revision context;
   - any pre-change or history excerpts the advisor needs, because it has no shell;
   - relevant findings, their dispositions, and unresolved disagreements;
   - the subject packs applicable to the decision;
   - whether the report is owner-facing or agent-facing;
   - the requested language;
   - any explicit reasoning-effort override;
   - whether this is a fresh consultation or a follow-up.

   Do not forward the full conversation or an undifferentiated tool-output dump. Selected relevant excerpts, source
   references, and prior findings with their provenance and disposition are encouraged; name the source of each.

4. Choose the session.
   - A new decision uses a fresh child session: invoke `technical-advisor` without a `sessionID`.
   - A follow-up about the same decision MAY continue the advisor session by passing the `sessionID` the first
     invocation returned.
   - IF the identifier is unavailable or does not belong to this decision, THEN start a fresh consultation and say that
     it is fresh.
5. Choose the effort.
   - The advisor's configured default is `high`; invoke it with no model override.
   - Pass `model: openai/gpt-6.1-sol#xhigh` on the invocation only when the owner explicitly asks for extra-high effort
     in this request.
   - Never raise the effort automatically, and never substitute another model or effort. If the configured choice is
     unavailable, report the limitation and ask the owner which alternative to use.
6. Invoke the `technical-advisor` subagent with the handoff. Retain the session identifier it returns for this decision.
7. Present the report as returned, recommendation first. Do not re-summarize it, do not present its recommendation as
   approval, and do not apply its suggested action without the owner's decision.

**Direct delegation.** An agent that delegates directly supplies the same handoff, applies the same session rules,
passes the effort override only on the owner's explicit request, and presents the report under the same rule: a
recommendation is not authorization.

**Decision points**

- IF the advisor reports that the available evidence cannot settle the question, THEN present that limitation with the
  recommendation rather than asking for another pass.
- IF the invocation fails, or its output cannot be read, THEN report the failure rather than constructing a
  recommendation.
- IF the owner asks for an independent second opinion on the same decision, THEN start a fresh child session instead of
  continuing the existing one, and say that it is independent.
- IF a resource-intensive experiment would resolve the question, THEN the advisor proposes it; run it only after the
  owner authorizes it, in the calling agent's own session, not through the advisor.
- IF the advisor needs a diff or history excerpt the handoff does not carry, THEN obtain it with a read-only command in
  your own session and supply the excerpt; do not ask the advisor to run it.

**Verification** — the report starts with its recommendation, states its evidence, keeps uncertainty visible, and names
the subject packs it loaded.

**Repository lifecycle** — this command starts no mutating unit: it edits nothing, changes no branch, and publishes
nothing. The advisor's `edit`, `shell`, MCP, web, and delegation tools are denied by its own permissions.
