# Tasks

## 1. Spec delta

- [x] 1.1 `openspec/changes/sharpen-review-quick/specs/showcase/quality/agent-skills/spec.md` — one `MODIFIED` block for
      "Per-change quality-gate and analysis subagents are available": the requirement body and its ten scenarios are
      copied verbatim, and the "Quick review runs after proposal and implementation" scenario gains the checklists
      (proposal, implementation, conventions), the per-class evidence requirements, the prioritization order, the
      finding shape, and the moved-or-rewritten recurrence clause. **Done.**

## 2. Implementation

- [x] 2.1 `.opencode/agent/review-quick.md` — the same in the definition's own words: the checklists, the per-class
      evidence requirements, the prioritization order, the finding shape (class, location, issue, evidence, correction),
      and the moved-defect recurrence clause. **Done.**

## 3. Verification

- [x] 3.1 `./gradlew spotlessApply spotlessCheck` and `openspec validate --all` pass; the manual 120-character check is
      clean over the changed set (the change dir is untracked, so the set comes from
      `git status --porcelain --untracked-files=all`, not `git diff --name-only`). **Done** — `spotlessCheck` exit 0;
      `openspec validate --all` 27/27 and `openspec validate sharpen-review-quick` valid; the >120 lines are the
      formatter-owned `design.md` claims-table rows and the convention-exempt one-line `docs/review-rounds.tsv` rows.
- [x] 3.2 Run the `review-quick` subagent over the planning artifacts, then the implementation, repeating until it
      reports no new observations; record each round in `docs/review-rounds.tsv`. **Done** — the per-round record is in
      `docs/review-rounds.tsv`.
- [x] 3.3 Smoke-run the edited definition after a reload, with a control: a synthetic surface with (a) a test whose
      assertion would still pass with the behavior broken, (b) a finding stated without the evidence its class needs,
      and (c) a moved defect a prior round already raised must yield a `test-control` finding for (a), the missing
      evidence named for (b), and the moved defect marked as a recurrence for (c). A definition edit is not exercised
      until it is picked up (the subagent-reload gotcha), so this gates the archive. **Done** — after a reload, the run
      returned (a) `test-control`, (b) `claim-unreproducible` with the missing evidence named, (c) a recurrence with the
      root-cause advice, matching the three controls.
- [x] 3.4 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. **Also report** any rule this change obsoletes or makes redundant. **Done** — the
      capture returned 2 durable proposals (1 addition, 1 retirement) and 1 authoring-system recommendation: it refined
      the pre-write pass's count clause (name the instances it covers where the class has no greppable predicate),
      retired the review-gate bullet's partial copy of the review-quick's spec'd surface to a pointer, and routed the
      count clause to `openspec/config.yaml`'s `rules.design`. The applied net delta is 0 `AGENTS.md` lines
      (`git diff -U0 origin/main -- AGENTS.md`: the count clause +2, the retirement −2). **No further retirements.**
- [x] 3.5 Ask the user for the manual review pass once 3.2 is clean and 3.4 is done; do not commit or archive before
      they approve. **Done** — requested and approved.

## 4. The change's own records

- [x] 4.1 Record the pre-write derivation pass (every unit owes it): the claims table is in `design.md`, its rows
      derived from the pre-change definition and spec, the archive, and the `## Purpose`, with the
      `scripts/claim-scan.py` candidates (base `origin/main`) each resolved into a row or dismissed beside the table.
      **Done** — the pass ran; the scan candidates (absolute/count/cross-ref/path) are each a row or a listed dismissal.
- [x] 4.2 Append this unit's review-round rows to `docs/review-rounds.tsv` as each round's report arrives. **Done** —
      each round's row appended as its report arrived.
