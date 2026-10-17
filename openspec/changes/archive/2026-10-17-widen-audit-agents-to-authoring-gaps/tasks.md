# Tasks

## 1. The spec delta

- [x] 1.1 `openspec/changes/widen-audit-agents-to-authoring-gaps/specs/showcase/quality/agent-skills/spec.md` — two
      `MODIFIED` blocks, each copying the main spec's requirement verbatim (its body and all its scenarios, in order):
      the audit requirement gains the authoring-system gap class (a body paragraph and three scenarios), and the
      structured-writing requirement's "Dense prose is restructured on review" scenario gains the finding's practical
      impact and a minimally invasive rewrite. Verify with `openspec validate --changes` that no scenario is dropped or
      renamed. **Done** — written at the proposal.

## 2. The definition and its describing artifacts

- [x] 2.1 `.opencode/agent/agents-auditor.md` — the frontmatter `description`, the consistency axis (the structured
      writing dimensions and the one-authoritative-definition judgment), the new authoring-system gap class paragraph,
      the method's mechanism-investigation and ambiguity bullets, and the report contract's verdict line and budget.
      **Done.**
- [x] 2.2 `.opencode/commands/audit-agents.md` — its `description`, its step-1 read-list, and its step-2 report list.
      **Done.**
- [x] 2.3 `AGENTS.md` — the `agents-auditor` bullet names the new class in its lead and its finding-classes list.
      **Done.**
- [x] 2.4 `README.md` — the audit prose and the two table rows (the agent table and the slash-command table). **Done.**
- [x] 2.5 `.opencode/commands/apply-audit-reductions.md` and `docs/ideas.md` — the two further live enumerations of the
      audit's report classes name the new class. **Done.**
- [x] 2.6 Apply this unit's own capture (3.4): `AGENTS.md`'s "recurring unit type" bullet gains two clauses, its "the
      only X" bullet retires the verified-false completeness count, and its authoring-system recommendation is parked in
      `docs/ideas.md`. **Done.**

## 3. Verification

- [x] 3.1 `./gradlew spotlessApply spotlessCheck` and `openspec validate --all` pass; the manual 120-character check is
      clean over the changed set (the change dir is untracked, so the set comes from
      `git status --porcelain --untracked-files=all`, not `git diff --name-only`). **Done** — `spotlessCheck` exit 0;
      `openspec validate --all` 27/27 and `openspec validate widen-audit-agents-to-authoring-gaps` valid; the 120-check
      reports no line over 120 (the formatter-owned `README.md`/`design.md` tables and the convention-exempt
      `docs/review-rounds.tsv` rows are the only long ones, and the audit's verdict template was split to fit).
- [x] 3.2 Run the `review-quick` subagent over the planning artifacts, then the implementation, repeating until it
      reports no new observations; record each round in `docs/review-rounds.tsv`. **Done** — 7 rounds, one reviewer
      session continued across them; the loop closed on a round reporting nothing remains; the per-round record is in
      `docs/review-rounds.tsv`.
- [x] 3.3 Smoke-run the edited definition after a reload, with a control: a recurring defect seeded across the artifacts
      one mechanism produces must yield an authoring-system gap naming that mechanism, and a one-off defect in a single
      artifact must **not** (it stays a consistency finding). A definition edit is not exercised until it is picked up
      (the subagent-reload gotcha), so this gates the archive. **Done** — the post-reload run applied the new class: the
      recurring defect (three files authored from one skill's template) was reported as an authoring-system gap naming
      the skill's `SKILL.md` and routing the correction directly, and the one-off defect (a single `AGENTS.md` entry)
      was reported as a `structural` consistency finding, not a gap, with the boundary test named; the verdict line read
      `1 finding (0 merge, 0 removal, 0 route), 0 advisory, 0 accreted, 1 authoring-system gap`. An in-session run
      before the restart showed the same behaviour, but the auditor reads its own definition from disk, so only the
      post-reload run is definitive.
- [x] 3.4 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. **Also report** any rule this change obsoletes or makes redundant. **Done** — the
      capture's verdict: 3 durable proposals (2 additions, 1 retirement), 1 authoring-system recommendation, proposed
      net +4 `AGENTS.md` lines. Both additions merge into the "recurring unit type" bullet; the retirement deletes the
      verified-false "(the swept-surface set is enumerated in exactly those four places)" count from the "the only X"
      bullet (the bullet's own next sentence names six grep locations, and README's auditor prose is a fifth sweep
      site). The applied net delta is +3 lines (`git diff -U0 AGENTS.md`: the merge hunk `@@ -1665,6 +1667,10 @@` and
      the retirement hunk `@@ -3509,9 +3515,8 @@`) — the second merged clause was reduced to a pointer at the "the only
      X" bullet's member grep on review, since that bullet already carries the remedy. The recommendation (a
      single-sourcing clause for `openspec/config.yaml`'s `rules.tasks`) is parked in `docs/ideas.md`.
- [x] 3.5 Ask the user for the manual review pass once 3.2 is clean and 3.4 is done; do not commit or archive before
      they approve. **Asked** — the request is in the session; the outcome is the owner's.

## 4. The change's own records

- [x] 4.1 Record the pre-write derivation pass (every unit owes it): the claims table is in `design.md`, its rows
      derived from the pre-change definition and spec, the capture change it complements, and the describing sites the
      design's sweep names, with the `scripts/claim-scan.py` candidates (base `origin/main`) each resolved into a row or
      dismissed beside the table. **Done** — the pass ran; every scan candidate is a row or a listed dismissal.
- [x] 4.2 Append this unit's review-round rows to `docs/review-rounds.tsv` as each round's report arrives. **Done** —
      each round's row appended as its report arrived.
- [x] 4.3 **Owed — for the archive commit**: the `agent-skills` `## Purpose` enumerates the auditor's reported classes
      ("the meta rules it reports with their origins, and the merge, removal, and route candidates it reports") and now
      omits the authoring-system gap class. A delta cannot carry a Purpose, so the edit lands with the archive. **Done** —
      the Purpose now names "the authoring-system gaps it traces to a mechanism that produces an artifact".

## Workflow follow-up

- Archive the change after the owner approves and 3.3 has run; the archive commit rides this PR.
