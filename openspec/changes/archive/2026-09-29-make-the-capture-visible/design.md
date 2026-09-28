# Design

## Context

See `proposal.md` for motivation. The premise was verified against the repository, not taken from the retrospective.

- The capture obligation lives in `AGENTS.md`'s capture bullet (`:184–244`): run the `lesson-capture` subagent once per
  unit after its implementation quick review is clean; it forbids skipping the subagent or concluding "nothing to
  capture" on the agent's own judgment, and prescribes a merge-time read that detects what the merge alone affected. The
  rule is unambiguous; the two skipped runs named in the retrospective (`scope-capture-trigger-to-the-unit`,
  `check-unique-cron-schedules`) left no trace, so the skip surfaced only when a human or the merge-time read
  reconstructed it.
- The merge-time read already reads the pull request, so a record placed in the PR's change dir (`tasks.md`) or its body
  is readable there without a new mechanism. That is the "verifiable at the merge" property S1 asks for.
- `openspec/config.yaml` injects its per-artifact `rules` into the instructions the CLI returns at
  `openspec new change`/`openspec instructions` time, so a `tasks` rule reaches every change while its `tasks.md` is
  authored — the mechanism `inject-positive-control-task-rule` (#375) shipped and `widen-config-probe-to-guidance`
  (#377) guards. A malformed rule is silently dropped (the config read-path defect), but `ci.yml`'s probe fails the
  build on it.
- A change has a change dir; a docs refresh, a standalone fix, and a dependency bump do not. The capture rule is
  unit-scoped, so a record surface that only covers changes leaves the non-change units S1's evidence class also
  includes (the window's ~40 capture/park/record PRs).

Constraints:

- **No CI check can decide whether the capture ran.** A gate can assert a checkbox exists in a change's `tasks.md`, but
  a fabricated tick passes it, so the record is a visibility mechanism verified by the merge-time read, not a gate — the
  same reasoning #375 recorded for its injected rule.
- **A `MODIFIED` requirement replaces its whole block**, so the delta copies the main spec's block verbatim (all six
  scenarios, in order, byte-identical header) and edits it; `openspec validate --changes` checks the scenario-set
  equality.
- **Specs describe outcomes, not the surfaces implementing them.** The record's outcome (a capture leaves a record the
  merge-time read verifies) belongs in the capability; the choice of `tasks.md` vs the PR checklist is control flow and
  stays in `AGENTS.md` and this design.

## Goals / Non-Goals

**Goals:**

- A skipped per-unit capture is a missing or unticked record at the merge, for every unit class: a change by a
  `tasks.md` task, a unit with no change dir by a pull-request checklist line.
- Reach every future change at planning time, without depending on the agent recalling a prose obligation.
- State the record once as an observable outcome in the `agent-skills` capability, so the corpus holds the behavior.

**Non-Goals:**

- Automated enforcement of the capture: no check can confirm a subagent ran, so the record is not a gate.
- Persisting the capture's verdict _content_ (its proposals and rejections) — that is the separate parked idea
  (`docs/ideas.md`, "Record the lesson-capture's rejected proposals"), not S1's record of the run.
- Editing `.opencode/agent/lesson-capture.md` (see Decisions).
- Retitling the requirement header `Per-change quality-gate and analysis subagents are available` or the existing
  scenario labels: a `MODIFIED` block cannot retitle them, and a `REMOVED`/`ADDED` pair is disproportionate.

## Decisions

### Decision: the record lives in both a change's `tasks.md` and the pull-request checklist

A change records the capture as a task in its `tasks.md`; a unit with no change dir records it as a line in the
pull-request checklist (`.github/PULL_REQUEST_TEMPLATE.md`). Each surface covers the units the other cannot, and both
are readable by the merge-time read. Neither is applied automatically: the change-side box is injected by the config
`tasks` rule at planning time, while the checklist line is written into the pull-request body by the agent, per the
`AGENTS.md` obligation (task 3.1) — the repository's PR convention supplies the body from a file (`--body-file`, per its
`gh pr create --body` gotcha), so the template does not supply the line.

- **Alternative — `tasks.md` only:** reaches changes but leaves non-change units (docs refresh, standalone fix,
  dependency bump) with no record; the capture rule is unit-scoped, so half the units would keep the recollection
  failure.
- **Alternative — PR checklist only:** the change dir would carry no record (a reviewer of the archived change could not
  see the capture), and a PR body is not durable while the `tasks.md` task survives in `openspec/changes/archive/`.
- **Alternative — a separate record file or a CI gate:** a gate can assert a task exists but not that the subagent ran,
  so it would pass a fabricated tick and give a false green (the reasoning #375 recorded); a new file has no reader.

### Decision: deliver the change-side record through the `openspec/config.yaml` `tasks` rule

The rule reaches every change's `tasks.md` at planning time without a prose obligation, and it is the surface #375
proved and #377's probe guards against the config read-path defect (a malformed rule drops the whole artifact's rule set
silently). Because the injected rule is a second copy of the obligation stated in `AGENTS.md`, the docs-refresh bullet's
config-copy sentence is widened from the `context:` block to also cover the per-artifact `rules`, stating what is
un-gated precisely — the rule's content and currency (whether its wording still matches `AGENTS.md` and the spec), since
`ci.yml`'s probe guards the list's shape but not its content.

- **Alternative — an `AGENTS.md` prose obligation alone:** exactly the class that failed: the rule is read on recall and
  a skipped run looks like a run that found nothing.
- **Alternative — a spec obligation alone:** a spec is loaded only when its capability is worked on and cannot inject a
  task into `tasks.md`, so it would not reach the moment the box is authored.

### Decision: `.opencode/agent/lesson-capture.md` does not change

The record is planned before the subagent runs (a task authored at propose time, or a checklist line) and verified after
it (the merge-time read), both by the main agent. The subagent cannot observe its own skip — it runs only when it is not
skipped — so a definition edit would bind the obligation to the actor that cannot see the failure. The capability spec
still gains the record, because its capture scenario already covers the main agent's part in the loop ("which the main
agent verifies and applies").

- **Alternative — make the subagent emit the record:** the run already happening is what the record must detect; a
  record the subagent writes cannot exist when the subagent is skipped.
- **Alternative — add the record to the definition for discoverability:** it would duplicate the `AGENTS.md` obligation
  on the wrong actor and owe a definition sweep (frontmatter, README row) for no behavioral gain.

### Decision: the spec delta `MODIFIED`s the per-unit quality-gate requirement

The delta carries the requirement block verbatim, adds the record `AND` clause to the capture scenario, and adds the "A
skipped capture is a missing record at the merge" scenario — the same `AND`-plus-scenario shape the requirement already
uses for retirement.

- **Alternative — an `ADDED` requirement:** the record refines the capture the existing requirement already describes,
  and the guidance prefers a `MODIFIED` block over a permanent new corpus entry.
- **Alternative — `REMOVED`/`ADDED` to retitle the header:** disproportionate for a body change; the header (and the
  existing scenario labels) stay byte-identical, as `scope-capture-trigger-to-the-unit` also recorded.

The capability's `## Purpose` does not move: it enumerates the review and lesson-capture agents and their per-unit
scope, neither of which this change alters, so no archive-time Purpose refresh is owed. Recorded as a task, not assumed.

### Decision: the record is a visibility mechanism, not a gate

The merge-time read verifies the record; no build or CI check does. A check could only assert the checkbox exists, and a
fabricated tick would pass it, so a gate would add a false signal rather than catch the skip.

## Risks / Trade-offs

- **A checkbox is still tickable without the run** → the record makes a skip _visible_, not impossible: the merge-time
  read reports the missing or unticked record, and the existing "Do not skip the subagent" clause still makes the
  fabricated-tick act one the agent is directed against. This is strictly more than the recollection it replaces.
- **Drift between the injected config rule and the `AGENTS.md`/spec obligation** → the docs-refresh bullet's config-copy
  sentence now covers the per-artifact `rules` as to content and currency, and `ci.yml`'s config probe fails the build
  on a malformed rule — the shape it guards, not the content.
- **Under-coverage of non-change units** → the pull-request checklist line is the universal surface, and it is carried
  by the `AGENTS.md` obligation (the agent writes it into the body) rather than supplied by the template, since the body
  comes from a file.
- **The config rule reaches only OpenSpec changes** → intended: the `tasks` rule is the change-side half; the checklist
  line is the other.
