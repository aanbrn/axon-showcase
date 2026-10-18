# Tasks

## 1. The rule

- [x] 1.1 `openspec/changes/add-a-fresh-session-trigger/specs/showcase/quality/agent-skills/spec.md` — the `MODIFIED`
      requirement carries the main spec's block verbatim with the session clause gaining the third trigger.
      Verification: all ten scenarios present, in order, headers unchanged; `openspec validate --changes` passes.
- [x] 1.2 `AGENTS.md` — the "Continue the reviewer's session" bullet's trigger list synced to the requirement (expired,
      the definition changed, or a mid-round non-response), keeping its pointer to the spec as the rule's home.
      Verification: the bullet's three triggers match the requirement's, and the pointer sentence is unchanged.

## 2. The parked idea

- [x] 2.1 `docs/ideas.md` — remove the "Add a third fresh-session trigger to the review loop's `review-quick` session
      rule" entry, implemented by this change. Verification: `grep -c "third fresh-session trigger" docs/ideas.md` → 0,
      and no other entry is disturbed.

## 3. Verification

- [x] 3.1 Sweep the corpus for a site still stating only the two triggers: grep `session id`, `fresh session`, and
      `reviewer's session` across `AGENTS.md`, `README.md`, `.opencode/`, `openspec/specs/`, and `openspec/config.yaml`.
      Verification: each live site states the three triggers or points at the requirement.
- [x] 3.2 `./gradlew spotlessApply` after the last edit to a Spotless-owned file, then `./gradlew spotlessCheck`.
      Verification: both green, and the apply ran after the final write.
- [x] 3.3 `openspec validate --all` and `openspec validate --changes`. Verification: both report no failures.
- [x] 3.4 The 120-character recipe over the changed set the formatter does not cover. Verification: no over-120 line.
- [x] 3.5 Run the per-unit `lesson-capture` decision against the skip condition and record the outcome here (the run, or
      the skip and its reason). The unit touches `AGENTS.md`, so the condition cannot hold; the run produced 1 durable
      proposal (merged into the "Programmatic edits to a structured file" gotcha: an edit anchored on a neighbouring
      item's line applies at the neighbour), net **+3** `AGENTS.md` lines, 0 retirements, 0 authoring-system
      recommendations.
- [x] 3.6 Carry the pre-write derivation pass — its output is the claims table in `design.md`, and each row's source or
      deriving command reproduces.
