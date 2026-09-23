# Design

## Context

See `proposal.md` for motivation. The check-evidence rule lives in `AGENTS.md` (always loaded, read on recall). The
OpenSpec CLI injects a project's per-artifact `rules` into the instructions it returns at `openspec new change` /
`openspec instructions` time, so a rule placed there is surfaced while a change's `tasks.md` is authored. The config has
a read-path defect class: an unquoted scalar containing `: ` parses as a mapping, the rule list stops being an array of
strings, and the CLI ignores that artifact's rules with only a stderr warning — so a rule must be written to parse as a
string and its consumption confirmed.

## Goals / Non-Goals

**Goals:**

- Surface the positive-control obligation at planning time, where a check-adding change authors its tasks, rather than
  only on recall.
- Keep the rule a single parseable string, and confirm the CLI consumes it.

**Non-Goals:**

- Replacing the `AGENTS.md` check-evidence bullet: the config rule is injected only when a change's tasks are authored,
  while the bullet is always loaded, so both surfaces are needed.
- A CI or lint check: no check can decide whether an agent actually exercised a positive control.

## Decisions

### Decision: Deliver the rule as a single `openspec/config.yaml` `tasks` rule

Both halves of the obligation join the `tasks` rule set: a change that adds or changes a check must plan the
positive-control task, and plan to read the output of any scratch command used as evidence. The planned task carries
into apply, so the obligation fires at the moment of action through the task list.

- **Alternative — more `AGENTS.md` prose:** the retrospective found this class did not fire on recall; another bullet
  would be read the same way.
- **Alternative — put the scratch-output half in `operations.apply.guidance`:** it would fire at apply time, but the CI
  configuration probe guards only the `rules` surface — a malformed guidance item is silently dropped and no current
  check reports it (the probe's pattern matches `ignoring this artifact's rules`, not the guidance warning) — whereas a
  malformed `tasks` rule is caught, so the guarded surface is the one to use.
- **Alternative — a spec requirement:** no capability spec covers the OpenSpec configuration's rule content (grepped),
  and the behavior is planning tooling, not application behavior.
- **Alternative — a CI check:** a check can verify a positive-control task exists in `tasks.md`, but not that the check
  was actually shown to fail; the obligation is a planning prompt, not a gate.

### Decision: Write the rule to parse as one string, and verify the CLI consumes it

The rule text contains no unquoted `: `, and implementation confirms consumption with
`openspec instructions tasks --change … --json` (plus a temporary-removal control), matching the config read-path gotcha
and the CI configuration probe.

- **Alternative — trust the CLI reads it:** the config defect class shows a rule can be present yet silently ignored;
  the probe is the guard.

## Risks / Trade-offs

- **A malformed rule is silently ignored** → the read-path probe (task 1.2) and the CI configuration check (task 3.2)
  catch it; the rule stays on the `rules` surface, which that check guards.
- **Drift between the config rule and the `AGENTS.md` bullet** → the two are the injection and recall surfaces of one
  rule; keep their wording aligned when either changes.
