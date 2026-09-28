# Design

## Context

See `proposal.md` — Why. The premise was verified against the repository rather than taken from the parked idea.

`AGENTS.md`'s capture bullet is the authority: it already reads "**Capture lessons once, after a unit's
implementation**, and detect at the merge" and "a `captured: <unit>` marker", widened by the
`re-measure-load-test-baseline` unit whose capture shipped as PR #431 (`Capture the re-measure unit's lessons`; the
change was a capture-only docs unit with no change dir, which is why no archived change dir carries that name). The same
convention's other instances were not swept, so they still read change-scoped while describing the same per-unit rule.

Three constraints bound the design:

- **A definition edit that changes spec'd behavior is a change, not a docs edit** — `showcase/quality/agent-skills`
  describes the `lesson-capture` and `review-quick` subagents, so widening their definitions owes that capability's
  delta. The parked idea recorded the same routing decision.
- **A `MODIFIED` requirement replaces the whole block**, so each delta copies its main-spec block verbatim (every
  scenario, in the main spec's order) and edits it, and `openspec validate --changes` checks the scenario-set equality.
- **The `captured: <change>` token is a stated marker form**, so widening it to `<unit>` is a requirement change — it
  belongs in the delta rather than being a silent docs edit.

## Goals / Non-Goals

**Goals:**

- Make the unit the subject of the capture and review convention across the instance set the plan enumerates
  (`proposal.md`'s Impact; `tasks.md`'s sweep tasks): the two definitions, the two capability deltas (and the
  `agent-skills` `## Purpose` at archive), the README, the experience-analyzer's layer line, `AGENTS.md`'s
  experience-analyzer bullet, and the parked idea's removal.
- Derive that set by grep each time it grows, and record every surviving change-scoped instance as a keep with a reason,
  so the corpus states one vocabulary without over-widening.

**Non-Goals:**

- **No edit outside the widened set — in particular, no `AGENTS.md` edit beyond the instance the sweep list names**
  (`:1018`'s experience-analyzer bullet `per-change`). `AGENTS.md`'s capture rule is already unit-scoped and is the
  authority this change brings the others into line with, so an instance there is edited only when the sweep names it.
  The kept instances, each with its reason, are listed under Decisions.
- Editing `docs/ideas.md` by hand — the change's own idea removal is a task and rides the branch.
- Retitling the requirement header `Per-change quality-gate and analysis subagents are available`, or the "A change that
  obsoletes a rule yields a retirement candidate" scenario: `openspec validate` rejects a `MODIFIED` block whose header
  is not found and whose scenario names drift, so both stay verbatim while their bodies read unit-scoped. A
  `REMOVED`/`ADDED` pair would be disproportionate — the header names the artifact class the gates belong to, and the
  scenario name is a label.
- Any change to `scripts/commit-hygiene.py` or the pre-commit guard, which match the bare `captured:` token and check
  placement only.

## Decisions

**The review scenario is in scope, and its durability clause widens with it.** `AGENTS.md`'s review gate is explicitly
unit-scoped — "**The review gate is not OpenSpec-specific.** Run the same quick-review-then-manual-review sequence for
every unit of work that will become a PR — a docs refresh, a standalone fix, a dependency bump". The `review-quick`
definition already carries that vocabulary (a non-OpenSpec change, "a diff against the repository with no change dir"),
and the `agent-skills` scenario's first clause says "a change's proposal (planning artifacts) or its implementation" — a
non-change unit has neither, so the clause genuinely excludes it. Widening the review subject is therefore the same
drift, not an unrelated one.

The durability clause attaches to the reviewed thing's diff, so it widens too: a capture is unit-scoped, and a unit
without a change dir that adds `AGENTS.md` rules still owes the durability challenge. The widened definition
(`review-quick.md`) and the delta state one subject. One mention in the scenario stays change-scoped by meaning and is a
recorded keep: `whether a future change would plausibly hit it` is about future changes, not the reviewed unit.

**`change dir` stays.** A change dir is a real artifact — only OpenSpec changes have one — so the capture's inputs list
keeps "the change dir (proposal/design/tasks/delta spec)" and the gate's routing keeps "or the change dir". The same
reasoning applies to `the unit's planning artifacts` in the review scenario: for a unit with a change dir the two are
the same directory, and for a unit without one the unit term is the correct one.

**The retirement scenario's header stays verbatim while its body widens.** The validator forbids retitling a `MODIFIED`
requirement block's scenarios, and a `REMOVED`/`ADDED` pair would be disproportionate for a one-word body change; the
scenario name is a label, and `AGENTS.md` and the capture definition carry the widened rule.

**The marker form widens via the delta, not silently.** `AGENTS.md` already states `captured: <unit>`; the
`agent-skills` marker clause and the `commit-hygiene` pointer to it are the two spec surfaces that still state
`<change>`. Both are edited in the same change so the corpus states one form.

**The `per-change` adjective widens in prose, but the requirement header stays.** The adjective names the same
unit-scoped subject as the rest of the sweep, so every instance the widened set enumerates loses it: the two
`agent-skills` requirement bodies (the quality-gate requirement's description and the report-contract requirement's
bearer list), the capability's `## Purpose` (at archive), the experience-analyzer's layer line, and `AGENTS.md`'s
experience-analyzer bullet. The header `Per-change quality-gate and analysis subagents are available` stays: a
`MODIFIED` block cannot retitle it (the validator matches by header verbatim), and a `REMOVED`/`ADDED` pair is
disproportionate for an adjective. The `agent-skills` `## Purpose` is refreshed in the archive commit (a delta cannot
carry a Purpose for an existing capability) — the capability's scope statement does move, since it enumerates the review
and lesson-capture agents as per-change.

**The sweep's kept set, each with its reason.** The instances the sweep leaves change-scoped — as of this plan; the list
is re-derived by `tasks.md` 5.2's grep rather than carried as an absolute — are kept for one of these reasons:

- **A `MODIFIED` block cannot retitle it** — the requirement header
  `Per-change quality-gate and analysis subagents are available` (main spec `:80`) and the scenario label
  `A change that obsoletes a rule yields a retirement candidate` (`:148`). Recorded as a Non-Goal; the bodies widen
  regardless.
- **The subject is inherently a change** — `.opencode/agent/review-thorough.md:3` compares the implementation against
  its delta specs, tasks, and design, artifacts only a change has.
- **The vocabulary names a historical aggregate, not the trigger scope** — `.opencode/agent/experience-analyzer.md:30`
  reads "per-change captures", the record a retrospective aggregates; per-change in practice, and not narrower than the
  rule.
- **The adjective names a meta-rule subject class** — `.opencode/agent/agents-auditor.md:101` and the `agent-skills`
  spec `:246` read "the per-change workflow" as one example of what an accreted meta rule is about; the class the
  literal adjective matches, not the capture's scope.
- **The sentence's own scope** — `AGENTS.md`'s auto-review bullet's "a change's proposal (planning artifacts)" is the
  OpenSpec-vocabulary half of its two-part sentence, the unit half being the next paragraph's "every unit of work".
- **A real artifact name** — `change dir` everywhere (the capture's inputs list and the gate's routing): only an
  OpenSpec change has one, and for a unit without one the parenthetical's artifacts are simply absent.
- **A mechanical consumer** — `scripts/commit-hygiene.py` and the pre-commit guard match the bare `captured:` token and
  check placement only; the `scripts/test-commit-hygiene.py` fixture string is the same.
- **A historical record** — an instance under `openspec/changes/archive/`, `docs/audits/`, or `docs/retrospectives/` is
  left as recorded.

A new instance the grep finds that is narrower than the rule is a widen site, not a keep.

## Risks / Trade-offs

- **Over-widening** (a genuinely change-scoped mention gets the unit term) → mitigated by reading each instance against
  the reuse test (does it name the thing being captured/reviewed?), and by the kept set recorded under Decisions, which
  is re-derived by `tasks.md` 5.2's grep rather than carried as an absolute list.
- **Under-widening** (a stale instance survives a grep-blind pass) → mitigated by sweeping each artifact for the
  convention with several searches — including the `per-change` adjective — rather than one phrase, and by reading the
  archived analogous changes' artifact sets.
- **The `agent-skills` `## Purpose` still reads `per-change` until the archive commit** — recorded as an explicit task,
  since a delta cannot carry a Purpose for an existing capability; the corpus is briefly inconsistent in that one file
  by design.
- **A term mismatch between the specs and the definitions** — both are edited in this change, so the corpus and the
  definitions land on the same vocabulary.

## Migration Plan

Not applicable — agent tooling and specs take effect on the next OpenCode reload. Verification is reading the widened
definitions, the two deltas, and the README, plus `openspec validate --changes` and the formatter gate; the sweep's
completeness is checked by grepping each artifact for the convention's vocabulary — including the `per-change` adjective
— and the two edited definitions are smoke-run after a reload, seeded with a non-change unit the capture must accept and
a change-scoped check it must still perform.
