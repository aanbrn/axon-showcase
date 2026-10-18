# Tasks

## 1. The capture workflow's rule

- [x] 1.1 `AGENTS.md` — the capture workflow's **Trigger** names the evidence gate: the implementation capture runs once
      the quick review is clean and the skip condition does not hold, and the merge-time detection still always runs.
      Verification: the trigger names the clean review, points at the skip condition, and leaves the merge clause
      unchanged.
- [x] 1.2 `AGENTS.md` — the **Procedure** gains the decision as its first step (decide whether the skip condition holds,
      skip when it does, recording the reason) and renumbers the run, the apply, and the merge. Verification: the
      procedure names the decision, the run, the apply with the record, and the merge, in order.
- [x] 1.3 `AGENTS.md` — a **Decision points** bullet states the skip condition as a closed conjunction over surfaces the
      unit already produced, naming the surface each clause reads. Verification: every clause names a surface the unit
      produced, and none rests on a recollection.
- [x] 1.4 `AGENTS.md` — the **Failure handling** absolute ("Do not skip the subagent") becomes "do not skip outside the
      skip condition", keeping the rationale that the subagent is the arbiter and citing the three recorded skips.
      Verification: the bullet forbids an out-of-condition skip and no longer forbids the in-condition one.
- [x] 1.5 `AGENTS.md` — the **Completion criteria** record the decision (the applied net delta, or the skip and its
      reason) rather than only the run, and the merge-time read re-verifies a recorded skip. Verification: the criteria
      name both outcomes and the re-verification.

## 2. The surfaces that carry the rule

- [x] 2.1 `.opencode/agent/lesson-capture.md` — the frontmatter `description` and the opening sentence state when the
      subagent is used (a unit whose evidence shows a candidate lesson). Verification: neither implies a run per unit.
- [x] 2.2 `openspec/config.yaml` — the injected `tasks` rule carries the capture **decision** — run it when the skip
      condition does not hold, apply the durable proposals, and record the applied net delta or the skip and its reason
      — with no unquoted `: ` in the item. Verification: `openspec instructions tasks --change <name> --json` returns
      the item, and the CLI emits no list-shape warning.
- [x] 2.3 `.github/PULL_REQUEST_TEMPLATE.md` — the capture checklist line records the decision (ran with its delta, or
      skipped with its reason) for a unit with no change dir. Verification: the line names both outcomes.
- [x] 2.4 `.opencode/commands/opsx-tool-update.md` — the review-gate bullet names the capture decision rather than the
      run. Verification: the bullet no longer requires the run unconditionally.
- [x] 2.5 `README.md` — the `lesson-capture` agent-table row, the "Lesson capture" bullet, and the self-learning loop's
      per-unit sentence state the evidence-gated invocation and the recorded skip. Verification: each of the three sites
      reads the new model, and no site still calls the capture automatic.

## 3. The delta and its coherence

- [x] 3.1 `openspec/changes/make-the-capture-selective/specs/showcase/quality/agent-skills/spec.md` — the `MODIFIED`
      requirement carries the main spec's block verbatim with the capture scenario's evidence-gated invocation and
      recorded skip, and the merge scenario's re-verification. Verification: all ten scenarios present, in order, with
      their headers unchanged; `openspec validate --changes` passes.
- [x] 3.2 Sweep the corpus for a site still stating the unconditional run: grep `lesson-capture` across `AGENTS.md`,
      `README.md`, `.opencode/`, `openspec/specs/`, and `openspec/config.yaml`. Verification: each remaining site states
      the selective model or is a historical record.

## 4. Verification

- [x] 4.1 `./gradlew spotlessApply` after the last edit to a Spotless-owned file, then `./gradlew spotlessCheck`.
      Verification: both green, and the apply ran after the final write.
- [x] 4.2 `openspec validate --all` and `openspec validate --changes`. Verification: both report no failures.
- [x] 4.3 The 120-character recipe (`perl -CSD -lne 'print "$ARGV:$." if length > 120'`) over the changed set the
      formatter does not cover — `openspec/config.yaml`; the markdown and JSON are formatter-gated. Verification: no
      line in those files over 120.
- [ ] 4.4 Smoke-run the edited `lesson-capture` definition after an OpenCode reload, with both controls: a unit whose
      review record shows a compliance miss (a candidate lesson it must propose) and a unit whose only observation an
      existing `AGENTS.md` rule already covers (a lesson it must suppress). Verification: the run proposes the first and
      suppresses the second; record which branch the seed exercised.
- [x] 4.5 Run the per-unit `lesson-capture` decision for this unit against the skip condition — this change touches the
      authoring surface, so the condition cannot hold — and apply its durable proposals, recording the applied net
      `AGENTS.md` delta here. Applied: 1 durable proposal (the doc-consistency sweep bullet widened from `replaced` to
      `authored or replaced`, keyed on the rule's subject), net **+2** `AGENTS.md` lines; its two authoring-system
      recommendations applied to their mechanisms (`review-quick.md`'s `single-sourcing` class widened, and
      `openspec/config.yaml`'s injected single-sourcing rule widened to match, in all three artifact lists).
- [x] 4.6 Carry the pre-write derivation pass — its output is the claims table in `design.md`, and each row's source or
      deriving command reproduces.
