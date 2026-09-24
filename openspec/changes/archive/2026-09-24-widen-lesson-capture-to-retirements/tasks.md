# Tasks

## 1. Widen the lesson-capture definition

- [x] 1.1 In `.opencode/agent/lesson-capture.md`, add the **retirement/replacement analysis**: alongside its additions,
      it reports the `AGENTS.md` rules this change makes obsolete (the mechanism the rule describes is removed) or
      redundant (the change's new enforcement subsumes it), each naming the rule, why the change makes it so, and the
      retirement or replacement it proposes — a candidate for the main agent, not an action. Note the two directions
      (the file shrinks as well as grows).
- [x] 1.2 Add the retirement count to the report-contract verdict line (e.g.
      `<n> durable proposals (<n> additions, <n> retirements)`, with `nothing durable` only when there are neither) and
      update the frontmatter `description`.

## 2. Docs the change owns

- [x] 2.1 Update the `AGENTS.md` lesson-capture bullet: its opening sentence names the rules the change makes obsolete
      or redundant as retirement/replacement candidates, and its growth-discipline clause ("preferring a merge or a
      replacement over an addition") gains "and retiring a rule the change makes unnecessary".
- [x] 2.2 Update `README.md`: the agent-table `lesson-capture` row (its "consolidating rather than accreting" wording)
      and the `Lesson capture` bullet under "What the Agent Automates" (the sentence describing what the capture
      reports).

## 3. Spec capture

- [x] 3.1 Write the delta at
      `openspec/changes/widen-lesson-capture-to-retirements/specs/showcase/quality/agent-skills/spec.md`: one `MODIFIED`
      requirement carrying all five existing scenarios in order, the retirement AND clause in the capture scenario, and
      the new "A change that obsoletes a rule yields a retirement candidate" scenario.
- [x] 3.2 The `showcase/quality/agent-skills` `## Purpose` names the lesson-capture agent but enumerates no outputs, so
      this change does not falsify it; no refresh is owed and the archive commit carries none.

## 4. Verify the surrounding artifacts (no edit expected — confirm, do not assume)

- [x] 4.1 Grep the corpus and docs for the capture's output enumeration (`durable proposals`, `nothing durable`,
      `lesson-capture`) and reconcile any copy this change makes stale.
- [x] 4.2 Confirm `review-quick`'s durability challenge and the `Docs refresh on change` convention need no edit — the
      design Context records the overlap and why the capture is the disjoint home — or edit only what the sweep shows.

## 5. Verification

- [x] 5.1 Run `openspec validate --changes` and confirm the `MODIFIED` block carries every scenario the main spec's
      requirement has, plus the new one.
- [x] 5.2 Run `./gradlew spotlessApply` after the final edit to any Spotless-owned file, then confirm `spotlessCheck`
      and the PR gate (`./gradlew check -PskipITs -Pcoverage.gate.enabled=false`) are green.
- [x] 5.3 Smoke-test the widened subagent after an OpenCode reload, with both controls: a change that obsoletes a rule
      (it must yield a retirement candidate naming the rule and why) and one that does not (it must not). Run
      2026-09-24: `WIDENED: yes` (verdict `1 durable proposal (0 additions, 1 retirement)`); the positive control — a
      seeded rule naming `./scripts/seed-widgets.sh`, removed by the synthetic change — was reported as a retirement
      candidate naming the rule and the removed script; the negative control — a seeded rule naming
      `./gradlew :load-tests:test`, untouched by the change — was explicitly rejected as a retirement. Seeds reverted.
      Premises verified first: the script absent, the `load-tests` module present.
- [x] 5.4 Ran `review-quick` over the implementation diff to clean (round 1 fixed four findings), ran `lesson-capture`
      (nothing durable), and the user gave the manual review pass before committing.
