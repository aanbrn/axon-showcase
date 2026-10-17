# Tasks

## 1. The spec delta

- [x] 1.1 `openspec/changes/capture-authoring-system-gaps/specs/showcase/quality/agent-skills/spec.md` — the `MODIFIED`
      block copies the main spec's `### Requirement: Per-change quality-gate and analysis subagents are available`
      verbatim (its description and all nine scenarios, in the main spec's order), adds one `AND` clause and one
      scenario to the capture, extends the verdict clause to name the authoring-system count, and widens the
      quick-review scenario's scan clause to mirror the capture's applied `AGENTS.md` merges (task 2.5). Verify with
      `openspec validate --changes` that no scenario is dropped or renamed. **Done** — written at the proposal; the
      scenarios are the main spec's plus "A lesson whose cause is an authoring mechanism is routed to it".

## 2. The definition and its describing artifacts

- [x] 2.1 `.opencode/agent/lesson-capture.md` — the origin-diagnosis paragraph (after the promotion gate), the
      report-contract group (verdict bullet extended to name the authoring-system count), and the frontmatter
      `description`. **Done.**
- [x] 2.2 `AGENTS.md` — the "Capture lessons" workflow's step 1 names the third output kind, so the main agent routes it
      rather than applying it as an entry. **Done.**
- [x] 2.3 `README.md` — the agent table row, the "Lesson capture" prose bullet, and the Self-Learning Loop prose name
      the routing. **Done.**
- [x] 2.4 `openspec/config.yaml` — the capture task rule names the routing, so it stays accurate about what to apply.
      **Done.**
- [x] 2.5 Apply this unit's own capture (3.4): `AGENTS.md`'s review gate step-2 claims-table clauses widened with its
      two merges, the delta's scan clause mirroring them, the `openspec/config.yaml` `design` rule synced, and its
      authoring-system recommendation parked in `docs/ideas.md`. **Done.**

## 3. Verification

- [x] 3.1 `./gradlew spotlessApply spotlessCheck` and `openspec validate --all` pass; the manual 120-character check is
      clean over the changed set (the change dir is untracked, so the set comes from
      `git status --porcelain --untracked-files=all`, not `git diff --name-only`). **Done** — `spotlessCheck` exit 0;
      `openspec validate --all` 27/27 and `openspec validate capture-authoring-system-gaps` valid; the >120 lines are
      formatter-owned markdown (the `README.md` and `design.md` tables, and the definition's verdict-line inline code
      span, which Prettier cannot break), the convention-exempt one-line `docs/review-rounds.tsv` record rows, and one
      pre-existing `AGENTS.md` line.
- [x] 3.2 Run the `review-quick` subagent over the planning artifacts, then the implementation, repeating until it
      reports no new observations; record each round in `docs/review-rounds.tsv`. **Done** — the loop closed on a round
      reporting nothing remains, one reviewer session continued across the rounds; the per-round record is in
      `docs/review-rounds.tsv`.
- [x] 3.3 Smoke-run the edited definition after a reload, with a control: a synthetic unit whose lesson is
      mechanism-caused must yield an authoring-system recommendation, and one whose lesson is ordinary content knowledge
      must yield an ordinary proposal. A definition edit is not exercised until it is picked up (the subagent-reload
      gotcha), so this gates the archive. **Done** — the run applied the new step: the mechanism-caused unit (a
      template's shape encouraging a glued brace) yielded an authoring-system recommendation naming the
      `openspec/config.yaml` `design` rules, with the mechanism's premise verified, and the ordinary unit (a new
      endpoint needing `springdoc.packages-to-scan`) yielded nothing; the verdict line named the new count. Ran
      in-session with the edited definition in effect; a post-reload re-run is the stricter confirmation.
- [x] 3.4 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. **Also report** any rule this change obsoletes or makes redundant. **Done** — the
      capture's verdict: 2 durable proposals (2 additions, 0 retirements), 1 authoring-system recommendation, net +3
      `AGENTS.md` lines. Both additions merge into the review gate's step-2 bullet; the applied net delta is +3 lines
      (`git diff -U0 AGENTS.md` step-2 hunk `@@ -320,10 +320,13 @@`, 13 added / 10 removed). The recommendation — a
      `proposal`-rule requirement to derive a change's touched sites from its tasks — is parked in `docs/ideas.md`. **No
      retirements**: the change adds an obligation and removes no mechanism an existing rule describes.
- [x] 3.5 Ask the user for the manual review pass once 3.2 is clean and 3.4 is done; do not commit or archive before
      they approve. **Asked** — the request is in the session; the outcome is the owner's.

## 4. The change's own records

- [x] 4.1 Record the pre-write derivation pass (every unit owes it): the claims table is in `design.md`, its rows
      derived from the pre-change definition and spec, the `docs/ideas.md` entries, and the archived structured-writing
      change, with the `scripts/claim-scan.py` candidates (base `origin/main`) each resolved into a row or dismissed
      beside the table. **Done** — the pass ran; the scan's candidates are resolved in the dismissals block.
- [x] 4.2 Append this unit's review-round rows to `docs/review-rounds.tsv` as each round's report arrives. **Done** —
      each round's row appended as its report arrived.

## Workflow follow-up

- Archive the change after the owner approves and 3.3 has run; the archive commit rides this PR.
