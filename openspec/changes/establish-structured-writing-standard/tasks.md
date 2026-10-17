# Tasks

## 1. The authoritative standard

- [x] 1.1 `AGENTS.md` — add a `## Structured Writing Standard` section (after `## Project Overview`, before
      `## OpenSpec Workflow Agreement`) that is the standard's one authoritative definition: the shape every artifact's
      content takes, how it is applied while drafting, and how it is checked before finishing. **Done** — the section is
      at `AGENTS.md` (the `## Structured Writing Standard` block before `## OpenSpec Workflow Agreement`).

## 2. OpenSpec integration

- [x] 2.1 `openspec/config.yaml` — add one line to each artifact's `rules` (`proposal`, `specs`, `design`, `tasks`) and
      to `operations.apply.guidance` referencing `AGENTS.md`'s "Structured Writing Standard" (not restating it).
      **Done** — verified by 4.2.
- [x] 2.2 `openspec/changes/establish-structured-writing-standard/specs/showcase/quality/agent-skills/spec.md` — the
      `ADDED` requirement "Agent-authored technical artifacts follow a structured writing standard" and its six
      scenarios. **Done** — written at the proposal; `openspec validate` reports it valid with no warning.

## 3. Enforcement on review and audit

- [x] 3.1 `.opencode/agent/review-quick.md` — add the writing-standard conformance check to its verification list.
      **Done.**
- [x] 3.2 `.opencode/agent/review-thorough.md` — add the writing-standard check to its conventions. **Done.**
- [x] 3.3 `.opencode/agent/agents-auditor.md` — extend the consistency axis to report repo-authored prose that violates
      the standard. **Done.**
- [x] 3.4 `README.md` — the Self-Learning Loop's enumeration of the audit's consistency sub-checks names the
      writing-standard check. **Done.**
- [x] 3.5 `.opencode/agent/review-quick.md` — add the `structured-writing` class to the canonical finding list (a
      writing-standard violation fit no existing class), per the review contract's "the calling agent adds a class in
      the same change". **Done.**

## 4. Verification

- [x] 4.1 `./gradlew spotlessApply spotlessCheck` and `openspec validate --all` pass; the 120-character check over the
      changed set is clean. **Done** — `spotlessCheck` clean, `openspec validate --all` 27 passed / 0 failed,
      `openspec validate establish-structured-writing-standard` valid with no warning, config.yaml ≤120.
- [x] 4.2 **Verify enforcement reaches the artifact-creation path**: run
      `openspec instructions proposal|specs|design|tasks     --change establish-structured-writing-standard --json` and
      `openspec instructions apply --change … --json`, and confirm each `rules` list and the operation `guidance` carry
      the writing-standard reference. **Done** — all five surfaces (proposal, specs, design, tasks rules; apply
      guidance) returned the reference; the CLI emitted neither a "must be an array of strings" nor a "could not parse"
      warning. Consistent enforcement could **not** be established for the generator-owned CLI schema templates
      (`@fission-ai/openspec/schemas/spec-driven/`) and the generated `opsx-*`/`openspec-*` files, which
      `openspec update` overwrites — the standard reaches those artifacts only through `AGENTS.md` (always loaded) and
      the config rules.
- [x] 4.3 Run the `review-quick` subagent over the planning artifacts, then the implementation, repeating until it
      reports no new observations; append each round's row to `docs/review-rounds.tsv` as its report arrives (the ledger
      owns the per-round state; this task carries no round list). **Done** — six rounds, the sixth clean (rows 1–6 in
      `docs/review-rounds.tsv`).
- [x] 4.4 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta. **Also report** any rule this change obsoletes or makes redundant. **Done** — one durable
      proposal (0 additions, 0 retirements; an in-place widening of the "Single-source a claim one source owns" bullet
      to cover a rule set and a ledger-owned record), applied net **+2** lines (the bullet's 5→7 lines); the change's
      whole `AGENTS.md` delta is `+62/−5` (`git diff --numstat AGENTS.md`). No retirements were warranted.
- [x] 4.5 Ask the user for the manual review pass once 4.3 is clean and 4.4 is done; do not commit or archive before
      they approve. **Done** — the owner approved.
- [ ] 4.6 Smoke-run each edited definition after a reload (`review-quick`, `review-thorough`, `agents-auditor`), each
      with a known-dense passage it must flag and a conforming one it must clear — a precondition for archiving, not
      deferred across the merge (the `AGENTS.md` subagent-reload gotcha).

## 5. The change's own records

- [x] 5.1 Record the pre-write derivation pass: the claims table is in `design.md`, its rows derived from the corpus and
      the deriving commands, with the `scripts/claim-scan.py` candidates (base `origin/main`) each resolved into a row
      or dismissed beside the table. **Done.**
- [x] 5.2 Append each review round's row to `docs/review-rounds.tsv` as its report arrives (the ledger is the record;
      this task carries no per-round list). **Done** — rounds 1–6 appended.
- [ ] 5.3 **Owed — for the archive commit**: refresh the `agent-skills` `## Purpose` to name the structured writing
      standard (a delta cannot carry a Purpose), so the capability's Purpose matches what it now holds.

## Workflow follow-up

- Archive the change after the owner approves and 4.6 has run; the archive commit rides this PR and carries the task-5.3
  `agent-skills` `## Purpose` refresh.
