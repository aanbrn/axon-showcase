# Tasks

## 1. Spec delta

- [x] 1.1 `openspec/changes/require-evidence-in-report-contracts/specs/showcase/quality/agent-skills/spec.md` — eight
      `MODIFIED` blocks, the complete set derived from the shared contract's own scope (its bearers crossed with its
      item classes) rather than from the artifacts in hand: "Per-change quality-gate and analysis subagents are
      available" (18 scenarios), "The spec corpus is audited for structure and consistency" (10), "Project-owned agent
      tooling is audited for consistency and conciseness" (29), "The architecture is audited for drift from its recorded
      decisions" (10), "Unexplained design intent is surfaced for clarification" (9), "The README is audited for
      accuracy, design intent, and experience coverage" (5), "AGENTS.md entries follow a stated authoring standard"
      (11), and "Agent-authored technical artifacts follow a structured writing standard" (6). Each is a full copy of
      the current requirement plus one sentence: every item the report carries also carries the shared report contract's
      per-item budget, which that requirement owns, not this one. The per-item sentences stay as they are, so no
      scenario is added and the total stays 98. Verification: `openspec validate --changes` passes, and a block-diff
      against `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md` shows each block differing only by
      the pointer sentence. **Done** — eight blocks, each the requirement plus one pointer sentence; per-block scenario
      counts 18, 10, 29, 10, 9, 5, 11, 6 (total 98, unchanged); each block's diff is 3 lines;
      `openspec validate --changes` passes.

## 2. Implementation

- [x] 2.1 `.opencode/agent/readme-auditor.md` — the report contract's per-item budget references the shared contract's
      per-item budget by name (`showcase/quality/agent-skills`, _Each item is budgeted_) and the class clauses list only
      what each class adds. Verification: the budget names the contract and no site restates its field set. **Done** —
      the budget names the contract (`showcase/quality/agent-skills`, _Each item is budgeted_) and the classes list only
      their own fields.
- [x] 2.2 `.opencode/agent/specs-auditor.md` — the same, in both its findings list and its budget line. **Done** — both
      the findings list and the budget line delegate to the contract.
- [x] 2.3 `.opencode/agent/agents-auditor.md` — the same; its route-candidate clause reverts to its class-specific
      fields, since the reduction-candidate plan already carries the evidence. **Done** — the budget delegates; the
      route-candidate clause reverted to its class-specific fields.
- [x] 2.4 `.opencode/agent/review-thorough.md` — the same. **Done** — the budget delegates.
- [x] 2.5 `.opencode/agent/architecture-auditor.md` — the same, in both its budget line and its intent-item clause.
      **Done** — both the budget line and the intent-item clause delegate.
- [x] 2.6 `.opencode/agent/lesson-capture.md` — the same, in its budget line, its retirement clause, and its
      authoring-system-recommendation clause. **Done** — the budget line, the retirement clause, and the recommendation
      clause delegate.
- [x] 2.7 `.opencode/agent/review-quick.md` — the same. **Done** — the budget delegates.
- [x] 2.8 The five commands — `.opencode/commands/audit-readme.md`, `audit-specs.md`, `audit-agents.md`,
      `review-thorough.md`, `audit-architecture.md` — each presentation step presents the item as the shared report
      contract requires rather than naming a field. **Done** — all five delegate by name (verified by grep).
- [x] 2.9 `AGENTS.md` — the `review-thorough` bullet's parenthetical, which enumerated two report fields and read as
      complete, becomes a pointer to the contract. **Done** — the bullet points at the contract instead of enumerating
      two of its fields.
- [x] 2.10 `README.md` — the two prose sites that state a per-item report format (the `specs-auditor` paragraph and the
      `/audit-agents` paragraph) name the evidence line. **Done** — both README prose sites name the evidence line
      (verified against the pre-change README).
- [x] 2.11 Verify the describing sites the change does **not** touch and record the reason: the README agent-table rows
      and slash-command rows; the four auditor `AGENTS.md` bullets; and `openspec/config.yaml`. Verification: grep the
      seven subagent names and the report-field terms over those files and confirm every hit describes what a subagent
      audits, not its per-item report fields. **Done** — the grep returns no hit pairing a subagent name with a
      report-field term outside the two README sites.

## 3. Verification

- [x] 3.1 `./gradlew spotlessApply` after the final edit to a Spotless-owned file, then `spotlessCheck` green;
      `openspec validate --all` and `openspec validate --changes` pass; the 120-character recipe applied over the
      changed set derived from `git status --porcelain --untracked-files=all`. **Done** — `spotlessCheck` exit 0;
      `openspec validate --all` 27/27 and `--changes` 1/1; no added line exceeds the limit — the flagged lines are the
      change dir's markdown table rows and the `docs/review-rounds.tsv` record rows, both formatter-exempt, and the deep
      review's finding that the `openspec/config.yaml` item was unwrapped at 122 is fixed.
- [x] 3.2 Run the `review-quick` subagent over the planning artifacts, then over the implementation, repeating until it
      reports no new observations; append each round's record line to `docs/review-rounds.tsv` as its report arrives,
      and record the deep-review routing decision (task 3.5) on this task. **Done** — the loop ran 14 rounds in session
      `ses_eb1454c7dffeymi3FJdqZl4zRo`, clean at round 14 (`nothing remains`); rounds 7–13, after the redesign, found
      only propagation and record defects. **Routing decision (task 3.5): `RECOMMENDED`** on the `repeated-class` signal
      — `scripts/review-routing.py` reports 8 rows repeating a prior class — resolved by the owner to run. **Deep review
      (session `ses_eb0f275a7ffe1Oa0Cw4sbtKj4Y`), pass 1 of 2: 3 findings** — `should-fix`: the new
      `openspec/config.yaml` item was unwrapped at 122 characters (`FIXED_AND_VERIFIED`); `nitpick`: `design.md`'s Risk
      mitigation over-claimed that every reference names the scenario (`FIXED_AND_VERIFIED`); `nitpick`:
      `architecture-auditor.md`'s intent-item clause had dropped the spec's verb "state" (`FIXED_AND_VERIFIED`). **Pass
      2: `nothing remains`** — all three fixes verified against the current files, no new finding, so the deep review
      completes within its two-pass budget.
- [x] 3.3 Smoke-run each of the seven edited definitions after an OpenCode reload, with a control: a surface yielding
      one known finding must return a report whose item carries the shape the shared report contract delegates (it names
      the finding, carries its `file:line` anchor where applicable, and carries one line of evidence), and a surface
      with nothing wrong must return the one-line clean verdict. Gates the archive, because a definition edit is served
      from its pre-change registration until a reload. **Done** — all seven ran after the reload, and each read the
      shared contract and named its per-item shape: `agents-auditor` reported a live finding with the anchor and
      evidence line (the parked `testReviewRouting`/`review-routing.py` enumeration gap); `lesson-capture` reported a
      retirement and an authoring-system recommendation, both already realized; `review-quick` reviewed a supplied
      fragment and raised a `test-control` finding (this task's control under-specified the item's shape — corrected
      here); `review-thorough` returned `nothing remains` on the delta's scenario preservation; `readme-auditor`,
      `specs-auditor`, and `architecture-auditor` returned `nothing to report` — each refusing to fabricate when its
      seeded defect did not reproduce, and each confirming it did not report the surface named as deliberate.
- [x] 3.4 Run the `lesson-capture` subagent over the diff and apply its durable proposals, recording the applied net
      `AGENTS.md` delta or the skip and its reason on this task; report any rule the change obsoletes. **Done** — 1
      durable addition, 1 retirement, 1 authoring-system recommendation. Applied: the single-sourcing bullet gains the
      field-set class and the drift signal (a loop finding another site of one set single-sources it rather than
      sweeping, deriving the sites as the owner's `bearer × item-class`; a field-list grep is a lead); the Conventions
      clause's duplicate statement becomes a pointer to that rule; and `openspec/config.yaml`'s `rules.specs` gains the
      single-sourcing item the other three artifacts already carry. Applied net `AGENTS.md` delta: **+5 lines**
      (`git diff --numstat AGENTS.md` → +17/−12). No rule is obsoleted beyond the consolidation.
- [x] 3.5 Carry the deep-review routing decision: derive the floor with
      `python3 scripts/review-routing.py --change require-evidence-in-report-contracts`, decide at or above it, and
      record the outcome and the signals it relied on on task 3.2.
- [x] 3.6 Ask the user for the manual review pass once 3.2 is clean and 3.4 is done; do not commit, push, archive, or
      merge before they approve, and do not apply any audit finding to `README.md`. **Done** — requested and approved;
      CI ran green on the implementation commit, the change is archived, and no audit finding was applied to
      `README.md`.

## 4. The change's own records

- [x] 4.1 Record the pre-write derivation pass: the claims table is in `design.md`, with the `scripts/claim-scan.py`
      candidates (base `origin/main`) each resolved into a row or recorded as a dismissal beside the table. **Done** —
      the claims table is in `design.md` with five by-class dismissals of the scan's candidates.
- [x] 4.2 Append this unit's review-round rows to `docs/review-rounds.tsv` as each round's report arrives. **Done** —
      rounds 1–14 are recorded, appended as each round's report arrived.
