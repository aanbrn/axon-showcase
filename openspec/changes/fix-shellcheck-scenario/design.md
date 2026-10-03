# Design

## Context

See `proposal.md` — Why. `build-logic/src/main/kotlin/workflow-lint-conventions.gradle.kts` registers `workflowLint` as
an `Exec` task whose `commandLine` is `actionlint <files>` — no shellcheck argument, no shellcheck install. `ci.yml`
installs only actionlint (via its download script). actionlint catches `run:` script errors only through its shellcheck
integration (`-shellcheck`, defaulting to the `shellcheck` command on `PATH`), which is inert when shellcheck is absent.

Verified against `actionlint` 1.7.12 with shellcheck disabled (`-shellcheck ""`):

| Input                                                            | `actionlint -shellcheck ""` | Error class     |
| ---------------------------------------------------------------- | --------------------------- | --------------- |
| a `run:` step with a shell syntax error (`for` missing `; do`)   | exit 0                      | shellcheck-only |
| an invalid step key (`with:` on a `run` step)                    | exit 1                      | `syntax-check`  |
| an undefined context variable in `if` (`${{ nonexistent.foo }}`) | exit 1                      | `expression`    |

So the gate catches workflow-structure and expression errors natively; `run:` **shell** errors are the shellcheck
integration's, which this repository does not ship.

## Goals / Non-Goals

**Goals:**

- Make the "Broken run script fails the build" scenario assert an outcome the gate actually produces in CI.

**Non-Goals:**

- Installing or documenting shellcheck — a separate decision (a new prerequisite and a CI install), and actionlint uses
  shellcheck opportunistically rather than by contract.
- Changing any scenario other than the one unfalsifiable scenario, or any part of the requirement beyond the body's
  "`run:` script errors" clause — the other four scenarios and the rest of the body describe the gate correctly.
- Changing the gate's behavior — the code is correct; the spec's description of it is what is wrong.

## Decisions

### Correct the scenario and the body clause to actionlint's native error classes

The scenario becomes: when a workflow carries an error `actionlint` catches natively — an invalid step/`syntax-check`
error, or an `expression` error such as an undefined context variable — the lint check fails and reports the offending
workflow and location. The requirement body's "`run:` script errors surface locally" clause is corrected to the same
classes, because it makes the identical promise the scenario did: `run:` shell errors come only from the shellcheck
integration the repository does not ship, so the body would otherwise still assert behavior the gate cannot produce.
This is exactly reproducible with `actionlint -shellcheck ""`.

- **Alternative — install and document shellcheck:** makes the current wording true but adds a prerequisite, a CI
  install, and a dependency actionlint only uses opportunistically; the archived `retry-the-actionlint-download` design
  already recorded "the gate is actionlint alone" as the settled reading, so widening the gate is a separate change.
- **Alternative — drop the scenario:** loses the testable claim that the gate catches the errors it does natively; the
  correction keeps a falsifiable scenario rather than removing coverage.
- **Alternative — leave the body clause:** the change names this exact unfalsifiable claim in its Why, so leaving it in
  the body relocates the defect one level up rather than fixing it.

### Keep every other scenario unchanged, and the scenario header as it is

Only the one scenario's WHEN/THEN and the body's one clause are wrong. A `MODIFIED` block therefore carries the
requirement description (with the corrected clause) and every other scenario verbatim, in the main spec's order, so
`openspec validate` sees no dropped scenario. The scenario header "Broken run script fails the build" stays verbatim: a
`MODIFIED` block cannot rename a header, and retitling via REMOVED+ADDED would force every retained scenario to be
carried by hand for a name that still fits the run-step clause the corrected WHEN now leads with.

## Risks / Trade-offs

- [A reader wanted shellcheck coverage] → the scenario now names what the gate does; shellcheck remains out of scope,
  and the proposal says why installing it is a separate decision.
- [The corrected scenario drifts from the actual error classes] → it is reproduced against the pinned actionlint with
  shellcheck disabled, and the error classes named are actionlint's own (`syntax-check`, `expression`).

## Migration Plan

Not applicable — a spec-text correction and an idea removal; no code, data, or API change.

## Open Questions

None.
