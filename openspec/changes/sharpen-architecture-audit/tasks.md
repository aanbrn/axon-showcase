# Tasks

## 1. The spec delta

- [x] 1.1 `openspec/changes/sharpen-architecture-audit/specs/showcase/quality/agent-skills/spec.md` — two `MODIFIED`
      blocks, each copying the main spec's requirement verbatim (its body and all its scenarios, in order): the
      architecture requirement gains the authority model, the evidence precision, the dedup rule, the bounded advisory,
      and the staged investigation (five scenarios); the intent requirement gains the non-ADR rationale search, the
      condition classification, and the partial-landing distinction (two scenarios). Verify with
      `openspec validate --changes` that no scenario is dropped or renamed. **Done** — written at the proposal.

## 2. The definition and its describing artifacts

- [x] 2.1 `.opencode/agent/architecture-auditor.md` — the authority cases, the evidence precision, the dedup rule, the
      bounded advisory, the staged investigation, and the condition-classification / partial-landing precision, in the
      definition's own words. **Done.**
- [x] 2.2 `.opencode/commands/audit-architecture.md` — step 1 names the new rules. **Done.**
- [x] 2.3 `AGENTS.md` — the `architecture-auditor` bullet names the routing-relevant rules and delegates the rest to its
      spec pointer. **Done.**
- [x] 2.4 `README.md` — the architecture prose names the routing-relevant rules. **Done.**
- [x] 2.5 Apply this unit's own capture (3.4): `AGENTS.md`'s review gate step 2, the spec-delta workflow's two
      Purpose-refresh bullets, the boundary-wording bullet, and the failure-handling self-records bullet gain clauses;
      `openspec/config.yaml`'s `rules.design` clause is widened; `docs/ideas.md` gains one parked entry. **Done.**

## 3. Verification

- [x] 3.1 `./gradlew spotlessApply spotlessCheck` and `openspec validate --all` pass; the manual 120-character check is
      clean over the changed set (the change dir is untracked, so the set comes from
      `git status --porcelain --untracked-files=all`, not `git diff --name-only`). **Done** — `spotlessCheck` exit 0;
      `openspec validate --all` 27/27 and `openspec validate sharpen-architecture-audit` valid; the >120 lines are the
      formatter-owned `README.md`/`design.md` tables, the convention-exempt one-line `docs/review-rounds.tsv` rows, and
      one pre-existing `AGENTS.md` line.
- [x] 3.2 Run the `review-quick` subagent over the planning artifacts, then the implementation, repeating until it
      reports no new observations; record each round in `docs/review-rounds.tsv`. **Done** — the per-round record is in
      `docs/review-rounds.tsv`.
- [x] 3.3 Smoke-run the edited definition after a reload, with a control: a synthetic surface with (a) a dependency edge
      no recorded rule forbids, (b) two records contradicting each other, and (c) a deferral whose condition cannot be
      verified must yield no finding for (a), an advisory for (b), and no expiry claim for (c). A definition edit is not
      exercised until it is picked up (the subagent-reload gotcha), so this gates the archive. **Done** — after a
      reload, the run routed (a) not reported, (b) an advisory observation, (c) not reported, matching the three
      controls.
- [x] 3.4 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. **Also report** any rule this change obsoletes or makes redundant. **Done** — the
      capture ran twice (before and after the loop closed). The first returned 3 durable additions (0 retirements),
      merging into the review gate's step 2, the spec-delta workflow's two Purpose-refresh bullets, and the
      boundary-wording failure-handling bullet. The second widened the failure-handling self-records bullet (a recurring
      `claim-unreproducible` means the change's own self-records were drafted from intent), widened
      `openspec/config.yaml`'s `rules.design` clause to match, parked one `docs/ideas.md` idea (the review-loop session
      trigger), and skipped one low-confidence retirement (the `architecture-auditor` bullet's authority-case list). The
      captures' applied net delta is +12 `AGENTS.md` lines (per-hunk `git diff -U0 origin/main -- AGENTS.md`: +3, +7,
      +1, +1, +0); the whole-file delta is 53 insertions / 38 deletions, the describing-site sweep contributing the
      remaining +3. The first run also caught the live defect: the Purpose-refresh determination was wrong, so task 4.3
      records the refresh. **No retirements.**
- [x] 3.5 Ask the user for the manual review pass once 3.2 is clean and 3.4 is done; do not commit or archive before
      they approve. **Done** — requested and approved.

## 4. The change's own records

- [x] 4.1 Record the pre-write derivation pass (every unit owes it): the claims table is in `design.md`, its rows
      derived from the pre-change definition and spec, the authority precedent, and the describing sites, with the
      `scripts/claim-scan.py` candidates (base `origin/main`) each resolved into a row or dismissed beside the table.
      **Done** — the pass ran; every scan candidate is a row or a listed dismissal.
- [x] 4.2 Append this unit's review-round rows to `docs/review-rounds.tsv` as each round's report arrives. **Done** —
      each round's row appended as its report arrived.
- [ ] 4.3 **Owed — for the archive commit**: the `agent-skills` `## Purpose` names the parked-entry outcome ("a parked
      `docs/ideas.md` entry whose work has landed"), which this change widens to fully/partly landed or a different
      mechanism achieving the objective. A delta cannot carry a Purpose, so the edit lands with the archive.

## Workflow follow-up

- Archive the change after the owner approves and 3.3 has run; the archive commit rides this PR.
