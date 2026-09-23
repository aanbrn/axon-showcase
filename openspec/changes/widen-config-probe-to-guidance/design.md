# Design

## Context

See `proposal.md` for motivation. The CI `build` job's OpenSpec configuration probe (added by #218) creates a throwaway
change and greps its output for `ignoring this artifact's rules|could not parse`. `openspec/config.yaml` declares two
kinds of list surfaces the CLI reads: per-artifact `rules` and per-operation `operations.*.guidance`. A malformed item
on either makes the CLI drop that whole list with a warning — but the probe's pattern matches only the `rules` one, so a
malformed guidance item passes the gate. Reproduced on CLI 1.13.1: a malformed `operations.apply.guidance` item makes
`openspec new change` print
`Guidance for operation 'apply' must be an array of strings, ignoring this operation's guidance` and drops
`operationGuidance`; the current probe does not match it.

## Goals / Non-Goals

**Goals:**

- Make the configuration check fail on a malformed item on **any** declared list surface, not only `rules`.
- Keep the CI probe and the `/opsx-tool-update` re-verification in step.

**Non-Goals:**

- Changing what `openspec validate` checks, or adding a local (non-CI) gate for the config.
- Replacing the probe's mechanism (a throwaway `openspec new change` plus a grep).

## Decisions

### Decision: Match the generic list-shape warning, not an enumerated pair

The probe greps for `must be an array of strings|could not parse` instead of
`ignoring this artifact's rules|could not parse`. The list-shape phrase is emitted for every surface the CLI drops
(`Rules for '…' must be an array of strings, …` and `Guidance for operation '…' must be an array of strings, …`), so the
one pattern covers `rules`, `operations.*.guidance`, and any list surface a future config gains.

- **Alternative — enumerate the two surface warnings**
  (`ignoring this artifact's rules|ignoring this operation's guidance`): explicit, but under-covers a future surface and
  drifts from the CLI's wording; the generic phrase is the stable part of the message.

### Decision: Keep `openspec new change` as the probe's read, and widen `/opsx-tool-update` in the same change

`openspec new change` reads both surfaces (reproduced: a malformed guidance item warns from it), so no second command is
needed. The `/opsx-tool-update` re-verification names the same pattern and checks the four artifacts' `rules`; it is
widened to the same pattern and to the `operations` guidance surface (`openspec instructions apply --change … --json` →
`operationGuidance`), with the guidance warning added to its positive controls, so the two guards do not diverge. The
throwaway probe change is renamed from `openspec-config-rules-probe` to `openspec-config-probe`, since it no longer
covers only rules.

- **Alternative — add an `openspec instructions apply` invocation to the probe:** unnecessary, since `new change`
  already surfaces the guidance warning; it would add a step without covering anything new.

## Risks / Trade-offs

- **A broad pattern could match an unrelated warning** → verify both defect classes reproduce and a clean config is
  silent (the tasks' positive controls), so the pattern is exercised in both directions.
- **The probe runs only in CI** → the `/opsx-tool-update` command is the local re-verification, and it is widened here;
  a renamed warning would still pass silently, which is why both carry positive controls.
