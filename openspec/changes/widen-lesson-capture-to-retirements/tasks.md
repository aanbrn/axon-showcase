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
- [ ] 3.2 Read the `showcase/quality/agent-skills` `## Purpose` and record whether this change falsifies it (it names
      the lesson-capture agent but enumerates no outputs, so no refresh is expected); apply it in the archive commit if
      owed, since a delta cannot carry a `## Purpose`.

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
- [ ] 5.3 Smoke-test the widened subagent after an OpenCode reload, with both controls: a change that obsoletes a rule
      (it must yield a retirement candidate naming the rule and why) and one that does not (it must not). Verify each
      seed's premise against the repository before reading the run, and record which branch the run exercised.
- [ ] 5.4 Run `review-quick` over the implementation diff, fix its findings, re-run until clean, then request the user's
      manual review pass before committing.
