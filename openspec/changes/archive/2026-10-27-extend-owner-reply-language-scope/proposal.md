# Proposal

## Why

#707 unified the output-language rule, but sessions created after it, on the pinned model, still mix languages in
replies to a Russian-speaking owner. Those outputs conflict with the policy's audience rule: the agent writes its
progress narration around tool calls — and a presented report's headings and count descriptions — in English, while the
prose around them is Russian. The convention's destination list names neither case, so this change extends it to cover
both; why the observed outputs did not follow the audience rule is not established here.

## What Changes

- `AGENTS.md`: widen the owner-reply sub-bullet from a reply to every assistant message the owner can see in the turn,
  the progress narration around tool calls included.
- `AGENTS.md`: extend the relayed-report sub-bullet so a presented report's prose — its headings, labels, and count
  descriptions — is translated with the rest, while the report's structure, evidence, and uncertainty are preserved.
- `openspec/changes/extend-owner-reply-language-scope/specs/showcase/quality/agent-skills/spec.md`: `MODIFIED`
  requirement "Agent output language follows the output's audience" gains two scenarios — progress narration, and a
  presented report's translated prose — and retains its existing scenarios in order.
- `docs/process-defects/extend-owner-reply-language-scope.md`: the unit's process-defect record.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the output-language requirement gains the owner-visible narration case and the
  presented-report prose case; its body keeps pointing at the `AGENTS.md` convention.

## Impact

Agent guidance and the `agent-skills` spec only. No production code, API, dependency, build, test, or deployment surface
changes, and no gate is added: the recorded decision that no in-repo check can observe reply language stands.
