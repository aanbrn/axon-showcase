# Tasks

## 1. The spec delta

- [x] 1.1 `openspec/changes/sharpen-experience-analysis/specs/showcase/quality/agent-skills/spec.md` — the `MODIFIED`
      block copies the main spec's `### Requirement: Experience analysis is available to agents` verbatim (its body and
      all four scenarios, in the main spec's order), adds a body clause for the three obligations, and appends three
      scenarios. Verify with `openspec validate --changes` that no scenario is dropped or renamed. **Done** — written at
      the proposal.

## 2. The definition and its describing artifacts

- [x] 2.1 `.opencode/agent/experience-analyzer.md` — the retrospective's three obligations in the definition's own
      words, pointing at the capture/auditor for where an improvement belongs. **Done.**
- [x] 2.2 `.opencode/commands/retrospective.md` — step 4 names the three. **Done.**
- [x] 2.3 `AGENTS.md` — the `experience-analyzer` bullet names them. **Done.**
- [x] 2.4 `README.md` — the retrospective prose names them. **Done.**
- [x] 2.5 Apply this unit's own capture (3.4): `AGENTS.md`'s pre-write step 1 and "recurring unit type" bullet gain
      three clauses, `openspec/config.yaml`'s `proposal`/`design` rules mirror the first two, and no retirement or
      authoring-system recommendation was returned. **Done.**

## 3. Verification

- [x] 3.1 `./gradlew spotlessApply spotlessCheck` and `openspec validate --all` pass; the manual 120-character check is
      clean over the changed set (the change dir is untracked, so the set comes from
      `git status --porcelain --untracked-files=all`, not `git diff --name-only`). **Done** — `spotlessCheck` exit 0;
      `openspec validate --all` 27/27 and `openspec validate sharpen-experience-analysis` valid; the >120 lines are the
      formatter-owned `README.md`/`design.md` tables, the convention-exempt one-line `docs/review-rounds.tsv` rows, and
      one pre-existing `AGENTS.md` line.
- [x] 3.2 Run the `review-quick` subagent over the planning artifacts, then the implementation, repeating until it
      reports no new observations; record each round in `docs/review-rounds.tsv`. **Done** — 4 rounds, one reviewer
      session continued across them; the loop closed on a round reporting nothing remains; the per-round record is in
      `docs/review-rounds.tsv`.
- [x] 3.3 Smoke-run the edited definition after a reload, with a control: a synthetic window whose review-round data
      shows a recurring class must yield a root cause and an earliest prevention stage, an evaluation of the previous
      retrospective's recommendations, and a verifiable outcome per suggestion; a window with nothing to propose must
      say so in one line. A definition edit is not exercised until it is picked up (the subagent-reload gotcha), so this
      gates the archive. **Done** — the post-reload run applied all three: it root-caused a recurring
      `incomplete-enumeration` to the pre-write derivation pass and named that stage as the earliest prevention point,
      evaluated the previous retrospective's recommendations (one applied but not yet measurable, one parked), and gave
      each suggestion a targeted pattern, an expected outcome, and a verification; the empty window returned "Nothing
      worth proposing" in one line.
- [x] 3.4 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. **Also report** any rule this change obsoletes or makes redundant. **Done** — the
      capture's verdict: 3 durable proposals (3 additions, 0 retirements), 0 authoring-system recommendations, proposed
      net +6 `AGENTS.md` lines. All three merge into existing clauses (the pre-write pass's step 1 and the "recurring
      unit type" bullet); the applied net delta is +5 lines (`git diff -U0 AGENTS.md`: the step-1 hunk
      `@@ -303,10 +303,13 @@` and the "recurring unit type" hunk `@@ -1662,15 +1666,17 @@`). The first two clauses are
      mirrored into `openspec/config.yaml`'s `proposal`/`design` rules. **No retirements.**
- [x] 3.5 Ask the user for the manual review pass once 3.2 is clean and 3.4 is done; do not commit or archive before
      they approve. **Asked** — the request is in the session; the outcome is the owner's.

## 4. The change's own records

- [x] 4.1 Record the pre-write derivation pass (every unit owes it): the claims table is in `design.md`, its rows
      derived from the pre-change definition and spec, the practised retrospectives, and the describing sites, with the
      `scripts/claim-scan.py` candidates (base `origin/main`) each resolved into a row or dismissed beside the table.
      **Done** — the pass ran; every scan candidate is a row or a listed dismissal.
- [x] 4.2 Append this unit's review-round rows to `docs/review-rounds.tsv` as each round's report arrives. **Done** —
      each round's row appended as its report arrived.

## Workflow follow-up

- Archive the change after the owner approves and 3.3 has run; the archive commit rides this PR.
