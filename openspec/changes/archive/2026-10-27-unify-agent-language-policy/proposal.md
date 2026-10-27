# Proposal

## Why

Only the `technical-advisor` carries an output-language rule; the main agent's prompts to different subagents vary in
language, so the agent stack answers in mixed languages. One audience-based rule replaces the per-agent guesswork, keeps
every durable artifact English, and leaves the owner conversing in their own language.

## What Changes

- `AGENTS.md`: add one convention under "Conventions" — an agent's output language follows the output's audience, not
  the language of the turn that triggered it; it covers the prompt or handoff an agent sends a subagent, which is the
  language inconsistency the owner observes.
- `openspec/changes/unify-agent-language-policy/specs/showcase/quality/agent-skills/spec.md`: remove the
  consultation-only language requirement and add one repo-wide requirement that references the `AGENTS.md` convention
  and tests it with scenarios.
- `.opencode/agent/technical-advisor.md`: the `**Language.**` section becomes a pointer that tells the advisor to read
  the convention, instead of its own copy.
- `.opencode/commands/consult.md`: step 7 presents the report in the owner's language, preserving its contract, rather
  than "as returned".
- `README.md`: "The Agentic Process" states the capability once — talk to the agents in your own language, the
  repository written in English — and points at `AGENTS.md`; the advisor-only framing leaves the consultation paragraph.
- `openspec/config.yaml`: the `context:` conventions gain the English-artifact line, so generated artifacts follow it.
- `docs/ideas.md`: park two follow-ups this unit surfaced — the definition-edit probe rule's missing marker-removal
  step, which its own smoke-run task follows, and the audit prompt edit the narrowed scope leaves out.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the agent-output language behavior, widened from the consultation report to every
  agent's output.

## Impact

Agent tooling and documentation only: no production code, API, dependency, build, test, or deployment surface changes.
`AGENTS.md` gains one convention; `agent-skills` loses one requirement and gains one; `docs/ideas.md` gains two parked
entries. The audit workflow's prompt and its requirement are deliberately untouched.
